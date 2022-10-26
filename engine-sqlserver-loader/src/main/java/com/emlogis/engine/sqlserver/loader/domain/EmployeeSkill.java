package com.emlogis.engine.sqlserver.loader.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.emlogis.engine.sqlserver.loader.domain.EmployeeSkill.EmployeeSkillPK;


/**
 * The persistent class for the EmployeeSkills database table.
 * 
 */
@Entity(name="opta_EmployeeSkill")
@Table(name="EmployeeSkills")
@NamedQuery(name="EmployeeSkill.findAll", query="SELECT e FROM opta_EmployeeSkill e")
@IdClass(EmployeeSkillPK.class)
public class EmployeeSkill implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="EmployeeID", insertable=false, updatable=false)
	private long employeeID;

	@Column(name="IsPrimary", insertable=false, updatable=false)
	private boolean isPrimary;

	@Column(name="Name", insertable=false, updatable=false)
	private String name;

	@Column(name="SiteID", insertable=false, updatable=false)
	private long siteID;

	@Id
	@Column(name="SkillID", insertable=false, updatable=false)
	private long skillID;

	@Column(name="TeamID", insertable=false, updatable=false)
	private long teamID;

	public EmployeeSkill() {
	}

	public long getEmployeeID() {
		return this.employeeID;
	}

	public void setEmployeeID(long employeeID) {
		this.employeeID = employeeID;
	}

	public boolean getIsPrimary() {
		return this.isPrimary;
	}

	public void setIsPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSiteID() {
		return this.siteID;
	}

	public void setSiteID(long siteID) {
		this.siteID = siteID;
	}

	public long getSkillID() {
		return this.skillID;
	}

	public void setSkillID(long skillID) {
		this.skillID = skillID;
	}

	public long getTeamID() {
		return this.teamID;
	}

	public void setTeamID(long teamID) {
		this.teamID = teamID;
	}
	
	public static class EmployeeSkillPK implements Serializable{
		private static final long serialVersionUID = 1L;
		
		private long employeeID;
		private long skillID;
		
		public EmployeeSkillPK() {
		}
		
		public EmployeeSkillPK(long employeeID, long skillID) {
			this.employeeID = employeeID;
			this.skillID = skillID;
		}
		
		public long getEmployeeID() {
			return employeeID;
		}
		public void setEmployeeID(long employeeID) {
			this.employeeID = employeeID;
		}
		public long getSkillID() {
			return skillID;
		}
		public void setSkillID(long skillID) {
			this.skillID = skillID;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (employeeID ^ (employeeID >>> 32));
			result = prime * result + (int) (skillID ^ (skillID >>> 32));
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
			EmployeeSkillPK other = (EmployeeSkillPK) obj;
			if (employeeID != other.employeeID)
				return false;
			if (skillID != other.skillID)
				return false;
			return true;
		}
		
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EmployeeSkill [employeeID=");
		builder.append(employeeID);
		builder.append(", skillID=");
		builder.append(skillID);
		builder.append(", name=");
		builder.append(name);
		builder.append(", isPrimary=");
		builder.append(isPrimary);
		builder.append(", siteID=");
		builder.append(siteID);
		builder.append(", teamID=");
		builder.append(teamID);
		builder.append("]");
		return builder.toString();
	}

}