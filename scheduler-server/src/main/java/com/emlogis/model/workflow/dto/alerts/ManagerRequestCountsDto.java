package com.emlogis.model.workflow.dto.alerts;

public class ManagerRequestCountsDto extends TeamRequestCountsDto {

    private int pendingManagerRequests = -1;
    private int newManagerRequests = -1;

    public int getPendingManagerRequests() {
        return pendingManagerRequests;
    }

    public void setPendingManagerRequests(int pendingManagerRequests) {
        this.pendingManagerRequests = pendingManagerRequests;
    }

    public int getNewManagerRequests() {
        return newManagerRequests;
    }

    public void setNewManagerRequests(int newManagerRequests) {
        this.newManagerRequests = newManagerRequests;
    }

}
