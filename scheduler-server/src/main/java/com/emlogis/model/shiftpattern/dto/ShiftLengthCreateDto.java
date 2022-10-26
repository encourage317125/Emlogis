package com.emlogis.model.shiftpattern.dto;

import com.emlogis.model.dto.CreateDto;

public class ShiftLengthCreateDto extends CreateDto<ShiftLengthUpdateDto> {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
