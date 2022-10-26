package com.emlogis.common.services.notification;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emlogis.common.Constants;
import com.emlogis.common.services.hazelcast.HazelcastClientService;
import com.emlogis.model.notification.ArchivedReceiveQueueNotification;
import com.emlogis.model.notification.ArchivedSendQueueNotification;
import com.emlogis.model.notification.FailedSendNotification;
import com.emlogis.model.notification.Notification;
import com.emlogis.model.notification.NotificationSettings;
import com.emlogis.model.notification.NotificationStatusType;
import com.emlogis.model.notification.ReceiveNotification;
import com.emlogis.model.notification.SendNotification;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.scheduler.engine.communication.HzConstants;


@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class NotificationQueuingService {
	
	private final Logger logger = LoggerFactory.getLogger(NotificationQueuingService.class);
	
	private int queueProcessingSize;
	private int maxHours;
	
	@PersistenceContext(unitName=Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;
	
	@EJB
	NotificationSettingsService settingsService;
	
    @EJB
    HazelcastClientService hazelcastClientService;
    
	@Resource
	private EJBContext context;
	
	@PostConstruct
	public void init() {
		NotificationSettings settings = settingsService.getNotificationSettings();
		queueProcessingSize = settings.getQueueProcessingSize();
		maxHours = settings.getNotificationExpirationHours();
	}

    public EntityManager getEntityManager() {
        return entityManager;
    }
    

    
	public void enqueueMessage(Notification notification) {
		saveNotifcation(notification); 		
	}
	
	public Collection<SendNotification> getSendMessageByResponseCode(String responseCode) {
		
		Collection<SendNotification>  matchingSentMessages = null;
		
		SimpleQuery simpleQuery = new SimpleQuery();
		simpleQuery.setLimit(queueProcessingSize).setOrderByField("id").addFilter("status='" + NotificationStatusType.PROCESSED + "'");
		simpleQuery.addFilter("userResponseCode='" + responseCode + "'");
		simpleQuery.setEntityClass(SendNotification.class);
		
		matchingSentMessages = new SimpleQueryHelper().executeSimpleQuery(entityManager, simpleQuery);
				
		return matchingSentMessages;	
	}
	
	public Collection<ReceiveNotification> getReceivedMessagesToProcess() {
		
		Collection<ReceiveNotification>  receivedMessages = null;
		
		Lock queueLock = null;
		
		try {
			queueLock = hazelcastClientService.getNativeLock(HzConstants.NOTIFICATION_RECEIVE_QUEUE);
			if(queueLock != null && queueLock.tryLock(HzConstants.NOTIFICATION_QUEUE_WAIT_TIME, TimeUnit.SECONDS)) {
				
				try {
					SimpleQuery simpleQuery = new SimpleQuery();
					simpleQuery.setLimit(queueProcessingSize).setOrderByField("id").addFilter("status='" + NotificationStatusType.PENDING + "'");
					simpleQuery.setEntityClass(ReceiveNotification.class);
					
					receivedMessages = new SimpleQueryHelper().executeSimpleQuery(entityManager, simpleQuery);
					
					if(receivedMessages != null) {
						for(ReceiveNotification notification : receivedMessages) {
							notification.setStatus(NotificationStatusType.PROCESSING);
						}
					}
					
					updateMessagesStatus(receivedMessages);
				} finally {
					hazelcastClientService.releaseNativeLock(queueLock);
				}
			}
		} catch (InterruptedException e) {
			logger.error("Interrupted attemptng to lock Send Queue" );
		}  catch (Exception e1) {
			logger.error("Unable to attain Hazelcast lock for Send Queue Processing", e1);
		}
				
		return receivedMessages;
		
	}
	
	public Collection<SendNotification> getMessagesToSend() {
		
		Collection<SendNotification>  sendList = null;
		
		Lock queueLock = null;
		
		try {
			queueLock = hazelcastClientService.getNativeLock(HzConstants.NOTIFICATION_SEND_QUEUE);
			if(queueLock != null && queueLock.tryLock(HzConstants.NOTIFICATION_QUEUE_WAIT_TIME, TimeUnit.SECONDS)) {
				try {
					SimpleQuery simpleQuery = new SimpleQuery();
					simpleQuery.setLimit(queueProcessingSize).setOrderByField("id").addFilter("status='" + NotificationStatusType.PENDING + "'");
					simpleQuery.setEntityClass(SendNotification.class);
					
					sendList = new SimpleQueryHelper().executeSimpleQuery(entityManager, simpleQuery);
					
					if(sendList != null) {
						for(SendNotification notification : sendList) {
							notification.setStatus(NotificationStatusType.PROCESSING);
						}
					}
					
					updateMessagesStatus(sendList);
				} finally {
					hazelcastClientService.releaseNativeLock(queueLock);
				}
			}
		} catch (InterruptedException e) {
			logger.error("Interrupted attemptng to lock Send Queue" );
		}  catch (Exception e1) {
			logger.error("Unable to get messags for Send Queue Processing", e1);
		}
				
		return sendList;
		
	}
	
public Collection<SendNotification> getMessagesToRetry() {
		
		Collection<SendNotification>  retryList = null;
		
		Lock queueLock = null;
		
		try {
			queueLock = hazelcastClientService.getNativeLock(HzConstants.NOTIFICATION_SEND_QUEUE);
			if(queueLock != null && queueLock.tryLock(HzConstants.NOTIFICATION_QUEUE_WAIT_TIME, TimeUnit.SECONDS)) {
				try {
					SimpleQuery simpleQuery = new SimpleQuery();
					simpleQuery.setLimit(queueProcessingSize).setOrderByField("id").addFilter("status='" + NotificationStatusType.RETRYING + "'");
					simpleQuery.setEntityClass(SendNotification.class);
					
					retryList = new SimpleQueryHelper().executeSimpleQuery(entityManager, simpleQuery);
					
					if(retryList != null) {
						for(SendNotification notification : retryList) {
							notification.setStatus(NotificationStatusType.PROCESSING);
						}
					}
					
					updateMessagesStatus(retryList);
				} finally {
					hazelcastClientService.releaseNativeLock(queueLock);
				}
			}
		} catch (InterruptedException e) {
			logger.error("Interrupted attemptng to lock Send Queue" );
		}  catch (Exception e1) {
			logger.error("Unable to get messags for Send Queue Rettry Processing", e1);
		}
				
		return retryList;
		
	}
	
	public void archiveSendMessages() {
		
		Collection<SendNotification>  sendList = null;
		ArchivedSendQueueNotification archivedNotfication = null;
		
		Lock queueLock = null;
		
		try {
			queueLock = hazelcastClientService.getNativeLock(HzConstants.NOTIFICATION_SEND_QUEUE);
			if(queueLock != null && queueLock.tryLock(HzConstants.NOTIFICATION_QUEUE_WAIT_TIME, TimeUnit.SECONDS)) {
				boolean foundMessages = true;
				DateTime archiveTime = new DateTime().minusHours(maxHours);

				try {
					SimpleQuery simpleQuery = new SimpleQuery();
					simpleQuery.setLimit(queueProcessingSize).setOrderByField("id").addFilter("status='" + NotificationStatusType.PROCESSED + "'");
					simpleQuery.setFilter("deliveredOn<='" + archiveTime + "'");
					simpleQuery.setEntityClass(SendNotification.class);
					
					while(foundMessages) { 
						sendList = new SimpleQueryHelper().executeSimpleQuery(entityManager, simpleQuery);
					
						if(sendList != null && sendList.size() > 0) {
							for(SendNotification notification : sendList) {
								
								// Move messages to archive
	
								archivedNotfication = new ArchivedSendQueueNotification(notification);
								NotificationUtil.copyCommonNotificationData(notification, archivedNotfication);
								
								saveNotifcation(archivedNotfication);
								deleteNotification(notification);							
							}
						} else {
			            	foundMessages = false;
			            }
					}
					
					foundMessages = true;
					
					// Remove failed messages
					simpleQuery = new SimpleQuery();
					simpleQuery.setLimit(queueProcessingSize).setOrderByField("id").addFilter("status='" + NotificationStatusType.FAILED + "'");
					simpleQuery.setEntityClass(SendNotification.class);
					
					while(foundMessages) {
						sendList = new SimpleQueryHelper().executeSimpleQuery(entityManager, simpleQuery);
					
						if(sendList != null && sendList.size() > 0) {
							for(SendNotification notification : sendList) {
								
								// Move messages to archive
	
								FailedSendNotification faileNotification = new FailedSendNotification(notification);
								NotificationUtil.copyCommonNotificationData(notification, faileNotification);
								
								saveNotifcation(faileNotification);
								deleteNotification(notification);							
							}
						} else {
			            	foundMessages = false;
			            }
					}
										
				} finally {
					hazelcastClientService.releaseNativeLock(queueLock);
				}
			}
		} catch (InterruptedException e) {
			logger.error("Interrupted attemptng to lock Send Queue Archiving" );
		} catch (Exception e1) {
			logger.error("Unable to attain Hazelcast lock for Send Queue Archiving", e1);
		}
		
	}
	
	public void archiveReceivedMessages() {
		
		Collection<ReceiveNotification>  receiveList = null;
		ArchivedReceiveQueueNotification archivedNotfication = null;
		
		Lock queueLock = null;
		boolean foundMessages = true;
		
		try {
			queueLock = hazelcastClientService.getNativeLock(HzConstants.NOTIFICATION_RECEIVE_QUEUE);
			if(queueLock != null && queueLock.tryLock(HzConstants.NOTIFICATION_QUEUE_WAIT_TIME, TimeUnit.SECONDS)) {
				
				try {
					SimpleQuery simpleQuery = new SimpleQuery();
					simpleQuery.setLimit(queueProcessingSize).setOrderByField("id").addFilter("status='" + NotificationStatusType.PROCESSED + "'");
					simpleQuery.setEntityClass(ReceiveNotification.class);
					
					while(foundMessages) {
						receiveList = new SimpleQueryHelper().executeSimpleQuery(entityManager, simpleQuery);
					
						if(receiveList != null && receiveList.size() > 0) {
							for(ReceiveNotification notification : receiveList) {
								
								// Move messages to archive
								archivedNotfication = new ArchivedReceiveQueueNotification(notification);
								NotificationUtil.copyCommonNotificationData(notification, archivedNotfication);
								saveNotifcation(archivedNotfication);
								deleteNotification(notification);							
							}
						} else {
			            	foundMessages = false;
			            }
					}
				} finally {
					hazelcastClientService.releaseNativeLock(queueLock);
				}
			}
		} catch (InterruptedException e) {
			logger.error("Interrupted attemptng to lock Receive Queue Archiving" );
		} catch (Exception e1) {			
			logger.error("Unable to attain Hazelcast lock for Receive Queue Archiving", e1);
		}
		
	}
	
	public void updateMessagesStatus(Collection<? extends Notification> notCollection) {
		
		if(notCollection!= null){
			for(Notification notification : notCollection) {
				updateNotifcation(notification);
			}
		}
		
	}
	
	private void saveNotifcation(Notification notification) {	
		entityManager.persist(notification);
	}
	
	private void updateNotifcation(Notification notification) {
		entityManager.merge(notification); 
	}
	
	private void deleteNotification(Notification notification) {
		entityManager.remove(notification);	
	}

    public Notification getNotificationById(String sendNotificationId) {
        return entityManager.find(SendNotification.class, sendNotificationId);
    }
}
