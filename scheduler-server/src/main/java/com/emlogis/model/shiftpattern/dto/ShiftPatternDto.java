package com.emlogis.model.shiftpattern.dto;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.model.dto.BaseEntityDto;
import com.emlogis.model.shiftpattern.ShiftPatternType;

import java.util.Collection;

public class ShiftPatternDto extends BaseEntityDto {

    public final static String NAME = "name";

    private String name;
    private String description;
    private ShiftPatternType type;
    private DayOfWeek dayOfWeek;
    private Long cdDate;
    private int maxEmployeeCount;
    private String shiftLengthList;
    private boolean isShiftStructureGenerated;
    private Collection<ShiftReqDto> shiftReqDtos;
    private Collection<ShiftDemandDto> shiftDemandDtos;
    private String teamId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ShiftPatternType getType() {
        return type;
    }

    public void setType(ShiftPatternType type) {
        this.type = type;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Long getCdDate() {
        return cdDate;
    }

    public void setCdDate(Long cdDate) {
        this.cdDate = cdDate;
    }

    public int getMaxEmployeeCount() {
        return maxEmployeeCount;
    }

    public void setMaxEmployeeCount(int maxEmployeeCount) {
        this.maxEmployeeCount = maxEmployeeCount;
    }

    public String getShiftLengthList() {
        return shiftLengthList;
    }

    public void setShiftLengthList(String shiftLengthList) {
        this.shiftLengthList = shiftLengthList;
    }

    public boolean isShiftStructureGenerated() {
        return isShiftStructureGenerated;
    }

    public void setShiftStructureGenerated(boolean isShiftStructureGenerated) {
        this.isShiftStructureGenerated = isShiftStructureGenerated;
    }

    public Collection<ShiftReqDto> getShiftReqDtos() {
        return shiftReqDtos;
    }

    public void setShiftReqDtos(Collection<ShiftReqDto> shiftReqDtos) {
        this.shiftReqDtos = shiftReqDtos;
    }

    public Collection<ShiftDemandDto> getShiftDemandDtos() {
        return shiftDemandDtos;
    }

    public void setShiftDemandDtos(Collection<ShiftDemandDto> shiftDemandDtos) {
        this.shiftDemandDtos = shiftDemandDtos;
    }

	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}
}
