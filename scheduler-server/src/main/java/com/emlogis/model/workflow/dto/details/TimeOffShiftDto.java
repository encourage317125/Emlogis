package com.emlogis.model.workflow.dto.details;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lucas on 18.06.2015.
 */
public class TimeOffShiftDto implements Serializable {

    private String id;
    private Long startDateTime;
    private Long endDateTime;
    private String skillId;
    private String skillName;
    private String skillAbbrev;
    private Integer shiftLength;
    private Boolean excess;
    private String teamId;
    private String teamName;
    private String employeeId;
    private String employeeName;

    public TimeOffShiftDto() {
    }

    public TimeOffShiftDto(Object[] dbResult, String employeeId, String employeeName) {
        List<Object> dr = Arrays.asList(dbResult);
        this.id = (String) dr.get(0);
        this.startDateTime = ((java.sql.Timestamp)dr.get(1)).getTime();
        this.endDateTime = ((java.sql.Timestamp)dr.get(2)).getTime();
        this.skillId = (String) dr.get(3);
        this.skillName = (String) dr.get(4);
        this.skillAbbrev = (String) dr.get(5);
        this.shiftLength = (Integer) dr.get(6);
        this.excess = (Boolean) dr.get(7);
        this.teamId = (String) dr.get(8);
        this.teamName = (String) dr.get(9);
        this.employeeId = employeeId;
        this.employeeName = employeeName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Long startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Long getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Long endDateTime) {
        this.endDateTime = endDateTime;
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

    public String getSkillAbbrev() {
        return skillAbbrev;
    }

    public void setSkillAbbrev(String skillAbbrev) {
        this.skillAbbrev = skillAbbrev;
    }

    public Integer getShiftLength() {
        return shiftLength;
    }

    public void setShiftLength(Integer shiftLength) {
        this.shiftLength = shiftLength;
    }

    public Boolean getExcess() {
        return excess;
    }

    public void setExcess(Boolean excess) {
        this.excess = excess;
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
}
