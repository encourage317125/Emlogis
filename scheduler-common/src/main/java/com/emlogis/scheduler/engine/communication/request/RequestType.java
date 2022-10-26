package com.emlogis.scheduler.engine.communication.request;

public enum RequestType {
	
	Assignment("Assignment"),
	Qualification("Qualification"),
	OpenShiftEligibility("OpenShiftEligibility"),
	ShiftSwapEligibility("ShiftSwapEligibility"),
	Abort("Abort"),
	Shutdown("Shutdown");
	
	private String value;
	
	private RequestType(String value) {
		this.value = value;
	}

	private void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}
