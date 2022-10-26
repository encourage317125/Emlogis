package com.emlogis.test.rest.employee;

import com.emlogis.test.rest.BaseResourceTest;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class EmployeePictureTest extends BaseResourceTest {

    @Test
    public void testEmployeePicture() throws Exception {
        Map<String, Object> inputMap = new HashMap<>();

        inputMap.put("firstName", "TestFirstName1");
        inputMap.put("lastName", "TestLastName1");
        inputMap.put("employeeIdentifier", "1234567891");
        String input = mapToJson(inputMap);
        Response response = post(EmployeesTest.EMPLOYEES_TYPE_PATH, input);
        String jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        final String employeeId = getValueFromJsonByKey(jsonResponse, "id");

        File file = new File(RESOURCE_PATH + "employeePictureTest.PNG");

        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] image = IOUtils.toByteArray(fileInputStream);

        inputMap.clear();
        inputMap.put("image", image);
        input = mapToJson(inputMap);
        response = put(EmployeesTest.EMPLOYEES_TYPE_PATH + "/" + employeeId + "/picture", input);
        jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response = get(EmployeesTest.EMPLOYEES_TYPE_PATH + "/" + employeeId + "/picture");
        jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
    }
}
