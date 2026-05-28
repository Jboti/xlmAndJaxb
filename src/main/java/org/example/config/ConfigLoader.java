package org.example.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

    private static final Properties props = new Properties();

    static {
        try (FileInputStream properties = new FileInputStream("db.properties")) {
            props.load(properties);
        } catch (IOException e) {
            throw new RuntimeException("db.properties not found in project root.", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}