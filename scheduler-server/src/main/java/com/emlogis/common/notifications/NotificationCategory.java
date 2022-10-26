package com.emlogis.common.notifications;

public enum NotificationCategory {
	OPEN_SHIFTS,
	WORK_IN_PLACE,
	SHIFT_SWAP,
	TIME_OFF,
	AVAILABILITY,
	SCHEDULE,
	SHIFT_ASSIGN,
	SHIFT_DROP,
	SHIFT_ADD,
	SHIFT_STARTSTOP_MODIFIED,
	SHIFT_WIP,
	PASSWORD;

    public String getFilePart() {
		return this.name().toLowerCase();
    }
}
