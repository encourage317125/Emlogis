package com.emlogis.model.workflow.dto.details.abstracts;

import com.emlogis.model.workflow.dto.task.TaskShiftBriefInfoDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbstractRequestDetailsInfoDto implements RequestDetailsInfo {

    @JsonProperty(value = "requestId", required = true)
    private String requestId;

    @JsonProperty(value = "type", required = true)
    private String type;

    @JsonProperty(value = "submitDate", required = false)
    private Long submitDate;

    @JsonProperty(value = "eventDate", required = false)
    private Long eventDate;

    @JsonProperty(value = "expirationDate", required = false)
    private Long expirationDate;

    private Long employeeStartDate;

    @JsonProperty(value = "submitterShift", required = false)
    private TaskShiftBriefInfoDto submitterShift;

    @JsonProperty(value = "submitterId", required = true)
    private String submitterId;

    @JsonProperty(value = "submitterName", required = true)
    private String submitterName;

    @JsonProperty(value = "submitterTeamName", required = true)
    private String submitterTeamName;

    @JsonProperty(value = "submitterTeamId", required = true)
    private String submitterTeamId;

    @JsonProperty(value = "submitterSiteName", required = true)
    private String submitterSiteName;

    @JsonProperty(value = "submitterSiteId", required = true)
    private String submitterSiteId;

    @JsonProperty(value = "status", required = true)
    private String status;

    @JsonProperty(value = "subtype", required = false)
    private String subtype;

    public AbstractRequestDetailsInfoDto() {}

    public AbstractRequestDetailsInfoDto(
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
            String status
    ) {
        this();
        this.requestId = requestId;
        this.type = type;
        this.submitDate = submitionDate;
        this.eventDate = eventDate;
        this.expirationDate = expirationDate;
        this.submitterShift = submitterShift;
        this.submitterId = submitterId;
        this.submitterName = submitterName;
        this.submitterTeamName = submitterTeamName;
        this.submitterTeamId = submitterTeamId;
        this.submitterSiteName = submitterSiteName;
        this.submitterSiteId = submitterSiteId;
        this.status = status;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(Long submitDate) {
        this.submitDate = submitDate;
    }

    public Long getEventDate() {
        return eventDate;
    }

    public void setEventDate(Long eventDate) {
        this.eventDate = eventDate;
    }

    public Long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public TaskShiftBriefInfoDto getSubmitterShift() {
        return submitterShift;
    }

    public void setSubmitterShift(TaskShiftBriefInfoDto submitterShift) {
        this.submitterShift = submitterShift;
    }

    public String getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(String submitterId) {
        this.submitterId = submitterId;
    }

    public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }

    public String getSubmitterTeamName() {
        return submitterTeamName;
    }

    public void setSubmitterTeamName(String submitterTeamName) {
        this.submitterTeamName = submitterTeamName;
    }

    public String getSubmitterTeamId() {
        return submitterTeamId;
    }

    public void setSubmitterTeamId(String submitterTeamId) {
        this.submitterTeamId = submitterTeamId;
    }

    public String getSubmitterSiteName() {
        return submitterSiteName;
    }

    public void setSubmitterSiteName(String submitterSiteName) {
        this.submitterSiteName = submitterSiteName;
    }

    public String getSubmitterSiteId() {
        return submitterSiteId;
    }

    public void setSubmitterSiteId(String submitterSiteId) {
        this.submitterSiteId = submitterSiteId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getEmployeeStartDate() {
        return employeeStartDate;
    }

    public void setEmployeeStartDate(Long employeeStartDate) {
        this.employeeStartDate = employeeStartDate;
    }
}
