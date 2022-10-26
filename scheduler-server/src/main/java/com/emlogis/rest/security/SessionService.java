package com.emlogis.rest.security;

import java.io.Serializable;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.ejb.*;
import javax.inject.Inject;

import com.emlogis.common.Constants;
import com.emlogis.common.PasswordUtils;
import com.emlogis.common.exceptions.*;
import com.emlogis.common.exceptions.credentials.*;
import com.emlogis.common.validation.annotations.ValidatePassword;
import com.emlogis.common.validation.annotations.Validation;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.EmployeeActivityType;
import com.emlogis.model.tenant.*;
import com.emlogis.model.tenant.dto.ChangePasswordDto;
import com.emlogis.server.services.PasswordCoder;
import com.hazelcast.core.IMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.ResourcesBundle;
import com.emlogis.common.security.AccountACE;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.security.Permissions;
import com.emlogis.common.security.SessionInfo;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.hazelcast.HazelcastClientService;
import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.services.util.AccountUtilService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.rest.auditing.AuditContext;
import com.emlogis.rest.auditing.AuditRecord;
import com.emlogis.rest.resources.util.DtoMapper;
import com.emlogis.rest.security.BackendSessionInfo.TenantType;
import com.emlogis.shared.services.eventservice.EventService;

@Stateless
@LocalBean
public class SessionService {

	private final Logger logger = LoggerFactory.getLogger(SessionService.class);

	public final static String SESSION_INFO_PROP = "SessionInfo"; 	// name of attribute used to keep the SessionInfo object in session object

    private final static String AUDIT_RECORD_PROP = "AuditRecord"; 	// name of attribute used to keep the AuditRecord prepared by SecurityFilter

    @EJB
    private UserAccountService userAccountService;

    @EJB
    private PasswordCoder passwordCoder;

    @EJB
    private TenantService<Organization> tenantService;
    
    @EJB
    private EmployeeService employeeService;
    
    @EJB
    private AccountUtilService accountUtilService;

    @EJB
    private ResourcesBundle resourcesBundle;

    @EJB
    private HazelcastClientService hazelcastClientService;

    @Inject
    private EventService eventService;

    public SessionService() {
		logger.debug("SessionService constructor");
	}

    /**
     * createSession()
     * creates a session and store some information like tenantId, user identity and permissions in a session attribute
     * @param rememberMeClientDescr 
     * @param rememberMeClientId 
     * @param rememberMe 
     * @return a 'client' view of the session. (same object but with less information)
     */
    public SessionInfo createSession(String tenantId, String login, String password, boolean rememberMe,
                                     String rememberMeClientId, String rememberMeClientDescr) {
        ThreadContext.unbindSubject();
        Subject subject = SecurityUtils.getSubject();

        AuthenticationToken authenticationToken = new UsernamePasswordToken(tenantId + ":" + login, password);
        subject.login(authenticationToken);

        Session session = subject.getSession();
        UserAccount userAccount = userAccountService.getUserAccountBylogin(tenantId, login);        
    	validateAccountState(userAccount);
        Tenant tenant = tenantService.getTenant(tenantId);
    	
        if (rememberMe) {
        	updateRememberMeInfo(session, userAccount, rememberMeClientId, rememberMeClientDescr);
        } else if (userAccount.getInactivityPeriod() > 0) {
            session.setTimeout(userAccount.getInactivityPeriod() * 60L * 1000); // convert from min into ms
        } else {
            session.setTimeout(tenant.getInactivityPeriod() * 60L * 1000); // convert from min into ms
        }      
        
        SessionInfo result = createSessionInfo(subject, tenant, login, false);
        if (result == null) {
            String history = createUnsuccessfulLoginHistory(userAccount, password);
            userAccount.setUnsuccessfulLoginHistory(history);
        } else {
            userAccount.setLastLogged(new DateTime());
            userAccount.setAccountSuspendedUntil(new DateTime(0));
            userAccount.setUnsuccessfulLoginHistory("");
            
            if (rememberMe) {
            	// add remember info to backend session info just created, and 'update it'
            	BackendSessionInfo currentSessionInfo = getSessionInfo(false);
            	currentSessionInfo.setRememberMe(true);
            	currentSessionInfo.setRememberMeClientId(rememberMeClientId);
            	currentSessionInfo.setRememberMeClientDescr(rememberMeClientDescr);
                session.setAttribute(SESSION_INFO_PROP, EmlogisUtils.serializeObject(currentSessionInfo));
            }
        }

        userAccountService.update(userAccount);

        return result;
    }
    
