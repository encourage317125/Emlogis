package com.emlogis.common.services.notification;

import com.emlogis.common.Constants;
import com.emlogis.common.services.BaseService;
import com.emlogis.model.notification.NotificationSettings;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.UUID;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class NotificationSettingsService extends BaseService {
	
	@PersistenceContext(unitName=Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
	private EntityManager entityManager;

    public EntityManager getEntityManager() {
        return entityManager;
    }
    
    public NotificationSettings createNotificationSettings(int retryCount,int  maxDeliveryHours, int queueProcessingSize,
    		int expirationHours) {
    	NotificationSettings settings = new NotificationSettings(retryCount, maxDeliveryHours, queueProcessingSize, expirationHours);
    	
    	settings.setId(UUID.randomUUID().toString());
    	
    	entityManager.persist(settings);
    	
    	return settings;    	
    }
    
    public NotificationSettings getNotificationSettings() {
    	NotificationSettings settings = null;
    	
    	SimpleQuery settingsQuery = new SimpleQuery();
    	settingsQuery.setEntityClass(NotificationSettings.class);
    	
    	Collection<NotificationSettings> settingsResultList = new SimpleQueryHelper().executeSimpleQuery(entityManager, settingsQuery);
    	
    	if( (settingsResultList != null) &&  (settingsResultList.size() != 0) ){
    		settings = settingsResultList.iterator().next();
    	} else {
    		settings = new NotificationSettings(2, 24, 10, 24);
    	}
    	
    	return settings;
    	
    }

}
