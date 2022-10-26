package com.emlogis.rest.resources.schedule;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
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

import com.emlogis.common.facade.schedule.ScheduleFacade;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.security.Permissions;
import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.dto.ScheduleQueryByDayParamDto;
import com.emlogis.model.employee.dto.EmployeeCalendarAvailabilityDto;
import com.emlogis.model.employee.dto.EmployeeDto;
import com.emlogis.model.schedule.QualificationRequestSummary;
import com.emlogis.model.schedule.dto.*;
import com.emlogis.model.schedule.dto.changes.BaseScheduleChangeDto;
import com.emlogis.model.schedule.dto.CandidateShiftEligibleEmployeesDto;
import com.emlogis.model.schedule.dto.changes.ShiftDropChangeDto;
import com.emlogis.model.shiftpattern.dto.ShiftPatternDto;
import com.emlogis.model.structurelevel.dto.TeamDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.auditing.ParametersLogging;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;

import org.apache.commons.lang3.StringUtils;

@Path("/schedules")
@Authenticated
public class ScheduleResource extends BaseResource {

    @EJB
    private ScheduleFacade scheduleFacade;

    public static class ManualShiftOpenAssignParamsDto implements Serializable {
        private String employeeId = null;
        private Boolean force = false;
        private Map<ConstraintOverrideType, Boolean> overrideOptions;

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String id) {
            this.employeeId = id;
        }

        public Boolean getForce() {
            return force;
        }

        public void setForce(Boolean force) {
            this.force = force;
        }

		public Map<ConstraintOverrideType, Boolean> getOverrideOptions() {
			return overrideOptions;
		}

