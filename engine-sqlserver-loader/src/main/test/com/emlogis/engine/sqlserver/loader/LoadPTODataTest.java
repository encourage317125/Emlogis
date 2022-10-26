package com.emlogis.engine.sqlserver.loader;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.emlogis.engine.sqlserver.loader.domain.T_EmployeeCIRotation;
import com.emlogis.engine.sqlserver.loader.domain.timeoff.T_EmployeeCDAvailability;
import com.emlogis.engine.sqlserver.loader.domain.timeoff.T_EmployeeCIAvailability;
import com.emlogis.engine.sqlserver.loader.exception.NullEntityManagerException;

public class LoadPTODataTest extends LoadDataTestBase {

	@Test
	public void testLoadCDAvailability(){
		T_EmployeeCDAvailability cdAvailability = em.find(T_EmployeeCDAvailability.class, 240);
		System.out.println(cdAvailability);
		assertNotNull(cdAvailability);
	}
	
	@Test
	public void testLoadCIAvailability(){
		T_EmployeeCIAvailability ciAvailability = em.find(T_EmployeeCIAvailability.class, 15);
		System.out.println(ciAvailability);
		assertNotNull(ciAvailability);
	}
	
	@Test
	public void testLoadWeekdayRotation(){
		T_EmployeeCIRotation employeeCIRotation = em.find(T_EmployeeCIRotation.class, 3L);
		System.out.println(employeeCIRotation);
		assertNotNull(employeeCIRotation);
	}
	
	@Test
	public void testLoadCDAvailabilityList(){
		List<T_EmployeeCDAvailability> cdTimeOffList = null;
		try {
			cdTimeOffList = loader.loadTeamCDAvailability(Arrays.asList(2L), new LocalDate(2013, 1, 1), new LocalDate(2015, 1, 1));
		} catch (NullEntityManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(cdTimeOffList);
		System.out.println("Size: " + cdTimeOffList.size());
		System.out.println(cdTimeOffList);
		
		assertNotNull(cdTimeOffList);
	}
	
	@Test
	public void testLoadCIRotationList(){
		List<T_EmployeeCIRotation> ciRotationList = null;
		try {
			ciRotationList = loader.loadTeamCIRotation(Arrays.asList(8L));
		} catch (NullEntityManagerException e) {
			e.printStackTrace();
		}
		
		System.out.println(ciRotationList);
		System.out.println("Size: " + ciRotationList.size());
		System.out.println(ciRotationList);
		
		assertNotNull(ciRotationList);
	}
	
	@Test
	public void testLoadCIAvailabilityList(){
		List<T_EmployeeCIAvailability> ciTimeOffList = null;
		try {
			ciTimeOffList = loader.loadTeamCIAvailability(Arrays.asList(2L));
		} catch (NullEntityManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(ciTimeOffList);
		System.out.println("Size: " + ciTimeOffList.size());
		System.out.println(ciTimeOffList);
		
		assertNotNull(ciTimeOffList);
	}
	
	
}
