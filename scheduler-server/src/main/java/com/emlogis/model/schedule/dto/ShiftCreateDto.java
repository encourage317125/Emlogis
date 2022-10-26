package com.emlogis.model.schedule.dto;

import com.emlogis.model.dto.CreateDto;

import java.io.Serializable;

public class ShiftCreateDto extends CreateDto implements Serializable {

    private String teamId;
	private String skillId;
	private int skillProficiencyLevel;
	private long startDateTime;
	private long endDateTime;
	private int paidTime;
	private String siteName;
	private String skillAbbrev;
	private String comment;

	public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
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

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getSkillAbbrev() {
		return skillAbbrev;
	}

	public void setSkillAbbrev(String skillAbbrev) {
		this.skillAbbrev = skillAbbrev;
	}

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
