package com.emlogis.engine.domain.communication.constraints;

import java.util.ArrayList;
import java.util.Collection;

import com.emlogis.engine.domain.DayOfWeek;

public class DaysAfterWeekendConstraintDto extends ShiftConstraintDto {
	Collection<DayOfWeek> daysOffAfter;

	public DaysAfterWeekendConstraintDto() {
		this.daysOffAfter = new ArrayList<>();
	}
	
	public DaysAfterWeekendConstraintDto(Collection<DayOfWeek> daysOffAfter) {
		this.daysOffAfter = daysOffAfter;
	}

	public Collection<DayOfWeek> getDaysOffAfterList() {
		return daysOffAfter;
	}

	public void setDaysOffAfterList(Collection<DayOfWeek> daysOffAfter) {
		this.daysOffAfter = daysOffAfter;
	}
}
