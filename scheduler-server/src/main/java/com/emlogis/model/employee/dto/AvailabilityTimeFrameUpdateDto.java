package com.emlogis.model.employee.dto;

import com.emlogis.model.dto.UpdateDto;
import com.emlogis.model.employee.AvailabilityTimeFrame.AvailabilityType;

import java.io.Serializable;

public class AvailabilityTimeFrameUpdateDto extends UpdateDto implements Serializable {
	
	public final static String AVAILABILITY_TYPE_STRING ="availabilityType";
	public final static String EMPLOYEEID ="employeeId";
	public final static String ABSENCETYPEID ="absenceTypeId";
	public final static String REASON ="reason";
	public final static String STARTTIME ="startTime";
	public final static String DURATIONINMINUTES ="durationInMinutes";
	
	private AvailabilityType availabilityType;
	private String employeeId;
	private String absenceTypeId;
	private String reason;
	private Integer durationInMinutes;
	
	private String id;
	

	public AvailabilityType getAvailabilityType() {
		return availabilityType;
	}
	public void setAvailabilityType(AvailabilityType availabilityType) {
		this.availabilityType = availabilityType;
	}
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
	public Integer getDurationInMinutes() {
		return durationInMinutes;
	}
	public void setDurationInMinutes(Integer durationInMinutes) {
		this.durationInMinutes = durationInMinutes;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}	
}
