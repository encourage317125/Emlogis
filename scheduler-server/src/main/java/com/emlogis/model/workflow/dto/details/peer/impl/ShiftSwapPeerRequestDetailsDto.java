package com.emlogis.model.workflow.dto.details.peer.impl;

import com.emlogis.model.workflow.dto.details.peer.DetailedPeerRequestDetailsDto;
import com.emlogis.model.workflow.dto.details.peer.PeerDetailsInfo;
import com.emlogis.model.workflow.dto.task.TaskRecipientInfoDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 04.06.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShiftSwapPeerRequestDetailsDto extends DetailedPeerRequestDetailsDto implements PeerDetailsInfo {

    @JsonProperty(value = "recipients", required = true)
    private List<TaskRecipientInfoDto> recipients;

    public ShiftSwapPeerRequestDetailsDto() {
    }

    public ShiftSwapPeerRequestDetailsDto(DetailedPeerRequestDetailsDto prnt) {
        super(prnt.getRequestId(), prnt.getType(), prnt.getSubmitDate(), prnt.getEventDate(), prnt.getExpirationDate(),
                prnt.getSubmitterShift(), prnt.getSubmitterId(), prnt.getSubmitterName(), prnt.getSubmitterTeamName(),
                prnt.getSubmitterTeamId(), prnt.getSubmitterSiteName(), prnt.getSubmitterSiteId(), prnt.getStatus(),
                prnt.getApprovalNeeded(), prnt.getComment(), prnt.getDateOfAction(),
                prnt.getDescription(), prnt.getCommentary(), prnt.getIsRead());
    }

    public List<TaskRecipientInfoDto> getRecipients() {
        if (recipients == null) {
            recipients = new ArrayList<>();
        }
        return recipients;
    }

    public void setRecipients(List<TaskRecipientInfoDto> recipients) {
        this.recipients = recipients;
    }
}
