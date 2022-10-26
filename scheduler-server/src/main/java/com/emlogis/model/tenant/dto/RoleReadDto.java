package com.emlogis.model.tenant.dto;

public class RoleReadDto extends RoleDto {

    private String groups;
    private int nbOfGroups;
    private int nbOfMembers;

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public int getNbOfGroups() {
        return nbOfGroups;
    }

    public void setNbOfGroups(int nbOfGroups) {
        this.nbOfGroups = nbOfGroups;
    }

    public int getNbOfMembers() {
        return nbOfMembers;
    }

    public void setNbOfMembers(int nbOfMembers) {
        this.nbOfMembers = nbOfMembers;
    }
}
