package com.emlogis.model.schedule;

public enum ScheduleType {
	
	ShiftPatternBased("ShiftPatternBased"),
	ShiftStructureBased("ShiftStructureBased");
	
	private String value;
	
	private ScheduleType(String value) {
		this.value = value;
	}

	private void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}
