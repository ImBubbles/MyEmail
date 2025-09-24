package dev.haydenholmes;

import dev.haydenholmes.config.Properties;
import dev.haydenholmes.config.PropertyReader;
import dev.haydenholmes.log.Logger;
import dev.haydenholmes.network.SocketListener;

import java.io.File;

public class MySMTP {

    public static Properties properties;

    public static void main(String[] args) {
        MySMTP instance = new MySMTP();
        Thread server = new Thread(instance::startServer);
        server.start();
        Thread relay = new Thread(instance::startRelay);
        relay.start();
        while(true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public MySMTP() {
        PropertyReader pr = PropertyReader.init();
        if(pr==null)
            return;
        properties = Properties.parse(pr);
        if(properties==null) {
            return;
        }

        File file = new File(properties.PKCS12_PATH());
        Logger.debug("PKCS12 Path is " + file.getAbsolutePath());
        Logger.debug("PKCS12 existence is " + file.exists());
        //System.out.println(file.getAbsolutePath());
        //System.out.println(file.exists());

        // Actually reflect log filter value in properties
        Logger.setFilter(properties.LOG_FILTER());
    }

    public SocketListener startServer() {
        return new SocketListener(properties.PORT_SERVER());
    }

    public SocketListener startRelay() {
        return new SocketListener(properties.PORT_RELAY(), true);
    }

}
