package com.emlogis.model.schedule.dto;

public class ScheduleExecuteDto extends ExecuteDto {

    private Boolean preservePreAssignedShifts;
    private Boolean preservePostAssignedShifts;
    private Boolean preserveEngineAssignedShifts;

    public Boolean getPreservePreAssignedShifts() {
        return preservePreAssignedShifts;
    }

    public void setPreservePreAssignedShifts(Boolean preservePreAssignedShifts) {
        this.preservePreAssignedShifts = preservePreAssignedShifts;
    }

    public Boolean getPreservePostAssignedShifts() {
        return preservePostAssignedShifts;
    }

    public void setPreservePostAssignedShifts(Boolean preservePostAssignedShifts) {
        this.preservePostAssignedShifts = preservePostAssignedShifts;
    }

    public Boolean getPreserveEngineAssignedShifts() {
        return preserveEngineAssignedShifts;
    }

    public void setPreserveEngineAssignedShifts(Boolean preserveEngineAssignedShifts) {
        this.preserveEngineAssignedShifts = preserveEngineAssignedShifts;
    }
}
