package com.emlogis.common.services.schedule;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.emlogis.common.*;
import com.emlogis.common.exceptions.ShiftMgmtException;
import com.emlogis.common.exceptions.ShiftMgmtException.ShiftMgmtErrorCode;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.notifications.NotificationCategory;
import com.emlogis.common.notifications.NotificationOperation;
import com.emlogis.common.services.notification.NotificationService;
import com.emlogis.common.services.schedule.changes.ScheduleChangeService;
import com.emlogis.common.services.structurelevel.SiteService;
import com.emlogis.common.services.tenant.OrganizationService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.services.util.AccountUtilService;
import com.emlogis.engine.domain.WeekendDefinition;
import com.emlogis.model.dto.ScheduleQueryByDayParamDto;
import com.emlogis.model.schedule.*;
import com.emlogis.model.schedule.changes.ShiftDropChange;
import com.emlogis.model.schedule.dto.*;
import com.emlogis.model.shiftpattern.ShiftPattern;
import com.emlogis.model.tenant.Organization;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.tenant.settings.SchedulingSettings;
import com.emlogis.model.tenant.settings.scheduling.OptimizationSetting;
import com.emlogis.model.tenant.settings.scheduling.OptimizationSettingList;
import com.emlogis.model.tenant.settings.scheduling.OptimizationSettingName;
import com.emlogis.rest.resources.util.QueryPattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.joda.time.*;

import reactor.event.Event;

