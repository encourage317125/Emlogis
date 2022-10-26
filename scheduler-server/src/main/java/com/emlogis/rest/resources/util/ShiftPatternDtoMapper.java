package com.emlogis.rest.resources.util;

import com.emlogis.model.shiftpattern.ShiftPattern;
import com.emlogis.model.shiftpattern.dto.ShiftPatternDto;

public class ShiftPatternDtoMapper extends DtoMapper<ShiftPattern, ShiftPatternDto> {

    public ShiftPatternDtoMapper() {
        registerNestedDtoMapping("shiftReqDtos", "shiftReqs");
        registerNestedDtoMapping("shiftDemandDtos", "shiftDemands");
    }

}