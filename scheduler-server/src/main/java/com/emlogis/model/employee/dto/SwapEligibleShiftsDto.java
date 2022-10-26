package com.emlogis.model.employee.dto;

import com.emlogis.model.schedule.TaskState;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class SwapEligibleShiftsDto implements Serializable {
	
	public static class SwappableShiftDescriptor implements Serializable {
		public String employeeId;
		public String employeeName;
		public String shiftId;
		public String teamName;
		public String skillName;
		public Long startDateTime;
		public Long endDateTime;
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((employeeId == null) ? 0 : employeeId.hashCode());
			result = prime * result
					+ ((employeeName == null) ? 0 : employeeName.hashCode());
			result = prime * result
					+ ((endDateTime == null) ? 0 : endDateTime.hashCode());
			result = prime * result
					+ ((shiftId == null) ? 0 : shiftId.hashCode());
			result = prime * result
					+ ((skillName == null) ? 0 : skillName.hashCode());
			result = prime * result
					+ ((startDateTime == null) ? 0 : startDateTime.hashCode());
			result = prime * result
					+ ((teamName == null) ? 0 : teamName.hashCode());
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
			SwappableShiftDescriptor other = (SwappableShiftDescriptor) obj;
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
			if (endDateTime == null) {
				if (other.endDateTime != null)
					return false;
			} else if (!endDateTime.equals(other.endDateTime))
				return false;
			if (shiftId == null) {
				if (other.shiftId != null)
					return false;
			} else if (!shiftId.equals(other.shiftId))
				return false;
			if (skillName == null) {
				if (other.skillName != null)
					return false;
			} else if (!skillName.equals(other.skillName))
				return false;
			if (startDateTime == null) {
				if (other.startDateTime != null)
					return false;
			} else if (!startDateTime.equals(other.startDateTime))
				return false;
			if (teamName == null) {
				if (other.teamName != null)
					return false;
			} else if (!teamName.equals(other.teamName))
				return false;
			return true;
		}		
	}
	
	private String requestId;
	private TaskState requestState = TaskState.Idle;
	private Set<SwappableShiftDescriptor> swappableShifts = new HashSet<SwappableShiftDescriptor>();
	
	public String getRequestId() {
		return requestId;
	}
	
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	
	public TaskState getRequestState() {
		return requestState;
	}
	
	public void setRequestState(TaskState requesetState) {
		this.requestState = requesetState;
	}
	
	public Set<SwappableShiftDescriptor> getSwappableShifts() {
		return swappableShifts;
	}
	
	public void setSwappableShifts(Set<SwappableShiftDescriptor> swappableShifts) {
		this.swappableShifts = swappableShifts;
	}
}
