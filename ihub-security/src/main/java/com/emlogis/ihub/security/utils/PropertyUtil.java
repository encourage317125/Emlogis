package com.emlogis.ihub.security.utils;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Andrii Mozharovskyi on 01.10.2015.
 */
public class PropertyUtil {

    private static final String PROPERTIES_FILE_NAME = "ihub_custom_security_config.properties";

    private static Properties properties = null;

    public static String get(String propertyName){
        return getProperties().getProperty(propertyName);
    }

    public static String getSystemProperty(String name) {
        return System.getProperty(name);
    }

    private static Properties getProperties() {
        if(properties == null) {
            properties = loadProperties(PROPERTIES_FILE_NAME);
        }
        return properties;
    }

    private static Properties loadProperties(String fileName) {
        Properties prop;
        try {
            prop = new Properties();
            prop.load(PropertyUtil.class.getClassLoader().getResourceAsStream(fileName));
        } catch (IOException e) {
            throw new RuntimeException(String.format("Can not load file %s", fileName), e);
        }
        return prop;
    }
}
