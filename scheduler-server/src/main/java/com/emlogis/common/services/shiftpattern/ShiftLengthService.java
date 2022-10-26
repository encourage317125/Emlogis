package com.emlogis.common.services.shiftpattern;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.shiftpattern.ShiftLength;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.rest.security.SessionService;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ShiftLengthService {

    @PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    @EJB
    private SessionService sessionService;

    public ResultSet<ShiftLength> findShiftLengths(SimpleQuery simpleQuery) {
        simpleQuery.setEntityClass(ShiftLength.class);
        return new SimpleQueryHelper().executeSimpleQueryWithPaging(entityManager, simpleQuery);
    }

    public ShiftLength getShiftLength(PrimaryKey primaryKey) {
        return entityManager.find(ShiftLength.class, primaryKey);
    }

    public ShiftLength update(ShiftLength shiftLength) {
        return entityManager.merge(shiftLength);
    }

    public ShiftLength create(PrimaryKey primaryKey) {
        ShiftLength result = new ShiftLength(primaryKey);
        entityManager.persist(result);
        return result;
    }

    public void persist(ShiftLength shiftLength) {
        entityManager.persist(shiftLength);
    }

    public void delete(ShiftLength shiftLength) {
        String sql =
                "SELECT s.id, s.name \n" +
                "  FROM ShiftType s \n" +
                " WHERE s.shiftLengthTenantId = :tenantId \n" +
                "   AND s.shiftLengthId = :shiftLengthId \n";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tenantId", shiftLength.getTenantId());
        query.setParameter("shiftLengthId", shiftLength.getId());
        query.setMaxResults(10);

        List<Object[]> resultList = query.getResultList();
        if (resultList.size() > 0) {
            String referencedEntities = "";
            for (Object[] array : resultList) {
                referencedEntities += "[id=" + array[0] + ", name=" + array[1] + "] ";
            }
            throw new ValidationException(sessionService.getMessage("referenced.entity.delete.error", "ShiftType",
                    referencedEntities));
        } else {
            entityManager.remove(shiftLength);
        }
    }

    public ResultSet<ShiftLength> query(String tenantId, boolean hasShiftType, String siteId, Integer shiftLengthInMin,
                                        int offset, int limit, String orderBy, String orderDir) {
        String sql =
                "SELECT sl.* FROM ShiftLength sl LEFT JOIN ShiftType st " +
                "    ON sl.id = st.shiftLengthId AND sl.tenantId = st.shiftLengthTenantId " +
                " WHERE sl.tenantId = :tenantId " +
                (shiftLengthInMin != null ? " AND sl.lengthInMin = :shiftLengthInMin " : "") +
                (hasShiftType ? " GROUP BY sl.id HAVING count(st.id) > 0 " : "");

        String countSql = "SELECT COUNT(*) FROM (" + sql + ") x";
        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter("tenantId", tenantId);
        if (shiftLengthInMin != null) {
            countQuery.setParameter("shiftLengthInMin", shiftLengthInMin);
        }

        if (StringUtils.isNotBlank(orderBy)) {
            sql += " ORDER BY sl." + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        Query query = entityManager.createNativeQuery(sql, ShiftLength.class);
        query.setParameter("tenantId", tenantId);
        if (shiftLengthInMin != null) {
            query.setParameter("shiftLengthInMin", shiftLengthInMin);
        }
        query.setFirstResult(offset);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        Collection<ShiftLength> shiftLengths = query.getResultList();
        BigInteger total = (BigInteger) countQuery.getSingleResult();

        return new ResultSet<>(shiftLengths, total.intValue());
    }

}
