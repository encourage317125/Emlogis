package com.emlogis.model.workflow.entities;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.common.PkEntity;
import com.emlogis.workflow.enums.WorkflowActionDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.enums.status.WorkflowRequestStatusDict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by alexborlis on 15.02.15.
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class WorkflowRequestLog extends BaseEntity
        implements PkEntity, Serializable {

    @ManyToOne(targetEntity = WorkflowRequest.class, optional = false, cascade = CascadeType.MERGE)
    @JoinColumns({
            @JoinColumn(name = "fk_wfl_process_instance_id", referencedColumnName = "id", nullable = false),
            @JoinColumn(name = "fk_wfl_process_tenant_id", referencedColumnName = "tenantId", nullable = false)})
    private WorkflowRequest processInstance;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkflowActionDict action;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkflowRoleDict role;

//    @ManyToOne(targetEntity = Employee.class, optional = false)
//    @JoinColumns({@JoinColumn(name = "fk_tenant_id", referencedColumnName = "tenantId", nullable = false),
//            @JoinColumn(name = "fk_employee_id", referencedColumnName = "id", nullable = false)})
//    private Employee employee;

    @Column(name = "fk_actor_id", nullable = false)
    private String actorId;

    @Basic
    @Column(length = 2048, nullable = true)
    private String comment;

    @Basic
    @Column(length = 64, nullable = true)
    private String shiftId;

    @Column
    @Enumerated(EnumType.STRING)
    private WorkflowRequestStatusDict prevRequestStatus;    // previous request status (ie prior to performing action)

    @Column
    @Enumerated(EnumType.STRING)
    private WorkflowRequestStatusDict newRequestStatus;    // request status after performing action

    @Column
    @Enumerated(EnumType.STRING)                    // applicable only to WIP/SWAP requests
    private WorkflowRequestStatusDict prevPeerStatus;        // previous peer status (ie prior to performing action)

    @Column
    @Enumerated(EnumType.STRING)                    // applicable only to WIP/SWAP requests
    private WorkflowRequestStatusDict newPeerStatus;        // new peer status after performing action


    public WorkflowRequestLog() {
        super();
        this.shiftId = "EMPTY";
    }

    public WorkflowRequestLog(String tenantId) {
        super(new PrimaryKey(tenantId));
        this.shiftId = "EMPTY";
    }

    public WorkflowRequestLog(
            WorkflowRequest request,
            WorkflowActionDict action,
            WorkflowRoleDict role,
            String actorId,
            String shiftId,
            String comment
    ) {
        super(new PrimaryKey(request.getTenantId()));
        this.shiftId = "EMPTY";
        this.processInstance = request;
        this.action = action;
        this.role = role;
        this.actorId = actorId;
        if (shiftId != null) {
            this.shiftId = shiftId;
        }
        this.comment = comment;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        if (shiftId != null) {
            this.shiftId = shiftId;
        }
    }

    public WorkflowRequest getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(WorkflowRequest processInstance) {
        this.processInstance = processInstance;
    }

    public WorkflowActionDict getAction() {
        return action;
    }

    public void setAction(WorkflowActionDict action) {
        this.action = action;
    }

    public WorkflowRoleDict getRole() {
        return role;
    }

    public void setRole(WorkflowRoleDict role) {
        this.role = role;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public WorkflowRequestStatusDict getPrevRequestStatus() {
        return prevRequestStatus;
    }

    public void setPrevRequestStatus(WorkflowRequestStatusDict prevRequestStatus) {
        this.prevRequestStatus = prevRequestStatus;
    }

    public WorkflowRequestStatusDict getNewRequestStatus() {
        return newRequestStatus;
    }

    public void setNewRequestStatus(WorkflowRequestStatusDict newRequestStatus) {
        this.newRequestStatus = newRequestStatus;
    }

    public WorkflowRequestStatusDict getPrevPeerStatus() {
        return prevPeerStatus;
    }

    public void setPrevPeerStatus(WorkflowRequestStatusDict prevPeerStatus) {
        this.prevPeerStatus = prevPeerStatus;
    }

    public WorkflowRequestStatusDict getNewPeerStatus() {
        return newPeerStatus;
    }

    public void setNewPeerStatus(WorkflowRequestStatusDict newPeerStatus) {
        this.newPeerStatus = newPeerStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WorkflowRequestLog) {
            WorkflowRequestLog other = (WorkflowRequestLog) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getRole(), other.getRole());
            builder.append(getAction(), other.getAction());
            builder.append(getProcessInstance().getId(), other.getProcessInstance().getId());
            builder.append(getActorId(), other.getActorId());
            builder.append(getShiftId(), other.getShiftId());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getRole());
        builder.append(getAction());
        builder.append(getProcessInstance().getId());
        builder.append(getActorId());
        builder.append(getShiftId());
        return builder.toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("id", getId()).
                append("action", getAction()).
                append("role", getRole()).
                append("actorId", getActorId()).
                toString();
    }

//    public WorkflowProcessActionDto dto() {
//        WorkflowProcessActionDto dto = new WorkflowProcessActionDto();
//        dto.setId(getId());
//        dto.setRole(getRole());
//        dto.setAction(getAction());
//        dto.setDate(getUpdated().getMillis());
//        dto.setEmployeeId(getEmployee().getId());
//        dto.setTenantId(getEmployee().getTenantId());
//        return dto;
//    }
}
