package com.emlogis.model.employee.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)

public class CDAvailabilityTimeFrameDto extends AvailabilityTimeFrameDto  implements Serializable {
	
	private long startDateTime;
	
	private boolean isPTO;
	
	private AbsenceTypeDto absenceTypeDto;

	public Long getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(Long startDateTime) {
		this.startDateTime = startDateTime;
	}
	/**
	 * @return the isPTO
	 */
	public boolean getIsPTO() {
		return isPTO;
	}
	/**
	 * @param isPTO the isPTO to set
	 */
	public void setIsPTO(boolean isPTO) {
		this.isPTO = isPTO;
	}

	public AbsenceTypeDto getAbsenceTypeDto() {
		return absenceTypeDto;
	}

	public void setAbsenceTypeDto(AbsenceTypeDto absenceTypeDto) {
		this.absenceTypeDto = absenceTypeDto;
	}	
}
