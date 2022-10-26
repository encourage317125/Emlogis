package com.emlogis.model.workflow.dto.decision;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

/**
 * Created by root on 02.06.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("OPEN_SHIFT_REQUEST")
public class OpenShiftDecisionDto extends WorkflowDecisionDto  implements Serializable {

    public OpenShiftDecisionDto() {
        super();
    }


    public OpenShiftDecisionDto(String comment) {
        super(comment);
    }
}
