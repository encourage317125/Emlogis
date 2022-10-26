package com.emlogis.common.services.notification;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.search.FlagTerm;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emlogis.common.Constants;
import com.emlogis.common.services.hazelcast.HazelcastClientService;
import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.model.notification.MsgDeliveryProviderSettings;
import com.emlogis.model.notification.MsgDeliveryProviderStatus;
import com.emlogis.model.notification.MsgDeliveryTenantSettings;
import com.emlogis.model.notification.MsgDeliveryType;
import com.emlogis.model.notification.NotificationStatusType;
import com.emlogis.model.notification.ReceiveNotification;
import com.emlogis.model.notification.SendNotification;
import com.emlogis.model.tenant.Tenant;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.scheduler.engine.communication.HzConstants;
import com.sun.mail.imap.IMAPSSLStore;

@Singleton
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Startup
@DependsOn({"NotificationReceivingDeliveryService"})
public class NotificationReadEmailService {

	private final Logger logger = LoggerFactory
			.getLogger(NotificationReadEmailService.class);

	@EJB
	HazelcastClientService hazelcastClientService;

	@EJB
	NotificationQueuingService queuingService;	
    
    @EJB
    NotificationConfigurationService configurationService;
    
    @EJB
    TenantService<Tenant> tenantService;

	private Store store = null;
	private Folder folder = null;
	private String hostName;
	private String username;
	private String password;
	private String mailBox;
	private String providerId;
	


	@Lock(LockType.READ)
	public void readEmail() throws InterruptedException {

		java.util.concurrent.locks.Lock queueLock = null;

		try {
			queueLock = hazelcastClientService
					.getNativeLock(HzConstants.NOTIFICATION_RECEIVE_EMAIL);
			if (queueLock != null
					&& queueLock.tryLock(
							HzConstants.NOTIFICATION_QUEUE_WAIT_TIME,
							TimeUnit.SECONDS)) {

				logger.debug("About to read email messages");
				checkAndProcessEmail();
			}
		}  finally {
			hazelcastClientService.releaseNativeLock(queueLock);
		}
	}
	

	public void checkAndProcessEmail() throws InterruptedException {
		
		ResultSet<Tenant> tenantResultSet = tenantService.findTenants(Tenant.class, new SimpleQuery());
		
		for (Tenant tenant : tenantResultSet.getResult()) {
			MsgDeliveryTenantSettings emailTenantSettings = configurationService.getRecipientConfig(tenant.getTenantId(), 
					MsgDeliveryType.EMAIL);
			
			MsgDeliveryProviderSettings emailDeliveryProviderSettings = emailTenantSettings.getDeliveryProviderSettings();
			
			if(emailDeliveryProviderSettings.getStatus() != MsgDeliveryProviderStatus.OK) {
				// proceed to next tenant if the provider settings for this tenant are not setup
				continue;
			}
			
			final Map<String, String> tenantSettingsMap = emailTenantSettings.getSettings();
			final Map<String, String> providerSettingsMap = emailDeliveryProviderSettings.getSettings();
			

            int port = new Integer(providerSettingsMap.get(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_RECEIVEPORT));
            hostName = providerSettingsMap.get(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_RECEIVEHOST);

            username = providerSettingsMap.get(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_USERNAME);
            password = providerSettingsMap.get(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_PASSWORD);
            mailBox = tenantSettingsMap.get(MsgDeliveryTenantSettings.TENANT_MAILBOX);

            
            if(store == null){
            	Session session;

	            Properties properties = new Properties();
	
	            properties.put("port", port);
	            properties.put("protocal", "imap");
	            properties.put("host", hostName);
	
	            session = Session.getInstance(properties, null);
	
	            URLName urln = new URLName("imap", hostName, port, null, username, password);
	            
	            store = new IMAPSSLStore(session, urln);
            
            }

            try {
				readTenantEmail();
			} catch (MessagingException | IOException e ) {
				logger.error("Error", e);
			} 
			
		}
		
	}

	private void readTenantEmail() throws MessagingException, IOException {

		if (store != null && !store.isConnected()) {
			logger.debug("initMail():store got connected..");
			store.connect(hostName, username, password);
		}

		folder = store.getFolder(mailBox);
		folder.open(Folder.READ_WRITE);

		Flags seen = new Flags(Flags.Flag.SEEN);
		FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
		Message[] newMessages = folder.search(unseenFlagTerm);

		Collection<SendNotification> matchingNotifications = null;
		SendNotification sendNotification = null;

		boolean foundMessages = false;

		if (newMessages.length == 0) {
			//logger.debug("No email messages found.");
		} else {
			foundMessages = true;
		}

		for (Message message : newMessages) {

			String subject = message.getSubject();
			String responseCode = NotificationUtil.getSplittedResult(subject,
					"<", ">");

			// Check the first part of the message for Yes/No
			String messageContent = NotificationUtil
					.getMailMessageText(message).substring(0,
							Constants.NOTIFICATION_EMAIL_MATCH_LENGTH);

			Boolean approved = NotificationUtil
					.getEmailResponseApproval(messageContent);

			if ((approved != null) && (!StringUtils.isBlank(responseCode))) {

				// Get the matching send Notification
				matchingNotifications = queuingService
						.getSendMessageByResponseCode(responseCode);

				if (matchingNotifications != null
						&& matchingNotifications.size() > 0) {
					sendNotification = matchingNotifications.iterator().next();
				} else {
					sendNotification = null;
				}

				if (sendNotification != null) {
					
					ReceiveNotification notification = new ReceiveNotification();					
					NotificationUtil.copyCommonNotificationData(sendNotification, notification);
					notification.setReplyToId( sendNotification.getId() );
					notification.setId(UUID.randomUUID().toString());
					notification.setStatus(NotificationStatusType.PENDING);
					notification.setApproved(approved);
					
					notification.setSubject(subject);
					notification.setMessageContent(messageContent);
					
					notification.setQueuedOn(new DateTime());

					queuingService.enqueueMessage(notification);

				} else {
					logger.info("No matching send notification found for this message: "
							+ messageContent);
				}

			} else {
				logger.info("No Approval or Response Code for this email "
						+ subject);
			}
		}

		if (foundMessages)
			folder.setFlags(newMessages, new Flags(Flags.Flag.SEEN), true);

		folder.close(false);
	}

}
