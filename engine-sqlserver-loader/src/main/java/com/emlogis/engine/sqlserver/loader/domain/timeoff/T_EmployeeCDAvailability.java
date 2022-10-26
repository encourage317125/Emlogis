package com.emlogis.engine.sqlserver.loader.domain.timeoff;

import java.io.Serializable;

import javax.persistence.*;

import java.util.Date;

/**
 * The persistent class for the T_EmployeeCDAvailability database table.
 * 
 */
@Entity
@Table(name = "T_EmployeeCDAvailability")
public class T_EmployeeCDAvailability implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "EmployeeCDAvailabilityID", unique = true, nullable = false)
	private int employeeCDAvailabilityID;
	
	@Column(name = "EmployeeID")
	private int employeeID;
	
	@Column(name = "AvailabilityDate", nullable = false)
	private Date availabilityDate;
	
	@Column(name = "availabilityEndDate", nullable = true)
	private Date availabilityEndDate;


	@Column(name = "AvailabilityStatus", nullable = false)
	private int availabilityStatus;


	@Column(name = "EndTime", nullable = false)
	private int endTime;


	@Column(name = "StartTime", nullable = false)
	private int startTime;
	
	@Column(name = "AbsenceTypeID", nullable = true)
	private Long abscenceTypeID;

	public T_EmployeeCDAvailability() {
	}

	public int getEmployeeCDAvailabilityID() {
		return this.employeeCDAvailabilityID;
	}

	public void setEmployeeCDAvailabilityID(int employeeCDAvailabilityID) {
		this.employeeCDAvailabilityID = employeeCDAvailabilityID;
	}

	public Date getAvailabilityDate() {
		return this.availabilityDate;
	}

	public void setAvailabilityDate(Date availabilityDate) {
		this.availabilityDate = availabilityDate;
	}


	public int getAvailabilityStatus() {
		return this.availabilityStatus;
	}

	public void setAvailabilityStatus(int availabilityStatus) {
		this.availabilityStatus = availabilityStatus;
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

	public Long getAbscenceTypeID() {
		return abscenceTypeID;
	}

	public void setAbscenceTypeID(Long abscenceTypeID) {
		this.abscenceTypeID = abscenceTypeID;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("T_EmployeeCDAvailability [employeeCDAvailabilityID=");
		builder.append(employeeCDAvailabilityID);
		builder.append(", employeeID=");
		builder.append(employeeID);
		builder.append(", availabilityDate=");
		builder.append(availabilityDate);
		builder.append(", availabilityStatus=");
		builder.append(availabilityStatus);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", startTime=");
		builder.append(startTime);
		builder.append("]");
		return builder.toString();
	}

	public Date getAvailabilityEndDate() {
		return availabilityEndDate;
	}

	public void setAvailabilityEndDate(Date availabilityEndDate) {
		this.availabilityEndDate = availabilityEndDate;
	}


}