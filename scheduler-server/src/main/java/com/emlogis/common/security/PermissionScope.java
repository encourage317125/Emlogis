package com.emlogis.common.security;

public enum PermissionScope {
	
	Customer("Customer"),
	ServiceProvider("ServiceProvider"),
	All("All");
	
	private String value;
	
	private PermissionScope(String value) {
		this.value = value;
	}

	private void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}
