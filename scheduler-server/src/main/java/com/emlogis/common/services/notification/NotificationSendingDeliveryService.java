package com.emlogis.common.services.notification;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.notification.NotificationServiceException;
import com.emlogis.common.notifications.NotificationDeliveryFormat;
import com.emlogis.common.services.BaseService;
import com.emlogis.common.services.notification.template.NotificationMessageTemplateLoader;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.services.util.AccountUtilService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.notification.MsgDeliveryProviderSettings;
import com.emlogis.model.notification.MsgDeliveryProviderStatus;
import com.emlogis.model.notification.MsgDeliveryTenantSettings;
import com.emlogis.model.notification.MsgDeliveryType;
import com.emlogis.model.notification.Notification;
import com.emlogis.model.notification.NotificationMessage;
import com.emlogis.model.notification.NotificationStatusType;
import com.emlogis.model.notification.SendNotification;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.server.services.ESClientService;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.resource.factory.MessageFactory;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

@Startup
@Singleton
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@DependsOn({"NotificationService"})
public class NotificationSendingDeliveryService extends BaseService {

    private final Logger logger = LoggerFactory.getLogger(NotificationSendingDeliveryService.class);

    @EJB
    NotificationQueuingService queuingService;

    @EJB
    NotificationReceivingDeliveryService receivingDeliveryService;

    @EJB
    UserAccountService userAccountService;
    
    @EJB
    AccountUtilService accountUtilServiceService;

    @EJB
    @Typed(NotificationMessageTemplateLoader.class)
    private TemplateLoader templateLoader;

    private Configuration templateConfiguration;

    
    @EJB
    NotificationConfigurationService configurationService;

    @Inject
    ESClientService esClientService;

    private AtomicBoolean busy = new AtomicBoolean(false);

    @Lock(LockType.READ)
    @Asynchronous
    public void notifyMessagesForDelivery() {
        try {
            logger.debug("Request message delivery now");
            processMessages();
        } catch (InterruptedException e) {
            logger.error("processMessages was Interrupted");
        }
    }
    
    @Lock(LockType.READ)
    public void notifyMessagesForDeliverySync() {
        try {
            logger.debug("Request message delivery now");
            processMessages();
        } catch (InterruptedException e) {
            logger.error("processMessages was Interrupted");
        }
    }

    @PostConstruct
    @Lock(LockType.READ)
    public void init() {
        logger.info("Setup template configuration");
        templateConfiguration = new Configuration();
        templateConfiguration.setTemplateLoader(templateLoader);
    }
    
   

    @Lock(LockType.READ)
    private void processMessages() throws InterruptedException {
    	if (!busy.compareAndSet(false, true)) {
            return;
        }
    	
    	try {
        	logger.debug("About to get notifications to send");
	        boolean foundMessages = true;
	        
	        while(foundMessages) {
	        	Collection<SendNotification> sendNotificationsList = queuingService.getMessagesToSend();
	
	            if (sendNotificationsList != null && sendNotificationsList.size() > 0) {
	                for (SendNotification notification : sendNotificationsList) {
	                    logger.debug("The id of the nextmessage is:" + notification.getId());
	
	                    sendFormattedNotificationMessage(notification);                                        
	                }
	                queuingService.updateMessagesStatus(sendNotificationsList);
	            } else {
	            	foundMessages = false;
	            }
	        }
	
	        logger.debug("Sent notifications");
        }finally {
        	busy.set(false);
        }

    }

    @Lock(LockType.READ)
    private void sendFormattedNotificationMessage(SendNotification notification) {
    	
    	UserAccount userAccount;
		//check that userAccount site has notifications enabled
		Site site;
		try {
			PrimaryKey userAccountKey = new PrimaryKey(notification.getTenantId(), notification.getReceiverUserId());
			
			userAccount = userAccountService.getUserAccount(userAccountKey);

			if (userAccount == null) {
				logger.error("Error sending notification, invalid user id: " + notification.getReceiverUserId());
				notification.setStatus(NotificationStatusType.FAILED);
			    return;
			}
			site = accountUtilServiceService.getUserSite(userAccount);
			if(site != null && !site.getIsNotificationEnabled()) {
				logger.error("Error sending notification, site notification is disabled: " + site.getName());
				notification.setStatus(NotificationStatusType.RETRYING);
				return;
			}

		} catch (Exception e) {
			logger.error("Error sending notification, for user id: ", notification.getReceiverUserId(), e);
			notification.setStatus(NotificationStatusType.FAILED);
			return;
		}
		
		Locale userLocale = accountUtilServiceService.getUserLocale(userAccount, site);
		DateTimeZone userTimeZone = accountUtilServiceService.getActualTimeZone(userAccount);
		
        //process notification message
        NotificationMessage notificationMessage = new NotificationMessage(notification.getNotificationOperation(), notification.getNotificationCategory(), notification.getNotificationRole(),
        		userLocale.getLanguage(), userTimeZone, notification.getMessageAttributes(),
                templateConfiguration);

        //finally send notification
        try {
            sendNotification(userAccount, notification, notificationMessage);
            notification.setStatus(NotificationStatusType.PROCESSED);
            notification.setDeliveredOn(new DateTime());
        } catch (NotificationServiceException e) {
        	notification.setStatus(NotificationStatusType.RETRYING);
            logger.error("Error sending notification", e);
        }
    }
    
    


