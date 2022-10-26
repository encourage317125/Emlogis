package com.emlogis.model.employee.dto;

import java.io.Serializable;

public class CDAvailabilityTimeFrameUpdateDto extends AvailabilityTimeFrameUpdateDto implements Serializable {
	
	public final static String STARTDATETIME ="startDateTime";
	
	private Long startDateTime;
	
	private Boolean isPTO;
	
	/**
	 * @return the isPTO
	 */
	public Boolean getIsPTO() {
		return isPTO;
	}
	
	/**
	 * @param isPTO the isPTO to set
	 */
	public void setIsPTO(Boolean isPTO) {
		this.isPTO = isPTO;
	}

	public Long getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(Long startDateTime) {
		this.startDateTime = startDateTime;
	}	
}
