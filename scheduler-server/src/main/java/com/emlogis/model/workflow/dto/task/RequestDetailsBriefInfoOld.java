package com.emlogis.model.workflow.dto.task;

import com.emlogis.model.workflow.dto.details.abstracts.AbstractRequestDetailsInfoDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by user on 19.08.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestDetailsBriefInfoOld extends AbstractRequestDetailsInfoDto implements Serializable {


    private TaskShiftBriefInfoDto recipientShift;

    private Boolean canBeActedByEmployee;

    private Boolean isRead;

    private String description;

    public RequestDetailsBriefInfoOld() {
        super();
    }

    public RequestDetailsBriefInfoOld(
            String processId,
            String type,
            Long submitionDate,
            Long eventDate,
            Long expirationDate,
            String status,
            TaskShiftBriefInfoDto originatorShift,
            String originatorName,
            String originatorId,
            TaskShiftBriefInfoDto recipientShift,
            Boolean canBeActedByEmployee,
            String submitterTeamName, String submitterTeamId,
            String submitterSiteName, String submitterSiteId,
            Boolean isRead,
            String description
    ) {
        super(processId, type, submitionDate, eventDate, expirationDate, originatorShift, originatorId, originatorName,
                submitterTeamName, submitterTeamId, submitterSiteName, submitterSiteId, status);
        this.recipientShift = recipientShift;
        this.canBeActedByEmployee = canBeActedByEmployee;
        this.isRead = isRead;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public TaskShiftBriefInfoDto getRecipientShift() {
        return recipientShift;
    }

    public void setRecipientShift(TaskShiftBriefInfoDto recipientShift) {
        this.recipientShift = recipientShift;
    }

    public Boolean getCanBeActedByEmployee() {
        return canBeActedByEmployee;
    }

    public void setCanBeActedByEmployee(Boolean canBeActedByEmployee) {
        this.canBeActedByEmployee = canBeActedByEmployee;
    }

}
