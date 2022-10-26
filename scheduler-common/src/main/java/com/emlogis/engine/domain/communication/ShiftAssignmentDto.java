package com.emlogis.engine.domain.communication;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Seconds;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public class ShiftAssignmentDto implements Serializable {

	private String shiftId;
	private String shiftSkillId;
	
	private DateTime shiftStartDateTime;
	private DateTime shiftEndDateTime;

	private String employeeId;
	private String employeeName;
	private String teamId;

	private boolean isLocked;
	private	boolean isExcess;
	
	@JsonIgnore
	public int getShiftDurationSeconds() {
		return Seconds.secondsBetween(shiftStartDateTime, shiftEndDateTime).getSeconds();
	}
	
	@JsonIgnore
	public int getShiftDurationHours() {
		return Hours.hoursBetween(shiftStartDateTime, shiftEndDateTime).getHours();
	}
	
	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}
	
	public DateTime getShiftStartDateTime() {
		return shiftStartDateTime;
	}

	public void setShiftStartDateTime(DateTime shiftStartDateTime) {
		this.shiftStartDateTime = shiftStartDateTime;
	}
	
	public DateTime getShiftEndDateTime() {
		return shiftEndDateTime;
	}

	public void setShiftEndDateTime(DateTime shiftEndDateTime) {
		this.shiftEndDateTime = shiftEndDateTime;
	}
	
	public String getShiftSkillId() {
		return shiftSkillId;
	}

	public void setShiftSkillId(String shiftSkillId) {
		this.shiftSkillId = shiftSkillId;
	}

	public String getShiftId() {
		return shiftId;
	}

	public void setShiftId(String shiftId) {
		this.shiftId = shiftId;
	}

    public String getEmployeeId() {
		return employeeId;
	}

    public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public boolean isLocked() {
		return isLocked;
	}

    public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

    public boolean isExcess() {
		return isExcess;
	}

    public void setExcess(boolean isExcess) {
		this.isExcess = isExcess;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ShiftAssignmentDto [");
		if (shiftId != null) {
			builder.append("shiftId=");
			builder.append(shiftId);
			builder.append(", ");
		}
		if (employeeId != null) {
			builder.append("employeeId=");
			builder.append(employeeId);
			builder.append(", ");
		}
		builder.append("isLocked=");
		builder.append(isLocked);
		builder.append(", isExcess=");
		builder.append(isExcess);
		builder.append("]");
		return builder.toString();
	}

}
