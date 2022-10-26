package com.emlogis.test.rest.schedule;

import com.emlogis.model.schedule.dto.ShiftReqOldDto;
import com.emlogis.model.schedule.dto.ShiftStructureDto;
import com.emlogis.test.rest.BaseResourceTest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ShiftStructureResourceTest extends BaseResourceTest {

    public final static String SHIFT_STRUCTURES = "shiftstructures";
    public final static String SHIFT_REQS = "shiftreqs";

    @Test
    public void testGetObjects() {
        Response response = get(SHIFT_STRUCTURES);

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testCrud() {
        final String id = "integrationTestTemporaryObject";
        final String shiftStructuresSlashId = SHIFT_STRUCTURES + "/" + id;

        Response response = get(shiftStructuresSlashId);
        if (response.getStatus() == HttpStatus.SC_OK) {
            response = delete(shiftStructuresSlashId);
            Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        }

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put(ShiftStructureDto.ID, id);
        inputMap.put(ShiftStructureDto.START_DATE, System.currentTimeMillis());
        String input = mapToJson(inputMap);
        response = post(SHIFT_STRUCTURES, input);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        inputMap = new HashMap<>();
        inputMap.put(ShiftStructureDto.START_DATE, System.currentTimeMillis() + 10000);
        input = mapToJson(inputMap);
        response = put(shiftStructuresSlashId, input);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response = delete(shiftStructuresSlashId);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testShiftReqCrud() {
        final String id = "integrationTestTemporaryObject";
        final String reqId = "integrationTestTemporaryObjectReq";
        final String shiftStructuresSlashId = SHIFT_STRUCTURES + "/" + id;
        final String shiftStructuresSlashIdSlashShiftReqs = shiftStructuresSlashId + "/" + SHIFT_REQS;

        Response response = get(shiftStructuresSlashId);
        if (response.getStatus() != HttpStatus.SC_OK) {
            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put(ShiftStructureDto.ID, id);
            inputMap.put(ShiftStructureDto.START_DATE, System.currentTimeMillis());
            String input = mapToJson(inputMap);
            response = post(SHIFT_STRUCTURES, input);
            System.out.println(response.readEntity(String.class));
            Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        }

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put(ShiftReqOldDto.ID, reqId);
        inputMap.put(ShiftReqOldDto.SHIFT_STRUCTURE_ID, id);
        String input = mapToJson(inputMap);
        response = post(shiftStructuresSlashIdSlashShiftReqs, input);
        System.out.println(response.readEntity(String.class));

        response = put(shiftStructuresSlashIdSlashShiftReqs + "/" + reqId, input);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testFileScenario() throws IOException {
        playFileScenario("shift_structure_test.csv");
    }
}
