package com.emlogis.common;

public enum ImportType {
	EMPLOYEE__V1("Employee");
	
	private String entityName =  "";
	
	ImportType(String entityName) {
		this.entityName = entityName;
	}
	
	public String getEntityName () {
		return entityName;
	}

}
