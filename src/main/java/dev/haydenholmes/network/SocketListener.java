package dev.haydenholmes.network;

import dev.haydenholmes.MyEmail;
import dev.haydenholmes.log.Logger;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class SocketListener {

    private ServerSocket serverSocket = null;

    public SocketListener() {
        try {
            this.serverSocket = new ServerSocket(MyEmail.properties.PORT());
        } catch (IOException e) {
            Logger.exception(e);
            Logger.error("Could not start listening service");
            return;
        }
        Logger.info("Server socket created");
        listen();
    }

    public void listen() {
        while(true) {
            try {
                Logger.debug("Awaiting client connect on port " + MyEmail.properties.PORT());
                Socket client = serverSocket.accept();
                if(client==null) {
                    continue;
                }
                Thread handler = new Thread(() -> {
                   new ESMTPHandler(client);
                });
                handler.start();
            } catch (IOException e) {
                Logger.exception(e);
                break;
            }
        }
    }

}
