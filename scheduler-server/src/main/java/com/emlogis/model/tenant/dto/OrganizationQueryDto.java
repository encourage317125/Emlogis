package com.emlogis.model.tenant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationQueryDto extends OrganizationDto {

    private int nbOfSites;
    private int nbOfTeams;
    private int nbOfEmployees;

    public int getNbOfSites() {
        return nbOfSites;
    }

    public void setNbOfSites(int nbOfSites) {
        this.nbOfSites = nbOfSites;
    }

    public int getNbOfTeams() {
        return nbOfTeams;
    }

    public void setNbOfTeams(int nbOfTeams) {
        this.nbOfTeams = nbOfTeams;
    }

    public int getNbOfEmployees() {
        return nbOfEmployees;
    }

    public void setNbOfEmployees(int nbOfEmployees) {
        this.nbOfEmployees = nbOfEmployees;
    }
}
