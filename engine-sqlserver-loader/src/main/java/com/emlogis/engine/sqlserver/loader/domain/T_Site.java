package com.emlogis.engine.sqlserver.loader.domain;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.math.BigDecimal;


/**
 * The persistent class for the T_Site database table.
 * 
 */
@Entity
@NamedQuery(name="T_Site.findAll", query="SELECT t FROM T_Site t")
public class T_Site implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="SiteID")
	private long siteID;

	@Column(name="BackToBack")
	private long backToBack;

	@Column(name="BeginOverTimeDay")
	private BigDecimal beginOverTimeDay;

	@Column(name="BeginOverTimeTwoWeek")
	private BigDecimal beginOverTimeTwoWeek;

	@Column(name="BeginOverTimeWeek")
	private BigDecimal beginOverTimeWeek;

	@Column(name="Description")
	private String description;

	@Column(name="EmployeeConsecutiveDays")
	private long employeeConsecutiveDays;

	@Column(name="EnableNotifications")
	private boolean enableNotifications;

	@Column(name="EndTime")
	private int endTime;

	@Column(name="FirstDayOfWeek")
	private int firstDayOfWeek;

	@Column(name="HoursOffBetweenDays")
	private long hoursOffBetweenDays;

	@Column(name="Name")
	private String name;

	@Column(name="OvertimeStartDate")
	private Timestamp overtimeStartDate;

	@Column(name="ShiftDuration")
	private int shiftDuration;

	@Column(name="StartTime")
	private int startTime;

	@Column(name="TimeIncrement")
	private int timeIncrement;

	public T_Site() {
	}

	public long getSiteID() {
		return this.siteID;
	}

	public void setSiteID(long siteID) {
		this.siteID = siteID;
	}

	public long getBackToBack() {
		return this.backToBack;
	}

	public void setBackToBack(long backToBack) {
		this.backToBack = backToBack;
	}

	public BigDecimal getBeginOverTimeDay() {
		return this.beginOverTimeDay;
	}

	public void setBeginOverTimeDay(BigDecimal beginOverTimeDay) {
		this.beginOverTimeDay = beginOverTimeDay;
	}

	public BigDecimal getBeginOverTimeTwoWeek() {
		return this.beginOverTimeTwoWeek;
	}

	public void setBeginOverTimeTwoWeek(BigDecimal beginOverTimeTwoWeek) {
		this.beginOverTimeTwoWeek = beginOverTimeTwoWeek;
	}

	public BigDecimal getBeginOverTimeWeek() {
		return this.beginOverTimeWeek;
	}

	public void setBeginOverTimeWeek(BigDecimal beginOverTimeWeek) {
		this.beginOverTimeWeek = beginOverTimeWeek;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getEmployeeConsecutiveDays() {
		return this.employeeConsecutiveDays;
	}

	public void setEmployeeConsecutiveDays(long employeeConsecutiveDays) {
		this.employeeConsecutiveDays = employeeConsecutiveDays;
	}

	public boolean getEnableNotifications() {
		return this.enableNotifications;
	}

	public void setEnableNotifications(boolean enableNotifications) {
		this.enableNotifications = enableNotifications;
	}

	public int getEndTime() {
		return this.endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public int getFirstDayOfWeek() {
		return this.firstDayOfWeek;
	}

	public void setFirstDayOfWeek(int firstDayOfWeek) {
		this.firstDayOfWeek = firstDayOfWeek;
	}

	public long getHoursOffBetweenDays() {
		return this.hoursOffBetweenDays;
	}

	public void setHoursOffBetweenDays(long hoursOffBetweenDays) {
		this.hoursOffBetweenDays = hoursOffBetweenDays;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Timestamp getOvertimeStartDate() {
		return this.overtimeStartDate;
	}

	public void setOvertimeStartDate(Timestamp overtimeStartDate) {
		this.overtimeStartDate = overtimeStartDate;
	}

	public int getShiftDuration() {
		return this.shiftDuration;
	}

	public void setShiftDuration(int shiftDuration) {
		this.shiftDuration = shiftDuration;
	}

	public int getStartTime() {
		return this.startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getTimeIncrement() {
		return this.timeIncrement;
	}

	public void setTimeIncrement(int timeIncrement) {
		this.timeIncrement = timeIncrement;
	}

}