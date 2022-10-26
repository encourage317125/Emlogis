package com.emlogis.model.schedule.dto;

import com.emlogis.model.dto.BaseEntityDto;

import java.util.List;
import java.util.Map;

public class ScheduleReportDto extends BaseEntityDto {

    private String completionReport;

    private Object[] employees;

    private ScheduleOverviewDto scheduleOverview;

    private List summaryBySkill;

    public String getCompletionReport() {
        return completionReport;
    }

    public void setCompletionReport(String completionReport) {
        this.completionReport = completionReport;
    }

    public Object[] getEmployees() {
        return employees;
    }

    public void setEmployees(Object[] employees) {
        this.employees = employees;
    }

    public ScheduleOverviewDto getScheduleOverview() {
        return scheduleOverview;
    }

    public void setScheduleOverview(ScheduleOverviewDto scheduleOverview) {
        this.scheduleOverview = scheduleOverview;
    }

    public List getSummaryBySkill() {
        return summaryBySkill;
    }

    public void setSummaryBySkill(List summaryBySkill) {
        this.summaryBySkill = summaryBySkill;
    }
}
