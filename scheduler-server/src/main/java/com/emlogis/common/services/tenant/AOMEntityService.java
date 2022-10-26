package com.emlogis.common.services.tenant;

import com.emlogis.common.Constants;
import com.emlogis.model.AOMEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.server.services.AOMService;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class AOMEntityService<T extends AOMEntity> {

	@PersistenceContext(unitName=Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    @EJB
    AOMService aomService;				// metamodel management service

    public EntityManager getEntityManager() {
        return entityManager;
    }

	public AOMEntity getAOMEntity(PrimaryKey primaryKey) {
		return getEntityManager().find(AOMEntity.class, primaryKey);
	}
	
    protected void insert(T aomEntity) {
        entityManager.persist(aomEntity);
    }

    public T update(T aomEntity) {
        aomEntity.touch();
        return entityManager.merge(aomEntity);
    }

    public void delete(T aomEntity) {
    	entityManager.remove(aomEntity);
    }


    //-----------------------------------------------------------------------------------------------------------------------
    // AOM property  management   => must go into AOMEntity servicex
   
    // getProperty( name)
    // if json not initilized, get it form src attribute
    // based on metamodel, invoke appropriate json getter and convert to appropriate tyep
    
    // setProperty( name)
    // use metamodel to validate data (convert it ..)
    // if json not initilized, get it form src attribute
    // based on metamodel invoke appropraiet json setter 
    
    // get properties()
    // get propertiesAJson()
    // return list of keys .. ? + metamodel type ?
    // then getProperty()

    // get propertyNames()
    // get propertiesAJson()
    // return list of keys .

    
    // get propertiesAJson()
    // if json not initilized, get it form src attribute
    // return json
	/*
	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	*/
}
