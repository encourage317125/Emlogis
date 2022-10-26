package com.emlogis.common.services.notification;

import static com.emlogis.common.services.notification.NotificationUtil.copyNotificationMessage;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.event.selector.Selector;
import reactor.event.selector.Selectors;

import com.emlogis.common.Constants;
import com.emlogis.common.PropertyUtil;
import com.emlogis.common.ResourcesBundle;
import com.emlogis.common.UniqueId;
import com.emlogis.common.notifications.NotificationResult;
import com.emlogis.common.notifications.NotificationRole;
import com.emlogis.common.notifications.NotificationType;
import com.emlogis.common.notifications.NotificationTypeMap;
import com.emlogis.common.services.BaseService;
import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.services.util.AccountUtilService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.NotificationConfig;
import com.emlogis.model.notification.MsgDeliveryProviderSettings;
import com.emlogis.model.notification.MsgDeliveryProviderStatus;
import com.emlogis.model.notification.MsgDeliveryTenantSettings;
import com.emlogis.model.notification.MsgDeliveryType;
import com.emlogis.model.notification.Notification;
import com.emlogis.model.notification.NotificationStatusType;
import com.emlogis.model.notification.SendNotification;
import com.emlogis.model.notification.dto.NotificationDTO;
import com.emlogis.model.notification.dto.NotificationMessageDTO;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.tenant.AccountStatus;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.server.services.ESClientService;
import com.emlogis.server.services.eventservice.ASEventService;
import com.emlogis.server.services.eventservice.NotificationClient;
import com.emlogis.server.services.eventservice.NotificationConsumer;
import com.emlogis.shared.services.eventservice.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@DependsOn({"StartupServiceBean"})
public class NotificationService extends BaseService {

    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @EJB
    TenantService tenantService;

    @EJB
    NotificationConfigurationService configurationService;

    @EJB
    NotificationQueuingService queuingService;

    @EJB
    NotificationSendingDeliveryService sendingDeliveryService;

    @EJB
    UserAccountService userService;
    
    @EJB
    AccountUtilService userUtilService;

    @EJB
    private ResourcesBundle resourcesBundle;

    @Inject
    private ASEventService eventService;

    @Inject
    ESClientService esClientService;

    @Inject()
    @Typed(NotificationClientImpl.class)
    NotificationClient notificationClient;

    NotificationConsumer<Object> notificationConsumer;
    
    private Random random = new Random();

    @PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    public EntityManager getEntityManager() {
        return entityManager;
    }

    @PostConstruct
    public void init() {
        String useConsumerService = System.getProperty(Constants.NOTIFICATION_DEV_CONSUMER);

        if (StringUtils.isNotBlank(useConsumerService) && useConsumerService.equalsIgnoreCase("yes")) {
            // Example of how to register for Notification events
            notificationConsumer = new NotificationConsumer<>(notificationClient);
            Selector selector = Selectors.R("<" + EventService.TOPIC_NOTIFICATION + ">.*");
            eventService.on(selector, notificationConsumer);
            logger.debug("Notification Example Turned on");
        } else {
            logger.debug("Notification Example Turned Off");
        }
    }

