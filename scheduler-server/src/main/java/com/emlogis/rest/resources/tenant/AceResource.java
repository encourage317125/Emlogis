package com.emlogis.rest.resources.tenant;

import com.emlogis.common.facade.tenant.AceFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ACEDto;
import com.emlogis.model.dto.ACEUpdateDto;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.dto.convenience.MatchedDto;
import com.emlogis.model.tenant.dto.RoleDto;
import com.emlogis.model.tenant.dto.UserAccountDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;

import javax.ejb.EJB;
import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

@Path("aces")
@Authenticated
public class AceResource extends BaseResource {

    @EJB
    private AceFacade aceFacade;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Audited(label = "Get ACL", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ACEDto> getAcl(
            @QueryParam("select") String select, // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        return aceFacade.getObjects(getTenantId(), select, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("/aces/{aceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Audited(label = "Get ACE", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ACEDto getAce(@PathParam("aceId") String aceId) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        PrimaryKey acePrimaryKey = createPrimaryKey(aceId);
        return aceFacade.getObject(acePrimaryKey);
    }

    @DELETE
    @Path("/aces/{aceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = Permissions.Role_Mgmt)
    @Audited(label = "Delete ACE", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteAce(@PathParam("aceId") String aceId) {
        PrimaryKey acePrimaryKey = createPrimaryKey(aceId);
        aceFacade.delete(acePrimaryKey);
        return Response.ok().build();
    }

    @PUT
    @Path("/aces/{aceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = Permissions.Role_Mgmt)
    @Audited(label = "Update ACE", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ACEDto updateAce(@PathParam("aceId") String aceId, ACEUpdateDto aceUpdateDto) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        PrimaryKey acePrimaryKey = createPrimaryKey(aceId);
        return aceFacade.update(acePrimaryKey, aceUpdateDto);
    }

    @GET
    @Path("{aceId}/roles")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Audited(label = "Get roles related to ACE", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<RoleDto> getRoles(@PathParam("aceId") String aceId) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        PrimaryKey acePrimaryKey = createPrimaryKey(aceId);
        return aceFacade.getRoles(acePrimaryKey);
    }

    @GET
    @Path("{aceId}/useraccounts")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Audited(label = "Get useraccounts related to ACE", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<UserAccountDto> getUserAccounts(@PathParam("aceId") String aceId) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        PrimaryKey acePrimaryKey = createPrimaryKey(aceId);
        return aceFacade.getUserAccounts(acePrimaryKey);
    }

    @GET
    @Path("/{aceId}/ops/matches")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Audited(label = "Get matching to ACE sites and teams", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public MatchedDto getMatched(@PathParam("aceId") String aceId) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        PrimaryKey acePrimaryKey = createPrimaryKey(aceId);
        return aceFacade.getMatched(acePrimaryKey);
    }

}
