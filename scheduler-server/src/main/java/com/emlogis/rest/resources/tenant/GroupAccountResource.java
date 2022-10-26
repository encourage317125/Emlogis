package com.emlogis.rest.resources.tenant;

import com.emlogis.common.facade.tenant.AccountFacade;
import com.emlogis.common.facade.tenant.GroupAccountFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.dto.convenience.ACEConfigurationAllSitesDto;
import com.emlogis.model.tenant.dto.GroupAccountDto;
import com.emlogis.model.tenant.dto.GroupAccountViewDto;
import com.emlogis.model.tenant.dto.GroupReadDto;
import com.emlogis.model.tenant.dto.UserAccountDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
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

@Path("groupaccounts")
@Authenticated
public class GroupAccountResource extends AccountResource {

    @EJB
    private GroupAccountFacade groupAccountFacade;

    @Override
    protected AccountFacade getAccountFacade() {
        return groupAccountFacade;
    }

    /**
     *
     * @param select
     * @param filter
     * @param offset
     * @param limit
     * @param orderBy
     * @param orderDir
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "List GroupAccounts", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<GroupAccountDto> getObjects(
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        
        return groupAccountFacade.getObjects(tenantId, select, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Get GroupAccount Info", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public GroupAccountDto getObject(@PathParam("groupId") String groupId) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        PrimaryKey primaryKey = createPrimaryKey(groupId);

        return groupAccountFacade.getObject(primaryKey);
    }

    @PUT
    @Path("{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Update GroupAccount Info", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public GroupAccountDto update(@PathParam("groupId") final String groupId, GroupAccountDto accountDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            IllegalArgumentException {
        PrimaryKey primaryKey = createPrimaryKey(groupId);

        return groupAccountFacade.updateObject(primaryKey, accountDto);
    }

    @DELETE
    @Path("{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Delete GroupAccount", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response delete(@PathParam("groupId") String groupId) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        PrimaryKey primaryKey = createPrimaryKey(groupId);

        groupAccountFacade.deleteObject(primaryKey);
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Create GroupAccount", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public GroupAccountDto create(GroupAccountDto accountDto) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, IllegalArgumentException {
    	PrimaryKey primaryKey;
    	if (StringUtils.isBlank(accountDto.getId())) {
    		// id is not specified (which is preferred), let's generate one
    		primaryKey = createUniquePrimaryKey();
    	} else {
    		primaryKey = createPrimaryKey(accountDto.getId());
    	}
        accountDto.setId(primaryKey.getId());
        return groupAccountFacade.createObject(primaryKey, accountDto);
    }

    @GET
    @Path("{groupId}/users")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "List GroupAccount Members", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<UserAccountDto> members(
            @PathParam("groupId") String groupId,
            @QueryParam("select") String select,        // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        PrimaryKey primaryKey = createPrimaryKey(groupId);
        return groupAccountFacade.members(primaryKey, select, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{groupId}/unassociatedusers")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<UserAccountDto> getUnassociatedMembers(
            @PathParam("groupId") String groupId,
            @QueryParam("select") String select, // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchFieldException, IllegalArgumentException,
                IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        PrimaryKey primaryKey = createPrimaryKey(groupId);
        return groupAccountFacade.getUnassociatedMembers(primaryKey, select, filter, offset, limit, orderBy, orderDir);
    }

    @POST
    @Path("{groupId}/ops/adduser")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "AddMember To GroupAccount", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response addMember(@PathParam("groupId") String groupId, String memberId) {
        PrimaryKey primaryKey = createPrimaryKey(groupId);
        PrimaryKey memberPrimaryKey = createPrimaryKey(memberId);

        groupAccountFacade.addMember(primaryKey, memberPrimaryKey);
        return Response.ok().build();
    }

    @POST
    @Path("{groupId}/ops/addusers")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "AddMembers To GroupAccount", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response addMembers(@PathParam("groupId") String groupId, String[] memberIdList) {
        PrimaryKey groupPrimaryKey = createPrimaryKey(groupId);
        List<PrimaryKey> userPrimaryKeys = getUserListPK(groupPrimaryKey, memberIdList);
        groupAccountFacade.addMembers(groupPrimaryKey, userPrimaryKeys);
		return Response.ok().build();
    }
 
    @POST
    @Path("{groupId}/ops/removeuser")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "RemoveMember From GroupAccount", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response removeMember(@PathParam("groupId") String groupId, String memberId) {
        PrimaryKey primaryKey = createPrimaryKey(groupId);
        PrimaryKey memberPrimaryKey = createPrimaryKey(memberId);

        groupAccountFacade.removeMember(primaryKey, memberPrimaryKey);
        return Response.ok().build();
    }
  
    @POST
    @Path("{groupId}/ops/removeusers")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "RemoveMembers From GroupAccount", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response removeMembers(@PathParam("groupId") String groupId, String[] memberIdList) {
    	PrimaryKey groupPrimaryKey = createPrimaryKey(groupId);
        List<PrimaryKey> userPrimaryKeys = getUserListPK(groupPrimaryKey, memberIdList);
        groupAccountFacade.removeMembers(groupPrimaryKey, userPrimaryKeys);
		return Response.ok().build();
    }
	
    @GET
    @Path("ops/quicksearch")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Audited(label = "Group quick search", callCategory = ApiCallCategory.AccountManagement)
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
        return groupAccountFacade.quickSearch(tenantId, searchValue, searchFields, returnedFields, limit, orderBy,
                orderDir);
    }

    @GET
    @Path("ops/query")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Audited(label = "Group query", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<GroupReadDto> query(
            @QueryParam("filter") String filter,
            @QueryParam("search") String searchValue,
            @QueryParam("searchfields") String searchFields,
            @QueryParam("userfilter") String userFilter,
            @QueryParam("rolefilter") String roleFilter,
            @QueryParam("groupfilter") String groupFilter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        return groupAccountFacade.query(tenantId, searchValue, searchFields, userFilter, roleFilter, groupFilter,
                filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{groupId}/aces/ops/getsitesteamsaces")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Audited(label = "Get Group ACL", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ACEConfigurationAllSitesDto getSitesTeamsAces(@PathParam("groupId") String groupId)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            NoSuchFieldException {
        PrimaryKey groupPrimaryKey = createPrimaryKey(groupId);
        return groupAccountFacade.getSitesTeamsAces(groupPrimaryKey);
    }

    @POST
    @Path("{groupId}/aces/ops/setsitesteamsaces")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Audited(label = "Set Group ACL", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ACEConfigurationAllSitesDto setSitesTeamsAces(@PathParam("groupId") String groupId,
                                                         ACEConfigurationAllSitesDto aceConfigurationAllSitesDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            NoSuchFieldException {
        PrimaryKey groupPrimaryKey = createPrimaryKey(groupId);
        return groupAccountFacade.setSitesTeamsAces(groupPrimaryKey, aceConfigurationAllSitesDto);
    }

    @GET
    @Path("{groupId}/groupview")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Audited(label = "Get GroupAccountViewDto", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public GroupAccountViewDto getGroupAccountView(@PathParam("groupId") String groupId)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            NoSuchFieldException {
        PrimaryKey groupPrimaryKey = createPrimaryKey(groupId);
        return groupAccountFacade.getGroupAccountView(groupPrimaryKey);
    }

    private List<PrimaryKey> getUserListPK(PrimaryKey groupPrimaryKey, String[] userIdList) {
        List<PrimaryKey> userPrimaryKeys = new ArrayList<>();
        if (userIdList != null) {
            for (String userId : userIdList) {
                if (!StringUtils.isBlank(userId)) {
                    userPrimaryKeys.add(new PrimaryKey(groupPrimaryKey.getTenantId(), userId));
                }
            }
        }
        return userPrimaryKeys;
    }

}
