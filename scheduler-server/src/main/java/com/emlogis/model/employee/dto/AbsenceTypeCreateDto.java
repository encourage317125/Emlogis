package com.emlogis.model.employee.dto;

import com.emlogis.model.dto.CreateDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbsenceTypeCreateDto extends CreateDto {
	
	public final static String NAME = "name";
	public final static String DESCRIPTION = "description";
	public final static String TIMETODEDUCT = "timeToDeductInMin";
	public final static String SITEID = "siteId";

	private String name;	
	private String description;	
	private int timeToDeductInMin;
    private boolean isActive = true;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getTimeToDeductInMin() {
		return timeToDeductInMin;
	}

	public void setTimeToDeductInMin(int timeToDeductInMin) {
		this.timeToDeductInMin = timeToDeductInMin;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

}
