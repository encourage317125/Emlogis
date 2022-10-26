package com.emlogis.model.contract.dto;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.contract.patterns.WeekdayRotationPattern.RotationPatternType;

import java.io.Serializable;

public class WeekdayRotationPatternCLDTO extends PatternCLDTO implements Serializable {
	
	private DayOfWeek dayOfWeek;
	private int numberOfDays;
	private int outOfTotalDays;
	private RotationPatternType rotationType;
	
	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}
	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	public int getNumberOfDays() {
		return numberOfDays;
	}
	public void setNumberOfDays(int numberOfDays) {
		this.numberOfDays = numberOfDays;
	}
	public int getOutOfTotalDays() {
		return outOfTotalDays;
	}
	public void setOutOfTotalDays(int outOfTotalDays) {
		this.outOfTotalDays = outOfTotalDays;
	}
	public RotationPatternType getRotationType() {
		return rotationType;
	}
	public void setRotationType(RotationPatternType rotationType) {
		this.rotationType = rotationType;
	}	
}
