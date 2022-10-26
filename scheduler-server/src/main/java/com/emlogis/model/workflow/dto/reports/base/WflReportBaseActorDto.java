package com.emlogis.model.workflow.dto.reports.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by alex on 2/27/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WflReportBaseActorDto implements Serializable {

    private String name;
    private String teamName;
    private String status;

    public WflReportBaseActorDto() {
    }

    public WflReportBaseActorDto(String name, String teamName, String status) {
        this.name = name;
        this.teamName = teamName;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
