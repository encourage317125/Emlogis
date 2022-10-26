package com.emlogis.model.schedule.dto;

import com.emlogis.model.schedule.TaskState;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class CandidateShiftEligibleEmployeesDto implements Serializable {
	
	public static class EmployeeDescriptorDto implements Serializable {
		public String employeeId;
		public String employeeName;
		public String homeTeamName;
		public String primarySkillName;
		public String primarySkillAbbreviation;
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((employeeId == null) ? 0 : employeeId.hashCode());
			result = prime * result
					+ ((employeeName == null) ? 0 : employeeName.hashCode());
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
			EmployeeDescriptorDto other = (EmployeeDescriptorDto) obj;
			if (employeeId == null) {
				if (other.employeeId != null)
					return false;
			} else if (!employeeId.equals(other.employeeId))
				return false;
			if (employeeName == null) {
				if (other.employeeName != null)
					return false;
			} else if (!employeeName.equals(other.employeeName))
				return false;
			return true;
		}
		
		
	}
	
	private String requestId;
	private TaskState requesetState = TaskState.Idle;
	private Set<EmployeeDescriptorDto> eligibleEmployees = new HashSet<EmployeeDescriptorDto>();
	
	public String getRequestId() {
		return requestId;
	}
	
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	
	public TaskState getState() {
		return requesetState;
	}
	
	public void setState(TaskState state) {
		this.requesetState = state;
	}

	public Set<EmployeeDescriptorDto> getEligibleEmployees() {
		return eligibleEmployees;
	}

	public void setEligibleEmployees(Set<EmployeeDescriptorDto> eligibleTeammates) {
		this.eligibleEmployees = eligibleTeammates;
	}
	
	
}
