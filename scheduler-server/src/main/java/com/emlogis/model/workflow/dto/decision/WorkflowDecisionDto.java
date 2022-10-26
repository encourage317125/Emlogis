package com.emlogis.model.workflow.dto.decision;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Created by alexborlis on 22.01.15.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        defaultImpl = PeerDecisionDto.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = WipDecisionDto.class, name = "WIP_REQUEST"),
        @JsonSubTypes.Type(value = OpenShiftDecisionDto.class, name = "OPEN_SHIFT_REQUEST"),
        @JsonSubTypes.Type(value = SwapDecisionDto.class, name = "SHIFT_SWAP_REQUEST"),
        @JsonSubTypes.Type(value = AvailabilityDecisionDto.class, name = "AVAILABILITY_REQUEST"),
        @JsonSubTypes.Type(value = TimeOffDecisionDto.class, name = "TIME_OFF_REQUEST"),
        @JsonSubTypes.Type(value = PeerDecisionDto.class, name = "PEER")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowDecisionDto implements Serializable {

    @JsonProperty(value = "comment", required = false)
    private String comment;

    public WorkflowDecisionDto() {
    }

    public WorkflowDecisionDto(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
