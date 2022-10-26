package com.emlogis.engine.domain.dto;

import com.emlogis.engine.domain.organization.TeamAssociationType;

import java.io.Serializable;

public class EmployeeTeamDto implements Serializable {

    private String employeeId;
    private String teamId;
    private TeamAssociationType type;
    private boolean isHomeTeam;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public TeamAssociationType getType() {
        return type;
    }

    public void setType(TeamAssociationType type) {
        this.type = type;
    }

    public boolean isHomeTeam() {
        return isHomeTeam;
    }

    public void setHomeTeam(boolean isHomeTeam) {
        this.isHomeTeam = isHomeTeam;
    }
}
