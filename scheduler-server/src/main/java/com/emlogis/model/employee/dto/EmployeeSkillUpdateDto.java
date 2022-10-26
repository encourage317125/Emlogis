package com.emlogis.model.employee.dto;

import com.emlogis.model.dto.BaseEntityDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeSkillUpdateDto extends BaseEntityDto implements Serializable{

	private Boolean isPrimarySkill;
	private Integer skillScore;  // null or 1..5 (TODO Range validation in Facade) 

	/**
	 * Getter for isPrimarySkill
	 * @return
	 */
	public Boolean getIsPrimarySkill() {
		return isPrimarySkill;
	}

	/**
	 * Setter for isPrimarySkill
	 * @param isPrimarySkill
	 */
	public void setIsPrimarySkill(Boolean isPrimarySkill) {
		this.isPrimarySkill = isPrimarySkill;
	}

	/**
	 * Getter for skillScore
	 * @return
	 */
	public Integer getSkillScore() {
		return skillScore;
	}

	/**
	 * Setter for skillScore
	 * @param skillScore
	 */
	public void setSkillScore(Integer skillScore) {
		this.skillScore = skillScore;
	}

}
