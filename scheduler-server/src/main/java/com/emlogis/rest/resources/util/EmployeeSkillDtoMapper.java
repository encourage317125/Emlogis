package com.emlogis.rest.resources.util;

import com.emlogis.model.employee.EmployeeSkill;
import com.emlogis.model.employee.dto.EmployeeSkillViewDto;

public class EmployeeSkillDtoMapper extends DtoMapper<EmployeeSkill, EmployeeSkillViewDto> {

    public EmployeeSkillDtoMapper() {
        registerNestedDtoMapping("skillDto", "skill");
        registerNestedDtoMapping("employeeSummaryDto", "employee");
    }

}
