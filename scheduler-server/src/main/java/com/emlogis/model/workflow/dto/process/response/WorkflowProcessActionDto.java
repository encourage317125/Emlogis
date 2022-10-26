package com.emlogis.model.workflow.dto.process.response;

import com.emlogis.workflow.enums.WorkflowActionDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by alexborlis on 15.02.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowProcessActionDto implements Serializable {

    private String id;

    private WorkflowRoleDict role;

    private Long date;

    private String employeeId;

    private String tenantId;

    private WorkflowActionDict action;

    public WorkflowProcessActionDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WorkflowRoleDict getRole() {
        return role;
    }

    public void setRole(WorkflowRoleDict role) {
        this.role = role;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public WorkflowActionDict getAction() {
        return action;
    }

    public void setAction(WorkflowActionDict action) {
        this.action = action;
    }
}
