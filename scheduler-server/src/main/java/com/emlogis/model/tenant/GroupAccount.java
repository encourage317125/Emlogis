package com.emlogis.model.tenant;

import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(name = "GroupAccount")
public class GroupAccount extends Account {
	
	public static final String DEFAULT_ADMINGROUP_ID = "admingroup";
	public static final String DEFAULT_SCHEDULECREATORGROUP_ID = "schedulecreatorgroup";
	public static final String DEFAULT_SHIFTMANAGERGROUP_ID = "shiftmanagergroup";
//	public static final String DEFAULT_TACTICAL_ADMINSCHEDULERGROUP_ID = "tacticaladminschedulergroup";
//	public static final String DEFAULT_BASIC_ADMINSCHEDULERGROUP_ID = "basicadminschedulergroup";
	public static final String DEFAULT_EMPLOYEEGROUP_ID = "employeegroup";
	public static final String DEFAULT_SUPPORTGROUP_ID = "servicesupportgroup";
	public static final String DEFAULT_SERVICEADMINGROUP_ID = "serviceadmingroup";

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "User_Group",
            joinColumns = {@JoinColumn(name = "group_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "group_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_tenantId", referencedColumnName = "tenantId"),
                    @JoinColumn(name = "user_id", referencedColumnName = "id")})
    private Set<UserAccount> members = new HashSet<>();

	public GroupAccount() {		
		super();
	}

	public GroupAccount(PrimaryKey primaryKey) {
		super(primaryKey);
	}

    public Set<UserAccount> getMembers() {
        return members;
    }

    public void setMembers(Set<UserAccount> members) {
        this.members = members;
    }

	public void removeAllMembers() {
		members.clear();
	}

}