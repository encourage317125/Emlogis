package com.emlogis.rest.resources.tenant;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.emlogis.common.exceptions.credentials.PasswordViolationException;
import com.emlogis.model.employee.dto.PasswordUpdateDto;
import com.emlogis.model.tenant.dto.UserAccountViewDto;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;

import com.emlogis.common.facade.employee.EmployeeFacade;
import com.emlogis.common.facade.tenant.AccountFacade;
import com.emlogis.common.facade.tenant.UserAccountFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.employee.dto.EmployeeAvailabilityDto;
import com.emlogis.model.employee.dto.EmployeeInfoDto;
import com.emlogis.model.employee.dto.EmployeeOpenShiftDto;
import com.emlogis.model.employee.dto.NotificationSettingDto;
import com.emlogis.model.tenant.dto.AccountPictureDto;
import com.emlogis.model.tenant.dto.GroupAccountDto;
import com.emlogis.model.tenant.dto.PasswordResetDto;
import com.emlogis.model.tenant.dto.UserAccountDto;
import com.emlogis.model.tenant.dto.UserAccountInfoDto;
import com.emlogis.model.tenant.dto.UserAccountQueryDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;

/**
 * Resource for UserAccount Administration.
 * allows listing/viewing/updating/creating/deleting user accounts.
 * @author EmLogis
 *
 */
@Path("/useraccounts")
@Authenticated
public class UserAccountResource extends AccountResource {

    @EJB
    private UserAccountFacade userAccountFacade;

    @EJB
    private EmployeeFacade employeeFacade;

    @Override
    protected AccountFacade getAccountFacade() {
        return userAccountFacade;
    }

    /**
     * Get list of UserAccounts
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Audited(label = "List UserAccounts", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<UserAccountDto> getObjects(
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        return userAccountFacade.getObjects(tenantId, select, filter, offset, limit, orderBy, orderDir);
    }

    /**
     * Read one User Account
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Audited(label = "Get UserAccountInfo", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public UserAccountDto getObject(@PathParam("id") final String id) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return userAccountFacade.getObject(primaryKey);
    }

    /**
     * Update a  UserAccount
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.AccountProfile_Update, Permissions.Account_Mgmt})
    @Audited(label = "Update UserAccountInfo", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public UserAccountDto updateObject(@PathParam("id") final String id, UserAccountDto accountDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return userAccountFacade.updateObject(primaryKey, accountDto);
    }

    /**
     * Creates an account
     * @return
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Audited(label = "Create UserAccount", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public UserAccountDto createObject(UserAccountDto accountDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey;
        if (StringUtils.isBlank(accountDto.getId())) {
        	// id is not specified (which is preferred), let's generate one
        	primaryKey = createUniquePrimaryKey();
        } else {
        	primaryKey = createPrimaryKey(accountDto.getId());
        }
        accountDto.setId(primaryKey.getId());
        return userAccountFacade.createObject(primaryKey, accountDto);
    }

    /**
     * Delete an account
     * @return
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Audited(label = "Delete UserAccount", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteObject(@PathParam("id") final String id) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        userAccountFacade.deleteObject(primaryKey);
		return Response.ok().build();
    }

	/**
     * Returns informatin about current account  (currently almost same as getObject())
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws NoSuchFieldException
	 */
	@GET
	@Path("info")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get UserAccount Info Summary", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public UserAccountInfoDto getUserAccountInfo()
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
		
