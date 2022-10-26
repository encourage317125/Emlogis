package com.emlogis.rest.resources.employee;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

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

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;

import com.emlogis.common.exceptions.credentials.PasswordViolationException;
import com.emlogis.common.facade.employee.EmployeeFacade;
import com.emlogis.common.facade.schedule.ShiftFacade;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.security.Permissions;
import com.emlogis.common.services.notification.NotificationConfigInfo;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.contract.dto.ContractDTO;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.employee.EmployeeTeamBelonging;
import com.emlogis.model.employee.dto.*;
import com.emlogis.model.notification.MsgDeliveryType;
import com.emlogis.model.structurelevel.dto.SiteDto;
import com.emlogis.model.structurelevel.dto.TeamDto;
import com.emlogis.model.tenant.dto.AccountPictureDto;
import com.emlogis.model.tenant.dto.UserAccountDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;


/**
 * Resource for Employee Administration.
 * allows listing/viewing/updating/creating/deleting user employees.
 * @author EmLogis
 *
 */
@Path("/employees")
@Authenticated
public class EmployeeResource extends BaseResource {

    @EJB
    private EmployeeFacade employeeFacade;

    @EJB
    private ShiftFacade shiftFacade;
    
	public static class WipEligibleEmployeesRequestParamsDto implements Serializable {
    	private String shiftId = null;
    	private Boolean isAsync = false;
    	private Integer maxComputationTime = 60;
    	private Integer maxUnimprovedSecondsSpent = 60;
    	private Integer maxSynchronousWaitSeconds = 120;
    	private Boolean includeDetails =  false;

    	public String getShiftId() {
			return shiftId;
		}
		
    	public void setShiftId(String shiftId) {
			this.shiftId = shiftId;
		}
		
    	public Boolean getIsAsync() {
			return isAsync;
		}
		
    	public void setIsAsync(Boolean isAsync) {
			this.isAsync = isAsync;
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
    }

    public static class SwapElligibleEmployeesRequestParamsDto implements Serializable {
    	private String shiftId = null;
    	private Boolean isAsync = false;
    	private Integer maxComputationTime = 60;
    	private Integer maxUnimprovedSecondsSpent = 60;
    	private Integer maxSynchronousWaitSeconds = 120;
    	private Boolean includeDetails = false;

    	public String getShiftId() {
			return shiftId;
		}
		
    	public void setShiftId(String shiftId) {
			this.shiftId = shiftId;
		}
		
    	public Boolean getIsAsync() {
			return isAsync;
		}
		
    	public void setIsAsync(Boolean isAsync) {
			this.isAsync = isAsync;
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
    }

    /**
     * Get list of Employees
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
    @RequirePermissionIn(permissions = {Permissions.Employee_View})
    @Audited(label = "List Employees", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<EmployeeDto> getObjects(
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        return employeeFacade.getObjects(tenantId, select, filter, offset, limit, orderBy, orderDir);
    }

    /**
     * Read a Employee
     * @param id
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.Employee_View})
    @Audited(label = "Get EmployeeInfo", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public EmployeeWithOvertimeDto getObject(@PathParam("id") final String id) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return employeeFacade.getObject(primaryKey);
    }

    /**
     * Update an Employee
     * @param employeeId
     * @param employeeUpdateDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @PUT
    @Path("{employeeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
    @Audited(label = "Update EmployeeInfo", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public EmployeeWithOvertimeDto updateObject(@PathParam("employeeId") final String employeeId,
                                                EmployeeUpdateDto employeeUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(employeeId);
        return employeeFacade.updateObject(primaryKey, employeeUpdateDto);
    }

    /**
     * Creates an employee
     * @param employeeCreateDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
    @Audited(label = "Create Employee", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public EmployeeWithOvertimeDto createObject(EmployeeCreateDto employeeCreateDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return employeeFacade.createObject(getTenantId(), employeeCreateDto);
    }

    @POST
    @Path("multicreate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
    @Audited(label = "Create Employees", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public int createObjects(Collection<EmployeeCreateDto> employeeCreateDtos)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return employeeFacade.createObjects(getTenantId(), employeeCreateDtos);
    }

    /**
     * Delete an employee. Note that this is a 'soft delete' where employee is marked isDeleted = true and timestamp
     * strings are appended email, employeeIdentifier, and loginName if applicable.
     * 
     * @param id
     * @return
     */
    @POST
    @Path("{id}/ops/softdelete")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
    @Audited(label = "Soft Delete Employee", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response softDeleteObject(@PathParam("id") final String id) {
        PrimaryKey primaryKey = createPrimaryKey(id);
        employeeFacade.softDeleteObject(primaryKey);
		return Response.ok().build();
    }

    /**
     * Get the UserAccount associated to the Employee specified by the employeeId
     * @param employeeId
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    @GET
	@Path("{employeeId}/useraccount")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
	@Audited(label = "Get Employee UserAccount", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public UserAccountDto getUserAccount(@PathParam("employeeId") String employeeId) throws InstantiationException,
	        IllegalAccessException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException {
		PrimaryKey primaryKey = createPrimaryKey(employeeId);
	
	    return employeeFacade.getUserAccount(primaryKey);
	}

    /**
	 * Update the UserAccount associated to the Employee specified by the employeeId
     * @param employeeId
     * @param userAccountDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
	@PUT
	@Path("{employeeId}/useraccount")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
	@Audited(label = "Update Employee UserAccount", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public UserAccountDto updateUserAccount(@PathParam("employeeId") String employeeId, UserAccountDto userAccountDto) 
			throws InstantiationException, IllegalAccessException, InvocationTargetException, 
			NoSuchMethodException, IllegalArgumentException {
		PrimaryKey primaryKey = createPrimaryKey(employeeId);
	
	    return employeeFacade.updateUserAccount(primaryKey, userAccountDto);
	}

	/**
	 * Check that the Notification settings of an employee allows to send him a message.
	 * 	 
	 * @param employeeId
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 */
	@POST
	@Path("{employeeId}/ops/checknotification")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
	@Audited(label = "Check Employee Notification enablement", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public NotificationConfigInfo checkNotificationConfig(
			@PathParam("employeeId") String employeeId,
            @QueryParam("deliverytype") @DefaultValue("EMAIL") String deliveryTypeStr
			) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            IllegalArgumentException, PasswordViolationException {
		PrimaryKey primaryKey = createPrimaryKey(employeeId);
		MsgDeliveryType deliveryType = MsgDeliveryType.valueOf(deliveryTypeStr);
		return employeeFacade.checkNotificationEnabled(primaryKey, deliveryType);
	}


	/**
	 * Check that the Notification associated to the Employee currently logged into the system, allows to send him a message.
	 * 	 
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 */
	@POST
	@Path("ops/checknotification")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.EmployeeProfile_Update})
	@Audited(label = "Check Employee Notification enablement", callCategory = ApiCallCategory.Session)
	@Interceptors(AuditingInterceptor.class)
	public NotificationConfigInfo checkNotificationConfig(
            @QueryParam("deliverytype") @DefaultValue("EMAIL") String deliveryTypeStr
			) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, IllegalArgumentException, PasswordViolationException {
		return checkNotificationConfig(this.getEmployeeId(), deliveryTypeStr);
	}
	
