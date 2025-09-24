package dev.haydenholmes;

import dev.haydenholmes.config.Properties;
import dev.haydenholmes.config.PropertyReader;
import dev.haydenholmes.log.Logger;
import dev.haydenholmes.network.SocketListener;

import java.io.File;

public class MySMTP {

    public static Properties PROPERTIES;

    public static void main(String[] args) {
        MySMTP.init();
        Thread server = new Thread(MySMTP::startServer);
        server.start();
        Thread relay = new Thread(MySMTP::startRelay);
        relay.start();
        while(true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public static boolean init() {
        return loadProperties();
    }

    private static boolean init(Properties properties) {
        if(properties==null) {
            return false;
        }

        PROPERTIES = properties;

        File file = new File(properties.PKCS12_PATH());
        Logger.debug("PKCS12 Path is " + file.getAbsolutePath());
        Logger.debug("PKCS12 existence is " + file.exists());
        //System.out.println(file.getAbsolutePath());
        //System.out.println(file.exists());

        // Actually reflect log filter value in properties
        Logger.setFilter(properties.LOG_FILTER());
        return true;
    }

    private static boolean loadProperties() {
        PropertyReader pr = PropertyReader.init();
        if(pr==null)
            return false;
        return loadProperties(Properties.parse(pr));
    }

    public static boolean loadProperties(Properties properties) {
        return init(properties);
    }

    public static SocketListener startServer() {
        return new SocketListener(PROPERTIES.PORT_SERVER());
    }

    public static SocketListener startRelay() {
        return new SocketListener(PROPERTIES.PORT_RELAY(), true);
    }

}
