package com.emlogis.common.facade.schedule;

import com.emlogis.common.Constants;
import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.ModelUtils;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.facade.employee.EmployeeFacade;
import com.emlogis.common.notifications.NotificationCategory;
import com.emlogis.common.notifications.NotificationOperation;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.services.contract.ContractLineService;
import com.emlogis.common.services.employee.AbsenceTypeService;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.employee.SkillService;
import com.emlogis.common.services.hazelcast.HazelcastClientService;
import com.emlogis.common.services.notification.NotificationService;
import com.emlogis.common.services.schedule.PostedOpenShiftService;
import com.emlogis.common.services.schedule.ScheduleService;
import com.emlogis.common.services.schedule.ShiftService;
import com.emlogis.common.services.schedule.changes.ScheduleChangeService;
import com.emlogis.common.services.shiftpattern.ShiftPatternService;
import com.emlogis.common.services.structurelevel.SiteService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.services.util.AccountUtilService;
import com.emlogis.common.validation.annotations.*;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.communication.ShiftQualificationDto;
import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.dto.ScheduleQueryByDayParamDto;
import com.emlogis.model.dto.ScheduleQueryDto;
import com.emlogis.model.employee.*;
import com.emlogis.model.employee.dto.*;
import com.emlogis.model.notification.dto.NotificationMessageDTO;
import com.emlogis.model.schedule.*;
import com.emlogis.model.schedule.Schedule;
import com.emlogis.model.schedule.changes.*;
import com.emlogis.model.schedule.dto.*;
import com.emlogis.model.schedule.dto.OpenShiftEligibilitySimpleResultDto.EligibleEmployeeDto;
import com.emlogis.model.schedule.dto.OpenShiftEligibilitySimpleResultDto.OpenShiftDto;
import com.emlogis.model.schedule.dto.SummarizedPostedOpenShiftDto.PostedEmployeeDto;
import com.emlogis.model.schedule.dto.changes.*;
import com.emlogis.model.shiftpattern.PatternElt;
import com.emlogis.model.shiftpattern.ShiftPattern;
import com.emlogis.model.shiftpattern.dto.ShiftPatternDto;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.structurelevel.dto.TeamDto;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.rest.resources.util.DtoMapper;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.joda.time.LocalDate;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import javax.ejb.*;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.*;

