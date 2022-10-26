package com.emlogis.model.workflow.dto.details.manager.impl;

import com.emlogis.model.workflow.dto.details.TimeOffShiftDto;
import com.emlogis.model.workflow.dto.details.manager.DetailedManagerRequestDetailsDto;
import com.emlogis.model.workflow.dto.details.manager.ManagerDetailsInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 04.06.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeOffManagerRequestDetailsDto
        extends DetailedManagerRequestDetailsDto implements ManagerDetailsInfo {

    private List<TimeOffShiftDto> shifts;

    public TimeOffManagerRequestDetailsDto() {
    }

    public TimeOffManagerRequestDetailsDto(DetailedManagerRequestDetailsDto prnt) {
        super(prnt.getRequestId(), prnt.getType(), prnt.getSubmitDate(), prnt.getEventDate(), prnt.getExpirationDate(),
                prnt.getSubmitterShift(), prnt.getSubmitterId(), prnt.getSubmitterName(), prnt.getSubmitterTeamName(),
                prnt.getSubmitterTeamId(), prnt.getSubmitterSiteName(), prnt.getSubmitterSiteId(), prnt.getStatus(),
                prnt.getManagerApprovalNeeded(), prnt.getComment(), prnt.getDateOfAction(),
                prnt.getDescription(), prnt.getCommentary(), prnt.getHistory(), prnt.getIsRead(), prnt.getReason());
    }

    public List<TimeOffShiftDto> getShifts() {
        if (shifts == null) {
            shifts = new ArrayList<>();
        }
        return shifts;
    }

    public void setShifts(List<TimeOffShiftDto> shifts) {
        this.shifts = shifts;
    }
}
