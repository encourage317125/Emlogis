package com.emlogis.model.shiftpattern.dto;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.model.shiftpattern.ShiftPatternType;

import java.io.Serializable;

public class ShiftPatternSummaryDto implements Serializable {

    private String siteId;
    private String siteName;
    private DayOfWeek siteFirstDayOfTheWeek;
    private String teamId;
    private String teamName;
    private String skillId;
    private String skillName;
    private String shiftPatternId;
    private String shiftPatternName;
    private ShiftPatternType shiftPatternType;
    private DayOfWeek shiftPatternDayOfWeek;
    private Long shiftPatternCdDate;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public DayOfWeek getSiteFirstDayOfTheWeek() {
        return siteFirstDayOfTheWeek;
    }

    public void setSiteFirstDayOfTheWeek(DayOfWeek siteFirstDayOfTheWeek) {
        this.siteFirstDayOfTheWeek = siteFirstDayOfTheWeek;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getSkillId() {
        return skillId;
    }

    public void setSkillId(String skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getShiftPatternId() {
        return shiftPatternId;
    }

    public void setShiftPatternId(String shiftPatternId) {
        this.shiftPatternId = shiftPatternId;
    }

    public String getShiftPatternName() {
        return shiftPatternName;
    }

    public void setShiftPatternName(String shiftPatternName) {
        this.shiftPatternName = shiftPatternName;
    }

    public ShiftPatternType getShiftPatternType() {
        return shiftPatternType;
    }

    public void setShiftPatternType(ShiftPatternType shiftPatternType) {
        this.shiftPatternType = shiftPatternType;
    }

    public DayOfWeek getShiftPatternDayOfWeek() {
        return shiftPatternDayOfWeek;
    }

    public void setShiftPatternDayOfWeek(DayOfWeek shiftPatternDayOfWeek) {
        this.shiftPatternDayOfWeek = shiftPatternDayOfWeek;
    }

    public Long getShiftPatternCdDate() {
        return shiftPatternCdDate;
    }

    public void setShiftPatternCdDate(Long shiftPatternCdDate) {
        this.shiftPatternCdDate = shiftPatternCdDate;
    }
}
