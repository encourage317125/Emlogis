package com.emlogis.engine.sqlserver.loader.domain;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the T_EmployeeCIRotation database table.
 * 
 */
@Entity
@NamedQuery(name="T_EmployeeCIRotation.findAll", query="SELECT t FROM T_EmployeeCIRotation t")
public class T_EmployeeCIRotation implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="EmployeeCIRotationID", insertable=false, updatable=false)
	private long employeeCIRotationID;

	@Column(name="EmployeeID")
	private long employeeID;

	@Column(name="RotationValue", insertable=false, updatable=false)
	private int rotationValue;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("T_EmployeeCIRotation [employeeCIRotationID=");
		builder.append(employeeCIRotationID);
		builder.append(", employeeID=");
		builder.append(employeeID);
		builder.append(", rotationValue=");
		builder.append(rotationValue);
		builder.append(", weekdayNumber=");
		builder.append(weekdayNumber);
		builder.append("]");
		return builder.toString();
	}

	@Column(name="WeekdayNumber", insertable=false, updatable=false)
	private int weekdayNumber;

	public T_EmployeeCIRotation() {
	}

	public long getEmployeeCIRotationID() {
		return this.employeeCIRotationID;
	}

	public void setEmployeeCIRotationID(long employeeCIRotationID) {
		this.employeeCIRotationID = employeeCIRotationID;
	}

	public long getEmployeeID() {
		return this.employeeID;
	}

	public void setEmployeeID(long employeeID) {
		this.employeeID = employeeID;
	}

	public int getRotationValue() {
		return this.rotationValue;
	}

	public void setRotationValue(int rotationValue) {
		this.rotationValue = rotationValue;
	}

	public int getWeekdayNumber() {
		return this.weekdayNumber;
	}

	public void setWeekdayNumber(int weekdayNumber) {
		this.weekdayNumber = weekdayNumber;
	}

}