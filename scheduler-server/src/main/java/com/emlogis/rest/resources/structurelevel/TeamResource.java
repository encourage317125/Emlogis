package com.emlogis.rest.resources.structurelevel;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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

import com.amazonaws.services.cloudfront.model.InvalidArgumentException;
import com.emlogis.common.availability.AvailcalViewDto;
import com.emlogis.common.facade.schedule.ShiftStructureFacade;
import com.emlogis.common.facade.structurelevel.StructureLevelFacade;
import com.emlogis.common.facade.structurelevel.TeamFacade;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.security.PermissionCheck;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.contract.dto.ContractDTO;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.employee.dto.*;
import com.emlogis.model.schedule.dto.ScheduleDto;
import com.emlogis.model.schedule.dto.ShiftStructureCreateDto;
import com.emlogis.model.schedule.dto.ShiftStructureDto;
import com.emlogis.model.schedule.dto.ShiftStructureUpdateDto;
import com.emlogis.model.shiftpattern.dto.DailyDemandDto;
import com.emlogis.model.shiftpattern.dto.ExtendedShiftReqDto;
import com.emlogis.model.structurelevel.dto.*;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;

/**
 * Resource for Team Administration.
 * allows listing/viewing/updating/creating/deleting user teams.
 * @author EmLogis
 *
 */
@Path("/teams")
@Authenticated
public class TeamResource extends StructureLevelResource {

    @EJB
    private TeamFacade teamFacade;

    @EJB
    private ShiftStructureFacade shiftStructureFacade;

    @Override
    protected StructureLevelFacade getStructureLevelFacade() {
        return teamFacade;
    }

    /**
     * Get list of Teams
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "List Teams", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<TeamDto> getObjects(
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        AccountACL acl = getAcl();
        return teamFacade.getObjects(tenantId, select, filter, offset, limit, orderBy, orderDir, acl);
    }

    /**
     * Read a Team
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
    @Audited(label = "Get TeamInfo", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public TeamDto getObject(@PathParam("id") final String id) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        checkSiteAndTeamReadAccess(primaryKey);		// check Site & Team Access
        return teamFacade.getObject(primaryKey);
    }

	/**
     * Update a  Team
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Update TeamInfo", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public TeamDto updateObject(@PathParam("id") final String id, TeamUpdateDto teamUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        checkSiteAndTeamWriteAccess(primaryKey);		// check Site & Team Access
        return teamFacade.updateObject(primaryKey, teamUpdateDto);
    }

    /**
     * Creates a team
     * @return
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Create Team", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public TeamDto createObject(TeamCreateDto teamCreateDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey sitePrimaryKey = createPrimaryKey(teamCreateDto.getSiteId());
        // check the parent Site has OrganizationProfileMgmt permission
        // TODO: revisit / doublecheck this behavior. 
        checkAcl(sitePrimaryKey, PermissionCheck.ANY, Permissions.OrganizationProfile_Mgmt);

        PrimaryKey teamPrimaryKey = createUniquePrimaryKey();
        return teamFacade.createObject(sitePrimaryKey, teamPrimaryKey, teamCreateDto);
    }

    /**
     * Delete a team
     * @return
     */
    @POST
    @Path("{id}/ops/softdelete")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Soft Delete Team", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response softDeleteObject(@PathParam("id") final String id) {
    	PrimaryKey primaryKey = createPrimaryKey(id);
        checkSiteAndTeamWriteAccess(primaryKey);		// check Site & Team Access
        
