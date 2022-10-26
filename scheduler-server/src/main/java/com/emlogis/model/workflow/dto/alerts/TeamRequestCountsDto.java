package com.emlogis.model.workflow.dto.alerts;

public class TeamRequestCountsDto {

    private int pendingTeamRequests = -1;
    private int newTeamRequests = -1;

    public int getPendingTeamRequests() {
        return pendingTeamRequests;
    }

    public void setPendingTeamRequests(int pendingTeamRequests) {
        this.pendingTeamRequests = pendingTeamRequests;
    }

    public int getNewTeamRequests() {
        return newTeamRequests;
    }

    public void setNewTeamRequests(int newTeamRequests) {
        this.newTeamRequests = newTeamRequests;
    }

}
