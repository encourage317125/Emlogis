package com.emlogis.model.shiftpattern.dto;

import com.emlogis.model.dto.CreateDto;

public class ShiftTypeCreateDto extends CreateDto<ShiftTypeUpdateDto> {

    private String shiftLengthId;

    public String getShiftLengthId() {
        return shiftLengthId;
    }

    public void setShiftLengthId(String shiftLengthId) {
        this.shiftLengthId = shiftLengthId;
    }

}
