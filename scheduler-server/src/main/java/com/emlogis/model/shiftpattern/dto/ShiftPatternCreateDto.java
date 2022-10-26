package com.emlogis.model.shiftpattern.dto;

import com.emlogis.model.dto.CreateDto;

public class ShiftPatternCreateDto extends CreateDto<ShiftPatternUpdateDto> {

    public static final String UPDATE_DAY_OF_WEEK = UPDATE_DTO + ".dayOfWeek";
    public static final String UPDATE_CD_DATE = UPDATE_DTO + ".cdDate";

    private String name;
    private String skillId;
    private String teamId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSkillId() {
        return skillId;
    }

    public void setSkillId(String skillId) {
        this.skillId = skillId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
}
