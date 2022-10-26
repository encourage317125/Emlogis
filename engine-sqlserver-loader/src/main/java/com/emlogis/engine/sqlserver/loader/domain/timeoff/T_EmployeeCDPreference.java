package com.emlogis.engine.sqlserver.loader.domain.timeoff;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * The persistent class for the T_EmployeeCDPreference database table.
 * 
 */
@Entity
@Table(name = "T_EmployeeCDPreference")
public class T_EmployeeCDPreference implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "EmployeeCDPreferenceID", unique = true, nullable = false)
	private int employeeCDPreferenceID;
	
	@Column(name = "EmployeeID")
	private int employeeID;
	
	@Column(name = "PreferenceDate", nullable = false)
	private Date preferenceDate;

	@Column(name = "PreferenceStatus", nullable = false)
	private int preferenceStatus;

	@Column(name = "EndTime", nullable = false)
	private int endTime;

	@Column(name = "StartTime", nullable = false)
	private int startTime;
	
	public T_EmployeeCDPreference() {
	}

	public int getEmployeeCDPreferenceID() {
		return this.employeeCDPreferenceID;
	}

	public void setEmployeeCDPreferenceID(int employeeCDPreferenceID) {
		this.employeeCDPreferenceID = employeeCDPreferenceID;
	}

	public Date getPreferenceDate() {
		return this.preferenceDate;
	}

	public void setPreferenceDate(Date preferenceDate) {
		this.preferenceDate = preferenceDate;
	}

	public int getPreferenceStatus() {
		return this.preferenceStatus;
	}

	public void setPreferenceStatus(int preferenceStatus) {
		this.preferenceStatus = preferenceStatus;
	}

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getEmployeeID() {
		return employeeID;
	}

	public void setEmployeeID(int employeeID) {
		this.employeeID = employeeID;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("T_EmployeeCDPreference [employeeCDPreferenceID=");
		builder.append(employeeCDPreferenceID);
		builder.append(", employeeID=");
		builder.append(employeeID);
		builder.append(", preferenceDate=");
		builder.append(preferenceDate);
		builder.append(", preferenceStatus=");
		builder.append(preferenceStatus);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", startTime=");
		builder.append(startTime);
		builder.append("]");
		return builder.toString();
	}

}