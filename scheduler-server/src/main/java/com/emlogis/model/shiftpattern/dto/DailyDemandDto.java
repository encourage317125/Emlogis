package com.emlogis.model.shiftpattern.dto;

import com.emlogis.engine.domain.DayOfWeek;

/**
 * Created by Andrii Mozharovskyi on 10/9/15.
 */
public class DailyDemandDto extends ShiftDemandDto {
    private String shiftPatternId;
    private String teamId;
    private String skillId;
    private DayOfWeek dayOfWeek;
    private Long cdDate;
    private int maxEmployeeCount;

    public String getShiftPatternId() {
        return shiftPatternId;
    }

    public void setShiftPatternId(String shiftPatternId) {
        this.shiftPatternId = shiftPatternId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getSkillId() {
        return skillId;
    }

    public void setSkillId(String skillId) {
        this.skillId = skillId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Long getCdDate() {
        return cdDate;
    }

    public void setCdDate(Long cdDate) {
        this.cdDate = cdDate;
    }

    public int getMaxEmployeeCount() {
        return maxEmployeeCount;
    }

    public void setMaxEmployeeCount(int maxEmployeeCount) {
        this.maxEmployeeCount = maxEmployeeCount;
    }
}
