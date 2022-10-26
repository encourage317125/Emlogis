package com.emlogis.model.employee.dto;

import com.emlogis.model.dto.BaseEntityDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeSkillDto extends BaseEntityDto implements Serializable{

	// TODO Add support for nested DTOs to BaseFacade.toDto(). For now, working
	// around the problem with EmployeeTeamSkillDto, which extends these attributes
	// and Employee and Skill, so that all attributes can be populated by utility
	// methods.

	private boolean isPrimarySkill;

	private int skillScore;  // null or 1..5 (TODO Range validation in Facade) 

	/**
	 * Getter for isPrimarySkill
	 * @return
	 */
	public boolean getIsPrimarySkill() {
		return isPrimarySkill;
	}

	/**
	 * Setter for isPrimarySkill
	 * @param isPrimarySkill
	 */
	public void setIsPrimarySkill(boolean isPrimarySkill) {
		this.isPrimarySkill = isPrimarySkill;
	}

	/**
	 * Getter for skillScore
	 * @return
	 */
	public int getSkillScore() {
		return skillScore;
	}

	/**
	 * Setter for skillScore
	 * @param skillScore
	 */
	public void setSkillScore(int skillScore) {
		this.skillScore = skillScore;
	}

}
