package com.emlogis.common.notifications;

public enum NotificationDeliveryFormat {
	PLAIN_TEXT("PLAIN_TEXT", "text"),
	HTML("HTML", "html"),
    SMS_TEXT("SMS_TEXT", "sms");		// needed ? or we can use plain text may be

    private String value;
    private String formatString;

    NotificationDeliveryFormat(String value, String formatString) {
        this.value = value;
        this.formatString = formatString;
    }

    public String getValue() {
        return value;
    }

    public String formatString() {
        return formatString;
    }
}
