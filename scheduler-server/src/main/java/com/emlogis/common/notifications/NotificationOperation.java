package com.emlogis.common.notifications;

public enum NotificationOperation {
	
	// Misc Types
	POST,
	GENERATION_START,
	GENERATION_COMPLETE,
	DELETE,
	SCHEDULE_CHANGE,
	REMOVE,
	RESET,
	CONFIRMATION,

	// Worflow Types
    BECOME_ADMIN_PENDING,
    BECOME_PEER_PENDING,
    OBTAINED_FINAL_STATE;


    public String getFilePart() {
    	return this.name().toLowerCase();
    }

}
