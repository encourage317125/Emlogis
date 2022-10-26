package com.emlogis.common.services.employee;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.AbsenceType;
import com.emlogis.model.employee.AbsenceTypeDeleteResult;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.rest.security.SessionService;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class AbsenceTypeService {
	
	@PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    @EJB
	private SessionService sessionService;

	/**
	 * findAbsenceTypes() find a list of AbsenceTypes matching criteria;
	 * @param sq
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public ResultSet<AbsenceType> findAbsenceTypes(SimpleQuery sq) throws InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		sq.setEntityClass(AbsenceType.class);
		SimpleQueryHelper sqh = new SimpleQueryHelper();
		return sqh.executeSimpleQueryWithPaging(entityManager, sq);
	}	
	
	/**
	 * Get absenceType 
	 * @param primaryKey
	 * @return
	 */
	public AbsenceType getAbsenceType(PrimaryKey primaryKey) {
		return entityManager.find(AbsenceType.class, primaryKey);
	}

	/**
	 * Create absenceType
	 * @param pk
	 * @param name
	 * @param timeToDeductInMin
	 * @param description
	 * @param site
	 * @return absenceType
	 */
    public AbsenceType createAbsenceType(PrimaryKey pk, String name, int timeToDeductInMin, String description,
                                         boolean isActive, Site site) {
		AbsenceType absenceType = new AbsenceType(pk, name, description, timeToDeductInMin, isActive, site);
		entityManager.persist(absenceType);
		site.addAbsenceType(absenceType);
		return absenceType;
	}
        
    /**
     * Update absenceType
     * @param absenceType
     * @return
     */
    public AbsenceType update(AbsenceType absenceType) {
        absenceType.touch();
        return entityManager.merge(absenceType);
    }   
    
    /**
     * Delete absenceType
     * @param absenceType
     * @param site
     */
    public AbsenceTypeDeleteResult delete(AbsenceType absenceType, Site site) {
        Set<AbsenceType> absenceTypes = site.getAbsenceTypes();
        if (absenceTypes == null || !absenceTypes.contains(absenceType)) {
            throw new ValidationException(sessionService.getMessage("validation.absencetype.remove",
                    absenceType.getId(), site.getId()));
        }
        if (findEntitiesWithAbsenceType(absenceType, "AvailabilityTimeFrame").size() > 0) {
            //throw new ValidationException(sessionService.getMessage("entity.constraint.violation"));
            absenceType.setActive(false);
            entityManager.merge(absenceType);
            return AbsenceTypeDeleteResult.MADE_INACTIVE;
        } else {
            site.removeAbsenceType(absenceType);
            entityManager.remove(absenceType);
            return AbsenceTypeDeleteResult.DELETED;
        }
    }

    private Collection findEntitiesWithAbsenceType(AbsenceType absenceType, String name) {
        Query query = entityManager.createQuery("SELECT e FROM " + name + " e WHERE e.absenceType = :absenceType");
        query.setParameter("absenceType", absenceType);
        return query.getResultList();
    }

}

