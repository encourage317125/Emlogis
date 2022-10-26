package com.emlogis.engine.domain.communication;

import java.util.ArrayList;
import java.util.Collection;

public class QualificationResultDto extends ScheduleResultDto {
	
	private Collection<ShiftQualificationDto> qualifyingShifts = new ArrayList<ShiftQualificationDto>();

	/**
	 * @return the qualifyingShifts
	 */
	public Collection<ShiftQualificationDto> getQualifyingShifts() {
		return qualifyingShifts;
	}

	/**
	 * @param qualifyingShifts the qualifyingShifts to set
	 */
	public void setQualifyingShifts(Collection<ShiftQualificationDto> qualifyingShifts) {
		this.qualifyingShifts = qualifyingShifts;
	}
	
}
