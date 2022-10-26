package com.emlogis.engine.sqlserver.loader.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.emlogis.engine.sqlserver.loader.domain.ProductionShiftSummary.ProductionShiftSummaryPK;


/**
 * The persistent class for the ProductionShiftSummary database table.
 * 
 */
@Entity
@NamedQuery(name="ProductionShiftSummary.findAll", query="SELECT p FROM ProductionShiftSummary p")
@IdClass(ProductionShiftSummaryPK.class)
public class ProductionShiftSummary implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name="EmployeeID", insertable=false, updatable=false)
	@Id
	private long employeeID;

	@Column(name="EmployeeIdentifier", insertable=false, updatable=false)
	private String employeeIdentifier;

	@Column(name="EndTime", insertable=false, updatable=false)
	private int endTime;

	@Column(name="PaidHours", insertable=false, updatable=false)
	private int paidHours;

	@Column(name="ScheduleName", insertable=false, updatable=false)
	@Id
	private String scheduleName;

	@Column(name="ScheduleStatus", insertable=false, updatable=false)
	private int scheduleStatus;

	@Column(name="ShiftDate", insertable=false, updatable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date shiftDate;

	@Column(name="ShiftID", insertable=false, updatable=false)
	@Id
	private long shiftID;

	@Column(name="ShiftType", insertable=false, updatable=false)
	private long shiftType;

	@Column(name="SiteName", insertable=false, updatable=false)
	private String siteName;

	@Column(name="SiteRequirementID", insertable=false, updatable=false)
	@Id
	private long siteRequirementID;

	@Column(name="StartTime", insertable=false, updatable=false)
	private int startTime;

	@Column(name="TeamID", insertable=false, updatable=false)
	private long teamId;

	@Column(name="TeamName", insertable=false, updatable=false)
	private String teamName;

	public ProductionShiftSummary() {
	}

	public long getEmployeeID() {
		return this.employeeID;
	}

	public void setEmployeeID(long employeeID) {
		this.employeeID = employeeID;
	}

	public String getEmployeeIdentifier() {
		return this.employeeIdentifier;
	}

	public void setEmployeeIdentifier(String employeeIdentifier) {
		this.employeeIdentifier = employeeIdentifier;
	}

	public int getEndTime() {
		return this.endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public int getPaidHours() {
		return this.paidHours;
	}

	public void setPaidHours(int paidHours) {
		this.paidHours = paidHours;
	}

	public String getScheduleName() {
		return this.scheduleName;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	public int getScheduleStatus() {
		return this.scheduleStatus;
	}

	public void setScheduleStatus(int scheduleStatus) {
		this.scheduleStatus = scheduleStatus;
	}

	public Date getShiftDate() {
		return this.shiftDate;
	}

	public void setShiftDate(Date shiftDate) {
		this.shiftDate = shiftDate;
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

	public String getSiteName() {
		return this.siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public long getSiteRequirementID() {
		return this.siteRequirementID;
	}

	public void setSiteRequirementID(long siteRequirementID) {
		this.siteRequirementID = siteRequirementID;
	}

	public int getStartTime() {
		return this.startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public long getTeamId() {
		return this.teamId;
	}

	public void setTeamId(long teamId) {
		this.teamId = teamId;
	}

	public String getTeamName() {
		return this.teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}
	
	public static class ProductionShiftSummaryPK implements Serializable{
		private long employeeID;
		private String scheduleName;
		private long siteRequirementID;
		private long shiftID;
		
		public ProductionShiftSummaryPK(){
			
		}
		
		public ProductionShiftSummaryPK(long employeeID, String scheduleName, long siteRequirementID, long shiftID) {
			this.employeeID = employeeID;
			this.scheduleName = scheduleName;
			this.siteRequirementID = siteRequirementID;
			this.shiftID = shiftID;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (employeeID ^ (employeeID >>> 32));
			result = prime * result + ((scheduleName == null) ? 0 : scheduleName.hashCode());
			result = prime * result + (int) (shiftID ^ (shiftID >>> 32));
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
			ProductionShiftSummaryPK other = (ProductionShiftSummaryPK) obj;
			if (employeeID != other.employeeID)
				return false;
			if (scheduleName == null) {
				if (other.scheduleName != null)
					return false;
			} else if (!scheduleName.equals(other.scheduleName))
				return false;
			if (shiftID != other.shiftID)
				return false;
			if (siteRequirementID != other.siteRequirementID)
				return false;
			return true;
		}
		
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProductionShiftSummary [employeeID=");
		builder.append(employeeID);
		builder.append(", employeeIdentifier=");
		builder.append(employeeIdentifier);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", paidHours=");
		builder.append(paidHours);
		builder.append(", scheduleName=");
		builder.append(scheduleName);
		builder.append(", scheduleStatus=");
		builder.append(scheduleStatus);
		builder.append(", shiftDate=");
		builder.append(shiftDate);
		builder.append(", shiftID=");
		builder.append(shiftID);
		builder.append(", shiftType=");
		builder.append(shiftType);
		builder.append(", siteName=");
		builder.append(siteName);
		builder.append(", siteRequirementID=");
		builder.append(siteRequirementID);
		builder.append(", startTime=");
		builder.append(startTime);
		builder.append(", teamIds=");
		builder.append(teamId);
		builder.append(", teamName=");
		builder.append(teamName);
		builder.append("]");
		return builder.toString();
	}

}