package com.emlogis.model.workflow.dto.decision;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by alexborlis on 22.01.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowDecisionResultInfoDto implements Serializable {

    @JsonProperty(value = "status", required = false)
    private String status;

    @JsonProperty(value = "peerId", required = false)
    private String peerId;

    @JsonProperty(value = "tenantId", required = false)
    private String tenantId;

    @JsonProperty(value = "actorId", required = false)
    private String actorId;

    public WorkflowDecisionResultInfoDto() {
    }

    public WorkflowDecisionResultInfoDto(
            String status,
            String peerId,
            String tenantId,
            String actorId
    ) {
        this.status = status;
        this.tenantId = tenantId;
        this.actorId = actorId;
        this.peerId = peerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }
}