    public NotificationConfigInfo userHasNotificationEnabled(UserAccount user, MsgDeliveryType msgDeliveryType) {
            	
        NotificationConfigInfo config = null;
        String info = "";
        
        // Get user Locale
        
        String language = "en";
        Locale userLocale = null;
        
        if(user == null) {
        	info = resourcesBundle.getMessage(language, Constants.NMESSAGE_NO_USER);
        	config = new NotificationConfigInfo(info);
        	return config;
        }
        
        if( !StringUtils.isBlank(user.getLanguage()) ) {
        	language = user.getLanguage();
        }
        
        Site site = userUtilService.getUserSite(user);
        
        if(site != null) {
        	userLocale = userUtilService.getUserLocale(user, site);
            language = userLocale.getLanguage();
        }    
              
        
        
        // Check for Active Account
        if(user.getStatus() != AccountStatus.Active &&
        		user.getStatus() != AccountStatus.PendingConfirmation &&
        		user.getStatus() != AccountStatus.PendingPwdChange){
        	info = resourcesBundle.getMessage(language, Constants.NMESSAGE_USER_ACC_DISABLED);
        	config = new NotificationConfigInfo(info);
        	return config;
        }
        
        
        // Check employee and tenant notifications enabled
        
        if(!user.isNotificationEnabled()) {
        	info = resourcesBundle.getMessage(language, Constants.NMESSAGE_USER_NOTIF_DISABLED);
        	config = new NotificationConfigInfo(info);
        	return config;
        }
        
        Set<NotificationConfig> notificationConfigSet = user.getNotificationConfigs();
        
        boolean userDeliveryVerified = false;
        Iterator<NotificationConfig> notifIterator = notificationConfigSet.iterator();
        
        String deliveryAddress = "";
              
        
        configLoop: while (notifIterator.hasNext()) {
            NotificationConfig notifConfig = notifIterator.next();

            if (notifConfig.getEnabled()) {
                
                switch (notifConfig.getMethod()) {
                                
                case CorporateEmail:
                    if(msgDeliveryType == MsgDeliveryType.EMAIL && !StringUtils.isBlank(user.getWorkEmail()) ){
                    	userDeliveryVerified = true;
                    	deliveryAddress = user.getWorkEmail();
                    	
	                    // If the work email is configured and the delivery type is email,
	                    // we want to use that and stop looking
                	
                    	break configLoop;
                    }                    
                    
                    break;
                
                case PersonalEmail:
                    if(msgDeliveryType == MsgDeliveryType.EMAIL && !StringUtils.isBlank(user.getHomeEmail()) ){
                    	userDeliveryVerified = true;
                    	deliveryAddress = user.getHomeEmail();
                    }
                    break;
                    
                case SMS:
                    if(msgDeliveryType == MsgDeliveryType.SMS && !StringUtils.isBlank(user.getMobilePhone()) ){
                    	userDeliveryVerified = true;
                    	deliveryAddress = user.getMobilePhone();
                    }
                    break;                        
                }                
            }
        }
        
        if(!userDeliveryVerified) {
        	info = resourcesBundle.getMessage(language, Constants.NMESSAGE_USER_CONFIG_DISABLED,msgDeliveryType.toString());
        	config = new NotificationConfigInfo(info);
        	return config;
        }               
        
        if(site!=null && !site.getIsNotificationEnabled()){
        	info = resourcesBundle.getMessage(language, Constants.NMESSAGE_SITE_NOTIF_DISABLED);
        	config = new NotificationConfigInfo(info);
        	return config;
        }
        
        String tenantId = user.getTenantId();
        
        if(StringUtils.isBlank(tenantId)) {
        	info = resourcesBundle.getMessage(language, Constants.NMESSAGE_NO_TENANT);
        	config = new NotificationConfigInfo(info);
        	return config;
        } 
        
        MsgDeliveryTenantSettings tenantSettings = configurationService.getRecipientConfig(tenantId, msgDeliveryType);
        
        MsgDeliveryProviderSettings providerSettings = tenantSettings.getDeliveryProviderSettings();
        
        MsgDeliveryProviderStatus providerStatus = providerSettings.getStatus();
    	
    	if (providerStatus != MsgDeliveryProviderStatus.OK) {
    		info = resourcesBundle.getMessage(language, Constants.NMESSAGE_USER_TENANT_NOT_CONFIGURED, msgDeliveryType.toString());
        	config = new NotificationConfigInfo(info);
        	return config;
    	}
    	
    	// This user account appears to be setup correctly for this delivery type
    	info = resourcesBundle.getMessage(language, Constants.NMESSAGE_USER_CONFIGURED,msgDeliveryType.toString());
    	
    	config = new NotificationConfigInfo(info, deliveryAddress );        
        
        return config;
    }

