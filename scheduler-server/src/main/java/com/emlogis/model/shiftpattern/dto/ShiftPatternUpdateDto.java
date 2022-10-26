package com.emlogis.model.shiftpattern.dto;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.model.dto.UpdateDto;
import com.emlogis.model.shiftpattern.ShiftPatternType;

import java.util.Collection;

public class ShiftPatternUpdateDto extends UpdateDto {

    public static final String DAY_OF_WEEK = "dayOfWeek";
    public static final String CD_DATE = "cdDate";

    private String name;
    private String description;
    private ShiftPatternType type;
    private DayOfWeek dayOfWeek;
    private Long cdDate;
    private Integer maxEmployeeCount;
    private String shiftLengthList;
//    private Boolean isShiftStructureGenerated;
    private Collection<ShiftReqDto> shiftReqDtos;
    private Collection<ShiftDemandDto> shiftDemandDtos;

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

    public Integer getMaxEmployeeCount() {
        return maxEmployeeCount;
    }

    public void setMaxEmployeeCount(Integer maxEmployeeCount) {
        this.maxEmployeeCount = maxEmployeeCount;
    }

    public String getShiftLengthList() {
        return shiftLengthList;
    }

    public void setShiftLengthList(String shiftLengthList) {
        this.shiftLengthList = shiftLengthList;
    }
/*
    public Boolean isShiftStructureGenerated() {
        return isShiftStructureGenerated;
    }

    public void setShiftStructureGenerated(Boolean isShiftStructureGenerated) {
        this.isShiftStructureGenerated = isShiftStructureGenerated;
    }
*/
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
}
