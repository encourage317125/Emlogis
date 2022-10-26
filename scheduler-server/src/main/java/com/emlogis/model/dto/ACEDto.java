package com.emlogis.model.dto;

import com.emlogis.common.security.Permissions;

import java.util.Set;

public class ACEDto extends BaseEntityDto {

	private	String entityClass;
    
	private	String pattern;
    	
	private Set<Permissions> permissions;
    
	private	String description;

	public String getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(String entityClass) {
		this.entityClass = entityClass;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public Set<Permissions> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permissions> permissions) {
		this.permissions = permissions;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}	

}