    private void updateRememberMeInfo(Session session, UserAccount userAccount, String rememberMeClientId,
                                      String rememberMeClientDescr) {
		// update remember me information
		// force timeout to be 30 days
	    session.setTimeout(Constants._30_DAYS_MILLISECONDS);
	    Set<RememberMe> rememberMeSet = userAccount.getRememberMeSet();
	    
	    // we allow exactly one session per unique clientId per user account
	    // ie, if a rememberme session is created with id 123, all subsequent session creations with same id 
	    // will terminate the previous session and reopen a new one
	    
	    // search for existing rememberme entity.
	    // NOTE: loop below should be replaced by a simple get (after implementing  RememberMe equals() & hash()
	    RememberMe rmEntity = null; 
	    String rememberMeId = userAccount.getId() + "-" + rememberMeClientId;
	    for (RememberMe rm : rememberMeSet) {
	    	if (StringUtils.equals(rememberMeId, rm.getId())) {
	    		rmEntity = rm;
	    		break;
	    	}
	    }
	    if (rmEntity == null) {
	    	// simple case, no remember entity yet, 
	    	// create a new remember me record for the session, create one with useraccountId + client unique Id as key.
	    	rmEntity = new RememberMe(new PrimaryKey(userAccount.getTenantId(), rememberMeId));
	    	rmEntity.setCreated(System.currentTimeMillis());
	    	rmEntity.setClientUniqueId(rememberMeClientId);
	    	rmEntity.setClientDescr(rememberMeClientDescr);
	    	rmEntity.setTokenId(session.getId().toString());
	    	rmEntity.setExpirationDate(System.currentTimeMillis() + Constants._30_DAYS_MILLISECONDS);
	    	rmEntity.setUserAccount(userAccount);
	    	userAccountService.persistRememberMe(rmEntity);
	    	userAccount.addRememberMe(rmEntity);
	    	userAccountService.update(userAccount);
	    } else {
	    	// we found an entity for that clientId.
	    	// need to terminate previous session and update entity
	    	String previousSessionTokenId = rmEntity.getTokenId();
	    	closeSession(previousSessionTokenId);
	    	rmEntity.setCreated(System.currentTimeMillis());  // overwrite creation date (a bit dirty though)
	    	rmEntity.setClientDescr(rememberMeClientDescr);	  // should be the same, but just in case, write new one
	    	rmEntity.setTokenId(session.getId().toString());
	    	rmEntity.setExpirationDate(System.currentTimeMillis() + Constants._30_DAYS_MILLISECONDS);
        	userAccountService.mergeRememberMe(rmEntity);
	    }
	}

    public SessionInfo createSession(PrimaryKey userPrimaryKey) {
        UserAccount userAccount = userAccountService.getUserAccount(userPrimaryKey);
        return createSession(userAccount.getTenantId(), userAccount.getLogin(), userAccount.getPassword(), false, null, null);
    }

    public Collection<BackendSessionDto> getSessionsInfo() {
        Collection<BackendSessionDto> result = new ArrayList<>();
        DtoMapper<BackendSessionInfo,BackendSessionDto> mapper = new DtoMapper<>();

        Map<Serializable, byte[]> sessionMap = hazelcastClientService.getActiveSessions();
        for (byte[] sessionBytes : sessionMap.values()) {
            Session session = (Session) EmlogisUtils.deserializeObject(sessionBytes);

            BackendSessionInfo backendSessionInfo = (BackendSessionInfo) EmlogisUtils.deserializeObject(
                    (byte[]) session.getAttribute(SessionService.SESSION_INFO_PROP));
            if (backendSessionInfo != null) {
                result.add(mapper.map(backendSessionInfo, BackendSessionDto.class));
            }
        }
        return result;
    }

