package com.emlogis.engine.domain.dto;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

public class ShiftDto implements Serializable {

    private String id;
    private DateTime startDateTime;
    private DateTime endDateTime;
    private int requiredEmployeeSize = 1;
    private String skillId;
    private String teamId;
    private boolean isExcessShift = false;
    private boolean beingQualified = false;  // True if shift being qualified or checked for eligibility
    
    // True if this shift is being checked for swap eligibility
    // If true this shift will be checked against shifts where beingQualified == true
    private boolean beingSwapped = false;   
    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(DateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public DateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(DateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public int getRequiredEmployeeSize() {
        return requiredEmployeeSize;
    }

    public void setRequiredEmployeeSize(int requiredEmployeeSize) {
        this.requiredEmployeeSize = requiredEmployeeSize;
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

	/**
	 * @return the beingQualified
	 */
	public boolean getBeingQualified() {
		return beingQualified;
	}

	/**
	 * @param beingQualified the beingQualified to set
	 */
	public void setBeingQualified(boolean beingQualified) {
		this.beingQualified = beingQualified;
	}

	public boolean isBeingSwapped() {
		return beingSwapped;
	}

	public void setBeingSwapped(boolean beingSwapped) {
		this.beingSwapped = beingSwapped;
	}

	public boolean isExcessShift() {
		return isExcessShift;
	}

	public void setExcessShift(boolean isExcessShift) {
		this.isExcessShift = isExcessShift;
	}

}
