package com.emlogis.test.rest.tenant;

import com.emlogis.test.rest.BaseResourceTest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class UserAccountResourceTest extends BaseResourceTest {

    private final static String USER_ACCOUNTS = "useraccounts";

    private String testUserId;

    @Before
    public void before() {
        testUserId = getProperty("emlogis.user.id");

        Assert.assertNotNull(testUserId);
    }

    @Test
    public void testGetObjects() {
        Response response = get(USER_ACCOUNTS);

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testGetObject() {
        Response response = get(USER_ACCOUNTS + "/" + testUserId);

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testCrud() {
        final String id = "integrationTestTemporaryObject";

        Response response = get(USER_ACCOUNTS + "/" + id);
        if (response.getStatus() == HttpStatus.SC_OK) {
            response.close();
            response = delete(USER_ACCOUNTS + "/" + id);
            Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        }
        response.close();

        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("id", id);
        inputMap.put("login", id.substring(1, 16));
        inputMap.put("name", id);
        inputMap.put("email", "some@correct.email");
        inputMap.put("description", id);
        inputMap.put("inactivityPeriod", "17");
        String input = mapToJson(inputMap);
        response = post(USER_ACCOUNTS, input);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        response.close();

        inputMap = new HashMap<>();
        inputMap.put("name", null);
        inputMap.put("email", "some_other@correct.email");
        inputMap.remove("description");
        input = mapToJson(inputMap);
        response = put(USER_ACCOUNTS + "/" + id, input);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        response.close();

        response = delete(USER_ACCOUNTS + "/" + id);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        response.close();
    }

    @Test
    public void testChangePassword() {
        Response response = post(USER_ACCOUNTS + "/" + testUserId +
                "/ops/chgpassword?previousPassword=oldPassword&newPassword=n");
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);

        response = post(USER_ACCOUNTS + "/" + testUserId +
                "/ops/chgpassword?previousPassword=oldPassword&newPassword=newPassword");
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

}
