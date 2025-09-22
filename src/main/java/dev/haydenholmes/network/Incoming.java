package dev.haydenholmes.network;

import dev.haydenholmes.log.Logger;
import dev.haydenholmes.network.response.Code;
import dev.haydenholmes.network.response.ready.ReadyESMTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public final class Incoming {

    private final ServerSocket serverSocket;

    public Incoming(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void listen() {
        while(true) {
            try {
                Socket client = serverSocket.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);

                out.println(ReadyESMTP.acceptance());
                String line;
                while((line = in.readLine()) != null) {
                    Logger.debug("Received: " + line);
                    if(line.startsWith(Code.ESMTP_COMMANDS.EHLO.value())) {
                        continue;
                    }
                    if(line.startsWith(Code.ESMTP_COMMANDS.MAIL_FROM.value())) {
                        continue;
                    }
                    if(line.startsWith(Code.ESMTP_COMMANDS.RCPT_TO.value())) {
                        continue;
                    }
                    if(line.startsWith(Code.ESMTP_COMMANDS.DATA.value())) {
                        continue;
                    }
                    if(line.startsWith(Code.ESMTP_COMMANDS.QUIT.value())) {
                        break;
                    }
                    out.println();
                }


            } catch (IOException e) {
                Logger.exception(e);
                break;
            }
        }
    }

}
