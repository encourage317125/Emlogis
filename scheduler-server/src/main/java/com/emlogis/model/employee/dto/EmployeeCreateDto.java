package com.emlogis.model.employee.dto;


import com.emlogis.model.dto.CreateDto;
import com.emlogis.model.tenant.dto.UserAccountDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeCreateDto extends CreateDto implements Serializable {

	// public constants for validation annotations
	public static final	String EMPLOYEE_IDENTIFIER = "employeeIdentifier";
	public static final	String FIRST_NAME = "firstName";
	public static final	String LAST_NAME = "lastName";

	// required attributes
	private	String firstName;
	private	String lastName;
	private	String employeeIdentifier;
	
	// supplemental DTOs
	private EmployeeUpdateDto updateDto;
	private UserAccountDto userAccountDto;
	private EmployeeTeamCreateDto employeeTeamCreateDto;

	/**
	 * Getter for firstName
	 * @return
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Setter for firstName
	 * @param firstName
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Getter for lastName
	 * @return
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Setter for lastName
	 * @param lastName
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Getter for employeeIdentifier
	 * @return
	 */
	public String getEmployeeIdentifier() {
		return employeeIdentifier;
	}

	/**
	 * Setter for employeeIdentifier
	 * @param employeeIdentifier
	 */
	public void setEmployeeIdentifier(String employeeIdentifier) {
		this.employeeIdentifier = employeeIdentifier;
	}

	/**
	 * Getter for updateDto
	 */
	public EmployeeUpdateDto getUpdateDto() {
		return updateDto;
	}

	/**
	 * Setter for updateDto
	 * @param updateDto
	 */
	public void setUpdateDto(EmployeeUpdateDto updateDto) {
		this.updateDto = updateDto;
	}

	/**
	 * Getter for userAccountDto
	 * @return
	 */
	public UserAccountDto getUserAccountDto() {
		return userAccountDto;
	}

	/**
	 * Setter for userAccountDto
	 * @param userAccountDto
	 */
	public void setUserAccountDto(UserAccountDto userAccountDto) {
		this.userAccountDto = userAccountDto;
	}

    public EmployeeTeamCreateDto getEmployeeTeamCreateDto() {
        return employeeTeamCreateDto;
    }

    public void setEmployeeTeamCreateDto(EmployeeTeamCreateDto employeeTeamCreateDto) {
        this.employeeTeamCreateDto = employeeTeamCreateDto;
    }
}