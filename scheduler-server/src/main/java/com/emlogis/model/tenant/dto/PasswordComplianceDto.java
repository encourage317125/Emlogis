package com.emlogis.model.tenant.dto;

import com.emlogis.model.dto.ReadDto;
import com.emlogis.model.tenant.AccountStatus;
import org.drools.core.util.StringUtils;

public class PasswordComplianceDto extends ReadDto {

    private String id;
    private String login;
    private String email;
    private boolean firstLogin;
    private AccountStatus accountStatus;
    private	String violations;

	public PasswordComplianceDto() {
		this.setClName(this.getClass().getSimpleName());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isFirstLogin() {
		return firstLogin;
	}

	public void setFirstLogin(boolean firstLogin) {
		this.firstLogin = firstLogin;
	}

	public AccountStatus getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(AccountStatus accountStatus) {
		this.accountStatus = accountStatus;
	}

	public String getViolations() {
		return violations;
	}

	public void setViolations(String violations) {
		this.violations = violations;
	}

	public boolean isAccountCompliant() {
		return StringUtils.isEmpty(violations);
	}

}
