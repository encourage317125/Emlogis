package com.emlogis.workflow.service;

import com.emlogis.authentication.TestAuthenticationService;
import com.emlogis.workflow.controller.dto.AuthWipSubmitDto;
import com.emlogis.workflow.controller.dto.WIPCreateReportDto;
import com.emlogis.workflow.controller.dto.WipCreateRequestDto;
import com.emlogis.workflow.controller.dto.WipSubmitDto;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.emlogis.utils.JsonUtil.toJsonString;
import static org.apache.http.impl.client.HttpClientBuilder.create;

/**
 * Created by user on 01.10.15.
 */
@Service
public class CreateRequestService {

    @Autowired
    private TestAuthenticationService authenticationService;

    public WIPCreateReportDto createWip(WipCreateRequestDto requestDto) {
        try {
            Map<Container, Object> resultMap = new HashMap<>();
            HttpClient httpClient = create().build();

            for (AuthWipSubmitDto authWipSubmitDto : requestDto.getRequests()) {
                String token = authenticationService.login(requestDto.getUrl(), authWipSubmitDto.getTenantId(),
                        authWipSubmitDto.getLogin(), authWipSubmitDto.getPassword());
                HttpPost request = new HttpPost(requestDto.getUrl() + "/scheduler-server/emlogis/rest/requests/submitter");
                StringEntity params = new StringEntity(toJsonString((WipSubmitDto) authWipSubmitDto));
                request.addHeader("content-type", "application/json");
                request.setEntity(params);
                HttpResponse response = httpClient.execute(request);
                HttpEntity responseEntity = response.getEntity();
                resultMap.put(new Container(authWipSubmitDto.getLogin(), token), responseEntity);
            }
            return null;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    class Container {
        private final String login;
        private final String token;

        public Container(String login, String token) {
            this.login = login;
            this.token = token;
        }

        public String getLogin() {
            return login;
        }

        public String getToken() {
            return token;
        }
    }
}