    /**
     * Get the Site associated to the Employee specified by the employeeId
     * 
     * Note that Site can be null, if Employee is not associated to any Team
     * 
     * @param employeeId
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    @GET
	@Path("{employeeId}/site")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View, Permissions.Employee_Mgmt})
	@Audited(label = "Get Employee Site", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public SiteDto getSite(@PathParam("employeeId") String employeeId) throws InstantiationException,
	        IllegalAccessException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException {
	    return employeeFacade.getSite(createPrimaryKey(employeeId));
	}

	/**
	 * Add skill to employee
	 * @param employeeId
	 * @param employeeSkillCreateDto
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@POST
	@Path("{employeeId}/skills")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
	@Audited(label = "Add EmployeeSkill To Employee", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public EmployeeSkillViewDto addEmployeeSkill(@PathParam("employeeId") String employeeId,
                                                 EmployeeSkillCreateDto employeeSkillCreateDto)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
	    return employeeFacade.addEmployeeSkill(employeePrimaryKey, employeeSkillCreateDto);
	}

	/**
	 * Get employee skills
	 * @param employeeId
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 */
	@GET
	@Path("{employeeId}/skills")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
	@Audited(label = "List Employee's EmployeeSkills", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public ResultSetDto<EmployeeSkillViewDto> getEmployeeSkills(
            @PathParam("employeeId") String employeeId,
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
		PrimaryKey primaryKey = createPrimaryKey(employeeId);
	    return employeeFacade.getEmployeeSkills(primaryKey, select, filter, offset, limit, orderBy, orderDir);
	}

	/**
	 * Update employee skill
	 * @param employeeId
	 * @param skillId
	 * @param employeeSkillUpdateDto
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@PUT
	@Path("{employeeId}/skills/{skillId}")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
	@Audited(label = "Update EmployeeSkill for Employee", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public EmployeeSkillViewDto updateEmployeeSkill(@PathParam("employeeId") String employeeId, 
			@PathParam("skillId") String skillId, EmployeeSkillUpdateDto employeeSkillUpdateDto) 
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
		PrimaryKey skillPrimaryKey = createPrimaryKey(skillId);
	    return employeeFacade.updateEmployeeSkill(employeePrimaryKey, skillPrimaryKey, employeeSkillUpdateDto);
	}

	/**
	 * Remove employee skill
	 * @param employeeId
	 * @param skillId
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@DELETE
	@Path("{employeeId}/skills/{skillId}")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
	@Audited(label = "Remove EmployeeSkill for Employee", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public Response removeEmployeeSkill(@PathParam("employeeId") String employeeId, 
			@PathParam("skillId") String skillId) 
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
		PrimaryKey skillPrimaryKey = createPrimaryKey(skillId);
	    employeeFacade.removeEmployeeSkill(employeePrimaryKey, skillPrimaryKey);
	    return Response.ok().build();
	}

	@GET
	@Path("{employeeId}/unassociatedskills")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
	@Audited(label = "List Employee's Unassociated Skills", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public ResultSetDto<SkillDto> getUnassociatedEmployeeSkills(
            @PathParam("employeeId") String employeeId,
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
		PrimaryKey primaryKey = createPrimaryKey(employeeId);
	    return employeeFacade.getUnassociatedEmployeeSkills(primaryKey, select, filter, offset, limit, orderBy,
                orderDir);
	}

	/**
	 * Add employee team.  Note that since there must always be a home
	 * team, if this is the first team to be added then it will be made 
	 * the home team implicitly (regardless of provided DTO's isHomeTeam 
	 * setting).
	 * @param employeeId
	 * @param employeeTeamCreateDto
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@POST
	@Path("{employeeId}/teams")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
	@Audited(label = "Add EmployeeTeam To Employee", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public EmployeeTeamViewDto addEmployeeTeam(@PathParam("employeeId") String employeeId,
                                               EmployeeTeamCreateDto employeeTeamCreateDto)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
	    return employeeFacade.addEmployeeTeam(employeePrimaryKey, employeeTeamCreateDto);
	}

	/**
	 * Get employee teams
	 * @param employeeId
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 */
	@GET
	@Path("{employeeId}/teams")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
	@Audited(label = "List Employee's EmployeeTeams", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public ResultSetDto<EmployeeTeamViewDto> getEmployeeTeams(
            @PathParam("employeeId") String employeeId,
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
		PrimaryKey primaryKey = createPrimaryKey(employeeId);
	    return employeeFacade.getEmployeeTeams(primaryKey, select, filter, offset, limit, orderBy, orderDir);
	}

	@GET
	@Path("{employeeId}/unassociatedteams")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
	@Audited(label = "List Employee's Unassociated Teams", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public ResultSetDto<TeamDto> getUnassociatedEmployeeTeams(
            @PathParam("employeeId") String employeeId,
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
		PrimaryKey primaryKey = createPrimaryKey(employeeId);
	    return employeeFacade.getUnassociatedEmployeeTeams(primaryKey, select, filter, offset, limit, orderBy,
                orderDir);
	}

	/**
	 * Update employee team.  Note that since there must always be a
	 * home team, the update will not be allowed if it would have 
	 * resulted in there being no home team designation.
	 * @param employeeId
	 * @param teamId
	 * @param employeeTeamUpdateDto
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@PUT
	@Path("{employeeId}/teams/{teamId}")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
	@Audited(label = "Update EmployeeTeam for Employee", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public EmployeeTeamViewDto updateEmployeeTeam(@PathParam("employeeId") String employeeId, 
			@PathParam("teamId") String teamId, EmployeeTeamUpdateDto employeeTeamUpdateDto) 
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
		PrimaryKey teamPrimaryKey = createPrimaryKey(teamId);
	    return employeeFacade.updateEmployeeTeam(employeePrimaryKey, teamPrimaryKey, employeeTeamUpdateDto);
	}

	/**
	 * Remove employee team.  Note that since there must always be a
	 * home team, the removal will not be allowed if it would have 
	 * resulted in there being no home team designation.
	 * @param employeeId
	 * @param teamId
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@DELETE
	@Path("{employeeId}/teams/{teamId}")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
	@Audited(label = "Remove EmployeeTeam for Employee", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public Response removeEmployeeTeam(@PathParam("employeeId") String employeeId, 
			@PathParam("teamId") String teamId) 
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
		PrimaryKey teamPrimaryKey = createPrimaryKey(teamId);
	    employeeFacade.removeEmployeeTeam(employeePrimaryKey, teamPrimaryKey);
	    return Response.ok().build();
	}
	
	@GET
	@Path("{employeeId}/contracts") 
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
	@Audited(label = "List Employee's Contracts", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public ResultSetDto<ContractDTO> getContracts(
            @PathParam("employeeId") String employeeId,
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
		PrimaryKey primaryKey = createPrimaryKey(employeeId);
		String tenantId = getTenantId();
	    return employeeFacade.getContracts(primaryKey, tenantId, select, filter, offset, limit, orderBy, orderDir);
	}

	/**
     * Get specified the CDAvailabilityTimeFrames for an Employee
	 * @param employeeId
	 * @param select
	 * @param filter
	 * @param offset
	 * @param limit
	 * @param orderBy
	 * @param orderDir
	 * @param startDateTime
	 * @param endDateTime
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
    @GET
    @Path("{employeeId}/cdavailability")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
            Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.Availability_RequestMgmt,
            Permissions.Availability_Request})
    @Audited(label = "Get CDAvailabilityTimeFrame", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<CDAvailabilityTimeFrameDto> getCDAvailabilityTimeFrames(
            @PathParam("employeeId") final String employeeId,
            @QueryParam("select") String select,
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir, 
            @QueryParam("startdatetime") Long startDateTime, 
            @QueryParam("enddatetime") Long endDateTime) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(employeeId);
        String tenantId = primaryKey.getTenantId();

        return employeeFacade.getCDAvailabilityTimeFrames(tenantId, employeeId, select, filter, offset, limit, orderBy,
                orderDir, startDateTime, endDateTime);
    }
	
    /**
     * Get specified a CDAvailabilityTimeFrame
     * @param id
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @GET
    @Path("{employeeId}/cdavailability/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
            Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.Availability_RequestMgmt,
            Permissions.Availability_Request})
    @Audited(label = "Get CDAvailabilityTimeFrame", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public CDAvailabilityTimeFrameDto getCDAvailabilityTimeFrame(@PathParam("id") final String id)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return employeeFacade.getCDAvailabilityTimeFrame(primaryKey);
    }
	
    /**
     * Creates a CDAvailabilityTimeFrame
     * @param employeeId
     * @param cdAvailabilityTimeFrameCreateDTO
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @POST
    @Path("{employeeId}/cdavailability")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
            Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.Availability_RequestMgmt})
    @Audited(label = "Create CDAvailabilityTimeFrame", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public CDAvailabilityTimeFrameDto createCDAvailabilityTimeFrame(
            @PathParam("employeeId") String employeeId,
            CDAvailabilityTimeFrameCreateDto cdAvailabilityTimeFrameCreateDTO) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createUniquePrimaryKey();
        
        cdAvailabilityTimeFrameCreateDTO.setEmployeeId(employeeId);
        
        return employeeFacade.createCDAvailabilityTimeFrame(primaryKey, cdAvailabilityTimeFrameCreateDTO);
    }
    
    /**
     * Delete a CDAvailabilityTimeFrame
     * @param id
     * @return
     */
    @DELETE
    @Path("{employeeId}/cdavailability/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
            Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.Availability_RequestMgmt})
    @Audited(label = "Delete CDAvailabilityTimeFrame", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteCDAvailabilityTimeFrame(@PathParam("id") final String id) {
        PrimaryKey primaryKey = createPrimaryKey(id);
        employeeFacade.deleteCDAvailabilityTimeFrame(primaryKey);
		return Response.ok().build();
    }
	
    /**
     * Get specified the CIAvailabilityTimeFrames for an Employee
     * @param employeeId
     * @param select
     * @param filter
     * @param offset
     * @param limit
     * @param orderBy
     * @param orderDir
     * @param startDateTime
     * @param endDateTime
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @GET
    @Path("{employeeId}/ciavailability")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
            Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.Availability_RequestMgmt,
            Permissions.Availability_Request})
    @Audited(label = "Get CIAvailabilityTimeFrame", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<CIAvailabilityTimeFrameDto> getCIAvailabilityTimeFrames(
            @PathParam("employeeId") final String employeeId,
            @QueryParam("select") String select,
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir, 
            @QueryParam("startDateTime") Long startDateTime, 
            @QueryParam("endDateTime") Long endDateTime) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(employeeId);
        String tenantId = primaryKey.getTenantId();

        return employeeFacade.getCIAvailabilityTimeFrames(tenantId, employeeId, select, filter, offset, limit, orderBy,
                orderDir, startDateTime, endDateTime);
    }
	
    /**
     * Get specified a CIAvailabilityTimeFrame
     * @param id
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @GET
    @Path("{employeeId}/ciavailability/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
            Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.Availability_RequestMgmt,
            Permissions.Availability_Request})
    @Audited(label = "Get CIAvailabilityTimeFrame", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public CIAvailabilityTimeFrameDto getCIAvailabilityTimeFrame(@PathParam("id") final String id)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return employeeFacade.getCIAvailabilityTimeFrame(primaryKey);
    }
	
    /**
     * Creates a CIAvailabilityTimeFrame
     * @param employeeId
     * @param ciAvailabilityTimeFrameCreateDTO
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @POST
    @Path("{employeeId}/ciavailability")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
            Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.Availability_RequestMgmt})
    @Audited(label = "Create CIAvailabilityTimeFrame", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public CIAvailabilityTimeFrameDto createCIAvailabilityTimeFrame(
            @PathParam("employeeId") String employeeId,
            CIAvailabilityTimeFrameCreateDto ciAvailabilityTimeFrameCreateDTO) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createUniquePrimaryKey();
        
        ciAvailabilityTimeFrameCreateDTO.setEmployeeId(employeeId);
        
        return employeeFacade.createCIAvailabilityTimeFrame(primaryKey, ciAvailabilityTimeFrameCreateDTO);
    }
    
    /**
     * Delete a CIAvailabilityTimeFrame
     * @param id
     * @return
     */
    @DELETE
    @Path("{employeeId}/ciavailability/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
            Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.Availability_RequestMgmt})
    @Audited(label = "Delete CIAvailabilityTimeFrame", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteCIAvailabilityTimeFrame(@PathParam("id") final String id) {
        PrimaryKey primaryKey = createPrimaryKey(id);
        employeeFacade.deleteCIAvailabilityTimeFrame(primaryKey);
		return Response.ok().build();
    }

    @GET
    @Path("ops/quicksearch")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_View})
    @Audited(label = "Employees quick search", callCategory = ApiCallCategory.EmployeeManagement)
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
        AccountACL acl = getAcl();
        return employeeFacade.quickSearch(tenantId, searchValue, searchFields, returnedFields, limit, orderBy,
                orderDir, acl);
    }

