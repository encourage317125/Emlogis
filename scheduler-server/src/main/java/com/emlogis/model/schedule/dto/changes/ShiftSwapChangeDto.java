package com.emlogis.model.schedule.dto.changes;

public class ShiftSwapChangeDto extends BaseScheduleChangeDto {

    private String changeRequestId;

    private String employeeAnewShiftId;

    private String employeeAnewShiftCopy;

    private String employeeBnewShiftId;

    private String employeeBnewShiftCopy;

    public String getChangeRequestId() {
        return changeRequestId;
    }

    public void setChangeRequestId(String changeRequestId) {
        this.changeRequestId = changeRequestId;
    }

    public String getEmployeeAnewShiftId() {
        return employeeAnewShiftId;
    }

    public void setEmployeeAnewShiftId(String employeeAnewShiftId) {
        this.employeeAnewShiftId = employeeAnewShiftId;
    }

    public String getEmployeeAnewShiftCopy() {
        return employeeAnewShiftCopy;
    }

    public void setEmployeeAnewShiftCopy(String employeeAnewShiftCopy) {
        this.employeeAnewShiftCopy = employeeAnewShiftCopy;
    }

    public String getEmployeeBnewShiftId() {
        return employeeBnewShiftId;
    }

    public void setEmployeeBnewShiftId(String employeeBnewShiftId) {
        this.employeeBnewShiftId = employeeBnewShiftId;
    }

    public String getEmployeeBnewShiftCopy() {
        return employeeBnewShiftCopy;
    }

    public void setEmployeeBnewShiftCopy(String employeeBnewShiftCopy) {
        this.employeeBnewShiftCopy = employeeBnewShiftCopy;
    }
}
