package com.emlogis.engine.sqlserver.loader.domain;

import java.io.Serializable;

import javax.persistence.*;

import java.sql.Timestamp;


/**
 * The persistent class for the ShiftDemand database table.
 * 
 */
@Entity
@NamedQuery(name="ShiftDemand.findAll", query="SELECT s FROM ShiftDemand s")
public class ShiftDemand implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name="Date")
	private Timestamp demandDate;

	@Column(name="EndingTime")
	private int endingTime;

	@Column(name="ShiftDescription")
	private String shiftDescription;

	@Column(name="ShiftID")
	private long shiftID;

	@Column(name="ShiftType")
	private long shiftType;

	@Column(name="SiteID")
	private long siteID;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="SiteShiftStructureID")
	private long siteRequirementID;

	@Column(name="SkillID")
	private long skillID;

	@Column(name="SkillName")
	private String skillName;

	@Column(name="StartingTime")
	private int startingTime;

	@Column(name="TeamID")
	private long teamID;

	public ShiftDemand() {
	}

	public Timestamp getDemandDate() {
		return this.demandDate;
	}

	public void setDemandDate(Timestamp demandDate) {
		this.demandDate = demandDate;
	}

	public int getEndingTime() {
		return this.endingTime;
	}

	public void setEndingTime(int endingTime) {
		this.endingTime = endingTime;
	}

	public String getShiftDescription() {
		return this.shiftDescription;
	}

	public void setShiftDescription(String shiftDescription) {
		this.shiftDescription = shiftDescription;
	}

	public long getShiftID() {
		return this.shiftID;
	}

	public void setShiftID(long shiftID) {
		this.shiftID = shiftID;
	}

	public long getShiftType() {
		return this.shiftType;
	}

	public void setShiftType(long shiftType) {
		this.shiftType = shiftType;
	}

	public long getSiteID() {
		return this.siteID;
	}

	public void setSiteID(long siteID) {
		this.siteID = siteID;
	}

	public long getSiteRequirementID() {
		return this.siteRequirementID;
	}

	public void setSiteRequirementID(long siteRequirementID) {
		this.siteRequirementID = siteRequirementID;
	}

	public long getSkillID() {
		return this.skillID;
	}

	public void setSkillID(long skillID) {
		this.skillID = skillID;
	}

	public String getSkillName() {
		return this.skillName;
	}

	public void setSkillName(String skillName) {
		this.skillName = skillName;
	}

	public int getStartingTime() {
		return this.startingTime;
	}

	public void setStartingTime(int startingTime) {
		this.startingTime = startingTime;
	}

	public long getTeamID() {
		return this.teamID;
	}

	public void setTeamID(long teamID) {
		this.teamID = teamID;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ShiftDemand [demandDate=");
		builder.append(demandDate);
		builder.append(", endingTime=");
		builder.append(endingTime);
		builder.append(", shiftDescription=");
		builder.append(shiftDescription);
		builder.append(", shiftID=");
		builder.append(shiftID);
		builder.append(", shiftType=");
		builder.append(shiftType);
		builder.append(", siteID=");
		builder.append(siteID);
		builder.append(", siteRequirementID=");
		builder.append(siteRequirementID);
		builder.append(", skillID=");
		builder.append(skillID);
		builder.append(", skillName=");
		builder.append(skillName);
		builder.append(", startingTime=");
		builder.append(startingTime);
		builder.append(", teamID=");
		builder.append(teamID);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (siteRequirementID ^ (siteRequirementID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ShiftDemand other = (ShiftDemand) obj;
		if (siteRequirementID != other.siteRequirementID)
			return false;
		return true;
	}

}