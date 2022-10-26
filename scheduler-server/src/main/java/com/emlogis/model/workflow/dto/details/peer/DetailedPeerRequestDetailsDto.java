package com.emlogis.model.workflow.dto.details.peer;

import com.emlogis.model.workflow.dto.commentary.RequestCommentary;
import com.emlogis.model.workflow.dto.details.abstracts.AbstractRequestDetailsInfoDto;
import com.emlogis.model.workflow.dto.task.TaskShiftBriefInfoDto;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by user on 21.08.15.
 */
public class DetailedPeerRequestDetailsDto extends AbstractRequestDetailsInfoDto implements PeerDetailsInfo {

    @JsonProperty(value = "approvalNeeded", required = true)
    private Boolean approvalNeeded = true;

    @JsonProperty(value = "comment", required = false)
    private String comment;

    @JsonProperty(value = "dateOfAction", required = false)
    private Long dateOfAction;

    @JsonProperty(value = "description", required = true)
    private String description;

    @JsonProperty(value = "commentary", required = true)
    private RequestCommentary commentary;


    @JsonProperty(value = "isRead", required = false)
    private Boolean isRead;

    public DetailedPeerRequestDetailsDto() {
        super();
    }

    public DetailedPeerRequestDetailsDto(
            AbstractRequestDetailsInfoDto prnt,
            Boolean approvalNeeded,
            Long dateOfAction,
            String comment,
            String description,
            RequestCommentary commentary,
            Boolean isRead
    ) {
        super(prnt.getRequestId(), prnt.getType(), prnt.getSubmitDate(), prnt.getEventDate(),
                prnt.getExpirationDate(), prnt.getSubmitterShift(), prnt.getSubmitterId(),
                prnt.getSubmitterName(), prnt.getSubmitterTeamName(), prnt.getSubmitterTeamId(),
                prnt.getSubmitterSiteName(), prnt.getSubmitterSiteId(), prnt.getStatus());
        this.approvalNeeded = approvalNeeded;
        this.dateOfAction = dateOfAction;
        this.comment = comment;
        this.commentary = commentary;
        this.description = description;
        this.isRead = isRead;
    }

    public DetailedPeerRequestDetailsDto(
            String requestId,
            String type,
            Long submitionDate,
            Long eventDate,
            Long expirationDate,
            TaskShiftBriefInfoDto submitterShift,
            String submitterId,
            String submitterName,
            String submitterTeamName,
            String submitterTeamId,
            String submitterSiteName,
            String submitterSiteId,
            String status,
            Boolean approvalNeeded,
            String comment,
            Long dateOfAction,
            String description,
            RequestCommentary commentary,
            Boolean isRead
    ) {
        super(requestId, type, submitionDate, eventDate, expirationDate, submitterShift, submitterId, submitterName,
                submitterTeamName, submitterTeamId, submitterSiteName, submitterSiteId, status);
        this.approvalNeeded = approvalNeeded;
        this.comment = comment;
        this.dateOfAction = dateOfAction;
        this.commentary = commentary;
        this.description = description;
        this.isRead = isRead;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Boolean getApprovalNeeded() {
        return approvalNeeded;
    }

    public void setApprovalNeeded(Boolean approvalNeeded) {
        this.approvalNeeded = approvalNeeded;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getDateOfAction() {
        return dateOfAction;
    }

    public void setDateOfAction(Long dateOfAction) {
        this.dateOfAction = dateOfAction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RequestCommentary getCommentary() {
        return commentary;
    }

    public void setCommentary(RequestCommentary commentary) {
        this.commentary = commentary;
    }


}
