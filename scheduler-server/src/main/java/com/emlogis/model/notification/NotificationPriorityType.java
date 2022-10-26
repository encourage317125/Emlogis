package com.emlogis.model.notification;

public enum NotificationPriorityType {	
	LOW("LOW"),
	NORMAL("NORMAL"),
	HIGH("HIGH");
	
    private String value;

    NotificationPriorityType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
