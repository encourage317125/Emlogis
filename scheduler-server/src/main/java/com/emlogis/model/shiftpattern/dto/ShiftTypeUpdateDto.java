package com.emlogis.model.shiftpattern.dto;

import com.emlogis.model.dto.UpdateDto;

public class ShiftTypeUpdateDto extends UpdateDto {

    private String name;
    private Integer paidTimeInMin;
    private String description;
    private Long startTime;
    private boolean isActive = true;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public Integer getPaidTimeInMin() {
		return paidTimeInMin;
	}

	public void setPaidTimeInMin(Integer paidTimeInMin) {
		this.paidTimeInMin = paidTimeInMin;
	}

	public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

}
