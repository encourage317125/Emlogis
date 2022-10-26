package com.emlogis.model.schedule.dto;

import com.emlogis.model.dto.ResultSetDto;

public class ScheduleViewBaseDto extends ScheduleDto {

    private Object[] siteInfo;
    private ResultSetDto<Object[]> teamsInfo;
    private ResultSetDto<Object[]> employeesInfo;
    private ResultSetDto<Object[]> skillsInfo;
    private int totalMinutes;
    private int overtimeHours;
    private int unfilledShiftCount;
    private int totalCost;

    public Object[] getSiteInfo() {
        return siteInfo;
    }

    public void setSiteInfo(Object[] siteInfo) {
        this.siteInfo = siteInfo;
    }

    public ResultSetDto<Object[]> getTeamsInfo() {
        return teamsInfo;
    }

    public void setTeamsInfo(ResultSetDto<Object[]> teamsInfo) {
        this.teamsInfo = teamsInfo;
    }

    public ResultSetDto<Object[]> getEmployeesInfo() {
        return employeesInfo;
    }

    public void setEmployeesInfo(ResultSetDto<Object[]> employeesInfo) {
        this.employeesInfo = employeesInfo;
    }

    public ResultSetDto<Object[]> getSkillsInfo() {
        return skillsInfo;
    }

    public void setSkillsInfo(ResultSetDto<Object[]> skillsInfo) {
        this.skillsInfo = skillsInfo;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(int totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public int getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(int overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public int getUnfilledShiftCount() {
        return unfilledShiftCount;
    }

    public void setUnfilledShiftCount(int unfilledShiftCount) {
        this.unfilledShiftCount = unfilledShiftCount;
    }

    public int getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(int totalCost) {
        this.totalCost = totalCost;
    }
}
