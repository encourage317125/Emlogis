package com.emlogis.model.tenant;

public enum ModuleStatus {
	
	Disabled("Disabled"),
	Trial("Trial"),
	TrialExpired("TrialExpired"),
	Subscribed("Subscribed"),
	SubscriptionExpired("SubscriptionExpired");
	
	private String value;
	
	private ModuleStatus(String value) {
		this.value = value;
	}

	private void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}
