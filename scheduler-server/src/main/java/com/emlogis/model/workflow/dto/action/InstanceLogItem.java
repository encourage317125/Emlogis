package com.emlogis.model.workflow.dto.action;

import com.emlogis.model.workflow.entities.WorkflowRequestLog;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Created by user on 13.07.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstanceLogItem implements Serializable {

    @JsonProperty(value = "tenantId", required = true)
    private String tenantId;

    @JsonProperty(value = "itemId", required = true)
    private String actionId;

    @JsonProperty(value = "instanceId", required = true)
    private String instanceId;

    @JsonProperty(value = "itemCreated", required = true)
    private Long itemCreated;


    @JsonProperty(value = "instanceType", required = true)
    private String instanceType;

    @JsonProperty(value = "instanceCreated", required = true)
    private Long instanceCreated;

    @JsonProperty(value = "instanceInitiatorId", required = true)
    private String instanceInitiatorId;

    @JsonProperty(value = "instanceInitiatorName", required = true)
    private String instanceInitiatorName;

    @JsonProperty(value = "action", required = true)
    private String action;

    @JsonProperty(value = "role", required = true)
    private String role;

    @JsonProperty(value = "actorId", required = true)
    private String actorId;

//    @JsonProperty(value = "actorName", required = true)
//    private String actorName;

    @JsonProperty(value = "comment", required = true)
    private String comment;

    @JsonProperty(value = "shiftId", required = true)
    private String shiftId;

    @JsonProperty(value = "prevRequestStatus", required = false)
    private String prevRequestStatus;

    @JsonProperty(value = "newRequestStatus", required = false)
    private String newRequestStatus;

    @JsonProperty(value = "prevPeerStatus", required = false)
    private String prevPeerStatus;

    @JsonProperty(value = "newPeerStatus", required = false)
    private String newPeerStatus;

    public InstanceLogItem() {
    }

    public InstanceLogItem(WorkflowRequestLog log) {
        this.tenantId = log.getTenantId();
        this.actionId = log.getId();
        this.instanceId = log.getProcessInstance().getId();
        this.itemCreated = log.getCreated().getMillis();
        this.instanceType = log.getProcessInstance().getRequestType().name();
        this.instanceCreated = log.getProcessInstance().getCreated().getMillis();
        this.instanceInitiatorId = log.getProcessInstance().getInitiator().getId();
        this.instanceInitiatorName = log.getProcessInstance().getInitiator().reportName();
        this.action = log.getAction().name();
        this.role = log.getRole().name();
        this.actorId = log.getActorId();
        //this.actorName = log.getEmployee().reportName();
        this.comment = log.getComment();
        this.shiftId = log.getShiftId();
        if (log.getPrevRequestStatus() != null)
        this.prevRequestStatus = log.getPrevRequestStatus().name();
        if (log.getNewRequestStatus() != null)
        this.newRequestStatus = log.getNewRequestStatus().name();
        if (log.getPrevPeerStatus() != null)
        this.prevPeerStatus = log.getPrevPeerStatus().name();
        if (log.getNewPeerStatus() != null)
        this.newPeerStatus = log.getNewPeerStatus().name();
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public Long getInstanceCreated() {
        return instanceCreated;
    }

    public void setInstanceCreated(Long instanceCreated) {
        this.instanceCreated = instanceCreated;
    }

    public Long getItemCreated() {
        return itemCreated;
    }

    public void setItemCreated(Long itemCreated) {
        this.itemCreated = itemCreated;
    }

    public String getInstanceInitiatorId() {
        return instanceInitiatorId;
    }

    public void setInstanceInitiatorId(String instanceInitiatorId) {
        this.instanceInitiatorId = instanceInitiatorId;
    }

    public String getInstanceInitiatorName() {
        return instanceInitiatorName;
    }

    public void setInstanceInitiatorName(String instanceInitiatorName) {
        this.instanceInitiatorName = instanceInitiatorName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }
//
//    public String getActorName() {
//        return actorName;
//    }
//
//    public void setActorName(String actorName) {
//        this.actorName = actorName;
//    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getPrevRequestStatus() {
        return prevRequestStatus;
    }

    public void setPrevRequestStatus(String prevRequestStatus) {
        this.prevRequestStatus = prevRequestStatus;
    }

    public String getNewRequestStatus() {
        return newRequestStatus;
    }

    public void setNewRequestStatus(String newRequestStatus) {
        this.newRequestStatus = newRequestStatus;
    }

    public String getPrevPeerStatus() {
        return prevPeerStatus;
    }

    public void setPrevPeerStatus(String prevPeerStatus) {
        this.prevPeerStatus = prevPeerStatus;
    }

    public String getNewPeerStatus() {
        return newPeerStatus;
    }

    public void setNewPeerStatus(String newPeerStatus) {
        this.newPeerStatus = newPeerStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InstanceLogItem) {
            InstanceLogItem other = (InstanceLogItem) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getTenantId(), other.getTenantId());
            builder.append(getActionId(), other.getActionId());
            builder.append(getInstanceId(), other.getInstanceId());
            builder.append(getItemCreated(), other.getItemCreated());
            builder.append(getInstanceType(), other.getInstanceType());
            builder.append(getInstanceCreated(), other.getInstanceCreated());
            builder.append(getInstanceInitiatorId(), other.getInstanceInitiatorId());
            builder.append(getInstanceInitiatorName(), other.getInstanceInitiatorName());
            builder.append(getAction(), other.getAction());
            builder.append(getRole(), other.getRole());
            builder.append(getActorId(), other.getActorId());
            //builder.append(getActorName(), other.getActorName());
            builder.append(getComment(), other.getComment());
            builder.append(getShiftId(), other.getShiftId());
            builder.append(getPrevRequestStatus(), other.getPrevRequestStatus());
            builder.append(getNewRequestStatus(), other.getNewRequestStatus());
            builder.append(getPrevPeerStatus(), other.getPrevPeerStatus());
            builder.append(getNewPeerStatus(), other.getNewPeerStatus());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getTenantId());
        builder.append(getActionId());
        builder.append(getInstanceId());
        builder.append(getItemCreated());
        builder.append(getInstanceType());
        builder.append(getInstanceCreated());
        builder.append(getInstanceInitiatorId());
        builder.append(getInstanceInitiatorName());
        builder.append(getAction());
        builder.append(getRole());
        builder.append(getActorId());
       // builder.append(getActorName());
        builder.append(getComment());
        builder.append(getShiftId());
        builder.append(getPrevRequestStatus());
        builder.append(getNewRequestStatus());
        builder.append(getPrevPeerStatus());
        builder.append(getNewPeerStatus());
        return builder.toHashCode();
    }

}
