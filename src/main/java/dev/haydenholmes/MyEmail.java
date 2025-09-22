package dev.haydenholmes;

import dev.haydenholmes.config.Properties;
import dev.haydenholmes.config.PropertyReader;
import dev.haydenholmes.email.Email;
import dev.haydenholmes.email.EmailListener;
import dev.haydenholmes.log.Logger;
import dev.haydenholmes.network.SocketListener;

import java.io.File;
import java.util.HashSet;

public class MyEmail {

    public static Properties properties;
    private static HashSet<EmailListener> listeners;
    private static boolean listening = false;

    public static void main(String[] args) {
        PropertyReader pr = PropertyReader.init();
        if(pr==null)
            return;
        properties = Properties.parse(pr);
        if(properties==null) {
            return;
        }

        File file = new File(properties.PKCS12_PATH());
        System.out.println(file.getAbsolutePath());
        System.out.println(file.exists());

        // Actually reflect log filter value in properties
        Logger.setFilter(properties.LOG_FILTER());

        // Init listeners list
        listeners = new HashSet<>();

        // Start listener
        startListener();

    }

    public static void startListener() {
        SocketListener listener = new SocketListener();
    }

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
