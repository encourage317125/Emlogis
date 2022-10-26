package com.emlogis.model.schedule.dto;

import com.emlogis.model.dto.BaseEntityDto;

public class ShiftReqOldDto extends BaseEntityDto {

    public static final String SHIFT_STRUCTURE_ID = "shiftStructureId";

    private String shiftStructureId;

    private long startTime;

    private int durationInMins;

    private int dayIndex;

    private boolean night;

    private boolean excess;

    private int employeeCount;

    private String skillId;

    private int skillProficiencyLevel;

    private String shiftLengthId;

    private String shiftLengthName;

    private String skillName;

    public String getShiftStructureId() {
        return shiftStructureId;
    }

    public void setShiftStructureId(String shiftStructureId) {
        this.shiftStructureId = shiftStructureId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getDurationInMins() {
        return durationInMins;
    }

    public void setDurationInMins(int durationInMins) {
        this.durationInMins = durationInMins;
    }

    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public boolean isNight() {
        return night;
    }

    public void setNight(boolean night) {
        this.night = night;
    }

    public boolean isExcess() {
        return excess;
    }

    public void setExcess(boolean isExcess) {
        this.excess = isExcess;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }

    public String getSkillId() {
        return skillId;
    }

    public void setSkillId(String skillId) {
        this.skillId = skillId;
    }

    public int getSkillProficiencyLevel() {
        return skillProficiencyLevel;
    }

    public void setSkillProficiencyLevel(int skillProficiencyLevel) {
        this.skillProficiencyLevel = skillProficiencyLevel;
    }

    public String getShiftLengthId() {
        return shiftLengthId;
    }

    public void setShiftLengthId(String shiftLengthId) {
        this.shiftLengthId = shiftLengthId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

	public String getShiftLengthName() {
		return shiftLengthName;
	}

	public void setShiftLengthName(String shiftLengthName) {
		this.shiftLengthName = shiftLengthName;
	}
}
