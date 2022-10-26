package com.emlogis.model.tenant;


import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(name = "Account")
public class Account extends BaseEntity implements Serializable {

    @Column(nullable = false)
	private	String name;
	    
	private	String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "Role_Account",
            joinColumns = {@JoinColumn(name = "account_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "account_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "role_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "role_id", referencedColumnName = "id")})
	private Set<Role> roles = new HashSet<>();

	public Account() {}

	public Account(PrimaryKey primaryKey) {
		super(primaryKey);
		setName(primaryKey.getId());		// init name with Id by default
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }
    
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        roles.add(role);
    }
 
    public void removeAllRoles() {
        roles.clear();
    }
    
}
