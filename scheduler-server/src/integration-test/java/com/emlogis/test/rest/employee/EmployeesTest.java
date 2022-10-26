package com.emlogis.test.rest.employee;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.test.rest.BaseResourceTest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EmployeesTest extends BaseResourceTest {
	@SuppressWarnings("deprecation")
	
	public final static String SITES_TYPE_PATH     = "sites";
	public final static String TEAMS_TYPE_PATH     = "teams";
	public final static String SKILLS_TYPE_PATH    = "skills";
	public final static String EMPLOYEES_TYPE_PATH = "employees";
	
	@Test
	public void testEmployee() {
		////////////////////////////////////////////////////////////////////////////////
        // Create the underlying site, teams, and skills
		////////////////////////////////////////////////////////////////////////////////

		// Create site 'Federation'
		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("name", "Federation");
		inputMap.put("description", "United Federation of Planets");
		String input = mapToJson(inputMap);
		Response response = post(SITES_TYPE_PATH, input);
		String jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        
        final String siteFederationId = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("Site 'Federation' Id is: " + siteFederationId);

        
		// Create team 'Enterprise'
		inputMap.clear();
		Map<String, Object> updateInputMap = new HashMap<>();
		updateInputMap.put("name", "Enterprise");
		updateInputMap.put("description", "USS Enterprise");
		inputMap.put("updateDto", updateInputMap);
		inputMap.put("siteId", siteFederationId);
		input = mapToJson(inputMap);
		response = post(TEAMS_TYPE_PATH, input);
		jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        
        final String teamEnterpriseId = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("Team 'Enterprise' Id is: " + teamEnterpriseId);


		////////////////////////////////////////////////////////////////////////////////
        // Create team 'Voyager'
		inputMap.clear();
		updateInputMap.clear();
		updateInputMap.put("name", "Voyager");
		updateInputMap.put("description", "USS Voyager");
		inputMap.put("updateDto", updateInputMap);
		inputMap.put("siteId", siteFederationId);
		input = mapToJson(inputMap);
		response = post(TEAMS_TYPE_PATH, input);
		jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        
        final String teamVoyagerId = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("Team 'Voyager' Id is: " + teamVoyagerId);

		// Create skill 'Captain'
		inputMap.clear();
		inputMap.put("name", "Captain");
		inputMap.put("description", "Captains a ship");
		inputMap.put("abbreviation", "Cpt");
		input = mapToJson(inputMap);
		response = post(SKILLS_TYPE_PATH, input);
		jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        
        final String skillCaptainId = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("Skill 'Captain' Id is: " + skillCaptainId);

        // Create skill 'Admiral'
		inputMap.clear();
		inputMap.put("name", "Admiral");
		inputMap.put("description", "Commands a fleet of ships");
		inputMap.put("abbreviation", "Adm");
		input = mapToJson(inputMap);
		response = post(SKILLS_TYPE_PATH, input);
		jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        
        final String skillAdmiralId = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("Skill 'Admiral' Id is: " + skillAdmiralId);

		////////////////////////////////////////////////////////////////////////////////
        // Test employee creation (POST /employees)
		////////////////////////////////////////////////////////////////////////////////

        // Create minimal employee 'Picard'
		inputMap.clear();
		inputMap.put("firstName", "Jean-Luc");
		inputMap.put("lastName", "Picard");
		inputMap.put("employeeIdentifier", "123456789");
		input = mapToJson(inputMap);
		response = post(EMPLOYEES_TYPE_PATH, input);
		jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        
        final String employeePicardId = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("Employee 'Picard' Id is: " + employeePicardId);

        // Create minimal employee/userAccount 'Janeway'
		inputMap.clear();
		inputMap.put("firstName", "Kathryn");
		inputMap.put("lastName", "Janeway");
		inputMap.put("employeeIdentifier", "234567891");

		Map<String, Object> userAccountInputMap = new HashMap<>();
		userAccountInputMap.put("name", "Kathryn Janeway");
		userAccountInputMap.put("login", "kjaneway");
		userAccountInputMap.put("email", "kjaneway@emlogis.com");
		inputMap.put("userAccountDto", userAccountInputMap);
				
		input = mapToJson(inputMap);
		response = post(EMPLOYEES_TYPE_PATH, input);
		jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        
        final String employeeJanewayId = getValueFromJsonByKey(jsonResponse, "id");
        System.out.println("Employee 'Janeway' Id is: " + employeeJanewayId);

        // Create full employee/userAccount 'Kirk'
        System.out.println();
        System.out.println("Create full employee/userAccount 'Kirk'");
        inputMap.clear();
		inputMap.put("firstName", "James");
		inputMap.put("lastName", "Kirk");
		inputMap.put("employeeIdentifier", "345678912");

		updateInputMap.clear();
		updateInputMap.put("middleName", "Tiberius");
		updateInputMap.put("isEngineSchedulable", "false");
		updateInputMap.put("isManuallySchedulable", "false");
		inputMap.put("updateDto", updateInputMap);

		userAccountInputMap = new HashMap<>();
		userAccountInputMap.put("name", "James T. Kirk");
		userAccountInputMap.put("login", "jtkirk");
		userAccountInputMap.put("email", "jtkirk@emlogis.com");
		inputMap.put("userAccountDto", userAccountInputMap);
				
		input = mapToJson(inputMap);
		response = post(EMPLOYEES_TYPE_PATH, input);
		jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        
        final String employeeKirkId = getValueFromJsonByKey(jsonResponse, "id");
        String kirkIdentifier = getValueFromJsonByKey(jsonResponse, "employeeIdentifier");
        String kirkmail = getValueFromJsonByKey(jsonResponse, "workEmail");
        Assert.assertEquals("345678912", kirkIdentifier);
        Assert.assertNull(kirkmail);
        System.out.println("Employee 'Kirk' Id is: " + employeeKirkId);
        System.out.println("Employee 'Kirk' identifier is: " + kirkIdentifier);
        System.out.println("Employee 'Kirk' email is: " + kirkmail);

		////////////////////////////////////////////////////////////////////////////////
        // Test employee update (PUT /employees/{employeeId})
		////////////////////////////////////////////////////////////////////////////////

        // Update employee 'Kirk'
        System.out.println();
        System.out.println("Update employee 'Kirk'");
        inputMap.clear();
		inputMap.put("employeeIdentifier", "capkirk2");
		inputMap.put("workEmail", "kirk2@enterprise.uss");
		input = mapToJson(inputMap);
		response = put(EMPLOYEES_TYPE_PATH + "/" + employeeKirkId, input);
		jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        kirkIdentifier = getValueFromJsonByKey(jsonResponse, "employeeIdentifier");
        kirkmail = getValueFromJsonByKey(jsonResponse, "workEmail");
        Assert.assertEquals("capkirk2", kirkIdentifier);
        Assert.assertEquals("kirk2@enterprise.uss", kirkmail);
        System.out.println("Employee 'Kirk' identifier is: " + kirkIdentifier);
        System.out.println("Employee 'Kirk' email is: " + kirkmail);
        
		////////////////////////////////////////////////////////////////////////////////
        // Test getting a single employee (GET /employees/{employeeId})
		////////////////////////////////////////////////////////////////////////////////
        
        // Test getting employee 'Kirk'
        System.out.println();
        System.out.println("Get employee 'Kirk'");
		response = get(EMPLOYEES_TYPE_PATH + "/" + employeeKirkId);
		jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        String tmpFirstName = getValueFromJsonByKey(jsonResponse, "firstName");
        String tmpMiddleName = getValueFromJsonByKey(jsonResponse, "middleName");
        String tmpLastName = getValueFromJsonByKey(jsonResponse, "lastName");
        Assert.assertEquals("James", tmpFirstName);
        Assert.assertEquals("Tiberius", tmpMiddleName);
        Assert.assertEquals("Kirk", tmpLastName);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

// APIs TO BE TESTED        
//	// Employees
//		GET /employees
//		GET /employees/{employeeId}/userAccount
//		PUT /employees/{employeeId}/userAccount
//		POST /employees/{employeeId}/userAccount/ops/chgpassword
//		DELETE /employees/{employeeId}
//
//	// EmployeeSkills
//		GET /employees/{employeeId}/skills/
//		POST /employees/{employeeId}/skills/
//		PUT /employees/{employeeId}/skills/{skillId}
//		DELETE /employees/{employeeId}/skills/{skillId}
//		  
//	// EmployeeTeams
//		GET /employees/{employeeId}/teams/
//		POST /employees/{employeeId}/teams/
//		PUT /employees/{employeeId}/teams/{teamId}
//		DELETE /employees/{employeeId}/teams/{teamId}

//        // Delete team 'Enterprise'
//        response = delete(TEAMS_TYPE_PATH + "/" + teamEnterpriseId);
//        jsonResponse = response.readEntity(String.class);
//		System.out.println("\n");
//		System.out.println("Test delete 'Enterprise' team ");
//        System.out.println(jsonResponse);
//        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
//
//        
//        // Delete site 'Federation'
//        response = delete(this.SITES_TYPE_PATH + "/" + siteFederationId);
//        jsonResponse = response.readEntity(String.class);
//		System.out.println("\n");
//		System.out.println("Test delete 'Federation' site ");
//        System.out.println(jsonResponse);
//        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        
        // Attempt to get deleted records

        response.close();
	}

    @Test
    public void testLoadEmployee() {
        // Create site 'TestLoadEmployeeSite'
        Map<String, Object> inputMap = new HashMap<>();
        Map<String, Object> updateInputMap = new HashMap<>();
        inputMap.put("updateDto", updateInputMap);
        updateInputMap.put("name", "TestLoadEmployeeSite");
        updateInputMap.put("description", "Test Load Employee Site");
        String input = mapToJson(inputMap);
        Response response = post(SITES_TYPE_PATH, input);
        String jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        response.close();

        final String siteId = getValueFromJsonByKey(jsonResponse, "id");

        inputMap.clear();
        updateInputMap = new HashMap<>();
        updateInputMap.put("name", "TestLoadEmployeeTeam");
        updateInputMap.put("description", "Test Load Employee Team ");
        inputMap.put("updateDto", updateInputMap);
        inputMap.put("siteId", siteId);
        input = mapToJson(inputMap);
        response = post(TEAMS_TYPE_PATH, input);
        jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        response.close();

        String teamId = getValueFromJsonByKey(jsonResponse, "id");

        // Create skill 'TestLoadEmployeeSkill'
//        inputMap.clear();
//        inputMap.put("name", "TestLoadEmployeeSkill");
//        inputMap.put("description", "Test Load Employee Skill");
//        inputMap.put("abbreviation", "TLES3");
//        input = mapToJson(inputMap);
//        response = post(SKILLS_TYPE_PATH, input);
//        jsonResponse = response.readEntity(String.class);
//        System.out.println(jsonResponse);
//        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
//        response.close();
//
//        final String skillId = getValueFromJsonByKey(jsonResponse, "id");

        final int employeesCount = 200000;

        input = null;

        long startTime = System.currentTimeMillis();

        // Create minimal employees
        for (int i = 1; i <= employeesCount; i++) {
            if (i % 20000 == 0 && i > 0) {
                // Create team 'TestLoadEmployeeTeam'
                inputMap.clear();
                updateInputMap = new HashMap<>();
                updateInputMap.put("name", "TestLoadEmployeeTeam" + i);
                updateInputMap.put("description", "Test Load Employee Team " + i);
                inputMap.put("updateDto", updateInputMap);
                inputMap.put("siteId", siteId);
                input = mapToJson(inputMap);
                response = post(TEAMS_TYPE_PATH, input);
                jsonResponse = response.readEntity(String.class);
                System.out.println(jsonResponse);
                Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
                response.close();

                teamId = getValueFromJsonByKey(jsonResponse, "id");
            }

            Map<String, Object> employeeTeamCreateMap = new HashMap<>();
            employeeTeamCreateMap.put("isHomeTeam", true);
            employeeTeamCreateMap.put("teamId", teamId);

            inputMap.clear();
            inputMap.put("firstName", "TestLoadEmpFirstName" + i);
            inputMap.put("lastName", "TestLoadEmpSecondName" + i);
            inputMap.put("employeeIdentifier", "TestLoadEmpIdentity" + i);
            inputMap.put("employeeTeamCreateDto", employeeTeamCreateMap);
            String employeeInput = mapToJson(inputMap);

            response = post(EMPLOYEES_TYPE_PATH, employeeInput);
            Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
//            jsonResponse = response.readEntity(String.class);
//            String employeeId = getValueFromJsonByKey(jsonResponse, "id");
            response.close();

            /*inputMap.clear();
            inputMap.put("teamId", teamId);
            inputMap.put("isFloating", false);
            inputMap.put("isHomeTeam", false);
            inputMap.put("isSchedulable", false);
            String employeeTeamInput = mapToJson(inputMap);
            response = post(EMPLOYEES_TYPE_PATH + "/" + employeeId + "/teams", employeeTeamInput);
            if (response.getStatus() != HttpStatus.SC_OK) {
                System.out.println(i);
            }
            Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
            response.close();*/

            if (i % 100 == 0) {
                long time = System.currentTimeMillis() - startTime;
                System.out.println(i + " time: " + time + " - " + time / i);
            }
        }

        System.out.println("" + employeesCount + " employees creation took (ms): "
                + (System.currentTimeMillis() - startTime));
    }

    @Test
    public void testManagerSubordinates() {
        final String employeeId = "1zh0jcx95sno15xdl30u9";

        Response response = get(EMPLOYEES_TYPE_PATH + "/" + employeeId + "/ops/managers");
        String jsonResponse = response.readEntity(String.class);
        System.out.println(jsonResponse);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        response.close();

        Collection<String> managerIds = (Collection<String>) EmlogisUtils.fromJsonString(jsonResponse);

        for (String managerId : managerIds) {
            response = get(EMPLOYEES_TYPE_PATH + "/" + managerId + "/ops/subordinates?ids=" + employeeId);
            jsonResponse = response.readEntity(String.class);
            System.out.println("subordinates : " + jsonResponse);
            Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
            response.close();

            Collection<String> ids = (Collection<String>) EmlogisUtils.fromJsonString(jsonResponse);
            Assert.assertTrue(ids.contains(employeeId));

            // other way to check

            response = get(EMPLOYEES_TYPE_PATH + "/" + managerId + "/teams");
            jsonResponse = response.readEntity(String.class);
            System.out.println("teams : " + jsonResponse);
            Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
            response.close();

            System.out.println();
        }
    }

}
