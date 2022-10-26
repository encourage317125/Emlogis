package com.emlogis.rest.resources.shiftpattern;

import com.emlogis.common.facade.shiftpattern.ShiftLengthFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.shiftpattern.dto.ShiftLengthCreateDto;
import com.emlogis.model.shiftpattern.dto.ShiftLengthDto;
import com.emlogis.model.shiftpattern.dto.ShiftLengthUpdateDto;
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
@Path("shiftlengths")
@Authenticated
public class ShiftLengthResource extends BaseResource {

    @EJB
    private ShiftLengthFacade shiftLengthFacade;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "List ShiftLengths", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ShiftLengthDto> getObjects(
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        return shiftLengthFacade.getObjects(tenantId, select, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "Get ShiftLength info", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftLengthDto getObject(@PathParam("id") final String id) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return shiftLengthFacade.getObject(primaryKey);
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Update ShiftLength", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftLengthDto updateObject(@PathParam("id") final String id, ShiftLengthUpdateDto shiftLengthUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return shiftLengthFacade.updateObject(primaryKey, shiftLengthUpdateDto);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Create ShiftLength", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftLengthDto createObject(ShiftLengthCreateDto shiftLengthCreateDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey;
        if (StringUtils.isBlank(shiftLengthCreateDto.getId())) {
            // id is not specified (which is preferred), let's generate one
            primaryKey = createUniquePrimaryKey();
        } else {
            primaryKey = createPrimaryKey(shiftLengthCreateDto.getId());
        }
        shiftLengthCreateDto.setId(primaryKey.getId());
        return shiftLengthFacade.createObject(primaryKey, shiftLengthCreateDto);
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Delete ShiftLength", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteObject(@PathParam("id") final String id) {
        PrimaryKey primaryKey = createPrimaryKey(id);
        shiftLengthFacade.deleteObject(primaryKey);
        return Response.ok().build();
        // TODO bag request if there are any ShiftType referenced
    }

    @GET
    @Path("/ops/query")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Demand_Mgmt})
    @Audited(label = "Query ShiftLengths", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ShiftLengthDto> query(
            @QueryParam("hasshifttype") @DefaultValue("true") boolean hasShiftType,
            @QueryParam("siteId") String siteId,
            @QueryParam("shiftlength") Integer shiftLengthInMin,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        return shiftLengthFacade.query(getTenantId(), hasShiftType, siteId, shiftLengthInMin, offset, limit, orderBy,
                orderDir);
    }
}