    /**
     * @param userToImpersonate  id or login of account to impersonate
     * @param byId true means userToImpersonate is an accountId, false means userToImpersonate is a login
     * @param readOnly
     * @return
     */
    public SessionInfo impersonateSession(String userToImpersonate, boolean byId, boolean readOnly) {
        // TODO close current session cleanly and log this into ES ... ideally we should have 2 records / close current start new one

        Subject subject = SecurityUtils.getSubject();
        // get session info related to current user
        BackendSessionInfo currentSessionInfo = getSessionInfo(false);
        String impersonatingUserId = currentSessionInfo.getUserId();
        String impersonatingUserName = currentSessionInfo.getUserName();
        long currentSessionStartTime = currentSessionInfo.getStarted();

    	// check we are not already impersonating a user
    	if (StringUtils.isNotBlank(currentSessionInfo.getImpersonatingUserId())) {
    		throw new ValidationException("Invalid request to Impersonate, current session is already impersonating " +
                    "an account and Impersonate from an impersonated user is forbidden");
    	}
    	// check impersonatedLogin is valid
    	if (StringUtils.isBlank(userToImpersonate)) {
    		throw new ValidationException("Missing login or Id of account to impersonate");
    	}
        UserAccount userAccount;
        if (byId) {
        	userAccount = userAccountService.getUserAccount(new PrimaryKey(currentSessionInfo.getTenantId(),
                    userToImpersonate));
        } else {
        	userAccount = userAccountService.getUserAccountBylogin(currentSessionInfo.getTenantId(), userToImpersonate);
        }     
    	if (userAccount == null) {
    		throw new ValidationException("Invalid login of account to impersonate");
    	}
    	String userToImpersonateLogin = userAccount.getLogin();
                
        // switch to user
        Tenant tenant = tenantService.getTenant(currentSessionInfo.getTenantId());
        SessionInfo clientSessionInfo = createSessionInfo(subject, tenant,
                userToImpersonateLogin, readOnly);
    
        // add impersonating user info and update new backend info
        currentSessionInfo = getSessionInfo(false);
        currentSessionInfo.setImpersonatingUserId(impersonatingUserId);
        currentSessionInfo.setImpersonatingUserName(impersonatingUserName);
        currentSessionInfo.setStarted(currentSessionStartTime);
        Session session = getSession(false);
        session.setAttribute(SESSION_INFO_PROP, EmlogisUtils.serializeObject(currentSessionInfo));
        
        // and add impersonation info to client info
        clientSessionInfo.setImpersonatingUserName(impersonatingUserName);
        
        return clientSessionInfo;
    }

    public SessionInfo unimpersonateSession() {
        // TODO close current session cleanly and log this into ES ... ideally we should have 2 records / close current start new one

        Subject subject = SecurityUtils.getSubject();
        // get session info related to current user
        BackendSessionInfo currentSessionInfo = getSessionInfo(false);
        String impersonatingUserId = currentSessionInfo.getImpersonatingUserId();
        String impersonatingUserName = currentSessionInfo.getImpersonatingUserName();
        long currentSessionStartTime = currentSessionInfo.getStarted();

    	// check we are really currently impersonating a user
    	if (StringUtils.isBlank(impersonatingUserId)) {
    		throw new ValidationException(
                    "Invalid request to Un-Impersonate, current session is not impersonating an account");
    	}
    	// check impersonating account still exist (edge condition)
        UserAccount userAccount = (UserAccount) userAccountService.getAccount(new PrimaryKey(
                currentSessionInfo.getTenantId(),impersonatingUserId));
    	if (userAccount == null) {
    		throw new ValidationException("Unable to retrieve Impersonating account");
    	}
                
        // switch to previous user 
        Tenant tenant = tenantService.getTenant(currentSessionInfo.getTenantId());
        SessionInfo clientSessionInfo = createSessionInfo(subject, tenant,
                userAccount.getLogin(), false);
        
        // clear impersonating user info and update new backend info
        currentSessionInfo = getSessionInfo(false);
        Session session = getSession(false);
        currentSessionInfo.setStarted(currentSessionStartTime);
        session.setAttribute(SESSION_INFO_PROP, EmlogisUtils.serializeObject(currentSessionInfo));

        return clientSessionInfo;
    }
    
    /**
     * retrieve a session and 'touches' the session
     * @param token
     * @return
     */
    public BackendSessionInfo getSessionInfo(String token) {
    	return getSessionInfo(token, true);
	}

    /**
     * retrieve a session and optionally 'touches' the session unless it has remember be option in which case touch is disabled
     * @param token
     * @return
     */
    public BackendSessionInfo getSessionInfo(String token, boolean touch) {
		logger.debug("Session retrieve: from token " + token);

        Subject subject = new Subject.Builder().sessionId(token).buildSubject();
		Session session = subject.getSession(false);
		logger.debug("Session retrieved from token Authenticated: " + subject.isAuthenticated());
		if (!subject.isAuthenticated() || session == null) {
			return null;
		}
		logger.debug("Session: " + session.getId() + " from: " + session.getHost() + " to:" +
                session.getTimeout() / 1000 + "sec., started: " + session.getStartTimestamp() + " last accessed: " +
                session.getLastAccessTime());
        // TODO check how we can cleanup the state of the thread after the REST resource has executed
		ThreadState threadState = new SubjectThreadState(subject);
		threadState.bind();
		BackendSessionInfo sessionInfo = (BackendSessionInfo) EmlogisUtils.deserializeObject(
                (byte[]) session.getAttribute(SESSION_INFO_PROP));
		if (touch && !sessionInfo.isRememberMe()) {
			session.touch();
		}
		return sessionInfo;
	}

