package com.emlogis.common.services.notification;

import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Startup
@DependsOn({"NotificationSendingDeliveryService"})
public class NotificationArchivingService {
	
	private final Logger logger = LoggerFactory.getLogger(NotificationArchivingService.class);
	
	@EJB
	NotificationQueuingService queuingService;
	
	@Lock(LockType.READ)
	public void archiveMessages()  throws InterruptedException {
			logger.debug("About request archive of messages");				
			queuingService.archiveSendMessages();
			queuingService.archiveReceivedMessages();
			logger.debug("Archived messages");
	}
	
	public void saveMessages() {
		
	}

}
