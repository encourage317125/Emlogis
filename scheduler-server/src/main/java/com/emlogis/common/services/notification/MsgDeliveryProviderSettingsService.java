package com.emlogis.common.services.notification;

import com.emlogis.common.Constants;
import com.emlogis.common.services.BaseService;
import com.emlogis.model.notification.MsgDeliveryProviderSettings;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class MsgDeliveryProviderSettingsService extends BaseService {
	
	
	private final Logger logger = LoggerFactory.getLogger(MsgDeliveryProviderSettingsService.class);
	
	@PersistenceContext(unitName=Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    public EntityManager getEntityManager() {
        return entityManager;
    }
    
	public ResultSet<MsgDeliveryProviderSettings> findMsgDeliveryProviderSettings(
			SimpleQuery simpleQuery) {
		
		simpleQuery.setEntityClass(MsgDeliveryProviderSettings.class);
		SimpleQueryHelper sqh = new SimpleQueryHelper();
		return sqh.executeSimpleQueryWithPaging(getEntityManager(), simpleQuery);
	}

	/**
	 * Get MsgDeliveryProvider 
	 * @param providerId
	 * @return
	 */
	public MsgDeliveryProviderSettings getMsgDeliveryProvider(String providerId) {
		
		return entityManager.find(MsgDeliveryProviderSettings.class, providerId);
	}

	/**
	 * Create MsgDeliveryProvider
	 * @param providerId
	 * @return
	 */
    public MsgDeliveryProviderSettings createMsgDeliveryProviderSettings(String providerId) {
    	
		MsgDeliveryProviderSettings msgDeliveryProvider = new MsgDeliveryProviderSettings();
		msgDeliveryProvider.setId(providerId);
		entityManager.persist(msgDeliveryProvider);
		return msgDeliveryProvider;
	}

    /**
     * Update MsgDeliveryProvider
	 * @param msgDeliveryProvider
	 * @return
     */
    public MsgDeliveryProviderSettings update(MsgDeliveryProviderSettings msgDeliveryProvider) {
    	msgDeliveryProvider.touch();
        return entityManager.merge(msgDeliveryProvider);
    }

    /**
     * Delete MsgDeliveryProvider
	 * @param msgDeliveryProvider
	 */
    public void delete(MsgDeliveryProviderSettings msgDeliveryProvider) {
    	entityManager.remove(msgDeliveryProvider);
    }

    
}
