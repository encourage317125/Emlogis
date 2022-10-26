package com.emlogis.model.contract.dto;

import java.io.Serializable;

public class WeekendWorkPatternCLDTO extends PatternCLDTO implements Serializable {
	
	private String daysOffAfter;
	private String daysOffBefore;
	
	public String getDaysOffAfter() {
		return daysOffAfter;
	}
	public void setDaysOffAfter(String daysOffAfter) {
		this.daysOffAfter = daysOffAfter;
	}
	public String getDaysOffBefore() {
		return daysOffBefore;
	}
	public void setDaysOffBefore(String daysOffBefore) {
		this.daysOffBefore = daysOffBefore;
	}	
}
