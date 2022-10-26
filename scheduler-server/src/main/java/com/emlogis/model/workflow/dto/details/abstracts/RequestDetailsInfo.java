package com.emlogis.model.workflow.dto.details.abstracts;

import com.emlogis.model.workflow.dto.task.TaskShiftBriefInfoDto;

import java.io.Serializable;

/**
 * Created by user on 21.08.15.
 */
public interface RequestDetailsInfo extends Serializable {

    String getSubtype();

    void setSubtype(String subtype);

    String getRequestId();

    void setRequestId(String requestId);

    String getType();

    void setType(String type);

    Long getSubmitDate();

    void setSubmitDate(Long submitDate);

    Long getEventDate();

    void setEventDate(Long eventDate);

    Long getExpirationDate();

    void setExpirationDate(Long expirationDate);

    TaskShiftBriefInfoDto getSubmitterShift();

    void setSubmitterShift(TaskShiftBriefInfoDto submitterShift);

    String getSubmitterId();

    void setSubmitterId(String submitterId);

    String getSubmitterName();

    void setSubmitterName(String submitterName);

    String getSubmitterTeamName();

    void setSubmitterTeamName(String submitterTeamName);

    String getSubmitterTeamId();

    void setSubmitterTeamId(String submitterTeamId);

    String getSubmitterSiteName();

    void setSubmitterSiteName(String submitterSiteName);

    String getSubmitterSiteId();

    void setSubmitterSiteId(String submitterSiteId);

    String getStatus();

    void setStatus(String status);

}
