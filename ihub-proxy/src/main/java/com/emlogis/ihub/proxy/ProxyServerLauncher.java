package com.emlogis.ihub.proxy;

import com.emlogis.ihub.proxy.config.ApplicationArgumentsIhubProxiesConfigProvider;
import com.emlogis.ihub.proxy.config.IhubProxiesConfig;
import com.emlogis.ihub.proxy.config.IhubProxiesConfigProvider;
import com.emlogis.ihub.proxy.config.PropertiesFileIhubProxiesConfigProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

/**
 * Created by Andrii Mozharovskyi on 02.10.2015.
 */
public class ProxyServerLauncher {
    private static final String HOST_HEADER_NAME = "Host";
    private static final String LOCATION_HEADER_NAME = "Location";
    private static final String REFERER_HEADER_NAME = "Referer";
    private static final String ORIGIN_HEADER_NAME = "Origin";

    private static final boolean DEBUG_MODE = true;

    public static void main(String[] args) {
        IhubProxiesConfigProvider configProvider;
//        configProvider = new ApplicationArgumentsIhubProxiesConfigProvider(args);
        configProvider = new PropertiesFileIhubProxiesConfigProvider("ihub_proxy_config.properties");

        IhubProxiesConfig conf = configProvider.getConfig();

        buildProxyServer(conf.getProxyHost(), conf.getProxyIportalPort(), conf.getIhubIportalDomain()).start();
        buildProxyServer(conf.getProxyHost(), conf.getProxyRestPort(), conf.getIhubRestDomain()).start();
    }

    public static HttpProxyServerBootstrap buildProxyServer(String proxyDomain, int proxyPort, final String realServerHost){
        final String proxyHost = proxyDomain + ":" + proxyPort;

        HttpProxyServerBootstrap server =
            DefaultHttpProxyServer.bootstrap()
                .withPort(proxyPort)
                .withFiltersSource(new HttpFiltersSourceAdapter() {
                    public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                    return new HttpFiltersAdapter(originalRequest) {
                        @Override
                        public HttpResponse clientToProxyRequest(HttpObject httpObject) {
//                            System.out.println("");
//                            System.out.print("Request Client To Proxy");
                            try {
                                if(httpObject instanceof DefaultHttpRequest) {
                                    DefaultHttpRequest request = (DefaultHttpRequest) httpObject;
                                    request.setUri("http://"+ request.headers().get(HOST_HEADER_NAME) +request.getUri());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        public HttpResponse proxyToServerRequest(HttpObject httpObject) {
                            //System.out.println("");
                            try {
                                if(httpObject instanceof DefaultHttpRequest) {
                                    DefaultHttpRequest request = (DefaultHttpRequest) httpObject;
                                    request.headers().set(HOST_HEADER_NAME, realServerHost);

                                    if(DEBUG_MODE) {
                                        System.out.println("Request Proxy To Server {" + request.getMethod() + "}"
                                                + " [" + request.headers().get(HOST_HEADER_NAME) + request.getUri());
                                    }

                                    if(request.getMethod().name().equals("POST")) {
//                                        System.out.print("Request Proxy to Server POST");
//                                        System.out.print(" [" + request.headers().get(HOST_HEADER_NAME) + request.getUri());

                                        String referer = request.headers().get(REFERER_HEADER_NAME);
                                        String origin = request.headers().get(ORIGIN_HEADER_NAME);
                                        if(referer != null) {
                                            request.headers().set(REFERER_HEADER_NAME,
                                                    referer.replaceFirst(proxyHost, realServerHost));
                                        }
                                        if(origin != null) {
                                            request.headers().set(ORIGIN_HEADER_NAME,
                                                    origin.replaceFirst(proxyHost, realServerHost));
                                        }
//                                                    System.out.println("");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        public HttpObject serverToProxyResponse(HttpObject httpObject) {
//                            System.out.println("");
//                            System.out.print("Response Server To Proxy");
                            try {
                                if(httpObject instanceof DefaultHttpResponse) {
                                    DefaultHttpResponse response = (DefaultHttpResponse) httpObject;
                                    //response.headers().set(HOST_HEADER_NAME, proxyHost);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return httpObject;
                        }

                        @Override
                        public HttpObject proxyToClientResponse(HttpObject httpObject) {
//                                        System.out.println("");
//                                        System.out.print("\nResponse Proxy To Client");
                            try {
                                if(httpObject instanceof DefaultHttpResponse) {
                                    DefaultHttpResponse response = (DefaultHttpResponse) httpObject;
                                    String locationHeader = response.headers().get(LOCATION_HEADER_NAME);
                                    if(locationHeader != null) {
                                        response.headers().set(LOCATION_HEADER_NAME, response.headers()
                                                .get(LOCATION_HEADER_NAME).replaceFirst(realServerHost, proxyHost));
                                    }

                                    response.headers().set("Access-Control-Allow-Origin", "*");
                                    response.headers().set("Access-Control-Allow-Headers",
                                            "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With, Access-Control-Allow-Origin");
                                    response.headers().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, HEAD");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return httpObject;
                        }
                    };
                    }
                });
        return server;
    }
}
