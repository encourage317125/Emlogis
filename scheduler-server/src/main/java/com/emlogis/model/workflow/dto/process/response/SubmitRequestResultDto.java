package com.emlogis.model.workflow.dto.process.response;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by user on 21.07.15.
 */
public class SubmitRequestResultDto {

    private String submitterId;

    private String submitterName;

    private Set<SuccessSubmitResultDto> created;

    private Set<ErrorSubmitResultDto> errors;

    public SubmitRequestResultDto() {
    }

    public SubmitRequestResultDto(String submitterId, String submitterName) {
        this.submitterId = submitterId;
        this.submitterName = submitterName;
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
	
	public Set<SuccessSubmitResultDto> getCreated() {
        if (created == null) {
            created = new HashSet<>();
        }
        return created;
    }

    public void setCreated(Set<SuccessSubmitResultDto> created) {
        this.created = created;
    }

    public Set<ErrorSubmitResultDto> getErrors() {
        if (errors == null) {
            errors = new HashSet<>();
        }
        return errors;
    }

    public void setErrors(Set<ErrorSubmitResultDto> errors) {
        this.errors = errors;
    }
}
