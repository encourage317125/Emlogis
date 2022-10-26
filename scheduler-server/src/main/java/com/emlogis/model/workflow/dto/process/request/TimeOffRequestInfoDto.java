package com.emlogis.model.workflow.dto.process.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by lucas on 28.05.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeOffRequestInfoDto implements Serializable {

    private Long date;
    private String absenceTypeId;
    private String reason;
    private String identifier;

    public TimeOffRequestInfoDto() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getAbsenceTypeId() {
        return absenceTypeId;
    }

    public void setAbsenceTypeId(String absenceTypeId) {
        this.absenceTypeId = absenceTypeId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
