package com.emlogis.model.workflow.dto.decision;

import com.emlogis.workflow.enums.WorkflowShiftManagerActionDict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 03.06.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShiftDecisionAction implements Serializable {

    @JsonProperty(value = "shiftId", required = true)
    private String shiftId;

    @JsonProperty(value = "action", required = true)
    private WorkflowShiftManagerActionDict action;

    @JsonProperty(value = "employeeIds", required = false)
    private List<String> employeeIds;

    public ShiftDecisionAction() {
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public WorkflowShiftManagerActionDict getAction() {
        return action;
    }

    public void setAction(WorkflowShiftManagerActionDict action) {
        this.action = action;
    }

    public List<String> getEmployeeIds() {
        if (employeeIds == null) {
            employeeIds = new ArrayList<>();
        }
        return employeeIds;
    }

    public void setEmployeeIds(List<String> employeeIds) {
        this.employeeIds = employeeIds;
    }
}
