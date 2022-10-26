package com.emlogis.common;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by user on 21.04.15.
 */
public class PropertyUtil {

    public static String sysProp(String name) {
        return System.getProperty(name);
    }

    private Properties properties;

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public PropertyUtil(String propertyFilename) {
        try {
            properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream(propertyFilename));

        } catch (IOException e) {
            throw new RuntimeException(String.format("Can not load file %s", propertyFilename), e);
        }
    }

    public String getProrerty(String propertyName) {
        return getProperties().getProperty(propertyName);
    }


}
