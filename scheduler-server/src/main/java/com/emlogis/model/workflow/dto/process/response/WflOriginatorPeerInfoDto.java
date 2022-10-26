package com.emlogis.model.workflow.dto.process.response;

import com.emlogis.model.workflow.dto.task.TaskShiftBriefInfoDto;
import com.emlogis.workflow.enums.status.WorkflowRequestStatusDict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by lucas on 25.05.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WflOriginatorPeerInfoDto implements Serializable {

    private String name;
    private String employeeId;
    private WorkflowRequestStatusDict status;
    private Long date;
    private String comment;
    private TaskShiftBriefInfoDto recipientShift;
    private String description;
    private String reviewerId;
    private String reviewerName;
    private Long reviewed;

    public WflOriginatorPeerInfoDto() {
    }

    public WflOriginatorPeerInfoDto(
            String name, String employeeId, WorkflowRequestStatusDict status,
            Long date, String comment, TaskShiftBriefInfoDto recipientShift
    ) {
        this.name = name;
        this.employeeId = employeeId;
        this.status = status;
        this.date = date;
        this.comment = comment;
        this.recipientShift = recipientShift;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public WorkflowRequestStatusDict getStatus() {
        return status;
    }

    public void setStatus(WorkflowRequestStatusDict status) {
        this.status = status;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public TaskShiftBriefInfoDto getRecipientShift() {
        return recipientShift;
    }

    public void setRecipientShift(TaskShiftBriefInfoDto recipientShift) {
        this.recipientShift = recipientShift;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public Long getReviewed() {
        return reviewed;
    }

    public void setReviewed(Long reviewed) {
        this.reviewed = reviewed;
    }
}
