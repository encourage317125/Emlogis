package com.emlogis.engine.domain.timeoff.dto;

import com.emlogis.engine.domain.EmployeeRosterInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.joda.time.*;

import java.util.Date;

public abstract class CDTimeWindowDto extends TimeWindowDto {
	protected DateTime dayOffStart;
	protected DateTime dayOffEnd;
	
	public CDTimeWindowDto() {
		super();
	}	
	
	public CDTimeWindowDto(CDTimeWindowDto dto){
		super(dto);
		this.dayOffStart = dto.dayOffStart;
		this.dayOffEnd   = dto.dayOffEnd;
	}
	
	public DateTime getDayOffStart() {
		return dayOffStart;
	}
	public void setDayOffStart(DateTime dayOffStart) {
		this.dayOffStart = dayOffStart;
	}
	public DateTime getDayOffEnd() {
		return dayOffEnd;
	}
	public void setDayOffEnd(DateTime dayOffEnd) {
		this.dayOffEnd = dayOffEnd;
	}
	
	@JsonIgnore
	public DateTime getStartDateTime(){
		if(startTime != null){
			return dayOffStart.withFields(startTime).withZone(dayOffStart.getZone());
		}
		return dayOffStart;
	}
	
	@JsonIgnore
	public DateTime getEndDateTime(){
		if(endTime != null){
			return dayOffEnd.plus(endTime.getMillisOfDay());
		}
		return dayOffEnd.plus(86400000); // #milliseconds of a day
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
