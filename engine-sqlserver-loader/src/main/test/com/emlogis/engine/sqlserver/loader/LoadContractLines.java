package com.emlogis.engine.sqlserver.loader;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.emlogis.engine.sqlserver.loader.domain.EmployeeContractLine;
import com.emlogis.engine.sqlserver.loader.domain.EmployeeContractLine.EmployeeContractPK;
import com.emlogis.engine.sqlserver.loader.domain.SiteContractLine;
import com.emlogis.engine.sqlserver.loader.domain.SiteContractLine.SiteContractLinePK;

public class LoadContractLines extends LoadDataTestBase {

	@Test
	public void testSimpleEmployeeContractLineLoad(){
		EmployeeContractLine contractLine = em.find(EmployeeContractLine.class, new EmployeeContractPK(1, "HoursDay"));
		System.out.println(contractLine);
		assertNotNull(contractLine);
	}
	
	@Test
	public void testSimpleSiteContractLineLoad(){
		SiteContractLine contractLine = em.find(SiteContractLine.class, new SiteContractLinePK(1, "ConsecutiveDays"));
		System.out.println(contractLine);
		assertNotNull(contractLine);
	}
}
