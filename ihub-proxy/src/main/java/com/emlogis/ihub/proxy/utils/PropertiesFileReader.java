package com.emlogis.ihub.proxy.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Andrii Mozharovskyi on 01.10.2015.
 */
public class PropertiesFileReader {
    private String propertiesFileName;

    public PropertiesFileReader(String propertiesFileName) {
        this.propertiesFileName = propertiesFileName;
    }

    public String get(String propertyName){
        InputStream inputStream = null;
        try {
            Properties prop = new Properties();
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFileName);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                System.err.println(new FileNotFoundException("Properties file '" + propertiesFileName + "' is not found in the classpath!").getStackTrace());
                return null;
            }
            return prop.getProperty(propertyName);
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
            return null;
        } finally {
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.err.println(e.getStackTrace());
                }
            }
        }

    }
}
