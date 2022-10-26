package com.emlogis.engine.domain.timeoff;

import org.joda.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public abstract class TimeWindow {
	protected LocalTime startTime;
	protected LocalTime endTime;

	protected String employeeId;
	protected int weight;

	// CI/CD Unavailability and PTO have different overrides
	protected boolean isPTO;

	protected boolean isAllDay = true;
	
	public static final LocalTime END_OF_DAY_TIME = new LocalTime(23, 59, 59, 999);
	
	public LocalTime getStartTime() {
		return startTime;
	}

	public void setTimeWindow(LocalTime startTime, LocalTime endTime) {
		if (startTime == null) {
			this.startTime = LocalTime.MIDNIGHT;
		} else {
			this.startTime = startTime;
			isAllDay = false;
		}

		if (endTime == null)
			this.endTime = new LocalTime(23, 59, 59);
		else
			this.endTime = endTime;
	}

	protected void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	protected void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TimeOff [startTime=");
		builder.append(startTime);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", employeeId=");
		builder.append(employeeId);
		builder.append(", weight=");
		builder.append(weight);
		builder.append("]");
		return builder.toString();
	}
}
