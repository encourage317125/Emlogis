package com.emlogis.model.workflow.dto.administrative.process;

import com.emlogis.model.workflow.dto.administrative.role.WorkflowAbstractRoleDto;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexborlis on 22.01.15.
 */
//@XmlRootElement
//@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowAbstractProcessDto implements Serializable {

    //@XmlElement(name = "id")
    @JsonProperty(value = "id", required = true)
    private Long processId;

   // @XmlElement(name = "name")
    @JsonProperty(value = "name", required = true)
    private String processName;

   // @XmlElement(name = "tenantId")
    @JsonProperty(value = "tenantId", required = true)
    private String organizationId;

    //@XmlElement(name = "organizationName")
    @JsonProperty(value = "organizationName", required = true)
    private String organizationName;

   // @XmlElement(name = "typeName")
    @JsonProperty(value = "typeName", required = true)
    private String workflowTypeName;

  //  @XmlElement(name = "type")
    @JsonProperty(value = "type", required = true)
    private WorkflowRequestTypeDict type;

  //  @XmlElement(name = "roles")
    @JsonProperty(value = "roles", required = true)
    private List<WorkflowAbstractRoleDto> roles;

    public WorkflowAbstractProcessDto() {
    }

    public WorkflowAbstractProcessDto(
            Long processId, java.lang.String processName, java.lang.String organizationId, java.lang.String organizationName,
            java.lang.String workflowTypeName, WorkflowRequestTypeDict type) {
        this.processId = processId;
        this.processName = processName;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.workflowTypeName = workflowTypeName;
        this.type = type;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public java.lang.String getProcessName() {
        return processName;
    }

    public void setProcessName(java.lang.String processName) {
        this.processName = processName;
    }

    public java.lang.String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(java.lang.String organizationId) {
        this.organizationId = organizationId;
    }

    public java.lang.String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(java.lang.String organizationName) {
        this.organizationName = organizationName;
    }

    public java.lang.String getWorkflowTypeName() {
        return workflowTypeName;
    }

    public void setWorkflowTypeName(java.lang.String workflowTypeName) {
        this.workflowTypeName = workflowTypeName;
    }

    public WorkflowRequestTypeDict getType() {
        return type;
    }

    public void setType(WorkflowRequestTypeDict type) {
        this.type = type;
    }

    public List<WorkflowAbstractRoleDto> getRoles() {
        if (roles == null) {
            roles = new ArrayList<>();
        }
        return roles;
    }

    public void addRole(WorkflowAbstractRoleDto role) {
        getRoles().add(role);
    }

    public void setRoles(List<WorkflowAbstractRoleDto> roles) {
        this.roles = roles;
    }
}
