package com.emlogis.model.workflow.dto.administrative.role;

import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by alexborlis on 30.01.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowAbstractRoleDto implements Serializable {

    @JsonProperty(value = "id", required = true)
    private String id;

    @JsonProperty(value = "name", required = true)
    private String name;

    @JsonProperty(value = "type", required = true)
    private WorkflowRoleDict type;

    public WorkflowAbstractRoleDto() {
    }

    public WorkflowAbstractRoleDto(String id, String name, WorkflowRoleDict type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WorkflowRoleDict getType() {
        return type;
    }

    public void setType(WorkflowRoleDict type) {
        this.type = type;
    }
}
