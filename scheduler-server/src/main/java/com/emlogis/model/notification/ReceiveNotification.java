package com.emlogis.model.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.joda.time.DateTime;

import java.util.Map;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity()

public class ReceiveNotification extends Notification {
	
	private String replyToId;
	private String subject;
	
	@Column(length = 1024)
	private String messageContent;
	private boolean approved;
	
	
	public ReceiveNotification() {

	}

	public ReceiveNotification(String providerId, String providerMessageId, String messageContent, String replyId,
			String appServerId, String subject, String fromAddress, String toAddress, MsgDeliveryType deliveryType, String responseCode, boolean approved, Map<String, String> messageAttributes ) {
		
		setId(UUID.randomUUID().toString());
		setProviderId(providerId);
		setProviderMessageId(providerMessageId);
		setMessageContent(messageContent);
		setReplyToId(replyId);
		setAppServerId(appServerId);
		setSubject(subject);
		setFromAddress(fromAddress);
		setToAddress(toAddress);
		setStatus(NotificationStatusType.PENDING);
		setQueuedOn(new DateTime());
		setDeliveryType(deliveryType);
		setApproved(approved);
		setUserResponseCode(responseCode);
		setMessageAttributes(messageAttributes);
	}

	public String getReplyToId() {
		return replyToId;
	}

	public void setReplyToId(String replyToId) {
		this.replyToId = replyToId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessageContent() {
		return messageContent;
	}

	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

}
