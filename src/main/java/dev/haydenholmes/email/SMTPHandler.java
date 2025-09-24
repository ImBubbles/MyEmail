package dev.haydenholmes.email;

import dev.haydenholmes.network.protocol.auth.AuthRequest;

import java.net.Socket;
import java.util.HashSet;

public final class SMTPHandler {


    private static final HashSet<SMTPListener> listeners = new HashSet<>();

    public static void registerListener(SMTPListener emailListener) {
        listeners.add(emailListener);
    }

    public static void unregisterListener(SMTPListener emailListener) {
        listeners.remove(emailListener);
    }

    public static void email(Email email) {
        listeners.forEach(listener -> listener.onEmail(email));
    }

    public static boolean auth(AuthRequest authRequest) { // returns true if any listeners return true
        return listeners.stream().anyMatch(listener -> listener.getAuthed(authRequest));
    }

    public static boolean isRelay(Socket connection) { // returns true if any listeners return true
        return listeners.stream().anyMatch(listener -> listener.isRelay(connection));
    }

    public static boolean allowConnection(Socket connection) { // returns false if any listeners return false
        return listeners.stream().anyMatch(listener -> !(listener.allowConnection(connection)));
    }

}
