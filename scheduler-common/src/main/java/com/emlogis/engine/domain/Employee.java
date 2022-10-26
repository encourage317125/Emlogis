package com.emlogis.engine.domain;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Employee {

	private String employeeId;
    private String firstName;
    private String lastName;
    
    // The start/stop date for each employee,
    // To be used as a built in hard constraint 
    private DateTime startDate;
    private DateTime stopDate;
    private int seniority;
    
    // Used to calculate cost of schedule
    private int hourlyRate;
    
    // Used for strength comparator
    private int numberOfDaysOffInPlanningPeriod;
    
    private List<String> teamIds;
    private List<String> skillIds;
    
    private boolean isScheduleable = true;

    public Employee(){
    	teamIds = new ArrayList<>();
    	skillIds = new ArrayList<>();
    }
    
    public boolean isEmployeeActive(DateTime shiftStartTime, DateTime shiftEndTime){
    	return (startDate == null || shiftStartTime.isAfter(startDate) || shiftStartTime.isEqual(startDate))
    				&& (stopDate == null || shiftEndTime.isBefore(stopDate) || shiftEndTime.isEqual(shiftEndTime));
    }
    
    @JsonIgnore
    public String getLabel() {
        return "Employee " + firstName + " " + lastName;
    }
    
    @JsonIgnore
    public String getFullName(){
    	return firstName + " " + lastName;
    }

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public DateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(DateTime startDate) {
		this.startDate = startDate;
	}

	public DateTime getStopDate() {
		return stopDate;
	}

	public void setStopDate(DateTime stopDate) {
		this.stopDate = stopDate;
	}

//	public List<TimeOff> getTimeOff() {
//		return timeOff;
//	}
//
//	public void setTimeOff(List<TimeOff> timeOff) {
//		this.timeOff = timeOff;
//	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Employee [employeeId=");
		builder.append(employeeId);
		builder.append(", name=");
		builder.append(firstName);
		builder.append(" ");
		builder.append(lastName);
		builder.append(", numOfSkills= ");
		builder.append(skillIds.size());
		builder.append(", numOfTeams= ");
		builder.append(teamIds.size());
		builder.append("]");
		return builder.toString();
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getSeniority() {
		return seniority;
	}
	public void setSeniority(int seniority) {
		this.seniority = seniority;
	}
	public boolean isScheduleable() {
		return isScheduleable;
	}
	public void setScheduleable(boolean isScheduleable) {
		this.isScheduleable = isScheduleable;
	}

	public int getNumberOfDaysOffInPlanningPeriod() {
		return numberOfDaysOffInPlanningPeriod;
	}

	public void setNumberOfDaysOffInPlanningPeriod(int numberOfDaysOffInPlanningPeriod) {
		this.numberOfDaysOffInPlanningPeriod = numberOfDaysOffInPlanningPeriod;
	}

	public List<String> getTeamIds() {
		return teamIds;
	}

	public void setTeamIds(List<String> teamIds) {
		this.teamIds = teamIds;
	}

	public List<String> getSkillIds() {
		return skillIds;
	}

	public void setSkillIds(List<String> skillIds) {
		this.skillIds = skillIds;
	}

	public int getHourlyRate() {
		return hourlyRate;
	}

	public void setHourlyRate(int hourlyRate) {
		this.hourlyRate = hourlyRate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((employeeId == null) ? 0 : employeeId.hashCode());
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
		Employee other = (Employee) obj;
		if (employeeId == null) {
			if (other.employeeId != null)
				return false;
		} else if (!employeeId.equals(other.employeeId))
			return false;
		return true;
	}

	

}
