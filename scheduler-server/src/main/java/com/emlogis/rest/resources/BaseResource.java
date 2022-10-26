package com.emlogis.rest.resources;

import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.security.ACLProtected;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.security.PermissionCheck;
import com.emlogis.common.security.Permissions;
import com.emlogis.common.services.tenant.ACEService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.rest.security.SessionService;

import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import javax.inject.Inject;

import java.security.AccessControlException;
import java.util.Locale;

public abstract class BaseResource {
	
    @Inject
    private SessionService sessionService;

    @EJB
    private ACEService aceService;
    
    public SessionService getSessionService() {
        return sessionService;
    }

    /**
     * getTenantId() returns the tenantId of customer associated to current API client
     * @return tenantId
     */
    protected String getTenantId() {
    	return sessionService.getTenantId();
    }
 
	/**
	 * get Id associated to currently logged API client
	 * @return userId
	 */
	public String getUserId() {
		return sessionService.getUserId();
	}

	/**
	 * get Id associated to currently logged API Employee (can be null if client has an account but is not an employee
	 * @return userId
	 */
	public String getEmployeeId() {
		return sessionService.getEmployeeId();
	}
    
    protected PrimaryKey createPrimaryKey(String id) {
        String tenantId = getTenantId();

		if (StringUtils.isBlank(tenantId) || StringUtils.isBlank(id)) {
			throw new ValidationException("Invalid or missing Instance Id, or missing or invalid tenant identifier " +
                    "or object identifier");
		}
        return new PrimaryKey(tenantId, id);
    }

    protected PrimaryKey createUniquePrimaryKey() {
        String tenantId = getTenantId();
		if (StringUtils.isBlank(tenantId)) {
			throw new ValidationException("Invalid or missing tenant identifier");
		}
        return new PrimaryKey(getTenantId());
    }

    /**
     * getAcls() returns all ACLs associated with current account
     *      
     * @return AccountACLList
     */
    public AccountACL getAcl() {
    	return sessionService.getAcl();
    }

	/**
	 * check an entity has at least one of the permissions specified in parameter
	 * throw an exception if access denied
	 */
    public ACLProtected checkAcl(PrimaryKey primaryKey, PermissionCheck checkType, Permissions ... permissions)
            throws AccessControlException {
		return aceService.checkAcl(primaryKey, getAcl(), checkType, permissions);
	}

    protected Locale locale(){
        String token = getSessionService().getTokenId();
        return new Locale(getSessionService().getSessionInfo(token).getLanguage());
    }

}