	/**
	 * method that gets the tenant associated to a session without 'touching the session' 
	 * NOTE: this method is to be used by servlets only, which do not go through the std security layer
	 * other situations (like REST APIs) should use the getTenantId() without parameter API call.
	 * 
	 * @param tokenId
	 * @return
	 */
	public String getTenantId(String tokenId) {
		BackendSessionInfo session = getSessionInfo(tokenId, false);
		return (session == null ? null : session.getTenantId());
	}
	
    public boolean isSessionAlive() {
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession(false);
        return subject.isAuthenticated() && session != null
                && (session.getStartTimestamp().getTime() + session.getTimeout()) > System.currentTimeMillis();
    }

    public String getMessage(String code, Object... params) {
        BackendSessionInfo sessionInfo = getSessionInfo(false);
        String language = sessionInfo.getLanguage();

        return resourcesBundle.getMessage(language, code, params);
    }

    public void updateAllSessionsACL() {
        IMap<Serializable, byte[]> sessionMap = hazelcastClientService.getActiveSessions();
        for (Serializable id : sessionMap.keySet()) {
            Session session = (Session) EmlogisUtils.deserializeObject(sessionMap.get(id));
            BackendSessionInfo backendSessionInfo = (BackendSessionInfo) EmlogisUtils.deserializeObject(
                    (byte[]) session.getAttribute(SESSION_INFO_PROP));

            PrimaryKey primaryKey = new PrimaryKey(backendSessionInfo.getTenantId(), backendSessionInfo.getUserId());
            UserAccount userAccount = userAccountService.getUserAccount(primaryKey);
            AccountACL accountACL = loadAccountACL(userAccount, backendSessionInfo);
            backendSessionInfo.setAcls(accountACL);

            session.setAttribute(SESSION_INFO_PROP, EmlogisUtils.serializeObject(backendSessionInfo));

            long time = System.currentTimeMillis() - session.getLastAccessTime().getTime();

            byte[] sessionBytes = EmlogisUtils.serializeObject(session);
            sessionMap.put(id, sessionBytes, session.getTimeout() - time, TimeUnit.MILLISECONDS);
        }
    }

	public void closeSession(Serializable tokenId) {
        Map<Serializable, byte[]> sessionMap = hazelcastClientService.getActiveSessions();
        sessionMap.remove(tokenId);
        ThreadContext.unbindSubject();
	}

    public void touch() {
        Session session = getSession(false);
        session.touch();
    }

	/**
	 * get tenantId associated to currently logged API client
	 * @return tenantId
	 */
	public String getTenantId() {
		return getSessionInfo(false).getTenantId();
	}

	/**
	 * get TokenId associated to currently logged API client
	 * @return tokenId
	 */
	public String getTokenId() {
		return getSessionInfo(false).getToken();
	}

	/**
	 * get userName (login) associated to currently logged API client
	 * @return login
	 */
	public String getUserName() {
		return getSessionInfo(false).getUserName();
	}

	/**
	 * get Id associated to currently logged API client
	 * @return userId
	 */
	public String getUserId() {
		return getSessionInfo(false).getUserId();
	}

	/**
	 * get Id associated to currently logged API Employee (can be null if client has an account but is not an employee
	 * @return userId
	 */
	public String getEmployeeId() {
		return getSessionInfo(false).getEmployeeId();
	}
	
	/**
	 * get ImpersonatingUserId associated to currently logged API client
	 * @return ImpersonatingUserId
	 */
	public String getImpersonatingUserId() {
		return getSessionInfo(false).getImpersonatingUserId();
	}

	/**
	 * get ImpersonatingUserName associated to currently logged API client
	 * @return ImpersonatingUserName
	 */
	public String getImpersonatingUserName() {
		return getSessionInfo(false).getImpersonatingUserName();
	}

	/**
	 * get ActualUserId associated to currently logged API client
	 * @return ActualUserId
	 */
	public String getActualUserId() {
		BackendSessionInfo  csi = getSessionInfo(true);
		return csi != null ? csi.getActualUserId() : null;
	}

	/**
	 * get ActualUserName associated to currently logged API client
	 * @return ActualUserName
	 */
	public String getActualUserName() {
		BackendSessionInfo  csi = getSessionInfo(true);
		return csi != null ? csi.getActualUserName() : null;
	}
	
