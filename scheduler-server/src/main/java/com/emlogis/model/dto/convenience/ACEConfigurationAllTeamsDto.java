package com.emlogis.model.dto.convenience;

import com.emlogis.model.AccessType;

import java.io.Serializable;
import java.util.Set;

public class ACEConfigurationAllTeamsDto implements Serializable {

    private AccessType allTeamsAccessType;
    private Set<ACEConfigurationTeamDto> teamDtos;

    public AccessType getAllTeamsAccessType() {
        return allTeamsAccessType;
    }

    public void setAllTeamsAccessType(AccessType allTeamsAccessType) {
        this.allTeamsAccessType = allTeamsAccessType;
    }

    public Set<ACEConfigurationTeamDto> getTeamDtos() {
        return teamDtos;
    }

    public void setTeamDtos(Set<ACEConfigurationTeamDto> teamDtos) {
        this.teamDtos = teamDtos;
    }
}
