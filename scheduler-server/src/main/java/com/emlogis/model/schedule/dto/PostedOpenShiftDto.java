package com.emlogis.model.schedule.dto;

import com.emlogis.model.dto.BaseEntityDto;

public class PostedOpenShiftDto extends BaseEntityDto {
	
	private String shiftId;
    private String scheduleId;
    
    private String scheduleName;
    private String siteName;
    
    private String skillId;
    private String skillName;
    private String skillAbbrev;

    private String employeeId;
    private String employeeName;
    
    private String teamId;
    private String teamName;

    private long postId;
    private int shiftLength;
    private long startDateTime;
    private long endDateTime;
    private boolean excess;
    private long deadline;
    private String comments;
    private String terms;
    private boolean isRequested;
    private long requestedOn;

	public String getShiftId() {
		return shiftId;
	}

	public void setShiftId(String shiftId) {
		this.shiftId = shiftId;
	}

	public String getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public String getScheduleName() {
		return scheduleName;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
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



	public long getPostId() {
		return postId;
	}

	public void setPostId(long postId) {
		this.postId = postId;
	}

	public boolean isRequested() {
		return isRequested;
	}

	public void setRequested(boolean isRequested) {
		this.isRequested = isRequested;
	}

	public long getRequestedOn() {
		return requestedOn;
	}

	public void setRequestedOn(long requestedOn) {
		this.requestedOn = requestedOn;
	}
/*
	public String getOverrideOptions() {
		return overrideOptions;
	}

	public void setOverrideOptions(String overrideOptions) {
		this.overrideOptions = overrideOptions;
	}
*/
	public int getShiftLength() {
		return shiftLength;
	}

	public void setShiftLength(int shiftLength) {
		this.shiftLength = shiftLength;
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

	public long getDeadline() {
		return deadline;
	}

	public void setDeadline(long deadline) {
		this.deadline = deadline;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getTerms() {
		return terms;
	}

	public void setTerms(String terms) {
		this.terms = terms;
	}

    
}
