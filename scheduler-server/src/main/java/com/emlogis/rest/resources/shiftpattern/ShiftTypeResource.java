package com.emlogis.rest.resources.shiftpattern;

import com.emlogis.common.facade.shiftpattern.ShiftTypeFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.shiftpattern.dto.ShiftTypeCreateDto;
import com.emlogis.model.shiftpattern.dto.ShiftTypeDto;
import com.emlogis.model.shiftpattern.dto.ShiftTypeUpdateDto;
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

@Deprecated
@Path("shifttypes")
@Authenticated
public class ShiftTypeResource extends BaseResource {

    @EJB
    private ShiftTypeFacade shiftTypeFacade;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "List ShiftTypes", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ShiftTypeDto> getObjects(
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        return shiftTypeFacade.getObjects(tenantId, select, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "Get ShiftType info", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftTypeDto getObject(@PathParam("id") final String id) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return shiftTypeFacade.getObject(primaryKey);
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Update ShiftType", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftTypeDto updateObject(@PathParam("id") final String id, ShiftTypeUpdateDto shiftTypeUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return shiftTypeFacade.updateObject(primaryKey, shiftTypeUpdateDto);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Create ShiftType", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftTypeDto createObject(ShiftTypeCreateDto shiftTypeCreateDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey;
        if (StringUtils.isBlank(shiftTypeCreateDto.getId())) {
            // id is not specified (which is preferred), let's generate one
            primaryKey = createUniquePrimaryKey();
        } else {
            primaryKey = createPrimaryKey(shiftTypeCreateDto.getId());
        }
        shiftTypeCreateDto.setId(primaryKey.getId());
        return shiftTypeFacade.createObject(primaryKey, shiftTypeCreateDto);
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Delete ShiftType", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteObject(@PathParam("id") final String id) {
        PrimaryKey primaryKey = createPrimaryKey(id);
        shiftTypeFacade.deleteObject(primaryKey);
        return Response.ok().build();
    }
    
}
