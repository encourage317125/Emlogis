package com.emlogis.rest.security;

import com.emlogis.common.security.Permissions;
import com.emlogis.common.security.Security;
import com.emlogis.rest.auditing.AuditRecord;
import com.emlogis.rest.auditing.Audited;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;

@Priority(Priorities.AUTHENTICATION)
@Provider
public class SecurityFilter implements ContainerRequestFilter {
	
	private final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    public final static String AUTH_ACCESS_DENIED = "Access denied for this resource. Authentication required";
    public final static String ACCESS_FORBIDDEN = "Access to this resource is forbidden";
    public final static String NO_RESOURCE_METHOD = "Failed to get target REST Resource method:";

	@Inject	
	private SessionService sessionService;

	public SecurityFilter() {
		super();
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		// get REST resource method invoked.
		Method resourceMethod = getRESTResourceMethod(requestContext);
		Class resourceClass = resourceMethod.getDeclaringClass();
		
		//Get request headers
	    MultivaluedMap<String, String> headers = requestContext.getHeaders();
	    
        // prepare an Audit record that will be returned either directly by the security filter, 
	    // either by the auditing interceptor. goal is to log an audit record:
	    // - on all calls that fail to go through REST call (generally because of authentication or authorization problems)
	    // - on call that do go through REST resource AND that have Audit specified (via annotation)
	    // - either on 
    	UriInfo uriInfo = requestContext.getUriInfo();
	    AuditRecord auditRecord = new AuditRecord();
	   	auditRecord.setTimestamp(System.currentTimeMillis());
        auditRecord.setToken(requestContext.getHeaderString(Security.TOKEN_HEADER_NAME));
        auditRecord.setResourceClass(resourceClass.getSimpleName());
        auditRecord.setResourceMethod(resourceMethod.getName());
        String url = uriInfo.getAbsolutePath().toString();
        auditRecord.setURL(url);
        String clienthostname = requestContext.getHeaderString("X-Forwarded-For");
        
        if (StringUtils.isNotEmpty(clienthostname)) {
        	clienthostname=clienthostname.split(":")[0];
        }
        auditRecord.setClientHostname(clienthostname);
        auditRecord.setClientUserAgent(requestContext.getHeaderString("User-Agent"));
        auditRecord.setClientOrigin(requestContext.getHeaderString("Origin"));
        auditRecord.setHTTPMethod(requestContext.getMethod());

	    if (resourceClass.isAnnotationPresent(Audited.class) || resourceMethod.isAnnotationPresent(Audited.class)) {    	
	    	Audited audited = (Audited)resourceClass.getAnnotation(Audited.class);
	    	if (audited == null) {
	    		audited = resourceMethod.getAnnotation(Audited.class);
	    	}	    	
	    	// audit requested, enrich audit record with params from annotation
	        auditRecord.setCategory(audited.type());
	        auditRecord.setApiCall(audited.label());
	        auditRecord.setCallCategory(audited.callCategory().getValue());
	        auditRecord.setParamsLogging(audited.paramsLogging());
	        // and make record available to AuditInterceptor via session service 
			sessionService.setAuditRecord(auditRecord);
	    }

	    // check authentication
	    boolean requireAuthentication = resourceMethod.isAnnotationPresent(Authenticated.class)
                || resourceClass.isAnnotationPresent(Authenticated.class);
		if (requireAuthentication) {
			// client must be authenticated. check it is or reply with access 
			logger.debug( "SecurityFilter: " + resourceMethod.getClass().getSimpleName() + "."
                    + resourceMethod.getName() + " REQUIRES authentication");
			BackendSessionInfo sessionInfo = checkAuthentication(headers);
			if (sessionInfo == null) {
				// Note do no longer do the 2 things below, that should be handled by RestExceptionHandler
				//auditRecord.setHTTPReturnCode(Response.Status.UNAUTHORIZED);
	            //esClientService.indexAuditRecord(auditRecord.getTenantId(), auditRecord);
	            throw new AuthenticationException(AUTH_ACCESS_DENIED);
			}
			
			// if Audit on, add session / client info into Audit record, then add audit record to session
            auditRecord.setSessionId(sessionInfo.getToken());
            auditRecord.setTenantId(sessionInfo.getTenantId());
            auditRecord.setTenantName(sessionInfo.getTenantName());
            auditRecord.setUserId(sessionInfo.getUserId());
            auditRecord.setUserName(sessionInfo.getUserName());

			// authentication OK, check permissions
			if (!checkAuthorization(resourceClass, resourceMethod, sessionInfo)) {
				// Note do no longer do the 2 things below, that should be handled by RestExceptionHandler
				//auditRecord.setHTTPReturnCode(Response.Status.FORBIDDEN);
	            //esClientService.indexAuditRecord(auditRecord.getTenantId(), auditRecord);
	            throw new AccessControlException(ACCESS_FORBIDDEN);
			}
		}
		else {
	        // in case tenantId is present in URL, pass it to SessionResource.login() via a header field
	        String tenantId = getTenantIdFromURL(url);
	        if (tenantId != null) {
	        	headers.add(Security.TENANTID_HEADER_NAME, tenantId);
	        }
		}
	}
	
