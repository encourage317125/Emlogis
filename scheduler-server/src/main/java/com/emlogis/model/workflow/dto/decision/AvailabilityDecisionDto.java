package com.emlogis.model.workflow.dto.decision;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

/**
 * Created by root on 02.06.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("AVAILABILITY_REQUEST")
public class AvailabilityDecisionDto extends WorkflowDecisionDto  implements Serializable {

    public AvailabilityDecisionDto() {
    }
}
