package com.emlogis.ihub.security.http.impl;

import com.emlogis.ihub.security.http.RequestHelper;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJacksonProvider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Created by Andrii Mozharovskyi on 10.07.2015.
 */
public class RestEasyRequestHelper extends RequestHelper {
    private Client client;

    public RestEasyRequestHelper(String restApiUrl) {
        super(restApiUrl);
        client = ClientBuilder.newClient();
        ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(instance);
        instance.registerProvider(ResteasyJacksonProvider.class);
    }

    public String get(String path, Map<String, String> params) {
        return get(path, params, MediaType.APPLICATION_JSON_TYPE);
    }

    public String get(String path, Map<String, String> params, MediaType mediaType) {
        WebTarget target = client.target(getBaseURL() + "/" + path + buildURLParamsString(params));
//        Response response = target.request(MediaType.APPLICATION_JSON).header("EmlogisToken", token).get();
        Response response = target.request(mediaType).get();
        Object o = response.getEntity();
        String result = response.readEntity(String.class);
        response.close();
        return result;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        try {
            client.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
