package com.emlogis.engine.domain.communication.constraints;

public class TeamShiftConstraintDto extends ShiftConstraintDto {
	
	private String requiredTeamId;

	/**
	 * @return the requiredTeamId
	 */
	public String getRequiredTeamId() {
		return requiredTeamId;
	}

	/**
	 * @param requiredTeamId the requiredTeamId to set
	 */
	public void setRequiredTeamId(String requiredTeamId) {
		this.requiredTeamId = requiredTeamId;
	}
	
}
