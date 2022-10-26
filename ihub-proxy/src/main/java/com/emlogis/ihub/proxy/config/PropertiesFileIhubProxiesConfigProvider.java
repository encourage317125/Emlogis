package com.emlogis.ihub.proxy.config;

import com.emlogis.ihub.proxy.utils.PropertiesFileReader;

/**
 * Created by Andrii Mozharovskyi on 05.10.2015.
 */
public class PropertiesFileIhubProxiesConfigProvider implements IhubProxiesConfigProvider {
    private String propertiesFileName;

    public PropertiesFileIhubProxiesConfigProvider(String propertiesFileName) {
        this.propertiesFileName = propertiesFileName;
    }

    public IhubProxiesConfig getConfig() {
        PropertiesFileReader reader = new PropertiesFileReader(propertiesFileName);
        IhubProxiesConfig cfg = new IhubProxiesConfig();
        cfg.setProxyHost(reader.get("proxyHost"));

        String proxyIportalPort = reader.get("proxyIportalPort");
        String proxyRestPort = reader.get("proxyRestPort");
        try {
            cfg.setProxyIportalPort(Integer.parseInt(proxyIportalPort));
            cfg.setProxyRestPort(Integer.parseInt(proxyRestPort));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Wrong proxy port values - expected integer, found: proxyIportalPort: " + proxyIportalPort
                    + " proxyRestPort: " + proxyRestPort);
        }

        String ihubHost = reader.get("ihubHost");
        cfg.setIhubIportalDomain(ihubHost + ":" + IhubProxiesConfig.IHUB_IPORTAL_PORT);
        cfg.setIhubRestDomain(ihubHost + ":" + IhubProxiesConfig.IHUB_REST_PORT);
        return cfg;
    }
}
