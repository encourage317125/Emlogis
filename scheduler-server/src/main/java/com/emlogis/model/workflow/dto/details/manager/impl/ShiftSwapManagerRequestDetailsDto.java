package com.emlogis.model.workflow.dto.details.manager.impl;

import com.emlogis.model.workflow.dto.details.manager.DetailedManagerRequestDetailsDto;
import com.emlogis.model.workflow.dto.details.manager.ManagerDetailsInfo;
import com.emlogis.model.workflow.dto.task.TaskRecipientInfoDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 04.06.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShiftSwapManagerRequestDetailsDto
        extends DetailedManagerRequestDetailsDto implements ManagerDetailsInfo {

    @JsonProperty(value = "recipients", required = true)
    private List<TaskRecipientInfoDto> recipients;

    public ShiftSwapManagerRequestDetailsDto() {
    }

    public ShiftSwapManagerRequestDetailsDto(DetailedManagerRequestDetailsDto prnt) {
        super(prnt.getRequestId(), prnt.getType(), prnt.getSubmitDate(), prnt.getEventDate(), prnt.getExpirationDate(),
                prnt.getSubmitterShift(), prnt.getSubmitterId(), prnt.getSubmitterName(), prnt.getSubmitterTeamName(),
                prnt.getSubmitterTeamId(), prnt.getSubmitterSiteName(), prnt.getSubmitterSiteId(), prnt.getStatus(),
                prnt.getManagerApprovalNeeded(), prnt.getComment(), prnt.getDateOfAction(),
                prnt.getDescription(), prnt.getCommentary(), prnt.getHistory(), prnt.getIsRead(), prnt.getReason());
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
