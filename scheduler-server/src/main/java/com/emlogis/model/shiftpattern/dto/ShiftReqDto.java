package com.emlogis.model.shiftpattern.dto;

import com.emlogis.model.dto.BaseEntityDto;

public class ShiftReqDto extends BaseEntityDto {

    private String shiftTypeId;
    private String shiftLengthId;
    private int employeeCount;
    private int excessCount;

    public String getShiftTypeId() {
        return shiftTypeId;
    }

    public void setShiftTypeId(String shiftTypeId) {
        this.shiftTypeId = shiftTypeId;
    }

    public String getShiftLengthId() {
        return shiftLengthId;
    }

    public void setShiftLengthId(String shiftLengthId) {
        this.shiftLengthId = shiftLengthId;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }

    public int getExcessCount() {
        return excessCount;
    }

    public void setExcessCount(int excessCount) {
        this.excessCount = excessCount;
    }
}

