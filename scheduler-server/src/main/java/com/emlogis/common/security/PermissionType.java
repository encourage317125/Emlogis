package com.emlogis.common.security;

public enum PermissionType {
	
	View("View"),
	Update("Update");
	
	private String value;
	
	private PermissionType(String value) {
		this.value = value;
	}

	private void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}
