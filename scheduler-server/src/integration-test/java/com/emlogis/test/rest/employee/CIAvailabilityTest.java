package com.emlogis.test.rest.employee;

import com.emlogis.test.rest.BaseResourceTest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class CIAvailabilityTest extends BaseResourceTest {
    public final static String EMPLOYEE_TYPE_PATH = "employees";
    public final static String ABSENCE_TYPE_PATH = "absencetypes";
    public final static String CI_AVAIL_TYPE_PATH = "ciavailability";
    public final static String SITE_PATH = "sites";

    @Test
    public void testCIAvailability() {

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

        // Attempt to add CI Entry with unsupported availabilityTpe Avail
        // 8/1/2014-8/31/2014, Day=1 , 8am, 2 hours duration, Zeus
        inputMap.clear();
        inputMap.put("availabilityType", "Avail");
        inputMap.put("absenceTypeId", absenceTypeId);
        inputMap.put("reason", "Ready to Work");
        inputMap.put("startTime", "28800000");
        inputMap.put("durationInMinutes", "120");
        inputMap.put("startDate", "1406869200000");
        inputMap.put("endDate", "1409461200000");
        inputMap.put("dayOfTheWeek", "MONDAY");

        input = mapToJson(inputMap);
        response = post(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CI_AVAIL_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create CI type Avail");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);


        // Add CI Entry
        // 8/1/2014-8/31/2014, Day=1 , 8am, 2 hours duration, Zeus
        inputMap.clear();
        inputMap.put("availabilityType", "UnAvail");
        inputMap.put("absenceTypeId", absenceTypeId);
        inputMap.put("reason", "Ready to Work");
        inputMap.put("startTime", "28800000");
        inputMap.put("durationInMinutes", "120");
        inputMap.put("startDate", "1406872800000"); // Using millis for 1:00 am, but should persist rounded to 1406869200000 (midnight)
        inputMap.put("endDate", "1409464800000");   // Using millis for 1:00 am, but should persist rounded to 1409461200000 (midnight)
        inputMap.put("dayOfTheWeek", "MONDAY");

        input = mapToJson(inputMap);
        response = post(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CI_AVAIL_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create CI 1");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        final String ci1Id = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("CI1 ID is: " + ci1Id);
        System.out.println("\n");


        // Add Second CI Entry
        // 8/1/2014 - 8/31/2014, 4ppm, 6 hours duration, day - 2, Zeus, UnAvail
        inputMap.clear();
        inputMap.put("availabilityType", "UnAvail");
        inputMap.put("absenceTypeId", absenceTypeId);
        inputMap.put("reason", "First Time Ever taking Off");
        inputMap.put("startTime", "57600000");
        inputMap.put("durationInMinutes", "360");
        inputMap.put("startDate", "1406869200000");
        inputMap.put("endDate", "1409461200000");
        inputMap.put("dayOfTheWeek", "TUESDAY");

        input = mapToJson(inputMap);
        response = post(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CI_AVAIL_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create CI 2");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        final String ci2Id = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("CI2 ID is: " + ci2Id);
        System.out.println("\n");

        // Add a CI Entry for Hera

        // 8/1/2014-8/31/2014, 8am, 2 hours duration, day = 2, Hera
        inputMap.clear();
        inputMap.put("availabilityType", "UnAvail");
        inputMap.put("absenceTypeId", absenceTypeId);
        inputMap.put("reason", "Ready to Work");
        inputMap.put("startTime", "28800000");
        inputMap.put("durationInMinutes", "120");
        inputMap.put("startDate", "1406869200000");
        inputMap.put("endDate", "1409461200000");
        inputMap.put("dayOfTheWeek", "TUESDAY");

        input = mapToJson(inputMap);
        response = post(EMPLOYEE_TYPE_PATH + "/" + heraId + "/" + CI_AVAIL_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create CI 3");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        final String ci3Id = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("CI3 ID is: " + ci3Id);
        System.out.println("\n");

        // Get First Entry
        response = get(EMPLOYEE_TYPE_PATH + "/" + heraId + "/" + CI_AVAIL_TYPE_PATH + "/" + ci1Id);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Get CI 1");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        String ciAvailabilityTypeString = getValueFromJsonByKey(jsonResponse, "availabilityType");
        String ciAbsenceTypeId = getValueFromJsonByKey(jsonResponse, "absenceTypeId");
        String ciReason = getValueFromJsonByKey(jsonResponse, "reason");
        Long ciStartTime = new Long((Integer) getObjectFromJsonByKey(jsonResponse, "startTime"));
        Integer ciDurationInMinutes = (Integer) getObjectFromJsonByKey(jsonResponse, "durationInMinutes");
        Long ciStartDate = new Long((long) getObjectFromJsonByKey(jsonResponse, "startDate"));
        Long ciEndDate = new Long((long) getObjectFromJsonByKey(jsonResponse, "endDate"));
        //   Integer ciDayOfTheWeek = (Integer) getObjectFromJsonByKey(jsonResponse, "dayOfTheWeek");
        String ciDayOfTheWeek = getValueFromJsonByKey(jsonResponse, "dayOfTheWeek");

        Assert.assertEquals(ciAvailabilityTypeString, "UnAvail");
        Assert.assertEquals(ciAbsenceTypeId, absenceTypeId);
        Assert.assertEquals(ciReason, "Ready to Work");
        Assert.assertEquals(ciStartTime, new Long(28800000));
        Assert.assertEquals(ciDurationInMinutes, new Integer(120));
        Assert.assertEquals(ciStartDate, new Long(1406869200000L)); // Creation arg was 1406872800000, which is millis for 1:00 am, but returned results should be 1406869200000 (midnight)
        Assert.assertEquals(ciEndDate, new Long(1409461200000L));   // Creation arg was 1409464800000, which is millis for 1:00 am, but returned results should be 1409461200000 (midnight)
        Assert.assertEquals(ciDayOfTheWeek, "MONDAY");

        // Get CI List
        response = get(EMPLOYEE_TYPE_PATH + "/" + heraId + "/" + CI_AVAIL_TYPE_PATH);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Get CI List");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        // Attempt to enter duration longer than a day
        // 9/1/2014 - 9/7/2014, 12pm, 1500 minutes/25 hours duration, day = 0,  Zeus
        inputMap.clear();
        inputMap.put("availabilityType", "UnAvail");
        inputMap.put("absenceTypeId", absenceTypeId);
        inputMap.put("reason", "Ready to Work for a longer than a day");
        inputMap.put("startTime", "43200000");
        inputMap.put("durationInMinutes", "1500");
        inputMap.put("startDate", "1409547600000");
        inputMap.put("endDate", "1410066000000");
        inputMap.put("dayOfTheWeek", "SUNDAY");

        input = mapToJson(inputMap);
        response = post(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CI_AVAIL_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create CI 4 Long Duration");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);

        System.out.println("\n");

        // Attempt to enter overlapping timeframe with first timeframe

        // 8/1/2014 - 8/8/2014, 9am, 2 hours duration, Day=1, Zeus
        inputMap.clear();
        inputMap.put("availabilityType", "UnAvail");
        inputMap.put("absenceTypeId", absenceTypeId);
        inputMap.put("reason", "Ready to Work");
        inputMap.put("startTime", "32400000");
        inputMap.put("durationInMinutes", "120");
        inputMap.put("startDate", "1406869200000");
        inputMap.put("endDate", "1407474000000");
        inputMap.put("dayOfTheWeek", "MONDAY");

        input = mapToJson(inputMap);
        response = post(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CI_AVAIL_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Overlapping TimeFrame CI 5");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);

        // Attempt to Enter invalid value for Availability type
        // 91/2014-9/7/2014, 8am, 2 hours duration, day = 2, Hera, Availability type = "Test"
        inputMap.clear();
        inputMap.put("availabilityType", "Test");
        inputMap.put("absenceTypeId", absenceTypeId);
        inputMap.put("reason", "Ready to Work");
        inputMap.put("startTime", "28800000");
        inputMap.put("durationInMinutes", "120");
        inputMap.put("startDate", "1409547600000");
        inputMap.put("endDate", "1410066000000");
        inputMap.put("dayOfTheWeek", "TUESDAY");

        input = mapToJson(inputMap);
        response = post(EMPLOYEE_TYPE_PATH + "/" + heraId + "/" + CI_AVAIL_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test Create CI 3");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);

        // Delete Data we've created

        // Test Delete CI 1
        response = delete(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CI_AVAIL_TYPE_PATH + "/" + ci1Id);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test delete CI 1");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        // Attempt to get deleted record
        response = get(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CI_AVAIL_TYPE_PATH + "/" + ci1Id);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test attempt to get deleted CI 1");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_BAD_REQUEST);

        // Delete CI2 And CI3
        response = delete(EMPLOYEE_TYPE_PATH + "/" + zeusId + "/" + CI_AVAIL_TYPE_PATH + "/" + ci2Id);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test delete CI 2");
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response = delete(EMPLOYEE_TYPE_PATH + "/" + heraId + "/" + CI_AVAIL_TYPE_PATH + "/" + ci3Id);
        jsonResponse = response.readEntity(String.class);
        System.out.println("\n");
        System.out.println("Test delete CI 3");
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
