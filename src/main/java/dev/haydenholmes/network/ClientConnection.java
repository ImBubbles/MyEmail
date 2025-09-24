package dev.haydenholmes.network;

import dev.haydenholmes.log.Logger;
import dev.haydenholmes.network.protocol.Code;
import dev.haydenholmes.network.protocol.ready.ReadySMTPC;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientConnection {

    private BufferedReader in;
    private PrintWriter out;

    private String host;
    private int port;

    private Socket serverSocket;
    private Code.SEQUENCE_STATE next;

    public ClientConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private String getLine() {
        String result;
        try {
            result = in.readLine();
            return result;
        } catch (IOException e) {
            Logger.exception(e);
        }
        return "";
    }

    public boolean connect() {
        try {
            this.serverSocket = new Socket(host, port);
            this.in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            this.out = new PrintWriter(serverSocket.getOutputStream(), true);
        } catch (Exception e) {
            Logger.exception(e);
            return false;
        }
        Logger.debug("Connected to server " + host + ":" + port);

        Logger.debug("With response " + getLine());

        this.next = Code.SEQUENCE_STATE.HELO;
        return true;
    }

    public String ehlo() {
        if(next != Code.SEQUENCE_STATE.HELO)
            return "";
        out.println(ReadySMTPC.ehlo());
        next = Code.SEQUENCE_STATE.AUTH;
        return getLine();
    }

    public boolean startTLS() {

        if(next != Code.SEQUENCE_STATE.AUTH && next != Code.SEQUENCE_STATE.HELO) {
            return false;
        }

        out.println(ReadySMTPC.startTLS());
        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslSocket = (SSLSocket) factory.createSocket(
                    serverSocket,
                    host,
                    port,
                    true
            );

            sslSocket.setUseClientMode(true);
            sslSocket.startHandshake();

            this.serverSocket = sslSocket;
            this.in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            this.out = new PrintWriter(sslSocket.getOutputStream());

            Code.SMTP_STATUS status = Code.SMTP_STATUS.fromString(getLine());
            if(status == Code.SMTP_STATUS.FAILURE) {
                quit();
                return false;
            }

            if(!isTLS())
                return false;

            next = Code.SEQUENCE_STATE.MAIL_FROM;
            return true;
            
        } catch (IOException e) {
            Logger.exception(e);
            return false;
        }
    }

    public boolean mailFrom(String address, List<Code.SMTP_NOTIFY> notifs, Code.SMTP_ORCPT orcpt) {

        if(!isTLS()) {
            quit();
            return false;
        }

        out.println(ReadySMTPC.mailFrom(
                address,
                notifs,
                orcpt
        ));
        Code.SMTP_STATUS status = Code.SMTP_STATUS.fromString(getLine());
        next = Code.SEQUENCE_STATE.RCPT_TO;
        return status == Code.SMTP_STATUS.ACKNOWLEDGE;
    }

    public String quit() {
        next = Code.SEQUENCE_STATE.DEAD;
        out.println(ReadySMTPC.quit());
        return getLine();
    }

    public boolean isTLS() {
        return this.serverSocket instanceof SSLSocket;
    }

}
