package com.emlogis.model.tenant.dto;

import com.emlogis.model.dto.ReadDto;

import java.util.Collection;

public class PasswordComplianceReportDto extends ReadDto {

    private int accountTotal;

    private int accountOK;

    private int accountNOK;
    
    Collection<PasswordComplianceDto> violations;

	public PasswordComplianceReportDto() {}

	public PasswordComplianceReportDto(int accountTotal, int accountOK, int accountNOK,
                                       Collection<PasswordComplianceDto> violations) {
		this.setClName(this.getClass().getSimpleName());
		this.accountTotal = accountTotal;
		this.accountOK = accountOK;
		this.accountNOK = accountNOK;
		this.violations = violations;
	}

	public int getAccountTotal() {
		return accountTotal;
	}

	public void setAccountTotal(int accountTotal) {
		this.accountTotal = accountTotal;
	}

	public int getAccountOK() {
		return accountOK;
	}

	public void setAccountOK(int accountOK) {
		this.accountOK = accountOK;
	}

	public int getAccountNOK() {
		return accountNOK;
	}

	public void setAccountNOK(int accountNOK) {
		this.accountNOK = accountNOK;
	}

	public Collection<PasswordComplianceDto> getViolations() {
		return violations;
	}

	public void setViolations(Collection<PasswordComplianceDto> violations) {
		this.violations = violations;
	}
    
}
