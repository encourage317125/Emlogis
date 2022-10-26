package com.emlogis.server.services.eventservice;

import com.emlogis.model.notification.ReceiveNotification;
import com.emlogis.model.notification.dto.NotificationMessageDTO;

public interface NotificationClient {

	void notify(NotificationMessageDTO notificationMessageDTO);
		
}
