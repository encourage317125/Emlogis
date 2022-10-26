package com.emlogis.test.rest.tenant;

import com.emlogis.test.rest.BaseResourceTest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;

public class PermissionResourceTest extends BaseResourceTest {

    private final static String PERMISSIONS = "permissions";

    @Test
    public void testGetObjects() {
        Response response = get(PERMISSIONS);

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        response.close();
    }

}
