package com.emlogis.engine.domain.communication.constraints;

import org.joda.time.LocalDate;

public class CoupledWeekendConstraintDto extends ShiftConstraintDto{
	
	private LocalDate weekendStartDate;
	
	public CoupledWeekendConstraintDto(){
	}
	
	public CoupledWeekendConstraintDto(LocalDate weekendStartDate){
		this.weekendStartDate = weekendStartDate;
	}

	public LocalDate getWeekendStartDate() {
		return weekendStartDate;
	}

	public void setWeekendStartDate(LocalDate weekendStartDate) {
		this.weekendStartDate = weekendStartDate;
	}
	
	

}
