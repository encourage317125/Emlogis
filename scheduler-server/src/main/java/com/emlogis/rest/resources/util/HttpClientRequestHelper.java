package com.emlogis.rest.resources.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vlad on 7/14/15.
 */
public class HttpClientRequestHelper {

    private String baseURL;

    public HttpClientRequestHelper(String baseURL) {
        if (baseURL != null) {
            this.baseURL = baseURL;
        }
    }

    private String getBaseURL() {
        return baseURL;
    }

    public String get(String path, String params, Map<String, String> headers) {

        CloseableHttpResponse response = null;
        String result = null;

        HashMap<String,String> paramsMap = null;
        if (params != null){
            try {
                paramsMap = new ObjectMapper().readValue(params, HashMap.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(getBaseURL() + path + buildURLParamsString(paramsMap));

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpget.addHeader(entry.getKey(), entry.getValue());
                }
            }
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


    public String post(String path, String payload, String contentType, Map<String, String> headers) {
        String res = "";

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(getBaseURL() + path);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httppost.addHeader(entry.getKey(), entry.getValue());
            }
        }
        StringEntity input = null;
        try {
            input = new StringEntity(payload);
            input.setContentType(contentType);
            httppost.setEntity(input);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            try {
                res  =  EntityUtils.toString(entity, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return res;
    }

    protected String buildURLParamsString(Map<String, String> params) {
        if(params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder paramsString = new StringBuilder("?");
        for(Map.Entry<String, String> entry : params.entrySet()){
            paramsString.append(entry.getKey());
            paramsString.append("=");
            try {
                paramsString.append(URLEncoder.encode(entry.getValue(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            paramsString.append("&");
        }
        if(paramsString.charAt(paramsString.length()-1) == '&'){
            paramsString.deleteCharAt(paramsString.length()-1);
        }
        return paramsString.toString();
    }

}
