package com.emlogis.model.workflow.dto.reports.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by alex on 2/27/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WflReportWorkflowTypeDto implements Serializable {

    private String name;
    private String id;

    public WflReportWorkflowTypeDto() {
    }

    public WflReportWorkflowTypeDto(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
