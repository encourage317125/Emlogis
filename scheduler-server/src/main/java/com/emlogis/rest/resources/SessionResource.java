package com.emlogis.rest.resources;

import com.emlogis.common.exceptions.credentials.EmLogisCredentialsException;
import com.emlogis.common.exceptions.credentials.PasswordViolationException;
import com.emlogis.common.facade.employee.EmployeeFacade;
import com.emlogis.common.facade.tenant.UserAccountFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.common.security.Security;
import com.emlogis.common.security.SessionInfo;
import com.emlogis.common.services.notification.NotificationConfigInfo;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.notification.MsgDeliveryType;
import com.emlogis.model.tenant.dto.ChangePasswordDto;
import com.emlogis.model.tenant.dto.PasswordResetDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.auditing.LogRecordCategory;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.BackendSessionDto;
import com.emlogis.rest.security.RequirePermissionIn;
import com.emlogis.rest.security.SessionService;
import com.emlogis.server.services.PasswordCoder;
import com.emlogis.server.services.eventservice.ASEventService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

@Stateless
@LocalBean
@Path("/sessions")
public class SessionResource extends BaseResource {

    private final static Logger logger = Logger.getLogger("session");

    @Inject
	private ASEventService eventService;

    @Inject
	private PasswordCoder passwordCoder;
    
    @EJB
    private UserAccountFacade userAccountFacade;

	/**
	 * Read list of sessions known by system
	 */
	@GET
    @Produces(MediaType.APPLICATION_JSON)
//    @Authenticated
//    @Audited(type = LogRecordCategory.Session, label = "List Sessions", callCategory = ApiCallCategory.Session)
    @Interceptors(AuditingInterceptor.class)
	public Collection<BackendSessionDto> getObjects() {
		return getSessionService().getSessionsInfo();
	}

    @GET
    @Path("alive")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(type = LogRecordCategory.Session, label = "ChekAlive Session", callCategory = ApiCallCategory.Session)
    @Interceptors(AuditingInterceptor.class)
	public boolean isSessionAlive() {
		return getSessionService().isSessionAlive();
	}

