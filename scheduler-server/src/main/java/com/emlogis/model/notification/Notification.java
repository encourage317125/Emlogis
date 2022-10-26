package com.emlogis.model.notification;

import java.util.HashMap;
import java.util.Map;

import com.emlogis.common.notifications.NotificationCategory;
import com.emlogis.common.notifications.NotificationOperation;
import com.emlogis.common.notifications.NotificationDeliveryFormat;
import com.emlogis.common.notifications.NotificationRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Notification  {
	
	@Id()
    @Column(unique = true, length = 64)
    private String id;
	
	private String providerId;
	private String providerMessageId;
	private String tenantId;
	
	@Enumerated(EnumType.STRING)
	private SenderType senderType = SenderType.SYSTEM;
	private String senderUserId;	
	private String senderName;
	private String senderService;
	
	private String receiverUserId;	
	private String receiverName;
	
	private  String userResponseCode;
	private boolean emailOnly = false;
	
	@Enumerated(EnumType.STRING)
	private MsgDeliveryType deliveryType = MsgDeliveryType.EMAIL;
	@Enumerated(EnumType.STRING)
	private NotificationDeliveryFormat   deliveryFormat;
	
	@Enumerated(EnumType.STRING)
	private NotificationOperation notificationOperation;
	
	@Enumerated(EnumType.STRING)
	private NotificationCategory notificationCategory;

	@Enumerated(EnumType.STRING)
	private NotificationRole notificationRole = NotificationRole.NONE;

	private Boolean isWorkflowType = false;
	
	private String fromAddress;
	private String toAddress;
	
	@ElementCollection(fetch = FetchType.LAZY) // let JPA do the mapping table itself instead of using the complicated @CollectionTable
	@Column(length = 10000, columnDefinition = "varchar(10000)")
	private Map<String, String> messageAttributes = new HashMap<String, String>();
	
	// This field is not really used at the moment
	@Enumerated(EnumType.STRING)
	private NotificationPriorityType priorityType = NotificationPriorityType.NORMAL;
	
	@Column()
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime queuedOn = new DateTime();
	
	@Enumerated(EnumType.STRING)
	private NotificationStatusType status = NotificationStatusType.PENDING;
	
	public Notification() {}

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

	public SenderType getSenderType() {
		return senderType;
	}

	public void setSenderType(SenderType senderType) {
		this.senderType = senderType;
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

	public MsgDeliveryType getDeliveryType() {
		return deliveryType;
	}

	public void setDeliveryType(MsgDeliveryType deliveryType) {
		this.deliveryType = deliveryType;
	}

	public NotificationOperation getNotificationOperation() {
		return notificationOperation;
	}

	public void setNotificationOperation(NotificationOperation notificationOperation) {
		this.notificationOperation = notificationOperation;
	}

	public NotificationRole getNotificationRole() {
		return notificationRole;
	}

	public void setNotificationRole(NotificationRole notificationRole) {
		this.notificationRole = notificationRole;
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

	public DateTime getQueuedOn() {
		return queuedOn;
	}

	public void setQueuedOn(DateTime queuedOn) {
		this.queuedOn = queuedOn;
	}

	public NotificationStatusType getStatus() {
		return status;
	}

	public void setStatus(NotificationStatusType status) {
		this.status = status;
	}

	public String getAppServerId() {
		return appServerId;
	}

	public void setAppServerId(String appServerId) {
		this.appServerId = appServerId;
	}

	public NotificationPriorityType getPriorityType() {
		return priorityType;
	}

	public void setPriorityType(NotificationPriorityType priorityType) {
		this.priorityType = priorityType;
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

	public String getUserResponseCode() {
		return userResponseCode;
	}

	public void setUserResponseCode(String userResponseCode) {
		this.userResponseCode = userResponseCode;
	}
	
	public boolean getEmailOnly() {
		return emailOnly;
	}

	public void setEmailOnly(boolean emailOnly) {
		this.emailOnly = emailOnly;
	}

	public Map<String, String> getMessageAttributes() {
		return messageAttributes;
	}

	public void setMessageAttributes(Map<String, String> messageAttributes) {
		this.messageAttributes.clear();
		this.messageAttributes.putAll(messageAttributes);
	}

	public NotificationDeliveryFormat getDeliveryFormat() {
		return deliveryFormat;
	}

	public void setDeliveryFormat(NotificationDeliveryFormat deliveryFormat) {
		this.deliveryFormat = deliveryFormat;
	}

	public NotificationCategory getNotificationCategory() {
		return notificationCategory;
	}

	public void setNotificationCategory(NotificationCategory notificationCategory) {
		this.notificationCategory = notificationCategory;
	}

	public Boolean getIsWorkflowType() {
		return isWorkflowType;
	}

	public void setIsWorkflowType(Boolean isWorkflowType) {
		this.isWorkflowType = isWorkflowType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((appServerId == null) ? 0 : appServerId.hashCode());
		result = prime * result
				+ ((deliveryFormat == null) ? 0 : deliveryFormat.hashCode());
		result = prime * result
				+ ((deliveryType == null) ? 0 : deliveryType.hashCode());
		result = prime * result + (emailOnly ? 1231 : 1237);
		result = prime * result
				+ ((fromAddress == null) ? 0 : fromAddress.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((isWorkflowType == null) ? 0 : isWorkflowType.hashCode());
		result = prime
				* result
				+ ((messageAttributes == null) ? 0 : messageAttributes
						.hashCode());
		result = prime
				* result
				+ ((notificationCategory == null) ? 0 : notificationCategory
						.hashCode());
		result = prime
				* result
				+ ((notificationOperation == null) ? 0 : notificationOperation
						.hashCode());
		result = prime
				* result
				+ ((notificationRole == null) ? 0 : notificationRole.hashCode());
		result = prime * result
				+ ((priorityType == null) ? 0 : priorityType.hashCode());
		result = prime * result
				+ ((providerId == null) ? 0 : providerId.hashCode());
		result = prime
				* result
				+ ((providerMessageId == null) ? 0 : providerMessageId
						.hashCode());
		result = prime * result
				+ ((queuedOn == null) ? 0 : queuedOn.hashCode());
		result = prime
				* result
				+ ((receiverUserId == null) ? 0 : receiverUserId
						.hashCode());
		result = prime * result
				+ ((receiverName == null) ? 0 : receiverName.hashCode());
		result = prime
				* result
				+ ((senderUserId == null) ? 0 : senderUserId.hashCode());
		result = prime * result
				+ ((senderName == null) ? 0 : senderName.hashCode());
		result = prime * result
				+ ((senderService == null) ? 0 : senderService.hashCode());
		result = prime * result
				+ ((senderType == null) ? 0 : senderType.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result
				+ ((tenantId == null) ? 0 : tenantId.hashCode());
		result = prime * result
				+ ((toAddress == null) ? 0 : toAddress.hashCode());
		result = prime
				* result
				+ ((userResponseCode == null) ? 0 : userResponseCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Notification other = (Notification) obj;
		if (appServerId == null) {
			if (other.appServerId != null)
				return false;
		} else if (!appServerId.equals(other.appServerId))
			return false;
		if (deliveryFormat != other.deliveryFormat)
			return false;
		if (deliveryType != other.deliveryType)
			return false;
		if (emailOnly != other.emailOnly)
			return false;
		if (fromAddress == null) {
			if (other.fromAddress != null)
				return false;
		} else if (!fromAddress.equals(other.fromAddress))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isWorkflowType == null) {
			if (other.isWorkflowType != null)
				return false;
		} else if (!isWorkflowType.equals(other.isWorkflowType))
			return false;
		if (messageAttributes == null) {
			if (other.messageAttributes != null)
				return false;
		} else if (!messageAttributes.equals(other.messageAttributes))
			return false;
		if (notificationCategory != other.notificationCategory)
			return false;
		if (notificationOperation != other.notificationOperation)
			return false;
		if (notificationRole != other.notificationRole)
			return false;
		if (priorityType != other.priorityType)
			return false;
		if (providerId == null) {
			if (other.providerId != null)
				return false;
		} else if (!providerId.equals(other.providerId))
			return false;
		if (providerMessageId == null) {
			if (other.providerMessageId != null)
				return false;
		} else if (!providerMessageId.equals(other.providerMessageId))
			return false;
		if (queuedOn == null) {
			if (other.queuedOn != null)
				return false;
		} else if (!queuedOn.equals(other.queuedOn))
			return false;
		if (receiverUserId == null) {
			if (other.receiverUserId != null)
				return false;
		} else if (!receiverUserId.equals(other.receiverUserId))
			return false;
		if (receiverName == null) {
			if (other.receiverName != null)
				return false;
		} else if (!receiverName.equals(other.receiverName))
			return false;
		if (senderUserId == null) {
			if (other.senderUserId != null)
				return false;
		} else if (!senderUserId.equals(other.senderUserId))
			return false;
		if (senderName == null) {
			if (other.senderName != null)
				return false;
		} else if (!senderName.equals(other.senderName))
			return false;
		if (senderService == null) {
			if (other.senderService != null)
				return false;
		} else if (!senderService.equals(other.senderService))
			return false;
		if (senderType != other.senderType)
			return false;
		if (status != other.status)
			return false;
		if (tenantId == null) {
			if (other.tenantId != null)
				return false;
		} else if (!tenantId.equals(other.tenantId))
			return false;
		if (toAddress == null) {
			if (other.toAddress != null)
				return false;
		} else if (!toAddress.equals(other.toAddress))
			return false;
		if (userResponseCode == null) {
			if (other.userResponseCode != null)
				return false;
		} else if (!userResponseCode.equals(other.userResponseCode))
			return false;
		return true;
	}
	
	
}
