package dev.haydenholmes.email;

import dev.haydenholmes.network.protocol.auth.AuthPlainRequest;
import dev.haydenholmes.network.protocol.auth.AuthRequest;

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

    public boolean isRelay(String address) {
        return false;
    }

}
