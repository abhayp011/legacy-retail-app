package com.acme.retail.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Global configuration holder. Loaded lazily from db.properties on the classpath. */
public class AppConfig {

    private static Properties props = new Properties();
    private static boolean loaded = false;

    public static synchronized String get(String key) {
        if (!loaded) {
            load();
        }
        return props.getProperty(key);
    }

    private static void load() {
        InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("db.properties");
        try {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException ignore) {
            }
        }
        loaded = true;
    }
}
