package com.emlogis.workflow.enums;

import com.emlogis.model.employee.dto.AvailcalUpdateParamsCDAvailDto;
import com.emlogis.model.employee.dto.AvailcalUpdateParamsCDPrefDto;
import com.emlogis.model.employee.dto.AvailcalUpdateParamsCIAvailDto;
import com.emlogis.model.employee.dto.AvailcalUpdateParamsCIPrefDto;

/**
 * Created by user on 02.07.15.
 */
public enum AvailabilityRequestSubtype {

    AvailcalUpdateParamsCDPrefDto(AvailcalUpdateParamsCDPrefDto.class, "updateAvailcalCDPref"),
    AvailcalUpdateParamsCDAvailDto(AvailcalUpdateParamsCDAvailDto.class, "updateAvailcalCDAvail"),
    AvailcalUpdateParamsCIPrefDto(AvailcalUpdateParamsCIPrefDto.class, "updateAvailcalCIPref"),
    AvailcalUpdateParamsCIAvailDto(AvailcalUpdateParamsCIAvailDto.class, "updateAvailcalCIAvail"),
    //AvailcalUpdateParamsWeekdayRotationDto(AvailcalUpdateParamsWeekdayRotationDto.class, "updateAvailcalWeekdayRotation"),
    NONE(Class.class, "none");

    /**
     * Class that identify the structure of input request DTO
     */
    private final Class clazz;
    /**
     * Name of the method from {@link com.emlogis.common.services.employee.EmployeeService} to be invoked
     */
    private final String methodname;

    AvailabilityRequestSubtype(Class clazzDto, String methodName) {
        this.clazz = clazzDto;
        this.methodname = methodName;
    }

    public Class getClazz() {
        return clazz;
    }

    public String getMethodname() {
        return methodname;
    }
}
