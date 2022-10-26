package com.emlogis.engine.sqlserver.loader;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.emlogis.engine.sqlserver.converter.DBConversionUtilsTester;

@RunWith(Suite.class)
@SuiteClasses({LoadEmployeeTest.class, LoadPTODataTest.class, LoadShiftDemandTest.class, LoadContractLines.class, DBConversionUtilsTester.class})
public class DatabaseLoadingTests {
	
	
}
