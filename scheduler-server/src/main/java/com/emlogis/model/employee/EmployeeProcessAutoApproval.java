package com.emlogis.model.employee;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.entities.WflProcessType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeProcessAutoApproval extends BaseEntity {

    private boolean autoApproval = false;

    @ManyToOne(cascade = CascadeType.DETACH)
    private Employee employee;

    @ManyToOne(targetEntity = WflProcessType.class, optional = false, fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "fk_process_type_id", referencedColumnName = "id", nullable = false)
    private WflProcessType wflProcessType;

    public EmployeeProcessAutoApproval() {}

    public EmployeeProcessAutoApproval(PrimaryKey primaryKey) {
        super(primaryKey);
    }

    public boolean isAutoApproval() {
        return autoApproval;
    }

    public void setAutoApproval(boolean autoApproval) {
        this.autoApproval = autoApproval;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public WflProcessType getWflProcessType() {
        return wflProcessType;
    }

    public void setWflProcessType(WflProcessType wflProcessType) {
        this.wflProcessType = wflProcessType;
    }
}
