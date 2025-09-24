package dev.haydenholmes.network.protocol.auth;

public abstract class AuthRequest {

    private boolean authed = false;
    public void auth(boolean authed) {
        this.authed = authed;
    }
    public boolean isAuthed() {
        return this.authed;
    }

}
