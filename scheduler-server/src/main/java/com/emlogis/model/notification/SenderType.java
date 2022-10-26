package com.emlogis.model.notification;

public enum SenderType {
	
	SYSTEM("SYSTEM"),
	ACCOUNT("ACCOUNT");
	
    private String value;

    SenderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
