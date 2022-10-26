package com.emlogis.model.workflow.dto.details;

import com.emlogis.model.employee.dto.*;
import com.emlogis.workflow.enums.status.WorkflowRequestStatusDict;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Created by user on 22.09.15.
 */
public class WorkflowAvailabilitySettingsDto implements Serializable {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = AvailcalUpdateParamsCDPrefDto.class, name = "AvailcalUpdateParamsCDPrefDto"),
            @JsonSubTypes.Type(value = AvailcalUpdateParamsCDAvailDto.class, name = "AvailcalUpdateParamsCDAvailDto"),
            @JsonSubTypes.Type(value = AvailcalUpdateParamsCIPrefDto.class, name = "AvailcalUpdateParamsCIPrefDto"),
            @JsonSubTypes.Type(value = AvailcalUpdateParamsCIAvailDto.class, name = "AvailcalUpdateParamsCIAvailDto"),
    })
    private AvailabilityWorkflowRequest updateDto;

    private String tenantId;

    private String requestId;

    private WorkflowRequestStatusDict status;

    public WorkflowAvailabilitySettingsDto() {
    }

    public WorkflowAvailabilitySettingsDto(
            AvailabilityWorkflowRequest updateDto,
            String tenantId,
            String requestId,
            WorkflowRequestStatusDict status
    ) {
        this.updateDto = updateDto;
        this.tenantId = tenantId;
        this.requestId = requestId;
        this.status = status;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public WorkflowRequestStatusDict getStatus() {
        return status;
    }

    public void setStatus(WorkflowRequestStatusDict status) {
        this.status = status;
    }

    public AvailabilityWorkflowRequest getUpdateDto() {
        return updateDto;
    }

    public void setUpdateDto(AvailabilityWorkflowRequest updateDto) {
        this.updateDto = updateDto;
    }
}
