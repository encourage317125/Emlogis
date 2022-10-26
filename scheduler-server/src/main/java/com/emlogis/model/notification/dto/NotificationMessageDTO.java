package com.emlogis.model.notification.dto;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.emlogis.common.notifications.NotificationCategory;
import com.emlogis.common.notifications.NotificationOperation;
import com.emlogis.common.notifications.NotificationRole;
import com.emlogis.model.notification.NotificationPriorityType;

public class NotificationMessageDTO {
	private String tenantId;
	private String senderUserId;
	private String receiverUserId;
	private boolean emailOnly = false;
	
	private NotificationOperation notificationOperation;
	private NotificationCategory notificationCategory;
	private NotificationRole notificationRole = NotificationRole.NONE;
	
	private Boolean isWorkflowType = false;
	
	private Map<String, String> messageAttributes = new HashMap<String, String>();
	
	private NotificationPriorityType priorityType = NotificationPriorityType.NORMAL;
	private DateTime tobeDeliveredOn = new DateTime();
	private String replyToId;
	private boolean approved;
	
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
	public String getReceiverUserId() {
		return receiverUserId;
	}
	public void setReceiverUserId(String receiverUserId) {
		this.receiverUserId = receiverUserId;
	}
	public boolean isEmailOnly() {
		return emailOnly;
	}
	public void setEmailOnly(boolean emailOnly) {
		this.emailOnly = emailOnly;
	}
	public NotificationOperation getNotificationOperation() {
		return notificationOperation;
	}
	public void setNotificationOperation(NotificationOperation notificationOperation) {
		this.notificationOperation = notificationOperation;
	}
	public NotificationCategory getNotificationCategory() {
		return notificationCategory;
	}
	public void setNotificationCategory(NotificationCategory notificationCategory) {
		this.notificationCategory = notificationCategory;
	}
	public NotificationRole getNotificationRole() {
		return notificationRole;
	}
	public void setNotificationRole(NotificationRole notificationRole) {
		this.notificationRole = notificationRole;
	}
	public Boolean getIsWorkflowType() {
		return isWorkflowType;
	}
	public void setIsWorkflowType(Boolean isWorkflowType) {
		this.isWorkflowType = isWorkflowType;
	}
	public Map<String, String> getMessageAttributes() {
		return messageAttributes;
	}
	public void setMessageAttributes(Map<String, String> messageAttributes) {
		this.messageAttributes = messageAttributes;
	}
	public NotificationPriorityType getPriorityType() {
		return priorityType;
	}
	public void setPriorityType(NotificationPriorityType priorityType) {
		this.priorityType = priorityType;
	}
	public DateTime getTobeDeliveredOn() {
		return tobeDeliveredOn;
	}
	public void setTobeDeliveredOn(DateTime tobeDeliveredOn) {
		this.tobeDeliveredOn = tobeDeliveredOn;
	}
	public String getReplyToId() {
		return replyToId;
	}
	public void setReplyToId(String replyToId) {
		this.replyToId = replyToId;
	}
	public boolean isApproved() {
		return approved;
	}
	public void setApproved(boolean approved) {
		this.approved = approved;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (approved ? 1231 : 1237);
		result = prime * result + (emailOnly ? 1231 : 1237);
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
		result = prime
				* result
				+ ((receiverUserId == null) ? 0 : receiverUserId
						.hashCode());
		result = prime * result
				+ ((replyToId == null) ? 0 : replyToId.hashCode());
		result = prime
				* result
				+ ((senderUserId == null) ? 0 : senderUserId.hashCode());
		result = prime * result
				+ ((tenantId == null) ? 0 : tenantId.hashCode());
		result = prime * result
				+ ((tobeDeliveredOn == null) ? 0 : tobeDeliveredOn.hashCode());
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
		NotificationMessageDTO other = (NotificationMessageDTO) obj;
		if (approved != other.approved)
			return false;
		if (emailOnly != other.emailOnly)
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
		if (receiverUserId == null) {
			if (other.receiverUserId != null)
				return false;
		} else if (!receiverUserId.equals(other.receiverUserId))
			return false;
		if (replyToId == null) {
			if (other.replyToId != null)
				return false;
		} else if (!replyToId.equals(other.replyToId))
			return false;
		if (senderUserId == null) {
			if (other.senderUserId != null)
				return false;
		} else if (!senderUserId.equals(other.senderUserId))
			return false;
		if (tenantId == null) {
			if (other.tenantId != null)
				return false;
		} else if (!tenantId.equals(other.tenantId))
			return false;
		if (tobeDeliveredOn == null) {
			if (other.tobeDeliveredOn != null)
				return false;
		} else if (!tobeDeliveredOn.equals(other.tobeDeliveredOn))
			return false;
		return true;
	}
	
	
}
