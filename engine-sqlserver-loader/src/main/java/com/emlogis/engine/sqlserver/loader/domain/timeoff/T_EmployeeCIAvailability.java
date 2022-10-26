package com.emlogis.engine.sqlserver.loader.domain.timeoff;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


/**
 * The persistent class for the T_EmployeeCIAvailability database table.
 * 
 */
@Entity
@Table(name="T_EmployeeCIAvailability")
public class T_EmployeeCIAvailability implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="EmployeeCIAvailabilityID", unique=true, nullable=false)
	private int employeeCIAvailabilityID;

	@Column(name="AvailabilityStatus", nullable=false)
	private int availabilityStatus;

	@Column(name="EndTime", nullable=false)
	private int endTime;

	@Column(name = "EmployeeID")
	private int employeeID;

	@Column(name="StartTime", nullable=false)
	private int startTime;


	@Column(name="WeekdayNumber", nullable=false)
	private int weekdayNumber;

    public T_EmployeeCIAvailability() {
    }

	public int getEmployeeCIAvailabilityID() {
		return this.employeeCIAvailabilityID;
	}

	public void setEmployeeCIAvailabilityID(int employeeCIAvailabilityID) {
		this.employeeCIAvailabilityID = employeeCIAvailabilityID;
	}

	public int getAvailabilityStatus() {
		return this.availabilityStatus;
	}

	public void setAvailabilityStatus(int availabilityStatus) {
		this.availabilityStatus = availabilityStatus;
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
		builder.append("T_EmployeeCIAvailability [employeeCIAvailabilityID=");
		builder.append(employeeCIAvailabilityID);
		builder.append(", availabilityStatus=");
		builder.append(availabilityStatus);
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