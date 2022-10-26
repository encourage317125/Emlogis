package com.emlogis.engine.domain.communication.constraints;

import com.emlogis.engine.domain.timeoff.dto.CDTimeOffDto;

public class CDTimeOffShiftConstraintDto extends ShiftConstraintDto {
	
	private CDTimeOffDto timeOff;

	/**
	 * @return the timeOff
	 */
	public CDTimeOffDto getTimeOff() {
		return timeOff;
	}

	/**
	 * @param timeOff the timeOff to set
	 */
	public void setTimeOff(CDTimeOffDto timeOff) {
		this.timeOff = timeOff;
	}
	
}
