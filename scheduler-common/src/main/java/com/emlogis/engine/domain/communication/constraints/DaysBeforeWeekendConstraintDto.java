package com.emlogis.engine.domain.communication.constraints;

import java.util.ArrayList;
import java.util.Collection;

import com.emlogis.engine.domain.DayOfWeek;

public class DaysBeforeWeekendConstraintDto extends ShiftConstraintDto {
	Collection<DayOfWeek> daysOffBefore;

	public DaysBeforeWeekendConstraintDto() {
		this.daysOffBefore = new ArrayList<>();
	}
	
	public DaysBeforeWeekendConstraintDto(Collection<DayOfWeek> daysOffBefore) {
		this.daysOffBefore = daysOffBefore;
	}

	public Collection<DayOfWeek> getDaysOffAfterList() {
		return daysOffBefore;
	}

	public void setDaysOffAfterList(Collection<DayOfWeek> daysOffBefore) {
		this.daysOffBefore = daysOffBefore;
	}
}
