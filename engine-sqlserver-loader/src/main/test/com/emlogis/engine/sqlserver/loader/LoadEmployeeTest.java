package com.emlogis.engine.sqlserver.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.emlogis.engine.sqlserver.loader.domain.EmployeeIDToTeamID;
import com.emlogis.engine.sqlserver.loader.domain.EmployeeIDToTeamID.EmployeeIDToTeamIDPK;
import com.emlogis.engine.sqlserver.loader.domain.EmployeeSkill;
import com.emlogis.engine.sqlserver.loader.domain.EmployeeSkill.EmployeeSkillPK;
import com.emlogis.engine.sqlserver.loader.domain.ProductionShiftSummary;
import com.emlogis.engine.sqlserver.loader.domain.ProductionShiftSummary.ProductionShiftSummaryPK;
import com.emlogis.engine.sqlserver.loader.domain.T_Employee;
import com.emlogis.engine.sqlserver.loader.domain.T_SiteResource;
import com.emlogis.engine.sqlserver.loader.exception.NullEntityManagerException;

public class LoadEmployeeTest extends LoadDataTestBase {

	@Test
	public void testFindEmployee() {
		T_Employee employee = em.find(T_Employee.class, 2L);
		System.out.println(employee);
		assertNotNull(employee);
	}
	
	@Test
	public void testFindEmployeeSkill() {
		EmployeeSkill employeeSkill = em.find(EmployeeSkill.class, new EmployeeSkillPK(2, 1));
		System.out.println(employeeSkill);
		assertNotNull(employeeSkill);
	}
	
	@Test
	public void testFindEmployeeRTO() {
		T_SiteResource tSiteResource = em.find(T_SiteResource.class, 40L);
		System.out.println(tSiteResource);
		assertNotNull(tSiteResource);
	}
	
	@Test
	public void testFindEmployeeTeamAssos() {
		EmployeeIDToTeamID employeeToTeamId = em.find(EmployeeIDToTeamID.class, new EmployeeIDToTeamIDPK(252, 14));
		System.out.println(employeeToTeamId);
		assertNotNull(employeeToTeamId);
	}
	
	@Test
	public void testFindEmployeeProductionDemand() {
		ProductionShiftSummary shiftSummary = em.find(ProductionShiftSummary.class, 
								new ProductionShiftSummaryPK(2, "11/18/12", 713, 72711));
		System.out.println(shiftSummary);
		assertNotNull(shiftSummary);
	}
	
	@Test
	public void testFindEmployeeProductionDemandList() {
		List<ProductionShiftSummary> shiftSummaries = null;
		try {
			shiftSummaries = loader.loadEmployeePostedShiftAssignments(2L, new LocalDate(2013, 1, 1), new LocalDate(2015, 1, 1));
		} catch (NullEntityManagerException e) {
			e.printStackTrace();
		}
		System.out.println(shiftSummaries);
		assertNotNull(shiftSummaries);
	}
	
	@Test
	public void testFindEmployeeProductionDemandListByDates() {
		List<ProductionShiftSummary> shiftSummaries = null;
		try {
			shiftSummaries = loader.loadEmployeePostedShiftAssignments(2L, Arrays.asList(new LocalDate(2012, 11, 11).toDate(), new LocalDate(2015, 11, 13).toDate()));
		} catch (NullEntityManagerException e) {
			e.printStackTrace();
		}
		System.out.println(shiftSummaries);
		assertEquals(2, shiftSummaries.size());
		assertNotNull(shiftSummaries);
	}
	
	@Test
	public void testLoadEmployeeSkillsList() {
		List<EmployeeSkill> skillsList = null;
		try {
			skillsList = loader.loadTeamEmployeeSkills(Arrays.asList(2L));
		} catch (NullEntityManagerException e) {
			e.printStackTrace();
		}
		System.out.println(skillsList);
		assertNotNull(skillsList);
		assertEquals(6, skillsList.size());
	}

	@Test
	public void testLoadEmployeeList() {
		List<T_Employee> employeeList = null;
		try {
			employeeList = loader.loadEmployeesForTeam(Arrays.asList(2L));
		} catch (NullEntityManagerException e) {
			e.printStackTrace();
		}
		System.out.println(employeeList);
		assertNotNull(employeeList);
		assertEquals(6, employeeList.size());
	}
	
	@Test
	public void testLoadEmployeeTeamAssociations() {
		List<EmployeeIDToTeamID> employeeToTeamAssociation = null;
		try {
			employeeToTeamAssociation = loader.loadEmployeeToTeamRelationships(Arrays.asList(13L));
		} catch (NullEntityManagerException e) {
			e.printStackTrace();
		}
		System.out.println(employeeToTeamAssociation); 
		assertNotNull(employeeToTeamAssociation);
		assertEquals(18, employeeToTeamAssociation.size());
	}

}
