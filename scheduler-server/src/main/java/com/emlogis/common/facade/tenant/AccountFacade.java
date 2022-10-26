package com.emlogis.common.facade.tenant;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.services.tenant.AccountService;
import com.emlogis.common.services.tenant.RoleService;
import com.emlogis.common.validation.annotations.Validate;
import com.emlogis.common.validation.annotations.ValidatePaging;
import com.emlogis.common.validation.annotations.Validation;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.tenant.Account;
import com.emlogis.model.tenant.Role;
import com.emlogis.model.tenant.dto.RoleDto;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class AccountFacade extends BaseFacade {

    @EJB
    private RoleService roleService;

    abstract protected AccountService getAccountService();

    @SuppressWarnings("unchecked")
    @Validation
    public ResultSetDto<RoleDto> findAccountRoles(
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
            PrimaryKey accountPrimaryKey,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, InstantiationException {
        SimpleQuery simpleQuery = new SimpleQuery(accountPrimaryKey.getTenantId());
        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true)
                .setEntityClass(Role.class);
        ResultSet<Role> resultSet = getAccountService().findAccountRoles(accountPrimaryKey, simpleQuery);
        return toResultSetDto(resultSet, RoleDto.class);
    }

    @SuppressWarnings("unchecked")
    @Validation
    public ResultSetDto<RoleDto> getUnassociatedRoles(
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
            PrimaryKey accountPrimaryKey,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, InstantiationException {
    	
        try {
	        SimpleQuery simpleQuery = new SimpleQuery(accountPrimaryKey.getTenantId());
	        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true)
                .setEntityClass(Role.class);
        	ResultSet<Role> resultSet = getAccountService().getUnassociatedRoles(accountPrimaryKey, simpleQuery);
            return toResultSetDto(resultSet, RoleDto.class);
        }
        catch (Throwable t) {
        	t.printStackTrace();
        	throw t;
        }
    }

    @Validation
    public void addRole(
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class) PrimaryKey accountPrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class) PrimaryKey rolePrimaryKey) {
        Account account = getAccountService().getAccount(accountPrimaryKey);
        Role role = roleService.getRole(rolePrimaryKey);
        getAccountService().addRole(account, role);
    }

    @Validation
    public void addRoles(
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class) PrimaryKey accountPrimaryKey,
            List<PrimaryKey> rolePrimaryKeys) {
    	// TODO, replace some code in getRoles()  by a new Array Validator when available
    	List<Role> roles = getRoles(rolePrimaryKeys);
    	Account account = getAccountService().getAccount(accountPrimaryKey);
    	for (Role role : roles) {
    		if (!account.getRoles().contains(role)) {  // silently skip roles already associated
    			getAccountService().addRole(account, role);;
    		}
   		}
    }

	@Validation
    public void removeRole(
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class) PrimaryKey accountPrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class) PrimaryKey rolePrimaryKey) {
        Account account = getAccountService().getAccount(accountPrimaryKey);
        Role role = roleService.getRole(rolePrimaryKey);
        getAccountService().removeRole(account, role);
    }

    @Validation
    public void removeRoles(
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class) PrimaryKey accountPrimaryKey,
            List<PrimaryKey> rolePrimaryKeys) {
    	// TODO, replace some code in getRoles()  by a new Array Validator when available
    	List<Role> roles = getRoles(rolePrimaryKeys);
    	Account account = getAccountService().getAccount(accountPrimaryKey);
    	for (Role role : roles) {
    		if (account.getRoles().contains(role)) {  // silently skip roles not associated to this account
    			getAccountService().removeRole(account, role);;
    		}
   		}
    }

    private List<Role> getRoles(List<PrimaryKey> rolePrimaryKeys) {
    	List<Role> roles = new ArrayList<>();
    	if (rolePrimaryKeys != null) {
	    	for (PrimaryKey roleKey : rolePrimaryKeys) {
	    		Role role = roleService.getRole(roleKey);
	    		if (role == null) {
	    			Map<String, Object> paramMap = new HashMap<>();
	    			paramMap.put("accountId", roleKey.getId());
	    			throw new ValidationException( "Cannot add/remove roles to/from account as at least one role is invalid. ",  paramMap);
	    		} else {
	    			roles.add(role);
	    		}
	    	}
    	}
    	return roles; 	
	}

	public RoleService getRoleService() {
		return roleService;
	}
    

}
