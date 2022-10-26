package com.emlogis.model.schedule.dto;

import java.io.Serializable;

import com.emlogis.model.schedule.ScheduleStatus;

public class ShiftMigrateDto extends ShiftCreateDto implements Serializable {
	
	private String employeeId;
	private String shiftStructureId;
	private String shiftLengthId;
	private String shiftLengthName;
	private ScheduleStatus scheduleStatus;
    

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getShiftStructureId() {
		return shiftStructureId;
	}

	public void setShiftStructureId(String shiftStructureId) {
		this.shiftStructureId = shiftStructureId;
	}

	public String getShiftLengthId() {
		return shiftLengthId;
	}

	public void setShiftLengthId(String shiftLengthId) {
		this.shiftLengthId = shiftLengthId;
	}

	public String getShiftLengthName() {
		return shiftLengthName;
	}

	public void setShiftLengthName(String shiftLengthName) {
		this.shiftLengthName = shiftLengthName;
	}

	public ScheduleStatus getScheduleStatus() {
		return scheduleStatus;
	}

	public void setScheduleStatus(ScheduleStatus scheduleStatus) {
		this.scheduleStatus = scheduleStatus;
	}	
}
