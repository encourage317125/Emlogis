package com.emlogis.model.schedule.dto;

import com.emlogis.engine.domain.DayOfWeek;

import java.io.Serializable;

public class SchedulePatternDto implements Serializable {

    private Integer dayOffset;
    private Long cdDate;
    private String patternId;
    private String patternName;
    private DayOfWeek patternDayOfWeek;
    private Long patternCdDate;
    private String patternTeamName;

    public Integer getDayOffset() {
        return dayOffset;
    }

    public void setDayOffset(Integer dayOffset) {
        this.dayOffset = dayOffset;
    }

    public Long getCdDate() {
        return cdDate;
    }

    public void setCdDate(Long cdDate) {
        this.cdDate = cdDate;
    }

    public String getPatternId() {
        return patternId;
    }

    public void setPatternId(String patternId) {
        this.patternId = patternId;
    }

    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }

    public DayOfWeek getPatternDayOfWeek() {
        return patternDayOfWeek;
    }

    public void setPatternDayOfWeek(DayOfWeek patternDayOfWeek) {
        this.patternDayOfWeek = patternDayOfWeek;
    }

    public Long getPatternCdDate() {
        return patternCdDate;
    }

    public void setPatternCdDate(Long patternCdDate) {
        this.patternCdDate = patternCdDate;
    }

    public String getPatternTeamName() {
        return patternTeamName;
    }

    public void setPatternTeamName(String patternTeamName) {
        this.patternTeamName = patternTeamName;
    }
}