	public boolean hasPermission(Permissions perm) {
		BackendSessionInfo backendSessionInfo = getSessionInfo(false);		
		return backendSessionInfo.hasPermission(perm);
	}
	
    /**
     * getAcl() returns all ACLs associated with current account
     *      
     * @return
     */
    public AccountACL getAcl() {
        return getAcl(null);
    }
    
    /**
     * getAcls() returns all ACLs associated with current account, for a specific class, or all ACls if unspecified (null)
     * 
     * IMPORTANT NOTE: Foe users with 'RoleMgmt' PERMISSION, returns null, which bypasses ACL controls
     *      
     * @param entityClass
     * @return
     */
    private AccountACL getAcl(String entityClass) {
    	BackendSessionInfo backendSessionInfo = getSessionInfo(false);
    	if (backendSessionInfo.hasPermission(Permissions.Role_Mgmt)) {
    		return null;
    	}
    	
    	// TODO return AccountACL from BackendSessionInfo directly vs querying the database (which has the benefit of loading always up todate ACLs)  
    	String userId = backendSessionInfo.getActualUserId();
    	UserAccount userAccount = userAccountService.getUserAccount(new PrimaryKey(getTenantId(),userId));
        Set<AccountACE> allAces = userAccountService.getAcl(userAccount);
        AccountACL result = new AccountACL();
        for (AccountACE ace: allAces) {
        	if (entityClass == null || StringUtils.equals(entityClass, ace.getEntityClass())) {
        		result.addAce(ace);
        	}
        }
        return result;
    }

	/**
	 * store AuditRecord prepared by SecurityFilter in session, so that it can be retrieved by AuditInterceptor
	 */
	public void setAuditRecord(AuditRecord auditRecord) {
		// for holding the AuditRecord, we use a ThreadLocal variable vs the Session as on session termination
		// the AuditRecord would be lost at the time the AuditInterceptor needs it
		AuditContext auctx = new AuditContext(auditRecord);
	}

	/**
	 * get AuditRecord prepared by SecurityFilter
	 * @return AuditRecord
	 */
	public AuditRecord getAuditRecord() {
		return AuditContext.get();
	}

	/**
	 * get getSessionInfo object associated to currently logged API client
	 *
	 * @param softFail: if true, returns null on failure. if false, throws an exception on failure
	 * @return BackendSessionInfo
	 */
	private BackendSessionInfo getSessionInfo(boolean softFail) {
        Session session = getSession(softFail);
        if (session == null) {
        	// we are in big trouble. we should have a valid open session at this point
        	// TODO LOG this error & throw Exception if hardFail
        	if (softFail) {
        		return null;
        	}
        	throw new IllegalStateException(
                    "Invalid call to getSessionInfo() client not authenticated or session is terminated");
        }
        BackendSessionInfo csi = (BackendSessionInfo) EmlogisUtils.deserializeObject(
                (byte[]) session.getAttribute(SESSION_INFO_PROP));
        if (csi == null) {
        	// we are in big trouble in that case too. we should have a session info object at this point
        	// TODO LOG this error & throw Exception if hardFail
        	if (softFail) {
        		return null;
        	}
        	throw new IllegalStateException(
                    "Invalid call to getSessionInfo() client authenticated but session is terminated");
        }
        return csi;
	}

	/**
	 * get getSessionInfo object associated to currently logged API client
	 *
	 * @param softFail: if true, returns null on failure. if false, throws an exception on failure
	 * @return Session
	 */
	private Session getSession(boolean softFail) {
        Subject subject = SecurityUtils.getSubject();
		logger.debug("getSessionInfo() Authenticated: " + subject.isAuthenticated());
        Session session = subject.getSession();
        if (!subject.isAuthenticated() || session == null) {
        	// we are in big trouble. we should have a valid open session at this point
        	// TODO LOG this error & throw Exception
        	if (softFail) {
        		return null;
        	}
        	throw new IllegalStateException(
                    "Invalid call to getSessionInfo() client not authenticated or session is terminated");
        }
        return session;
	}

