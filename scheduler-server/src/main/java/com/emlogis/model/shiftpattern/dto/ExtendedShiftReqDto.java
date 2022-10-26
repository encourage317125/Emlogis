package com.emlogis.model.shiftpattern.dto;

/**
 * Created by Andrii Mozharovskyi on 9/17/15.
 */
public class ExtendedShiftReqDto extends ShiftReqDto {

    private Long cdDate;
    private Integer dayOfTheWeek;
    private long shiftStartTimeMillis;
    private long shiftLengthMillis;

    private String teamId;
    private String skillId;

    public Long getCdDate() {
        return cdDate;
    }

    public void setCdDate(Long date) {
        this.cdDate = date;
    }

    public Integer getDayOfTheWeek() {
        return dayOfTheWeek;
    }

    public void setDayOfTheWeek(Integer dayOfTheWeek) {
        this.dayOfTheWeek = dayOfTheWeek;
    }

    public long getShiftStartTimeMillis() {
        return shiftStartTimeMillis;
    }

    public void setShiftStartTimeMillis(long shiftStartTimeMillis) {
        this.shiftStartTimeMillis = shiftStartTimeMillis;
    }

    public long getShiftLengthMillis() {
        return shiftLengthMillis;
    }

    public void setShiftLengthMillis(long shiftLengthMillis) {
        this.shiftLengthMillis = shiftLengthMillis;
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
