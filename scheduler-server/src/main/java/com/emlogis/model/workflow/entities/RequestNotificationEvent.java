package com.emlogis.model.workflow.entities;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.common.PkEntity;
import com.emlogis.workflow.enums.WorkflowActionDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by user on 14.08.15.
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class RequestNotificationEvent extends BaseEntity implements PkEntity, Serializable {

    @Column(name = "requestId", nullable = false)
    private String requestId;

    @Column(name = "actorId", nullable = false)
    private String actorId;

    @Column
    @Enumerated(EnumType.STRING)
    private WorkflowActionDict action;

    @Column
    @Enumerated(EnumType.STRING)
    private WorkflowRoleDict role;

    @Column
    @Enumerated(EnumType.STRING)
    private RequestNotificationStatus status;

    @Column(name = "shiftId", nullable = true)
    private String shiftId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "reason", nullable = true)
    private String reason;

    @ElementCollection(fetch = FetchType.LAZY)
    @Column(length = 8192, columnDefinition = "varchar(8192)")
    private Map<String, String> queuedNotifications = new HashMap<String, String>();

    public RequestNotificationEvent() {
        super();
    }

    public RequestNotificationEvent(
            WorkflowRequest request,
            WorkflowActionDict action,
            String actorId,
            WorkflowRoleDict role,
            String shiftId
    ) {
        super(new PrimaryKey(request.getTenantId()));
        this.status = RequestNotificationStatus.PENDING;
        this.requestId = request.getId();
        this.actorId = actorId;
        this.action = action;
        this.role = role;
        this.shiftId = shiftId;
        this.code = UUID.randomUUID().toString();
    }

    public Map<String, String> getQueuedNotifications() {
        if(queuedNotifications == null) {
            queuedNotifications = new HashMap<>();
        }
        return queuedNotifications;
    }

    public void setQueuedNotifications(Map<String, String> queuedNotifications) {
        this.queuedNotifications = queuedNotifications;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public RequestNotificationStatus getStatus() {
        return status;
    }

    public void setStatus(RequestNotificationStatus status) {
        this.status = status;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public WorkflowRoleDict getRole() {
        return role;
    }

    public void setRole(WorkflowRoleDict role) {
        this.role = role;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public WorkflowActionDict getAction() {
        return action;
    }

    public void setAction(WorkflowActionDict action) {
        this.action = action;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RequestNotificationEvent) {
            RequestNotificationEvent other = (RequestNotificationEvent) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getId(), other.getId());
            builder.append(getTenantId(), other.getTenantId());
            builder.append(getRequestId(), other.getRequestId());
            builder.append(getActorId(), other.getActorId());
            builder.append(getAction(), other.getAction());
            builder.append(getCreated(), other.getCreated());
            builder.append(getShiftId(), other.getShiftId());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getId());
        builder.append(getTenantId());
        builder.append(getRequestId());
        builder.append(getActorId());
        builder.append(getAction());
        builder.append(getCreated());
        builder.append(getShiftId());
        return builder.toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("id", getId()).
                append("tenant", getTenantId()).
                append("requestId", getRequestId()).
                append("shiftId", getShiftId() != null ? getShiftId() : "no shift").
                toString();
    }
}
