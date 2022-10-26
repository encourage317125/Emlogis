package com.emlogis.engine.sqlserver.loader.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQuery;

import com.emlogis.engine.sqlserver.loader.domain.EmployeeIDToTeamID.EmployeeIDToTeamIDPK;
import com.emlogis.engine.sqlserver.loader.domain.EmployeeSkill.EmployeeSkillPK;


/**
 * The persistent class for the EmployeeIDToTeamID database table.
 * 
 */
@Entity
@NamedQuery(name="EmployeeIDToTeamID.findAll", query="SELECT e FROM EmployeeIDToTeamID e")
@IdClass(value = EmployeeIDToTeamIDPK.class)
public class EmployeeIDToTeamID implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="EmployeeID")
	private long employeeID;

	@Column(name="SiteID")
	private long siteID;

	@Id
	@Column(name="TeamID")
	private long teamID;

	@Column(name="TeamStatusValue")
	private int teamStatusValue;

	public EmployeeIDToTeamID() {
	}

	public long getEmployeeID() {
		return this.employeeID;
	}

	public void setEmployeeID(long employeeID) {
		this.employeeID = employeeID;
	}

	public long getSiteID() {
		return this.siteID;
	}

	public void setSiteID(long siteID) {
		this.siteID = siteID;
	}

	public long getTeamID() {
		return this.teamID;
	}

	public void setTeamID(long teamID) {
		this.teamID = teamID;
	}

	public int getTeamStatusValue() {
		return this.teamStatusValue;
	}

	public void setTeamStatusValue(int teamStatusValue) {
		this.teamStatusValue = teamStatusValue;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EmployeeIDToTeamID [siteID=");
		builder.append(siteID);
		builder.append(", teamID=");
		builder.append(teamID);
		builder.append(", employeeID=");
		builder.append(employeeID);
		builder.append(", teamStatusValue=");
		builder.append(teamStatusValue);
		builder.append("]");
		return builder.toString();
	}

	public static class EmployeeIDToTeamIDPK implements Serializable{
		private static final long serialVersionUID = 1L;
		
		private long employeeID;
		private long teamID;
		
		public EmployeeIDToTeamIDPK() {	}
		
		public EmployeeIDToTeamIDPK(long employeeID, long teamID) {
			this.employeeID = employeeID;
			this.teamID = teamID;
		}
		
		public long getEmployeeID() {
			return employeeID;
		}
		public void setEmployeeID(long employeeID) {
			this.employeeID = employeeID;
		}
		public long getTeamID() {
			return teamID;
		}
		public void setTeamID(long teamID) {
			this.teamID = teamID;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (employeeID ^ (employeeID >>> 32));
			result = prime * result + (int) (teamID ^ (teamID >>> 32));
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
			EmployeeIDToTeamIDPK other = (EmployeeIDToTeamIDPK) obj;
			if (employeeID != other.employeeID)
				return false;
			if (teamID != other.teamID)
				return false;
			return true;
		}
	}

}