import com.emlogis.common.security.AccountACL;
import com.emlogis.common.services.BaseService;
import com.emlogis.common.services.employee.CDAvailabilityTimeFrameService;
import com.emlogis.common.services.employee.CIAvailabilityTimeFrameService;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.employee.SkillService;
import com.emlogis.common.services.hazelcast.HazelcastClientService;
import com.emlogis.common.services.schedule.ResponseHandlerService.ResultMonitor;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.ShiftDate;
import com.emlogis.engine.domain.communication.*;
import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.engine.domain.contract.contractline.ContractScope;
import com.emlogis.engine.domain.contract.contractline.dto.BooleanCLDto;
import com.emlogis.engine.domain.contract.contractline.dto.IntMinMaxCLDto;
import com.emlogis.engine.domain.contract.contractline.dto.WeekdayRotationPatternCLDto;
import com.emlogis.engine.domain.contract.contractline.dto.WeekendWorkPatternCLDto;
import com.emlogis.engine.domain.contract.dto.ConstraintOverrideDto;
import com.emlogis.engine.domain.contract.dto.ContractDto;
import com.emlogis.engine.domain.contract.patterns.WeekdayRotationPattern;
import com.emlogis.engine.domain.dto.*;
import com.emlogis.engine.domain.dto.ShiftDto;
import com.emlogis.engine.domain.organization.TeamAssociationType;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.domain.timeoff.CDOverrideAvailDate;
import com.emlogis.engine.domain.timeoff.PreferenceType;
import com.emlogis.engine.domain.timeoff.dto.*;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.contract.ContractLine;
import com.emlogis.model.contract.IntMinMaxCL;
import com.emlogis.model.employee.*;
import com.emlogis.model.employee.AvailabilityTimeFrame.AvailabilityType;
import com.emlogis.model.notification.dto.NotificationMessageDTO;
import com.emlogis.model.schedule.Schedule;
import com.emlogis.model.schedule.changes.ChangeCategory;
import com.emlogis.model.schedule.changes.ChangeType;
import com.emlogis.model.schedule.changes.ScheduleChange;
import com.emlogis.model.schedule.dto.CandidateShiftEligibleEmployeesDto.EmployeeDescriptorDto;
import com.emlogis.model.schedule.dto.OpenShiftEligibilitySimpleResultDto.EligibleEmployeeDto;
import com.emlogis.model.schedule.dto.OpenShiftEligibilitySimpleResultDto.OpenShiftDto;
import com.emlogis.model.schedule.dto.QualificationExecuteDto.QualificationAssignment;
import com.emlogis.model.shiftpattern.PatternElt;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.tenant.Tenant;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.rest.security.SessionService;
import com.emlogis.scheduler.engine.communication.EngineStatus;
import com.emlogis.scheduler.engine.communication.request.EngineRequest;
import com.emlogis.scheduler.engine.communication.request.RequestType;
import com.emlogis.server.services.eventservice.ASEventService;
import com.emlogis.shared.services.eventservice.EventKeyBuilder;
import com.emlogis.shared.services.eventservice.EventScope;
import com.emlogis.shared.services.eventservice.EventService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ScheduleService extends BaseService {

    private final static Logger logger = Logger.getLogger(ScheduleService.class);
    
    @PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;
    
    @Resource
    private SessionContext sessionContext;

    @EJB 
    private ScheduleService scheduleServiceEJB;  // for TransactionAttribute-aware self-invocation
    
    @EJB
    private SessionService sessionService;

    @EJB
    private ShiftService shiftService;

    @EJB
    private HazelcastClientService hazelcastClientService;

    @EJB
    private EmployeeService employeeService;

    @EJB
    private SkillService skillService;

    @EJB
    private TeamService teamService;

    @EJB
    private SiteService siteService;

    @EJB
    private ShiftStructureService shiftStructureService;

    @EJB
    private CIAvailabilityTimeFrameService ciAvailabilityTimeFrameService;

    @EJB
    private CDAvailabilityTimeFrameService cdAvailabilityTimeFrameService;

    @EJB
    private OrganizationService organizationService;

    @EJB
    private ResponseHandlerService responseHandlerService;

    @EJB
    private PostedOpenShiftService postedOpenShiftService;
    
    @EJB
    private AccountUtilService accountUtilService;

    @EJB
    private UserAccountService userAccountService;

    @EJB
    private NotificationService notificationService;

    @EJB
    private ScheduleChangeService scheduleChangeService;

    /**
     * Convenience aggregation of CDAvailabilityTimeFrame and matching
     * Joda Interval for the benefit of using Interval API for calculations.
     * @author emlogis
     *
     */
    private class CDAvailabilityInterval {

    	Interval interval;
    	CDAvailabilityTimeFrame cdAvailabilityTimeFrame;

    	public CDAvailabilityInterval(CDAvailabilityTimeFrame cdAvail) {
    		this.cdAvailabilityTimeFrame = cdAvail;
    		DateTime startDateTime = cdAvail.getStartDateTime().withTimeAtStartOfDay();
    		DateTime endDateTime = new DateTime(startDateTime.plusMinutes(cdAvail.getDurationInMinutes().getMinutes()));
    		this.interval = new Interval(startDateTime, endDateTime);
    	}    	
    }

    private class ShiftInfo {
        String shiftStructureId;
        String shiftPatternId;
        Date cdDate;
        Integer dayOffset;
        Time startTime;
        Integer dayIndex;
        Integer durationInMins;
        Integer employeeCount;
        Integer excessCount;
        Integer skillProficiencyLevel;
        String shiftLengthId;
        String shiftLengthName;
        Integer paidTimeInMin;
        Integer lengthInMin;
        Boolean excess;
        String skillId;
        String skillName;
        String skillAbbrev;
        String teamId;
        String teamName;
        String siteName;
        String shiftReqId;
    }

    private class BooleanClInfo {
        String employeeId;
        Boolean enabled;
        Integer weight;
        ContractLineType contractLineType;

        private BooleanClInfo(Object[] array) {
            this.employeeId = (String) array[0];
            this.enabled = (Boolean) array[1];
            this.weight = array[2] == null ? null : (int) array[2];
            this.contractLineType = array[3] == null ? null : ContractLineType.valueOf((String) array[3]);
        }
    }

    private class IntMinMaxClInfo {
        String employeeId;
        Boolean maximumEnabled;
        Boolean minimumEnabled;
        Integer maximumValue;
        Integer minimumValue;
        Integer maximumWeight = -1; //default -1
        Integer minimumWeight = -1; //default -1
        ContractLineType contractLineType;

        IntMinMaxClInfo(Object[] array) {
            this.employeeId = (String) array[0];
            this.maximumEnabled = (Boolean) array[1];
            this.minimumEnabled = (Boolean) array[2];
            this.maximumValue = array[3] == null ? null : (int) array[3];
            this.minimumValue = array[4] == null ? null : (int) array[4];
            this.maximumWeight = array[5] == null ? null : (int) array[5];
            this.minimumWeight = array[6] == null ? null : (int) array[6];
            this.contractLineType = array[7] == null ? null : ContractLineType.valueOf((String) array[7]);
        }
    }

    private class WeekendRotationPatternClInfo {
        String employeeId;
        Integer weight;
        DayOfWeek dayOfWeek;
        Integer numberOfDays;
        Integer outOfTotalDays;
        WeekdayRotationPattern.RotationPatternType rotationType;
        ContractLineType contractLineType;

        WeekendRotationPatternClInfo(Object[] array) {
            this.employeeId = (String) array[0];
            this.weight = array[1] == null ? null : (int) array[1];
            this.dayOfWeek = array[2] == null ? null : DayOfWeek.values()[(int) array[2]];
            this.numberOfDays = array[3] == null ? null : (int) array[3];
            this.outOfTotalDays = array[4] == null ? null : (int) array[4];
            this.rotationType = array[5] == null ? null
                    : WeekdayRotationPattern.RotationPatternType.values()[(int) array[5]];
            this.contractLineType = array[6] == null ? null : ContractLineType.valueOf((String) array[6]);
        }
    }

    private class WeekendWorkPatternClInfo {
        String employeeId;
        Integer weight;
        String daysOffAfter;
        String daysOffBefore;
        ContractLineType contractLineType;

        WeekendWorkPatternClInfo(Object[] array) {
            this.employeeId = (String) array[0];
            this.weight = array[1] == null ? null : (int) array[1];
            this.daysOffAfter = (String) array[2];
            this.daysOffBefore = (String) array[3];
            this.contractLineType = array[4] == null ? null : ContractLineType.valueOf((String) array[4]);
        }
    }
    
    private class QualificationEntitiesAggregation {
    	Schedule schedule;
    	List<Shift> shifts = new ArrayList<>();
    }
    
    private class OpenShiftEligibilityEntitiesAggregation {
    	List<Employee> employees = new ArrayList<>();
    	List<Shift> shifts = new ArrayList<>();
    }
    
    private class ShiftSwapEligibilityEntitiesAggregation {
    	List<Shift> swapSeekingShifts = new ArrayList<>();
    	List<Shift> swapCandidateShifts = new ArrayList<>();
    }
    
    @PostConstruct
    public void init(){
    	scheduleServiceEJB = sessionContext.getBusinessObject(ScheduleService.class);
    }

    public void flush() {
        entityManager.flush();
    }

    public ResultSet<Schedule> findSchedules(SimpleQuery simpleQuery) {
        simpleQuery.setEntityClass(Schedule.class);
        SimpleQueryHelper sqh = new SimpleQueryHelper();
        return sqh.executeSimpleQueryWithPaging(entityManager, simpleQuery);
    }

    public Schedule getSchedule(PrimaryKey primaryKey) {
        return entityManager.find(Schedule.class, primaryKey);
    }

    public Schedule resetState(PrimaryKey primaryKey) {
        Schedule schedule = getSchedule(primaryKey);

        schedule.setExecutionAckDate(0);
        schedule.setExecutionEndDate(0);
        schedule.setExecutionStartDate(0);
        schedule.setResponseReceivedDate(0);
        schedule.setRequestSentDate(0);
        schedule.setState(TaskState.Idle);

        Schedule updatedSchedule = update(schedule);
        getEventService().sendEntityUpdateEvent(schedule, ScheduleDto.class);
        return updatedSchedule;
    }

    public int unassignedShiftCount(Schedule schedule) {
        String sql = "SELECT COUNT(*) FROM Shift WHERE scheduleId = :scheduleId " +
                                                 " AND tenantId = :tenantId AND employeeId IS NULL";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", schedule.getId());
        query.setParameter("tenantId", schedule.getTenantId());
        return ((BigInteger) query.getSingleResult()).intValue();
    }

    public Schedule promote(PrimaryKey primaryKey) {
        Schedule targetSchedule = getSchedule(primaryKey);

        checkProductionPostedScheduledTeams(targetSchedule);

        ScheduleStatus status = targetSchedule.getStatus();

        if (status == ScheduleStatus.Simulation) {
            status = ScheduleStatus.Production;
        } else if (status == ScheduleStatus.Production) {
            status = ScheduleStatus.Posted;
        } else {
            throw new ValidationException(sessionService.getMessage("schedule.promote.status.error",
                    primaryKey.getId(), targetSchedule.getStatus()));
        }
        Collection<Schedule> schedules;

        /*commented out group promotion
        if (targetSchedule.getScheduleGroupId() != null) {
            SimpleQuery simpleQuery = new SimpleQuery(primaryKey.getTenantId());
            simpleQuery.setEntityClass(Schedule.class);
            simpleQuery.addFilter("scheduleGroupId = '" + targetSchedule.getScheduleGroupId() + "'");
            schedules = new SimpleQueryHelper().executeSimpleQuery(entityManager, simpleQuery);
        } else {
            schedules = new ArrayList<>();
            schedules.add(targetSchedule);
        }*/

        schedules = new ArrayList<>();
        schedules.add(targetSchedule);

        for (Schedule schedule : schedules) {
            schedule = promoteSchedule(schedule, status);

            if (targetSchedule.equals(schedule)) {
                targetSchedule = schedule;
            }
        }

        return targetSchedule;
    }

    public Schedule cloneSchedule(PrimaryKey primaryKey) {
        Schedule schedule = getSchedule(primaryKey);
        try {
            Schedule result = schedule.clone();

            checkUniqueScheduleName(schedule.getTenantId(), result.getName());

            result.setPrimaryKey(new PrimaryKey(schedule.getTenantId()));
            result.setExecutionAckDate(0);
            result.setExecutionEndDate(0);
            result.setExecutionStartDate(0);
            result.setResponseReceivedDate(0);
            result.setRequestSentDate(0);
            result.setCompletion(null);
            result.setCompletionInfo(StringUtils.EMPTY);
            result.setScheduleReport(null);
            result.setHardScore(0);
            result.setMediumScore(0);
            result.setSoftScore(0);
            result.setEngineId(null);
            result.setEngineLabel(null);

            checkProductionPostedScheduledTeams(result);

            entityManager.persist(result);
            return result;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(sessionService.getMessage("schedule.clone.error", schedule.getId()), e);
        }
    }

    public Schedule duplicate(PrimaryKey schedulePrimaryKey, String name, AssignmentMode mode, long startDate,
                              UserAccount mngrAccount) {
        String tenantId = schedulePrimaryKey.getTenantId();

        Schedule schedule = getSchedule(schedulePrimaryKey);
        try {
            Schedule result = schedule.clone();
            if (StringUtils.isNotBlank(name)) {
            	result.setName(name);
            }

            checkUniqueScheduleName(tenantId, result.getName());

            result.setStatus(ScheduleStatus.Simulation);
            result.setPrimaryKey(new PrimaryKey(tenantId));
            result.setExecutionAckDate(0);
            result.setResponseReceivedDate(0);
            result.setRequestSentDate(0);
            result.setCompletion(null);
            result.setCompletionInfo(StringUtils.EMPTY);
            result.setScheduleReport(null);
            result.setHardScore(0);
            result.setSoftScore(0);
            result.setEngineId(null);
            result.setEngineLabel(null);
            result.setReturnedAssignedShifts(-1);
            result.setReturnedOpenShifts(-1);

            // this part is important for correct handling pre/post/engine assigned shifts
            result.setExecutionEndDate(schedule.getExecutionEndDate());
            result.setExecutionStartDate(schedule.getExecutionStartDate());
            result.setState(schedule.getState());
            // end of part for correct handling pre/post/engine assigned shifts

            long scheduleCorrelatedDate = TimeUtil.datePlusDays(schedule.getStartDate(),
                    schedule.getScheduleLengthInDays());
            long scheduleNewStartDate = startDate != 0 ? startDate : scheduleCorrelatedDate;
            result.setStartDate(scheduleNewStartDate);

            checkProductionPostedScheduledTeams(result);

            SchedulingOptions schedulingOptions = schedule.getSchedulingOptions();
            if (schedulingOptions != null) {
                schedulingOptions = schedulingOptions.clone();
                schedulingOptions.setPrimaryKey(new PrimaryKey(tenantId));

                entityManager.persist(schedulingOptions);

                result.setSchedulingOptions(schedulingOptions);
            }

            boolean scheduleModified = false;
            Set<ShiftStructure> newShiftStructures = new HashSet<>();
            if (ScheduleType.ShiftStructureBased.equals(schedule.getScheduleType())) {
                for (ShiftStructure shiftStructure : schedule.getShiftStructures()) {
                    long correlatedDate = TimeUtil.datePlusDays(shiftStructure.getStartDate(),
                            schedule.getScheduleLengthInDays());
                    long newStartDate = TimeUtil.truncateDate(startDate != 0 ? startDate : correlatedDate);

                    List<ShiftStructure> shiftStructures = shiftStructureService.getTeamShiftStructuresByDate(
                            shiftStructure.getTeam(), newStartDate);
                    if (shiftStructures.size() > 0) {
                        throw new ValidationException(sessionService.getMessage("validation.shiftstructure.startdate",
                                new Date(newStartDate), shiftStructure.getTeam().getName()));
                    }

                    PrimaryKey newStructurePrimaryKey = shiftStructure.getPrimaryKey();
                    ShiftStructure newShiftStructure = shiftStructureService.duplicate(newStructurePrimaryKey,
                            newStartDate);

                    if (isCorrelatedDates(newStartDate, correlatedDate, schedule.getScheduleLengthInDays())) {
                        String scheduleGroupId = schedule.getScheduleGroupId();
                        if (scheduleGroupId == null) {
                            scheduleGroupId = UniqueId.getId();
                            schedule.setScheduleGroupId(scheduleGroupId);
                            scheduleModified = true;
                        }
                        result.setScheduleGroupId(scheduleGroupId);
                    }

                    newShiftStructures.add(newShiftStructure);
                }
                result.setShiftStructures(newShiftStructures);
            } else {
                Set<PatternElt> newPatternElts = new HashSet<>();
                for (PatternElt patternElt : schedule.getPatternElts()) {
                    PatternElt newPatternElt = new PatternElt(new PrimaryKey(tenantId));
                    newPatternElt.setShiftPattern(patternElt.getShiftPattern());
                    newPatternElt.setSchedule(result);
                    newPatternElt.setDayOffset(patternElt.getDayOffset());
                    if (newPatternElt.getCdDate() != null
                            && newPatternElt.getCdDate().getMillis() > Constants.DATE_2000_01_01) {
                        newPatternElt.setCdDate(new DateTime(scheduleNewStartDate));
                    }

                    createPatternElt(patternElt);

                    newPatternElts.add(newPatternElt);
                }
                result.setPatternElts(newPatternElts);
            }

            if (scheduleModified) {
                update(schedule);
            }

            entityManager.persist(result);

            if (mode == AssignmentMode.PREASSIGNMENT || mode == AssignmentMode.ALLASSIGNMENT) {
                generateShifts(result, 0, mngrAccount);
                Collection<Shift> shifts = shiftService.getScheduleShifts(schedule);
                Collection<Shift> newShifts = shiftService.getScheduleShifts(result);
                for (Shift shift : shifts) {
                    if (mode == AssignmentMode.PREASSIGNMENT && AssignmentType.MANUAL.equals(shift.getAssignmentType())
                            || mode == AssignmentMode.ALLASSIGNMENT) {
                        Shift shiftToOverride = findMatchingShift(schedule, shift, result, newShifts);
                        if (shiftToOverride != null || StringUtils.isEmpty(shift.getShiftPatternId())) {
                            if (shiftToOverride == null) {
                                shiftToOverride = shift.clone();
                                shiftToOverride.setPrimaryKey(new PrimaryKey(shift.getTenantId()));
                                shiftToOverride.setScheduleId(result.getId());
                            }
                            shiftToOverride.setLocked(shift.isLocked());
                            shiftToOverride.setExcess(shift.isExcess());
                            if (StringUtils.isNotEmpty(shift.getEmployeeId())) {
                                shiftToOverride.makeShiftAssignment(shift.getEmployeeId(), shift.getEmployeeName(),
                                        shift.getAssignmentType());
                            } else {
                                shiftToOverride.dropShiftAssignment();
                            }
                            shiftService.update(shiftToOverride);
                        }
                    }
                }
            }

            return result;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(sessionService.getMessage("schedule.clone.error", schedule.getId()), e);
        }
    }

    public Schedule create(PrimaryKey primaryKey) {
        Schedule schedule = new Schedule(primaryKey);
        SchedulingOptions schedulingOptions = new SchedulingOptions(new PrimaryKey(primaryKey.getTenantId()));
        schedule.setSchedulingOptions(schedulingOptions);
        insertSchedulingOptions(schedulingOptions);
        entityManager.persist(schedule);
        return schedule;
    }

    public Schedule update(Schedule schedule) {
        checkProductionPostedScheduledTeams(schedule);
        return entityManager.merge(schedule);
    }

    public void insertSchedulingOptions(SchedulingOptions schedulingOptions) {
        entityManager.persist(schedulingOptions);
    }

    public SchedulingOptions updateSchedulingOptions(SchedulingOptions schedulingOptions) {
        return entityManager.merge(schedulingOptions);
    }

    public void trackScheduleChanges(Schedule schedule, ChangeType changeType, UserAccount managerAccount) {
        scheduleChangeService.createChange(schedule, ScheduleChange.class, ChangeCategory.ScheduleChange, changeType,
                null, managerAccount, null, true);
    }

    public void delete(Schedule schedule, UserAccount mngrAccount) {
        entityManager.remove(schedule);

        if (ScheduleStatus.Posted.equals(schedule.getStatus())) {
            sendScheduleDeleteNotification(schedule, mngrAccount);
        }

        shiftService.clearScheduleShifts(schedule, mngrAccount);
    }

    public ResultSet<Team> getTeams(PrimaryKey primaryKey, SimpleQuery simpleQuery) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SimpleQueryHelper sqh = new SimpleQueryHelper();
        SimpleQueryHelper.tryAddNotDeletedFilter(simpleQuery);
        return sqh.executeGetAssociatedWithPaging(entityManager, simpleQuery, primaryKey, Schedule.class, "teams");
    }

    public Collection<Team> associateTeams(Schedule schedule, List<String> ids, UserAccount managerAccount) {
        String tenantId = schedule.getTenantId();

        Collection<Team> teams = schedule.getTeams();
        if (teams == null) {
            teams = new HashSet<>();
        }
        Collection<Team> toRemove = new HashSet<>();
        toRemove.addAll(teams);
        if (ids != null) {
            for (String id : ids) {
                Team team = ModelUtils.find(teams, id);
                if (team == null) {
                    team = teamService.getTeam(new PrimaryKey(tenantId, id));
                    teams.add(team);
                } else {
                    toRemove.remove(team);
                }
            }
        }
        teams.removeAll(toRemove);

        shiftService.clearScheduleShifts(schedule, toRemove, managerAccount);

        schedule.setScheduledTeamCount(teams.size());
        schedule.setScheduledEmployeeCount(getEmployeeCount(schedule.getTenantId(), schedule.getId()));

        // unassosiate ShiftPatterns for teams removed from schedule
        if (ScheduleType.ShiftPatternBased.equals(schedule.getScheduleType())) {
            unassosiateShiftPatterns(toRemove, schedule);
        }

        update(schedule);

        return teams;
    }

    public ResultSet<Employee> getEmployees(PrimaryKey schedulePrimaryKey,
                                            String filter,
                                            int offset,
                                            int limit,
                                            String orderBy,
                                            String orderDir) {
        ResultSet<Employee> result = new ResultSet<>();

        String sql =
            "SELECT DISTINCT e.* FROM Schedule s, Team_Schedule ts, EmployeeTeam et, EmployeeSkill es, Employee e " +
            " WHERE ts.schedules_id = :scheduleId AND ts.schedules_tenantId = :tenantId " +
            "   AND ts.schedules_tenantId = et.tenantId AND et.tenantId = e.tenantId AND e.tenantId = es.tenantId " +
            "   AND ts.schedules_tenantId = s.tenantId AND ts.schedules_id = s.id " +
            "   AND ts.Team_id = et.teamId AND et.employeeId = e.id AND e.id = es.employeeId " +
            "   AND (e.activityType != 0 OR e.inactiveDate > s.startDate) " +
            (StringUtils.isNotBlank(filter) ? " AND " + SimpleQueryHelper.buildFilterClause(filter, "e") : "") +
            " AND " + QueryPattern.NOT_DELETED.val("e");

        String countSql = "SELECT count(*) FROM (" + sql + ") x";

        if (StringUtils.isNotBlank(orderBy)) {
            sql += " ORDER BY e." + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        Query query = entityManager.createNativeQuery(sql, Employee.class);
        query.setParameter("scheduleId", schedulePrimaryKey.getId());
        query.setParameter("tenantId", schedulePrimaryKey.getTenantId());
        query.setFirstResult(offset);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter("scheduleId", schedulePrimaryKey.getId());
        countQuery.setParameter("tenantId", schedulePrimaryKey.getTenantId());

        result.setTotal(((BigInteger) countQuery.getSingleResult()).intValue());
        result.setResult(query.getResultList());

        return result;
    }

    public Map<String, Integer> scheduleEmployeesMinutesPerWeek(PrimaryKey schedulePrimaryKey) {
        Map<String, Integer> result = new HashMap<>();

        String sql =
            "SELECT " +
            "    et.employeeId, " +
            "    CASE  " +
            "       WHEN imm.minimumEnabled THEN imm.minimumValue " +
            "       WHEN imm.maximumEnabled THEN imm.maximumValue " +
            "       ELSE 0 " +
            "    END minutes " +
            "  FROM Team_Schedule ts " +
            "       LEFT JOIN EmployeeTeam et ON ts.Team_id = et.teamId AND ts.Team_tenantId = et.teamTenantId " +
            "       LEFT JOIN EmployeeContract ec ON et.employeeId = ec.employeeId " +
            "                                    AND et.employeeTenantId = ec.employeeTenantId " +
            "       LEFT JOIN IntMinMaxCL imm ON ec.id = imm.contractId AND ec.tenantId = imm.contractTenantId " +
            "                                AND imm.contractLineType = 'HOURS_PER_WEEK' " +
            " WHERE ts.schedules_id = :scheduleId AND ts.schedules_tenantId = :tenantId ";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", schedulePrimaryKey.getId());
        query.setParameter("tenantId", schedulePrimaryKey.getTenantId());

        List<Object[]> rows = query.getResultList();
        for (Object[] row : rows) {
            result.put((String) row[0], ((Number) row[1]).intValue());
        }

        return result;
    }

    public Collection<ShiftPattern> getApplicableShiftPatterns(PrimaryKey schedulePrimaryKey, String filter,
            int offset, int limit, String orderBy, String orderDir) {
        String sql =
            "SELECT DISTINCT sp.* FROM Schedule s " +
            "  LEFT JOIN Team_Schedule ts ON s.id = ts.schedules_id AND s.tenantId = ts.schedules_tenantId " +
            "  LEFT JOIN ShiftPattern sp ON sp.teamId = ts.Team_id AND sp.teamTenantId = ts.Team_tenantId " +
            " WHERE s.id = :scheduleId AND s.tenantId = :tenantId " +
            (StringUtils.isNotBlank(filter) ? " AND " + SimpleQueryHelper.buildFilterClause(filter, "sp") : "");

        Query query = entityManager.createNativeQuery(sql, ShiftPattern.class);
        query.setParameter("scheduleId", schedulePrimaryKey.getId());
        query.setParameter("tenantId", schedulePrimaryKey.getTenantId());
        if (limit > 0) {
            query.setFirstResult(offset);
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        if (StringUtils.isNotBlank(orderBy)) {
            sql += " ORDER BY sp." + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        return query.getResultList();
    }

    public Map<String, double[]> getEmployeeAvailability(Schedule schedule) {
        Map<String, List> contractLines = getScheduleIntCLs(schedule);
        Map<String, List> cdAvaliability = getScheduleCDAvailability(schedule);
        Map<String, List> ciAvaliability = getScheduleCIAvailability(schedule);

        Map<String, double[]> avalHours = new HashMap<>();

        int lengthInDays = schedule.getScheduleLengthInDays();
        int totalAvail = 1440 * lengthInDays;
        long scheduleStartDate = schedule.getStartDate();
        long scheduleEndDate = schedule.getStartDate() + (lengthInDays * 24 * 60 * 60 * 1000);

        Iterator it = contractLines.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry cl = (Map.Entry) it.next();
            int totalMinutesMin = 0;
            int totalMinutesMax = 0;
            for (Object[] row : (List<Object[]>) cl.getValue()) {
                switch ((String) row[0]) { //getContractLineType
                    case "HOURS_PER_WEEK": {
                        if ((boolean) row[1]) {//(intMinMaxCL.getMaximumEnabled()){
                            totalMinutesMax += (Integer) row[2] * (lengthInDays / 7);
                        } else {
                            totalMinutesMax = -1;
                        }
                        if ((boolean) row[3]) {
                            totalMinutesMin += (Integer) row[4] * (lengthInDays / 7);
                        } else {
                            totalMinutesMin = -1;
                        }
                    }
                }
            }
            if (totalMinutesMax < 0 || totalMinutesMax < 0){
                System.out.println();
            }
            avalHours.put((String)cl.getKey(), new double[]{totalMinutesMin, totalMinutesMax});
        }

        it = cdAvaliability.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry cd = (Map.Entry)it.next();
            if (avalHours.containsKey(cd.getKey())) {
                int totalMinutesMin = (int) avalHours.get(cd.getKey()) [0];
                int totalMinutesMax = (int) avalHours.get(cd.getKey()) [1];
                for (Object[] row : (List<Object[]>) cd.getValue()){
                    //cd.availabilityType, cd.durationInMinutes, cd.isPTO, cd.startDateTime
                    if (row[0].equals("UnAvail") && (boolean) row[2]) {
                        long frameStartDate = ((Timestamp) row[3]).getTime();
                        long frameEndDate = frameStartDate + ((int) row[1] * 60 * 1000);
                        long diffMin = 0;
                        long diffMax = 0;
                        if ((frameStartDate >= scheduleStartDate && frameStartDate <= scheduleEndDate) &&
                                (frameEndDate >= scheduleStartDate && frameEndDate <= scheduleEndDate)) {
                            diffMin = (frameEndDate - frameStartDate) / 1000 / 60;
                        } else if ((frameStartDate >= scheduleStartDate && frameStartDate <= scheduleEndDate) &&
                                (frameEndDate > scheduleEndDate)){
                           diffMin = (scheduleEndDate - frameStartDate) / 1000 / 60;
                        } else if ((frameStartDate < scheduleStartDate) &&
                                (frameEndDate >= scheduleStartDate && frameEndDate <= scheduleEndDate)){
                           diffMin = (frameEndDate - scheduleStartDate) / 1000 / 60;
                        }
                        if (totalMinutesMax < 0 || totalMinutesMax < 0){
                            System.out.println();
                        }
                        totalMinutesMax -= (totalMinutesMax > 0) ? diffMin : 0;
                        totalMinutesMin -= (totalMinutesMin > 0) ? diffMin : 0;
                        if ((frameStartDate < scheduleStartDate && frameEndDate > scheduleEndDate)) {
                            totalMinutesMax = 0;
                            totalMinutesMin = 0;
                        }
                    }
                }
                avalHours.put((String) cd.getKey(), new double[] {totalMinutesMin, totalMinutesMax});
            }
        }

        it = ciAvaliability.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry ci = (Map.Entry) it.next();
            if (avalHours.containsKey(ci.getKey())) {
                int totalMinutesMin = (int) avalHours.get(ci.getKey())[0];
                int totalMinutesMax = (int) avalHours.get(ci.getKey())[1];
                int totalUnavail = 0;
                for (Object[] row : (List<Object[]>) ci.getValue()) {
                    //row = [ci.availabilityType, ci.durationInMinutes, ci.dayOfTheWeek, ci.startTime, ci.startDateTime, ci.endDateTime]
                    //if frame always effective
                    if ((scheduleStartDate >= ((Timestamp) row[4]).getTime()) &&
                            ((row[5] == null || scheduleStartDate < ((Timestamp) row[5]).getTime()))) {
                        totalUnavail += ((int) row[1]) * (lengthInDays / 7);
                    }
                    //if frame starts inside schedule
                    if ((scheduleStartDate < ((Timestamp) row[4]).getTime()) &&
                            (scheduleEndDate > ((Timestamp) row[4]).getTime()) &&
                            ((row[5] == null || scheduleEndDate < ((Timestamp) row[5]).getTime()))) {
                        totalUnavail += ((int) row[1]) * (
                                (scheduleEndDate - ((Timestamp) row[4]).getTime() / 1000 / 60 / 60 / 24 / 7));
                    }
                    //if frame inside schedule
                    if (row[5] != null && (scheduleStartDate < ((Timestamp) row[4]).getTime()) &&
                            (scheduleEndDate > ((Timestamp) row[5]).getTime())) {
                        totalUnavail += ((int) row[1]) * (
                                ((((Timestamp) row[5]).getTime() - ((Timestamp) row[4]).getTime()) / 1000 / 60 / 60 / 24 / 7));
                    }
                    //if frame ends inside schedule
                    if (row[5] != null && (scheduleStartDate > ((Timestamp) row[4]).getTime()) &&
                            (scheduleEndDate > ((Timestamp) row[5]).getTime()) &&
                            (scheduleStartDate < ((Timestamp) row[5]).getTime())) {
                        totalUnavail += ((int) row[1]) * (
                                ((((Timestamp) row[5]).getTime() - scheduleStartDate) / 1000 / 60 / 60 / 24 / 7));
                    }
                }
                int CIavail = totalAvail - totalUnavail;
                if (CIavail < totalMinutesMin) {
                    totalMinutesMin = CIavail;
                }
                avalHours.put((String) ci.getKey(), new double[]{totalMinutesMin, totalMinutesMax});
            }
        }

        it = avalHours.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry elem = (Map.Entry)it.next();
            int totalMinutesMin = (int)avalHours.get(elem.getKey())[0];
            int totalMinutesMax = (int)avalHours.get(elem.getKey())[1];
            avalHours.put((String)elem.getKey(), new double[]{(totalMinutesMin > 0) ? (double)totalMinutesMin / 60 : -1,
                    (totalMinutesMax > 0) ? (double)totalMinutesMax / 60 : -1});
        }

        return avalHours;
    }

    public double[] getEmployeeAvailability(Employee employee, Schedule schedule) {
        int totalMinutesMin = 0;
        int totalMinutesMax = 0;

        int lengthInDays = schedule.getScheduleLengthInDays();
        long scheduleStartDate = schedule.getStartDate();
        long scheduleEndDate = schedule.getStartDate() + (lengthInDays * 24 * 60 * 60 * 1000);

        Set<ContractLine> contractLines = employee.getEmployeeContracts().iterator().next().getContractLines();

        for (ContractLine contractLine : contractLines){
            if(contractLine instanceof IntMinMaxCL){
                IntMinMaxCL intMinMaxCL = (IntMinMaxCL) contractLine;
                switch (intMinMaxCL.getContractLineType().getValue()){
                    case "HoursWeek": {
                        if (intMinMaxCL.getMaximumEnabled()){
                            totalMinutesMax += intMinMaxCL.getMaximumValue() * (lengthInDays/7);
                        } else {
                            totalMinutesMax = -1;
                        }
                        if (intMinMaxCL.getMinimumEnabled()){
                            totalMinutesMin += intMinMaxCL.getMinimumValue() * (lengthInDays/7);
                        } else {
                            totalMinutesMin = -1;
                        }
                    }
                }
            }
        }

        Set<AvailabilityTimeFrame> availabilityTimeFrames = employee.getAvailabilityTimeFrames();

        for (AvailabilityTimeFrame timeFrame : availabilityTimeFrames){

            if(timeFrame.getAvailabilityType().equals(AvailabilityTimeFrame.AvailabilityType.UnAvail)){
                if(timeFrame instanceof CDAvailabilityTimeFrame){
                    CDAvailabilityTimeFrame cdAvailabilityTimeFrame = (CDAvailabilityTimeFrame) timeFrame;
                    long frameStartDate = cdAvailabilityTimeFrame.getStartDateTime().getMillis();
                    long frameEndDate = frameStartDate + cdAvailabilityTimeFrame.getDurationInMinutes().toStandardDuration().getMillis();

                    long diff = 0;
                    if ((frameStartDate >= scheduleStartDate && frameStartDate <= scheduleEndDate) &&
                            (frameEndDate >= scheduleStartDate && frameEndDate <= scheduleEndDate)){
                        diff = (frameEndDate - frameStartDate) / 1000 / 60;
                    } else if ((frameStartDate >= scheduleStartDate && frameStartDate <= scheduleEndDate) &&
                            (frameEndDate > scheduleEndDate)){
                        diff = (scheduleEndDate - frameStartDate) / 1000 / 60;
                    } else if ((frameStartDate < scheduleStartDate) &&
                            (frameEndDate >= scheduleStartDate && frameEndDate <= scheduleEndDate)){
                        diff = (frameEndDate - scheduleStartDate) / 1000/ 60;
                    }
                    totalMinutesMax -= (totalMinutesMax > 0) ? diff : 0;
                    totalMinutesMin -= (totalMinutesMin > 0) ? diff : 0;
                    if ((frameStartDate < scheduleStartDate && frameEndDate > scheduleEndDate)){
                        totalMinutesMax = 0;
                        totalMinutesMin = 0;
                    }
                }

                if(timeFrame instanceof CIAvailabilityTimeFrame){
                    CIAvailabilityTimeFrame ciAvailabilityTimeFrame = (CIAvailabilityTimeFrame) timeFrame;
                    long frameStartDate = ciAvailabilityTimeFrame.getStartTime().toDateTimeToday().getMillis();
                    if (frameStartDate < scheduleStartDate){
                        for (org.elasticsearch.common.joda.time.LocalDate date = new org.elasticsearch.common.joda.time.LocalDate(scheduleStartDate);
                             date.isBefore(new org.elasticsearch.common.joda.time.LocalDate(scheduleEndDate)); date = date.plusDays(1)){
                            //todo check 1st last days of schedule
                            if (date.getDayOfWeek()-1 == ciAvailabilityTimeFrame.getDayOfTheWeek().ordinal()){
                                totalMinutesMax -= ciAvailabilityTimeFrame.getDurationInMinutes().getMinutes();
                                totalMinutesMin -= ciAvailabilityTimeFrame.getDurationInMinutes().getMinutes();
                            }
                        }
                    } else if (frameStartDate > scheduleStartDate && frameStartDate < scheduleEndDate){
                        for (org.elasticsearch.common.joda.time.LocalDate date = new org.elasticsearch.common.joda.time.LocalDate(frameStartDate);
                             date.isBefore(new org.elasticsearch.common.joda.time.LocalDate(scheduleEndDate)); date = date.plusDays(1)){
                            //todo check  last day of schedule
                            if (date.getDayOfWeek()-1 == ciAvailabilityTimeFrame.getDayOfTheWeek().ordinal()){
                                totalMinutesMax -= ciAvailabilityTimeFrame.getDurationInMinutes().getMinutes();
                                totalMinutesMin -= ciAvailabilityTimeFrame.getDurationInMinutes().getMinutes();
                            }
                        }
                    }
                }
            }
        }
        return new double[]{(totalMinutesMin > 0) ? (double)totalMinutesMin / 60 : -1,
                (totalMinutesMax > 0) ? (double)totalMinutesMax / 60 : -1};
    }

    public ScheduleReport getScheduleReport(PrimaryKey schedulePrimaryKey) {
        Schedule schedule = getSchedule(schedulePrimaryKey);

        ScheduleReport result = schedule.getScheduleReport();

        if (result == null) {
            String awsFolder = System.getProperty(Constants.AWS_FOLDER_PROPERTY);
            if (StringUtils.isBlank(awsFolder)) {
                awsFolder = Constants.AWS_DEFAULT_FOLDER;
            }
            awsFolder += "/EmLogis-" + schedulePrimaryKey.getTenantId();

            try {
                AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(
                        Constants.AWS_PROFILE_CONFIG_FILE_PATH, Constants.AWS_PROFILE_NAME);
                AmazonS3 s3client = new AmazonS3Client(credentialsProvider);
                S3Object s3Object = s3client.getObject(awsFolder, schedulePrimaryKey.getId());
                S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s3ObjectInputStream));
                String json = "";
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    json += line;
                }
                result = EmlogisUtils.fromJsonString(json, ScheduleReport.class);
            } catch (AmazonS3Exception | IOException | IllegalArgumentException e) {
                logger.error("getScheduleReport Error", e);
                result = null;
            }
         }

        return result;
    }

    public static final String OPER_COUNT = "COUNT(*)";
    public static final String OPER_SUM_HOURS = "SUM(shiftLength)";
    public static final String FILTER_REGULAR = "excess=false";
    public static final String FILTER_EXCESS = "excess=true";
    public static final String FILTER_ASSIGNED = "employeeId is not null";
    public static final String FILTER_UNASSIGNED = "employeeId is null";
    public static final DateTime BOT = new DateTime().withYear(2000); 
    public static final DateTime EOT = new DateTime().withYear(2200); 
    

    public ScheduleOverviewDto getScheduleOverview(PrimaryKey schedulePrimaryKey){
        Schedule schedule = getSchedule(schedulePrimaryKey);

        ScheduleOverviewDto overviewDto = new ScheduleOverviewDto();
        ScheduleOverviewDto.OverviewRow shifts = overviewDto.getShifts();
        shifts.setRegular(new Object[]{getShiftsCount(schedule, OPER_COUNT, FILTER_REGULAR)});
        shifts.setExcess(new Object[]{getShiftsCount(schedule, OPER_COUNT, FILTER_EXCESS)});
        shifts.setTotal(new Object[]{getShiftsCount(schedule, OPER_COUNT, "")});
        shifts.setEmployees(new Object[]{schedule.getScheduledEmployeeCount()});

        ScheduleOverviewDto.OverviewRow shiftsAssignments = overviewDto.getShiftsAssignments();
        shiftsAssignments.setRegular(new Object[]{
                getShiftsCount(schedule, OPER_COUNT, FILTER_REGULAR, FILTER_ASSIGNED),
                getShiftsCount(schedule, OPER_COUNT, FILTER_REGULAR, FILTER_UNASSIGNED)
        });
        shiftsAssignments.setExcess(new Object[]{
                getShiftsCount(schedule, OPER_COUNT, FILTER_EXCESS, FILTER_ASSIGNED),
                getShiftsCount(schedule, OPER_COUNT, FILTER_EXCESS, FILTER_UNASSIGNED)
        });
        shiftsAssignments.setTotal(new Object[]{
                getShiftsCount(schedule, OPER_COUNT, FILTER_ASSIGNED),
                getShiftsCount(schedule, OPER_COUNT, FILTER_UNASSIGNED)
        });

        ScheduleOverviewDto.OverviewRow hours = overviewDto.getHours();
        hours.setRegular(new Object[]{getShiftsCount(schedule, OPER_SUM_HOURS, FILTER_REGULAR)});
        hours.setExcess(new Object[]{getShiftsCount(schedule, OPER_SUM_HOURS, FILTER_EXCESS)});
        hours.setTotal(new Object[]{getShiftsCount(schedule, OPER_SUM_HOURS, "")});

        ScheduleOverviewDto.OverviewRow hoursAssignments = overviewDto.getHoursAssignments();
        hoursAssignments.setRegular(new Object[]{
                getShiftsCount(schedule, OPER_SUM_HOURS, FILTER_REGULAR, FILTER_ASSIGNED),
                getShiftsCount(schedule, OPER_SUM_HOURS, FILTER_REGULAR, FILTER_UNASSIGNED)
        });
        hoursAssignments.setExcess(new Object[]{
                getShiftsCount(schedule, OPER_SUM_HOURS, FILTER_EXCESS, FILTER_ASSIGNED),
                getShiftsCount(schedule, OPER_SUM_HOURS, FILTER_EXCESS, FILTER_UNASSIGNED)
        });
        hoursAssignments.setTotal(new Object[]{
                getShiftsCount(schedule, OPER_SUM_HOURS, FILTER_ASSIGNED),
                getShiftsCount(schedule, OPER_SUM_HOURS, FILTER_UNASSIGNED)
        });
        shiftsAssignments.setEmployees(new Object[]{getEmplAssignCount(schedule)});

        hours.setEmployees(new Double[]{0.0, 0.0});
        hoursAssignments.setEmployees(hoursAssignments.getTotal());
        return overviewDto;
    }

    public Double[] getTotalAvalHours(Map<String, double[]> allHours){
        Double[] avalHours = new Double[]{0.0,0.0};
        Iterator it = allHours.entrySet().iterator();
        double[] temp;
        while (it.hasNext()) {
            Map.Entry emplHours = (Map.Entry)it.next();
            temp = (double[])emplHours.getValue();
            avalHours[0] += (temp[0] > 0 ) ? temp[0] : 0;
            avalHours[1] += (temp[1] > 0 ) ? temp[1] : 0;
        }
        return avalHours;
    }

    public List<Map> getAvalHoursBySkill(Schedule schedule, Map<String, double[]> allHours){
        Collection<Team> teams = schedule.getTeams();
        Set<Skill> skills = new HashSet<>();
        for(Team team : teams){
            skills.addAll(team.getSkills());
        }
        ArrayList<Map> reportMapsList = new ArrayList();
        for (Skill skill : skills){

            Map map = new HashMap();
            map.put("id", skill.getId());
            reportMapsList.add(map);
        }
        Map<String, List> skillsEmpls = getEmplGrouped(schedule);
        for (Map map : reportMapsList){
            List<String> emplIds = skillsEmpls.get(map.get("id"));
            double[] skillHours = new double[]{0.0, 0.0};
            if (emplIds != null){
                for (String emplId : emplIds){
                    double[] temp = allHours.get(emplId);
                    if (temp != null){
                        skillHours[0] += (temp[0] > 0 ) ? temp[0] : 0;
                        skillHours[1] += (temp[1] > 0 ) ? temp[1] : 0;
                    }
                }
            }
            map.put("hours", skillHours);
        }
        return reportMapsList;
    }

    public List getSummaryBySkill(Schedule schedule){
        Collection<Team> teams = schedule.getTeams();
        Set<Skill> skills = new HashSet<>();
        for(Team team : teams){
            skills.addAll(team.getSkills());
        }
        ArrayList<Map> reportMapsList = new ArrayList<Map>();
        for (Skill skill : skills){
            ScheduleReportBySkillDto reportBySkillDto = new ScheduleReportBySkillDto();

            Map map = new HashMap();
            map.put("id", skill.getId());
            map.put("name", skill.getName());
            map.put("report", reportBySkillDto);
            reportMapsList.add(map);
        }
        ArrayList<String[]> queries = new ArrayList();
        queries.add(new String[]{OPER_COUNT, FILTER_REGULAR});
        queries.add(new String[]{OPER_COUNT, FILTER_EXCESS});
        queries.add(new String[]{OPER_SUM_HOURS, FILTER_REGULAR});
        queries.add(new String[]{OPER_SUM_HOURS, FILTER_EXCESS});
        queries.add(new String[]{OPER_COUNT, FILTER_REGULAR, FILTER_ASSIGNED});
        queries.add(new String[]{OPER_COUNT, FILTER_EXCESS, FILTER_ASSIGNED});
        queries.add(new String[]{OPER_SUM_HOURS, FILTER_REGULAR, FILTER_ASSIGNED});
        queries.add(new String[]{OPER_SUM_HOURS, FILTER_EXCESS, FILTER_ASSIGNED});
        for (String[] query : queries){
            String[] filters = Arrays.copyOfRange(query, 1, query.length);
            List<Object[]> counts = getShiftsCountGrouped(schedule, query[0], "skillId", filters );
            for (Object[] count : counts) {
                for (Map map : reportMapsList){
                    if (count[0].equals(map.get("id"))){
                        ScheduleReportBySkillDto dto = ((ScheduleReportBySkillDto) map.get("report"));
                        int val = ((Number) count[1]).intValue();
                        if (query.length == 2){
                            if (query[0].equals(OPER_COUNT) && filters[0].equals(FILTER_REGULAR)) {
                                dto.getShifts().setRegular(val);
                            } else if (query[0].equals(OPER_COUNT) && filters[0].equals(FILTER_EXCESS)) {
                                dto.getShifts().setExcess(val);
                            } else if (query[0].equals(OPER_SUM_HOURS) && filters[0].equals(FILTER_REGULAR)) {
                                dto.getHours().setRegular(val);
                            } else if (query[0].equals(OPER_SUM_HOURS) && filters[0].equals(FILTER_EXCESS)) {
                                dto.getHours().setExcess(val);
                            }
                        } else if (query.length == 3 && query[2].equals(FILTER_ASSIGNED)){
                            if (query[0].equals(OPER_COUNT) && filters[0].equals(FILTER_REGULAR)) {
                                dto.getAssignedShifts().setRegular(val);
                            } else if (query[0].equals(OPER_COUNT) && filters[0].equals(FILTER_EXCESS)) {
                                dto.getAssignedShifts().setExcess(val);
                            } else if (query[0].equals(OPER_SUM_HOURS) && filters[0].equals(FILTER_REGULAR)) {
                                dto.getAssignedHours().setRegular(val);
                            } else if (query[0].equals(OPER_SUM_HOURS) && filters[0].equals(FILTER_EXCESS)) {
                                dto.getAssignedHours().setExcess(val);
                            }
                        }
                    }
                }
            }
        }
        List<Object[]> counts = getEmployeesCountGrouped(schedule, OPER_COUNT, "");
        for (Object[] count : counts) {
            for (Map map : reportMapsList){
                if (count[0].equals(map.get("id"))){
                    ScheduleReportBySkillDto dto = ((ScheduleReportBySkillDto) map.get("report"));
                    int val = ((Number) count[1]).intValue();
                    dto.setResources(val);
                }
            }
        }
        counts = getEmplAssignCountGrouped(schedule);
        for (Object[] count : counts) {
            for (Map map : reportMapsList){
                if (count[0].equals(map.get("id"))){
                    ScheduleReportBySkillDto dto = ((ScheduleReportBySkillDto) map.get("report"));
                    int val = ((Number) count[1]).intValue();
                    dto.setResourcesAssignments(val);
                }
            }
        }
        for (Map map : reportMapsList){
            ScheduleReportBySkillDto reportBySkillDto = (ScheduleReportBySkillDto)map.get("report");
            reportBySkillDto.getShifts().setTotal(reportBySkillDto.getShifts().getRegular() +
                    reportBySkillDto.getShifts().getExcess());
            reportBySkillDto.getHours().setTotal(reportBySkillDto.getHours().getRegular() +
                    reportBySkillDto.getHours().getExcess());
            reportBySkillDto.getAssignedShifts().setTotal(reportBySkillDto.getAssignedShifts().getRegular() +
                    reportBySkillDto.getAssignedShifts().getExcess());
            reportBySkillDto.getAssignedHours().setTotal(reportBySkillDto.getAssignedHours().getRegular() +
                    reportBySkillDto.getAssignedHours().getExcess());
        }
        return reportMapsList;
    }

    public Map<String, List> getScheduleIntCLs(Schedule schedule) {
        String sql =
            "SELECT ee.id as employeeId, cl.contractLineType, cl.maximumEnabled, cl.maximumValue, cl.minimumEnabled, " +
            "       cl.minimumValue " +
            "  FROM " +
            "     (SELECT DISTINCT e.* " +
            "        FROM Team_Schedule ts, EmployeeTeam et, EmployeeSkill es, Employee e " +
            "       WHERE ts.schedules_id = :scheduleId " +
            "         AND ts.schedules_tenantId = :tenantId " +
            "         AND ts.schedules_tenantId = et.tenantId " +
            "         AND et.tenantId = e.tenantId " +
            "         AND e.tenantId = es.tenantId " +
            "         AND ts.Team_id = et.teamId " +
            "         AND et.employeeId = e.id " +
            "         AND e.id = es.employeeId) ee, EmployeeContract ec, IntMinMaxCL cl " +
            " WHERE ec.employeeId = ee.id " +
            "   AND ec.employeeTenantId = ee.tenantId " +
            "   AND cl.contractId = ec.id " +
            "   AND cl.contractTenantId = ec.tenantId " +
            " ORDER BY employeeId ";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", schedule.getId());
        query.setParameter("tenantId", schedule.getTenantId());
        List<Object[]> res = query.getResultList();
        Map<String, List> groupedRes = new HashMap<>();
        for (Object[] row : res) {
            String emplId = (String) row[0];
            if (groupedRes.containsKey(emplId)) {
                groupedRes.get(emplId).add(Arrays.copyOfRange(row, 1, row.length));
            } else {
                List temp = new ArrayList();
                temp.add(Arrays.copyOfRange(row, 1, row.length));
                groupedRes.put(emplId, temp);
            }
        }
        return groupedRes;
    }

    public Map<String, List> getScheduleCDAvailability(Schedule schedule) {
        String sql = "SELECT " +
                "  ee.id AS employeeId, " +
                "  cd.availabilityType, " +
                "  cd.durationInMinutes, " +
                "  cd.isPTO, " +
                "  cd.startDateTime " +
                "FROM (SELECT DISTINCT e.* " +
                "      FROM Team_Schedule ts, EmployeeTeam et, EmployeeSkill es, " +
                "        Employee e " +
                "      WHERE ts.schedules_id = :scheduleId AND ts.schedules_tenantId = :tenantId AND " +
                "            ts.schedules_tenantId = et.tenantId AND et.tenantId = e.tenantId AND e.tenantId = es.tenantId " +
                "            AND ts.Team_id = et.teamId AND et.employeeId = e.id AND e.id = es.employeeId) ee, CDAvailabilityTimeFrame cd " +
                "WHERE cd.employeeId = ee.id AND cd.employeeTenantId = ee.tenantId " +
                "ORDER BY employeeId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", schedule.getId());
        query.setParameter("tenantId", schedule.getTenantId());
        List<Object[]> res = query.getResultList();
        Map<String, List> groupedRes = new HashMap<>();
        for (Object[] row : res) {
            String emplId = (String) row[0];
            if (groupedRes.containsKey(emplId)) {
                groupedRes.get(emplId).add(Arrays.copyOfRange(row, 1, row.length));
            } else {
                List temp = new ArrayList();
                temp.add(Arrays.copyOfRange(row, 1, row.length));
                groupedRes.put(emplId, temp);
            }
        }
        return groupedRes;
    }

    public Map<String, List> getScheduleCIAvailability(Schedule schedule) {
        String sql = "SELECT " +
                "  ee.id AS employeeId, " +
                "  ci.availabilityType, " +
                "  ci.durationInMinutes, " +
                "  ci.dayOfTheWeek, " +
                "  ci.startTime, " +
                "  ci.startDateTime, " +
                "  ci.endDateTime " +
                "FROM (SELECT DISTINCT e.* " +
                "      FROM Team_Schedule ts, EmployeeTeam et, EmployeeSkill es, " +
                "        Employee e " +
                "      WHERE ts.schedules_id = :scheduleId AND ts.schedules_tenantId = :tenantId AND " +
                "            ts.schedules_tenantId = et.tenantId AND et.tenantId = e.tenantId AND e.tenantId = es.tenantId " +
                "            AND ts.Team_id = et.teamId AND et.employeeId = e.id AND e.id = es.employeeId) ee, CIAvailabilityTimeFrame ci " +
                "WHERE ci.employeeId = ee.id AND ci.employeeTenantId = ee.tenantId " +
                "ORDER BY employeeId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", schedule.getId());
        query.setParameter("tenantId", schedule.getTenantId());
        List<Object[]> res = query.getResultList();
        Map<String, List> groupedRes = new HashMap<>();
        for (Object[] row : res) {
            String emplId = (String) row[0];
            if (groupedRes.containsKey(emplId)) {
                groupedRes.get(emplId).add(Arrays.copyOfRange(row, 1, row.length));
            } else {
                List temp = new ArrayList();
                temp.add(Arrays.copyOfRange(row, 1, row.length));
                groupedRes.put(emplId, temp);
            }
        }
        return groupedRes;
    }

    public Number getShiftsCount(Schedule schedule, String operation, String... filters) {
        String filterClause = "";
        String alias = "s";
        for (int i = 0; i < filters.length; ++i) {
        	if (StringUtils.isNotBlank(filters[i])) {
        		filterClause += StringUtils.isNotEmpty(filterClause) ? " AND " + alias + "." + filters[i] : alias +
                        "." + filters[i];
        	}
        }
        filterClause = StringUtils.isNotBlank(filterClause) ? "AND " + filterClause : "";
        String sql = "SELECT " + operation + " FROM Shift s WHERE s.scheduleId = :scheduleId " +
                " AND s.tenantId = :tenantId " + filterClause;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", schedule.getId());
        query.setParameter("tenantId", schedule.getTenantId());
        Object res = query.getSingleResult();
        switch (operation) {
            case OPER_COUNT:
                return ((BigInteger) res).intValue();
            case OPER_SUM_HOURS:
                return res != null ? ((BigDecimal) res).doubleValue() / 60 : 0;
            default:
                return 0;
        }
    }
    public int getEmplAssignCount(Schedule schedule) {
        String sql = "SELECT COUNT(*) from (SELECT  DISTINCT employeeId, tenantId  from Shift s " +
                "where s.scheduleId = :scheduleId " +
                " AND s.tenantId = :tenantId AND employeeId is not NULL) res";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", schedule.getId());
        query.setParameter("tenantId", schedule.getTenantId());
        Object res = query.getSingleResult();
        return ((BigInteger) res).intValue();
    }

    public List<Object[]> getShiftsCountGrouped(Schedule schedule, String operation, String groupBy, String... filters) {
        String filterClause = "";
        String alias = "s";
        for (int i = 0; i < filters.length; ++i) {
        	if (StringUtils.isNotBlank(filters[i])){
        		filterClause += StringUtils.isNotEmpty(filterClause) ? " AND " + alias + "." + filters[i] : alias
                        + "." + filters[i];
        	}
        }
        filterClause = StringUtils.isNotBlank(filterClause) ? "AND " + filterClause : "";
        String sql = "SELECT " + groupBy + ", " + operation + " FROM Shift s WHERE s.scheduleId = :scheduleId " +
                " AND s.tenantId = :tenantId " + filterClause + " GROUP BY " + groupBy;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", schedule.getId());
        query.setParameter("tenantId", schedule.getTenantId());
        List<Object[]> resultData = query.getResultList();
        if (operation.equals(OPER_SUM_HOURS)){
            for (Object[] row : resultData){
                row[1] = ((Number)row[1]).intValue()/60;
            }
        }
        return resultData;

    }

    public List<Object[]> getEmployeesCountGrouped(Schedule schedule, String operation, String... filters) {
        String filterClause = "";
        String alias = "e";
        for (int i = 0; i < filters.length; ++i) {
            if (StringUtils.isNotBlank(filters[i])) {
                filterClause += StringUtils.isNotEmpty(filterClause) ? " AND " + alias + "." + filters[i] : alias
                        + "." + filters[i];
            }
        }
        filterClause = StringUtils.isNotBlank(filterClause) ? "AND " + filterClause : "";
        String sql = "SELECT " +
                "  skillId, " +
                "  count(*) " +
                "FROM (SELECT DISTINCT " +
                "        e.id, " +
                "        es.skillId " +
                "      FROM Team_Schedule ts, EmployeeTeam et, EmployeeSkill es, Employee e " +
                "      WHERE ts.schedules_id = :scheduleId AND ts.schedules_tenantId = :tenantId " +
                "            AND ts.schedules_tenantId = et.tenantId AND et.tenantId = e.tenantId AND e.tenantId = es.tenantId " +
                "            AND ts.Team_id = et.teamId AND et.employeeId = e.id AND e.id = es.employeeId " + filterClause +
                "     ) res " +
                "GROUP BY res.skillId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", schedule.getId());
        query.setParameter("tenantId", schedule.getTenantId());
        List<Object[]> resultData = query.getResultList();
        if (operation.equals(OPER_SUM_HOURS)){
            for (Object[] row : resultData){
                row[1] = ((Number)row[1]).intValue()/60;
            }
        }
        return resultData;

    }

    public List<Object[]> getEmplAssignCountGrouped(Schedule schedule) {
        String sql = "SELECT " +
                "  res.skillId, " +
                "  COUNT(*) " +
                "FROM ( " +
                "       SELECT DISTINCT " +
                "         s.employeeId, " +
                "         s.tenantId, " +
                "         s.skillId " +
                "       FROM Shift s " +
                "       WHERE s.scheduleId = :scheduleId " +
                "             AND s.tenantId = :tenantId AND employeeId IS NOT NULL) res " +
                "GROUP BY res.skillId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", schedule.getId());
        query.setParameter("tenantId", schedule.getTenantId());
        List<Object[]> resultData = query.getResultList();
        return resultData;

    }

    public Map<String, List> getEmplGrouped(Schedule schedule) {
        String sql = "SELECT " +
                "  skillId, " +
                "  id " +
                "FROM (SELECT DISTINCT " +
                "        e.id, " +
                "        es.skillId " +
                "      FROM Team_Schedule ts, EmployeeTeam et, EmployeeSkill es, Employee e " +
                "      WHERE ts.schedules_id = :scheduleId AND ts.schedules_tenantId = :tenantId " +
                "            AND ts.schedules_tenantId = et.tenantId AND et.tenantId = e.tenantId AND e.tenantId = es.tenantId " +
                "            AND ts.Team_id = et.teamId AND et.employeeId = e.id AND e.id = es.employeeId) res ";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", schedule.getId());
        query.setParameter("tenantId", schedule.getTenantId());
        List<Object[]> res = query.getResultList();
        Map<String, List> groupedRes = new HashMap<>();
        for (Object[] row : res){
            String skillId = (String)row[0];
            if (groupedRes.containsKey(skillId)) {
                groupedRes.get(skillId).add(row[1]);
            } else {
                List temp = new ArrayList();
                temp.add(row[1]);
                groupedRes.put(skillId, temp);
            }
        }
        return groupedRes;
    }

    public void deleteScheduleReport(ScheduleReport scheduleReport) {
        entityManager.remove(scheduleReport);
    }

    public void createScheduleReport(ScheduleReport scheduleReport) {
        entityManager.persist(scheduleReport);
    }

    public ResultSet<ShiftStructure> getShiftStructures(PrimaryKey primaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper sqh = new SimpleQueryHelper();
        return sqh.executeGetAssociatedWithPaging(entityManager, simpleQuery, primaryKey, Schedule.class,
                "shiftStructures");
    }

    public ResultSet<ShiftDropChange> getShiftDropChanges(SimpleQuery simpleQuery) {
        SimpleQueryHelper helper = new SimpleQueryHelper();
        ResultSet<ShiftDropChange> result = helper.executeSimpleQueryWithPaging(entityManager, simpleQuery);
        return result;
    }

    public Schedule generateShifts(Schedule schedule, long executionStartDate, UserAccount mngrAccount) {
        long generateShiftsStartTime = System.currentTimeMillis();

        sendScheduleGenerateNotification(schedule, mngrAccount, NotificationOperation.GENERATION_START);

        if (!(TaskState.Idle.equals(schedule.getState()) || TaskState.Complete.equals(schedule.getState())
                || TaskState.Starting.equals(schedule.getState()))) {
            throw new ValidationException(sessionService.getMessage("validation.schedule.state.nonoperational"));
        }
        if (ScheduleStatus.Posted.equals(schedule.getStatus())) {
            throw new ValidationException(sessionService.getMessage("validation.schedule.status.nonoperational"));
        }

        java.util.concurrent.locks.Lock executeLock = hazelcastClientService.getLock(schedule.getId());
        if (executeLock == null) {
            executeLock = new ReentrantLock();
            hazelcastClientService.putLock(executeLock, schedule.getId());
        }

        executeLock.lock();
        try {
            clearScheduleChanges(schedule, ChangeType.SCHEDULECLEAR, ChangeType.SCHEDULECREATE);
            trackScheduleChanges(schedule, ChangeType.SCHEDULECLEAR, mngrAccount);

            Collection<Shift> preservedShifts = new HashSet<>();
            if (schedule.isPreservePreAssignedShifts()) {
                preservedShifts.addAll(shiftService.getPreAssignedShifts(schedule));
            }
            if (schedule.isPreservePostAssignedShifts()) {
                preservedShifts.addAll(shiftService.getPostAssignedShifts(schedule));
            }
            if (schedule.isPreserveEngineAssignedShifts()) {
                preservedShifts.addAll(shiftService.getEngineAssignedShifts(schedule));
            }

            shiftService.clearScheduleShifts(schedule, mngrAccount);

            if (ScheduleType.ShiftStructureBased.equals(schedule.getScheduleType())) {
                generateShiftsFromShiftStructure(schedule, preservedShifts, mngrAccount);
            } else {
                generateShiftsFromShiftPattern(schedule, preservedShifts, mngrAccount);
            }

            schedule.setExecutionAckDate(0);
            schedule.setExecutionEndDate(0);
            schedule.setExecutionStartDate(executionStartDate);
            schedule.setResponseReceivedDate(0);
            schedule.setRequestSentDate(0);
            schedule.setReturnedOpenShifts(-1);
            schedule.setReturnedAssignedShifts(-1);
            schedule.setCompletion(ScheduleCompletion.OK);
            schedule.setCompletionInfo(StringUtils.EMPTY);
            schedule.setShiftGenerationDuration(System.currentTimeMillis() - generateShiftsStartTime);
            schedule.setEmployeeGenerationDuration(-1);
            schedule.setResponseProcessingDuration(-1);
            schedule.setRequestGenerationDuration(-1);
            if (schedule.getScheduleReport() != null) {
                deleteScheduleReport(schedule.getScheduleReport());
                schedule.setScheduleReport(null);
            }
            schedule.setHardScore(0);
            schedule.setMediumScore(0);
            schedule.setSoftScore(0);

            Schedule result = update(schedule);
            entityManager.flush();

            return result;
        } finally {
            sendScheduleGenerateNotification(schedule, mngrAccount, NotificationOperation.GENERATION_COMPLETE);

            executeLock.unlock();
        }
    }

    public Schedule execute(PrimaryKey schedulePrimaryKey, int maxComputationTime, int maximumUnimprovedSecondsSpent,
                            Boolean	preservePreAssignedShifts, Boolean preservePostAssignedShifts,
                            Boolean preserveEngineAssignedShifts, UserAccount mngrAccount)
            throws IllegalAccessException {
        long executionStartDate = System.currentTimeMillis();
        StopWatch watch = new StopWatch();
        watch.start();
        watch.split();
        Schedule schedule = getSchedule(schedulePrimaryKey);
        logger.info(String.format("Schedule %s[scheduleId=%s] Execution starting: ellapsed %s, took: %d sec",
                schedule.getName(), schedule.getId(), watch.toString(),
                (watch.getTime() - watch.getSplitTime()) / 1000));
        notifyProgress(schedule, "Schedule execution starting...");

        java.util.concurrent.locks.Lock executeLock = hazelcastClientService.getLock(schedule.getId());
        if (executeLock == null) {
            executeLock = new ReentrantLock();
            hazelcastClientService.putLock(executeLock, schedule.getId());
        }
        
        executeLock.lock();
        logger.info(String.format("Schedule %s[scheduleId=%s] Lock acquired: ellapsed %s, took: %d sec",
                schedule.getName(), schedule.getId(), watch.toString(),
                (watch.getTime() - watch.getSplitTime()) / 1000));
        watch.split();
        try {
            if (!(TaskState.Idle.equals(schedule.getState()) || TaskState.Complete.equals(schedule.getState()))) {
                throw new ValidationException(sessionService.getMessage("validation.schedule.state.nonoperational"));
            }
            if (ScheduleStatus.Posted.equals(schedule.getStatus())
                    || ScheduleStatus.Production.equals(schedule.getStatus())) {
                throw new ValidationException(sessionService.getMessage("validation.schedule.status.nonoperational"));
            }
            Collection<EngineStatus> engineStatuses = hazelcastClientService.getEngines();
            if (engineStatuses == null || engineStatuses.size() == 0) {
                throw new ValidationException(sessionService.getMessage("validation.engine.unavailable"));
            }

            if (TaskState.Idle.equals(schedule.getState())) {
                trackScheduleChanges(schedule, ChangeType.SCHEDULERUN, mngrAccount);
            } else if (TaskState.Complete.equals(schedule.getState())) {
                trackScheduleChanges(schedule, ChangeType.SCHEDULERERUN, mngrAccount);
            }

            schedule.setExecutionStartDate(executionStartDate);
            schedule.setExecutionAckDate(0);
            schedule.setExecutionEndDate(0);
            schedule.setResponseReceivedDate(0);
            schedule.setRequestSentDate(0);
            schedule.setEmployeeGenerationDuration(-1);
            schedule.setResponseProcessingDuration(-1);
            schedule.setRequestGenerationDuration(-1);
            schedule.setShiftGenerationDuration(-1);
            schedule.setMaxComputationTime(maxComputationTime);
            schedule.setMaximumUnimprovedSecondsSpent(maximumUnimprovedSecondsSpent);
            if (preservePreAssignedShifts != null) {
                schedule.setPreservePreAssignedShifts(preservePreAssignedShifts);
            }
            if (preservePostAssignedShifts != null) {
                schedule.setPreservePostAssignedShifts(preservePostAssignedShifts);
            }
            if (preserveEngineAssignedShifts != null) {
                schedule.setPreserveEngineAssignedShifts(preserveEngineAssignedShifts);
            }
            schedule.setCompletion(ScheduleCompletion.OK);
            schedule.setCompletionInfo(StringUtils.EMPTY);
            schedule.setState(TaskState.Starting);
            schedule.setScheduledTeamCount(schedule.getTeams() == null ? -1 : schedule.getTeams().size());
            schedule.setScheduledEmployeeCount(getEmployeeCount(schedule.getTenantId(), schedule.getId()));

            schedule = update(schedule);
            entityManager.flush();
            getEventService().sendEntityUpdateEvent(schedule, ScheduleDto.class);

            notifyProgress(schedule, "Starting Shift generation");
            logger.info(String.format("Schedule %s[scheduleId=%s] Starting Shift generation: ellapsed %s, took: %d sec",
                    schedule.getName(), schedule.getId(), watch.toString(),
                    (watch.getTime() - watch.getSplitTime()) / 1000));
            watch.split();

            // Generate shifts
            schedule = generateShifts(schedule, executionStartDate, mngrAccount);
            Collection<Shift> modelShifts = shiftService.getScheduleShifts(schedule);

            notifyProgress(schedule, "Shift generation complete: " + modelShifts.size() + " Shifts");
            logger.info(String.format("Schedule %s[scheduleId=%s] Shift generation complete: ellapsed %s, took: %d sec",
                    schedule.getName(), schedule.getId(), watch.toString(),
                    (watch.getTime() - watch.getSplitTime()) / 1000));
            watch.split();

            notifyProgress(schedule, "Starting Enriching Shift & generation Assignments... (" + modelShifts.size()
                    + " Shifts)");
            logger.info(String.format("Schedule %s[scheduleId=%s] Starting Enriching Shift & generation Assignments: " +
                            "ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), watch.toString(),
                    (watch.getTime() - watch.getSplitTime()) / 1000));
            watch.split();
            
            Site site = getSite(schedule);
            DateTimeZone siteTimeZone = site.getTimeZone();

            List<ShiftDto> shiftDtos = new ArrayList<>();
            List<ShiftAssignmentDto> shiftAssignmentDtos = new ArrayList<>();
            Map<String, Skill> requiredSkillsMap = new HashMap<>();

            long requestGenerationStartDateTime = System.currentTimeMillis();

            for (Shift shift : modelShifts) {
                ShiftDto shiftDto = new ShiftDto();
                shiftDto.setRequiredEmployeeSize(1);
                shiftDto.setTeamId(shift.getTeamId());
                shiftDto.setSkillId(shift.getSkillId());
                shiftDto.setId(shift.getId());
                shiftDto.setStartDateTime(new DateTime(shift.getStartDateTime()));
                shiftDto.setEndDateTime(new DateTime(shift.getEndDateTime()));
                shiftDto.setExcessShift(shift.isExcess());

                shiftDtos.add(shiftDto);

                if (shift.getEmployeeId() != null) {
                    ShiftAssignmentDto shiftAssignmentDto = shiftToShiftAssignmentDto(shift);
                    shiftAssignmentDtos.add(shiftAssignmentDto);
                }

                if (!requiredSkillsMap.containsKey(shift.getSkillId())) {
                    Skill skill = skillService.getSkill(new PrimaryKey(shift.getTenantId(), shift.getSkillId()));
                    requiredSkillsMap.put(shift.getSkillId(), skill);
                }
            }
            String requestId = UniqueId.getId();
            schedule.setRequestId(requestId);
            notifyProgress(schedule, "Shift & Assignments generation complete.");
            logger.info(String.format("Schedule %s[scheduleId=%s] Shift & Assignments generation complete: " +
                            "ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), watch.toString(),
                    (watch.getTime() - watch.getSplitTime()) / 1000));
            watch.split();

            logger.info(String.format("Schedule [scheduleId=%s] state set %s", schedule.getId(), schedule.getState()));

            Collection<Skill> requiredSkills = requiredSkillsMap.values();
            Collection<Team> teams = refineTeamsBySkills(schedule.getTeams(), requiredSkills);

            long employeeGenerationStartTime = System.currentTimeMillis();
            List<Employee> refinedEmployees = getModelEmployees(schedule.getTenantId(), schedule.getId(),
                    requiredSkills);
            schedule.setEmployeeGenerationDuration(System.currentTimeMillis() - employeeGenerationStartTime);

            long startDate = schedule.getStartDate();
			long endDate = schedule.getEndDate();
            String tenantId = schedule.getTenantId();
			Collection<CDAvailabilityTimeFrame> cdUnAvailTimeFrames =
                    getCdUnAvailTimeFrames(refinedEmployees, startDate, endDate, tenantId);
            Collection<CDAvailabilityTimeFrame> cdAvailTimeFrames =
                    getCdAvailTimeFrames(refinedEmployees, startDate, endDate, tenantId);
            Collection<CIAvailabilityTimeFrame> ciUnAvailTimeFrames =
                    getCiUnAvailTimeFrames(refinedEmployees, startDate, endDate, tenantId);
            
            Collection<CDAvailabilityTimeFrame> cdUnAvailPrefTimeFrames =
                    getCdUnAvailPreferenceTimeFrames(refinedEmployees, startDate, endDate, tenantId);
            Collection<CDAvailabilityTimeFrame> cdAvailPrefTimeFrames =
                    getCdAvailPreferenceTimeFrames(refinedEmployees, startDate, endDate, tenantId);
            Collection<CIAvailabilityTimeFrame> ciUnAvailPrefTimeFrames =
                    getCiUnAvailPreferenceTimeFrames(refinedEmployees, startDate, endDate, tenantId);
            Collection<CIAvailabilityTimeFrame> ciAvailPrefTimeFrames =
                    getCiAvailPreferenceTimeFrames(refinedEmployees, startDate, endDate, tenantId);

            notifyProgress(schedule, "Starting generation of " + refinedEmployees.size() + " Employees and "
                    + requiredSkills.size() + " Skills");
            logger.info(String.format("Schedule %s[scheduleId=%s] Starting generation of " + refinedEmployees.size()
                            + " Employees and " + requiredSkills.size() + " Skills: ellapsed %s, took: %d sec",
                    schedule.getName(), schedule.getId(), watch.toString(),
                    (watch.getTime() - watch.getSplitTime()) / 1000));
            watch.split();

            Map<String, List> assignMap = getPreassignedShiftsOtherTeams(schedule, refinedEmployees);
            List<ShiftAssignmentDto> preassignments = assignMap.get("shiftAssignmentDtos");
            List<ShiftDto> preassignedShifts = assignMap.get("shiftDtos");
            shiftAssignmentDtos.addAll(preassignments);
            shiftDtos.addAll(preassignedShifts);

            assignMap = getPreassignedShiftsFromContract(schedule, site, refinedEmployees);
            preassignments = assignMap.get("shiftAssignmentDtos");
            preassignedShifts = assignMap.get("shiftDtos");
            shiftAssignmentDtos.addAll(preassignments);
            shiftDtos.addAll(preassignedShifts);

            AssignmentRequestDto assignmentRequestDto = new AssignmentRequestDto();
            assignmentRequestDto.setMaxComputationTime(maxComputationTime);
            assignmentRequestDto.setMaximumUnimprovedSecondsSpent(maximumUnimprovedSecondsSpent);
            assignmentRequestDto.setConstraintOverrideDtos(getScheduleConstraintOverrideDtos(schedule, refinedEmployees, null));
            assignmentRequestDto.setEmployeeRosterDto(createEmployeeRosterDto(schedule));
            try {
                assignmentRequestDto.setEmployeeDtos(getEngineEmployeeDtos(refinedEmployees, schedule,
                        cdUnAvailTimeFrames, cdAvailTimeFrames, ciUnAvailTimeFrames, cdUnAvailPrefTimeFrames,
                        cdAvailPrefTimeFrames, ciUnAvailPrefTimeFrames, ciAvailPrefTimeFrames));
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new ValidationException(sessionService.getMessage("validation.employee.availability.error"));
            }
            assignmentRequestDto.setSkillDtos(getSkillDtos(requiredSkills));
            assignmentRequestDto.setEmployeeSkillDtos(getEmployeeSkillDtos(refinedEmployees, requiredSkills));
            assignmentRequestDto.setEmployeeTeamDtos(getEmployeeTeamDtos(teams));
            assignmentRequestDto.setContractDtos(getContractDtos(refinedEmployees));
            assignmentRequestDto.setShiftAssignmentDtos(shiftAssignmentDtos);
            assignmentRequestDto.setShiftDtos(shiftDtos);

            schedule.setRequestGenerationDuration(System.currentTimeMillis() - requestGenerationStartDateTime);
            schedule.setRequestSentDate(System.currentTimeMillis());
            schedule.setReturnedOpenShifts(-1);
            schedule.setReturnedAssignedShifts(-1);
            schedule.setState(TaskState.Queued);
            schedule = update(schedule);
            entityManager.flush();
            getEventService().sendEntityUpdateEvent(schedule, ScheduleDto.class);

            addTimeZoneToDateTimeFields(AssignmentRequestDto.class, assignmentRequestDto, siteTimeZone);

            logger.info(String.format("Schedule %s[scheduleId=%s] Employee & Skills generation complete: " +
                            "ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), watch.toString(),
                    (watch.getTime() - watch.getSplitTime()) / 1000));
            watch.split();

            notifyProgress(schedule, "Engine request generation complete. sending... ");
            logger.info(String.format("Schedule %s[scheduleId=%s] Engine request generation complete. sending: " +
                            "ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), watch.toString(),
                    (watch.getTime() - watch.getSplitTime()) / 1000));
            watch.split();
            hazelcastClientService.putInRequestDataMap(requestId, schedule.getId(), schedule.getName(), null,
                    assignmentRequestDto);

            EngineRequest engineRequest = buildNewEngineRequest(RequestType.Assignment, requestId, schedule);
            engineRequest.setIncludeDetailedResponse(true);
            hazelcastClientService.putEngineRequest(engineRequest, true);
            notifyProgress(schedule, "Engine request sent. ");
            logger.info(String.format("Schedule %s[scheduleId=%s] Engine request sent: ellapsed %s, took: %d sec",
                    schedule.getName(), schedule.getId(), watch.toString(),
                    (watch.getTime() - watch.getSplitTime()) / 1000));
            watch.split();
        } catch (Throwable t) {
            if (t instanceof ValidationException) {
                throw t;
            }
            schedule.setState(TaskState.Complete);
            schedule.setCompletion(ScheduleCompletion.Aborted);
            schedule.setCompletionInfo("Generation aborted because of an unexpected error");
            logger.error("execute Error",t);
        } finally {
            executeLock.unlock();
        }
        logger.info(String.format("Schedule %s[scheduleId=%s] Schedule execution launch complete: " +
                "ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), watch.toString(),
                (watch.getTime() - watch.getSplitTime()) / 1000));
        watch.split();

        return schedule;
    }

    // TODO Modified from a copy/paste of execute, so consider refactoring for reuse of shared logic.
	/**
	 * Transactional (TransactionAttributeType.REQUIRES_NEW) utility method to request qualification of shifts.
	 *  
     * @param maxComputationTime
     * @param maximumUnimprovedSecondsSpent
     * @param qualificationShifts
	 * @param requestId (optional... if null, it will be set automatically)
     * @param individualEmpConstraintOverrideOpts (optional... ignored if null)
     * @return
     * @throws IllegalAccessException
     */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public QualificationRequestTracker executeQualification(int maxComputationTime, int maximumUnimprovedSecondsSpent,
            Collection<Shift> qualificationShifts, String requestId, 
            Map<String, Map<ConstraintOverrideType, Boolean>> individualEmpConstraintOverrideOpts,
            boolean requestingAppServerHandlesResponse) throws IllegalAccessException {
		if (requestId == null) {
			requestId = UniqueId.getId();	
		}

		List<Employee> qualificationEmployees = new ArrayList<>();

		// TODO Are any employee handling performance optimizations from execute needed here?
		// Let's get the employees who are assigned to the shifts being qualified...
		String scheduleId = null;
		String tenantId = null;
		for (Shift shift : qualificationShifts) {
			if (shift.getEmployeeId() != null) {
				PrimaryKey empPrimaryKey = new PrimaryKey(shift.getTenantId(), shift.getEmployeeId());
				Employee employee = employeeService.getEmployee(empPrimaryKey);
				qualificationEmployees.add(employee);
			}
			if (scheduleId == null) {
				scheduleId = shift.getScheduleId();
				tenantId = shift.getTenantId();
			} 
		}
		Schedule schedule = getSchedule(new PrimaryKey(tenantId, scheduleId));

		long executionStartDate = System.currentTimeMillis();
		StopWatch watch = new StopWatch();
		watch.start();
		watch.split();
		logger.info(String.format("Schedule %s[scheduleId=%s] change Qualification [requestId=%s] Execution starting: ellapsed %s, took: %d sec",
				schedule.getName(), schedule.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
		notifyProgress(schedule, "Schedule change qualification execution starting...");

		Collection<EngineStatus> engineStatuses = hazelcastClientService.getEngines();
		if (engineStatuses == null || engineStatuses.size() == 0) {
			throw new ValidationException(sessionService.getMessage("validation.engine.unavailable"));
		}

		final RequestType requestType = RequestType.Qualification;
		QualificationRequestTracker requestTracker = new QualificationRequestTracker();
		requestTracker.setScheduleId(scheduleId);
		requestTracker.setRequestId(requestId);
		requestTracker.setExecutionStartDate(executionStartDate);
		requestTracker.setExecutionAckDate(0);
		requestTracker.setExecutionEndDate(0);
		requestTracker.setResponseReceivedDate(0);
		requestTracker.setRequestSentDate(0);
		requestTracker.setEmployeeGenerationDuration(-1);
		requestTracker.setResponseProcessingDuration(-1);
		requestTracker.setRequestGenerationDuration(-1);
		requestTracker.setShiftGenerationDuration(-1);
		requestTracker.setMaxComputationTime(maxComputationTime);
		requestTracker.setMaximumUnimprovedSecondsSpent(maximumUnimprovedSecondsSpent);
		requestTracker.setCompletion(ScheduleCompletion.OK);
		requestTracker.setCompletionInfo(StringUtils.EMPTY);
		requestTracker.setState(TaskState.Starting);
		requestTracker.setScheduledTeamCount(schedule.getTeams() == null ? -1 : schedule.getTeams().size());
		requestTracker.setScheduledEmployeeCount(getEmployeeCount(schedule.getTenantId(), schedule.getId()));

		// Let's get the existing employee(s) shifts to support the qualification...
		Collection<Shift> modelShifts = new ArrayList<Shift>();
		for (Employee employee : qualificationEmployees){
			modelShifts.addAll(shiftService.getEmployeeScheduleShifts(schedule, employee));
		}

		// For any shift(s) we're looking to qualify, let's make sure were giving the engine
		// the variant(s) we're wanting to qualify instead of variant(s) already existing...
		for (Shift shift : qualificationShifts) {
			if (modelShifts.contains(shift)) {
				modelShifts.remove(shift);  // removing the version obtained from database
			}
			modelShifts.add(shift);     // adding the version to be qualified
		}

		notifyProgress(schedule, "Starting Enriching Shift & generation Assignments (for schedule variant qualification)... (" + 
				modelShifts.size() + " Shifts)");
		logger.info(String.format("Schedule %s[scheduleId=%s] change Qualification [requestId=%s] Starting Enriching Shift & generation Assignments: " +
				"ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
		watch.split();
		List<ShiftDto> shiftDtos = new ArrayList<>();
		List<ShiftAssignmentDto> shiftAssignmentDtos = new ArrayList<>();
		Map<String, Skill> requiredSkillsMap = new HashMap<>();

		long requestGenerationStartDateTime = System.currentTimeMillis();
		
        Site site = getSite(schedule);
        DateTimeZone siteTimeZone = site.getTimeZone();
        
		for (Shift shift : modelShifts) {
			ShiftDto shiftDto = new ShiftDto();
			shiftDto.setEndDateTime(new DateTime(shift.getEndDateTime()));
			shiftDto.setStartDateTime(new DateTime(shift.getStartDateTime()));
			shiftDto.setRequiredEmployeeSize(1);
			shiftDto.setTeamId(shift.getTeamId());
			shiftDto.setSkillId(shift.getSkillId());
			shiftDto.setId(shift.getId());
			if (qualificationShifts.contains(shift)){
				shiftDto.setBeingQualified(true);
			} else {
				shiftDto.setBeingQualified(false);
			}

			shiftDtos.add(shiftDto);

			if (shift.getEmployeeId() != null) {
				ShiftAssignmentDto shiftAssignmentDto = shiftToShiftAssignmentDto(shift);
				shiftAssignmentDtos.add(shiftAssignmentDto);
			}

			if (!requiredSkillsMap.containsKey(shift.getSkillId())) {
				Skill skill = skillService.getSkill(new PrimaryKey(shift.getTenantId(), shift.getSkillId()));
				requiredSkillsMap.put(shift.getSkillId(), skill);
			}
		}

		notifyProgress(schedule, "Shift & Assignments generation (for schedule variant qualification) complete.");
		logger.info(String.format("Schedule %s[scheduleId=%s] Shift & Assignments generation complete for schedule change Qualification [requestId=%s]: " +
				"ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
		watch.split();

		Collection<Skill> requiredSkills = requiredSkillsMap.values();
		Collection<Team> teams = refineTeamsBySkills(schedule.getTeams(), requiredSkills);

		Collection<CDAvailabilityTimeFrame> cdUnAvailTimeFrames = getCdUnAvailTimeFrames(qualificationEmployees,
				schedule.getStartDate(), schedule.getEndDate(), schedule.getTenantId());
		Collection<CDAvailabilityTimeFrame> cdAvailTimeFrames = getCdAvailTimeFrames(qualificationEmployees,
				schedule.getStartDate(), schedule.getEndDate(), schedule.getTenantId());
		Collection<CIAvailabilityTimeFrame> ciUnAvailTimeFrames = getCiUnAvailTimeFrames(qualificationEmployees,
				schedule.getStartDate(), schedule.getEndDate(), schedule.getTenantId());

		// TODO Confirm Employee Preference should not be included in qualification request since it is a soft constraint.
		// Just using empty collections since Employee Preference is not needed for qualification... 
        Collection<CDAvailabilityTimeFrame> cdUnAvailPrefTimeFrames = new ArrayList<>();
        Collection<CDAvailabilityTimeFrame> cdAvailPrefTimeFrames   = new ArrayList<>();
        Collection<CIAvailabilityTimeFrame> ciUnAvailPrefTimeFrames = new ArrayList<>();
        Collection<CIAvailabilityTimeFrame> ciAvailPrefTimeFrames   = new ArrayList<>();

		notifyProgress(schedule, "Starting generation of " + qualificationEmployees.size() + " Employees and "
				+ requiredSkills.size() + " Skills" + "for schedule (variant) qualification");
		logger.info(String.format("Schedule %s[scheduleId=%s] change Qualification [requestId=%s] starting generation of " + qualificationEmployees.size()
				+ " Employees and " + requiredSkills.size() + " Skills: ellapsed %s, took: %d sec",
				schedule.getName(), schedule.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
		watch.split();
		AssignmentRequestDto assignmentRequestDto = new AssignmentRequestDto();
		assignmentRequestDto.setMaxComputationTime(maxComputationTime);
		assignmentRequestDto.setMaximumUnimprovedSecondsSpent(maximumUnimprovedSecondsSpent);

		List<ConstraintOverrideDto> constraintOverrideDtos = new ArrayList<>();
		Set<String> constraintOverriddenEmployeeIds = new HashSet<>();
		if (individualEmpConstraintOverrideOpts != null && individualEmpConstraintOverrideOpts.size() > 0) {
			constraintOverriddenEmployeeIds = individualEmpConstraintOverrideOpts.keySet();			
			List<ConstraintOverrideDto> altEmployeeConstraintOverrideDtos =
                    getEmployeeConstraintOverrideDtos(individualEmpConstraintOverrideOpts);
			constraintOverrideDtos.addAll(altEmployeeConstraintOverrideDtos);
		}
		List<ConstraintOverrideDto> schedConstraintOverrideDtos = getScheduleConstraintOverrideDtos(schedule,
                qualificationEmployees, constraintOverriddenEmployeeIds);
		constraintOverrideDtos.addAll(schedConstraintOverrideDtos);
		assignmentRequestDto.setConstraintOverrideDtos(constraintOverrideDtos);
		
		assignmentRequestDto.setEmployeeRosterDto(createEmployeeRosterDto(schedule));
		
		try {
            assignmentRequestDto.setEmployeeDtos(getEngineEmployeeDtos(qualificationEmployees, schedule, // throw exception
                    cdUnAvailTimeFrames, cdAvailTimeFrames, ciUnAvailTimeFrames, cdUnAvailPrefTimeFrames,
                    cdAvailPrefTimeFrames, ciUnAvailPrefTimeFrames, ciAvailPrefTimeFrames));
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new ValidationException(sessionService.getMessage("validation.employee.availability.error"));
		}

        Map<String, List> assignMap = getPreassignedShiftsOtherTeams(schedule, qualificationEmployees);
        List<ShiftAssignmentDto> preassignments = assignMap.get("shiftAssignmentDtos");
        List<ShiftDto> preassignedShifts = assignMap.get("shiftDtos");
        shiftAssignmentDtos.addAll(preassignments);
        shiftDtos.addAll(preassignedShifts);

        assignmentRequestDto.setSkillDtos(getSkillDtos(requiredSkills));
		assignmentRequestDto.setEmployeeSkillDtos(getEmployeeSkillDtos(qualificationEmployees, requiredSkills));
		assignmentRequestDto.setEmployeeTeamDtos(getEmployeeTeamDtos(teams));
		assignmentRequestDto.setContractDtos(getContractDtos(qualificationEmployees));
		assignmentRequestDto.setShiftAssignmentDtos(shiftAssignmentDtos);
		assignmentRequestDto.setShiftDtos(shiftDtos);

        addTimeZoneToDateTimeFields(AssignmentRequestDto.class, assignmentRequestDto, siteTimeZone);

        // Put updated QualificationRequestTrackerDto in QualificationTrackingMap...
		requestTracker.setRequestGenerationDuration(System.currentTimeMillis() - requestGenerationStartDateTime);
		requestTracker.setRequestSentDate(System.currentTimeMillis());
		requestTracker.setReturnedOpenShifts(-1);
		requestTracker.setReturnedAssignedShifts(-1);
		requestTracker.setState(TaskState.Queued);
		hazelcastClientService.putQualificationRequestTracker(requestId, requestTracker);

		notifyProgress(schedule, "Generation of engine request for qualification complete. sending... ");
		logger.info(String.format("Schedule %s[scheduleId=%s] change Qualification [requestId=%s] Engine request generation complete. sending: " +
				"ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
		watch.split();

		// TODO Change the 4th arg in following call to putInRequestDataMap to reflect the appServerId.  
		hazelcastClientService.putInRequestDataMap(requestId, schedule.getId(), schedule.getName(), null,
                assignmentRequestDto);

		EngineRequest engineRequest = buildNewEngineRequest(requestType, requestId, schedule);
		hazelcastClientService.putEngineRequest(engineRequest, !requestingAppServerHandlesResponse);
		notifyProgress(schedule, "Engine qualification request sent. ");
		logger.info(String.format("Schedule %s[scheduleId=%s] Engine Qualification [requestId=%s] request sent: ellapsed %s, took: %d sec",
				schedule.getName(), schedule.getId(), watch.toString(), requestId, 
				(watch.getTime() - watch.getSplitTime()) / 1000));
		watch.split();
		logger.info(String.format("Schedule %s[scheduleId=%s] change Qualification [requestId=%s] execution launch complete: " +
				"ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));

		entityManager.clear();  // to avoid unwanted Transparent Persistence
		return requestTracker;
	}
	
    
	// TODO Modified from a copy/paste of execute & executeQualification, so consider refactoring for reuse of shared logic.
	/**
	 * Transactional (TransactionAttributeType.REQUIRES_NEW) utility method to request open shift eligibility of shift/employee combinations
	 * 
	 * @param schedulePk
	 * @param maxComputationTime
	 * @param maximumUnimprovedSecondsSpent
	 * @param employeePks
	 * @param shifts
	 * @param requestId (optional... if null, it will be set automatically)
	 * @param includeDetails
	 * @param sharedEmpConstraintOverrideOptions (optional... ignored if null)
	 * @param individualEmpConstraintOverrideOpts (optional... ignored if null)
	 * @return
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public QualificationRequestTracker executeOpenShiftEligibility(PrimaryKey schedulePk, int maxComputationTime,
            int maximumUnimprovedSecondsSpent, Collection<PrimaryKey> employeePks, Collection<Shift> shifts,
            String requestId, Boolean includeDetails, Map<ConstraintOverrideType, Boolean> sharedEmpConstraintOverrideOptions, 
            Map<String, Map<ConstraintOverrideType, Boolean>> individualEmpConstraintOverrideOpts,
            boolean requestingAppServerHandlesResponse) throws IllegalAccessException {
		if (requestId == null){
			requestId = UniqueId.getId();	
		}		
		String scheduleId = schedulePk.getId();
		Schedule schedule = getSchedule(new PrimaryKey(schedulePk.getTenantId(), scheduleId));

		// Consider all scheduled team's employees if none were specified... 
		List<Employee> employees;
		if (employeePks == null || employeePks.isEmpty()){
			// Use query for efficiency and filtering out duplicates
            ResultSet<Employee> rs = getEmployees(schedulePk, "", 0, -1, null, null);
            employees = new ArrayList<>(rs.getResult());
			
			// Since employees weren't specified, the ones we'll consider instead should use 
			// the shared employee constraint override options (if there are any), which we'll
			// go ahead and make look as if specified for each employee for downstream processing...
			if (sharedEmpConstraintOverrideOptions != null) {
				if (individualEmpConstraintOverrideOpts == null) {
					individualEmpConstraintOverrideOpts = new HashMap<>();
				}
				for (Employee employee : employees) {
					if (!individualEmpConstraintOverrideOpts.containsKey(employee.getId())) {
						individualEmpConstraintOverrideOpts.put(employee.getId(), sharedEmpConstraintOverrideOptions);
					}
				}
			}
		} else {
			employees = new ArrayList<>();
			for (PrimaryKey employeePk : employeePks) {
				employees.add(employeeService.getEmployee(employeePk));
			}			
		}

		// Consider all of schedules open shifts if none were specified...
		if (shifts == null || shifts.isEmpty()){
			shifts = shiftService.getScheduleOpenShifts(schedule);
		}
	
		long executionStartDate = System.currentTimeMillis();
		StopWatch watch = new StopWatch();
		watch.start();
		watch.split();
		logger.info(String.format("Schedule %s[scheduleId=%s] Open SHift Eligibility [requestId=%s] " +
                "Execution starting: ellapsed %s, took: %d sec",
				schedule.getName(), schedulePk.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
		notifyProgress(schedule, "Schedule eligibility execution starting...");
	
		Collection<EngineStatus> engineStatuses = hazelcastClientService.getEngines();
		if (engineStatuses == null || engineStatuses.size() == 0) {
			throw new ValidationException(sessionService.getMessage("validation.engine.unavailable"));
		}
	
		final RequestType requestType = RequestType.OpenShiftEligibility;
		QualificationRequestTracker requestTracker = new QualificationRequestTracker();
		requestTracker.setScheduleId(scheduleId);
		requestTracker.setRequestId(requestId);
		requestTracker.setExecutionStartDate(executionStartDate);
		requestTracker.setExecutionAckDate(0);
		requestTracker.setExecutionEndDate(0);
		requestTracker.setResponseReceivedDate(0);
		requestTracker.setRequestSentDate(0);
		requestTracker.setEmployeeGenerationDuration(-1);
		requestTracker.setResponseProcessingDuration(-1);
		requestTracker.setRequestGenerationDuration(-1);
		requestTracker.setShiftGenerationDuration(-1);
		requestTracker.setMaxComputationTime(maxComputationTime);
		requestTracker.setMaximumUnimprovedSecondsSpent(maximumUnimprovedSecondsSpent);
		requestTracker.setCompletion(ScheduleCompletion.OK);
		requestTracker.setCompletionInfo(StringUtils.EMPTY);
		requestTracker.setState(TaskState.Starting);
		requestTracker.setScheduledTeamCount(schedule.getTeams() == null ? -1 : schedule.getTeams().size());
		requestTracker.setScheduledEmployeeCount(getEmployeeCount(schedule.getTenantId(), schedule.getId()));

		// Let's get the existing employee(s) shifts to support the eligibility execution...
		Collection<Shift> modelShifts = new ArrayList<>();
		for (Employee employee : employees) {
			modelShifts.addAll(shiftService.getEmployeeScheduleShifts(schedule, employee));
		}
	
		// Let's add the shift(s) for which we're trying to determine eligibility...
		for (Shift shift : shifts) {
			modelShifts.add(shift);
		}
	
		notifyProgress(schedule, "Starting Enriching Shift & generation Assignments (for open shift elligibility)... (" + 
				modelShifts.size() + " Shifts)");
		logger.info(String.format("Schedule %s[scheduleId=%s] Open Shift Eligibility [requestId=%s] Starting Enriching Shift & generation Assignments: " +
				"ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
		watch.split();
		List<ShiftDto> shiftDtos = new ArrayList<>();
		List<ShiftAssignmentDto> shiftAssignmentDtos = new ArrayList<>();
		Map<String, Skill> requiredSkillsMap = new HashMap<>();
	
		long requestGenerationStartDateTime = System.currentTimeMillis();
        
		Site site = getSite(schedule);
        DateTimeZone siteTimeZone = site.getTimeZone();
		
        for (Shift shift : modelShifts) {
			ShiftDto shiftDto = new ShiftDto();
			shiftDto.setEndDateTime(new DateTime(shift.getEndDateTime()));
			shiftDto.setStartDateTime(new DateTime(shift.getStartDateTime()));
			shiftDto.setRequiredEmployeeSize(1);
			shiftDto.setTeamId(shift.getTeamId());
			shiftDto.setSkillId(shift.getSkillId());
			shiftDto.setId(shift.getId());
			if (shifts.contains(shift)){
				shiftDto.setBeingQualified(true);
				// Note that since it is being qualified, we want the engine to treat it as
				// if it were an open shift, so we won't send an associated shift assignment.
			} else {
				shiftDto.setBeingQualified(false);
				if (shift.getEmployeeId() != null) {
					ShiftAssignmentDto shiftAssignmentDto = shiftToShiftAssignmentDto(shift);
					shiftAssignmentDtos.add(shiftAssignmentDto);
				}
			}
	
			shiftDtos.add(shiftDto);
	
			if (!requiredSkillsMap.containsKey(shift.getSkillId())) {
				Skill skill = skillService.getSkill(new PrimaryKey(shift.getTenantId(), shift.getSkillId()));
				requiredSkillsMap.put(shift.getSkillId(), skill);
			}
		}
	
		notifyProgress(schedule, "Shift & Assignments generation (for open shift eligibility) complete.");
		logger.info(String.format("Schedule %s[scheduleId=%s] Shift & Assignments generation complete for open shift Eligibility [requestId=%s]: " +
				"ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
		watch.split();
	
		Collection<Skill> requiredSkills = requiredSkillsMap.values();
		Collection<Team> teams = refineTeamsBySkills(schedule.getTeams(), requiredSkills);
	
		Collection<CDAvailabilityTimeFrame> cdUnAvailTimeFrames =
                getCdUnAvailTimeFrames(employees, schedule.getStartDate(), schedule.getEndDate(), schedule.getTenantId());
		Collection<CDAvailabilityTimeFrame> cdAvailTimeFrames =
                getCdAvailTimeFrames(employees, schedule.getStartDate(), schedule.getEndDate(), schedule.getTenantId());
		Collection<CIAvailabilityTimeFrame> ciUnAvailTimeFrames =
                getCiUnAvailTimeFrames(employees, schedule.getStartDate(), schedule.getEndDate(), schedule.getTenantId());

		// TODO Confirm Employee Preference need not be included in eligibility request since it is a soft constraint. 
		// Just using empty collections since Employee Preference is not needed for eligibility... 
        Collection<CDAvailabilityTimeFrame> cdUnAvailPrefTimeFrames = new ArrayList<>();
        Collection<CDAvailabilityTimeFrame> cdAvailPrefTimeFrames   = new ArrayList<>();
        Collection<CIAvailabilityTimeFrame> ciUnAvailPrefTimeFrames = new ArrayList<>();
        Collection<CIAvailabilityTimeFrame> ciAvailPrefTimeFrames   = new ArrayList<>();
	
		notifyProgress(schedule, "Starting generation of " + employees.size() + " Employees and "
				+ requiredSkills.size() + " Skills" + "for shift eligibility");
		logger.info(String.format("Schedule %s[scheduleId=%s] open shift eligibility [requestId=%s] starting generation of " + employees.size()
				+ " Employees and " + requiredSkills.size() + " Skills: ellapsed %s, took: %d sec",
				schedule.getName(), schedule.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
		watch.split();
		AssignmentRequestDto assignmentRequestDto = new AssignmentRequestDto();
		assignmentRequestDto.setMaxComputationTime(maxComputationTime);
		assignmentRequestDto.setMaximumUnimprovedSecondsSpent(maximumUnimprovedSecondsSpent);

		List<ConstraintOverrideDto> constraintOverrideDtos = new ArrayList<>();
		Set<String> constraintOverriddenEmployeeIds = new HashSet<>();
		if (individualEmpConstraintOverrideOpts != null && individualEmpConstraintOverrideOpts.size() > 0){
			constraintOverriddenEmployeeIds = individualEmpConstraintOverrideOpts.keySet();			
			List<ConstraintOverrideDto> altEmployeeConstraintOverrideDtos =
                    getEmployeeConstraintOverrideDtos(individualEmpConstraintOverrideOpts);
			constraintOverrideDtos.addAll(altEmployeeConstraintOverrideDtos);
		}
		List<ConstraintOverrideDto> schedConstraintOverrideDtos = getScheduleConstraintOverrideDtos(schedule, employees,
                constraintOverriddenEmployeeIds);
		constraintOverrideDtos.addAll(schedConstraintOverrideDtos);
		assignmentRequestDto.setConstraintOverrideDtos(constraintOverrideDtos);
		assignmentRequestDto.setEmployeeRosterDto(createEmployeeRosterDto(schedule));
		
		try {
            assignmentRequestDto.setEmployeeDtos(getEngineEmployeeDtos(employees, schedule, // throw exception
                    cdUnAvailTimeFrames, cdAvailTimeFrames, ciUnAvailTimeFrames, cdUnAvailPrefTimeFrames,
                    cdAvailPrefTimeFrames, ciUnAvailPrefTimeFrames, ciAvailPrefTimeFrames));
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new ValidationException(sessionService.getMessage("validation.employee.availability.error"));
		}
		
		assignmentRequestDto.setSkillDtos(getSkillDtos(requiredSkills));
		assignmentRequestDto.setEmployeeSkillDtos(getEmployeeSkillDtos(employees, requiredSkills));
		assignmentRequestDto.setEmployeeTeamDtos(getEmployeeTeamDtos(teams));
		assignmentRequestDto.setContractDtos(getContractDtos(employees));
		assignmentRequestDto.setShiftAssignmentDtos(shiftAssignmentDtos);
		assignmentRequestDto.setShiftDtos(shiftDtos);
	
		// Put updated EligibilityRequestTracker in EligibilityTrackingMap...
		requestTracker.setRequestGenerationDuration(System.currentTimeMillis() - requestGenerationStartDateTime);
		requestTracker.setRequestSentDate(System.currentTimeMillis());
		requestTracker.setReturnedOpenShifts(-1);
		requestTracker.setReturnedAssignedShifts(-1);
		requestTracker.setState(TaskState.Queued);
		hazelcastClientService.putQualificationRequestTracker(requestId, requestTracker);

        addTimeZoneToDateTimeFields(AssignmentRequestDto.class, assignmentRequestDto, siteTimeZone);

		notifyProgress(schedule, "Generation of engine request for open shift eligibility complete. sending... ");
		logger.info(String.format("Schedule %s[scheduleId=%s] open shift Eligibility [requestId=%s] Engine request generation complete. sending: " +
				"ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
		watch.split();
	
		// TODO Change the 4th arg in following call to putInRequestDataMap to reflect the appServerId.  
		hazelcastClientService.putInRequestDataMap(requestId, schedule.getId(), schedule.getName(), null,
                assignmentRequestDto);
	
		EngineRequest engineRequest = buildNewEngineRequest(requestType, requestId, schedule);
		if (includeDetails != null && includeDetails) {
			engineRequest.setIncludeDetailedResponse(true);
		}
		hazelcastClientService.putEngineRequest(engineRequest, !requestingAppServerHandlesResponse);
		notifyProgress(schedule, "Engine shift eligibility request sent. ");
		logger.info(String.format("Schedule %s[scheduleId=%s] Engine Open Shift Eligibility [requestId=%s] request sent: ellapsed %s, took: %d sec",
				schedule.getName(), schedule.getId(), watch.toString(), requestId, 
				(watch.getTime() - watch.getSplitTime()) / 1000));
		watch.split();
		logger.info(String.format("Schedule %s[scheduleId=%s] open shift Eligibility [requestId=%s] execution launch complete: " +
				"ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
	
		entityManager.clear();  // to avoid unwanted Transparent Persistence
		return requestTracker;
	}

	// TODO Modified from a copy/paste of execute & executeQualification, so consider refactoring for reuse of shared logic.
	/**
	 * Transactional (TransactionAttributeType.REQUIRES_NEW) utility method to request shift swap eligibility of shift/employee combinations
	 * 
	 * @param schedulePk
	 * @param maxComputationTime
	 * @param maximumUnimprovedSecondsSpent
	 * @param swapSeekingShifts
	 * @param swapCandidateShifts
	 * @param requestId (optional... if null, it will be set automatically)
	 * @param includeDetails
	 * @return
	 * @throws IllegalAccessException
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ShiftSwapEligibilityRequestTracker executeShiftSwapEligibility(
	        PrimaryKey schedulePk, int maxComputationTime, int maximumUnimprovedSecondsSpent,
	        Collection<Shift> swapSeekingShifts, Collection<Shift> swapCandidateShifts, String requestId,
	        Boolean includeDetails, boolean requestingAppServerHandlesResponse) throws IllegalAccessException {
		if (requestId == null){
			requestId = UniqueId.getId();	
		}		
		String scheduleId = schedulePk.getId();
		Schedule schedule = getSchedule(new PrimaryKey(schedulePk.getTenantId(), scheduleId));
	
		Set<Employee> employeeSet = new HashSet<>();
		
		if (swapSeekingShifts == null || swapSeekingShifts.isEmpty()){
			// TODO better message for this exception...
			throw new ValidationException(sessionService.getMessage("Invalid argument... "
					+ "swapSeekingShifts collection cannot be empty"));
		} else {
			for (Shift swapSeekingShift : swapSeekingShifts){
				if (swapSeekingShift.getAssigned()           == null ||
						swapSeekingShift.getAssignmentType() == null ||
						swapSeekingShift.getEmployeeId()     == null ||
						swapSeekingShift.getEmployeeName()   == null    ){
					// TODO better message for this exception...
					throw new ValidationException(sessionService.getMessage("Invalid argument... "
							+ "swapSeekingShifts collection must all be assigned"));				
				}
				
				PrimaryKey empPrimaryKey = new PrimaryKey(schedule.getTenantId(), swapSeekingShift.getEmployeeId());
				Employee employee = employeeService.getEmployee(empPrimaryKey);
				employeeSet.add(employee);
			}
		}

		// Consider all other assigned shifts as candidates if none were specified...
		if (swapCandidateShifts == null || swapCandidateShifts.isEmpty()){
			Collection<Shift> nonCandidateShifts = new ArrayList<Shift>();
			swapCandidateShifts = shiftService.getScheduleAssignedShifts(schedule);
			for (Shift swapCandidateShift : swapCandidateShifts){
				if (swapSeekingShifts.contains(swapCandidateShift)){
					nonCandidateShifts.add(swapCandidateShift);
				}
			}
			for (Shift nonCandidateShift : nonCandidateShifts){
				swapCandidateShifts.remove(nonCandidateShift);
			}
		}
		
		for (Shift swapCandidateShift : swapCandidateShifts){
			PrimaryKey empPrimaryKey = new PrimaryKey(schedule.getTenantId(), swapCandidateShift.getEmployeeId());
			Employee employee = employeeService.getEmployee(empPrimaryKey);
			employeeSet.add(employee);
		}

		List<Employee> employees = new ArrayList<Employee>();
		employees.addAll(employeeSet);		

		long executionStartDate = System.currentTimeMillis();
		StopWatch watch = new StopWatch();
		watch.start();
		watch.split();
		logger.info(String.format("Schedule %s[scheduleId=%s] Shift Swap Eligibility [requestId=%s] Execution starting: ellapsed %s, took: %d sec",
				schedule.getName(), schedulePk.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
		notifyProgress(schedule, "Schedule shift swap eligibility execution starting...");
	
		Collection<EngineStatus> engineStatuses = hazelcastClientService.getEngines();
		if (engineStatuses == null || engineStatuses.size() == 0) {
			throw new ValidationException(sessionService.getMessage("validation.engine.unavailable"));
		}
	
		final RequestType requestType = RequestType.ShiftSwapEligibility;
		ShiftSwapEligibilityRequestTracker requestTracker = new ShiftSwapEligibilityRequestTracker();
		requestTracker.setScheduleId(scheduleId);
		requestTracker.setRequestId(requestId);
		requestTracker.setExecutionStartDate(executionStartDate);
		requestTracker.setExecutionAckDate(0);
		requestTracker.setExecutionEndDate(0);
		requestTracker.setResponseReceivedDate(0);
		requestTracker.setRequestSentDate(0);
		requestTracker.setEmployeeGenerationDuration(-1);
		requestTracker.setResponseProcessingDuration(-1);
		requestTracker.setRequestGenerationDuration(-1);
		requestTracker.setShiftGenerationDuration(-1);
		requestTracker.setMaxComputationTime(maxComputationTime);
		requestTracker.setMaximumUnimprovedSecondsSpent(maximumUnimprovedSecondsSpent);
		requestTracker.setCompletion(ScheduleCompletion.OK);
		requestTracker.setCompletionInfo(StringUtils.EMPTY);
		requestTracker.setState(TaskState.Starting);
		requestTracker.setScheduledTeamCount(schedule.getTeams() == null ? -1 : schedule.getTeams().size());
		requestTracker.setScheduledEmployeeCount(getEmployeeCount(schedule.getTenantId(), schedule.getId()));

		// Let's get the existing employee(s) shifts to support the eligibility execution...
		Set<Shift> modelShifts = new HashSet<Shift>();
		for (Employee employee : employees){
			modelShifts.addAll(shiftService.getEmployeeScheduleShifts(schedule, employee));
		}
	
		for (Shift swapSeekingShift : swapSeekingShifts){
			modelShifts.add(swapSeekingShift);
		}
	
		for (Shift swapCandidateShift : swapCandidateShifts){
			modelShifts.add(swapCandidateShift);
		}
	
		notifyProgress(schedule, "Starting Enriching Shift & generation Assignments (for shift swap elligibility)... (" + 
				modelShifts.size() + " Shifts)");
		logger.info(String.format("Schedule %s[scheduleId=%s] Shift Swap Eligibility [requestId=%s] Starting Enriching Shift & generation Assignments: " +
				"ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
		watch.split();
		List<ShiftDto> shiftDtos = new ArrayList<>();
		List<ShiftAssignmentDto> shiftAssignmentDtos = new ArrayList<>();
		Map<String, Skill> requiredSkillsMap = new HashMap<>();
	
		long requestGenerationStartDateTime = System.currentTimeMillis();
	
	    Site site = getSite(schedule);
        DateTimeZone siteTimeZone = site.getTimeZone();
        
		for (Shift shift : modelShifts) {
			ShiftDto shiftDto = shiftToShiftDto(shift);
			
			if (swapSeekingShifts.contains(shift)) {
				shiftDto.setBeingSwapped(true);
			} else if (swapCandidateShifts.contains(shift)) {
				shiftDto.setBeingQualified(true);
			}
			
			if (shift.getEmployeeId() != null) {
				ShiftAssignmentDto shiftAssignmentDto = shiftToShiftAssignmentDto(shift);
				shiftAssignmentDtos.add(shiftAssignmentDto);
			}
	
			shiftDtos.add(shiftDto);

			if (!requiredSkillsMap.containsKey(shift.getSkillId())) {
				Skill skill = skillService.getSkill(new PrimaryKey(shift.getTenantId(), shift.getSkillId()));
				requiredSkillsMap.put(shift.getSkillId(), skill);
			}
		}
	
		notifyProgress(schedule, "Shift & Assignments generation (for shift swap eligibility) complete.");
		logger.info(String.format("Schedule %s[scheduleId=%s] Shift & Assignments generation complete for shift swap Eligibility [requestId=%s]: " +
				"ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
		watch.split();
	
		Collection<Skill> requiredSkills = requiredSkillsMap.values();
		Collection<Team> teams = refineTeamsBySkills(schedule.getTeams(), requiredSkills);
	
		Collection<CDAvailabilityTimeFrame> cdUnAvailTimeFrames = 
				getCdUnAvailTimeFrames(employees, schedule.getStartDate(), schedule.getEndDate(), schedule.getTenantId());
		Collection<CDAvailabilityTimeFrame> cdAvailTimeFrames = 
				getCdAvailTimeFrames(employees, schedule.getStartDate(), schedule.getEndDate(), schedule.getTenantId());
		Collection<CIAvailabilityTimeFrame> ciUnAvailTimeFrames = 
				getCiUnAvailTimeFrames(employees, schedule.getStartDate(), schedule.getEndDate(), schedule.getTenantId());

		// TODO Confirm Employee Preference need not be included in eligibility request since it is a soft constraint. 
		// Just using empty collections since Employee Preference is not needed for eligibility... 
        Collection<CDAvailabilityTimeFrame> cdUnAvailPrefTimeFrames = new ArrayList<>();
        Collection<CDAvailabilityTimeFrame> cdAvailPrefTimeFrames   = new ArrayList<>();
        Collection<CIAvailabilityTimeFrame> ciUnAvailPrefTimeFrames = new ArrayList<>();
        Collection<CIAvailabilityTimeFrame> ciAvailPrefTimeFrames   = new ArrayList<>();
	
		notifyProgress(schedule, "Starting generation of " + employees.size() + " Employees and "
				+ requiredSkills.size() + " Skills" + "for shift eligibility");
		logger.info(String.format("Schedule %s[scheduleId=%s] shift swap eligibility [requestId=%s] starting generation of " + employees.size()
				+ " Employees and " + requiredSkills.size() + " Skills: ellapsed %s, took: %d sec",
				schedule.getName(), schedule.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
		watch.split();
		AssignmentRequestDto assignmentRequestDto = new AssignmentRequestDto();
		assignmentRequestDto.setMaxComputationTime(maxComputationTime);
		assignmentRequestDto.setMaximumUnimprovedSecondsSpent(maximumUnimprovedSecondsSpent);
		assignmentRequestDto.setConstraintOverrideDtos(getScheduleConstraintOverrideDtos(schedule, employees, null));
		assignmentRequestDto.setEmployeeRosterDto(createEmployeeRosterDto(schedule));
		
		try {
            assignmentRequestDto.setEmployeeDtos(getEngineEmployeeDtos(employees, schedule, // throw exception
                    cdUnAvailTimeFrames, cdAvailTimeFrames, ciUnAvailTimeFrames, cdUnAvailPrefTimeFrames,
                    cdAvailPrefTimeFrames, ciUnAvailPrefTimeFrames, ciAvailPrefTimeFrames));
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new ValidationException(sessionService.getMessage("validation.employee.availability.error"));
		}
		
		assignmentRequestDto.setSkillDtos(getSkillDtos(requiredSkills));
		assignmentRequestDto.setEmployeeSkillDtos(getEmployeeSkillDtos(employees, requiredSkills));
		assignmentRequestDto.setEmployeeTeamDtos(getEmployeeTeamDtos(teams));
		assignmentRequestDto.setContractDtos(getContractDtos(employees));
		assignmentRequestDto.setShiftAssignmentDtos(shiftAssignmentDtos);
		assignmentRequestDto.setShiftDtos(shiftDtos);
	
		// Put updated EligibilityRequestTracker in EligibilityTrackingMap...
		requestTracker.setRequestGenerationDuration(System.currentTimeMillis() - requestGenerationStartDateTime);
		requestTracker.setRequestSentDate(System.currentTimeMillis());
		requestTracker.setReturnedOpenShifts(-1);
		requestTracker.setReturnedAssignedShifts(-1);
		requestTracker.setState(TaskState.Queued);
		hazelcastClientService.putShiftSwapEligibilityRequestTracker(requestId, requestTracker);
	
	    addTimeZoneToDateTimeFields(AssignmentRequestDto.class, assignmentRequestDto, siteTimeZone);
	
		notifyProgress(schedule, "Generation of engine request for shift swap eligibility complete. sending... ");
		logger.info(String.format("Schedule %s[scheduleId=%s] shift swap Eligibility [requestId=%s] Engine request generation complete. sending: " +
				"ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
		watch.split();
	
		// TODO Change the 4th arg in following call to putInRequestDataMap to reflect the appServerId.  
		hazelcastClientService.putInRequestDataMap(requestId, schedule.getId(), schedule.getName(), null, assignmentRequestDto);
	
		EngineRequest engineRequest = buildNewEngineRequest(requestType, requestId, schedule);
		if (includeDetails != null && includeDetails) {
			engineRequest.setIncludeDetailedResponse(true);
		}
		hazelcastClientService.putEngineRequest(engineRequest, !requestingAppServerHandlesResponse);
		notifyProgress(schedule, "Engine shift swap eligibility request sent. ");
		logger.info(String.format("Schedule %s[scheduleId=%s] Engine Shift Swap Eligibility [requestId=%s] request sent: ellapsed %s, took: %d sec",
				schedule.getName(), schedule.getId(), watch.toString(), requestId, 
				(watch.getTime() - watch.getSplitTime()) / 1000));
		watch.split();
		logger.info(String.format("Schedule %s[scheduleId=%s] shift swap Eligibility [requestId=%s] execution launch complete: " +
				"ellapsed %s, took: %d sec", schedule.getName(), schedule.getId(), requestId, watch.toString(),
				(watch.getTime() - watch.getSplitTime()) / 1000));
	
		entityManager.clear();  // to avoid unwanted Transparent Persistence
		return requestTracker;
	}

	private List<ConstraintOverrideDto> getEmployeeConstraintOverrideDtos(Map<String, Map<ConstraintOverrideType,
            Boolean>> altEmpOverrideOpts) {
		List<ConstraintOverrideDto> altConstraintOverrideDtos = new ArrayList<>();
		for (String empId : altEmpOverrideOpts.keySet()){
			Map<ConstraintOverrideType, Boolean> empOverrides = altEmpOverrideOpts.get(empId);
			Set<ConstraintOverrideType> empConstraintOverrideTypes = empOverrides.keySet();
			for (ConstraintOverrideType empConstraintOverrideType : empConstraintOverrideTypes){
				if (empOverrides.get(empConstraintOverrideType)) {
					ConstraintOverrideDto constraintOverrideDto = new ConstraintOverrideDto();
					constraintOverrideDto.setEmployeeId(empId);
					constraintOverrideDto.setType(empConstraintOverrideType);
					altConstraintOverrideDtos.add(constraintOverrideDto);
				}
			}
		}
		return altConstraintOverrideDtos;
	}

	private EngineRequest buildNewEngineRequest(RequestType requestType, String requestId, Schedule schedule) {
		EngineRequest engineRequest = new EngineRequest();
        engineRequest.setRequestType(requestType);
        engineRequest.setRequestId(requestId);
        engineRequest.setScheduleId(schedule.getId());
        engineRequest.setScheduleName(schedule.getName());
        engineRequest.setTenantId(schedule.getTenantId());

        Tenant tenant = organizationService.getTenant(schedule.getTenantId());
        engineRequest.setTenantName(tenant.getName());

    	String accountId = sessionService.getActualUserId();
    	String accountName;
    	if (accountId != null) {
    		accountName = sessionService.getActualUserName();
    	} else {
    		accountId = accountName = "System";
    	}
    	engineRequest.setAccountId(accountId);
    	engineRequest.setAccountName(accountName);

        boolean s3use = !RequestType.Qualification.equals(requestType)
                && !RequestType.OpenShiftEligibility.equals(requestType)
                && !RequestType.ShiftSwapEligibility.equals(requestType)
                && "true".equalsIgnoreCase(System.getProperty(Constants.SCHEDULE_REPORT_AWS_USE));
        engineRequest.setStoreSchedulerReportInS3(s3use);

		return engineRequest;
	}

	public PatternElt updatePatternElt(PatternElt patternElt) {
        return entityManager.merge(patternElt);
    }

    public void createPatternElt(PatternElt patternElt) {
        entityManager.persist(patternElt);
    }

    public void deletePatternElt(PatternElt patternElt) {
        entityManager.remove(patternElt);
    }

    public Site getSite(Schedule schedule) {
        Site result = null;

        Set<Team> teams = schedule.getTeams();
        if (teams != null) {
            Team team = teams.iterator().next();
            if (team != null) {
                result = teamService.getSite(team);
            }
        }

        return result;
    }

    private ShiftAssignmentDto shiftToShiftAssignmentDto(Shift shift) {
        ShiftAssignmentDto shiftAssignmentDto = new ShiftAssignmentDto();
        shiftAssignmentDto.setEmployeeId(shift.getEmployeeId());
        shiftAssignmentDto.setEmployeeName(shift.getEmployeeName());
        shiftAssignmentDto.setExcess(shift.isExcess());
        shiftAssignmentDto.setLocked(shift.isLocked());
        shiftAssignmentDto.setShiftId(shift.getId());
        shiftAssignmentDto.setShiftSkillId(shift.getSkillId());
        shiftAssignmentDto.setShiftEndDateTime(new DateTime(shift.getEndDateTime()));
        shiftAssignmentDto.setShiftStartDateTime(new DateTime(shift.getStartDateTime()));

        return shiftAssignmentDto;
    }

    private ShiftDto shiftToShiftDto(Shift shift) {
        ShiftDto shiftDto = new ShiftDto();
        shiftDto.setRequiredEmployeeSize(1);
        shiftDto.setTeamId(shift.getTeamId());
        shiftDto.setSkillId(shift.getSkillId());
        shiftDto.setId(shift.getId());
        shiftDto.setStartDateTime(new DateTime(shift.getStartDateTime()));
        shiftDto.setEndDateTime(new DateTime(shift.getEndDateTime()));
        shiftDto.setExcessShift(shift.isExcess());

        return shiftDto;
    }

    private EmployeeRosterDto createEmployeeRosterDto(Schedule schedule) {
        EmployeeRosterDto rosterDto = new EmployeeRosterDto();

        Site site = getSite(schedule);
        rosterDto.setTimeZone(site.getTimeZone().getID()); 
        rosterDto.setFirstDayOfWeek(site.getFirstDayOfWeek());
        rosterDto.setWeekendDefinition(site.getWeekendDefinition());
        rosterDto.setFirstShiftDate(new ShiftDate(new Date(schedule.getStartDate())));
        rosterDto.setLastShiftDate(new ShiftDate(new Date(schedule.getEndDate())));
        rosterDto.setPlanningWindowStart(rosterDto.getFirstShiftDate().getDateOfFirstDayOfWeek(
                rosterDto.getFirstDayOfWeek()));
        
        ShiftDate twoWeekOvertimeStart = new ShiftDate(new Date(site.getTwoWeeksOvertimeStartDate()));
        rosterDto.setTwoWeekOvertimeStartDate(twoWeekOvertimeStart);

        rosterDto.setRuleWeightMultipliers(schedule.getRuleWeightMultipliers());

        rosterDto.setScoringRulesToScoreLevelMap(new HashMap<RuleName, Integer>());

        SchedulingSettings schedulingSettings = site.getSchedulingSettings();
        if (!schedulingSettings.isOverride()) {
            Organization organization = organizationService.getOrganization(schedule.getTenantId());
            schedulingSettings = organization.getSchedulingSettings();
        }

        OptimizationSettingList optimizationSettings = schedulingSettings.getOptimizationSettings();

        SchedulingOptions schedulingOptions = schedule.getSchedulingOptions();
        if ("COP".equals(schedulingOptions.getOptimizationPreferenceSetting())) {
            if (schedulingOptions.isOverrideOptimizationPreference()) {
                rosterDto.putScoringRuleScoreLevel(RuleName.SCHEDULE_OVERTIME_RULE, 0);
                rosterDto.putScoringRuleScoreLevel(RuleName.SCHEDULE_COST_RULE, 1);
                rosterDto.putScoringRuleScoreLevel(RuleName.CD_PREFERENCE_RULE, 2);
                rosterDto.putScoringRuleScoreLevel(RuleName.CI_PREFERENCE_RULE, 2);
                rosterDto.putScoringRuleScoreLevel(RuleName.SENIORITY_RULE, 3);
                rosterDto.putScoringRuleScoreLevel(RuleName.EXTRA_SHIFT_RULE, 3);
            }
        }

        if (rosterDto.getScoringRulesToScoreLevelMap().size() == 0) {
            for (int i = 0; i < optimizationSettings.size(); i++) {
                OptimizationSetting optimizationSetting = optimizationSettings.get(i);
                if (OptimizationSettingName.OptimizationPreference.equals(optimizationSetting.getName())) {
                    rosterDto.putScoringRuleScoreLevel(RuleName.SCHEDULE_COST_RULE, i);
                    rosterDto.putScoringRuleScoreLevel(RuleName.SCHEDULE_OVERTIME_RULE, i);
                    rosterDto.putScoringRuleScoreLevel(RuleName.CI_PREFERENCE_RULE, i);
                    rosterDto.putScoringRuleScoreLevel(RuleName.CD_PREFERENCE_RULE, i);
                } else if (OptimizationSettingName.MinimizeNbOfExcessShifts.equals(optimizationSetting.getName())) {
                    rosterDto.putScoringRuleScoreLevel(RuleName.EXTRA_SHIFT_RULE, i);
                } else if (OptimizationSettingName.DistributeOpenShifts.equals(optimizationSetting.getName())) {
                    rosterDto.putScoringRuleScoreLevel(RuleName.OPEN_SHIFT_SEPARATION_RULE, i);
                } else if (OptimizationSettingName.DistributeExcessShifts.equals(optimizationSetting.getName())) {
                    rosterDto.putScoringRuleScoreLevel(RuleName.EXCESS_SHIFT_SEPARATION_RULE, i);
                } else if (OptimizationSettingName.ClusterHorizontally.equals(optimizationSetting.getName())) {
                    rosterDto.putScoringRuleScoreLevel(RuleName.HORIZONTAL_CLUSTERING_RULE, i);
                } else if (OptimizationSettingName.ClusterVertically.equals(optimizationSetting.getName())) {
                    rosterDto.putScoringRuleScoreLevel(RuleName.VERTICAL_CLUSTERING_RULE, i);
                } else if (OptimizationSettingName.DistributeWorkedWeekends.equals(optimizationSetting.getName())) {
                    rosterDto.putScoringRuleScoreLevel(RuleName.WORKED_WEEKENDS_SEPARATION_RULE, i);
                } else if (OptimizationSettingName.PrimarySkillValue.equals(optimizationSetting.getName())) {
                    rosterDto.putScoringRuleScoreLevel(RuleName.PREFER_PRIMARY_SKILL_RULE, i);
                } else if (OptimizationSettingName.HomeTeamValue.equals(optimizationSetting.getName())) {
                    rosterDto.putScoringRuleScoreLevel(RuleName.TEAM_PREFERENCE_RULE, i);
                } else if (OptimizationSettingName.TeamScattering.equals(optimizationSetting.getName())) {
                    rosterDto.putScoringRuleScoreLevel(RuleName.PREFER_TEAM_SCATTERING_RULE, i);
                } else if (OptimizationSettingName.DoubleUp.equals(optimizationSetting.getName())) {
                    rosterDto.putScoringRuleScoreLevel(RuleName.AVOID_SKILL_CHANGE_RULE, i);
                    rosterDto.putScoringRuleScoreLevel(RuleName.AVOID_TEAM_CHANGE_RULE, i);
                } else if (OptimizationSettingName.CoupleWeekends.equals(optimizationSetting.getName())) {
                    rosterDto.putScoringRuleScoreLevel(RuleName.COUPLED_WEEKEND_RULE, i);
                }
            }
        }

        rosterDto.setIncludePTOInMinCalculations(!schedulingSettings.isReduceMaximumHoursForPTO());
        rosterDto.setForceCompletionEnabled(schedulingSettings.isForceCompletion());
        switch (schedulingSettings.getProfileDayType()) {
            case DayShiftStarts :
                rosterDto.setProfileDayType(com.emlogis.engine.domain.dto.ProfileDayType.START_DATE);
                break;
            case DayShiftEnds:
                rosterDto.setProfileDayType(com.emlogis.engine.domain.dto.ProfileDayType.END_DATE);
                break;
            case ShiftMajority:
                rosterDto.setProfileDayType(com.emlogis.engine.domain.dto.ProfileDayType.MAJORITY_HOURS);
                break;
            case SplitByMidnight:
                rosterDto.setProfileDayType(com.emlogis.engine.domain.dto.ProfileDayType.SPLIT_BY_MIDNIGHT);
        }

        return rosterDto;
    }

    private Collection<CDAvailabilityTimeFrame> getCdAvailTimeFrames(List<Employee> employees, long startDate,
                                                                     long endDate, String tenantId) {
        return getCDAvailabilityTimeFrames(employees, startDate, endDate, tenantId, "Avail");
    }

    private Collection<CDAvailabilityTimeFrame> getCdUnAvailTimeFrames(List<Employee> employees, long startDate,
                                                                       long endDate, String tenantId) {
        return getCDAvailabilityTimeFrames(employees, startDate, endDate, tenantId, "UnAvail");
    }

    private Collection<CIAvailabilityTimeFrame> getCiUnAvailTimeFrames(List<Employee> employees, long startDate,
                                                                       long endDate, String tenantId) {
        return getCIAvailabilityTimeFrames(employees, startDate, endDate, tenantId, "UnAvail");
    }

    
    private Collection<CDAvailabilityTimeFrame> getCdUnAvailPreferenceTimeFrames(
            List<Employee> employees, long startDate, long endDate, String tenantId) {
        return getCDAvailabilityTimeFrames(employees, startDate, endDate, tenantId, "UnAvailPreference");
    }

    private Collection<CIAvailabilityTimeFrame> getCiUnAvailPreferenceTimeFrames(
            List<Employee> employees, long startDate, long endDate, String tenantId) {
        return getCIAvailabilityTimeFrames(employees, startDate, endDate, tenantId, "UnAvailPreference");
    }

    private Collection<CDAvailabilityTimeFrame> getCdAvailPreferenceTimeFrames(
            List<Employee> employees, long startDate, long endDate, String tenantId) {
        return getCDAvailabilityTimeFrames(employees, startDate, endDate, tenantId, "AvailPreference");
    }

    private Collection<CIAvailabilityTimeFrame> getCiAvailPreferenceTimeFrames(
            List<Employee> employees, long startDate, long endDate, String tenantId) {
        return getCIAvailabilityTimeFrames(employees, startDate, endDate, tenantId, "AvailPreference");
    }

    
    private Collection<CDAvailabilityTimeFrame>getCDAvailabilityTimeFrames(
            List<Employee> employees,
            long startDate,
            long endDate,
            String tenantId,
            String availabilityType) {
        String employeeIds = ModelUtils.commaSeparatedQuotedIds(employees);
        String sql =
                "SELECT f.* FROM " + CDAvailabilityTimeFrame.class.getSimpleName() + " f WHERE " +
                "     f.tenantId = :tenantId " +
                " AND f.startDateTime <= :scheduleLastDay " +
                " AND f.startDateTime >= :scheduleFirstDay " +
                " AND f.employeeId IN (" + employeeIds + ") " +
                " AND f.availabilityType = :availabilityType ";

        Query query = entityManager.createNativeQuery(sql, CDAvailabilityTimeFrame.class);
        query.setParameter("scheduleLastDay", new Timestamp(endDate));
        query.setParameter("scheduleFirstDay", new Timestamp(startDate));
        query.setParameter("tenantId", tenantId);
        query.setParameter("availabilityType", availabilityType);

        return query.getResultList();
    }

    private Collection<CIAvailabilityTimeFrame> getCIAvailabilityTimeFrames(
            List<Employee> employees,
            long startDate,
            long endDate,
            String tenantId,
            String availabilityType) {
        String employeeIds = ModelUtils.commaSeparatedQuotedIds(employees);
        String sql =
                "SELECT f.* FROM " + CIAvailabilityTimeFrame.class.getSimpleName() + " f WHERE " +
                "     f.tenantId = :tenantId " +
                " AND f.startDateTime <= :scheduleLastDay " +
                " AND ( f.endDateTime IS NULL OR f.endDateTime >= :scheduleFirstDay ) " +
                " AND f.employeeId IN (" + employeeIds + ") " +
                " AND f.availabilityType = :availabilityType ";

        Query query = entityManager.createNativeQuery(sql, CIAvailabilityTimeFrame.class);
        query.setParameter("scheduleLastDay", new Timestamp(endDate));
        query.setParameter("scheduleFirstDay", new Timestamp(startDate));
        query.setParameter("tenantId", tenantId);
        query.setParameter("availabilityType", availabilityType);

        return query.getResultList();
    }

    private <T extends AvailabilityTimeFrame> Collection<T> getEmployeeFrames(Collection<T> frames, String employeeId) {
        Collection<T> result = new ArrayList<>();
        for (T frame : frames) {
            if (employeeId.equals(frame.getEmployeeId())) {
                result.add(frame);
            }
        }
        return result;
    }

    private List<TimeWindowDto> getEmployeeTimeOffDtos(Collection<CDAvailabilityTimeFrame> allCdUnAvailTimeFrames,
    		Collection<CDAvailabilityTimeFrame> allCdAvailTimeFrames,
    		Collection<CIAvailabilityTimeFrame> allCiUnAvailTimeFrames,
    		Collection<CDAvailabilityTimeFrame> allCdUnAvailPrefTimeFrames, 
    		Collection<CDAvailabilityTimeFrame> allCdAvailPrefTimeFrames, 
    		Collection<CIAvailabilityTimeFrame> allCiUnAvailPrefTimeFrames, 
    		Collection<CIAvailabilityTimeFrame> allCiAvailPrefTimeFrames, 
    		Employee employee, long schedStartDate, long schedEndDate)
    				throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	List<TimeWindowDto> result = new ArrayList<>();

    	String employeeId = employee.getId();
    	DateTimeZone siteTimeZone = employee.getSite().getTimeZone();

    	// Let's get employee's Preferences  to send over as-is...
    	Collection<CDAvailabilityTimeFrame> cdUnavailPrefTimeFrames = getEmployeeFrames(allCdUnAvailPrefTimeFrames, employeeId);
    	for (CDAvailabilityTimeFrame cdUnavailPrefTimeFrame : cdUnavailPrefTimeFrames) {
    		result.add(toCDPreferenceDto(cdUnavailPrefTimeFrame, siteTimeZone));
    	}

    	Collection<CDAvailabilityTimeFrame> cdAvailPrefTimeFrames = getEmployeeFrames(allCdAvailPrefTimeFrames, employeeId);
    	for (CDAvailabilityTimeFrame cdAvailPrefTimeFrame : cdAvailPrefTimeFrames) {
    		result.add(toCDPreferenceDto(cdAvailPrefTimeFrame, siteTimeZone));
    	}

    	Collection<CIAvailabilityTimeFrame> ciUnavailPrefTimeFrames = getEmployeeFrames(allCiUnAvailPrefTimeFrames, employeeId);
    	for (CIAvailabilityTimeFrame ciUnavailPrefTimeFrame : ciUnavailPrefTimeFrames) {
    		result.add(toCIPreferenceDto(ciUnavailPrefTimeFrame, siteTimeZone));
    	}

    	Collection<CIAvailabilityTimeFrame> ciAvailPrefTimeFrames = getEmployeeFrames(allCiAvailPrefTimeFrames, employeeId);
    	for (CIAvailabilityTimeFrame ciAvailPrefTimeFrame : ciAvailPrefTimeFrames) {
    		result.add(toCIPreferenceDto(ciAvailPrefTimeFrame, siteTimeZone));
    	}

    	// Let's get employee's CD Unavails to send over as-is...
    	Collection<CDAvailabilityTimeFrame> cdUnavailTimeFrames = getEmployeeFrames(allCdUnAvailTimeFrames, employeeId);
    	for (CDAvailabilityTimeFrame cdUnavailTimeFrame : cdUnavailTimeFrames) {
    		result.add(toCDTimeOffDto(cdUnavailTimeFrame, siteTimeZone));
    	}

    	// Let's get applicable CI Unavails so they can be analyzed for overlap analysis...
    	Collection<CIAvailabilityTimeFrame> ciUnavailTimeFrames = getEmployeeFrames(allCiUnAvailTimeFrames, employeeId);

    	// Let's get applicable CD Avails so they can be analyzed for overlap analysis (and also
    	// go ahead and wrap them so they can alternatively be examined as Joda Intervals...
    	Collection<CDAvailabilityTimeFrame> cdAvailTimeFrames = getEmployeeFrames(allCdAvailTimeFrames, employeeId);
    	//Collection<CDAvailabilityInterval> cdAvailabilityIntervals = new ArrayList<>();
    	//for (CDAvailabilityTimeFrame cdAvailabilityTimeFrame : cdAvailTimeFrames) {
    	//cdAvailabilityIntervals.add(new CDAvailabilityInterval(cdAvailabilityTimeFrame));
    	//}

    	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// Now each applicable CI Unavail, send in overlapped CDAvail "Avail for Day" as "exceptions" (CDOverrideAvail")
    	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    	// FOR EACH CI Unavail time frame...
    	for (CIAvailabilityTimeFrame ciUnavailTimeFrame : ciUnavailTimeFrames) {

    		CITimeOffDto ciTO = toCITimeOffDto(ciUnavailTimeFrame, siteTimeZone);

    		for (CDAvailabilityTimeFrame cdAvailTf : cdAvailTimeFrames) {
    			if (cdAvailTf.getStartDateTime().getMillis() <= (ciUnavailTimeFrame.getStartDateTime()).getMillis()
    					&& (ciUnavailTimeFrame.getEndDateTime()==null 
    					|| cdAvailTf.getStartDateTime().isBefore(ciUnavailTimeFrame.getEndDateTime()))){

    				LocalDate tzAdjustedDay = new LocalDate(cdAvailTf.getStartDateTime(), siteTimeZone);
    				int tzAdjustedSchedDayOfWeek = tzAdjustedDay.getDayOfWeek();  

    				DayOfWeek ciUnavailDayOfWeek = ciUnavailTimeFrame.getDayOfTheWeek();
    				if ((ciUnavailDayOfWeek == DayOfWeek.SUNDAY     &&  tzAdjustedSchedDayOfWeek == DateTimeConstants.SUNDAY) ||
    						(ciUnavailDayOfWeek == DayOfWeek.MONDAY     &&  tzAdjustedSchedDayOfWeek == DateTimeConstants.MONDAY) ||
    						(ciUnavailDayOfWeek == DayOfWeek.TUESDAY    &&  tzAdjustedSchedDayOfWeek == DateTimeConstants.TUESDAY) ||
    						(ciUnavailDayOfWeek == DayOfWeek.WEDNESDAY  &&  tzAdjustedSchedDayOfWeek == DateTimeConstants.WEDNESDAY) ||
    						(ciUnavailDayOfWeek == DayOfWeek.THURSDAY   &&  tzAdjustedSchedDayOfWeek == DateTimeConstants.THURSDAY) ||
    						(ciUnavailDayOfWeek == DayOfWeek.FRIDAY     &&  tzAdjustedSchedDayOfWeek == DateTimeConstants.FRIDAY ) ||
    						(ciUnavailDayOfWeek == DayOfWeek.SATURDAY   &&  tzAdjustedSchedDayOfWeek == DateTimeConstants.SATURDAY)) {
    					ciTO.addCDOverride(new CDOverrideAvailDate(tzAdjustedDay));
    				}
    			}
    		}
    		result.add(ciTO);
    	}

    	return result;
    }

	@SuppressWarnings("unused")
	private CDAvailabilityTimeFrame toCDAvailabilityTimeFrame(CIAvailabilityTimeFrame ciTimeFrame, LocalDate day) {
		CDAvailabilityTimeFrame cdTimeFrame = 
				new CDAvailabilityTimeFrame( 
						new PrimaryKey("dummyPrimaryKey"), ciTimeFrame.getEmployee(), ciTimeFrame.getAbsenceType(), 
						"dummyReason", ciTimeFrame.getDurationInMinutes(), ciTimeFrame.getAvailabilityType(),
                        day.toDateTime(ciTimeFrame.getStartTime()), false );
		
		return cdTimeFrame;
	}

	private CDTimeOffDto toCDTimeOffDto(CDAvailabilityTimeFrame cdAvailabilityTimeFrame, DateTimeZone siteTimeZone) {
		AvailabilityType availType = cdAvailabilityTimeFrame.getAvailabilityType();
		if (AvailabilityType.UnAvailPreference.equals(availType) || AvailabilityType.AvailPreference.equals(availType)) {
			throw new IllegalArgumentException("Argument must be Employee Availability Time Frame");
		}
		
		CDTimeOffDto cdTimeOffDto = new CDTimeOffDto();
		
		DateTime startDateTZ = new DateTime(cdAvailabilityTimeFrame.getStartDateTime(), siteTimeZone);
		DateTime endDateTZ = startDateTZ.plusMinutes(cdAvailabilityTimeFrame.getDurationInMinutes().getMinutes());
		
		cdTimeOffDto.setDayOffStart(startDateTZ);
		cdTimeOffDto.setDayOffEnd(endDateTZ);
		
		cdTimeOffDto.setEmployeeId(cdAvailabilityTimeFrame.getEmployeeId());
		
		cdTimeOffDto.setStartTime(startDateTZ.toLocalTime());
		cdTimeOffDto.setEndTime(endDateTZ.toLocalTime());
		
		cdTimeOffDto.setWeight(-1);

		if (endDateTZ.getMillisOfDay()==0) {
			cdTimeOffDto.setAllDay(true);
		} else {
			cdTimeOffDto.setAllDay(false);
		}		
		
		if (cdAvailabilityTimeFrame.getIsPTO()) {
			if (cdAvailabilityTimeFrame.getDurationInMinutes().getMinutes() >= Constants.MIN_MINUTES_PER_DAY &&
					cdAvailabilityTimeFrame.getAvailabilityType().equals(AvailabilityType.UnAvail)) {
				cdTimeOffDto.setPTO(true);				
			} else {
				// Should never happen, but if isPTO failed sanity check then ...
				throw new ValidationException(sessionService.getMessage("validation.employee.pto.error"));
			}
		} else {
			cdTimeOffDto.setPTO(false);
		}

		return cdTimeOffDto;
	}

	private CITimeOffDto toCITimeOffDto(CIAvailabilityTimeFrame ciUnavailTimeFrame, DateTimeZone siteTimeZone) {
		AvailabilityType availType = ciUnavailTimeFrame.getAvailabilityType();
		if (AvailabilityType.UnAvailPreference.equals(availType) || AvailabilityType.AvailPreference.equals(availType)) {
			throw new IllegalArgumentException("Argument must be Employee Availability Time Frame");
		}

		CITimeOffDto ciTimeOffDto = new CITimeOffDto();

		ciTimeOffDto.setDayOfWeek(ciUnavailTimeFrame.getDayOfTheWeek());
		ciTimeOffDto.setEmployeeId(ciUnavailTimeFrame.getEmployeeId());
		ciTimeOffDto.setStartTime(ciUnavailTimeFrame.getStartTime());
		ciTimeOffDto.setEndTime(ciUnavailTimeFrame.getStartTime().plusMinutes(
                ciUnavailTimeFrame.getDurationInMinutes().getMinutes()));
		ciTimeOffDto.setWeight(-1);

		if (ciUnavailTimeFrame.getStartTime().getMillisOfDay() == 0
                && (ciUnavailTimeFrame.getDurationInMinutes().getMinutes() == 24 * 60)) {
			ciTimeOffDto.setAllDay(true);
		} else {
			ciTimeOffDto.setAllDay(false);
		}		
		
		DateTime effStartTime = new DateTime((ciUnavailTimeFrame.getStartDateTime() != null ? ciUnavailTimeFrame.getStartDateTime(): BOT));
		DateTime effEndTime = new DateTime((ciUnavailTimeFrame.getEndDateTime() != null ? ciUnavailTimeFrame.getEndDateTime(): EOT));
		
		ciTimeOffDto.setEffectiveStart(effStartTime);
		ciTimeOffDto.setEffectiveEnd(effEndTime);

		ciTimeOffDto.setPTO(false); // PTO not supported for ci

		return ciTimeOffDto;
	}

	private CDPreferenceDto toCDPreferenceDto(CDAvailabilityTimeFrame cdAvailabilityTimeFrame, DateTimeZone siteTimeZone) {
		CDPreferenceDto cdPreferenceDto = new CDPreferenceDto();
		cdPreferenceDto.setPTO(false);

		AvailabilityType availType = cdAvailabilityTimeFrame.getAvailabilityType();
		if (AvailabilityType.UnAvailPreference.equals(availType)){
			cdPreferenceDto.setType(PreferenceType.PreferedUnavail);
		} else if (AvailabilityType.AvailPreference.equals(availType)){
			cdPreferenceDto.setType(PreferenceType.PreferedAvail);			
		} else {
			throw new IllegalArgumentException("Argument must be Employee Preference Time Frame");
		}

		cdPreferenceDto.setDayOffStart(cdAvailabilityTimeFrame.getStartDateTime());
		cdPreferenceDto.setDayOffEnd(cdAvailabilityTimeFrame.getStartDateTime());
		
		cdPreferenceDto.setEmployeeId(cdAvailabilityTimeFrame.getEmployeeId());
		
		//set start date-time to site time-zone before converting it LocalTime
		LocalTime startTimeLT = (new DateTime(cdAvailabilityTimeFrame.getStartDateTime(), siteTimeZone)).toLocalTime();
		
		cdPreferenceDto.setStartTime(startTimeLT);
		cdPreferenceDto.setEndTime(startTimeLT.plusMinutes(cdAvailabilityTimeFrame.getDurationInMinutes().getMinutes()));

		cdPreferenceDto.setWeight(1);

		if (cdAvailabilityTimeFrame.getStartDateTime().getMillis() == 0
                && (cdAvailabilityTimeFrame.getDurationInMinutes().getMinutes() == 24 * 60)) {
			cdPreferenceDto.setAllDay(true);
		} else {
			cdPreferenceDto.setAllDay(false);
		}

		return cdPreferenceDto;
	}

	private CIPreferenceDto toCIPreferenceDto(CIAvailabilityTimeFrame ciAvailTimeFrame, DateTimeZone siteTimeZone) {
		CIPreferenceDto ciPreferenceDto = new CIPreferenceDto();
		ciPreferenceDto.setPTO(false);

		AvailabilityType availType = ciAvailTimeFrame.getAvailabilityType();
		if (AvailabilityType.UnAvailPreference.equals(availType)){
			ciPreferenceDto.setType(PreferenceType.PreferedUnavail);
		} else if (AvailabilityType.AvailPreference.equals(availType)){
			ciPreferenceDto.setType(PreferenceType.PreferedAvail);			
		} else {
			throw new IllegalArgumentException("Argument must be Employee Preference Time Frame");
		}

		ciPreferenceDto.setDayOfWeek(ciAvailTimeFrame.getDayOfTheWeek());
		ciPreferenceDto.setEmployeeId(ciAvailTimeFrame.getEmployeeId());
		ciPreferenceDto.setStartTime(ciAvailTimeFrame.getStartTime());
		ciPreferenceDto.setEndTime(ciAvailTimeFrame.getStartTime().plusMinutes(
                ciAvailTimeFrame.getDurationInMinutes().getMinutes()));
		ciPreferenceDto.setWeight(1);

		DateTime effStartTime = new DateTime((ciAvailTimeFrame.getStartDateTime() != null ? ciAvailTimeFrame.getStartDateTime(): BOT));
		DateTime effEndTime = new DateTime((ciAvailTimeFrame.getEndDateTime() != null ? ciAvailTimeFrame.getEndDateTime(): EOT));
		
		ciPreferenceDto.setEffectiveStart(effStartTime);
		ciPreferenceDto.setEffectiveStart(effEndTime);

		if (ciAvailTimeFrame.getStartTime().getMillisOfDay() == 0
                && ciAvailTimeFrame.getDurationInMinutes().getMinutes() == 24 * 60) {
			ciPreferenceDto.setAllDay(true);
		} else {
			ciPreferenceDto.setAllDay(false);
		}		

		return ciPreferenceDto;
	}

    @SuppressWarnings("unchecked")
    private List<ContractDto> getContractDtos(List<Employee> employees) {
        Map<String, ContractDto> employeeContractMap = new HashMap<>();

        if (employees==null || employees.size()==0){
        	return null;
        }
        
        //Create a ContractDTO for every employee
        for (Employee e: employees){
        	ContractDto contractDto = new ContractDto();
            contractDto.setId(e.getId());
            contractDto.setScope(ContractScope.EmployeeContract);
            employeeContractMap.put(e.getId(), contractDto);
        }
             
        String ids = ModelUtils.commaSeparatedQuotedIds(employees);

        String booleanSql =
            "SELECT ec.employeeId, cl.enabled, cl.weight, cl.contractLineType " +
            "  FROM EmployeeContract ec LEFT JOIN BooleanCL cl " +
            "              ON ec.tenantId = cl.contractTenantId AND ec.id = cl.contractId " +
            " WHERE ec.employeeId IN (" + ids + ") ";
        Query booleanQuery = entityManager.createNativeQuery(booleanSql);
        List<Object[]> booleanResult = booleanQuery.getResultList();
        for (Object[] objects : booleanResult) {
            BooleanClInfo info = new BooleanClInfo(objects);

            if (info.enabled != null) {
	            ContractDto contractDto = employeeContractMap.get(info.employeeId);
	            BooleanCLDto booleanCLDto = new BooleanCLDto();
	            booleanCLDto.setEnabled(info.enabled);
	            
	            if (info.weight != null) {
	                booleanCLDto.setWeight(info.weight);
	            }
	            booleanCLDto.setContractLineType(info.contractLineType);
	
	            contractDto.getContractLineDtos().add(booleanCLDto);
            }
        }

        String intSql =
            "SELECT ec.employeeId, cl.maximumEnabled, cl.minimumEnabled, cl.maximumValue, " +
            "       cl.minimumValue, cl.maximumWeight, cl.minimumWeight, cl.contractLineType " +
            "  FROM EmployeeContract ec LEFT JOIN IntMinMaxCL cl " +
            "              ON ec.tenantId = cl.contractTenantId AND ec.id = cl.contractId " +
            " WHERE ec.employeeId IN (" + ids + ") ";
        Query intQuery = entityManager.createNativeQuery(intSql);
        List<Object[]> intResult = intQuery.getResultList();
        for (Object[] objects : intResult) {
            IntMinMaxClInfo info = new IntMinMaxClInfo(objects);
            if (info.maximumEnabled != null || info.minimumEnabled != null) {
	            ContractDto contractDto = employeeContractMap.get(info.employeeId);
	
	            IntMinMaxCLDto intMinMaxCLDto = new IntMinMaxCLDto();
	            
	            //add check for info.maximumValue!=0 because somehow N/A is translated to 0 for DaysofWeek 
	            // and ConsecutiveDays TODO Need a smart save for employee settings
	            if (info.maximumEnabled != null && info.maximumValue!=0) {
	                intMinMaxCLDto.setMaximumEnabled(info.maximumEnabled);
	            }
	            
	            if (info.minimumEnabled != null) {
	                intMinMaxCLDto.setMinimumEnabled(info.minimumEnabled);
	            }
	            if (info.maximumValue != null) {
	                intMinMaxCLDto.setMaximumValue(info.maximumValue);
	            }
	            if (info.minimumValue != null) {
	                intMinMaxCLDto.setMinimumValue(info.minimumValue);
	            }
	            
	            // TODO ston verify that we only use maxiumEnabled and minimumEnable values but weights are the same = -1
	//            if (info.maximumWeight != null) {
	//                intMinMaxCLDto.setMaximumWeight(info.maximumWeight);
	//            }
	//            if (info.minimumWeight != null) {
	//                intMinMaxCLDto.setMinimumWeight(info.minimumWeight);
	//            }
	            intMinMaxCLDto.setContractLineType(info.contractLineType);
	
	
	            contractDto.getContractLineDtos().add(intMinMaxCLDto);
            }
        }

        String weekendRotationSql =
            "SELECT ec.employeeId, cl.weight, cl.dayOfWeek, cl.numberOfDays, " +
            "       cl.outOfTotalDays, cl.rotationType, cl.contractLineType " +
            "  FROM EmployeeContract ec LEFT JOIN WeekdayRotationPatternCL cl " +
            "             ON ec.tenantId = cl.contractTenantId AND ec.id = cl.contractId " +
            " WHERE ec.employeeId IN (" + ids + ")";
        Query weekendRotationQuery = entityManager.createNativeQuery(weekendRotationSql);
        List<Object[]> weekendRotationResult = weekendRotationQuery.getResultList();
        for (Object[] objects : weekendRotationResult) {
            WeekendRotationPatternClInfo info = new WeekendRotationPatternClInfo(objects);
            
            if (info.dayOfWeek!=null && info.rotationType!=null){
	            ContractDto contractDto = employeeContractMap.get(info.employeeId);
	
	            WeekdayRotationPatternCLDto weekdayRotationPatternCLDto = new WeekdayRotationPatternCLDto();
	            if (info.weight != null) {
	                weekdayRotationPatternCLDto.setWeight(info.weight);
	            }
	            weekdayRotationPatternCLDto.setDayOfWeek(info.dayOfWeek);
	            if (info.numberOfDays != null) {
	                weekdayRotationPatternCLDto.setNumberOfDays(info.numberOfDays);
	            }
	            if (info.outOfTotalDays != null) {
	                weekdayRotationPatternCLDto.setOutOfTotalDays(info.outOfTotalDays);
	            }
	            weekdayRotationPatternCLDto.setRotationType(info.rotationType);
	            weekdayRotationPatternCLDto.setContractLineType(info.contractLineType);
	
	            contractDto.getContractLineDtos().add(weekdayRotationPatternCLDto);
            }
        }

        String weekendWorkSql =
            "SELECT ec.employeeId, cl.weight, cl.daysOffAfter, cl.daysOffBefore, cl.contractLineType " +
            "  FROM EmployeeContract ec LEFT JOIN WeekendWorkPatternCL cl " +
            "                 ON ec.tenantId = cl.contractTenantId AND ec.id = cl.contractId " +
            " WHERE ec.employeeId IN (" + ids + ")";
        Query weekendWorkQuery = entityManager.createNativeQuery(weekendWorkSql);
        List<Object[]> weekendWorkResult = weekendWorkQuery.getResultList();
        for (Object[] objects : weekendWorkResult) {
            WeekendWorkPatternClInfo info = new WeekendWorkPatternClInfo(objects);

            if (info.contractLineType!=null){
	            ContractDto contractDto = employeeContractMap.get(info.employeeId);
	            WeekendWorkPatternCLDto weekendWorkPatternCLDto = new WeekendWorkPatternCLDto();
	            if (info.weight != null) {
	                weekendWorkPatternCLDto.setWeight(info.weight);
	            }
	            weekendWorkPatternCLDto.setDaysOffAfter(info.daysOffAfter);
	            weekendWorkPatternCLDto.setDaysOffBefore(info.daysOffBefore);
	            weekendWorkPatternCLDto.setContractLineType(info.contractLineType);
	
	            contractDto.getContractLineDtos().add(weekendWorkPatternCLDto);
            }
        }

        List<ContractDto> result = new ArrayList<>();
        result.addAll(employeeContractMap.values());

        return result;
    }

    private int getEmployeeCount(String tenantId, String scheduleId) {
        String sql =
            "SELECT count(*) FROM (SELECT DISTINCT e.* FROM Team_Schedule ts, EmployeeTeam et, Employee e " +
            "                       WHERE ts.schedules_id = :scheduleId AND ts.schedules_tenantId = :tenantId " +
            "                         AND ts.schedules_tenantId = et.tenantId AND et.tenantId = e.tenantId " +
            "                         AND ts.Team_id = et.teamId AND et.employeeId = e.id AND e.isDeleted = false) x ";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", scheduleId);
        query.setParameter("tenantId", tenantId);

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    private List<Employee> getModelEmployees(String tenantId, String scheduleId, Collection<Skill> requiredSkills) {
        String skillIds = ModelUtils.commaSeparatedQuotedIds(requiredSkills);
        String sql =
            "SELECT DISTINCT e.* FROM Team_Schedule ts, EmployeeTeam et, EmployeeSkill es, Employee e " +
            " WHERE ts.schedules_id = :scheduleId AND ts.schedules_tenantId = :tenantId " +
            "   AND ts.schedules_tenantId = et.tenantId AND et.tenantId = e.tenantId AND e.tenantId = es.tenantId " +
            "   AND ts.Team_id = et.teamId AND et.employeeId = e.id AND e.id = es.employeeId " +
            "   AND e.activityType IN (1,2) AND e.isDeleted = false " +
            (StringUtils.isNotBlank(skillIds) ? " AND es.skillId IN (" + skillIds + ")" : "");

        Query query = entityManager.createNativeQuery(sql, Employee.class);
        query.setParameter("scheduleId", scheduleId);
        query.setParameter("tenantId", tenantId);

        return query.getResultList();
    }

    private List<Team> refineTeamsBySkills(Collection<Team> teams, Collection<Skill> requiredSkills) {
        List<Team> result = new ArrayList<>();

        for (Team team : teams) {
            if (team.getSkills() != null) {
                for (Skill skill : team.getSkills()) {
                    if (requiredSkills.contains(skill)) {
                        result.add(team);
                        break;
                    }
                }
            }
        }

        return result;
    }

    private List<EmployeeTeamDto> getEmployeeTeamDtos(Collection<Team> teams) {
        List<EmployeeTeamDto> result = new ArrayList<>();

        for (Team team : teams) {
            Set<EmployeeTeam> employeeTeams = team.getEmployeeTeams();
            for (EmployeeTeam employeeTeam : employeeTeams) {
                EmployeeTeamDto employeeTeamDto = new EmployeeTeamDto();
                employeeTeamDto.setEmployeeId(employeeTeam.getEmployee().getId());
                employeeTeamDto.setTeamId(team.getId());
                employeeTeamDto.setHomeTeam(employeeTeam.getIsHomeTeam());
                employeeTeamDto.setType(
                        employeeTeam.getIsFloating() ? TeamAssociationType.FLOAT : TeamAssociationType.ON);
                result.add(employeeTeamDto);
            }
        }

        return result;
    }

    private List<EmployeeDto> getEngineEmployeeDtos(List<Employee> employees, Schedule schedule,
                                                    Collection<CDAvailabilityTimeFrame> cdUnAvailTimeFrames,
                                                    Collection<CDAvailabilityTimeFrame> cdAvailTimeFrames,
                                                    Collection<CIAvailabilityTimeFrame> ciUnAvailTimeFrames, 
                                                    Collection<CDAvailabilityTimeFrame> cdUnAvailPrefTimeFrames, 
                                                    Collection<CDAvailabilityTimeFrame> cdAvailPrefTimeFrames, 
                                                    Collection<CIAvailabilityTimeFrame> ciUnAvailPrefTimeFrames, 
                                                    Collection<CIAvailabilityTimeFrame> ciAvailPrefTimeFrames)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        List<EmployeeDto> result = new ArrayList<>();

        for (Employee employee : employees) {
            EmployeeDto engineEmployeeDto = transformToEngineEmployeeDto(employee, schedule, cdUnAvailTimeFrames,
                    cdAvailTimeFrames, ciUnAvailTimeFrames, cdUnAvailPrefTimeFrames, cdAvailPrefTimeFrames,
                    ciUnAvailPrefTimeFrames, ciAvailPrefTimeFrames);
            result.add(engineEmployeeDto);
        }

        return result;
    }

    private List<EmployeeSkillDto> getEmployeeSkillDtos(List<Employee> employees, Collection<Skill> requiredSkills) {
        List<EmployeeSkillDto> result = new ArrayList<>();

        for (Employee employee : employees) {
            for (EmployeeSkill employeeSkill : employee.getEmployeeSkills()) {
                for (Skill skill : requiredSkills) {
                    if (skill.getId().equals(employeeSkill.getSkill().getId())) {
                        EmployeeSkillDto employeeSkillDto = new EmployeeSkillDto();
                        employeeSkillDto.setEmployeeId(employee.getId());
                        employeeSkillDto.setSkillId(employeeSkill.getSkill().getId());
                        employeeSkillDto.setPrimarySkill(employeeSkill.getIsPrimarySkill());
                        employeeSkillDto.setSkillLevel(String.valueOf(employeeSkill.getSkillScore()));

                        result.add(employeeSkillDto);

                        break;
                    }
                }
            }
        }

        return result;
    }

    private List<SkillDto> getSkillDtos(Collection<Skill> requiredSkills) {
        List<SkillDto> result = new ArrayList<>();

        for (Skill skill : requiredSkills) {
            SkillDto skillDto = new SkillDto();
            skillDto.setId(skill.getId());
            skillDto.setName(skill.getName());
            skillDto.setAbbreviation(skill.getAbbreviation());

            result.add(skillDto);
        }

        return result;
    }

    private EmployeeDto transformToEngineEmployeeDto(Employee employee, Schedule schedule,
                                                     Collection<CDAvailabilityTimeFrame> cdUnAvailTimeFrames,
                                                     Collection<CDAvailabilityTimeFrame> cdAvailTimeFrames,
                                                     Collection<CIAvailabilityTimeFrame> ciUnAvailTimeFrames, 
                                                     Collection<CDAvailabilityTimeFrame> cdUnAvailPrefTimeFrames, 
                                                     Collection<CDAvailabilityTimeFrame> cdAvailPrefTimeFrames, 
                                                     Collection<CIAvailabilityTimeFrame> ciUnAvailPrefTimeFrames, 
                                                     Collection<CIAvailabilityTimeFrame> ciAvailPrefTimeFrames)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        EmployeeDto result = new EmployeeDto();

        result.setId(employee.getId());
        result.setFirstName(employee.getFirstName());
        result.setLastName(employee.getLastName());
        result.setScheduleable(employee.getActivityType() == EmployeeActivityType.Active);
        // TODO ston why do we set senority to -1?
        result.setSeniority(-1);
        result.setEmployeeTimeOffDtos(getEmployeeTimeOffDtos(cdUnAvailTimeFrames, cdAvailTimeFrames,
                ciUnAvailTimeFrames, cdUnAvailPrefTimeFrames, cdAvailPrefTimeFrames, ciUnAvailPrefTimeFrames,
                ciAvailPrefTimeFrames, employee, schedule.getStartDate(), schedule.getEndDate()));

        DateTime startDate = employee.getStartDate() == null ? new DateTime(2010, 1, 1, 0, 0)
                : employee.getStartDate().toDateTime(new LocalTime(0));
        result.setStartDate(startDate);

        DateTime stopDate = employee.getEndDate() == null ? new DateTime(2040, 1, 1, 0, 0)
                : employee.getEndDate().toDateTime(new LocalTime(0));
        result.setStopDate(stopDate);

        return result;
    }

    private List<ConstraintOverrideDto> getScheduleConstraintOverrideDtos(
            Schedule schedule, List<Employee> scheduleEmployees, Set<String> altOverridenEmployeeIds) {
        List<ConstraintOverrideDto> result = new ArrayList<>();
        SchedulingOptions schedulingOptions = schedule.getSchedulingOptions();
        Map<ConstraintOverrideType, OverrideOption> map = schedulingOptions.getOverrideOptions();
        for (ConstraintOverrideType type : map.keySet()) {
            OverrideOption overrideOption = map.get(type);
            if (overrideOption.getScope() == OverrideOptionScope.All) {
                for (Employee employee : scheduleEmployees) {
                    String employeeId = employee.getId();
                    if (altOverridenEmployeeIds == null || !altOverridenEmployeeIds.contains(employeeId)) {
						ConstraintOverrideDto constraintOverrideDto = new ConstraintOverrideDto(employeeId, type);
						result.add(constraintOverrideDto);
					}
                }
            } else if (overrideOption.getScope() == OverrideOptionScope.Select) {
                Collection<String> employeeIds = overrideOption.getEmployeeIds();
                for (String employeeId : employeeIds) {
                    if (altOverridenEmployeeIds == null || !altOverridenEmployeeIds.contains(employeeId)) {
						ConstraintOverrideDto constraintOverrideDto = new ConstraintOverrideDto(
								employeeId, type);
						result.add(constraintOverrideDto);
					}
                }
            }
        }

        return result;
    }

    private void clearScheduleChanges(Schedule schedule, ChangeType... exceptionalTypes) {
        String[] changeTables = new String[] {"ScheduleChange", "ShiftAddChange", "ShiftAssignChange",
                "ShiftDropChange", "ShiftEditChange", "ShiftSwapChange", "ShiftWipChange", "ShiftDeleteChange"};

        for (String table : changeTables) {
            String sql = "DELETE FROM " + table + " WHERE scheduleId = :scheduleId ";
            if (exceptionalTypes != null) {
                sql += " AND type NOT IN (" + ModelUtils.commaSeparatedQuotedValues(exceptionalTypes) + ")";
            }
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("scheduleId", schedule.getId());

            query.executeUpdate();
        }
    }

    private boolean isCorrelatedDates(long date1, long date2, int days) {
        long differenceInMillis = Math.abs(date1 - date2);
        int differenceInDays = (int) differenceInMillis / 1000 / 60 / 60 / 24;
        return differenceInDays % days == 0;
    }

    private Shift findMatchingShift(Schedule scheduleMatchTo, Shift shiftMatchTo, Schedule schedule,
                                    Collection<Shift> shifts) {
        for (Shift shift : shifts) {
            if (StringUtils.equals(shiftMatchTo.getTeamId(), shift.getTeamId())
                    && StringUtils.equals(shiftMatchTo.getShiftLengthId(), shift.getShiftLengthId())
                    && StringUtils.equals(shiftMatchTo.getSkillId(), shift.getSkillId())
                    && StringUtils.equals(shiftMatchTo.getShiftPatternId(), shift.getShiftPatternId())
                    && shiftMatchTo.getStartDateTime() - scheduleMatchTo.getStartDate() ==
                            shift.getStartDateTime() - schedule.getStartDate()
                    && shiftMatchTo.getEmployeeIndex() == shift.getEmployeeIndex()) {
                return shift;
            }
        }

        return null;
    }

    private Schedule promoteSchedule(Schedule schedule, ScheduleStatus status) {
        schedule.setStatus(status);

        Schedule result = update(schedule);

        checkProductionPostedScheduledTeams(schedule);

        Collection<Shift> shifts = shiftService.getScheduleShifts(schedule);
        for (Shift shift : shifts) {
            shift.setScheduleStatus(status);
            shiftService.update(shift);
        }

        return result;
    }

    /**
     * Manual shift delete.
     * 
     * @param schedule
     * @param shift
     * @param managerAccount
     */
    public void manualShiftDelete(Schedule schedule, Shift shift, String wflRequestId, UserAccount managerAccount,
                                  String reason) {
    	com.emlogis.model.schedule.dto.ShiftDto beforeDeleteDto = null;
    	Shift shiftBefore;
		try {
			shiftBefore = shift.clone();
			shiftBefore.setPrimaryKey(shift.getPrimaryKey()); // force primary key to be identical to original
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(sessionService.getMessage("schedule.shift.clone.error", shift.getId()), e);
		}
		try {
			beforeDeleteDto = toDto(shift, com.emlogis.model.schedule.dto.ShiftDto.class);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}
    	shiftService.delete(shift);
        scheduleChangeService.trackShiftDeleteChange(shiftBefore, schedule, wflRequestId, managerAccount, reason);
    	postedOpenShiftService.deletePostedOpenShiftsByShift(shift.getPrimaryKey());
        getEventService().sendEntityDeleteEvent(shift, com.emlogis.model.schedule.dto.ShiftDto.class, beforeDeleteDto);
    }

    public Shift manualShiftCreate(Schedule schedule, Shift shift, String wflRequestId, UserAccount managerAccount,
                                   String reason) {
        shiftService.insert(shift);
        scheduleChangeService.trackShiftAddChange(shift, schedule, wflRequestId, managerAccount, reason);
        getEventService().sendEntityCreateEvent(shift, com.emlogis.model.schedule.dto.ShiftDto.class);
        return shift;
    }

    public Shift manualShiftEdit(Schedule schedule, Shift previousShift, Shift shift, String wflRequestId,
                                 UserAccount managerAccount, String reason) {
        shiftService.update(shift);
        scheduleChangeService.trackShiftEditChange(previousShift, shift, schedule, wflRequestId, managerAccount, reason);
        
        if (managerAccount != null) {
        	sendScheduleChangeNotification(shift, NotificationCategory.SHIFT_STARTSTOP_MODIFIED, previousShift,
                    managerAccount);
        }
        getEventService().sendEntityUpdateEvent(shift, com.emlogis.model.schedule.dto.ShiftDto.class);
        return shift;
    }

    public Shift manualShiftDrop(Schedule schedule, Shift shift, String wflRequestId, UserAccount managerAccount,
                                 String reason) throws IllegalAccessException {
    	Shift shiftBefore;
		try {
			shiftBefore = shift.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(sessionService.getMessage("schedule.shift.clone.error", shift.getId()), e);
		}
    	
		shift.dropShiftAssignment();
    	shift = shiftService.update(shift);
    	entityManager.flush();

        scheduleChangeService.trackShiftDropChange(shiftBefore, shift, schedule, wflRequestId, managerAccount, reason);
    	if (managerAccount != null) {
    		sendScheduleChangeNotification(shiftBefore, NotificationCategory.SHIFT_DROP, null, managerAccount);
    	}
        getEventService().sendEntityUpdateEvent(shift, com.emlogis.model.schedule.dto.ShiftDto.class);
		return shift;
    }

    /** Assign employee to work open shift IF QUALIFIED.
	 *  Method is non-transactional (TransactionAttributeType.NOT_SUPPORTED), but it coordinates calls 
	 *  to transactional methods for getting entities and requesting qualification, as well as for 
	 *  persisting changes for qualified clean data.
	 * 
     * @param schedulePk
     * @param shiftPk
     * @param employeePk
	 * @param force 
     * @param overrideOptions 
     * @return
     */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public QualificationRequestSummary manualShiftOpenAssign(final PrimaryKey schedulePk, PrimaryKey shiftPk,
            PrimaryKey employeePk, Boolean force, String wflRequestId, UserAccount managerAccount, String reason, 
            Map<ConstraintOverrideType, Boolean> overrideOptions)
            throws IllegalAccessException {
		boolean shiftQualified = false;

		// get detached entities
		List<PrimaryKey> shiftPks = new ArrayList<>();
		shiftPks.add(shiftPk);
		List<Shift> detachedShifts = scheduleServiceEJB.getDetachedShifts( shiftPks );
		Shift shiftBefore = detachedShifts.get(0);

		// additional entity validation
		if (!shiftBefore.getScheduleId().equals(schedulePk.getId())) {
	        throw new ValidationException(sessionService.getMessage("validation.schedule.shift.norelation",
                    shiftBefore.getId()));
		}
		if (shiftBefore.getAssigned() != null || shiftBefore.getAssignmentType() != null 
    			|| shiftBefore.getEmployeeId() != null || shiftBefore.getEmployeeName() != null) {
    		throw new ValidationException( sessionService.getMessage("validation.schedule.shift.notopen",
                    shiftBefore.getId()) );
    	}
		
		// set assignment for the detached shift entity
		Shift shiftAfter;
		try {
			shiftAfter = shiftBefore.clone();
			shiftAfter.setPrimaryKey(shiftBefore.getPrimaryKey()); // ensure clone's primary key is identical to original
		} catch (CloneNotSupportedException e) {
	        throw new RuntimeException(sessionService.getMessage("schedule.shift.clone.error", shiftBefore.getId()), e);
		}
		String employeeName = scheduleServiceEJB.getEmployeeFullName(employeePk);
		shiftAfter.makeShiftAssignment(employeePk.getId(), employeeName,
                AssignmentType.MANUAL);

		// Get any constraint overrides needed for qualification of assignment and later for eligibility for PostedOpenShifts...
		PostedOpenShift postedOS = postedOpenShiftService.getPostedOpenShiftByEmployeeAndShift(
				shiftAfter.getTenantId(), shiftAfter.getEmployeeId(), shiftAfter.getId());
		Map<String, Map<ConstraintOverrideType, Boolean>> tmpIndividualEmpConstraintOverrideOpts = null;
		if (overrideOptions != null && !overrideOptions.isEmpty()){
			tmpIndividualEmpConstraintOverrideOpts = new HashMap<>();
			tmpIndividualEmpConstraintOverrideOpts.put(shiftAfter.getEmployeeId(), overrideOptions);			
		} else if (postedOS != null){
			// If no overrides were provided by API, we'll default to those from the posted OS...
			Map<ConstraintOverrideType, Boolean> postedOverrideOptions = postedOS.getOverrideOptions();
			tmpIndividualEmpConstraintOverrideOpts = new HashMap<>();
			tmpIndividualEmpConstraintOverrideOpts.put(shiftAfter.getEmployeeId(), postedOverrideOptions);
		}
		final Map<String, Map<ConstraintOverrideType, Boolean>> individualEmpConstraintOverrideOpts =
                tmpIndividualEmpConstraintOverrideOpts;
		
		String requestId = null;
		if (!force) {
			// request qualification
			Collection<Shift> qualificationShifts = new ArrayList<>();
			qualificationShifts.add(shiftAfter);
			requestId = UniqueId.getId();
			QualificationRequestTracker reqTracker = scheduleServiceEJB.executeQualification(0, 0, qualificationShifts, requestId,
                    individualEmpConstraintOverrideOpts, true);
			QualificationResultDto resultDto = getQualificationResults(requestId, 60);
			if (resultDto != null) {
				reqTracker = hazelcastClientService.getQualificationRequestTracker(reqTracker.getRequestId());
				ScheduleCompletion completion = reqTracker.getCompletion();
				if (ScheduleCompletion.OK.equals(completion)){
					Collection<ShiftQualificationDto> qualifyingShifts = resultDto.getQualifyingShifts();
					if (qualifyingShifts != null) {
						for (ShiftQualificationDto qualifyingShift : qualifyingShifts) {
							String qualifyingShiftId = qualifyingShift.getShiftId();
							if (qualifyingShiftId.equals(shiftAfter.getId()) && qualifyingShift.getIsAccepted()) {
								shiftQualified = true;
							}
						}
					}
					// ShiftMgmtException placeholder   
					if (!shiftQualified) {
						Map<String, Object> paramMap = new HashMap(); //map for getting failing constraints
				        throw new ShiftMgmtException(ShiftMgmtErrorCode.EmployeeDoesntQualify, sessionService.getMessage("validation.employee.shiftassign.not.qualified", employeeName), paramMap);
					}

				} else if (ScheduleCompletion.Aborted.equals(completion)){
					throw new ValidationException(sessionService.getMessage("validation.engine.abort"));
				} else if (ScheduleCompletion.Error.equals(completion)){
					throw new ValidationException(sessionService.getMessage("validation.engine.error"));					
				}
			}
		}
		
		// persist if qualified and data remains clean, or if forced...
		if (!shiftQualified && !force) {
			return new QualificationRequestSummary(requestId, false, false);
		} else {
			List<Shift> shiftsBefore = new ArrayList<>();
			shiftsBefore.add(shiftBefore);
			
			List<Shift> shiftsAfter = new ArrayList<>();
            shiftsAfter.add(shiftAfter);

			scheduleServiceEJB.updateDetachedShifts(shiftsBefore, shiftsAfter);
            scheduleChangeService.trackShiftAssignmentChange(shiftAfter, getSchedule(schedulePk), requestId,
                    managerAccount, reason);

	    	// remove Shift from PostedOpenShifts
			postedOpenShiftService.deletePostedOpenShiftsByShift(shiftPk);

			// Update the PostedOpenShifts table...
			final ArrayList<String> employeeIds = new ArrayList<String>();
			employeeIds.add(employeePk.getId());

			// We'll asynchronously update the PostedOpenShifts table (if needed) 
			// so we can proceed with returning to the caller...
			new Thread(new Runnable() {
				public void run(){
					try {
						scheduleServiceEJB.updateScheduleEmployeesPostedOpenShifts(schedulePk, employeeIds,
                                individualEmpConstraintOverrideOpts);
					} catch (IllegalAccessException e) {
						logger.info("Exception while checking if any PostedOpenShift table updates are needed"
								+ " after most recent open shift assignment involving schedule  " + schedulePk.getId()
								+ " and employee " + employeeIds);
					}
				}
			}).start();
			
			// PLACEHOLDER code to send a ShiftAssign notification
			if (managerAccount != null) {
				// send notification
				sendScheduleChangeNotification(shiftAfter, NotificationCategory.SHIFT_ASSIGN, null, managerAccount);
			}
	        getEventService().sendEntityUpdateEvent(shiftAfter, com.emlogis.model.schedule.dto.ShiftDto.class);

			if (!force) {
				return new QualificationRequestSummary(requestId, true, false);
			} else {
				return new QualificationRequestSummary(requestId, true, true);				
			}
		}
    }

	/** Assign employee to work in place (WIP) IF QUALIFIED.  
	 *  Method is non-transactional (TransactionAttributeType.NOT_SUPPORTED), but it coordinates calls 
	 *  to transactional methods for getting entities and requesting qualification, as well as for 
	 *  persisting changes for qualified clean data.
	 * 
     * @param schedulePk
     * @param shiftPk
     * @param wipEmployeePk
	 * @param force 
	 * @param managerAccount
     * @return
     */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public QualificationRequestSummary manualShiftWIP(final PrimaryKey schedulePk, PrimaryKey shiftPk, PrimaryKey
            wipEmployeePk, Boolean force, String wflRequestId, UserAccount managerAccount, String reason)
            throws IllegalAccessException {
		boolean shiftQualified = false;

		// get detached entities
		List<PrimaryKey> shiftPks = new ArrayList<>();
		shiftPks.add(shiftPk);
		List<Shift> detachedShifts = scheduleServiceEJB.getDetachedShifts(shiftPks);
		Shift shiftBefore = detachedShifts.get(0);

		// additional entity validation
		if (!shiftBefore.getScheduleId().equals(schedulePk.getId())){
	        throw new ValidationException(sessionService.getMessage("validation.schedule.shift.norelation",
                    shiftBefore.getId()));
		}
		if (shiftBefore.getAssigned() == null || shiftBefore.getAssignmentType() == null 
    			|| shiftBefore.getEmployeeId() == null || shiftBefore.getEmployeeName() == null) {
    		throw new ValidationException(sessionService.getMessage("validation.schedule.shift.notassigned",
                    shiftBefore.getId()));
    	}

		// set WIP assignment for the detached shift entity
		Shift shiftAfter;
		try {
			shiftAfter = shiftBefore.clone();
			shiftAfter.setPrimaryKey(shiftBefore.getPrimaryKey()); // ensure clone's primary key is identical to original
		} catch (CloneNotSupportedException e) {
	        throw new RuntimeException(sessionService.getMessage("schedule.shift.clone.error", shiftBefore.getId()), e);
		}
		shiftAfter.makeShiftAssignment(wipEmployeePk.getId(), scheduleServiceEJB.getEmployeeFullName(wipEmployeePk),
                AssignmentType.MANUAL);
		
		String employeeIdBefore = shiftBefore.getEmployeeId();
		String employeeIdAfter = shiftAfter.getEmployeeId();

		String requestId = null;
		if (!force) {
			// request qualification
			Collection<Shift> qualificationShifts = new ArrayList<>();
			qualificationShifts.add(shiftAfter);
			requestId = UniqueId.getId();
			QualificationRequestTracker reqTracker = scheduleServiceEJB.executeQualification(0, 0, qualificationShifts, requestId, null, true);
			QualificationResultDto resultDto = getQualificationResults(requestId, 60);
			if (resultDto != null) {
				reqTracker = hazelcastClientService.getQualificationRequestTracker(reqTracker.getRequestId());
				ScheduleCompletion completion = reqTracker.getCompletion();
				if (ScheduleCompletion.OK.equals(completion)) {
					Collection<ShiftQualificationDto> qualifyingShifts = resultDto.getQualifyingShifts();
					if (qualifyingShifts != null) {
						for (ShiftQualificationDto qualifyingShift : qualifyingShifts) {
							String qualifyingShiftId = qualifyingShift.getShiftId();
							if (qualifyingShiftId.equals(shiftAfter.getId()) && qualifyingShift.getIsAccepted()) {
								shiftQualified = true;
							}
						}
					}
					// ShiftMgmtException placeholder  
					if (!shiftQualified) {
						Map<String, Object> paramMap = new HashMap(); //map for getting failing constraints
				        throw new ShiftMgmtException(ShiftMgmtErrorCode.EmployeeDoesntQualify, sessionService.getMessage("validation.employee.shiftwip.not.qualified", shiftAfter.getEmployeeName()), paramMap);
					}
				} else if (ScheduleCompletion.Aborted.equals(completion)){
					throw new ValidationException(sessionService.getMessage("validation.engine.abort"));
				} else if (ScheduleCompletion.Error.equals(completion)){
					throw new ValidationException(sessionService.getMessage("validation.engine.error"));					
				}
			}
		}

        final QualificationRequestSummary qualificationRequestSummary = new QualificationRequestSummary(requestId,
                false, force);

        // persist if qualified and data remains clean, or if forced...
		if (!shiftQualified && !force){
            qualificationRequestSummary.setFullyQualified(false);
            qualificationRequestSummary.setIsSuccess(false);
            qualificationRequestSummary.setMessage("not qualified");
            return qualificationRequestSummary;
			//return new QualificationRequestSummary(requestId, false, false);
		} else {
			List<Shift> shiftsBefore = new ArrayList<Shift>();
			shiftsBefore.add(shiftBefore);
			
			List<Shift> shiftsAfter = new ArrayList<Shift>();
			shiftsAfter.add(shiftAfter);
			
			scheduleServiceEJB.updateDetachedShifts(shiftsBefore, shiftsAfter);
            scheduleChangeService.trackShiftWipChange(shiftBefore.getEmployeeId(), shiftBefore.getEmployeeName(),
                    shiftAfter, getSchedule(schedulePk), wflRequestId, managerAccount, reason);
	    	
			// Update the PostedOpenShifts table...
			final ArrayList<String> employeeIds = new ArrayList<>();
			employeeIds.add(employeeIdBefore);
			employeeIds.add(employeeIdAfter);

			// Get any constraint overrides needed for qualification of assignment and later for eligibility for PostedOpenShifts... 
			PostedOpenShift postedOS = postedOpenShiftService.getPostedOpenShiftByEmployeeAndShift(
					shiftAfter.getTenantId(), shiftAfter.getEmployeeId(), shiftAfter.getId());
			Map<String, Map<ConstraintOverrideType, Boolean>> tmpIndividualEmpConstraintOverrideOpts = null;
			if (postedOS != null){
				Map<ConstraintOverrideType, Boolean> empOverrideOptions = postedOS.getOverrideOptions();
				tmpIndividualEmpConstraintOverrideOpts = new HashMap<>();
				tmpIndividualEmpConstraintOverrideOpts.put(shiftAfter.getEmployeeId(), empOverrideOptions);
			}
			final Map<String, Map<ConstraintOverrideType, Boolean>> individualEmpConstraintOverrideOpts = tmpIndividualEmpConstraintOverrideOpts;

            try {
                scheduleServiceEJB.updateScheduleEmployeesPostedOpenShifts(schedulePk, employeeIds, individualEmpConstraintOverrideOpts);
            } catch (Throwable e) {
                qualificationRequestSummary.setIsSuccess(false);
                qualificationRequestSummary.setMessage(e.getMessage());
                logger.info("Exception while checking if any PostedOpenShift table updates are needed"
                        + " after most recent WIP involving schedule  " + schedulePk.getId() + " and employee" + employeeIds);
            }
            if (managerAccount != null) {
            	// PLACEHOLDER FOR WIP NOTIFICATION
            	// note that notifications to ech employees must be  sent
            	
            	// Send Drop from first employee
            	sendScheduleChangeNotification(shiftBefore, NotificationCategory.SHIFT_DROP, null, managerAccount);
            	
            	// Send WIP for assigned employee
            	sendScheduleChangeNotification(shiftAfter, NotificationCategory.SHIFT_WIP, shiftBefore, managerAccount);
            }
            getEventService().sendEntityUpdateEvent(shiftAfter, com.emlogis.model.schedule.dto.ShiftDto.class);
            qualificationRequestSummary.setFullyQualified(true);
            return qualificationRequestSummary;
        }
    }
    
	/** Swap shifts IF QUALIFIED.
	 *  Method is non-transactional (TransactionAttributeType.NOT_SUPPORTED), but it coordinates calls 
	 *  to transactional methods for getting entities and requesting qualification, as well as for 
	 *  persisting changes for qualified clean data.
	 * 
	 * @param schedulePk
	 * @param shift1pk
	 * @param shift2pk
	 * @param force 
	 * @param managerAccount
	 */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public QualificationRequestSummary manualShiftSwap(PrimaryKey schedulePk, PrimaryKey shift1pk, PrimaryKey shift2pk,
                                                       Boolean force, String wflRequestId, UserAccount managerAccount,
                                                       String reason) throws IllegalAccessException {
		boolean shift1Qualified = false;// TODO these two variables are always FALSE
		boolean shift2Qualified = false;// TODO what a reason to use them???
				
		// get detached entities
		List<PrimaryKey> shiftPks = new ArrayList<>();
		shiftPks.add(shift1pk);
		shiftPks.add(shift2pk);
		List<Shift> detachedShifts = scheduleServiceEJB.getDetachedShifts( shiftPks );
		Shift shift1Before = detachedShifts.get(0);
		Shift shift2Before = detachedShifts.get(1);
		
		// additional entity validation
		if (!shift1Before.getScheduleId().equals(schedulePk.getId())){
	        throw new ValidationException(sessionService.getMessage("validation.schedule.shift.norelation",
                    shift1Before.getId()));
		}
		if (!shift2Before.getScheduleId().equals(schedulePk.getId())){
	        throw new ValidationException(sessionService.getMessage("validation.schedule.shift.norelation",
                    shift2Before.getId()));
		}
	
		// swap assignments for the detached shift entities
		Shift shift1After;
		try {
			shift1After = shift1Before.clone();
			shift1After.setPrimaryKey(shift1Before.getPrimaryKey()); // ensure clone's primary key is identical to original
		} catch (CloneNotSupportedException e) {
	        throw new RuntimeException(sessionService.getMessage("schedule.shift.clone.error", shift1Before.getId()), e);
		}
	
		Shift shift2After;
		try {
			shift2After = shift2Before.clone();
			shift2After.setPrimaryKey(shift2Before.getPrimaryKey()); // ensure clone's primary key is identical to original
		} catch (CloneNotSupportedException e) {
	        throw new RuntimeException(sessionService.getMessage("schedule.shift.clone.error", shift2Before.getId()), e);
		}
		
		shift1After.makeShiftAssignment(shift2Before.getEmployeeId(), shift2Before.getEmployeeName(), AssignmentType.MANUAL);
		shift2After.makeShiftAssignment(shift1Before.getEmployeeId(), shift1Before.getEmployeeName(), AssignmentType.MANUAL);		
		
		String requestId = null;
		if (!force) {
			// request qualification
			Collection<Shift> qualificationShifts = new ArrayList<Shift>();
			qualificationShifts.add(shift1After);
			qualificationShifts.add(shift2After);
			requestId = UniqueId.getId();
			QualificationRequestTracker reqTracker = scheduleServiceEJB.executeQualification(0, 0, qualificationShifts, requestId, null, true);
			QualificationResultDto resultDto = getQualificationResults(requestId, 60);
			if (resultDto != null) {
				reqTracker = hazelcastClientService.getQualificationRequestTracker(reqTracker.getRequestId());
				ScheduleCompletion completion = reqTracker.getCompletion();
				if (ScheduleCompletion.OK.equals(completion)) {
					Collection<ShiftQualificationDto> qualifyingShifts = resultDto.getQualifyingShifts();
					if (qualifyingShifts != null) {
						for (ShiftQualificationDto qualifyingShift : qualifyingShifts) {
							String qualifyingShiftId = qualifyingShift.getShiftId();
							if (qualifyingShiftId.equals(shift1After.getId()) && qualifyingShift.getIsAccepted()) {
								shift1Qualified = true;
							} else if (qualifyingShiftId.equals(shift2After.getId()) && qualifyingShift.getIsAccepted()) {
								shift2Qualified = true;
							}
						}
					}
					// ShiftMgmtException placeholder  
					if (!shift1Qualified) {
						Map<String, Object> paramMap = new HashMap(); //map for getting failing constraints
				        throw new ShiftMgmtException(ShiftMgmtErrorCode.EmployeeDoesntQualify, sessionService.getMessage("validation.employee.shiftswap.not.qualified", shift1After.getEmployeeName()), paramMap);
					}
					if (!shift2Qualified) {
						Map<String, Object> paramMap = new HashMap(); //map for getting failing constraints
				        throw new ShiftMgmtException(ShiftMgmtErrorCode.EmployeeDoesntQualify, sessionService.getMessage("validation.employee.shiftswap.not.qualified", shift2After.getEmployeeName()), paramMap);
					}
				} else if (ScheduleCompletion.Aborted.equals(completion)){
					throw new ValidationException(sessionService.getMessage("validation.engine.abort"));
				} else if (ScheduleCompletion.Error.equals(completion)){
					throw new ValidationException(sessionService.getMessage("validation.engine.error"));					
				}
			}
		}

        final QualificationRequestSummary qualificationRequestSummary =
                new QualificationRequestSummary(requestId, false, force);
		
		// persist if qualified and data remains clean, or if forced...
		if (!force && (!shift1Qualified || !shift2Qualified)) {
			
            qualificationRequestSummary.setFullyQualified(false);
            qualificationRequestSummary.setIsSuccess(false);
            qualificationRequestSummary.setMessage("not qualified");
            return qualificationRequestSummary;
			//return new QualificationRequestSummary(requestId, false, false);
		} else {
			List<Shift> shiftsBefore = new ArrayList<>();
			shiftsBefore.add(shift1Before);
			shiftsBefore.add(shift2Before);
			
			List<Shift> shiftsAfter = new ArrayList<>();
			shiftsAfter.add(shift1After);
			shiftsAfter.add(shift2After);
			
			scheduleServiceEJB.updateDetachedShifts(shiftsBefore, shiftsAfter);
            scheduleChangeService.trackShiftSwapChange(shift1After, shift2After, getSchedule(schedulePk), wflRequestId,
                    managerAccount, reason);
/*
            scheduleChangeService.trackShiftSwapChange(shift1After.getEmployeeId(), shift1After.getEmployeeName(),
                    toJsonString(shift1After), shift1After.getId(), shift2After.getEmployeeId(), shift2After.getEmployeeName(),
                    toJsonString(shift2After), shift2After.getId(), getSchedule(schedulePk));
*/                    
            // Update the PostedOpenShifts table...
			final ArrayList<String> employeeIds = new ArrayList<>();
			employeeIds.add(shift1After.getEmployeeId());
			employeeIds.add(shift2After.getEmployeeId());


			// Get any constraint overrides needed for reevaluating eligibility for PostedOpenShifts... 
			Map<String, Map<ConstraintOverrideType, Boolean>> tmpIndividualEmpConstraintOverrideOpts = null;
			PostedOpenShift postedOS1 = postedOpenShiftService.getPostedOpenShiftByEmployeeAndShift(
					shift1After.getTenantId(), shift1After.getEmployeeId(), shift1After.getId());
			PostedOpenShift postedOS2 = postedOpenShiftService.getPostedOpenShiftByEmployeeAndShift(
					shift2After.getTenantId(), shift2After.getEmployeeId(), shift2After.getId());
			if (postedOS1 != null) {
				Map<ConstraintOverrideType, Boolean> empOverrideOptions = postedOS1.getOverrideOptions();
				tmpIndividualEmpConstraintOverrideOpts = new HashMap<>();
				tmpIndividualEmpConstraintOverrideOpts.put(shift1After.getEmployeeId(), empOverrideOptions);
			}
			if (postedOS2 != null) {
				Map<ConstraintOverrideType, Boolean> empOverrideOptions = postedOS2.getOverrideOptions();
				tmpIndividualEmpConstraintOverrideOpts = new HashMap<>();
				tmpIndividualEmpConstraintOverrideOpts.put(shift2After.getEmployeeId(), empOverrideOptions);
			}
			final Map<String, Map<ConstraintOverrideType, Boolean>> individualEmpConstraintOverrideOpts =
                    tmpIndividualEmpConstraintOverrideOpts;
            try {
                scheduleServiceEJB.updateScheduleEmployeesPostedOpenShifts(schedulePk, employeeIds,
                        individualEmpConstraintOverrideOpts);
            } catch (Throwable e) {
                qualificationRequestSummary.setIsSuccess(false);
                qualificationRequestSummary.setMessage(e.getMessage());
                logger.info("Exception while checking if any PostedOpenShift table updates are needed"
                        + " after most recent swap involving schedule  " + schedulePk.getId()
                        + " and employees" + employeeIds);
            }

            if (managerAccount != null) {
            	// PLACEHOLDER FOR SWAP NOTIFICATION
            	// note that notifications to ech employees must be  sent    
            	sendScheduleChangeNotification(shift1Before, NotificationCategory.SHIFT_SWAP, shift2Before,
                        managerAccount);
            	sendScheduleChangeNotification(shift2Before, NotificationCategory.SHIFT_SWAP, shift1Before,
                        managerAccount);
            }
            getEventService().sendEntityUpdateEvent(shift1After, com.emlogis.model.schedule.dto.ShiftDto.class);
            getEventService().sendEntityUpdateEvent(shift2After, com.emlogis.model.schedule.dto.ShiftDto.class);
            qualificationRequestSummary.setFullyQualified(true);
            return qualificationRequestSummary;
        }
	}

	/**
	 * Checks employee eligibility and makes call to apply any needed updates to PostedOpenShifts
	 * @param schedulePk
	 * @param employeeIds
	 * @throws IllegalAccessException
	 */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void updateScheduleEmployeesPostedOpenShifts(PrimaryKey schedulePk, ArrayList<String> employeeIds, 
			Map<String, Map<ConstraintOverrideType, Boolean>> individualEmpConstraintOverrideOpts)
            throws IllegalAccessException {
		// TODO: Revisit the hardcoded time limit getOpenShfitEligibility arguments.
		// TODO: This should be fast enough for just one or two employee, but if performance suffers, then 
		// consider doing this work asynchronously so that caller(s) can go on about their business
		QualificationRequestTracker reqTracker = this.getOpenShiftEligibility(
				schedulePk, employeeIds, 
				null,  // null shiftIds list, so all of schedule's open shifts will be considered 
				120,   // maxComputationTime 
				60,    // maxUnimprovedSecondsSpent 
				130,   // maxSynchronousWaitSeconds
				true, null, individualEmpConstraintOverrideOpts);

		scheduleServiceEJB.applyEligibilityToPostedOS(schedulePk, employeeIds, reqTracker);
	}

	/**
	/** Transactional (TransactionAttributeType.REQUIRES_NEW) utility method to 
	 * apply any needed updates to PostedOpenShift based upon eligibility check
	 * @param schedulePk
	 * @param employeeIds
	 * @param reqTracker
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void applyEligibilityToPostedOS(PrimaryKey schedulePk,
			ArrayList<String> employeeIds,
			QualificationRequestTracker reqTracker) {
		Collection<PostedOpenShift> postedOpenShifts = new ArrayList<>();
		for (String employeeId : employeeIds) {
			postedOpenShifts.addAll(postedOpenShiftService.getPostedOpenShiftsOfEmployeeSchedule(schedulePk, employeeId));
		}

		Collection<ShiftQualificationDto> qualShifts = reqTracker.getQualificationShifts();
		for (ShiftQualificationDto qualShift : qualShifts){
			if (!qualShift.getIsAccepted()) {
				for (PostedOpenShift postedOpenShift : postedOpenShifts) {
					if (qualShift.getShiftId().equals(postedOpenShift.getShiftId())
							&& qualShift.getEmployeeId().equals(postedOpenShift.getEmployeeId())) {
						postedOpenShiftService.delete(postedOpenShift);
					}
				}
			}
		}
	}

	private Set<Shift> generateShiftsFromShiftPattern(Schedule schedule, Collection<Shift> preservedShifts,
                                                      UserAccount managerAccount) {
        Set<Shift> result = new HashSet<>();

        List<ShiftInfo> shiftInfos = getShiftInfoFromShiftPattern(schedule.getTenantId(), schedule.getId());

        for (ShiftInfo shiftInfo : shiftInfos) {
            Date startDate = shiftInfo.cdDate == null || shiftInfo.cdDate.getTime() <= Constants.DATE_2000_01_01
                    ? new Date(schedule.getStartDate() + shiftInfo.dayOffset * 24L * 60 * 60 * 1000)
                    : shiftInfo.cdDate;

            long shiftStartDate = startDate.getTime() + shiftInfo.startTime.getTime();
            long shiftEndDate = shiftStartDate + shiftInfo.lengthInMin * 60 * 1000L;

            for (int i = 0; i < shiftInfo.employeeCount + shiftInfo.excessCount; i++) {
                Shift shift = new Shift(new PrimaryKey(schedule.getTenantId()));

                shift.setStartDateTime(shiftStartDate);
                shift.setEndDateTime(shiftEndDate);

                shift.setScheduleId(schedule.getId());

                shift.setShiftPatternId(shiftInfo.shiftPatternId);
                shift.setTeamId(shiftInfo.teamId);
                shift.setTeamName(shiftInfo.teamName);
                shift.setSkillId(shiftInfo.skillId);
                shift.setSkillName(shiftInfo.skillName);
                shift.setSkillAbbrev(shiftInfo.skillAbbrev);
                shift.setSiteName(shiftInfo.siteName);
//                shift.setShiftReqId(shiftInfo.shiftReqId);

                shift.setShiftLengthId(shiftInfo.shiftLengthId);
                shift.setShiftLength(shiftInfo.lengthInMin);
                shift.setShiftLengthName(shiftInfo.shiftLengthName);
                shift.setPaidTime(shiftInfo.paidTimeInMin);

                shift.setExcess(i >= shiftInfo.employeeCount);

                shift.setEmployeeIndex(i);

                Shift matchingShift = getMatchingShift(preservedShifts, shift);
                if (matchingShift != null) {
                    shift.setLocked(true);

                    long assigned = matchingShift.getAssigned().getMillis();
                    if (schedule.isPreservePreAssignedShifts() && schedule.getExecutionStartDate() > assigned) {
                    	shift.makeShiftAssignment(matchingShift.getEmployeeId(),
                                matchingShift.getEmployeeName(), AssignmentType.MANUAL, matchingShift.getAssigned());
                    } else if (schedule.isPreservePostAssignedShifts() && schedule.getExecutionEndDate() < assigned) {
                    	shift.makeShiftAssignment(matchingShift.getEmployeeId(),
                                matchingShift.getEmployeeName(), AssignmentType.MANUAL, matchingShift.getAssigned());
                    } else if (schedule.isPreserveEngineAssignedShifts() && schedule.getExecutionEndDate() >= assigned
                            && schedule.getExecutionStartDate() <= assigned) {
                    	shift.makeShiftAssignment(matchingShift.getEmployeeId(),
                                matchingShift.getEmployeeName(), AssignmentType.ENGINE, matchingShift.getAssigned());
                    } else {
                        // normally this section will never be reached
                    	shift.makeShiftAssignment(matchingShift.getEmployeeId(), matchingShift.getEmployeeName(),
                                matchingShift.getAssignmentType(), matchingShift.getAssigned());
                    }
                }

                result.add(shift);
                shiftService.insert(shift);
                scheduleChangeService.trackShiftAddChange(shift, schedule, null, managerAccount, null);
            }
        }

        return result;
    }

    private Set<Shift> generateShiftsFromShiftStructure(Schedule schedule, Collection<Shift> preservedShifts,
                                                        UserAccount managerAccount) {
        Set<Shift> result = new HashSet<>();

        Set<ShiftStructure> shiftStructures = schedule.getShiftStructures();
        Map<String, ShiftStructure> shiftStructureMap = new HashMap<>();
        for (ShiftStructure shiftStructure : shiftStructures) {
            shiftStructureMap.put(shiftStructure.getId(), shiftStructure);
        }

        List<ShiftInfo> shiftInfos = getShiftInfoFromShiftStructure(schedule.getTenantId(), schedule.getId());
        for (ShiftInfo shiftInfo : shiftInfos) {
            ShiftStructure shiftStructure = shiftStructureMap.get(shiftInfo.shiftStructureId);

            Date structureStartDate = new Date(shiftStructure.getStartDate());
            Date shiftDate = DateUtils.addDays(structureStartDate, shiftInfo.dayIndex);
            long shiftStartDate = shiftDate.getTime() + shiftInfo.startTime.getTime();
            long shiftEndDate = shiftStartDate + shiftInfo.durationInMins * 60 * 1000L;

            for (int i = 0; i < shiftInfo.employeeCount; i++) {
                Shift shift = new Shift(new PrimaryKey(schedule.getTenantId()));

                shift.setStartDateTime(shiftStartDate);
                shift.setEndDateTime(shiftEndDate);
                shift.setScheduleId(schedule.getId());
                shift.setShiftStructureId(shiftStructure.getId());
                shift.setTeamId(shiftStructure.getTeam().getId());
                shift.setTeamName(shiftStructure.getTeam().getName());
                shift.setSkillProficiencyLevel(shiftInfo.skillProficiencyLevel);
                shift.setShiftLengthId(shiftInfo.shiftLengthId);
//                shift.setPaidTime(shiftInfo.paidTimeInMin);
                shift.setShiftLength(shiftInfo.lengthInMin);
                shift.setShiftLengthName(shiftInfo.shiftLengthName);
                shift.setSkillId(shiftInfo.skillId);
                shift.setSkillName(shiftInfo.skillName);
                shift.setSkillAbbrev(shiftInfo.skillAbbrev);
                shift.setSiteName(shiftInfo.siteName);
                shift.setExcess(shiftInfo.excess);
                shift.setEmployeeIndex(i);
//                shift.setShiftReqId(shiftInfo.shiftReqId);

                Shift matchingShift = getMatchingShift(preservedShifts, shift);
                if (matchingShift != null) {
                	shift.makeShiftAssignment(matchingShift.getEmployeeId(), matchingShift.getEmployeeName(),
                            matchingShift.getAssignmentType(), matchingShift.getAssigned());
                }

                result.add(shift);
                shiftService.insert(shift);
                scheduleChangeService.trackShiftAddChange(shift, schedule, null, managerAccount, null);
            }
        }
        return result;
    }

    private List<ShiftInfo> getShiftInfoFromShiftStructure(String tenantId, String scheduleId) {
        String sql =
            "SELECT " +
            "    sr.shiftStructureId, " +
            "    sr.startTime, " +
            "    sr.dayIndex, " +
            "    sr.durationInMins, " +
            "    sr.employeeCount, " +
            "    sr.skillProficiencyLevel, " +
            "    sr.shiftLengthId, " +
            "    sl.name shiftLengthName, " +
//            "    sl.paidTimeInMin, " +   -> paidTimeInMin has moved to ShiftType
            "    sl.lengthInMin, " +
            "    sr.excess, " +
            "    sr.skillId, " +
            "    sr.skillName, " +
            "    s.abbreviation skillAbbrev, " +
            "    site.name siteName," +
            "    sr.id shiftReqId " +
            "  FROM ShiftStructure_Schedule sss " +
            "       JOIN ShiftStructure_ShiftReqOld sssr ON sss.ShiftStructure_tenantId = sssr.ShiftStructure_tenantId " +
            "                                           AND sss.ShiftStructure_id = sssr.ShiftStructure_id " +
            "       LEFT JOIN ShiftReqOld sr ON sssr.ShiftStructure_tenantId = sr.tenantId AND sssr.shiftReqs_id = sr.id " +
            "       LEFT JOIN ShiftLength sl ON sr.shiftLengthId = sl.id AND sr.shiftLengthTenantId = sl.tenantId " +
            "       LEFT JOIN Skill s ON sr.skillId = s.id AND sr.tenantId = s.tenantId " +
            "       LEFT JOIN ShiftStructure ss ON sss.ShiftStructure_id = ss.id AND sss.ShiftStructure_tenantId = ss.tenantId " +
            "       LEFT JOIN Team t ON ss.teamId = t.id AND ss.tenantId = t.tenantId " +
            "       LEFT JOIN AOMRelationship r ON t.id = r.dst_id AND t.tenantId = r.dst_tenantId " +
            "       LEFT JOIN Site site ON r.src_id = site.id AND r.src_tenantId = site.tenantId " +
            " WHERE sss.schedules_id = :scheduleId " +
            "   AND sss.schedules_tenantId = :tenantId ";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);
        query.setParameter("scheduleId", scheduleId);

        List<ShiftInfo> result = new ArrayList<>();
        List<Object[]> objects = query.getResultList();
        for (Object[] obj : objects) {
            int i = 0;
            ShiftInfo shiftInfo = new ShiftInfo();

            shiftInfo.shiftStructureId = (String) obj[i++];
            shiftInfo.startTime = (Time) obj[i++];
            shiftInfo.dayIndex = (Integer) obj[i++];
            shiftInfo.durationInMins = (Integer) obj[i++];
            shiftInfo.employeeCount = (Integer) obj[i++];
            shiftInfo.skillProficiencyLevel = (Integer) obj[i++];
            shiftInfo.shiftLengthId = (String) obj[i++];
            shiftInfo.shiftLengthName = (String) obj[i++];
//            shiftInfo.paidTimeInMin = (Integer) obj[i++];
            shiftInfo.lengthInMin = (Integer) obj[i++];
            shiftInfo.excess = (Boolean) obj[i++];
            shiftInfo.skillId = (String) obj[i++];
            shiftInfo.skillName = (String) obj[i++];
            shiftInfo.skillAbbrev = (String) obj[i++];
            shiftInfo.siteName = (String) obj[i++];
            shiftInfo.shiftReqId = (String) obj[i];

            result.add(shiftInfo);
        }

        return result;
    }

    private List<ShiftInfo> getShiftInfoFromShiftPattern(String tenantId, String scheduleId) {
        String sql =
                "SELECT " +
                "    sr.shiftPatternId, " +
                "    pe.cdDate, " +
                "    pe.dayOffset, " +
                "    st.startTime, " +
                "    sr.employeeCount, " +
                "    st.shiftLengthId, " +
                "    sl.name shiftLengthName, " +
                "    st.paidTimeInMin, " +
                "    sl.lengthInMin, " +
                "    sr.excessCount, " +
                "    sp.skillId, " +
                "    s.name skillName, " +
                "    s.abbreviation skillAbbrev, " +
                "    sp.teamId, " +
                "    t.name teamName, " +
                "    site.name siteName, " +
                "    sr.id shiftReqId " +
                "  FROM PatternElt pe " +
                "       LEFT JOIN ShiftPattern sp " +
                "                     ON pe.shiftPatternId = sp.id AND pe.shiftPatternTenantId = sp.tenantId " +
                "       LEFT JOIN ShiftReq sr " +
                "                     ON sp.id = sr.shiftPatternId AND sp.tenantId = sr.shiftPatternTenantId " +
                "       LEFT JOIN ShiftType st " +
                "                     ON sr.shiftTypeId = st.id AND sr.shiftTypeTenantId = st.tenantId " +
                "       LEFT JOIN ShiftLength sl " +
                "                     ON st.shiftLengthId = sl.id AND st.shiftLengthTenantId = sl.tenantId " +
                "       LEFT JOIN Skill s ON sp.skillId = s.id AND sp.tenantId = s.tenantId " +
                "       LEFT JOIN Team t ON sp.teamId = t.id AND sp.tenantId = t.tenantId " +
                "       LEFT JOIN AOMRelationship r ON t.id = r.dst_id AND t.tenantId = r.dst_tenantId " +
                "       LEFT JOIN Site site ON r.src_id = site.id AND r.src_tenantId = site.tenantId " +
                " WHERE pe.scheduleId = :scheduleId AND pe.tenantId = :tenantId ";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);
        query.setParameter("scheduleId", scheduleId);

        List<ShiftInfo> result = new ArrayList<>();
        List<Object[]> objects = query.getResultList();
        for (Object[] obj : objects) {
            int i = 0;
            ShiftInfo shiftInfo = new ShiftInfo();

            shiftInfo.shiftPatternId = (String) obj[i++];
            shiftInfo.cdDate = (Date) obj[i++];
            shiftInfo.dayOffset = (Integer) obj[i++];
            shiftInfo.startTime = (Time) obj[i++];
            shiftInfo.employeeCount = (Integer) obj[i++];
            shiftInfo.shiftLengthId = (String) obj[i++];
            shiftInfo.shiftLengthName = (String) obj[i++];
            shiftInfo.paidTimeInMin = (Integer) obj[i++];
            shiftInfo.lengthInMin = (Integer) obj[i++];
            shiftInfo.excessCount = (Integer) obj[i++];
            shiftInfo.skillId = (String) obj[i++];
            shiftInfo.skillName = (String) obj[i++];
            shiftInfo.skillAbbrev = (String) obj[i++];
            shiftInfo.teamId = (String) obj[i++];
            shiftInfo.teamName = (String) obj[i++];
            shiftInfo.siteName = (String) obj[i++];
            shiftInfo.shiftReqId = (String) obj[i];

            result.add(shiftInfo);
        }

        return result;
    }

    private Shift getMatchingShift(Collection<Shift> shifts, Shift shiftForMatch) {
        for (Shift shift : shifts) {
            if (StringUtils.equals(shift.getSkillId(), shiftForMatch.getSkillId())
                    && StringUtils.equals(shift.getTeamId(), shiftForMatch.getTeamId())
                    && StringUtils.equals(shift.getShiftLengthId(), shiftForMatch.getShiftLengthId())
                    && StringUtils.equals(shift.getShiftPatternId(), shiftForMatch.getShiftPatternId())
                    && StringUtils.equals(shift.getShiftStructureId(), shiftForMatch.getShiftStructureId())
                    && StringUtils.equals(shift.getScheduleId(), shiftForMatch.getScheduleId())
                    && shift.getSkillProficiencyLevel() == shiftForMatch.getSkillProficiencyLevel()
                    && shift.isExcess() == shiftForMatch.isExcess()
                    && shift.getEmployeeIndex() == shiftForMatch.getEmployeeIndex()
                    && EmlogisUtils.equals(shift.getStartDateTime(), shiftForMatch.getStartDateTime())
                    && EmlogisUtils.equals(shift.getEndDateTime(), shiftForMatch.getEndDateTime())) {
                return shift;
            }
        }
        return null;
    }

	private void notifyProgress(Schedule schedule, String progressInfo) {
		ASEventService eventService = getEventService();
        Object key = new EventKeyBuilder().setTopic(EventService.TOPIC_SYSTEM_NOTIFICATION).setTenantId(schedule.getTenantId())
                .setEntityClass("Schedule").setEventType("Progress").setEntityId(schedule.getId()).build();
        Map<String,Object> eventBody = new HashMap<>();
        eventBody.put("progress", 0);
        eventBody.put("hardScore", 0);
        eventBody.put("softScore", 0);
        eventBody.put("msg", progressInfo);
        try {
			eventService.sendEvent(EventScope.AppServer, key, Event.wrap(eventBody),"SchedulingService");
		} catch (Throwable t) {
            logger.error("notifyProgress", t);
		}
    }

    public ResultSet<Object[]> query(String tenantId,
                                     Collection<String> sites,
                                     Collection<String> teams,
                                     Collection<ScheduleStatus> statuses,
                                     Collection<TaskState> states,
                                     ScheduleType scheduleType,
                                     int scheduleLengthInDays,
                                     String search,
                                     long startDate,
                                     ScheduleQueryDto.ScheduleDate scheduleDate,
                                     int offset,
                                     int limit,
                                     String orderBy,
                                     String orderDir,
                                     AccountACL acl) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        String aclFilter = null;

        if (teams != null && teams.size() > 0) {
            String teamsFilter = ModelUtils.commaSeparatedQuotedValues(teams);

            SimpleQuery simpleQuery = new SimpleQuery(tenantId);
            simpleQuery.setEntityClass(Team.class);
            simpleQuery.setFilter("primaryKey.id IN (" + teamsFilter + ")");
            ResultSet<Team> teamResultSet = teamService.findTeams(simpleQuery, acl);

            Collection<Team> teamCollection = teamResultSet.getResult();
            if (teamCollection!= null && teamCollection.size() > 0) {
                aclFilter = "t.id IN (" + ModelUtils.commaSeparatedQuotedIds(teamCollection) + ")";
            }
        } else if (sites != null && sites.size() > 0) {
            String sitesClause = ModelUtils.commaSeparatedQuotedValues(sites);

            SimpleQuery simpleQuery = new SimpleQuery(tenantId);
            simpleQuery.setEntityClass(Site.class);
            simpleQuery.setFilter("primaryKey.id IN (" + sitesClause + ")");
            ResultSet<Site> siteResultSet = siteService.findSites(simpleQuery, acl);

            Collection<Site> siteCollections = siteResultSet.getResult();
            if (siteCollections!= null && siteCollections.size() > 0) {
                aclFilter = "site.id IN (" + ModelUtils.commaSeparatedQuotedIds(siteCollections) + ")";
            }

            simpleQuery = new SimpleQuery(tenantId);
            simpleQuery.setEntityClass(Team.class);
            ResultSet<Team> teamResultSet = teamService.findTeams(simpleQuery, acl);

            Collection<Team> teamCollection = teamResultSet.getResult();
            if (teamCollection!= null && teamCollection.size() > 0) {
                if (StringUtils.isEmpty(aclFilter)) {
                    aclFilter = " t.id IN (" + ModelUtils.commaSeparatedQuotedIds(teamCollection) + ")";
                } else {
                    aclFilter += " AND t.id IN (" + ModelUtils.commaSeparatedQuotedIds(teamCollection) + ")";
                }
            }
        } else {
            SimpleQuery simpleQuery = new SimpleQuery(tenantId);
            simpleQuery.setEntityClass(Team.class);
            ResultSet<Team> teamResultSet = teamService.findTeams(simpleQuery, acl);

            Collection<Team> teamCollection = teamResultSet.getResult();
            if (teamCollection!= null && teamCollection.size() > 0) {
                aclFilter = "t.id IN (" + ModelUtils.commaSeparatedQuotedIds(teamCollection) + ")";
            }
        }

        String statusFilter = ModelUtils.commaSeparatedEnumOrdinals(statuses);
        if (StringUtils.isNotEmpty(statusFilter)) {
            statusFilter = " s.status IN (" + statusFilter + ")";
        }

        String stateFilter = ModelUtils.commaSeparatedEnumOrdinals(states);
        if (StringUtils.isNotEmpty(stateFilter)) {
            stateFilter = " s.state IN (" + stateFilter + ")";
        }

        String searchFilter = null;
        if (StringUtils.isNotBlank(search)) {
            searchFilter = "(LOWER(s.name) LIKE LOWER('%" + search + "%') " +
                    "OR LOWER(s.description) LIKE LOWER('%" + search + "%') " +
                    "OR LOWER(t.name) LIKE LOWER('%" + search + "%') " +
                    "OR LOWER(site.name) LIKE LOWER('%" + search + "%')) ";
        }

        String startDateFilter = null;
        long cutStartDate = 0;
        if (startDate > 0) {
            if (ScheduleQueryDto.ScheduleDate.START.equals(scheduleDate)) {
                startDateFilter = "(s.startDate >= :startDateBegin AND s.startDate < :startDateEnd)";
                cutStartDate = ModelUtils.cutDateTimeToDate(startDate);
            } else {
                startDateFilter = "(s.startDate <= :scheduleDate AND s.endDate >= :scheduleDate)";
            }
        }

        String sql =
            "SELECT s.id, s.name, s.description, s.startDate, s.endDate, s.scheduleLengthInDays, s.status, s.state, " +
            "       CONCAT(site.id, ':', site.name) sites, " +
            "       GROUP_CONCAT(DISTINCT t.id, ':', t.name SEPARATOR ',') teams " +
            "  FROM Schedule s " +
            "    LEFT JOIN Team_Schedule ts ON ts.schedules_id = s.id AND ts.schedules_tenantId = s.tenantId " +
            "    LEFT JOIN Team t ON ts.Team_id = t.id AND ts.Team_tenantId = t.tenantId " +
            "    LEFT JOIN AOMRelationship r ON t.id = r.dst_id AND t.tenantId = r.dst_tenantId " +
            "    LEFT JOIN Site site ON r.src_id = site.id AND r.src_tenantId = site.tenantId " +
            " WHERE s.tenantId = :tenantId " +
            (StringUtils.isEmpty(aclFilter) ? "" : " AND " + aclFilter) +
            (StringUtils.isEmpty(statusFilter) ? "" : " AND " + statusFilter) +
            (StringUtils.isEmpty(stateFilter) ? "" : " AND " + stateFilter) +
            (scheduleType == null ? "" : " AND s.scheduleType = " + scheduleType.ordinal() + " ") +
                    (scheduleLengthInDays > 0 ? " AND s.scheduleLengthInDays = " + scheduleLengthInDays + " " : "") +
            (StringUtils.isEmpty(searchFilter) ? "" : " AND " + searchFilter) +
            (StringUtils.isEmpty(startDateFilter) ? "" : " AND " + startDateFilter) +
            " GROUP BY s.id ";

        String countSql = "SELECT COUNT(*) FROM (" + sql + ") x";
        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter("tenantId", tenantId);
        if (StringUtils.isNotEmpty(startDateFilter)) {
            if (ScheduleQueryDto.ScheduleDate.START.equals(scheduleDate)) {
                countQuery.setParameter("startDateBegin", new Timestamp(cutStartDate));
                countQuery.setParameter("startDateEnd", new Timestamp(cutStartDate + Constants.DAY_MILLISECONDS));
            } else {
                countQuery.setParameter("scheduleDate", new Timestamp(startDate));
            }
        }

        if (StringUtils.isNotBlank(orderBy)) {
            if (orderBy.startsWith("Schedule.")) {
                orderBy = orderBy.replaceFirst("Schedule.", "s.");
            } else {
                orderBy = "s." + orderBy;
            }
            sql += " ORDER BY " + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);
        if (StringUtils.isNotEmpty(startDateFilter)) {
            if (ScheduleQueryDto.ScheduleDate.START.equals(scheduleDate)) {
                query.setParameter("startDateBegin", new Timestamp(cutStartDate));
                query.setParameter("startDateEnd", new Timestamp(cutStartDate + Constants.DAY_MILLISECONDS));
            } else {
                query.setParameter("scheduleDate", new Timestamp(startDate));
            }
        }
        if (offset > -1) {
            query.setFirstResult(offset);
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        ResultSet<Object[]> result = new ResultSet<>();
        result.setResult(query.getResultList());
        result.setTotal(((BigInteger) countQuery.getSingleResult()).intValue());

        return result;
    }

	/** Transactional (TransactionAttributeType.REQUIRES_NEW) utility method to get
     *  aggregation of detached schedule, employee, and shift entities.
     *  NOTE: Public access for calling through business interface
     *        so that transaction attribute will be honored.
     * 
     * @param schedulePk
     * @param qualificationExecuteDto
     * @return
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public QualificationEntitiesAggregation getDetachedQualificationEntities(
			PrimaryKey schedulePk,
			QualificationExecuteDto qualificationExecuteDto) {
    	Schedule schedule = null;
    	if (schedulePk != null) {
    		schedule = getSchedule(schedulePk);
    		if (schedule == null) {
	            throw new ValidationException(sessionService.getMessage("validation.schedule.shift.noschedule",
                        schedulePk.getId()));
    		}
    	}

		QualificationEntitiesAggregation qualificationEntitiesAggregation = new QualificationEntitiesAggregation();
		qualificationEntitiesAggregation.schedule = schedule;

		String tenantId = schedule.getTenantId();
		for (QualificationAssignment qualificationAssignment : qualificationExecuteDto.getQualificationAssignments()) {
			String shiftId = qualificationAssignment.getShiftId();
			PrimaryKey shiftPk = new PrimaryKey( tenantId, shiftId );
			Shift shift = shiftService.getShift(shiftPk);
			if (shift != null) {
				String employeeId = qualificationAssignment.getEmployeeId();
				PrimaryKey employeePk = new PrimaryKey( tenantId, employeeId );
				Employee employee = employeeService.getEmployee(employeePk);

				// assign the shift as designated by the client
				if (employee != null) {
					shift.makeShiftAssignment(employeeId, getEmployeeFullName(employee), AssignmentType.MANUAL);
				} else {
		            throw new ValidationException(sessionService.getMessage("validation.schedule.shift.noemployee",
                            employeeId));
				}
			} else {
	            throw new ValidationException(sessionService.getMessage("validation.schedule.shift.noshift", shiftId));					
			}			

			qualificationEntitiesAggregation.shifts.add(shift);
		}
		
		entityManager.clear();  // to detach all entities from the persistence context
		return qualificationEntitiesAggregation;
    }

	/** Transactional (TransactionAttributeType.REQUIRES_NEW) utility method to get
     *  aggregation of detached schedule, employee, and shift entities.
     *  NOTE: Public access for calling through business interface
     *        so that transaction attribute will be honored.
     * 
     * @param schedulePk
     * @param employeeIds
     * @param shiftIds
     * @return
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public OpenShiftEligibilityEntitiesAggregation getDetachedOpenShiftEligibilityEntities(PrimaryKey schedulePk,
                                                                         List<String> employeeIds,
                                                                         List<String> shiftIds) {
		List<Employee> employees = new ArrayList<>();
		if (employeeIds != null) {
			for (String employeeId : employeeIds) {
				PrimaryKey employeePk = new PrimaryKey(schedulePk.getTenantId(), employeeId);
				Employee employee = employeeService.getEmployee(employeePk);
				if (employee != null) {
					for (EmployeeSkill employeeSkill : employee.getEmployeeSkills()) {
						// Just iterating to simulate eager fetch
					}
					employee.getEmployeeContracts().toArray();
					employee.getEmployeeTeams().toArray();
					employees.add(employee);
				} else {
		            throw new ValidationException(sessionService.getMessage("validation.schedule.shift.noemployee",
                            employeePk.getId()));
				}
			}
		}
		
		List<Shift> shifts = new ArrayList<>();
		if (shiftIds != null) {
			for (String shiftId : shiftIds) {
				PrimaryKey shiftPk = new PrimaryKey(schedulePk.getTenantId(), shiftId);
				Shift shift = shiftService.getShift(shiftPk);
				if (shift != null) {
					shifts.add(shift);
				} else {
		            throw new ValidationException(sessionService.getMessage("validation.schedule.shift.noshift",
                            shiftPk.getId()));
				}
			}
		}
		
		OpenShiftEligibilityEntitiesAggregation eligibilityInputsAggregation =
                new OpenShiftEligibilityEntitiesAggregation();
		eligibilityInputsAggregation.employees = employees;
		eligibilityInputsAggregation.shifts = shifts;
		entityManager.clear();  // to detach all entities from the persistence context
		return eligibilityInputsAggregation;
    }
    
    /** Transactional (TransactionAttributeType.REQUIRES_NEW) utility method to get detached shifts.
     *  NOTE: Public access for calling through business interface
     *        so that transaction attribute will be honored.
     * 
     * @param shiftPks
     * @return
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<Shift> getDetachedShifts(List<PrimaryKey> shiftPks) {
    	List<Shift> detachedShifts = new ArrayList<>();
    	for (PrimaryKey shiftPk : shiftPks){
			Shift shift = shiftService.getShift(shiftPk);
    		if (shift != null) {
    			detachedShifts.add(shift); 
			} else {
	            throw new ValidationException(sessionService.getMessage("validation.schedule.shift.noshift",
                        shiftPk.getId()));
			}
    	}
    	entityManager.clear();
    	return detachedShifts;
    }
	
    /** Transactional (TransactionAttributeType.REQUIRES_NEW) utility method to get detached shifts.
     *  NOTE: Public access for calling through business interface
     *        so that transaction attribute will be honored.
     * 
     * @param shiftPk
     * @return
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public PrimaryKey getSchedulePkFromShiftPk(PrimaryKey shiftPk) {
		Shift shift = shiftService.getShift(shiftPk);
		return new PrimaryKey(shift.getTenantId(), shift.getScheduleId());
    }
	
	/** Transactional (TransactionAttributeType.REQUIRES_NEW) utility method to update (merge) shifts 
	 *  if they remain clean.
     *  NOTE: Public access for calling through business interface
     *        so that transaction attribute will be honored.
	 * 
	 * @param shiftsBefore
	 * @param shiftsAfter
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void updateDetachedShifts(List<Shift> shiftsBefore, List<Shift> shiftsAfter) {
		boolean dirtyData = false;
		for (Shift beforeShift : shiftsBefore) {
			Shift existingShift = shiftService.getShift(beforeShift.getPrimaryKey());
			if (!shiftsAreEquivalent(beforeShift, existingShift)) {
				dirtyData = true;
			}
		}
		
		if (!dirtyData) {
			for (Shift afterShift : shiftsAfter) {
				shiftService.update(afterShift);
			}
		} else {
            throw new ValidationException(sessionService.getMessage("validation.schedule.shift.edit.dirtydata"));
		}
	}

	/** Transactional (TransactionAttributeType.REQUIRES_NEW) utility 
	 *  method to obtain full name of employee.
     *  NOTE: Public access for calling through business interface
     *        so that transaction attribute will be honored.
     * 
	 * @param employeePk
	 * @return
	 */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String getEmployeeFullName( PrimaryKey employeePk ){
		Employee employee = employeeService.getEmployee( employeePk );

    	if (employee != null) {
			StringBuilder employeeName = new StringBuilder();
			employeeName.append(employee.getFirstName());
			if (employee.getMiddleName() != null) {
				employeeName.append(" " + employee.getMiddleName());
			}
			employeeName.append(" " + employee.getLastName());
			return employeeName.toString();
		} else {
            throw new ValidationException(sessionService.getMessage("validation.schedule.shift.noemployee",
                    employeePk.getId()));
		}
	}
	
	/** Utility method to obtain full name of employee.
	 * 
	 * @param employee
	 * @return
	 */
	private String getEmployeeFullName( Employee employee ){
    	StringBuilder employeeName = new StringBuilder();
    	employeeName.append(employee.getFirstName());
    	if (employee.getMiddleName() != null) {
            employeeName.append(" " + employee.getMiddleName());
        }
    	employeeName.append(" " + employee.getLastName());
    	return employeeName.toString();
	}

	
	/** Utility method to test if two shifts have equivalent key properties
	 *  of team, skill, employee, startDate , and endDate.
	 * 
	 * @param shift1
	 * @param shift2
	 * @return
	 */
	private boolean shiftsAreEquivalent(Shift shift1, Shift shift2) {
        return StringUtils.equals(shift1.getEmployeeId(), shift2.getEmployeeId())
                && StringUtils.equals(shift1.getSkillId(), shift2.getSkillId())
                && StringUtils.equals(shift1.getTeamId(), shift2.getTeamId())
                && shift1.getStartDateTime().equals(shift2.getStartDateTime())
                && shift1.getEndDateTime().equals(shift2.getEndDateTime());
	}

	/** Utility method to have the thread wait on, and return, qualification results.
	 * 
	 * @param requestId
	 * @return
	 */
	private QualificationResultDto getQualificationResults(String requestId, int maxSynchronousWaitSeconds) {
		ResultMonitor resultMonitor = new ResultMonitor();
		responseHandlerService.putResultMonitor(requestId, resultMonitor);
		synchronized(resultMonitor) {
			if (!resultMonitor.getWasSignaled()) {
				try {
					resultMonitor.wait(maxSynchronousWaitSeconds * 1000);
				} catch (InterruptedException e) {
					logger.info("Thread interrupted while waiting for engine qualification "
							+ "response. Logging for information purposes... " + e.toString());
				}							
			}
		}

        return (QualificationResultDto) resultMonitor.getResult();
	}
	
	
	/** Utility method to have the thread wait on, and return, shift swap eligibility results.
	 * 
	 * @param requestId
	 * @return
	 */
	private ShiftSwapEligibilityResultDto getShiftSwapEligibilityResults(String requestId,
                                                                         int maxSynchronousWaitSeconds) {
		ResultMonitor resultMonitor = new ResultMonitor();
		responseHandlerService.putResultMonitor(requestId, resultMonitor);
		synchronized(resultMonitor) {
			if (!resultMonitor.getWasSignaled()) {
				try {
					resultMonitor.wait(maxSynchronousWaitSeconds * 1000);
				} catch (InterruptedException e) {
					logger.info("Thread interrupted while waiting for engine qualification "
							+ "response. Logging for information purposes... " + e.toString());
				}							
			}
		}

        return (ShiftSwapEligibilityResultDto) resultMonitor.getResult();
	}
	
	
	public QualificationRequestTracker getQualificationRequestTracker(String requestId){
		return hazelcastClientService.getQualificationRequestTracker(requestId);
	}

	public ShiftSwapEligibilityRequestTracker getShiftSwapEligibilityRequestTracker(String requestId){
		return hazelcastClientService.getShiftSwapEligibilityRequestTracker(requestId);
	}

    /** Get result from synchronously processed open shift eligibility execution
	 * @param schedulePk
	 * @param employeeIds
	 * @param shiftIds
	 * @param includeDetails
     * @param sharedEmployeeOverrideOptions 
     * @param specificEmployeeOverrideOptions 
	 * @return requestTracker
	 */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public QualificationRequestTracker getOpenShiftEligibility(PrimaryKey schedulePk, List<String> employeeIds,
            List<String> shiftIds, int maxComputationTime, int maxUnimprovedSecondsSpent, int maxSynchronousWaitSeconds,
            Boolean includeDetails, Map<ConstraintOverrideType, Boolean> sharedEmployeeOverrideOptions, 
            Map<String, Map<ConstraintOverrideType, Boolean>> specificEmployeeOverrideOptions) throws IllegalAccessException {
		// get detached entities
		OpenShiftEligibilityEntitiesAggregation eligibilityEntities =
                scheduleServiceEJB.getDetachedOpenShiftEligibilityEntities(schedulePk, employeeIds, shiftIds);
		List<Employee> employees = eligibilityEntities.employees;
		List<Shift> shifts = eligibilityEntities.shifts;
		
		// Since all of these shifts are being qualified, we want them to be considered as if 
		// they are open shifts, so let's make them look that way for the engine...
		for (Shift shift : shifts){
			shift.dropShiftAssignment();
		}
		
		// TODO Was passing in list of employees, but ran into lazy init issues so 
		// for now let's instead just get and pass in List of employee pks...
		List<PrimaryKey> employeePks = new ArrayList<>();
		for (Employee employee : employees) {
			employeePks.add(employee.getPrimaryKey());
		}
	
		QualificationRequestTracker requestTracker = scheduleServiceEJB.executeOpenShiftEligibility(schedulePk, 
				maxComputationTime, maxUnimprovedSecondsSpent, employeePks, shifts, null, includeDetails, 
				sharedEmployeeOverrideOptions, specificEmployeeOverrideOptions, true);
		
		QualificationResultDto resultDto = getQualificationResults(requestTracker.getRequestId(), maxSynchronousWaitSeconds);
		requestTracker = getQualificationRequestTracker(requestTracker.getRequestId());  // updating after completion
		
		if (requestTracker != null  &&  resultDto != null) {
			requestTracker.setQualificationShifts(resultDto.getQualifyingShifts());
		} else {
			throw new ValidationException("validation.engine.noresult");
		}
		
		return requestTracker;
	}

    /** Get result from synchronously processed qualification execution
	 * @param schedulePk
	 * @return requestTracker
	 */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public QualificationRequestTracker getQualification(
			PrimaryKey schedulePk,
			QualificationExecuteDto qualificationExecuteDto) throws IllegalAccessException {
		QualificationEntitiesAggregation qualEntities = scheduleServiceEJB.getDetachedQualificationEntities(schedulePk,
                qualificationExecuteDto);
		
		int maxSynchronousWaitSeconds = qualificationExecuteDto.getMaxSynchronousWaitSeconds();
		int maxComputationTime = qualificationExecuteDto.getMaxComputationTime();
		int maximumUnimprovedSecondsSpent = qualificationExecuteDto.getMaximumUnimprovedSecondsSpent();
		QualificationRequestTracker requestTracker = scheduleServiceEJB.executeQualification(maxComputationTime,
                maximumUnimprovedSecondsSpent, qualEntities.shifts, null, null, true);
		QualificationResultDto resultDto = getQualificationResults(requestTracker.getRequestId(),
                maxSynchronousWaitSeconds);
		
		if (resultDto != null) {
			requestTracker.setQualificationShifts(resultDto.getQualifyingShifts());
		} else {
			throw new ValidationException("validation.engine.noresult");
		}
		
		return requestTracker;
	}

    /** Get result from synchronously processed qualification execution
	 * @return requestTracker
	 */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public QualificationRequestTracker getQualification(Collection<String> shiftIds)
            throws IllegalAccessException {
        Collection<Shift> qualificationShifts = shiftService.getShifts(null, shiftIds);

		QualificationRequestTracker requestTracker = scheduleServiceEJB.executeQualification(0, 0, qualificationShifts,
                null, null, true);
		QualificationResultDto resultDto = getQualificationResults(requestTracker.getRequestId(), 0);

		if (resultDto != null) {
			requestTracker.setQualificationShifts(resultDto.getQualifyingShifts());
		} else {
			throw new ValidationException("validation.engine.noresult");
		}

		return requestTracker;
	}

	/** Fire off execution of open shift eligibility for asynchronous processing.
	 * @param schedule
	 * @param employees
	 * @param shifts
	 * @param includeDetails
	 * @return requestId
	 */
	public String executeOpenShiftEligibility(Schedule schedule, List<Employee> employees, List<Shift> shifts, 
			int maxComputationTime, int maxUnimprovedSecondsSpent, Boolean includeDetails)
            throws IllegalAccessException {
		// TODO Was passing in list of employees, but ran into lazy init issues so
		// for now let's instead just get and pass in List of employee pks...
		List<PrimaryKey> employeePks = new ArrayList<>();
		for (Employee employee : employees){
			employeePks.add(employee.getPrimaryKey());
		}

		QualificationRequestTracker requestTracker = executeOpenShiftEligibility(schedule.getPrimaryKey(),
                maxComputationTime, maxUnimprovedSecondsSpent, employeePks, shifts, null, includeDetails, null, null, false);
		
		return requestTracker.getRequestId();
	}

	/** Fire off execution of qualification for asynchronous processing.
	 * @param schedulePk
	 * @param qualificationExecuteDto
	 * @return requestId
	 */
	public String executeQualification(PrimaryKey schedulePk, QualificationExecuteDto qualificationExecuteDto)
            throws IllegalAccessException {
		QualificationEntitiesAggregation qualEntities = scheduleServiceEJB.getDetachedQualificationEntities(schedulePk,
                qualificationExecuteDto);
		int maxComputationTime = qualificationExecuteDto.getMaxComputationTime();
		int maximumUnimprovedSecondsSpent = qualificationExecuteDto.getMaximumUnimprovedSecondsSpent();
		QualificationRequestTracker requestTracker = 
				executeQualification(maxComputationTime, maximumUnimprovedSecondsSpent, qualEntities.shifts, null, null, false);
		return requestTracker.getRequestId();
	}

    public List<Object[]> summaryReport(String scheduleId) {
        String sql =
            "SELECT " +
            "    sp.skillId " +
            "  , sh.skillName " +
            "  , SUM(sh.shiftLength / 60) baseline " +
            "  , SUM(sh1.shiftLength / 60) excess " +
            "  , SUM(sh2.shiftLength / 60) unfilled " +
            "  FROM Schedule s " +
            "    LEFT JOIN PatternElt pe ON s.id = pe.scheduleId AND s.tenantId = pe.scheduleTenantId " +
            "    LEFT JOIN ShiftPattern sp ON sp.id = pe.shiftPatternId AND sp.tenantId = pe.shiftPatternTenantId " +
            "    LEFT JOIN Shift sh ON sp.id = sh.shiftPatternId " +
            "    LEFT JOIN Shift sh1 ON sp.id = sh1.shiftPatternId AND sh1.excess " +
            "    LEFT JOIN Shift sh2 ON sp.id = sh2.shiftPatternId AND sh2.employeeName IS NULL " +
            " WHERE s.id = :scheduleId " +
            "GROUP BY sp.skillId ";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", scheduleId);

        return query.getResultList();
    }

    public List<Object[]> summaryByEmployeeReport(String scheduleId, String teamsIds, String employeeTypes) {
        String sql =
                "SELECT " +
                "    sh.employeeId " +
                "  , sh.employeeName " +
                "  , sh.skillId " +
                "  , sh.skillName " +
                "  , sh.teamId " +
                "  , sh.teamName " +
                "  , sh.shiftLength / 60 shiftLength " +
                "  , sh.startDateTime " +
                "  , sh.endDateTime " +
                "  , e.employeeType " +
                "FROM Shift sh " +
                "LEFT JOIN Employee e ON e.id = sh.employeeId "  +
                "WHERE sh.employeeId is NOT NULL AND sh.scheduleId = :scheduleId " +
                        " AND sh.teamId in (" + teamsIds + ")" +
                        " AND e.employeeType in (" + employeeTypes + ")" +
                        " AND " + QueryPattern.NOT_DELETED.val("e");
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", scheduleId);

        return query.getResultList();
    }

    public List<Object[]> hourlyStaffingReport(String scheduleId) {
        String sql =
            "SELECT " +
            "    sh.skillId " +
            "  , sh.skillName " +
            "  , sh.teamId " +
            "  , sh.teamName " +
            "  , sh.shiftLength " +
            "  , sh.startDateTime " +
            "  , sh.endDateTime " +
            "FROM Shift sh " +
            "WHERE sh.scheduleId = :scheduleId ";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", scheduleId);
        return query.getResultList();
    }

    public List<Object[]> unavailibleTimeFrames(String scheduleId, String teamsIds){
        String sql =
            "SELECT tfs.employeeId, empls.employeeName, cl.maximumValue / 60, tfs.startDateTime, abs.name FROM ( " +
            "SELECT cd.id, cd.tenantId, cd.AbsenceTypeId, cd.employeeId, cd.durationInMinutes, " +
            "  cd.availabilityType, cd.startDateTime " +
            "FROM CDAvailabilityTimeFrame cd ) tfs " +
            "JOIN ( " +
            "SELECT DISTINCT e.id, CONCAT(e.firstName, ' ', e.lastName) employeeName, s.startDate, s.endDate " +
            "FROM Team_Schedule ts, EmployeeTeam et, EmployeeSkill es, Employee e, Schedule s " +
            " WHERE ts.schedules_id = :scheduleId AND ts.schedules_tenantId = :tenantId " +
            "   AND s.id = ts.schedules_id " +
            "   AND ts.schedules_tenantId = et.tenantId AND et.tenantId = e.tenantId AND e.tenantId = es.tenantId " +
            "   AND ts.Team_id = et.teamId AND ts.Team_id in ( " + teamsIds + " ) " +
                    "       AND et.employeeId = e.id AND e.id = es.employeeId " +
                    "       AND " + QueryPattern.NOT_DELETED.val("e") + " ) empls " +
            "ON empls.id = tfs.employeeId " +
            "LEFT JOIN AbsenceType abs ON abs.id=tfs.AbsenceTypeId " +
            "LEFT JOIN EmployeeContract ec On ec.employeeId=tfs.employeeId " +
            "LEFT JOIN IntMinMaxCL cl ON cl.contractId=ec.id AND cl.contractLineType='HOURS_PER_DAY' " +
            "WHERE  tfs.availabilityType='UnAvail' " +
            "       AND tfs.startDateTime > empls.startDate AND tfs.startDateTime < empls.endDate ";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", scheduleId);
        query.setParameter("tenantId", sessionService.getTenantId());

        return query.getResultList();
    }

	/** Transactional (TransactionAttributeType.REQUIRES_NEW) utility method to get
	 *  aggregation of detached schedule, employee, and shift entities.
	 *  NOTE: Public access for calling through business interface
	 *        so that transaction attribute will be honored.
	 * 
	 * @param schedulePk
	 * @param swapSeekingShiftIds
	 * @param swapCandidateShiftIds
	 * @return
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ShiftSwapEligibilityEntitiesAggregation getDetachedShiftSwapEligibilityEntities(
            PrimaryKey schedulePk, List<String> swapSeekingShiftIds, List<String> swapCandidateShiftIds) {
		List<Shift> swapSeekingShifts = new ArrayList<>();
		if (swapSeekingShiftIds != null) {
			for (String swapSeekingShiftId : swapSeekingShiftIds) {
				PrimaryKey swapSeekingShiftPk = new PrimaryKey(schedulePk.getTenantId(), swapSeekingShiftId);
				Shift shift = shiftService.getShift(swapSeekingShiftPk);
				if (shift != null) {
					swapSeekingShifts.add(shift);
				} else {
		            throw new ValidationException(sessionService.getMessage("validation.schedule.shift.noshift",
	                        swapSeekingShiftPk.getId()));
				}
			}
		}
		
		List<Shift> swapCandidateShifts = new ArrayList<>();
		if (swapCandidateShiftIds != null) {
			for (String swapCandidateShiftId : swapCandidateShiftIds) {
				PrimaryKey swapCandidateShiftPk = new PrimaryKey(schedulePk.getTenantId(), swapCandidateShiftId);
				Shift swapCandidateShift = shiftService.getShift(swapCandidateShiftPk);
				if (swapCandidateShift != null) {
					swapCandidateShifts.add(swapCandidateShift);
				} else {
		            throw new ValidationException(sessionService.getMessage("validation.schedule.shift.noshift",
	                        swapCandidateShiftPk.getId()));
				}
			}
		}
		
		ShiftSwapEligibilityEntitiesAggregation eligibilityInputsAggregation =
                new ShiftSwapEligibilityEntitiesAggregation();
		eligibilityInputsAggregation.swapSeekingShifts = swapSeekingShifts;
		eligibilityInputsAggregation.swapCandidateShifts = swapCandidateShifts;
		entityManager.clear();  // to detach all entities from the persistence context
		return eligibilityInputsAggregation;
	}

	/** Get result from synchronously processed shift swap eligibility execution
	 * @param schedulePk
	 * @param swapSeekingShiftIds
	 * @param swapCandidateShiftIds
	 * @param includeDetails
	 * @return requestTracker
	 * @throws IllegalAccessException 
	 */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public ShiftSwapEligibilityRequestTracker getShiftSwapEligibility(
            PrimaryKey schedulePk, List<String> swapSeekingShiftIds, List<String> swapCandidateShiftIds,
            int maxComputationTime, int maxUnimprovedSecondsSpent, int maxSynchronousWaitSeconds,
            Boolean includeDetails) throws IllegalAccessException {
		// get detached entities
		ShiftSwapEligibilityEntitiesAggregation eligibilityEntities = 
				scheduleServiceEJB.getDetachedShiftSwapEligibilityEntities(schedulePk, swapSeekingShiftIds,
                        swapCandidateShiftIds);
		List<Shift> swapSeekingShifts = eligibilityEntities.swapSeekingShifts;
		List<Shift> swapCandidateShifts = eligibilityEntities.swapCandidateShifts;
		
		ShiftSwapEligibilityRequestTracker requestTracker = scheduleServiceEJB.executeShiftSwapEligibility(schedulePk,
                maxComputationTime, maxUnimprovedSecondsSpent, swapSeekingShifts, swapCandidateShifts, null,
                includeDetails, true);
		
		ShiftSwapEligibilityResultDto resultDto = getShiftSwapEligibilityResults(requestTracker.getRequestId(),
                maxSynchronousWaitSeconds);
		requestTracker = this.getShiftSwapEligibilityRequestTracker(requestTracker.getRequestId());  // updating after completion
		
		if (requestTracker != null && resultDto != null) {
			requestTracker.setQualificationShifts(resultDto.getQualifyingShifts());
		} else {
			throw new ValidationException("validation.engine.noresult");
		}
		return requestTracker;
	}

	/** Fire off execution of shift swap eligibility for asynchronous processing.
	 * @param schedule
	 * @param swapSeekingShifts
	 * @param maxComputationTime
	 * @param maxUnimprovedSecondsSpent
	 * @param includeDetails
	 * @return requestId
	 * @throws IllegalAccessException 
	 */
	public String executeShiftSwapEligibility(Schedule schedule, List<Shift> swapSeekingShifts,
                                              List<Shift> swapCandidateShifts, int maxComputationTime,
                                              int maxUnimprovedSecondsSpent, Boolean includeDetails)
            throws IllegalAccessException {
		ShiftSwapEligibilityRequestTracker requestTracker = executeShiftSwapEligibility(schedule.getPrimaryKey(),
				maxComputationTime, maxUnimprovedSecondsSpent, swapSeekingShifts, swapCandidateShifts, null,
                includeDetails, false);
		
		return requestTracker.getRequestId();
	}
    
    public List<Object[]> headerReport(String scheduleId) {
        String sql =
            "SELECT " +
            "    s.name scheduleName " +
            "  , s.startDate " +
            "  , s.status " +
            "  , site.name siteName " +
            "  FROM Schedule s " +
            "    LEFT JOIN Team_Schedule ts ON s.id = ts.schedules_id AND s.tenantId = ts.schedules_tenantId " +
            "    LEFT JOIN Team t ON t.id = ts.Team_id AND t.tenantId = ts.Team_tenantId " +
            "    LEFT JOIN AOMRelationship r ON t.id = r.dst_id AND t.tenantId = r.dst_tenantId " +
            "    LEFT JOIN Site site ON r.src_id = site.id AND r.src_tenantId = site.tenantId " +
            " WHERE s.id = :scheduleId ";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", scheduleId);

        return query.getResultList();
    }

    public List<Object[]> scheduleShiftsReport(String scheduleId) {
        String sql =
            "SELECT " +
            "  s.startDate, " +
            "  sh.startDateTime, " +
            "  sh.shiftLength, " +
            "  sh.employeeId " +
            " FROM Schedule s" +
            "    LEFT JOIN PatternElt pe ON s.id = pe.scheduleId AND s.tenantId = pe.scheduleTenantId " +
            "    LEFT JOIN ShiftPattern sp ON sp.id = pe.shiftPatternId AND sp.tenantId = pe.shiftPatternTenantId " +
            "    LEFT JOIN Shift sh ON sp.id = sh.shiftPatternId " +
            " WHERE s.id = :scheduleId ";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", scheduleId);

        return query.getResultList();
    }

    public List<Object[]> scheduleEmployeeReport(String scheduleId) {
        String sql =
            "SELECT  " +
            "    e.id " +
            "  , e.hourlyRate " +
            "  , immcl.contractLineType " +
            "  , CASE  " +
            "          WHEN immcl.maximumEnabled THEN immcl.maximumValue " +
            "          WHEN immcl.minimumEnabled THEN immcl.minimumValue " +
            "          ELSE -1  " +
            "    END AS norma " +
            "  FROM EmployeeContract ec  " +
            "    LEFT JOIN IntMinMaxCL immcl ON ec.id = immcl.contractId AND ec.tenantId = immcl.contractTenantId " +
            "    LEFT JOIN Employee e ON ec.employeeId = e.id AND ec.tenantId = e.tenantId " +
            " WHERE  " +
            "       immcl.contractLineType IN ('DAILY_OVERTIME', 'WEEKLY_OVERTIME', 'TWO_WEEK_OVERTIME') " +
            "   AND e.isDeleted = false " +
            "   AND e.id IN  " +
            "    (SELECT e.id " +
            "       FROM Schedule s " +
            "         LEFT JOIN PatternElt pe ON s.id = pe.scheduleId AND s.tenantId = pe.scheduleTenantId " +
            "         LEFT JOIN ShiftPattern sp ON sp.id = pe.shiftPatternId AND sp.tenantId = pe.shiftPatternTenantId " +
            "         LEFT JOIN Shift sh ON sp.id = sh.shiftPatternId " +
            "         LEFT JOIN Employee e ON sh.employeeId = e.id AND sh.tenantId = e.tenantId " +
                    "           AND " + QueryPattern.NOT_DELETED.val("e") +
                    "       WHERE s.id = :scheduleId) ";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("scheduleId", scheduleId);

        return query.getResultList();
    }

    public void checkProductionPostedScheduledTeams(Schedule schedule) {
        if (!ScheduleStatus.Simulation.equals(schedule.getStatus())) {
            Collection<String> teamIds = ModelUtils.idSet(schedule.getTeams());
            checkProductionPostedScheduledTeams(teamIds, schedule.getStartDate(), schedule.getEndDate(),
                    schedule.getTenantId(), schedule.getId());
        }
    }

    public void checkProductionPostedScheduledTeams(Collection<String> teamIds, long startDate, long endDate,
                                                    String tenantId, String scheduleId) {
        String ids = ModelUtils.commaSeparatedQuotedValues(teamIds);
        if (StringUtils.isEmpty(ids)) {
            return;
        }

        String sql =
            "SELECT count(*) " +
            "  FROM Schedule s" +
            "    LEFT JOIN Team_Schedule ts ON s.id = ts.schedules_id AND s.tenantId = ts.schedules_tenantId " +
            " WHERE tenantId = :tenantId " +
            "   AND (  :startDate BETWEEN s.startDate AND ADDDATE(s.startDate, INTERVAL s.scheduleLengthInDays DAY) " +
            "       OR :endDate BETWEEN s.startDate AND ADDDATE(s.startDate, INTERVAL s.scheduleLengthInDays DAY)) " +
            "   AND s.status IN (" + ScheduleStatus.Posted.ordinal() + "," + ScheduleStatus.Production.ordinal() + ")" +
            "   AND ts.Team_id IN (" + ModelUtils.commaSeparatedQuotedValues(teamIds) + ")" +
            "   AND s.id <> :scheduleId ";

        Query query = entityManager.createNativeQuery(sql);

        query.setParameter("tenantId", tenantId);
        query.setParameter("scheduleId", scheduleId);
        query.setParameter("startDate", new Timestamp(startDate + 1000));
        query.setParameter("endDate", new Timestamp(endDate - 1000));

        BigInteger count = (BigInteger) query.getSingleResult();
        if (count.intValue() > 0) {
            throw new ValidationException(sessionService.getMessage("validation.schedule.scheduled.teams", scheduleId));
        }
    }

    private void addTimeZoneToDateTimeFields(Class clazz, Object object, DateTimeZone zone)
            throws IllegalAccessException {
        List<Field> fields = EmlogisUtils.getAllFields(clazz);
        for (Field field : fields) {
            field.setAccessible(true);
            Class fieldClass = field.getType();
            Object fieldValue = field.get(object);
            if (fieldValue != null && (!fieldClass.isEnum() && fieldClass.getPackage() != null
                    && fieldClass.getPackage().getName().startsWith("com.emlogis")
                    || Collection.class.isAssignableFrom(fieldClass)
                    || Map.class.isAssignableFrom(fieldClass))
                    || fieldClass == DateTime.class) {
                if (fieldClass == DateTime.class) {
                    DateTime dateTime = new DateTime(fieldValue, zone);

                    field.set(object, dateTime);
                } else {
                    if (Collection.class.isAssignableFrom(fieldClass)) {
                        for (Object item : (Collection) fieldValue) {
                            if (item != null) {
                                addTimeZoneToDateTimeFields(item.getClass(), item, zone);
                            }
                        }
                    } else if (Map.class.isAssignableFrom(fieldClass)) {
                        for (Object item : ((Map) fieldValue).values()) {
                            if (item != null) {
                                addTimeZoneToDateTimeFields(item.getClass(), item, zone);
                            }
                        }
                    } else {
                        addTimeZoneToDateTimeFields(fieldClass, fieldValue, zone);
                    }
                }
            }
        }
    }

	/** Get result from synchronously processed qualification execution against variation
	 *  of existing shift with modified start and/or end times.
	 *  
     * @param schedulePk
     * @param shiftTimeQualificationExecuteDto
     * @return
     * @throws IllegalAccessException
     */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public QualificationRequestSummary getShiftTimeQualification(
			PrimaryKey schedulePk,
			ShiftTimeQualificationExecuteDto shiftTimeQualificationExecuteDto) throws IllegalAccessException {
		// get detached entities
		List<PrimaryKey> shiftPks = new ArrayList<>();
		shiftPks.add( new PrimaryKey( schedulePk.getTenantId(), shiftTimeQualificationExecuteDto.getShiftId()) );
		List<Shift> detachedShifts = scheduleServiceEJB.getDetachedShifts( shiftPks );
		Shift shiftBefore = detachedShifts.get(0);

		// additional entity validation
		if (!shiftBefore.getScheduleId().equals(schedulePk.getId())) {
	        throw new ValidationException(sessionService.getMessage("validation.schedule.shift.norelation",
                    shiftBefore.getId()));
		}
		if (shiftTimeQualificationExecuteDto.getProposedNewStartDateTime() == null &&
				shiftTimeQualificationExecuteDto.getProposedNewEndDateTime() == null) {
	        throw new RuntimeException(sessionService.getMessage("validation.schedule.shift.invalidtimechange",
                    shiftBefore.getId()));
		}

		// set new time(s) for the detached shift entity
		Shift shiftAfter;
		try {
			shiftAfter = shiftBefore.clone();
			shiftAfter.setPrimaryKey(shiftBefore.getPrimaryKey()); // ensure clone's primary key is identical to original
		} catch (CloneNotSupportedException e) {
	        throw new RuntimeException(sessionService.getMessage("schedule.shift.clone.error", shiftBefore.getId()), e);
		}

		if (shiftTimeQualificationExecuteDto.getProposedNewStartDateTime() != null) {
			shiftAfter.setStartDateTime(shiftTimeQualificationExecuteDto.getProposedNewStartDateTime());			
		}
		
		if (shiftTimeQualificationExecuteDto.getProposedNewEndDateTime() != null) {
			shiftAfter.setEndDateTime(shiftTimeQualificationExecuteDto.getProposedNewEndDateTime());			
		}

		int maxSynchronousWaitSeconds = shiftTimeQualificationExecuteDto.getMaxSynchronousWaitSeconds();
		int maxComputationTime = shiftTimeQualificationExecuteDto.getMaxComputationTime();
		int maximumUnimprovedSecondsSpent = shiftTimeQualificationExecuteDto.getMaximumUnimprovedSecondsSpent();
		Collection<Shift> qualShifts = new ArrayList<>();
		qualShifts.add(shiftAfter);
		QualificationRequestTracker requestTracker = scheduleServiceEJB.executeQualification(maxComputationTime,
	            maximumUnimprovedSecondsSpent, qualShifts, null, null, true);
		QualificationResultDto resultDto = getQualificationResults(requestTracker.getRequestId(),
	            maxSynchronousWaitSeconds);
		if (resultDto != null) {
			Collection<ShiftQualificationDto> qualifyingShifts = resultDto.getQualifyingShifts();
			if (qualifyingShifts != null) {
				for (ShiftQualificationDto qualifyingShift : qualifyingShifts) {
					String qualifyingShiftId = qualifyingShift.getShiftId();
					if (qualifyingShiftId.equals(shiftAfter.getId()) && qualifyingShift.getIsAccepted()) {
						return new QualificationRequestSummary(requestTracker.getRequestId(), true, false);
					}
				}
			}
		}
		return new QualificationRequestSummary(requestTracker.getRequestId(), false, false);
	}

	/**
	 * Get employees eligible for a proposed (unpersisted) open shift.
	 * @param schedulePk
	 * @param teamPk
	 * @param skillPk
	 * @param startDateTime
	 * @param endDateTime
	 * @param maxComputationTime
	 * @param maxSynchronousWaitSeconds
	 * @param maxUnimprovedSecondsSpent
	 * @param includeDetails
	 * @param overrideOptions 
	 * @return
	 * @throws IllegalAccessException
	 */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public CandidateShiftEligibleEmployeesDto getProposedOpenShiftEligibleEmployees(
            PrimaryKey schedulePk, PrimaryKey teamPk, PrimaryKey skillPk, long startDateTime, long endDateTime,
            Integer maxComputationTime, Integer maxSynchronousWaitSeconds, Integer maxUnimprovedSecondsSpent,
            Boolean includeDetails, Map<ConstraintOverrideType, Boolean> overrideOptions) throws IllegalAccessException {
		List<Shift> shifts = new ArrayList<>();
		Shift shift = new Shift();
		PrimaryKey tmpShiftPk = new PrimaryKey(schedulePk.getTenantId(), null);
		shift.setPrimaryKey(tmpShiftPk);
		shift.setTeamId(teamPk.getId());
		shift.setSkillId(skillPk.getId());
		shift.setStartDateTime(startDateTime);
		shift.setEndDateTime(endDateTime);		
		shifts.add(shift);
	
		QualificationRequestTracker requestTracker = scheduleServiceEJB.executeOpenShiftEligibility(schedulePk, 
				maxComputationTime, maxUnimprovedSecondsSpent, null, shifts, null, includeDetails, overrideOptions, null, true);
		
		QualificationResultDto resultDto = getQualificationResults(requestTracker.getRequestId(),
                maxSynchronousWaitSeconds);
		requestTracker = getQualificationRequestTracker(requestTracker.getRequestId());  // updating after completion
		
		if (requestTracker != null && resultDto != null) {
			CandidateShiftEligibleEmployeesDto returnDto = new CandidateShiftEligibleEmployeesDto();
			Collection<ShiftQualificationDto> qualifyingShifts = resultDto.getQualifyingShifts();
			if (qualifyingShifts != null) {
				returnDto.setRequestId(requestTracker.getRequestId());
				scheduleServiceEJB.populateEligibleEmployees(tmpShiftPk, returnDto, qualifyingShifts);
			}
			return returnDto;
		} else {
			throw new ValidationException("validation.engine.noresult");
		}
	}

    /** Transactional (TransactionAttributeType.REQUIRES_NEW) utility method to 
     *  populate eligible employees for a CandidateShiftEligibleEmployeesDto
     *  NOTE: Public access for calling through business interface
     *        so that transaction attribute will be honored.
	 * 
	 * @param shiftPk
	 * @param shiftEligibleEmployees
	 * @param qualifyingShifts
	 */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void populateEligibleEmployees(PrimaryKey shiftPk, CandidateShiftEligibleEmployeesDto shiftEligibleEmployees,
			Collection<ShiftQualificationDto> qualifyingShifts) {
		for (ShiftQualificationDto qualifyingShift : qualifyingShifts) {
			String qualifyingShiftId = qualifyingShift.getShiftId();
			if (qualifyingShiftId.equals(shiftPk.getId()) && qualifyingShift.getIsAccepted()) {
				EmployeeDescriptorDto empDto = new EmployeeDescriptorDto();
				empDto.employeeId = qualifyingShift.getEmployeeId();
				empDto.employeeName = qualifyingShift.getEmployeeName();
				
				Employee employee = employeeService.getEmployee(new PrimaryKey(shiftPk.getTenantId(), qualifyingShift.getEmployeeId()));
				if(employee!= null){
					Team homeTeam = employee.getHomeTeam();
					if (homeTeam != null){
						empDto.homeTeamName = homeTeam.getName();						
					}
					
					Skill primarySkill = employee.getPrimarySkill();
					if (primarySkill != null) {
						empDto.primarySkillName = primarySkill.getName();
						empDto.primarySkillAbbreviation = primarySkill.getAbbreviation();
					}
				} else {
					throw new ValidationException(sessionService.getMessage("validation.schedule.shift.noemployee", empDto.employeeId));
				}
				shiftEligibleEmployees.getEligibleEmployees().add(empDto);
			}
		}
	}

	/** Transactional (TransactionAttributeType.REQUIRES_NEW) utility method to 
     *  populate open shifts and their eligible employees for an OpenShiftEligibilitySimpleResultDto.
     *  NOTE: Public access for calling through business interface
     *        so that transaction attribute will be honored.
     *        
	 * @param schedulePk 
	 * @param eligibilityResultDto
	 * @param openShifts
	 * @param qualificationShifts
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void populateOpenShiftsAndEligibleEmployees(PrimaryKey schedulePk, OpenShiftEligibilitySimpleResultDto eligibilityResultDto,
			Map<String, OpenShiftDto> openShifts, Collection<ExtendedShiftQualificationDto> qualificationShifts) {
		for (ExtendedShiftQualificationDto shiftQualification : qualificationShifts) {
			if (shiftQualification.getIsAccepted()) {
				String shiftId = shiftQualification.getShiftId();
				OpenShiftDto osDto = openShifts.get(shiftId);
				// shift is accepted, add it to map
				if (osDto == null) {
					osDto = new OpenShiftDto(shiftId);
					osDto.setStartDateTime(shiftQualification.getStartDateTime());
					osDto.setEndDateTime(shiftQualification.getEndDateTime());
					osDto.setShiftLength(shiftQualification.getShiftLength());
					osDto.setSkillId(shiftQualification.getSkillId());
					osDto.setSkillName(shiftQualification.getSkillName());
					osDto.setTeamId(shiftQualification.getTeamId());
					osDto.setTeamName(shiftQualification.getTeamName());
					openShifts.put(shiftId, osDto);
					eligibilityResultDto.addOpenShifts(osDto);
				}
				
				EligibleEmployeeDto empDto = new EligibleEmployeeDto(shiftQualification.getEmployeeId(),
                        shiftQualification.getEmployeeName());
				Employee employee = employeeService.getEmployee(new PrimaryKey(schedulePk.getTenantId(), shiftQualification.getEmployeeId()));
				if(employee!= null){
					Team homeTeam = employee.getHomeTeam();
					if (homeTeam != null){
						empDto.setHomeTeamName(homeTeam.getName());						
					}
					
					Skill primarySkill = employee.getPrimarySkill();
					if (primarySkill != null) {
						empDto.setPrimarySkillName(primarySkill.getName());
						empDto.setPrimarySkillAbbreviation(primarySkill.getAbbreviation());
					}
				} else {
					throw new ValidationException(sessionService.getMessage("validation.schedule.shift.noemployee", empDto.getId()));
				}
				
				osDto.addEmployee(empDto);
			}
		}
	}

	
	/** Get result from synchronously processed qualification execution against variation
	 *  of existing shift with new assignment.
	 * 
	 * @param schedulePk
	 * @param overriddenShiftQualExecuteDto
	 * @return
	 * @throws IllegalAccessException
	 */
	public QualificationRequestSummary getOverriddenShiftQualification(
			PrimaryKey schedulePk,
			OverriddenShiftQualExecuteDto overriddenShiftQualExecuteDto) throws IllegalAccessException {
		PrimaryKey shiftPk = new PrimaryKey(schedulePk.getTenantId(), overriddenShiftQualExecuteDto.getShiftId() );
		PrimaryKey employeePk = new PrimaryKey(schedulePk.getTenantId(), overriddenShiftQualExecuteDto.getEmployeeId());

		// get detached entities
		List<PrimaryKey> shiftPks = new ArrayList<>();
		shiftPks.add(shiftPk);
		List<Shift> detachedShifts = scheduleServiceEJB.getDetachedShifts(shiftPks);
		Shift shiftBefore = detachedShifts.get(0);

		// additional entity validation
		if (!shiftBefore.getScheduleId().equals(schedulePk.getId())){
			throw new ValidationException(sessionService.getMessage("validation.schedule.shift.norelation",
                    shiftBefore.getId()));
		}
		if (shiftBefore.getAssigned() != null || shiftBefore.getAssignmentType() != null 
				|| shiftBefore.getEmployeeId() != null || shiftBefore.getEmployeeName() != null) {
			throw new ValidationException( sessionService.getMessage("validation.schedule.shift.notopen",
                    shiftBefore.getId()));
		}

		// set assignment for the detached shift entity
		Shift shiftAfter;
		try {
			shiftAfter = shiftBefore.clone();
			shiftAfter.setPrimaryKey(shiftBefore.getPrimaryKey()); // ensure clone's primary key is identical to original
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(sessionService.getMessage("schedule.shift.clone.error", shiftBefore.getId()), e);
		}
		shiftAfter.makeShiftAssignment(overriddenShiftQualExecuteDto.getEmployeeId(),
                scheduleServiceEJB.getEmployeeFullName(employeePk), AssignmentType.MANUAL);

		String requestId;
		// request qualification
		Map<String, Map<ConstraintOverrideType, Boolean>> individualEmpConstraintOverrideOpts = new HashMap<>();
		individualEmpConstraintOverrideOpts.put(shiftAfter.getEmployeeId(),
                overriddenShiftQualExecuteDto.getOverrideOptions());

		Collection<Shift> qualificationShifts = new ArrayList<>();
		qualificationShifts.add(shiftAfter);
		requestId = UniqueId.getId();
		QualificationRequestTracker requestTracker = scheduleServiceEJB.executeQualification(0, 
				0, qualificationShifts, requestId, individualEmpConstraintOverrideOpts, true);
		QualificationResultDto resultDto = getQualificationResults(requestId, 60);
		if (resultDto != null) {
			Collection<ShiftQualificationDto> qualifyingShifts = resultDto.getQualifyingShifts();
			if (qualifyingShifts != null) {
				for (ShiftQualificationDto qualifyingShift : qualifyingShifts) {
					String qualifyingShiftId = qualifyingShift.getShiftId();
					if (qualifyingShiftId.equals(shiftAfter.getId()) && qualifyingShift.getIsAccepted()) {
						return new QualificationRequestSummary(requestTracker.getRequestId(), true, false);
					}
				}
			}
		}
		return new QualificationRequestSummary(requestTracker.getRequestId(), false, false);
	}

    public Collection<Object[]> queryByDay(String tenantId, ScheduleQueryByDayParamDto paramDto, AccountACL acl) {
        String aclFilter = null;

        SimpleQuery simpleQuery = new SimpleQuery(tenantId);
        simpleQuery.setEntityClass(Team.class);
        if (StringUtils.isNotEmpty(paramDto.getTeamId())) {
            simpleQuery.setFilter("primaryKey.id = '" + paramDto.getTeamId() + "'");
        } else if (StringUtils.isNotEmpty(paramDto.getTeamName())) {
            simpleQuery.setFilter("name = '" + paramDto.getTeamName() + "'");
        }
        ResultSet<Team> teamResultSet = teamService.findTeams(simpleQuery, acl);

        Collection<Team> teamCollection = teamResultSet.getResult();
        if (teamCollection!= null && teamCollection.size() > 0) {
            aclFilter = "t.id IN (" + ModelUtils.commaSeparatedQuotedIds(teamCollection) + ")";
        }

        String statusFilter = null;
        if (paramDto.getStatus() != null) {
            statusFilter = " s.status = '" + paramDto.getStatus().ordinal() + "'";
        }

        String startDateFilter = null;
        long cutStartDate = 0;
        if (paramDto.getDate() > 0) {
            startDateFilter = "(s.startDate >= :startDateBegin AND s.startDate < :startDateEnd)";
            DateTimeZone timeZone = DateTimeZone.forID(paramDto.getTimezone());
            if (timeZone != null) {
                cutStartDate = ModelUtils.cutDateTimeToDate(paramDto.getDate(), timeZone);
            } else {
                cutStartDate = ModelUtils.cutDateTimeToDate(paramDto.getDate());
            }
        }

        String sql =
            "SELECT s.id, s.name, s.description, s.startDate, s.endDate, s.scheduleLengthInDays, s.status, s.state, " +
            "       CONCAT(site.id, ':', site.name) sites, " +
            "       GROUP_CONCAT(DISTINCT t.id, ':', t.name SEPARATOR ',') teams " +
            "  FROM Schedule s " +
            "    LEFT JOIN Team_Schedule ts ON ts.schedules_id = s.id AND ts.schedules_tenantId = s.tenantId " +
            "    LEFT JOIN Team t ON ts.Team_id = t.id AND ts.Team_tenantId = t.tenantId " +
            "    LEFT JOIN AOMRelationship r ON t.id = r.dst_id AND t.tenantId = r.dst_tenantId " +
            "    LEFT JOIN Site site ON r.src_id = site.id AND r.src_tenantId = site.tenantId " +
            " WHERE s.tenantId = :tenantId " +
            (StringUtils.isEmpty(aclFilter) ? "" : " AND " + aclFilter) +
            (StringUtils.isEmpty(statusFilter) ? "" : " AND " + statusFilter) +
            (StringUtils.isEmpty(startDateFilter) ? "" : " AND " + startDateFilter) +
            " GROUP BY s.id ";

        Query query = entityManager.createNativeQuery(sql);

        query.setParameter("tenantId", tenantId);
        if (StringUtils.isNotEmpty(startDateFilter)) {
            query.setParameter("startDateBegin", new Timestamp(cutStartDate));
            query.setParameter("startDateEnd", new Timestamp(cutStartDate + Constants.DAY_MILLISECONDS));
        }

        return query.getResultList();
    }

	private void sendScheduleChangeNotification(Shift shift, NotificationCategory notificationCategory, Shift peerShift,
                                                UserAccount by) {
		String shiftTeamName = shift.getTeamName();
		String shiftSkillName = shift.getSkillName();
		
		Employee employee = employeeService.getEmployee(new PrimaryKey(shift.getTenantId(), shift.getEmployeeId()));
		
		UserAccount userAccount = employee.getUserAccount();
		
		String shiftStartDate = accountUtilService.getTimeZoneAdjustedDateString(new DateTime(shift.getStartDateTime()),
                userAccount, Constants.NOTIF_DATE_FORMATTER);
		
		String shiftStartTime = accountUtilService.getTimeZoneAdjustedDateString(new DateTime(shift.getStartDateTime()),
                userAccount, Constants.NOTIF_TIME_FORMATTER);
		
		String shiftEndTime = accountUtilService.getTimeZoneAdjustedDateString(new DateTime(shift.getEndDateTime()),
				userAccount, Constants.NOTIF_TIME_FORMATTER);
		
		Map<String, String> messageAttributes = new HashMap<>();
		
		messageAttributes.put("shiftTeamName", shiftTeamName);
		messageAttributes.put("shiftSkillName", shiftSkillName);
		messageAttributes.put("shiftStartDate", shiftStartDate);
		messageAttributes.put("shiftStartTime", shiftStartTime);
		messageAttributes.put("shiftEndTime", shiftEndTime);

		if (notificationCategory == NotificationCategory.SHIFT_STARTSTOP_MODIFIED) {
			String shiftPrevStartDate = accountUtilService.getTimeZoneAdjustedDateString(
                    new DateTime(peerShift.getStartDateTime()), userAccount, Constants.NOTIF_DATE_FORMATTER);

			String shiftPrevStartTime = accountUtilService.getTimeZoneAdjustedDateString(
                    new DateTime(peerShift.getStartDateTime()), userAccount, Constants.NOTIF_TIME_FORMATTER);
			
			String shiftPrevEndTime = accountUtilService.getTimeZoneAdjustedDateString(
                    new DateTime(peerShift.getEndDateTime()), userAccount, Constants.NOTIF_TIME_FORMATTER);
			
			messageAttributes.put("shiftPrevStartDate", shiftPrevStartDate);
			messageAttributes.put("shiftPrevStartTime", shiftPrevStartTime);
			messageAttributes.put("shiftPrevEndTime", shiftPrevEndTime);
		}
		
		if (notificationCategory == NotificationCategory.SHIFT_WIP) {
			String peerName = peerShift.getEmployeeName();
			
			messageAttributes.put("peerName", peerName);
		}
		
		if (notificationCategory == NotificationCategory.SHIFT_SWAP) {
			String shiftPeerStartDate = accountUtilService.getTimeZoneAdjustedDateString(
                    new DateTime(peerShift.getStartDateTime()), userAccount, Constants.NOTIF_DATE_FORMATTER);
			String shiftPeerStartTime = accountUtilService.getTimeZoneAdjustedDateString(
                    new DateTime(peerShift.getStartDateTime()), userAccount, Constants.NOTIF_TIME_FORMATTER);
			
			String shiftPeerEndTime = accountUtilService.getTimeZoneAdjustedDateString(
                    new DateTime(peerShift.getEndDateTime()), userAccount, Constants.NOTIF_TIME_FORMATTER);
			
			String peerName = peerShift.getEmployeeName();
			String peerEmployeeTeamName = peerShift.getTeamName();
			String peerEmployeeSkillName = peerShift.getSiteName();
			
			messageAttributes.put("shiftPeerStartDate", shiftPeerStartDate);
			messageAttributes.put("shiftPeerStartTime", shiftPeerStartTime);
			messageAttributes.put("shiftPeerEndTime", shiftPeerEndTime);
			
			messageAttributes.put("peerName", peerName);
			messageAttributes.put("peerEmployeeTeamName", peerEmployeeTeamName);
			messageAttributes.put("peerEmployeeSkillName", peerEmployeeSkillName);
		}
		
		NotificationMessageDTO notificationMessageDTO = new NotificationMessageDTO();
		
		if (by != null) {
			// PLACEHOLDER
			// Add sender information
			notificationMessageDTO.setSenderUserId(by.getId());
		}
		
		notificationMessageDTO.setNotificationOperation(NotificationOperation.SCHEDULE_CHANGE);
		notificationMessageDTO.setNotificationCategory(notificationCategory);

		notificationMessageDTO.setTenantId(employee.getTenantId());
		notificationMessageDTO.setReceiverUserId(userAccount.getId());
		notificationMessageDTO.setMessageAttributes(messageAttributes);
		notificationService.sendNotification(notificationMessageDTO);
	}

	private void sendScheduleDeleteNotification(Schedule schedule, UserAccount by) {
        Collection<Shift> shifts = shiftService.getScheduleShifts(schedule);
        for (Shift shift : shifts) {
            if (StringUtils.isNotBlank(shift.getEmployeeId())) {
                PrimaryKey employeePrimaryKey = new PrimaryKey(shift.getTenantId(), shift.getEmployeeId());
                Employee employee = employeeService.getEmployee(employeePrimaryKey);
                UserAccount userAccount = employee.getUserAccount();

                String shiftTeamName = shift.getTeamName();
                String shiftSkillName = shift.getSkillName();
                String shiftStartDate = accountUtilService.getTimeZoneAdjustedDateString(
                        new DateTime(shift.getStartDateTime()), userAccount, Constants.NOTIF_DATE_FORMATTER);
                String shiftStartTime = accountUtilService.getTimeZoneAdjustedDateString(
                        new DateTime(shift.getStartDateTime()), userAccount, Constants.NOTIF_TIME_FORMATTER);
                String shiftEndTime = accountUtilService.getTimeZoneAdjustedDateString(
                        new DateTime(shift.getEndDateTime()), userAccount, Constants.NOTIF_TIME_FORMATTER);

                Map<String, String> messageAttributes = new HashMap<>();

                messageAttributes.put("shiftTeamName", shiftTeamName);
                messageAttributes.put("shiftSkillName", shiftSkillName);
                messageAttributes.put("shiftStartDate", shiftStartDate);
                messageAttributes.put("shiftStartTime", shiftStartTime);
                messageAttributes.put("shiftEndTime", shiftEndTime);

                NotificationMessageDTO notificationMessageDTO = new NotificationMessageDTO();

                if (by != null) {
                    notificationMessageDTO.setSenderUserId(by.getId());
                }

                notificationMessageDTO.setNotificationOperation(NotificationOperation.DELETE);
                notificationMessageDTO.setNotificationCategory(NotificationCategory.SCHEDULE);
                notificationMessageDTO.setTenantId(employee.getTenantId());
                notificationMessageDTO.setReceiverUserId(userAccount.getId());
                notificationMessageDTO.setMessageAttributes(messageAttributes);

                notificationService.sendNotification(notificationMessageDTO);
            }
        }
	}

    private void sendScheduleGenerateNotification(Schedule schedule, UserAccount manager,
                                                  NotificationOperation notificationOperation) {
        List<UserAccount> userAccounts = new ArrayList<>();
        if (ScheduleStatus.Simulation.equals(schedule.getStatus())) {
            userAccounts.add(manager);
        } else if (ScheduleStatus.Production.equals(schedule.getStatus())) {
            Set<Team> teams = schedule.getTeams();
            Set<String> teamIds = ModelUtils.idSet(teams);
            Set<String> userIds = new HashSet<>();
            Collection<Object[]> rows = organizationService.getManagersByTeams(schedule.getTenantId(), null);
            for (Object[] row : rows) {
                String teamId = (String) row[0];
                if (teamIds.contains(teamId)) {
                    String userId = (String) row[4];
                    userIds.add(userId);
                }
            }

            if (!userIds.isEmpty()) {
                String userIdsClause = ModelUtils.commaSeparatedQuotedValues(userIds);
                SimpleQuery simpleQuery = new SimpleQuery(schedule.getTenantId());
                simpleQuery.addFilter("primaryKey.id IN (" + userIdsClause + ")");
                ResultSet<UserAccount> resultSet = userAccountService.findUserAccounts(simpleQuery);

                userAccounts.addAll(resultSet.getResult());
            }
        }
        for (UserAccount userAccount : userAccounts) {
            Map<String, String> messageAttributes = new HashMap<>();

            String scheduleStartDate = accountUtilService.getTimeZoneAdjustedDateString(
                    new DateTime(schedule.getStartDate()), userAccount, Constants.NOTIF_DATE_FORMATTER);

            messageAttributes.put("scheduleName", schedule.getName());
            messageAttributes.put("scheduleStartDate", scheduleStartDate);

            NotificationMessageDTO notificationMessageDTO = new NotificationMessageDTO();

            notificationMessageDTO.setSenderUserId(manager.getId());
            notificationMessageDTO.setNotificationOperation(notificationOperation);
            notificationMessageDTO.setNotificationCategory(NotificationCategory.SCHEDULE);
            notificationMessageDTO.setTenantId(userAccount.getTenantId());
            notificationMessageDTO.setReceiverUserId(userAccount.getId());
            notificationMessageDTO.setMessageAttributes(messageAttributes);

            notificationService.sendNotification(notificationMessageDTO);
        }
    }

    private void checkUniqueScheduleName(String tenantId, String name) {
        SimpleQuery simpleQuery = new SimpleQuery(tenantId);
        simpleQuery.addFilter("name = '" + name + "'");
        ResultSet<Schedule> scheduleResultSet = findSchedules(simpleQuery);
        if (scheduleResultSet.getResult() != null && !scheduleResultSet.getResult().isEmpty()) {
            throw new ValidationException(sessionService.getMessage("validation.field.unique.error", "Schedule",
                    "name"));
        }
    }

    private Map<String, List> getPreassignedShiftsOtherTeams(Schedule schedule, List<Employee> employees) {
        List<Shift> shifts = shiftService.getPreassignedShiftsOtherTeams(schedule, employees);
        return transformShiftsIntoAssignmentMap(shifts);
    }

    private Map<String, List> getPreassignedShiftsFromContract(Schedule schedule, Site site, List<Employee> employees) {
        Set<Shift> shiftSet = new HashSet<>();

        List<Shift> preassignedWeekendShifts = getPreassignedWeekendShifts(schedule, site, employees);
        shiftSet.addAll(preassignedWeekendShifts);

        List<Shift> intMinMaxForDaysShifts = getIntMinMaxForDaysShifts(schedule, site, employees);
        shiftSet.addAll(intMinMaxForDaysShifts);

        List<Shift> intMinMaxForHoursShifts = getIntMinMaxForHoursShifts(schedule, employees);
        shiftSet.addAll(intMinMaxForHoursShifts);

        List<Shift> weekdayRotationPatternShifts = getWeekdayRotationPatternShifts(schedule, site, employees);
        shiftSet.addAll(weekdayRotationPatternShifts);

        return transformShiftsIntoAssignmentMap(shiftSet);
    }

    private Map<String, List> transformShiftsIntoAssignmentMap(Collection<Shift> shifts) {
        Map<String, List> result = new HashMap<>();

        List<ShiftAssignmentDto> shiftAssignmentDtos = new ArrayList<>();
        List<ShiftDto> shiftDtos = new ArrayList<>();

        result.put("shiftAssignmentDtos", shiftAssignmentDtos);
        result.put("shiftDtos", shiftDtos);

        for (Shift shift : shifts) {
            ShiftAssignmentDto shiftAssignmentDto = shiftToShiftAssignmentDto(shift);
            shiftAssignmentDto.setLocked(true);
            shiftAssignmentDtos.add(shiftAssignmentDto);

            ShiftDto shiftDto = shiftToShiftDto(shift);
            shiftDtos.add(shiftDto);
        }

        return result;
    }

    private List<Shift> getWeekdayRotationPatternShifts(Schedule schedule, Site site, List<Employee> employees) {
        return shiftService.getWeekdayRotationPatternShifts(employees, schedule.getStartDate(), schedule.getEndDate(),
                site.getTimeZone());
    }

    private List<Shift> getIntMinMaxForDaysShifts(Schedule schedule, Site site, List<Employee> employees) {
        long start = new LocalDate(schedule.getStartDate(), site.getTimeZone()).toDate().getTime();
        long end = new LocalDate(schedule.getEndDate(), site.getTimeZone()).toDate().getTime();
        return shiftService.getIntMinMaxForDaysShifts(employees, start, end);
    }

    private List<Shift> getIntMinMaxForHoursShifts(Schedule schedule, List<Employee> employees) {
        return shiftService.getIntMinMaxForHoursShifts(employees, schedule.getStartDate(), schedule.getEndDate());
    }

    private List<Shift> getPreassignedWeekendShifts(Schedule schedule, Site site, List<Employee> employees) {
        List<Shift> result = new ArrayList<>();

        WeekendDefinition weekendDefinition = site.getWeekendDefinition();
        int dayCount = 0;
        int lastWeekendDayOfWeek = 0;
        int firstWeekendDayOfWeek = 0;
        switch (weekendDefinition) {
            case FRIDAY_SATURDAY_SUNDAY:
                dayCount = 3;
                lastWeekendDayOfWeek = 7;
                firstWeekendDayOfWeek = 5;
                break;
            case FRIDAY_SATURDAY_SUNDAY_MONDAY:
                dayCount = 4;
                lastWeekendDayOfWeek = 1;
                firstWeekendDayOfWeek = 5;
                break;
            case SATURDAY_SUNDAY:
                dayCount = 2;
                lastWeekendDayOfWeek = 7;
                firstWeekendDayOfWeek = 6;
                break;
            case SATURDAY_SUNDAY_MONDAY:
                dayCount = 3;
                lastWeekendDayOfWeek = 1;
                firstWeekendDayOfWeek = 6;
        }

        DateTime startDateTime = new DateTime(schedule.getStartDate());
        int previousToScheduleStartDayOfWeek = startDateTime.getDayOfWeek() - 1;
        if (previousToScheduleStartDayOfWeek == 0) {
            previousToScheduleStartDayOfWeek = 7;
        }
        if (previousToScheduleStartDayOfWeek == lastWeekendDayOfWeek) {
            long start = new LocalDate(startDateTime.minusDays(dayCount), site.getTimeZone()).toDate().getTime();
            long end = new LocalDate(startDateTime, site.getTimeZone()).toDate().getTime();
            List<Shift> prevShifts = shiftService.getPreassignedWeekendShifts(employees, start, end);
            result.addAll(prevShifts);
        }

        DateTime endDateTime = new DateTime(schedule.getEndDate());
        int nextToScheduleEndDayOfWeek = endDateTime.getDayOfWeek() + 1;
        if (nextToScheduleEndDayOfWeek == 8) {
            nextToScheduleEndDayOfWeek = 1;
        }
        if (nextToScheduleEndDayOfWeek == firstWeekendDayOfWeek) {
            long start = new LocalDate(endDateTime, site.getTimeZone()).plusDays(1).toDate().getTime();
            long end = new LocalDate(start, site.getTimeZone()).plusDays(dayCount).toDate().getTime();
            List<Shift> nextShifts = shiftService.getPreassignedWeekendShifts(employees, start, end);
            result.addAll(nextShifts);
        }

        return result;
    }

    private void unassosiateShiftPatterns(Collection<Team> teamsToRemove, Schedule schedule) {
        String teamIds = ModelUtils.commaSeparatedQuotedIds(teamsToRemove);
        if (StringUtils.isEmpty(teamIds)) {
            return;
        }
        String sql =
            "SELECT * FROM PatternElt " +
            " WHERE scheduleId = :scheduleId " +
            "   AND shiftPatternId IN (SELECT id FROM ShiftPattern WHERE teamId IN (" + teamIds + "))";
        Query query = entityManager.createNativeQuery(sql, PatternElt.class);
        query.setParameter("scheduleId", schedule.getId());
        List<PatternElt> patternEltsToDelete = query.getResultList();
        for (PatternElt patternElt : patternEltsToDelete) {
            deletePatternElt(patternElt);
        }
        schedule.getPatternElts().removeAll(patternEltsToDelete);
    }

}
