package com.emlogis.model.employee.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeSkillViewDto extends EmployeeSkillDto implements Serializable{

	// TODO Add support for nested DTOs to BaseFacade.toDto(). For now, working
	// around the problem by extending EmployeeSkillDto with nested DTOs for
	// Employee and Skill, so that all attributes can be populated by utility
	// methods.
	
	// Employee DTO
	private EmployeeSummaryDto employeeSummaryDto;
	
	// SkillDto
	private SkillDto skillDto;

	/**
	 * @return the employeeSummaryDto
	 */
	public EmployeeSummaryDto getEmployeeSummaryDto() {
		return employeeSummaryDto;
	}

	/**
	 * @param employeeSummaryDto the employeeSummaryDto to set
	 */
	public void setEmployeeSummaryDto(EmployeeSummaryDto employeeSummaryDto) {
		this.employeeSummaryDto = employeeSummaryDto;
	}

	/**
	 * @return the skillDto
	 */
	public SkillDto getSkillDto() {
		return skillDto;
	}

	/**
	 * @param skillDto the skillDto to set
	 */
	public void setSkillDto(SkillDto skillDto) {
		this.skillDto = skillDto;
	}

}
