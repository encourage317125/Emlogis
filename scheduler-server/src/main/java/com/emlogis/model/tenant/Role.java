package com.emlogis.model.tenant;

import com.emlogis.model.ACE;
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
@Entity(name = "Role")
public class Role extends BaseEntity implements Serializable {
	
	public static final String DEFAULT_ADMINROLE_ID = "adminrole";
	public static final String DEFAULT_SCHEDULECREATORROLE_ID = "schedulecreatorrole";
	public static final String DEFAULT_SHIFTMANAGERROLE_ID = "shiftmanagerrole";
//	public static final String DEFAULT_TACTICAL_SCHEDULERROLE_ID = "tacticaladminschedulerrole";
//	public static final String DEFAULT_BASIC_SCHEDULERROLE_ID = "basicadminschedulerrole";
	public static final String DEFAULT_EMPLOYEEROLE_ID = "employeerole";
	public static final String DEFAULT_SERVICESUPPORTROLE_ID = "svcsupportrole";
	public static final String DEFAULT_SERVICEADMIN_ID = "svcadminrole";
	public static final String DEFAULT_ACCOUNTMANAGERROLE_ID = "accountmanagerrole";
	public static final String DEFAULT_ROLEMANAGERROLE_ID = "rolemanagerrole";

    private String name;

    private String label;

    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Permission> permissions = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "Role_ACE",
            joinColumns = {@JoinColumn(name = "role_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "role_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "ace_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "ace_id", referencedColumnName = "id")})
	private Set<ACE> acl = new HashSet<>();
	
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "Role_Account",
            joinColumns = {@JoinColumn(name = "role_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "role_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "account_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "account_id", referencedColumnName = "id")})
    private Set<Account> accounts = new HashSet<>();

	public Role() {}

	public Role(PrimaryKey primaryKey) {
		super(primaryKey);
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }
	
    public void removePermission(Permission perm) {
    	permissions.remove(perm);
    }

    public void addPermission(Permission perm) {
    	permissions.add(perm);
    }
    
	public void removeAllPermissions() {
		permissions.clear();
	}

	public Set<ACE> getAcl() {
		return acl;
	}

	public void setAcl(Set<ACE> acl) {
		this.acl = acl;
	}
	
    public void removeAce(ACE ace) {
        acl.remove(ace);
    }

    public void addAce(ACE ace) {
        ace.setTagged(null);
        if (acl == null) {
            acl = new HashSet<>();
        }
        acl.add(ace);
    }
 
    public void removeAllAces() {
        acl.clear();
    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }

    public String toString() {
		String s = this.getClName() + ": " + getTenantId() + ":" + getId() + " " + name  + " ";
		if (description != null) {
			s += (" (" + description + ")");
		}
		return s;
	}

}
