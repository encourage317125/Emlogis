package com.emlogis.model.workflow.dto.details.manager.impl;

import com.emlogis.model.workflow.dto.details.manager.DetailedManagerRequestDetailsDto;
import com.emlogis.model.workflow.dto.details.manager.ManagerDetailsInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by root on 04.06.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenShiftManagerRequestDetailsDto
        extends DetailedManagerRequestDetailsDto implements ManagerDetailsInfo {

    public OpenShiftManagerRequestDetailsDto() {
    }

    public OpenShiftManagerRequestDetailsDto(DetailedManagerRequestDetailsDto prnt) {
        super(prnt.getRequestId(), prnt.getType(), prnt.getSubmitDate(), prnt.getEventDate(), prnt.getExpirationDate(),
                prnt.getSubmitterShift(), prnt.getSubmitterId(), prnt.getSubmitterName(), prnt.getSubmitterTeamName(),
                prnt.getSubmitterTeamId(), prnt.getSubmitterSiteName(), prnt.getSubmitterSiteId(), prnt.getStatus(),
                prnt.getManagerApprovalNeeded(), prnt.getComment(), prnt.getDateOfAction(),
                prnt.getDescription(), prnt.getCommentary(), prnt.getHistory(), prnt.getIsRead(), prnt.getReason());
    }
}
