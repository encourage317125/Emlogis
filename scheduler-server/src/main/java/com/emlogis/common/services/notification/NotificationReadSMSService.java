package com.emlogis.common.services.notification;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Message;
import com.twilio.sdk.resource.list.MessageList;

@Singleton
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Startup
@DependsOn({ "NotificationReadEmailService" })
public class NotificationReadSMSService {

	private final Logger logger = LoggerFactory
			.getLogger(NotificationReadSMSService.class);

	private String providerId;

	@EJB
	HazelcastClientService hazelcastClientService;

	@EJB
	NotificationQueuingService queuingService;
	
    @EJB
    NotificationConfigurationService configurationService;
    
    @EJB
    TenantService<Tenant> tenantService;

	Account acct;
	Map<String, String> filterMap;

	@Lock(LockType.READ)
	public void readSMSMessages() throws InterruptedException {

		java.util.concurrent.locks.Lock queueLock = null;

		try {
			queueLock = hazelcastClientService
					.getNativeLock(HzConstants.NOTIFICATION_RECEIVE_SMS);
			if (queueLock != null
					&& queueLock.tryLock(
							HzConstants.NOTIFICATION_QUEUE_WAIT_TIME,
							TimeUnit.SECONDS)) {

				logger.debug("About to read SMS messages");
				checkAndProcessSMS();
			}
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			hazelcastClientService.releaseNativeLock(queueLock);
			
		}

	}
	
	public void checkAndProcessSMS() {
		
		ResultSet<Tenant> tenantResultSet = tenantService.findTenants(Tenant.class, new SimpleQuery());
		
		TwilioRestClient client = null;
		
		for (Tenant tenant : tenantResultSet.getResult()) {
			MsgDeliveryTenantSettings smsTenantSettings = configurationService.getRecipientConfig(tenant.getTenantId(), 
					MsgDeliveryType.SMS);
			
			MsgDeliveryProviderSettings smsDeliveryProviderSettings = smsTenantSettings.getDeliveryProviderSettings();
			
			if(smsDeliveryProviderSettings.getStatus() != MsgDeliveryProviderStatus.OK) {
				// proceed to next tenant if the provider settings for this tenant are not setup
				continue;
			}
			
			final Map<String, String> tenantSettingsMap = smsTenantSettings.getSettings();
			final Map<String, String> providerSettingsMap = smsDeliveryProviderSettings.getSettings();
			
			String accsid = providerSettingsMap.get(MsgDeliveryProviderSettings.TWILIO_PROVIDER_ACCOUNTID);
            String acckey = providerSettingsMap.get(MsgDeliveryProviderSettings.TWILIO_PROVIDER_AUTHKEY);
            String subAccId = Constants.TWILIO_PROVIDER_SUBACCOUNTID;

            String twilioNumber = tenantSettingsMap.get(MsgDeliveryTenantSettings.TWILIO_PROVIDER_FROMNUMBER);
            
            client = new TwilioRestClient(accsid, acckey);

            filterMap = new HashMap<String, String>();
            filterMap.put("To", twilioNumber);
		
            String notificationToPhoneNumber = System.getProperty(Constants.NOTIFICATION_DEV_SMS_NUMBER);

            if ( !isBlank(notificationToPhoneNumber) ) {
            	filterMap.put("From", notificationToPhoneNumber);
            }

            // Get the account and call factory class
            acct = client.getAccount(subAccId);
            
            checkAndProcessTenantSMS(); 
			
		}
		
	}

	private void checkAndProcessTenantSMS() {
		MessageList newMessages = acct.getMessages(filterMap);
		Boolean approved = false;
		
		Collection<SendNotification> matchingNotifications = null;
		SendNotification sendNotification = null;

		for (Message message : newMessages) {

			String messageContent = message.getBody();
			messageContent = NotificationUtil
					.getResponseWithOutWhiteSpace(messageContent);

			approved = NotificationUtil.getSMSResponseApproval(messageContent);

			String smsResponseCode = NotificationUtil
					.getSMSReponseCode(messageContent);

			if ((approved != null) && (!StringUtils.isBlank(smsResponseCode))) {

				logger.debug("The SMS Response code for this message is: "
						+ smsResponseCode);

				// Get the matching send Notification
				matchingNotifications = queuingService
						.getSendMessageByResponseCode(smsResponseCode);
				
				if(matchingNotifications != null && matchingNotifications.size() > 0) {
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
					
					notification.setMessageContent(messageContent);
					
					notification.setQueuedOn(new DateTime());
					
					queuingService.enqueueMessage(notification);

				} else {
					logger.info(
							"No matching send notification found for this message: ",
							message.getBody());
				}

			} else {
				logger.info(
						"No approval or SMS Response code for this message: ",
						message.getBody());
			}

			// Delete the message
			try {
				message.delete();
			} catch (TwilioRestException e) {
				logger.error("Unable to delete SMS message: ",
						message.getBody(), e);
			}

		}
	}

}
