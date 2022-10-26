package com.emlogis.model.schedule.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QualificationExecuteDto extends ExecuteDto {

	private int maxSynchronousWaitSeconds = 60;
	private List<QualificationAssignment> qualificationAssignments = new ArrayList<>();

	public static class QualificationAssignment implements Serializable {
	    private String shiftId;
	    private String employeeId;

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
	}

	public int getMaxSynchronousWaitSeconds() {
		return maxSynchronousWaitSeconds;
	}

	public void setMaxSynchronousWaitSeconds(int maxSynchronousWaitSeconds) {
		this.maxSynchronousWaitSeconds = maxSynchronousWaitSeconds;
	}

	public List<QualificationAssignment> getQualificationAssignments() {
		return qualificationAssignments;
	}

	public void setQualificationAssignments(List<QualificationAssignment> qualificationAssignments) {
		this.qualificationAssignments = qualificationAssignments;
	}

}
