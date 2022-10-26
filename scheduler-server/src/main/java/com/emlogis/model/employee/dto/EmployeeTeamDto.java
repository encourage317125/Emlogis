package com.emlogis.model.employee.dto;

import com.emlogis.model.dto.BaseEntityDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeTeamDto extends BaseEntityDto implements Serializable {
	
	// TODO Add support for nested DTOs to BaseFacade.toDto(). For now, working
	// around the problem with EmployeeTeamViewDto, which extends these attributes
	// and Employee and Team, so that all attributes can be populated by utility
	// methods.

	private boolean isFloating;

	private boolean isHomeTeam;
	
	private boolean isSchedulable;

	/**
	 * @return the isFloating
	 */
	public boolean getIsFloating() {
		return isFloating;
	}

	/**
	 * @param isFloating the isFloating to set
	 */
	public void setIsFloating(boolean isFloating) {
		this.isFloating = isFloating;
	}

	/**
	 * @return the isHomeTeam
	 */
	public boolean getIsHomeTeam() {
		return isHomeTeam;
	}

	/**
	 * @param isHomeTeam the isHomeTeam to set
	 */
	public void setIsHomeTeam(boolean isHomeTeam) {
		this.isHomeTeam = isHomeTeam;
	}

	/**
	 * @return the isSchedulable
	 */
	public boolean getIsSchedulable() {
		return isSchedulable;
	}

	/**
	 * @param isSchedulable the isSchedulable to set
	 */
	public void setIsSchedulable(boolean isSchedulable) {
		this.isSchedulable = isSchedulable;
	}

}
