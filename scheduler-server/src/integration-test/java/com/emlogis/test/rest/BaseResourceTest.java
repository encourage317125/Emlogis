package com.emlogis.test.rest;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.security.Security;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;

public class BaseResourceTest {

    protected static final String VARIABLE_FIRST_PART = "response_";
    protected static final String RESOURCE_PATH = "scheduler-server/src/integration-test/resources/";

    private Properties properties;
    private String baseUrl;
    private String token;

    private Client client;

    public BaseResourceTest() {
        client = ClientBuilder.newClient();

        final String propertiesFileName = "test.properties";
        try {
            properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream(propertiesFileName));

            baseUrl = getProperty("jboss.url") + getProperty("emlogis.url");
        } catch (IOException e) {
            throw new RuntimeException(String.format("Can not load file %s", propertiesFileName), e);
        }
    }

    @Before
    public void login() {
        String tenantId = getProperty("emlogis.user.tenantId");
        String login = getProperty("emlogis.user.login");
        String password = getProperty("emlogis.user.password");

        token = login(tenantId, login, password);
        System.out.println("SessionId: " + token);
    }

    @After
    public void logout() {
        logout(token);
    }

    protected Response get(String path) {
        return get(path, token);
    }

    protected Response get(String path, String token) {
        String url = baseUrl + "/" + path;
        WebTarget target = client.target(url);
        return target.request(MediaType.APPLICATION_JSON).header(Security.TOKEN_HEADER_NAME, token).get();
    }

    protected Response delete(String path) {
        return delete(path, token);
    }

    protected Response delete(String path, String token) {
        String url = baseUrl + "/" + path;
        WebTarget target = client.target(url);
        return target.request(MediaType.APPLICATION_JSON).header(Security.TOKEN_HEADER_NAME, token).delete();
    }

    protected Response post(String path) {
        return post(path, null);
    }

    protected Response post(String path, String input) {
        String url = baseUrl + "/" + path;
        WebTarget target = client.target(url);
        return target.request(MediaType.APPLICATION_JSON).header(Security.TOKEN_HEADER_NAME, token)
                .post(input == null ? null : Entity.json(input));
    }

    protected Response put(String path, String input) {
        String url = baseUrl + "/" + path;
        WebTarget target = client.target(url);
        return target.request(MediaType.APPLICATION_JSON).header(Security.TOKEN_HEADER_NAME, token)
                .put(input == null ? null : Entity.json(input));
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> jsonToMap(String json) {
        return (Map<String, String>) EmlogisUtils.fromJsonString(json);
    }

    protected String mapToJson(Map map) {
        return EmlogisUtils.toJsonString(map);
    }

    protected String getValueFromJsonByKey(String json, String key) {
        Map<String, String> map = jsonToMap(json);
        return  map.get(key);
    }

    protected Object getObjectFromJsonByKey(String json, String key) {
        Map<String, String> map = jsonToMap(json);
        return map.get(key);
    }

    protected String getProperty(String name) {
        return properties.getProperty(name);
    }

    protected void playTextScenario(String scenario) throws IOException {
        playScenario(new StringReader(scenario));
    }

    protected void playFileScenario(String file) throws IOException {
        playScenario(new FileReader(RESOURCE_PATH + file));
    }

    protected String login(String tenantId, String login, String password) {
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("tenantId", tenantId);
        inputMap.put("login", login);
        inputMap.put("password", password);
        String input = mapToJson(inputMap);

        WebTarget target = client.target(baseUrl + "/sessions");
        Response response = target.request().post(Entity.json(input));

        int code = response.getStatus();
        String output = response.readEntity(String.class);
        response.close();

        Map<String, String> outputMap = jsonToMap(output);

        System.out.println("Login response code = " + code);
        Assert.assertEquals(code, HttpStatus.SC_OK);

        return outputMap.get("token");
    }

    protected void logout(String token) {
        WebTarget target = client.target(baseUrl + "/sessions");
        Response response = target.request().header(Security.TOKEN_HEADER_NAME, token).delete();

        int code = response.getStatus();

        response.close();

        System.out.println("Logout response code = " + code);
        Assert.assertEquals(code, HttpStatus.SC_OK);
    }

    private void playScenario(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);

        List<String[]> data = new ArrayList<>();

        String line = bufferedReader.readLine();
        String[] fields = line.split("[|]");

        while ((line = bufferedReader.readLine()) != null) {
            if (StringUtils.isNotBlank(line) && !line.startsWith("--")) {
                String[] values = line.split("[|]");
                data.add(values);
            }
        }

        Map<String, String> variableMap = new HashMap<>();

        for (String[] row : data) {
            Response response;
            String url = getValue("url", fields, row);
            String method = getValue("method", fields, row);
            String json = getValue("json", fields, row);
            String status = getValue("status", fields, row);

            url = enrichTextByVariables(url, variableMap);
            if (StringUtils.isNotEmpty(json)) {
                json = enrichTextByVariables(json, variableMap);
            }
            method = method.toLowerCase();
            switch (method) {
                case "post":
                    response = post(url, json);
                    break;
                case "get":
                    response = get(url);
                    break;
                case "put":
                    response = put(url, json);
                    break;
                case "delete":
                    response = delete(url);
                    break;
                default:
                    throw new RuntimeException("Unknown http method");
            }
            String responseJson = response.readEntity(String.class);
            for (int i = 0; i < fields.length; i++) {
                String cell;
                if (row.length > i) {
                    cell = row[i];
                } else {
                    cell = null;
                }
                if (fields[i].startsWith(VARIABLE_FIRST_PART) && StringUtils.isNotEmpty(cell)
                        && cell.startsWith("#{") && cell.endsWith("}")) {
                    String responseField = fields[i].substring(VARIABLE_FIRST_PART.length());
                    variableMap.put(cell,  getValueFromJsonByKey(responseJson, responseField));
                }
            }

            System.out.println(responseJson);
            if (status == null) {
                Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
            } else {
                Assert.assertEquals(response.getStatus(), Long.valueOf(status).longValue());
            }

            response.close();
        }
    }

    private String getValue(String key, String[] fields, String[] row) {
        int columnIndex = -1;
        for (int i = 0; i < fields.length; i++) {
            if (StringUtils.equals(key, fields[i])) {
                columnIndex = i;
                break;
            }
        }

        if (columnIndex > -1 && columnIndex < row.length) {
            return row[columnIndex];
        } else {
            return null;
        }
    }

    private String enrichTextByVariables(String text, Map<String, String> variableMap) {
        String result = text;

        Set<String> keys = variableMap.keySet();
        for (String key : keys) {
            if (text.contains(key)) {
                result = result.replace(key, variableMap.get(key));
            }
        }

        return result;
    }

}
