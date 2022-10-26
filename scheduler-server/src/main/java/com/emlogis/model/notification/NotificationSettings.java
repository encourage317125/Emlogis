package com.emlogis.model.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity()
public class NotificationSettings {

	@Id()
    @Column(unique = true, length = 64)
    private String id;
	
	private int retryCount;
	private int maxDeliveryHours;
	private int queueProcessingSize;
	private int notificationExpirationHours;

    public NotificationSettings() {}

    public NotificationSettings(int retryCount, int maxDeliveryHours, int queueProcessingSize, int notificationExpirationHours) {
        this.retryCount = retryCount;
        this.maxDeliveryHours = maxDeliveryHours;
        this.queueProcessingSize = queueProcessingSize;
        this.notificationExpirationHours = notificationExpirationHours;
    }

    public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getRetryCount() {
		return retryCount;
	}
	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}
	public int getMaxDeliveryHours() {
		return maxDeliveryHours;
	}
	public void setMaxDeliveryHours(int maxDeliveryHours) {
		this.maxDeliveryHours = maxDeliveryHours;
	}

	public int getQueueProcessingSize() {
		return queueProcessingSize;
	}

	public void setQueueProcessingSize(int queueProcessingSize) {
		this.queueProcessingSize = queueProcessingSize;
	}

	public int getNotificationExpirationHours() {
		return notificationExpirationHours;
	}

	public void setNotificationExpirationHours(int notificationExpirationHours) {
		this.notificationExpirationHours = notificationExpirationHours;
	}	
}
