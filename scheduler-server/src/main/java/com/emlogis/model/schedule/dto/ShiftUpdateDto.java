package com.emlogis.model.schedule.dto;

import com.emlogis.model.dto.UpdateDto;

public class ShiftUpdateDto extends UpdateDto {
	
	private String skillId;

	private Long startDateTime;

	private Long endDateTime;

	private Integer paidTime;


    public String getSkillId() {
        return skillId;
    }

    public void setSkillId(String skillId) {
        this.skillId = skillId;
    }

    public Long getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Long startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Long getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Long endDateTime) {
        this.endDateTime = endDateTime;
    }

	/**
	 * @return the paidTime
	 */
	public Integer getPaidTime() {
		return paidTime;
	}

	/**
	 * @param paidTime the paidTime to set
	 */
	public void setPaidTime(Integer paidTime) {
		this.paidTime = paidTime;
	}
}
