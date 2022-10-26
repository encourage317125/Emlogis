package com.emlogis.model.workflow.dto.process.request.submit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by Alexander on 21.10.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenShiftSubmitItemDto implements Serializable {

    @JsonProperty(value = "shiftId", required = true)
    private String shiftId;
    @JsonProperty(value = "identifier", required = true)
    private String identifier;

    public OpenShiftSubmitItemDto() {
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