    private SessionInfo createSessionInfo(Subject subject, Tenant tenant, String login, boolean readOnly) {
        Session session = subject.getSession(false);
        String sessionId = session.getId().toString();
        String tenantId = tenant.getTenantId();

        // create the client session info object
        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.setToken(sessionId);
        sessionInfo.setLang("en");

        // create the backend one
        BackendSessionInfo backendSessionInfo = new BackendSessionInfo();
        backendSessionInfo.setToken(sessionId);
        backendSessionInfo.setTenantId(tenantId);
        backendSessionInfo.setTenantName(tenant.getName());

        if (StringUtils.equals(tenantId, Organization.DEFAULT_SERVICEPROVIDER_ID)) {
        	backendSessionInfo.setTenantType( TenantType.SvcProvider);
        }
        
        // add remote client info if available
        AuditRecord ar = getAuditRecord();
        if (ar != null) {
        	backendSessionInfo.setClientHostname(ar.getClientHostname());
        	backendSessionInfo.setClientIPAddress(ar.getClientIPAddress());
        	backendSessionInfo.setClientOrigin(ar.getClientOrigin());
        	backendSessionInfo.setClientUserAgent(ar.getClientUserAgent());
        }

        // load User Account to get info and 
        // to get list of permissions and roles associated to Account, then add them to client and backend session info objects
        UserAccount userAccount = userAccountService.getUserAccountBylogin(tenantId, login);
        if (StringUtils.isNotEmpty(userAccount.getLanguage())) {
            backendSessionInfo.setLanguage(userAccount.getLanguage());
        } else {
            backendSessionInfo.setLanguage(tenant.getLanguage());
        }
        Set<Object> permissionsAndRoles = userAccountService.getPermissionsAndRoles(userAccount, readOnly);
        for (Object object : permissionsAndRoles) {
            if (object instanceof Role) {
                Role role = (Role) object;
                backendSessionInfo.addRole(role.getId());
                sessionInfo.addRole(role.getId());
            } else {
                Permission permission = (Permission) object;
                backendSessionInfo.addPermission(permission.getId());
                sessionInfo.addPermission(permission.getId().getValue());
            }
        }
        backendSessionInfo.setUserId(userAccount.getId());
        backendSessionInfo.setUserName(userAccount.getName());
		DateTimeZone tz = accountUtilService.getActualTimeZone(userAccount);
		backendSessionInfo.setEmployeeTimeZone(tz);

        Employee employee = userAccount.getEmployee() ;
        if (employee != null) {
        	// if account associated to an employee, get employee info: id, name, timezone
        	backendSessionInfo.setEmployeeId(userAccount.getEmployee().getId());
        	backendSessionInfo.setEmployeeName(userAccount.getEmployee().getFirstName() + " " + userAccount.getEmployee().getLastName());
        	if (employee.getActivityType() != EmployeeActivityType.Inactive) {
        		backendSessionInfo.setSchedulableEmployee(true);
        	}   	
        }
        
        // now permissions are set, load ACLs
    	// unless user has RoleMgmt permission (which bypasses ACLs), 
        AccountACL accountACL = loadAccountACL(userAccount, backendSessionInfo);

        backendSessionInfo.setAcls(accountACL);

        sessionInfo.setUserId(backendSessionInfo.getUserId());	
        sessionInfo.setUserName(backendSessionInfo.getUserName());
    	sessionInfo.setEmployeeId(backendSessionInfo.getEmployeeId());
    	sessionInfo.setEmployeeName(backendSessionInfo.getEmployeeName());
    	sessionInfo.setEmployeeTimeZone(backendSessionInfo.getEmployeeTimeZone());
        sessionInfo.setTenantType(backendSessionInfo.getTenantType());	
        sessionInfo.setSchedulableEmployee(backendSessionInfo.isSchedulableEmployee());

        // associate backend info to session
        session.setAttribute(SESSION_INFO_PROP, EmlogisUtils.serializeObject(backendSessionInfo));
        logger.debug("Session created with id: " + sessionId);

        // TODO check how we can cleanup the state of the thread after the REST resource has executed
        ThreadState threadState = new SubjectThreadState(subject);
        threadState.bind();

        // at this state, the security filter is supposed prior to invoking the resource, to have added the Audit Record
        // which needs to be enriched with session information
        AuditRecord auditRecord = getAuditRecord();
        if (auditRecord != null) {
            auditRecord.setSessionId(backendSessionInfo.getToken());
            auditRecord.setTenantId(backendSessionInfo.getTenantId());
            auditRecord.setTenantName(backendSessionInfo.getTenantName());
            auditRecord.setUserId(backendSessionInfo.getUserId());
            auditRecord.setUserName(backendSessionInfo.getUserName());
        }
        return sessionInfo;
    }

