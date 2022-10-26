package com.emlogis.model.shiftpattern.dto;

import java.util.Collection;

public class DraftDemandDto {

    private Collection<ShiftDemandDto> shiftDemandDtos;
    private Collection<String> allowedShiftLengthIds;

    public Collection<ShiftDemandDto> getShiftDemandDtos() {
        return shiftDemandDtos;
    }

    public void setShiftDemandDtos(Collection<ShiftDemandDto> shiftDemandDtos) {
        this.shiftDemandDtos = shiftDemandDtos;
    }

    public Collection<String> getAllowedShiftLengthIds() {
        return allowedShiftLengthIds;
    }

    public void setAllowedShiftLengthIds(Collection<String> allowedShiftLengthIds) {
        this.allowedShiftLengthIds = allowedShiftLengthIds;
    }
}
