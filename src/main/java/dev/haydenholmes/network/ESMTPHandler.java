package dev.haydenholmes.network;

import dev.haydenholmes.log.Logger;
import dev.haydenholmes.network.response.Code;
import dev.haydenholmes.network.response.ready.ReadyESMTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ESMTPHandler {

    public ESMTPHandler(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

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
        }
    }

}
