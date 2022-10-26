package com.emlogis.model.notification;

public enum NotificationStatusType {
	
	PENDING("PENDING"),
	PROCESSING("PROCESSING"),
	PROCESSED("PROCESSED"),
	ARCHIVING("ARCHIVING"),
	ARCHIVED("ARCHIVED"),
	RETRYING("RETRYING"),
	FAILED("FAILED");
	
    private String value;

    NotificationStatusType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
