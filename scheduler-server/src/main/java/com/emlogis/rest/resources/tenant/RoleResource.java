package com.emlogis.rest.resources.tenant;

import com.emlogis.common.facade.tenant.GroupAccountFacade;
import com.emlogis.common.facade.tenant.RoleFacade;
import com.emlogis.common.facade.tenant.UserAccountFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ACECreateDto;
import com.emlogis.model.dto.ACEDto;
import com.emlogis.model.dto.ACEUpdateDto;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.dto.convenience.ACEConfigurationAllSitesDto;
import com.emlogis.model.dto.convenience.MatchedDto;
import com.emlogis.model.tenant.GroupAccount;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.tenant.dto.*;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;

import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Path("/roles")
@Authenticated
public class RoleResource extends BaseResource {

    @EJB
    private RoleFacade roleFacade;

    @EJB
    private GroupAccountFacade groupAccountFacade;
    
    @EJB
    private UserAccountFacade userAccountFacade;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Audited(label = "List Roles", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<RoleDto> findRoles(
                @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
                @QueryParam("filter") String filter,
                @QueryParam("offset") @DefaultValue("0") int offset,
                @QueryParam("limit") @DefaultValue("20") int limit,
                @QueryParam("orderby") String orderBy,
                @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        return roleFacade.getObjects(tenantId, select, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{roleId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Audited(label = "Get Role Info", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public RoleDto getObject(@PathParam("roleId") final String roleId) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(roleId);
    	return roleFacade.getObject(primaryKey);
    }

    @PUT
    @Path("{roleId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_Mgmt})
    @Audited(label = "Update Role", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public RoleDto updateObject(@PathParam("roleId") final String roleId, RoleDto roleDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(roleId);
        return roleFacade.updateObject(primaryKey, roleDto);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_Mgmt})
    @Audited(label = "Create Role", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public RoleDto createObject(RoleCreateDto roleCreateDto) throws InstantiationException,IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
    	PrimaryKey primaryKey = this.createUniquePrimaryKey();
    	return roleFacade.createObject(primaryKey, roleCreateDto);
    }

    @DELETE
    @Path("{roleId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_Mgmt})
    @Audited(label = "Delete Role", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public boolean delete(@PathParam("roleId") String roleId) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        PrimaryKey primaryKey = createPrimaryKey(roleId);
    	return roleFacade.delete(primaryKey);
    }

    @POST
    @Path("{roleId}/ops/duplicate")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_Mgmt})
    @Audited(label = "Duplicate Role", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public RoleDto duplicate(@PathParam("roleId") String roleId, RoleCreateDto roleCreateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(roleId);
        return roleFacade.duplicateObject(primaryKey, roleCreateDto);
    }
    
	@GET
	@Path("{roleId}/permissions")
	@Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
	@Audited(label = "List Role Permissions", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
	public ResultSetDto<PermissionDto> permissions(
            @PathParam("roleId") String roleId,
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, NoSuchFieldException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(roleId);
	    return roleFacade.getPermissions(primaryKey, select, filter, offset, limit, orderBy, orderDir);
	}

    @POST
    @Path("{roleId}/ops/addpermission")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_Mgmt})
    @Audited(label = "Add Permission to Role", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public boolean addPermission(@PathParam("roleId") String roleId, String permissionId) {
        PrimaryKey primaryKey = createPrimaryKey(roleId);
        Permissions perm = Permissions.valueOf(Permissions.class, permissionId);
        return roleFacade.addPermission(primaryKey, perm);
    }
    
    @POST
    @Path("{roleId}/ops/addpermissions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Add Permissions to Role", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Role_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response addPermissions(@PathParam("roleId") String roleId, String[] permissionIdList) {
        PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        List<Permissions> permissions = getPermissionList(permissionIdList);
        roleFacade.addPermissions(rolePrimaryKey, permissions);
		return Response.ok().build();
    }

	@POST
    @Path("{roleId}/ops/removepermission")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_Mgmt})
    @Audited(label = "Remove Permission from Role", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public boolean removePermission(@PathParam("roleId") String roleId, String permissionId) {
        PrimaryKey primaryKey = createPrimaryKey(roleId);
        Permissions perm = Permissions.valueOf(Permissions.class, permissionId);
        return roleFacade.removePermission(primaryKey, perm);
    }

    @POST
    @Path("{roleId}/ops/removepermissions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Add Permissions to Role", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Role_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response removePermissions(@PathParam("roleId") String roleId, String[] permissionIdList) {
        PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        List<Permissions> permissions = getPermissionList(permissionIdList);
        roleFacade.removePermissions(rolePrimaryKey, permissions);
		return Response.ok().build();
    }

    @GET
    @Path("{roleId}/unassociatedpermissions")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Audited(label = "List Unassociated Permissions", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<PermissionDto> getUnassociatedRolePermissions(
            @PathParam("roleId") String roleId,
            @QueryParam("select") String select, // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        PrimaryKey primaryKey = createPrimaryKey(roleId);
        return roleFacade.getUnassociatedRolePermissions(primaryKey, select, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{roleId}/aces")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Audited(label = "Get role's ACL", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ACEDto> getAcl(
            @PathParam("roleId") String roleId,
            @QueryParam("select") String select, // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        return roleFacade.getAcl(rolePrimaryKey, select, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{roleId}/aces/{aceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Audited(label = "Get Role ACEs", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ACEDto getAce(
            @PathParam("roleId") String roleId,
            @PathParam("aceId") String aceId) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        PrimaryKey acePrimaryKey = createPrimaryKey(aceId);
        return roleFacade.getAce(rolePrimaryKey, acePrimaryKey);
    }

    @POST
    @Path("{roleId}/aces")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = Permissions.Role_Mgmt)
    @Audited(label = "Create ACE", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ACEDto createAce(@PathParam("roleId") String roleId, ACECreateDto aceCreateDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            NoSuchFieldException {
        PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        return roleFacade.createAce(rolePrimaryKey, aceCreateDto);
    }

    @PUT
    @Path("{roleId}/aces/{aceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = Permissions.Role_Mgmt)
    @Audited(label = "Update ACE", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ACEDto updateAce(@PathParam("roleId") String roleId,
                            @PathParam("aceId") String aceId,
                            ACEUpdateDto aceUpdateDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            NoSuchFieldException {
        PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        PrimaryKey acePrimaryKey = createPrimaryKey(aceId);
        return roleFacade.updateAce(rolePrimaryKey, acePrimaryKey, aceUpdateDto);
    }

    @DELETE
    @Path("{roleId}/aces/{aceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = Permissions.Role_Mgmt)
    @Audited(label = "Delete ACE", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteAce(@PathParam("roleId") String roleId, @PathParam("aceId") String aceId) {
        PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        PrimaryKey acePrimaryKey = createPrimaryKey(aceId);

        roleFacade.deleteAce(rolePrimaryKey, acePrimaryKey);
        return Response.ok().build();
    }

    @GET
    @Path("{roleId}/useraccounts")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Audited(label = "Get useraccounts related to the role", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<UserAccountDto> getUserAccounts(
            @PathParam("roleId") String roleId,
            @QueryParam("inherited") @DefaultValue("false") boolean inherited) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
    	PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        return roleFacade.getUserAccounts(rolePrimaryKey, inherited);
    }

    @GET
    @Path("{roleId}/unassociateduseraccounts")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Audited(label = "List Unassociated Accounts", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<AccountDto> getUnassociatedUserAccounts(
            @PathParam("roleId") String roleId,
            @QueryParam("select") String select, // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        PrimaryKey primaryKey = createPrimaryKey(roleId);
        return roleFacade.getUnassociatedAccounts(primaryKey, select, filter, offset, limit, orderBy, orderDir, UserAccount.class);
    }

    @GET
    @Path("{roleId}/groupaccounts")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_Mgmt})
    @Audited(label = "Get groupaccounts related to the role", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<GroupAccountDto> getGroupAccounts(@PathParam("roleId") String roleId)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            NoSuchFieldException {
    	PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        return roleFacade.getGroupAccounts(rolePrimaryKey);
    }


    @GET
    @Path("{roleId}/unassociatedgroupaccounts")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Audited(label = "List Unassociated Accounts", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<AccountDto> getUnassociatedGroupAccounts(
            @PathParam("roleId") String roleId,
            @QueryParam("select") String select, // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        PrimaryKey primaryKey = createPrimaryKey(roleId);
        return roleFacade.getUnassociatedAccounts(primaryKey, select, filter, offset, limit, orderBy, orderDir, GroupAccount.class);
    }

    @GET
    @Path("{roleId}/aces/ops/matches")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_Mgmt})
    @Audited(label = "Get Site/Team matched to the role", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public MatchedDto getMatched(@PathParam("roleId") String roleId) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
    	PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        return roleFacade.getMatched(rolePrimaryKey);
    }

    @GET
    @Path("{roleId}/aces/{aceId}/ops/matches")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View})
    @Audited(label = "Get Role ACL", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public MatchedDto getMatched(@PathParam("roleId") String roleId, @PathParam("aceId") String aceId)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            NoSuchFieldException {
        PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        PrimaryKey acePrimaryKey = createPrimaryKey(aceId);
        return roleFacade.getMatched(rolePrimaryKey, acePrimaryKey);
    }

    @GET
    @Path("{roleId}/aces/ops/getsitesteamsaces")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View})
    @Audited(label = "Set Role ACL", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ACEConfigurationAllSitesDto getSitesTeamsAces(@PathParam("roleId") String roleId)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            NoSuchFieldException {
        PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        return roleFacade.getSitesTeamsAces(rolePrimaryKey);
    }

    @POST
    @Path("{roleId}/aces/ops/setsitesteamsaces")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_Mgmt})
    @Audited(label = "Get Site/Team matched to the role", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ACEConfigurationAllSitesDto setSitesTeamsAces(@PathParam("roleId") String roleId,
                                                         ACEConfigurationAllSitesDto aceConfigurationAllSitesDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            NoSuchFieldException {
        PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        return roleFacade.setSitesTeamsAces(rolePrimaryKey, aceConfigurationAllSitesDto);
    }

    @POST
    @Path("{roleId}/ops/addusers")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "AssociateUsers To Role", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Role_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response addMembers(@PathParam("roleId") String roleId, String[] userIdList) {
        PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        List<PrimaryKey> accountPrimaryKeys = getAccountListPK(rolePrimaryKey, userIdList);
        // role facade cannot implement addUsers as this would introduce circular dependencies between underlying group,user and role services
        // so for the sake of impl simplicity, have to iterate through loop in resource
        for (PrimaryKey accountKey : accountPrimaryKeys) {
            userAccountFacade.addRole(accountKey, rolePrimaryKey);        	
        }
		return Response.ok().build();
    }
    
    @POST
    @Path("{roleId}/ops/removeusers")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "UnassociateUsers From Role", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Role_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response removeMembers(@PathParam("roleId") String roleId, String[] userIdList) {
        PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        List<PrimaryKey> accountPrimaryKeys = getAccountListPK(rolePrimaryKey, userIdList);
        // role facade cannot implement removeUsers as this would introduce circular dependencies between underlying group,user and role services
        // so for the sake of impl simplicity, have to iterate through loop in resource
        for (PrimaryKey accountKey : accountPrimaryKeys) {
            userAccountFacade.removeRole(accountKey, rolePrimaryKey);        	
        }
		return Response.ok().build();
    }

    @POST
    @Path("{roleId}/ops/addgroups")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "AssociateGroups To Role", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Role_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response addGroups(@PathParam("roleId") String roleId, String[] groupIdList) {
        PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        List<PrimaryKey> accountPrimaryKeys = getAccountListPK(rolePrimaryKey, groupIdList);
        // role facade cannot implement addUsers as this would introduce circular dependencies between underlying group,user and role services
        // so for the sake of impl simplicity, have to iterate through loop in resource
        for (PrimaryKey accountKey : accountPrimaryKeys) {
        	groupAccountFacade.addRole(accountKey, rolePrimaryKey);        	
        }
		return Response.ok().build();
    }
    
    @POST
    @Path("{roleId}/ops/removegroups")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "UnassociateGroups From Role", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Role_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response removeGroups(@PathParam("roleId") String roleId, String[] groupIdList) {
        PrimaryKey rolePrimaryKey = createPrimaryKey(roleId);
        List<PrimaryKey> accountPrimaryKeys = getAccountListPK(rolePrimaryKey, groupIdList);
        // role facade cannot implement removeUsers as this would introduce circular dependencies between underlying group,user and role services
        // so for the sake of impl simplicity, have to iterate through loop in resource
        for (PrimaryKey accountKey : accountPrimaryKeys) {
            groupAccountFacade.removeRole(accountKey, rolePrimaryKey);        	
        }
		return Response.ok().build();
    }

    @GET
    @Path("ops/quicksearch")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Audited(label = "Role quick search", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<Object> quickSearch(
            @QueryParam("search") String searchValue,
            @QueryParam("searchfields") String searchFields,
            @QueryParam("returnedfields") String returnedFields,
            @QueryParam("limit") @DefaultValue("-1") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        return roleFacade.quickSearch(tenantId, searchValue, searchFields, returnedFields, limit, orderBy, orderDir);
    }

    @GET
    @Path("ops/query")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Audited(label = "Role query", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<RoleReadDto> query(
            @QueryParam("search") String searchValue,
            @QueryParam("searchfields") String searchFields,
            @QueryParam("userfilter") String userFilter,
            @QueryParam("groupfilter") String groupFilter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        return roleFacade.query(tenantId, searchValue, searchFields, userFilter, groupFilter, offset, limit,
                orderBy, orderDir);
    }

    @GET
    @Path("{roleId}/roleview")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Audited(label = "Get RoleViewDto", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public RoleViewDto getRoleView(@PathParam("roleId") String roleId) throws InstantiationException,
            NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(roleId);
        return roleFacade.getRoleView(primaryKey);
    }

    private List<PrimaryKey> getAccountListPK(PrimaryKey rolePrimaryKey, String[] accountIdList) {
        List<PrimaryKey> accountPrimaryKeys = new ArrayList<PrimaryKey>();
        if (accountIdList != null) {
            for (String accountId : accountIdList) {
                if (!StringUtils.isBlank(accountId)) {
                    accountPrimaryKeys.add(new PrimaryKey(rolePrimaryKey.getTenantId(), accountId));
                }
            }
        }
        return accountPrimaryKeys;
    }

    private List<Permissions> getPermissionList(String[] permissionIdList) {
        List<Permissions> perms = new ArrayList<>();
        if (permissionIdList != null) {
            for (String permId : permissionIdList) {
                if (!StringUtils.isBlank(permId)) {
                    Permissions perm = Permissions.valueOf(Permissions.class, permId);
                    perms.add(perm);
                }
            }
        }
        return perms;
    }

}
