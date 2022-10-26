package com.emlogis.model.workflow.dto.reports.requests;

import com.emlogis.model.workflow.dto.reports.base.WflReportDataDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by Andrii Mozharovskyi on 8/11/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeOffWflReportDataDto extends WflReportDataDto implements Serializable {

    private String employeeName;
    private Long dayOffDate;
    private String absenceType;
    private String status;

    public TimeOffWflReportDataDto() {
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Long getDayOffDate() {
        return dayOffDate;
    }

    public void setDayOffDate(Long dayOffDate) {
        this.dayOffDate = dayOffDate;
    }

    public String getAbsenceType() {
        return absenceType;
    }

    public void setAbsenceType(String absenceType) {
        this.absenceType = absenceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
