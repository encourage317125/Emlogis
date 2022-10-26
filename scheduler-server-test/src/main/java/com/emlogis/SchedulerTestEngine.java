package com.emlogis;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

import java.net.URL;
import java.security.ProtectionDomain;

/**
 * Created by user on 01.10.15.
 */
public class SchedulerTestEngine {


    public Server server;
    public HttpConfiguration https;

    public static Server standaloneServer;
    public static HttpConfiguration standaloneHttps;

    public static void main(String[] args) {
        standaloneServer = new Server();
        ServerConnector connector = new ServerConnector(standaloneServer);
        connector.setPort(9999);
        standaloneHttps = new HttpConfiguration();
        standaloneHttps.addCustomizer(new SecureRequestCustomizer());
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(SchedulerTestEngine.class.getResource(
                "/keystore.jks").toExternalForm());
        sslContextFactory.setKeyStorePassword("123456");
        sslContextFactory.setKeyManagerPassword("123456");
        ServerConnector sslConnector = new ServerConnector(standaloneServer,
                new SslConnectionFactory(sslContextFactory, "http/1.1"),
                new HttpConnectionFactory(standaloneHttps));
        sslConnector.setPort(9998);
        standaloneServer.setConnectors(new Connector[]{connector, sslConnector});
        WebAppContext context = new WebAppContext();
        context.setServer(standaloneServer);
        context.setContextPath("/");
        ProtectionDomain protectionDomain = SchedulerTestEngine.class
                .getProtectionDomain();
        URL location = protectionDomain.getCodeSource().getLocation();
        context.setWar(location.toExternalForm());

        standaloneServer.setHandler(context);
        while (true) {
            try {
                standaloneServer.start();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            System.in.read();
            standaloneServer.stop();
            standaloneServer.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(100);
        }
    }

    public String start() {
        try {
            server = new Server();
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(9999);
            HttpConfiguration https = new HttpConfiguration();
            https.addCustomizer(new SecureRequestCustomizer());
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(SchedulerTestEngine.class.getResource("/keystore.jks").toExternalForm());
            sslContextFactory.setKeyStorePassword("123456");
            sslContextFactory.setKeyManagerPassword("123456");
            ServerConnector sslConnector = new ServerConnector(server,
                    new SslConnectionFactory(sslContextFactory, "http/1.1"),
                    new HttpConnectionFactory(https));
            sslConnector.setPort(9998);
            server.setConnectors(new Connector[]{connector, sslConnector});
            WebAppContext context = new WebAppContext();
            context.setServer(server);
            context.setContextPath("/");
            ProtectionDomain protectionDomain = SchedulerTestEngine.class
                    .getProtectionDomain();
            URL location = protectionDomain.getCodeSource().getLocation();
            context.setWar(location.toExternalForm());

            server.setHandler(context);
            server.start();
            return server.getURI() + " " + server.getState();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return throwable.getMessage();
        }
    }

    public String stop() {
        try {
            server.stop();
            return server.getURI() + " " + server.getState();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return throwable.getMessage();
        }
    }

    public Server getServer() {
        return server;
    }

    public HttpConfiguration getHttps() {
        return https;
    }
}
