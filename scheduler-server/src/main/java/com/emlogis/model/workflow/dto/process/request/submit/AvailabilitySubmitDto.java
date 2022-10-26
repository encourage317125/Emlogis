package com.emlogis.model.workflow.dto.process.request.submit;

import com.emlogis.model.employee.dto.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

/**
 * Created by lucas on 28.05.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("AVAILABILITY_REQUEST")
public class AvailabilitySubmitDto extends SubmitDto implements Serializable {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = AvailcalUpdateParamsCDPrefDto.class, name = "AvailcalUpdateParamsCDPrefDto"),
            @JsonSubTypes.Type(value = AvailcalUpdateParamsCDAvailDto.class, name = "AvailcalUpdateParamsCDAvailDto"),
            @JsonSubTypes.Type(value = AvailcalUpdateParamsCIPrefDto.class, name = "AvailcalUpdateParamsCIPrefDto"),
            @JsonSubTypes.Type(value = AvailcalUpdateParamsCIAvailDto.class, name = "AvailcalUpdateParamsCIAvailDto"),
    })
    private AvailabilityWorkflowRequest availUpdate;


    public AvailabilitySubmitDto() {
    }

    public AvailabilityWorkflowRequest getAvailUpdate() {
        return availUpdate;
    }

    public void setAvailUpdate(AvailabilityWorkflowRequest availUpdate) {
        this.availUpdate = availUpdate;
    }
}
