package com.emlogis.model.workflow.dto.details.manager.impl;

import com.emlogis.model.employee.dto.*;
import com.emlogis.model.workflow.dto.details.manager.DetailedManagerRequestDetailsDto;
import com.emlogis.model.workflow.dto.details.manager.ManagerDetailsInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by root on 04.06.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AvailabilityManagerRequestDetailsDto<AT extends AvailabilityWorkflowRequest>
        extends DetailedManagerRequestDetailsDto implements ManagerDetailsInfo {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = AvailcalUpdateParamsCDPrefDto.class, name = "AvailcalUpdateParamsCDPrefDto"),
            @JsonSubTypes.Type(value = AvailcalUpdateParamsCDAvailDto.class, name = "AvailcalUpdateParamsCDAvailDto"),
            @JsonSubTypes.Type(value = AvailcalUpdateParamsCIPrefDto.class, name = "AvailcalUpdateParamsCIPrefDto"),
            @JsonSubTypes.Type(value = AvailcalUpdateParamsCIAvailDto.class, name = "AvailcalUpdateParamsCIAvailDto"),
    })
    private AT availUpdate;

    public AvailabilityManagerRequestDetailsDto(DetailedManagerRequestDetailsDto prnt, AT availUpdate) {
        super(prnt.getRequestId(), prnt.getType(), prnt.getSubmitDate(), prnt.getEventDate(), prnt.getExpirationDate(),
                prnt.getSubmitterShift(), prnt.getSubmitterId(), prnt.getSubmitterName(), prnt.getSubmitterTeamName(),
                prnt.getSubmitterTeamId(), prnt.getSubmitterSiteName(), prnt.getSubmitterSiteId(), prnt.getStatus(),
                prnt.getManagerApprovalNeeded(), prnt.getComment(), prnt.getDateOfAction(),
                prnt.getDescription(), prnt.getCommentary(), prnt.getHistory(), prnt.getIsRead(), prnt.getReason());
        this.availUpdate = availUpdate;
    }

    public AT getAvailUpdate() {
        return availUpdate;
    }

    public void setAvailUpdate(AT availUpdate) {
        this.availUpdate = availUpdate;
    }
}
