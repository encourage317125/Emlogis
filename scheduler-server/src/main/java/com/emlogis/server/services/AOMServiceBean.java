package com.emlogis.server.services;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.validation.annotations.Validate;
import com.emlogis.common.validation.annotations.Validation;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.aom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AOMServiceBean implements AOMService {
	
	private final Logger logger = LoggerFactory.getLogger(AOMServiceBean.class);
		
	@PersistenceContext(unitName=Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

	private	static Map<String, AOMTenantMetamodel> metamodels = new HashMap<>();	// map of metamodel definitions keyed by tenant ids

	public AOMServiceBean() {
		super();
		logger.debug("***************************** AOMServiceBean constructor");
	}

	@PostConstruct
	void init() {
		logger.debug("***************************** AOMServiceBean - Init: Loading Metamodels ....");
		
		//get nb of metamodels first, 
		
		String entityName = AOMTenantMetamodelEntity.class.getSimpleName();
		Object countResult;
		int metamodelCnt = 0;
		String queryStr = "SELECT COUNT(elts) FROM " + entityName + " elts";
		Query countQuery = entityManager.createQuery( queryStr);
		countResult = countQuery.getSingleResult();
		if (countResult!=null) {
			metamodelCnt = (((Long)countResult).intValue());
		}

		//then load them, say 10 by 10
		int loaded = 0;
		int maxresult = 10;
		queryStr = "SELECT elts  FROM " + entityName + " elts";
		while (loaded < metamodelCnt) {
			List<AOMTenantMetamodelEntity> result;
			Query query = entityManager.createQuery(queryStr);
			query.setFirstResult(loaded);
			query.setMaxResults(maxresult);
			result = query.getResultList();
			loaded += result.size();
			for (AOMTenantMetamodelEntity metaModelEntity : result) {
		    	try {
					addMetaModel(metaModelEntity.getTenantId(), metaModelEntity.getAomTenantMetamodelObj());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			logger.debug("Got: " + result.size() + " MetaModels");
			logger.debug("\r");
		}
		
		logger.debug("***************************** AOMServiceBean - Init complete: " + metamodels.size()
                + " Metamodels laded.");
	}

	@Override
	public AOMTenantMetamodel createAOMTenantMetamodel(String tenantId, String createdBy, String ownedBy)
            throws IOException {
		return doCreateAOMTenantMetamodel(new PrimaryKey(tenantId, AOMTenantMetamodelEntity.METAMODEL_ID), createdBy,
                ownedBy);
	}
	
    @Validation
	private AOMTenantMetamodel doCreateAOMTenantMetamodel(
            @Validate(validator = EntityExistValidatorBean.class, type = AOMTenantMetamodelEntity.class,
                    expectedResult = false)
            PrimaryKey aomMetaModelPK,
            String createdBy, 
            String ownedBy) throws IOException {
    	AOMTenantMetamodelEntity metaModelEntity = new AOMTenantMetamodelEntity(aomMetaModelPK);
    	metaModelEntity.setCreatedBy(createdBy);
    	metaModelEntity.setOwnedBy(ownedBy);
    	AOMTenantMetamodel tenantMetaModel = new AOMTenantMetamodel();
    	metaModelEntity.setAomTenantMetamodelObj(tenantMetaModel); 	
    	insert(metaModelEntity);
    	addMetaModel(aomMetaModelPK.getTenantId(), tenantMetaModel);
    	return tenantMetaModel;
	}

	@Override
	public AOMTenantMetamodel getAOMTenantMetamodel(String tenantId) {
		return metamodels.get(tenantId);
	}

	@Override
	public void deleteAOMTenantMetamodel(String tenantId) {
		doDeleteAOMTenantMetamodel(new PrimaryKey(tenantId, AOMTenantMetamodelEntity.METAMODEL_ID));
	}

    @Validation
	private void doDeleteAOMTenantMetamodel(
            @Validate(validator = EntityExistValidatorBean.class, type = AOMTenantMetamodelEntity.class,
                    expectedResult = true)
            PrimaryKey aomMetaModelPK) {
    	metamodels.remove(aomMetaModelPK.getTenantId());
		AOMTenantMetamodelEntity metaModelEntity = getAOMTenantMetamodelEntity(aomMetaModelPK.getTenantId());
		delete(metaModelEntity);
	}

	@Override
	public AOMRelationshipDef addAOMRelationshipDef(String tenantId, AOMRelationshipDef def, String updatedBy)
            throws IOException {
		// TODO harden this code
		AOMTenantMetamodel tenantMetaModel = getAOMTenantMetamodel(tenantId);
		if (tenantMetaModel == null) {
			throw new ValidationException("Unable to retrieve AOM Metamodel for: " + tenantId);
		}
		if (def == null || def.getType() == null) {
			throw new ValidationException("Invalid AOMRelationshipDef or missing attributes. " + tenantId);
		}		
		if (tenantMetaModel.getRelationshipDefs().containsKey(def.getType())) {
			throw new ValidationException("Unable to add new AOM Relationship type as type is already dfined: "
                    + def.getType());
		}
		// TODO check AOMRelationshipDef is valid (has type on src and dst, etc ...)
		tenantMetaModel.getRelationshipDefs().put(def.getType(), def);
		
		update(tenantId, tenantMetaModel, updatedBy);
		return def;
	}

	@Override
	public AOMRelationshipDef getAOMRelationshipDef(String tenantId, String relationshipType) {
		AOMTenantMetamodel tenantMetaModel = getAOMTenantMetamodel(tenantId);
		if (tenantMetaModel == null) {
			throw new ValidationException("Unable to retrieve AOM Metamodel for: " + tenantId);
		}
		return tenantMetaModel.getRelationshipDefs().get(relationshipType);
	}

	@Override
	public Map<String, AOMRelationshipDef> getAOMRelationshipDefs(String tenantId) {
		AOMTenantMetamodel tenantMetaModel = getAOMTenantMetamodel(tenantId);
		if (tenantMetaModel == null) {
			throw new ValidationException("Unable to retrieve AOM Metamodel for: " + tenantId);
		}
		return tenantMetaModel.getRelationshipDefs();
	}

	@Override
	public void deleteAOMRelationshipDef(String tenantId, String relationshipType, String updatedBy)
            throws IOException {
		// TODO check there are already instance in database. should we reject if that is the case ? 
		// certainly, and have a 'force' method in case we do want to delete 
		// TODO Auto-generated method stub
		AOMRelationshipDef def = getAOMRelationshipDef(tenantId, relationshipType);
		if (def != null){
			getAOMRelationshipDefs(tenantId).remove(relationshipType);
			update(tenantId, getAOMTenantMetamodel(tenantId), updatedBy);
		}	
	}

	@Override
	public AOMEntityDef addAOMEntityDef(String tenantId, AOMEntityDef def, String updatedBy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AOMEntityDef getAOMEntityDef(String tenantId, String aomEntityType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, AOMEntityDef> getAOMEntityDefs(String tenantId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAOMEntityDef(String tenantId, String aomEntityType,
			String updatedBy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AOMTypeDef addAOMTypeDef(String tenantId, String aomEntityType,
			AOMTypeDef def, String updatedBy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AOMTypeDef getAOMTypeDef(String tenantId, String aomEntityType,
			String aomType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, AOMTypeDef> getAOMTypeDefs(String tenantId,
			String aomEntityType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAOMTypeDef(String tenantId, String aomEntityType,
			String aomType, String updatedBy) {
		// TODO Auto-generated method stub
	}

	@Override
	public AOMPropertyDef addAOMPropertyDef(String tenantId,
			String aomEntityType, AOMPropertyDef def, String updatedBy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AOMPropertyDef getAOMTypeDef(String tenantId, String aomEntityType,
			String aomType, String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, AOMPropertyDef> getAOMTypeDefs(String tenantId,
			String aomEntityType, String aomType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAOMPropertyDef(String tenantId, String aomEntityType,
			String aomType, String propertyName, String updatedBy) {
		// TODO Auto-generated method stub
	}

	private void addMetaModel(String tenantId, AOMTenantMetamodel tenantMetaModel) {
		metamodels.put(tenantId, tenantMetaModel);		
	}
	

	private AOMTenantMetamodelEntity getAOMTenantMetamodelEntity(String tenantId){
		PrimaryKey primaryKey = new PrimaryKey(tenantId, AOMTenantMetamodelEntity.METAMODEL_ID);
		return entityManager.find(AOMTenantMetamodelEntity.class, primaryKey);
	}

	private void insert(AOMTenantMetamodelEntity metaModelEntity) {
        entityManager.persist(metaModelEntity);
    }

	private AOMTenantMetamodelEntity update(String tenantId, AOMTenantMetamodel tenantMetaModel, String updatedBy)
            throws IOException {
		AOMTenantMetamodelEntity metaModelEntity = getAOMTenantMetamodelEntity(tenantId);
		metaModelEntity.setAomTenantMetamodelObj(tenantMetaModel);
		metaModelEntity.setUpdatedBy(updatedBy);
        return update(metaModelEntity);
    }

	private AOMTenantMetamodelEntity update(AOMTenantMetamodelEntity metaModelEntity) {
		metaModelEntity.touch();
        return entityManager.merge(metaModelEntity);
    }

	private void delete(AOMTenantMetamodelEntity metaModelEntity) {
        entityManager.remove(metaModelEntity);
    }  

}
