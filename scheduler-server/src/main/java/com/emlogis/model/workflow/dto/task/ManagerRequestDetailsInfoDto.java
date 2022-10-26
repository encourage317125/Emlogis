package com.emlogis.model.workflow.dto.task;

import com.emlogis.model.workflow.dto.details.abstracts.AbstractRequestDetailsInfoDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ManagerRequestDetailsInfoDto extends AbstractRequestDetailsInfoDto implements Serializable {

    @JsonProperty(value = "description", required = true)
    private String description;

    @JsonProperty(value = "numberOfRecipients", required = true)
    private Integer numberOfRecipients;

    @JsonProperty(value = "numberOfRecipientsApproved", required = true)
    private Integer numberOfRecipientsApproved;

    @JsonProperty(value = "reviewerId", required = false)
    private String reviewerId;

    @JsonProperty(value = "reviewerName", required = false)
    private String reviewerName;

    @JsonProperty(value = "reviewed", required = false)
    private Long reviewed;

    @JsonProperty(value = "isRead", required = true)
    private Boolean isRead;

    public ManagerRequestDetailsInfoDto() {
        super();
    }

    public ManagerRequestDetailsInfoDto(
            String processId, String type, Long submitionDate, Long eventDate, Long expirationDate,
            String status, TaskShiftBriefInfoDto originatorShift, String originatorName, String originatorId,
            String description, String submitterTeamName, String submitterTeamId,
            String submitterSiteName, String submitterSiteId,
            Integer numberOfRecipients, Integer numberOfRecipientsApproved,
            String reviewerId, String reviewerName, Long reviewed
    ) {
        super(processId, type, submitionDate, eventDate, expirationDate, originatorShift, originatorId, originatorName,
                submitterTeamName, submitterTeamId, submitterSiteName, submitterSiteId, status);
        this.description = description;
        this.numberOfRecipients = numberOfRecipients;
        this.numberOfRecipientsApproved = numberOfRecipientsApproved;
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.reviewed = reviewed;

    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getNumberOfRecipients() {
        return numberOfRecipients;
    }

    public void setNumberOfRecipients(Integer numberOfRecipients) {
        this.numberOfRecipients = numberOfRecipients;
    }

    public Integer getNumberOfRecipientsApproved() {
        return numberOfRecipientsApproved;
    }

    public void setNumberOfRecipientsApproved(Integer numberOfRecipientsApproved) {
        this.numberOfRecipientsApproved = numberOfRecipientsApproved;
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
