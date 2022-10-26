package com.emlogis.common.services.notification;

import java.util.Collection;

import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.event.Event;

import com.emlogis.model.notification.NotificationStatusType;
import com.emlogis.model.notification.ReceiveNotification;
import com.emlogis.model.notification.dto.NotificationMessageDTO;
import com.emlogis.server.services.eventservice.ASEventService;
import com.emlogis.shared.services.eventservice.EventKeyBuilder;
import com.emlogis.shared.services.eventservice.EventScope;
import com.emlogis.shared.services.eventservice.EventService;

@Startup
@Singleton
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@DependsOn({"NotificationArchivingService"})
public class NotificationReceivingDeliveryService {
	
private final Logger logger = LoggerFactory.getLogger(NotificationReceivingDeliveryService.class);
	
	@EJB
	NotificationQueuingService queuingService;

	@Inject
	private ASEventService eventService;	
	
	@Lock(LockType.READ)
	public void notifyMessagesForDelivery()  {
		try {
			logger.debug("Request message receipt now");
			processAvailableMessages();
		} catch (InterruptedException e) {
			logger.error("processMessages was Interrupted",e);
		}
	}	
	
	@Lock(LockType.READ)
	private void processAvailableMessages() throws InterruptedException {
		// This the main code
		logger.debug("About to get received notifications to process");
		boolean foundMessages = true;
		
		while(foundMessages) {
			Collection<ReceiveNotification> receivedNotificationsList = queuingService.getReceivedMessagesToProcess();
		
			if(receivedNotificationsList != null && receivedNotificationsList.size() > 0){
				for(ReceiveNotification notification : receivedNotificationsList) {
					processReceivedNotifications(notification);
					
				}
				
				queuingService.updateMessagesStatus(receivedNotificationsList);
			} else {
	        	foundMessages = false;
	        }
		}
		
					
		logger.debug("Processed received notifications");		
	}

	private void processReceivedNotifications(ReceiveNotification notification) {
		
		NotificationMessageDTO notificationMessageDTO = NotificationUtil.copyNotificationToMessage(notification);
		
		// Send Message
		Object key = new EventKeyBuilder().setTopic(EventService.TOPIC_NOTIFICATION).setTenantId(notification.getTenantId())
				.setEntityClass("NotificationMessageDTO").setEventType("Notification").setEntityId(notification.getId()).build();
		
		try {
			eventService.sendEvent(EventScope.Local, key, Event.wrap(notificationMessageDTO), "NotificationReceivingDeliveryService");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logger.error("Notification event could not be sent",e);
		}
		
		notification.setStatus(NotificationStatusType.PROCESSED);		
	}

}
