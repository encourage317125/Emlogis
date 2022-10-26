package com.emlogis.model.employee.dto;

import com.emlogis.common.notifications.NotificationDeliveryFormat;
import com.emlogis.common.notifications.NotificationDeliveryMethod;
import com.emlogis.common.notifications.NotificationType;

import java.util.Collection;
import java.util.Map;

public class NotificationSettingDto {

    private String mobilePhone;
    private String homeEmail;
    private String workEmail;
    private Boolean isNotificationEnabled;
    private Collection<NotificationConfigDto> notificationConfigs;
    private Map<NotificationType, Boolean> notificationTypes;

    public static class NotificationConfigDto {
        private Boolean enabled;
        private NotificationDeliveryMethod method;
        private NotificationDeliveryFormat format;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public NotificationDeliveryMethod getMethod() {
            return method;
        }

        public void setMethod(NotificationDeliveryMethod method) {
            this.method = method;
        }

        public NotificationDeliveryFormat getFormat() {
            return format;
        }

        public void setFormat(NotificationDeliveryFormat format) {
            this.format = format;
        }
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getWorkEmail() {
        return workEmail;
    }

    public void setWorkEmail(String workEmail) {
        this.workEmail = workEmail;
    }

    public String getHomeEmail() {
        return homeEmail;
    }

    public void setHomeEmail(String homeEmail) {
        this.homeEmail = homeEmail;
    }

    public Boolean getIsNotificationEnabled() {
        return isNotificationEnabled;
    }

    public void setIsNotificationEnabled(Boolean isNotificationEnabled) {
    	if (isNotificationEnabled == null) {
    		isNotificationEnabled = new Boolean(false);
    	}
        this.isNotificationEnabled = isNotificationEnabled;
    }

    public Collection<NotificationConfigDto> getNotificationConfigs() {
        return notificationConfigs;
    }

    public void setNotificationConfigs(Collection<NotificationConfigDto> notificationConfigs) {
        this.notificationConfigs = notificationConfigs;
    }

    public Map<NotificationType, Boolean> getNotificationTypes() {
        return notificationTypes;
    }

    public void setNotificationTypes(Map<NotificationType, Boolean> notificationTypes) {
        this.notificationTypes = notificationTypes;
    }
}
