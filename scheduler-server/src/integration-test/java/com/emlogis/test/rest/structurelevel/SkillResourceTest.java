package com.emlogis.test.rest.structurelevel;

import com.emlogis.model.employee.dto.SkillDto;
import com.emlogis.test.rest.BaseResourceTest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class SkillResourceTest extends BaseResourceTest {

    public final static String SKILLS = "skills";

    @Test
    public void testGetObjects() {
        Response response = get(SKILLS);

        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }

    @Test
    public void testCrud() {
        final String id = "integrationTestTemporaryObject";

        Response response = get(SKILLS + "/" + id);
        if (response.getStatus() == HttpStatus.SC_OK) {
            response = delete(SKILLS + "/" + id);
            Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        }

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put(SkillDto.ID, id);
        inputMap.put(SkillDto.NAME, id);
        inputMap.put(SkillDto.ABBREVIATION, "ABBR");
        inputMap.put(SkillDto.DESCRIPTION, "some description");
        String input = mapToJson(inputMap);
        response = post(SKILLS, input);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        inputMap = new HashMap<>();
        inputMap.put(SkillDto.NAME, "NewName");
        inputMap.put(SkillDto.ABBREVIATION, "NN");
        input = mapToJson(inputMap);
        response = put(SKILLS + "/" + id, input);
        System.out.println(response.readEntity(String.class));
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response = delete(SKILLS + "/" + id);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
    }
    
}
