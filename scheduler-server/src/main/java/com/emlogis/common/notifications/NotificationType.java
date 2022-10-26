package com.emlogis.common.notifications;

public enum NotificationType {
	// Employee notification types
	TIME_OFF_REQUEST,
	AVAILABILITY_REQUEST,
	OPEN_SHIFT_REQUEST,
	SWAP_REQUEST,
	WIP_REQUEST,
	SCHEDULE_POSTED,
	SCHEDULE_DELETED,
	OPEN_SHIFT_POSTED,
	MY_SCHEDULE_CHANGE,
	MANAGER_APPROVAL,				// should be renamed should we go with MANAGER_xx types later on.
	SCHEDULE_MANAGEMENT;
	
	// manager notification types (provisional)
	// MANAGER_REQUESTCOMPLETION	// notifications for workflow requests results
	// MANAGER_SCHEDULELIFECYCLE	// notifications about Schedule generation, posting and delete 
}
