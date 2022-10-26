package com.emlogis.model.schedule.dto;

public class ShiftTimeQualificationExecuteDto extends ExecuteDto {

	private int maxSynchronousWaitSeconds = 60;
	private String shiftId;
	private Long proposedNewStartDateTime;
	private Long proposedNewEndDateTime;

	public int getMaxSynchronousWaitSeconds() {
		return maxSynchronousWaitSeconds;
	}

	public void setMaxSynchronousWaitSeconds(int maxSynchronousWaitSeconds) {
		this.maxSynchronousWaitSeconds = maxSynchronousWaitSeconds;
	}

	public String getShiftId() {
		return shiftId;
	}

	public void setShiftId(String shiftId) {
		this.shiftId = shiftId;
	}

	public Long getProposedNewStartDateTime() {
		return proposedNewStartDateTime;
	}

	public void setProposedNewStartDateTime(Long proposedNewStartDateTime) {
		this.proposedNewStartDateTime = proposedNewStartDateTime;
	}

	public Long getProposedNewEndDateTime() {
		return proposedNewEndDateTime;
	}

	public void setProposedNewEndDateTime(Long proposedNewEndDateTime) {
		this.proposedNewEndDateTime = proposedNewEndDateTime;
	}

}
