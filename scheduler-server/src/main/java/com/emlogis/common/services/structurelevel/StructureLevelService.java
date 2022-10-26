package com.emlogis.common.services.structurelevel;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.services.tenant.AOMEntityService;
import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.aom.AOMRelationshipDef;
import com.emlogis.model.structurelevel.AOMRelationship;
import com.emlogis.model.structurelevel.StructureLevel;
import com.emlogis.server.services.AOMService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class StructureLevelService<T extends StructureLevel> extends AOMEntityService {
	
	private final Logger logger = LoggerFactory.getLogger(StructureLevelService.class);
	
	@PersistenceContext(unitName=Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    @EJB
    private AOMEntityService aomEntityService;		// AOM Attribute management service

    @EJB
    private AOMService aomService;				// metamodel management service

    /*
    public EntityManager getEntityManager() {
        return entityManager;
    }
    */

	public StructureLevel getStructureLevel(PrimaryKey primaryKey) {
		return getEntityManager().find(StructureLevel.class, primaryKey);
	}

    public T update(T structureLevel) {
        structureLevel.touch();
        return entityManager.merge(structureLevel);
    }


    public void delete(T structureLevel) {
    	// TODO .... disconnect from all related items and delete AOM relationships
    	// clean up ...
    	//structureLevel.removeAllRoles();
        super.delete(structureLevel);
    }


    //-----------------------------------------------------------------------------------------------------------------------
    // AOM Relationship management
    
    // WORK IN PRGRESS ....
    
   
    /**
     * return the list of objects associated via a specific relationship type to a 'seed' structurelevel instance.
     * dir allows to get related objects that are either source (pointing to the seed), either destination of the relationship
     * (both can be easily be implemented too, but waiting for a use case)
     * @param seed
     * 
     * @param relationshipType
     * @param dir
     * @return
     */    
    // TODO    
    // getAssociatedObjects( seed, dir, by related class) let's wait for a real use case as AOM use is very limited for now.

    public List<StructureLevel> getAssociatedObjects(StructureLevel seed, String relationshipType,
                                                     TraversalDirection dir) {
    	List<T> result;
		Query query;
		String queryString;
		if (dir == TraversalDirection.OUT){
			queryString = "SELECT elt FROM StructureLevel elt, AOMRelationship r WHERE elt = r.dst " +
					      " AND (r.type = :type) AND (r.src = :seed)";
		} else {
			queryString = "SELECT elt FROM StructureLevel elt, AOMRelationship r WHERE elt = r.src " +
					      " AND (r.type = :type) AND (r.dst = :seed)";
		}
		query = getEntityManager().createQuery(queryString);
		query.setParameter("type", relationshipType);
		query.setParameter("seed", seed);
		try {
			result = query.getResultList();
			logger.debug("element count: " + result.size());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}	
		return (List<StructureLevel>) result;
	}

    public AOMRelationship addAOMRelationship(StructureLevel src, StructureLevel dst, String relationshipType) {
    	AOMRelationshipDef rdef = aomService.getAOMRelationshipDef(src.getTenantId(), relationshipType);
    	if (rdef == null 
    			|| !StringUtils.equals(src.getAomEntityType(), rdef.getSrcEntityType()) 
    			|| !StringUtils.equals(dst.getAomEntityType(), rdef.getDstEntityType())) {
    		Map<String, Object> paramMap = new HashMap<>();
    		paramMap.put("RelationshipType", relationshipType);
    		paramMap.put("SrcClassName", src.getClass().getSimpleName());
    		paramMap.put("SrcAOMEntityType", src.getAomEntityType());
    		paramMap.put("SrcMetaModel AOMEntityType", (rdef != null ? rdef.getSrcEntityType() : "unknown"));
    		paramMap.put("DstClassName", dst.getClass().getSimpleName());
    		paramMap.put("DstAOMEntityType", dst.getAomEntityType());
    		paramMap.put("DstMetaModel AOMEntityType", (rdef != null ? rdef.getDstEntityType() : "unknown"));
    		throw new ValidationException("Can't create AOM relationship as not matching MetaModel", paramMap);
    	}
    	// TODO add more relationship checks to enforce data integrity (ex: cardinality, etc ...)
    	// ex can a team belong to more than one site ?
    	
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("relationshipType", relationshipType);
		paramMap.put("source", src.getName());
		paramMap.put("sourceType", src.getAomEntityType());
		paramMap.put("sourceId", src.getId());
		paramMap.put("destination", dst.getName());
		paramMap.put("destinationType", dst.getAomEntityType());
		paramMap.put("destinationId", dst.getId()); 		
    	if (getAssociation(src, dst, relationshipType) != null) {
    		throw new ValidationException("Cannot associate 2 entities because this association is already existing",
                    paramMap);
    	}
    	if (!StringUtils.equals(src.getTenantId(), dst.getTenantId())) {
    		throw new ValidationException("Cannot associate 2 entities because they don't belong to same Organization",
                    paramMap);
    	}
    	
		try {
	    	AOMRelationship relationship = new AOMRelationship(src.getTenantId(), src, dst, relationshipType);
			getEntityManager().persist(relationship);
            relationship.setSrc(src);
			src.addDstRel(relationship);

            relationship.setDst(dst);
			dst.addSrcRel(relationship);
			getEntityManager().merge(relationship);
			return relationship;
		} catch( Exception e){
			e.printStackTrace();
			throw e; 
		}	
    }
    
    public void removeAOMRelationship(StructureLevel src, StructureLevel dst, String relationshipType) {
		try {
		    // find relationships between the 2 entities and delete it
			AOMRelationship r = getAssociation( src, dst, relationshipType);
			if (r != null) {
				removeAOMRelationship(r);
			}
		} catch( Exception e){
			e.printStackTrace();
			throw e;
		}	
    }

    public void removeAOMRelationship(AOMRelationship r) {
		try {
			StructureLevel src = r.getSrc();
			src.removeDstRel(r);

			StructureLevel dst = r.getDst();
			dst.removeSrcRel(r);

			getEntityManager().remove(r);
		} catch( Exception e){
			e.printStackTrace();
			throw e;
		}	
    }

	protected <E extends BaseEntity> void deleteEntities(Collection<E> entities) {
		if (entities != null) {
			for (E entity : entities) {
				deleteEntity(entity);
			}
		}
	}

	protected <E extends BaseEntity> void deleteEntity(E entity) {
		getEntityManager().remove(entity);
	}

    private AOMRelationship getAssociation(StructureLevel src, StructureLevel dst, String relationshipType) {
		Query query;
		String queryString;
		String tenantId = src.getTenantId();
		queryString = "SELECT r FROM AOMRelationship r WHERE r.primaryKey.tenantId = :tenantId AND r.type = :type " +
				      " AND (r.src = :src) AND (r.dst = :dst)";
		query = getEntityManager().createQuery(queryString);
		query.setParameter("tenantId", tenantId);
		query.setParameter("type", relationshipType);
		query.setParameter("src", src);
		query.setParameter("dst", dst);
		try {
			List<Object> list = query.getResultList();
			switch (list.size()) {
                case 0:
                    return null;
                case 1:
                    return (AOMRelationship) list.get(0);
                default:
                    // we are in trouble as only one relationship was expected
                    throw new RuntimeException(
                        "DataModel integrity exception, found several instances of relationships type: "
                                + relationshipType + " between" + src.toString() + " and " + dst.toString()
                    );
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}	    
	}
    
}
