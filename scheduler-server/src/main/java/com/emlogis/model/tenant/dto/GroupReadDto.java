package com.emlogis.model.tenant.dto;

import com.emlogis.model.dto.BaseEntityDto;

public class GroupReadDto extends BaseEntityDto {

    private String name;
    private String description;
    private int nbOfMembers;
    private int nbOfRoles;
    private String roles;

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

    public int getNbOfMembers() {
        return nbOfMembers;
    }

    public void setNbOfMembers(int nbOfMembers) {
        this.nbOfMembers = nbOfMembers;
    }

    public int getNbOfRoles() {
        return nbOfRoles;
    }

    public void setNbOfRoles(int nbOfRoles) {
        this.nbOfRoles = nbOfRoles;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}
