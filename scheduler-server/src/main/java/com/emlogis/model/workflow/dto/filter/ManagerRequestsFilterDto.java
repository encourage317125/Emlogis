package com.emlogis.model.workflow.dto.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 04.05.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManagerRequestsFilterDto extends RequestFilterDto implements Serializable {

    @JsonProperty(value = "sites", required = false)
    private List<String> sites;

    @JsonProperty(value = "teams", required = false)
    private List<String> teams;

    public ManagerRequestsFilterDto() {
    }

    public List<String> getSites() {
        if (sites == null) {
            sites = new ArrayList<>();
        }
        return sites;
    }

    public void setSites(List<String> sites) {
        this.sites = sites;
    }

    public List<String> getTeams() {
        if (teams == null) {
            teams = new ArrayList<>();
        }
        return teams;
    }

    public void setTeams(List<String> teams) {
        this.teams = teams;
    }

}

