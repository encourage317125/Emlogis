package com.emlogis.model.schedule;

public enum ScheduleStatus {
	
	Simulation("Simulation"),
	Production("Production"),
	Posted("Posted");
	
	private String value;
	
	private ScheduleStatus(String value) {
		this.value = value;
	}

	private void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}
