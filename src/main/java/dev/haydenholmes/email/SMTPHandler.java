package dev.haydenholmes.email;

import dev.haydenholmes.network.protocol.auth.AuthRequest;

import java.util.HashSet;

public final class SMTPHandler {


    private static final HashSet<SMTPListener> listeners = new HashSet<>();

    public static void registerListener(SMTPListener emailListener) {
        listeners.add(emailListener);
    }

    public static void unregisterListener(SMTPListener emailListener) {
        listeners.remove(emailListener);
    }

    public static void handleEmail(Email email) {
        listeners.forEach(listener -> listener.onEmail(email));
    }

    public static boolean handleAuth(AuthRequest authRequest) { // returns true if any listeners return true
        return listeners.stream().anyMatch(auth -> auth.getAuthed(authRequest));
    }

    public static boolean isRelay(String address) { // returns true if any listeners return true
        return listeners.stream().anyMatch(auth -> auth.isRelay(address));
    }

}
