package dev.haydenholmes.network;

import dev.haydenholmes.MyEmail;
import dev.haydenholmes.email.Email;
import dev.haydenholmes.email.Recipient;
import dev.haydenholmes.log.Logger;
import dev.haydenholmes.network.response.Code;
import dev.haydenholmes.network.response.ready.ReadyESMTP;

import javax.management.MBeanRegistration;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;

public class ESMTPHandler {

    private enum State {
        HELO, // Initial state, expecting HELO or EHLO
        MAIL_FROM, // After HELO/EHLO, expecting MAIL FROM
        RCPT_TO, // After MAIL FROM, expecting RCPT TO or another RCPT TO
        DATA, // After RCPT TO, expecting DATA
        MESSAGE_BODY // In DATA mode, reading the email body
    }

    private BufferedReader in;
    private PrintWriter out;
    private Socket clientSocket;

    private State awaiting;

    private Email email;
    private boolean restartConnection;

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
                String command = line.toUpperCase().split(" ")[0];
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
        // Do some simple string validation of this
        // Because this is meant to be an API, sort of has to be kept broad/simple so sorry anyone who breaks their keyboard over this if something happens in prod

        if(!line.startsWith("MAIL FROM:")) {
            out.println(ReadyESMTP.badSyntax());
            return;
        }

        String[] regex = line.split(":");
        if(regex.length==1) {
            out.println(ReadyESMTP.badSyntax());
            return;
        }

        String address = regex[1];

        if(!(Email.validateEmailString(address))) {
            out.println(ReadyESMTP.badSyntax());
            return;
        }

        // Get just the address

        address = Email.trimEmail(address);

        this.email.setSender(address);

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

        String[] regex = line.split(":");

        if(regex.length==1) {
            out.println(ReadyESMTP.badSyntax());
            return;
        }

        String address = regex[1];

        if(!(Email.validateEmailString(address))) {
            out.println(ReadyESMTP.badSyntax());
            return;
        }

        address = Email.trimEmail(address);

        email.addRecipient(new Recipient(address, false));

        out.println(ReadyESMTP.acknowledge());

    }

    private void handleData() {
        if(awaiting != State.RCPT_TO) {
            out.println(ReadyESMTP.badSequence());
            return;
        }

        out.println(ReadyESMTP.startMail());

        StringBuilder messageBody = new StringBuilder();
        try {
            String dataLine;
            while((dataLine = in.readLine()) != null) {
                if(dataLine.equals(".")) {
                    break;
                }
                messageBody.append(dataLine).append("\n");
            }
            email.setMessage(messageBody.toString());

            if(Logger.getFilter()<=Logger.LogLevel.DEBUG.getWeight()) {
                StringBuilder recipients = new StringBuilder();
                for(Recipient recipient : this.email.getRecipients()) {
                    recipients.append(recipient).append(", ");
                }
                String str = recipients.toString();
                String recipientsString = str.substring(0, str.length()-3);
                Logger.debug("Received full email from " + this.email.getSender() + " for recipients "+ recipientsString);
                Logger.debug("Message body:\n" + email.getMessage());
            }



            out.println(ReadyESMTP.acknowledge());
            awaiting = State.HELO;

        } catch (IOException e) {
            Logger.exception(e);
            out.println(ReadyESMTP.transactionFailed());
            awaiting = State.HELO;
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
