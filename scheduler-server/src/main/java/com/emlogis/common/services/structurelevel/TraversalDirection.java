package com.emlogis.common.services.structurelevel;

public enum TraversalDirection {
	
	OUT("OUT"),
	IN("IN");
	
	private String value;
	
	private TraversalDirection(String value) {
		this.value = value;
	}

	private void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}
