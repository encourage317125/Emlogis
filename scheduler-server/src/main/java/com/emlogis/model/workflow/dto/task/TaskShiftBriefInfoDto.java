package com.emlogis.model.workflow.dto.task;

/**
 * Created by alex on 4/6/15.
 */
public class TaskShiftBriefInfoDto {

    private String id;
    private Long startDateTime;
    private Long endDateTime;
    private String teamId;
    private String teamName;
    private String skillId;
    private String skillName;

    public TaskShiftBriefInfoDto() {
    }

    public TaskShiftBriefInfoDto(
            String shiftId, Long shiftTimefrom, Long shiftTimeTo, String shiftTeam, String shiftSkill
    ) {
        this.id = shiftId;
        this.startDateTime = shiftTimefrom;
        this.endDateTime = shiftTimeTo;
        this.teamName = shiftTeam;
        this.skillName = shiftSkill;
    }

    public TaskShiftBriefInfoDto(
            String id,
            Long startDateTime,
            Long endDateTime,
            String teamId,
            String teamName,
            String skillId,
            String skillName
    ) {
        this.id = id;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.teamId = teamId;
        this.teamName = teamName;
        this.skillId = skillId;
        this.skillName = skillName;
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

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
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
}
