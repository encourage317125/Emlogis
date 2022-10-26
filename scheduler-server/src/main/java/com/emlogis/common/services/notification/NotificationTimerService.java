package com.emlogis.common.services.notification;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@Singleton
@LocalBean
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@DependsOn({"NotificationReadSMSService"})
public class NotificationTimerService {
	private final Logger logger = LoggerFactory.getLogger(NotificationTimerService.class);
	
	@EJB
	NotificationSendingDeliveryService sendingDeliveryService;
	
	@EJB
	NotificationArchivingService archivingService;
	
	@EJB
	NotificationReadEmailService readEmailService;
	
	@EJB
	NotificationReadSMSService readSMSService;
	
	@EJB
	NotificationReceivingDeliveryService receivingDeliveryService;
	
	@EJB
	NotificationRetryService retryService;
	
	private AtomicBoolean busy = new AtomicBoolean(false);
    
    @Lock(LockType.READ)
    @Schedule(minute = "*/2", hour = "*", persistent = false)
    private void processMessages() throws InterruptedException {
    	
    	if (!busy.compareAndSet(false, true)) {
            return;
        }
    
    	try {
    		try{ 
    			sendingDeliveryService.notifyMessagesForDelivery();
    		} catch(Exception e) {
    			logger.error("Sending delivery service errorr: ",e);
    		}
    		try{ 
    			archivingService.archiveMessages();
    		} catch(Exception e) {
    			logger.error("Archiving service error: ",e);
    		}
    		try{ 
    			readSMSService.readSMSMessages();
    		} catch(Exception e) {
    			logger.error("Read SMS service error: ",e);
    		}
    		try{ 
    			readEmailService.readEmail(); 
    		} catch(Exception e) {
    			logger.error("REad email error: ",e);
    		}    		
    		try{ 
    			receivingDeliveryService.notifyMessagesForDelivery();
    		} catch(Exception e) {
    			logger.error("Receiving delivery service error: ",e);
    		}
    		try{ 
    			retryService.processMessages();
    		} catch(Exception e) {
    			logger.error("Retry service error: ",e);
    		}
    		
    	} finally {
    		busy.set(false);
    	}    	    
    }
}
