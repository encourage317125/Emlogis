package com.emlogis.model.employee.dto;

import java.io.Serializable;

import org.joda.time.LocalTime;

public class AvailcalSimpleTimeFrame implements Serializable {
	private long startTime;  // milliseconds offset into the day
	private long endTime;    // milliseconds offset into the day
	
	public long getStartTime() {return startTime;}
	public void setStartTime(long startTime) {this.startTime = startTime;}
	public long getEndTime() {return endTime;}
	public void setEndTime(long endTime) {this.endTime = endTime;}
	
	@Override
	public String toString() {
		LocalTime startLocalTime = new LocalTime(startTime);
		LocalTime endLocalTime = new LocalTime(endTime);
		return "AvailcalSimpleTimeFrame ["
				+ "startTime=" + startTime + " (" + startLocalTime + "), "
				+ "endTime=" + endTime + " (" + endLocalTime + ")]";
		
	}	
}
