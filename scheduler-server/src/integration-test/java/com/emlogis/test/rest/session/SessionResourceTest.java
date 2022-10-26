package com.emlogis.test.rest.session;

import com.emlogis.test.rest.BaseResourceTest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;

public class SessionResourceTest extends BaseResourceTest {

    @Test
    public void multiLoginTest() {
        String tenantId = getProperty("emlogis.user.tenantId");
        String login = getProperty("emlogis.user.login");
        String password = getProperty("emlogis.user.password");

        String[] tokens = new String[100];
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = login(tenantId, login, password);
        }

        for (String token : tokens) {
            Response response = get("useraccounts", token);

            Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

            response.close();
        }

        for (String token : tokens) {
            logout(token);
        }
    }

}
