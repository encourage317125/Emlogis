package com.emlogis.common.services.schedule;

import com.emlogis.common.Constants;
import com.emlogis.common.ModelUtils;
import com.emlogis.common.TimeUtil;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.notification.NotificationService;
import com.emlogis.common.services.schedule.changes.ScheduleChangeService;
import com.emlogis.common.services.util.AccountUtilService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.schedule.Schedule;
import com.emlogis.model.schedule.ScheduleStatus;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ShiftService {

    @EJB
    private ScheduleChangeService scheduleChangeService;
    
    @EJB
    private AccountUtilService accountUtilService;
    
    @EJB
    private EmployeeService employeeService;
    
    @EJB
    private NotificationService notificationService;

    @PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    private static class StartEndDateTime {
        long startDateTime;
        long endDateTime;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StartEndDateTime)) return false;

            StartEndDateTime that = (StartEndDateTime) o;

            if (endDateTime != that.endDateTime) return false;
            if (startDateTime != that.startDateTime) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (startDateTime ^ (startDateTime >>> 32));
            result = 31 * result + (int) (endDateTime ^ (endDateTime >>> 32));
            return result;
        }
    }

    public ResultSet<Shift> findShifts(SimpleQuery simpleQuery) {
        simpleQuery.setEntityClass(Shift.class);
        SimpleQueryHelper sqh = new SimpleQueryHelper();
        return sqh.executeSimpleQueryWithPaging(entityManager, simpleQuery);
    }

    public Collection<Shift> getScheduleOpenShifts(PrimaryKey schedulePrimaryKey, Long startDateTime,
                                                   Long endDateTime) {
        String sql =
                "SELECT * FROM Shift WHERE tenantId = :tenantId AND scheduleId = :scheduleId AND employeeId IS NULL";

        if (startDateTime != null && startDateTime > Constants.DATE_2000_01_01) {
            sql += " AND startDateTime >= :startDateTime ";
        }

        if (endDateTime != null && endDateTime > Constants.DATE_2000_01_01) {
            sql += " AND startDateTime < :endDateTime ";
        }

        Query query = entityManager.createNativeQuery(sql, Shift.class);
        query.setParameter("tenantId", schedulePrimaryKey.getTenantId());
        query.setParameter("scheduleId", schedulePrimaryKey.getId());
        if (startDateTime != null && startDateTime > Constants.DATE_2000_01_01) {
            query.setParameter("startDateTime", new Timestamp(startDateTime));
        }
        if (endDateTime != null && endDateTime > Constants.DATE_2000_01_01) {
            query.setParameter("endDateTime", new Timestamp(endDateTime));
        }
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public Collection<Shift> getShifts(String tenantId, Collection<String> ids) {
        if (ids == null || ids.size() == 0) {
            return new ArrayList<>();
        } else {
            String idsText = ModelUtils.commaSeparatedQuotedValues(ids);
            Query query = entityManager.createQuery(
                    "SELECT s FROM Shift s " +
                    " WHERE s.primaryKey.id IN (" + idsText + ") " +
                    (StringUtils.isEmpty(tenantId) ? "" : " AND s.primaryKey.tenantId = :tenantId ")
            );
            if (StringUtils.isNotEmpty(tenantId)) {
                query.setParameter("tenantId", tenantId);
            }
            return query.getResultList();
        }
    }

    //filter - semicolon-separated string
    //filter2 - complicated filter that use without alias
    public ResultSet<Object[]> getShiftsOps(String scheduleId, String tenantId, String filter, String filter2,
                                            String returnedFields, int offset, int limit, String orderBy,
                                            String orderDir) {
        String filterClause = StringUtils.isNotBlank(filter) ? " AND "
                + SimpleQueryHelper.buildFilterClause(filter, "s") : "";
        String returnedFieldsClause = SimpleQueryHelper.createReturnedFieldsClause(returnedFields, "s");
        String sql =
                "SELECT " + (returnedFieldsClause != null ? returnedFieldsClause : " s.* ") +
                " FROM Shift s " +
                " WHERE s.tenantId = :tenantId AND s.scheduleId = :scheduleId " + filterClause +
                (StringUtils.isNotBlank(filter2) ? " AND " + filter2 : "");

        String countSql = "SELECT COUNT(*) FROM (" + sql + ") x";
        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter("tenantId", tenantId);
        countQuery.setParameter("scheduleId", scheduleId);

        if (StringUtils.isNotBlank(orderBy)) {
            sql += " ORDER BY s." + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);
        query.setParameter("scheduleId", scheduleId);
        query.setFirstResult(offset);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        List<Object[]> resultData = query.getResultList();
        BigInteger total = (BigInteger) countQuery.getSingleResult();
        return new ResultSet<>(resultData, total.intValue());
    }

    public ResultSet<Object[]> getShifts(String employeeId, long startDate, long endDate, String timeZoneId,
                                         int scheduleStatus, String returnedFields, int offset, int limit,
                                         String orderBy, String orderDir, boolean exactTotalCount) {
        String returnedFieldsClause = SimpleQueryHelper.createReturnedFieldsClause(returnedFields, "s");
        String sql = "SELECT " + (returnedFieldsClause != null ? returnedFieldsClause : "s.* ") +
                "  FROM Shift s " +
                " WHERE s.employeeId = :employeeId " +
                "   AND (  (startDateTime >= :newStartDate AND startDateTime <= :newEndDate) " +
                "       OR (endDateTime >= :newStartDate AND endDateTime <= :newEndDate) " +
                "       OR (startDateTime < :newStartDate AND endDateTime > :newEndDate))" +
                "   AND s.scheduleStatus = :scheduleStatus ";

        String countSql = "SELECT COUNT(*) FROM (" + sql + ") x";

        if (StringUtils.isNotBlank(orderBy)) {
            sql += " ORDER BY s." + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("employeeId", employeeId);
        query.setParameter("scheduleStatus", scheduleStatus);
        query.setParameter("newStartDate", new Timestamp(startDate));
        query.setParameter("newEndDate", new Timestamp(endDate));

        query.setFirstResult(offset);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        List<Object[]> resultData = query.getResultList();

        if (exactTotalCount && limit > 0) {
            Query countQuery = entityManager.createNativeQuery(countSql);
            countQuery.setParameter("employeeId", employeeId);
            countQuery.setParameter("scheduleStatus", scheduleStatus);
            countQuery.setParameter("newStartDate", new Timestamp(startDate));
            countQuery.setParameter("newEndDate", new Timestamp(endDate));

            BigInteger total = (BigInteger) countQuery.getSingleResult();
        	return new ResultSet<>(resultData, total.intValue());
        } else {
        	return new ResultSet<>(resultData, resultData.size());        	
        }
    }

    public ResultSet<Object[]> getScheduleAndProdPostedShifts(String employeeId, String scheduleId, long startDate,
            long endDate, String returnedFields, int offset, int limit, String orderBy, String orderDir,
            boolean exactTotalCount) {
        String returnedFieldsClause = SimpleQueryHelper.createReturnedFieldsClause(returnedFields, "s");
        String sql = "SELECT " + (returnedFieldsClause != null ? returnedFieldsClause : "s.* ") +
                "  FROM Shift s " +
                " WHERE s.employeeId = :employeeId " +
                "   AND (  (startDateTime >= :newStartDate AND startDateTime <= :newEndDate) " +
                "       OR (endDateTime >= :newStartDate AND endDateTime <= :newEndDate) " +
                "       OR (startDateTime < :newStartDate AND endDateTime > :newEndDate))" +
                "   AND (s.scheduleId = :scheduleId OR s.scheduleStatus IN (" + ScheduleStatus.Production.ordinal() +
                        "," + ScheduleStatus.Posted.ordinal() + "))";

        String countSql = "SELECT COUNT(*) FROM (" + sql + ") x";

        if (StringUtils.isNotBlank(orderBy)) {
            sql += " ORDER BY s." + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("employeeId", employeeId);
        query.setParameter("scheduleId", scheduleId);
        query.setParameter("newStartDate", new Timestamp(startDate));
        query.setParameter("newEndDate", new Timestamp(endDate));

        query.setFirstResult(offset);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        List<Object[]> resultData = query.getResultList();

        if (exactTotalCount && limit > 0) {
            Query countQuery = entityManager.createNativeQuery(countSql);
            countQuery.setParameter("employeeId", employeeId);
            countQuery.setParameter("scheduleId", scheduleId);
            countQuery.setParameter("newStartDate", new Timestamp(startDate));
            countQuery.setParameter("newEndDate", new Timestamp(endDate));

            BigInteger total = (BigInteger) countQuery.getSingleResult();
        	return new ResultSet<>(resultData, total.intValue());
        } else {
        	return new ResultSet<>(resultData, resultData.size());
        }
    }

    public Shift getShift(PrimaryKey primaryKey) {
        return entityManager.find(Shift.class, primaryKey);
    }

    public void insert(Shift shift) {
        entityManager.persist(shift);
    }

    public Shift update(Shift shift) {
        return entityManager.merge(shift);
    }

    public void delete(Shift shift) {
        entityManager.remove(shift);
    }

    public Collection<Shift> getPreAssignedShifts(Schedule schedule) {
        String sql =
                "SELECT sh.* FROM Shift sh, Schedule sc " +
                " WHERE sh.tenantId = sc.tenantId AND sh.scheduleId = sc.id " +
                "   AND sh.tenantId = :tenantId AND sh.scheduleId = :scheduleId " +
                "   AND sh.employeeId IS NOT NULL AND sc.executionStartDate > sh.assigned ";

        Query query = entityManager.createNativeQuery(sql, Shift.class);
        query.setParameter("tenantId", schedule.getTenantId());
        query.setParameter("scheduleId", schedule.getId());

        return query.getResultList();
    }

    public Collection<Shift> getPostAssignedShifts(Schedule schedule) {
        String sql =
                "SELECT sh.* FROM Shift sh, Schedule sc " +
                " WHERE sh.tenantId = sc.tenantId AND sh.scheduleId = sc.id " +
                "   AND sh.tenantId = :tenantId AND sh.scheduleId = :scheduleId " +
                "   AND sh.employeeId IS NOT NULL AND sc.executionEndDate < sh.assigned ";

        Query query = entityManager.createNativeQuery(sql, Shift.class);
        query.setParameter("tenantId", schedule.getTenantId());
        query.setParameter("scheduleId", schedule.getId());

        return query.getResultList();
    }

    public Collection<Shift> getEngineAssignedShifts(Schedule schedule) {
        String sql =
                "SELECT sh.* FROM Shift sh, Schedule sc " +
                " WHERE sh.tenantId = sc.tenantId AND sh.scheduleId = sc.id " +
                "   AND sh.tenantId = :tenantId AND sh.scheduleId = :scheduleId AND sh.employeeId IS NOT NULL " +
                "   AND sc.executionEndDate >= sh.assigned AND sc.executionStartDate <= sh.assigned ";

        Query query = entityManager.createNativeQuery(sql, Shift.class);
        query.setParameter("tenantId", schedule.getTenantId());
        query.setParameter("scheduleId", schedule.getId());

        return query.getResultList();
    }

    public Collection<Shift> getScheduleShifts(Schedule schedule) {
        return getScheduleShifts(schedule, null);
    }

    public Collection<Shift> getScheduleShifts(Schedule schedule, Boolean notExcessOpenShifts) {
        String sql =
            "SELECT * FROM Shift WHERE scheduleId = :scheduleId AND tenantId = :tenantId " +
            (notExcessOpenShifts != null && notExcessOpenShifts ? " AND NOT (excess AND employeeId IS NULL) " : "");
        Query query = entityManager.createNativeQuery(sql, Shift.class);
        query.setParameter("scheduleId", schedule.getId());
        query.setParameter("tenantId", schedule.getTenantId());

        return query.getResultList();
    }

    public List<Shift> getPreassignedShiftsOtherTeams(Schedule schedule, List<Employee> employees) {
        String sql =
            "SELECT s.* FROM Shift s LEFT JOIN Schedule sch ON s.scheduleId = sch.id " +
            " WHERE s.tenantId = :tenantId " +
            "   AND sch.id != :scheduleId " +
            "   AND s.teamId NOT IN (SELECT ts.Team_id FROM Team_Schedule ts " +
            "                         WHERE sch.id = ts.schedules_id AND sch.tenantId = ts.schedules_tenantId) " +
            "   AND sch.status IN (" + ScheduleStatus.Production.ordinal() +","+ ScheduleStatus.Posted.ordinal() + ")" +
            "   AND s.employeeId IN (" + ModelUtils.commaSeparatedQuotedIds(employees) + ") " +
            "   AND (   s.startDateTime >= :startDate AND s.startDateTime <= :endDate " +
            "        OR s.endDateTime >= :startDate AND s.endDateTime <= :endDate) ";
        Query query = entityManager.createNativeQuery(sql, Shift.class);
        query.setParameter("scheduleId", schedule.getId());
        query.setParameter("tenantId", schedule.getTenantId());
        query.setParameter("startDate", new Timestamp(schedule.getStartDate()));
        query.setParameter("endDate", new Timestamp(schedule.getEndDate()));

        return query.getResultList();
    }

    public Collection<Shift> getScheduleShiftsBetweenDates(Schedule schedule, long start, long end,
                                                           Boolean notExcessOpenShifts) {
        String sql =
            "SELECT * FROM Shift WHERE scheduleId = :scheduleId AND tenantId = :tenantId " +
            "   AND (startDateTime >= :startDate AND startDateTime <= :endDate " +
            "        OR endDateTime >= :startDate AND endDateTime <= :endDate) " +
            (notExcessOpenShifts != null && notExcessOpenShifts ? " AND NOT (excess AND employeeId IS NULL) " : "");
        Query query = entityManager.createNativeQuery(sql, Shift.class);
        query.setParameter("scheduleId", schedule.getId());
        query.setParameter("tenantId", schedule.getTenantId());
        query.setParameter("startDate", new Timestamp(start));
        query.setParameter("endDate", new Timestamp(end));

        return query.getResultList();
    }

    public List<Shift> getPreassignedWeekendShifts(Collection<Employee> employees, long start, long end) {
        String sql =
            "SELECT * FROM Shift WHERE " +
            "  (   startDateTime >= :startDate AND startDateTime <= :endDate " +
            "   OR endDateTime >= :startDate AND endDateTime <= :endDate) " +
            "  AND employeeId IN (" +
            "    SELECT ec.employeeId FROM EmployeeContract ec LEFT JOIN BooleanCL b ON ec.id = b.contractId " +
            "     WHERE b.enabled " +
            "       AND b.contractLineType = 'COMPLETE_WEEKEND' " +
            "       AND ec.employeeId IN (" + ModelUtils.commaSeparatedQuotedIds(employees) + ") " +
            "  ) ";
        Query query = entityManager.createNativeQuery(sql, Shift.class);
        query.setParameter("startDate", new Timestamp(start));
        query.setParameter("endDate", new Timestamp(end));

        return query.getResultList();
    }

    public List<Shift> getIntMinMaxForDaysShifts(Collection<Employee> employees, long start, long end) {
        String sql =
            "SELECT * " +
            "  FROM Shift s, " +
            "    (SELECT " +
            "       ec.employeeId, " +
            "       CASE  " +
            "         WHEN   imm.contractLineType = 'TWO_WEEK_OVERTIME' " +
            "             OR imm.contractLineType = 'HOURS_PER_TWO_WEEKS' THEN 7 " +
            "         WHEN imm.minimumEnabled THEN imm.minimumValue " +
            "         WHEN imm.maximumEnabled THEN imm.maximumValue " +
            "       END days " +
            "       FROM EmployeeContract ec LEFT JOIN IntMinMaxCL imm ON ec.id = imm.contractId " +
            "      WHERE imm.contractLineType IN ('CONSECUTIVE_WORKING_DAYS', 'CONSECUTIVE_TWELVE_HOUR_DAYS', " +
            "                                     'TWO_WEEK_OVERTIME', 'HOURS_PER_TWO_WEEKS') " +
            "        AND ec.employeeId IN (" + ModelUtils.commaSeparatedQuotedIds(employees) + ")) x " +
            " WHERE s.employeeId = x.employeeId " +
            "   AND (       startDateTime >= ADDDATE(:startDate, INTERVAL -x.days DAY)  " +
            "           AND startDateTime < :startDate " +
            "        OR     startDateTime >= :endDate " +
            "           AND startDateTime < ADDDATE(:endDate, INTERVAL x.days + 1 DAY) " +
            "        OR     endDateTime >= :endDate " +
            "           AND endDateTime < ADDDATE(:endDate, INTERVAL x.days + 1 DAY) " +
            "        OR     endDateTime >= ADDDATE(:startDate, INTERVAL -x.days DAY)  " +
            "           AND endDateTime < :startDate " +
            "       )";
        Query query = entityManager.createNativeQuery(sql, Shift.class);
        query.setParameter("startDate", new Timestamp(start));
        query.setParameter("endDate", new Timestamp(end));

        return query.getResultList();
    }

    public List<Shift> getIntMinMaxForHoursShifts(Collection<Employee> employees, long start, long end) {
        String sql =
            "SELECT * " +
            "  FROM Shift s, " +
            "    (SELECT " +
            "       ec.employeeId, " +
            "       CASE  " +
            "         WHEN imm.minimumEnabled THEN imm.minimumValue " +
            "         WHEN imm.maximumEnabled THEN imm.maximumValue " +
            "       END hours " +
            "       FROM EmployeeContract ec LEFT JOIN IntMinMaxCL imm ON ec.id = imm.contractId " +
            "      WHERE imm.contractLineType = 'HOURS_BETWEEN_DAYS' " +
            "        AND ec.employeeId IN (" + ModelUtils.commaSeparatedQuotedIds(employees) + ")) x " +
            " WHERE s.employeeId = x.employeeId " +
            "   AND (       startDateTime >= ADDDATE(:startDate, INTERVAL -x.hours HOUR)  " +
            "           AND startDateTime < :startDate " +
            "        OR     startDateTime >= :endDate " +
            "           AND startDateTime < ADDDATE(:endDate, INTERVAL x.hours HOUR) " +
            "        OR     endDateTime >= :endDate " +
            "           AND endDateTime < ADDDATE(:endDate, INTERVAL x.hours HOUR) " +
            "        OR     endDateTime >= ADDDATE(:startDate, INTERVAL -x.hours HOUR)  " +
            "           AND endDateTime < :startDate " +
            "       )";
        Query query = entityManager.createNativeQuery(sql, Shift.class);
        query.setParameter("startDate", new Timestamp(start));
        query.setParameter("endDate", new Timestamp(end));

        return query.getResultList();
    }

    public List<Shift> getWeekdayRotationPatternShifts(Collection<Employee> employees, long start, long end,
                                                       DateTimeZone dateTimeZone) {
        List<Shift> result = new ArrayList<>();

        String sql =
            "SELECT wrp.dayOfWeek, wrp.numberOfDays, wrp.outOfTotalDays, ec.employeeId " +
            "  FROM EmployeeContract ec LEFT JOIN WeekdayRotationPatternCL wrp ON ec.id = wrp.contractId " +
            " WHERE wrp.contractLineType = 'CUSTOM'" +
            "   AND ec.employeeId IN (" + ModelUtils.commaSeparatedQuotedIds(employees) + ") ";
        Query query = entityManager.createNativeQuery(sql);

        Map<StartEndDateTime, List<String>> datesEmployeeIdsMap = new HashMap<>();

        List<Object[]> rows = query.getResultList();
        for (Object[] row : rows) {
            int dayOfWeek = (int) row[0] + 1; // in database MONDAY == 0, but we handle MONDAY == 1
            int numberOfDays = (int) row[1];
            int outOfTotalDays = (int) row[2];
            String employeeId = (String) row[3];

            for (int i = 0; i < outOfTotalDays - 1; i++) {
                StartEndDateTime keyBefore = new StartEndDateTime();
                long date = TimeUtil.datePlusDays(start, -i * 7);
                keyBefore.startDateTime = TimeUtil.getFirstDayOfWeekBefore(dayOfWeek, date, dateTimeZone);
                keyBefore.endDateTime = TimeUtil.datePlusDays(keyBefore.startDateTime, numberOfDays);
                List<String> valueBefore = datesEmployeeIdsMap.get(keyBefore);
                if (valueBefore == null) {
                    valueBefore = new ArrayList<>();
                    datesEmployeeIdsMap.put(keyBefore, valueBefore);
                }
                valueBefore.add(employeeId);

                StartEndDateTime keyAfter = new StartEndDateTime();
                date = TimeUtil.datePlusDays(end, i * 7);
                keyAfter.startDateTime = TimeUtil.getFirstDayOfWeekAfter(dayOfWeek, date, dateTimeZone);
                keyAfter.endDateTime = TimeUtil.datePlusDays(keyAfter.startDateTime, numberOfDays);
                List<String> valueAfter = datesEmployeeIdsMap.get(keyAfter);
                if (valueAfter == null) {
                    valueAfter = new ArrayList<>();
                    datesEmployeeIdsMap.put(keyAfter, valueAfter);
                }
                valueAfter.add(employeeId);
            }
        }

        for (Map.Entry<StartEndDateTime, List<String>> entry : datesEmployeeIdsMap.entrySet()) {
            long startTime = entry.getKey().startDateTime;
            long endTime = entry.getKey().endDateTime;
            List<Shift> shifts = employeesShiftsBetweenDates(entry.getValue(), startTime, endTime);
            result.addAll(shifts);
        }

        return result;
    }

    public Collection<Shift> getScheduleOpenShifts(Schedule schedule) {
        SimpleQuery simpleQuery = new SimpleQuery(schedule.getTenantId());
        simpleQuery.setEntityClass(Shift.class);
        simpleQuery.addFilter("scheduleId='" + schedule.getId() + "'");
        simpleQuery.addFilter("assigned=null");
        simpleQuery.addFilter("assignmentType=null");
        simpleQuery.addFilter("employeeId=null");
        simpleQuery.addFilter("employeeName=null");
        return new SimpleQueryHelper().executeSimpleQuery(entityManager, simpleQuery);
    }

    public Collection<Shift> getEmployeeScheduleShifts(Schedule schedule, Employee employee) {
        SimpleQuery simpleQuery = new SimpleQuery(schedule.getPrimaryKey().getTenantId());
        simpleQuery.setEntityClass(Shift.class);
        simpleQuery.addFilter("scheduleId='" + schedule.getPrimaryKey().getId() + "'");
        simpleQuery.addFilter("employeeId='" + employee.getPrimaryKey().getId() + "'");
        return new SimpleQueryHelper().executeSimpleQuery(entityManager, simpleQuery);
    }

    public void clearScheduleShifts(Schedule schedule, UserAccount managerAccount) {
        Collection<Shift> shifts = getScheduleShifts(schedule);
        for (Shift shift : shifts) {
            scheduleChangeService.trackShiftDeleteChange(shift, schedule, null, managerAccount, null);
        }
        Query query = entityManager.createNativeQuery("DELETE FROM Shift WHERE scheduleId = :scheduleId");
        query.setParameter("scheduleId", schedule.getId());
        query.executeUpdate();
    }

    public void clearScheduleShifts(Schedule schedule, Collection<Team> teams, UserAccount managerAccount) {
        SimpleQuery simpleQuery = new SimpleQuery(schedule.getTenantId());
        simpleQuery.setEntityClass(Shift.class);
        String teamIdsClause = "teamId IN (" + ModelUtils.commaSeparatedQuotedIds(teams) + ")";
        simpleQuery.addFilter(teamIdsClause);
        simpleQuery.addFilter("scheduleId='" + schedule.getPrimaryKey().getId() + "'");

        Collection<Shift> shifts = new SimpleQueryHelper().executeSimpleQuery(entityManager, simpleQuery);
        for (Shift shift : shifts) {
            scheduleChangeService.trackShiftDeleteChange(shift, schedule, null, managerAccount, null);
        }

        Query query = entityManager.createNativeQuery("DELETE FROM Shift WHERE scheduleId = :scheduleId AND "
                + teamIdsClause);
        query.setParameter("scheduleId", schedule.getId());
        query.executeUpdate();
    }


	public Collection<Shift> getScheduleAssignedShifts(Schedule schedule) {
	    SimpleQuery simpleQuery = new SimpleQuery(schedule.getTenantId());
	    simpleQuery.setEntityClass(Shift.class);
	    simpleQuery.addFilter("scheduleId='" + schedule.getId() + "'");
//	    simpleQuery.addFilter("assigned != null");					// needed only if want a 'strong' test on assignment
//	    simpleQuery.addFilter("assignmentType != null");			// needed only if want a 'strong' test on assignment
	    simpleQuery.addFilter("employeeId != null");	
//	    simpleQuery.addFilter("employeeName != null");				// needed only if want a 'strong' test on assignment
	    return new SimpleQueryHelper().executeSimpleQuery(entityManager, simpleQuery);
	}

    public Shift merge(Shift shift) {
        try {
            String queryStr = "" +
                    " SELECT shift.* FROM Shift shift " +
                    "  WHERE shift.id = '" + shift.getId() + "' " +
                    "    AND shift.scheduleId = '" + shift.getScheduleId() + "' " +
                    "    AND shift.teamId = '" + shift.getTeamId() + "' " +
                    "    AND shift.teamName = '" + shift.getTeamName() + "' " +
                    "    AND shift.shiftStructureId = '" + shift.getShiftStructureId() + "' " +
                    "    AND shift.shiftPatternId = '" + shift.getShiftPatternId() + "' " +
                    "    AND shift.skillId = '" + shift.getSkillId() + "' " +
                    "    AND shift.skillAbbrev = '" + shift.getSkillAbbrev() + "' " +
                    "    AND shift.skillName = '" + shift.getSkillName() + "' " +
                    "    AND shift.siteName = '" + shift.getSiteName() + "' " +
                    "    AND shift.employeeId = '" + shift.getEmployeeId() + "' " +
                    "    AND shift.startDateTime = '" + new Timestamp(shift.getStartDateTime()) + "' " +
                    "    AND shift.endDateTime = '" + new Timestamp(shift.getEndDateTime()) + "' ";
            return (Shift) entityManager.createNativeQuery(queryStr, Shift.class).getSingleResult();
        } catch (NoResultException nre) {
            shift.setPrimaryKey(new PrimaryKey(shift.getTenantId()));
            entityManager.persist(shift);
            return shift;
        }
    }

    private List<Shift> employeesShiftsBetweenDates(Collection<String> employeeIds, long start, long finish) {
        String sql =
            "SELECT * FROM Shift WHERE (   startDateTime >= :start AND startDateTime < :finish " +
            "                           OR endDateTime > :start AND endDateTime <= :finish)" +
            "                      AND employeeId IN (" + ModelUtils.commaSeparatedQuotedValues(employeeIds) + ") ";

        Query query = entityManager.createNativeQuery(sql, Shift.class);
        query.setParameter("start", new Timestamp(start));
        query.setParameter("finish", new Timestamp(finish));

        return query.getResultList();
    }
}
