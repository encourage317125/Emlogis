package com.emlogis.engine.sqlserver.loader;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.emlogis.engine.sqlserver.loader.domain.ShiftDemand;

public class LoadShiftDemandTest extends LoadDataTestBase {

	@Test
	public void testBasicLoading() {
		ShiftDemand demand = em.find(ShiftDemand.class, 329L );
		System.out.println(demand);
		assertNotNull(demand);
	}
	
	@Test
	public void testLoadingShiftDemand(){
//		List<Shift> shiftDemand = loader.getShiftDemandForTeam();
//		System.out.println(shiftDemand);
//		assertNotNull(shiftDemand);
//		assertEquals(shiftDemand.size(), 572);
	}

}
 