    // TODO: The send methods will be changed to send data to eamil/sms addresses passed to methods when we can insure
    // real customers will not receive erroneous messages

    @Lock(LockType.READ)
    private void sendNotification(UserAccount userAccount, Notification notification,NotificationMessage notificationMessage) throws NotificationServiceException {

        String smsId;


        String tenantId = notification.getTenantId();
        
        MsgDeliveryType deliveryType = notification.getDeliveryType();
        
        MsgDeliveryProviderSettings providerSettings = configurationService.getProviderSettings(notification.getProviderId());       
        
        MsgDeliveryTenantSettings tenantSettings = null;
        
        
        
        switch (deliveryType) {
        
        case EMAIL : 
        	tenantSettings = configurationService.getRecipientConfig(tenantId, MsgDeliveryType.EMAIL);
        	sendMail(notificationMessage.formatSubject(notification.getDeliveryFormat()),
                    notificationMessage.formatBody(notification.getDeliveryFormat()),
            		notification.getFromAddress(), notification.getToAddress(), notification.getAppServerId(),
            		notification.getDeliveryFormat(), tenantId, tenantSettings);
        	
        	break;
        	
        case SMS :
        	tenantSettings = configurationService.getRecipientConfig(tenantId, MsgDeliveryType.SMS);
        	smsId = sendSms(notificationMessage.formatBody(notification.getDeliveryFormat()), notification.getToAddress(), 
        			notification.getAppServerId(),tenantId,
        			tenantSettings);
        	notification.setProviderMessageId(smsId);
        	
        	break;
        	
        	default :
        		
        		logger.error("Notification failure - Delivery Type is invalid - id: " + notification.getId());
        		throw new NotificationServiceException("Delivery Type is invalid");
                
        }

    }


