package com.emlogis.model.workflow.entities;

import com.emlogis.common.UniqueId;
import com.emlogis.model.common.BaseEntityBean;
import com.emlogis.model.common.SimpleKeyBaseEntity;
import com.emlogis.model.employee.EmployeeProcessAutoApproval;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class WflProcessType extends BaseEntityBean implements SimpleKeyBaseEntity, Serializable {

    @Id()
    @Column(unique = true, length = 64)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkflowRequestTypeDict type;

    @OneToMany(mappedBy = "wflProcessType", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<EmployeeProcessAutoApproval> employeeProcessAutoApprovals = new HashSet<>();

    public WflProcessType() {
        super();
        this.id = UniqueId.getId();
    }

    public WflProcessType(String name, WorkflowRequestTypeDict type) {
        this();
        this.name = name;
        this.type = type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WorkflowRequestTypeDict getType() {
        return type;
    }

    public void setType(WorkflowRequestTypeDict type) {
        this.type = type;
    }

    public Set<EmployeeProcessAutoApproval> getEmployeeProcessAutoApprovals() {
        return employeeProcessAutoApprovals;
    }

    public void setEmployeeProcessAutoApprovals(Set<EmployeeProcessAutoApproval> employeeProcessAutoApprovals) {
        this.employeeProcessAutoApprovals = employeeProcessAutoApprovals;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WflProcessType) {
            WflProcessType other = (WflProcessType) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getId(), other.getId());
            builder.append(getName(), other.getName());
            builder.append(getType(), other.getType());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getId());
        builder.append(getName());
        builder.append(getType());
        return builder.toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("id", getId()).
                append("name", getName()).
                append("type", getType()).
                toString();
    }
}
