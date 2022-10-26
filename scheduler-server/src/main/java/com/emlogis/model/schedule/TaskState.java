package com.emlogis.model.schedule;

public enum TaskState {
	
	Idle("Idle"),
	Starting("Starting"),
	Queued("Queued"),
	Running("Running"),
    Aborting("Aborting"),
    Complete("Complete");

	private String value;
	
	private TaskState(String value) {
		this.value = value;
	}

	private void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}
