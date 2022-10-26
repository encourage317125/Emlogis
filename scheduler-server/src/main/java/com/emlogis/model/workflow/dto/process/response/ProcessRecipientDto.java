package com.emlogis.model.workflow.dto.process.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by user on 17.04.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessRecipientDto implements Serializable {

    @JsonProperty(value = "employeeId", required = true)
    private String employeeId;

    @JsonProperty(value = "shiftId", required = false)
    private String shiftId;

    public ProcessRecipientDto() {
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
