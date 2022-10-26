package com.emlogis.model.workflow.dto.process.request.submit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Created by alexborlis on 08.02.15.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AvailabilitySubmitDto.class, name = "AVAILABILITY_REQUEST"),
        @JsonSubTypes.Type(value = OpenShiftSubmitDto.class, name = "OPEN_SHIFT_REQUEST"),
        @JsonSubTypes.Type(value = ShiftSwapSubmitDto.class, name = "SHIFT_SWAP_REQUEST"),
        @JsonSubTypes.Type(value = WorkInPlaceSubmitDto.class, name = "WIP_REQUEST"),
        @JsonSubTypes.Type(value = TimeOffSubmitDto.class, name = "TIME_OFF_REQUEST")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmitDto implements Serializable {

    @JsonProperty(value = "type", required = true)
    private String type;

    @JsonProperty(value = "submitterId", required = true)
    private String submitterId;

    @JsonProperty(value = "expiration", required = true)
    private Long expiration;

    @JsonProperty(value = "comment", required = false)
    private String comment;

    public SubmitDto() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(String submitterId) {
        this.submitterId = submitterId;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
