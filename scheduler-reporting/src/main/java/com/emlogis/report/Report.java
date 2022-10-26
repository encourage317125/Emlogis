package com.emlogis.report;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Report {
    public static final String EMLOGIS_TOKEN_NAME = "EmlogisToken";
    //Default REST API url. Should be overriden from report within constructor.
    private String baseUrl = "http://192.168.56.101:8080/scheduler-server/emlogis/rest";

    private Client client;

    private enum ResponseStatus {
        SUCCESS
    }

    private enum RequestType {
        GET, POST
    }

    public static void main(String[] args) {
        try {
            Report report = new Report();
            String token = report.loginByAccount("tmp", "schedulecreator");

            Map<String, Object> obj = report.getObject(token, "sites/1zkj78x43q04ztq1fl535");

            System.out.println(obj);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public Report() {
        try {
            client = ClientBuilder.newClient();
        } catch(Throwable t){
            t.printStackTrace();
        }
    }

    public Report(String restApiUrl) {
        this();
        if (restApiUrl != null) {
            baseUrl = restApiUrl;
        }
    }

    public String loginByAccount(String tenantId, String accountId) {
        try {
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put("tenantId", tenantId);
            inputMap.put("accountId", accountId);
            String input = mapToJson(inputMap);

            WebTarget target = client.target(baseUrl + "/sessions/loginbyaccount");
            Response response = target.request().post(Entity.json(input));

            String output = response.readEntity(String.class);
            response.close();

            Map<String, String> outputMap = jsonToMap(output);
            return outputMap.get("token");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String login(String tenantId, String login, String password) {
        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("tenantId", tenantId);
        inputMap.put("login", login);
        inputMap.put("password", password);
        String input = mapToJson(inputMap);

        WebTarget target = client.target(baseUrl + "/sessions");
        Response response = target.request().post(Entity.json(input));

        String output = response.readEntity(String.class);
        response.close();

        Map<String, String> outputMap = jsonToMap(output);

        return outputMap.get("token");
    }

    public List<Map<String, Object>> getList(String sessionId, String url) {
        String json = get(url, sessionId);
        return fromJsonString(json, new TypeReference<List<Map<String, Object>>>() {});
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getListFromResultSet(String sessionId, String url) {
        String json = get(url, sessionId);
        Map<String, Object> resultSetMap = fromJsonString(json, new TypeReference<Map<String, Object>>() {});
        return (List<Map<String, Object>>) resultSetMap.get("result");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getListFromResultSet(String sessionId, String url, String requestType) {
        if(requestType.equals(RequestType.GET.toString())) {
            return getListFromResultSet(sessionId, url);
        } else if(requestType.equals(RequestType.POST.toString())){
            String json = post(url, sessionId);
            Map<String, Object> resultSetMap = fromJsonString(json, new TypeReference<Map<String, Object>>() {});
            return (List<Map<String, Object>>) resultSetMap.get("result");
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getListFromResultSetByPost(String sessionId, String url, Object postEntity) {
        String response = post(url, sessionId, postEntity);
        Map<String, Object> resultSetMap = fromJsonString(response, new TypeReference<Map<String, Object>>() {});
        return (List<Map<String, Object>>) resultSetMap.get("result");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getObject(String sessionId, String url) {
        String response = get(url, sessionId);
        return fromJsonString(response, new TypeReference<Map<String, Object>>() {});
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getListFromReportDto(String sessionId, String url, String requestType) {
        String response = null;
        if(requestType.equals(RequestType.GET.toString())) {
            response = get(url, sessionId);
        } else if(requestType.equals(RequestType.POST.toString())){
            response = post(url, sessionId);
        } else {
            return null;
        }
        Map<String, Object> data = getDataFromReportDtoResponse(response);
        if(data != null) {
            return (List<Map<String, Object>>) data.get("data");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getDataFromReportDtoResponse(String responseAsJSONString) {
        Map<String, Object> resultSetMap = fromJsonString(responseAsJSONString, new TypeReference<Map<String, Object>>() {});
        if(resultSetMap.get("status").equals(ResponseStatus.SUCCESS.toString())) {
            return (Map<String, Object>) resultSetMap.get("data");
        }
        return null;
    }

    public void logout(String token) {
        WebTarget target = client.target(baseUrl + "/sessions");
        Response response = target.request().header(EMLOGIS_TOKEN_NAME, token).delete();
        response.close();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> jsonToMap(String json) {
        return (Map<String, String>) fromJsonString(json, Object.class);
    }

    protected String mapToJson(Map map) {
        return toJsonString(map);
    }

    protected String get(String path, String token) {
        WebTarget target = client.target(baseUrl + "/" + path);
        Response response = target.request(MediaType.APPLICATION_JSON).header(EMLOGIS_TOKEN_NAME, token).get();
        String result = response.readEntity(String.class);
        response.close();
        return result;
    }

    protected String post(String path, String token) {
        return post(path, token, "");
    }

    protected String post(String path, String token, Object payload) {
        WebTarget target = client.target(baseUrl + "/" + path);
        Response response = target.request(MediaType.APPLICATION_JSON)
                .header(EMLOGIS_TOKEN_NAME, token).post(Entity.entity(toJsonString(payload), "application/json"));
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

    private <T> T fromJsonString(String json, TypeReference typeReference) {
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.registerModule(new JodaModule());
        try {
            return objMapper.readValue(json, typeReference);
        } catch (IOException e) {
            // we are in trouble ; Unable to load  object
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String toJsonString(Object object)  {
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.registerModule(new JodaModule());
        try {
            return objMapper.writeValueAsString(object);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJsonString(String json, Class<T> clazz) {
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.registerModule(new JodaModule());
        try {
            return objMapper.readValue(json, clazz);
        } catch (IOException e) {
            // we are in trouble ; Unable to load  object
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> newStringMap() {
        return new HashMap<String, Object>();
    }

    public List<String> newStringList(String ... strings) {
        List<String> list = new ArrayList<String>(strings.length);
        for(String str : strings) {
            list.add(str);
        }
        return list;
    }

}
