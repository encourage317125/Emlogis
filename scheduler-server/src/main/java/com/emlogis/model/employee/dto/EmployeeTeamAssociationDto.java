package com.emlogis.model.employee.dto;

public class EmployeeTeamAssociationDto {

    private String teamId;
    private Boolean isFloating;
    private Boolean isSchedulable;

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public Boolean getIsFloating() {
        return isFloating;
    }

    public void setIsFloating(Boolean isFloating) {
        this.isFloating = isFloating;
    }

    public Boolean getIsSchedulable() {
        return isSchedulable;
    }

    public void setIsSchedulable(Boolean isSchedulable) {
        this.isSchedulable = isSchedulable;
    }
}
