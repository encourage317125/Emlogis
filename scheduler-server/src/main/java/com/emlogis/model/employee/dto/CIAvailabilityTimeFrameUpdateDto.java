package com.emlogis.model.employee.dto;

import com.emlogis.engine.domain.DayOfWeek;

import java.io.Serializable;

public class CIAvailabilityTimeFrameUpdateDto extends AvailabilityTimeFrameUpdateDto implements Serializable {
	
	public final static String STARTDATETIME ="startDateTime";
	public final static String ENDDATETIME ="endDateTime";
	public final static String DAYOFTHEWEEK ="dayOfTheWeek";
	public final static String STARTTIME ="startTime";

	
	private Long startDateTime;
	private Long endDateTime;
	private DayOfWeek dayOfTheWeek;
	private long startTime;
	
	
	public Long getStartDateTime() {
		return startDateTime;
	}
	public void setStartDateTime(Long startDateTime) {
		this.startDateTime = startDateTime;
	}
	public Long getEndDateTime() {
		return endDateTime;
	}
	public void setEndDateTime(Long endDateTime) {
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
