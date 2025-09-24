package dev.haydenholmes.network;

import dev.haydenholmes.email.SMTPHandler;
import dev.haydenholmes.email.SMTPListener;
import dev.haydenholmes.log.Logger;
import dev.haydenholmes.network.protocol.ready.ReadySMTPS;

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
        if(!relay)
            Logger.info("Server created on port " + port);
        else
            Logger.info("Relay server created on port " + port);
        listen();
    }

    public void listen() {
        while(true) {
            try {
                Logger.debug("Awaiting client connect on port " + port);
                Socket client = serverSocket.accept();
                boolean allowConnection = SMTPHandler.allowConnection(client);
                if(client==null||(!allowConnection)) {
                    continue;
                }
                if(relay) {
                    // Check if address is a relay
                    boolean relayAddress = SMTPHandler.isRelay(client);
                    if(!relayAddress) {
                        return;
                    }
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
