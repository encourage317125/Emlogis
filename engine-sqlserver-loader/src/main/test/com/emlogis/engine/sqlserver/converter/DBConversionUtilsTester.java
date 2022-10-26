package com.emlogis.engine.sqlserver.converter;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.joda.time.LocalTime;
import org.junit.Test;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.sqlserver.loader.DBConversionUtils;

public class DBConversionUtilsTester {

	@Test
	public void testConvertDBMilitaryTimeNoSpill() {
		int dbTime = 2130;
		MutableBoolean spill = new MutableBoolean(false);
		LocalTime expectedTime = new LocalTime(21, 30, 0);
		assertEquals(expectedTime, DBConversionUtils.convertDBMilitaryTime(dbTime,spill));
		assertEquals(false,spill.booleanValue());
	}
	
	@Test
	public void testConvertDBMilitaryTimeSpillAtStart() {
		int dbTime = 2400;
		MutableBoolean spill = new MutableBoolean(false);
		LocalTime expectedTime = new LocalTime(00, 00, 0);
		assertEquals(expectedTime, DBConversionUtils.convertDBMilitaryTime(dbTime,spill));
		assertEquals(true, spill.booleanValue());
	}
	
	@Test
	public void testConvertDBMilitaryTimeSpillOneAm() {
		int dbTime = 2530;
		MutableBoolean spill = new MutableBoolean(false);
		LocalTime expectedTime = new LocalTime(01, 30, 0);
		assertEquals(expectedTime, DBConversionUtils.convertDBMilitaryTime(dbTime,spill));
		assertEquals(true, spill.booleanValue());
	}

	@Test
	public void testConvertDBMilitaryTimeStandard() {
		int dbTime = 2130;
		LocalTime expectedTime = new LocalTime(21, 30, 0);
		assertEquals(expectedTime, DBConversionUtils.convertDBMilitaryTime(dbTime));
	}
	
	@Test
	public void testConvertDBMilitaryTimeStartOfDay() {
		int dbTime = 101;
		LocalTime expectedTime = new LocalTime(01, 01, 0);
		assertEquals(expectedTime, DBConversionUtils.convertDBMilitaryTime(dbTime));
	}
	
	@Test
	public void testConvertDayListBitmapMonTues(){
		int dayBitMap = 3;
		Collection<DayOfWeek> expectedList = Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
		
		Collection<DayOfWeek> convertedList = DBConversionUtils.convertDayBitMapToList(dayBitMap);
		
		assertTrue(CollectionUtils.isEqualCollection(expectedList, convertedList));
	}
	
	@Test
	public void testConvertDayListBitmapThurAndFri(){
		int dayBitMap = 24;
		Collection<DayOfWeek> expectedList = Arrays.asList(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
		
		Collection<DayOfWeek> convertedList = DBConversionUtils.convertDayBitMapToList(dayBitMap);
		
		assertTrue(CollectionUtils.isEqualCollection(expectedList, convertedList));
	}
	
	@Test
	public void testConvertHoursDecimalRoundHour(){
		double hoursDecimal = 2.0;
		int expectedMinutes = 120;
		int minutes = DBConversionUtils.convertDecimalHoursToMinutes(hoursDecimal);
		
		assertEquals(expectedMinutes, minutes);
	}
	
	@Test
	public void testConvertHoursDecimalHalfHour(){
		double hoursDecimal = 2.5;
		int expectedMinutes = 150;
		int minutes = DBConversionUtils.convertDecimalHoursToMinutes(hoursDecimal);
		
		assertEquals(expectedMinutes, minutes);
	}
	
	@Test
	public void testConvertHoursDecimalThreeQuatersHour(){
		double hoursDecimal = 2.75;
		int expectedMinutes = 165;
		int minutes = DBConversionUtils.convertDecimalHoursToMinutes(hoursDecimal);
		
		assertEquals(expectedMinutes, minutes);
	}
	

}
