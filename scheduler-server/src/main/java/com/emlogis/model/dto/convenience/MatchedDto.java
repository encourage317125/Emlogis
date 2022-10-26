package com.emlogis.model.dto.convenience;

import com.emlogis.model.structurelevel.dto.SiteDto;
import com.emlogis.model.structurelevel.dto.TeamDto;

import java.io.Serializable;
import java.util.Collection;

public class MatchedDto implements Serializable {

    private Collection<SiteDto> siteDtos;
    private Collection<TeamDto> teamDtos;

    public Collection<SiteDto> getSiteDtos() {
        return siteDtos;
    }

    public void setSiteDtos(Collection<SiteDto> siteDtos) {
        this.siteDtos = siteDtos;
    }

    public Collection<TeamDto> getTeamDtos() {
        return teamDtos;
    }

    public void setTeamDtos(Collection<TeamDto> teamDtos) {
        this.teamDtos = teamDtos;
    }
}
