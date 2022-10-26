package com.emlogis.engine.domain.communication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.builder.CompareToBuilder;

import com.emlogis.engine.domain.ShiftAssignment;
import com.emlogis.engine.domain.communication.constraints.ShiftConstraintDto;

public class ShiftQualificationDto implements Serializable, Comparable {
	
	private String shiftId;
	private String employeeId;
	private String employeeName;
	private boolean isAccepted = true;
	private Collection<ShiftConstraintDto> causes;

	public ShiftQualificationDto(){
		causes = new ArrayList<>();
	}
	
	public ShiftQualificationDto(ShiftAssignment shift){
		shiftId = shift.getShift().getId();
		causes = new ArrayList<>();
		if(shift.getEmployee() != null){
			employeeId = shift.getEmployeeId();
			employeeName = shift.getEmployee().getFullName();
		}
	}
	
	/**
	 * @return the shiftId
	 */
	public String getShiftId() {
		return shiftId;
	}
	
	/**
	 * @param shiftId the shiftId to set
	 */
	public void setShiftId(String shiftId) {
		this.shiftId = shiftId;
	}
	
	/**
	 * @return the employeeId
	 */
	public String getEmployeeId() {
		return employeeId;
	}
	
	/**
	 * @param employeeId the employeeId to set
	 */
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	
	/**
	 * @return the employeeName
	 */
	public String getEmployeeName() {
		return employeeName;
	}
	
	/**
	 * @param employeeName the employeeName to set
	 */
	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}
	
	/**
	 * @return the isAccepted
	 */
	public boolean getIsAccepted() {
		return isAccepted;
	}
	
	/**
	 * @param isAccepted the isAccepted to set
	 */
	public void setIsAccepted(boolean isAccepted) {
		this.isAccepted = isAccepted;
	}
	
	/**
	 * @return the causes
	 */
	public Collection<ShiftConstraintDto> getCauses() {
		return causes;
	}
	
	/**
	 * @param causes the causes to set
	 */
	public void setCauses(Collection<ShiftConstraintDto> causes) {
		this.causes = causes;
	}

	@Override
	public String toString() {
		return "ShiftQualificationDto [shiftId=" + shiftId + ", employeeId=" + employeeId + ", employeeName="
				+ employeeName + ", isAccepted=" + isAccepted + ", causes=" + causes + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((employeeId == null) ? 0 : employeeId.hashCode());
		result = prime * result + ((shiftId == null) ? 0 : shiftId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ShiftQualificationDto other = (ShiftQualificationDto) obj;
		if (employeeId == null) {
			if (other.employeeId != null)
				return false;
		} else if (!employeeId.equals(other.employeeId))
			return false;
		if (shiftId == null) {
			if (other.shiftId != null)
				return false;
		} else if (!shiftId.equals(other.shiftId))
			return false;
		return true;
	}

	@Override
	public int compareTo(Object o) {
		ShiftQualificationDto dto = (ShiftQualificationDto) o;
		return new CompareToBuilder().append(shiftId, dto.shiftId)
				.append(employeeId, dto.employeeId).toComparison();
	}

}
