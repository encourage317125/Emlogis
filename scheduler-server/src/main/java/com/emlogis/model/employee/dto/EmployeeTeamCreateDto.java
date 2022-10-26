package com.emlogis.model.employee.dto;

import com.emlogis.model.dto.CreateDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeTeamCreateDto extends CreateDto {

	private boolean isFloating;
	private boolean isHomeTeam;
	private boolean isSchedulable;
	private String teamId;

	/**
	 * @return the isFloating
	 */
	public Boolean getIsFloating() {
		return isFloating;
	}

	/**
	 * @param isFloating the isFloating to set
	 */
	public void setIsFloating(Boolean isFloating) {
		this.isFloating = isFloating;
	}

	/**
	 * @return the isHomeTeam
	 */
	public Boolean getIsHomeTeam() {
		return isHomeTeam;
	}

	/**
	 * @param isHomeTeam the isHomeTeam to set
	 */
	public void setIsHomeTeam(Boolean isHomeTeam) {
		this.isHomeTeam = isHomeTeam;
	}

	/**
	 * @return the isSchedulable
	 */
	public Boolean getIsSchedulable() {
		return isSchedulable;
	}

	/**
	 * @param isSchedulable the isSchedulable to set
	 */
	public void setIsSchedulable(Boolean isSchedulable) {
		this.isSchedulable = isSchedulable;
	}

	/**
	 * @return the teamId
	 */
	public String getTeamId() {
		return teamId;
	}

	/**
	 * @param teamId the teamId to set
	 */
	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

}
