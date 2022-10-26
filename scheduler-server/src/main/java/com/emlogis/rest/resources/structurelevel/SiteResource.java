package com.emlogis.rest.resources.structurelevel;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import javax.ejb.EJB;
import javax.interceptor.Interceptors;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.model.employee.AbsenceTypeDeleteResult;
import com.emlogis.model.employee.dto.*;
import com.emlogis.model.schedule.dto.ScheduleDto;
import com.emlogis.model.shiftpattern.dto.*;
import com.emlogis.model.structurelevel.dto.*;

import org.apache.commons.lang3.StringUtils;

import com.emlogis.common.facade.structurelevel.SiteFacade;
import com.emlogis.common.facade.structurelevel.StructureLevelFacade;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.security.PermissionCheck;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.contract.dto.ContractDTO;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.tenant.settings.dto.SchedulingSettingsDto;
import com.emlogis.model.tenant.settings.dto.SchedulingSettingsUpdateDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;
import com.emlogis.rest.security.RequirePermissions;

import org.joda.time.DateTimeZone;

/**
 * Resource for Site Administration.
 * allows listing/viewing/updating/creating/deleting user sites.
 * @author EmLogis
 *
 */
/**
 * @author emlogis
 *
 */
@Path("/sites")
@Authenticated
public class SiteResource extends StructureLevelResource {

    @EJB
    private SiteFacade siteFacade;

	@Override
	protected StructureLevelFacade getStructureLevelFacade() {
		return siteFacade;
	}