    public String sendNotification(NotificationMessageDTO notificationMessageDTO) {
        try {
            SendNotification sendNotification = copyNotificationMessage(notificationMessageDTO);
            String notificationId = processNotification(sendNotification);
            return notificationId;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public Notification createNotification(NotificationDTO notificationDTO) {
        SendNotification notification = new SendNotification();

        if (!StringUtils.isBlank(notificationDTO.getTenantId())) {
            notification.setTenantId(notificationDTO.getTenantId());
        }

        if (!StringUtils.isBlank(notificationDTO.getSenderUserId())) {
            notification.setSenderUserId(notificationDTO.getSenderUserId());
        }

        if (!StringUtils.isBlank(notificationDTO.getReceiverUserId())) {
            notification.setReceiverUserId(notificationDTO.getReceiverUserId());
        }

        if (notificationDTO.getNotificationOperation() != null) {
            notification.setNotificationOperation(notificationDTO.getNotificationOperation());
        }

        if (notificationDTO.getNotificationCategory() != null) {
            notification.setNotificationCategory(notificationDTO.getNotificationCategory());
        }
        
        notification.setNotificationRole(NotificationRole.NONE);
        notification.setIsWorkflowType(false);


        Map<String, String> messageAttributeMap = new HashMap<>();
        messageAttributeMap.put("recipientName", "Test recipientName");
        messageAttributeMap.put("requestDate", "Test requestDate");
        messageAttributeMap.put("originatorTeam", "Test originatorTeam");
        messageAttributeMap.put("originatorName", "Test originatorName");
        messageAttributeMap.put("tenant", "Test tenant");
        messageAttributeMap.put("eventDate", "Test eventDate");
        messageAttributeMap.put("link", "Test link");
        messageAttributeMap.put("code", "Test code");
        messageAttributeMap.put("recipientAccount", "Test recipientAccount");
        messageAttributeMap.put("tenant", "Test tenant");
        messageAttributeMap.put("notificationName", "Test notificationName");

        Map<String, String> shift = new HashMap<>();
        List<Map<String, String>> shiftList = new ArrayList<>();

        shift.put("skillName", "Engineer");
        shift.put("team", "A TEam");
        shift.put("shiftDate", "7/1/2015");
        shift.put("shiftStartTime", "8:00am");
        shift.put("shiftEndTime", "8:00pm");
        shiftList.add(shift);

        ObjectMapper objectMapper = new ObjectMapper();

        StringWriter writer = new StringWriter();

        try {
            objectMapper.writeValue(writer, shiftList);
        } catch (Exception e) {
            logger.error("Error getting openshift template value for testing: ", e);
        }

        String openShiftListString = writer.toString();
        messageAttributeMap.put("shiftListString", openShiftListString);
        messageAttributeMap.put("shiftListOverflow", "false");

        notification.setMessageAttributes(messageAttributeMap);

        processNotification(notification);

        return notification;
    }

    private String processNotification(SendNotification notification) {
        notification.setId(UniqueId.getId());

        UserAccount sender;

        if (notificationHasNoReceiver(notification)) {
            logger.error("Notification failure - Tenant Or Receiver is null - Notification ID - " + notification.getId());
            notification.setStatus(NotificationStatusType.FAILED);
            return null;
        }

        if (!validateNotification(notification)) {
            logger.error("Notification failure - Invalid Send Notification - Notification ID - " + notification.getId());
            notification.setStatus(NotificationStatusType.FAILED);
            return null;
        }

        //check if that receiver exists
        UserAccount userAccount = userWithEnabledNotifications(notification, true);
        if (userAccount == null) {
            return null;
        }

        //check that user site has notifications enabled
        Site site = getUserSite(userAccount, notification);
        if (site == null) {
        	logger.warn("Notification warning - user account does not have a site - user ID - " + userAccount.getId());
        } else {
        	// Check that site is enabled
            if(!siteEnabled(site, notification)) {
            	return null;
            }
        }
                
        // Check TenantId
        String tenantId = notification.getTenantId();

        if (!StringUtils.isBlank(notification.getSenderUserId())) {
            PrimaryKey senderUserKey = new PrimaryKey(tenantId, notification.getSenderUserId());
            sender = userService.getUserAccount(senderUserKey);
            notification.setSenderName(sender.getFirstName() + " " + sender.getLastName());
        }

        notification.setReceiverName(userAccount.getFirstName() + " " + userAccount.getLastName());

        Set<NotificationConfig> notificationConfigSet = userAccount.getNotificationConfigs();

        Iterator<NotificationConfig> notifIterator = notificationConfigSet.iterator();

        int messageNumber = 0;
        String deliveryAddress = "";

        boolean queuedMessage = false;

        while (notifIterator.hasNext()) {
            NotificationConfig notifConfig = notifIterator.next();

            if (notifConfig.getEnabled()) {
                queuedMessage = true;
                switch (notifConfig.getMethod()) {
                    case CorporateEmail:
                    	deliveryAddress = userAccount.getWorkEmail();
                        messageNumber = queueMessageNotification(notification, tenantId, messageNumber,
                                MsgDeliveryType.EMAIL, notifConfig, deliveryAddress);
                        break;
                    case PersonalEmail:
                    	deliveryAddress = userAccount.getHomeEmail();
                        messageNumber = queueMessageNotification(notification, tenantId, messageNumber,
                                MsgDeliveryType.EMAIL, notifConfig, deliveryAddress);
                        break;
                    case SMS:
                        if (notification.getEmailOnly()) {
                            continue;
                        }
                        deliveryAddress = userAccount.getMobilePhone();
                        messageNumber = queueMessageNotification(notification, tenantId, messageNumber,
                                MsgDeliveryType.SMS, notifConfig, deliveryAddress);
                        
                        break;
                    default:
                        logger.error("Notification failure - Delivery Method is invalid - " + notifConfig.getMethod());
                        break;
                }
            }

        }

        if (queuedMessage) {
            sendingDeliveryService.notifyMessagesForDelivery();
        }

        return notification.getId();
    }

    private int queueMessageNotification(SendNotification notification, String tenantId, int messageNumber,
                                         MsgDeliveryType deliveryType, NotificationConfig notificationConfig,
                                         String deliveryAddress) {
        SendNotification messageNotification = new SendNotification();
        NotificationUtil.copyCommonNotificationData(notification, messageNotification);

        messageNotification.setId(notification.getId() + "-" + messageNumber++);
        messageNotification.setDeliveryFormat(notificationConfig.getFormat());

        messageNotification.setDeliveryType(deliveryType);

        MsgDeliveryTenantSettings deliveryTenantSettings = configurationService.getRecipientConfig(tenantId, deliveryType);
        MsgDeliveryProviderSettings providerSettings =
                deliveryTenantSettings.getDeliveryProviderSettings();
        
        if(StringUtils.isBlank(deliveryAddress)) {
        	deliveryAddress = Constants.NOTIFICATION_EMPTY_VALUE;
        	messageNotification.setStatus(NotificationStatusType.FAILED);
        }

        messageNotification.setProviderId(providerSettings.getId());
        
        messageNotification.setToAddress(deliveryAddress);
        
        messageNotification.getMessageAttributes().put("deliveryAdddress", deliveryAddress);
        
        int userResponseCode = NotificationUtil.getSMSResponseCode(random);
        messageNotification.setUserResponseCode(String.valueOf(userResponseCode));
        
        messageNotification.getMessageAttributes().put(Constants.NOTIFICATION_RESPONSE_CODE,
                messageNotification.getUserResponseCode() );
        
        String appServerId = PropertyUtil.sysProp("applicationServerName");

        messageNotification.getMessageAttributes().put("notificationId", messageNotification.getId());
        messageNotification.getMessageAttributes().put("serverId", appServerId);
        messageNotification.setAppServerId(appServerId);

        queuingService.enqueueMessage(messageNotification);
        return messageNumber;
    }

    public List<Notification> createNotifications(List<NotificationDTO> notificationDTOList) {
        List<Notification> notiList = new ArrayList<>();

        Notification notification;

        for (NotificationDTO notificationDTO : notificationDTOList) {
            notification = createNotification(notificationDTO);

            notiList.add(notification);
        }

        return notiList;
    }

    private Boolean validateNotification(SendNotification notification) {
        return notification.getNotificationOperation() != null && notification.getMessageAttributes() != null;
    }

    private UserAccount userAccount(String tenantId, String id) {
        try {
            return userService.getUserAccount(new PrimaryKey(tenantId, id));
        } catch (Throwable throwable){
            throw new RuntimeException(throwable);
        }
    }

    private Boolean notificationHasNoReceiver(Notification notification) {
        return isBlank(notification.getReceiverUserId()) || isBlank(notification.getTenantId());
    }

    private Boolean userHasNotificationTypeEnabled(UserAccount userAccount, Notification notification) {
        NotificationType notificationType = NotificationTypeMap.getNotificationType(
                notification.getNotificationOperation(),
                notification.getNotificationCategory(),
                notification.getNotificationRole(),
                notification.getIsWorkflowType());
        return userAccount.getNotificationTypes().containsKey(notificationType) ?
                userAccount.getNotificationTypes().get(notificationType) : false;
    }

    private Boolean userHasSameNotificationTypeEnabledAndUserNotificationsEnabled(UserAccount userAccount,
            Notification notification, boolean checkType) {
    	
    	if(checkType) {
    		return userAccount.isNotificationEnabled() && userHasNotificationTypeEnabled(userAccount, notification);
    	} else {
    		return userAccount.isNotificationEnabled();
    	}
        
    }

    private Site getUserSite(UserAccount userAccount, Notification notification) {
        Site site = null;
        try {
            site = userUtilService.getUserSite(userAccount);
        } catch (Exception e) {
            logger.error("Could not find site for user with ID: " + userAccount.getId() , e);
            notification.setStatus(NotificationStatusType.FAILED);
            logger.error("Notification failure - Site is null - Notification ID - " + notification.getId());
            return null;
        }

        return site;
    }
    
    private boolean siteEnabled(Site site, Notification notification) {
    	boolean retVal = false;
    	
    	if(site.getIsNotificationEnabled()) {
    		retVal = true;
    	} else {
    		// Log elasticsearch site notification is turned off
    		notification.setStatus(NotificationStatusType.FAILED);
            esClientService.indexNotification(notification, "site_notifications_turned_off");
    	}
    	
    	return retVal;
    }

    private UserAccount userWithEnabledNotifications(SendNotification notification, boolean checkType) {
    	UserAccount userAccount = null;
        try {
            userAccount = userAccount(notification.getTenantId(), notification.getReceiverUserId());
        } catch (Exception error) {
            logger.error("Notification failure - user is null - Notification ID - " + notification.getId(), error);
            notification.setStatus(NotificationStatusType.FAILED);
            return null;
        }
        //check if receiver has this notification type settled and has notifications enabled
        if (!userHasSameNotificationTypeEnabledAndUserNotificationsEnabled(userAccount, notification, checkType)) {
            // Log elastic search user notifications turned off
            notification.setStatus(NotificationStatusType.FAILED);
            esClientService.indexNotification(notification, "user_notifications_turned_off");
            return null;
        }
        
        // Check that the user account is active
        if(userAccount.getStatus() != AccountStatus.Active &&
        		userAccount.getStatus() != AccountStatus.PendingConfirmation &&
        				userAccount.getStatus() != AccountStatus.PendingPwdChange){
        	notification.setStatus(NotificationStatusType.FAILED);
        	logger.error("Notification failure - user is not active - User ID - " + userAccount.getId());
        	return null;        	 
        }
        
        return userAccount;
    }

    public NotificationResult sendGeneralNotification(NotificationMessageDTO notificationMessageDTO, 
    		MsgDeliveryType deliveryType) {
    	
    	String language = "en";
    	String info = resourcesBundle.getMessage(language, Constants.NMESSAGE_SENT_SUCCESS);
    	
    	NotificationResult notificationResult = new NotificationResult(true, info);
    	
    	SendNotification sendNotification = copyNotificationMessage(notificationMessageDTO);
    	
    	// process notification for this user
    	
    	notificationResult = processGeneralNotification(sendNotification, deliveryType, notificationResult);
    	
        return notificationResult;
    }
    
    private NotificationResult processGeneralNotification (SendNotification notification, MsgDeliveryType msgDeliveryType,
    		NotificationResult notificationResult){
    	
    	notification.setId(UniqueId.getId());

        UserAccount sender;
        
        String language = "en";
        String info = "";

        if (notificationHasNoReceiver(notification)) {
            logger.error("Notification failure - Tenant Or Receiver is null - Notification ID - " + notification.getId());
            notification.setStatus(NotificationStatusType.FAILED);
            
            info = resourcesBundle.getMessage(language, Constants.NMESSAGE_BAD_PARAM);
            notificationResult.setMessage(info);
            notificationResult.setSucessfullySent(false);
            return notificationResult;
        }

        if (!validateNotification(notification)) {
            logger.error("Notification failure - Invalid Send Notification - Notification ID - " + notification.getId());
            notification.setStatus(NotificationStatusType.FAILED);
            info = resourcesBundle.getMessage(language, Constants.NMESSAGE_BAD_PARAM);
            notificationResult.setMessage(info);
            notificationResult.setSucessfullySent(false);
            return notificationResult;
        }

        //check if that receiver exists
        UserAccount userAccount = userWithEnabledNotifications(notification, false);
        if (userAccount == null) {
        	info = resourcesBundle.getMessage(language, Constants.NMESSAGE_NO_USER);
            notificationResult.setMessage(info);
            notificationResult.setSucessfullySent(false);
            return notificationResult;
        }

        //check that user site has notifications enabled
        Locale locale = null;
        Site site = getUserSite(userAccount, notification);
        if (site != null) {
        	
            // Check that site is enabled
            if(!siteEnabled(site, notification)) {
            	return notificationResult;
            }
            
        	locale = userUtilService.getUserLocale(userAccount, site);
        	language = locale.getLanguage();
        } else {
        	notification.setStatus(NotificationStatusType.PENDING);
        }
        
        // Check TenantId
        String tenantId = notification.getTenantId();

        if (!StringUtils.isBlank(notification.getSenderUserId())) {
            PrimaryKey senderUserKey = new PrimaryKey(tenantId, notification.getSenderUserId());
            sender = userService.getUserAccount(senderUserKey);
            notification.setSenderName(sender.getFirstName() + " " + sender.getLastName());
        }

        notification.setReceiverName(userAccount.getFirstName() + " " + userAccount.getLastName());

        Set<NotificationConfig> notificationConfigSet = userAccount.getNotificationConfigs();

        Iterator<NotificationConfig> notifIterator = notificationConfigSet.iterator();

        int messageNumber = 0;

        boolean queuedMessage = false;
        
        NotificationConfig selectedNotifConfig = null;
        
        String deliveryAddress = "";

        configLoop: while (notifIterator.hasNext()) {
            NotificationConfig notifConfig = notifIterator.next();

            
            if (notifConfig.getEnabled()) {
                queuedMessage = true;
                switch (notifConfig.getMethod()) {
                    case CorporateEmail:
                    	if(msgDeliveryType == MsgDeliveryType.EMAIL && !StringUtils.isBlank(userAccount.getWorkEmail()) ){
                        	selectedNotifConfig = notifConfig;
                        	deliveryAddress = userAccount.getWorkEmail();
                        	
    	                    // If the work email is configured and the delivery type is email,
    	                    // we want to use that and stop looking
                    	
                        	break configLoop;
                        }   
                    	
                        break;
                    case PersonalEmail:
                    	if(msgDeliveryType == MsgDeliveryType.EMAIL && !StringUtils.isBlank(userAccount.getHomeEmail()) ){
                    		selectedNotifConfig = notifConfig;
                    		deliveryAddress = userAccount.getHomeEmail();
                        }
                        break;
                    case SMS:
                    	if(msgDeliveryType == MsgDeliveryType.SMS && !StringUtils.isBlank(userAccount.getMobilePhone()) ){
                    		selectedNotifConfig = notifConfig;
                    		deliveryAddress = userAccount.getMobilePhone();
                        }
                        break;
                    default:
                        logger.error("Notification failure - Delivery Method is invalid - " + notifConfig.getMethod());
                        break;
                }
            }

        }

        if (selectedNotifConfig != null) {
        	
        	messageNumber = queueMessageNotification(notification, tenantId, messageNumber,
                    msgDeliveryType, selectedNotifConfig, deliveryAddress);
        	
        	notificationResult.setNotificationId(notification.getId());        	
            sendingDeliveryService.notifyMessagesForDelivery();            
             
        } else {
        	info = resourcesBundle.getMessage(language, Constants.NMESSAGE_USER_CONFIG_DISABLED);
            notificationResult.setMessage(info);
            notificationResult.setSucessfullySent(false);
            return notificationResult;
        }
        
        

        return notificationResult;
    }

}