    @GET
    @Path("ops/query")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_View})
    @Audited(label = "Employees query", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<EmployeeJoinsDto> query(
            @QueryParam("teamids") String teamIds,
            @QueryParam("search") String searchValue,
            @QueryParam("searchfields") String searchFields,
            @QueryParam("skillfilter") String skillFilter,
            @QueryParam("skillownershipfilter") String skillOwnershipFilter,
            @QueryParam("teamfilter") String teamFilter,
            @QueryParam("teammembershipfilter") String teamMembershipFilter,
            @QueryParam("accountfilter") String accountFilter,
            @QueryParam("activitytypefilter") String activityTypeFilter,
            @QueryParam("employeenamefilter") String employeeNameFilter,
            @QueryParam("belonging") @DefaultValue("Both") EmployeeTeamBelonging belonging,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        AccountACL acl = getAcl();
        return employeeFacade.query(tenantId, teamIds, searchValue, searchFields, skillFilter, skillOwnershipFilter,
                teamFilter, teamMembershipFilter, accountFilter, activityTypeFilter, employeeNameFilter,
                belonging, offset, limit, orderBy, orderDir, acl);
    }

    /**
     * Get list of Shift
     */
    @GET
    @Path("{employeeId}/shifts")
    @Produces(MediaType.APPLICATION_JSON)
// commented out temporarily until we clarify security for this API
//  @RequirePermissionIn(permissions = {Permissions.Demand_View})
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
            @QueryParam("orderdir") String orderDir) throws InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        return shiftFacade.getObjects(employeeId, startDate, endDate, timeZone, scheduleStatus, returnedFields, offset,
                limit, orderBy, orderDir);
    }

    @GET
    @Path("{employeeId}/ops/managers")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
    @Audited(label = "Select managers for employee", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<String> managers(@PathParam("employeeId") String employeeId) {
        PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
        return employeeFacade.managers(employeePrimaryKey);
    }

    @GET
    @Path("{employeeId}/ops/manageraccounts")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
    @Audited(label = "Select manager's account ids for employee", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<String> managerAccountIds(@PathParam("employeeId") String employeeId) {
        PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
        return employeeFacade.managerAccountIds(employeePrimaryKey);
    }

    @GET
    @Path("{employeeId}/ops/manageraccountsnew")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
    @Audited(label = "Select manager's account ids for employee", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<String> managerAccountIdsNew(@PathParam("employeeId") String employeeId) {
        PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
        return employeeFacade.managerAccountIds(employeePrimaryKey);
    }

	@GET
	@Path("info")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Calling Employee's Info Summary", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public EmployeeInfoDto getEmployeeInfo() 
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
	    return employeeFacade.getEmployeeInfo(createPrimaryKey(this.getEmployeeId()));
	}

	@GET
	@Path("{employeeId}/info")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Specified Employee's Info Summary", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public EmployeeInfoDto getEmployeeInfo(@PathParam("employeeId") String employeeId)
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
	    return employeeFacade.getEmployeeInfo(createPrimaryKey(employeeId));
	}

	@GET
	@Path("availability")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Calling Employee's Availability Summary", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public EmployeeAvailabilityDto getEmployeeAvailability(
	            @QueryParam("startdate") Long startDate,
	            @QueryParam("enddate") Long endDate,
	            @QueryParam("schedulestatus") @DefaultValue("Posted") String scheduleStatus,
	            @QueryParam("timezone") String timeZone) 
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
		
	    return getEmployeeAvailability(this.getEmployeeId(), startDate, endDate, scheduleStatus, timeZone);
	}

	@GET
	@Path("{employeeId}/availability")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Specified Employee Availability Summary", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public EmployeeAvailabilityDto getEmployeeAvailability(
				@PathParam("employeeId") String employeeId, 
				@QueryParam("startdate") Long startDate,
	            @QueryParam("enddate") Long endDate,
	            @QueryParam("schedulestatus") @DefaultValue("Posted") String scheduleStatus,
	            @QueryParam("timezone") String timeZone) 
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
		
		PrimaryKey employeePk = createPrimaryKey(employeeId);
		DateTimeZone tz = null;
		if (! StringUtils.isBlank(timeZone)) {
			tz = DateTimeZone.forID(timeZone);
		}
	    return employeeFacade.getEmployeeAvailability(employeePk, startDate, endDate, scheduleStatus, tz);
	}


	@GET
	@Path("postedopenshifts")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Employee OpenShifts", callCategory = ApiCallCategory.Unclassified)
	@Interceptors(AuditingInterceptor.class)
	public Collection<EmployeeOpenShiftDto> getOpenShifts(
				@PathParam("employeeId") String employeeId, 
	            @QueryParam("startdate") @DefaultValue("0") Long startDate,
	            @QueryParam("enddate") @DefaultValue("0") Long endDate)
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
	    return employeeFacade.getEmployeeOpenShifts(createPrimaryKey(getEmployeeId()), startDate, endDate);
	}
	
	@GET
	@Path("{employeeId}/postedopenshifts")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Employee OpenShifts", callCategory = ApiCallCategory.Unclassified)
	@Interceptors(AuditingInterceptor.class)
	public Collection<EmployeeOpenShiftDto> getEmployeeOpenShifts(
				@PathParam("employeeId") String employeeId, 
	            @QueryParam("startdate") @DefaultValue("0") Long startDate,
	            @QueryParam("enddate") @DefaultValue("0") Long endDate)
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
	    return employeeFacade.getEmployeeOpenShifts(createPrimaryKey(employeeId), startDate, endDate);
	}

	@GET
	@Path("calendarview")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Current Employee Calendar View", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public EmployeeAvailabilityDto getCalendarView(
				@QueryParam("startdate") Long startDate,
	            @QueryParam("enddate") Long endDate,
	            @QueryParam("schedulestatus") @DefaultValue("Posted") String scheduleStatus,
	            @QueryParam("timezone") @DefaultValue("UTC") String timeZone,
                @QueryParam("returnedfields") String returnedFields,
				@QueryParam("requestinfo") @DefaultValue("true") Boolean requestInfo,
                @QueryParam("offset") @DefaultValue("0") int offset,
                @QueryParam("limit") @DefaultValue("-1") int limit,
                @QueryParam("orderby") @DefaultValue("startDateTime") String orderBy,
                @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
		PrimaryKey employeePk = createPrimaryKey(this.getEmployeeId());
	    return employeeFacade.getCalendarView(requestInfo, employeePk, startDate, endDate, scheduleStatus, timeZone,
                returnedFields, offset, limit, orderBy, orderDir, EmployeeAvailabilityAndShiftsDto.class);
	}

	@GET
	@Path("{employeeId}/calendarview")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Specified Employee Calendar View", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public EmployeeAvailabilityDto getCalendarView(
				@PathParam("employeeId") String employeeId,
				@QueryParam("startdate") Long startDate,
	            @QueryParam("enddate") Long endDate,
	            @QueryParam("schedulestatus") @DefaultValue("Posted") String scheduleStatus,
	            @QueryParam("timezone") @DefaultValue("UTC") String timeZone,
                @QueryParam("returnedfields") String returnedFields,
                @QueryParam("requestinfo") @DefaultValue("true") Boolean requestInfo,
                @QueryParam("offset") @DefaultValue("0") int offset,
                @QueryParam("limit") @DefaultValue("-1") int limit,
                @QueryParam("orderby") @DefaultValue("startDateTime") String orderBy,
                @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
		PrimaryKey employeePk = new PrimaryKey(this.getTenantId(), employeeId);
	    return employeeFacade.getCalendarView(requestInfo, employeePk, startDate, endDate, scheduleStatus, timeZone,
                returnedFields, offset, limit, orderBy, orderDir, EmployeeAvailabilityAndShiftsDto.class);
	}

    @GET
	@Path("calendarandavailabilityview")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Current Employee Calendar and Availability View",
            callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public EmployeeCalendarAvailabilityDto getCalendarAvailabilityView(
				@QueryParam("requestinfo") @DefaultValue("false") Boolean requestInfo,
				@QueryParam("startdate") Long startDate,
	            @QueryParam("enddate") Long endDate,
	            @QueryParam("schedulestatus") @DefaultValue("Posted") String scheduleStatus,
	            @QueryParam("timezone") @DefaultValue("UTC") String timeZone,
                @QueryParam("returnedfields") String returnedFields,
                @QueryParam("offset") @DefaultValue("0") int offset,
                @QueryParam("limit") @DefaultValue("-1") int limit,
                @QueryParam("orderby") @DefaultValue("startDateTime") String orderBy,
                @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
		PrimaryKey employeePk = createPrimaryKey(this.getEmployeeId());
	    return employeeFacade.getCalendarAvailabilityView(requestInfo, employeePk, startDate, endDate, scheduleStatus,
                timeZone, returnedFields, offset, limit, orderBy, orderDir);
	}

	@GET
	@Path("{employeeId}/calendarandavailabilityview")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Specified Employee Calendar and Availability View",
            callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public EmployeeCalendarAvailabilityDto getCalendarAvailabilityView(
                @QueryParam("requestinfo") @DefaultValue("false") Boolean requestInfo,
				@PathParam("employeeId") String employeeId,
				@QueryParam("startdate") Long startDate,
	            @QueryParam("enddate") Long endDate,
	            @QueryParam("schedulestatus") @DefaultValue("Posted") String scheduleStatus,
	            @QueryParam("timezone") @DefaultValue("UTC") String timeZone,
                @QueryParam("returnedfields") String returnedFields,
                @QueryParam("offset") @DefaultValue("0") int offset,
                @QueryParam("limit") @DefaultValue("-1") int limit,
                @QueryParam("orderby") @DefaultValue("startDateTime") String orderBy,
                @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
		PrimaryKey employeePk = new PrimaryKey(this.getTenantId(), employeeId);
	    return employeeFacade.getCalendarAvailabilityView(requestInfo, employeePk, startDate, endDate, scheduleStatus,
                timeZone, returnedFields, offset, limit, orderBy, orderDir);
	}

	/**
	 * Request WIP eligible employees for specified employee/shift
	 * 
	 * @param employeeId
	 * @param params
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@POST
	@Path("{employeeid}/ops/getwipeligibleemployees")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View}) // TODO?
	@Audited(label = "Request WIP eligible employees for specified employee/shift",
            callCategory = ApiCallCategory.OpenShiftEligibility) // TODO?
	@Interceptors(AuditingInterceptor.class)
	public WipEligibleTeammatesDto requestWipEligibleEmployees(
			@PathParam("employeeid") String employeeId, 
	        WipEligibleEmployeesRequestParamsDto params)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String shiftId = params.getShiftId();
		Boolean isAsync = params.getIsAsync();
        Integer maxComputationTime = params.getMaxComputationTime();
        Integer maxUnimprovedSecondsSpent = params.getMaxUnimprovedSecondsSpent();
        Integer maxSynchronousWaitSeconds = params.getMaxSynchronousWaitSeconds();
        Boolean includeDetails = params.getIncludeDetails();

		PrimaryKey shiftPk = createPrimaryKey(shiftId);
	
		if (isAsync) {
		    return employeeFacade.requestWipEligibleEmployeesAsynchronously(shiftPk, employeeId, 
		    		maxComputationTime, maxUnimprovedSecondsSpent, includeDetails);	// maxSynchronousWaitSeconds ignored
		} else {
		    return employeeFacade.requestWipEligibleEmployeesSynchronously(shiftPk, employeeId, 
		    		maxComputationTime, maxUnimprovedSecondsSpent, maxSynchronousWaitSeconds, includeDetails);						
		}
	}

	/**
	 * Request WIP eligible employees for calling employee/shift
	 * 
	 * @param params
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@POST
	@Path("ops/getwipeligibleemployees")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View}) // TODO?
	@Audited(label = "Request WIP eligible employees for calling employee/shift",
            callCategory = ApiCallCategory.OpenShiftEligibility) // TODO?
	@Interceptors(AuditingInterceptor.class)
	public WipEligibleTeammatesDto requestWipEligibleEmployees(WipEligibleEmployeesRequestParamsDto params)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String shiftId = params.getShiftId();
		Boolean isAsync = params.getIsAsync();
	    Integer maxComputationTime = params.getMaxComputationTime();
	    Integer maxUnimprovedSecondsSpent = params.getMaxUnimprovedSecondsSpent();
	    Integer maxSynchronousWaitSeconds = params.getMaxSynchronousWaitSeconds();
        Boolean includeDetails = params.getIncludeDetails();
	
		PrimaryKey shiftPk = createPrimaryKey(shiftId);
	
		if (isAsync) {
		    return employeeFacade.requestWipEligibleEmployeesAsynchronously(shiftPk, null, 
		    		maxComputationTime, maxUnimprovedSecondsSpent, includeDetails);	// maxSynchronousWaitSeconds ignored
		} else {
		    return employeeFacade.requestWipEligibleEmployeesSynchronously(shiftPk, null, 
		    		maxComputationTime, maxUnimprovedSecondsSpent, maxSynchronousWaitSeconds, includeDetails);						
		}
	
	}

	/**
	 * Get WIP eligible employees
	 * @param requestId
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@GET
	@Path("ops/getwipeligibleemployees/{requestId}/status")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View}) // TODO?
	@Audited(label = "Get WIP eligible employees", callCategory = ApiCallCategory.OpenShiftEligibility) // TODO?
	@Interceptors(AuditingInterceptor.class)
	public WipEligibleTeammatesDto getWipEligibleEmployeesRequestStatus(@PathParam("requestId") String requestId)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		return employeeFacade.getWipEligibleEmployeesRequestStatus(requestId);
	}

	/**
	 * Get swap eligible employees
	 * @param requestId
	 * @param shiftId
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@GET
	@Path("ops/getswapeligiblshifts/{requestId}/status")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View}) // TODO?
	@Audited(label = "Get Swap eligible employees", callCategory = ApiCallCategory.ShiftSwapEligibility) // TODO?
	@Interceptors(AuditingInterceptor.class)
	public SwapEligibleShiftsDto getSwapEligibleShiftsRequestStatus(
			@PathParam("requestId") String requestId, String shiftId )
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		return employeeFacade.getSwapEligibleShiftsRequestStatus(requestId, shiftId);
	}

	/**
	 * Request swap eligible employees for specified employee/shift
	 * 
	 * @param employeeId
	 * @param params
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@POST
	@Path("{employeeid}/ops/getswapeligibleshifts")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View}) // TODO?
	@Audited(label = "Get swap eligible employees for empl/shift",
            callCategory = ApiCallCategory.ShiftSwapEligibility) // TODO?
	@Interceptors(AuditingInterceptor.class)
	public SwapEligibleShiftsDto requestSwapEligibleShifts(
			@PathParam("employeeid") String employeeId, 
	        SwapElligibleEmployeesRequestParamsDto params)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String shiftId = params.getShiftId();
		Boolean isAsync = params.getIsAsync();
	    Integer maxComputationTime = params.getMaxComputationTime();
	    Integer maxUnimprovedSecondsSpent = params.getMaxUnimprovedSecondsSpent();
	    Integer maxSynchronousWaitSeconds = params.getMaxSynchronousWaitSeconds();
        Boolean includeDetails = params.getIncludeDetails();
	
		PrimaryKey shiftPk = createPrimaryKey(shiftId);
	
		if (isAsync) {
		    return employeeFacade.requestSwapEligibleShiftsAsynchronously(shiftPk, employeeId, 
		    		maxComputationTime, maxUnimprovedSecondsSpent, includeDetails);	// maxSynchronousWaitSeconds ignored
		} else {
		    return employeeFacade.requestSwapEligibleShiftsSynchronously(shiftPk, employeeId, 
		    		maxComputationTime, maxUnimprovedSecondsSpent, maxSynchronousWaitSeconds, includeDetails);						
		}
	
	}

	/**
	 * Request swap eligible employees for calling employee/shift
	 * 
	 * @param params
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@POST
	@Path("ops/getswapeligibleshifts")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View}) // TODO?
	@Audited(label = "get swap eligible employees for calling employee/shift",
            callCategory = ApiCallCategory.ShiftSwapEligibility) // TODO?
	@Interceptors(AuditingInterceptor.class)
	public SwapEligibleShiftsDto requestSwapEligibleShifts(SwapElligibleEmployeesRequestParamsDto params)
	        throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String shiftId = params.getShiftId();
		Boolean isAsync = params.getIsAsync();
	    Integer maxComputationTime = params.getMaxComputationTime();
	    Integer maxUnimprovedSecondsSpent = params.getMaxUnimprovedSecondsSpent();
	    Integer maxSynchronousWaitSeconds = params.getMaxSynchronousWaitSeconds();
        Boolean includeDetails = params.getIncludeDetails();
	
		PrimaryKey shiftPk = createPrimaryKey(shiftId);
	
		if (isAsync) {
		    return employeeFacade.requestSwapEligibleShiftsAsynchronously(shiftPk, null, 
		    		maxComputationTime, maxUnimprovedSecondsSpent, includeDetails);	// maxSynchronousWaitSeconds ignored
		} else {
		    return employeeFacade.requestSwapEligibleShiftsSynchronously(shiftPk, null, 
		    		maxComputationTime, maxUnimprovedSecondsSpent, maxSynchronousWaitSeconds, includeDetails);						
		}
	}

    @GET
    @Path("{employeeId}/managerdetailsview")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.Employee_View,
            Permissions.EmployeeProfile_Update})
    @Audited(label = "Get Employee Manager Details View", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public EmployeeManagerViewDto managerDetailsView(@PathParam("employeeId") String employeeId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
        return employeeFacade.managerDetailsView(employeePrimaryKey);
    }

    @GET
    @Path("profileview")	
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.EmployeeProfile_Update})
    @Audited(label = "Get Employee Profile View", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public EmployeeManagerViewDto ProfileView()
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	
    	// actually return the same information as manager view, for the current employee
    	EmployeeManagerViewDto dto = employeeFacade.managerDetailsView(createPrimaryKey(getEmployeeId()));
    	if (dto != null) {
    		// placeholder for restricting information returned back to user if needed
    	}
    	return dto;
    }
    
	@Deprecated	// used by migartion only
    @GET
    @Path("{employeeId}/notificationsettings")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.Employee_View,
            Permissions.EmployeeProfile_Update})
    @Audited(label = "Get Employee Notification Settings", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public NotificationSettingDto getNotificationSettings(@PathParam("employeeId") String employeeId) {
        PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
        return employeeFacade.getNotificationSettings(employeePrimaryKey);
    }

	@Deprecated	// used by migartion only
    @GET
    @Path("notificationsettings")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.Employee_View,
            Permissions.EmployeeProfile_Update})
    @Audited(label = "Get Current Employee Notification Settings", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public NotificationSettingDto getNotificationSettings() {
        return getNotificationSettings(this.getEmployeeId());
    }

	@Deprecated	// used by migartion only
    @PUT
    @Path("{employeeId}/notificationsettings")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
    @Audited(label = "Update Employee Notification Settings", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public NotificationSettingDto updateNotificationSettings(@PathParam("employeeId") String employeeId,
                                                             NotificationSettingDto notificationSettingDto) {
        PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
        return employeeFacade.updateNotificationSettings(employeePrimaryKey, notificationSettingDto);
    }

	@Deprecated	// used by migartion only
    @PUT
    @Path("notificationsettings")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
    @Audited(label = "Update Current Employee Notification Settings", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public NotificationSettingDto updateNotificationSettings(NotificationSettingDto notificationSettingDto) {
        return updateNotificationSettings(this.getEmployeeId(), notificationSettingDto);
    }

    @GET
    @Path("{employeeId}/autoapprovals")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.Employee_View,
            Permissions.EmployeeProfile_Update})
    @Audited(label = "Get Employee Autoapprovals", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public AutoApprovalsSettingDto getAutoApprovals(@PathParam("employeeId") String employeeId) {
        PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
        return employeeFacade.getAutoApprovals(employeePrimaryKey);
    }

    @GET
    @Path("autoapprovals")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.Employee_View,
            Permissions.EmployeeProfile_Update})
    @Audited(label = "Get Current Employee Autoapprovals", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public AutoApprovalsSettingDto getAutoApprovals() {
        return getAutoApprovals(this.getEmployeeId());
    }

    @PUT
    @Path("{employeeId}/autoapprovals")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
    @Audited(label = "Update Employee Autoapprovals", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public AutoApprovalsSettingDto updateAutoApprovals(@PathParam("employeeId") String employeeId,
                                                       AutoApprovalsSettingDto autoApprovalsSettingDto) {
        PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
        return employeeFacade.updateAutoApprovals(employeePrimaryKey, autoApprovalsSettingDto);
    }

    @PUT
    @Path("autoapprovals")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
    @Audited(label = "Update Current Employee Autoapprovals", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public AutoApprovalsSettingDto updateAutoApprovals(AutoApprovalsSettingDto autoApprovalsSettingDto) {
        return updateAutoApprovals(this.getEmployeeId(), autoApprovalsSettingDto);
    }

    @GET
    @Path("{employeeId}/hoursandovertime")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.Employee_View,
            Permissions.EmployeeProfile_Update})
    @Audited(label = "Get Employee Hours And Overtime", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public HoursAndOvertimeDto getHoursAndOvertime(@PathParam("employeeId") String employeeId) {
        PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
        return employeeFacade.getHoursAndOvertime(employeePrimaryKey);
    }

    @GET
    @Path("hoursandovertime")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.Employee_View,
            Permissions.EmployeeProfile_Update})
    @Audited(label = "Get Current Employee Hours And Overtime", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public HoursAndOvertimeDto getHoursAndOvertime() {
        return getHoursAndOvertime(this.getEmployeeId());
    }

    @PUT
    @Path("{employeeId}/hoursandovertime")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
    @Audited(label = "Update Employee Hours And Overtime", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public HoursAndOvertimeDto updateHoursAndOvertime(@PathParam("employeeId") String employeeId,
                                                      HoursAndOvertimeDto hoursAndOvertimeDto) {
        PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
        return employeeFacade.updateHoursAndOvertime(employeePrimaryKey, hoursAndOvertimeDto);
    }

    @PUT
    @Path("hoursandovertime")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
    @Audited(label = "Update Current Employee Hours And Overtime", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public HoursAndOvertimeDto updateHoursAndOvertime(HoursAndOvertimeDto hoursAndOvertimeDto) {
        return updateHoursAndOvertime(this.getEmployeeId(), hoursAndOvertimeDto);
    }


    @POST
    @Path("{employeeId}/ops/updateemployeeskills")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
    @Audited(label = "Update Employee Skills", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public boolean updateEmployeeSkills(@PathParam("employeeId") String employeeId,
                                        AddUpdateRemoveDto<EmployeeSkillAssociationDto> dto) {
        PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
        return employeeFacade.updateEmployeeSkills(employeePrimaryKey, dto);
    }

    @POST
    @Path("{employeeId}/ops/updateemployeeteams")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
    @Audited(label = "Update Employee Teams", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public boolean updateEmployeeTeams(@PathParam("employeeId") String employeeId,
                                       AddUpdateRemoveDto<EmployeeTeamAssociationDto> dto) {
        PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
        return employeeFacade.updateEmployeeTeams(employeePrimaryKey, dto);
    }

    @POST
	@Path("ops/availcal/view")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Calling Employee Availability View", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public AvailcalViewDto getAvailcalView(
				@QueryParam("daterangestart") Long startDate,  // required
	            @QueryParam("daterangeend") Long endDate)   // required
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
    	return employeeFacade.getAvailcalView(createPrimaryKey(this.getEmployeeId()), startDate, endDate);
	}

    // TODO - Using query params for a POST goes against convention.  Move query params into params DTO
	@POST
	@Path("{employeeId}/ops/availcal/view")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Specified Employee Availability View", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)	
	public AvailcalViewDto getAvailcalView(
				@PathParam("employeeId") String employeeId, 
				@QueryParam("daterangestart") Long startDate,  // required
	            @QueryParam("daterangeend") Long endDate)   // required
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
		PrimaryKey employeePk = new PrimaryKey(this.getTenantId(), employeeId);
		return employeeFacade.getAvailcalView(employeePk, startDate, endDate);
	}

	@POST
	@Path("ops/availcal/workflowrequestpreview")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Calling Employee Availability Change Preview",
            callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public AvailcalViewDto getAvailcalPreviewForWorkflowRequest(
	            AvailcalPreviewWorkflowRequestParamsDto params) 
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
		return employeeFacade.getAvailcalPreviewForWorkflowRequest(createPrimaryKey(this.getEmployeeId()), params);
	}

	@POST
	@Path("{employeeId}/ops/availcal/workflowrequestpreview")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Specified Employee Availability Change Preview",
            callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)	
	public AvailcalViewDto getAvailcalPreviewForWorkflowRequest(
				@PathParam("employeeId") String employeeId, 
	            AvailcalPreviewWorkflowRequestParamsDto params) 
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
		PrimaryKey employeePk = new PrimaryKey(this.getTenantId(), employeeId);
		return employeeFacade.getAvailcalPreviewForWorkflowRequest(employeePk, params);
	}

    // TODO - Using query params for a POST goes against convention.  Move query params into params DTO
	@POST
	@Path("ops/availcal/cdavailpreview")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Calling Employee Availability Change Preview",
            callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public AvailcalViewDto getAvailcalPreviewCDAvail(
				@QueryParam("daterangestart") Long startDate,  // required
	            @QueryParam("daterangeend") Long endDate,  // required
	            AvailcalUpdateParamsCDAvailDto params) 
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
		return employeeFacade.getAvailcalPreviewCDAvail(createPrimaryKey(this.getEmployeeId()), startDate, endDate, params);
	}

    // TODO - Using query params for a POST goes against convention.  Move query params into params DTO
	@POST
	@Path("{employeeId}/ops/availcal/cdavailpreview")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Specified Employee Availability Change Preview",
            callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)	
	public AvailcalViewDto getAvailcalPreviewCDAvail(
				@PathParam("employeeId") String employeeId, 
				@QueryParam("daterangestart") Long startDate,  // required
	            @QueryParam("daterangeend") Long endDate,  // required
	            AvailcalUpdateParamsCDAvailDto params) 
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
		PrimaryKey employeePk = new PrimaryKey(this.getTenantId(), employeeId);
		return employeeFacade.getAvailcalPreviewCDAvail(employeePk, startDate, endDate, params);
	}

    // TODO - Using query params for a POST goes against convention.  Move query params into params DTO
	@POST
	@Path("ops/availcal/ciavailpreview")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Calling Employee Availability Change Preview",
            callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public AvailcalViewDto getAvailcalPreviewCIAvail(
				@QueryParam("daterangestart") Long startDate,  // required
	            @QueryParam("daterangeend") Long endDate,  // required
	            AvailcalUpdateParamsCIAvailDto params) 
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
		return employeeFacade.getAvailcalPreviewCIAvail(createPrimaryKey(this.getEmployeeId()), startDate, endDate,
                params);
	}

    // TODO - Using query params for a POST goes against convention.  Move query params into params DTO
	@POST
	@Path("{employeeId}/ops/availcal/ciavailpreview")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Specified Employee Availability Change Preview Summary",
            callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)	
	public AvailcalViewDto getAvailcalPreviewCIAvail(
				@PathParam("employeeId") String employeeId, 
				@QueryParam("daterangestart") Long startDate,  // required
	            @QueryParam("daterangeend") Long endDate,  // required
	            AvailcalUpdateParamsCIAvailDto params) 
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
		PrimaryKey employeePk = new PrimaryKey(this.getTenantId(), employeeId);
		return employeeFacade.getAvailcalPreviewCIAvail(employeePk, startDate, endDate, params);
	}

    // TODO - Using query params for a POST goes against convention.  Move query params into params DTO
	@POST
	@Path("{employeeId}/ops/availcal/cdavailupdate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
	        Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.Availability_RequestMgmt})
	@Audited(label = "Update CD Availability", callCategory = ApiCallCategory.OrganizationManagement)
	@Interceptors(AuditingInterceptor.class)
	public AvailcalViewDto updateAvailcalCDAvail(
	        @PathParam("employeeId") String employeeId,
			@QueryParam("daterangestart") Long startDate,  // optional
	        @QueryParam("daterangeend") Long endDate,  // optional
	        AvailcalUpdateParamsCDAvailDto params) throws InstantiationException,
	        IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		PrimaryKey employeePk = new PrimaryKey(this.getTenantId(), employeeId);
		return employeeFacade.updateAvailcalCDAvail(employeePk, params, startDate, endDate);
	}

    // TODO - Using query params for a POST goes against convention.  Move query params into params DTO
	@POST
	@Path("{employeeId}/ops/availcal/cdprefupdate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
	        Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.Availability_RequestMgmt})
	@Audited(label = "Update CD Preference", callCategory = ApiCallCategory.OrganizationManagement)
	@Interceptors(AuditingInterceptor.class)
	public AvailcalViewDto updateAvailcalCDPref(
	        @PathParam("employeeId") String employeeId,
			@QueryParam("daterangestart") Long startDate,  // optional
	        @QueryParam("daterangeend") Long endDate,  // optional
	        AvailcalUpdateParamsCDPrefDto params) throws InstantiationException,
	        IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		PrimaryKey employeePk = new PrimaryKey(this.getTenantId(), employeeId);
		return employeeFacade.updateAvailcalCDPref(employeePk, params, startDate, endDate);
	}

    // TODO - Using query params for a POST goes against convention.  Move query params into params DTO
	@POST
	@Path("{employeeId}/ops/availcal/ciavailupdate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
	        Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.Availability_RequestMgmt})
	@Audited(label = "Update CI Availability", callCategory = ApiCallCategory.OrganizationManagement)
	@Interceptors(AuditingInterceptor.class)
	public AvailcalViewDto updateAvailcalCIAvail(
	        @PathParam("employeeId") String employeeId,
			@QueryParam("daterangestart") Long startDate,  // optional
	        @QueryParam("daterangeend") Long endDate,  // optional
	        AvailcalUpdateParamsCIAvailDto params) throws InstantiationException,
	        IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		PrimaryKey employeePk = new PrimaryKey(this.getTenantId(), employeeId);
		return employeeFacade.updateAvailcalCIAvail(employeePk, params, startDate, endDate);
	}

    // TODO - Using query params for a POST goes against convention.  Move query params into params DTO
	@POST
	@Path("{employeeId}/ops/availcal/ciprefupdate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
	        Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.Availability_RequestMgmt})
	@Audited(label = "Update CI Preference", callCategory = ApiCallCategory.OrganizationManagement)
	@Interceptors(AuditingInterceptor.class)
	public AvailcalViewDto updateAvailcalCIPref(
	        @PathParam("employeeId") String employeeId,
			@QueryParam("daterangestart") Long startDate,  // optional
	        @QueryParam("daterangeend") Long endDate,  // optional
	        AvailcalUpdateParamsCIPrefDto params) throws InstantiationException,
	        IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		PrimaryKey employeePk = new PrimaryKey(this.getTenantId(), employeeId);
		return employeeFacade.updateAvailcalCIPref(employeePk, params, startDate, endDate);
	}

    // TODO - Using query params for a POST goes against convention.  Move query params into params DTO
	@POST
	@Path("{employeeId}/ops/availcal/cdcopy")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
	        Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.Availability_RequestMgmt})
	@Audited(label = "Copy CD Avail and/or Pref", callCategory = ApiCallCategory.OrganizationManagement)
	@Interceptors(AuditingInterceptor.class)
	public AvailcalViewDto updateAvailcalCDCopy(
	        @PathParam("employeeId") String employeeId,
			@QueryParam("daterangestart") Long startDate,  // optional
	        @QueryParam("daterangeend") Long endDate,  // optional
	        AvailcalUpdateParamsCDCopyDto params) throws InstantiationException,
	        IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		PrimaryKey employeePk = new PrimaryKey(this.getTenantId(), employeeId);
		return employeeFacade.updateAvailcalCDCopy(employeePk, params, startDate, endDate);
	}

	@POST
	@Path("{employeeId}/ops/availcal/rotationupdate")
	@Consumes(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
	        Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.Availability_RequestMgmt})
	@Audited(label = "Update Rotation for Day", callCategory = ApiCallCategory.OrganizationManagement)
	@Interceptors(AuditingInterceptor.class)
	public void updateAvailcalWeekdayRotation(
	        @PathParam("employeeId") String employeeId,
	        AvailcalUpdateParamsWeekdayRotationDto params) throws InstantiationException,
	        IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		PrimaryKey employeePk = new PrimaryKey(this.getTenantId(), employeeId);
		employeeFacade.updateAvailcalWeekdayRotation(employeePk, params);
	}

	@POST
	@Path("{employeeId}/ops/availcal/coupleweekendsupdate")
	@Consumes(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
	        Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.Availability_RequestMgmt})
	@Audited(label = "Update Weekend Coupling", callCategory = ApiCallCategory.OrganizationManagement)
	@Interceptors(AuditingInterceptor.class)
	public void updateAvailcalWeekendCoupling(
	        @PathParam("employeeId") String employeeId, 
	        AvailcalUpdateParamsCoupleWeekends params) throws InstantiationException,
	        IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		PrimaryKey employeePk = new PrimaryKey(this.getTenantId(), employeeId);
		boolean coupleWeekends = params.isCoupleWeekends();
		employeeFacade.updateAvailcalWeekendCoupling(employeePk, coupleWeekends);
	}

    @GET
    @Path("{employeeId}/picture")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt, Permissions.Account_View, Permissions.Employee_Mgmt,
            Permissions.Employee_View, Permissions.EmployeeProfile_Update})
    @Audited(label = "Get Employee Picture", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public AccountPictureDto getPicture(@PathParam("employeeId") String employeeId) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
        PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
        return employeeFacade.getPicture(employeePrimaryKey);
    }

    @GET
    @Path("picture")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt, Permissions.Account_View, Permissions.Employee_Mgmt,
            Permissions.Employee_View, Permissions.EmployeeProfile_Update})
    @Audited(label = "Get Current Employee Picture", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public AccountPictureDto getPicture() throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, IOException {
        return getPicture(this.getEmployeeId());
    }

    @PUT
    @Path("{employeeId}/picture")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt, Permissions.Employee_Mgmt,
            Permissions.EmployeeProfile_Update})
    @Audited(label = "Update Employee Picture", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public boolean updatePicture(@PathParam("employeeId") String employeeId, AccountPictureDto employeePictureDto) {
        PrimaryKey employeePrimaryKey = createPrimaryKey(employeeId);
        return employeeFacade.updatePicture(employeePrimaryKey, employeePictureDto);
    }

    @PUT
    @Path("picture")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
    @Audited(label = "Update Current Employee Picture", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public boolean updatePicture(AccountPictureDto employeePictureDto) {
        return updatePicture(this.getEmployeeId(), employeePictureDto);
    }

    @GET
    @Path("{employeeId}/remembermesessions")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_View})
    @Audited(label = "List of RememberMe Objects", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<RememberMeDto> getRememberMeObjects(
            @PathParam("employeeId") String employeeId,
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        PrimaryKey employeePrimaryKey = new PrimaryKey(tenantId, employeeId);
        return employeeFacade.getRememberMeObjects(employeePrimaryKey, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("remembermesessions")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_View})
    @Audited(label = "List of RememberMe Objects", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<RememberMeDto> getRememberMeObjects(
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String employeeId = getEmployeeId();
        return getRememberMeObjects(employeeId, filter, offset, limit, orderBy, orderDir);
    }

}