        teamFacade.softDeleteObject(primaryKey);
        return Response.ok().build();
    }

    @DELETE
    @Path("{id}/ops/harddelete")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Hard Delete Team", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response hardDeleteObject(@PathParam("id") final String id) {
    	PrimaryKey primaryKey = createPrimaryKey(id);
        checkSiteAndTeamWriteAccess(primaryKey);		// check Site & Team Access

        teamFacade.hardDeleteObject(primaryKey);
        return Response.ok().build();
    }

    /**
     * Get the list of Skills associated with the Team specified by teamId
     * @param teamId
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    @GET
    @Path("{teamId}/skills")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View})
    @Audited(label = "List Team Skills", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<SkillDto> getSkills(
            @PathParam("teamId") String teamId,
            @QueryParam("select") String select, // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, NoSuchFieldException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException {
        PrimaryKey primaryKey = createPrimaryKey(teamId);
        checkSiteAndTeamReadAccess(primaryKey);		// check Site & Team Access
        return teamFacade.getSkills(primaryKey, select, filter, offset, limit, orderBy, orderDir);
    }

    /**
     * Associates a Skill specified by its Id to the Team.
     * Also adds the Skill to Team's 'parent' Site if it isn't already added.
     * @param teamId
     * @param skillId
     * @return
     */
    @POST
    @Path("{teamId}/ops/addskill")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "AddSkill To Team", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response addSkill(@PathParam("teamId") String teamId,
                             @QueryParam("skillId") String skillId) {
        PrimaryKey teamPrimaryKey = createPrimaryKey(teamId);
        checkSiteAndTeamWriteAccess(teamPrimaryKey);		// check Site & Team Access
        teamFacade.addSkill(teamPrimaryKey, skillId);
        return Response.ok().build();
    }

    /**
     * Removes a Skill specified by its Id from the Team
     * @param teamId
     * @param skillId
     * @return
     */
    @POST
    @Path("{teamId}/ops/removeskill")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "RemoveSkill From Team", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response removeSkill(@PathParam("teamId") String teamId,
                                @QueryParam("skillId") String skillId) {
        PrimaryKey teamPrimaryKey = createPrimaryKey(teamId);
        checkSiteAndTeamWriteAccess(teamPrimaryKey);		// check Site & Team Access
        teamFacade.removeSkill(teamPrimaryKey, skillId);
        return Response.ok().build();
    }

    @GET
    @Path("{teamId}/shiftstructures")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "List Team's ShiftStructure", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ShiftStructureDto> getShiftStructures(
            @PathParam("teamId") String teamId,
            @QueryParam("select") String select, // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, NoSuchFieldException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException {
        PrimaryKey primaryKey = createPrimaryKey(teamId);
        checkSiteAndTeamReadAccess(primaryKey);		// check Site & Team Access
        return teamFacade.getShiftStructures(primaryKey, select, filter, offset, limit, orderBy, orderDir);
    }

    @POST
    @Path("{teamId}/shiftstructures")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Create ShiftStructure", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftStructureDto createShiftStructure(
            @PathParam("teamId") String teamId,
            ShiftStructureCreateDto structureCreateDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        structureCreateDto.setTeamId(teamId);
        PrimaryKey primaryKey = this.createUniquePrimaryKey();
        checkSiteAndTeamWriteAccess(primaryKey);		// check Site & Team Access
        return shiftStructureFacade.createObject(primaryKey, structureCreateDto);
    }

    /**
     * Read one ShiftStructure
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @GET
    @Path("{teamId}/shiftstructures/{shiftStructureId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "Get ShiftStructureInfo", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftStructureDto getShiftStructure(
            @PathParam("teamId") String teamId,
            @PathParam("shiftStructureId") String shiftStructureId) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(shiftStructureId);
        checkSiteAndTeamReadAccess(primaryKey);		// check Site & Team Access
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
    @Path("{teamId}/shiftstructures/{shiftStructureId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Update ShiftStructureInfo", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftStructureDto updateShiftStructure(
            @PathParam("teamId") String teamId,
            @PathParam("shiftStructureId") String shiftStructureId,
            ShiftStructureUpdateDto shiftStructureUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(shiftStructureId);
        checkSiteAndTeamWriteAccess(primaryKey);		// check Site & Team Access
        return shiftStructureFacade.updateObject(primaryKey, shiftStructureUpdateDto);
    }

    @DELETE
    @Path("{teamId}/shiftstructures/{shiftStructureId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Delete ShiftStructure", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response removeShiftStructure(@PathParam("teamId") String teamId,
                                         @PathParam("shiftStructureId") String shiftStructureId) {
        PrimaryKey teamPrimaryKey = createPrimaryKey(teamId);
        teamFacade.removeShiftStructure(teamPrimaryKey, shiftStructureId);
        checkSiteAndTeamWriteAccess(teamPrimaryKey);		// check Site & Team Access
        return Response.ok().build();
    }

    /**
     * Get Team employees
     * @param teamId
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    @GET
    @Path("{teamId}/employees")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
    @Audited(label = "List Team's Employees", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<EmployeeViewDto> getEmployees(
            @PathParam("teamId") String teamId,
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
    	PrimaryKey primaryKey = createPrimaryKey(teamId);
        checkSiteAndTeamReadAccess(primaryKey);		// check Site & Team Access
        return teamFacade.getEmployees(primaryKey, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{teamId}/membership")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.Employee_View})
    @Audited(label = "List of Team membership objects", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<EmployeeTeamViewDto> getTeamMembership(
            @PathParam("teamId") String teamId,
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        PrimaryKey primaryKey = createPrimaryKey(teamId);
        checkSiteAndTeamReadAccess(primaryKey);		// check Site & Team Access
        return teamFacade.getTeamMembership(primaryKey, filter, offset, limit, orderBy, orderDir);
    }

    @POST
    @Path("{teamId}/membership/ops/addemployees")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
    @Audited(label = "Associates employees to a team", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<EmployeeTeamViewDto> addTeamMembership(
            @PathParam("teamId") String teamId, TeamEmployeeCreateDto[] dtos) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            InstantiationException {
        PrimaryKey primaryKey = createPrimaryKey(teamId);
        return teamFacade.addTeamMembership(primaryKey, dtos);
    }

    @POST
    @Path("{teamId}/membership/ops/removeemployees")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
    @Audited(label = "Removes association between a team and specified employees",
            callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response removeTeamMembership(
            @PathParam("teamId") String teamId, String[] employeeIds) {
        PrimaryKey primaryKey = createPrimaryKey(teamId);
        teamFacade.removeTeamMembership(primaryKey, employeeIds);
        return Response.ok().build();
    }

    @GET
    @Path("{teamId}/unassociatedemployees")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
    @Audited(label = "List Team's Unassociated Employees", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<EmployeeViewDto> getUnassociatedTeamEmployees(
            @PathParam("teamId") String teamId,
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        PrimaryKey primaryKey = createPrimaryKey(teamId);
        checkSiteAndTeamReadAccess(primaryKey);		// check Site & Team Access
        return teamFacade.getUnassociatedEmployees(primaryKey, select, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{teamId}/schedules")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
    @Audited(label = "List Team's Schedules", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ScheduleDto> getSchedules(
            @PathParam("teamId") String teamId,
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        PrimaryKey primaryKey = createPrimaryKey(teamId);
        checkSiteAndTeamReadAccess(primaryKey);		// check Site & Team Access
        return teamFacade.getSchedules(primaryKey, select, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{id}/unassociatedskills")
    @Produces(MediaType.APPLICATION_JSON)
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
        checkSiteAndTeamReadAccess(primaryKey);		// check Site & Team Access
        return teamFacade.getUnassociatedSkills(primaryKey, select, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{id}/unassociatedsites")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "List Unassociated Sites", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<SiteDto> getUnassociatedSites(
            @PathParam("id") String id,
            @QueryParam("select") String select, // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        checkSiteAndTeamReadAccess(primaryKey);		// check Site & Team Access
        return teamFacade.getUnassociatedSites(primaryKey, select, filter, offset, limit, orderBy, orderDir);
    }
    
	@GET
	@Path("{teamId}/contracts") 
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
	@Audited(label = "List Employee's Contracts", callCategory = ApiCallCategory.AccountManagement)
	@Interceptors(AuditingInterceptor.class)
	public ResultSetDto<ContractDTO> getContracts(
            @PathParam("teamId") String teamId,
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
		PrimaryKey primaryKey = createPrimaryKey(teamId);
		String tenantId = getTenantId();
        checkSiteAndTeamReadAccess(primaryKey);		// check Site & Team Access
	    return teamFacade.getContracts(primaryKey, tenantId, select, filter, offset, limit, orderBy, orderDir);
	}

	@GET
	@Path("{teamId}/site")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.Employee_View})
	@Audited(label = "Get Site of Team", callCategory = ApiCallCategory.AccountManagement)
	@Interceptors(AuditingInterceptor.class)
	public SiteDto getSite(@PathParam("teamId") String teamId) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
		PrimaryKey teamPrimaryKey = createPrimaryKey(teamId);
        checkSiteAndTeamReadAccess(teamPrimaryKey);		// check Site & Team Access
	    return teamFacade.getSite(teamPrimaryKey);
	}

	@GET
	@Path("{teamId}/ops/query")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.Employee_View})
	@Audited(label = "Employees of Team", callCategory = ApiCallCategory.AccountManagement)
	@Interceptors(AuditingInterceptor.class)
	public ResultSet<EmployeeQueryDto> queryEmployees(
            @PathParam("teamId") String teamId,
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
		PrimaryKey teamPrimaryKey = createPrimaryKey(teamId);
        checkSiteAndTeamReadAccess(teamPrimaryKey);
	    return teamFacade.queryEmployees(teamPrimaryKey, offset, limit, orderBy, orderDir);
	}

    @GET
    @Path("/dailyschedulereport")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Reports_Exe, Permissions.Reports_View})
    @Audited(label = "Daily Schedule Report", callCategory = ApiCallCategory.Qualification)
    @Interceptors(AuditingInterceptor.class)
    public List<Map<String, Object>> dailyScheduleReport(@QueryParam("teamsIds") String teamsIds,
                                                         @QueryParam("startDate") Long startDate,
                                                         @QueryParam("startTime") @DefaultValue("-1") long startTime,
                                                         @QueryParam("endTime") @DefaultValue("-1") long endTime) {
        return teamFacade.dailyScheduleReport(teamsIds, startDate, startTime, endTime);
    }

    @GET
    @Path("/cdavailability")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View})
    @Audited(label = "CD availability time frames in time interval for requested teams",
            callCategory = ApiCallCategory.Qualification)
    @Interceptors(AuditingInterceptor.class)
    public Collection<AvailcalViewDto.AvailCDTimeFrame> cdAvailability(@QueryParam("teamIds") String teamIds,
                                                    @QueryParam("startDate") Long startDate,
                                                    @QueryParam("endDate") Long endDate) {
        List<String> teamIdsList = Arrays.asList(teamIds.split(";"));
        return teamFacade.getCDAvailability(teamIdsList, startDate, endDate);
    }

    @GET
    @Path("/ciavailability")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View})
    @Audited(label = "CI availability time frames for requested teams", callCategory = ApiCallCategory.Qualification)
    @Interceptors(AuditingInterceptor.class)
    public Collection<AvailcalViewDto.AvailCITimeFrame> ciAvailability(@QueryParam("teamIds") String teamIds,
                                                    @QueryParam("startDate") Long startDate,
                                                    @QueryParam("endDate") Long endDate) {
        List<String> teamIdsList = Arrays.asList(teamIds.split(";"));
        if(teamIdsList.size() == 0) {
            throw new InvalidArgumentException("Team ids list is empty!");
        }
        return teamFacade.getCIAvailability(teamIdsList, startDate, endDate);
    }

    @GET
    @Path("/cdpreference")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View})
    @Audited(label = "CD availability time frames in time interval for requested teams",
            callCategory = ApiCallCategory.Qualification)
    @Interceptors(AuditingInterceptor.class)
    public Collection<AvailcalViewDto.PrefCDTimeFrame> cdPreference(@QueryParam("teamIds") String teamIds,
                                                                    @QueryParam("startDate") Long startDate,
                                                                    @QueryParam("endDate") Long endDate) {
        List<String> teamIdsList = Arrays.asList(teamIds.split(";"));
        return teamFacade.getCDPreference(teamIdsList, startDate, endDate);
    }

    @GET
    @Path("/cipreference")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View})
    @Audited(label = "CI availability time frames for requested teams", callCategory = ApiCallCategory.Qualification)
    @Interceptors(AuditingInterceptor.class)
    public Collection<AvailcalViewDto.PrefCITimeFrame> ciPreference(@QueryParam("teamIds") String teamIds,
                                                                    @QueryParam("startDate") Long startDate,
                                                                    @QueryParam("endDate") Long endDate) {
        List<String> teamIdsList = Arrays.asList(teamIds.split(";"));
        if(teamIdsList.size() == 0) {
            throw new InvalidArgumentException("Team ids list is empty!");
        }
        return teamFacade.getCIPreference(teamIdsList, startDate, endDate);
    }

    @GET
    @Path("/employees")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View})
    @Audited(label = "Employees for requested teams", callCategory = ApiCallCategory.Qualification)
    @Interceptors(AuditingInterceptor.class)
    public List<Map<String, Object>> employees(@QueryParam("teamIds") String teamIds) {
        List<String> teamIdsList = Arrays.asList(teamIds.split(";"));
        return teamFacade.getTeamsEmployees(teamIdsList);
    }

    @GET
    @Path("/employeesprofiles")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View})
    @Audited(label = "Employees for requested teams", callCategory = ApiCallCategory.Qualification)
    @Interceptors(AuditingInterceptor.class)
    public List<Map<String, Object>> employeesprofiles(@QueryParam("teamIds") String teamIds) {
        List<String> teamIdsList = Arrays.asList(teamIds.split(";"));
        return teamFacade.getTeamsEmployeesExtended(teamIdsList);
    }

    @GET
    @Path("/skills")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View})
    @Audited(label = "Skills for all requested teams", callCategory = ApiCallCategory.Qualification)
    @Interceptors(AuditingInterceptor.class)
    public List<Map<String, Object>> skills(@QueryParam("teamIds") String teamIds) {
        List<String> teamIdsList = Arrays.asList(teamIds.split(";"));
        return teamFacade.getTeamsSkills(teamIdsList);
    }

    @GET
    @Path("/shiftdemands")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View})
    @Audited(label = "Shift demands for all requested teams and date", callCategory = ApiCallCategory.Qualification)
    @Interceptors(AuditingInterceptor.class)
    public List<DailyDemandDto> shiftDemands(@QueryParam("teamIds") String teamIds, @QueryParam("date") Long date) {
        List<String> teamIdsList = Arrays.asList(teamIds.split(";"));
        return teamFacade.getShiftDemands(teamIdsList, date);
    }

    @GET
    @Path("/shiftrequirements")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View})
    @Audited(label = "Skills for all requested teams", callCategory = ApiCallCategory.Qualification)
    @Interceptors(AuditingInterceptor.class)
    public List<ExtendedShiftReqDto> shiftRequirements(@QueryParam("teamIds") String teamIds,
                                                       @QueryParam("startDate") Long startDate,
                                                       @QueryParam("endDate") Long endDate) {
        List<String> teamIdsList = Arrays.asList(teamIds.split(";"));
        return teamFacade.getShiftRequirements(teamIdsList, startDate, endDate);
    }

    private void checkSiteAndTeamReadAccess(PrimaryKey primaryKey) {
        AccountACL acl = getAcl();
        Permissions[] sitePermissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt};
        Permissions[] teamPermissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt};
        teamFacade.checkSiteAndTeamAcl(primaryKey, acl, PermissionCheck.ANY, sitePermissions, PermissionCheck.ANY,
                teamPermissions);
	}

    private void checkSiteAndTeamWriteAccess(PrimaryKey primaryKey) {
        AccountACL acl = getAcl();
        Permissions[] sitePermissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt};
        Permissions[] teamPermissions = {Permissions.OrganizationProfile_Mgmt};
        teamFacade.checkSiteAndTeamAcl(primaryKey, acl, PermissionCheck.ANY, sitePermissions, PermissionCheck.ANY,
                teamPermissions);
	}

}