package com.emlogis.engine.domain.communication.constraints;

public class SkillShiftConstraintDto extends ShiftConstraintDto {
	
	private String requiredSkillId;

	/**
	 * @return the requiredSkillId
	 */
	public String getRequiredSkillId() {
		return requiredSkillId;
	}

	/**
	 * @param requiredSkillId the requiredSkillId to set
	 */
	public void setRequiredSkillId(String requiredSkillId) {
		this.requiredSkillId = requiredSkillId;
	}
	
}
