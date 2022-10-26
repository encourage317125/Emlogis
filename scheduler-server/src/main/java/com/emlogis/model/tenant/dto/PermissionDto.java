package com.emlogis.model.tenant.dto;

import com.emlogis.common.security.PermissionScope;
import com.emlogis.common.security.PermissionType;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.dto.ReadDto;

public class PermissionDto  extends ReadDto {

	private Permissions id;
    private String name;
    private String description;
    private PermissionType type;
    private PermissionScope scope;

	public Permissions getId() {
		return id;
	}

	public void setId(Permissions id) {
		this.id = id;
	}

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	public PermissionType getType() {
		return type;
	}

	public void setType(PermissionType type) {
		this.type = type;
	}

	public PermissionScope getScope() {
		return scope;
	}

	public void setScope(PermissionScope scope) {
		this.scope = scope;
	}
    
}
