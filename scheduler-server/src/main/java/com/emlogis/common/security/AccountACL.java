package com.emlogis.common.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class wrapping a list of ACLs
 * 
 * provides a convenience metthod to get a sublist of the ACLs related to a specific entity/class (and to some
 * extent to clone the AccountACLList by specifying entityClass = null)
 *
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountACL implements Serializable {
	
	private	List<AccountACE> acl = new ArrayList<>();

	public AccountACL() {
		super();
	}

	public AccountACL(List<AccountACE> acl) {
		super();
		this.acl = acl;
	}

	public List<AccountACE> getAcl() {
		return acl;
	}

	public void setAcl(List<AccountACE> acls) {
		this.acl = acls;
	}
	
	public void addAce(AccountACE ace) {
		acl.add(ace);
	}
	
	public void removeAce(AccountACE ace) {
		acl.remove(ace);
	}

    public String[] getAceIds() {
        if (acl == null) {
            return null;
        }
        String[] result = new String[acl.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = acl.get(i).getId();
        }
        return result;
    }

	public AccountACL getEntityAcls(String entityClass) {
		AccountACL subacls = new AccountACL();
        for (AccountACE ace: acl) {
        	if (entityClass == null || StringUtils.equals(entityClass, ace.getEntityClass())) {
        		subacls.addAce(ace);
        	}
        }
        return subacls;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("AccountACList: ");
		for (AccountACE ace : acl) {
			sb.append("\n\t").append(ace.toString());
		}
		return sb.toString();
	}

}
