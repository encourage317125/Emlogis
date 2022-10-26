package com.emlogis.model.employee.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import com.emlogis.model.contract.dto.OvertimeDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeWithOvertimeDto extends EmployeeDto implements Serializable {
	
	private OvertimeDto overtimeDto;

	public OvertimeDto getOvertimeDto() {
		return overtimeDto;
	}

	public void setOvertimeDto(OvertimeDto overtimeDto) {
		this.overtimeDto = overtimeDto;
	}

}
