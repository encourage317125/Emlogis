package com.emlogis.model.dto.convenience;

import com.emlogis.model.AccessType;

import java.io.Serializable;

public class ACEConfigurationSiteDto implements Serializable {

    private String id;
    private String name;
    private String description;
    private AccessType accessType;
    private ACEConfigurationAllTeamsDto teamsDto;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public ACEConfigurationAllTeamsDto getTeamsDto() {
        return teamsDto;
    }

    public void setTeamsDto(ACEConfigurationAllTeamsDto teamsDto) {
        this.teamsDto = teamsDto;
    }
}
