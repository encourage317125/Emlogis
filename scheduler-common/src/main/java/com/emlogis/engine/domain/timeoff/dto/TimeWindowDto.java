package com.emlogis.engine.domain.timeoff.dto;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@XmlRootElement
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class TimeWindowDto {
	
	protected LocalTime 	startTime;
	protected LocalTime 	endTime;

	protected String 		employeeId;
	protected int 		weight = -1;

	protected boolean 	isPTO;

	protected boolean 	isAllDay = true;
	
	public TimeWindowDto(){
		
	}
	
	public TimeWindowDto(TimeWindowDto timeOff){
		this.startTime = timeOff.startTime;
		this.endTime = timeOff.endTime;
		this.employeeId = timeOff.employeeId;
		this.isPTO = timeOff.isPTO;
		this.isAllDay = timeOff.isAllDay;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public boolean isPTO() {
		return isPTO;
	}

	public void setPTO(boolean isPTO) {
		this.isPTO = isPTO;
	}

	public boolean isAllDay() {
		return isAllDay;
	}
	
	public void setAllDay(boolean isAllDay) {
		this.isAllDay = isAllDay;
	}

}
