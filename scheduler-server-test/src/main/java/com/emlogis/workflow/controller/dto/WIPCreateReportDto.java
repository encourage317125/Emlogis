package com.emlogis.workflow.controller.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by user on 01.10.15.
 */
public class WIPCreateReportDto implements Serializable {

    private String submitterId;

    private String submitterName;

    private Set<SuccessSubmitDto> created;

    private Set<ErrorSubmitDto> errors;

    public WIPCreateReportDto() {
    }

    public String getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(String submitterId) {
        this.submitterId = submitterId;
    }

    public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }

    public Set<SuccessSubmitDto> getCreated() {
        if (created == null) {
            created = new HashSet<>();
        }
        return created;
    }

    public void setCreated(Set<SuccessSubmitDto> created) {
        this.created = created;
    }

    public Set<ErrorSubmitDto> getErrors() {
        if (errors == null) {
            errors = new HashSet<>();
        }
        return errors;
    }

    public void setErrors(Set<ErrorSubmitDto> errors) {
        this.errors = errors;
    }
}
