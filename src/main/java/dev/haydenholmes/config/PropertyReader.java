package dev.haydenholmes.config;

import dev.haydenholmes.log.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertyReader {

    Properties properties;

    private PropertyReader() {
        this.properties = new Properties();
    }

    private boolean loadFile() {
        try(InputStream inputStream = PropertyReader.class.getClassLoader().getResourceAsStream("application.properties")) {
            if(inputStream == null) {
                Logger.error("Unable to load properties file");
                return false;
            }
            properties.load(inputStream);
        } catch (IOException e) {
            Logger.exception(e);
        }
        return true;
    }

    // PRIVATE TO TRY AND FORCE GOOD PRACTICE OF USING ENUMS OVER LOOSE STRING VALUES

    private String get(String key) {
        return properties.getProperty(key);
    }

    private String get(String key, String def) {
        return properties.getProperty(key, def);
    }

    public String get(Property property) {
        return get(property.name());
    }

    public String get(Property property, String def) {
        return get(property.name(), def);
    }

    public static PropertyReader init() {
        PropertyReader propertyReader = new PropertyReader();
        boolean valid = propertyReader.loadFile();
        return valid ? propertyReader : null;
    }

}
