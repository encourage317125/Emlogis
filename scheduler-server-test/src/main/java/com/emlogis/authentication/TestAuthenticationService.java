package com.emlogis.authentication;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.springframework.stereotype.Service;

import static com.emlogis.utils.JsonUtil.toJsonString;
import static org.apache.http.impl.client.HttpClientBuilder.create;

/**
 * Created by user on 02.10.15.
 */
@Service
public class TestAuthenticationService {


    public String login(
            String urlStr,
            String tenant,
            String login,
            String password
    ) {
        HttpClient httpClient = create().build();
        try {
            AuthDto authDto = new AuthDto(tenant, login, password);
            HttpPost request = new HttpPost(urlStr + "/scheduler-server/emlogis/rest/sessions");
            StringEntity params = new StringEntity(toJsonString(authDto));
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            HttpEntity responseEntity = response.getEntity();
            return "";
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
