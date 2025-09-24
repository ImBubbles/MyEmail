package dev.haydenholmes.network;

import dev.haydenholmes.MyEmail;
import dev.haydenholmes.email.Email;
import dev.haydenholmes.email.SMTPHandler;
import dev.haydenholmes.email.Recipient;
import dev.haydenholmes.log.Logger;
import dev.haydenholmes.network.protocol.Code;
import dev.haydenholmes.network.protocol.auth.AuthPlainRequest;
import dev.haydenholmes.network.protocol.ready.ReadySMTPS;
import dev.haydenholmes.util.StringCast;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

public final class ServerConnection {

    private BufferedReader in;
    private PrintWriter out;
    private Socket clientSocket;
    private Code.SEQUENCE_STATE awaiting;
    private Email email;
    private final boolean relayServer;

    public ServerConnection(Socket clientSocket) {
        this(clientSocket, false);
    }

    public ServerConnection(Socket clientSocket, boolean relayServer) {

        this.clientSocket = clientSocket;
        this.awaiting = Code.SEQUENCE_STATE.HELO;
        this.email = new Email();
        this.relayServer = relayServer;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println(ReadySMTPS.acceptance());

            String line;
            while((line = in.readLine()) != null) {
                Logger.debug("Received from " + clientSocket.getInetAddress() + ": " + line);
                // Ignore http
                if(line.contains("HTTP")) {
                    continue;
                }
                String command = line.toUpperCase(Locale.ROOT).split(" ")[0];
                Logger.debug("Trying to test command " + command);
                if(command.equals(Code.SMTP_COMMANDS.EHLO.value())) {
                    handleEhloHelo(line);
                }
                else if(command.equals(Code.SMTP_COMMANDS.MAIL.value())) {
                    handleMailFrom(line);
                }
                else if(command.equals(Code.SMTP_COMMANDS.STARTTLS.value())) {
                    handleStartTLS();
                }
                else if(command.equals(Code.SMTP_COMMANDS.AUTH.value())) {
                    if(relayServer)
                        handleAuth();
                }
                else if(command.equals(Code.SMTP_COMMANDS.RCPT.value())) {
                    handleRcptTo(line);
                }
                else if(command.equals(Code.SMTP_COMMANDS.DATA.value())) {
                    handleData();
                }
                else if(command.equals(Code.SMTP_COMMANDS.RSET.value())) {
                    handleRset();
                }
                else if(command.equals(Code.SMTP_COMMANDS.QUIT.value())) {
                    handleQuit();
                    break;
                } else {
                    out.println(ReadySMTPS.badCommand());
                }
            }
        } catch (IOException e) {
            Logger.exception(e);
        } finally {
            try {
                if(in != null)
                    in.close();
                if(out != null)
                    out.close();
                if(clientSocket != null)
                    clientSocket.close();
            } catch (IOException e) {
                Logger.exception(e);
            }
        }
    }

    private void handleEhloHelo(String line) {
        // A simple check to ensure this is the first command.
        if (awaiting != Code.SEQUENCE_STATE.HELO) {
            out.println(ReadySMTPS.badSequence());
            return;
        }

        // Extract the domain name.
        String[] parts = line.split(" ");
        if (parts.length < 2) {
            out.println(ReadySMTPS.badSyntax());
            return;
        }

        // Respond with a success code and supported ESMTP extensions.
        out.println(ReadySMTPS.advertiseHello(parts[1]));
        out.println(ReadySMTPS.advertiseSize());
        if(relayServer)
            out.println(ReadySMTPS.advertiseAuth());
        //out.println(ReadySMTPS.advertisePipelining());
        out.println(ReadySMTPS.advertise8BITMIME());
        //out.println(ReadySMTPS.advertiseHelp());
        if(!MyEmail.properties.PKCS12_PATH().isEmpty())
            out.println(ReadySMTPS.advertiseTLS());
        out.println(ReadySMTPS.acknowledge());

        if(!relayServer)
            awaiting = Code.SEQUENCE_STATE.MAIL_FROM;
        else
            awaiting = Code.SEQUENCE_STATE.AUTH;
    }

    private void handleStartTLS() {

        // Check if enabled (shouldn't be here if not but just in case)
        if(MyEmail.properties.PKCS12_PATH().isEmpty()) {
            out.println(ReadySMTPS.badCommand());
            return;
        }

        // Check if keystore file exists
        File file = new File(MyEmail.properties.PKCS12_PATH());
        boolean exists = file.exists() && file.isFile();
        if(!exists) {
            out.println(ReadySMTPS.badCommand());
            return;
        }

        // Finally check sequence

        if(!(awaiting == Code.SEQUENCE_STATE.HELO || awaiting == Code.SEQUENCE_STATE.MAIL_FROM)) {
            out.println(ReadySMTPS.badSequence());
            return;
        }

        try {
            out.println(ReadySMTPS.startTLSReady());

            char[] password = MyEmail.properties.PKCS12_PASSWORD().toCharArray();

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(file), password);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, password);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // Create the new SSLSocket from the existing client socket.
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(
                    clientSocket,
                    clientSocket.getInetAddress().getHostAddress(),
                    clientSocket.getPort(),
                    true
            );

            // bro please fking work
            sslSocket.setUseClientMode(false);

            sslSocket.startHandshake();

            this.in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            this.out = new PrintWriter(sslSocket.getOutputStream(), true);
            this.clientSocket = sslSocket;

            this.awaiting = Code.SEQUENCE_STATE.HELO;
        } catch (Exception e) {
            Logger.error("Error during TLS handshake");
            Logger.exception(e);
            out.println(ReadySMTPS.transactionFailed());
            this.awaiting = Code.SEQUENCE_STATE.HELO;
        }
    }

    private void handleMailFrom(String line) {
        if(awaiting != Code.SEQUENCE_STATE.MAIL_FROM) {
            out.println(ReadySMTPS.badSequence());
            return;
        }

        // Expecting MAIL FROM:<address>
        // OR something like MAIL FROM:<user@example.com> [SIZE=12345] [BODY=8BITMIME] [SMTPUTF8]
        // Do some simple string validation of this
        // Because this is meant to be an API, sort of has to be kept broad/simple so sorry anyone who breaks their keyboard over this if something happens in prod

        if(!line.startsWith("MAIL FROM:")) {
            out.println(ReadySMTPS.badSyntax());
            return;
        }

        String remainder = line.substring(line.indexOf(":")+1).trim();

        if(remainder.isEmpty()) {
            out.println(ReadySMTPS.badSyntax());
            return;
        }

        int lt = remainder.indexOf('<');
        int gt = remainder.indexOf('>');
        if (lt == -1 || gt == -1 || gt < lt) {
            out.println(ReadySMTPS.badSyntax());
            return;
        }
        String address = remainder.substring(lt, gt + 1);

        if(!(Email.validateAddressString(address))) {
            out.println(ReadySMTPS.badSyntax());
            return;
        }

        // Get just the address

        address = Email.trimAddress(address);

        this.email.setSender(address);

        // Check if address is a relay
        boolean relayAddress = SMTPHandler.isRelay(address);
        if(!relayServer && relayAddress) {
            out.println(ReadySMTPS.relayNotAllowed());
            return;
        }
        if(relayServer && !relayAddress) {
            out.println(ReadySMTPS.relayOnly());
            return;
        }

        remainder = remainder.substring(remainder.indexOf(">")).trim();

        if(!remainder.isEmpty()) {
            String[] params = remainder.split("\\s+");
            for(String param : params) {
                String p = param.toUpperCase(Locale.ROOT);
                // Remove brackets
                if(p.startsWith("[") && p.endsWith("]")) {
                    p = p.substring(1, p.length() - 1).trim();
                }
                // Handle
                if(p.startsWith("SIZE=")) {
                    Integer size = StringCast.toInteger(p.substring(5));
                    if(size==null) {
                        out.println(ReadySMTPS.badSyntax());
                        return;
                    }
                    email.setSize(size);
                } else if(p.startsWith("BODY=")) {
                    String body = p.substring(5);
                    Code.SMTP_BODY type = Code.SMTP_BODY.parseString(body);
                    if(type==null) {
                        out.println(ReadySMTPS.badSyntax());
                        return;
                    }
                    email.setBodyType(type);
                } else if(p.equals("SMTPUTF8")) {
                    email.setSMTPUTF8(true);
                } else {
                    Logger.debug("Unknown \"MAIL FROM\" parameter: " + param);
                }
            }
        }

        out.println(ReadySMTPS.acknowledge());
        awaiting = Code.SEQUENCE_STATE.RCPT_TO;

    }

    private void handleRcptTo(String line) {

        if(awaiting != Code.SEQUENCE_STATE.RCPT_TO) {
            out.println(ReadySMTPS.badSequence());
            return;
        }

        // Expected format is "RCPT TO:<address>" with opt parameters
        if(!line.startsWith("RCPT TO:")) {
            out.println(ReadySMTPS.badSyntax());
            return;
        }

        String remainder = line.substring(line.indexOf(':') + 1).trim();
        if (remainder.isEmpty()) {
            out.println(ReadySMTPS.badSyntax());
            return;
        }

        int lt = remainder.indexOf('<');
        int gt = remainder.indexOf('>');
        if (lt == -1 || gt == -1 || gt < lt) {
            out.println(ReadySMTPS.badSyntax());
            return;
        }
        String address = remainder.substring(lt, gt + 1);

        if(!(Email.validateAddressString(address))) {
            out.println(ReadySMTPS.badSyntax());
            return;
        }

        address = Email.trimAddress(address);

        remainder = remainder.substring(remainder.indexOf(">")).trim();

        Set<Code.SMTP_NOTIFY> notifySet = null;
        Code.SMTP_ORCPT orcpt = null;
        String originalEmail = "";
        if(!remainder.isEmpty()) {
            String[] params = remainder.split("\\s+");
            for (String param : params) {
                String p = param.toUpperCase(Locale.ROOT).trim();

                // Remove brackets if present
                if (p.startsWith("[") && p.endsWith("]")) {
                    p = p.substring(1, p.length() - 1).trim();
                }

                // Handle known parameters
                if (p.startsWith("NOTIFY=")) {
                    notifySet = Code.SMTP_NOTIFY.parseString(p.substring(7));
                } else if (p.startsWith("ORCPT=")) {
                    String val = p.substring(6);
                    if(!val.contains(":")) {
                        out.println(ReadySMTPS.badSyntax());
                        return;
                    }
                    String[] regex = val.split(";", 2);
                    if(regex.length==1) {
                        out.println(ReadySMTPS.badSyntax());
                        return;
                    }
                    String rawORCPT = regex[0].trim();
                    orcpt = Code.SMTP_ORCPT.parseString(rawORCPT);
                    originalEmail = Email.trimAddress(regex[1]);
                    if(orcpt==null) {
                        out.println(ReadySMTPS.badSyntax());
                        return;
                    }
                } else {
                    Logger.debug("Unknown RCPT TO parameter: " + param);
                }
            }
        }

        if(notifySet!=null && notifySet.isEmpty()) {
            out.println(ReadySMTPS.badSyntax());
            return;
        }

        email.addRecipient(new Recipient(address, false, notifySet, orcpt, originalEmail));

        out.println(ReadySMTPS.acknowledge());

    }

    private void handleData() {
        if (awaiting != Code.SEQUENCE_STATE.RCPT_TO) {
            out.println(ReadySMTPS.badSequence());
            return;
        }

        out.println(ReadySMTPS.startMail());

        awaiting = Code.SEQUENCE_STATE.DATA;

        StringBuilder rawMessage = new StringBuilder();
        Map<String, String> headers = new LinkedHashMap<>();
        StringBuilder body = new StringBuilder();

        boolean inHeaders = true;

        try {
            String line;
            while ((line = in.readLine()) != null) {
                // End of DATA
                if (line.equals(".")) break;

                // Dot-stuffing: remove leading dot if present
                if (line.startsWith("..")) {
                    line = line.substring(1);
                }

                // Normalize line endings
                line = line.replaceAll("\\r?\\n", "") + "\r\n";

                rawMessage.append(line);

                if (inHeaders) {
                    if (line.trim().isEmpty()) {
                        // Empty line separates headers from body
                        inHeaders = false;
                        continue;
                    }

                    // Simple header parsing: "Key: Value"
                    int colonIndex = line.indexOf(':');
                    if (colonIndex > 0) {
                        String key = line.substring(0, colonIndex).trim();
                        String value = line.substring(colonIndex + 1).trim();
                        headers.put(key, value);
                    }
                } else {
                    body.append(line);
                }
            }

            // Store full message
            email.setMessage(rawMessage.toString());
            email.setHeaders(headers);
            email.setBody(body.toString());

            // Optional debug logging
            if (Logger.getFilter() <= Logger.LogLevel.DEBUG.getWeight()) {
                String recipientsString = email.getRecipients().stream()
                        .map(Recipient::toString)
                        .collect(Collectors.joining(", "));
                Logger.debug("Received full email from " + email.getSender() + " for recipients " + recipientsString);
                Logger.debug("Headers:\n" + headers.toString());
                Logger.debug("Body:\n" + email.getBody());
            }

            SMTPHandler.handleEmail(email);

            out.println(ReadySMTPS.acknowledge());
            awaiting = Code.SEQUENCE_STATE.HELO;

        } catch (IOException e) {
            Logger.exception(e);
            out.println(ReadySMTPS.transactionFailed());
            awaiting = Code.SEQUENCE_STATE.HELO;

            // Discard partial email content
            this.email = new Email();
        }
    }

    private void handleAuth() {

        if(awaiting != Code.SEQUENCE_STATE.AUTH) {
            out.println(ReadySMTPS.badSequence());
            return;
        }

        if(!(clientSocket instanceof SSLSocket)) {
            out.println(ReadySMTPS.tlsRequired());
            return;
        }

        String username;
        String password;
        try {
            out.println(ReadySMTPS.username64());
            username = in.readLine();

            out.println(ReadySMTPS.password64());
            password = in.readLine();
        } catch (IOException e) {
            Logger.exception(e);
            return;
        }

        AuthPlainRequest apr = new AuthPlainRequest(
                StringCast.fromBase64(username),
                StringCast.fromBase64(password)
        );

        boolean authed = SMTPHandler.handleAuth(apr);
        if(!authed) {
            out.println(ReadySMTPS.authFailed());
            return;
        }

        out.println(ReadySMTPS.authSuccess());

    }

    private void handleRset() {
        this.awaiting = Code.SEQUENCE_STATE.HELO;
        this.email = new Email();
        out.println(ReadySMTPS.acknowledge());
    }

    private void handleQuit() {
        out.println(ReadySMTPS.bye());
    }

}
