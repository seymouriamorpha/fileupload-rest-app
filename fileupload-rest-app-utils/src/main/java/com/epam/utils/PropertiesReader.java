package com.epam.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

public class PropertiesReader {

    private static volatile PropertiesReader instance;
    private Properties properties;

    private PropertiesReader(InputStream inputStream) throws IOException {
        properties = new Properties();
        try {
            properties.load(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public static PropertiesReader getInstance(InputStream inputStream) throws IOException {
        if (instance == null) {
            synchronized (PropertiesReader.class) {
                if(instance == null){
                    instance = new PropertiesReader(inputStream);
                }
            }
        }
        return instance;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public Set<String> getPropertyKeys() {
        return properties.stringPropertyNames();
    }

}
