package com.emlogis.ihub.security.http;

import java.util.Map;

/**
 * Created by Andrii Mozharovskyi on 13.07.2015.
 */
public abstract class RequestHelper {

    private String baseURL;

    public RequestHelper(String baseURL) {
        if (baseURL != null) {
            this.baseURL = baseURL;
        }
    }

    public abstract String get(String path, Map<String, String> params);

    protected String buildURLParamsString(Map<String, String> params) {
        if(params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder paramsString = new StringBuilder("?");
        for(Map.Entry<String, String> entry : params.entrySet()){
            paramsString.append(entry.getKey());
            paramsString.append("=");
            paramsString.append(entry.getValue());
            paramsString.append("&");
        }
        if(paramsString.charAt(paramsString.length()-1) == '&'){
            paramsString.deleteCharAt(paramsString.length()-1);
        }
        return paramsString.toString();
    }

    protected String getBaseURL() {
        return baseURL;
    }

}
