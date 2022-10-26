package com.emlogis.model.employee.dto;

import com.emlogis.model.dto.CreateDto;
import com.emlogis.model.employee.AvailabilityTimeFrame.AvailabilityType;

import java.io.Serializable;

public class AvailabilityTimeFrameCreateDto extends CreateDto implements Serializable {
	
	public final static String AVAILABILITY_TYPE_STRING ="availabilityType";
	public final static String EMPLOYEE_ID ="employeeId";
	public final static String ABSENCE_TYPE_ID ="absenceTypeId";
	public final static String REASON ="reason";
	public final static String DURATION_IN_MINUTESINT ="durationInMinutes";
	
	private AvailabilityType availabilityType;
	private String employeeId;
	private String absenceTypeId;
	private String reason;
	
	private int durationInMinutes;
	
	public String getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	public String getAbsenceTypeId() {
		return absenceTypeId;
	}
	public void setAbsenceTypeId(String absenceTypeId) {
		this.absenceTypeId = absenceTypeId;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}

	public int getDurationInMinutes() {
		return durationInMinutes;
	}
	public void setDurationInMinutes(int durationInMinutes) {
		this.durationInMinutes = durationInMinutes;
	}
	public AvailabilityType getAvailabilityType() {
		return availabilityType;
	}
	public void setAvailabilityType(AvailabilityType availabilityType) {
		this.availabilityType = availabilityType;
	}	
}
