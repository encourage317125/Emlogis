package com.emlogis.test.rest.employee;

import com.emlogis.test.rest.BaseResourceTest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class CDAvailabilityTest extends BaseResourceTest {
    public final static String EMPLOYEE_TYPE_PATH = "employees";
    public final static String ABSENCE_TYPE_PATH = "absencetypes";
    public final static String CD_AVAIL_TYPE_PATH = "cdavailability";
    public final static String SITE_PATH = "sites";

    @Test
    public void testCDAvailability() {

        // Create Zeus Employee

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("firstName", "Zeus");
        inputMap.put("lastName", "Jackson");
        inputMap.put("employeeIdentifier", "429694037967");

        String input = mapToJson(inputMap);
        Response response = post(EMPLOYEE_TYPE_PATH, input);

        String jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create Zeus Employee");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        // Get The Employee ID

        final String zeusId = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("Employee Zeus Id is: " + zeusId);

        System.out.println("\n");

        // Create Hera Employee

        inputMap.clear();
        inputMap.put("firstName", "Hera");
        inputMap.put("lastName", "Jackson");
        inputMap.put("employeeIdentifier", "772159985661");

        input = mapToJson(inputMap);
        response = post(EMPLOYEE_TYPE_PATH, input);

        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create Hera Employee");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        // Get The Employee ID

        final String heraId = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("Employee Hera Id is: " + heraId);

        // define a site
        inputMap.clear();
        inputMap.put("name", "Jackson Site");
        inputMap.put("description", "Jackson Site description");
        input = mapToJson(inputMap);
        response = post(SITE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create Site");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);


        final String siteId = getValueFromJsonByKey(jsonResponse, "id");

        // Add AbsenceType
        inputMap.clear();
        inputMap.put("name", "Vacation");
        inputMap.put("description", "Vacation description");
        inputMap.put("hoursToDeduct", "8");

        input = mapToJson(inputMap);
        response = post(SITE_PATH + "/" + siteId + "/" + ABSENCE_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create Absence Type");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        final String absenceTypeId = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("Absence Type ID is: " + absenceTypeId);
        System.out.println("\n");

        // Attempt to add CD Entry with invalid isPTO designation
        // 8/1/2014, 8am, 2 hours duration, Zeus
        inputMap.clear();
        inputMap.put("availabilityType", "Avail");
        inputMap.put("absenceTypeId", absenceTypeId);
        inputMap.put("reason", "Ready to Work");
        inputMap.put("startTime", "28800000");
        inputMap.put("durationInMinutes", "120");
        inputMap.put("isPTO", true);
        inputMap.put("startDate", "1406872800000"); // Using millis for 1:00 am, but should persist rounded to 1406869200000 (midnight)

        input = mapToJson(inputMap);
        response = post(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CD_AVAIL_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create CD 1a bad PTO");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);

        // Add CD Entry
        // 8/1/2014, 8am, 2 hours duration, Zeus
        inputMap.clear();
        inputMap.put("availabilityType", "Avail");
        inputMap.put("absenceTypeId", absenceTypeId);
        inputMap.put("reason", "Ready to Work");
        inputMap.put("startTime", "28800000");
        inputMap.put("durationInMinutes", "120");
        inputMap.put("startDate", "1406872800000"); // Using millis for 1:00 am, but should persist rounded to 1406869200000 (midnight)

        input = mapToJson(inputMap);
        response = post(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CD_AVAIL_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create CD 1");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        final String cd1Id = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("CD1 ID is: " + cd1Id);
        System.out.println("\n");


        // Add Second CD Entry
        // 8/1/2014, 4ppm, 6 hours duration, Zeus, UnAvail
        inputMap.clear();
        inputMap.put("availabilityType", "UnAvail");
        inputMap.put("absenceTypeId", absenceTypeId);
        inputMap.put("reason", "First Time Ever taking Off");
        inputMap.put("startTime", "57600000");
        inputMap.put("durationInMinutes", "360");
        inputMap.put("startDate", "1406869200000");

        input = mapToJson(inputMap);
        response = post(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CD_AVAIL_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create CD 2");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        final String cd2Id = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("CD2 ID is: " + cd2Id);
        System.out.println("\n");

        // Add a CD Entry for Hera

        // 8/1/2014, 8am, 2 hours duration, Hera
        inputMap.clear();
        inputMap.put("availabilityType", "Avail");
        inputMap.put("absenceTypeId", absenceTypeId);
        inputMap.put("reason", "Ready to Work");
        inputMap.put("startTime", "28800000");
        inputMap.put("durationInMinutes", "120");
        inputMap.put("startDate", "1406869200000");

        input = mapToJson(inputMap);
        response = post(EMPLOYEE_TYPE_PATH + "/" + heraId + "/" + CD_AVAIL_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create CD 3");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        final String cd3Id = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("CD3 ID is: " + cd3Id);
        System.out.println("\n");

        // Get First Entry
        response = get(EMPLOYEE_TYPE_PATH + "/" + heraId + "/" + CD_AVAIL_TYPE_PATH + "/" + cd1Id);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Get CD 1");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        String cdAvailabilityTypeString = getValueFromJsonByKey(jsonResponse, "availabilityType");
        String cdAbsenceTypeId = getValueFromJsonByKey(jsonResponse, "absenceTypeId");
        String cdReason = getValueFromJsonByKey(jsonResponse, "reason");
        Long cdStartTime = new Long((Integer) getObjectFromJsonByKey(jsonResponse, "startTime"));
        Integer cdDurationInMinutes = (Integer) getObjectFromJsonByKey(jsonResponse, "durationInMinutes");
        Long cdStartDate = new Long((long) getObjectFromJsonByKey(jsonResponse, "startDate"));

        Assert.assertEquals(cdAvailabilityTypeString, "Avail");
        Assert.assertEquals(cdAbsenceTypeId, absenceTypeId);
        Assert.assertEquals(cdReason, "Ready to Work");
        Assert.assertEquals(cdStartTime, new Long(28800000));
        Assert.assertEquals(cdDurationInMinutes, new Integer(120));
        Assert.assertEquals(cdStartDate, new Long(1406869200000L)); // Creation arg was 1406872800000, which is millis for 1:00 am, but returned results should be 1406869200000 (midnight)

        // Get CD List
        response = get(EMPLOYEE_TYPE_PATH + "/" + heraId + "/" + CD_AVAIL_TYPE_PATH);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Get CD List");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        // Attempt to enter duration longer than a day
        // 8/2/2014, 12pm, 1500 minutes/25 hours duration, Zeus
        inputMap.clear();
        inputMap.put("availabilityType", "Avail");
        inputMap.put("absenceTypeId", absenceTypeId);
        inputMap.put("reason", "Ready to Work for a longer than a day");
        inputMap.put("startTime", "43200000");
        inputMap.put("durationInMinutes", "1500");
        inputMap.put("startDate", "1406955600000");

        input = mapToJson(inputMap);
        response = post(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CD_AVAIL_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create CD 4 Long Duration");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);

        System.out.println("\n");

        
        // Try another with a valid PTO config
        // 8/2/2014, 12am, 24 hours duration, Zeus
        inputMap.clear();
        inputMap.put("availabilityType", "Avail");
        inputMap.put("absenceTypeId", absenceTypeId);
        inputMap.put("reason", "Ready to Work for a longer than a day");
        inputMap.put("startTime", "0");
        inputMap.put("isPTO", true);
        inputMap.put("durationInMinutes", "1440");
        inputMap.put("startDate", "1406955600000");

        input = mapToJson(inputMap);
        response = post(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CD_AVAIL_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create CD 4a PTO");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        final String cd4aId = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("CD4a ID is: " + cd4aId);
        System.out.println("\n");

        // Attempt to enter overlapping timeframe with first timeframe

        // 8/1/2014, 9am, 2 hours duration, Zeus
        inputMap.clear();
        inputMap.put("availabilityType", "Avail");
        inputMap.put("absenceTypeId", absenceTypeId);
        inputMap.put("reason", "Ready to Work");
        inputMap.put("startTime", "32400000");
        inputMap.put("durationInMinutes", "120");
        inputMap.put("startDate", "1406869200000");

        input = mapToJson(inputMap);
        response = post(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CD_AVAIL_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Overlapping TimeFrame CD 5");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);

        // Attempt to Enter invalid value for Availability type
        // 9/1/2014, 8am, 2 hours duration, Hera, Availability = "Test"
        inputMap.clear();
        inputMap.put("availabilityType", "Test");
        inputMap.put("absenceTypeId", absenceTypeId);
        inputMap.put("reason", "Ready to Work");
        inputMap.put("startTime", "28800000");
        inputMap.put("durationInMinutes", "120");
        inputMap.put("startDate", "1409547600000");

        input = mapToJson(inputMap);
        response = post(EMPLOYEE_TYPE_PATH + "/" + heraId + "/" + CD_AVAIL_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create CD Bad Avail");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);

        // Delete Data we've created

        // Test Delete CD 1
        response = delete(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CD_AVAIL_TYPE_PATH + "/" + cd1Id);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test delete CD 1");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        // Attempt to get deleted record
        response = get(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CD_AVAIL_TYPE_PATH + "/" + cd1Id);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test attempt to get deleted CD 1");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);

        // Delete CD2 And CD3
        response = delete(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CD_AVAIL_TYPE_PATH + "/" + cd2Id);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test delete CD 2");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response = delete(EMPLOYEE_TYPE_PATH + "/" + heraId + "/" + CD_AVAIL_TYPE_PATH + "/" + cd3Id);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test delete CD 3");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response = delete(EMPLOYEE_TYPE_PATH + "/" + heraId + "/" + CD_AVAIL_TYPE_PATH + "/" + cd4aId);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test delete CD 4a");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        // Test Delete Absence Type
        response = delete(SITE_PATH + "/" + siteId + "/" + ABSENCE_TYPE_PATH + "/" + absenceTypeId);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test delete AbsenceType Id");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        // Attempt to get deleted record
        response = get(SITE_PATH + "/" + siteId + "/" + ABSENCE_TYPE_PATH + "/" + absenceTypeId);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test attempt to get deleted Absence Type");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);

        // Test Delete Zeus Employee
        response = delete(EMPLOYEE_TYPE_PATH + "/" + zeusId);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test delete Zeus Employee ");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        // Attempt to get deleted record
        response = get(EMPLOYEE_TYPE_PATH + "/" + zeusId);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test attempt to get deleted Zeus Employee");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);

        // Test Delete Zeus Employee
        response = delete(EMPLOYEE_TYPE_PATH + "/" + heraId);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test delete Hera Employee ");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        // Attempt to get deleted record
        response = get(EMPLOYEE_TYPE_PATH + "/" + heraId);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test attempt to get deleted Hera Employee");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);

        response.close();
    }

}
