package com.emlogis.model.structurelevel.dto;

import com.emlogis.model.dto.CreateDto;

public class SiteCreateDto extends CreateDto<SiteUpdateDto> {

    public static final String NAME = "updateDto.name";
    public static final String DESCRIPTION = "updateDto.description";
    public static final String ABBREVIATION = "updateDto.abbreviation";
    public static final String ADDRESS = "updateDto.address";
    public static final String ADDRESS2 = "updateDto.address2";
    public static final String CITY = "updateDto.city";
    public static final String STATE = "updateDto.state";
    public static final String COUNTRY = "updateDto.country";
    public static final String ZIP = "updateDto.zip";
    public static final String SHIFT_INCREMENTS = "updateDto.shiftIncrements";
    public static final String SHIFT_OVERLAPS = "updateDto.shiftOverlaps";
    public static final String MAX_CONSECUTIVE_SHIFTS = "updateDto.maxConsecutiveShifts";
    public static final String TIME_OFF_BETWEEN_SHIFTS = "updateDto.timeOffBetweenShifts";

    private Integer[] defaultShiftLengths;

    public Integer[] getDefaultShiftLengths() {
        return defaultShiftLengths;
    }

    public void setDefaultShiftLengths(Integer[] defaultShiftLengths) {
        this.defaultShiftLengths = defaultShiftLengths;
    }
}
