package com.emlogis.test.rest.tenant;

import com.emlogis.test.rest.BaseResourceTest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class RoleResourceTest extends BaseResourceTest {

    private final static String ROLES = "roles";

    private String testRoleId;
    private String permissionId;

    @Before
    public void before() {
        testRoleId = getProperty("emlogis.role.id");
        permissionId = getProperty("emlogis.permission.id");

        Assert.assertNotNull(testRoleId);
    }

    @Test
    public void testGetObjects() {
        Response response = get(ROLES);

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testGetObject() {
        Response response = get(ROLES + "/" + testRoleId);

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testCrud() {
        final String id = "integrationTestTemporaryObject";

        Response response = get(ROLES + "/" + id);
        if (response.getStatus() == HttpStatus.SC_OK) {
            response.close();
            response = delete(ROLES + "/" + id);
            Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        }
        response.close();

        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("id", id);
        inputMap.put("label", id);
        inputMap.put("name", id);
        inputMap.put("description", id);
        String input = mapToJson(inputMap);
        response = post(ROLES, input);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        response.close();

        inputMap = new HashMap<>();
        inputMap.put("label", "NewLabel");
        inputMap.put("name", "NewName");
        inputMap.put("description", "NewDescription");
        input = mapToJson(inputMap);
        response = put(ROLES + "/" + id, input);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        response.close();

        response = delete(ROLES + "/" + id);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        response.close();
    }

    @Test
    public void testGetPermissions() {
        Response response = get(ROLES + "/" + testRoleId + "/permissions");

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testAddRemovePermission() {
        Response response = post(ROLES + "/" + testRoleId + "/ops/addpermission?permissionId=" + permissionId);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response = post(ROLES + "/" + testRoleId + "/ops/removepermission?permissionId=" + permissionId);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

}
