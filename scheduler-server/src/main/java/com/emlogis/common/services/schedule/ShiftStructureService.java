package com.emlogis.common.services.schedule;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.schedule.Schedule;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.schedule.ShiftReqOld;
import com.emlogis.model.schedule.ShiftStructure;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.rest.security.SessionService;
import org.joda.time.DateTime;

import javax.ejb.*;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ShiftStructureService {

    @Inject
    private SessionService sessionService;

    @PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    public ResultSet<ShiftStructure> findShiftStructures(SimpleQuery simpleQuery) throws InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        simpleQuery.setEntityClass(ShiftStructure.class);
        SimpleQueryHelper sqh = new SimpleQueryHelper();
        return sqh.executeSimpleQueryWithPaging(entityManager, simpleQuery);
    }

    public ShiftStructure getShiftStructure(PrimaryKey primaryKey) {
        return entityManager.find(ShiftStructure.class, primaryKey);
    }

    public ShiftStructure create(PrimaryKey primaryKey) {
        ShiftStructure result = new ShiftStructure(primaryKey);
        entityManager.persist(result);
        return result;
    }

    public ShiftStructure update(ShiftStructure shiftStructure) {
        return entityManager.merge(shiftStructure);
    }

    public void delete(ShiftStructure shiftStructure) {
        if (findShiftsWithShiftStructure(shiftStructure).size() > 0) {
            throw new ValidationException(sessionService.getMessage("entity.constraint.violation"));
        } else {
            Team team = shiftStructure.getTeam();
            team.removeShiftStructure(shiftStructure);

            Set<ShiftReqOld> shiftReqs = shiftStructure.getShiftReqs();
            for (ShiftReqOld shiftReq : shiftReqs) {
                entityManager.remove(shiftReq);
            }

            entityManager.remove(shiftStructure);
        }
    }

    public ResultSet<ShiftReqOld> findShiftReqs(SimpleQuery simpleQuery) throws InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        simpleQuery.setEntityClass(ShiftReqOld.class);
        SimpleQueryHelper sqh = new SimpleQueryHelper();
        return sqh.executeSimpleQueryWithPaging(entityManager, simpleQuery);
    }

    public ShiftReqOld getShiftReq(PrimaryKey primaryKey) {
        return entityManager.find(ShiftReqOld.class, primaryKey);
    }

    public void persistShiftReq(ShiftReqOld shiftReq) {
        entityManager.persist(shiftReq);
    }

    public void updateShiftReq(ShiftReqOld shiftReq) {
        entityManager.merge(shiftReq);
    }

    public void deleteShiftReq(PrimaryKey structurePrimaryKey, PrimaryKey reqPrimaryKey) {
        ShiftStructure shiftStructure = getShiftStructure(structurePrimaryKey);
        ShiftReqOld shiftReq = getShiftReq(reqPrimaryKey);

        Set<ShiftReqOld> shiftReqs = shiftStructure.getShiftReqs();

        if (shiftReqs.contains(shiftReq)) {
            shiftReqs.remove(shiftReq);
            update(shiftStructure);

            entityManager.remove(shiftReq);
        }
    }

    public ResultSet<Schedule> getSchedules(PrimaryKey employeePrimaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetAssociatedWithPaging(entityManager, simpleQuery, employeePrimaryKey,
                ShiftStructure.class, "schedules");
    }

    public ShiftStructure duplicate(PrimaryKey structurePrimaryKey, long startDate) {
        String tenantId = structurePrimaryKey.getTenantId();

        ShiftStructure oldShiftStructure = getShiftStructure(structurePrimaryKey);

        ShiftStructure newShiftStructure = new ShiftStructure();
        newShiftStructure.setPrimaryKey(new PrimaryKey(tenantId));
        newShiftStructure.setStartDate(startDate);
        newShiftStructure.setTeam(oldShiftStructure.getTeam());

        newShiftStructure.touch();

        entityManager.persist(newShiftStructure);

        Set<ShiftReqOld> oldShiftReqs = oldShiftStructure.getShiftReqs();
        Set<ShiftReqOld> newShiftReqs = new HashSet<>();
        if (oldShiftReqs != null) {
            for (ShiftReqOld shiftReq : oldShiftReqs) {
                try {
                    ShiftReqOld newShiftReq = shiftReq.clone();
                    newShiftReq.setPrimaryKey(new PrimaryKey(tenantId));
                    newShiftReq.setShiftStructureId(newShiftStructure.getId());

                    persistShiftReq(newShiftReq);

                    newShiftReqs.add(newShiftReq);
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(sessionService.getMessage("shiftstructure.shiftreq.duplicate.error",
                            shiftReq.getId()), e);
                }
            }
        }
        newShiftStructure.setShiftReqs(newShiftReqs);

        return update(newShiftStructure);
    }

    public ShiftStructure duplicateShiftReqs(PrimaryKey structurePrimaryKey, int dayIndexFrom, int dayIndexTo) {
        String tenantId = structurePrimaryKey.getTenantId();

        ShiftStructure shiftStructure = getShiftStructure(structurePrimaryKey);

        Set<ShiftReqOld> shiftReqFromSet = getShiftReqsOfDay(shiftStructure, dayIndexFrom);
        for (ShiftReqOld shiftReqFrom : shiftReqFromSet) {
            try {
                ShiftReqOld newShiftReq = shiftReqFrom.clone();
                newShiftReq.setPrimaryKey(new PrimaryKey(tenantId));
                newShiftReq.setDayIndex(dayIndexTo);

                persistShiftReq(newShiftReq);

                shiftStructure.getShiftReqs().add(newShiftReq);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(sessionService.getMessage("shiftstructure.shiftreq.duplicate.error",
                        shiftReqFrom.getId()), e);
            }
        }

        return update(shiftStructure);
    }

    @SuppressWarnings("unchecked")
    public List<ShiftStructure> getTeamShiftStructuresByDate(Team team, long date) {
        String queryString = "SELECT s FROM ShiftStructure s WHERE s.primaryKey.tenantId = :tenantId " +
                "AND s.startDate = :date AND s.team = :team";
        Query query = entityManager.createQuery(queryString);
        query.setParameter("tenantId", team.getTenantId());
        query.setParameter("team", team);
        query.setParameter("date", new DateTime(date));

        return query.getResultList();
    }

    public Set<ShiftReqOld> getShiftReqsOfDay(ShiftStructure shiftStructure, int dayIndex) {
        Set<ShiftReqOld> result = new HashSet<>();
        Set<ShiftReqOld> shiftReqs = shiftStructure.getShiftReqs();
        for (ShiftReqOld shiftReq : shiftReqs) {
            if (shiftReq.getDayIndex() == dayIndex) {
                result.add(shiftReq);
            }
        }
        return result;
    }

    private Collection<Shift> findShiftsWithShiftStructure(ShiftStructure shiftStructure) {
        SimpleQuery shiftSimpleQuery = new SimpleQuery(shiftStructure.getTenantId());
        shiftSimpleQuery.setEntityClass(Shift.class);
        shiftSimpleQuery.addFilter("shiftStructureId = '" + shiftStructure.getId() + "'");

        return new SimpleQueryHelper().executeSimpleQuery(entityManager, shiftSimpleQuery);
    }
}
