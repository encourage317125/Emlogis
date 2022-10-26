package com.emlogis.common.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountACE implements Serializable {
	
	private	String id;					// ACE id

	private	String entityClass;		// class name of entity this ACL applies to (ex: Site, Team, etc)
    
	private	String pattern;			//
    	
	private	Set<Permissions> permissions;
    
	private	String description;		// optional description

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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
	
	public void addPermission(Permissions permission) {
		this.permissions.add(permission);
	}
	
	public void removePermission(Permissions permission) {
		this.permissions.remove(permission);
	}
	
	public void removePermissions(List<Permissions> permissions) {
		for (Permissions perm : permissions) {
			this.permissions.remove(perm);
		}
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String toString() {
		
		String s = "AccountACL: " + id + " " + entityClass + ":" + pattern + " ";
		for (Permissions perm : permissions) {
			s += (perm.getValue() + ",");
		}
		if (description != null) {
			s += (" (" + description + ")");
		}
		return s;
	}


}
