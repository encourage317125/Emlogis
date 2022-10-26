package com.emlogis.model.employee.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeReportDto {

	// attributes
	private String id;
	private	String firstName;
	private	String lastName;
	private double totalHours;
	private double[] availableHours;
    private List skills;

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

	public double getTotalHours() {
		return totalHours;
	}

	public void setTotalHours(double totalHours) {
		this.totalHours = totalHours;
	}

    public List getSkills() {
        return skills;
    }

    public void setSkills(List skills) {
        this.skills = skills;
    }

    public double[] getAvailableHours() {
        return availableHours;
    }

    public void setAvailableHours(double[] availableHours) {
        this.availableHours = availableHours;
    }
}