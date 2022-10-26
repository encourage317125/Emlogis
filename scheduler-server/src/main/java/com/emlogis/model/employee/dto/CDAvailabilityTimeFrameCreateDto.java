package com.emlogis.model.employee.dto;

import java.io.Serializable;

public class CDAvailabilityTimeFrameCreateDto extends AvailabilityTimeFrameCreateDto implements Serializable {
	
	public final static String STARTDATETIME ="startDateTime";
	
	private long startDateTime;
	
	private boolean isPTO;

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
}
