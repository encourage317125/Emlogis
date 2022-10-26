package com.emlogis.common.notifications;

import com.emlogis.model.notification.MsgDeliveryType;

public enum NotificationDeliveryMethod {
    CorporateEmail("CorporateEmail", MsgDeliveryType.EMAIL),
    PersonalEmail("PersonalEmail", MsgDeliveryType.EMAIL),
    SMS("SMS", MsgDeliveryType.SMS);                            // TODO think about SMS, SMS could be corporate cell phone or personal phone ...  or alternate phone nb

    private final String value;
    private final MsgDeliveryType msgDeliveryType;

    private NotificationDeliveryMethod(String value, MsgDeliveryType msgDeliveryType) {
        this.value = value;
        this.msgDeliveryType = msgDeliveryType;
    }

    public String getValue() {
        return value;
    }

    public MsgDeliveryType deliveryType() {
        return msgDeliveryType;
    }
}
