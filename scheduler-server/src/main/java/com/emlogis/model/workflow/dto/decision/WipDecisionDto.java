package com.emlogis.model.workflow.dto.decision;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

/**
 * Created by root on 02.06.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("WIP_REQUEST")
public class WipDecisionDto extends WorkflowDecisionDto implements Serializable {

    @JsonProperty(value = "employeeId", required = false)
    private String chosenEmployee;

    public WipDecisionDto() {
    }

    public String getChosenEmployee() {
        return chosenEmployee;
    }

    public void setChosenEmployee(String chosenEmployee) {
        this.chosenEmployee = chosenEmployee;
    }
}