    @Validation
    public void changePassword(
            @ValidatePassword(tenantField = Constants.TENANT_ID, passwordField = "newPassword")
            ChangePasswordDto changePasswordDto,
            URI uri) throws PasswordViolationException {
        UserAccount account = userAccountService.getUserAccountBylogin(changePasswordDto.getTenantId(),
                changePasswordDto.getLogin());
        if (account != null) {
            validateResetPassword(account);
            if (!StringUtils.equals(account.getPassword(), changePasswordDto.getOldPassword())) {
                throw new PasswordViolationException(resourcesBundle.getMessage(account.getLanguage(),
                        "validation.password.incorrect"));
            } else {
                userAccountService.changePassword(account, changePasswordDto.getNewPassword(), uri);
            }
        }
    }

    public void changePasswordConfirmation(String confirmationId) {
        userAccountService.changePasswordConfirmation(confirmationId);
    }

    public void validatePasswordOnLogin(String tenantId, String login, String password)
            throws EmLogisCredentialsException {
        UserAccount userAccount = userAccountService.getUserAccountBylogin(tenantId, login);
        PasswordPolicies passwordPolicies = tenantService.getPasswordPolicies(tenantId);

        try {
            updateUserStatusDueToPassword(userAccount, passwordPolicies, password);
        } catch (InvalidLoginException e) {
            throw e;
        } catch (EmLogisCredentialsException e) {
            String decodedPassword = passwordCoder.decode(password);

            if (!(AccountStatus.Locked.equals(userAccount.getStatus())
                    || AccountStatus.Suspended.equals(userAccount.getStatus()))) {
                String history = createUnsuccessfulLoginHistory(userAccount, decodedPassword);
                userAccount.setUnsuccessfulLoginHistory(history);
                checkMaxUnsuccessfulLogins(userAccount, passwordPolicies);
                userAccountService.update(userAccount);
            }

            throw e;
        }
    }

    public void updateUserStatusDueToPasswordPolicies(UserAccount userAccount, PasswordPolicies passwordPolicies)
            throws EmLogisCredentialsException {
        updateUserStatusDueToPassword(userAccount, passwordPolicies, userAccount.getPassword());
    }

    public void updateUserStatusDueToPassword(UserAccount userAccount, PasswordPolicies passwordPolicies,
                                              String password) throws EmLogisCredentialsException {
        try {
            checkPasswordPolicies(userAccount, passwordPolicies, password);
        } catch (EmLogisCredentialsException e) {
            boolean modified = false;

            if (e instanceof ForceChangeOnFirstLogonException) {
                userAccount.setStatus(AccountStatus.PendingPwdChange);
                userAccount.setLastLogged(new DateTime());

                modified = true;
            } else {
                if (e instanceof PasswordViolationException || e instanceof ExpiredPasswordException) {
                    userAccount.setStatus(AccountStatus.PendingPwdChange);

                    modified = true;
                }
            }

            if (modified) {
                userAccountService.update(userAccount);
            }

            throw e;
        }
    }

    public boolean checkMaxUnsuccessfulLogins(UserAccount userAccount, PasswordPolicies passwordPolicies) {
        String history = userAccount.getUnsuccessfulLoginHistory();

        if (history != null) {
            String[] logins = history.split(";");
            if (logins.length >= passwordPolicies.getMaxUnsucessfullLogin()) {
                if (UnsucessfullLoginAction.LOCK.equals(passwordPolicies.getUnsucessfullLoginAction())) {
                    userAccount.setStatus(AccountStatus.Locked);
                } else if (UnsucessfullLoginAction.SUSPEND.equals(passwordPolicies.getUnsucessfullLoginAction())) {
                    userAccount.setStatus(AccountStatus.Suspended);
                }

                return true;
            }
        }

        return false;
    }

    private void checkPasswordPolicies(UserAccount userAccount, PasswordPolicies passwordPolicies, String password)
            throws EmLogisCredentialsException {
        long currentTime = System.currentTimeMillis();

        if (userAccount == null) {
            throw new InvalidLoginException();
        }

        if (AccountStatus.Locked.equals(userAccount.getStatus())) {
            throw new AccountLockedException();
        }

        if (AccountStatus.Suspended.equals(userAccount.getStatus())
                || userAccount.getAccountSuspendedUntil() != null
                    // we are considering that date less this constant (2000/01/01) is invalid
                    && userAccount.getAccountSuspendedUntil().isAfter(Constants.DATE_2000_01_01)
                    && userAccount.getAccountSuspendedUntil().isAfter(currentTime)) {
            throw new AccountSuspendedException();
        }

        if (userAccount.getAccountValidityDate() > Constants.DATE_2000_01_01
                && userAccount.getAccountValidityDate() < currentTime) {
            throw new ExpiredPasswordException();
        }

        if (AccountStatus.PendingPwdChange.equals(userAccount.getStatus())) {
            throw new PendingPasswordChangeException();
        }

        if (passwordPolicies.isForceChangeOnFirstLogon()
                && ((userAccount.getLastLogged() == null ||
                        userAccount.getLastLogged().getMillis() <= Constants.DATE_2000_01_01)
                    && AccountStatus.Active.equals(userAccount.getStatus()))) {
            throw new ForceChangeOnFirstLogonException(getMessage("validation.password.first.login.change"));
        }

        String decodedPassword = passwordCoder.decode(password);
        String violations = PasswordUtils.getPasswordViolations(passwordPolicies, decodedPassword);
        if (StringUtils.isNotEmpty(violations)) {
            throw new PasswordViolationException(violations);
        }
    }

