package com.emlogis.common.security;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTimeZone;

import com.emlogis.rest.security.BackendSessionInfo.TenantType;

import java.util.HashMap;
import java.util.Map;

@XmlRootElement
public class SessionInfo {
		
	private String 	token;
	private String 	userId;                                	 	// account id
	private String 	userName;                                   // account name
	private String 	employeeId;                                 // emmployee id
	private String 	employeeName;                               // emmployee name
    private DateTimeZone employeeTimeZone = DateTimeZone.UTC; 	// employee TimeZone

	private Map<String, Boolean> roles = new HashMap<>();
	private Map<String, Boolean> permissions = new HashMap<>();
	private String 	lang;			                            // language
    private	String 	impersonatingUserName;						// name of impersonating user (if any)
	private TenantType tenantType = TenantType.Customer;							
	private boolean	schedulableEmployee = false;


	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
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

	public Map<String,Boolean> getRoles() {
		return roles;
	}

	public void setRoles(Map<String, Boolean> roles) {
		this.roles = roles;
	}

	public void addRole(String role) {
		roles.put(role, true);
	}

	public Map<String, Boolean> getPermissions() {
		return permissions;
	}

	public void setPermissions(Map<String, Boolean> permissions) {
		this.permissions = permissions;
	}

	public void addPermission(String permission) {
		permissions.put(permission, true);
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getImpersonatingUserName() {
		return impersonatingUserName;
	}

	public void setImpersonatingUserName(String impersonatingUserName) {
		this.impersonatingUserName = impersonatingUserName;
	}

	public TenantType getTenantType() {
		return tenantType;
	}

	public void setTenantType(TenantType tenantType) {
		this.tenantType = tenantType;
	}

	public boolean isSchedulableEmployee() {
		return schedulableEmployee;
	}

	public void setSchedulableEmployee(boolean schedulableEmployee) {
		this.schedulableEmployee = schedulableEmployee;
	}

	
}