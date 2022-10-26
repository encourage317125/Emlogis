package com.emlogis.model.workflow.dto.commentary;

import java.io.Serializable;

/**
 * Created by user on 15.07.15.
 */
public class RequestCommentDto implements Serializable {

    private Long datetime;
    private String employeeName;
    private String comment;

    public RequestCommentDto() {
    }

    public RequestCommentDto(
            Long datetime,
            String employeeName,
            String comment
    ) {
        this.datetime = datetime;
        this.employeeName = employeeName;
        this.comment = comment;
    }

    public Long getDatetime() {
        return datetime;
    }

    public void setDatetime(Long datetime) {
        this.datetime = datetime;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
