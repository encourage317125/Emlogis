package com.emlogis.rest.resources.schedule;

import com.emlogis.common.facade.schedule.ShiftFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.schedule.dto.ShiftDto;
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
import java.lang.reflect.InvocationTargetException;

@Path("/shifts")
@Authenticated
public class ShiftResource extends BaseResource {

    @EJB
    private ShiftFacade shiftFacade;

    /**
     * Get list of Shift
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "List Shifts", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ShiftDto> getObjects(
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        return shiftFacade.getObjects(tenantId, select, filter, offset, limit, orderBy, orderDir);
    }

    /**
     * Get list of Shift for supporting Dashboard Calendar
     */
    @GET
    @Path("employee/{employeeId}")
    @Produces(MediaType.APPLICATION_JSON)
// commented out temporarily until we clarify security for this API
//    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "List Shifts", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<Object[]> getObjectOps(
            @PathParam("employeeId") String employeeId,
            @QueryParam("startdate") long startDate,
            @QueryParam("enddate") long endDate,
            @QueryParam("timezone") @DefaultValue("UTC") String timeZone,
            @QueryParam("schedulestatus") @DefaultValue("Posted") String scheduleStatus,
            @QueryParam("returnedfields") String returnedFields,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("-1") int limit,
            @QueryParam("orderby") @DefaultValue("startDateTime") String orderBy,
            @QueryParam("orderdir") String orderDir
    ) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return shiftFacade.getObjects(employeeId, startDate, endDate, timeZone, scheduleStatus, returnedFields, offset,
                limit, orderBy, orderDir);
    }
}
