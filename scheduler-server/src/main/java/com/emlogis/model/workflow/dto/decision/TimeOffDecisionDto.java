package com.emlogis.model.workflow.dto.decision;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 02.06.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("TIME_OFF_REQUEST")
public class TimeOffDecisionDto extends WorkflowDecisionDto implements Serializable {

    //@XmlElement(name = "shiftActions")
    @JsonProperty(value = "shiftActions", required = true)
    private List<ShiftDecisionAction> decisionActionList;

    public TimeOffDecisionDto() {
    }

    public List<ShiftDecisionAction> getDecisionActionList() {
        if (decisionActionList == null) {
            decisionActionList = new ArrayList<>();
        }
        return decisionActionList;
    }

    public void setDecisionActionList(List<ShiftDecisionAction> decisionActionList) {
        this.decisionActionList = decisionActionList;
    }
}
