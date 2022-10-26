package com.emlogis.model.schedule.dto.changes;

public class ShiftAssignChangeDto extends BaseScheduleChangeDto {

    private String changeRequestId; //TODO or info about openshift

    private String employeeId;

    private String employeeName;

    private String shiftId;

    private String shiftCopy;

    public String getChangeRequestId() {
        return changeRequestId;
    }

    public void setChangeRequestId(String changeRequestId) {
        this.changeRequestId = changeRequestId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
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
