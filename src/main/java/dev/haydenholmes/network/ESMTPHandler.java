package dev.haydenholmes.network;

import dev.haydenholmes.MyEmail;
import dev.haydenholmes.email.Email;
import dev.haydenholmes.email.Recipient;
import dev.haydenholmes.log.Logger;
import dev.haydenholmes.network.response.Code;
import dev.haydenholmes.network.response.ready.ReadyESMTP;
import dev.haydenholmes.util.StringCast;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ESMTPHandler {

    private enum State {
        HELO, // Initial state, expecting HELO or EHLO
        MAIL_FROM, // After HELO/EHLO, expecting MAIL FROM
        RCPT_TO; // After MAIL FROM, expecting RCPT TO or another RCPT TO
        //DATA, // Because while SMTP does not have any end identifier for the last RCPT_TO transmission
        // server must be prepared to accept both RCPT_TO or DATA, making this state arbitrary
    }

    private BufferedReader in;
    private PrintWriter out;
    private Socket clientSocket;

    private State awaiting;

    private Email email;

    public ESMTPHandler(Socket clientSocket) {

        this.clientSocket = clientSocket;
        this.awaiting = State.HELO;
        this.email = new Email();

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println(ReadyESMTP.acceptance());

            String line;
            while((line = in.readLine()) != null) {
                Logger.debug("Received from " + clientSocket.getInetAddress() + ": " + line);
                // Ignore http
                if(line.contains("HTTP")) {
                    continue;
                }
                String command = line.toUpperCase(Locale.ROOT).split(" ")[0];
                Logger.debug("Trying to test command " + command);
                if(command.equals(Code.ESMTP_COMMANDS.EHLO.value())) {
                    handleEhloHelo(line);
                }
                else if(command.equals(Code.ESMTP_COMMANDS.MAIL_FROM.value())) {
                    handleMailFrom(line);
                }
                else if(command.equals(Code.ESMTP_COMMANDS.STARTTLS.value())) {
                    handleStartTLS();
                }
                else if(command.equals(Code.ESMTP_COMMANDS.RCPT_TO.value())) {
                    handleRcptTo(line);
                }
                else if(command.equals(Code.ESMTP_COMMANDS.DATA.value())) {
                    handleData();
                }
                else if(command.equals(Code.ESMTP_COMMANDS.RSET.value())) {
                    handleRset();
                }
                else if(command.equals(Code.ESMTP_COMMANDS.QUIT.value())) {
                    handleQuit();
                    break;
                } else {
                    out.println(ReadyESMTP.badCommand());
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
        if (awaiting != State.HELO) {
            out.println(ReadyESMTP.badSequence());
            return;
        }

        // Extract the domain name.
        String[] parts = line.split(" ");
        if (parts.length < 2) {
            out.println(ReadyESMTP.badSyntax());
            return;
        }

        // Respond with a success code and supported ESMTP extensions.
        out.println(ReadyESMTP.advertiseHello(parts[1]));
        out.println(ReadyESMTP.advertiseSize());
        out.println(ReadyESMTP.advertiseAuth());
        out.println(ReadyESMTP.advertisePipelining());
        out.println(ReadyESMTP.advertise8BITMIME());
        out.println(ReadyESMTP.advertiseHelp());
        if(!MyEmail.properties.PKCS12_PATH().isEmpty())
            out.println(ReadyESMTP.advertiseTLS());
        out.println(ReadyESMTP.acknowledge());

        awaiting = State.MAIL_FROM;
    }

    private void handleStartTLS() {

        // Check if enabled (shouldn't be here if not but just in case)
        if(MyEmail.properties.PKCS12_PATH().isEmpty()) {
            out.println(ReadyESMTP.badCommand());
            return;
        }

        // Check if keystore file exists
        File file = new File(MyEmail.properties.PKCS12_PATH());
        boolean exists = file.exists() && file.isFile();
        if(!exists) {
            out.println(ReadyESMTP.badCommand());
            return;
        }

        // Finally check sequence

        if(!(awaiting == State.HELO || awaiting == State.MAIL_FROM)) {
            out.println(ReadyESMTP.badSequence());
            return;
        }

        try {
            out.println(ReadyESMTP.startTLSReady());

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

            this.awaiting = State.HELO;
        } catch (Exception e) {
            Logger.error("Error during TLS handshake");
            Logger.exception(e);
            out.println(ReadyESMTP.transactionFailed());
            this.awaiting = State.HELO;
        }
    }

    private void handleMailFrom(String line) {
        if(awaiting != State.MAIL_FROM) {
            out.println(ReadyESMTP.badSequence());
            return;
        }

        // Expecting MAIL FROM:<address>
        // OR something like MAIL FROM:<user@example.com> [SIZE=12345] [BODY=8BITMIME] [SMTPUTF8]
        // Do some simple string validation of this
        // Because this is meant to be an API, sort of has to be kept broad/simple so sorry anyone who breaks their keyboard over this if something happens in prod

        if(!line.startsWith("MAIL FROM:")) {
            out.println(ReadyESMTP.badSyntax());
            return;
        }

        String remainder = line.substring(line.indexOf(":")+1).trim();

        if(remainder.isEmpty()) {
            out.println(ReadyESMTP.badSyntax());
            return;
        }

        int lt = remainder.indexOf('<');
        int gt = remainder.indexOf('>');
        if (lt == -1 || gt == -1 || gt < lt) {
            out.println(ReadyESMTP.badSyntax());
            return;
        }
        String address = remainder.substring(lt, gt + 1);

        if(!(Email.validateAddressString(address))) {
            out.println(ReadyESMTP.badSyntax());
            return;
        }

        // Get just the address

        address = Email.trimAddress(address);

        this.email.setSender(address);

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
                        out.println(ReadyESMTP.badSyntax());
                        return;
                    }
                    email.setSize(size);
                } else if(p.startsWith("BODY=")) {
                    String body = p.substring(5);
                    Code.ESMTP_BODY type = Code.ESMTP_BODY.parseString(body);
                    if(type==null) {
                        out.println(ReadyESMTP.badSyntax());
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

        out.println(ReadyESMTP.acknowledge());
        awaiting = State.RCPT_TO;

    }

    private void handleRcptTo(String line) {

        if(awaiting != State.RCPT_TO) {
            out.println(ReadyESMTP.badSequence());
            return;
        }

        // Expected format is "RCPT TO:<address>" with opt parameters
        if(!line.startsWith("RCPT TO:")) {
            out.println(ReadyESMTP.badSyntax());
            return;
        }

        String remainder = line.substring(line.indexOf(':') + 1).trim();
        if (remainder.isEmpty()) {
            out.println(ReadyESMTP.badSyntax());
            return;
        }

        int lt = remainder.indexOf('<');
        int gt = remainder.indexOf('>');
        if (lt == -1 || gt == -1 || gt < lt) {
            out.println(ReadyESMTP.badSyntax());
            return;
        }
        String address = remainder.substring(lt, gt + 1);

        if(!(Email.validateAddressString(address))) {
            out.println(ReadyESMTP.badSyntax());
            return;
        }

        address = Email.trimAddress(address);

        remainder = remainder.substring(remainder.indexOf(">")).trim();

        Set<Code.ESMTP_NOTIFY> notifySet = null;
        Code.ESMTP_ORCPT orcpt = null;
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
                    notifySet = Code.ESMTP_NOTIFY.parseString(p.substring(7));
                } else if (p.startsWith("ORCPT=")) {
                    String val = p.substring(6);
                    if(!val.contains(":")) {
                        out.println(ReadyESMTP.badSyntax());
                        return;
                    }
                    String[] regex = val.split(";", 2);
                    if(regex.length==1) {
                        out.println(ReadyESMTP.badSyntax());
                        return;
                    }
                    String rawORCPT = regex[0].trim();
                    orcpt = Code.ESMTP_ORCPT.parseString(rawORCPT);
                    originalEmail = Email.trimAddress(regex[1]);
                    if(orcpt==null) {
                        out.println(ReadyESMTP.badSyntax());
                        return;
                    }
                } else {
                    Logger.debug("Unknown RCPT TO parameter: " + param);
                }
            }
        }

        if(notifySet!=null && notifySet.isEmpty()) {
            out.println(ReadyESMTP.badSyntax());
            return;
        }

        email.addRecipient(new Recipient(address, false, notifySet, orcpt, originalEmail));

        out.println(ReadyESMTP.acknowledge());

    }

    private void handleData() {
        if (awaiting != State.RCPT_TO) {
            out.println(ReadyESMTP.badSequence());
            return;
        }

        out.println(ReadyESMTP.startMail());

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

            out.println(ReadyESMTP.acknowledge());
            awaiting = State.HELO;

        } catch (IOException e) {
            Logger.exception(e);
            out.println(ReadyESMTP.transactionFailed());
            awaiting = State.HELO;

            // Discard partial email content
            this.email = new Email();
        }
    }

    private void handleRset() {
        this.awaiting = State.HELO;
        this.email = new Email();
        out.println(ReadyESMTP.acknowledge());
    }

    private void handleQuit() {
        out.println(ReadyESMTP.bye());
    }

}
