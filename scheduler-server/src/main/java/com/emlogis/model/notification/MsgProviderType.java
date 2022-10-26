package com.emlogis.model.notification;

public enum MsgProviderType {
	
	Twillio("Twillio"),
	POPSMTPEmail("POPSMTPEmail");

    private String value;

    private MsgProviderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
