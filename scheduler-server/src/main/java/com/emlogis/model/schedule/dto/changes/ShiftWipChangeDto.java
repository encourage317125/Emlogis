package com.emlogis.model.schedule.dto.changes;

public class ShiftWipChangeDto extends BaseScheduleChangeDto {

    private String changeRequestId;

    private String previousEmployeeId;

    private String previousEmployeeName;

    private String wipEmployeeId;

    private String wipEmployeeName;

    private String shiftId;

    private String shiftCopy;

    public String getChangeRequestId() {
        return changeRequestId;
    }

    public void setChangeRequestId(String changeRequestId) {
        this.changeRequestId = changeRequestId;
    }

    public String getPreviousEmployeeId() {
        return previousEmployeeId;
    }

    public void setPreviousEmployeeId(String previousEmployeeId) {
        this.previousEmployeeId = previousEmployeeId;
    }

    public String getPreviousEmployeeName() {
        return previousEmployeeName;
    }

    public void setPreviousEmployeeName(String previousEmployeeName) {
        this.previousEmployeeName = previousEmployeeName;
    }

    public String getWipEmployeeId() {
        return wipEmployeeId;
    }

    public void setWipEmployeeId(String wipEmployeeId) {
        this.wipEmployeeId = wipEmployeeId;
    }

    public String getWipEmployeeName() {
        return wipEmployeeName;
    }

    public void setWipEmployeeName(String wipEmployeeName) {
        this.wipEmployeeName = wipEmployeeName;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getShiftCopy() {
        return shiftCopy;
    }

    public void setShiftCopy(String shiftCopy) {
        this.shiftCopy = shiftCopy;
    }
}