		public void setOverrideOptions(
				Map<ConstraintOverrideType, Boolean> overrideOptions) {
			this.overrideOptions = overrideOptions;
		}
    }

    public static class ManualShiftSwapParamsDto implements Serializable {
        private String shiftBId = null;
        private Boolean force = false;

        public String getShiftBId() {
            return shiftBId;
        }

        public void setShiftBId(String id) {
            this.shiftBId = id;
        }

        public Boolean getForce() {
            return force;
        }

        public void setForce(Boolean force) {
            this.force = force;
        }
    }

    public static class ManualShiftWIPParamsDto implements Serializable {
        private String wipEmployeeId = null;
        private Boolean force = false;

        public String getWipEmployeeId() {
            return wipEmployeeId;
        }

        public void setWipEmployeeId(String id) {
            this.wipEmployeeId = id;
        }

        public Boolean getForce() {
            return force;
        }

        public void setForce(Boolean force) {
            this.force = force;
        }
    }

	public static class CandidateShiftEligibleEmployeesParams implements Serializable {
    	private String teamId;
    	private String skillId;
    	private long startDateTime;
    	private long endDateTime;
    	private Integer maxComputationTime = 60;
    	private Integer maxUnimprovedSecondsSpent = 60;
    	private Integer maxSynchronousWaitSeconds = 120;
    	private Boolean includeDetails =  false;
        private Map<ConstraintOverrideType, Boolean> overrideOptions;

        public String getTeamId() {
            return teamId;
        }

        public void setTeamId(String teamId) {
            this.teamId = teamId;
        }

        public String getSkillId() {
            return skillId;
        }

        public void setSkillId(String skillId) {
            this.skillId = skillId;
        }

        public long getStartDateTime() {
            return startDateTime;
        }

        public void setStartDateTime(long startDateTime) {
            this.startDateTime = startDateTime;
        }

        public long getEndDateTime() {
            return endDateTime;
        }

        public void setEndDateTime(long endDateTime) {
            this.endDateTime = endDateTime;
        }

        public Integer getMaxComputationTime() {
            return maxComputationTime;
        }

        public void setMaxComputationTime(Integer maxComputationTime) {
            this.maxComputationTime = maxComputationTime;
        }

        public Integer getMaxUnimprovedSecondsSpent() {
            return maxUnimprovedSecondsSpent;
        }

        public void setMaxUnimprovedSecondsSpent(Integer maxUnimprovedSecondsSpent) {
            this.maxUnimprovedSecondsSpent = maxUnimprovedSecondsSpent;
        }

        public Integer getMaxSynchronousWaitSeconds() {
            return maxSynchronousWaitSeconds;
        }

        public void setMaxSynchronousWaitSeconds(Integer maxSynchronousWaitSeconds) {
            this.maxSynchronousWaitSeconds = maxSynchronousWaitSeconds;
        }

        public Boolean getIncludeDetails() {
            return includeDetails;
        }

        public void setIncludeDetails(Boolean includeDetails) {
            this.includeDetails = includeDetails;
        }

		public Map<ConstraintOverrideType, Boolean> getOverrideOptions() {
			return overrideOptions;
		}

		public void setOverrideOptions(Map<ConstraintOverrideType, Boolean> overrideOptions) {
			this.overrideOptions = overrideOptions;
		}
    }
	
    /**
     * Get list of Schedules
     *
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View})
    @Audited(label = "List Schedules", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ScheduleDto> getObjects(
            @QueryParam("select") String select,        // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        return scheduleFacade.getObjects(tenantId, select, filter, offset, limit, orderBy, orderDir);
    }

    /**
     * Read one Schedule
     *
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View})
    @Audited(label = "Get ScheduleInfo", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ScheduleDto getObject(@PathParam("id") final String id) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return scheduleFacade.getObject(primaryKey);
    }

    /**
     * Update a  Schedule
     *
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt})
    @Audited(label = "Update ScheduleInfo", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ScheduleDto updateObject(@PathParam("id") final String id, ScheduleUpdateDto scheduleUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return scheduleFacade.updateObject(primaryKey, scheduleUpdateDto);
    }

    /**
     * Creates an Schedule
     *
     * @return Schedule
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt})
    @Audited(label = "Create Schedule", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ScheduleDto createObject(ScheduleCreateDto scheduleCreateDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createUniquePrimaryKey();
        return scheduleFacade.createObject(primaryKey, scheduleCreateDto, this.getUserId());
    }

    /**
     * Delete an Schedule
     *
     * @return Response
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt})
    @Audited(label = "Delete Schedule", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteObject(@PathParam("id") String id) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        scheduleFacade.deleteObject(primaryKey, this.getUserId());
        return Response.ok().build();
    }

    @GET
    @Path("{id}/ops/getsettings")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_View, Permissions.Schedule_Mgmt})
    @Audited(label = "Get Schedule Settings", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ScheduleSettingsDto getSettings(@PathParam("id") String id)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return scheduleFacade.getSettings(createPrimaryKey(id));
    }

    @PUT
    @Path("{id}/ops/setsettings")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_AdvancedMgmt})
    @Audited(label = "Update Schedule Settings", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ScheduleSettingsDto settingsDto(@PathParam("id") String id, ScheduleSettingsDto settingsDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return scheduleFacade.setSettings(createPrimaryKey(id), settingsDto);
    }

    @POST
    @Path("{id}/ops/execute")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt})
    @Audited(label = "Execute Schedule", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ScheduleDto execute(@PathParam("id") String id, ScheduleExecuteDto scheduleExecuteDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return scheduleFacade.execute(primaryKey, scheduleExecuteDto, getUserId());
    }

    @POST
    @Path("{id}/ops/generateshifts")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt})
    @Audited(label = "Generate Shifts", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ScheduleDto generateShifts(@PathParam("id") String id) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return scheduleFacade.generateShifts(primaryKey, getUserId());
    }    

    @POST
    @Path("{id}/ops/abort")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt})
    @Audited(label = "Abort Schedule", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ScheduleDto abort(@PathParam("id") String id, @PathParam("timeout") @DefaultValue("1000") long timeout)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        scheduleFacade.abort(primaryKey, timeout);
        return scheduleFacade.getObject(primaryKey);
    }

    @POST
    @Path("{id}/ops/clone")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt})
    @Audited(label = "Clone Schedule", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ScheduleDto cloneSchedule(@PathParam("id") String id) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return scheduleFacade.cloneSchedule(primaryKey);
    }

    @POST
    @Path("{id}/ops/duplicate")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt})
    @Audited(label = "Duplicate Schedule", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ScheduleDto duplicate(@PathParam("id") String id, ScheduleDuplicateDto duplicateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return scheduleFacade.duplicate(primaryKey, duplicateDto.getName(), duplicateDto.getMode(),
                duplicateDto.getStartDate(), getUserId());
    }

    @POST
    @Path("{id}/ops/promote")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update})
    @Audited(label = "Promote Schedule", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ScheduleDto promote(@PathParam("id") String id)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return scheduleFacade.promote(primaryKey);
    }

    @POST
    @Path("{id}/ops/resetstate")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update})
    @Audited(label = "Reset Schedule State", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ScheduleDto resetState(@PathParam("id") String id)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return scheduleFacade.resetState(primaryKey);
    }
    
    @GET
    @Path("{id}/executionreport")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_View, Permissions.Schedule_Mgmt})
    @Audited(label = "Get Schedule ExecutionReport", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ScheduleReportDto getScheduleReport(@PathParam("id") String scheduleId) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey schedulePrimaryKey = createPrimaryKey(scheduleId);
        return scheduleFacade.getScheduleReport(schedulePrimaryKey);
    }

    /**
     * Get list of ShiftDto
     *
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @GET
    @Path("{id}/shifts")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View, Permissions.Schedule_Mgmt})
    @Audited(label = "Get Shifts", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ShiftDto> getObjects(
            @PathParam("id") String scheduleId,
            @QueryParam("select") String select,        // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey schedulePrimaryKey = createPrimaryKey(scheduleId);
        return scheduleFacade.getShifts(schedulePrimaryKey, select, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{id}/shifts/ops")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View, Permissions.Schedule_Mgmt,
            Permissions.Schedule_Update})
    @Audited(label = "Get Shifts", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<Object[]> getShifts(
            @PathParam("id") String scheduleId,
            @QueryParam("filter") String filter,
            @QueryParam("returnedfields") String returnedFields,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey schedulePrimaryKey = createPrimaryKey(scheduleId);
        return scheduleFacade.getShiftsOps(schedulePrimaryKey, filter, returnedFields, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{id}/shifts/ops/get")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View, Permissions.Schedule_Mgmt,
            Permissions.Schedule_Update})
    @Audited(label = "Get Shifts", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<Object[]> getObjects(
            @PathParam("id") String scheduleId,
            @QueryParam("startdate") long startDate,
            @QueryParam("enddate") long endDate,
            @QueryParam("filter") String filter,
            @QueryParam("returnedfields") String returnedFields) throws InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        PrimaryKey schedulePrimaryKey = createPrimaryKey(scheduleId);
        return scheduleFacade.getShifts(schedulePrimaryKey, startDate, endDate, filter, returnedFields);
    }
    
    /**
     * Get list of changes for Schedule
     *
     * @return ResultSetDto<BaseScheduleChangeDto>
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @GET
    @Path("{id}/changes")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View})
    @Audited(label = "List ScheduleChanges", callCategory = ApiCallCategory.Unclassified)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<BaseScheduleChangeDto> getChanges(
            @PathParam("id") String scheduleId,
            @QueryParam("startdate") long startDate,
            @QueryParam("enddate") long endDate,
            @QueryParam("type") String type,
            @QueryParam("employees") String employees,
            @QueryParam("select") String select,        // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        PrimaryKey schedulePrimaryKey = createPrimaryKey(scheduleId);
        return scheduleFacade.getScheduleChanges(schedulePrimaryKey, startDate, endDate, type, employees, select,
                offset, limit, orderBy, orderDir);
    }

    /**
     * Delete changes by Schedule
     *
     * @return Response
     */
    @DELETE
    @Path("{id}/changes")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt})
    @Audited(label = "Delete Schedule Changes", callCategory = ApiCallCategory.Unclassified)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteChanges(@PathParam("id") String id) {
        PrimaryKey schedulePrimaryKey = createPrimaryKey(id);
        scheduleFacade.deleteScheduleChanges(schedulePrimaryKey);
        return Response.ok().build();
    }

    @GET
    @Path("{id}/teams")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View, Permissions.Schedule_Mgmt})
    @Audited(label = "Get Schedule Teams", callCategory = ApiCallCategory.Unclassified)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<TeamDto> getTeams(
            @PathParam("id") String scheduleId,
            @QueryParam("filter") String filter,
            @QueryParam("select") String select,        // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchMethodException, NoSuchFieldException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        PrimaryKey schedulePrimaryKey = createPrimaryKey(scheduleId);
        return scheduleFacade.getTeams(schedulePrimaryKey, filter, select, offset, limit, orderBy, orderDir);
    }

    @POST
    @Path("{id}/ops/setteams")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_Mgmt})
    @Audited(label = "Set Schedule Teams", callCategory = ApiCallCategory.Unclassified)
    @Interceptors(AuditingInterceptor.class)
    public Collection<SchedulePatternDto> associateTeams(
            @PathParam("id") String scheduleId, String teamIds) throws NoSuchMethodException, NoSuchFieldException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        PrimaryKey schedulePrimaryKey = createPrimaryKey(scheduleId);
        String[] ids;
        if (StringUtils.isNotBlank(teamIds)) {
            ids = teamIds.split(",");
        } else {
            ids = null;
        }
        return scheduleFacade.associateTeams(schedulePrimaryKey, this.getUserId(), ids);
    }

    @GET
    @Path("{id}/employees")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View, Permissions.Schedule_Mgmt})
    @Audited(label = "Get Schedule Employees", callCategory = ApiCallCategory.Unclassified)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<EmployeeDto> getEmployees(
            @PathParam("id") String scheduleId,
            @QueryParam("filter") String filter,
            @QueryParam("select") String select,        // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchMethodException, NoSuchFieldException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        PrimaryKey schedulePrimaryKey = createPrimaryKey(scheduleId);
        return scheduleFacade.getEmployees(schedulePrimaryKey, filter, select, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{id}/shiftstructures")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View, Permissions.Schedule_Mgmt})
    @Audited(label = "Get Schedule ShiftStructures", callCategory = ApiCallCategory.Unclassified)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ShiftStructureDto> getShiftStructures(
            @PathParam("id") String scheduleId,
            @QueryParam("filter") String filter,
            @QueryParam("select") String select,        // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchMethodException, NoSuchFieldException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        PrimaryKey schedulePrimaryKey = createPrimaryKey(scheduleId);
        return scheduleFacade.getShiftStructures(schedulePrimaryKey, filter, select, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{id}/options")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_View, Permissions.Schedule_Mgmt})
    @Audited(label = "Get Schedule Options", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public SchedulingOptionsDto getOptions(@PathParam("id") final String id)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return scheduleFacade.getOptions(primaryKey);
    }

    @PUT
    @Path("{id}/options")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt})
    @Audited(label = "Update Schedule Options", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public SchedulingOptionsDto updateOptions(@PathParam("id") final String id,
                                              SchedulingOptionsDto schedulingOptionsDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return scheduleFacade.updateOptions(primaryKey, schedulingOptionsDto);
    }


    /**
     * Manual shift delete.
     * 
     * TODO: Hickory/Aspen does not support manual deletion of shifts from a schedule.
     *       If this is allowed in Mercury, some consideration should be given to the
     *       behavior.  For example, when and under what circumstances can deletes
     *       be performed?  Can deletes be performed on assigned shifts, or should
     *       validation be added to first check that is it an open shift?  Etcetera.
     *       
     * @param scheduleId
     * @param shiftId
     * @return
     */
    @DELETE
	@Path("{scheduleId}/shifts/{shiftId}")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update,
            Permissions.Shift_Mgmt})
	@Audited(label = "Delete Shift", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public Response manualShiftDelete(@PathParam("scheduleId") String scheduleId,
                                        @PathParam("shiftId") String shiftId){
	    PrimaryKey schedulePk = createPrimaryKey(scheduleId);
	    PrimaryKey shiftPk = createPrimaryKey(shiftId);
	    scheduleFacade.manualShiftDelete(schedulePk, shiftPk,null, this.getUserId(), null);
	    return Response.ok().build();
	}
	
	@POST
	@Path("{scheduleId}/shifts")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update,
            Permissions.Shift_Mgmt})
	@Audited(label = "Create Shift", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public ShiftDto manualShiftCreate(@PathParam("scheduleId") String scheduleId, ShiftCreateDto shiftDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
	    PrimaryKey schedulePk = createPrimaryKey(scheduleId);
	    return scheduleFacade.manualShiftCreate(schedulePk, shiftDto, null, this.getUserId(), null);
	}

    @GET
    @Path("{scheduleId}/shiftdropchanges")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View,  Permissions.Demand_Mgmt})
    @Audited(label = "Create Shift", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ShiftDropChangeDto> getShiftDropChanges(@PathParam("scheduleId") String scheduleId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey schedulePk = createPrimaryKey(scheduleId);
        return scheduleFacade.getShiftDropChanges(schedulePk);
    }
	
	@POST
	@Path("{scheduleId}/ops/createshift")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update,
            Permissions.Shift_Mgmt})
	@Audited(label = "Create Shift With Action", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public ShiftDto shiftCreateWithAction(@PathParam("scheduleId") String scheduleId,
                                          CreateShiftParamsDto createShiftParamsDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
	    PrimaryKey schedulePk = createPrimaryKey(scheduleId);
	    return scheduleFacade.shiftCreate(schedulePk, createShiftParamsDto, null, this.getUserId());
	}

	@POST
	@Path("{scheduleId}/shifts/miigrate")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
	@Audited(label = "Migrate Shift", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public ShiftDto migrateShift(@PathParam("scheduleId") String scheduleId, ShiftMigrateDto shiftDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
	    PrimaryKey schedulePk = createPrimaryKey(scheduleId);
	    return scheduleFacade.migrateShift(schedulePk, shiftDto);
	}

	@PUT
	@Path("{scheduleId}/shifts/{shiftId}")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update,
            Permissions.Shift_Mgmt})
	@Audited(label = "Edit Shift", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public ShiftDto manualShiftEdit(@PathParam("scheduleId") String scheduleId,
                                    @PathParam("shiftId") String shiftId,
                                    ShiftUpdateDto shiftDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
	    PrimaryKey schedulePk = createPrimaryKey(scheduleId);
	    PrimaryKey shiftPk = createPrimaryKey(shiftId);
	    return scheduleFacade.manualShiftEdit(schedulePk, shiftPk, shiftDto, null, this.getUserId(), null);
	}

	@POST
	@Path("{scheduleId}/shifts/{shiftId}/ops/drop")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update,
            Permissions.Shift_Mgmt})
	@Audited(label = "Drop Shift", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public ShiftDto manualShiftDrop(@PathParam("scheduleId") String scheduleId,
                                    @PathParam("shiftId") String shiftId,
                                    String reason) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
	    PrimaryKey schedulePk = createPrimaryKey(scheduleId);
	    PrimaryKey shiftPk = createPrimaryKey(shiftId);
	    return scheduleFacade.manualShiftDrop(schedulePk, shiftPk, null, this.getUserId(), reason);
	}

	/** Assign employee to work open shift IF QUALIFIED.
	 * 
	 * @param scheduleId
	 * @param shiftId
	 * @param employeeId
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@POST
	@Path("{scheduleId}/shifts/{shiftId}/ops/deprecatedassign")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update,
            Permissions.Shift_Mgmt})
	@Audited(label = "Assign Shift", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	@Deprecated
	public QualificationRequestSummary manualShiftOpenAssign(@PathParam("scheduleId") String scheduleId,
                                                             @PathParam("shiftId") String shiftId,
                                                             @QueryParam("force") @DefaultValue("false") boolean force,
                                                             String employeeId) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
	    PrimaryKey schedulePk = createPrimaryKey(scheduleId);
	    PrimaryKey shiftPk = createPrimaryKey(shiftId);
	    PrimaryKey employeePk = createPrimaryKey(employeeId);
	    return scheduleFacade.manualShiftOpenAssign(schedulePk, shiftPk, employeePk, force, null, this.getUserId(), null, null);
	}
	
	/** Assign employee to work in place (WIP) IF QUALIFIED.
	 * 
	 * @param scheduleId
	 * @param shiftId
	 * @param wipEmployeeId
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@POST
	@Path("{scheduleId}/shifts/{shiftId}/ops/deprecatedwip")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update,
            Permissions.Shift_Mgmt})
	@Audited(label = "Work In Place", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	@Deprecated
	public QualificationRequestSummary manualShiftWIP(
            @PathParam("scheduleId") String scheduleId,
			@PathParam("shiftId") String shiftId, String wipEmployeeId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
	    PrimaryKey schedulePk = createPrimaryKey(scheduleId);
	    PrimaryKey shiftPk = createPrimaryKey(shiftId);
	    PrimaryKey employeePk = createPrimaryKey(wipEmployeeId);
	    return scheduleFacade.manualShiftWIP(schedulePk, shiftPk, employeePk, false, null, this.getUserId(), null);
	}
	
	/** Swap shifts IF QUALIFIED.
	 * 
	 * @param scheduleId
	 * @param shiftAId
	 * @param shiftBId
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@POST
	@Path("{scheduleId}/shifts/{shiftId}/ops/deprecatedswap")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update,
            Permissions.Shift_Mgmt})
	@Audited(label = "Swap Shifts", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	@Deprecated
	public QualificationRequestSummary manualShiftSwap(
            @PathParam("scheduleId") String scheduleId,
			@PathParam("shiftId") String shiftAId, String shiftBId)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
	    PrimaryKey schedulePk = createPrimaryKey(scheduleId);
	    PrimaryKey shiftAPk = createPrimaryKey(shiftAId);
	    PrimaryKey shiftBPk = createPrimaryKey(shiftBId);
	    return scheduleFacade.manualShiftSwap(schedulePk, shiftAPk, shiftBPk, false, null, this.getUserId(), null);
	}

	/** Assign employee to work open shift.
	 * 
	 * @param scheduleId
	 * @param shiftId
	 * @param params
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@POST
	@Path("{scheduleId}/shifts/{shiftId}/ops/assign")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update,
            Permissions.Shift_Mgmt})
	@Audited(label = "Assign Shift", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public QualificationRequestSummary manualShiftOpenAssign(@PathParam("scheduleId") String scheduleId,
                                                             @PathParam("shiftId") String shiftId,
                                                             ManualShiftOpenAssignParamsDto params)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
	    PrimaryKey schedulePk = createPrimaryKey(scheduleId);
	    PrimaryKey shiftPk = createPrimaryKey(shiftId);
	    PrimaryKey employeePk = createPrimaryKey(params.getEmployeeId());
	    Boolean force = params.getForce();
	    Map<ConstraintOverrideType, Boolean> overrideOptions = params.getOverrideOptions();
	    return scheduleFacade.manualShiftOpenAssign(schedulePk, shiftPk, employeePk, force, null, this.getUserId(), null, overrideOptions);
	}

	/** Assign employee to work in place (WIP).
	 * 
	 * @param scheduleId
	 * @param shiftId
	 * @param params
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@POST
	@Path("{scheduleId}/shifts/{shiftId}/ops/wip")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update,
            Permissions.Shift_Mgmt})
	@Audited(label = "Work In Place", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public QualificationRequestSummary manualShiftWIP(
	        @PathParam("scheduleId") String scheduleId,
			@PathParam("shiftId") String shiftId, ManualShiftWIPParamsDto params)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
	    PrimaryKey schedulePk = createPrimaryKey(scheduleId);
	    PrimaryKey shiftPk = createPrimaryKey(shiftId);
	    PrimaryKey employeePk = createPrimaryKey(params.getWipEmployeeId());
	    Boolean force = params.getForce();
	    return scheduleFacade.manualShiftWIP(schedulePk, shiftPk, employeePk, force, null, this.getUserId(), null);
	}

	/** Swap shifts.
	 * 
	 * @param scheduleId
	 * @param shiftAId
	 * @param params
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@POST
	@Path("{scheduleId}/shifts/{shiftId}/ops/swap")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update,
            Permissions.Shift_Mgmt})
	@Audited(label = "Swap Shifts", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public QualificationRequestSummary manualShiftSwap(
            @PathParam("scheduleId") String scheduleId,
			@PathParam("shiftId") String shiftAId, ManualShiftSwapParamsDto params)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
	    PrimaryKey schedulePk = createPrimaryKey(scheduleId);
	    PrimaryKey shiftAPk = createPrimaryKey(shiftAId);
	    PrimaryKey shiftBPk = createPrimaryKey(params.getShiftBId());
	    Boolean force = params.getForce();
	    return scheduleFacade.manualShiftSwap(schedulePk, shiftAPk, shiftBPk, force, null, this.getUserId(), null);
	}

	@GET
	@Path("{scheduleId}/shiftpatterns")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_View,  Permissions.Demand_Mgmt})
	@Audited(label = "Get Schedule PatternElts", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public Collection<SchedulePatternDto> getPatternElts(@PathParam("scheduleId") String scheduleId)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
	    PrimaryKey primaryKey = createPrimaryKey(scheduleId);
	    return scheduleFacade.getPatternElts(primaryKey);
	}

	@GET
	@Path("{scheduleId}/applicableshiftpatterns")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_View,  Permissions.Demand_Mgmt})
	@Audited(label = "Get Schedule Applicable ShiftPatterns", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public Collection<ShiftPatternDto> getApplicableShiftPatterns(
            @PathParam("scheduleId") String scheduleId,
            @QueryParam("filter") String filter,
            @QueryParam("select") String select,        // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
	    PrimaryKey primaryKey = createPrimaryKey(scheduleId);
	    return scheduleFacade.getApplicableShiftPatterns(primaryKey, filter, offset, limit, orderBy, orderDir);
	}

	@PUT
	@Path("{scheduleId}/shiftpatterns")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
	@Audited(label = "Update Schedule PatternElts", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public Collection<SchedulePatternDto> updatePatternElts(@PathParam("scheduleId") String scheduleId,
                                                            SchedulePatternFullUpdateDto schedulePatternFullUpdateDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
	    PrimaryKey primaryKey = createPrimaryKey(scheduleId);
	    return scheduleFacade.updatePatternElts(primaryKey, schedulePatternFullUpdateDto);
	}
	
	@PUT
	@Path("{scheduleId}/ops/fullupdate")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
	@Audited(label = "Schedule Full Update", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public ScheduleDto fullUpdate(@PathParam("scheduleId") String scheduleId,
                                  SchedulePatternFullUpdateDto schedulePatternFullUpdateDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
	    PrimaryKey primaryKey = createPrimaryKey(scheduleId);
	    return scheduleFacade.fullUpdate(primaryKey, schedulePatternFullUpdateDto, this.getUserId());
	}

    @Deprecated
    @POST
    @Path("ops/querynew")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Demand_Mgmt, Permissions.Schedule_View})
    @Audited(label = "Schedule query new", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ScheduleWithSiteAndTeamsDto> queryNew(com.emlogis.model.schedule.dto.ScheduleQueryDto queryDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String tenantId = getTenantId();
        AccountACL acl = getAcl();
        return scheduleFacade.query(tenantId, queryDto, acl);
    }

    @POST
    @Path("ops/query")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Demand_Mgmt, Permissions.Schedule_View})
    @Audited(label = "Schedule query", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ScheduleWithSiteAndTeamsDto> query(com.emlogis.model.schedule.dto.ScheduleQueryDto queryDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String tenantId = getTenantId();
        AccountACL acl = getAcl();
        return scheduleFacade.query(tenantId, queryDto, acl);
    }

    @GET
    @Path("{scheduleId}/qualificationrequesttracker/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View, Permissions.Schedule_View, Permissions.Schedule_Mgmt})
    @Audited(label = "Get QualificationRequestTrackerDto", callCategory = ApiCallCategory.Qualification,
            paramsLogging=ParametersLogging.InputOnly)
    @Interceptors(AuditingInterceptor.class)
	public QualificationRequestTrackerDto getQualificationRequestTracker(
			@PathParam("scheduleId") String scheduleId, 
			@PathParam("requestId") String requestId) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException{
	    PrimaryKey schedulePk = createPrimaryKey(scheduleId);
		return scheduleFacade.getQualificationRequestTracker(schedulePk, requestId);
	}

    /** Get result from synchronously processed open shift eligibility execution
	 * @param scheduleId
	 * @param eligibilityExecuteDto
	 * @return requestTracker
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@POST
	@Path("{id}/ops/getopenshifteligibility")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update})
	@Audited(label = "Get Open Shift Eligibility", callCategory = ApiCallCategory.OpenShiftEligibility,
            paramsLogging=ParametersLogging.InputOnly)
	@Interceptors(AuditingInterceptor.class)
	public QualificationRequestTrackerDto getOpenShiftEligibility(@PathParam("id") String scheduleId,
                                                                  OpenShiftEligibilityExecuteDto eligibilityExecuteDto)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		int maxComputationTime = eligibilityExecuteDto.getMaxComputationTime();
		int maxUnimprovedSecondsSpent = eligibilityExecuteDto.getMaximumUnimprovedSecondsSpent();
		int maxSynchronousWaitSeconds = eligibilityExecuteDto.getMaxSynchronousWaitSeconds();

		PrimaryKey schedulePk = createPrimaryKey(scheduleId);
		
		List<String> employeeIds = eligibilityExecuteDto.getEmployeeIds();
		List<String> shiftIds = eligibilityExecuteDto.getShiftIds();
				
		// To avoid extra iterative passes, simply passing these ID collections on to the
		// facade for conversion to PrimaryKeys and JPA entities in single iterative passes.
	    return scheduleFacade.getOpenShiftEligibility(schedulePk, employeeIds, shiftIds,
                maxComputationTime, maxUnimprovedSecondsSpent, maxSynchronousWaitSeconds);
	}


    /** Get result from synchronously processed open shift eligibility execution
	 * @param scheduleId
	 * @param eligibilityExecuteDto
	 * @return requestTracker
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
     * @throws InterruptedException 
	 */
	@POST
	@Path("{id}/ops/getopenshifteligibilitysimple")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update})
	@Audited(label = "Get Open Shift Eligibility", callCategory = ApiCallCategory.OpenShiftEligibility,
            paramsLogging = ParametersLogging.InputOnly)
	@Interceptors(AuditingInterceptor.class)
	public /*OpenShiftEligibilitySimpleResultDto */ Object getOpenShiftEligibilitySimple(
            @PathParam("id") String scheduleId,
            OpenShiftEligibilityExecuteDto eligibilityExecuteDto,
            @QueryParam("asstring") @DefaultValue("false") boolean asstring)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            InterruptedException {
		int maxComputationTime = eligibilityExecuteDto.getMaxComputationTime();
		int maxUnimprovedSecondsSpent = eligibilityExecuteDto.getMaximumUnimprovedSecondsSpent();
		int maxSynchronousWaitSeconds = eligibilityExecuteDto.getMaxSynchronousWaitSeconds();
		Map<ConstraintOverrideType, Boolean> overrideOptions= eligibilityExecuteDto.getOverrideOptions();
		
		// TODO remove this ugly PATCH
		maxComputationTime = maxSynchronousWaitSeconds = 500;

		PrimaryKey schedulePk = createPrimaryKey(scheduleId);
		
		List<String> employeeIds = eligibilityExecuteDto.getEmployeeIds();
		List<String> shiftIds = eligibilityExecuteDto.getShiftIds();
				
		// To avoid extra iterative passes, simply passing these ID collections on to the
		// facade for conversion to PrimaryKeys and JPA entities in single iterative passes.
		OpenShiftEligibilitySimpleResultDto dto = scheduleFacade.getOpenShiftEligibilitySimple(schedulePk, employeeIds,
                shiftIds, maxComputationTime, maxUnimprovedSecondsSpent, maxSynchronousWaitSeconds, overrideOptions);
		if (!asstring) {
			return dto;
		}
		
        //TEMPORARY CODE TO QUICKLY COPY PASTE OPENSHIFTS to POST in Postman
		System.out.println();
		Collection<OpenShiftEligibilitySimpleResultDto.OpenShiftDto> osdtos = dto.getOpenShifts();
		int i = 0;
		int shifts = osdtos.size();		
		StringBuffer sb = new StringBuffer("{\"openShifts\":{\n");
		for (OpenShiftEligibilitySimpleResultDto.OpenShiftDto osdto : osdtos) {
			sb.append("\"" + osdto.getId() + "\":[");
			int j = 0;
			for (OpenShiftEligibilitySimpleResultDto.EligibleEmployeeDto empdto : osdto.getEmployees()) {
				if (j++ > 0) {
                    sb.append(",");
                }
				sb.append("\"" + empdto.getId() + "\"");
			}
			sb.append("]\n");
			if(++i < shifts) {
                sb.append(",");
            }
		}
		sb.append("}\n}");
		return sb.toString();
	}

    /** Get result from synchronously processed qualification execution
	 * @param scheduleId
	 * @param qualificationExecuteDto
	 * @return requestTracker
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@POST
	@Path("{id}/ops/getqualification")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update})
	@Audited(label = "Get Qualification", callCategory = ApiCallCategory.Qualification)
	@Interceptors(AuditingInterceptor.class)
	public QualificationRequestTrackerDto getQualification(@PathParam("id") String scheduleId,
                                                           QualificationExecuteDto qualificationExecuteDto)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		PrimaryKey schedulePk = createPrimaryKey(scheduleId);
		
		// To avoid extra iterative passes, simply passing these ID collections on to the
		// facade for conversion to PrimaryKeys and JPA entities in single iterative passes.
	    return scheduleFacade.getQualification(schedulePk, qualificationExecuteDto);
	}

	/** Fire off execution of open shift eligibility for asynchronous processing.
     * @param scheduleId
     * @param eligibilityExecuteDto
     * @return requestId
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
	@POST
	@Path("{id}/ops/executeopenshifteligibility")
	@Produces(MediaType.TEXT_PLAIN)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update})
	@Audited(label = "Execute Open Shift Eligibility", callCategory = ApiCallCategory.OpenShiftEligibility)
	@Interceptors(AuditingInterceptor.class)
	public String executeOpenShiftEligibility(@PathParam("id") String scheduleId,
                                              OpenShiftEligibilityExecuteDto eligibilityExecuteDto)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		int maxComputationTime = eligibilityExecuteDto.getMaxComputationTime();
		int maxUnimprovedSecondsSpent = eligibilityExecuteDto.getMaximumUnimprovedSecondsSpent();

		PrimaryKey schedulePk = createPrimaryKey(scheduleId);
		
		List<String> employeeIds = eligibilityExecuteDto.getEmployeeIds();
		List<String> shiftIds = eligibilityExecuteDto.getShiftIds();
		
		// To avoid extra iterative passes, simply passing these ID collections on to the
		// facade for conversion to PrimaryKeys and JPA entities in single iterative passes.
	    return scheduleFacade.executeOpenShiftEligibility(schedulePk, employeeIds, shiftIds,
                maxComputationTime, maxUnimprovedSecondsSpent);
		// TODO Return request acceptance status in addition to the requestId already being returned.
	}

    /** Fire off execution of qualification for asynchronous processing.
     * @param scheduleId
     * @param qualificationExecuteDto
     * @return requestId
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
	@POST
	@Path("{id}/ops/executequalification")
	@Produces(MediaType.TEXT_PLAIN)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt})
	@Audited(label = "Execute Qualification", callCategory = ApiCallCategory.Qualification)
	@Interceptors(AuditingInterceptor.class)
	public String executeQualification(@PathParam("id") String scheduleId,
                                       QualificationExecuteDto qualificationExecuteDto)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		PrimaryKey schedulePk = createPrimaryKey(scheduleId);
		
		// To avoid extra iterative passes, simply passing these ID collections on to the
		// facade for conversion to PrimaryKeys and JPA entities in single iterative passes.
	    return scheduleFacade.executeQualification(schedulePk, qualificationExecuteDto);
		// TODO Return request acceptance status in addition to the requestId already being returned.
	}

	@GET
	@Path("/{scheduleId}/summaryreport")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Reports_Exe, Permissions.Reports_View})
	@Audited(label = "Schedule Summary Report", callCategory = ApiCallCategory.Qualification)
	@Interceptors(AuditingInterceptor.class)
	public List<Map<String, Object>> summaryReport(@PathParam("scheduleId") String scheduleId) {
        return scheduleFacade.summaryReport(scheduleId);
	}

	@GET
	@Path("/{scheduleId}/employeesummaryreport")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Reports_Exe, Permissions.Reports_View})
	@Audited(label = "Schedule Summary Report", callCategory = ApiCallCategory.Qualification)
	@Interceptors(AuditingInterceptor.class)
	public List<Map<String, Object>> summaryByEmployeeReport(
            @PathParam("scheduleId") String scheduleId,
            @QueryParam("teamsIds") String teamsIds,
            @QueryParam("employeeTypes") String employeeTypes) {
        return scheduleFacade.summaryByEmployeeReport(scheduleId, teamsIds, employeeTypes);
    }

	@GET
	@Path("/{scheduleId}/hourlystaffingreport")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Reports_Exe, Permissions.Reports_View})
	@Audited(label = "Hourly Staffing Report", callCategory = ApiCallCategory.Qualification)
	@Interceptors(AuditingInterceptor.class)
	public List<Map<String, Object>> hourlyStaffingReport(
            @PathParam("scheduleId") String scheduleId) {
        return scheduleFacade.hourlyStaffingReport(scheduleId);
    }

	@GET
	@Path("/{scheduleId}/headerreport")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Reports_Exe, Permissions.Reports_View})
	@Audited(label = "Schedule Report Header", callCategory = ApiCallCategory.Qualification)
	@Interceptors(AuditingInterceptor.class)
	public List<Map<String, Object>> headerReport(@PathParam("scheduleId") String scheduleId) {
        return scheduleFacade.headerReport(scheduleId);
	}

	@GET
	@Path("/{scheduleId}/overtimereport")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt})
	@Audited(label = "Schedule Report Header", callCategory = ApiCallCategory.Qualification)
	@Interceptors(AuditingInterceptor.class)
	public Map<String, Object> overtimeReport(@PathParam("scheduleId") String scheduleId) {
        PrimaryKey schedulePrimaryKey = createPrimaryKey(scheduleId);
        return scheduleFacade.overtimeReport(schedulePrimaryKey);
	}

	/** Get result from synchronously processed shift swap eligibility execution
	 * @param scheduleId
	 * @param eligibilityExecuteDto
	 * @return requestTracker
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@POST
	@Path("{id}/ops/getshiftswapeligibility")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update})
	@Audited(label = "Get Shift Swap Eligibility", callCategory = ApiCallCategory.ShiftSwapEligibility,
            paramsLogging = ParametersLogging.InputOnly)
	@Interceptors(AuditingInterceptor.class)
	public ShiftSwapEligibilityRequestTrackerDto getShiftSwapEligibility(
            @PathParam("id") String scheduleId, ShiftSwapEligibilityExecuteDto eligibilityExecuteDto)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		int maxComputationTime = eligibilityExecuteDto.getMaxComputationTime();
		int maxUnimprovedSecondsSpent = eligibilityExecuteDto.getMaximumUnimprovedSecondsSpent();
		int maxSynchronousWaitSeconds = eligibilityExecuteDto.getMaxSynchronousWaitSeconds();
	
		PrimaryKey schedulePk = createPrimaryKey(scheduleId);
		
		List<String> swapSeekingShiftIds = eligibilityExecuteDto.getSwapSeekingShifts();
		List<String> swapCandidateShiftIds = eligibilityExecuteDto.getSwapCandidateShifts();
				
		// To avoid extra iterative passes, simply passing these ID collections on to the
		// facade for conversion to PrimaryKeys and JPA entities in single iterative passes.
	    return scheduleFacade.getShiftSwapEligibility(schedulePk, swapSeekingShiftIds, swapCandidateShiftIds, 
	    		maxComputationTime, maxUnimprovedSecondsSpent, maxSynchronousWaitSeconds);
	}

	/** Fire off execution of shift swap eligibility for asynchronous processing.
	 * @param scheduleId
	 * @param eligibilityExecuteDto
	 * @return requestId
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@POST
	@Path("{id}/ops/executeshiftswapeligibility")
	@Produces(MediaType.TEXT_PLAIN)
//	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt}) // Employees must be able to invoke this
	@Audited(label = "Execute Shift Swap Eligibility", callCategory = ApiCallCategory.ShiftSwapEligibility)
	@Interceptors(AuditingInterceptor.class)
	public String executeShiftSwapEligibility(@PathParam("id") String scheduleId,
                                              ShiftSwapEligibilityExecuteDto eligibilityExecuteDto)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		int maxComputationTime = eligibilityExecuteDto.getMaxComputationTime();
		int maxUnimprovedSecondsSpent = eligibilityExecuteDto.getMaximumUnimprovedSecondsSpent();
	
		PrimaryKey schedulePk = createPrimaryKey(scheduleId);
		
		List<String> swapSeekingShiftIds = eligibilityExecuteDto.getSwapSeekingShifts();
		List<String> swapCandidateShiftIds = eligibilityExecuteDto.getSwapCandidateShifts();
		
		// To avoid extra iterative passes, simply passing these ID collections on to the
		// facade for conversion to PrimaryKeys and JPA entities in single iterative passes.
	    return scheduleFacade.executeShiftSwapEligibility(schedulePk, swapSeekingShiftIds, swapCandidateShiftIds, 
	    		maxComputationTime, maxUnimprovedSecondsSpent);
		// TODO Return request acceptance status in addition to the requestId already being returned.
	}

    @GET
    @Path("/{scheduleId}/scheduleview")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View,Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt,
            Permissions.Schedule_View})
    @Audited(label = "Schedule from a manager point of view", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ScheduleViewDto scheduleView(@PathParam("scheduleId") String scheduleId) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        PrimaryKey schedulePrimaryKey = createPrimaryKey(scheduleId);
        return scheduleFacade.scheduleView(schedulePrimaryKey);
    }

    @GET
    @Path("/{scheduleId}/scheduledayview")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View,Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt,
            Permissions.Schedule_View})
    @Audited(label = "Schedule from a manager point of view for a day", callCategory = ApiCallCategory.Qualification)
    @Interceptors(AuditingInterceptor.class)
    public ScheduleDayViewDto scheduleDayView(@PathParam("scheduleId") String scheduleId,
                                              @QueryParam("date") Long date,
                                              @QueryParam("shiftsOnly") @DefaultValue("false") boolean shiftsOnly)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        PrimaryKey schedulePrimaryKey = createPrimaryKey(scheduleId);
        return scheduleFacade.scheduleDayView(schedulePrimaryKey, date, shiftsOnly);
    }

    // Post OpenShifts APIs
    
	@GET
	@Path("{scheduleId}/postedopenshifts")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_View,  Permissions.Demand_Mgmt, Permissions.Schedule_Update})
	@Audited(label = "Get Summarized PostedOpenShifts", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public ResultSetDto<SummarizedPostedOpenShiftDto> getPostedOpenShifts(
			@PathParam("scheduleId") String scheduleId,
			@QueryParam("select") String select,        // select is NOT IMPLEMENTED FOR NOW ..
			@QueryParam("filter") String filter,
			@QueryParam("offset") @DefaultValue("0") int offset,	// Paging is NOT IMPLEMENTED FOR NOW ..
			@QueryParam("limit") @DefaultValue("10000") int limit,	// Paging is NOT IMPLEMENTED FOR NOW ..
			@QueryParam("orderby") String orderBy,					
			@QueryParam("orderdir") String orderDir)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
	    PrimaryKey primaryKey = createPrimaryKey(scheduleId);
	    return scheduleFacade.getPostedOpenShifts(primaryKey, select, filter, offset, limit, orderBy, orderDir);
	}

	@GET
	@Path("{scheduleId}/postedopenshifts/{shiftId}")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_View,  Permissions.Demand_Mgmt, Permissions.Schedule_Update})
	@Audited(label = "Get Summarized PostedOpenShift", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public SummarizedPostedOpenShiftDto getPostedOpenShift(
			@PathParam("scheduleId") String scheduleId,
			@PathParam("shiftId") String shiftId)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
	    PrimaryKey primaryKey = createPrimaryKey(scheduleId);
	    return scheduleFacade.getPostedOpenShift(primaryKey, shiftId);
	}
	
	@GET
	@Path("{scheduleId}/postedopenshifts/raw")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_View,  Permissions.Demand_Mgmt, Permissions.Schedule_Update})
	@Audited(label = "Get Raw PostedOpenShifts", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public ResultSetDto<PostedOpenShiftDto> getPostedOpenShiftsRaw(
			@PathParam("scheduleId") String scheduleId,
			@QueryParam("select") String select,        // select is NOT IMPLEMENTED FOR NOW ..
			@QueryParam("filter") String filter,
			@QueryParam("offset") @DefaultValue("0") int offset,
			@QueryParam("limit") @DefaultValue("20") int limit,
			@QueryParam("orderby") String orderBy,
			@QueryParam("orderdir") String orderDir)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
	    PrimaryKey primaryKey = createPrimaryKey(scheduleId);
	    return scheduleFacade.getPostedOpenShiftsRaw(primaryKey, select, filter, offset, limit, orderBy, orderDir);
	}

    /**
     * postOpenShifts()  Post/Re-Post OpenShifts, and updates the OpenShift table with specified open shifts
	 *
     * @param scheduleId
     * @param openShiftsDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @POST
    @Path("{scheduleId}/postedopenshifts")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update})
    @Audited(label = "Post OpenShifts", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<SummarizedPostedOpenShiftDto> postOpenShifts(@PathParam("scheduleId") String scheduleId,
                                                                     PostOpenShiftDto openShiftsDto)
    	throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(scheduleId);
        return scheduleFacade.postOpenShifts(primaryKey, openShiftsDto);
    }

    @POST
    @Path("{scheduleId}/postedopenshifts/ops/cancelposts")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update})
    @Audited(label = "Cancel Posted OpenShifts", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response cancelPosts(@PathParam("scheduleId") String scheduleId,
                                Map<String, Collection<String>> cancelOpenShiftsMap) {
        PrimaryKey primaryKey = createPrimaryKey(scheduleId);
        scheduleFacade.cancelPosts(primaryKey, cancelOpenShiftsMap);
        return Response.ok().build();
    }

	@GET
	@Path("{scheduleId}/openshifts")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_View,  Permissions.Demand_Mgmt, Permissions.Schedule_Update})
	@Audited(label = "Get Summarized PostedOpenShifts", callCategory = ApiCallCategory.DemandManagement)
	@Interceptors(AuditingInterceptor.class)
	public ResultSetDto<SummarizedPostedOpenShiftDto> getPostedOpenShifts(
			@PathParam("scheduleId") String scheduleId,
            @QueryParam("startdate") @DefaultValue("0") long startDate,
            @QueryParam("enddate") @DefaultValue("0") long endDate)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
	    PrimaryKey primaryKey = createPrimaryKey(scheduleId);
	    return scheduleFacade.getSummarizedOpenShifts(primaryKey, startDate, endDate);
	}

    @POST
    @Path("{scheduleId}/postedopenshifts/ops/posttoall")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update})
    @Audited(label = "Post OpenShifts To All", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public int postAllOpenShifts(@PathParam("scheduleId") String scheduleId, PostAllOpenShiftDto postAllOpenShiftDto)
            throws IllegalAccessException {
        PrimaryKey primaryKey = createPrimaryKey(scheduleId);
        return scheduleFacade.postAllOpenShifts(primaryKey, postAllOpenShiftDto);
    }

    @POST
    @Path("{scheduleId}/shifts/{shiftId}/ops/manage")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update})
    @Audited(label = "Shift Manage API", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response shiftManage(@PathParam("scheduleId") String scheduleId,
                                @PathParam("shiftId") String shiftId,
                                ManageShiftParamsDto manageShiftParamsDto) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        PrimaryKey schedulePrimaryKey = createPrimaryKey(scheduleId);
        PrimaryKey shiftPrimaryKey = createPrimaryKey(shiftId);
        scheduleFacade.shiftManage(schedulePrimaryKey, shiftPrimaryKey, manageShiftParamsDto, null, this.getUserId());
        return Response.ok().build();
    }

	/** Get result from synchronously processed qualification execution against variation
	 *  of existing shift with modified start and/or end times.
	 *  
     * @param scheduleId
     * @param shiftTimeQualificationExecuteDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
	@POST
	@Path("{id}/ops/getshifttimequalification")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update})
	@Audited(label = "Get Qualification of shift with hypothetically changed start and/or end times",
            callCategory = ApiCallCategory.Qualification)
	@Interceptors(AuditingInterceptor.class)
	public QualificationRequestSummary getShiftTimeQualification(@PathParam("id") String scheduleId,
				ShiftTimeQualificationExecuteDto shiftTimeQualificationExecuteDto)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		PrimaryKey schedulePk = createPrimaryKey(scheduleId);
		
		// To avoid extra iterative passes, simply passing these ID collections on to the
		// facade for conversion to PrimaryKeys and JPA entities in single iterative passes.
	    return scheduleFacade.getShiftTimeQualification(schedulePk, shiftTimeQualificationExecuteDto);
	}
	
	/**
	 * Get employees eligible for a proposed (unpersisted) open shift.
	 * @param scheduleId
	 * @param params
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@POST
	@Path("{id}/ops/getproposedopeneshifteligibleemployees")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update})
	@Audited(label = "Request eligible employees for new candidate (unpersisted) shift",
            callCategory = ApiCallCategory.OpenShiftEligibility)
	@Interceptors(AuditingInterceptor.class)
	public CandidateShiftEligibleEmployeesDto getProposedOpenShiftEligibleEmployees(
			@PathParam("id") String scheduleId,
			CandidateShiftEligibleEmployeesParams params)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		PrimaryKey schedulePk = createPrimaryKey(scheduleId);
		PrimaryKey teamPk = createPrimaryKey(params.getTeamId());
		PrimaryKey skillPk = createPrimaryKey(params.getSkillId());
		long startDateTime = params.getStartDateTime();
		long endDateTime = params.getEndDateTime();
		Integer maxComputationTime = params.getMaxComputationTime();
		Integer maxSynchronousWaitSeconds = params.getMaxSynchronousWaitSeconds();
		Integer maxUnimprovedSecondsSpent = params.getMaxUnimprovedSecondsSpent();
		Boolean includeDetails = params.getIncludeDetails();
		Map<ConstraintOverrideType, Boolean> overrideOptions = params.getOverrideOptions();

		return scheduleFacade.getProposedOpenShiftEligibleEmployees(schedulePk, teamPk, skillPk, startDateTime, 
				endDateTime, maxComputationTime, maxSynchronousWaitSeconds, maxUnimprovedSecondsSpent, 
				includeDetails, overrideOptions);
	}

	/** Get result from synchronously processed qualification execution against variation
	 *  of existing shift with new assignment.
	 * 
	 * @param scheduleId
	 * @param overriddenShiftQualExecuteDto
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@POST
	@Path("{id}/ops/getoverriddenshiftqualification")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt, Permissions.Schedule_Update})
	@Audited(label = "Get Qualification of shift with hypothetically changed assignment",
            callCategory = ApiCallCategory.Qualification)
	@Interceptors(AuditingInterceptor.class)
	public QualificationRequestSummary getOverriddenShiftQualification(@PathParam("id") String scheduleId,
			OverriddenShiftQualExecuteDto overriddenShiftQualExecuteDto)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		PrimaryKey schedulePk = createPrimaryKey(scheduleId);
		
		// To avoid extra iterative passes, simply passing these ID collections on to the
		// facade for conversion to PrimaryKeys and JPA entities in single iterative passes.
	    return scheduleFacade.getOverriddenShiftQualification(schedulePk, overriddenShiftQualExecuteDto);
	}

	@POST
	@Path("/ops/querybyday")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Demand_Mgmt, Permissions.Schedule_Mgmt})
	@Audited(label = "Get Schedules By Day", callCategory = ApiCallCategory.Qualification)
	@Interceptors(AuditingInterceptor.class)
	public Collection<ScheduleWithSiteAndTeamsDto> queryByDay(ScheduleQueryByDayParamDto scheduleQueryByDayParamDto)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        AccountACL acl = getAcl();
	    return scheduleFacade.queryByDay(tenantId, scheduleQueryByDayParamDto, acl);
	}

    @GET
    @Path("{scheduleId}/employees/{employeeId}/calendarandavailabilityview")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_View, Permissions.Demand_View})
    @Audited(label = "Get Current Employee Calendar and Availability View For Schedule",
            callCategory = ApiCallCategory.Qualification)
    @Interceptors(AuditingInterceptor.class)
    public EmployeeCalendarAvailabilityDto getCalendarAvailabilityView(
            @PathParam("employeeId") String employeeId,
            @PathParam("scheduleId") String scheduleId,
            @QueryParam("requestinfo") @DefaultValue("false") Boolean requestInfo,
            @QueryParam("startdate") Long startDate,
            @QueryParam("enddate") Long endDate,
            @QueryParam("schedulestatus") @DefaultValue("Posted") String scheduleStatus,
            @QueryParam("returnedfields") String returnedFields,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("-1") int limit,
            @QueryParam("orderby") @DefaultValue("startDateTime") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
        PrimaryKey schedulePrimaryKey = createPrimaryKey(scheduleId);
        return scheduleFacade.getCalendarAvailabilityView(requestInfo, employeePrimaryKey, schedulePrimaryKey,
                startDate, endDate, scheduleStatus, returnedFields, offset, limit, orderBy, orderDir);
    }

}

