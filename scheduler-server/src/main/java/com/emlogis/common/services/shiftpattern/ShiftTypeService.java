package com.emlogis.common.services.shiftpattern;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.shiftpattern.ShiftType;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.rest.security.SessionService;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ShiftTypeService {

    @PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    @EJB
    private SessionService sessionService;

    public ResultSet<ShiftType> findShiftTypes(SimpleQuery simpleQuery) {
        simpleQuery.setEntityClass(ShiftType.class);
        return new SimpleQueryHelper().executeSimpleQueryWithPaging(entityManager, simpleQuery);
    }

    public ShiftType getShiftType(PrimaryKey primaryKey) {
        return entityManager.find(ShiftType.class, primaryKey);
    }

    public ShiftType update(ShiftType shiftType) {
        return entityManager.merge(shiftType);
    }

    public ShiftType create(PrimaryKey primaryKey) {
        ShiftType result = new ShiftType(primaryKey);
        entityManager.persist(result);
        return result;
    }

    public void delete(ShiftType shiftType) {
        String sql =
                "SELECT s.id \n" +
                "  FROM ShiftReq s \n" +
                " WHERE s.shiftTypeTenantId = :tenantId \n" +
                "   AND s.shiftTypeId = :shiftTypeId \n";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tenantId", shiftType.getTenantId());
        query.setParameter("shiftTypeId", shiftType.getId());
        query.setMaxResults(10);

        List<Object[]> resultList = query.getResultList();
        if (resultList.size() > 0) {
            String referencedEntities = "";
            for (Object id : resultList) {
                referencedEntities += "[id=" + id + "] ";
            }
            throw new ValidationException(sessionService.getMessage("referenced.entity.delete.error", "ShiftReq",
                    referencedEntities));
        } else {
            entityManager.remove(shiftType);
        }
    }
}
