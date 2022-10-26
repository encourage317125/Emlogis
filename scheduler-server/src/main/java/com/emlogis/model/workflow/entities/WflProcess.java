package com.emlogis.model.workflow.entities;


import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.common.PkEntity;
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
public class WflProcess extends BaseEntity implements PkEntity, Serializable {

    @Basic
    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(targetEntity = WflProcessType.class, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_wfl_process_type_id", referencedColumnName = "id", nullable = false)
    private WflProcessType type;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "WflRoleToProcess",
            inverseJoinColumns = {@JoinColumn(name = "fk_role_id", referencedColumnName = "id")},
            joinColumns = {
                    @JoinColumn(name = "fk_process_id", referencedColumnName = "id"),
                    @JoinColumn(name = "fk_process_tenant_id", referencedColumnName = "tenantId")})
    private Set<WflRole> roles;

    @ManyToOne(targetEntity = WflSourceScript.class, optional = false)
    @JoinColumn(name = "fk_workflow_template_id", nullable = false)
    private WflSourceScript template;

    public WflProcess() {
        super();
    }

    public WflProcess(
            String name,
            String tenantId,
            WflSourceScript template,
            WflProcessType type
    ) {
        super(new PrimaryKey(tenantId));
        this.name = name;
        this.template = template;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WflProcessType getType() {
        return type;
    }

    public void setType(WflProcessType type) {
        this.type = type;
    }

    public Set<WflRole> getRoles() {
        if (roles == null) {
            roles = new HashSet<>();
        }
        return roles;
    }

    public void setRoles(Set<WflRole> roles) {
        this.roles = roles;
    }

    public WflSourceScript getTemplate() {
        return template;
    }

    public void setTemplate(WflSourceScript template) {
        this.template = template;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WflProcess) {
            WflProcess other = (WflProcess) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getId(), other.getId());
            builder.append(getTenantId(), other.getTenantId());
            builder.append(getName(), other.getName());
//            builder.append(getType(), other.getType());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getId());
        builder.append(getName());
        builder.append(getTenantId());
//        builder.append(getType());
        return builder.toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("id", getId()).
                append("name", getName()).
                append("tenant", getTenantId()).
                toString();
    }
}