    private String createUnsuccessfulLoginHistory(UserAccount userAccount, String password) {
        String result = userAccount.getUnsuccessfulLoginHistory();
        if (result == null) {
            result = "";
        }
        if (StringUtils.isNotEmpty(result)) {
            result += ";";
        }
        result += System.currentTimeMillis() + "," + password;
        return result;
    }

    private AccountACL loadAccountACL(UserAccount userAccount, BackendSessionInfo backendSessionInfo) {
        AccountACL result = new AccountACL();
        if (!backendSessionInfo.hasPermission(Permissions.Role_Mgmt)) {
            Set<AccountACE> acl = userAccountService.getAcl(userAccount);
            for (AccountACE ace: acl) {
                // restrict acl permissions to role permissions
                // (in case the ACL contains a permission not part of account Roles)
                List<Permissions> permissionsToBeRemoved = new ArrayList<>();
                for (Permissions perm : ace.getPermissions()) {
                    if (!backendSessionInfo.hasPermission(perm)) {
                        permissionsToBeRemoved.add(perm);
                    }
                }
                ace.removePermissions(permissionsToBeRemoved);
                result.addAce(ace);
            }
        }
        return result;
    }

	/**
	 * retrieveEmployeeId() retrieve an employee from its tenant and login information only. returns null if not found 
	 * 
	 * NOTE: TO BE USED ONLY FOR password lost & reset use case.
	 * CAN BE A SECURITY HOLE. USAGE TO BE OBSERVED AND POSSIBLY WE WILL NEED TO REMOVE THAT FUNCTIONALITY LATER ON.
	 * 
	 * @param
	 * @return
	 */
	public String retrieveEmployeeId(String tenantId, String login) {
		String employeeId = null;
		UserAccount account  = userAccountService.getUserAccountBylogin(tenantId, login);
		if (account != null) {
			if (account.getEmployee() != null) {
				employeeId = account.getEmployee().getId();
			}	
		}
		return employeeId;
	}


	/**
	 * retrieveAccountId() retrieve an account from its tenant and login information only. returns null if not found 
	 * 
	 * NOTE: TO BE USED ONLY FOR password lost & reset use case.
	 * CAN BE A SECURITY HOLE. USAGE TO BE OBSERVED AND POSSIBLY WE WILL NEED TO REMOVE THAT FUNCTIONALITY LATER ON.
	 * 
	 * @param
	 * @return
	 */
	public String retrieveAccountId(String tenantId, String login) {
		UserAccount account  = userAccountService.getUserAccountBylogin(tenantId, login);
		return  (account != null ?  account.getId() : null);
	}

    public void validateResetPassword(UserAccount userAccount) {
        switch (userAccount.getStatus()) {
            case Revoked:
                throw new ValidationException(resourcesBundle.getMessage(userAccount.getLanguage(),
                        "validate.account.state.revoked"));
            case Locked:
                throw new ValidationException(resourcesBundle.getMessage(userAccount.getLanguage(),
                        "validate.account.state.locked"));
        }
    }

    private void validateAccountState(UserAccount userAccount) {
        switch (userAccount.getStatus()) {
            case Revoked:
                throw new ValidationException(resourcesBundle.getMessage(userAccount.getLanguage(),
                        "validate.account.state.revoked"));
            case Locked:
                throw new ValidationException(resourcesBundle.getMessage(userAccount.getLanguage(),
                        "validate.account.state.locked"));
            case Suspended:
                throw new ValidationException(resourcesBundle.getMessage(userAccount.getLanguage(),
                        "validate.account.state.suspended"));
            case PendingConfirmation:
                throw new ValidationException(resourcesBundle.getMessage(userAccount.getLanguage(),
                        "validate.account.state.pendingconfirmation"));
            case PendingPwdChange:
                throw new ValidationException(resourcesBundle.getMessage(userAccount.getLanguage(),
                        "validate.account.state.pendingpwdchange"));
        }
    }

}
