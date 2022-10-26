package com.emlogis.model.workflow.entities;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.common.PkEntity;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.status.WorkflowRequestStatusDict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by user on 20.07.15.
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class WorkflowRequestManager extends BaseEntity implements PkEntity, Serializable {

    @ManyToOne(targetEntity = WorkflowRequest.class, optional = false)
    @JoinColumns({
            @JoinColumn(name = "fk_wfl_process_instance_id", referencedColumnName = "id", nullable = false),
            @JoinColumn(name = "fk_wfl_process_tenant_id", referencedColumnName = "tenantId", nullable = false)
    })
    private WorkflowRequest request;

    @ManyToOne(targetEntity = UserAccount.class, optional = false)
    @JoinColumns({@JoinColumn(name = "fk_manager_tenant_id", referencedColumnName = "tenantId"),
            @JoinColumn(name = "fk_manager_account_id", referencedColumnName = "id")})
    private UserAccount manager;

    @Column
    @Enumerated(EnumType.STRING)
    WorkflowRequestTypeDict requestType;

    @Column
    @Enumerated(EnumType.STRING)
    WorkflowRequestStatusDict requestStatus;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime responded = new DateTime(0);

    @Column(nullable = false)
    private Boolean isRead = false;

    public WorkflowRequestManager() {
        super();
        isRead = false;
    }

    public WorkflowRequestManager(
            WorkflowRequest instance,
            UserAccount manager
    ) {
        super(new PrimaryKey(instance.getTenantId()));
        isRead = false;
        this.request = instance;
        this.requestType = instance.getRequestType();
        this.requestStatus = instance.getRequestStatus();
        this.isRead = false;
        this.manager = manager;
    }

    public WorkflowRequest getRequest() {
        return request;
    }

    public void setRequest(WorkflowRequest request) {
        this.request = request;
    }

    public UserAccount getManager() {
        return manager;
    }

    public void setManager(UserAccount manager) {
        this.manager = manager;
    }

    public WorkflowRequestTypeDict getRequestType() {
        return requestType;
    }

    public void setRequestType(WorkflowRequestTypeDict requestType) {
        this.requestType = requestType;
    }

    public WorkflowRequestStatusDict getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(WorkflowRequestStatusDict requestStatus) {
        this.requestStatus = requestStatus;
    }

    public DateTime getResponded() {
        return responded;
    }

    public void setResponded(DateTime responded) {
        this.responded = responded;
    }

    public Boolean getIsRead() {
        if (isRead == null) {
            this.isRead = false;
        }
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WorkflowRequestPeer) {
            WorkflowRequestManager other = (WorkflowRequestManager) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getId(), other.getId());
            builder.append(getTenantId(), other.getTenantId());
            builder.append(getRequest().getTenantId(), other.getRequest().getTenantId());
            builder.append(getRequest().getId(), other.getRequest().getId());
            builder.append(getManager().getTenantId(), other.getManager().getTenantId());
            builder.append(getManager().getId(), other.getManager().getId());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getId());
        builder.append(getTenantId());
        builder.append(getRequest().getTenantId());
        builder.append(getRequest().getId());
        builder.append(getManager().getTenantId());
        builder.append(getManager().getId());
        return builder.toHashCode();
    }

}
