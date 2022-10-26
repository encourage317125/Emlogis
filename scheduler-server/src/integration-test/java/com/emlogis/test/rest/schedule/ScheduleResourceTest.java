package com.emlogis.test.rest.schedule;

import com.emlogis.common.services.schedule.ShiftService;
import com.emlogis.model.dto.CreateDto;
import com.emlogis.model.schedule.dto.ScheduleDto;
import com.emlogis.model.schedule.dto.ShiftStructureDto;
import com.emlogis.test.rest.BaseResourceTest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.ejb.EJB;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

public class ScheduleResourceTest extends BaseResourceTest {

    public final static String SCHEDULES = "schedules";

    @EJB
    ShiftService shiftService;

    @Test
    public void testGetObjects() {
        Response response = get(SCHEDULES);

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testCrud() {
        Map<String, Object> inputMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        inputMap.put(ScheduleDto.START_DATE, new Date().getTime());
        inputMap.put(CreateDto.UPDATE_DTO, updateMap);
        String input = mapToJson(inputMap);
        Response response = post(SCHEDULES, input);
        String jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        String id = getValueFromJsonByKey(jsonResponse, "id");

        inputMap = new HashMap<>();
        inputMap.put(ScheduleDto.START_DATE, new Date().getTime() + 10000);
        input = mapToJson(inputMap);
        response = put(SCHEDULES + "/" + id, input);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response = delete(SCHEDULES + "/" + id);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testExecute() throws Exception {
        Map<String, Object> inputMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        inputMap.put(ScheduleDto.START_DATE, new Date().getTime());
        inputMap.put(CreateDto.UPDATE_DTO, updateMap);
        String input = mapToJson(inputMap);
        Response response = post(SCHEDULES, input);
        String jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        final String id = getValueFromJsonByKey(jsonResponse, "id");
        response = post(SCHEDULES + "/" + id + "/execute");

        jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);

        String requestId = getValueFromJsonByKey(jsonResponse, "requestId");
        System.out.println(requestId);

        response.close();
    }

    @Test
    public void testFullCycle() {
        long startDate = System.currentTimeMillis();

        // Add ShiftStructures
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put(ShiftStructureDto.START_DATE, startDate);
        String input = mapToJson(inputMap);
        Response response = post(ShiftStructureResourceTest.SHIFT_STRUCTURES , input);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        inputMap = new HashMap<>();
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put(ScheduleDto.START_DATE, startDate);
        Set<String> teamIdSet = new HashSet<>();
        teamIdSet.add("TeamA");
        updateMap.put("teamIds", teamIdSet);
        inputMap.put(CreateDto.UPDATE_DTO, updateMap);
        input = mapToJson(inputMap);
        response = post(SCHEDULES, input);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testFileScenario() throws IOException {
        playFileScenario("schedule_test.csv");
    }

    @Test
    public void testFileScenarioChanges() throws IOException {
        playFileScenario("schedule_change_test.csv");
    }

    @Test
    public void testFileScenarioChangesGetById() throws IOException {
        playFileScenario("schedule_change_test_get.csv");
    }

    @Test
    public void testChanges() {
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("startDate", new Date().getTime());
        inputMap.put("endDate", new Date().getTime() + 1000);
        String input = mapToJson(inputMap);
        Response response = post(SCHEDULES, input);
        String jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        String id = getValueFromJsonByKey(jsonResponse, "id");

        inputMap = new HashMap<>();
        inputMap.put(ScheduleDto.START_DATE, new Date().getTime() + 10000);
        input = mapToJson(inputMap);
        response = put(SCHEDULES + "/" + id, input);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        inputMap.put(ScheduleDto.START_DATE, new Date().getTime() + 10000);
        input = mapToJson(inputMap);
        response = put(SCHEDULES + "/" + id, input);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response = get(SCHEDULES + "/" + id + "/changes");

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

}
