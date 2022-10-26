package com.emlogis.rest.security;

import com.emlogis.common.security.AccountACL;
import com.emlogis.common.security.Permissions;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;

import javax.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement
public class BackendSessionInfo implements Serializable {
	
	public enum TenantType {
		Customer, SvcProvider
	}

    private	String token;
    private	String tenantId;
    private	String tenantName;
    private	String userId;                                  // account id
    private	String userName;                                // account name
	private String employeeId;                              // employee id
	private String employeeName;                            // employee name
	private boolean	schedulableEmployee = false;
    private DateTimeZone employeeTimeZone = DateTimeZone.UTC; // employee TimeZone

    private	Set<String>	roles = new HashSet<>();            // set of roles (for fast testing)
    private	Set<Permissions> permissions = new HashSet<>();	// set of permissions (for fast testing)
    private	AccountACL acls;								// set of ACLs
    private	String impersonatingUserId;                     // account id of impersonating user
    private	String impersonatingUserName;                   // name of impersonating user
    private	String language;                                  // language of user
    private	long started;									// datetime session was created
    
    														// cleint connexion info
	private	String clientIPAddress;
	private	String clientHostname;
	private	String clientUserAgent;							// user agent field from HTTP header
	private	String clientOrigin;							// origin HTTP header field
	private TenantType tenantType = TenantType.Customer;	
	
	// remember me attributes
	private boolean rememberMe = false;
	private String  rememberMeClientId;
	private String 	rememberMeClientDescr;

    // TODO add session mngt attributes such as creation date, touch date, etc ...

    public BackendSessionInfo() {
		super();
		started = System.currentTimeMillis();
	}

    public String getToken() {
        return token;
    }

	public void setToken(String token) {
        this.token = token;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public DateTimeZone getEmployeeTimeZone() {
		return employeeTimeZone;
	}

	public void setEmployeeTimeZone(DateTimeZone employeeTimeZone) {
		this.employeeTimeZone = employeeTimeZone;
	}

	public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void addRole( String role){
        this.roles.add( role);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public Set<Permissions> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permissions> permissions) {
        this.permissions = permissions;
    }

    public void addPermission(Permissions permission){
        this.permissions.add( permission);
    }

    public boolean hasPermission(Permissions permission) {
        return permissions.contains(permission);
    }

	public AccountACL getAcls() {
		return acls;
	}

	public void setAcls(AccountACL acls) {
		this.acls = acls;
	}

	public String getImpersonatingUserId() {
		return impersonatingUserId;
	}

	public void setImpersonatingUserId(String impersonatingUserId) {
		this.impersonatingUserId = impersonatingUserId;
	}

	public String getImpersonatingUserName() {
		return impersonatingUserName;
	}

	public void setImpersonatingUserName(String impersonatingUserName) {
		this.impersonatingUserName = impersonatingUserName;
	}

    public long getStarted() {
		return started;
	}

	public void setStarted(long started) {
		this.started = started;
	}

	public String getActualUserId() {
        if (StringUtils.isNotEmpty(impersonatingUserId)) {
            return impersonatingUserId;
        } else {
            return userId;
        }
    }

    public String getActualUserName() {
        if (StringUtils.isNotEmpty(impersonatingUserName)) {
            return impersonatingUserName;
        } else {
            return userName;
        }
    }

	public String getClientIPAddress() {
		return clientIPAddress;
	}

	public void setClientIPAddress(String clientIPAddress) {
		this.clientIPAddress = clientIPAddress;
	}

	public String getClientHostname() {
		return clientHostname;
	}

	public void setClientHostname(String clientHostname) {
		this.clientHostname = clientHostname;
	}

	public String getClientUserAgent() {
		return clientUserAgent;
	}

	public void setClientUserAgent(String clientUserAgent) {
		this.clientUserAgent = clientUserAgent;
	}

	public String getClientOrigin() {
		return clientOrigin;
	}

	public void setClientOrigin(String clientOrigin) {
		this.clientOrigin = clientOrigin;
	}
	
    public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}


	public TenantType getTenantType() {
		return tenantType;
	}

	public void setTenantType(TenantType tenantType) {
		this.tenantType = tenantType;
	}

	public String getClName() {                        // used and required by toDto conversion
        return this.getClass().getSimpleName();
    }

	public boolean isSchedulableEmployee() {
		return schedulableEmployee;
	}

	public void setSchedulableEmployee(boolean schedulableEmployee) {
		this.schedulableEmployee = schedulableEmployee;
	}

	public boolean isRememberMe() {
		return rememberMe;
	}

	public void setRememberMe(boolean rememberMe) {
		this.rememberMe = rememberMe;
	}

	public String getRememberMeClientId() {
		return rememberMeClientId;
	}

	public void setRememberMeClientId(String rememberMeClientId) {
		this.rememberMeClientId = rememberMeClientId;
	}

	public String getRememberMeClientDescr() {
		return rememberMeClientDescr;
	}

	public void setRememberMeClientDescr(String rememberMeClientDescr) {
		this.rememberMeClientDescr = rememberMeClientDescr;
	}


}
