package com.emlogis.model.dto;

import com.emlogis.model.schedule.ScheduleStatus;
import com.emlogis.model.schedule.TaskState;

public class ScheduleQueryDto {

    private String id;
    private String name;
    private String description;
    private long startDate;
    private int scheduleLengthInDays;
    private ScheduleStatus status;
    private TaskState state;
    private long executionStartDate;
    private long executionEndDate;
    private long created;
    private long updated;
    private String createdBy;
    private String updatedBy;
    private int returnedOpenShifts;
    private int returnedAssignedShifts;
    private String teamNames;

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

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public int getScheduleLengthInDays() {
        return scheduleLengthInDays;
    }

    public void setScheduleLengthInDays(int scheduleLengthInDays) {
        this.scheduleLengthInDays = scheduleLengthInDays;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public long getExecutionStartDate() {
        return executionStartDate;
    }

    public void setExecutionStartDate(long executionStartDate) {
        this.executionStartDate = executionStartDate;
    }

    public long getExecutionEndDate() {
        return executionEndDate;
    }

    public void setExecutionEndDate(long executionEndDate) {
        this.executionEndDate = executionEndDate;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public int getReturnedOpenShifts() {
        return returnedOpenShifts;
    }

    public void setReturnedOpenShifts(int returnedOpenShifts) {
        this.returnedOpenShifts = returnedOpenShifts;
    }

    public int getReturnedAssignedShifts() {
        return returnedAssignedShifts;
    }

    public void setReturnedAssignedShifts(int returnedAssignedShifts) {
        this.returnedAssignedShifts = returnedAssignedShifts;
    }

    public String getTeamNames() {
        return teamNames;
    }

    public void setTeamNames(String teamNames) {
        this.teamNames = teamNames;
    }
}
