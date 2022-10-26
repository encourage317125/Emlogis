package com.emlogis.engine.domain.communication;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ShiftSwapEligibilityResultDto extends ScheduleResultDto {
	
	private Map<String, Collection<ShiftQualificationDto>> qualifyingShifts = new HashMap<>();

	public Map<String, Collection<ShiftQualificationDto>> getQualifyingShifts() {
		return qualifyingShifts;
	}

	public void setQualifyingShifts(
			Map<String, Collection<ShiftQualificationDto>> qualifyingShifts) {
		this.qualifyingShifts = qualifyingShifts;
	}

	
}
