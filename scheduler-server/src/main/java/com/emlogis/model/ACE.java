package com.emlogis.model;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.tenant.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class ACE extends BaseEntity{
	
    @Column(nullable = false)
	private	String entityClass;		// class name of entity this ACE applies to (ex: Site, Team, etc)
    
    @Column(nullable = false)
	private	String pattern = "/.*";
    	
    @Column(nullable = false)
	private	String permissions;		// jsonified set of permissions
    
    @Column(nullable = true)
	private	String description;		// optional description

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime tagged;

    private transient Set<Permissions> actualPermissions;
    	
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "Role_ACE",
            joinColumns = {@JoinColumn(name = "ace_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "ace_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "role_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "role_id", referencedColumnName = "id")})
    private Set<Role> roles;
    
	protected ACE() {
		super();
	}

	protected ACE(PrimaryKey primaryKey) {
		super(primaryKey);
		setPermissions(new HashSet<Permissions>());
	}

	public ACE(PrimaryKey primaryKey, Class entityClass, String pattern, Set<Permissions> permissions,
               String description) {
		this(primaryKey);
		setEntityClass(entityClass);
		setPattern(pattern);
		setPermissions(permissions);
		setDescription(description);
	}

	public String getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class entityClass) {
		this.entityClass = entityClass.getSimpleName();
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public Set<Permissions> getPermissions() {
		if (actualPermissions != null) { 
			return actualPermissions; 
		}
        Set<String> permSet = EmlogisUtils.fromJsonString(permissions, Set.class);
		// fromJsonString() returns a collection of strings. convert to enum
		actualPermissions = new HashSet<>();
		for (String permStr : permSet) {
			actualPermissions.add(Permissions.valueOf(permStr));
		}
		return actualPermissions;
	}
	
	public void setPermissions(Set<Permissions> permissions) {
		actualPermissions = permissions;
		this.permissions = EmlogisUtils.toJsonString(permissions);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

    public void removeRole(Role role) {
    	roles.remove(role);
    }

    public void addRole(Role role) {
        tagged = null;

    	roles.add(role);
    }
 
    public void removeAllRoles() {
    	roles.clear();
    }

    public DateTime getTagged() {
        return tagged;
    }

    public void setTagged(DateTime tagged) {
        this.tagged = tagged;
    }

    public String toString() {
		String s = "ACE: " + getTenantId() + ":" + getId() + " " + entityClass + ":" + pattern + " ";
		Set<Permissions> perms = getPermissions();
		for (Permissions perm : perms) {
			s += (perm.getValue() + ",");
		}
		if (description != null) {
			s += (" (" + description + ")");
		}
		return s;
	}
	
}
