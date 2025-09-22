package dev.haydenholmes.config;

import dev.haydenholmes.log.Logger;
import dev.haydenholmes.util.StringCast;

public record Properties(int PORT, String domain) {

    public static Properties parse(PropertyReader propertyReader) {
        // PORT
        Integer port = StringCast.toInteger(propertyReader.get(Property.MYEMAIL_PORT, "25"));
        if(port == null) {
            Logger.error("Invalid port");
            return null;
        }
        // DOMAIN
        String domain = propertyReader.get(Property.MYEMAIL_DOMAIN, "mydomain.com");


        return new Properties(
                port,
                domain
        );

    }

}
