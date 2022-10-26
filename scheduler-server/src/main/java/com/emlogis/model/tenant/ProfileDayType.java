package com.emlogis.model.tenant;

public enum ProfileDayType {
	
	DayShiftStarts("DayShiftStarts"),
	DayShiftEnds("DayShiftEnds"),
	ShiftMajority("ShiftMajority"),
	SplitByMidnight("SplitByMidnight");

	private String value;
	
	private ProfileDayType(String value) {
		this.value = value;
	}

	private void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}
