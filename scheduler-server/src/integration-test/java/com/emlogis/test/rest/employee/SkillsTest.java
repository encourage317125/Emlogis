package com.emlogis.test.rest.employee;

import com.emlogis.test.rest.BaseResourceTest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class SkillsTest extends BaseResourceTest {
	@SuppressWarnings("deprecation")
	
	public final static String SKILLS = "skills";
	
	@Test
	public void testSetSkill() {
		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("name", "SkillOrange");
		inputMap.put("description", "SkillOrange description");
		inputMap.put("abbreviation", "sklorng");
		String input = mapToJson(inputMap);
		Response response = post(SKILLS, input);
		
		String jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        response.close();
	}

}
