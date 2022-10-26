package com.emlogis.common.notifications;

public class NotificationResult {

	private boolean sucessfullySent;
	
	private String message;
	private String notificationId;

	public NotificationResult(boolean sucessfullySent, String message) {
		super();
		this.sucessfullySent = sucessfullySent;
		this.message = message;
	}

	public boolean isSucessfullySent() {
		return sucessfullySent;
	}

	public void setSucessfullySent(boolean sucessfullySent) {
		this.sucessfullySent = sucessfullySent;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(String notificationId) {
		this.notificationId = notificationId;
	}
	
	
}
