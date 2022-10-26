package com.emlogis.rest.resources.util;

import com.emlogis.model.employee.EmployeeTeam;
import com.emlogis.model.employee.dto.EmployeeTeamViewDto;

public class EmployeeTeamDtoMapper extends DtoMapper<EmployeeTeam, EmployeeTeamViewDto> {

    public EmployeeTeamDtoMapper() {
        registerNestedDtoMapping("teamDto", "team");
        registerNestedDtoMapping("employeeSummaryDto", "employee");
    }

}