    @Lock(LockType.READ)
    private void sendMail(String subject, String body, String fromAddress, String toAddress, String appServerID,
                             NotificationDeliveryFormat notificationDeliveryFormat,
                             String tenant, MsgDeliveryTenantSettings tenantSettings) throws NotificationServiceException {
    	MsgDeliveryProviderSettings providerSettings = tenantSettings.getDeliveryProviderSettings();
    	MsgDeliveryProviderStatus providerStatus = providerSettings.getStatus();
    	
    	if (providerStatus != MsgDeliveryProviderStatus.OK) {
    		// Cannot continue since provider status is not okay
    		logger.error("Notification failure - email Delivery Provider status is not OK for Tenant: - " + tenant);
    		return;
    	}
    	
    	String tempDevString = "\n\n AppServerID: " + appServerID + "\n\n Email Address: " + toAddress;
    	body = body + tempDevString;
    	
        // Check that whether production mode or development mode
    	String notificationMode = System.getProperty(Constants.NOTIFICATION_MODE);
    	
    	if(isBlank(notificationMode) || !notificationMode.equalsIgnoreCase(Constants.NOTIFICATION_MODE_PROD)) {
    		
    		// if we are not in production mode, we need to use the emaill accoun in dev
    		String notificationToEmail = System.getProperty(Constants.NOTIFICATION_DEV_TO);
            
            if (isBlank(notificationToEmail) || notificationToEmail.equalsIgnoreCase("false")) {
                logger.debug("No Dev email property specified");
                return;
            }
            
            toAddress = notificationToEmail;
    	}
               
        final Map<String, String> settingsMap = providerSettings.getSettings();
        final Map<String, String> tenantSettingsMap = tenantSettings.getSettings();

        Properties props = new Properties();

        props.put("mail.smtp.host", settingsMap.get(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_SENDHOST));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", settingsMap.get(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_SENDPORT));
        props.put("mail.smtp.starttls.enable", "true");
        // To see what is going on behind the scenes
        //props.put("mail.debug", "false");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(settingsMap.get(
                                MsgDeliveryProviderSettings.POPSMTP_PROVIDER_USERNAME),
                        		settingsMap.get(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_PASSWORD) );
                    }
                });

        try {

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(tenantSettingsMap.get(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_FROM)));
            InternetAddress[] replyAddress = {new InternetAddress(settingsMap.get(
                    MsgDeliveryProviderSettings.POPSMTP_PROVIDER_REPLYTO))};
            msg.setReplyTo(replyAddress);
            InternetAddress[] address = {new InternetAddress(toAddress)};
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSentDate(new Date());
            msg.setSubject(subject);

            if (notificationDeliveryFormat == NotificationDeliveryFormat.HTML) {
                msg.setContent(body, "text/html; charset=utf-8");
            } else {
                msg.setText(body);
            }

            msg.setDescription("Emlogis Notifications");
            Transport.send(msg);
        } catch (MessagingException mex) {
            // Prints all nested (chained) exceptions as well
            logger.error("Encountered error trying to deliver email", mex);

            // How to access nested exceptions
            while (mex.getNextException() != null) {
                // Get next exception in chain
                Exception ex = mex.getNextException();

                logger.error("Nested error trying to deliver email", ex);
                if (!(ex instanceof MessagingException))
                    break;
                else mex = (MessagingException) ex;
            }
            
            throw new NotificationServiceException("Encountered problem sending email");
        }

    }

    @Lock(LockType.READ)
    private String sendSms(String body, String toAddress, String appServerID, String tenant, MsgDeliveryTenantSettings tenantSettings) throws NotificationServiceException {
    	MsgDeliveryProviderSettings providerSettings = tenantSettings.getDeliveryProviderSettings();
    	
    	MsgDeliveryProviderStatus providerStatus = providerSettings.getStatus();
    	
    	if (providerStatus != MsgDeliveryProviderStatus.OK) {
    		// Cannot continue since provider status is not okay
    		logger.error("Notification failure - sms Delivery Provider status is not OK for Tenant: - " + tenant);
    		return null;
    	}
    	
    	
    	String tempDevString = "\n\n AppServerID: " + appServerID + "\n\n SMS Address: " + toAddress;
    	body = body + tempDevString;
    	
    	// Check that whether production mode or development mode
    	String notificationMode = System.getProperty(Constants.NOTIFICATION_MODE);
    	
    	if(isBlank(notificationMode) || !notificationMode.equalsIgnoreCase(Constants.NOTIFICATION_MODE_PROD)) {
            // Check that we have a dev phone number
            String notificationToPhoneNumber = System.getProperty(Constants.NOTIFICATION_DEV_SMS_NUMBER);

            if (isBlank(notificationToPhoneNumber) || notificationToPhoneNumber.equalsIgnoreCase("false")) {
                logger.error("No Dev phone number property specified");
                return null;
            }
            
            toAddress = notificationToPhoneNumber;
    	}

        final Map<String, String> tenantSettingsMap = tenantSettings.getSettings();
        final Map<String, String> providerSettingsMap = providerSettings.getSettings();
        
        TwilioRestClient client = new TwilioRestClient(providerSettingsMap.get(
                MsgDeliveryProviderSettings.TWILIO_PROVIDER_ACCOUNTID),
        		providerSettingsMap.get(MsgDeliveryProviderSettings.TWILIO_PROVIDER_AUTHKEY));
        
        String subAccId = Constants.TWILIO_PROVIDER_SUBACCOUNTID;

        List<NameValuePair> params = new ArrayList<>();

        params.add(new BasicNameValuePair("Body", body));
        params.add(new BasicNameValuePair("To", toAddress));
        params.add(new BasicNameValuePair("From", tenantSettingsMap.get(
                MsgDeliveryTenantSettings.TWILIO_PROVIDER_FROMNUMBER)));

        try {
            // send an SMS message
            // ( This makes a POST request to the Messages resource)
            MessageFactory messageFactory = client.getAccount(subAccId).getMessageFactory();
            com.twilio.sdk.resource.instance.Message message = messageFactory.create(params);
            return message.getSid();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new NotificationServiceException("Encountered problem sending SMS");
        }
    }

}
