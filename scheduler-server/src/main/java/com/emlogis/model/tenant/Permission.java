package com.emlogis.model.tenant;

import com.emlogis.common.security.PermissionScope;
import com.emlogis.common.security.PermissionType;
import com.emlogis.common.security.Permissions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(name = "Permission")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Permission implements Serializable {

    @Id
    private Permissions id;

    private PermissionType type;

    private PermissionScope scope;
    
    private String name;

    private String description;

    public Permission() {}

    public Permission(Permissions id) {
		super();
		this.id = id;
    	setName(id.getValue());
	}
    
    public Permission(Permissions id, PermissionType type, PermissionScope scope, String description) {
        this(id);
        this.type = type;
        this.scope = scope;
        this.description = description;
    }

	public Permissions getId() {
		return id;
	}

	public void setId(Permissions id) {
		this.id = id;
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

    public String getClName() {						// used and required by toDto conversion
		return this.getClass().getSimpleName();
	}

	public void setClName(String cName) {}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Permission that = (Permission) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
