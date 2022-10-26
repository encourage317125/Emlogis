package com.emlogis.rest.resources.schedule;

import com.emlogis.common.facade.schedule.ShiftStructureFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.schedule.dto.*;
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

@Path("/shiftstructures")
@Authenticated
public class ShiftStructureResource extends BaseResource {

    @EJB
    private ShiftStructureFacade shiftStructureFacade;

    /**
     * Get list of ShiftStructure
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "List ShiftStructures", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ShiftStructureDto> getObjects(
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        return shiftStructureFacade.getObjects(tenantId, select, offset, limit, orderBy, orderDir);
    }

    /**
     * Read one ShiftStructure
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "Get ShiftStructureInfo", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftStructureDto getObject(@PathParam("id") final String id) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return shiftStructureFacade.getObject(primaryKey);
    }

    /**
     * Update a ShiftStructure
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Update ShiftStructureInfo", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftStructureDto updateObject(@PathParam("id") final String id,
                                          ShiftStructureUpdateDto shiftStructureUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return shiftStructureFacade.updateObject(primaryKey, shiftStructureUpdateDto);
    }

    /**
     * Creates an ShiftStructure
     * @return ShiftStructure
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Create ShiftStructure", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftStructureDto createObject(ShiftStructureCreateDto structureCreateDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createUniquePrimaryKey();
        return shiftStructureFacade.createObject(primaryKey, structureCreateDto);
    }

    /**
     * Delete an ShiftStructure
     * @return Response
     */
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Delete ShiftStructure", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteObject(@PathParam("id") final String id) {
        PrimaryKey primaryKey = createPrimaryKey(id);
        shiftStructureFacade.deleteObject(primaryKey);
        return Response.ok().build();
    }

    /**
     * Get list of ShiftReq
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @GET
    @Path("{shiftstructureId}/shiftreqs")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "List ShiftReqs", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ShiftReqOldDto> getShiftReqs(
            @PathParam("shiftstructureId") String shiftStructureId,
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey shiftStructurePrimaryKey = createPrimaryKey(shiftStructureId);

        return shiftStructureFacade.getShiftReqs(shiftStructurePrimaryKey, filter, offset, limit, orderBy, orderDir);
    }

    /**
     * Read one ShiftReq
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @GET
    @Path("{shiftstructureId}/shiftreqs/{shiftreqId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "Get ShiftReqInfo", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftReqOldDto getShiftReq(
                @PathParam("shiftstructureId") String shiftStructureId,
                @PathParam("shiftreqId") String shiftReqId) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey reqPrimaryKey = createPrimaryKey(shiftReqId);

        return shiftStructureFacade.getShiftReq(reqPrimaryKey);
    }

    /**
     * Update a ShiftReq
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @PUT
    @Path("{shiftstructureId}/shiftreqs/{shiftreqId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Update ShiftReqInfo", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftReqOldDto updateShiftReq(@PathParam("shiftstructureId") String shiftStructureId,
                                      @PathParam("shiftreqId") String shiftReqId,
                                      ShiftReqOldDto shiftReqOldDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey structurePrimaryKey = createPrimaryKey(shiftStructureId);

        return shiftStructureFacade.updateShiftReq(structurePrimaryKey, shiftReqOldDto);
    }

    /**
     * Creates an ShiftReq
     * @return ShiftReq
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @POST
    @Path("{shiftstructureId}/shiftreqs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Create ShiftReq", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftReqOldDto createShiftReq(@PathParam("shiftstructureId") String shiftStructureId,
                                         ShiftReqOldDto shiftReqOldDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey structurePrimaryKey = createPrimaryKey(shiftStructureId);

        PrimaryKey reqPrimaryKey;
        if (StringUtils.isBlank(shiftReqOldDto.getId())) {
            // id is not specified (which is preferred), let's generate one
            reqPrimaryKey = createUniquePrimaryKey();
        } else {
            reqPrimaryKey = createPrimaryKey(shiftReqOldDto.getId());
        }
        shiftReqOldDto.setId(reqPrimaryKey.getId());
        shiftReqOldDto.setShiftStructureId(shiftStructureId);

        return shiftStructureFacade.createShiftReq(structurePrimaryKey, shiftReqOldDto);
    }

    /**
     * Delete an ShiftReq
     * @return Response
     */
    @DELETE
    @Path("{shiftstructureId}/shiftreqs/{shiftreqId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Delete ShiftReq", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteShiftReq(@PathParam("shiftstructureId") String shiftStructureId,
                                   @PathParam("shiftreqId") String shiftReqId) {
        PrimaryKey structurePrimaryKey = createPrimaryKey(shiftStructureId);
        PrimaryKey reqPrimaryKey = createPrimaryKey(shiftReqId);

        shiftStructureFacade.deleteShiftReq(structurePrimaryKey, reqPrimaryKey);

        return Response.ok().build();
    }

    /**
     * Get list of Schedules
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @GET
    @Path("{shiftstructureId}/schedules")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "List Schedules", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ScheduleDto> getSchedules(
            @PathParam("shiftstructureId") String shiftStructureId,
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        PrimaryKey structurePrimaryKey = createPrimaryKey(shiftStructureId);

        return shiftStructureFacade.getSchedules(structurePrimaryKey, select, filter, offset, limit, orderBy, orderDir);
    }

    @POST
    @Path("{shiftstructureId}/ops/duplicate")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Duplicate Shift Structures", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftStructureDto duplicate(@PathParam("shiftstructureId") String shiftStructureId,
                                       @QueryParam("startdate") long startDate) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        PrimaryKey structurePrimaryKey = createPrimaryKey(shiftStructureId);
        return shiftStructureFacade.duplicate(structurePrimaryKey, startDate);
    }

    @POST
    @Path("{shiftstructureId}/ops/duplicateshiftreqs")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Duplicate Shift Reqs", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftStructureDto duplicateShiftReqs(
            @PathParam("shiftstructureId") String shiftStructureId,
            @QueryParam("dayindexfrom") int dayIndexFrom,
            @QueryParam("dayindexto") int dayIndexTo) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        PrimaryKey structurePrimaryKey = createPrimaryKey(shiftStructureId);
        return shiftStructureFacade.duplicateShiftReqs(structurePrimaryKey, dayIndexFrom, dayIndexTo);
    }

}
