package com.emlogis.model.workflow.dto.process.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by alexborlis on 03.02.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WflRoleInstanceDto implements Serializable {

    private String employeeId;

    private String shiftId;

    public WflRoleInstanceDto() {
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }
}
