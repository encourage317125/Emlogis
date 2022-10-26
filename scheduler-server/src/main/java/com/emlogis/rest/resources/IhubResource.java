package com.emlogis.rest.resources;

import com.emlogis.common.PropertyUtil;
import com.emlogis.rest.resources.util.HttpClientRequestHelper;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Proxy api for Ihub REST API
 */
@Stateless
@LocalBean
@Path("/ihub")
public class IhubResource extends BaseResource{

    PropertyUtil propertyUtil = new PropertyUtil("reports-ihub.properties");

    public String USERNAME = propertyUtil.getProrerty("ihubUsername");
    public String PASSWORD = propertyUtil.getProrerty("ihubPassword");
    public String IHUB_ORIGIN_REST_URL = "http://" + propertyUtil.getProrerty("ihubHost") + ":5000/ihub/v1/";

    @GET
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON)
    public String login(){
        HttpClientRequestHelper requestHelper = new HttpClientRequestHelper(IHUB_ORIGIN_REST_URL);
        String payload = "{\"username\":\"" + USERNAME +
                "\",\"password\":\"" + PASSWORD + "\"}";
        String responce = requestHelper.post("login", payload, MediaType.APPLICATION_JSON, null);
        return responce;
    }

    @GET
    @Path("ihubproperties")
    @Produces(MediaType.APPLICATION_JSON)
    public Map proxyUrl(){
        Map properties = new HashMap<>();
        properties.put("proxyHost", propertyUtil.getProrerty("proxyHost"));
        properties.put("proxyIportalPort", propertyUtil.getProrerty("proxyIportalPort"));
        return properties;
    }

    @GET
    @Path("get")
    @Produces(MediaType.APPLICATION_JSON)
    public String get(@QueryParam("urlpart") String url,
                      @QueryParam("params") String params,
                      @QueryParam("authid") String authId){
        HttpClientRequestHelper requestHelper = new HttpClientRequestHelper(IHUB_ORIGIN_REST_URL);
        Map headers = new HashMap<>();
        headers.put("AuthId", authId);
        return requestHelper.get(url, params, headers);
    }

    @POST
    @Path("post")
    @Produces(MediaType.APPLICATION_JSON)
    public String post(@QueryParam("urlpart") String url,
                       @QueryParam("authid") String authId,
                       String payload){
        HttpClientRequestHelper requestHelper = new HttpClientRequestHelper(IHUB_ORIGIN_REST_URL);
        Map headers = new HashMap<>();
        headers.put("AuthId", authId);
        return requestHelper.post(url, payload, MediaType.APPLICATION_FORM_URLENCODED, headers);
    }
}
