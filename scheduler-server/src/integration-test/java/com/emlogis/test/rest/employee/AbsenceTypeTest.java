package com.emlogis.test.rest.employee;

import com.emlogis.test.rest.BaseResourceTest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class AbsenceTypeTest extends BaseResourceTest {
    public final static String ABSENCE_TYPE_PATH = "absencetypes";
    public final static String SITE_PATH = "sites";

    @Test
    public void testAbsenceTypeRest() {

        // Create Site

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("name", "Jackson Site");
        inputMap.put("description", "Jackson Site description");
        String input = mapToJson(inputMap);
        Response response = post(SITE_PATH, input);
        String jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create Site");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);


        final String siteId = getValueFromJsonByKey(jsonResponse, "id");

        // Test Create Absence Type

        inputMap.clear();

        inputMap = new HashMap<>();
        inputMap.put("name", "Vacation2");
        inputMap.put("description", "Vacation description");
        inputMap.put("hoursToDeduct", "8");
        input = mapToJson(inputMap);
        response = post(SITE_PATH + "/" + siteId + "/" + ABSENCE_TYPE_PATH, input);

        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create Absence Type");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);


        // Test Get of Created Absence Type

        final String id = getValueFromJsonByKey(jsonResponse, "id");
        response = get(SITE_PATH + "/" + siteId + "/" + ABSENCE_TYPE_PATH + "/" + id);
        jsonResponse = response.readEntity(String.class);

        String name = getValueFromJsonByKey(jsonResponse, "name");
        String description = getValueFromJsonByKey(jsonResponse, "description");
        Integer hours = (Integer) getObjectFromJsonByKey(jsonResponse, "hoursToDeduct");

        Assert.assertEquals("Vacation2", name);
        Assert.assertEquals("Vacation description", description);
        Assert.assertEquals(new Integer(8), hours);

        // Test Update AbsenceType
        inputMap.put("description", "Vacation Update");
        inputMap.put("hoursToDeduct", "12");
        input = mapToJson(inputMap);
        response = put(SITE_PATH + "/" + siteId + "/" + ABSENCE_TYPE_PATH + "/" + id, input);

        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Get Absence Type");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        // Test Maximum length for Name
        inputMap.put("name", "Vaction33_11111111111111111111111");
        input = mapToJson(inputMap);
        response = post(SITE_PATH + "/" + siteId + "/" + ABSENCE_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test maximum length for name");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);

        // Test Maximum Length for Description
        inputMap.put("name", "Vacation3");
        inputMap.put("description", "Vacation description_1111111111111111111111111111111111111111111111111111111111111111111111111111111111");
        response = post(SITE_PATH + "/" + siteId + "/" + ABSENCE_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test maximum length for description");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);

        // Add another AbsenceType
        inputMap.clear();
        inputMap.put("name", "Vacation4");
        inputMap.put("description", "Vacation description3");
        inputMap.put("hoursToDeduct", "3");
        input = mapToJson(inputMap);
        response = post(SITE_PATH + "/" + siteId + "/" + ABSENCE_TYPE_PATH, input);

        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Insert second Absence Type");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        // Test That we have two Records total
        response = get(SITE_PATH + "/" + siteId + "/" + ABSENCE_TYPE_PATH);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test we can get Absence Type list");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        // Test Delete
        response = delete(SITE_PATH + "/" + siteId + "/" + ABSENCE_TYPE_PATH + "/" + id);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test delete");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        // Attempt to get deleted record
        response = get(SITE_PATH + "/" + siteId + "/" + ABSENCE_TYPE_PATH + "/" + id);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test attempt to get deleted Absence Type");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);

        response.close();
    }


}
