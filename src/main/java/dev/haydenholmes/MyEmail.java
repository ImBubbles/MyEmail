package dev.haydenholmes;

import dev.haydenholmes.config.Properties;
import dev.haydenholmes.config.PropertyReader;
import dev.haydenholmes.network.Listener;

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

        Listener listener = new Listener();

        //TIP Common Ports:
        // 25 - SMTP
        // 465 - SMTPS



    }

}
