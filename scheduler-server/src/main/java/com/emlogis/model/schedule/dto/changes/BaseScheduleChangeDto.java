package com.emlogis.model.schedule.dto.changes;

import com.emlogis.model.dto.BaseEntityDto;
import com.emlogis.model.schedule.changes.ChangeCategory;
import com.emlogis.model.schedule.changes.ChangeType;

public abstract class BaseScheduleChangeDto extends BaseEntityDto {

    private String scheduleId;

    private String scheduleName;

    private long scheduleStartDate;

    private long scheduleEndDate;

    private long changeDate;

    private String reason;

    private ChangeCategory category;

    private ChangeType type;

    private String changeEmployeeId;

    private String changeEmployeeName;

    private String employeeAId;

    private String employeeAName;

    private String employeeBId;

    private String employeeBName;

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public long getScheduleStartDate() {
        return scheduleStartDate;
    }

    public void setScheduleStartDate(long scheduleStartDate) {
        this.scheduleStartDate = scheduleStartDate;
    }

    public long getScheduleEndDate() {
        return scheduleEndDate;
    }

    public void setScheduleEndDate(long scheduleEndDate) {
        this.scheduleEndDate = scheduleEndDate;
    }

    public long getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(long changeDate) {
        this.changeDate = changeDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ChangeType getType() {
        return type;
    }

    public void setType(ChangeType type) {
        this.type = type;
    }

    public ChangeCategory getCategory() {
        return category;
    }

    public void setCategory(ChangeCategory category) {
        this.category = category;
    }

    public String getChangeEmployeeId() {
        return changeEmployeeId;
    }

    public void setChangeEmployeeId(String changeEmployeeId) {
        this.changeEmployeeId = changeEmployeeId;
    }

    public String getChangeEmployeeName() {
        return changeEmployeeName;
    }

    public void setChangeEmployeeName(String changeEmployeeName) {
        this.changeEmployeeName = changeEmployeeName;
    }

    public String getEmployeeAId() {
        return employeeAId;
    }

    public void setEmployeeAId(String employeeAId) {
        this.employeeAId = employeeAId;
    }

    public String getEmployeeAName() {
        return employeeAName;
    }

    public void setEmployeeAName(String employeeAName) {
        this.employeeAName = employeeAName;
    }

    public String getEmployeeBId() {
        return employeeBId;
    }

    public void setEmployeeBId(String employeeBId) {
        this.employeeBId = employeeBId;
    }

    public String getEmployeeBName() {
        return employeeBName;
    }

    public void setEmployeeBName(String employeeBName) {
        this.employeeBName = employeeBName;
    }
}