	    return userAccountFacade.getUserAccountInfo(createPrimaryKey(this.getUserId()));
	}

    @GET
    @Path("{id}/groups")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Audited(label = "List User Groups", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<GroupAccountDto> groups(
            @PathParam("id") String id,
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
    	PrimaryKey primaryKey = createPrimaryKey(id);
        return userAccountFacade.groups(primaryKey, select, filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("{id}/unassociatedgroups")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Audited(label = "List Unassociated Groups", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<GroupAccountDto> getUnassociatedGroups(
                @PathParam("id") String id,
                @QueryParam("select") String select, // select is NOT IMPLEMENTED FOR NOW ..
                @QueryParam("filter") String filter,
                @QueryParam("offset") @DefaultValue("0") int offset,
                @QueryParam("limit") @DefaultValue("20") int limit,
                @QueryParam("orderby") String orderBy,
                @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return userAccountFacade.getUnassociatedGroups(primaryKey, select, filter, offset, limit, orderBy, orderDir);
    }
    
    @GET
    @Path("ops/quicksearch")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Audited(label = "User quick search", callCategory = ApiCallCategory.AccountManagement)
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
        return userAccountFacade.quickSearch(tenantId, searchValue, searchFields, returnedFields, limit, orderBy,
                orderDir);
    }

    @GET
    @Path("ops/query")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Audited(label = "User query", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<UserAccountQueryDto> query(
            @QueryParam("search") String searchValue,
            @QueryParam("searchfields") String searchFields,
            @QueryParam("employeefilter") String employeeFilter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        return userAccountFacade.query(tenantId,  searchValue, searchFields, employeeFilter, offset, limit, orderBy,
                orderDir);
    }

    @POST
    @Path("{userId}/ops/addgroup")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "AddGroup To UserAccount", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response addGroup(@PathParam("userId") String userId, String groupId) {
        PrimaryKey userPrimaryKey = createPrimaryKey(userId);
        PrimaryKey groupPrimaryKey = createPrimaryKey(groupId);
        userAccountFacade.addGroup(userPrimaryKey, groupPrimaryKey);
        return Response.ok().build();
    }

    @POST
    @Path("{userId}/ops/addgroups")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "AddGroups To UserAccount", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response addGroups(@PathParam("userId") String userId, String[] groupIdList) {
        PrimaryKey userPrimaryKey = createPrimaryKey(userId);
        List<PrimaryKey> groupPrimaryKeys = getGroupListPK(getTenantId(), groupIdList);
        userAccountFacade.addGroups(userPrimaryKey, groupPrimaryKeys);
        return Response.ok().build();
    }

    @POST
    @Path("{userId}/ops/removegroup")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "RemoveGroup From UserAccount", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response removeGroup(@PathParam("userId") String userId, String groupId) {
        PrimaryKey primaryKey = createPrimaryKey(groupId);
        PrimaryKey groupPrimaryKey = createPrimaryKey(userId);
        userAccountFacade.removeGroup(primaryKey, groupPrimaryKey);
        return Response.ok().build();
    }

    @POST
    @Path("{userId}/ops/removegroups")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "RemoveGroups From UserAccount", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response removeGroups(@PathParam("userId") String userId, String[] groupIdList) {
        PrimaryKey userPrimaryKey = createPrimaryKey(userId);
        List<PrimaryKey> groupPrimaryKeys = getGroupListPK(getTenantId(), groupIdList);
        userAccountFacade.removeGroups(userPrimaryKey, groupPrimaryKeys);
        return Response.ok().build();
    }

    @GET
    @Path("{userId}/permissionsmapping")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Get permissions Role/Group mapping", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Response permissionsMapping(@PathParam("userId") String userId) {
        PrimaryKey userPrimaryKey = createPrimaryKey(userId);
        return Response.ok(userAccountFacade.permissionsMapping(userPrimaryKey).getMappings()).build();
    }

	@Deprecated
	@GET
	@Path("employeeinfo")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Calling User's Employee Info Summary", callCategory = ApiCallCategory.Unclassified)
	@Interceptors(AuditingInterceptor.class)
	public EmployeeInfoDto getEmployeeInfo() 
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
	    return employeeFacade.getEmployeeInfo(createPrimaryKey(getUserId()));
	}

	@Deprecated
	@GET
	@Path("employeeavailability")
	@Produces(MediaType.APPLICATION_JSON)
//	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Calling User's Employee Availability Summary", callCategory = ApiCallCategory.Unclassified)
	@Interceptors(AuditingInterceptor.class)
	public EmployeeAvailabilityDto getEmployeeAvailability(
	            @QueryParam("startdate") @DefaultValue("0") Long startDate,
	            @QueryParam("enddate") @DefaultValue("0") Long endDate,
	            @QueryParam("schedulestatus") @DefaultValue("Posted") String scheduleStatus,
	            @QueryParam("timezone") String timeZone) 
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
		
		DateTimeZone tz = null;
		if (! StringUtils.isBlank(timeZone)) {
			tz = DateTimeZone.forID(timeZone);
		}
	    return employeeFacade.getEmployeeAvailability(createPrimaryKey(getEmployeeId()), startDate, endDate,
                scheduleStatus, tz);
	}

	@GET
	@Path("employeepostedopenshifts")
	@Produces(MediaType.APPLICATION_JSON)
//	@RequirePermissionIn(permissions = {Permissions.Employee_View})
	@Audited(label = "Get Employee OpenShifts", callCategory = ApiCallCategory.Unclassified)
	@Interceptors(AuditingInterceptor.class)
	public Collection<EmployeeOpenShiftDto> getEmployeeOpenShifts(
	            @QueryParam("startdate") @DefaultValue("0") Long startDate,
	            @QueryParam("enddate") @DefaultValue("0") Long endDate)
	            		throws InstantiationException, IllegalAccessException, InvocationTargetException, 
	            		NoSuchMethodException, NoSuchFieldException {
	    return employeeFacade.getEmployeeOpenShifts(createPrimaryKey(getEmployeeId()), startDate, endDate);
	}

    @GET
    @Path("{userId}/notificationsettings")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.Employee_View,
            Permissions.EmployeeProfile_Update})
    @Audited(label = "Get Account/Employee Notification Settings", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public NotificationSettingDto getNotificationSettings(@PathParam("userId") String userId) {
        PrimaryKey primaryKey = createPrimaryKey(userId);
        return userAccountFacade.getNotificationSettings(primaryKey);
    }

    @GET
    @Path("notificationsettings")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.Employee_View,
            Permissions.EmployeeProfile_Update})
    @Audited(label = "Get Current Account/Employee Notification Settings", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public NotificationSettingDto getNotificationSettings() {
    	
        return getNotificationSettings(this.getUserId());
    }

    @PUT
    @Path("{userId}/notificationsettings")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
    @Audited(label = "Update Account/Employee Notification Settings", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public NotificationSettingDto updateNotificationSettings(@PathParam("userId") String userId,
                                                             NotificationSettingDto notificationSettingDto) {
        PrimaryKey primaryKey = createPrimaryKey(userId);
        return userAccountFacade.updateNotificationSettings(primaryKey, notificationSettingDto);
    }

    @POST
    @Path("{userId}/ops/enablenotification")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
    @Audited(label = "Enable/Disable Employee Notification", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public NotificationSettingDto updateNotificationSettings(@PathParam("userId") String userId, Boolean enable) {
        PrimaryKey primaryKey = createPrimaryKey(userId);
        return userAccountFacade.enableNotificationSettings(primaryKey, enable);
    }
    
    @PUT
    @Path("notificationsettings")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt})
    @Audited(label = "Update Account/Employee Employee Notification Settings", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public NotificationSettingDto updateNotificationSettings(NotificationSettingDto notificationSettingDto) {
        return updateNotificationSettings(this.getUserId(), notificationSettingDto);
    }

    @GET
    @Path("{userId}/userview")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Audited(label = "Get UserAccountViewDto", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public UserAccountViewDto getUserAccountView(@PathParam("userId") String userId)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            NoSuchFieldException {
        PrimaryKey userPrimaryKey = createPrimaryKey(userId);
        return userAccountFacade.getUserAccountView(userPrimaryKey);
    }

    @POST
    @Path("{userId}/ops/chgpassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
    @Audited(label = "Change Password", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response changePassword(@PathParam("userId") String userId,
                                   PasswordUpdateDto passwordUpdateDto,
                                   @Context UriInfo uriInfo) throws PasswordViolationException {
    	
        return doChangePassword(userId, passwordUpdateDto, uriInfo);
    }

    @POST
    @Path("/ops/chgpassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.EmployeeProfile_Update, Permissions.AccountProfile_Update})
    @Audited(label = "Change Password", callCategory = ApiCallCategory.Session)
    @Interceptors(AuditingInterceptor.class)
    public Response changePassword(PasswordUpdateDto passwordUpdateDto, @Context UriInfo uriInfo)
            throws PasswordViolationException {
        return doChangePassword(getUserId(), passwordUpdateDto, uriInfo);
    }

    private Response doChangePassword(String userId, PasswordUpdateDto passwordUpdateDto, UriInfo uriInfo) throws PasswordViolationException {
    	
        PrimaryKey userPrimaryKey = createPrimaryKey(userId);
        userAccountFacade.updateUserAccountPassword(userPrimaryKey, passwordUpdateDto, uriInfo.getBaseUri());
        return Response.ok().build();
    }

	/**
	 * Reset the password of the UserAccount  specified by the accountId
	 * 	 
	 * @param accountId
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 */
	@POST
	@Path("{accountId}/ops/resetpassword")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.Account_Mgmt, Permissions.Employee_Mgmt})
	@Audited(label = "Reset Employee UserAccount Password", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public Response resetPassword(@PathParam("accountId") String accountId) {
		return doResetPassword(accountId);
	}

	/**
	 * Reset the password of the UserAccount  currently logged into the system
	 * 	 
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 */
	@POST
	@Path("ops/resetpassword")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.EmployeeProfile_Update})
	@Audited(label = "Reset Employee UserAccount Password", callCategory = ApiCallCategory.Session)
	@Interceptors(AuditingInterceptor.class)
	public Response resetPassword() {
		return doResetPassword(this.getUserId());
	}

    private Response doResetPassword(String accountId) {
        PrimaryKey primaryKey = createPrimaryKey(accountId);
        PasswordResetDto passwordResetDto = userAccountFacade.resetPassword(primaryKey);
        if (StringUtils.isNotEmpty(passwordResetDto.getEmailAddress())) {
            return Response.ok(passwordResetDto).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(passwordResetDto).build();
        }
    }

    @GET
    @Path("{userId}/picture")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt, Permissions.Account_View, Permissions.Employee_Mgmt,
            Permissions.Employee_View, Permissions.EmployeeProfile_Update})
    @Audited(label = "Get User Picture", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public AccountPictureDto getPicture(@PathParam("userId") String userId) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
        PrimaryKey accountPrimaryKey = createPrimaryKey(userId);
        return userAccountFacade.getPicture(accountPrimaryKey);
    }

    @GET
    @Path("picture")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt, Permissions.Account_View, Permissions.Employee_Mgmt,
            Permissions.Employee_View, Permissions.EmployeeProfile_Update})
    @Audited(label = "Get Current User Picture", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public AccountPictureDto getPicture() throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, IOException {
        return getPicture(this.getUserId());
    }

    @PUT
    @Path("{userId}/picture")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt, Permissions.Employee_Mgmt,
            Permissions.EmployeeProfile_Update})
    @Audited(label = "Update User Picture", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public boolean updatePicture(@PathParam("userId") String userId, AccountPictureDto employeePictureDto) {
        PrimaryKey accountPrimaryKey = createPrimaryKey(userId);
        return userAccountFacade.updatePicture(accountPrimaryKey, employeePictureDto);
    }

    @PUT
    @Path("picture")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
    @Audited(label = "Update Current User Picture", callCategory = ApiCallCategory.EmployeeManagement)
    @Interceptors(AuditingInterceptor.class)
    public boolean updatePicture(AccountPictureDto employeePictureDto) {
        return updatePicture(this.getUserId(), employeePictureDto);
    }

    private List<PrimaryKey> getGroupListPK(String tenantId, String[] groupIdList) {
        List<PrimaryKey> result = new ArrayList<>();
        if (groupIdList != null) {
            for (String groupId : groupIdList) {
                if (!StringUtils.isBlank(groupId)) {
                    result.add(new PrimaryKey(tenantId, groupId));
                }
            }
        }
        return result;
    }

}
