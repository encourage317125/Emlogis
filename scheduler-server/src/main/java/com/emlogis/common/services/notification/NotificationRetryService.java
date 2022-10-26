package com.emlogis.common.services.notification;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emlogis.model.notification.NotificationSettings;
import com.emlogis.model.notification.NotificationStatusType;
import com.emlogis.model.notification.SendNotification;

@Singleton
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Startup
@DependsOn({"NotificationSendingDeliveryService"})

public class NotificationRetryService {
	
	private final Logger logger = LoggerFactory.getLogger(NotificationRetryService.class);
	
	 @EJB
	 NotificationQueuingService queuingService;
	 	    
	 @EJB
	 private NotificationSettingsService settingsService;
	 
	 private int maxRetry = 0;
	 private int maxDeliveryHours = 0;
	
	@Lock(LockType.READ)
    public void processMessages() throws InterruptedException {
		logger.debug("About to Retry Send Notifications");
        boolean foundMessages = true;
        
        while(foundMessages) {
        	Collection<SendNotification> retryNotificationList = queuingService.getMessagesToRetry();

            if (retryNotificationList != null && retryNotificationList.size() > 0) {
                for (SendNotification notification : retryNotificationList) {
                    logger.debug("The id of the nextmessage is:" + notification.getId());

                    checkFailedMessageRetry(notification);                                        
                }
                queuingService.updateMessagesStatus(retryNotificationList);
            } else {
            	foundMessages = false;
            }
        }
		
	}
	
	 @PostConstruct
	    @Lock(LockType.READ)
	    public void init() {
	    	NotificationSettings settings = settingsService.getNotificationSettings();
	    	
	    	if (settings != null) {
	    		maxRetry = settings.getRetryCount();
	    		maxDeliveryHours = settings.getMaxDeliveryHours();
	    	}
	    }

	private void checkFailedMessageRetry(SendNotification notification) {
    	int newRetryCount = notification.getRetryCount() + 1;
    	int hoursElapsed = (Hours.hoursBetween(notification.getQueuedOn(), new DateTime())).getHours();
    	
    	if ((newRetryCount <= maxRetry) && (hoursElapsed <= maxDeliveryHours)) {
    		notification.setRetryCount(newRetryCount);
    		notification.setLastRetryDateTime(new DateTime());
    		notification.setStatus(NotificationStatusType.PENDING);
    	} else {
    		notification.setStatus(NotificationStatusType.FAILED);
    	}
    }
}
