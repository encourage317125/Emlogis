package com.emlogis.model.employee.dto;

import com.emlogis.model.dto.ReadDto;
import com.emlogis.model.employee.AvailabilityTimeFrame.AvailabilityType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)

public class AvailabilityTimeFrameDto extends ReadDto  implements Serializable {
	
	private String id;
	private AvailabilityType availabilityType;
	private String employeeId;
	private String absenceTypeId;
	private String reason;
	private int durationInMinutes;
	

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
	public int getDurationInMinutes() {
		return durationInMinutes;
	}
	public void setDurationInMinutes(int durationInMinutes) {
		this.durationInMinutes = durationInMinutes;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}		
}