import static com.emlogis.model.schedule.ScheduleStatus.Posted;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ScheduleFacade extends BaseFacade {

    private final static Logger logger = Logger.getLogger(ScheduleFacade.class);

    public final static double OVERTIME_COEFF = 0.5;

    @EJB
    private EmployeeFacade employeeFacade;

    @EJB
    private ScheduleService scheduleService;

    @EJB
    private SiteService siteService;

    @EJB
    private TeamService teamService;

    @EJB
    private SkillService skillService;

    @EJB
    private ShiftService shiftService;

    @EJB
    private ShiftFacade shiftFacade;

    @EJB
    private PostedOpenShiftService postedOpenShiftService;

    @EJB
    private EmployeeService employeeService;

    @EJB
    private UserAccountService userAccountService;

    @EJB
    private ShiftPatternService shiftPatternService;

    @EJB
    private ScheduleChangeService trackingService;

    @EJB
    private AbsenceTypeService absenceTypeService;

    @EJB
    private HazelcastClientService hazelcastService;

    @EJB
    private AccountUtilService accountUtilService;

    @EJB
    private NotificationService notificationService;

    @Validation
    public ResultSetDto<ScheduleDto> getObjects(
            String tenantId,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQuery simpleQuery = new SimpleQuery(tenantId);
        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true);
        ResultSet<Schedule> resultSet = scheduleService.findSchedules(simpleQuery);
        return toResultSetDto(resultSet, ScheduleDto.class);
    }

    @Validation
    public ScheduleDto getObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Schedule schedule = scheduleService.getSchedule(primaryKey);
        return toDto(schedule, ScheduleDto.class);
    }

    @Validation
    public ScheduleDto updateObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class) PrimaryKey primaryKey,
            @ValidateUnique(fields = Constants.NAME, type = Schedule.class) ScheduleUpdateDto scheduleUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Schedule schedule = scheduleToUpdate(primaryKey, scheduleUpdateDto);

        getEventService().sendEntityUpdateEvent(schedule, ScheduleDto.class);
        return toDto(schedule, ScheduleDto.class);
    }

    @Validation
    public ScheduleDto createObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class, expectedResult = false)
            PrimaryKey primaryKey,
            @ValidateAll(
                    dates = {@ValidateDate(field = ScheduleCreateDto.START_DATE)},
                    uniques = {
                        @ValidateUnique(fields = ScheduleCreateDto.NAME, type = Schedule.class)
                    }
            )
            ScheduleCreateDto scheduleCreateDto,
			String managerAccountId) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        String tenantId = primaryKey.getTenantId();
        Set<String> teamIds = scheduleCreateDto.getTeamIds();
        ScheduleUpdateDto updateDto = scheduleCreateDto.getUpdateDto();

        if (updateDto == null || updateDto.getStatus() != null && updateDto.getStatus() != ScheduleStatus.Simulation) {
            long start = scheduleCreateDto.getStartDate();
            long end = scheduleCreateDto.getStartDate() +
                    scheduleCreateDto.getScheduleLengthInDays() * Constants.DAY_MILLISECONDS;
            scheduleService.checkProductionPostedScheduledTeams(teamIds, start, end, tenantId, primaryKey.getId());
        }

        Schedule schedule = scheduleService.create(primaryKey);
        UserAccount mngrAccount = null;
        if (managerAccountId != null) {
            mngrAccount = userAccountService.getUserAccount(new PrimaryKey(schedule.getTenantId(), managerAccountId));
        }


        if (scheduleCreateDto.getStartDate() > 0) {
            schedule.setStartDate(scheduleCreateDto.getStartDate());
        }

        if (scheduleCreateDto.getScheduleType() != null) {
            schedule.setScheduleType(scheduleCreateDto.getScheduleType());
        }

        if (teamIds != null && teamIds.size() > 0) {
            Collection<Team> teams = teamService.getTeams(tenantId, teamIds);

            Set<Team> teamSet = new HashSet<>();
            teamSet.addAll(teams);
            schedule.setTeams(teamSet);
            schedule.setScheduledTeamCount(teams.size());
            schedule.setScheduledEmployeeCount(teamService.getEmployeeCount(teams, tenantId));
        }

        if (ScheduleType.ShiftStructureBased.equals(schedule.getScheduleType()) && schedule.getTeams() != null) {
            associateShiftStructures(schedule, schedule.getTeams());
        }

        if (scheduleCreateDto.getScheduleLengthInDays() != schedule.getScheduleLengthInDays()) {
            schedule.setScheduleLengthInDays(scheduleCreateDto.getScheduleLengthInDays());
        }

        if (updateDto != null) {
            this.updateObject(primaryKey, updateDto);
        }

        setCreatedBy(schedule);
        setOwnedBy(schedule, this.getActualUserId());

        scheduleService.update(schedule);
        scheduleService.trackScheduleChanges(schedule, ChangeType.SCHEDULECREATE, mngrAccount);

        getEventService().sendEntityCreateEvent(schedule, ScheduleDto.class);
        return toDto(schedule, ScheduleDto.class);
    }

    @Validation
    public boolean deleteObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey primaryKey,
            String managerAccountId)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Schedule schedule = scheduleService.getSchedule(primaryKey);

        UserAccount managerAccount = null;
        if (managerAccountId != null) {
            managerAccount = userAccountService.getUserAccount(new PrimaryKey(schedule.getTenantId(), managerAccountId));
        }

        if(!isDeletable(schedule)) {
            throw new UnsupportedOperationException("Working schedule cannot be deleted!");
        }

        scheduleService.delete(schedule, managerAccount);
        getEventService().sendEntityDeleteEvent(schedule, ScheduleDto.class);
        return true;
    }

    private boolean isDeletable(Schedule schedule) {
        if(schedule.getStatus() != ScheduleStatus.Posted) {
            return true;
        }
        Set<Team> teams = schedule.getTeams();
        Site site = null;
        if(teams != null) {
            for(Team team : teams) {
                site = teamService.getSite(team);
                break;
            }
        }
        DateTimeZone siteTimeZone = site != null ? site.getTimeZone() : DateTimeZone.getDefault();
        DateTime currentDateTime = new DateTime(siteTimeZone);
        DateTime scheduleStartDateTime = new DateTime(schedule.getStartDate(), siteTimeZone);
        return currentDateTime.getMillis() < scheduleStartDateTime.getMillis();
    }

    @Validation
    public ResultSetDto<ShiftDto> getShifts(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InvocationTargetException, NoSuchMethodException, InstantiationException,
            IllegalAccessException {
        SimpleQuery simpleQuery = new SimpleQuery(schedulePrimaryKey.getTenantId());
        simpleQuery.setSelect(select)
                .setFilter(filter)
                .addFilter("scheduleId = '" + schedulePrimaryKey.getId() + "'")
                .setOffset(offset)
                .setLimit(limit)
                .setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equals(orderDir, "ASC"))
                .setTotalCount(true);
        ResultSet<Shift> resultSet = shiftService.findShifts(simpleQuery);
        return toResultSetDto(resultSet, ShiftDto.class);
    }

    @Validation
    public ResultSetDto<Object[]> getShiftsOps(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            String filter,
            String returnedFields,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InvocationTargetException, NoSuchMethodException, InstantiationException,
            IllegalAccessException {
        ResultSet<Object[]> resultSet= shiftService.getShiftsOps(schedulePrimaryKey.getId(),
                schedulePrimaryKey.getTenantId(), filter, "", returnedFields, offset, limit, orderBy, orderDir);
        ResultSetDto<Object[]> resultSetDto = new ResultSetDto<>();
        resultSetDto.setResult(resultSet.getResult());
        resultSetDto.setTotal(resultSet.getTotal());
        return resultSetDto;
    }

    @Validation
    public Collection<Object[]> getShifts(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            long startDate,
            long endDate,
            String filter,
            String returnedFields) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        String filterWithTime = (
            "((startDateTime >= '" + new DateTime(startDate) + "' and startDateTime <= '" + new DateTime(endDate) +
            "') or (endDateTime >= '" + new DateTime(startDate) + "' and endDateTime <= '" + new DateTime(endDate) +
            "') or (startDateTime < '" + new DateTime(startDate) + "' and endDateTime > '" + new DateTime(endDate) + "'))");

        ResultSet<Object[]> resultSet= shiftService.getShiftsOps(schedulePrimaryKey.getId(),
                schedulePrimaryKey.getTenantId(), filter, filterWithTime, returnedFields, 0, 0, null, null);

        return resultSet.getResult();
    }

    @Validation
    public ScheduleReportDto getScheduleReport(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        Schedule schedule = scheduleService.getSchedule(schedulePrimaryKey);

        ScheduleReportDto scheduleReportDto = new ScheduleReportDto();
        scheduleReportDto.setCompletionReport(scheduleService.getScheduleReport(schedulePrimaryKey).getCompletionReport());

        scheduleReportDto.setScheduleOverview(scheduleService.getScheduleOverview(schedulePrimaryKey));

        Map<String, double[]> avalHours = scheduleService.getEmployeeAvailability(schedule);

        Double[] totalHours = scheduleService.getTotalAvalHours(avalHours);
        scheduleReportDto.getScheduleOverview().getHours().setEmployees(totalHours);

        ResultSet<Employee> resultSet = scheduleService.getEmployees(schedulePrimaryKey, "", 0, 0, null,
                null);
        HashMap<String, EmployeeReportDto> dtoHashMap = new HashMap<>();
        EmployeeReportDto employeeReportDto;
        for (Employee employee : resultSet.getResult()) {
            employeeReportDto = new EmployeeReportDto();
            employeeReportDto.setId(employee.getId());
            employeeReportDto.setFirstName(employee.getFirstName());
            employeeReportDto.setLastName(employee.getLastName());
            employeeReportDto.setTotalHours(0);
            employeeReportDto.setAvailableHours(avalHours.get(employee.getPrimaryKey().getId()));
            ArrayList<Map> skills = new ArrayList<>();
            for (EmployeeSkill employeeSkill : employee.getEmployeeSkills()){
                Map<String, Object> map = new HashMap<>();
                map.put("id", employeeSkill.getSkill().getId());
                map.put("name", employeeSkill.getSkill().getName());
                map.put("isPrimary", employeeSkill.getIsPrimarySkill());
                skills.add(map);
            }
            employeeReportDto.setSkills(skills);
            dtoHashMap.put(employee.getId(), employeeReportDto);
        }

        Collection<Object[]> shifts = shiftService.getShiftsOps(schedule.getId(), schedule.getTenantId(), null, null,
                "employeeId,shiftLength", 0, 0, null, null).getResult();

        for (Object[] shift : shifts) {
            EmployeeReportDto employeeReportDto1 = dtoHashMap.get(shift[0]);
            if (employeeReportDto1 != null) {
                employeeReportDto1.setTotalHours(employeeReportDto1.getTotalHours() +
                        ((Integer) shift[1]).doubleValue() / 60);
            }
        }
        scheduleReportDto.setEmployees(dtoHashMap.values().toArray());

        List<Map> summaryBySkill = scheduleService.getSummaryBySkill(schedule);
        List<Map> avalHoursBySkill = scheduleService.getAvalHoursBySkill(schedule, avalHours);
        for (Map skillInfo : summaryBySkill) {
            String id = (String)skillInfo.get("id");
            ScheduleReportBySkillDto reportBySkillDto = (ScheduleReportBySkillDto)skillInfo.get("report");

            for (Map hours : avalHoursBySkill) {
                if ((hours.get("id")).equals(id)) {
                    reportBySkillDto.setResourcesHours((double[]) hours.get("hours"));
                    break;
                }
            }
            skillInfo.put("report", reportBySkillDto);
        }
        scheduleReportDto.setSummaryBySkill(summaryBySkill);
        return scheduleReportDto;
    }

    @Validation
    public ScheduleDto resetState(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        return toDto(scheduleService.resetState(schedulePrimaryKey), ScheduleDto.class);
    }

    @Validation
    public ScheduleDto promote(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        return toDto(scheduleService.promote(schedulePrimaryKey), ScheduleDto.class);
    }

    @Validation
    public ScheduleDto duplicate(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            String name,
            AssignmentMode mode,
            long startDate,
            String managerAccountId) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        UserAccount managerAccount = null;
        if (managerAccountId != null) {
            managerAccount = userAccountService.getUserAccount(new PrimaryKey(schedulePrimaryKey.getTenantId(),
                    managerAccountId));
        }
        Schedule schedule = scheduleService.duplicate(schedulePrimaryKey, name, mode, startDate, managerAccount);
        return toDto(schedule, ScheduleDto.class);
    }

    @Validation
    public ScheduleDto execute(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            ScheduleExecuteDto scheduleExecuteDto,
            String managerAccountId) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        UserAccount managerAccount = null;
        if (managerAccountId != null) {
            managerAccount = userAccountService.getUserAccount(new PrimaryKey(schedulePrimaryKey.getTenantId(),
                    managerAccountId));
        }
        Schedule schedule = scheduleService.execute(schedulePrimaryKey,
                scheduleExecuteDto.getMaxComputationTime(),
                scheduleExecuteDto.getMaximumUnimprovedSecondsSpent(),
                scheduleExecuteDto.getPreservePreAssignedShifts(),
                scheduleExecuteDto.getPreservePostAssignedShifts(),
                scheduleExecuteDto.getPreserveEngineAssignedShifts(),
                managerAccount);
        return toDto(schedule, ScheduleDto.class);
    }

    @Validation
    public ScheduleDto generateShifts(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            String managerAccountId) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Schedule schedule = scheduleService.getSchedule(schedulePrimaryKey);
        UserAccount managerAccount = null;
        if (managerAccountId != null) {
            managerAccount = userAccountService.getUserAccount(new PrimaryKey(schedulePrimaryKey.getTenantId(),
                    managerAccountId));
        }
        schedule = scheduleService.generateShifts(schedule, 0, managerAccount);
        return toDto(schedule, ScheduleDto.class);
    }

    @Validation
    public ResultSetDto<BaseScheduleChangeDto> getScheduleChanges(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            long startDate,
            long endDate,
            String type,
            String employeeIds,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InvocationTargetException, NoSuchMethodException, InstantiationException,
            IllegalAccessException {
        SimpleQuery simpleQuery = new SimpleQuery(schedulePrimaryKey.getTenantId());
        String filter = "scheduleId = '" + schedulePrimaryKey.getId() + "'";
        if (startDate != 0 && endDate != 0 && startDate < endDate) {
            filter += (" and changeDate >= '" + new DateTime(startDate) + "' and changeDate < '"
                    + new DateTime(endDate) + "'");
        } else {
            if (startDate != 0) {
                filter += (" and changeDate >= '" + new DateTime(startDate) + "'");
            }
            if (endDate != 0) {
                filter += (" and changeDate < '" + new DateTime(endDate) + "'");
            }
        }
        if (StringUtils.isNotBlank(type)) {
            filter += (" and type = '" + ChangeType.valueOf(type) + "'");
        }
        if (StringUtils.isNotBlank(employeeIds)) {
            filter += (" and (employeeAId in (" + employeeIds + ")" +
                    "OR employeeAId in (" + employeeIds + ") ");
        }
        simpleQuery.setSelect(select)
                .setFilter(filter)
                .setOffset(offset)
                .setLimit(limit)
                .setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC"))
                .setTotalCount(true);
        ResultSet<BaseScheduleChange> resultSet = trackingService.findChangeRecords(simpleQuery);

        Map<Class<? extends BaseScheduleChange>, Class<? extends BaseScheduleChangeDto>> classMap = new HashMap<>();
        classMap.put(ScheduleChange.class, ScheduleChangeDto.class);
        classMap.put(ShiftAddChange.class, ShiftAddChangeDto.class);
        classMap.put(ShiftAssignChange.class, ShiftAssignChangeDto.class);
        classMap.put(ShiftDeleteChange.class, ShiftDeleteChangeDto.class);
        classMap.put(ShiftDropChange.class, ShiftDropChangeDto.class);
        classMap.put(ShiftEditChange.class, ShiftEditChangeDto.class);
        classMap.put(ShiftSwapChange.class, ShiftSwapChangeDto.class);
        classMap.put(ShiftWipChange.class, ShiftWipChangeDto.class);

        return toResultSetDto(resultSet, classMap);
    }

    @Validation
    public boolean deleteScheduleChanges(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class) PrimaryKey primaryKey) {
        trackingService.deleteBySchedule(primaryKey);
        return true;
    }

    @Validation
    public ResultSetDto<TeamDto> getTeams(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            String filter,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(schedulePrimaryKey.getTenantId());
        simpleQuery.setSelect(select)
                .setFilter(filter)
                .setOffset(offset)
                .setLimit(limit)
                .setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equals(orderDir, "ASC"))
                .setTotalCount(true)
                .setEntityClass(Team.class);
        ResultSet<Team> resultSet = scheduleService.getTeams(schedulePrimaryKey, simpleQuery);
        return toResultSetDto(resultSet, TeamDto.class);
    }

    @Validation
    public Collection<SchedulePatternDto> associateTeams(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            String managerAccountId,
            String... ids) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException, NoSuchFieldException {
    	UserAccount managerAccount = null;
        if (managerAccountId != null) {
            managerAccount = userAccountService.getUserAccount(new PrimaryKey(schedulePrimaryKey.getTenantId(),
                    managerAccountId));
        }
        Schedule schedule = scheduleService.getSchedule(schedulePrimaryKey);
        if (!ScheduleType.ShiftPatternBased.equals(schedule.getScheduleType())) {
            throw new ValidationException(getMessage("schedule.type.error"));
        }
        if (Posted.equals(schedule.getStatus()) || ScheduleStatus.Production.equals(schedule.getStatus())) {
            throw new ValidationException(getMessage("schedule.forbidden.field.change", "teams"));
        }
        scheduleService.associateTeams(schedule, Arrays.asList(ids), managerAccount);
        return getSchedulePatternDtos(schedule);
    }

    @Validation
    public ResultSetDto<EmployeeDto> getEmployees(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            String filter,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException, NoSuchFieldException {
        ResultSet<Employee> resultSet = scheduleService.getEmployees(schedulePrimaryKey, filter, offset, limit, orderBy,
                orderDir);
        return toResultSetDto(resultSet, EmployeeDto.class);
    }

    @Validation
    public ResultSetDto<ShiftStructureDto> getShiftStructures(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            String filter,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(schedulePrimaryKey.getTenantId());
        simpleQuery.setSelect(select)
                .setFilter(filter)
                .setOffset(offset)
                .setLimit(limit)
                .setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC"))
                .setTotalCount(true)
                .setEntityClass(ShiftStructure.class);
        ResultSet<ShiftStructure> resultSet = scheduleService.getShiftStructures(schedulePrimaryKey, simpleQuery);
        return toResultSetDto(resultSet, ShiftStructureDto.class);
    }

    @Validation
    public ResultSetDto<ShiftDropChangeDto> getShiftDropChanges(PrimaryKey schedulePK)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        SimpleQuery simpleQuery = new SimpleQuery(schedulePK.getTenantId(), ShiftDropChange.class);
        simpleQuery.addFilter("scheduleId = '" + schedulePK.getId() + "'");
        simpleQuery.addFilter("type = '" + ChangeType.SHIFTDROP+"'");

        ResultSet<ShiftDropChange> dropChanges = scheduleService.getShiftDropChanges(simpleQuery);

        ResultSet<ShiftDropChangeDto> result = new ResultSet<>();
        result.setResult(new ArrayList<ShiftDropChangeDto>());
        for(ShiftDropChange dropChange : dropChanges.getResult()) {
            ShiftDropChangeDto dto = toDto(dropChange, ShiftDropChangeDto.class);
            result.getResult().add(dto);
        }

        ResultSetDto<ShiftDropChangeDto> resultSetDto = toResultSetDto(result, ShiftDropChangeDto.class);
        resultSetDto.setTotal(dropChanges.getResult().size());

        return resultSetDto;
    }

    @Validation
    public void abort(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey primaryKey,
            long timeout) {
        Schedule schedule = scheduleService.getSchedule(primaryKey);
        if (!(TaskState.Queued.equals(schedule.getState()) || TaskState.Running.equals(schedule.getState()))) {
            throw new ValidationException(getMessage("validation.schedule.state.nonoperational"));
        }
        schedule.setState(TaskState.Aborting);
        scheduleService.update(schedule);
        hazelcastService.abort(schedule.getRequestId(), timeout);
    }

    @Validation
    public ScheduleDto cloneSchedule(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey primaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        Schedule schedule = scheduleService.cloneSchedule(primaryKey);
        return toDto(schedule, ScheduleDto.class);
    }

    @Validation
    public SchedulingOptionsDto getOptions(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey primaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        Schedule schedule = scheduleService.getSchedule(primaryKey);
        return toDto(schedule.getSchedulingOptions(), SchedulingOptionsDto.class);
    }

    @Validation
    public SchedulingOptionsDto updateOptions(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            SchedulingOptionsDto schedulingOptionsDto) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Schedule schedule = scheduleService.getSchedule(schedulePrimaryKey);

        SchedulingOptions schedulingOptions = schedule.getSchedulingOptions();
        if (schedulingOptions == null) {
            schedulingOptions = new SchedulingOptions();
            schedulingOptions.setPrimaryKey(new PrimaryKey(schedulePrimaryKey.getTenantId()));
            schedulingOptions.setOverrideOptions(schedulingOptionsDto.getOverrideOptions());
            scheduleService.insertSchedulingOptions(schedulingOptions);
        } else {
            schedulingOptions.setOverrideOptions(schedulingOptionsDto.getOverrideOptions());
            scheduleService.updateSchedulingOptions(schedulingOptions);
        }

        schedule.setSchedulingOptions(schedulingOptions);
        scheduleService.update(schedule);

        return toDto(schedule.getSchedulingOptions(), SchedulingOptionsDto.class);
    }

    private void associateShiftStructures(Schedule schedule, Set<Team> teams) {
        Set<ShiftStructure> shiftStructureSet = new HashSet<>();

        for (Team team : teams) {
            ShiftStructure shiftStructure = getShiftStructureByDate(team, schedule.getStartDate());
            if (shiftStructure == null) {
                throw new ValidationException(getMessage("validation.schedule.shiftstructure.startdate",
                        team.getName(), schedule.getStartDate()));
            } else {
                shiftStructureSet.add(shiftStructure);
            }
        }

        schedule.setShiftStructures(shiftStructureSet);
    }

    private ShiftStructure getShiftStructureByDate(Team team, long startDate) {
        Set<ShiftStructure> shiftStructureSet = team.getShiftStructures();
        for (ShiftStructure shiftStructure : shiftStructureSet) {
            if (startDate == shiftStructure.getStartDate()) {
                return shiftStructure;
            }
        }
        return null;
    }

    /**
     * Manual shift delete.
     *
     * TODO: Hickory/Aspen does not support manual deletion of shifts from a schedule.
     *       If this is allowed in Mercury, some consideration should be given to the
     *       behavior.  For example, when and under what circumstances can deletes
     *       be performed?  Can deletes be performed on assigned shifts, or should
     *       validation be added to first check that is it an open shift?  Etcetera.
     *
     * @param schedulePk
     * @param shiftPk
     * @param managerAccountId
     * @return
     */
    @Validation
	public boolean manualShiftDelete(
			@Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
			PrimaryKey schedulePk,
			@Validate(validator = EntityExistValidatorBean.class, type = Shift.class)
			PrimaryKey shiftPk, 
			String wflRequestId, 
			String managerAccountId,
			String reason) {
    	Schedule schedule = scheduleService.getSchedule(schedulePk);
    	Shift shift = shiftService.getShift(shiftPk);
    	UserAccount mngrAccount = null;
    	if (managerAccountId != null) {
    		mngrAccount = userAccountService.getUserAccount(new PrimaryKey(schedule.getTenantId(), managerAccountId));
    	}

    	// We should only delete unassigned shifts, so let's validate that...
    	if (shift.getAssigned() != null || shift.getAssignmentType() != null
    			|| shift.getEmployeeId() != null || shift.getEmployeeName() != null) {
    		throw new ValidationException(getMessage("validation.schedule.shift.notunassigned", shift));
    	}

    	if (!shift.getScheduleId().equals(schedule.getId())) {
    		throw new ValidationException(getMessage("validation.schedule.shift.norelation", shift));
    	} else {
    		scheduleService.manualShiftDelete( schedule, shift, wflRequestId, mngrAccount, reason);
    		return true;
    	}
	}

    @Validation
    public ShiftDto migrateShift(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePk,
            ShiftMigrateDto shiftDto) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
    	Shift shift = manualShiftCreateTasks(schedulePk, shiftDto);

        shift.setShiftStructureId(shiftDto.getShiftStructureId());
        shift.setShiftLengthId(shiftDto.getShiftLengthId());
        shift.setShiftLengthName(shiftDto.getShiftLengthName());
        shift.setSiteName(shiftDto.getSiteName());
        shift.setScheduleStatus(shiftDto.getScheduleStatus());
        
        if( !StringUtils.isBlank(shiftDto.getEmployeeId()) ) {
        	Employee employee = employeeService.getEmployee(new PrimaryKey(shift.getTenantId(), shiftDto.getEmployeeId()));
        	StringBuilder employeeName = new StringBuilder();
	    	employeeName.append(employee.getFirstName());
	    	if (employee.getMiddleName() != null) {
	    		employeeName.append(" ");
	            employeeName.append(employee.getMiddleName());
	        }
	    	employeeName.append(" ");
	    	employeeName.append(employee.getLastName());
	
	    	shift.makeShiftAssignment(employee.getId(), employeeName.toString(), AssignmentType.ENGINE, new DateTime(shiftDto.getStartDateTime()));
        }
        
    	shift = shiftService.update(shift);

    	return toDto(shift, ShiftDto.class);
    }

    @Validation
	public ShiftDto manualShiftCreate(
			@Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
			PrimaryKey schedulePk,
			ShiftCreateDto shiftCreateDto,
			String wflRequestId, 
			String managerAccountId,
			String reason) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
    	
    	Schedule schedule = scheduleService.getSchedule(schedulePk);
    	UserAccount mngrAccount = null;
    	if (managerAccountId != null) {
    		mngrAccount = userAccountService.getUserAccount(new PrimaryKey(schedule.getTenantId(), managerAccountId));
    	}
        Shift shift = manualShiftCreateTasks(schedulePk, shiftCreateDto);

        shift.setEmployeeIndex(0);

        // There is no ShiftStructure or ShiftType when created manually...
        shift.setShiftStructureId(null);
        shift.setShiftLengthId(null);
        shift.setShiftLengthName(null);
   		return toDto(scheduleService.manualShiftCreate(schedule, shift, wflRequestId,  mngrAccount, reason), ShiftDto.class);
	}

    @Validation
	public ShiftDto shiftCreate(
			@Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            CreateShiftParamsDto createShiftParamsDto,
			String wflRequestId, 
			String managerAccountId) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        String tenantId = schedulePrimaryKey.getTenantId();
        Schedule schedule = scheduleService.getSchedule(schedulePrimaryKey);

        ShiftCreateDto shiftInfo = createShiftParamsDto.getShiftInfo();
        Skill skill = skillService.getSkill(new PrimaryKey(tenantId, shiftInfo.getSkillId()));
        if (skill == null) {
            throw new ValidationException(getMessage("validation.schedule.noskill", shiftInfo.getSkillId()));
        }
        Team team = teamService.getTeam(new PrimaryKey(tenantId, shiftInfo.getTeamId()));
        if (team == null) {
            throw new ValidationException(getMessage("validation.schedule.noteam", shiftInfo.getTeamId()));
        }
        if (StringUtils.isNotEmpty(createShiftParamsDto.getEmployeeId())) {
            PrimaryKey employeePrimaryKey = new PrimaryKey(tenantId, createShiftParamsDto.getEmployeeId());
            Employee employee = employeeService.getEmployee(employeePrimaryKey);
            if (employee == null) {
                throw new ValidationException(getMessage("validation.entity.nonexist", "Employee",
                        createShiftParamsDto.getEmployeeId()));
            }
        } else {
            Collection<String> ids = createShiftParamsDto.getEmployeeIds();
            if (ids != null && !ids.isEmpty()) {
                SimpleQuery simpleQuery = new SimpleQuery(tenantId);
                simpleQuery.setEntityClass(Employee.class).setFilter("primaryKey.id IN (" +
                        ModelUtils.commaSeparatedQuotedValues(ids) + ")");
                ResultSet<Employee> employeeResultSet = employeeService.findEmployees(simpleQuery);
                Set<String> returnedIds = ModelUtils.idSet(employeeResultSet.getResult());
                for (String id : ids) {
                    if (!returnedIds.contains(id)) {
                        throw new ValidationException(getMessage("validation.entity.nonexist", "Employee", id));
                    }
                }
            }
        }

        if (shiftInfo.getStartDateTime() > shiftInfo.getEndDateTime()) {
            throw new ValidationException(getMessage("validation.error.startdate.enddate"));
        }
        if (!(schedule.getStartDate() <= shiftInfo.getStartDateTime()
                && schedule.getEndDate() >= shiftInfo.getEndDateTime())) {
            throw new ValidationException(getMessage("validation.shiftdates.beyond.scheduledates"));
        }

    	UserAccount managerAccount = null;
    	if (managerAccountId != null) {
    		managerAccount = userAccountService.getUserAccount(new PrimaryKey(tenantId, managerAccountId));
    	}
    	Shift newShift = this.doCreateShift(schedule, createShiftParamsDto, wflRequestId, managerAccount,
                createShiftParamsDto.getComment());
    	return toDto(newShift, ShiftDto.class);
	}

    private Shift manualShiftCreateTasks(PrimaryKey schedulePrimaryKey, ShiftCreateDto shiftDto) {
        Shift shift = new Shift(new PrimaryKey(schedulePrimaryKey.getTenantId()));

        shift.setComment(shiftDto.getComment());

        Team team = teamService.getTeam(new PrimaryKey(schedulePrimaryKey.getTenantId(), shiftDto.getTeamId()));
    	if (team == null) {
    		throw new ValidationException(getMessage("validation.schedule.shift.noteam"));
    	} else {
            shift.setTeamId(team.getId());
            shift.setTeamName(team.getName());
    	}

    	Skill skill = skillService.getSkill(new PrimaryKey(schedulePrimaryKey.getTenantId(), shiftDto.getSkillId()));
    	if (skill == null) {
    		throw new ValidationException(getMessage("validation.schedule.noskill", shiftDto.getSkillId()));
    	} else if (!team.getSkills().contains(skill)) {
    		throw new ValidationException(getMessage("validation.schedule.shift.noskillteam"));
    	} else {
            shift.setSkillId(skill.getId());
            shift.setSkillName(skill.getName());
            shift.setSkillAbbrev(skill.getAbbreviation());
    	}
        shift.setSkillProficiencyLevel(shiftDto.getSkillProficiencyLevel());

    	if (shiftDto.getStartDateTime() > shiftDto.getEndDateTime()) {
    		throw new ValidationException(getMessage("validation.schedule.shift.invalidendtime"));
        } else {
            shift.setStartDateTime(shiftDto.getStartDateTime());
            shift.setEndDateTime(shiftDto.getEndDateTime());
        }

    	int shiftLength = (int) new Interval(shiftDto.getStartDateTime(), shiftDto.getEndDateTime()).toDuration()
                .getStandardMinutes();
        shift.setShiftLength(shiftLength);

        if (shiftLength < shiftDto.getPaidTime()) {
    		throw new ValidationException(getMessage("validation.schedule.shift.invalidpaidtime"));
        } else {
            shift.setPaidTime(shiftDto.getPaidTime());
        }

        shift.setScheduleId(schedulePrimaryKey.getId());
        getEventService().sendEntityCreateEvent(shift, com.emlogis.model.schedule.dto.ShiftDto.class);
    	return shift;
    }

    @Validation
	public ShiftDto manualShiftEdit(
			@Validate(validator = EntityExistValidatorBean.class, type = Schedule.class) PrimaryKey schedulePk,
			@Validate(validator = EntityExistValidatorBean.class, type = Shift.class) PrimaryKey shiftPk,
			ShiftUpdateDto shiftDto,
			String wflRequestId, 
			String managerAccountId,
			String reason) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
    	Schedule schedule = scheduleService.getSchedule(schedulePk);
        Shift shift = shiftService.getShift(shiftPk);
        Shift previousShift = (Shift) BeanUtils.cloneBean(shift);

    	UserAccount managerAccount = null;
    	if (managerAccountId != null) {
            managerAccount = userAccountService.getUserAccount(new PrimaryKey(schedulePk.getTenantId(),
                    managerAccountId));
    	}

        if (shiftDto != null) {
            if (StringUtils.isNotBlank(shiftDto.getSkillId())) {
                Team team = teamService.getTeam(new PrimaryKey(schedule.getTenantId(), shift.getTeamId()));
                Skill skill = skillService.getSkill(new PrimaryKey(schedule.getTenantId(), shiftDto.getSkillId()));
                if (skill == null) {
                    throw new ValidationException(getMessage("validation.schedule.shift.noskill"));
                } else if (!team.getSkills().contains(skill)) {
                    throw new ValidationException(getMessage("validation.schedule.shift.noskillteam"));
                } else {
                    shift.setSkillId(skill.getId());
                    shift.setSkillName(skill.getName());
                    shift.setSkillAbbrev(skill.getAbbreviation());
                }
            }

            if (shiftDto.getStartDateTime() != null) {
                shift.setStartDateTime(shiftDto.getStartDateTime());
            }
            if (shiftDto.getEndDateTime() != null) {
                shift.setEndDateTime(shiftDto.getEndDateTime());
            }
            if (shiftDto.getPaidTime() != null) {
                shift.setPaidTime(shiftDto.getPaidTime());
            }

            Interval shiftInterval;
            try {
                shiftInterval = new Interval(shiftDto.getStartDateTime(), shiftDto.getEndDateTime());
            } catch (IllegalArgumentException e) {
                throw new ValidationException(getMessage("validation.schedule.shift.invalidendtime"));
            }

            int calculatedShiftLength = (int) shiftInterval.toDuration().getStandardMinutes();
            if (shift.getPaidTime() > calculatedShiftLength) {
                throw new ValidationException(getMessage("validation.schedule.shift.invalidpaidtime"));
            }

            shift.setShiftLength(calculatedShiftLength);
        }

        shift = scheduleService.manualShiftEdit(schedule, previousShift, shift, wflRequestId, managerAccount, reason);

        return toDto(shift, ShiftDto.class);
	}

    @Validation
	public ShiftDto manualShiftDrop(
			@Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
			PrimaryKey schedulePk,
			@Validate(validator = EntityExistValidatorBean.class, type = Shift.class)
			PrimaryKey shiftPk, 
            String wflRequestId, 
            String managerAccountId, 
            String reason) throws InstantiationException, IllegalAccessException, InvocationTargetException, 
            NoSuchMethodException {
    	Schedule schedule = scheduleService.getSchedule(schedulePk);
    	Shift shift = shiftService.getShift(shiftPk);
    	UserAccount mngrAccount = null;
    	if (managerAccountId != null) {
    		mngrAccount = userAccountService.getUserAccount(new PrimaryKey(schedulePk.getTenantId(), managerAccountId));
    	}

    	if (!shift.getScheduleId().equals(schedule.getId())) {
    		throw new ValidationException( getMessage("validation.schedule.shift.norelation", shift) );
    	} else if (shift.getAssigned() == null && shift.getAssignmentType() == null
    			&& shift.getEmployeeId() == null && shift.getEmployeeName() == null) {
    		throw new ValidationException( getMessage("validation.schedule.shift.alreadyunassigned") );
    	} else {
    		return toDto(scheduleService.manualShiftDrop(schedule, shift, wflRequestId, mngrAccount, reason),
                    ShiftDto.class);
    	}
	}

    /** Assign employee to work open shift IF QUALIFIED.
     *  NOTE: Facade method implemented for pattern consistency, but just passing args on to
     *        service layer.  For this functionality, validation and JPA entity retrieval are
     *        performed in service layer in order to control transaction lifecycles.
     *
     * @param schedulePk
     * @param shiftPk
     * @param employeePk
     * @param force
     * @param overrideOptions 
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public QualificationRequestSummary manualShiftOpenAssign(PrimaryKey schedulePk, PrimaryKey shiftPk,
    		PrimaryKey employeePk, Boolean force, String wflRequestId, String managerAccountId, String reason, 
    		Map<ConstraintOverrideType, Boolean> overrideOptions)
    				throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        try {
            UserAccount mngrAccount = null;
            if (managerAccountId != null) {
                mngrAccount = userAccountService.getUserAccount(new PrimaryKey(schedulePk.getTenantId(), managerAccountId));
            }
            return scheduleService.manualShiftOpenAssign(schedulePk, shiftPk, 
            		employeePk, force, wflRequestId, mngrAccount, reason, overrideOptions);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }

    /** Assign employee to work in place (WIP) IF QUALIFIED.
     *  NOTE: Facade method implemented for pattern consistency, but just passing args on to
     *        service layer.  For this functionality, validation and JPA entity retrieval are
     *        performed in service layer in order to control transaction lifecycles.
     *
     * @param schedulePk
     * @param shiftPk
     * @param wipEmployeePk
     * @param force
     * @param managerAccountId
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public QualificationRequestSummary manualShiftWIP(PrimaryKey schedulePk, PrimaryKey shiftPk,
                                                      PrimaryKey wipEmployeePk, Boolean force, String wflRequestId,
                                                      String managerAccountId, String reason)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	UserAccount mngrAccount = null;
    	if (managerAccountId != null) {
    		mngrAccount = userAccountService.getUserAccount(new PrimaryKey(schedulePk.getTenantId(), managerAccountId));
    	}
        return scheduleService.manualShiftWIP(schedulePk, shiftPk, wipEmployeePk, force, wflRequestId, mngrAccount, reason);
    }

    /** Swap shifts IF QUALIFIED.
     *  NOTE: Facade method implemented for pattern consistency, but just passing args on to
     *        service layer.  For this functionality, validation and JPA entity retrieval are
     *        performed in service layer in order to control transaction lifecycles.
     *
     * @param schedulePk
     * @param shiftAPk
     * @param shiftBPk
     * @param force
     * @param managerAccountId
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public QualificationRequestSummary manualShiftSwap(
            PrimaryKey schedulePk,
            PrimaryKey shiftAPk,
            PrimaryKey shiftBPk,
            Boolean force,
            String wflRequestId,
            String managerAccountId,
            String reason) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
    	UserAccount mngrAccount = null;
    	if (managerAccountId != null) {
    		mngrAccount = userAccountService.getUserAccount(new PrimaryKey(schedulePk.getTenantId(), managerAccountId));
    	}
		return scheduleService.manualShiftSwap(schedulePk, shiftAPk, shiftBPk, force, wflRequestId, mngrAccount, reason);
	}

    @Validation
    public Collection<SchedulePatternDto> getPatternElts(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class) PrimaryKey primaryKey) {
        Schedule schedule = scheduleService.getSchedule(primaryKey);
        return getSchedulePatternDtos(schedule);
    }

    @Validation
    public Collection<ShiftPatternDto> getApplicableShiftPatterns(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey primaryKey,
            String filter,
            int offset,
            int limit,
            String orderBy,
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        Collection<ShiftPattern> shiftPatterns = scheduleService.getApplicableShiftPatterns(primaryKey, filter, offset,
                limit, orderBy, orderDir);

        DtoMapper<ShiftPattern, ShiftPatternDto> dtoMapper = new DtoMapper<>();
        dtoMapper.registerExceptDtoFieldForMapping("shiftReqDtos");
        dtoMapper.registerExceptDtoFieldForMapping("shiftDemandDtos");
        dtoMapper.registerExceptDtoFieldForMapping("teamId");

        return dtoMapper.map(shiftPatterns, ShiftPatternDto.class);
    }

    @Validation
    public ScheduleDto fullUpdate(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            SchedulePatternFullUpdateDto schedulePatternFullUpdateDto,
			String managerAccountId) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Schedule schedule = scheduleService.getSchedule(schedulePrimaryKey);
    	UserAccount managerAccount = null;
    	if (managerAccountId != null) {
    		managerAccount = userAccountService.getUserAccount(new PrimaryKey(schedule.getTenantId(), managerAccountId));
    	}
        if (!ScheduleType.ShiftPatternBased.equals(schedule.getScheduleType())) {
            throw new ValidationException(getMessage("schedule.type.error"));
        }
        if (schedulePatternFullUpdateDto.getTeamIds() != null) {
            if (Posted.equals(schedule.getStatus()) || ScheduleStatus.Production.equals(schedule.getStatus())) {
                throw new ValidationException(getMessage("schedule.forbidden.field.change", "teams"));
            }
            scheduleService.associateTeams(schedule, schedulePatternFullUpdateDto.getTeamIds(), managerAccount);
        }
        updatePatternElts(schedulePrimaryKey, schedulePatternFullUpdateDto);
        return toDto(scheduleService.getSchedule(schedulePrimaryKey), ScheduleDto.class);
    }

    @Validation
    public Collection<SchedulePatternDto> updatePatternElts(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey primaryKey,
            SchedulePatternFullUpdateDto schedulePatternFullUpdateDto) {
        String tenantId = primaryKey.getTenantId();

        Schedule schedule;

        ScheduleUpdateDto scheduleUpdateDto = schedulePatternFullUpdateDto.getScheduleUpdateDto();
        if (scheduleUpdateDto != null) {
            schedule = scheduleToUpdate(primaryKey, scheduleUpdateDto);
        } else {
            schedule = scheduleService.getSchedule(primaryKey);
        }

        Set<PatternElt> patternElts = schedule.getPatternElts();

        Set<PatternElt> patternEltsToDelete = new HashSet<>();
        patternEltsToDelete.addAll(patternElts);

        List<String> teamIds = schedulePatternFullUpdateDto.getTeamIds();

        List<SchedulePatternDto> schedulePatternDtos = schedulePatternFullUpdateDto.getSchedulePatternDtos();
        if (schedulePatternDtos != null) {
            for (SchedulePatternDto schedulePatternDto : schedulePatternDtos) {
                boolean createFlag = true;
                for (PatternElt patternElt : patternElts) {
                    ShiftPattern shiftPattern = patternElt.getShiftPattern();
                    if (teamIds != null && teamIds.contains(shiftPattern.getTeamId())
                            && StringUtils.equals(shiftPattern.getId(), schedulePatternDto.getPatternId())
                            && EmlogisUtils.equals(shiftPattern.getCdDate(), schedulePatternDto.getCdDate())
                            && EmlogisUtils.equals(patternElt.getDayOffset(), schedulePatternDto.getDayOffset())) {
                        // remove this pattern from list for delete
                        patternEltsToDelete.remove(patternElt);
                        // update the patternElt
                        boolean modified = false;
                        if (schedulePatternDto.getCdDate() != null) {
                            patternElt.setCdDate(new DateTime(schedulePatternDto.getCdDate()));
                            modified = true;
                        }
                        if (schedulePatternDto.getDayOffset() != null) {
                            patternElt.setDayOffset(schedulePatternDto.getDayOffset());
                            modified = true;
                        }

                        if (modified) {
                            scheduleService.updatePatternElt(patternElt);
                        }

                        createFlag = false;
                        break;
                    }
                }

                if (createFlag) {
                    PrimaryKey shiftPatternPrimaryKey = new PrimaryKey(tenantId, schedulePatternDto.getPatternId());
                    ShiftPattern shiftPattern = shiftPatternService.getShiftPattern(shiftPatternPrimaryKey);
                    if (shiftPattern == null) {
                        throw new ValidationException(getMessage("validation.shiftpattern.exist.error",
                                schedulePatternDto.getPatternId()));
                    }

                    if (teamIds != null && teamIds.contains(shiftPattern.getTeamId())) {
                        PatternElt patternElt = new PatternElt();
                        patternElt.setPrimaryKey(new PrimaryKey(tenantId));
                        patternElt.setSchedule(schedule);
                        patternElt.setShiftPattern(shiftPattern);
                        if (schedulePatternDto.getCdDate() != null) {
                            patternElt.setCdDate(new DateTime(schedulePatternDto.getCdDate()));
                        }
                        if (schedulePatternDto.getDayOffset() != null) {
                            patternElt.setDayOffset(schedulePatternDto.getDayOffset());
                        }

                        scheduleService.createPatternElt(patternElt);

                        patternElts.add(patternElt);
                    }
                }
            }
        }

        patternElts.removeAll(patternEltsToDelete);

        for (PatternElt patternElt : patternEltsToDelete) {
            scheduleService.deletePatternElt(patternElt);
        }

        getEventService().sendEntityUpdateEvent(schedule, ScheduleDto.class);
        return getSchedulePatternDtos(schedule);
    }

    private Collection<SchedulePatternDto> getSchedulePatternDtos(Schedule schedule) {
        Collection<SchedulePatternDto> result = new HashSet<>();

        Set<PatternElt> patternElts = schedule.getPatternElts();
        for (PatternElt patternElt : patternElts) {
            SchedulePatternDto schedulePatternDto = new SchedulePatternDto();

            if (patternElt.getCdDate() != null) {
                schedulePatternDto.setCdDate(patternElt.getCdDate().toDate().getTime());
            }
            schedulePatternDto.setDayOffset(patternElt.getDayOffset());

            ShiftPattern shiftPattern = patternElt.getShiftPattern();
            if (shiftPattern.getCdDate() != null) {
                schedulePatternDto.setPatternCdDate(shiftPattern.getCdDate().toDate().getTime());
            }
            schedulePatternDto.setPatternDayOfWeek(shiftPattern.getDayOfWeek());
            schedulePatternDto.setPatternId(shiftPattern.getId());
            schedulePatternDto.setPatternName(shiftPattern.getName());
            if (shiftPattern.getTeam() != null) {
                schedulePatternDto.setPatternTeamName(shiftPattern.getTeam().getName());
            }

            result.add(schedulePatternDto);
        }
        return result;
    }

    @Validation
	public ScheduleSettingsDto getSettings(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class) PrimaryKey primaryKey)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Schedule schedule = scheduleService.getSchedule(primaryKey);

        ScheduleSettingsDto settingsDto = new ScheduleSettingsDto();
        settingsDto.setMaxComputationTime(schedule.getMaxComputationTime());
        settingsDto.setMaximumUnimprovedSecondsSpent(schedule.getMaximumUnimprovedSecondsSpent());
        settingsDto.setPreservePreAssignedShifts(schedule.isPreservePreAssignedShifts());
        settingsDto.setPreservePostAssignedShifts(schedule.isPreservePostAssignedShifts());
        settingsDto.setPreserveEngineAssignedShifts(schedule.isPreserveEngineAssignedShifts());
        settingsDto.setRuleWeightMultipliers(schedule.getRuleWeightMultipliers());
        settingsDto.setOverrideOptions(schedule.getSchedulingOptions().getOverrideOptions());
        settingsDto.setOptimizationPreferenceSetting(schedule.getSchedulingOptions().getOptimizationPreferenceSetting());
        settingsDto.setSiteOptimizationPreferenceSetting(scheduleService.getSite(schedule).getSchedulingSettings().getCopSetting());
        settingsDto.setOverrideOptimizationPreference(schedule.getSchedulingOptions().isOverrideOptimizationPreference());

        Map<String, Integer> addInfo = new HashMap<>();
        addInfo.put("allShiftsCount", shiftService.getScheduleShifts(schedule).size());
        addInfo.put("preAssignedShiftsCount", shiftService.getPreAssignedShifts(schedule).size());
        addInfo.put("postAssignedShiftsCount", shiftService.getPostAssignedShifts(schedule).size());
        addInfo.put("engineAssignedShiftsCount", shiftService.getEngineAssignedShifts(schedule).size());
        settingsDto.setAdditionInfo(addInfo);

        ResultSet<Employee> resultSet = scheduleService.getEmployees(primaryKey, "", 0, 0, null, null);
        HashMap<String, EmployeeReportDto> dtoHashMap = new HashMap<>();
        EmployeeReportDto employeeReportDto;
        for (Employee employee : resultSet.getResult()){
            employeeReportDto = new EmployeeReportDto();
            employeeReportDto.setId(employee.getId());
            employeeReportDto.setFirstName(employee.getFirstName());
            employeeReportDto.setLastName(employee.getLastName());
            dtoHashMap.put(employee.getId(), employeeReportDto);
        }
        settingsDto.setEmployees(dtoHashMap.values().toArray());
		return settingsDto;
	}

    @Validation
	public ScheduleSettingsDto setSettings(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class) PrimaryKey primaryKey,
			ScheduleSettingsDto settingsDto) throws InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        Schedule schedule = scheduleService.getSchedule(primaryKey);

        schedule.setMaxComputationTime(settingsDto.getMaxComputationTime());
        schedule.setMaximumUnimprovedSecondsSpent(settingsDto.getMaximumUnimprovedSecondsSpent());
        schedule.setPreservePreAssignedShifts(settingsDto.isPreservePreAssignedShifts());
        schedule.setPreservePostAssignedShifts(settingsDto.isPreservePostAssignedShifts());
        schedule.setPreserveEngineAssignedShifts(settingsDto.isPreserveEngineAssignedShifts());
        schedule.setRuleWeightMultipliers(settingsDto.getRuleWeightMultipliers());
        schedule.getSchedulingOptions().setOverrideOptions(settingsDto.getOverrideOptions());
        schedule.getSchedulingOptions().setOptimizationPreferenceSetting(settingsDto.getOptimizationPreferenceSetting());
        schedule.getSchedulingOptions().setOverrideOptimizationPreference(settingsDto.isOverrideOptimizationPreference());

        scheduleService.update(schedule);

        SchedulingOptionsDto optionsDto = new SchedulingOptionsDto();
        optionsDto.setOverrideOptions(settingsDto.getOverrideOptions());
        updateOptions(primaryKey, optionsDto);
		return settingsDto;
	}

    private Schedule scheduleToUpdate(PrimaryKey primaryKey, ScheduleUpdateDto scheduleUpdateDto) {
        boolean modified = false;
        Schedule schedule = scheduleService.getSchedule(primaryKey);

        if (scheduleUpdateDto.getStartDate() != null && scheduleUpdateDto.getStartDate() > 0) {
            if (Posted.equals(schedule.getStatus())
                    || ScheduleStatus.Production.equals(schedule.getStatus())) {
                throw new ValidationException(getMessage("schedule.forbidden.field.change", "startDate"));
            }
            schedule.setStartDate(scheduleUpdateDto.getStartDate());
            modified = true;
        }
        if (!StringUtils.isBlank(scheduleUpdateDto.getName())) {
            schedule.setName(scheduleUpdateDto.getName());
            modified = true;
        }
        if (!StringUtils.isBlank(scheduleUpdateDto.getDescription())) {
            schedule.setDescription(scheduleUpdateDto.getDescription());
            modified = true;
        }
        if (scheduleUpdateDto.getRuleWeightMultipliers() != null) {
            schedule.setRuleWeightMultipliers(scheduleUpdateDto.getRuleWeightMultipliers());
            modified = true;
        }
        if (scheduleUpdateDto.getScheduleLengthInDays() > 0
                && scheduleUpdateDto.getScheduleLengthInDays() != schedule.getScheduleLengthInDays()) {
            if (Posted.equals(schedule.getStatus())
                    || ScheduleStatus.Production.equals(schedule.getStatus())) {
                throw new ValidationException(getMessage("schedule.forbidden.field.change", "scheduleLength"));
            }
            schedule.setScheduleLengthInDays(scheduleUpdateDto.getScheduleLengthInDays());
            modified = true;
        }
        if (schedule.isPreservePreAssignedShifts() !=  scheduleUpdateDto.getPreservePreAssignedShifts()) {
            schedule.setPreservePreAssignedShifts(scheduleUpdateDto.getPreservePreAssignedShifts());
            modified = true;
        }
        if (schedule.isPreservePostAssignedShifts() !=  scheduleUpdateDto.getPreservePostAssignedShifts()) {
            schedule.setPreservePostAssignedShifts(scheduleUpdateDto.getPreservePostAssignedShifts());
            modified = true;
        }
        if (schedule.getMaxComputationTime() != scheduleUpdateDto.getMaxComputationTime()) {
            schedule.setMaxComputationTime(scheduleUpdateDto.getMaxComputationTime());
            modified = true;
        }
        if (schedule.getMaximumUnimprovedSecondsSpent() != scheduleUpdateDto.getMaximumUnimprovedSecondsSpent()) {
            schedule.setMaximumUnimprovedSecondsSpent(scheduleUpdateDto.getMaximumUnimprovedSecondsSpent());
            modified = true;
        }
        if (scheduleUpdateDto.getStatus() != null && scheduleUpdateDto.getStatus() != schedule.getStatus()) {
            schedule.setStatus(scheduleUpdateDto.getStatus());
            modified = true;
        }

        if (modified) {
            setUpdatedBy(schedule);
            schedule = scheduleService.update(schedule);
        }

        return schedule;
    }

    public ResultSetDto<ScheduleWithSiteAndTeamsDto> query(
            String tenantId,
            com.emlogis.model.schedule.dto.ScheduleQueryDto queryDto,
            AccountACL acl) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        ResultSet<Object[]> scheduleResultSet = scheduleService.query(
                tenantId,
                queryDto.getSites(),
                queryDto.getTeams(),
                queryDto.getStatuses(),
                queryDto.getStates(),
                queryDto.getScheduleType(),
                queryDto.getScheduleLengthInDays(),
                queryDto.getSearch(),
                queryDto.getStartDate(),
                queryDto.getScheduleDate(),
                queryDto.getPaging() != null ? queryDto.getPaging().getOffset() : -1,
                queryDto.getPaging() != null ? queryDto.getPaging().getLimit() : -1,
                queryDto.getOrdering() != null ? queryDto.getOrdering().getOrderby() : null,
                queryDto.getOrdering() != null ? queryDto.getOrdering().getOrderdir() : null,
                acl);

        Collection<ScheduleWithSiteAndTeamsDto> scheduleWithSiteAndTeamsDtos = new ArrayList<>();

        ResultSetDto<ScheduleWithSiteAndTeamsDto> result = new ResultSetDto<>();
        result.setResult(scheduleWithSiteAndTeamsDtos);
        result.setTotal(scheduleResultSet.getTotal());

        for (Object[] row : scheduleResultSet.getResult()) {
            ScheduleWithSiteAndTeamsDto dto = new ScheduleWithSiteAndTeamsDto();
            scheduleWithSiteAndTeamsDtos.add(dto);

            dto.setId((String) row[0]);
            dto.setName((String) row[1]);
            dto.setDescription((String) row[2]);
            dto.setStartDate(row[3] == null ? 0 : ((Date) row[3]).getTime());
            dto.setEndDate(row[4] == null ? 0 : ((Date) row[4]).getTime());
            dto.setLengthInDays(row[5] == null ? 0 : (int) row[5]);
            dto.setStatus(row[6] == null ? null : ScheduleStatus.values()[(int) row[6]]);
            dto.setState(row[7] == null ? null : TaskState.values()[(int) row[7]]);

            if (row[8] != null) {
                String[] siteParts = ((String) row[8]).split(":");

                ScheduleWithSiteAndTeamsDto.SiteDto siteDto = new ScheduleWithSiteAndTeamsDto.SiteDto();
                dto.setSite(siteDto);

                siteDto.setSiteId(siteParts[0]);
                siteDto.setSiteName(siteParts[1]);

                Site site = siteService.getSite(new PrimaryKey(tenantId, siteDto.getSiteId()));

                siteDto.setSiteTimeZone(site.getTimeZone() == null ? null : site.getTimeZone().toString());
            }

            if (row[9] != null) {
                Collection<ScheduleWithSiteAndTeamsDto.TeamDto> teams = new ArrayList<>();
                dto.setTeams(teams);

                String[] teamPartsCollection = ((String) row[9]).split(",");
                for (String teamInfo : teamPartsCollection) {
                    String[] teamParts = teamInfo.split(":");
                    ScheduleWithSiteAndTeamsDto.TeamDto teamDto = new ScheduleWithSiteAndTeamsDto.TeamDto();
                    teams.add(teamDto);

                    teamDto.setTeamId(teamParts[0]);
                    teamDto.setTeamName(teamParts[1]);
                }
            }
        }

        return result;
    }

	@Validation
	public QualificationRequestTrackerDto getQualificationRequestTracker(
			@Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
			PrimaryKey schedulePk,
			String requestId) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
		QualificationRequestTracker requestTracker = scheduleService.getQualificationRequestTracker(requestId);
		if (requestTracker == null){
	        throw new ValidationException(getMessage("validation.schedule.qualificationrequest.noqualificationrequest", requestId));
		}
		if (!requestTracker.getScheduleId().equals(schedulePk.getId())){
	        throw new ValidationException(getMessage("validation.schedule.qualificationrequest.norelation", requestId));
		}

		QualificationRequestTrackerDto requestTrackerDto = toDto( requestTracker, QualificationRequestTrackerDto.class);
		Collection<ShiftQualificationDto> qualificationShiftDtoCollection =
                toCollectionDto(requestTracker.getQualificationShifts(), ShiftQualificationDto.class);
		requestTrackerDto.setQualificationShifts(qualificationShiftDtoCollection);
		return requestTrackerDto;
	}

    /** Get result from synchronously processed open shift eligibility execution
	 *  NOTE: Facade method implemented for pattern consistency, but just passing args on to
	 *        service layer.  For this functionality, validation and JPA entity retrieval are
	 *        performed in service layer in order to control transaction lifecycles.
	 * @param schedulePk
	 * @param employeeIds
	 * @param shiftIds
	 * @param maxComputationTime
	 * @param maxUnimprovedSecondsSpent
	 * @param maxSynchronousWaitSeconds
	 * @return requestTracker
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@Validation
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public QualificationRequestTrackerDto getOpenShiftEligibility(
	        @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
	        PrimaryKey schedulePk,
	        List<String> employeeIds, List<String> shiftIds,
	        int maxComputationTime, int maxUnimprovedSecondsSpent, int maxSynchronousWaitSeconds)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (maxSynchronousWaitSeconds < 1){
			throw new ValidationException(getMessage("validation.schedule.qualificationrequest.invalidsynchronouswait"));
		}

		QualificationRequestTracker requestTracker =
                scheduleService.getOpenShiftEligibility(schedulePk, employeeIds, shiftIds,
                        maxComputationTime, maxUnimprovedSecondsSpent, maxSynchronousWaitSeconds, null, null, null);

		QualificationRequestTrackerDto requestTrackerDto = toDto( requestTracker, QualificationRequestTrackerDto.class);
		Collection<ShiftQualificationDto> qualificationShiftDtoCollection =
                toCollectionDto(requestTracker.getQualificationShifts(), ShiftQualificationDto.class);
		requestTrackerDto.setQualificationShifts(qualificationShiftDtoCollection);
		return requestTrackerDto;
	}


	/**
	 * variation of the getOpenShiftEligibility() API. basically same API but returning a much smaller resultset, without any informaton on non eligible shifts
	 * @param schedulePk
	 * @param employeeIds
	 * @param shiftIds
	 * @param maxComputationTime
	 * @param maxUnimprovedSecondsSpent
	 * @param maxSynchronousWaitSeconds
	 * @param overrideOptions
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public OpenShiftEligibilitySimpleResultDto getOpenShiftEligibilitySimple(
			PrimaryKey schedulePk,
            List<String> employeeIds,
			List<String> shiftIds,
            int maxComputationTime,
			int maxUnimprovedSecondsSpent,
            int maxSynchronousWaitSeconds,
			Map<ConstraintOverrideType, Boolean> overrideOptions
    ) throws IllegalAccessException {
		if (maxSynchronousWaitSeconds < 1) {
			throw new ValidationException(getMessage("validation.schedule.qualificationrequest.invalidsynchronouswait"));
		}

		QualificationRequestTracker requestTracker;
		Map<String, Map<ConstraintOverrideType, Boolean>> specificEmployeeOverrideOpts;
		if (employeeIds == null || employeeIds.size() == 0) {
			requestTracker = scheduleService.getOpenShiftEligibility(schedulePk, employeeIds, shiftIds, maxComputationTime,
					maxUnimprovedSecondsSpent, maxSynchronousWaitSeconds, null, overrideOptions, null);
		} else {
			specificEmployeeOverrideOpts = new HashMap<>();
			for (String employeeId : employeeIds) {
				if (overrideOptions != null) {
					specificEmployeeOverrideOpts.put(employeeId, overrideOptions);
				}
			}
			requestTracker = scheduleService.getOpenShiftEligibility(schedulePk, employeeIds, shiftIds, maxComputationTime,
					maxUnimprovedSecondsSpent, maxSynchronousWaitSeconds, null, null, specificEmployeeOverrideOpts);
		}

		// build dto with core attributes
		OpenShiftEligibilitySimpleResultDto eligibilityResultDto = createOpenShiftEligibilityResultDto(requestTracker);
		// now add eligibility information
		Map<String,OpenShiftDto> openShifts = new HashMap<>(); // map of accepted openShifts keyed by shiftId
		Collection<ShiftQualificationDto> rawQualificationShifts = requestTracker.getQualificationShifts();

		Collection<ExtendedShiftQualificationDto> qualificationShifts = enrichQualificationDtos(schedulePk,
                rawQualificationShifts);

		scheduleService.populateOpenShiftsAndEligibleEmployees(schedulePk, eligibilityResultDto, openShifts, qualificationShifts);

		// merge result with Posted OpenShifts to return a summarized result set.
        Schedule schedule = scheduleService.getSchedule(schedulePk);
        if (schedule.getStatus() == Posted) {
    		// get Posted OpenShifts
        	SimpleQuery sq = new SimpleQuery(schedule.getTenantId());
            sq.setSelect(null).setFilter("scheduleId = '" + schedule.getId() + "'").setOffset(0).setLimit(10000)
            	.setOrderByField("shiftId").setOrderAscending(true).setTotalCount(false);
            Collection<PostedOpenShift> postedOsList = postedOpenShiftService.findPostedOpenShifts(sq).getResult();

    		// update result with Posted OpenShifts:

            // build map of new open shifts for easy lookup, and map of employees per shift
            Map<String,OpenShiftDto> osMap = new HashMap<>();		// map of new openshifts keyed by shift id
            Map<String,Set<String>> osEmployeeMap = new HashMap<>();		// map of employee list keyed by shift id
            Iterator<OpenShiftDto> openShiftIterator = eligibilityResultDto.getOpenShifts().iterator();
            while (openShiftIterator.hasNext()) {
                OpenShiftDto os = openShiftIterator.next();
                Long startDate = os.getStartDateTime();
                Long endDate = os.getEndDateTime();
                Iterator<EligibleEmployeeDto> employeeIterator = os.getEmployees().iterator();
                while (employeeIterator.hasNext()) {
                    EligibleEmployeeDto eligibleEmployeeDto = employeeIterator.next();
                    PrimaryKey employeePk = new PrimaryKey(schedulePk.getTenantId(), eligibleEmployeeDto.getId());
                    if(hasConcurrentShift(startDate, endDate, identifyTz(employeePk), employeePk)) {
                        employeeIterator.remove();
                    }
                }
                osMap.put(os.getId(), os);
                os.setEmpCount(os.getEmployees().size());
                Set<String> empList = new HashSet<>();
                for (EligibleEmployeeDto emp : os.getEmployees()) {
                    empList.add(emp.getId());
                }
                osEmployeeMap.put(os.getId(), empList);
            }

            // check if posted openshitfs are part of new open shift list, and update accordingly
            for (PostedOpenShift pos: postedOsList) {
            	if (osMap.containsKey(pos.getShiftId())) {
            		String shiftId = pos.getShiftId();
            		OpenShiftDto os = osMap.get(shiftId);
            		if (pos.isRequested()) {
            			os.addRequested();
            		}
            		if (os.getPosted() < pos.getCreated().getMillis()) {
            			os.setPosted(pos.getCreated().getMillis());
            		}
            		Set<String> empList = osEmployeeMap.get(shiftId);
            		if (!empList.contains(pos.getEmployeeId())) {
            			os.addEmployee(); // pb, this creates a discrepancy with actual list of new OS employees, which doesn't have the previous emp ...
            		}
            	}
            }
        }

		eligibilityResultDto.setShiftCount(eligibilityResultDto.getOpenShifts().size());
		return eligibilityResultDto;
	}

    private DateTimeZone identifyTz(PrimaryKey employeePk) {
        return accountUtilService.getActualTimeZone(employeeService.getEmployee(employeePk));
    }

    /**
     * Method checks if employee already has other concurrent sifts assigned on that time interval
     *
     * @param startDate  - {@link Long} start of time interval
     * @param endDate-   {@link Long} end of time interval
     * @param dtz        - employee Date Time Zone
     * @param employeePk - {@link PrimaryKey} of employee
     * @return - {@link Boolean} result
     */
    private Boolean hasConcurrentShift(
            Long startDate,
            Long endDate,
            DateTimeZone dtz,
            PrimaryKey employeePk
    ) {
        return shiftService.getShifts(employeePk.getId(), startDate, endDate, dtz.getID(), Posted.ordinal(),
                "id,startDateTime,endDateTime", 0, 25, "startDateTime", "ASC", false).getTotal() > 0;
    }

    private Collection<ExtendedShiftQualificationDto> enrichQualificationDtos(
    		PrimaryKey schedulePk,
			Collection<ShiftQualificationDto> rawQualificationShifts) {

    	Collection<ExtendedShiftQualificationDto> qualificationShifts = new ArrayList<>();
/*    	OLD VERSION
        Map<String, ShiftQualificationDto> rawShiftMap = new HashMap<>();		// map of raw ShiftQualification keyed by shift id
        for (ShiftQualificationDto qs : rawQualificationShifts) {
        	rawShiftMap.put(qs.getShiftId(), qs);
        }
*/
        Set<String> shiftIdSet = new HashSet<>();		// Set of Shift (ids) to load
        for (ShiftQualificationDto qs : rawQualificationShifts) {
        	shiftIdSet.add(qs.getShiftId());
        }

    	// get Shifts.
    	SimpleQuery sq = new  SimpleQuery(schedulePk.getTenantId());
    	String filter = "scheduleId = '" + schedulePk.getId() + "'";
    	sq.setFilter(filter);
    	int shiftCnt = shiftIdSet.size();
    	if (shiftCnt > 0) {
			// if not too many Shifts, do a select in(shiftid1, shiftid2, ...),  otherwise get all shifts
	    	if (shiftCnt < 100) {
	    		String cond = "primaryKey.id IN (";
	    		boolean first = true;
	            for (String shiftId : shiftIdSet) {
	            	if (first) {
	            		first = false;
	            	}
	            	else {
	            		cond += ",";
	            	}
	            	cond += ("'" + shiftId + "'");
	            }
	            cond += ") ";
	            sq.addFilter(cond);
	    	}
	        sq.setOffset(0).setLimit(10000).setOrderByField("startDateTime").setOrderAscending(true).setTotalCount(false);
	        Collection<Shift> shifts = shiftService.findShifts(sq).getResult();

	        // build enriched QualificationShifts        NEW VERSION
	        Map<String, Shift> shiftMap = new HashMap<>();		// map of loaded Shifts keyed by shift id
	        for (Shift shift : shifts) {
	        	shiftMap.put(shift.getId(), shift);
	        }
	        for (ShiftQualificationDto rawQShiftDto : rawQualificationShifts) {
	        	Shift shift = shiftMap.get(rawQShiftDto.getShiftId());
	        	if (shift != null) {
	        		ExtendedShiftQualificationDto extendedQShiftDto = new ExtendedShiftQualificationDto(rawQShiftDto);
	        		extendedQShiftDto.setSkillId(shift.getSkillId());
	        		extendedQShiftDto.setSkillName(shift.getSkillName());
	        		extendedQShiftDto.setSkillAbbrev(shift.getSkillAbbrev());
	        		extendedQShiftDto.setTeamId(shift.getTeamId());
	        		extendedQShiftDto.setTeamName(shift.getTeamName());
	        		extendedQShiftDto.setStartDateTime(shift.getStartDateTime());
	        		extendedQShiftDto.setEndDateTime(shift.getEndDateTime());
	        		extendedQShiftDto.setShiftLength(shift.getShiftLength());
	        		extendedQShiftDto.setExcess(shift.isExcess());
	        		qualificationShifts.add(extendedQShiftDto);
	        	}
	        }


	        // build enriched QualificationShifts     OLD VERSION
/*	        
	        for (Shift shift : shifts) {
	        	ShiftQualificationDto rawQShiftDto = rawShiftMap.get(shift.getId());
	        	if (rawQShiftDto != null) {
	        		ExtendedShiftQualificationDto extendedQShiftDto = new ExtendedShiftQualificationDto(rawQShiftDto);
	        		extendedQShiftDto.setSkillId(shift.getSkillId());
	        		extendedQShiftDto.setSkillName(shift.getSkillName());
	        		extendedQShiftDto.setSkillAbbrev(shift.getSkillAbbrev());
	        		extendedQShiftDto.setTeamId(shift.getTeamId());
	        		extendedQShiftDto.setTeamName(shift.getTeamName());
	        		extendedQShiftDto.setStartDateTime(shift.getStartDateTime());
	        		extendedQShiftDto.setEndDateTime(shift.getEndDateTime());
	        		extendedQShiftDto.setShiftLength(shift.getShiftLength());
	        		extendedQShiftDto.setExcess(shift.isExcess());
	        		qualificationShifts.add(extendedQShiftDto);
	        	}
	        }
*/
    	}
		return qualificationShifts;
	}

	private OpenShiftEligibilitySimpleResultDto createOpenShiftEligibilityResultDto(
            QualificationRequestTracker requestTracker) {
    	OpenShiftEligibilitySimpleResultDto dto = new  OpenShiftEligibilitySimpleResultDto();

    	dto.setName(requestTracker.getName());
    	dto.setScheduleId(requestTracker.getScheduleId());
    	dto.setStartDate(requestTracker.getStartDate());
    	dto.setEndDate(requestTracker.getEndDate());
    	dto.setState(requestTracker.getState());
    	dto.setCompletion(requestTracker.getCompletion());
    	dto.setCompletionInfo(requestTracker.getCompletionInfo());
    	dto.setEngineId(requestTracker.getEngineId());
    	dto.setEngineLabel(requestTracker.getEngineLabel());

// other fields to set..
/*    	
        private	long shiftGenerationDuration = -1;		//
        private	long employeeGenerationDuration = -1;	//
        private	long requestGenerationDuration = -1;	//
        private	long responseProcessingDuration = -1;	//
        private	long returnedOpenShifts = -1;			//	nb of open shifts returned by engine (can be != actual open shifts due to manual edits)
        private	long returnedAssignedShifts = -1;		//  nb of generated shifts returned by engine (can be != actual assigned shifts due to manual edits)

        private int scheduledTeamCount = -1;
        private int scheduledEmployeeCount = -1;

        private DateTime executionStartDate = new DateTime(0);      // date/time the last execution has been fired (date/time UTC)

        private DateTime requestSentDate = new DateTime(0);         // date/time the last request has been sent to Engine (date/time UTC)

        private DateTime executionAckDate = new DateTime(0);        // date/time the last execution has been acknowledged by Engine (date/time UTC)

        private DateTime responseReceivedDate = new DateTime(0);    // date/time the last response has been received (successfully or not) by engine (date/time UTC)

        private DateTime executionEndDate = new DateTime(0);        // date/time the last execution has been completed (successfully or not) by engine (date/time UTC)
*/

		return dto;
	}

	/** Get result from synchronously processed qualification execution
	 *  NOTE: Facade method implemented for pattern consistency, but just passing args on to
	 *        service layer.  For this functionality, validation and JPA entity retrieval are
	 *        performed in service layer in order to control transaction lifecycles.
	 * @param schedulePk
	 * @param qualificationExecuteDto
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@Validation
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public QualificationRequestTrackerDto getQualification(
	        @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
	        PrimaryKey schedulePk,
			QualificationExecuteDto qualificationExecuteDto)
	        		throws InstantiationException, IllegalAccessException,
	        		NoSuchMethodException, InvocationTargetException {
		if (qualificationExecuteDto.getMaxSynchronousWaitSeconds() < 1) {
			throw new ValidationException(getMessage("validation.schedule.qualificationrequest.invalidsynchronouswait"));
		}

		QualificationRequestTracker requestTracker = scheduleService.getQualification(schedulePk, qualificationExecuteDto);

		QualificationRequestTrackerDto requestTrackerDto = toDto( requestTracker, QualificationRequestTrackerDto.class);
		Collection<ShiftQualificationDto> qualificationShiftDtoCollection =
                toCollectionDto(requestTracker.getQualificationShifts(), ShiftQualificationDto.class);
		requestTrackerDto.setQualificationShifts(qualificationShiftDtoCollection);
		return requestTrackerDto;
	}

	/** Fire off execution of open shift eligibility for asynchronous processing.
	 * @param schedulePk
	 * @param employeeIds
	 * @param shiftIds
	 * @param maxComputationTime
	 * @param maxUnimprovedSecondsSpent
	 * @return requestId
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@Validation
	public String executeOpenShiftEligibility(
	        @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
	        PrimaryKey schedulePk,
	        List<String> employeeIds, List<String> shiftIds,
	        int maxComputationTime, int maxUnimprovedSecondsSpent)
	        		throws InstantiationException, IllegalAccessException,
	        		NoSuchMethodException, InvocationTargetException {
		Schedule schedule = scheduleService.getSchedule(schedulePk);

		List<Employee> employees = new ArrayList<>();
		for (String employeeId : employeeIds){
			PrimaryKey employeePk = new PrimaryKey(schedulePk.getTenantId(), employeeId);
			Employee employee = employeeService.getEmployee(employeePk);
			employees.add(employee);
		}

		List<Shift> shifts = new ArrayList<>();
		for (String shiftId : shiftIds){
			PrimaryKey shiftPk = new PrimaryKey(schedulePk.getTenantId(), shiftId);
			Shift shift = shiftService.getShift(shiftPk);
			if (shift.getAssigned() == null && shift.getAssignmentType() == null
	    			&& shift.getEmployeeId() == null && shift.getEmployeeName() == null) {
				shifts.add(shift);
			} else {
				throw new ValidationException(getMessage("validation.schedule.shift.notopen", shift.getId()));
			}
		}

		return scheduleService.executeOpenShiftEligibility(schedule, employees, shifts, maxComputationTime,
                maxUnimprovedSecondsSpent, null);
	}

	/** Fire off execution of qualification for asynchronous processing.
	 * @param schedulePk
	 * @param qualificationExecuteDto
	 * @return requestId
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@Validation
	public String executeQualification(
	        @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
	        PrimaryKey schedulePk,
			QualificationExecuteDto qualificationExecuteDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
		return scheduleService.executeQualification(schedulePk, qualificationExecuteDto);
	}

    /**
     * postOpenShifts()  updateds the OpenShift table with specified open shifts
     * Once added into that table OpenShifts can then be claimed by employees via workflows.
     * Successfully getting an open shift will remove the corresponding entry from the table (actually, getting a Shift, will remove all rows related to that Shift)
     * However, over time, we may have many leftovers (Shifts never removed from that table), and thus a cleanup mechamism is required, that will remove automatically all entries older than 30 days prior to the current date. (other rules are possible, for instance based on active posted schedules, so as to optimize the volume  leftover entries)
	 *
     * @param primaryKey
     * @param openShiftsDto
     * @return Nb of actually posted Shifts
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    @Validation
    public ResultSetDto<SummarizedPostedOpenShiftDto> postOpenShifts(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey primaryKey,
            PostOpenShiftDto openShiftsDto) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        if (openShiftsDto == null || openShiftsDto.getOpenShifts() == null) {
            throw new ValidationException(getMessage("validation.schedule.invalidopenshiftsparam"));
        }

        Schedule schedule = scheduleService.getSchedule(primaryKey);

        Collection<PostedOpenShift> postedOpenShifts = postedOpenShiftService.postOpenShifts(schedule,
                openShiftsDto.getPostMode(), openShiftsDto.getOpenShifts(), openShiftsDto.getOverrideOptions(),
                openShiftsDto.getDeadline(), openShiftsDto.getComments(), openShiftsDto.getTerms());

        ResultSetDto<SummarizedPostedOpenShiftDto> result = new ResultSetDto<>();
        result.setTotal(postedOpenShifts.size());
        result.setResult(summarizePostedOpenShifts(postedOpenShifts));

        return result;
    }

    @Validation
    public int postAllOpenShifts(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey primaryKey,
            PostAllOpenShiftDto postAllOpenShiftDto) throws IllegalAccessException {
        if (postAllOpenShiftDto == null || postAllOpenShiftDto.getOpenShifts() == null) {
            throw new ValidationException(getMessage("validation.schedule.invalidopenshiftsparam"));
        }

        Schedule schedule = scheduleService.getSchedule(primaryKey);

        QualificationExecuteDto executeDto = postAllOpenShiftDto.getExecuteDto();
        if (executeDto == null) {
            executeDto = new QualificationExecuteDto();
        }

        List<String> shiftIds = new ArrayList<>();
        shiftIds.addAll(postAllOpenShiftDto.getOpenShifts());

        OpenShiftEligibilitySimpleResultDto eligibilitySimpleResultDto = getOpenShiftEligibilitySimple(primaryKey, null,
                shiftIds, executeDto.getMaxComputationTime(), executeDto.getMaximumUnimprovedSecondsSpent(),
                executeDto.getMaxSynchronousWaitSeconds(), postAllOpenShiftDto.getOverrideOptions());

        Collection<OpenShiftDto> openShiftDtos = eligibilitySimpleResultDto.getOpenShifts();

        Map<String, Collection<String>> openShiftEmpIdsMap = new HashMap<>();
        for (OpenShiftDto openShiftDto : openShiftDtos) {
            Collection<String> employeeIds = new ArrayList<>();

            Set<EligibleEmployeeDto> employeeDtos = openShiftDto.getEmployees();
            for (EligibleEmployeeDto employeeDto : employeeDtos) {
                employeeIds.add(employeeDto.getId());
            }
            openShiftEmpIdsMap.put(openShiftDto.getId(), employeeIds);
        }

        Collection<PostedOpenShift> postedOpenShifts = postedOpenShiftService.postOpenShifts(schedule,
                PostMode.Cumulative, openShiftEmpIdsMap, postAllOpenShiftDto.getOverrideOptions(),
                postAllOpenShiftDto.getDeadline(), postAllOpenShiftDto.getComments(), postAllOpenShiftDto.getTerms());
        return postedOpenShifts.size();
    }

    public List<Map<String, Object>> summaryReport(String scheduleId) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<Object[]> summaryList = scheduleService.summaryReport(scheduleId);
        for (Object[] objects : summaryList) {
            Map<String, Object> summaryMap = new HashMap<>();
            summaryMap.put("skillId", objects[0]);
            summaryMap.put("skillName", objects[1]);
            summaryMap.put("baseline", objects[2]);
            summaryMap.put("excess", objects[3]);
            summaryMap.put("unfilled", objects[4]);
            result.add(summaryMap);
        }
        return result;
    }

    public List<Map<String, Object>> summaryByEmployeeReport(String scheduleId, String teamsIds, String employeeTypes) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Object[]> summaryList = scheduleService.summaryByEmployeeReport(scheduleId, teamsIds, employeeTypes);
        for (Object[] objects : summaryList) {
            Map<String, Object> summaryMap = new HashMap<>();
            summaryMap.put("employeeId", objects[0]);
            summaryMap.put("employeeName", objects[1]);
            summaryMap.put("skillId", objects[2]);
            summaryMap.put("skillName", objects[3]);
            summaryMap.put("teamId", objects[4]);
            summaryMap.put("teamName", objects[5]);
            summaryMap.put("shiftLength", objects[6]);
            summaryMap.put("startDateTime", objects[7]);
            summaryMap.put("endDateTime", objects[8]);
            summaryMap.put("leave", false);
            result.add(summaryMap);
        }
        List<Object[]> unavailTFs = scheduleService.unavailibleTimeFrames(scheduleId, teamsIds);
        for (Object[] objects : unavailTFs) {
            Map<String, Object> summaryMap = new HashMap<>();
            summaryMap.put("employeeId", objects[0]);
            summaryMap.put("employeeName", objects[1]);
            summaryMap.put("leaveHours", objects[2]);
            summaryMap.put("startDateTime", objects[3]);
            summaryMap.put("leaveType", objects[4]);
            summaryMap.put("leave", true);
            result.add(summaryMap);
        }
        return result;
    }

    public List<Map<String, Object>> hourlyStaffingReport(String scheduleId) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Object[]> summaryList = scheduleService.hourlyStaffingReport(scheduleId);
        for (Object[] objects : summaryList) {
            Map<String, Object> summaryMap = new HashMap<>();
            summaryMap.put("skillId", objects[0]);
            summaryMap.put("skillName", objects[1]);
            summaryMap.put("teamId", objects[2]);
            summaryMap.put("teamName", objects[3]);
            summaryMap.put("shiftLength", objects[4]);
            summaryMap.put("startDateTime", objects[5]);
            summaryMap.put("endDateTime", objects[6]);
            result.add(summaryMap);
        }
        return result;
    }

    public List<Map<String, Object>> headerReport(String scheduleId) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<Object[]> headerList = scheduleService.headerReport(scheduleId);
        for (Object[] objects : headerList) {
            Map<String, Object> headerMap = new HashMap<>();
            headerMap.put("scheduleName", objects[0]);
            headerMap.put("startDate", objects[1]);
            String scheduleStatus =
                    objects[2] != null ? ScheduleStatus.values()[((Number) objects[2]).intValue()].getValue() : null;
            headerMap.put("scheduleStatus", scheduleStatus);
            headerMap.put("siteName", objects[3]);

            result.add(headerMap);
        }

        return result;
    }

    public Map<String, Object> overtimeReport(PrimaryKey schedulePrimaryKey) {
        Map<String, Object> result = new HashMap<>();

        String scheduleId = schedulePrimaryKey.getId();

        int overtimes = 0;
        int costs = 0;

        List<Object[]> scheduleShiftsInfo = scheduleService.scheduleShiftsReport(scheduleId);
        Map<String, List<Map<String, Object>>> scheduleEmployeeMap = new HashMap<>();
        for (Object[] objects : scheduleShiftsInfo) {
            String employeeId = (String) objects[3];
            List<Map<String, Object>> employeeShiftList = scheduleEmployeeMap.get(employeeId);
            if (employeeShiftList == null) {
                employeeShiftList = new ArrayList<>();
                scheduleEmployeeMap.put(employeeId, employeeShiftList);
            }

            Map<String, Object> scheduleShiftsMap = new HashMap<>();
            scheduleShiftsMap.put("startDate", objects[0]);
            scheduleShiftsMap.put("startDateTime", objects[1]);
            scheduleShiftsMap.put("shiftLength", objects[2]);

            employeeShiftList.add(scheduleShiftsMap);
        }

        List<Object[]> scheduleEmployeeInfo = scheduleService.scheduleEmployeeReport(scheduleId);
        Map<String, Map<String, Object>> employeeInfoMap = new HashMap<>();
        for (Object[] objects : scheduleEmployeeInfo) {
            String employeeId = (String) objects[0];
            Map<String, Object> contractInfoMap = employeeInfoMap.get(employeeId);
            if (contractInfoMap == null) {
                contractInfoMap = new HashMap<>();
                employeeInfoMap.put(employeeId, contractInfoMap);
            }
            contractInfoMap.put((String) objects[2], objects[3]);
            contractInfoMap.put("hourlyRate", objects[1]);
        }

        Schedule schedule = scheduleService.getSchedule(schedulePrimaryKey);
        Set<Team> teams = schedule.getTeams();
        Team team = teams.iterator().next();

        if (team == null) {
            throw new ValidationException("validation.schedule.invalid.data");
        }

        Site site = teamService.getSite(team);

        List<Object[]> siteOvertimesInfo = siteService.siteOvertimesInfo(site.getId());
        int siteDayOvertimes = 0;
        int siteWeekOvertimes = 0;
        int siteTwoWeekOvertimes = 0;
        for (Object[] objects : siteOvertimesInfo) {
            if ("DAILY_OVERTIME".equals(objects[0])) {
                siteDayOvertimes = ((Number) objects[1]).intValue();
            } else if ("WEEKLY_OVERTIME".equals(objects[0])) {
                siteWeekOvertimes = ((Number) objects[1]).intValue();
            } else if ("TWO_WEEK_OVERTIME".equals(objects[0])) {
                siteTwoWeekOvertimes = ((Number) objects[1]).intValue();
            }
        }

        for (Map.Entry<String, List<Map<String, Object>>> mapEntry : scheduleEmployeeMap.entrySet()) {
            String employeeId = mapEntry.getKey();
            if (StringUtils.isNotEmpty(employeeId)) {
                Map<String, Object> contractInfoMap = employeeInfoMap.get(employeeId);
                List<Map<String, Object>> infoList = mapEntry.getValue();

                Float employeeDayOvertimes = null;
                Float employeeWeekOvertimes = null;
                Float employeeTwoWeekOvertimes = null;
                if (contractInfoMap != null) {
                    try {
                        if (contractInfoMap.containsKey("DAILY_OVERTIME")) {
                            employeeDayOvertimes = ((Number) contractInfoMap.get("DAILY_OVERTIME")).floatValue();
                        }
                        if (contractInfoMap.containsKey("WEEKLY_OVERTIME")) {
                            employeeWeekOvertimes = ((Number) contractInfoMap.get("WEEKLY_OVERTIME")).floatValue();
                        }
                        if (contractInfoMap.containsKey("TWO_WEEK_OVERTIME")) {
                            employeeTwoWeekOvertimes = ((Number) contractInfoMap.get("TWO_WEEK_OVERTIME")).floatValue();
                        }
                    } catch (Exception e) {
                        logger.error(getMessage("schedule.overwrite.error", schedule.getName(), schedule.getId(),
                                employeeId), e);
                        continue;
                    }
                }

                float dayOvertimes = calculateDayOvertimes(
                        employeeDayOvertimes == null ? siteDayOvertimes : employeeDayOvertimes, infoList);
                float weekOvertimes = calculateWeekOvertimes(
                        employeeWeekOvertimes == null ? siteWeekOvertimes : employeeWeekOvertimes, infoList);
                float twoWeekOvertimes = calculateTwoWeekOvertimes(
                        employeeTwoWeekOvertimes == null ? siteTwoWeekOvertimes : employeeTwoWeekOvertimes, infoList);

                int employeeOvertimes = (int) Math.max(Math.max(dayOvertimes, weekOvertimes), twoWeekOvertimes);

                overtimes += employeeOvertimes;

                int hourlyRate = contractInfoMap == null || contractInfoMap.get("hourlyRate") == null ? 0
                        : ((Number) contractInfoMap.get("hourlyRate")).intValue();
                float employeeCosts = calculateCosts(hourlyRate, infoList) + (int) (employeeOvertimes * OVERTIME_COEFF);

                costs += employeeCosts;
            }
        }

        result.put("overtimes", overtimes);
        result.put("costs", costs);

        return result;
    }

    public Map<String, Object> overtimeReportBetweenDates(Schedule schedule, Collection<Shift> shifts) {
        Map<String, Object> result = new HashMap<>();

        int overtimes = 0;
        int costs = 0;

        Map<String, List<Map<String, Object>>> scheduleEmployeeMap = new HashMap<>();
        for (Shift shift : shifts) {
            String employeeId = shift.getEmployeeId();
            List<Map<String, Object>> employeeShiftList = scheduleEmployeeMap.get(employeeId);
            if (employeeShiftList == null) {
                employeeShiftList = new ArrayList<>();
                scheduleEmployeeMap.put(employeeId, employeeShiftList);
            }

            Map<String, Object> scheduleShiftsMap = new HashMap<>();
            scheduleShiftsMap.put("startDate", schedule.getStartDate());
            scheduleShiftsMap.put("startDateTime", shift.getStartDateTime());
            scheduleShiftsMap.put("shiftLength", shift.getShiftLength());

            employeeShiftList.add(scheduleShiftsMap);
        }

        List<Object[]> scheduleEmployeeInfo = scheduleService.scheduleEmployeeReport(schedule.getId());
        Map<String, Map<String, Object>> employeeInfoMap = new HashMap<>();
        for (Object[] objects : scheduleEmployeeInfo) {
            String employeeId = (String) objects[0];
            Map<String, Object> contractInfoMap = employeeInfoMap.get(employeeId);
            if (contractInfoMap == null) {
                contractInfoMap = new HashMap<>();
                employeeInfoMap.put(employeeId, contractInfoMap);
            }
            contractInfoMap.put((String) objects[2], objects[3]);
            contractInfoMap.put("hourlyRate", objects[1]);
        }

        Set<Team> teams = schedule.getTeams();
        Team team = teams.iterator().next();

        if (team == null) {
            throw new ValidationException("validation.schedule.invalid.data");
        }

        Site site = teamService.getSite(team);

        List<Object[]> siteOvertimesInfo = siteService.siteOvertimesInfo(site.getId());
        int siteDayOvertimes = 0;
        for (Object[] objects : siteOvertimesInfo) {
            if ("DAILY_OVERTIME".equals(objects[0])) {
                siteDayOvertimes = ((Number) objects[1]).intValue();
            }
        }

        for (Map.Entry<String, List<Map<String, Object>>> mapEntry : scheduleEmployeeMap.entrySet()) {
            String employeeId = mapEntry.getKey();
            if (StringUtils.isNotEmpty(employeeId)) {
                Map<String, Object> contractInfoMap = employeeInfoMap.get(employeeId);
                List<Map<String, Object>> infoList = mapEntry.getValue();

                Float employeeDayOvertimes = null;
                try {
                	if (contractInfoMap.containsKey("DAILY_OVERTIME")) {
    	                employeeDayOvertimes = ((Number) contractInfoMap.get("DAILY_OVERTIME")).floatValue();
                	}
                } catch (Exception e) {
                    logger.error(getMessage("schedule.overwrite.error", schedule.getName(), schedule.getId(),
                            employeeId), e);
                    continue;
                }

                float dayOvertimes = calculateDayOvertimes(
                        employeeDayOvertimes == null ? siteDayOvertimes : employeeDayOvertimes, infoList);

                overtimes += dayOvertimes;

                int hourlyRate = ((Number) contractInfoMap.get("hourlyRate")).intValue();
                float employeeCosts = calculateCosts(hourlyRate, infoList) + (int) (dayOvertimes * OVERTIME_COEFF);

                costs += employeeCosts;
            }
        }

        result.put("overtimes", overtimes);
        result.put("costs", costs);

        return result;
    }

	/** Get result from synchronously processed shift swap eligibility execution
	 *  NOTE: Facade method implemented for pattern consistency, but just passing args on to
	 *        service layer.  For this functionality, validation and JPA entity retrieval are
	 *        performed in service layer in order to control transaction lifecycles.
	 * @param schedulePk
	 * @param swapSeekingShiftIds
	 * @param swapCandidateShiftIds
	 * @param maxComputationTime
	 * @param maxUnimprovedSecondsSpent
	 * @param maxSynchronousWaitSeconds
	 * @return requestTracker
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@Validation
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public ShiftSwapEligibilityRequestTrackerDto getShiftSwapEligibility(
	        @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
	        PrimaryKey schedulePk,
	        List<String> swapSeekingShiftIds, List<String> swapCandidateShiftIds,
	        int maxComputationTime, int maxUnimprovedSecondsSpent, int maxSynchronousWaitSeconds)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (maxSynchronousWaitSeconds < 1) {
			throw new ValidationException(getMessage("validation.schedule.qualificationrequest.invalidsynchronouswait"));
		}

		ShiftSwapEligibilityRequestTracker requestTracker =
	            scheduleService.getShiftSwapEligibility(schedulePk, swapSeekingShiftIds, swapCandidateShiftIds,
                        maxComputationTime, maxUnimprovedSecondsSpent, maxSynchronousWaitSeconds, null);

		ShiftSwapEligibilityRequestTrackerDto requestTrackerDto = toDto(requestTracker,
                ShiftSwapEligibilityRequestTrackerDto.class);
		Map<String, Collection<ShiftQualificationDto>> qualShiftsMap = requestTracker.getQualificationShifts();
		Set<String> keys = qualShiftsMap.keySet();
		for (String key : keys){
			Collection<ShiftQualificationDto> qualificationShiftDtoCollection =
					toCollectionDto(qualShiftsMap.get(key), ShiftQualificationDto.class);
			requestTrackerDto.getQualificationShiftsMap().put(key, qualificationShiftDtoCollection);
		}
		return requestTrackerDto;
	}

	/** Fire off execution of shift swap eligibility for asynchronous processing.
	 * @param schedulePk
	 * @param swapSeekingShiftIds
	 * @param swapCandidateShiftIds
	 * @param maxComputationTime
	 * @param maxUnimprovedSecondsSpent
	 * @return requestId
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@Validation
	public String executeShiftSwapEligibility(
	        @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
	        PrimaryKey schedulePk,
	        List<String> swapSeekingShiftIds, List<String> swapCandidateShiftIds,
	        int maxComputationTime, int maxUnimprovedSecondsSpent) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		Schedule schedule = scheduleService.getSchedule(schedulePk);

		List<Shift> swapSeekingShifts = new ArrayList<Shift>();
		for (String shiftId : swapSeekingShiftIds){
			PrimaryKey shiftPk = new PrimaryKey(schedulePk.getTenantId(), shiftId);
			Shift shift = shiftService.getShift(shiftPk);
			if (shift.getAssigned() == null || shift.getAssignmentType() == null
	    			|| shift.getEmployeeId() == null || shift.getEmployeeName() == null) {
				throw new ValidationException(getMessage("validation.schedule.shift.notopen", shift.getId()));
			} else {
				swapSeekingShifts.add(shift);
			}
		}

		List<Shift> swapCandidateShifts = new ArrayList<>();
		for (String shiftId : swapCandidateShiftIds){
			PrimaryKey shiftPk = new PrimaryKey(schedulePk.getTenantId(), shiftId);
			Shift shift = shiftService.getShift(shiftPk);
			if (shift.getAssigned() == null || shift.getAssignmentType() == null
	    			|| shift.getEmployeeId() == null || shift.getEmployeeName() == null) {
				throw new ValidationException(getMessage("validation.schedule.shift.notopen", shift.getId()));
			} else {
				swapCandidateShifts.add(shift);
			}
		}

		return scheduleService.executeShiftSwapEligibility(schedule, swapSeekingShifts, swapCandidateShifts,
                maxUnimprovedSecondsSpent, maxUnimprovedSecondsSpent, null);
	}

    @Validation
    public ScheduleViewDto scheduleView(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Schedule schedule = scheduleService.getSchedule(schedulePrimaryKey);

        DtoMapper<Schedule, ScheduleViewDto> dtoMapper = new DtoMapper<>();
        dtoMapper.registerExceptDtoFieldForMapping("siteInfo");
        dtoMapper.registerExceptDtoFieldForMapping("teamsInfo");
        dtoMapper.registerExceptDtoFieldForMapping("employeesInfo");
        dtoMapper.registerExceptDtoFieldForMapping("skillsInfo");
        dtoMapper.registerExceptDtoFieldForMapping("totalMinutes");
        dtoMapper.registerExceptDtoFieldForMapping("unfilledShiftCount");
        dtoMapper.registerExceptDtoFieldForMapping("overtimeHours");
        dtoMapper.registerExceptDtoFieldForMapping("totalCost");
        dtoMapper.registerExceptDtoFieldForMapping("weeks");

        ScheduleViewDto result = dtoMapper.map(schedule, ScheduleViewDto.class);

        int totalMinutes = 0;
        int unfilledShiftCount = 0;

        Set<Skill> skillSet = new HashSet<>();

        List<Object[]> teamsInfoList = new ArrayList<>();

        Site site = null;
        Set<Team> teams = schedule.getTeams();
        for (Team team : teams) {
            // assume that all team are from one site
            if (result.getSiteInfo() == null) {
                site = teamService.getSite(team);
                result.setSiteInfo(new String[] {site.getId(), site.getName(), site.getTimeZone().toString(),
                        site.getFirstDayOfWeek().name()});
            }

            teamsInfoList.add(new Object[] {team.getId(), team.getName(), true});

            Collection<Skill> skills = team.getSkills();
            if (skills != null) {
                for (Skill skill : skills) {
                    skillSet.add(skill);
                }
            }
        }

        // skills info
        List<Object[]> skillList = new ArrayList<>();

        for (Skill skill : skillSet) {
            skillList.add(new Object[] {skill.getId(), skill.getName(), skill.getAbbreviation(), true});
        }

        // Create weeks
        List<Map<String, Object>> weeks = new ArrayList<>();
        long timeZoneOffset = site.getTimeZone().getOffset(schedule.getStartDate());
        long scheduleStartDate = schedule.getStartDate() + timeZoneOffset;
        long scheduleEndDate = schedule.getEndDate() + timeZoneOffset;

        long start = getDateOfFirstDayInWeek(site.getFirstDayOfWeek(), scheduleStartDate);
        long end = scheduleEndDate;
        while (start < end) {
            Map<String, Object> weekMap = new HashMap<>();
            weeks.add(weekMap);

            weekMap.put("start", start - timeZoneOffset); // transform from local time in UTC

            start += Constants.WEEK_MILLISECONDS;

            weekMap.put("end", start - timeZoneOffset - 1);  // transform from local time in UTC
            weekMap.put("shifts", new ArrayList<Object[]>());
            weekMap.put("postedOpenShifts", new ArrayList<Object[]>());
            weekMap.put("employees", new HashMap<String, Map<String, Object>>());
        }

        Collection<Shift> shifts = shiftService.getScheduleShifts(schedule, true);
        for (Shift shift : shifts) {
            Object[] shiftInfo = new Object[] {shift.getId(), shift.isExcess(), shift.getTeamId(),
                    shift.getEmployeeId(), shift.getSkillId(), shift.getSkillAbbrev(), shift.getStartDateTime(),
                    shift.getEndDateTime(), shift.getPaidTime(), shift.getComment()};

            for (Map<String, Object> weekMap : weeks) {
                long weekStart = (long) weekMap.get("start");
                long weekEnd = (long) weekMap.get("end");
                if (shift.getStartDateTime() >= weekStart && shift.getStartDateTime() <= weekEnd) {
                    ((List<Object[]>) weekMap.get("shifts")).add(shiftInfo);
                    if (StringUtils.isNotEmpty(shift.getEmployeeId())) {
                        Map<String, Map<String, Object>> employeesMap =
                                (Map<String, Map<String, Object>>) weekMap.get("employees");
                        Map<String, Object> employeeMap = employeesMap.get(shift.getEmployeeId());
                        if (employeeMap == null) {
                            employeeMap = new HashMap<>();
                            employeesMap.put(shift.getEmployeeId(), employeeMap);
                        }
                        int scheduledMinutes = employeeMap.get("scheduledMinutes") == null ? shift.getShiftLength()
                                : (Integer) employeeMap.get("scheduledMinutes") + shift.getShiftLength();
                        employeeMap.put("scheduledMinutes", scheduledMinutes);
                    }
                    break;
                }
            }

            if (StringUtils.isEmpty(shift.getEmployeeId())) {
                unfilledShiftCount = unfilledShiftCount + shift.getShiftLength();
            }
            totalMinutes += shift.getShiftLength();
        }

        Map<String, Integer> employeesWeekMinutes = scheduleService.scheduleEmployeesMinutesPerWeek(schedulePrimaryKey);
        for (Map<String, Object> weekMap : weeks) {
            Set<Map<String, Object>> employeesSet = new HashSet<>();
            Map<String, Map<String, Object>> employeesMap = (Map<String, Map<String, Object>>) weekMap.get("employees");
            for (Map.Entry<String, Integer> entry : employeesWeekMinutes.entrySet()) {
                String employeeId = entry.getKey();
                Map<String, Object> employeeMap = employeesMap.get(employeeId);
                if (employeeMap == null) {
                    employeeMap = new HashMap<>();
                    employeeMap.put("scheduledMinutes", 0);
                }
                employeeMap.put("employeeId", employeeId);
                employeeMap.put("minimumMinutes", entry.getValue());
                employeesSet.add(employeeMap);
            }
            weekMap.put("employees", employeesSet);
        }

        Collection<PostedOpenShift> postedOpenShifts =
                postedOpenShiftService.getPostedOpenShiftsOfSchedule(schedulePrimaryKey, null, null, null);
        for (PostedOpenShift postedOpenShift : postedOpenShifts) {
            Object[] postedOpenShiftInfo = new Object[] {postedOpenShift.getShiftId(), postedOpenShift.getPostId(),
                    postedOpenShift.isRequested(), 1};

            for (Map<String, Object> weekMap : weeks) {
                long weekStart = (long) weekMap.get("start");
                long weekEnd = (long) weekMap.get("end");
                if (postedOpenShift.getStartDateTime() >= weekStart && postedOpenShift.getStartDateTime() <= weekEnd) {
                    List<Object[]> rows = (List<Object[]>) weekMap.get("postedOpenShifts");
                    boolean found = false;
                    for (Object[] row : rows) {
                        if (row[0].equals(postedOpenShiftInfo[0]) && row[1].equals(postedOpenShiftInfo[1])) {
                            if (!(boolean) row[2] && (boolean) postedOpenShiftInfo[2]) {
                                row[2] = true;
                            }
                            row[3] = (int) row[3] + 1;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        rows.add(postedOpenShiftInfo);
                    }
                    break;
                }
            }
        }

        // count total shifts
        for (Map<String, Object> weekMap : weeks) {
            weekMap.put("totalShifts", ((List<Object[]>) weekMap.get("shifts")).size());
        }

        result.setWeeks(weeks);

        // employees info
        List<Object[]> employeesInfoList = new ArrayList<>();

        ResultSet<Employee> employeeResultSet = scheduleService.getEmployees(schedulePrimaryKey, "", 0,
                0, null, null);

        Collections.sort((List<Employee>) employeeResultSet.getResult(), new Comparator<Employee>() {
            @Override
            public int compare(Employee employee1, Employee employee2) {
                int result = String.CASE_INSENSITIVE_ORDER.compare(employee1.getFirstName(), employee2.getFirstName());
                if (result == 0) {
                    result = String.CASE_INSENSITIVE_ORDER.compare(employee1.getLastName(), employee2.getLastName());
                }
                return result;
            }
        });

        long currentTime = System.currentTimeMillis();

        for (Employee employee : employeeResultSet.getResult()) {
            Skill primarySkill = employee.getPrimarySkill();
            String primarySkillId = primarySkill == null ? null : primarySkill.getId();

            Team homeTeam = employee.getHomeTeam();
            String homeTeamId = homeTeam == null ? null : homeTeam.getId();

            String teamIds = null;
            for (EmployeeTeam employeeTeam : employee.getEmployeeTeams()) {
                Team team = employeeTeam.getTeam();
                String teamId = team.getId();
                if (StringUtils.isEmpty(teamIds)) {
                    teamIds = teamId;
                } else {
                    teamIds += "," + teamId;
                }
                if (employeeTeam.getIsHomeTeam() && !arrayContainsId(teamsInfoList, teamId)) {
                    teamsInfoList.add(new Object[] {team.getId(), team.getName(), false});
                }
            }

            String skillIds = null;
            for (EmployeeSkill employeeSkill : employee.getEmployeeSkills()) {
                Skill skill = employeeSkill.getSkill();
                String skillId = skill.getId();
                if (StringUtils.isEmpty(skillIds)) {
                    skillIds = skillId;
                } else {
                    skillIds += "," + skillId;
                }
                if (employeeSkill.getIsPrimarySkill() && !arrayContainsId(skillList, skillId)) {
                    skillList.add(new Object[] {skill.getId(), skill.getName(), skill.getAbbreviation(), false});
                }
            }

            long millisSinceStarted = 0;
			if (employee.getStartDate() != null && employee.getStartDate().toDate().getTime() < currentTime) {
                millisSinceStarted = currentTime - employee.getStartDate().toDate().getTime();
			}
			employeesInfoList.add(new Object[] {employee.getId(), employee.getFirstName(), employee.getLastName(),
                    employee.getHourlyRate(), teamIds, homeTeamId, primarySkillId, skillIds, millisSinceStarted});
        }

        ResultSetDto<Object[]> employeeInfo = new ResultSetDto<>();
        employeeInfo.setResult(employeesInfoList);
        employeeInfo.setTotal(employeesInfoList.size());

        result.setEmployeesInfo(employeeInfo);

        result.setTotalMinutes(totalMinutes);
        result.setUnfilledShiftCount(unfilledShiftCount);

        ResultSetDto<Object[]> skillInfo = new ResultSetDto<>();
        skillInfo.setResult(skillList);
        skillInfo.setTotal(skillList.size());

        result.setSkillsInfo(skillInfo);

        ResultSetDto<Object[]> teamInfo = new ResultSetDto<>();
        teamInfo.setResult(teamsInfoList);
        teamInfo.setTotal(teamsInfoList.size());

        result.setTeamsInfo(teamInfo);

        Map<String, Object> overtimeMap = overtimeReport(schedulePrimaryKey);
        int overtimeHours = overtimeMap.get("overtimes") == null ? 0 : (Integer) overtimeMap.get("overtimes");
        int totalCost = overtimeMap.get("costs") == null ? 0 : (Integer) overtimeMap.get("costs");

        result.setOvertimeHours(overtimeHours);
        result.setTotalCost(totalCost);

        return result;
    }

    @Validation
    public ScheduleDayViewDto scheduleDayView(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            Long date,
            boolean shiftsOnly) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        Schedule schedule = scheduleService.getSchedule(schedulePrimaryKey);

        DtoMapper<Schedule, ScheduleDayViewDto> dtoMapper = new DtoMapper<>();
        dtoMapper.registerExceptDtoFieldForMapping("siteInfo");
        dtoMapper.registerExceptDtoFieldForMapping("teamsInfo");
        dtoMapper.registerExceptDtoFieldForMapping("employeesInfo");
        dtoMapper.registerExceptDtoFieldForMapping("skillsInfo");
        dtoMapper.registerExceptDtoFieldForMapping("totalMinutes");
        dtoMapper.registerExceptDtoFieldForMapping("unfilledShiftCount");
        dtoMapper.registerExceptDtoFieldForMapping("overtimeHours");
        dtoMapper.registerExceptDtoFieldForMapping("totalCost");
        dtoMapper.registerExceptDtoFieldForMapping("weeks");
        dtoMapper.registerExceptDtoFieldForMapping("shifts");
        dtoMapper.registerExceptDtoFieldForMapping("resourceAllocation");
        dtoMapper.registerExceptDtoFieldForMapping("postedOpenShifts");

        ScheduleDayViewDto result = dtoMapper.map(schedule, ScheduleDayViewDto.class);

        int totalMinutes = 0;
        int unfilledShiftCount = 0;

        Set<Skill> skillSet = new HashSet<>();

        List<Object[]> teamsInfoList = new ArrayList<>();

        Site site = null;
        Set<Team> teams = schedule.getTeams();
        for (Team team : teams) {
            // assume that all team are from one site
            if (result.getSiteInfo() == null) {
                site = teamService.getSite(team);
                if (shiftsOnly) {
                    break;
                }
                result.setSiteInfo(new String[] {site.getId(), site.getName(), site.getTimeZone().toString(),
                        site.getFirstDayOfWeek().name(), String.valueOf(site.getShiftIncrements())});
            }

            teamsInfoList.add(new Object[] {team.getId(), team.getName(), true});

            Collection<Skill> skills = team.getSkills();
            if (skills != null) {
                for (Skill skill : skills) {
                    skillSet.add(skill);
                }
            }
        }

        if (site == null) {
            throw new ValidationException(getMessage("validation.schedule.no.teams", schedule.getName()));
        }

        // Create shift info
        long shiftIncrement = site.getShiftIncrements() * 60 * 1000;
        List<Integer> resourceAllocation = new ArrayList<>();
        if (!shiftsOnly) {
            for (int i = 0; i < Constants.DAY_MILLISECONDS / shiftIncrement; i++) {
                resourceAllocation.add(0);
            }
        }

        long timeZoneOffset = site.getTimeZone().getOffset(schedule.getStartDate());

        long day = new LocalDate(date == null ? System.currentTimeMillis() : date).toDate().getTime();
        long start = day + timeZoneOffset;
        long end = day + Constants.DAY_MILLISECONDS - 1 + timeZoneOffset;

        Collection<PostedOpenShift> postedOsList =
                postedOpenShiftService.getPostedOpenShiftsOfSchedule(schedulePrimaryKey, start, end, false);
        List<ScheduleDayViewDto.PostedOpenShiftInfo> postedOpenShifts = new ArrayList<>();
        result.setPostedOpenShifts(postedOpenShifts);
        for (PostedOpenShift postedOpenShift : postedOsList) {
            ScheduleDayViewDto.PostedOpenShiftInfo postedOpenShiftInfo = new ScheduleDayViewDto.PostedOpenShiftInfo();
            postedOpenShiftInfo.setPosted(postedOpenShift.getDatePosted());
            postedOpenShiftInfo.setRequested(postedOpenShift.isRequested());
            postedOpenShiftInfo.setShiftId(postedOpenShift.getShiftId());

            postedOpenShifts.add(postedOpenShiftInfo);
        }

        List<Object[]> shiftInfos = new ArrayList<>();

        Collection<Shift> shifts = shiftService.getScheduleShiftsBetweenDates(schedule, start, end, true);
        Collection<Shift> shiftsForTotal = new ArrayList<>();
        for (Shift shift : shifts) {
            Object[] shiftInfo = new Object[] {shift.getId(), shift.isExcess(), shift.getTeamId(),
                    shift.getEmployeeId(), shift.getSkillId(), shift.getSkillAbbrev(), shift.getStartDateTime(),
                    shift.getEndDateTime(), shift.getPaidTime(), shift.getComment()};

            shiftInfos.add(shiftInfo);

            boolean startDateWithinDay = start <= shift.getStartDateTime() && end >= shift.getStartDateTime();

            if (startDateWithinDay) {
                if (StringUtils.isEmpty(shift.getEmployeeId())) {
                    unfilledShiftCount = unfilledShiftCount + shift.getShiftLength();
                }
                totalMinutes += shift.getShiftLength();

                shiftsForTotal.add(shift);
            }

            if (!shiftsOnly) {
                int relativeStart = (int) ((shift.getStartDateTime() + timeZoneOffset - start) / shiftIncrement);
                int relativeEnd = (int) ((shift.getEndDateTime() + timeZoneOffset - start) / shiftIncrement);
                while (relativeStart < relativeEnd) {
                    if (relativeStart >= 0) {
                        resourceAllocation.set(relativeStart, resourceAllocation.get(relativeStart) + 1);
                    }
                    relativeStart += 1;
                }
            }
        }

        result.setShifts(shiftInfos);
        result.setTotalMinutes(totalMinutes);
        result.setUnfilledShiftCount(unfilledShiftCount);

        Map<String, Object> overtimeMap = overtimeReportBetweenDates(schedule, shiftsForTotal);
        int overtimeHours = overtimeMap.get("overtime") == null ? 0 : (Integer) overtimeMap.get("overtime");
        int totalCost = overtimeMap.get("cost") == null ? 0 : (Integer) overtimeMap.get("cost");

        result.setOvertimeHours(overtimeHours);
        result.setTotalCost(totalCost);

        if (!shiftsOnly) {
            result.setResourceAllocation(resourceAllocation);

            // skills info
            List<Object[]> skillList = new ArrayList<>();
            for (Skill skill : skillSet) {
                skillList.add(new Object[]{skill.getId(), skill.getName(), skill.getAbbreviation(), true});
            }

            // employees info
            List<Object[]> employeesInfoList = new ArrayList<>();

            ResultSet<Employee> employeeResultSet = scheduleService.getEmployees(schedulePrimaryKey, "",
                    0, 0, null, null);

            Collections.sort((List<Employee>) employeeResultSet.getResult(), new Comparator<Employee>() {
                @Override
                public int compare(Employee employee1, Employee employee2) {
                    int result = String.CASE_INSENSITIVE_ORDER.compare(employee1.getFirstName(),
                            employee2.getFirstName());
                    if (result == 0) {
                        result = String.CASE_INSENSITIVE_ORDER.compare(employee1.getLastName(),
                                employee2.getLastName());
                    }
                    return result;
                }
            });

            long currentTime = System.currentTimeMillis();

            for (Employee employee : employeeResultSet.getResult()) {
                Skill primarySkill = employee.getPrimarySkill();
                String primarySkillId = primarySkill == null ? null : primarySkill.getId();

                Team homeTeam = employee.getHomeTeam();
                String homeTeamId = homeTeam == null ? null : homeTeam.getId();

                String teamIds = null;
                for (EmployeeTeam employeeTeam : employee.getEmployeeTeams()) {
                    Team team = employeeTeam.getTeam();
                    String teamId = team.getId();
                    if (StringUtils.isEmpty(teamIds)) {
                        teamIds = teamId;
                    } else {
                        teamIds += "," + teamId;
                    }
                    if (employeeTeam.getIsHomeTeam() && !arrayContainsId(teamsInfoList, teamId)) {
                        teamsInfoList.add(new Object[]{team.getId(), team.getName(), false});
                    }
                }

                String skillIds = null;
                for (EmployeeSkill employeeSkill : employee.getEmployeeSkills()) {
                    Skill skill = employeeSkill.getSkill();
                    String skillId = skill.getId();
                    if (StringUtils.isEmpty(skillIds)) {
                        skillIds = skillId;
                    } else {
                        skillIds += "," + skillId;
                    }
                    if (employeeSkill.getIsPrimarySkill() && !arrayContainsId(skillList, skillId)) {
                        skillList.add(new Object[]{skill.getId(), skill.getName(), skill.getAbbreviation(), false});
                    }
                }

                long millisSinceStarted = 0;
                if (employee.getStartDate() != null && employee.getStartDate().toDate().getTime() < currentTime) {
                    millisSinceStarted = currentTime - employee.getStartDate().toDate().getTime();
                }
                employeesInfoList.add(new Object[] {employee.getId(), employee.getFirstName(), employee.getLastName(),
                        employee.getHourlyRate(), teamIds, homeTeamId, primarySkillId, skillIds, millisSinceStarted});
            }

            ResultSetDto<Object[]> employeeInfo = new ResultSetDto<>();
            employeeInfo.setResult(employeesInfoList);
            employeeInfo.setTotal(employeesInfoList.size());

            result.setEmployeesInfo(employeeInfo);

            ResultSetDto<Object[]> skillInfo = new ResultSetDto<>();
            skillInfo.setResult(skillList);
            skillInfo.setTotal(skillList.size());

            result.setSkillsInfo(skillInfo);

            ResultSetDto<Object[]> teamInfo = new ResultSetDto<>();
            teamInfo.setResult(teamsInfoList);
            teamInfo.setTotal(teamsInfoList.size());

            result.setTeamsInfo(teamInfo);
        }

        return result;
    }

    @Validation
    public ShiftDto shiftManage(
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = Shift.class)
            PrimaryKey shiftPrimaryKey,
            ManageShiftParamsDto manageShiftParamsDto,
            String wflRequestId,
            String managerAccountId) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        String tenantId = schedulePrimaryKey.getTenantId();
        String comment = manageShiftParamsDto.getComment();
    	UserAccount managerAccount = null;
    	if (managerAccountId != null) {
    		managerAccount = userAccountService.getUserAccount(new PrimaryKey(tenantId, managerAccountId));
    	}
        Schedule schedule = scheduleService.getSchedule(schedulePrimaryKey);

        Shift shift = shiftService.getShift(shiftPrimaryKey);
        long previousShiftStartDateTime = shift.getStartDateTime();
        long previousShiftEndDateTime = shift.getEndDateTime();
        String previousComment = shift.getComment();
        Long previousChanged = shift.getChanged();
        String previousChgInfo = shift.getChgInfo();
        ShiftChangeType previousChangeType = shift.getChgType();

        ManageShiftParamsDto.ShiftInfo shiftInfo = manageShiftParamsDto.getShiftInfo();
        ManageShiftParamsDto.OpenShiftInfo openShiftInfo = manageShiftParamsDto.getOsShiftInfo();

    	// need to modify the original shift
        if ((shiftInfo == null || shift.getStartDateTime() == shiftInfo.getNewStartDateTime()
                && shift.getEndDateTime() == shiftInfo.getNewEndDateTime())) {
            if (StringUtils.isBlank(comment)) {
                throw new ValidationException(getMessage("shift.newdates.equals.olddated"));
            } else {
                shift.setComment(comment);
                shift.setChanged(System.currentTimeMillis());
                shift.setChgInfo(comment);
                shift.setChgType(ShiftChangeType.EDIT);

                manualShiftEdit(schedulePrimaryKey, shiftPrimaryKey, null, wflRequestId, managerAccountId, comment);

                return toDto(shift, ShiftDto.class);
            }
        }

        if (!manageShiftParamsDto.isForce()) {
            if (!checkQualification(schedulePrimaryKey, shiftPrimaryKey.getId(), shiftInfo.getNewStartDateTime(),
                    shiftInfo.getNewEndDateTime())) {
                throw new ValidationException(getMessage("employee.shift.not.qualified"));
            }
        }

        ShiftUpdateDto shiftUpdateDto = new ShiftUpdateDto();
        shiftUpdateDto.setStartDateTime(shiftInfo.getNewStartDateTime());
        shiftUpdateDto.setEndDateTime(shiftInfo.getNewEndDateTime());

        shift.setComment(comment);
        shift.setChanged(System.currentTimeMillis());
        shift.setChgInfo(comment);
        shift.setChgType(ShiftChangeType.EDIT);

        manualShiftEdit(schedulePrimaryKey, shiftPrimaryKey, shiftUpdateDto, wflRequestId, managerAccountId, comment);

        shift = shiftService.getShift(shiftPrimaryKey);
        Shift newShift = null;
        ShiftDto dto = toDto(shift, ShiftDto.class); // return original shift after modification if no further action

        if (openShiftInfo != null) {
        	// need now to create and open shift and possibly perform some action on it
        	CreateShiftParamsDto createShiftParamsDto = new CreateShiftParamsDto();
        	ShiftCreateDto osShiftInfo =  new ShiftCreateDto();
        	osShiftInfo.setStartDateTime(openShiftInfo.getStartDateTime());
        	osShiftInfo.setEndDateTime(openShiftInfo.getEndDateTime());
//  ??      	osShiftInfo.setId(id);
        	osShiftInfo.setSkillAbbrev(shift.getSkillAbbrev());
        	osShiftInfo.setSkillId(shift.getSkillId());
        	osShiftInfo.setSkillProficiencyLevel(shift.getSkillProficiencyLevel());
        	osShiftInfo.setTeamId(shift.getTeamId());
        	// TODO fix paid time
        	osShiftInfo.setPaidTime(shift.getPaidTime());		// this is incorrect as this shift is smaller than previous one
        	osShiftInfo.setSiteName(shift.getSiteName());
        	osShiftInfo.setComment(comment);

        	createShiftParamsDto.setAction(openShiftInfo.getAction());
        	createShiftParamsDto.setEmployeeId(openShiftInfo.getEmployeeId());
        	createShiftParamsDto.setEmployeeIds(openShiftInfo.getEmployeeIds());
        	createShiftParamsDto.setForce(manageShiftParamsDto.isForce());
        	createShiftParamsDto.setOverrideOptions(manageShiftParamsDto.getOverrideOptions());
        	createShiftParamsDto.setShiftInfo(osShiftInfo);

        	try {
        		newShift = doCreateShift(schedule, createShiftParamsDto, wflRequestId, managerAccount, comment);

                if (StringUtils.isNotEmpty(openShiftInfo.getAbsenceTypeId())) {
                    PrimaryKey absenceTypePrimaryKey = new PrimaryKey(tenantId, openShiftInfo.getAbsenceTypeId());
                    AbsenceType absenceType = absenceTypeService.getAbsenceType(absenceTypePrimaryKey);
                    if (absenceType == null) {
                        throw new ValidationException(getMessage("validation.absencetype.exist.error",
                                openShiftInfo.getAbsenceTypeId()));
                    }

                    // Create Unavailability for the employee owning the 'original' shift,
                    // and for the period corresponding to either the original shift 'delta'
                    CDAvailabilityTimeFrameCreateDto timeFrameCreateDto = new CDAvailabilityTimeFrameCreateDto();

                    int durationInMin = (int) Math.abs(previousShiftStartDateTime - shift.getStartDateTime()) / 60000;

                    timeFrameCreateDto.setIsPTO(true);
                    timeFrameCreateDto.setStartDateTime(Math.min(previousShiftStartDateTime, shift.getStartDateTime()));
                    timeFrameCreateDto.setDurationInMinutes(durationInMin);
                    timeFrameCreateDto.setEmployeeId(shift.getEmployeeId());
                    timeFrameCreateDto.setAbsenceTypeId(openShiftInfo.getAbsenceTypeId());
                    timeFrameCreateDto.setAvailabilityType(AvailabilityTimeFrame.AvailabilityType.UnAvail);
                    timeFrameCreateDto.setReason(comment);

                    employeeFacade.createCDAvailabilityTimeFrame(new PrimaryKey(tenantId), timeFrameCreateDto);
                }
            } catch (Throwable throwable) {
                // we need to 'rollback' changes which have been already committed
            	if (newShift != null) {
            		shiftService.delete(newShift);
            	}
                shift.setStartDateTime(previousShiftStartDateTime);
                shift.setEndDateTime(previousShiftEndDateTime);
                shift.setComment(previousComment);
                shift.setChanged(previousChanged == null ? 0 : previousChanged);
                shift.setChgInfo(previousChgInfo);
                shift.setChgType(previousChangeType);
                shiftService.update(shift);

                throw throwable;
            }
        }
        return dto;
    }

    private Shift doCreateShift(Schedule schedule, CreateShiftParamsDto createShiftParamsDto, String wflRequestId,
                                UserAccount managerAccount, String reason) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	ShiftCreateDto shiftInfo = createShiftParamsDto.getShiftInfo();
    	String tenantId = schedule.getTenantId();

    	// first, create OpenShift
        ShiftCreateDto shiftCreateDto = new ShiftCreateDto();
        shiftCreateDto.setStartDateTime(shiftInfo.getStartDateTime());
        shiftCreateDto.setEndDateTime(shiftInfo.getEndDateTime());
        shiftCreateDto.setTeamId(shiftInfo.getTeamId());
        shiftCreateDto.setSkillId(shiftInfo.getSkillId());
        shiftCreateDto.setSkillProficiencyLevel(shiftInfo.getSkillProficiencyLevel());
        shiftCreateDto.setPaidTime(shiftInfo.getPaidTime());

        ShiftDto newShiftDto = manualShiftCreate(schedule.getPrimaryKey(), shiftCreateDto, wflRequestId,
                managerAccount.getId(), reason);
        PrimaryKey newShiftPrimaryKey = new PrimaryKey(tenantId, newShiftDto.getId());
        Shift newShift = shiftService.getShift(newShiftPrimaryKey);
        newShift.setComment(shiftInfo.getComment());
        shiftService.update(newShift);
        // we need to do this because following underlaying logic is working in separate transaction
        scheduleService.flush();

       // then, see if any action needs to be performed
        try {
        	// if CreateAsOpenShift or CreateAndDrop, nothing to do
            if (ManageShiftAction.CreateAndAssign.equals(createShiftParamsDto.getAction())) {
            	// need to assign new shift to selected employee
                if (StringUtils.isEmpty(createShiftParamsDto.getEmployeeId())) {
                    throw new ValidationException(getMessage("validation.employee.not.exist", ""));
                }
                PrimaryKey employeePrimaryKey = new PrimaryKey(tenantId, createShiftParamsDto.getEmployeeId());
                manualShiftOpenAssign(schedule.getPrimaryKey(), newShift.getPrimaryKey(), employeePrimaryKey,
                        createShiftParamsDto.isForce(), wflRequestId, managerAccount.getId(), reason, null);
            } else if (ManageShiftAction.CreateAndPost.equals(createShiftParamsDto.getAction())) {
            	// need to post new shift to selected employees or all eligible employees if list unspecified
                if (!Posted.equals(schedule.getStatus())) {
                    throw new ValidationException("validation.schedule.status.nonoperational");
                }

                Map<String, Collection<String>> map = new HashMap<>();
                Collection<String> empIds = createShiftParamsDto.getEmployeeIds();
                if (empIds == null || empIds.isEmpty()) {
                    Collection<String> openShifts = new ArrayList<>();
                    openShifts.add(newShift.getId());

                    PostAllOpenShiftDto postAllOpenShiftDto = new PostAllOpenShiftDto();

                    postAllOpenShiftDto.setComments(shiftInfo.getComment());
                    postAllOpenShiftDto.setDeadline(newShift.getStartDateTime());
                    postAllOpenShiftDto.setOverrideOptions(createShiftParamsDto.getOverrideOptions());
                    postAllOpenShiftDto.setOpenShifts(openShifts);

                    postAllOpenShifts(schedule.getPrimaryKey(), postAllOpenShiftDto);
                } else {
                    map.put(newShift.getId(), empIds);
                    postedOpenShiftService.postOpenShifts(schedule, PostMode.Cumulative, map,
                    		createShiftParamsDto.getOverrideOptions(), newShift.getStartDateTime(),
                    		shiftInfo.getComment(), null); //TODO put terms instead of null when it will be ready
                }
            }
        } catch (Throwable throwable) {
            // we need to 'rollback' changes which have been already committed
            shiftService.delete(newShift);
            throw throwable;
        }
        return newShift;
    }

    private boolean checkQualification(PrimaryKey schedulePrimaryKey, String shiftId, long newStartDateTime,
                                       long newEndDateTime) throws IllegalAccessException {
        ShiftTimeQualificationExecuteDto executeDto = new ShiftTimeQualificationExecuteDto();
        executeDto.setProposedNewStartDateTime(newStartDateTime);
        executeDto.setProposedNewEndDateTime(newEndDateTime);
        executeDto.setShiftId(shiftId);

        QualificationRequestSummary summary = scheduleService.getShiftTimeQualification(schedulePrimaryKey, executeDto);
        return summary.getFullyQualified();
    }

    private boolean arrayContainsId(Collection<Object[]> arrays, Object id) {
        for (Object[] array : arrays) {
            if (array != null && array.length > 0) {
                if (id.equals(array[0])) {
                    return true;
                }
            }
        }
        return false;
    }

    private long getDateOfFirstDayInWeek(DayOfWeek dayOfWeek, long date) {
        LocalDate localDate = new LocalDate(date);
        while (true) {
            int dayOfWeekIdx = localDate.getDayOfWeek();
            if (dayOfWeekIdx == 7) {
                dayOfWeekIdx = 0;
            }
            if (dayOfWeekIdx == dayOfWeek.ordinal()) {
                break;
            }
            localDate = localDate.minusDays(1);
        }
        long result = localDate.toDate().getTime();
        if (date - Constants.WEEK_MILLISECONDS == result) {
            result = date;
        }
        return result;
    }

    private float calculateCosts(int hourlyRate, List<Map<String, Object>> infoList) {
        float result = 0;
        for (Map<String, Object> infoMap : infoList) {
            int shiftLength = ((Number) infoMap.get("shiftLength")).intValue();
            result += shiftLength / 60 * hourlyRate;
        }
        return result;
    }

    private int calculateDayOvertimes(float hoursPerDay, List<Map<String, Object>> infoList) {
        int result = 0;
        for (Map<String, Object> infoMap : infoList) {
            int shiftLength = ((Number) infoMap.get("shiftLength")).intValue();
            if (shiftLength > hoursPerDay * 60) {
                result = result + shiftLength - (int) (hoursPerDay * 60);
            }
        }
        return result;
    }

    private float calculateWeekOvertimes(float hoursPerWeek, List<Map<String, Object>> infoList) {
    	float result = 0;
        Map<Integer, Integer> weekShiftLengthMap = new HashMap<>();
        for (Map<String, Object> infoMap : infoList) {
            long startDate = ((Date) infoMap.get("startDate")).getTime();
            long startDateTime = ((Date) infoMap.get("startDateTime")).getTime();
            int weekIndex = (int) ((startDateTime - startDate) / Constants.WEEK_MILLISECONDS);
            int weekLength = 0;
            if (weekShiftLengthMap.containsKey(weekIndex)) {
                weekLength = weekShiftLengthMap.get(weekIndex);
            }
            weekLength += ((Number) infoMap.get("shiftLength")).intValue();
            weekShiftLengthMap.put(weekIndex, weekLength);
        }
        for (Integer weekShiftHours : weekShiftLengthMap.values()) {
            if (weekShiftHours > hoursPerWeek) {
                result = result + weekShiftHours - hoursPerWeek;
            }
        }
        return result;
    }

    private float calculateTwoWeekOvertimes(float hoursPerTwoWeek, List<Map<String, Object>> infoList) {
    	float result = 0;
        Map<Integer, Integer> twoWeekShiftLengthMap = new HashMap<>();
        for (Map<String, Object> infoMap : infoList) {
            long startDate = ((Date) infoMap.get("startDate")).getTime();
            long startDateTime = ((Date) infoMap.get("startDateTime")).getTime();
            int twoWeeksIndex = (int) ((startDateTime - startDate) / Constants.TWO_WEEK_MILLISECONDS);
            int twoWeeksLength = 0;
            if (twoWeekShiftLengthMap.containsKey(twoWeeksIndex)) {
                twoWeeksLength = twoWeekShiftLengthMap.get(twoWeeksIndex);
            }
            twoWeeksLength += ((Number) infoMap.get("shiftLength")).intValue();
            twoWeekShiftLengthMap.put(twoWeeksIndex, twoWeeksLength);
        }
        for (Integer twoWeekShiftHours : twoWeekShiftLengthMap.values()) {
            if (twoWeekShiftHours > hoursPerTwoWeek) {
                result = result + twoWeekShiftHours - hoursPerTwoWeek;
            }
        }
        return result;
    }

	/**
	 * getPostedOpenShifts() get list of Summarized PostedOpenShifts, with paging and sorting
	 * NOTE: paging and sorting  NOT IMPLEMENTED YET
	 * @param schedulePK
	 * @param select
	 * @param filter
	 * @param offset
	 * @param limit
	 * @param orderBy
	 * @param orderDir
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
    @Validation
	public ResultSetDto<SummarizedPostedOpenShiftDto> getPostedOpenShifts(
			@Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePK,
            String select, String filter, int offset,
			int limit, String orderBy, String orderDir) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
    	SimpleQuery sq = new SimpleQuery(schedulePK.getTenantId());
        sq.setSelect(null)
        	.setFilter(filter).addFilter("scheduleId = '" + schedulePK.getId() + "'")
        	.setOffset(offset).setLimit(limit)
        	.setOrderByField(orderBy).setOrderAscending(StringUtils.equals(orderDir, "ASC"))
        	.setTotalCount(true);
        Collection<PostedOpenShift> posList = postedOpenShiftService.findPostedOpenShifts(sq).getResult();

        // group PostedOpenShift by shift Id 
        Map<String, List<PostedOpenShift>> posByShiftIdMap = new HashMap<>();
        for (PostedOpenShift pos : posList) {
        	String shiftId = pos.getShiftId();
        	List<PostedOpenShift> postedShifts = posByShiftIdMap.get(shiftId);
        	if (postedShifts == null) {
        		postedShifts = new ArrayList<>();
        		posByShiftIdMap.put(shiftId, postedShifts);
        	}
        	postedShifts.add(pos);
        }

       // and then summarize them
        List<SummarizedPostedOpenShiftDto> summarizedPosList = new ArrayList<>();
        for (List<PostedOpenShift> postedShifts : posByShiftIdMap.values()) {
        	summarizedPosList.add(summarizePostedOpenShift(postedShifts));
        }

        ResultSetDto<SummarizedPostedOpenShiftDto> result = new ResultSetDto<>();
        result.setResult(summarizedPosList);
        result.setTotal(summarizedPosList.size());
        return result;
	}


	/**
	 * getPostedOpenShift() get a Summarized PostedOpenShift specified by schedule and shift Id
	 * @param schedulePK
	 * @param shiftId
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
    @Validation
	public SummarizedPostedOpenShiftDto getPostedOpenShift(
			@Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePK, String shiftId) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
    	SimpleQuery sq = new  SimpleQuery(schedulePK.getTenantId());
        sq.setSelect(null)
        	.addFilter("scheduleId = '" + schedulePK.getId() + "'")
        	.addFilter("shiftId = '" + shiftId + "'")
        	.setOffset(0).setLimit(1000)
        	.setTotalCount(false);
        Collection<PostedOpenShift> posList = postedOpenShiftService.findPostedOpenShifts(sq).getResult();

        SummarizedPostedOpenShiftDto sposDto = summarizePostedOpenShift(posList);
        return sposDto;
	}

	/**
	 * getPostedOpenShiftsRaw() get list of 'RAW' PostedOpenShifts (ie Posted OpenShifts rows as stored in Db)
	 * @param schedulePK
	 * @param select
	 * @param filter
	 * @param offset
	 * @param limit
	 * @param orderBy
	 * @param orderDir
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
    @Validation
	public ResultSetDto<PostedOpenShiftDto> getPostedOpenShiftsRaw(
			@Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePK,
            String select, String filter, int offset,
			int limit, String orderBy, String orderDir) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
    	SimpleQuery sq = new  SimpleQuery(schedulePK.getTenantId());
        sq.setSelect(null)
        	.setFilter(filter).addFilter("scheduleId = '" + schedulePK.getId() + "'")
        	.setOffset(offset).setLimit(limit)
        	.setOrderByField(orderBy).setOrderAscending(StringUtils.equals(orderDir, "ASC"))
        	.setTotalCount(true);
        ResultSet<PostedOpenShift> resultSet = postedOpenShiftService.findPostedOpenShifts(sq);
        return toResultSetDto(resultSet, PostedOpenShiftDto.class);
	}

	public void cancelPosts(PrimaryKey schedulePrimaryKey, Map<String, Collection<String>> cancelOpenShiftsMap) {
        postedOpenShiftService.cancelPosts(schedulePrimaryKey, cancelOpenShiftsMap);
    }

    @Validation
	public ResultSetDto<SummarizedPostedOpenShiftDto> getSummarizedOpenShifts(
			@Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            Long startDate,
            Long endDate) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException {
        Collection<PostedOpenShift> postedOpenShifts =
                postedOpenShiftService.getPostedOpenShiftsOfSchedule(schedulePrimaryKey, startDate, endDate, null);

        Collection<SummarizedPostedOpenShiftDto> summarizedPostedOpenShiftDtos =
                summarizePostedOpenShifts(postedOpenShifts);

		Collection<String> postedOpenShiftIds = new HashSet<>();
		for (SummarizedPostedOpenShiftDto sposDto : summarizedPostedOpenShiftDtos) {
			postedOpenShiftIds.add(sposDto.getShiftId());
		}

        Collection<Shift> openShifts = shiftService.getScheduleOpenShifts(schedulePrimaryKey, startDate, endDate);

        // and add non posted OpenShifts
		for (Shift shift : openShifts) {
			if (!postedOpenShiftIds.contains(shift.getId())) {
				// create a new 'dummy' Posted Open Shift entry
				SummarizedPostedOpenShiftDto sposDto = new SummarizedPostedOpenShiftDto();
				sposDto.setShiftId(shift.getId());
				sposDto.setScheduleId(shift.getScheduleId());
				//sposDto.setScheduleName(shift.g);
				sposDto.setSiteName(shift.getSiteName());
				sposDto.setTeamId(shift.getTeamId());
				sposDto.setTeamName(shift.getTeamName());

				sposDto.setStartDateTime(shift.getStartDateTime());
				sposDto.setEndDateTime(shift.getEndDateTime());
				sposDto.setShiftLength(shift.getShiftLength());
				sposDto.setExcess(shift.isExcess());

				sposDto.setSkillId(shift.getSkillId());
				sposDto.setSkillName(shift.getSkillName());
				sposDto.setSkillAbbrev(shift.getSkillAbbrev());

                summarizedPostedOpenShiftDtos.add(sposDto);
			}
		}

		ResultSetDto<SummarizedPostedOpenShiftDto> result = new ResultSetDto<>();
		result.setResult(summarizedPostedOpenShiftDtos);
		result.setTotal(summarizedPostedOpenShiftDtos.size());

		return result;
	}

    private Collection<SummarizedPostedOpenShiftDto> summarizePostedOpenShifts(
            Collection<PostedOpenShift> postedOpenShifts) {
        Collection<SummarizedPostedOpenShiftDto> result = new ArrayList<>();

        Map<String, Collection<PostedOpenShift>> shiftIdOpenShiftsMap = new HashMap<>();
        for (PostedOpenShift postedOpenShift : postedOpenShifts) {
            String shiftId = postedOpenShift.getShiftId();
            Collection<PostedOpenShift> postedOpenShiftsForShiftId = shiftIdOpenShiftsMap.get(shiftId);
            if (postedOpenShiftsForShiftId == null) {
                postedOpenShiftsForShiftId = new ArrayList<>();
            }
            postedOpenShiftsForShiftId.add(postedOpenShift);
            shiftIdOpenShiftsMap.put(shiftId, postedOpenShiftsForShiftId);
        }

        for (Map.Entry<String, Collection<PostedOpenShift>> entry : shiftIdOpenShiftsMap.entrySet()) {
            SummarizedPostedOpenShiftDto summarizePostedOpenShift = summarizePostedOpenShift(entry.getValue());
            result.add(summarizePostedOpenShift);
        }

        return result;
    }

    private SummarizedPostedOpenShiftDto summarizePostedOpenShift(Collection<PostedOpenShift> posList) {
        if (posList.size() < 1) {
            return null;
        }

        SummarizedPostedOpenShiftDto sposDto = new SummarizedPostedOpenShiftDto();
        Iterator<PostedOpenShift> it = posList.iterator();
        PostedOpenShift pos = it.next();

        sposDto.setShiftId(pos.getShiftId());
        sposDto.setScheduleId(pos.getScheduleId());
        sposDto.setScheduleName(pos.getScheduleName());
        sposDto.setSiteName(pos.getSiteName());
        sposDto.setSkillId(pos.getSkillId());
        sposDto.setSkillName(pos.getSkillName());
        sposDto.setSkillAbbrev(pos.getSkillAbbrev());
        sposDto.setTeamId(pos.getTeamId());
        sposDto.setTeamName(pos.getTeamName());

        sposDto.setPostId(pos.getPostId());
        sposDto.setShiftLength(pos.getShiftLength());
        sposDto.setStartDateTime(pos.getStartDateTime());
        sposDto.setEndDateTime(pos.getEndDateTime());
        sposDto.setExcess(pos.isExcess());
        sposDto.setDeadline(pos.getDeadline());
        sposDto.setComments(pos.getComments());
        sposDto.setTerms(pos.getTerms());
        // add employee and aggregated info
        do {
            PostedEmployeeDto empDto = new PostedEmployeeDto(pos.getEmployeeId(), pos.getEmployeeName());
            empDto.setRequested(pos.isRequested());
            empDto.setRequestedOn(pos.getRequestedOn());
            if (pos.isRequested()) {
                sposDto.addRequested();
                if (sposDto.getFirstDateRequested() >= pos.getRequestedOn())  {
                    sposDto.setFirstDateRequested(pos.getRequestedOn());
                }
            }
            sposDto.addEmployee(empDto);
            pos = it.hasNext() ? it.next() : null;
        } while (pos != null);

        sposDto.setEmpCount(sposDto.getEmployees().size());

        return sposDto;
    }

	/** Get result from synchronously processed qualification execution against variation
	 *  of existing shift with modified start and/or end times.
	 *  NOTE: Facade method implemented for pattern consistency, but just passing args on to
	 *        service layer.  For this functionality, validation and JPA entity retrieval are
	 *        performed in service layer in order to control transaction lifecycles.
	 *
     * @param schedulePk
     * @param shiftTimeQualificationExecuteDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
	@Validation
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public QualificationRequestSummary getShiftTimeQualification(
	        @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
	        PrimaryKey schedulePk,
	        ShiftTimeQualificationExecuteDto shiftTimeQualificationExecuteDto)
	        		throws InstantiationException, IllegalAccessException,
	        		NoSuchMethodException, InvocationTargetException {

		if (shiftTimeQualificationExecuteDto.getMaxSynchronousWaitSeconds() < 1) {
			throw new ValidationException(getMessage("validation.schedule.qualificationrequest.invalidsynchronouswait"));
		}

		return scheduleService.getShiftTimeQualification(schedulePk, shiftTimeQualificationExecuteDto);
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
    @Validation
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public CandidateShiftEligibleEmployeesDto getProposedOpenShiftEligibleEmployees(
    		@Validate(validator = EntityExistValidatorBean.class, type = Schedule.class) PrimaryKey schedulePk,
    		@Validate(validator = EntityExistValidatorBean.class, type = Team.class) PrimaryKey teamPk,
    		@Validate(validator = EntityExistValidatorBean.class, type = Skill.class) PrimaryKey skillPk,
    		long startDateTime, long endDateTime, Integer maxComputationTime, Integer maxSynchronousWaitSeconds,
    		Integer maxUnimprovedSecondsSpent, Boolean includeDetails, 
    		Map<ConstraintOverrideType, Boolean> overrideOptions) throws IllegalAccessException {
    	if (maxSynchronousWaitSeconds < 1) {
            throw new ValidationException(getMessage("validation.schedule.qualificationrequest.invalidsynchronouswait"));
        }

    	// TODO Validate teamId and skillId?  What about transaction?

		return scheduleService.getProposedOpenShiftEligibleEmployees(schedulePk, teamPk, skillPk, 
				startDateTime, endDateTime, maxComputationTime, maxSynchronousWaitSeconds, 
				maxUnimprovedSecondsSpent, includeDetails, overrideOptions);
	}

	/** Get result from synchronously processed qualification execution against variation
	 *  of existing shift with new assignment.
     *
     * @param schedulePk
     * @param overriddenShiftQualExecuteDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
	@Validation
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public QualificationRequestSummary getOverriddenShiftQualification(
	        @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
	        PrimaryKey schedulePk,
	        OverriddenShiftQualExecuteDto overriddenShiftQualExecuteDto)
	        		throws InstantiationException, IllegalAccessException,
	        		NoSuchMethodException, InvocationTargetException {
		if (overriddenShiftQualExecuteDto.getMaxSynchronousWaitSeconds() < 1) {
			throw new ValidationException(getMessage("validation.schedule.qualificationrequest.invalidsynchronouswait"));
		}

		return scheduleService.getOverriddenShiftQualification(schedulePk, overriddenShiftQualExecuteDto);
	}

    public Collection<ScheduleWithSiteAndTeamsDto> queryByDay(String tenantId, ScheduleQueryByDayParamDto paramDto,
                                                              AccountACL acl) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Collection<ScheduleWithSiteAndTeamsDto> result = new ArrayList<>();

        Collection<Object[]> scheduleRows = scheduleService.queryByDay(tenantId, paramDto, acl);

        if ((Posted.equals(paramDto.getStatus())
                || ScheduleStatus.Production.equals(paramDto.getStatus())) && paramDto.getDate() > 0
                && scheduleRows.size() > 1) {
            throw new ValidationException(getMessage("schedule.status.date.error", paramDto.getStatus(),
                    new Date(paramDto.getDate())));
        }

        for (Object[] row : scheduleRows) {
            ScheduleWithSiteAndTeamsDto dto = new ScheduleWithSiteAndTeamsDto();
            result.add(dto);

            dto.setId((String) row[0]);
            dto.setName((String) row[1]);
            dto.setDescription((String) row[2]);
            dto.setStartDate(row[3] == null ? 0 : ((Date) row[3]).getTime());
            dto.setEndDate(row[4] == null ? 0 : ((Date) row[4]).getTime());
            dto.setLengthInDays(row[5] == null ? 0 : (int) row[5]);
            dto.setStatus(row[6] == null ? null : ScheduleStatus.values()[(int) row[6]]);
            dto.setState(row[7] == null ? null : TaskState.values()[(int) row[7]]);

            if (row[8] != null) {
                String[] siteParts = ((String) row[8]).split(":");

                ScheduleWithSiteAndTeamsDto.SiteDto siteDto = new ScheduleWithSiteAndTeamsDto.SiteDto();
                dto.setSite(siteDto);

                siteDto.setSiteId(siteParts[0]);
                siteDto.setSiteName(siteParts[1]);

                Site site = siteService.getSite(new PrimaryKey(tenantId, siteDto.getSiteId()));

                siteDto.setSiteTimeZone(site.getTimeZone() == null ? null : site.getTimeZone().toString());
            }

            if (row[9] != null) {
                Collection<ScheduleWithSiteAndTeamsDto.TeamDto> teams = new ArrayList<>();
                dto.setTeams(teams);

                String[] teamPartsCollection = ((String) row[9]).split(",");
                for (String teamInfo : teamPartsCollection) {
                    String[] teamParts = teamInfo.split(":");
                    ScheduleWithSiteAndTeamsDto.TeamDto teamDto = new ScheduleWithSiteAndTeamsDto.TeamDto();
                    teams.add(teamDto);

                    teamDto.setTeamId(teamParts[0]);
                    teamDto.setTeamName(teamParts[1]);
                }
            }
        }

        return result;
    }

    @Validation
    public EmployeeCalendarAvailabilityDto getCalendarAvailabilityView(
            boolean requestInfo,
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = Schedule.class)
            PrimaryKey schedulePrimaryKey,
            Long startDate,
            Long endDate,
            String status,
            String returnedFields,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        EmployeeCalendarAvailabilityDto result = getCalendarView(requestInfo, employeePrimaryKey, schedulePrimaryKey,
                startDate, endDate, status, returnedFields, offset, limit, orderBy, orderDir);

        // Note that the getAvailcalView method expects both firstCalendarDate and lastCalendarDate to represent
        // DATES ONLY, meaning each should be the millis instant (UTC) that represents the site-specific start
        // of each date (midnight in site timezone).  So let's calculate to make sure that's what we send...
        DateTimeZone siteTimeZone = employeeService.getEmployee(employeePrimaryKey).getSite().getTimeZone();
        DateTime startDateInTZ = new DateTime(startDate, siteTimeZone);
        DateTime beginningOfStartDateInTZ = startDateInTZ.withTimeAtStartOfDay();
        Long firstCalendarDate = beginningOfStartDateInTZ.toInstant().getMillis();

        DateTime endDateInTZ = new DateTime(endDate, siteTimeZone);
        DateTime beginningOfEndDateInTZ = endDateInTZ.withTimeAtStartOfDay();
        Long lastCalendarDate = beginningOfEndDateInTZ.toInstant().getMillis();

        AvailcalViewDto availcalViewDto = employeeFacade.getAvailcalView(employeePrimaryKey, firstCalendarDate,
                lastCalendarDate);
        result.setAvailcalViewDto(availcalViewDto);

        return result;
    }

    @Validation
    public String getCalendarSyncId(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey) {
        Employee employee = employeeService.getEmployee(employeePrimaryKey);
		return employee.getCalendarSyncId();
	}

    private EmployeeCalendarAvailabilityDto getCalendarView(
            boolean requestInfo,
            PrimaryKey employeePrimaryKey,
            PrimaryKey schedulePrimaryKey,
            Long startDate,
            Long endDate,
            String status,
            String returnedFields,
            int offset,
            int limit,
            String orderBy,
            String orderDir)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (startDate == null || endDate == null) {
            throw new ValidationException(getMessage("validation.error.not.enough.params"));
        }

        EmployeeAvailabilityDto employeeAvailabilityDto =
                employeeFacade.getEmployeeAvailability(employeePrimaryKey, startDate, endDate, status, null);

        if (employeeAvailabilityDto.getEmpUnavailabilities() != null) {
            Iterator<EmployeeAvailabilityDto.EmployeeUnavailabilityDto> iterator =
                    employeeAvailabilityDto.getEmpUnavailabilities().iterator();
            while (iterator.hasNext()) {
                if (!iterator.next().isPto()) {
                    iterator.remove();
                }
            }
        }

        EmployeeCalendarAvailabilityDto result = new EmployeeCalendarAvailabilityDto();
        result.setEmpUnavailabilities(employeeAvailabilityDto.getEmpUnavailabilities());
        result.setOpenShiftsByDays(employeeAvailabilityDto.getOpenShiftsByDays());
        result.setOrgHolidays(employeeAvailabilityDto.getOrgHolidays());
        result.setSchedules(employeeAvailabilityDto.getSchedules());

        ResultSetDto<Object[]> shifts = shiftFacade.getScheduleAndProdPostedShifts(employeePrimaryKey.getId(),
                schedulePrimaryKey.getId(), startDate, endDate, returnedFields, offset, limit, orderBy, orderDir);

        result.setShifts(shifts);

        if (requestInfo) {
            Map<String, List<?>> wipSwapMap = employeeFacade.peerWipSwapRequestInfos(employeePrimaryKey, startDate,
                    endDate);

            result.setPeerSwapRequestInfos(
                    (List<EmployeeAvailabilityAndShiftsDto.PeerSwapRequestInfo>) wipSwapMap.get("SHIFT_SWAP_REQUEST"));
            result.setPeerWipRequestInfos(
                    (List<EmployeeAvailabilityAndShiftsDto.PeerWipRequestInfo>) wipSwapMap.get("WIP_REQUEST"));

            Map<String, List<?>> openShiftsMap = employeeFacade.submittedOpenShiftsRequestInfos(employeePrimaryKey,
                    startDate, endDate);

            result.setSubmittedWipSwapRequestInfos(
                    (List<EmployeeAvailabilityAndShiftsDto.SubmittedWipSwapRequestInfo>) openShiftsMap.get("SUBMITTED"));
            result.setSubmittedTimeOffRequestInfos(
                    (List<EmployeeAvailabilityAndShiftsDto.SubmittedTimeOffRequestInfo>) openShiftsMap.get("TIME_OFF"));
            result.setSubmittedOsRequestInfos(
                    (List<EmployeeAvailabilityAndShiftsDto.SubmittedOsRequestInfo>) openShiftsMap.get("OPEN_SHIFTS"));
        }

        return result;
    }

}
