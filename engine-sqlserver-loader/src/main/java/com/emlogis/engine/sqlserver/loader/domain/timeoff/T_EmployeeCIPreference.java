package com.emlogis.engine.sqlserver.loader.domain.timeoff;

import javax.persistence.*;
import java.io.Serializable;


/**
 * The persistent class for the T_EmployeeCIPreference database table.
 * 
 */
@Entity
@Table(name="T_EmployeeCIPreference")
public class T_EmployeeCIPreference implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="EmployeeCIPreferenceID", unique=true, nullable=false)
	private int employeeCIPreferenceID;

	@Column(name="PreferenceStatus", nullable=false)
	private int preferenceStatus;

	@Column(name="EndTime", nullable=false)
	private int endTime;

	@Column(name = "EmployeeID")
	private int employeeID;

	@Column(name="StartTime", nullable=false)
	private int startTime;


	@Column(name="WeekdayNumber", nullable=false)
	private int weekdayNumber;

    public T_EmployeeCIPreference() {
    }

	public int getEmployeeCIPreferenceID() {
		return this.employeeCIPreferenceID;
	}

	public void setEmployeeCIPreferenceID(int employeeCIPreferenceID) {
		this.employeeCIPreferenceID = employeeCIPreferenceID;
	}

	public int getPreferenceStatus() {
		return this.preferenceStatus;
	}

	public void setPreferenceStatus(int preferenceStatus) {
		this.preferenceStatus = preferenceStatus;
	}
	

	public int getEndTime() {
		return this.endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public int getStartTime() {
		return this.startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getWeekdayNumber() {
		return this.weekdayNumber;
	}

	public void setWeekdayNumber(int weekdayNumber) {
		this.weekdayNumber = weekdayNumber;
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
		builder.append("T_EmployeeCIPreference [employeeCIPreferenceID=");
		builder.append(employeeCIPreferenceID);
		builder.append(", preferenceStatus=");
		builder.append(preferenceStatus);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", employeeID=");
		builder.append(employeeID);
		builder.append(", startTime=");
		builder.append(startTime);
		builder.append(", weekdayNumber=");
		builder.append(weekdayNumber);
		builder.append("]");
		return builder.toString();
	}
	
}