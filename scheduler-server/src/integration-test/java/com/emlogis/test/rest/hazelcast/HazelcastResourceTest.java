package com.emlogis.test.rest.hazelcast;

import com.emlogis.test.rest.BaseResourceTest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;

public class HazelcastResourceTest extends BaseResourceTest {

    public final static String APPSERVERS = "appservers";
    public final static String ENGINES = "engines";

    @Test
    public void testGetAppServers() {
        Response response = get(APPSERVERS);

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testGetAppServer() {
        Response response = get(APPSERVERS + "/app1");

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testEngines() {
        Response response = get(ENGINES);

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testEngine() {
        Response response = get(ENGINES + "/engine1");

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testAbort() {
        Response response = post(APPSERVERS + "/testRequestId/ops/abort?timeout=1000");

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

}
