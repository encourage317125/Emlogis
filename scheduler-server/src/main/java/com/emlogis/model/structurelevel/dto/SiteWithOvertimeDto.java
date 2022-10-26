package com.emlogis.model.structurelevel.dto;

import com.emlogis.model.contract.dto.OvertimeDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class SiteWithOvertimeDto extends SiteDto implements Serializable {
	
	private OvertimeDto overtimeDto;

	public OvertimeDto getOvertimeDto() {
		return overtimeDto;
	}

	public void setOvertimeDto(OvertimeDto overtimeDto) {
		this.overtimeDto = overtimeDto;
	}
	
}

