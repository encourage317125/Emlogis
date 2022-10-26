package com.emlogis.model.workflow.entities;


import com.emlogis.common.UniqueId;
import com.emlogis.model.common.BaseEntityBean;
import com.emlogis.model.common.SimpleKeyBaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by Developer on 19.01.2015.
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class WflSourceScript extends BaseEntityBean implements SimpleKeyBaseEntity, Serializable {

    @Id
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Lob
    @Column(name = "template", nullable = false)
    private byte[] template;

    @ManyToOne(targetEntity = WflProcessType.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_script_process_type_id", referencedColumnName = "id", nullable = false)
    private WflProcessType type;

    @OneToMany(mappedBy = "template", targetEntity = WflProcess.class, fetch = FetchType.EAGER)
    private Set<WflProcess> processes;

    public WflSourceScript() {
        super();
        this.id = UniqueId.getId();
    }

    public WflSourceScript(String name, WflProcessType type, byte[] template) {
        this();
        this.name = name;
        this.type = type;
        this.template = template.clone();
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

    public byte[] getTemplate() {
        return template;
    }

    public void setTemplate(byte[] template) {
        this.template = template;
    }

    public WflProcessType getType() {
        return type;
    }

    public void setType(WflProcessType type) {
        this.type = type;
    }

    public Set<WflProcess> getProcesses() {
        return processes;
    }

    public void setProcesses(Set<WflProcess> processes) {
        this.processes = processes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WflSourceScript) {
            WflSourceScript other = (WflSourceScript) obj;
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
