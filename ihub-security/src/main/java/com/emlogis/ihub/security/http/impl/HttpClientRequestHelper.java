package com.emlogis.ihub.security.http.impl;

import java.io.*;
import java.util.Map;

import com.emlogis.ihub.security.http.RequestHelper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Created by Andrii Mozharovskyi on 13.07.2015.
 */
public class HttpClientRequestHelper extends RequestHelper {

    public HttpClientRequestHelper(String baseURL) {
        super(baseURL);
    }

    @Override
    public String get(String path, Map<String, String> params) {

        CloseableHttpResponse response = null;
        String result = null;

        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(getBaseURL() + path + buildURLParamsString(params));

            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

}