	/**
	 * login() creates a new session
	 * @return
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Audited(type = LogRecordCategory.Session, label = "Open Session", callCategory = ApiCallCategory.Session)
    @Interceptors(AuditingInterceptor.class)
	public Response login(@Context HttpHeaders headers, LoginInfo loginInfo) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, EmLogisCredentialsException {
        logger.debug("Session Resource.login(): " + loginInfo.toString());

        List<String> tokenList = headers.getRequestHeader(Security.TOKEN_HEADER_NAME);
        String tokenId = tokenList != null && tokenList.size() > 0 ? tokenList.get(0) : null;
        if (!"null".equalsIgnoreCase(tokenId) && StringUtils.isNotBlank(tokenId)) {
            getSessionService().closeSession(tokenId);    // force close of current session if one exist
        }

        // override or set the tenantId in loginInfo, if set in header by security filter. 
        List<String> tenantIdList = headers.getRequestHeader(Security.TENANTID_HEADER_NAME);
        String tenantId = tenantIdList != null && tenantIdList.size() > 0 ? tenantIdList.get(0) : null;
        if (!StringUtils.isBlank(tenantId)) {
        	loginInfo.setTenantId(tenantId);
        }        
        
        getSessionService().validatePasswordOnLogin(loginInfo.getTenantId(), loginInfo.getLogin(), loginInfo.getPassword());

        // get remember me information from header
        List<String> rememberMeList = headers.getRequestHeader(Security.REMEMBERME_HEADER_NAME);
        String rememberMeStr = rememberMeList != null && rememberMeList.size() > 0 ? rememberMeList.get(0) : null;
        boolean rememberMe = StringUtils.equals("true", rememberMeStr);
        
        List<String> clientIdList = headers.getRequestHeader(Security.REMEMBERMECLIENTID_HEADER_NAME);
        String rememberMeClientId = clientIdList != null && clientIdList.size() > 0 ? clientIdList.get(0) : null;
        
        List<String> clientDescrList = headers.getRequestHeader(Security.REMEMBERMECLIENTDESCR_HEADER_NAME);
        String rememberMeClientDescr = clientDescrList != null && clientDescrList.size() > 0 ? clientDescrList.get(0) : null;
        
        // clear remember me if rememberMeClientId or rememberMeClientDescr are empty.
        if (StringUtils.isBlank(rememberMeClientId) || StringUtils.isBlank(rememberMeClientDescr)) {
        	rememberMe = false;
        }
                   
        SessionInfo result = getSessionService().createSession(
        	loginInfo.getTenantId(), loginInfo.getLogin(), loginInfo.getPassword(),
        	rememberMe, rememberMeClientId, rememberMeClientDescr
        ); 
        if (result == null) {
            throw new AuthenticationException("Access denied for this resource. Authentication required");
        }
        return Response.ok(result).build();
	}

	/**
	 * login() creates a new session
	 * @return
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	
	@POST
    @Path("/loginbyaccount")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Audited(type = LogRecordCategory.Session, label = "Open Session", callCategory = ApiCallCategory.Session)
    @Interceptors(AuditingInterceptor.class)
	public Response loginByAccount(@Context HttpHeaders headers, AccountInfo accountInfo) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, EmLogisCredentialsException {
        logger.debug("Session Resource.loginByAccount(): " + accountInfo.toString());

        List<String> tokenList = headers.getRequestHeader(Security.TOKEN_HEADER_NAME);
        String tokenId = tokenList != null && tokenList.size() > 0 ? tokenList.get(0) : null;
        if (!"null".equalsIgnoreCase(tokenId) && StringUtils.isNotBlank(tokenId)) {
            getSessionService().closeSession(tokenId);    // force close of current session if one exist
        }

        PrimaryKey userPrimaryKey = new PrimaryKey(accountInfo.getTenantId(), accountInfo.getAccountId());

        SessionInfo result = getSessionService().createSession(userPrimaryKey);
        if (result == null) {
            throw new AuthenticationException("Access denied for this resource. Authentication required");
        }
        return Response.ok(result).build();
	}

    /**
     * @param userId	user login or accountId
     * @param byId 		true means userId is an accountId, false means userId is a login
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    @POST
    @Path("/ops/impersonate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    @RequirePermissionIn(permissions = {Permissions.Impersonate_ReadWrite})
    @Audited(type = LogRecordCategory.Session, label = "Impersonate Session", callCategory = ApiCallCategory.Session)
    @Interceptors(AuditingInterceptor.class)
    public SessionInfo impersonate(String userId, @QueryParam("byid") @DefaultValue("false") boolean byId)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        logger.debug("Session Resource.impersonate(): " + userId);

        SessionInfo result = getSessionService().impersonateSession(userId, byId, false);
        if (result == null) {
            throw new AuthenticationException("Access denied for this resource. Authentication required");
        }
        return result;
    }

    /**
     * @param userId	user login or accountId
     * @param byId 		true means userId is an accountId, false means userId is a login
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    @POST
    @Path("/ops/impersonateviewonly")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    @RequirePermissionIn(permissions = {Permissions.Impersonate_ViewOnly, Permissions.Impersonate_ReadWrite})
    @Audited(type = LogRecordCategory.Session, label = "Impersonate Session(ReadOnly)",
            callCategory = ApiCallCategory.Session)
    @Interceptors(AuditingInterceptor.class)
    public SessionInfo impersonateViewOnly(String userId, @QueryParam("byid") @DefaultValue("false") boolean byId)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        logger.debug("Session Resource.impersonate(): " + userId);

        SessionInfo result = getSessionService().impersonateSession(userId, byId, true);
        if (result == null) {
            throw new AuthenticationException("Access denied for this resource. Authentication required");
        }
        return result;
    }

    @POST
    @Path("/ops/unimpersonate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
//    @RequirePermissionIn(permissions = {Permissions.Impersonate, Permissions.ImpersonateViewOnly})
    @Audited(type = LogRecordCategory.Session, label = "Un-Impersonate Session", callCategory = ApiCallCategory.Session)
    @Interceptors(AuditingInterceptor.class)
    public SessionInfo unimpersonate() throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        logger.debug("Session Resource.Un-impersonate()");

        SessionInfo result = getSessionService().unimpersonateSession();
        if (result == null) {
            throw new AuthenticationException("Access denied for this resource. Authentication required");
        }
        return result;
    }
    
	/**
	 * logout() close the current session
	 * @return
	 */
	@DELETE
    @Authenticated
	@Audited(type = LogRecordCategory.Session, label = "Close Session", callCategory = ApiCallCategory.Session)
    @Interceptors(AuditingInterceptor.class)
	public Response logout() {
		logger.debug("Session Resource.logout()");

		String tokenId = getSessionService().getTokenId();
        getSessionService().closeSession(tokenId);
        userAccountFacade.deleteRememberMeByToken(tokenId);
        eventService.unregisterSSEClient(tokenId, "client logout"); // release SSE related resources
		return Response.ok().build();
	}

