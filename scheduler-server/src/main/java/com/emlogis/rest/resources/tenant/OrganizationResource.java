package com.emlogis.rest.resources.tenant;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.employee.dto.EmployeeRememberMeDto;
import com.emlogis.model.structurelevel.dto.HolidayCreateDto;
import com.emlogis.model.structurelevel.dto.HolidayDto;
import com.emlogis.model.structurelevel.dto.HolidayUpdateDto;
import com.emlogis.model.structurelevel.dto.TeamManagersDto;
import com.emlogis.model.tenant.dto.OrganizationDto;
import com.emlogis.model.tenant.dto.OrganizationUpdateDto;
import com.emlogis.model.tenant.settings.dto.SchedulingSettingsDto;
import com.emlogis.model.tenant.settings.dto.SchedulingSettingsUpdateDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;

import javax.interceptor.Interceptors;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

/**
 * Resource for Customer Operations on its Organization entity.
 *
 *
 */
@Path("/org")
@Authenticated
public class OrganizationResource extends TenantResource {

	/**
	 * Read current Organization
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws Exception
	 */
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View})
	@Audited(label = "Get Customer Info")
    @Interceptors(AuditingInterceptor.class)
	public OrganizationDto getObject() throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        String orgId = getTenantId();
		return getOrganizationFacade().getObject(orgId);
	}

	/**
	 * Update current Organization
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws Exception
	 */
	@PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View})
	@Audited(label = "Set Customer Info")
    @Interceptors(AuditingInterceptor.class)
	public OrganizationDto updateObject(OrganizationUpdateDto orgUpdateDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String orgId = getTenantId();
		return getOrganizationFacade().updateObject(orgId, orgUpdateDto);
	}

	/* 
	 * Holiday	management	
	 */
    @GET
    @Path("/holiday")
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "List of timeOffs")
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<HolidayDto> getHolidays(
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("100") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return getOrganizationFacade().getHolidays(getTenantId(), select, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("/holiday/{holidayId}")
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Get TimeOff info")
    @Interceptors(AuditingInterceptor.class)
    public HolidayDto getHoliday(@PathParam("holidayId") String id) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return getOrganizationFacade().getHoliday(primaryKey);
    }

    @POST
    @Path("/holiday")
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Update TimeOff")
    @Interceptors(AuditingInterceptor.class)
    public HolidayDto createHoliday(HolidayCreateDto timeOffCreateDto) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	String tenantId = getTenantId();
        return getOrganizationFacade().createHoliday(tenantId, timeOffCreateDto);
    }

    @PUT
    @Path("/holiday/{holidayId}")
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Update TimeOff")
    @Interceptors(AuditingInterceptor.class)
    public HolidayDto updateHoliday(@PathParam("holidayId") String id, HolidayUpdateDto timeOffUpdateDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	PrimaryKey primaryKey = createPrimaryKey(id);
        return getOrganizationFacade().updateHoliday(primaryKey, timeOffUpdateDto);
    }

    @DELETE
    @Path("/holiday/{holidayId}")
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Delete TimeOff")
    @Interceptors(AuditingInterceptor.class)
    public Response deleteHoliday(@PathParam("holidayId") String id) {
    	PrimaryKey primaryKey = createPrimaryKey(id);
        boolean success = getOrganizationFacade().deleteHoliday(primaryKey);
        if (success) {
            return Response.ok().build();
        } else {
            return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/holidays/ops/duplicateyear")
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Duplicate Holidays For Year")
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<HolidayDto> duplicateHolidays(Map<String, Integer> yearsMap) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String orgId = getTenantId();
        return getOrganizationFacade().duplicateHolidays(orgId, yearsMap.get("yearFrom"), yearsMap.get("yearTo"));
    }

    /*   
     * temp workaround for method above
     */
 	@GET
 	@Path("/schedulingsettings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
 	@Audited(label = "Get Global SchedulingSettings", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
 	public String getSchedulingSettings() throws InstantiationException,
 			IllegalAccessException, NoSuchMethodException, InvocationTargetException, ValidationException,
 			IllegalArgumentException, IOException {
 		SchedulingSettingsDto dto = getOrganizationFacade().getSchedulingSettings(getTenantId());
        return EmlogisUtils.toJsonString(dto);
 	}

    /*   
     * temp workaround for method above
     */
	@PUT
	@Path("/schedulingsettings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
	@Audited(label = "Set Global SchedulingSettings", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
	public String setSchedulingSettings(String dtoStr)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            ValidationException, IllegalArgumentException, IOException {
        SchedulingSettingsUpdateDto updateDto = EmlogisUtils.fromJsonString(dtoStr, SchedulingSettingsUpdateDto.class);
        SchedulingSettingsDto newDto = getOrganizationFacade().setSchedulingSettings(getTenantId(), updateDto);
        return EmlogisUtils.toJsonString(newDto);
	}

    @GET
	@Path("/ops/getmanagersbyteams")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt,
            Permissions.Support})
	@Audited(label = "Get Managers By Teams", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
	public Collection<TeamManagersDto> getManagersByTeamsNew() {
        String tenantId = getTenantId();
        AccountACL accountAcl = getAcl();
        return getOrganizationFacade().getManagersByTeams(tenantId, accountAcl);
	}

    @GET
	@Path("{tenantId}/remembermesessions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
	@Audited(label = "Get Tenant's RememberMe sessions", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<EmployeeRememberMeDto> getRememberMeObjects(
            @PathParam("tenantId") String tenantId,
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return getOrganizationFacade().getRememberMeObjects(tenantId, filter, offset, limit, orderBy, orderDir);
    }

    @GET
	@Path("summary")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Audited(label = "Get Counters", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Map<String, Integer> getCounters() {
        return getOrganizationFacade().getCounters(getTenantId());
    }

}
