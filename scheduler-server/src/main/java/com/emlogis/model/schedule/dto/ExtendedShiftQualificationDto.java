package com.emlogis.model.schedule.dto;

import java.util.Collection;

import com.emlogis.engine.domain.communication.ShiftQualificationDto;
import com.emlogis.engine.domain.communication.constraints.ShiftConstraintDto;

public class ExtendedShiftQualificationDto extends ShiftQualificationDto{
	
	// additional information retrieved once reponse from engine is received, to enrich the 
	// the ShiftQualificationDto result data
    private String skillId;
    private String skillName;
    private String skillAbbrev;
    private String teamId;
    private String teamName;
    private int shiftLength;
    private long startDateTime;
    private long endDateTime;
    private boolean excess;
    
	public ExtendedShiftQualificationDto(ShiftQualificationDto rawQShiftDto) {
		this.setShiftId(rawQShiftDto.getShiftId());
		this.setEmployeeId(rawQShiftDto.getEmployeeId());
		this.setEmployeeName(rawQShiftDto.getEmployeeName());
		this.setIsAccepted(rawQShiftDto.getIsAccepted());
		this.setCauses(rawQShiftDto.getCauses());
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

    

}
