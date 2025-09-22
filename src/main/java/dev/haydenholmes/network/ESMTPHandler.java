package dev.haydenholmes.network;

import dev.haydenholmes.MyEmail;
import dev.haydenholmes.log.Logger;
import dev.haydenholmes.network.response.Code;
import dev.haydenholmes.network.response.ready.ReadyESMTP;

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

    public ESMTPHandler(Socket clientSocket) {

        this.clientSocket = clientSocket;
        this.awaiting = State.HELO;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println(ReadyESMTP.acceptance());
            String line;
            while((line = in.readLine()) != null) {
                Logger.debug("Received from " + clientSocket.getInetAddress() + ": " + line);
                String command = line.toUpperCase().split(" ")[0];
                if(command.equals(Code.ESMTP_COMMANDS.EHLO.value())) {
                    handleEhloHelo(line);
                    continue;
                }
                if(command.equals(Code.ESMTP_COMMANDS.MAIL_FROM.value())) {
                    continue;
                }
                if(command.equals(Code.ESMTP_COMMANDS.STARTTLS.value())) {
                    handleStartTLS();
                    continue;
                }
                if(command.equals(Code.ESMTP_COMMANDS.RCPT_TO.value())) {
                    continue;
                }
                if(command.equals(Code.ESMTP_COMMANDS.DATA.value())) {
                    continue;
                }
                if(command.equals(Code.ESMTP_COMMANDS.QUIT.value())) {
                    break;
                }
                out.println(ReadyESMTP.badCommand());

                in.close();
                out.close();
                clientSocket.close();
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
        if(!MyEmail.properties.JKS_PATH().isEmpty())
            out.println(ReadyESMTP.advertiseTLS());
        out.println(ReadyESMTP.acknowledge());

        awaiting = State.MAIL_FROM;
    }

    private void handleStartTLS() {

        // Check if enabled (shouldn't be here if not but just in case)
        if(MyEmail.properties.JKS_PATH().isEmpty()) {
            out.println(ReadyESMTP.badCommand());
            return;
        }

        // Check if keystore file exists
        File file = new File(MyEmail.properties.JKS_PATH());
        boolean exists = file.exists() && file.isFile();
        if(!exists) {
            out.println(ReadyESMTP.badCommand());
            return;
        }

        // Finally check sequence

        if(awaiting != State.HELO) {
            out.println(ReadyESMTP.badSequence());
            return;
        }

        try {
            out.println(ReadyESMTP.startTLSReady());

            char[] password = MyEmail.properties.JKS_PASSWORD().toCharArray();

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream(file), password);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, password);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(
                    clientSocket,
                    clientSocket.getInetAddress().getHostAddress(),
                    clientSocket.getPort(),
                    true
            );

            // Finally holy shit start the handshake
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

}
