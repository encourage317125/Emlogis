package com.emlogis.engine.domain.dto;

import com.emlogis.engine.domain.timeoff.dto.TimeWindowDto;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

public class EmployeeDto implements Serializable {

    private String id;
    private String firstName;
    private String lastName;
    private DateTime startDate;
    private DateTime stopDate;
    private int seniority;
    private int hourlyRate; // Dollar value multiplied by 100
    private boolean scheduleable = true;
    private List<TimeWindowDto> employeeTimeOffDtos;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getSeniority() {
        return seniority;
    }

    public void setSeniority(int seniority) {
        this.seniority = seniority;
    }

    public boolean isScheduleable() {
        return scheduleable;
    }

    public void setScheduleable(boolean scheduleable) {
        this.scheduleable = scheduleable;
    }

    public int getHourlyRate() {
		return hourlyRate;
	}

	public void setHourlyRate(int hourlyRate) {
		this.hourlyRate = hourlyRate;
	}

	public List<TimeWindowDto> getEmployeeTimeOffDtos() {
        return employeeTimeOffDtos;
    }

    public void setEmployeeTimeOffDtos(List<TimeWindowDto> employeeTimeOffDtos) {
        this.employeeTimeOffDtos = employeeTimeOffDtos;
    }
}
