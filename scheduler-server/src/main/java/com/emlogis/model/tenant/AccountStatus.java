package com.emlogis.model.tenant;

public enum AccountStatus {
	
	Active("Active"),
	Suspended("Suspended"),
	Locked("Locked"),
	PendingConfirmation("PendingConfirmation"),
	PendingPwdChange("PendingPwdChange"),
	Revoked("Revoked");
	
	private String value;
	
	private AccountStatus(String value) {
		this.value = value;
	}

	private void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}
