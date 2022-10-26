package com.emlogis.engine.domain.timeoff;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.emlogis.engine.domain.EmployeeRosterInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class CDTimeWindow extends TimeWindow {
	private DateTime dayOffStart;
	private DateTime dayOffEnd;
	
	public DateTime getDayOffStart() {
		return dayOffStart;
	}
	
	public LocalDate getLocalDayOffStart() {
		return dayOffStart.toLocalDate();
	}
	
	public void setDayOffStart(DateTime dayOffStart) {
		this.dayOffStart = dayOffStart;
	}
	
	public DateTime getDayOffEnd() {
		return dayOffEnd;
	}
	
	public LocalDate getLocalDayOffEnd() {
		return dayOffEnd.toLocalDate();
	}
	
	public void setDayOffEnd(DateTime dayOffEnd) {
		this.dayOffEnd = dayOffEnd;
	}
	
	@JsonIgnore
	public DateTime getStartDateTime(){
		if(startTime != null){
			return dayOffStart.withFields(startTime);
		}
		return dayOffStart.withTimeAtStartOfDay();
	}
	
	@JsonIgnore
	public DateTime getEndDateTime(){
		LocalTime endOfDay = new LocalTime(23, 59, 59);
		if(endTime != null){
			return dayOffEnd.withFields(endTime);
		}
		return dayOffEnd.withFields(endOfDay);
	}
	
	@JsonIgnore
	public Date getStartJavaDate(){
		return getStartDateTime().toDate();
	}
	
	@JsonIgnore
	public boolean isInPlanningWindow(EmployeeRosterInfo info){
		return info.isInPlanningWindow(getStartDateTime());
	}
	
	@JsonIgnore
	public boolean isInScheduleWindow(EmployeeRosterInfo info){
		return info.isInScheduleWindow(getStartDateTime());
	}
	
	@JsonIgnore
	public Duration getDuration(){
		Duration dur = new Duration(getStartDateTime(), getEndDateTime());
		return dur;
	}
	
	@JsonIgnore
	public long getDurationInHours(){
		Duration dur = new Duration(getStartDateTime(), getEndDateTime());
		return dur.getStandardHours();
	}
	
	@JsonIgnore
	public long getDurationInMinutes(){
		Duration dur = new Duration(getStartDateTime(), getEndDateTime());
		return dur.getStandardMinutes();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CDTimeOff [dayOffStart=");
		builder.append(dayOffStart);
		builder.append(", dayOffEnd=");
		builder.append(dayOffEnd);
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}

}
