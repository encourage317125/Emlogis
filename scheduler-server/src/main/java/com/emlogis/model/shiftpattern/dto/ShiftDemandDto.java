package com.emlogis.model.shiftpattern.dto;

import com.emlogis.model.dto.BaseEntityDto;

public class ShiftDemandDto extends BaseEntityDto {

    private Long startTime;
    private int lengthInMin;
    private int employeeCount;

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public int getLengthInMin() {
        return lengthInMin;
    }

    public void setLengthInMin(int lengthInMin) {
        this.lengthInMin = lengthInMin;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }
}