    @GET
    @Path("/timezones")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "TimeZone List", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)    
    public Collection<String> getTimeZones() {
        return DateTimeZone.getAvailableIDs();
    }
    
    /**
     * Get list of Sites
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "List Sites", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<SiteDto> getObjects(
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	String tenantId = getTenantId();
        AccountACL acl = getAcl();
        return siteFacade.getObjects(tenantId, select, filter, offset, limit, orderBy, orderDir, acl);
    }

    /**
     * Read a Site
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Get SiteInfo", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public SiteWithOvertimeDto getObject(@PathParam("id") final String id) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	PrimaryKey primaryKey = createPrimaryKey(id);
        checkAcl(primaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_View,
                Permissions.OrganizationProfile_Mgmt);
        return siteFacade.getObject(primaryKey);
    }

    /**
     * Update a  Site
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Update SiteInfo", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public SiteWithOvertimeDto updateObject(@PathParam("id") final String id, SiteUpdateDto siteUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        checkAcl(primaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_Mgmt);
        return siteFacade.updateObject(primaryKey, siteUpdateDto);
    }

    /**
     * Creates a site
     * @return SiteDto
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Create Site", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public SiteWithOvertimeDto createObject(SiteCreateDto siteCreateDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey;
        // for testing reasons, we allow to provide an id via the DTO
        // in production, this option won't be available any more, ids will be generated on backend side
        // TODO, remove this option
        if (StringUtils.isBlank(siteCreateDto.getId())) {
        	// id is not specified (which is preferred), let's generate one
        	primaryKey = createUniquePrimaryKey();
        } else {
        	primaryKey = createPrimaryKey(siteCreateDto.getId());
        }
        siteCreateDto.setId(primaryKey.getId());
        return siteFacade.createObject(primaryKey, siteCreateDto);
    }

    /**
     * Delete a site
     * @return Response
     */
    @POST
    @Path("{id}/ops/softdelete")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Soft Delete Site", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response softDeleteObject(@PathParam("id") final String id) {
        PrimaryKey primaryKey = createPrimaryKey(id);
        // TODO check Site Access if ACL
        checkAcl(primaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_Mgmt);
        siteFacade.softDeleteObject(primaryKey);
		return Response.ok().build();
    }

    @DELETE
    @Path("{id}/ops/harddelete")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Hard Delete Site", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response hardDeleteObject(@PathParam("id") final String id) {
        PrimaryKey primaryKey = createPrimaryKey(id);
        checkAcl(primaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_Mgmt);
        siteFacade.hardDeleteObject(primaryKey);
		return Response.ok().build();
    }

 	@GET
	@Path("{id}/schedulingsettings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt,
            Permissions.Demand_View})
	@Audited(label = "Get Site SchedulingSettings", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
 	public String getSchedulingSettings(@PathParam("id") final String id) throws InstantiationException,
 			IllegalAccessException, NoSuchMethodException, InvocationTargetException, IllegalArgumentException {
        PrimaryKey primaryKey = createPrimaryKey(id);
 		SchedulingSettingsDto dto = siteFacade.getSchedulingSettings(primaryKey);
        return EmlogisUtils.toJsonString(dto);
 	}

	@PUT
	@Path("{id}/schedulingsettings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.Demand_Mgmt})
	@Audited(label = "Set Site SchedulingSettings", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
	public String setSchedulingSettings(@PathParam("id") final String id, String dtoStr)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            IllegalArgumentException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        SchedulingSettingsUpdateDto updateDto  = EmlogisUtils.fromJsonString(dtoStr, SchedulingSettingsUpdateDto.class);
        SchedulingSettingsDto newDto = siteFacade.setSchedulingSettings(primaryKey, updateDto);
        return EmlogisUtils.toJsonString(newDto);
	}

    @GET
    @Path("{siteId}/teams")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "List Site Teams", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<TeamDto> getTeams(
            @PathParam("siteId") String siteId,
            @QueryParam("select") String select, // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, NoSuchFieldException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException {
        PrimaryKey primaryKey = createPrimaryKey(siteId);
        checkAcl(primaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_View,
                Permissions.OrganizationProfile_Mgmt);
        AccountACL acl = getAcl();
        return siteFacade.getTeams(primaryKey, select, filter, offset, limit, orderBy, orderDir, acl);
    }

    @GET
    @Path("{siteId}/teamskills")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "List Site Teams", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<TeamSkillsDto> getTeamsSkills(
            @PathParam("siteId") String siteId,
            @QueryParam("select") String select, // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, NoSuchFieldException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException {
        PrimaryKey primaryKey = createPrimaryKey(siteId);
        checkAcl(primaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_View,
                Permissions.OrganizationProfile_Mgmt);
        AccountACL acl = getAcl();
        return siteFacade.getTeamsSkills(primaryKey, select, filter, offset, limit, orderBy, orderDir, acl);
    }

	/**
	 * Updates the SchedulingSettings of a Site
	 * @param id
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@PUT
	@Path("{id}/schedulingsettings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
	@Audited(label = "Set Site SchedulingSettings", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
	public SchedulingSettingsDto setSchedulingSettings(@PathParam("id") final String id,
                                                       SchedulingSettingsUpdateDto updateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            IllegalArgumentException {
        PrimaryKey primaryKey = createPrimaryKey(id);
		return siteFacade.setSchedulingSettings(primaryKey, updateDto);
	}

	/**
	 * Delete the SchedulingSettings of a Site
	 * @param id
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@DELETE
	@Path("{id}/schedulingsettings")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
	@Audited(label = "Set Site SchedulingSettings", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
	public Response setSchedulingSettings(@PathParam("id") final String id) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, IllegalArgumentException {
        PrimaryKey primaryKey = createPrimaryKey(id);
		return siteFacade.deleteSchedulingSettings(primaryKey) ? Response.ok().build() : Response.serverError().build();
	}

    /**
     * Get the collection of Skills associated to the Site specified by the siteId
     * @param siteId
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    @GET
	@Path("{siteId}/skills")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
	@Audited(label = "List Site Skills", callCategory = ApiCallCategory.OrganizationManagement)
	@Interceptors(AuditingInterceptor.class)
	public ResultSetDto<SkillDto> getSkills(
            @PathParam("siteId") String siteId,
            @QueryParam("select") String select, // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, NoSuchFieldException,
	        IllegalAccessException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException {

    	PrimaryKey primaryKey = createPrimaryKey(siteId);
        checkAcl(primaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt);
	    return siteFacade.getSkills(primaryKey, select, filter, offset, limit, orderBy, orderDir);
	}

    /**
     * Associated a Skill specified by its Id to the Site
     * @param siteId
     * @param skillId
     * @return
     */
	@POST
    @Path("{siteId}/ops/addskill")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "AddSkill To Site", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response addSkill(@PathParam("siteId") String siteId,
                             @QueryParam("skillId") String skillId) {
		
        PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        checkAcl(sitePrimaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_Mgmt);
        siteFacade.addSkill(sitePrimaryKey, skillId);
		return Response.ok().build();
    }

	/**
	 * Remove a Skill specified by its Id from the Site.
	 * 
	 * For now, as in Aspen, propagation doesn't happen automatically. The end
	 * user must first remove the Skill from all of the child Teams before they
	 * can remove the Skill from the Site. Implemented this way for now. 
	 * 
	 * TODO Optionally force propagation of removal to all child Teams.
	 * 
	 * @param siteId
	 * @param skillId
	 * @return
	 * 
	 */
    @POST
    @Path("{siteId}/ops/removeskill")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "RemoveSkill From Site", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
	public Response removeSkill(@PathParam("siteId") String siteId, @QueryParam("skillId") String skillId) {

    	PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        checkAcl(sitePrimaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_Mgmt);
		siteFacade.removeSkill(sitePrimaryKey, skillId);
		return Response.ok().build();
	}

    @GET
    @Path("{id}/unassociatedskills")
    @Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "List Unassociated Skills", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<SkillDto> getUnassociatedSkills(
            @PathParam("id") String id,
            @QueryParam("select") String select, // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {

    	PrimaryKey primaryKey = createPrimaryKey(id);
        checkAcl(primaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt);
        return siteFacade.getUnassociatedSkills(primaryKey, select, filter, offset, limit, orderBy, orderDir);
    }
    
	@GET
	@Path("{siteId}/contracts") 
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
	@Audited(label = "List Site Contracts", callCategory = ApiCallCategory.OrganizationManagement)
	@Interceptors(AuditingInterceptor.class)
	public ResultSetDto<ContractDTO> getContracts(
            @PathParam("siteId") String siteId,
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
		PrimaryKey primaryKey = createPrimaryKey(siteId);
        checkAcl(primaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_View,
                Permissions.OrganizationProfile_Mgmt);
	    return siteFacade.getContracts(primaryKey, select, filter, offset, limit, orderBy, orderDir);
	}

    @GET
    @Path("{siteId}/shifttypes")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Get ShiftTypes Of Site", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ShiftTypeDto> getShiftTypes(
            @PathParam("siteId") String siteId,
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        checkAcl(sitePrimaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_Mgmt,
                Permissions.OrganizationProfile_View);
        return siteFacade.getShiftTypes(sitePrimaryKey, select, filter, offset, limit, orderBy, orderDir);
    }

    @POST
    @Path("{siteId}/shifttypes")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Create ShiftType", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftTypeDto createShiftType(@PathParam("siteId") String siteId, ShiftTypeCreateDto shiftTypeCreateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        return siteFacade.createShiftType(sitePrimaryKey, shiftTypeCreateDto);
    }

    @GET
    @Path("{siteId}/shifttypes/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Get ShiftType", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftTypeDto getShiftType(
            @PathParam("siteId") String siteId,
            @PathParam("id") String id) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        PrimaryKey shiftTypePrimaryKey = createPrimaryKey(id);
        return siteFacade.getShiftType(shiftTypePrimaryKey);
    }

    @PUT
    @Path("{siteId}/shifttypes/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Update ShiftType", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftTypeDto updateShiftType(
            @PathParam("siteId") String siteId,
            @PathParam("id") String id,
            ShiftTypeUpdateDto shiftTypeUpdateDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        PrimaryKey shiftTypePrimaryKey = createPrimaryKey(id);
        return siteFacade.updateShiftType(sitePrimaryKey, shiftTypePrimaryKey, shiftTypeUpdateDto);
    }

    @DELETE
    @Path("{siteId}/shifttypes/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Remove ShiftType", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response removeShiftType(
            @PathParam("siteId") String siteId,
            @PathParam("id") String id) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        PrimaryKey shiftTypePrimaryKey = createPrimaryKey(id);
        checkAcl(sitePrimaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_Mgmt);
        siteFacade.removeShiftType(sitePrimaryKey, shiftTypePrimaryKey);
        return Response.ok().build();
    }

    @GET
    @Path("{siteId}/shiftlengths")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Get ShiftLengths Of Site", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ShiftLengthDto> getShiftLengths(
            @PathParam("siteId") String siteId,
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        checkAcl(sitePrimaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_Mgmt,
                Permissions.OrganizationProfile_View);
        return siteFacade.getShiftLengths(sitePrimaryKey, select, filter, offset, limit, orderBy, orderDir);
    }

    @POST
    @Path("{siteId}/shiftlengths")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Create ShiftLength", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftLengthDto createShiftLength(@PathParam("siteId") String siteId,
                                            ShiftLengthCreateDto shiftLengthCreateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        return siteFacade.createShiftLength(sitePrimaryKey, shiftLengthCreateDto);
    }

    @POST
    @Path("{siteId}/shiftlengths/ops/createmultiple")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Create Multiple ShiftLength", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<ShiftLengthDto> createMultipleShiftLengths(@PathParam("siteId") String siteId,
                                                                 MultipleShiftLengthDto multipleShiftLengthDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        return siteFacade.createMultipleShiftLengths(sitePrimaryKey, multipleShiftLengthDto);
    }

    @GET
    @Path("{siteId}/shiftlengths/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Get ShiftLength", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftLengthDto getShiftLength(
            @PathParam("siteId") String siteId,
            @PathParam("id") String id) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        PrimaryKey shiftLengthPrimaryKey = createPrimaryKey(id);
        return siteFacade.getShiftLength(shiftLengthPrimaryKey);
    }

    @PUT
    @Path("{siteId}/shiftlengths/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Update ShiftLength", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftLengthDto updateShiftLength(
            @PathParam("siteId") String siteId,
            @PathParam("id") String id,
            ShiftLengthUpdateDto shiftLengthUpdateDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        PrimaryKey shiftLengthPrimaryKey = createPrimaryKey(id);
        return siteFacade.updateShiftLength(sitePrimaryKey, shiftLengthPrimaryKey, shiftLengthUpdateDto);
    }

    @DELETE
    @Path("{siteId}/shiftlengths/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Remove ShiftLength", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response removeShiftLength(
            @PathParam("siteId") String siteId,
            @PathParam("id") String id) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        PrimaryKey shiftLengthPrimaryKey = createPrimaryKey(id);
        checkAcl(sitePrimaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_Mgmt);
        siteFacade.removeShiftLength(sitePrimaryKey, shiftLengthPrimaryKey);
        return Response.ok().build();
    }

    @POST
    @Path("{siteId}/shiftlengths/ops/updateactivation")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Update Activation ShiftLength", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<ShiftLengthDto> updateActivationShiftLengths(@PathParam("siteId") String siteId,
                                                                   Map<String, Boolean> updateActivationMap)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        return siteFacade.updateActivationShiftLengths(sitePrimaryKey, updateActivationMap);
    }

    @GET
    @Path("{siteId}/shiftlengths/{id}/shifttypes")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Get ShiftTypes of ShiftLength", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<ShiftTypeDto> getShiftTypesOfShiftLength(
            @PathParam("siteId") String siteId,
            @PathParam("id") String shiftLengthId) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        PrimaryKey shiftLengthPrimaryKey = createPrimaryKey(shiftLengthId);
        checkAcl(sitePrimaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_View,
                Permissions.OrganizationProfile_Mgmt);
        return siteFacade.getShiftTypesOfShiftLength(sitePrimaryKey, shiftLengthPrimaryKey);
    }

    @POST
    @Path("{siteId}/shifttypes/ops/createmultiple")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Create Multiple ShiftTypes", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<ShiftTypeDto> createMultipleShiftTypes(@PathParam("siteId") String siteId,
                                                             MultipleShiftTypeDto multipleShiftTypeDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        return siteFacade.createMultipleShiftTypes(sitePrimaryKey, multipleShiftTypeDto);
    }

    @POST
    @Path("{siteId}/shifttypes/ops/updateactivation")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Update Activation ShiftTypes", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<ShiftTypeDto> updateActivationShiftTypes(@PathParam("siteId") String siteId,
                                                               Map<String, Boolean> updateActivationMap)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        return siteFacade.updateActivationShiftTypes(sitePrimaryKey, updateActivationMap);
    }

    /**
     * Get queried collection of AbsenceTypes
     * @param siteId
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
    @Path("{siteId}/absencetypes")
    @Produces(MediaType.APPLICATION_JSON)
//    No permission needed here to allow all employees to get the list of abscence types
//    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
//            Permissions.Employee_View, Permissions.Availability_RequestMgmt, Permissions.Availability_Request})
    @Audited(label = "List AbsenceTypes", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<AbsenceTypeDto> getAbsenceTypes(
    		@PathParam("siteId") String siteId,
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey siteKey = createPrimaryKey(siteId);
        return siteFacade.getAbsenceTypes(siteKey, select, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{siteId}/dropshiftreasonsandabsencetypes")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "List DropShiftReasons + AbsenceTypes", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<AbsenceTypeDto> getDropShiftReasonsAbsenceTypes(
    		@PathParam("siteId") String siteId,
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey siteKey = createPrimaryKey(siteId);
        return siteFacade.getDropShiftReasonsAbsenceTypes(siteKey, select, filter, offset, limit, orderBy, orderDir);
    }

    /**
     * Get specified a AbsenceType
     * @param id
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @GET
    @Path("{siteId}/absencetypes/{id}")
    @Produces(MediaType.APPLICATION_JSON)
//  No permission needed here to allow all employees to get the list of abscence types    
//    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
//            Permissions.Employee_View, Permissions.Availability_RequestMgmt,Permissions.Availability_Request})
    @Audited(label = "Get AbsenceTypeInfo", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public AbsenceTypeDto getAbsenceType(@PathParam("siteId") final String siteId, @PathParam("id") final String id)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey siteKey = createPrimaryKey(siteId);
    	PrimaryKey primaryKey = createPrimaryKey(id);
//        checkAcl(siteKey, PermissionCheck.ANY, Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
//                Permissions.Employee_View, Permissions.Availability_RequestMgmt,Permissions.Availability_Request);
        return siteFacade.getAbsenceType(primaryKey);
    }

    /**
     * updateAbsenceType
     * @param id
     * @param absenceTypeUpdateDTO
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @PUT
    @Path("{siteId}/absencetypes/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.Availability_RequestMgmt})
    @Audited(label = "Update AbsenceTypeInfo", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public AbsenceTypeDto updateAbsenceType(
            @PathParam("siteId") String siteId,
            @PathParam("id") String id,
            AbsenceTypeUpdateDto absenceTypeUpdateDTO)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	
        PrimaryKey siteKey = createPrimaryKey(siteId);
    	PrimaryKey primaryKey = createPrimaryKey(id);
        checkAcl(siteKey, PermissionCheck.ANY, Permissions.OrganizationProfile_Mgmt, Permissions.Availability_RequestMgmt);
        AbsenceTypeDto dto =  siteFacade.updateAbsenceType(primaryKey, absenceTypeUpdateDTO);
        return dto;
    }

    /**
     * Create absence type
     * @param siteId
     * @param absenceTypeCreateDTO
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @POST
    @Path("{siteId}/absencetypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.Availability_RequestMgmt})
    @Audited(label = "Create AbsenceType", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public AbsenceTypeDto createAbsenceType(@PathParam("siteId") final String siteId, 
    		AbsenceTypeCreateDto absenceTypeCreateDTO) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey siteKey = createPrimaryKey(siteId);
        PrimaryKey primaryKey = createUniquePrimaryKey();
        checkAcl(siteKey, PermissionCheck.ANY, Permissions.OrganizationProfile_Mgmt, Permissions.Availability_RequestMgmt);
        return siteFacade.createAbsenceType(siteKey, primaryKey, absenceTypeCreateDTO);
    }
    
    /**
     * Delete a absenceType
     * @param id
     * @param siteId
     * @return
     */
    @DELETE
    @Path("{siteId}/absencetypes/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.Availability_RequestMgmt})
    @Audited(label = "Delete AbsenceType", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteAbsenceType(@PathParam("siteId") final String siteId, @PathParam("id") final String id) {
        PrimaryKey siteKey = createPrimaryKey(siteId);
        PrimaryKey primaryKey = createPrimaryKey(id);
        checkAcl(siteKey, PermissionCheck.ANY, Permissions.OrganizationProfile_Mgmt,
                Permissions.Availability_RequestMgmt);
        AbsenceTypeDeleteResult result = siteFacade.deleteAbsenceType(siteKey, primaryKey);
		return Response.ok(result).build();
    }

    @GET
    @Path("siteteamskills")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.OrganizationProfile_View})
    @Audited(label = "Site Team Skills", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<QueryDto> siteTeamSkills(
            @QueryParam("search") String searchValue,
            @QueryParam("searchfields") String searchFields,
            @QueryParam("sitefilter") String siteFilter,
            @QueryParam("teamfilter") String teamFilter,
            @QueryParam("skillfilter") String skillFilter) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        AccountACL acl = getAcl();
        return siteFacade.siteTeamSkills(tenantId, searchValue, searchFields, siteFilter, teamFilter, skillFilter, acl);
    }

    @GET
    @Path("ops/siteteams")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.OrganizationProfile_View})
    @Audited(label = "List of Sites and Teams", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<SiteTeamDto> siteTeams() throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        AccountACL acl = getAcl();
        return siteFacade.siteTeams(tenantId, acl);
    }

    @GET
    @Path("ops/siteschedules")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "List of Sites and Schedules", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<SiteScheduleDto> siteSchedules(@QueryParam("startdate") Long startDate)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        AccountACL acl = getAcl();
        return siteFacade.siteSchedules(tenantId, startDate, acl);
    }

    @Deprecated
    @GET
    @Path("{siteId}/siteschedules")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "List Site/Schedules info", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<SiteScheduleDto> schedules(@PathParam("siteId") String siteId,
                                                 @QueryParam("startdate") Long startDate,
                                                 @QueryParam("filter") String filter)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        AccountACL acl = getAcl();
        return siteFacade.schedules(siteId, startDate, acl, filter);
    }

    @Deprecated
    @GET
    @Path("{siteId}/schedules")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "List of Schedules for site", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ScheduleDto> schedulesForSite(
            @PathParam("siteId") String siteId,
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") Integer offset,
            @QueryParam("limit") @DefaultValue("200") Integer limit,	// TODO REPLACE 200 by 20 ONCE UI SPECIFIES THIS PARAM
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        PrimaryKey sitePrimaryKey = new PrimaryKey(getTenantId(), siteId);
        AccountACL acl = getAcl();
        return siteFacade.schedulesForSite(sitePrimaryKey, filter, offset, limit, orderBy, orderDir, acl);
    }

    /**
     * Get the collection of PostOverrides entities associated to the Site specified by the siteId
     * @param siteId
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    @GET
	@Path("{siteId}/postoverrides")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
	@Audited(label = "List Site PostOverrides", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public ResultSetDto<PostOverridesDto> getPostOverrides(
            @PathParam("siteId") String siteId) throws InstantiationException, NoSuchFieldException,
	        IllegalAccessException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException {

    	PrimaryKey primaryKey = createPrimaryKey(siteId);
        checkAcl(primaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt);
	    return siteFacade.getPostOverrides(primaryKey);
	}

    /**
     * Creates a new PostOverrides for the Site
     * @param siteId
     * @param overridesDto
     * @return
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
	@POST
    @Path("{siteId}/postoverrides")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissions(permissions = {Permissions.OrganizationProfile_View, Permissions.Schedule_Update})
    @Audited(label = "AddPostOverrides To Site", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response addPostOverrides(@PathParam("siteId") String siteId, PostOverridesDto overridesDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
        PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        checkAcl(sitePrimaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt);
        siteFacade.createPostOverrides(sitePrimaryKey, overridesDto);
		return Response.ok().build();
    }

	/**
	 * Get a PostOverrides specified by its name from the Site.
	 * 
	 * 
	 * @param siteId
	 * @param name
	 * @return
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * 
	 */
    @GET
    @Path("{siteId}/postoverrides/{name}")
    @Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "GetPostOverrides of Site", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
	public PostOverridesDto getPostOverrides(@PathParam("siteId") String siteId, @PathParam("name") String name)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	
    	PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        checkAcl(sitePrimaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt);
		return siteFacade.getPostOverrides(sitePrimaryKey, name);
	}
    
	/**
	 * Updates a PostOverrides specified by its name from the Site.
	 * 
	 * 
	 * @param siteId
	 * @param name
	 * @param overridesDto
	 * @return
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * 
	 */
    @PUT
    @Path("{siteId}/postoverrides/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissions(permissions = {Permissions.OrganizationProfile_View, Permissions.Schedule_Update})
    @Audited(label = "GetPostOverrides of Site", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
	public PostOverridesDto updatePostOverrides(@PathParam("siteId") String siteId, @PathParam("name") String name,
                                                PostOverridesDto overridesDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	
    	PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        checkAcl(sitePrimaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt);
		return siteFacade.updatePostOverrides(sitePrimaryKey, name, overridesDto);
	}

	/**
	 * Delete a PostOverrides specified by its name from the Site.
	 * 
	 * 
	 * @param siteId
	 * @param name
	 * @return
	 * 
	 */
    @DELETE
    @Path("{siteId}/postoverrides/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissions(permissions = {Permissions.OrganizationProfile_View, Permissions.Schedule_Update})
    @Audited(label = "RemoveSkill From Site", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
	public Response deletePostOverrides(@PathParam("siteId") String siteId, @PathParam("name") String name) {
    	
    	PrimaryKey sitePrimaryKey = createPrimaryKey(siteId);
        checkAcl(sitePrimaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt);
		siteFacade.deletePostOverrides(sitePrimaryKey, name);
		return Response.ok().build();
	}
    
}

