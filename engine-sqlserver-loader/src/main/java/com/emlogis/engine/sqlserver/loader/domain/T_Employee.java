package com.emlogis.engine.sqlserver.loader.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * The persistent class for the T_Employee database table.
 * 
 */
@Entity
@Table(name="T_Employee")
public class T_Employee implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="EmployeeID", unique=true, nullable=false)
	private long employeeID;

	@Column(name="AvailabilityEnabled")
	private boolean availabilityEnabled;

	@Column(name="BeginDoubleTimeDay", precision=5, scale=1)
	private BigDecimal beginDoubleTimeDay;

	@Column(name="BeginDoubleTimeTwoWeek", precision=5, scale=1)
	private BigDecimal beginDoubleTimeTwoWeek;

	@Column(name="BeginDoubleTimeWeek", precision=5, scale=1)
	private BigDecimal beginDoubleTimeWeek;

	@Column(name="BeginOvertimeDay", precision=5, scale=1)
	private BigDecimal beginOvertimeDay;

	@Column(name="BeginOvertimeTwoWeek", precision=5, scale=1)
	private BigDecimal beginOvertimeTwoWeek;

	@Column(name="BeginOvertimeWeek", precision=5, scale=1)
	private BigDecimal beginOvertimeWeek;

	@Column(name="EndDate", nullable=false)
	private Date endDate;

	@Column(name="FirstName", nullable=false, length=50)
	private String firstName;

	@Column(name="Gender", length=6)
	private String gender;

	@Column(name="HireDate")
	private Date hireDate;

	@Column(name="HourlyRate", nullable=false)
	private int hourlyRate;

	@Column(name="IsSchedulable", nullable=false)
	private boolean isSchedulable;

	@Column(name="LastName", nullable=false, length=50)
	private String lastName;

	@Column(name="MaxConsecutiveDays", nullable=false)
	private int maxConsecutiveDays;

	@Column(name="MaxDaysWeek", nullable=false)
	private int maxDaysWeek;

	@Column(name="MaxHoursDay", nullable=false)
	private int maxHoursDay;

	@Column(name="MaxHoursWeek", nullable=false)
	private int maxHoursWeek;

	@Column(name="MiddleName", nullable=false, length=50)
	private String middleName;

	@Column(name="MinHoursDay", nullable=false)
	private int minHoursDay;

	@Column(name="MinHoursTwoWeek")
	private Integer minHoursTwoWeek;

	@Column(name="MinHoursWeek", nullable=false)
	private int minHoursWeek;

	@Column(name="MinHoursWeekPrimarySkill", nullable=false)
	private int minHoursWeekPrimarySkill;

	@Column(name="MinHoursWeekTeam")
	private Integer minHoursWeekTeam;

	@Column(name="MinHoursWeekTeamID")
	private Integer minHoursWeekTeamID;

	@Column(name="MinHoursWeekTimeBlock")
	private Integer minHoursWeekTimeBlock;

	@Column(name="MinHoursWeekTimeBlockEndTIme")
	private Integer minHoursWeekTimeBlockEndTIme;

	@Column(name="MinHoursWeekTimeBlockStartTime")
	private Integer minHoursWeekTimeBlockStartTime;

	@Column(name="Overtime_Status")
	private String overtime_Status;

	@Column(name="OvertimeSetting", nullable=false)
	private int overtimeSetting;

	@Column(name="StartDate", nullable=false)
	private Date startDate;
	
	@Column(name="HomeTeamID", nullable = false)
	private long homeTeamId;

	public T_Employee() {
    }

	public long getEmployeeID() {
		return this.employeeID;
	}

	public void setEmployeeID(long employeeID) {
		this.employeeID = employeeID;
	}

	public boolean getAvailabilityEnabled() {
		return this.availabilityEnabled;
	}

	public void setAvailabilityEnabled(boolean availabilityEnabled) {
		this.availabilityEnabled = availabilityEnabled;
	}

	public BigDecimal getBeginDoubleTimeDay() {
		return this.beginDoubleTimeDay;
	}

	public void setBeginDoubleTimeDay(BigDecimal beginDoubleTimeDay) {
		this.beginDoubleTimeDay = beginDoubleTimeDay;
	}

	public BigDecimal getBeginDoubleTimeTwoWeek() {
		return this.beginDoubleTimeTwoWeek;
	}

	public void setBeginDoubleTimeTwoWeek(BigDecimal beginDoubleTimeTwoWeek) {
		this.beginDoubleTimeTwoWeek = beginDoubleTimeTwoWeek;
	}

	public BigDecimal getBeginDoubleTimeWeek() {
		return this.beginDoubleTimeWeek;
	}

	public void setBeginDoubleTimeWeek(BigDecimal beginDoubleTimeWeek) {
		this.beginDoubleTimeWeek = beginDoubleTimeWeek;
	}

	public BigDecimal getBeginOvertimeDay() {
		return this.beginOvertimeDay;
	}

	public void setBeginOvertimeDay(BigDecimal beginOvertimeDay) {
		this.beginOvertimeDay = beginOvertimeDay;
	}

	public BigDecimal getBeginOvertimeTwoWeek() {
		return this.beginOvertimeTwoWeek;
	}

	public void setBeginOvertimeTwoWeek(BigDecimal beginOvertimeTwoWeek) {
		this.beginOvertimeTwoWeek = beginOvertimeTwoWeek;
	}

	public BigDecimal getBeginOvertimeWeek() {
		return this.beginOvertimeWeek;
	}

	public void setBeginOvertimeWeek(BigDecimal beginOvertimeWeek) {
		this.beginOvertimeWeek = beginOvertimeWeek;
	}

	public Date getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getGender() {
		return this.gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Date getHireDate() {
		return this.hireDate;
	}

	public void setHireDate(Date hireDate) {
		this.hireDate = hireDate;
	}

	public int getHourlyRate() {
		return this.hourlyRate;
	}

	public void setHourlyRate(int hourlyRate) {
		this.hourlyRate = hourlyRate;
	}

	public boolean getIsSchedulable() {
		return this.isSchedulable;
	}

	public void setIsSchedulable(boolean isSchedulable) {
		this.isSchedulable = isSchedulable;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getMaxConsecutiveDays() {
		return this.maxConsecutiveDays;
	}

	public void setMaxConsecutiveDays(int maxConsecutiveDays) {
		this.maxConsecutiveDays = maxConsecutiveDays;
	}

	public int getMaxDaysWeek() {
		return this.maxDaysWeek;
	}

	public void setMaxDaysWeek(int maxDaysWeek) {
		this.maxDaysWeek = maxDaysWeek;
	}

	public int getMaxHoursDay() {
		return this.maxHoursDay;
	}

	public void setMaxHoursDay(int maxHoursDay) {
		this.maxHoursDay = maxHoursDay;
	}

	public int getMaxHoursWeek() {
		return this.maxHoursWeek;
	}

	public void setMaxHoursWeek(int maxHoursWeek) {
		this.maxHoursWeek = maxHoursWeek;
	}

	public String getMiddleName() {
		return this.middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public int getMinHoursDay() {
		return this.minHoursDay;
	}

	public void setMinHoursDay(int minHoursDay) {
		this.minHoursDay = minHoursDay;
	}

	public Integer getMinHoursTwoWeek() {
		return this.minHoursTwoWeek;
	}

	public void setMinHoursTwoWeek(Integer minHoursTwoWeek) {
		this.minHoursTwoWeek = minHoursTwoWeek;
	}

	public int getMinHoursWeek() {
		return this.minHoursWeek;
	}

	public void setMinHoursWeek(int minHoursWeek) {
		this.minHoursWeek = minHoursWeek;
	}

	public int getMinHoursWeekPrimarySkill() {
		return this.minHoursWeekPrimarySkill;
	}

	public void setMinHoursWeekPrimarySkill(int minHoursWeekPrimarySkill) {
		this.minHoursWeekPrimarySkill = minHoursWeekPrimarySkill;
	}

	public Integer getMinHoursWeekTeam() {
		return this.minHoursWeekTeam;
	}

	public void setMinHoursWeekTeam(Integer minHoursWeekTeam) {
		this.minHoursWeekTeam = minHoursWeekTeam;
	}

	public Integer getMinHoursWeekTeamID() {
		return this.minHoursWeekTeamID;
	}

	public void setMinHoursWeekTeamID(Integer minHoursWeekTeamID) {
		this.minHoursWeekTeamID = minHoursWeekTeamID;
	}

	public Integer getMinHoursWeekTimeBlock() {
		return this.minHoursWeekTimeBlock;
	}

	public void setMinHoursWeekTimeBlock(Integer minHoursWeekTimeBlock) {
		this.minHoursWeekTimeBlock = minHoursWeekTimeBlock;
	}

	public Integer getMinHoursWeekTimeBlockEndTIme() {
		return this.minHoursWeekTimeBlockEndTIme;
	}

	public void setMinHoursWeekTimeBlockEndTIme(Integer minHoursWeekTimeBlockEndTIme) {
		this.minHoursWeekTimeBlockEndTIme = minHoursWeekTimeBlockEndTIme;
	}

	public Integer getMinHoursWeekTimeBlockStartTime() {
		return this.minHoursWeekTimeBlockStartTime;
	}

	public void setMinHoursWeekTimeBlockStartTime(Integer minHoursWeekTimeBlockStartTime) {
		this.minHoursWeekTimeBlockStartTime = minHoursWeekTimeBlockStartTime;
	}

	public String getOvertime_Status() {
		return this.overtime_Status;
	}

	public void setOvertime_Status(String overtime_Status) {
		this.overtime_Status = overtime_Status;
	}

	public int getOvertimeSetting() {
		return this.overtimeSetting;
	}

	public void setOvertimeSetting(int overtimeSetting) {
		this.overtimeSetting = overtimeSetting;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public long getHomeTeamId() {
		return homeTeamId;
	}

	public void setHomeTeamId(long homeTeamId) {
		this.homeTeamId = homeTeamId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("T_Employee [employeeID=");
		builder.append(employeeID);
		builder.append(", firstName=");
		builder.append(firstName);
		builder.append(", lastName=");
		builder.append(lastName);
		builder.append("]");
		return builder.toString();
	}
}