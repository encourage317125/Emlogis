package com.emlogis.model.employee.dto;

import com.emlogis.model.dto.BaseEntityDto;

// NOTE: this dto is very similar to the Schedule.OpenShiftDto.  if there is no diff, we could get rid of that one ..

public class EmployeeOpenShiftDto  extends BaseEntityDto {
		
	private String	shiftId;
    private String 	scheduleId;
    
    private String 	scheduleName;

    private String siteName;
    

    private String skillId;

    private String skillName;

    private String skillAbbrev;


    private String employeeId;
    private String employeeName;
    
    private String teamId;
    private String teamName;

    private long datePosted;
    
    private int shiftLength;

    private long startDateTime;

    private long endDateTime;

    private boolean excess;

    private long deadline;		
    
    private String comments;
    
    private String terms;


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

	public long getDatePosted() {
		return datePosted;
	}

	public void setDatePosted(long datePosted) {
		this.datePosted = datePosted;
	}

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