package com.emlogis.model.employee.dto;

import com.emlogis.model.dto.BaseEntityDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillDto extends BaseEntityDto {

	public static final String NAME = "name";
	public static final String ABBREVIATION = "abbreviation";
	public static final String DESCRIPTION = "description";

	private String name;
	private	String abbreviation;
	private String description;
    private boolean isActive;
    private long startDate;
    private long endDate;
	
	/**
	 * Getter for name field
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter for name field
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter for description field
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Setter for description field
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Getter for abbreviation field
	 * @return
	 */
	public String getAbbreviation() {
		return abbreviation;
	}

	
	/**
	 * Setter for abbreviation field
	 * @param abbreviation
	 */
	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public long getStartDate() {
		return startDate;
	}

	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}

	public long getEndDate() {
		return endDate;
	}

	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}


}

