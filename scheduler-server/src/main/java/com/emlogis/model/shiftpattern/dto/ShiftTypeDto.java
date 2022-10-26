package com.emlogis.model.shiftpattern.dto;

import com.emlogis.model.dto.BaseEntityDto;

public class ShiftTypeDto extends BaseEntityDto {

    public final static String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String ISACTIVE = "isActive";
	public static final String STARTTIME = "startTime";
	public static final String PAIDTIMEINMIN = "paidTimeInMin";

    private String name;
    private int paidTimeInMin;
    private String description;
    private boolean isActive = true;
    private Long startTime;
    private String shiftLengthId;
    private Integer shiftLengthLength;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPaidTimeInMin() {
		return paidTimeInMin;
	}

	public void setPaidTimeInMin(int paidTimeInMin) {
		this.paidTimeInMin = paidTimeInMin;
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

	public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public String getShiftLengthId() {
        return shiftLengthId;
    }

    public void setShiftLengthId(String shiftLengthId) {
        this.shiftLengthId = shiftLengthId;
    }

    public Integer getShiftLengthLength() {
        return shiftLengthLength;
    }

    public void setShiftLengthLength(Integer shiftLengthLength) {
        this.shiftLengthLength = shiftLengthLength;
    }
}
