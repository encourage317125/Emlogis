package com.emlogis.model.employee.dto;

import com.emlogis.model.structurelevel.dto.TeamDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeTeamViewDto extends EmployeeTeamDto {

	// TODO Add support for nested DTOs to BaseFacade.toDto(). For now, working
	// around the problem by extending EmployeeTeamDto with nested DTOs for
	// Employee and Team, so that all attributes can be populated by utility
	// methods.

	// Employee DTO
	private EmployeeSummaryDto employeeSummaryDto;

	// TeamDto
    private TeamDto teamDto;

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
	 * @return the teamDto
	 */
	public TeamDto getTeamDto() {
		return teamDto;
	}

	/**
	 * @param teamDto the teamDto to set
	 */
	public void setTeamDto(TeamDto teamDto) {
		this.teamDto = teamDto;
	}



}
