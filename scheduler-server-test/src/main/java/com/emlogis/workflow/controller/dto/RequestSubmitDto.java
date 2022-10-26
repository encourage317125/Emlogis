package com.emlogis.workflow.controller.dto;

import java.io.Serializable;

/**
 * Created by user on 02.10.15.
 */
public class RequestSubmitDto implements Serializable {

    private String type;
    private String submitterId;
    private Long expiration;
    private String comment;

    public RequestSubmitDto() {
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
