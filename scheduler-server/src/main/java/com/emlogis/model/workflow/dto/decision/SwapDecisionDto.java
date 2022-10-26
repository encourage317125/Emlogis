package com.emlogis.model.workflow.dto.decision;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

/**
 * Created by root on 02.06.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("SHIFT_SWAP_REQUEST")
public class SwapDecisionDto extends WorkflowDecisionDto  implements Serializable {

    @JsonProperty(value = "employeeId", required = false)
    private String chosenEmployee;

    @JsonProperty(value = "shiftId", required = false)
    private String shiftId;

    public SwapDecisionDto() {
    }

    public String getChosenEmployee() {
        return chosenEmployee;
    }

    public void setChosenEmployee(String chosenEmployee) {
        this.chosenEmployee = chosenEmployee;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }
}
