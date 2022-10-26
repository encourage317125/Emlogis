package com.emlogis.model.notification;

public enum MsgDeliveryType {
	
	SMS("SMS"),
	EMAIL("EMAIL");

    private String value;

    private MsgDeliveryType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
