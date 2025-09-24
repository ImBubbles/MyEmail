package dev.haydenholmes.email;

import dev.haydenholmes.network.protocol.auth.AuthPlainRequest;
import dev.haydenholmes.network.protocol.auth.AuthRequest;

import java.net.Socket;

public abstract class SMTPListener {

    // Main func to handle all auth functions and return the result
    final protected boolean getAuthed(AuthRequest authRequest) {
        if(authRequest instanceof AuthPlainRequest)
            return onAuthPlain((AuthPlainRequest) authRequest);
        return onAuth(authRequest);
    }

    public void onEmail(Email email) {}

    public boolean onAuth(AuthRequest authRequest) {
        return false;
    }

    public boolean onAuthPlain(AuthPlainRequest authPlainRequest) {
        return false;
    }

    public boolean isRelay(Socket connection) { // false by default, any listeners with a return value of true will allow the continue through the relay
        return false;
    }

    public boolean allowConnection(Socket connection) { // true by default, any listeners with a return value of false will deny the connection
        return true;
    }

}
