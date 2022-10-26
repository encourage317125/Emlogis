package com.emlogis.model.employee.dto;

import com.emlogis.workflow.enums.AvailabilityRequestSubtype;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Created by user on 07.07.15.
 */
public interface AvailabilityWorkflowRequest {

    @JsonIgnore
    String getActionStr();

    @JsonIgnore
    AvailabilityRequestSubtype getType();

}
