package com.emlogis.model.employee.dto;

import com.emlogis.engine.domain.DayOfWeek;

import java.io.Serializable;

public class CIAvailabilityTimeFrameCreateDto extends AvailabilityTimeFrameCreateDto implements Serializable {
	
	public final static String STARTDATETIME ="startDateTime";
	public final static String ENDDATETIME ="endDateTime";
	public final static String DAYOFTHEWEEK ="dayOfTheWeek";
	public final static String STARTTIME ="startTime";
	
	private long startDateTime;
	private long endDateTime;
	private DayOfWeek dayOfTheWeek;
	private long startTime;
	
	public long getStartDateTime() {
		return startDateTime;
	}
	public void setStartDateTime(long startDateTime) {
		this.startDateTime = startDateTime;
	}
	public long getEndDateTime() {
		return endDateTime;
	}
	public void setEndDateTime(long endDateTime) {
		this.endDateTime = endDateTime;
	}
	public DayOfWeek getDayOfTheWeek() {
		return dayOfTheWeek;
	}
	public void setDayOfTheWeek(DayOfWeek dayOfTheWeek) {
		this.dayOfTheWeek = dayOfTheWeek;
	}	
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTimeLong) {
		this.startTime = startTimeLong;
	}
}
