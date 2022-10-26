package com.emlogis.rest.resources.util;

import com.emlogis.model.employee.CDAvailabilityTimeFrame;
import com.emlogis.model.employee.dto.CDAvailabilityTimeFrameDto;

public class CDAvailabilityTimeFrameDtoMapper extends DtoMapper<CDAvailabilityTimeFrame, CDAvailabilityTimeFrameDto> {

    public CDAvailabilityTimeFrameDtoMapper() {
        registerNestedDtoMapping("absenceTypeDto", "absenceType");
    }

}