package dev.haydenholmes;

import dev.haydenholmes.config.Properties;
import dev.haydenholmes.config.PropertyReader;
import dev.haydenholmes.log.Logger;
import dev.haydenholmes.network.Listener;

import java.io.File;

public class MyEmail {

    public static Properties properties;

    public static void main(String[] args) {
        PropertyReader pr = PropertyReader.init();
        if(pr==null)
            return;
        properties = Properties.parse(pr);
        if(properties==null) {
            return;
        }

        // Actually reflect log filter value in properties
        Logger.setFilter(properties.LOG_FILTER());

        File file = new File("keystore.jks");
        System.out.println(file.getAbsolutePath());

        Listener listener = new Listener();

        //TIP Common Ports:
        // 25 - SMTP
        // 465 - SMTPS



    }

}
