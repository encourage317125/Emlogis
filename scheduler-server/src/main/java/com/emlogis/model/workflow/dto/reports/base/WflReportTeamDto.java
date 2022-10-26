package com.emlogis.model.workflow.dto.reports.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by alex on 2/27/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WflReportTeamDto implements Serializable {

    private String name;

    private String teamId;

    public WflReportTeamDto() {
    }

    public WflReportTeamDto(String name, String teamId) {
        this.name = name;
        this.teamId = teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
}
