package com.emlogis.engine.sqlserver.loader.domain;

import java.io.Serializable;

import javax.persistence.*;


/**
 * The persistent class for the T_EmployeeWeekends database table.
 * 
 */
@Entity
@Table(name="T_EmployeeWeekends")
@NamedQuery(name="T_EmployeeWeekend.findAll", query="SELECT t FROM T_EmployeeWeekend t")
public class T_EmployeeWeekend implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="EmployeeWeekendsID", insertable=false, updatable=false)
	private long employeeWeekendsID;

	@Column(name="CoupleWeekends", insertable=false, updatable=false)
	private int coupleWeekends;

	@Column(name="DaysAfter", insertable=false, updatable=false)
	private int daysAfter;

	@Column(name="DaysBefore", insertable=false, updatable=false)
	private int daysBefore;

	@Column(name="EmployeeID")
	private long employeeID;

	public T_EmployeeWeekend() {
	}

	public long getEmployeeWeekendsID() {
		return this.employeeWeekendsID;
	}

	public void setEmployeeWeekendsID(long employeeWeekendsID) {
		this.employeeWeekendsID = employeeWeekendsID;
	}

	public int getCoupleWeekends() {
		return this.coupleWeekends;
	}

	public void setCoupleWeekends(int coupleWeekends) {
		this.coupleWeekends = coupleWeekends;
	}

	public int getDaysAfter() {
		return this.daysAfter;
	}

	public void setDaysAfter(int daysAfter) {
		this.daysAfter = daysAfter;
	}

	public int getDaysBefore() {
		return this.daysBefore;
	}

	public void setDaysBefore(int daysBefore) {
		this.daysBefore = daysBefore;
	}

	public long getEmployeeID() {
		return this.employeeID;
	}

	public void setEmployeeID(long employeeID) {
		this.employeeID = employeeID;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("T_EmployeeWeekend [coupleWeekends=");
		builder.append(coupleWeekends);
		builder.append(", daysAfter=");
		builder.append(daysAfter);
		builder.append(", daysBefore=");
		builder.append(daysBefore);
		builder.append(", employeeID=");
		builder.append(employeeID);
		builder.append("]");
		return builder.toString();
	}

}