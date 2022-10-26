package com.emlogis.ihub.proxy.config;

/**
 * Created by Andrii Mozharovskyi on 05.10.2015.
 */
public class IhubProxiesConfig {
    public static final String DEFAULT_PROXY_HOST = "localhost";
    public static final String DEFAULT_IHUB_HOST = "localhost";
    public static final String IHUB_IPORTAL_PORT = "8700";
    public static final String IHUB_REST_PORT = "5000";

    public static final int DEFAULT_IHUB_IPORTAL_PORT = 8709;
    public static final int DEFAULT_IHUB_REST_PORT = 5009;

    private String proxyHost = DEFAULT_PROXY_HOST;
    private int proxyIportalPort = DEFAULT_IHUB_IPORTAL_PORT;
    private int proxyRestPort = DEFAULT_IHUB_REST_PORT;
    private String ihubIportalDomain = DEFAULT_IHUB_HOST + ":" + IHUB_IPORTAL_PORT;
    private String ihubRestDomain = DEFAULT_IHUB_HOST + ":" + IHUB_REST_PORT;

    public IhubProxiesConfig() {
    }

    public IhubProxiesConfig(String proxyHost, int proxyIportalPort, int proxyRestPort, String ihubHost) {
        this.proxyHost = proxyHost;
        this.proxyIportalPort = proxyIportalPort;
        this.proxyRestPort = proxyRestPort;
        this.ihubIportalDomain = ihubHost + ":" + IHUB_IPORTAL_PORT;
        this.ihubRestDomain = ihubHost + ":" + IHUB_REST_PORT;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyIportalPort() {
        return proxyIportalPort;
    }

    public void setProxyIportalPort(int proxyIportalPort) {
        this.proxyIportalPort = proxyIportalPort;
    }

    public int getProxyRestPort() {
        return proxyRestPort;
    }

    public void setProxyRestPort(int proxyRestPort) {
        this.proxyRestPort = proxyRestPort;
    }

    public String getIhubIportalDomain() {
        return ihubIportalDomain;
    }

    public void setIhubIportalDomain(String ihubIportalDomain) {
        this.ihubIportalDomain = ihubIportalDomain;
    }

    public String getIhubRestDomain() {
        return ihubRestDomain;
    }

    public void setIhubRestDomain(String ihubRestDomain) {
        this.ihubRestDomain = ihubRestDomain;
    }
}
