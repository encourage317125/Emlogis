package com.emlogis.model.workflow.dto.details.submitter.impl;

import com.emlogis.model.workflow.dto.details.submitter.DetailedSubmitterRequestDetailsDto;
import com.emlogis.model.workflow.dto.details.submitter.SubmitterDetailsInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by root on 04.06.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenShiftSubmitterRequestDetailsDto
        extends DetailedSubmitterRequestDetailsDto implements SubmitterDetailsInfo {

    public OpenShiftSubmitterRequestDetailsDto() {
    }

    public OpenShiftSubmitterRequestDetailsDto(DetailedSubmitterRequestDetailsDto prnt) {
        super(prnt.getRequestId(), prnt.getType(), prnt.getSubmitDate(), prnt.getEventDate(), prnt.getExpirationDate(),
                prnt.getSubmitterShift(), prnt.getSubmitterId(), prnt.getSubmitterName(), prnt.getSubmitterTeamName(),
                prnt.getSubmitterTeamId(), prnt.getSubmitterSiteName(), prnt.getSubmitterSiteId(), prnt.getStatus(),
                prnt.getApprovalNeeded(), prnt.getComment(), prnt.getDateOfAction(),
                prnt.getDescription(), prnt.getCommentary(), prnt.getHistory(), prnt.getIsRead(), prnt.getReason());
    }
}
