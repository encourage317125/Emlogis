package com.emlogis.model.notification.dto;

import com.emlogis.common.notifications.NotificationCategory;
import com.emlogis.common.notifications.NotificationOperation;
import com.emlogis.model.notification.MsgDeliveryType;
import com.emlogis.model.notification.NotificationStatusType;
import com.emlogis.model.notification.SenderType;

public class NotificationDTO {
	
    private String id;
	
	private String providerId;
	private String providerMessageId;
	private String tenantId;
	private SenderType senderType;
	private String senderUserId;
	
	private String senderName;
	private String senderService;
	
	private String receiverUserId;	
	private String receiverName;
	
	
	private MsgDeliveryType deliveryType;
	private Boolean emailOnly = false;

	private NotificationOperation notificationOperation;
	private NotificationCategory notificationCategory;
	private String fromAddress;
	private String toAddress;
	
    private long queuedOn;
	
    private long tobeDeliveredOn;
	
    private long deliveredOn;
	
	private int retryCount;
	
	private NotificationStatusType status;
	
	private String appServerId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getSenderUserId() {
		return senderUserId;
	}

	public void setSenderUserId(String senderUserId) {
		this.senderUserId = senderUserId;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getSenderService() {
		return senderService;
	}

	public void setSenderService(String senderService) {
		this.senderService = senderService;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public long getQueuedOn() {
		return queuedOn;
	}

	public void setQueuedOn(long queuedOn) {
		this.queuedOn = queuedOn;
	}

	public long getTobeDeliveredOn() {
		return tobeDeliveredOn;
	}

	public void setTobeDeliveredOn(long tobeDeliveredOn) {
		this.tobeDeliveredOn = tobeDeliveredOn;
	}

	public long getDeliveredOn() {
		return deliveredOn;
	}

	public void setDeliveredOn(long deliveredOn) {
		this.deliveredOn = deliveredOn;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public String getAppServerId() {
		return appServerId;
	}

	public void setAppServerId(String appServerId) {
		this.appServerId = appServerId;
	}

	public SenderType getSenderType() {
		return senderType;
	}

	public void setSenderType(SenderType senderType) {
		this.senderType = senderType;
	}

	public MsgDeliveryType getDeliveryType() {
		return deliveryType;
	}

	public void setDeliveryType(MsgDeliveryType deliveryType) {
		this.deliveryType = deliveryType;
	}

	public Boolean getEmailOnly() {
		return emailOnly;
	}

	public void setEmailOnly(Boolean emailOnly) {
		this.emailOnly = emailOnly;
	}

	public NotificationOperation getNotificationOperation() {
		return notificationOperation;
	}

	public void setNotificationOperation(NotificationOperation notificationType) {
		this.notificationOperation = notificationType;
	}

	public NotificationCategory getNotificationCategory() {
		return notificationCategory;
	}

	public void setNotificationCategory(NotificationCategory notificationCategory) {
		this.notificationCategory = notificationCategory;
	}

	public NotificationStatusType getStatus() {
		return status;
	}

	public void setStatus(NotificationStatusType status) {
		this.status = status;
	}

	public String getProviderMessageId() {
		return providerMessageId;
	}

	public void setProviderMessageId(String providerMessageId) {
		this.providerMessageId = providerMessageId;
	}

	public String getReceiverUserId() {
		return receiverUserId;
	}

	public void setReceiverUserId(String receiverUserId) {
		this.receiverUserId = receiverUserId;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}
}