	/**
	 * touch()
	 * @return
	 */
	@POST
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/ops/touch")
    @Authenticated
	public Response touch() {
        getSessionService().touch();
		return Response.ok().build();
	}

	/**
	 * forceLogout() closes a session. requires administrator permission
	 * @return response object
	 */
	@DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	@Path("/ops/forcelogout")
    @Authenticated
	@Audited(type = LogRecordCategory.Session, label = "ForceClose Session", callCategory = ApiCallCategory.Session)
    @Interceptors(AuditingInterceptor.class)
	public Response forceLogout() {
		return logout();
	}

    /**
     * Account Change password
     * @return response object
     */
    @POST
    @Path("/ops/chgpassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Change Password", callCategory = ApiCallCategory.Session)
    @Interceptors(AuditingInterceptor.class)
    public Response changePassword(ChangePasswordDto dto, @Context UriInfo uriInfo) throws PasswordViolationException {
        getSessionService().changePassword(dto, uriInfo.getBaseUri());
        return Response.ok().build();
    }
    
    @GET
    @Path("/ops/chgpassword/confirmation/{confirmationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Change Password Confirmation", callCategory = ApiCallCategory.Session)
    @Interceptors(AuditingInterceptor.class)
    public Response changePassword(@PathParam("confirmationId") String confirmationId) {
        getSessionService().changePasswordConfirmation(confirmationId);
        return Response.ok().build();
    }

    @GET
    @Path("accountid")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    @Audited(type = LogRecordCategory.Session, label = "Get AccountIdentifier", callCategory = ApiCallCategory.Session)
    @Interceptors(AuditingInterceptor.class)
	public AccountInfo accountId() {
    	String tenantId = this.getTenantId();
		String accountId = getSessionService().getActualUserId();
		return new AccountInfo(tenantId, accountId);
	}

    @GET
    @Path("isauthenticated")
    @Interceptors(AuditingInterceptor.class)
    @Produces(MediaType.APPLICATION_JSON)
    public boolean isAuthenticated(@QueryParam("token") String token){
        return (getSessionService().getSessionInfo(token) != null);
    }

	/**
	 * Reset the password of the UserAccount specified by loginId
	 * 	 
	 * @param loginInfo
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
	@RequirePermissionIn(permissions = {Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update})
	@Audited(label = "Reset Employee UserAccount Password", callCategory = ApiCallCategory.EmployeeManagement)
	@Interceptors(AuditingInterceptor.class)
	public PasswordResetDto resetUserAccountPassword(RetrieveLoginInfo loginInfo) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException,
            PasswordViolationException {
		String accountId = getSessionService().retrieveAccountId(loginInfo.getTenantId(), loginInfo.getLogin());
		if (accountId == null) {
			return new PasswordResetDto("Unable to retrieve account information.");
		} else {
			PrimaryKey accountPrimaryKey = new PrimaryKey(loginInfo.getTenantId(), accountId);
			return userAccountFacade.resetPassword(accountPrimaryKey);
		}
	}

}

@XmlRootElement
class LoginInfo {

	private	String tenantId;
	private	String login;
	private	String password;

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

    public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

}

@XmlRootElement
class AccountInfo {

	private	String tenantId;
	private	String accountId;

    public AccountInfo() {}

    public AccountInfo(String tenantId, String accountId) {
		super();
		this.tenantId = tenantId;
		this.accountId = accountId;
	}

	public String getTenantId() {
		return tenantId;
	}

    public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

    public String getAccountId() {
		return accountId;
	}

    public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	
}

@XmlRootElement
class RetrieveLoginInfo {

	private	String tenantId;
	private	String login;

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

}
