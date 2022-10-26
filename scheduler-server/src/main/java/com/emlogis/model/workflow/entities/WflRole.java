package com.emlogis.model.workflow.entities;


import com.emlogis.common.UniqueId;
import com.emlogis.model.common.BaseEntityBean;
import com.emlogis.model.common.SimpleKeyBaseEntity;
import com.emlogis.model.workflow.dto.administrative.role.WorkflowAbstractRoleDto;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Developer on 19.01.2015.
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class WflRole extends BaseEntityBean
        implements SimpleKeyBaseEntity, Serializable {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Basic
    @Column(name = "name", nullable = false)
    private String name;

    @Basic
    @Column(name = "role_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkflowRoleDict roleType;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "WflRoleToProcess",
            inverseJoinColumns = {
                    @JoinColumn(name = "fk_process_id", referencedColumnName = "id"),
                    @JoinColumn(name = "fk_process_tenant_id", referencedColumnName = "tenantId")},
            joinColumns = {@JoinColumn(name = "fk_role_id", referencedColumnName = "id")})
    private Set<WflProcess> processes;

    public WflRole() {
        super();
        this.id = UniqueId.getId();
    }

    public WflRole(String name, WorkflowRoleDict type) {
        this();
        this.name = name;
        this.roleType = type;
    }

    public WflRole(String name, WorkflowRoleDict roleDict, WflProcess process) {
        this(name, roleDict);
        getProcesses().add(process);
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

    public WorkflowRoleDict getRoleType() {
        return roleType;
    }

    public void setRoleType(WorkflowRoleDict roleType) {
        this.roleType = roleType;
    }

    public Set<WflProcess> getProcesses() {
        if (processes == null) {
            processes = new HashSet<>();
        }
        return processes;
    }

    public void setProcesses(Set<WflProcess> processes) {
        this.processes = processes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WflRole) {
            WflRole other = (WflRole) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getId(), other.getId());
            builder.append(getName(), other.getName());
            builder.append(getRoleType().name(), other.getRoleType().name());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getId());
        builder.append(getName());
        builder.append(getRoleType().name());
        return builder.toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("id", getId()).
                append("name", getName()).
                append("type", getRoleType()).
                toString();
    }

    public WorkflowAbstractRoleDto dto() {
        WorkflowAbstractRoleDto dto = new WorkflowAbstractRoleDto();
        dto.setId(getId());
        dto.setName(getName());
        dto.setType(getRoleType());
        return dto;
    }
}