	private  String getTenantIdFromURL(String url) {
		if (StringUtils.contains(url, Security.EMLOGIS_DOMAINNAME)) {
			String s = StringUtils.substringAfter(url, "//");
			String host = StringUtils.substringBefore(s, Security.EMLOGIS_DOMAINNAME);
			if (StringUtils.isNotBlank(host) && ! StringUtils.startsWith(s, Security.EMLOGIS_CLOUD_ID)) {
                return StringUtils.substringBefore(host, ".");
			}
		}
		return null;
	}
	
	private BackendSessionInfo checkAuthentication(MultivaluedMap<String, String> headers) {
		String token = headers.getFirst(Security.TOKEN_HEADER_NAME);
		if (StringUtils.isBlank(token)) {
			return null;
		}
        return sessionService.getSessionInfo(token);
	}
	
	private boolean checkAuthorization(Class resourceclass, Method resourceMethod, BackendSessionInfo sessionInfo) {
		// This method should normally invoke the AuthorizationService, but code is so
		// simple that it is 'inlined' here
		
		// get class level + method level annotations
		// check RequirePermissionIn first
		List<RequirePermissionIn> inPermissionAnnotations = new ArrayList<>();
		if (resourceclass.isAnnotationPresent(RequirePermissionIn.class)) {
			inPermissionAnnotations.add((RequirePermissionIn) resourceclass.getAnnotation(RequirePermissionIn.class));
		}
		if (resourceMethod.isAnnotationPresent(RequirePermissionIn.class)) {
			inPermissionAnnotations.add(resourceMethod.getAnnotation(RequirePermissionIn.class));
		}
		if (inPermissionAnnotations.size() > 0) {
			// check client has one of enumerated permissions 
			logger.debug("SecurityFilter: " + resourceMethod.getClass().getSimpleName() + "." +
                    resourceMethod.getName() + " REQUIRES at least one permission in: ");
			//requestContext.abortWith(this.accessForbidenResponse());
			for (RequirePermissionIn an : inPermissionAnnotations) {
				for (Permissions permission : an.permissions()) {
					if (sessionInfo.hasPermission(permission)) {
						return true;
					}
				}
			}
			// no permission found in set of account permissions. deny access
			return false;
		} else {
			// check RequireAllPermission only difference is that ALL permissions must be met
			List<RequirePermissions> allPermissionAnnotations = new ArrayList<>();
			if (resourceclass.isAnnotationPresent(RequirePermissions.class)) {
				allPermissionAnnotations.add((RequirePermissions) resourceclass.getAnnotation(RequirePermissions.class));
			}
			if (resourceMethod.isAnnotationPresent(RequirePermissions.class)) {
				allPermissionAnnotations.add(resourceMethod.getAnnotation(RequirePermissions.class));
			}
			if (allPermissionAnnotations.size() > 0){
				// check client has all of enumerated permissions 
				logger.debug("SecurityFilter: " + resourceMethod.getClass().getSimpleName() + "."
                        + resourceMethod.getName() + " REQUIRES pemission: ");
				//requestContext.abortWith(this.accessForbidenResponse());
				for (RequirePermissions an : allPermissionAnnotations) {
					for (Permissions permission : an.permissions()) {
						if (!sessionInfo.hasPermission(permission)) {
							// at least one permission is missing. deny access
							return false;
						}
					}
				}
				// all permission found proceed
				return true;
			} else {
				// no permission requirement, proceed
				return true;
			}		
		}
	}

	/**
	 * getRESTResourceMethod() get the target REST resource method.
	 * 
	 * NOTE: CODE RESTEASY DEPENDENT
	 * 
	 * get the REST resource method resolved by REST Easy.
	 * because there is a pb running the project with the libray that contains the org.jboss.resteasy.core.ResourceMethodInvoker.getMethod() method 
	 * we use reflexion to invoke it.
	 * NOTE:aquiring the REST resource invoked method impl has to be changed in case we change AppServer flavor or REST library (like jersey)  
	 * 
	 * @param requestContext
	 * @return
	 */
	private Method getRESTResourceMethod(ContainerRequestContext requestContext) {
		Object resourceMethodInvoker = requestContext.getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");
		logger.debug("object: " + resourceMethodInvoker.getClass().getName());

		try {
			Method method = resourceMethodInvoker.getClass().getMethod("getMethod", new Class[] {});
			return (Method) method.invoke(resourceMethodInvoker);
			// TODO get resource class name !
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new SecurityException(NO_RESOURCE_METHOD, e);
		}
	}
	
}
