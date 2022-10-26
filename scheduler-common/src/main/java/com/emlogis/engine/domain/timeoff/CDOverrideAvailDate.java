package com.emlogis.engine.domain.timeoff;

import org.joda.time.LocalDate;

import java.io.Serializable;

public class CDOverrideAvailDate implements Serializable {
	private LocalDate overrideDate;


	public CDOverrideAvailDate() {
	}

	public CDOverrideAvailDate(LocalDate overrideDate) {
		this.overrideDate = overrideDate;
	}

	public LocalDate getOverrideDate() {
		return overrideDate;
	}

	public void setOverrideDate(LocalDate overrideDate) {
		this.overrideDate = overrideDate;
	}
	
	
}
