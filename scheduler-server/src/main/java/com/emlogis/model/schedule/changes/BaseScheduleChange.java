package com.emlogis.model.schedule.changes;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
//@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BaseScheduleChange extends BaseChangeEntity {

    private String scheduleId;

    private String scheduleName;

    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime scheduleStartDate;

    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime scheduleEndDate;

    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime changeDate;

    @Enumerated(EnumType.STRING)
    private ChangeCategory category;

    @Enumerated(EnumType.STRING)
    private ChangeType type;

    private String reason;

    private String changeEmployeeId;

    private String changeEmployeeName;

    private String employeeAId;

    private String employeeAName;

    private String employeeBId;

    private String employeeBName;
    
//    ATTRIBUTE TO BE ADDED
//    private String changeRequestId;		// workflow request Id to relate the change to a request when change triggered by request (vs UI)


    public BaseScheduleChange() {}

    public BaseScheduleChange(ChangePrimaryKey primaryKey) {
        super(primaryKey);
    }

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
        return scheduleStartDate == null ? null : scheduleStartDate.getMillis();
    }

    public void setScheduleStartDate(long scheduleStartDate) {
        this.scheduleStartDate = new DateTime(scheduleStartDate);
    }

    public long getScheduleEndDate() {
        return scheduleEndDate.getMillis();
    }

    public void setScheduleEndDate(long scheduleEndDate) {
        this.scheduleEndDate = new DateTime(scheduleEndDate);
    }

    public long getChangeDate() {
        return changeDate == null ? null : changeDate.getMillis();
    }

    public void setChangeDate(long changeDate) {
        this.changeDate = new DateTime(changeDate);
    }

    public ChangeCategory getCategory() {
        return category;
    }

    public void setCategory(ChangeCategory category) {
        this.category = category;
    }

    public ChangeType getType() {
        return type;
    }

    public void setType(ChangeType type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public String getEmployeeBName() {
        return employeeBName;
    }

    public void setEmployeeBName(String employeeBName) {
        this.employeeBName = employeeBName;
    }

    public String getEmployeeBId() {
        return employeeBId;
    }

    public void setEmployeeBId(String employeeBId) {
        this.employeeBId = employeeBId;
    }

    public String getEmployeeAName() {
        return employeeAName;
    }

    public void setEmployeeAName(String employeeAName) {
        this.employeeAName = employeeAName;
    }

    public void setEmployeeAId(String employeeAId) {
        this.employeeAId = employeeAId;
    }
}
