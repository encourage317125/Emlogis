package com.emlogis.model.employee.dto;

import com.emlogis.model.schedule.TaskState;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class WipEligibleTeammatesDto implements Serializable {
	
	public static class TeammateDescriptorDto implements Serializable {
		public String employeeId;
		public String employeeName;
		
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
			TeammateDescriptorDto other = (TeammateDescriptorDto) obj;
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
	private Set<TeammateDescriptorDto> eligibleTeammates = new HashSet<TeammateDescriptorDto>();
	
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

	public Set<TeammateDescriptorDto> getEligibleTeammates() {
		return eligibleTeammates;
	}

	public void setEligibleTeammates(Set<TeammateDescriptorDto> eligibleTeammates) {
		this.eligibleTeammates = eligibleTeammates;
	}
	
	
}
