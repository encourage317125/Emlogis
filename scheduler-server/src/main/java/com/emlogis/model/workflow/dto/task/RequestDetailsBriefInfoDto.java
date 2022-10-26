package com.emlogis.model.workflow.dto.task;

import com.emlogis.model.workflow.dto.details.abstracts.AbstractRequestDetailsInfoDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by alex on 31.03.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestDetailsBriefInfoDto extends AbstractRequestDetailsInfoDto implements Serializable {

    private Boolean canBeActedByEmployee;
    private Boolean isRead;
    private String description;

    public RequestDetailsBriefInfoDto() {
        super();
    }

    public RequestDetailsBriefInfoDto(
            String processId,
            String type,
            Long submitionDate,
            Long eventDate,
            Long expirationDate,
            String status,
            TaskShiftBriefInfoDto originatorShift,
            String originatorName,
            String originatorId,
            Boolean canBeActedByEmployee,
            String submitterTeamName,
            String submitterTeamId,
            String submitterSiteName,
            String submitterSiteId,
            String description
    ) {
        super(processId, type, submitionDate, eventDate, expirationDate, originatorShift, originatorId, originatorName,
                submitterTeamName, submitterTeamId, submitterSiteName, submitterSiteId, status);
        this.canBeActedByEmployee = canBeActedByEmployee;
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

    public Boolean getCanBeActedByEmployee() {
        return canBeActedByEmployee;
    }

    public void setCanBeActedByEmployee(Boolean canBeActedByEmployee) {
        this.canBeActedByEmployee = canBeActedByEmployee;
    }
}
