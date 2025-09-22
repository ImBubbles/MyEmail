package dev.haydenholmes.email;

import java.util.HashSet;

public final class EmailHandler {


    private static final HashSet<EmailListener> listeners = new HashSet<>();

    public static void registerListener(EmailListener emailListener) {
        listeners.add(emailListener);
    }

    public static void unregisterListener(EmailListener emailListener) {
        listeners.remove(emailListener);
    }

    public static void onEmail(Email email) {
        listeners.forEach(listener -> listener.onEmail(email));
    }

}
