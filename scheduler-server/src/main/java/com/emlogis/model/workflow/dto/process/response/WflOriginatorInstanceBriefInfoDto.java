package com.emlogis.model.workflow.dto.process.response;

import com.emlogis.model.workflow.dto.task.TaskShiftBriefInfoDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by alexborlis on 20.02.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WflOriginatorInstanceBriefInfoDto implements Serializable {

    private String requestId;

    private String status;

    private String type;

    private TaskShiftBriefInfoDto originatorShift;

    private Long submitDate;

    private Long eventDate;

    private Long expirationDate;

    private Boolean isReadByPeer = false;

    private Boolean isReadByManager = false;

    private String description;

    public WflOriginatorInstanceBriefInfoDto() {
        super();
    }

    public WflOriginatorInstanceBriefInfoDto(
            String processId,
            String typeId,
            Long submitDate,
            Long expirationDate,
            String description
    ) {
        this.requestId = processId;
        this.type = typeId;
        this.submitDate = submitDate;
        this.expirationDate = expirationDate;
        this.description = description;
    }

    public WflOriginatorInstanceBriefInfoDto(WflOriginatorInstanceBriefInfoDto other) {
        this.requestId = other.getRequestId();
        this.type = other.getType();
        this.submitDate = other.getSubmitDate();
        this.expirationDate = other.getExpirationDate();
        this.description = other.getDescription();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsReadByPeer() {
        return isReadByPeer;
    }

    public void setIsReadByPeer(Boolean isReadByPeer) {
        this.isReadByPeer = isReadByPeer;
    }

    public Boolean getIsReadByManager() {
        return isReadByManager;
    }

    public void setIsReadByManager(Boolean isReadByManager) {
        this.isReadByManager = isReadByManager;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TaskShiftBriefInfoDto getOriginatorShift() {
        return originatorShift;
    }

    public void setOriginatorShift(TaskShiftBriefInfoDto originatorShift) {
        this.originatorShift = originatorShift;
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
}
