package com.emlogis.engine.domain.communication.constraints;

import com.emlogis.engine.domain.timeoff.dto.CITimeOffDto;

public class CITimeOffShiftConstraintDto extends ShiftConstraintDto {
	
	private CITimeOffDto timeOff;

	/**
	 * @return the timeOff
	 */
	public CITimeOffDto getTimeOff() {
		return timeOff;
	}

	/**
	 * @param timeOff the timeOff to set
	 */
	public void setTimeOff(CITimeOffDto timeOff) {
		this.timeOff = timeOff;
	}

	@Override
	public String toString() {
		return "CITimeOffShiftConstraintDto [timeOff=" + timeOff + ", getConstraintName()=" + getConstraintName()
				+ ", getWeight()=" + getWeight() + ", getInvolvedShifts()=" + getInvolvedShifts() + "]";
	}
	
}
