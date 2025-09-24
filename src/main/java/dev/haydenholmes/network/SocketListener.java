package dev.haydenholmes.network;

import dev.haydenholmes.log.Logger;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class SocketListener {

    private ServerSocket serverSocket = null;
    private final int port;
    private final boolean relay;

    public SocketListener(int port) {
        this(port, false);
    }

    public SocketListener(int port, boolean relay) {
        this.port = port;
        this.relay = relay;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            Logger.error("Could not start listening service");
            Logger.exception(e);
            return;
        }
        Logger.info("Server socket created on port " + port);
        listen();
    }

    public void listen() {
        while(true) {
            try {
                Logger.debug("Awaiting client connect on port " + port);
                Socket client = serverSocket.accept();
                if(client==null) {
                    continue;
                }
                Thread handler = new Thread(() -> {
                   new ServerConnection(client, relay);
                });
                handler.start();
            } catch (IOException e) {
                Logger.exception(e);
                break;
            }
        }
    }

}
