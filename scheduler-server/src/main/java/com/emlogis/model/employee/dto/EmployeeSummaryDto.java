package com.emlogis.model.employee.dto;

import com.emlogis.model.structurelevel.dto.AOMEntityDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeSummaryDto extends AOMEntityDto implements Serializable {
	
	// attributes
	private	String firstName;
	private	String middleName;
	private	String lastName;
	private	String employeeIdentifier;
    
    

    /**
     * getter for firstName
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
	 * Getter for middleName
	 * @return
	 */
	public String getMiddleName() {
		return middleName;
	}

	
	
	/**
	 * Setter for milddleName
	 * @param middleName
	 */
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
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
	
}