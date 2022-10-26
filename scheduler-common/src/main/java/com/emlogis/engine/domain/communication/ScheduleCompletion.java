package com.emlogis.engine.domain.communication;

public enum ScheduleCompletion {
	
	OK("OK"),
	Error("Error"),
	Aborted("Aborted");
	
	private String value;
	
	private ScheduleCompletion(String value) {
		this.value = value;
	}

	private void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}
