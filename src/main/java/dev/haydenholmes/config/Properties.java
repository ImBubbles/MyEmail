package dev.haydenholmes.config;

import dev.haydenholmes.log.Logger;
import dev.haydenholmes.util.StringCast;

public record Properties(int PORT, String DOMAIN, int SIZE, int LOG_FILTER, String JKS_PATH, String JKS_PASSWORD) {

    public static Properties parse(PropertyReader propertyReader) {
        // PORT
        Integer port = StringCast.toInteger(propertyReader.get(Property.MYEMAIL_PORT, "25"));
        if(port == null) {
            Logger.error("Invalid port");
            return null;
        }

        // DOMAIN
        String domain = propertyReader.get(Property.MYEMAIL_DOMAIN, "mydomain.com");

        // SIZE
        Integer size = StringCast.toInteger(propertyReader.get(Property.MYEMAIL_MAX_SIZE, "10485760"));
        if(size == null) {
            Logger.error("Invalid size");
            return null;
        }

        // Log filter
        Integer filter = StringCast.toInteger(propertyReader.get(Property.MYEMAIL_LOG_FILTER, "0"));
        if(filter == null) {
            Logger.error("Invalid filter");
            return null;
        }

        // JKS PATH
        String jks = propertyReader.get(Property.MYEMAIL_JKS_PATH, "");
        if(jks.isBlank()) {
            Logger.error("Could not find .jks path, TLS not enabled");
        }

        // JKS Password
        String jksPass = propertyReader.get(Property.MYEMAIL_JKS_PASSWORD, "");
        if(jksPass.isBlank()) {
            Logger.error("Password not set for JKS file TLS not enabled");
            jks = "";
        }

        return new Properties(
                port,
                domain,
                size,
                filter,
                jks,
                jksPass
        );

    }

}
