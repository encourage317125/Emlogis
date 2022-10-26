package com.emlogis.common.services.shiftpattern;

import com.emlogis.common.Constants;
import com.emlogis.common.demand.DemandComputeDirector;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.shiftpattern.ShiftDemand;
import com.emlogis.model.shiftpattern.ShiftLength;
import com.emlogis.model.shiftpattern.ShiftPattern;
import com.emlogis.model.shiftpattern.ShiftReq;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.rest.security.SessionService;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.*;
import javax.persistence.Query;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ShiftPatternService {

    @PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    @EJB
    private SessionService sessionService;

    @EJB
    private ShiftLengthService shiftLengthService;

    public ResultSet<ShiftPattern> findShiftPatterns(SimpleQuery simpleQuery) {
        simpleQuery.setEntityClass(ShiftPattern.class);
        return new SimpleQueryHelper().executeSimpleQueryWithPaging(entityManager, simpleQuery);
    }

    public ShiftPattern getShiftPattern(PrimaryKey primaryKey) {
        return entityManager.find(ShiftPattern.class, primaryKey);
    }

    public ShiftPattern update(ShiftPattern shiftPattern) {
        return entityManager.merge(shiftPattern);
    }

    public ShiftPattern create(PrimaryKey primaryKey) {
        ShiftPattern result = new ShiftPattern(primaryKey);
        entityManager.persist(result);
        return result;
    }

    public void delete(ShiftPattern shiftPattern) {
        String sql = "SELECT DISTINCT s.id, s.name \n" +
                     "  FROM PatternElt p, Schedule s \n" +
                     " WHERE s.tenantId = p.scheduleTenantId \n" +
                     "   AND p.tenantId = :tenantId \n" +
                     "   AND s.id = p.scheduleId \n" +
                     "   AND p.tenantId = p.shiftPatternId \n" +
                     "   AND p.shiftPatternId = :patternId \n";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tenantId", shiftPattern.getTenantId());
        query.setParameter("patternId", shiftPattern.getId());
        query.setMaxResults(10);

        List<Object[]> resultList = query.getResultList();
        if (resultList.size() > 0) {
            String referencedEntities = "";
            for (Object[] array : resultList) {
                referencedEntities += "[id=" + array[0] + ", name=" + array[1] + "] ";
            }
            throw new ValidationException(sessionService.getMessage("referenced.entity.delete.error", "Schedule",
                    referencedEntities));
        } else {
            entityManager.remove(shiftPattern);
        }
    }

    public void createShiftReq(ShiftReq shiftReq) {
        entityManager.persist(shiftReq);
    }

    public void deleteShiftReq(ShiftReq shiftReq) {
        entityManager.remove(shiftReq);
    }

    public ShiftReq updateShiftReq(ShiftReq shiftReq) {
        return entityManager.merge(shiftReq);
    }

    public ShiftReq getShiftReq(PrimaryKey shiftReqPrimaryKey) {
        return entityManager.find(ShiftReq.class, shiftReqPrimaryKey);
    }

    public void createShiftDemand(ShiftDemand shiftDemand) {
        entityManager.persist(shiftDemand);
    }

    public void deleteShiftDemand(ShiftDemand shiftDemand) {
        entityManager.remove(shiftDemand);
    }

    public ShiftDemand updateShiftDemand(ShiftDemand shiftDemand) {
        return entityManager.merge(shiftDemand);
    }

    public ShiftDemand getShiftDemand(PrimaryKey shiftDemandPrimaryKey) {
        return entityManager.find(ShiftDemand.class, shiftDemandPrimaryKey);
    }

    public Set<ShiftReq> generateDemandShiftReqs(PrimaryKey primaryKey) {
        ShiftPattern shiftPattern = getShiftPattern(primaryKey);
        Set<ShiftReq> shiftReqs = shiftPattern.getShiftReqs();
        if (shiftReqs != null) {
            for (ShiftReq shiftReq : shiftReqs) {
                deleteShiftReq(shiftReq);
            }
            shiftReqs.clear();
        } else {
            shiftReqs = new HashSet<>();
        }

        Collection<ShiftLength> allowedShiftLengths = getAllowedShiftLengths(shiftPattern);
        if (allowedShiftLengths == null || allowedShiftLengths.isEmpty()) {
            throw new ValidationException(sessionService.getMessage("validation.shiftpattern.lengths.error"));
        }

        DemandComputeDirector director = new DemandComputeDirector();
        Set<ShiftReq> newShiftReqs = director.computeDemand(shiftPattern, allowedShiftLengths);

        shiftReqs.addAll(newShiftReqs);
        if (shiftReqs.size() > 0) {
            for (ShiftReq shiftReq : shiftReqs) {
                createShiftReq(shiftReq);
            }
            shiftPattern.setShiftReqs(shiftReqs);
        }

        update(shiftPattern);

        return newShiftReqs;
    }

    public Set<ShiftReq> generateDraftDemandShiftReqs(Collection<ShiftLength> allowedShiftLengthList,
                                                      Collection<ShiftDemand> shiftDemands) {
        DemandComputeDirector director = new DemandComputeDirector();
        return director.computeDemand(shiftDemands, allowedShiftLengthList);
    }

    private Collection<ShiftLength> getAllowedShiftLengths(ShiftPattern shiftPattern) {
        Collection<ShiftLength> result = new ArrayList<>();

        String lengthListStr = shiftPattern.getShiftLengthList();
        if (StringUtils.isNotEmpty(lengthListStr)) {
            String[] lengthIds = lengthListStr.split(",");
            for (String lengthId : lengthIds) {
                PrimaryKey primaryKey = new PrimaryKey(shiftPattern.getTenantId(), lengthId);
                ShiftLength shiftLength = shiftLengthService.getShiftLength(primaryKey);

                result.add(shiftLength);
            }
        }
        return result;
    }

}
