package com.emlogis.engine.sqlserver.loader;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.emlogis.engine.domain.DayOfWeek;

public class DBConversionUtils {
	public static final int END_OF_DAY_TIME = 2400;
	public static final int TIME_OVERFLOW = 2500;
	
	/*
	 * Converts an integer time (1200) to a LocalTime object TODO: Modify the DB
	 * schema to use a strong typed time column
	 */
	public static LocalTime convertDBMilitaryTime(int milTime, MutableBoolean crossesNextDay) {
		if (milTime == -1)
			return null; // A -1 signifies that the TimeOf is for the entire day

		if(milTime >= END_OF_DAY_TIME){ //In Mercury database time can go over 2400 if shift crosses next day
			crossesNextDay.setValue(true);
			milTime = milTime - END_OF_DAY_TIME;
		}
		
		DateTimeFormatter fmt = DateTimeFormat.forPattern("HHmm");

		// TODO: Update as part of changes to time storage in DB
		String milTimeStr = String.valueOf(milTime);
		if (milTimeStr.length() < 4) {
			milTimeStr = StringUtils.leftPad(milTimeStr, 4, '0');		
		}
		
		LocalTime convertedTime = fmt.parseLocalTime(milTimeStr);
		return convertedTime;
	}
	
	/*
	 * Converts an integer time (1200) to a Localtime object TODO: Modify the DB
	 * schema to use a strong typed time column
	 */
	public static LocalTime convertDBMilitaryTime(int milTime) {
		return convertDBMilitaryTime(milTime, new MutableBoolean());
	}
	
	/**
	 * Takes an input in decimal hours (2.25) and converts it to 
	 * minutes(120 + 15 = 135)
	 * 
	 * @param decimalHours
	 * @return
	 */
	public static int convertDecimalHoursToMinutes(double decimalHours){
		int minutesInHours = ( (int)decimalHours ) * 60;
		int minutesInFraction = (int) (decimalHours * 60) % 60;
		return minutesInHours + minutesInFraction;
	}
	
	/**
	 * Converts an int representing a bit map of the days of 
	 * the week into a list of Days Of Week.
	 * Monday is always the LSB of the integer. 
	 *
	 * 
	 * @param daysBitMap The integer value of the day bit map
	 * @return Collection of Days that are set to on in the daysBitMap
	 */
	public static Collection<DayOfWeek> convertDayBitMapToList(int daysBitMap){
		Collection<DayOfWeek> daysOfWeek = new ArrayList<DayOfWeek>();

		String binaryMapStr = Integer.toBinaryString(daysBitMap);
		binaryMapStr = StringUtils.leftPad(binaryMapStr, 7, '0');
		
		// Add the day of every bit set to ON
		for(int i = binaryMapStr.length() - 1; i > 0; i--){
			boolean isOn = binaryMapStr.charAt(i) == '1' ? true : false;
			if(isOn){
				daysOfWeek.add(valueOfBinaryDay(i));
			}
		}
		return daysOfWeek;
	}
	
	/**
	 * Converts an int representing a bit map of the days of 
	 * the week into a list of Days Of Week Strings
	 * Monday is always the LSB of the integer. 
	 *
	 * 
	 * @param daysBitMap The integer value of the day bit map
	 * @return Collection of Day Strings that are set to on in the daysBitMap
	 */
	public static Collection<String> convertDayBitMapToString(int daysBitMap){
		Collection<String> daysOfWeek = new ArrayList<String>();

		String binaryMapStr = Integer.toBinaryString(daysBitMap);
		binaryMapStr = StringUtils.leftPad(binaryMapStr, 7, '0');
		
		// Add the day of every bit set to ON
		for(int i = binaryMapStr.length() - 1; i > 0; i--){
			boolean isOn = binaryMapStr.charAt(i) == '1' ? true : false;
			if(isOn){
				daysOfWeek.add(valueOfBinaryDay(i).getName());
			}
		}
		return daysOfWeek;
	}
	
	/**
	 * In the DB bitmaps days of week start from the end of the 
	 * string. Therefore the values 
	 * 
	 * @param calendarDayInWeek
	 * @return
	 */
	private static DayOfWeek valueOfBinaryDay(int calendarDayInWeek) {
		switch (calendarDayInWeek) {
		case 6:
			return DayOfWeek.MONDAY;
		case 5:
			return DayOfWeek.TUESDAY;
		case 4:
			return DayOfWeek.WEDNESDAY;
		case 3:
			return DayOfWeek.THURSDAY;
		case 2:
			return DayOfWeek.FRIDAY;
		case 1:
			return DayOfWeek.SATURDAY;
		case 0:
			return DayOfWeek.SUNDAY;
		default:
			throw new IllegalArgumentException("The calendarDayInWeek ("
					+ calendarDayInWeek + ") is not supported.");
		}
	}
}
