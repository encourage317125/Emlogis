package com.emlogis.model.notification;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity()
public class ArchivedReceiveQueueNotification extends Notification implements Serializable {

	private String receiveId;
	
	private String replyToId;
	private String subject;
	
	@Column(length = 1024)
	private String messageContent;
	private boolean approved;
	
	
	public ArchivedReceiveQueueNotification(ReceiveNotification receivedNotification) {
		setId(UUID.randomUUID().toString());
		this.receiveId = receivedNotification.getId();
		setReplyToId(receivedNotification.getReplyToId());
		setSubject(receivedNotification.getSubject());
		setMessageContent(receivedNotification.getMessageContent());
		setApproved(receivedNotification.isApproved());
	}	

	public ArchivedReceiveQueueNotification() {
	}

	public String getReceiveId() {
		return receiveId;
	}

	public void setReceiveId(String receiveId) {
		this.receiveId = receiveId;
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
