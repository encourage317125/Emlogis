package com.emlogis.rest.resources.tenant;

import com.emlogis.common.facade.tenant.AccountFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.tenant.dto.RoleDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;
import org.apache.commons.lang3.StringUtils;

import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Authenticated
abstract public class AccountResource extends BaseResource {

    abstract protected AccountFacade getAccountFacade();

    @GET
    @Path("{accountId}/roles")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "List Account Roles", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt, Permissions.Role_View,
            Permissions.Role_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<RoleDto> findAccountRoles(
            @PathParam("accountId") String accountId,
            @QueryParam("select") String select, // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        PrimaryKey accountPrimaryKey = createPrimaryKey(accountId);
        return getAccountFacade().findAccountRoles(accountPrimaryKey, select, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{accountId}/unassociatedroles")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt, Permissions.Role_View,
            Permissions.Role_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<RoleDto> getUnassociatedRoles(
            @PathParam("accountId") String accountId,
            @QueryParam("select") String select, // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        PrimaryKey accountPrimaryKey = createPrimaryKey(accountId);
        return getAccountFacade().getUnassociatedRoles(accountPrimaryKey, select, filter, offset, limit, orderBy,
                orderDir);
    }

    @POST
    @Path("{accountId}/ops/addrole")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "AddRole To Account", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response addRole(@PathParam("accountId") String accountId, String roleId) {
        PrimaryKey accountPrimaryKey = createPrimaryKey(accountId);
        PrimaryKey rolePrimaryKey = new PrimaryKey(accountPrimaryKey.getTenantId(), roleId);
        getAccountFacade().addRole(accountPrimaryKey, rolePrimaryKey);
		return Response.ok().build();
    }

    @POST
    @Path("{accountId}/ops/addroles")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "AddRoles To Account", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response addRoles(@PathParam("accountId") String accountId, String[] roleIdList) {
        PrimaryKey accountPrimaryKey = createPrimaryKey(accountId);
        List<PrimaryKey> rolePrimaryKeys = getRoleListPK(accountPrimaryKey, roleIdList);
        getAccountFacade().addRoles(accountPrimaryKey, rolePrimaryKeys);
		return Response.ok().build();
    }

	@POST
    @Path("{accountId}/ops/removerole")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "RemoveRole From Account", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response removeRole(@PathParam("accountId") String accountId, String roleId) {
        PrimaryKey accountPrimaryKey = createPrimaryKey(accountId);
        PrimaryKey rolePrimaryKey = new PrimaryKey(accountPrimaryKey.getTenantId(), roleId);
        getAccountFacade().removeRole(accountPrimaryKey, rolePrimaryKey);
		return Response.ok().build();
    }

    @POST
    @Path("{accountId}/ops/removeroles")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "RemoveRoles From Account", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response removeRoles(@PathParam("accountId") String accountId, String[] roleIdList) {
        PrimaryKey accountPrimaryKey = createPrimaryKey(accountId);
        List<PrimaryKey> rolePrimaryKeys = getRoleListPK(accountPrimaryKey, roleIdList);
        getAccountFacade().removeRoles(accountPrimaryKey, rolePrimaryKeys);
		return Response.ok().build();
    }

	private List<PrimaryKey> getRoleListPK(PrimaryKey accountPrimaryKey, String[] roleIdList) {
		List<PrimaryKey> rolePrimaryKeys = new ArrayList<>();
	    if (roleIdList != null) {
	    	for (String roleId : roleIdList) {
	    		if (!StringUtils.isBlank(roleId)) {
	    			rolePrimaryKeys.add(new PrimaryKey(accountPrimaryKey.getTenantId(), roleId));
	    		}
	    	}
	    }
		return rolePrimaryKeys;
	}

}
