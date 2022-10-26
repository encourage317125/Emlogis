package com.emlogis.ihub.proxy.config;

/**
 * Created by Andrii Mozharovskyi on 05.10.2015.
 */
public class ApplicationArgumentsIhubProxiesConfigProvider implements IhubProxiesConfigProvider {
    public static final int REQUIRED_NUMBER_OF_ARGUMENTS = 4;
    String[] args;

    public ApplicationArgumentsIhubProxiesConfigProvider(String[] args) {
        this.args = args;
    }

    public IhubProxiesConfig getConfig() {
        if(args.length != REQUIRED_NUMBER_OF_ARGUMENTS) {
            throw new IllegalArgumentException("You must pass " + REQUIRED_NUMBER_OF_ARGUMENTS + " parameters:"
                    + " proxyHost, proxyIportalPort, proxyRestPort, ihubHost");
        }

        IhubProxiesConfig IhubProxiesConfig = new IhubProxiesConfig();
        IhubProxiesConfig.setProxyHost(args[0]);
        try {
            IhubProxiesConfig.setProxyIportalPort(Integer.parseInt(args[1]));
            IhubProxiesConfig.setProxyRestPort(Integer.parseInt(args[2]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Wrong proxy port values - expected integer, found: proxyIportalPort: " + args[1]
                    + " proxyRestPort: " + args[2]);
        }
        IhubProxiesConfig.setIhubIportalDomain(args[3] + ":" + IhubProxiesConfig.IHUB_IPORTAL_PORT);
        IhubProxiesConfig.setIhubRestDomain(args[3] + ":" + IhubProxiesConfig.IHUB_REST_PORT);

        return IhubProxiesConfig;
    }
}
