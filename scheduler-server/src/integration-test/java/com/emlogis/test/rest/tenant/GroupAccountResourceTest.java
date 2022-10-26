package com.emlogis.test.rest.tenant;

import com.emlogis.test.rest.BaseResourceTest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class GroupAccountResourceTest extends BaseResourceTest {

    private final static String GROUP_ACCOUNTS = "groupaccounts";

    private String groupId;
    private String memberId;

    @Before
    public void before() {
        groupId = getProperty("emlogis.group");
        memberId = getProperty("emlogis.member");
    }

    @Test
    public void testGetObjects() {
        Response response = get(GROUP_ACCOUNTS);

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testGetObject() {
        Response response = get(GROUP_ACCOUNTS + "/" + groupId);

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testCrud() {
        final String id = "integrationTestTemporaryObject";

        Response response = get(GROUP_ACCOUNTS + "/" + id);
        if (response.getStatus() == HttpStatus.SC_OK) {
            response.close();
            response = delete(GROUP_ACCOUNTS + "/" + id);
            Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        }
        response.close();

        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("id", id);
        inputMap.put("name", id);
        inputMap.put("description", id);
        String input = mapToJson(inputMap);
        response = post(GROUP_ACCOUNTS, input);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        response.close();

        inputMap = new HashMap<>();
        inputMap.put("name", "NewName");
        inputMap.put("description", "NewDescription");
        input = mapToJson(inputMap);
        response = put(GROUP_ACCOUNTS + "/" + id, input);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        response.close();

        response = delete(GROUP_ACCOUNTS + "/" + id);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        response.close();
    }

    @Test
    public void testUpdateWrongObject() {
        final String id = "integrationTestTemporaryObjectForUpdate";
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("name", id);
        inputMap.put("description", id);
        String input = mapToJson(inputMap);
        Response response = put(GROUP_ACCOUNTS + "/" + id, input);

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);

        response.close();
    }

    @Test
    public void testGetMembers() {
        Response response = get(GROUP_ACCOUNTS + "/" + groupId + "/users");

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testAddRemoveMember() {
        String path = GROUP_ACCOUNTS + "/" + groupId + "/ops/adduser?memberId=" + memberId;
        Response response = post(path);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        path = GROUP_ACCOUNTS + "/" + groupId + "/ops/removeuser?memberId=" + memberId;
        response = post(path);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

}
