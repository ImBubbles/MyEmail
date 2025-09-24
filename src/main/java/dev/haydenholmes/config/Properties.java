package dev.haydenholmes.config;

import dev.haydenholmes.log.Logger;
import dev.haydenholmes.util.StringCast;

public record Properties(int PORT_SERVER, int PORT_RELAY, String DOMAIN, int SIZE, int LOG_FILTER, String PKCS12_PATH, String PKCS12_PASSWORD) {

    public static Properties parse(PropertyReader propertyReader) {
        // PORT SERVER
        Integer serverPort = StringCast.toInteger(propertyReader.get(Property.MYSMTP_PORT_SERVER, "25"));
        if(serverPort == null) {
            Logger.error("Invalid server port");
            return null;
        }

        // PORT RELAY
        Integer relayPort = StringCast.toInteger(propertyReader.get(Property.MYSMTP_PORT_RELAY, "465"));
        if(relayPort == null) {
            Logger.error("Invalid relay port");
            return null;
        }

        // DOMAIN
        String domain = propertyReader.get(Property.MYSMTP_DOMAIN, "mydomain.com");

        // SIZE
        Integer size = StringCast.toInteger(propertyReader.get(Property.MYSMTP_MAX_SIZE, "10485760"));
        if(size == null) {
            Logger.error("Invalid size");
            return null;
        }

        // Log filter
        Integer filter = StringCast.toInteger(propertyReader.get(Property.MYSMTP_LOG_FILTER, "0"));
        if(filter == null) {
            Logger.error("Invalid filter");
            return null;
        }

        // PKCS12 PATH
        String pkcs12 = propertyReader.get(Property.MYSMTP_PKCS12_PATH, "");
        if(pkcs12.isBlank()) {
            Logger.error("Could not find .jks path, TLS not enabled");
        }

        // PKCS12 Password
        String pkcs12Pass = propertyReader.get(Property.MYSMTP_PKCS12_PASSWORD, "");
        if(pkcs12Pass.isBlank()) {
            Logger.error("Password not set for JKS file TLS not enabled");
            pkcs12 = "";
        }

        return new Properties(
                serverPort,
                relayPort,
                domain,
                size,
                filter,
                pkcs12,
                pkcs12Pass
        );

    }

}
