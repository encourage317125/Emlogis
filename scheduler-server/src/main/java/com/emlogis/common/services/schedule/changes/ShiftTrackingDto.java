package com.emlogis.common.services.schedule.changes;

import com.emlogis.model.dto.BaseEntityDto;
import com.emlogis.model.schedule.AssignmentType;
import com.emlogis.model.schedule.ScheduleStatus;
import com.emlogis.model.schedule.ShiftChangeType;

public class ShiftTrackingDto extends BaseEntityDto {

    private String scheduleId;
    
    private ScheduleStatus 	scheduleStatus = ScheduleStatus.Simulation;

    private String teamId;

    private String teamName;

    private String siteName;

    private String shiftStructureId;

    private boolean locked;

    private String shiftLengthId;

    private String shiftLengthName;

    private int shiftLength;

    private int paidTime;

    private String skillId;

    private String skillName;

    private String skillAbbrev;

    private int skillProficiencyLevel;

    private String employeeId;

    private String employeeName;

    private AssignmentType assignmentType;

    private long assigned;

    private long startDateTime;

    private long endDateTime;

    private boolean excess;

    
    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public ScheduleStatus getScheduleStatus() {
		return scheduleStatus;
	}

	public void setScheduleStatus(ScheduleStatus scheduleStatus) {
		this.scheduleStatus = scheduleStatus;
	}

	public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getShiftStructureId() {
        return shiftStructureId;
    }

    public void setShiftStructureId(String shiftStructureId) {
        this.shiftStructureId = shiftStructureId;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getShiftLengthId() {
        return shiftLengthId;
    }

    public void setShiftLengthId(String shiftLengthId) {
        this.shiftLengthId = shiftLengthId;
    }

    public String getShiftLengthName() {
        return shiftLengthName;
    }

    public void setShiftLengthName(String shiftLengthName) {
        this.shiftLengthName = shiftLengthName;
    }

    public int getShiftLength() {
        return shiftLength;
    }

    public void setShiftLength(int shiftLength) {
        this.shiftLength = shiftLength;
    }

    public int getPaidTime() {
        return paidTime;
    }

    public void setPaidTime(int paidTime) {
        this.paidTime = paidTime;
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

    public AssignmentType getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(AssignmentType assignmentType) {
        this.assignmentType = assignmentType;
    }

    public long getAssigned() {
        return assigned;
    }

    public void setAssigned(long assigned) {
        this.assigned = assigned;
    }

    public long getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(long startDateTime) {
        this.startDateTime = startDateTime;
    }

    public long getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(long endDateTime) {
        this.endDateTime = endDateTime;
    }

    public boolean isExcess() {
        return excess;
    }

    public void setExcess(boolean excess) {
        this.excess = excess;
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

    public String getSkillAbbrev() {
        return skillAbbrev;
    }

    public void setSkillAbbrev(String skillAbbrev) {
        this.skillAbbrev = skillAbbrev;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

}
