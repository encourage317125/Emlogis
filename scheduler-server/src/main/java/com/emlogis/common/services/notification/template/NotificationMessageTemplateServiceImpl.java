package com.emlogis.common.services.notification.template;

import java.math.BigInteger;
import java.util.Collection;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.emlogis.common.Constants;
import com.emlogis.model.notification.template.NotificationMessageTemplate;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class NotificationMessageTemplateServiceImpl  {

	@PersistenceContext(unitName=Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;
    
    public Class<NotificationMessageTemplate> getEntityClass() {
        return NotificationMessageTemplate.class;
    }

    public NotificationMessageTemplate findTemplateByName(String name) {
    	
    	NotificationMessageTemplate retVal = null;
    	
    	Collection<NotificationMessageTemplate>  matchingTemplates = null;
    	SimpleQuery simpleQuery = new SimpleQuery();
    	
    	simpleQuery.addFilter("templateName='" + name + "'");
		simpleQuery.setEntityClass(getEntityClass());
		
		matchingTemplates = new SimpleQueryHelper().executeSimpleQuery(entityManager, simpleQuery);
		
		if( matchingTemplates!=null && !matchingTemplates.isEmpty() ) {
			retVal = matchingTemplates.iterator().next();
		}
		
        return retVal;
    }

	public void create(NotificationMessageTemplate template) {
		entityManager.persist(template);
		
	}

	public void deleteAll() {
		entityManager.createQuery("Delete from NotificationMessageTemplate").executeUpdate();		
	}

	public Object find(Long id) {
		return entityManager.find(NotificationMessageTemplate.class, id);
	}

	public Boolean templatesNameLikeExists(String templateNameLike) {
		try {
			String queryStr =
					" SELECT count(nmt.id) FROM EGS.NotificationMessageTemplate nmt " +
							"  WHERE nmt.templateName like '%" + templateNameLike + "%';";
			Query query = entityManager.createNativeQuery(queryStr);
			return ((BigInteger) query.getSingleResult()).longValue() > 0;
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			throw new RuntimeException(throwable);
		}
	}
}

