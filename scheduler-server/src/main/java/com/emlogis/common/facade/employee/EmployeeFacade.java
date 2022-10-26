package com.emlogis.common.facade.employee;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.*;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import com.emlogis.common.EmployeeCalendarUtils;
import com.emlogis.common.facade.schedule.ShiftFacade;
import com.emlogis.common.services.contract.ContractLineService;
import com.emlogis.common.services.contract.ContractService;
import com.emlogis.common.services.structurelevel.SiteService;
import com.emlogis.common.services.tenant.OrganizationService;
import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.common.services.workflow.type.WflProcessTypeService;
import com.emlogis.common.validation.annotations.*;
import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.model.contract.Contract;
import com.emlogis.model.contract.ContractLine;
import com.emlogis.model.contract.IntMinMaxCL;
import com.emlogis.model.employee.*;
import com.emlogis.model.employee.dto.*;
import com.emlogis.model.tenant.*;
import com.emlogis.model.workflow.entities.WflProcessType;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.rest.resources.util.*;
import com.emlogis.server.services.cache.BasicCacheService;
import com.emlogis.workflow.enums.AvailabilityRequestSubtype;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.facade.contract.ContractFacade;
import com.emlogis.common.facade.tenant.UserAccountFacade;
import com.emlogis.common.facade.workflow.dashboard.details.RequestDetailsFacade;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.services.employee.AbsenceTypeService;
import com.emlogis.common.services.employee.CDAvailabilityTimeFrameService;
import com.emlogis.common.services.employee.CIAvailabilityTimeFrameService;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.employee.SkillService;
import com.emlogis.common.services.notification.NotificationConfigInfo;
import com.emlogis.common.services.schedule.PostedOpenShiftService;
import com.emlogis.common.services.schedule.ScheduleService;
import com.emlogis.common.services.schedule.ShiftService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.common.services.tenant.GroupAccountService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.services.util.AccountUtilService;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.communication.ShiftQualificationDto;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.contract.EmployeeContract;
import com.emlogis.model.contract.dto.ContractDTO;
import com.emlogis.model.contract.dto.OvertimeDto;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.employee.AvailabilityTimeFrame.AvailabilityType;
import com.emlogis.model.notification.MsgDeliveryType;
import com.emlogis.model.schedule.PostedOpenShift;
import com.emlogis.model.schedule.QualificationRequestTracker;
import com.emlogis.model.schedule.Schedule;
import com.emlogis.model.schedule.ScheduleStatus;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.schedule.ShiftSwapEligibilityRequestTracker;
import com.emlogis.model.schedule.TaskState;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.structurelevel.dto.SiteDto;
import com.emlogis.model.structurelevel.dto.TeamDto;
import com.emlogis.model.tenant.dto.AccountPictureDto;
import com.emlogis.model.tenant.dto.UserAccountDto;
import com.emlogis.rest.security.SessionService;

import static com.emlogis.common.EmlogisUtils.fromJsonString;
import static com.emlogis.model.common.CacheConstants.EMP_AVAILABILITY_CACHE;
import static com.emlogis.model.common.CacheConstants.EMP_AVAILABILITY_CACHE_MONTHS;
import static com.emlogis.model.schedule.ScheduleStatus.valueOf;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class EmployeeFacade extends BaseFacade {

    @EJB
    private EmployeeService employeeService;

    @EJB
    private SkillService skillService;

    @EJB
    private TeamService teamService;

    @EJB
    private SiteService siteService;

    @EJB
    private SessionService sessionService;

    @EJB
    private GroupAccountService groupAccountService;

    @EJB
    private UserAccountService userAccountService;

    @EJB
    private UserAccountFacade userAccountFacade;

    @EJB
    private TenantService tenantService;

    @EJB
    private ContractFacade contractFacade;

    @EJB
    private AbsenceTypeService absenceTypeService;

    @EJB
    private CDAvailabilityTimeFrameService cdAvailabilityTimeFrameService;

    @EJB
    private CIAvailabilityTimeFrameService ciAvailabilityTimeFrameService;

    @EJB
    private PostedOpenShiftService postedOpenShiftService;

    @EJB
    private ShiftService shiftService;

    @EJB
    private ScheduleService scheduleService;

    @EJB
    private ShiftFacade shiftFacade;

    @EJB
    private WflProcessTypeService wflProcessTypeService;

    @EJB
    private ContractService contractService;

    @EJB
    private ContractLineService contractLineService;

    @EJB
    private BasicCacheService cacheService;
    
    @EJB
    private RequestDetailsFacade requestDetailsfacade;
    
    @EJB
    private WorkflowRequestService workflowRequestService;
    
    @EJB
    private AccountUtilService accountUtilService;

    @EJB
    private OrganizationService organizationService;


    public static class EmployeeOpenShiftUnicity {
        String skillId;
        String teamId;
        int shiftLength;
        long startDateTime;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EmployeeOpenShiftUnicity)) return false;

            EmployeeOpenShiftUnicity that = (EmployeeOpenShiftUnicity) o;

            return shiftLength == that.shiftLength && startDateTime == that.startDateTime
                    && StringUtils.equals(skillId, that.skillId) && StringUtils.equals(teamId, that.teamId);
        }

        @Override
        public int hashCode() {
            int result = skillId.hashCode();
            result = 31 * result + teamId.hashCode();
            result = 31 * result + shiftLength;
            result = 31 * result + (int) (startDateTime ^ (startDateTime >>> 32));
            return result;
        }
    }

    /**
     * Gets objects (Employees)
     *
     * @param tenantId
     * @param select
     * @param filter
     * @param offset
     * @param limit
     * @param orderBy
     * @param orderDir
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public ResultSetDto<EmployeeDto> getObjects(
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

        if (StringUtils.isEmpty(simpleQuery.getFilter()) || !simpleQuery.getFilter().contains("isDeleted")) {
            simpleQuery.addFilter("isDeleted=false");
        }

        ResultSet<Employee> rs = employeeService.findEmployees(simpleQuery);
        return toResultSetDto(rs, EmployeeDto.class);
    }

    /**
     * Get object (Employee)
     *
     * @param primaryKey
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public EmployeeWithOvertimeDto getObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Employee employee = employeeService.getEmployee(primaryKey);
        EmployeeContract contract = employee.getEmployeeContracts().iterator().next();

        OvertimeDto overtimeDto = contractFacade.getOverTimeCLValues(contract);

        DtoMapper<Employee, EmployeeWithOvertimeDto> dtoMapper = new DtoMapper<>();
        dtoMapper.registerExceptDtoFieldForMapping("overtimeDto");

        EmployeeWithOvertimeDto dto = dtoMapper.map(employee, EmployeeWithOvertimeDto.class);
        dto.setOvertimeDto(overtimeDto);

        return dto;
    }

    /**
     * Update object (Employee)
     *
     * @param primaryKey
     * @param employeeUpdateDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public EmployeeWithOvertimeDto updateObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey primaryKey,
            @ValidateAll(
                strLengths = {
                    @ValidateStrLength(field = EmployeeUpdateDto.FIRST_NAME, min = 1, max = 50, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.LAST_NAME, min = 1, max = 50, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.MIDDLE_NAME, min = 1, max = 50, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.EMPLOYEE_IDENTIFIER, min = 1, max = 256, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.WORK_EMAIL, min = 6, max = 256, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.ADDRESS, min = 1, max = 50, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.ADDRESS2, min = 1, max = 50, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.CITY, min = 1, max = 50, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.STATE, min = 1, max = 2, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.ZIP, min = 1, max = 13, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.EC_RELATIONSHIP, min = 1, max = 50, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.EC_PHONE_NUMBER, min = 1, max = 20, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.EMERGENCY_CONTACT, min = 1, max = 50, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.GENDER, min = 1, max = 6, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.HOME_PHONE, min = 1, max = 20, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.WORK_PHONE, min = 1, max = 20, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.MOBILE_PHONE, min = 1, max = 20, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.HOME_EMAIL, min = 1, max = 50, passNull = true),
                    @ValidateStrLength(field = EmployeeUpdateDto.PROFESSIONAL_LABEL, min = 1, max = 50, passNull = true)
                },
                regexes = {
                    @ValidateRegex(field = EmployeeUpdateDto.WORK_EMAIL, regex = Constants.EMAIL_REGEX),
                    @ValidateRegex(field = EmployeeUpdateDto.HOME_EMAIL, regex = Constants.EMAIL_REGEX)
                },
                uniques = {
                    @ValidateUnique(fields = EmployeeUpdateDto.EMPLOYEE_IDENTIFIER, type = Employee.class)
                }
            )
            EmployeeUpdateDto employeeUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Employee employee = employeeService.getEmployee(primaryKey);

        boolean modified = updateEmployee(employee, employeeUpdateDto);

        if (modified) {
            setUpdatedBy(employee);
            employee = employeeService.update(employee);
            getEventService().sendEntityUpdateEvent(employee, EmployeeDto.class);
        }

        return getObject(primaryKey);
    }

    /**
     * Creates a Employee and associate it to a 'parent' Site
     *
     * @param tenantId
     * @param employeeCreateDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public EmployeeWithOvertimeDto createObject(
            String tenantId,
            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = EmployeeCreateDto.EMPLOYEE_IDENTIFIER, min = 1, max = 256),
                            @ValidateStrLength(field = EmployeeCreateDto.FIRST_NAME, min = 1, max = 50),
                            @ValidateStrLength(field = EmployeeCreateDto.LAST_NAME, min = 1, max = 50)
                    },
                    uniques = {
                            @ValidateUnique(fields = EmployeeCreateDto.EMPLOYEE_IDENTIFIER, type = Employee.class)
                    }
            )
            EmployeeCreateDto employeeCreateDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Employee employee = createEmployee(tenantId, employeeCreateDto);
        return getObject(employee.getPrimaryKey());
    }

    public int createObjects(String tenantId, Collection<EmployeeCreateDto> employeeCreateDtos)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        for (EmployeeCreateDto createDto : employeeCreateDtos) {
            createEmployee(tenantId, createDto);
        }

        return employeeCreateDtos.size();
    }

    /**
     * Delete an employee. Note that this is a 'soft delete' where employee is marked isDeleted = true and timestamp
     * strings are appended email, employeeIdentifier, and loginName if applicable.
     *
     * @param primaryKey
     * @return
     */
    @Validation
    public boolean softDeleteObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey primaryKey) {
        Employee employee = employeeService.getEmployee(primaryKey);
        employeeService.softDelete(employee);
        getEventService().sendEntityDeleteEvent(employee, EmployeeDto.class);
        return true;
    }

    /**
     * Get the UserAccount associated to the Employee specified by the PrimaryKey
     *
     * @param employeePrimaryKey
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    @Validation
    public UserAccountDto getUserAccount(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, IllegalArgumentException {
        Employee employee = employeeService.getEmployee(employeePrimaryKey);
        UserAccount userAccount = employee.getUserAccount();
        return this.toDto(userAccount, UserAccountDto.class);
    }

    /**
     * Update the UserAccount associated to the Employee specified by the PrimaryKey key. If
     * one is not already associated, associated matching existing or newly created UserAccount.
     *
     * @param employeePrimaryKey
     * @param userAccountDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    @Validation
    public UserAccountDto updateUserAccount(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey, UserAccountDto userAccountDto) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException {
    	Employee employee = employeeService.getEmployee(employeePrimaryKey);
        UserAccount userAccount = employee.getUserAccount();
        UserAccountDto retUserAccountDto =  userAccountFacade.updateObject(userAccount.getPrimaryKey(), userAccountDto);
        // event below is a bit misleading as actually this is the employee account which is modified, not the employee directly
        getEventService().sendEntityUpdateEvent(employee, EmployeeDto.class);
        return retUserAccountDto;
    }

	private void createUserAccount(Employee employee, String firstName, String lastName, String workEmail,
                                   UserAccountDto userAccountDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
		String login = null;
		if (userAccountDto != null) {
			login = userAccountDto.getLogin();
			if (StringUtils.isBlank(workEmail)) {
				workEmail = userAccountDto.getWorkEmail();
			}
		}
		
		login = employeeService.createLogin(employee, null, firstName, lastName, login, true);
		if (StringUtils.isBlank(workEmail)) {
			workEmail = "chgemail-" + login + "@emlogis.com";
		}
		if (userAccountDto == null) {
			userAccountDto = new UserAccountDto();
		}
		userAccountDto.setLogin(login);
		userAccountDto.setFirstName(firstName);
		userAccountDto.setLastName(lastName);
		userAccountDto.setWorkEmail(workEmail);		
		userAccountDto.setInactivityPeriod(0L);  // default (org level)inactivity period

        PrimaryKey primaryKey = new PrimaryKey(employee.getTenantId(), "emp-" + employee.getId());

        userAccountFacade.createObject(primaryKey, userAccountDto);

        UserAccount userAccount = userAccountService.getUserAccount(primaryKey);

        if (employee.getActivityType() == EmployeeActivityType.Inactive) {
        	userAccount.setStatus(AccountStatus.Revoked);
        }
        employee.setUserAccount(userAccount);
        
        // make user account a member of the 'Employees' group
        PrimaryKey groupPk = new PrimaryKey(employee.getTenantId(), GroupAccount.DEFAULT_EMPLOYEEGROUP_ID);
        GroupAccount group = groupAccountService.getGroupAccount(groupPk);
        groupAccountService.addMember(group, userAccount);
        userAccountService.update(userAccount);
        employeeService.update(employee);        
	}

    @Validation
	public NotificationConfigInfo checkNotificationEnabled(
			@Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
			MsgDeliveryType deliveryType) {
        Employee employee = employeeService.getEmployee(employeePrimaryKey);
        return employeeService.checkNotificationEnabled(employee, deliveryType);
	}

    @Validation
    public AccountPictureDto getPicture(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, IOException {
        Employee employee = employeeService.getEmployee(employeePrimaryKey);
        return userAccountFacade.getPicture(employee.getUserAccount().getPrimaryKey());
    }

    @Validation
    public boolean updatePicture(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            AccountPictureDto accountPictureDto) {
        Employee employee = employeeService.getEmployee(employeePrimaryKey);
        return userAccountFacade.updatePicture(employee.getUserAccount().getPrimaryKey(), accountPictureDto);
    }
    
    /**
     * Add employee skill
     *
     * @param employeePrimaryKey
     * @param employeeSkillCreateDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    @Validation
    public EmployeeSkillViewDto addEmployeeSkill(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            EmployeeSkillCreateDto employeeSkillCreateDto)
            throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        String skillId = employeeSkillCreateDto.getSkillId();
        Skill skill = skillService.getSkill(new PrimaryKey(employeePrimaryKey.getTenantId(), skillId));
        Employee employee = employeeService.getEmployee(employeePrimaryKey);

        // Validate relationship doesn't already exist between employee and skill.
        // Table constraint prevents it anyway, but this is typically a small collection
        // and we can provide better feedback to the caller by validating here.
/*        
        for (EmployeeSkill employeeSkill : employee.getEmployeeSkills()) {
            if (employeeSkill.getSkill().equals(skill)) {
                throw new ValidationException("Relationship already exists between this employee and skill.");
            }
        }
*/
        EmployeeSkill employeeSkill = new EmployeeSkill(new PrimaryKey(employeePrimaryKey.getTenantId()));
        employeeSkill.setIsPrimarySkill(employeeSkillCreateDto.getIsPrimarySkill());
        employeeSkill.setSkillScore(employeeSkillCreateDto.getSkillScore());
        employeeSkill = employeeService.addEmployeeSkill(employee, skill, employeeSkill);
        return new EmployeeSkillDtoMapper().map(employeeSkill, EmployeeSkillViewDto.class);
    }

    /**
     * Get employee skills
     *
     * @param employeePrimaryKey
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    @Validation
    public ResultSetDto<EmployeeSkillViewDto> getEmployeeSkills(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            String select, // select is NOT IMPLEMENTED FOR NOW ...
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(employeePrimaryKey.getTenantId());
        simpleQuery.setEntityClass(EmployeeSkill.class).setSelect(select).setFilter(filter).setOffset(offset)
                .setLimit(limit).setOrderByField(orderBy)
                .addFilter("employee.primaryKey.id='" + employeePrimaryKey.getId() + "'")
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true);
        ResultSet<EmployeeSkill> resultSet = employeeService.getEmployeeSkills(employeePrimaryKey, simpleQuery);
        return this.toResultSetDto(resultSet, EmployeeSkillViewDto.class, new EmployeeSkillDtoMapper());
    }

    /**
     * Update employee skill
     *
     * @param employeePrimaryKey
     * @param skillPrimaryKey
     * @param employeeSkillUpdateDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    @Validation
    public EmployeeSkillViewDto updateEmployeeSkill(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = Skill.class)
            PrimaryKey skillPrimaryKey,
            EmployeeSkillUpdateDto employeeSkillUpdateDto)
            throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        boolean modified = false;

        Employee employee = employeeService.getEmployee(employeePrimaryKey);
        Skill skill = skillService.getSkill(skillPrimaryKey);
        EmployeeSkill employeeSkill = employeeService.findEmployeeSkill(employee, skill);

        if (employeeSkill != null) {
            if (employeeSkillUpdateDto.getIsPrimarySkill() != null) {
                employeeSkill.setIsPrimarySkill(employeeSkillUpdateDto.getIsPrimarySkill());
                modified = true;
            }
            if (employeeSkillUpdateDto.getSkillScore() != null) {
                employeeSkill.setSkillScore(employeeSkillUpdateDto.getSkillScore());
                modified = true;
            }
            if (modified) {
                setUpdatedBy(employeeSkill);
                employeeSkill = employeeService.updateEmployeeSkill(employeeSkill);
            }
            return new EmployeeSkillDtoMapper().map(employeeSkill, EmployeeSkillViewDto.class);
        } else {
            throw new ValidationException("Can't update. EmployeeSkill wasn't found.");
        }
    }

    /**
     * Remove employee skills
     *
     * @param employeePrimaryKey
     * @param skillPrimaryKey
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    @Validation
    public void removeEmployeeSkill(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            PrimaryKey skillPrimaryKey)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Employee employee = employeeService.getEmployee(employeePrimaryKey);
        Skill skill = skillService.getSkill(skillPrimaryKey);
        employeeService.removeEmployeeSkill(employee, skill);
    }

    /**
     * Add employee team.  Note that since there must always be a home
     * team, if this is the first team to be added then it will be made
     * the home team implicitly (regardless of provided DTO's isHomeTeam
     * setting).
     *
     * @param employeePrimaryKey
     * @param employeeTeamCreateDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    @Validation
    public EmployeeTeamViewDto addEmployeeTeam(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            EmployeeTeamCreateDto employeeTeamCreateDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String teamId = employeeTeamCreateDto.getTeamId();
        Employee employee = employeeService.getEmployee(employeePrimaryKey);
        Team team = teamService.getTeam(new PrimaryKey(employeePrimaryKey.getTenantId(), teamId));

        // Validate relationship doesn't already exist between employee and team.
        // Table constraint prevents it anyway, but this is typically a small collection
        // and we can provide better feedback to the caller by validating there.
        if (employeeService.checkEmployeeTeam(employee.getPrimaryKey(), team.getPrimaryKey())) {
            throw new ValidationException("Relationship already exists between this employee and team.");
        }

        EmployeeTeam employeeTeam = new EmployeeTeam(new PrimaryKey(employeePrimaryKey.getTenantId()));
        employeeTeam.setIsFloating(employeeTeamCreateDto.getIsFloating());
        employeeTeam.setIsHomeTeam(employeeTeamCreateDto.getIsHomeTeam());
        employeeTeam.setIsSchedulable(employeeTeamCreateDto.getIsSchedulable());
        employeeTeam = employeeService.addEmployeeTeam(employee, team, employeeTeam);
        return new EmployeeTeamDtoMapper().map(employeeTeam, EmployeeTeamViewDto.class);
    }

    /**
     * Get employee teams
     *
     * @param employeePrimaryKey
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    @Validation
    public ResultSetDto<EmployeeTeamViewDto> getEmployeeTeams(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class) PrimaryKey employeePrimaryKey,
            String select, // select is NOT IMPLEMENTED FOR NOW ...
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(employeePrimaryKey.getTenantId());
        simpleQuery.setEntityClass(EmployeeTeam.class).setSelect(select).setFilter(filter).setOffset(offset)
                .setLimit(limit).setOrderByField(orderBy)
                .addFilter("employee.primaryKey.id='" + employeePrimaryKey.getId() + "'")
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true);
        ResultSet<EmployeeTeam> resultSet = employeeService.getEmployeeTeams(employeePrimaryKey, simpleQuery);
        return this.toResultSetDto(resultSet, EmployeeTeamViewDto.class, new EmployeeTeamDtoMapper());
    }

    @Validation
    public ResultSetDto<TeamDto> getUnassociatedEmployeeTeams(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            String select, // select is NOT IMPLEMENTED FOR NOW ...
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(employeePrimaryKey.getTenantId());
        simpleQuery.setEntityClass(Team.class).setSelect(select).setFilter(filter).setOffset(offset)
                .setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true);
        ResultSet<Team> resultSet = employeeService.getUnassociatedEmployeeTeams(employeePrimaryKey, simpleQuery);
        return this.toResultSetDto(resultSet, TeamDto.class);
    }

    @Validation
    public ResultSetDto<SkillDto> getUnassociatedEmployeeSkills(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            String select, // select is NOT IMPLEMENTED FOR NOW ...
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(employeePrimaryKey.getTenantId());
        simpleQuery.setEntityClass(Skill.class).setSelect(select).setFilter(filter).setOffset(offset)
                .setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true);
        ResultSet<Skill> resultSet = employeeService.getUnassociatedEmployeeSkills(employeePrimaryKey, simpleQuery);
        return this.toResultSetDto(resultSet, SkillDto.class);
    }

    /**
     * Update employee team.  Note that since there must always be a
     * home team, the update will not be allowed if it would have
     * resulted in there being no home team designation.
     *
     * @param employeePrimaryKey
     * @param teamPrimaryKey
     * @param employeeTeamUpdateDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    @Validation
    public EmployeeTeamViewDto updateEmployeeTeam(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey teamPrimaryKey,
            EmployeeTeamUpdateDto employeeTeamUpdateDto) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        boolean modified = false;

        Employee employee = employeeService.getEmployee(employeePrimaryKey);
        Team team = teamService.getTeam(teamPrimaryKey);
        EmployeeTeam employeeTeam = employeeService.findEmployeeTeam(employee, team);

        if (employeeTeam != null) {
            if (employeeTeamUpdateDto.getIsFloating() != null) {
                employeeTeam.setIsFloating(employeeTeamUpdateDto.getIsFloating());
                modified = true;
            }
            if (employeeTeamUpdateDto.getIsHomeTeam() != null) {
                employeeTeam.setIsHomeTeam(employeeTeamUpdateDto.getIsHomeTeam());
                modified = true;
            }
            if (employeeTeamUpdateDto.getIsSchedulable() != null) {
                employeeTeam.setIsSchedulable(employeeTeamUpdateDto.getIsSchedulable());
                modified = true;
            }
            if (modified) {
                setUpdatedBy(employeeTeam);
                employeeTeam = employeeService.updateEmployeeTeam(employeeTeam);
            }
            return new EmployeeTeamDtoMapper().map(employeeTeam, EmployeeTeamViewDto.class);
        } else {
            throw new ValidationException("Can't update. EmployeeTeam wasn't found.");
        }
    }

    /**
     * Remove employee team.  Note that since there must always be a
     * home team, the removal will not be allowed if it would have
     * resulted in there being no home team designation.
     *
     * @param employeePrimaryKey
     * @param teamPrimaryKey
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    @Validation
    public void removeEmployeeTeam(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            PrimaryKey teamPrimaryKey)
            throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Employee employee = employeeService.getEmployee(employeePrimaryKey);
        Team team = teamService.getTeam(teamPrimaryKey);
        employeeService.removeEmployeeTeam(employee, team);
    }

    public ResultSetDto<ContractDTO> getContracts(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            String tenantId,
            String select, // select is NOT IMPLEMENTED FOR NOW ...
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
        Employee employee = employeeService.getEmployee(employeePrimaryKey);

        if (StringUtils.isEmpty(filter)) {
            filter = "employee.primaryKey.id=" + "'" + employee.getId() + "' ";
        } else {
            filter += ";employee.primaryKey.id=" + "'" + employee.getId() + "' ";
        }

        return contractFacade.getObjects(tenantId, select, filter, offset, limit, orderBy, orderDir);
    }

    /**
     * Get Site (Employee)
     *
     * @param primaryKey
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public SiteDto getSite(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class) PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Employee employee = employeeService.getEmployee(primaryKey);
        Site site = employee.getSite();
        SiteDto siteDto = null;
        if (site != null) {
            siteDto = toDto(site, SiteDto.class);
        }
        return siteDto;
    }

    /**
     * Getter for the absenceTypeService field
     *
     * @return
     */
    protected AbsenceTypeService getAbsenceTypeService() {
        return absenceTypeService;
    }

    /**
     * Getter for the employeeService field
     *
     * @return
     */
    protected EmployeeService getEmployeeService() {
        return employeeService;
    }

    /**
     * Getter for the cdAvailabilityTimeFrameService field
     *
     * @return
     */
    protected CDAvailabilityTimeFrameService getCDAvailabilityTimeFrameService() {
        return cdAvailabilityTimeFrameService;
    }

    /**
     * Get queried collection of CDAvailabilityTimeFrames
     *
     * @param tenantId
     * @param employeeId
     * @param select
     * @param filter
     * @param offset
     * @param limit
     * @param orderBy
     * @param orderDir
     * @param startDateTime
     * @param endDateTime
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public ResultSetDto<CDAvailabilityTimeFrameDto> getCDAvailabilityTimeFrames(
            String tenantId,
            String employeeId,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir,
            Long startDateTime,
            Long endDateTime) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQuery sq = new SimpleQuery(tenantId);
        sq.setSelect(select)
        	.setFilter(filter)
        	.setOffset(offset).setLimit(limit)
        	.setOrderByField(orderBy).setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC"))
        	.setTotalCount(true);
        
        sq.addFilter("employee.primaryKey.id=" + "'" + employeeId + "' ");
        if (startDateTime != null) {
            DateTime intervalStartDate = new DateTime(startDateTime);
        	sq.addFilter("startDateTime > '" + intervalStartDate + "' ");
        }
        if (endDateTime != null) {
            DateTime intervalStartDate = new DateTime(endDateTime);
        	sq.addFilter("startDateTime < '" + intervalStartDate + "' ");
        }

        ResultSet<CDAvailabilityTimeFrame> rs = cdAvailabilityTimeFrameService.findCDAvailabilityTimeFrames(sq);
        
        ResultSetDto<CDAvailabilityTimeFrameDto> dtoRs = new ResultSetDto<>( );
        
        Collection<CDAvailabilityTimeFrameDto> cdAvailabilityTimeFrameDtos = new ArrayList<>();
        
        CDAvailabilityTimeFrameDto cdDto;
        
        for (CDAvailabilityTimeFrame cdTimeFrame: rs.getResult()) {
        	cdDto = buildCDAvailTimeFrameDTO(cdTimeFrame);
        	cdAvailabilityTimeFrameDtos.add(cdDto);
        }
        
        dtoRs.setResult(cdAvailabilityTimeFrameDtos);
        dtoRs.setTotal(cdAvailabilityTimeFrameDtos.size());
        
        return dtoRs;
    }

    /**
     * Get specified CDAvailabilityTimeFrame
     *
     * @param primaryKey
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public CDAvailabilityTimeFrameDto getCDAvailabilityTimeFrame(
            @Validate(validator = EntityExistValidatorBean.class, type = CDAvailabilityTimeFrame.class)
            PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        CDAvailabilityTimeFrame cdAvailabilityTimeFrame = cdAvailabilityTimeFrameService.getCDAvailabilityTimeFrame(primaryKey);
        CDAvailabilityTimeFrameDto cdDto = buildCDAvailTimeFrameDTO(cdAvailabilityTimeFrame);
       
        return cdDto;
    }

	private CDAvailabilityTimeFrameDto buildCDAvailTimeFrameDTO(
			CDAvailabilityTimeFrame cdAvailabilityTimeFrame) {
		AbsenceType absenceType = cdAvailabilityTimeFrame.getAbsenceType();
        
        DtoMapper<CDAvailabilityTimeFrame, CDAvailabilityTimeFrameDto> cdDtoMapper = new DtoMapper<>();
        cdDtoMapper.registerExceptDtoFieldForMapping("absenceTypeDto");
        
        CDAvailabilityTimeFrameDto cdDto = cdDtoMapper.map(cdAvailabilityTimeFrame, CDAvailabilityTimeFrameDto.class);
        
        DtoMapper<AbsenceType, AbsenceTypeDto> abTypeDtoMapper = new DtoMapper<>();
        AbsenceTypeDto absenceTypeDto = abTypeDtoMapper.map(absenceType, AbsenceTypeDto.class);
        
        cdDto.setAbsenceTypeDto(absenceTypeDto);
		return cdDto;
	}


    /**
     * Update CDAvailabilityTimeFrame
     *
     * @param primaryKey
     * @param cdAvailabilityTimeFrameUpdateDTO
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public CDAvailabilityTimeFrameDto updateCDAvailabilityTimeFrame(
            @Validate(validator = EntityExistValidatorBean.class, type = CDAvailabilityTimeFrame.class)
            PrimaryKey primaryKey, CDAvailabilityTimeFrameUpdateDto cdAvailabilityTimeFrameUpdateDTO)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        boolean modified = false;
        CDAvailabilityTimeFrame cdAvailabilityTimeFrame = cdAvailabilityTimeFrameService.getCDAvailabilityTimeFrame(primaryKey);

        if (!StringUtils.isBlank(cdAvailabilityTimeFrameUpdateDTO.getReason())) {
            cdAvailabilityTimeFrame.setReason(cdAvailabilityTimeFrameUpdateDTO.getReason());
            modified = true;
        }

        if (!StringUtils.isBlank(cdAvailabilityTimeFrameUpdateDTO.getAbsenceTypeId())) {

            // Get the updated AbsenceType
            PrimaryKey absPrimaryKey = new PrimaryKey(cdAvailabilityTimeFrame.getPrimaryKey().getTenantId(),
                    cdAvailabilityTimeFrameUpdateDTO.getAbsenceTypeId());
            AbsenceType absenceType = absenceTypeService.getAbsenceType(absPrimaryKey);

            cdAvailabilityTimeFrame.setAbsenceType(absenceType);
            modified = true;
        }

        if (cdAvailabilityTimeFrameUpdateDTO.getAvailabilityType() != null) {
            cdAvailabilityTimeFrame.setAvailabilityType(cdAvailabilityTimeFrameUpdateDTO.getAvailabilityType());
            modified = true;
        }

        if (cdAvailabilityTimeFrameUpdateDTO.getStartDateTime() != null) {
            cdAvailabilityTimeFrame.setStartDateTime(new DateTime(cdAvailabilityTimeFrameUpdateDTO.getStartDateTime()));
            modified = true;
        }

        if (cdAvailabilityTimeFrameUpdateDTO.getStartDateTime() != null) {
            cdAvailabilityTimeFrame.setStartDateTime(new DateTime(cdAvailabilityTimeFrameUpdateDTO.getStartDateTime()));
            modified = true;
        }

        if (cdAvailabilityTimeFrameUpdateDTO.getDurationInMinutes() != null) {
            cdAvailabilityTimeFrame.setDurationInMinutes(Minutes.minutes(cdAvailabilityTimeFrameUpdateDTO.getDurationInMinutes()));
            modified = true;
        }

        if (cdAvailabilityTimeFrameUpdateDTO.getIsPTO() != null) {
            cdAvailabilityTimeFrame.setIsPTO(cdAvailabilityTimeFrameUpdateDTO.getIsPTO());
            modified = true;
        }

        if (modified) {
            setUpdatedBy(cdAvailabilityTimeFrame);
            cdAvailabilityTimeFrame = cdAvailabilityTimeFrameService.update(cdAvailabilityTimeFrame);
        }

		return new CDAvailabilityTimeFrameDtoMapper().map(cdAvailabilityTimeFrame, CDAvailabilityTimeFrameDto.class);
    }

    /**
     * Create CDAvailabilityTimeFrame
     *
     * @param primaryKey //todo:: primary key of what????
     * @param cdAvailabilityTimeFrameCreateDTO
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public CDAvailabilityTimeFrameDto createCDAvailabilityTimeFrame(
            @Validate(
                    validator = EntityExistValidatorBean.class,
                    type = CDAvailabilityTimeFrame.class,
                    expectedResult = false)
            PrimaryKey primaryKey,
            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = CDAvailabilityTimeFrameCreateDto.REASON, max = 255)   // (max 50 in legacy app)
                    }
            )
            CDAvailabilityTimeFrameCreateDto cdAvailabilityTimeFrameCreateDTO)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {

        // TODO Look into whether any of this logic can be moved down to the service layer.

        String tenantId = primaryKey.getTenantId();
        String employeeId = cdAvailabilityTimeFrameCreateDTO.getEmployeeId();

        Employee employee = employeeService.getEmployee(new PrimaryKey(tenantId, employeeId));

        if (employee == null) {
            throw new ValidationException("Cannot find the specified Employee");
        }

        AbsenceType absenceType = null;

        if (!StringUtils.isBlank(cdAvailabilityTimeFrameCreateDTO.getAbsenceTypeId())) {
            absenceType = absenceTypeService.getAbsenceType(new PrimaryKey(tenantId, cdAvailabilityTimeFrameCreateDTO.getAbsenceTypeId()));
        }

        DateTime startDateTime = new DateTime(cdAvailabilityTimeFrameCreateDTO.getStartDateTime());

        Minutes duration = Minutes.minutes(cdAvailabilityTimeFrameCreateDTO.getDurationInMinutes());

        AvailabilityType availabilityType = cdAvailabilityTimeFrameCreateDTO.getAvailabilityType();

        boolean isPTO = cdAvailabilityTimeFrameCreateDTO.getIsPTO();

        CDAvailabilityTimeFrame cdAvailabilityTimeFrame =
                cdAvailabilityTimeFrameService.createCDAvailabilityTimeFrame(primaryKey, employee, absenceType,
                        cdAvailabilityTimeFrameCreateDTO.getReason(),
                        duration, availabilityType, startDateTime, isPTO);
        setCreatedBy(cdAvailabilityTimeFrame);
        setOwnedBy(cdAvailabilityTimeFrame, null);

        cdAvailabilityTimeFrame = cdAvailabilityTimeFrameService.update(cdAvailabilityTimeFrame);

        return new CDAvailabilityTimeFrameDtoMapper().map(cdAvailabilityTimeFrame, CDAvailabilityTimeFrameDto.class);
    }

    /**
     * Delete CDAvailabilityTimeFrame
     *
     * @param primaryKey
     * @return
     */
    @Validation
    public boolean deleteCDAvailabilityTimeFrame(
            @Validate(validator = EntityExistValidatorBean.class, type = CDAvailabilityTimeFrame.class) PrimaryKey primaryKey) {
        CDAvailabilityTimeFrame cdAvailabilityTimeFrame = cdAvailabilityTimeFrameService.getCDAvailabilityTimeFrame(primaryKey);
        cdAvailabilityTimeFrameService.delete(cdAvailabilityTimeFrame);
        return true;
    }


    /**
     * Getter for the ciAvailabilityTimeFrameService field
     *
     * @return
     */
    protected CIAvailabilityTimeFrameService getCIAvailabilityTimeFrameService() {
        return ciAvailabilityTimeFrameService;
    }


    /**
     * Get queried collection of CIAvailabilityTimeFrames
     *
     * @param tenantId
     * @param employeeId
     * @param select
     * @param filter
     * @param offset
     * @param limit
     * @param orderBy
     * @param orderDir
     * @param startDateTime
     * @param endDateTime
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public ResultSetDto<CIAvailabilityTimeFrameDto> getCIAvailabilityTimeFrames(
            String tenantId,
            String employeeId,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir,
            Long startDateTime,
            Long endDateTime) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {

        SimpleQuery simpleQuery = new SimpleQuery(tenantId);

        StringBuilder filterBuilder = new StringBuilder();

        filterBuilder.append("employee.primaryKey.id=" + "'" + employeeId + "' ");

        if (startDateTime != null) {
            DateTime intervalStartDate = new DateTime(startDateTime);
            filterBuilder.append("And endDate > '" + intervalStartDate + "' ");
        }

        if (endDateTime != null) {
            DateTime intervalEndDate = new DateTime(endDateTime);
            filterBuilder.append("And startDate < '" + intervalEndDate + "' ");
        }

        filter = filterBuilder.toString();

        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true);
        ResultSet<CIAvailabilityTimeFrame> rs = ciAvailabilityTimeFrameService.findCIAvailabilityTimeFrames(simpleQuery);
        return toResultSetDto(rs, CIAvailabilityTimeFrameDto.class);
    }

    /**
     * Get specified CIAvailabilityTimeFrame
     *
     * @param primaryKey
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public CIAvailabilityTimeFrameDto getCIAvailabilityTimeFrame(
            @Validate(validator = EntityExistValidatorBean.class, type = CIAvailabilityTimeFrame.class)
            PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        CIAvailabilityTimeFrame ciAvailabilityTimeFrame = ciAvailabilityTimeFrameService.getCIAvailabilityTimeFrame(primaryKey);
        return toDto(ciAvailabilityTimeFrame, CIAvailabilityTimeFrameDto.class);
    }


    /**
     * Update CIAvailabilityTimeFrame
     *
     * @param primaryKey
     * @param ciAvailabilityTimeFrameUpdateDTO
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public CIAvailabilityTimeFrameDto updateCIAvailabilityTimeFrame(
            @Validate(validator = EntityExistValidatorBean.class, type = CIAvailabilityTimeFrame.class)
            PrimaryKey primaryKey, CIAvailabilityTimeFrameUpdateDto ciAvailabilityTimeFrameUpdateDTO)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        boolean modified = false;
        CIAvailabilityTimeFrame ciAvailabilityTimeFrame = ciAvailabilityTimeFrameService.getCIAvailabilityTimeFrame(primaryKey);

        if (!StringUtils.isBlank(ciAvailabilityTimeFrameUpdateDTO.getReason())) {
            ciAvailabilityTimeFrame.setReason(ciAvailabilityTimeFrameUpdateDTO.getReason());
            modified = true;
        }

        if (!StringUtils.isBlank(ciAvailabilityTimeFrameUpdateDTO.getAbsenceTypeId())) {
            // Get the updated AbsenceType
            PrimaryKey absPrimaryKey = new PrimaryKey(ciAvailabilityTimeFrame.getPrimaryKey().getTenantId(),
                    ciAvailabilityTimeFrameUpdateDTO.getAbsenceTypeId());
            AbsenceType absenceType = absenceTypeService.getAbsenceType(absPrimaryKey);

            ciAvailabilityTimeFrame.setAbsenceType(absenceType);
            modified = true;
        }

        if (ciAvailabilityTimeFrameUpdateDTO.getAvailabilityType() != null) {
            ciAvailabilityTimeFrame.setAvailabilityType(ciAvailabilityTimeFrameUpdateDTO.getAvailabilityType());
            modified = true;
        }

        if (ciAvailabilityTimeFrameUpdateDTO.getDurationInMinutes() != null) {
            ciAvailabilityTimeFrame.setDurationInMinutes(Minutes.minutes(ciAvailabilityTimeFrameUpdateDTO.getDurationInMinutes()));
            modified = true;
        }

        if (modified) {
            setUpdatedBy(ciAvailabilityTimeFrame);
            ciAvailabilityTimeFrame = ciAvailabilityTimeFrameService.update(ciAvailabilityTimeFrame);
        }

        return toDto(ciAvailabilityTimeFrame, CIAvailabilityTimeFrameDto.class);
    }

    /**
     * Create CIAvailabilityTimeFrame
     *
     * @param primaryKey
     * @param ciAvailabilityTimeFrameCreateDTO
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public CIAvailabilityTimeFrameDto createCIAvailabilityTimeFrame(
            @Validate(validator = EntityExistValidatorBean.class, type = CIAvailabilityTimeFrame.class, expectedResult = false)
            PrimaryKey primaryKey,
            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = CIAvailabilityTimeFrameCreateDto.REASON, max = 255)  // (max 50 in legacy app)
                    }
            )
            CIAvailabilityTimeFrameCreateDto ciAvailabilityTimeFrameCreateDTO)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // TODO Look into whether any of this logic can be moved down to the service layer.

        String tenantId = primaryKey.getTenantId();
        String employeeId = ciAvailabilityTimeFrameCreateDTO.getEmployeeId();

        Employee employee = employeeService.getEmployee(new PrimaryKey(tenantId, employeeId));

        AbsenceType absenceType = null;

        if (!StringUtils.isBlank(ciAvailabilityTimeFrameCreateDTO.getAbsenceTypeId())) {
            absenceType = absenceTypeService.getAbsenceType(new PrimaryKey(tenantId, ciAvailabilityTimeFrameCreateDTO.getAbsenceTypeId()));
        }

        LocalTime startTime = LocalTime.fromMillisOfDay(ciAvailabilityTimeFrameCreateDTO.getStartTime());

        Minutes duration = Minutes.minutes(ciAvailabilityTimeFrameCreateDTO.getDurationInMinutes());

        AvailabilityType availabilityType = ciAvailabilityTimeFrameCreateDTO.getAvailabilityType();

        DayOfWeek dayOfTheWeek = ciAvailabilityTimeFrameCreateDTO.getDayOfTheWeek();

        DateTime startDateTime = new DateTime(ciAvailabilityTimeFrameCreateDTO.getStartDateTime());
        
        // allow null endDateTime
        DateTime endDateTime;

        if (ciAvailabilityTimeFrameCreateDTO.getEndDateTime() != 0) {
        	endDateTime = new DateTime(ciAvailabilityTimeFrameCreateDTO.getEndDateTime());
        } else {
        	endDateTime = null;
        }
        
        CIAvailabilityTimeFrame ciAvailabilityTimeFrame =
                ciAvailabilityTimeFrameService.createCIAvailabilityTimeFrame(primaryKey, employee, absenceType,
                        ciAvailabilityTimeFrameCreateDTO.getReason(),
                        startTime, duration, availabilityType, dayOfTheWeek, startDateTime, endDateTime);
        setCreatedBy(ciAvailabilityTimeFrame);
        setOwnedBy(ciAvailabilityTimeFrame, null);

        ciAvailabilityTimeFrame = ciAvailabilityTimeFrameService.update(ciAvailabilityTimeFrame);

        return toDto(ciAvailabilityTimeFrame, CIAvailabilityTimeFrameDto.class);
    }

    /**
     * Delete CIAvailabilityTimeFrame
     *
     * @param primaryKey
     * @return
     */
    @Validation
    public boolean deleteCIAvailabilityTimeFrame(
            @Validate(validator = EntityExistValidatorBean.class, type = CIAvailabilityTimeFrame.class)
            PrimaryKey primaryKey) {
        CIAvailabilityTimeFrame ciAvailabilityTimeFrame = ciAvailabilityTimeFrameService.getCIAvailabilityTimeFrame(primaryKey);
        ciAvailabilityTimeFrameService.delete(ciAvailabilityTimeFrame);
        return true;
    }

    @Validation
    public Collection<Object> quickSearch(String tenantId,
                                          String searchValue,
                                          String searchFields,
                                          String returnedFields,
                                          int limit,
                                          @ValidatePaging(name = Constants.ORDER_BY)
                                          String orderBy,
                                          @ValidatePaging(name = Constants.ORDER_DIR)
                                          String orderDir,
                                          AccountACL acl) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        return employeeService.quickSearch(tenantId, searchValue, searchFields, returnedFields, limit, orderBy,
                orderDir, acl);
    }

    @Validation
    public ResultSetDto<EmployeeJoinsDto> query(String tenantId,
                                                String teamIds,
                                                String searchValue,
                                                String searchFields,
                                                String skillFilter,
                                                String skillOwnershipFilter,
                                                String teamFilter,
                                                String teamMembershipFilter,
                                                String accountFilter,
                                                String activityTypeFilter,
                                                String employeeNameFilter,
                                                EmployeeTeamBelonging belonging,
                                                int offset,
                                                int limit,
                                                @ValidatePaging(name = Constants.ORDER_BY)
                                                String orderBy,
                                                @ValidatePaging(name = Constants.ORDER_DIR)
                                                String orderDir,
                                                AccountACL acl) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        ResultSet<Object[]> employeeResultSet = employeeService.query(tenantId, teamIds, searchValue, searchFields,
                skillFilter, skillOwnershipFilter, teamFilter, teamMembershipFilter, accountFilter, activityTypeFilter,
                employeeNameFilter, belonging, offset, limit, orderBy, orderDir, acl);

        List<EmployeeJoinsDto> joinsDtos = new ArrayList<>();
        for (Object[] employee : employeeResultSet.getResult()) {
            EmployeeJoinsDto joinsDto = new EmployeeJoinsDto();
            joinsDto.setId((String) employee[0]);
            joinsDto.setFirstName((String) employee[1]);
            joinsDto.setLastName((String) employee[2]);
            joinsDto.setPrimaryJobRole((String) employee[3]);
            joinsDto.setActivityType(EmployeeActivityType.values()[(int) employee[4]].name());
            joinsDto.setHomeSite((String) employee[5]);
            joinsDto.setHomeTeam((String) employee[6]);
            joinsDto.setHireDate(employee[7] == null ? null : ((Date) employee[7]).getTime());
            joinsDto.setWorkEmail((String) employee[8]);
            joinsDto.setMobilePhone((String) employee[9]);

            joinsDto.setPrimaryShift(null); // TODO not implemented yet

            joinsDtos.add(joinsDto);
        }

        ResultSetDto<EmployeeJoinsDto> result = new ResultSetDto<>();

        result.setResult(joinsDtos);
        result.setTotal(employeeResultSet.getTotal());

        return result;
    }

    @Validation
    public Collection<String> managers(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey) {
        return employeeService.managers(employeePrimaryKey, null);
    }

    @Validation
    public Collection<String> managerAccountIds(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey) {
        return employeeService.managerAccountIds(employeePrimaryKey, null);
    }

    @Validation
    public EmployeeInfoDto getEmployeeInfo(@Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
                                           PrimaryKey  employeePk)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Employee employee = employeeService.getEmployee(employeePk);
        EmployeeInfoDto employeeAvailabilityInfoDto = employeeService.getEmployeeInfo(employee);

        employeeAvailabilityInfoDto.setEmployeeDto(toDto(employee, EmployeeDto.class));
        employeeAvailabilityInfoDto.setCalendarSyncUrl("/calendar/" + employee.getCalendarSyncId());
        return employeeAvailabilityInfoDto;
    }

    /**
     * @param employeePk
     * @param start
     * @param end
     * @param status
     * @param timeZone   NOTE That TZ is used to compute the count of OpenShifts by Day in client TZ,
     *                   days are returned in UTC but directly in client TZ.
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    @Validation
    public EmployeeAvailabilityDto getEmployeeAvailability(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePk,
            Long start,
            Long end,
            String status,
            DateTimeZone timeZone)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Employee employee = employeeService.getEmployee(employeePk);
        if (timeZone == null) {
        	timeZone = accountUtilService.getActualTimeZone(employee); 
        }
        
        return getEmployeeAvailability(employee, start, end, status, timeZone);
    }
    
    private EmployeeAvailabilityDto getEmployeeAvailability(Employee employee, Long start, Long end, String status,
    		DateTimeZone timeZone) {
        ScheduleStatus scheduleStatus;
        try {
            scheduleStatus = valueOf(status);
        } catch (Exception error) {
            throw new ValidationException(getMessage("validation.schedule.status.invalid", status));
        }

    	String employeeId = employee.getId();
    	String tenantId = employee.getTenantId();

        EmployeeAvailabilityDto employeeAvailabilityInfoDto = null;
        DateTime now = new DateTime();
        long cacheStartBound = now.minusMonths(EMP_AVAILABILITY_CACHE_MONTHS).getMillis();
        long cacheEndBound = now.plusMonths(EMP_AVAILABILITY_CACHE_MONTHS).getMillis();
        Boolean startDateOption = start >= cacheStartBound && end <= cacheEndBound;
        if (startDateOption) {
            employeeAvailabilityInfoDto = (EmployeeAvailabilityDto) cacheService.getEntry(
                    EMP_AVAILABILITY_CACHE, tenantId, employeeId);
        }
        if (employeeAvailabilityInfoDto == null) {
            employeeAvailabilityInfoDto = employeeService.getEmployeeAvailability(employee, scheduleStatus,
                    Math.min(start, cacheStartBound), Math.max(end, cacheEndBound));
            cacheService.putEntry(EMP_AVAILABILITY_CACHE, tenantId, employeeId, employeeAvailabilityInfoDto);
        }

        Collection<EmployeeAvailabilityDto.EmployeeUnavailabilityDto> employeeUnavailabilitiesToRemove =
                findToRemove(employeeAvailabilityInfoDto.getEmpUnavailabilities(), start, end);
        employeeAvailabilityInfoDto.getEmpUnavailabilities().removeAll(employeeUnavailabilitiesToRemove);

        Collection<EmployeeAvailabilityDto.OrgHolidayDto> orgHolidaysToRemove =
                findToRemove(employeeAvailabilityInfoDto.getOrgHolidays(), start, end);
        employeeAvailabilityInfoDto.getOrgHolidays().removeAll(orgHolidaysToRemove);

        Collection<EmployeeAvailabilityDto.ScheduleDto> schedulesToRemove =
                findToRemove(employeeAvailabilityInfoDto.getSchedules(), start, end);
        employeeAvailabilityInfoDto.getSchedules().removeAll(schedulesToRemove);

        // get Employee OpenShifts
        // TODO, there are optimization that can be done to skip getting openshifts (mostly based on dates)
        Collection<PostedOpenShift> openShifts = new ArrayList<>();
        if (scheduleStatus == ScheduleStatus.Posted) {
            openShifts = postedOpenShiftService.getPostedOpenShiftsOfEmployee(employee.getTenantId(), employeeId, start,
                    end);
            openShifts = postedOpenShiftsDeduplication(openShifts);
        }
        // convert list of OpenShifts into an aggregrated result of Nb of OpenShift per Day in client TZ
        // TODO wrap this into convenience method
        // TODO  use client TZ
        Map<String, Integer> openShiftPerDay = getOpenShiftsByDay(openShifts, timeZone);
        employeeAvailabilityInfoDto.setOpenShiftsByDays(openShiftPerDay);

        return employeeAvailabilityInfoDto;
    }

    @Validation
    public <T extends EmployeeAvailabilityAndShiftsDto> T getCalendarView(
            boolean requestInfo,
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePk,
            Long startDate,
            Long endDate,
            String status,
            String timeZoneAsStr,
            String returnedFields,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir,
            Class<T> clazz)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (startDate == null || endDate == null) {
            throw new ValidationException(getMessage("validation.error.not.enough.params"));
        }

        Employee employee = employeeService.getEmployee(employeePk);

        // override timezone with employee actual TZ
        DateTimeZone timeZone = accountUtilService.getActualTimeZone(employee);
        EmployeeAvailabilityDto employeeAvailabilityDto =
                getEmployeeAvailability(employee, startDate, endDate, status, timeZone);

        if (employeeAvailabilityDto.getEmpUnavailabilities() != null) {
            Iterator<EmployeeAvailabilityDto.EmployeeUnavailabilityDto> iterator =
                    employeeAvailabilityDto.getEmpUnavailabilities().iterator();
            while (iterator.hasNext()) {
                if (!iterator.next().isPto()) {
                    iterator.remove();
                }
            }
        }

        T result = clazz.newInstance();
        result.setEmpUnavailabilities(employeeAvailabilityDto.getEmpUnavailabilities());
        result.setOpenShiftsByDays(employeeAvailabilityDto.getOpenShiftsByDays());
        result.setOrgHolidays(employeeAvailabilityDto.getOrgHolidays());
        result.setSchedules(employeeAvailabilityDto.getSchedules());

        ResultSetDto<Object[]> shifts = shiftFacade.getObjects(employeePk.getId(), startDate, endDate,
                timeZone.toString(), status, returnedFields, offset, limit, orderBy, orderDir);

        result.setShifts(shifts);

        if (requestInfo) {
            Map<String, List<?>> wipSwapMap = peerWipSwapRequestInfos(employeePk, startDate, endDate);

            result.setPeerSwapRequestInfos(
                    (List<EmployeeAvailabilityAndShiftsDto.PeerSwapRequestInfo>) wipSwapMap.get("SHIFT_SWAP_REQUEST"));
            result.setPeerWipRequestInfos(
                    (List<EmployeeAvailabilityAndShiftsDto.PeerWipRequestInfo>) wipSwapMap.get("WIP_REQUEST"));

            Map<String, List<?>> openShiftsMap = submittedOpenShiftsRequestInfos(employeePk, startDate, endDate);

            result.setSubmittedWipSwapRequestInfos(
                    (List<EmployeeAvailabilityAndShiftsDto.SubmittedWipSwapRequestInfo>) openShiftsMap.get("SUBMITTED"));
            result.setSubmittedTimeOffRequestInfos(
                    (List<EmployeeAvailabilityAndShiftsDto.SubmittedTimeOffRequestInfo>) openShiftsMap.get("TIME_OFF"));
            result.setSubmittedOsRequestInfos(
                    (List<EmployeeAvailabilityAndShiftsDto.SubmittedOsRequestInfo>) openShiftsMap.get("OPEN_SHIFTS"));
        }

        return result;
    }

    @Validation
    public EmployeeCalendarAvailabilityDto getCalendarAvailabilityView(
            boolean requestInfo,
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePk,
            Long startDate,
            Long endDate,
            String status,
            String timeZone,
            String returnedFields,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        EmployeeCalendarAvailabilityDto result = getCalendarView(requestInfo, employeePk, startDate, endDate, status,
                timeZone, returnedFields, offset, limit, orderBy, orderDir, EmployeeCalendarAvailabilityDto.class);

        // Note that the getAvailcalView method expects both firstCalendarDate and lastCalendarDate to represent
        // DATES ONLY, meaning each should be the millis instant (UTC) that represents the site-specific start 
        // of each date (midnight in site timezone).  So let's calculate to make sure that's what we send...
        DateTimeZone siteTimeZone = employeeService.getEmployee(employeePk).getSite().getTimeZone();
        DateTime startDateInTZ = new DateTime(startDate, siteTimeZone);
        DateTime beginningOfStartDateInTZ = startDateInTZ.withTimeAtStartOfDay();
        Long firstCalendarDate = beginningOfStartDateInTZ.toInstant().getMillis();
        
        DateTime endDateInTZ = new DateTime(endDate, siteTimeZone);        
        DateTime beginningOfEndDateInTZ = endDateInTZ.withTimeAtStartOfDay();
        Long lastCalendarDate = beginningOfEndDateInTZ.toInstant().getMillis();        

        AvailcalViewDto availcalViewDto = getAvailcalView(employeePk, firstCalendarDate, lastCalendarDate);
        result.setAvailcalViewDto(availcalViewDto);

        return result;
    }

    public Map<String, List<?>> peerWipSwapRequestInfos(PrimaryKey employeePk, Long startDate, Long endDate) {
        Map<String, List<?>> result = new HashMap<>();

        List<EmployeeAvailabilityAndShiftsDto.PeerSwapRequestInfo> swapList = new ArrayList<>();
        List<EmployeeAvailabilityAndShiftsDto.PeerWipRequestInfo> wipList = new ArrayList<>();

        result.put("SHIFT_SWAP_REQUEST", swapList);
        result.put("WIP_REQUEST", wipList);

        Collection<Object[]> rows = employeeService.getPeerWipSwapRequestInfo(employeePk, startDate, endDate);

        for (Object[] row : rows) {
            if ("SHIFT_SWAP_REQUEST".equals(row[6])) {
                EmployeeAvailabilityAndShiftsDto.PeerSwapRequestInfo swapRequestInfo =
                        new EmployeeAvailabilityAndShiftsDto.PeerSwapRequestInfo();

                swapRequestInfo.setRequestId(String.valueOf(row[0]));
                swapRequestInfo.setPeerStatus((String) row[1]);
                swapRequestInfo.setPeerShiftId((String) row[2]);
                swapRequestInfo.setSubmitterShiftId((String) row[3]);
                swapRequestInfo.setSubmitterShiftStartDateTime(row[4] == null ? 0 : ((Timestamp) row[4]).getTime());
                swapRequestInfo.setSubmitterShiftEndDateTime(row[5] == null ? 0 : ((Timestamp) row[5]).getTime());

                swapList.add(swapRequestInfo);
            } else if ("WIP_REQUEST".equals(row[6])) {
                EmployeeAvailabilityAndShiftsDto.PeerWipRequestInfo wipRequestInfo =
                        new EmployeeAvailabilityAndShiftsDto.PeerWipRequestInfo();

                wipRequestInfo.setRequestId(String.valueOf(row[0]));
                wipRequestInfo.setPeerStatus((String) row[1]);
                wipRequestInfo.setSubmitterShiftId((String) row[3]);
                wipRequestInfo.setSubmitterShiftStartDateTime(row[4] == null ? 0 : ((Timestamp) row[4]).getTime());
                wipRequestInfo.setSubmitterShiftEndDateTime(row[5] == null ? 0 : ((Timestamp) row[5]).getTime());

                wipList.add(wipRequestInfo);
            }
        }

        return result;
    }

    public Map<String, List<?>> submittedOpenShiftsRequestInfos(PrimaryKey employeePk, Long startDate, Long endDate) {
        Map<String, List<?>> result = new HashMap<>();

        List<EmployeeAvailabilityAndShiftsDto.SubmittedOsRequestInfo> openShiftsRequestInfos = new ArrayList<>();
        List<EmployeeAvailabilityAndShiftsDto.SubmittedWipSwapRequestInfo> submittedWipSwapRequestInfos = new ArrayList<>();
        List<EmployeeAvailabilityAndShiftsDto.SubmittedTimeOffRequestInfo> timeOffRequestInfos = new ArrayList<>();

        result.put("OPEN_SHIFTS", openShiftsRequestInfos);
        result.put("SUBMITTED", submittedWipSwapRequestInfos);
        result.put("TIME_OFF", timeOffRequestInfos);

        Collection<Object[]> rows = employeeService.getSubmittedOpenShiftsRequestInfo(employeePk, startDate, endDate);
        
        if (rows!=null){
	        for (Object[] row : rows) {
	            if ("SHIFT_SWAP_REQUEST".equals(row[1]) || "WIP_REQUEST".equals(row[1])) {
	                EmployeeAvailabilityAndShiftsDto.SubmittedWipSwapRequestInfo submittedWipSwapRequestInfo =
	                        new EmployeeAvailabilityAndShiftsDto.SubmittedWipSwapRequestInfo();
	
	                submittedWipSwapRequestInfo.setRequestId(String.valueOf(row[0]));
	                submittedWipSwapRequestInfo.setRequestType((String) row[1]);
	                submittedWipSwapRequestInfo.setRequestStatus((String) row[2]);
	                submittedWipSwapRequestInfo.setShiftId((String) row[3]);
	
	                submittedWipSwapRequestInfos.add(submittedWipSwapRequestInfo);
	            } else if ("TIME_OFF_REQUEST".equals(row[1])) {
	                EmployeeAvailabilityAndShiftsDto.SubmittedTimeOffRequestInfo submittedTimeOffRequestInfo =
	                        new EmployeeAvailabilityAndShiftsDto.SubmittedTimeOffRequestInfo();
	
	                submittedTimeOffRequestInfo.setRequestId(String.valueOf(row[0]));
	                submittedTimeOffRequestInfo.setRequestType((String) row[1]);
	                submittedTimeOffRequestInfo.setRequestStatus((String) row[2]);
	                submittedTimeOffRequestInfo.setRequestDate(row[4] == null ? 0 : ((Timestamp) row[4]).getTime());
	
	                timeOffRequestInfos.add(submittedTimeOffRequestInfo);
	            } else if ("OPEN_SHIFT_REQUEST".equals(row[1])) {
	                EmployeeAvailabilityAndShiftsDto.SubmittedOsRequestInfo openShiftsRequestInfo =
	                        new EmployeeAvailabilityAndShiftsDto.SubmittedOsRequestInfo();
	
	                openShiftsRequestInfo.setRequestId(String.valueOf(row[0]));
	                openShiftsRequestInfo.setRequestType((String) row[1]);
	                openShiftsRequestInfo.setRequestStatus((String) row[2]);
	                openShiftsRequestInfo.setShiftId((String) row[3]);
	                openShiftsRequestInfo.setRequestDate(row[4] == null ? 0 : ((Timestamp) row[4]).getTime());
	
	                openShiftsRequestInfos.add(openShiftsRequestInfo);
	            }
	        }
        }

        return result;
    }

    private Map<String, Integer> getOpenShiftsByDay(Collection<PostedOpenShift> openShifts, DateTimeZone timeZone) {
        Map<String, Integer> openShiftPerDay = new HashMap<>();
        if (openShifts != null) {
	        for (PostedOpenShift openShift : openShifts) {
	            DateTime day = getDay(new DateTime(openShift.getStartDateTime()), timeZone);
	            DateTime nextDay = day.plusDays(1);
	            String key = day.getMillis() + "-" + nextDay.getMillis();            
	            Integer count = openShiftPerDay.get(key);
	            count = (count == null ? 1 : 1 + count);
	            openShiftPerDay.put(key, count);
	        }
        }
        return openShiftPerDay;
    	
    }
    
    private DateTime getDay(DateTime datetime, DateTimeZone tz) {
        return new DateTime(datetime.getYear(), datetime.getMonthOfYear(), datetime.getDayOfMonth(), 0, 0, tz);
    }

    @Validation
    public Collection<EmployeeOpenShiftDto> getEmployeeOpenShifts(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePk,
            Long startDate,
            Long endDate)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Employee employee = employeeService.getEmployee(employeePk);
        Collection<PostedOpenShift> openShifts = postedOpenShiftService.getPostedOpenShiftsOfEmployee(
                employee.getTenantId(), employee.getId(), startDate, endDate);

        openShifts = postedOpenShiftsDeduplication(openShifts);

        return toCollectionDto(openShifts, EmployeeOpenShiftDto.class);
    }

    /**
     * Fire off execution of WIP eligibility for ASYNCHRONOUS processing.
     *
     * @param shiftPk
     * @param employeeId
     * @param maxComputationTime
     * @param maxUnimprovedSecondsSpent
     * @param includeDetails
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public WipEligibleTeammatesDto requestWipEligibleEmployeesAsynchronously(
            @Validate(validator = EntityExistValidatorBean.class, type = Shift.class) PrimaryKey shiftPk,
            String employeeId, Integer maxComputationTime, Integer maxUnimprovedSecondsSpent, Boolean includeDetails)
            throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        String tenantId = sessionService.getTenantId();
        Employee employee;
        if (employeeId == null) {
            String userId = sessionService.getUserId();
            PrimaryKey userAccountPk = new PrimaryKey(tenantId, userId);
            UserAccount userAccount = userAccountService.getUserAccount(userAccountPk);
            employee = userAccount.getEmployee();
            employeeId = employee.getId();
        } else {
            PrimaryKey employeePk = new PrimaryKey(tenantId, employeeId);
            employee = employeeService.getEmployee(employeePk);
        }

        // Validate employee exists...
        if (employee == null) {
            throw new ValidationException(getMessage("validation.schedule.shift.noemployee", employeeId));
        }

        Shift shift = shiftService.getShift(shiftPk);
        if (!shift.getEmployeeId().equals(employee.getId())) {
            throw new ValidationException(getMessage("validation.employee.shiftnotassigned", employee.getId(),
                    shift.getId()));
        }

        List<Shift> shifts = new ArrayList<>();
        shifts.add(shift);  // just this one shift

        // Engine should only consider like-skilled teammates of employee seeking WIP...
        List<Employee> employees = new ArrayList<>();
        PrimaryKey teamPk = new PrimaryKey(shift.getTenantId(), shift.getTeamId());
        PrimaryKey skillPk = new PrimaryKey(shift.getTenantId(), shift.getSkillId());
        List<String> teamMemberIds = employeeService.getLikeSkilledTeammates(teamPk, skillPk);
        
        for (String teamMemberId : teamMemberIds) {
            if (!teamMemberId.equals(employee.getId())) {
                employees.add(employeeService.getEmployee(new PrimaryKey(shift.getTenantId(), teamMemberId)));
            }
        }

        PrimaryKey schedulePk = new PrimaryKey(shiftPk.getTenantId(), shift.getScheduleId());
        Schedule schedule = scheduleService.getSchedule(schedulePk);

        String requestId = scheduleService.executeOpenShiftEligibility(schedule, employees, shifts,
                maxUnimprovedSecondsSpent, maxUnimprovedSecondsSpent, includeDetails);

        return getWipEligibleEmployeesRequestStatus(requestId);
    }

    /**
     * Fire off execution of WIP eligibility for SYNCHRONOUS processing.
     *
     * @param shiftPk
     * @param employeeId
     * @param maxComputationTime
     * @param maxUnimprovedSecondsSpent
     * @param maxSynchronousWaitSeconds
     * @param includeDetails
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public WipEligibleTeammatesDto requestWipEligibleEmployeesSynchronously(
            @Validate(validator = EntityExistValidatorBean.class, type = Shift.class) PrimaryKey shiftPk,
            String employeeId, Integer maxComputationTime, Integer maxUnimprovedSecondsSpent,
            Integer maxSynchronousWaitSeconds, Boolean includeDetails)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (maxSynchronousWaitSeconds < 1) {
            throw new ValidationException(getMessage("validation.schedule.qualificationrequest.invalidsynchronouswait"));
        }

        String tenantId = sessionService.getTenantId();
        Employee employee;
        if (employeeId == null) {
            String userId = sessionService.getUserId();
            PrimaryKey userAccountPk = new PrimaryKey(tenantId, userId);
            UserAccount userAccount = userAccountService.getUserAccount(userAccountPk);
            employee = userAccount.getEmployee();
            employeeId = employee.getId();
        } else {
            PrimaryKey employeePk = new PrimaryKey(tenantId, employeeId);
            employee = employeeService.getEmployee(employeePk);
        }

        // Validate employee exists...
        if (employee == null) {
            throw new ValidationException(getMessage("validation.schedule.shift.noemployee", employeeId));
        }

        Shift shift = shiftService.getShift(shiftPk);
        if (!shift.getEmployeeId().equals(employee.getId())) {
            throw new ValidationException(getMessage("validation.employee.shiftnotassigned", employee.getId(), shift.getId()));
        }

        PrimaryKey schedulePk = scheduleService.getSchedulePkFromShiftPk(shiftPk);

        // Engine should only consider like-skilled teammates of employee seeking WIP...
        List<String> employeeIds = new ArrayList<String>();
        PrimaryKey teamPk = new PrimaryKey(shift.getTenantId(), shift.getTeamId());
        PrimaryKey skillPk = new PrimaryKey(shift.getTenantId(), shift.getSkillId());
        List<String> teamMemberIds = employeeService.getLikeSkilledTeammates(teamPk, skillPk);
       
        for (String teamMemberId : teamMemberIds) {
            if (!teamMemberId.equals(employee.getId())) {
                employeeIds.add(teamMemberId);
            }
        }

        List<String> shiftIds = new ArrayList<>();
        shiftIds.add(shift.getId());

        WipEligibleTeammatesDto returnDto = new WipEligibleTeammatesDto();

        if (employeeIds.size() == 0){
        	// There are no like-skilled teammates, so nobody eligible to WIP.
        	// THerefore no need to call upon engine, so let's leave the eligibleTeammaets Set empty and ...
            returnDto.setState(TaskState.Complete);  // treat request as complete.
            returnDto.setRequestId("");  // empty request ID will do
        } else {
            QualificationRequestTracker requestTracker = scheduleService.getOpenShiftEligibility(schedulePk,
                    employeeIds, shiftIds, maxUnimprovedSecondsSpent, maxUnimprovedSecondsSpent,
                    maxSynchronousWaitSeconds, includeDetails, null, null);

            // TODO Fix the following. The requestTracker state not kept up to date!
            // returnDto.setState( requestTracker.getState() );
            returnDto.setState(TaskState.Complete);  // TODO Temporarily forcing correct state assignment (see above).
            returnDto.setRequestId(requestTracker.getRequestId());

            Collection<ShiftQualificationDto> resultingShiftQuals = requestTracker.getQualificationShifts();
            if (resultingShiftQuals!=null){
	            for (ShiftQualificationDto resultingShiftQual : resultingShiftQuals) {
	                if (resultingShiftQual.getIsAccepted()) {
	                    WipEligibleTeammatesDto.TeammateDescriptorDto teammate =
                                new WipEligibleTeammatesDto.TeammateDescriptorDto();
	                    teammate.employeeId = resultingShiftQual.getEmployeeId();
	                    teammate.employeeName = resultingShiftQual.getEmployeeName();
	                    returnDto.getEligibleTeammates().add(teammate);
	                }
	            }    
            }
        }
        
        return returnDto;
    }

    @Validation
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public WipEligibleTeammatesDto getWipEligibleEmployeesRequestStatus(String requestId) {
        WipEligibleTeammatesDto returnDto = new WipEligibleTeammatesDto();
        QualificationRequestTracker requestTracker = scheduleService.getQualificationRequestTracker(requestId);
        if (requestTracker != null) {
            TaskState state = requestTracker.getState();
            returnDto.setState(state);
            returnDto.setRequestId(requestId);
            if (state.equals(TaskState.Complete)) { // in case it already completed
                Collection<ShiftQualificationDto> resultingShiftQuals = requestTracker.getQualificationShifts();
                
                if (resultingShiftQuals!=null){
	                for (ShiftQualificationDto resultingShiftQual : resultingShiftQuals) {
	                    if (resultingShiftQual.getIsAccepted()) {
	                        WipEligibleTeammatesDto.TeammateDescriptorDto teammate =
	                                new WipEligibleTeammatesDto.TeammateDescriptorDto();
	                        teammate.employeeId = resultingShiftQual.getEmployeeId();
	                        teammate.employeeName = resultingShiftQual.getEmployeeName();
	                        returnDto.getEligibleTeammates().add(teammate);
	                    }
	                }
                }
            }
            return returnDto;
        } else {
            throw new ValidationException(getMessage("validation.schedule.qualificationrequest.noqualificationrequest",
                    requestId));
        }
    }

    @Validation
    public SwapEligibleShiftsDto getSwapEligibleShiftsRequestStatus(String requestId, String shiftId) {
        SwapEligibleShiftsDto returnDto = new SwapEligibleShiftsDto();
        ShiftSwapEligibilityRequestTracker requestTracker =
                scheduleService.getShiftSwapEligibilityRequestTracker(requestId);
        if (requestTracker != null) {
            TaskState state = requestTracker.getState();
            returnDto.setRequestState(state);
            returnDto.setRequestId(requestId);

            if (state.equals(TaskState.Complete)) { // in case it already completed
                Map<String, Collection<ShiftQualificationDto>> resultingShiftQualsMap =
                        requestTracker.getQualificationShifts();
                Collection<ShiftQualificationDto> resultingShiftQuals = resultingShiftQualsMap.get(shiftId);
                String tenantId = sessionService.getTenantId();
                if (resultingShiftQuals!=null){
	                for (ShiftQualificationDto resultingShiftQual : resultingShiftQuals) {
	                    if (resultingShiftQual.getIsAccepted()) {
	                        SwapEligibleShiftsDto.SwappableShiftDescriptor swappableShift =
                                    new SwapEligibleShiftsDto.SwappableShiftDescriptor();
	                        swappableShift.employeeId = resultingShiftQual.getEmployeeId();
	                        swappableShift.employeeName = resultingShiftQual.getEmployeeName();
	                        swappableShift.shiftId = resultingShiftQual.getShiftId();
	
	                        Shift acceptedShift = shiftService.getShift(new PrimaryKey(tenantId,
                                    resultingShiftQual.getShiftId()));
	                        if (acceptedShift != null) {
	                            swappableShift.startDateTime = acceptedShift.getStartDateTime();
	                            swappableShift.endDateTime = acceptedShift.getEndDateTime();
	                            swappableShift.skillName = acceptedShift.getSkillName();
	                            swappableShift.teamName = acceptedShift.getTeamName();
	                            returnDto.getSwappableShifts().add(swappableShift);
	                        }
	                    }
	                }
                }
            }
            return returnDto;
        } else {
            throw new ValidationException(getMessage("validation.schedule.qualificationrequest.noqualificationrequest",
                    requestId));
        }
    }

    /**
     * Fire off execution of swap eligibility for ASYNCHRONOUS processing.
     *
     * @param swapSeekingShiftPk
     * @param employeeId
     * @param maxComputationTime
     * @param maxUnimprovedSecondsSpent
     * @param includeDetails
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public SwapEligibleShiftsDto requestSwapEligibleShiftsAsynchronously(
            @Validate(validator = EntityExistValidatorBean.class, type = Shift.class) PrimaryKey swapSeekingShiftPk,
            String employeeId, Integer maxComputationTime, Integer maxUnimprovedSecondsSpent, Boolean includeDetails)
            throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {

        String tenantId = sessionService.getTenantId();
        Employee employee;
        if (employeeId == null) {
            String userId = sessionService.getUserId();
            PrimaryKey userAccountPk = new PrimaryKey(tenantId, userId);
            UserAccount userAccount = userAccountService.getUserAccount(userAccountPk);
            employee = userAccount.getEmployee();
            employeeId = employee.getId();
        } else {
            PrimaryKey employeePk = new PrimaryKey(tenantId, employeeId);
            employee = employeeService.getEmployee(employeePk);
        }

        // Validate employee exists...
        if (employee == null) {
            throw new ValidationException(getMessage("validation.schedule.shift.noemployee", employeeId));
        }

        Shift swapSeekingShift = shiftService.getShift(swapSeekingShiftPk);
        if (!swapSeekingShift.getEmployeeId().equals(employee.getId())) {
            throw new ValidationException(getMessage("validation.employee.shiftnotassigned", employee.getId(), swapSeekingShift.getId()));
        }

        List<Shift> swapSeekingShifts = new ArrayList<>();
        swapSeekingShifts.add(swapSeekingShift);  // just this one shift

        PrimaryKey schedulePk = new PrimaryKey(swapSeekingShiftPk.getTenantId(), swapSeekingShift.getScheduleId());
        Schedule schedule = scheduleService.getSchedule(schedulePk);

        String requestId = scheduleService.executeShiftSwapEligibility(schedule, swapSeekingShifts, null, maxUnimprovedSecondsSpent,
                maxUnimprovedSecondsSpent, includeDetails);

        return getSwapEligibleShiftsRequestStatus(requestId, swapSeekingShift.getId());
    }

    /**
     * Fire off execution of swap eligibility for SYNCHRONOUS processing.
     *
     * @param shiftPk
     * @param employeeId
     * @param maxComputationTime
     * @param maxUnimprovedSecondsSpent
     * @param maxSynchronousWaitSeconds
     * @param includeDetails
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public SwapEligibleShiftsDto requestSwapEligibleShiftsSynchronously(
            @Validate(validator = EntityExistValidatorBean.class, type = Shift.class) PrimaryKey shiftPk,
            String employeeId, Integer maxComputationTime, Integer maxUnimprovedSecondsSpent,
            Integer maxSynchronousWaitSeconds, Boolean includeDetails)
            throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {

        if (maxSynchronousWaitSeconds < 1) {
            throw new ValidationException(getMessage("validation.schedule.qualificationrequest.invalidsynchronouswait"));
        }

        String tenantId = sessionService.getTenantId();
        Employee employee;
        if (employeeId == null) {
            String userId = sessionService.getUserId();
            PrimaryKey userAccountPk = new PrimaryKey(tenantId, userId);
            UserAccount userAccount = userAccountService.getUserAccount(userAccountPk);
            employee = userAccount.getEmployee();
            employeeId = employee.getId();
        } else {
            PrimaryKey employeePk = new PrimaryKey(tenantId, employeeId);
            employee = employeeService.getEmployee(employeePk);
        }

        // Validate employee exists...
        if (employee == null) {
            throw new ValidationException(getMessage("validation.schedule.shift.noemployee", employeeId));
        }

        Shift shift = shiftService.getShift(shiftPk);
        if (!shift.getEmployeeId().equals(employee.getId())) {
            throw new ValidationException(getMessage("validation.employee.shiftnotassigned", employee.getId(), shift.getId()));
        }

        PrimaryKey schedulePk = scheduleService.getSchedulePkFromShiftPk(shiftPk);

        List<String> swapSeekingShiftIds = new ArrayList<String>();
        swapSeekingShiftIds.add(shift.getId());

        ShiftSwapEligibilityRequestTracker requestTracker = scheduleService.getShiftSwapEligibility(schedulePk, swapSeekingShiftIds, null,
                maxUnimprovedSecondsSpent, maxUnimprovedSecondsSpent, maxSynchronousWaitSeconds, includeDetails);

        SwapEligibleShiftsDto returnDto = new SwapEligibleShiftsDto();

        // TODO Fix the following. The requestTracker state not kept up to date!
        // returnDto.setState( requestTracker.getState() );
        returnDto.setRequestState(TaskState.Complete);  // TODO Temporarily forcing correct state assignment (see above).
        returnDto.setRequestId(requestTracker.getRequestId());

        Map<String, Collection<ShiftQualificationDto>> resultingShiftQualsMap = requestTracker.getQualificationShifts();
        Collection<ShiftQualificationDto> shiftResults = resultingShiftQualsMap.get(shiftPk.getId());
        if (shiftResults != null){
	        for (ShiftQualificationDto resultingShiftQual : shiftResults) {
	            if (resultingShiftQual.getIsAccepted()) {
	                SwapEligibleShiftsDto.SwappableShiftDescriptor swappableShift = new SwapEligibleShiftsDto.SwappableShiftDescriptor();
	                swappableShift.employeeId = resultingShiftQual.getEmployeeId();
	                swappableShift.employeeName = resultingShiftQual.getEmployeeName();
	                swappableShift.shiftId = resultingShiftQual.getShiftId();
	
	                Shift acceptedShift = shiftService.getShift(new PrimaryKey(tenantId, resultingShiftQual.getShiftId()));
	                if (acceptedShift != null) {
	                    swappableShift.startDateTime = acceptedShift.getStartDateTime();
	                    swappableShift.endDateTime = acceptedShift.getEndDateTime();
	                    swappableShift.skillName = acceptedShift.getSkillName();
	                    swappableShift.teamName = acceptedShift.getTeamName();
	                    returnDto.getSwappableShifts().add(swappableShift);
	                }
	            }
	        }
        }
        
        return returnDto;
    }

    @Validation
    public Collection<EmployeeProcessAutoApprovalDto> autoApprovals(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey) {
        Collection<EmployeeProcessAutoApprovalDto> result = new ArrayList<>();

        Employee employee = employeeService.getEmployee(employeePrimaryKey);
        Set<EmployeeProcessAutoApproval> processAutoApprovals = employee.getEmployeeProcessAutoApprovals();
        if (processAutoApprovals != null) {
            for (EmployeeProcessAutoApproval processAutoApproval : processAutoApprovals) {
                EmployeeProcessAutoApprovalDto autoApprovalDto = new EmployeeProcessAutoApprovalDto();
                autoApprovalDto.setEmployeeId(employeePrimaryKey.getId());
                autoApprovalDto.setAutoApproval(processAutoApproval.isAutoApproval());
                autoApprovalDto.setWflProcessTypeId(processAutoApproval.getWflProcessType().getId());
                autoApprovalDto.setWflProcessTypeName(processAutoApproval.getWflProcessType().getName());

                result.add(autoApprovalDto);
            }
        }

        return result;
    }

    @Validation
    public void createAutoApproval(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            EmployeeProcessAutoApprovalDto autoApprovalDto) {
        Employee employee = employeeService.getEmployee(employeePrimaryKey);
        Set<EmployeeProcessAutoApproval> processAutoApprovals = employee.getEmployeeProcessAutoApprovals();
        boolean found = false;
        if (processAutoApprovals != null) {
            for (EmployeeProcessAutoApproval processAutoApproval : processAutoApprovals) {
                if (processAutoApproval.getWflProcessType().getId().equals(autoApprovalDto.getWflProcessTypeId())) {
                    processAutoApproval.setAutoApproval(autoApprovalDto.isAutoApproval());

                    employeeService.mergeEmployeeProcessAutoApproval(processAutoApproval);

                    found = true;

                    break;
                }
            }
        }

        if (!found) {
            WflProcessType wflProcessType = wflProcessTypeService.find(autoApprovalDto.getWflProcessTypeId());

            PrimaryKey primaryKey = new PrimaryKey(employeePrimaryKey.getTenantId());
            EmployeeProcessAutoApproval processAutoApproval = new EmployeeProcessAutoApproval(primaryKey);
            processAutoApproval.setAutoApproval(autoApprovalDto.isAutoApproval());
            processAutoApproval.setEmployee(employee);
            processAutoApproval.setWflProcessType(wflProcessType);

            employeeService.persistEmployeeProcessAutoApproval(processAutoApproval);
        }
    }

    @Validation
    public void deleteAutoApproval(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = EmployeeProcessAutoApproval.class)
            PrimaryKey autoApprovalPrimaryKey) {
        employeeService.deleteEmployeeProcessAutoApproval(autoApprovalPrimaryKey);
    }

    @Validation
    public EmployeeManagerViewDto managerDetailsView(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        DtoMapper<Employee, EmployeeManagerViewDto> dtoMapper = new DtoMapper<>();
        dtoMapper.registerExceptDtoFieldForMapping("userAccountDto");
        dtoMapper.registerExceptDtoFieldForMapping("siteInfo");
        dtoMapper.registerExceptDtoFieldForMapping("roleInfo");
        dtoMapper.registerExceptDtoFieldForMapping("skillInfo");
        dtoMapper.registerExceptDtoFieldForMapping("teamInfo");
        dtoMapper.registerExceptDtoFieldForMapping("ptoInfo");
        dtoMapper.registerExceptDtoFieldForMapping("licenseCertificationInfo");
        dtoMapper.registerExceptDtoFieldForMapping("autoApprovalsSettingDto");
        dtoMapper.registerExceptDtoFieldForMapping("notificationSettings");

        Employee employee = employeeService.getEmployee(employeePrimaryKey);

        EmployeeManagerViewDto result = dtoMapper.map(employee, EmployeeManagerViewDto.class);

        // autoApprovals
        AutoApprovalsSettingDto autoApprovalsSettingDto = getAutoApprovals(employee.getPrimaryKey());
        result.setAutoApprovalsSettingDto(autoApprovalsSettingDto);
        

        // PTOs
        Set<AvailabilityTimeFrame> availabilityTimeFrames = employee.getAvailabilityTimeFrames();
        LocalDate today = new LocalDate();
        Collection<Map<String, Object>> ptoInfo = new ArrayList<>();
        
        if (availabilityTimeFrames != null) {
	        for (AvailabilityTimeFrame availabilityTimeFrame : availabilityTimeFrames) {
	            if (availabilityTimeFrame instanceof CDAvailabilityTimeFrame) {
	                CDAvailabilityTimeFrame cdAvailabilityTimeFrame = (CDAvailabilityTimeFrame) availabilityTimeFrame;
	                if (cdAvailabilityTimeFrame.getIsPTO()) {
	                    DateTime dateTime = cdAvailabilityTimeFrame.getStartDateTime();
	                    if (dateTime != null && dateTime.getMillis() >= today.toDate().getTime()) {
	                        Map<String, Object> ptoMap = new HashMap<>();
	                        ptoInfo.add(ptoMap);
	                        ptoMap.put("ID", cdAvailabilityTimeFrame.getId());
	                        ptoMap.put("startDateTime", dateTime);
	                        ptoMap.put("reason", cdAvailabilityTimeFrame.getReason());
	                        if (cdAvailabilityTimeFrame.getAbsenceType() != null) {
	                            ptoMap.put("absenceTypeName", cdAvailabilityTimeFrame.getAbsenceType().getName());
	                        }
	                    }
	                }
	            }
	        }
        }
        result.setPtoInfo(ptoInfo);

        UserAccount userAccount = employee.getUserAccount();
        if (userAccount != null) {
            // notification settings
            result.setNotificationSettings(userAccountService.getNotificationSettings(userAccount)); 

            result.setUserAccountDto(this.toDto(userAccount, UserAccountDto.class));

            // roles info
            Collection<Map<String, Object>> roleInfos = new ArrayList<>();
            result.setRoleInfo(roleInfos);

            Set<Object> permissionsAndRoles = userAccountService.getPermissionsAndRoles(userAccount, false);
            if (permissionsAndRoles!=null){
	            for (Object object : permissionsAndRoles) {
	                if (object instanceof Role) {
	                    Map<String, Object> roleInfoMap = new HashMap<>();
	                    roleInfos.add(roleInfoMap);
	
	                    roleInfoMap.put("roleId", ((Role) object).getId());
	                    roleInfoMap.put("roleName", ((Role) object).getName());
	                }
	            }
            }
        }

        // teams & site
        Site site = null;
        Collection<Map<String, Object>> teamInfos = new ArrayList<>();

        Set<EmployeeTeam> employeeTeams = employee.getEmployeeTeams();
        if (employeeTeams != null) {
            for (EmployeeTeam employeeTeam : employeeTeams) {
                Team team = employeeTeam.getTeam();
                if (team != null) {
                    if (site == null) {
                        site = teamService.getSite(team);

                        Map<String, Object> siteInfo = new HashMap<>();
                        siteInfo.put("siteId", site.getId());
                        siteInfo.put("siteName", site.getName());
                        siteInfo.put("timeZone", site.getTimeZone().getID());
                        siteInfo.put("firstDayOfWeek", site.getFirstDayOfWeek());

                        result.setSiteInfo(siteInfo);
                    }

                    Map<String, Object> teamMap = new HashMap<>();
                    teamMap.put("teamId", team.getId());
                    teamMap.put("name", team.getName());
                    teamMap.put("isHomeTeam", employeeTeam.getIsHomeTeam());
                    teamMap.put("isFloating", employeeTeam.getIsFloating());
                    teamMap.put("isSchedulable", employeeTeam.getIsSchedulable());

                    teamInfos.add(teamMap);
                }
            }
        }

        result.setTeamInfo(teamInfos);

        // skills
        Collection<Map<String, Object>> skillInfos = new ArrayList<>();

        Set<EmployeeSkill> employeeSkills = employee.getEmployeeSkills();
        if (employeeSkills != null) {
            for (EmployeeSkill employeeSkill : employeeSkills) {
                Skill skill = employeeSkill.getSkill();
                if (skill != null) {
                    Map<String, Object> skillMap = new HashMap<>();
                    skillMap.put("skillId", skill.getId());
                    skillMap.put("name", skill.getName());
                    skillMap.put("abbreviation", skill.getAbbreviation());
                    skillMap.put("isActive", skill.getIsActive());
                    skillMap.put("isPrimarySkill", employeeSkill.getIsPrimarySkill());
                    skillMap.put("skillScore", employeeSkill.getSkillScore());

                    skillInfos.add(skillMap);
                }
            }
        }

        result.setSkillInfo(skillInfos);

        return result;
    }

    @Deprecated
    @Validation
    public NotificationSettingDto getNotificationSettings(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey) {
        Employee employee = employeeService.getEmployee(employeePrimaryKey);
    	return userAccountService.getNotificationSettings(employee.getUserAccount());
    }

    @Deprecated
    @Validation
    public NotificationSettingDto updateNotificationSettings(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            NotificationSettingDto notificationSettingDto) {
        Employee employee = employeeService.getEmployee(employeePrimaryKey);
    	return userAccountService.updateNotificationSettings(employee.getUserAccount(), notificationSettingDto);
    }

    @Validation
    public AutoApprovalsSettingDto getAutoApprovals(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey) {
        AutoApprovalsSettingDto result = new AutoApprovalsSettingDto();

        Employee employee = employeeService.getEmployee(employeePrimaryKey);
        Set<EmployeeProcessAutoApproval> autoApprovals = employee.getEmployeeProcessAutoApprovals();
        if (autoApprovals != null) {
            for (EmployeeProcessAutoApproval autoApproval : autoApprovals) {
                WflProcessType wflProcessType = autoApproval.getWflProcessType();
                if (wflProcessType != null) {
                    WorkflowRequestTypeDict workflowRequestTypeDict = wflProcessType.getType();
                    if (WorkflowRequestTypeDict.SHIFT_SWAP_REQUEST.equals(workflowRequestTypeDict)) {
                        result.setSwapAutoApprove(autoApproval.isAutoApproval());
                    } else if (WorkflowRequestTypeDict.AVAILABILITY_REQUEST.equals(workflowRequestTypeDict)) {
                        result.setAvailAutoApprove(autoApproval.isAutoApproval());
                    } else if (WorkflowRequestTypeDict.WIP_REQUEST.equals(workflowRequestTypeDict)) {
                        result.setWipAutoApprove(autoApproval.isAutoApproval());
                    }
                }
            }
        }

        return result;
    }

    @Validation
    public AutoApprovalsSettingDto updateAutoApprovals(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            AutoApprovalsSettingDto autoApprovalsSettingDto) {
        Employee employee = employeeService.getEmployee(employeePrimaryKey);

        EmployeeProcessAutoApproval wipAutoApproval = null;
        EmployeeProcessAutoApproval swapAutoApproval = null;
        EmployeeProcessAutoApproval availAutoApproval = null;

        Set<EmployeeProcessAutoApproval> autoApprovals = employee.getEmployeeProcessAutoApprovals();
        
        if (autoApprovals != null) {
	        for (EmployeeProcessAutoApproval autoApproval : autoApprovals) {
	            WflProcessType wflProcessType = autoApproval.getWflProcessType();
	            if (wflProcessType != null) {
	                WorkflowRequestTypeDict workflowRequestTypeDict = wflProcessType.getType();
	                if (WorkflowRequestTypeDict.SHIFT_SWAP_REQUEST.equals(workflowRequestTypeDict)
	                        && autoApprovalsSettingDto.getSwapAutoApprove() != null) {
	                    swapAutoApproval = autoApproval;
	                    swapAutoApproval.setAutoApproval(autoApprovalsSettingDto.getSwapAutoApprove());
	                    employeeService.mergeEmployeeProcessAutoApproval(swapAutoApproval);
	                } else if (WorkflowRequestTypeDict.AVAILABILITY_REQUEST.equals(workflowRequestTypeDict)
	                        && autoApprovalsSettingDto.getAvailAutoApprove() != null) {
	                    availAutoApproval = autoApproval;
	                    availAutoApproval.setAutoApproval(autoApprovalsSettingDto.getAvailAutoApprove());
	                    employeeService.mergeEmployeeProcessAutoApproval(availAutoApproval);
	                } else if (WorkflowRequestTypeDict.WIP_REQUEST.equals(workflowRequestTypeDict)
	                        && autoApprovalsSettingDto.getWipAutoApprove() != null) {
	                    wipAutoApproval = autoApproval;
	                    wipAutoApproval.setAutoApproval(autoApprovalsSettingDto.getWipAutoApprove());
	                    employeeService.mergeEmployeeProcessAutoApproval(wipAutoApproval);
	                }
	            }
	        }

            boolean updateEmployeeFlag = false;

            if (wipAutoApproval == null && autoApprovalsSettingDto.getWipAutoApprove() != null) {
                updateEmployeeFlag = true;

                WflProcessType wflProcessType = wflProcessTypeService.findByDictionary(
                        WorkflowRequestTypeDict.WIP_REQUEST);

                wipAutoApproval = new EmployeeProcessAutoApproval(new PrimaryKey(employee.getTenantId()));
                wipAutoApproval.setAutoApproval(autoApprovalsSettingDto.getWipAutoApprove());
                wipAutoApproval.setWflProcessType(wflProcessType);
                wipAutoApproval.setEmployee(employee);

                employeeService.persistEmployeeProcessAutoApproval(wipAutoApproval);
                autoApprovals.add(wipAutoApproval);
            }

            if (swapAutoApproval == null && autoApprovalsSettingDto.getSwapAutoApprove() != null) {
                updateEmployeeFlag = true;

                WflProcessType wflProcessType =
                        wflProcessTypeService.findByDictionary(WorkflowRequestTypeDict.SHIFT_SWAP_REQUEST);

                swapAutoApproval = new EmployeeProcessAutoApproval(new PrimaryKey(employee.getTenantId()));
                swapAutoApproval.setAutoApproval(autoApprovalsSettingDto.getSwapAutoApprove());
                swapAutoApproval.setWflProcessType(wflProcessType);
                swapAutoApproval.setEmployee(employee);

                employeeService.persistEmployeeProcessAutoApproval(swapAutoApproval);
                autoApprovals.add(swapAutoApproval);
            }

            if (availAutoApproval == null && autoApprovalsSettingDto.getAvailAutoApprove() != null) {
                updateEmployeeFlag = true;

                WflProcessType wflProcessType =
                        wflProcessTypeService.findByDictionary(WorkflowRequestTypeDict.AVAILABILITY_REQUEST);

                availAutoApproval = new EmployeeProcessAutoApproval(new PrimaryKey(employee.getTenantId()));
                availAutoApproval.setAutoApproval(autoApprovalsSettingDto.getAvailAutoApprove());
                availAutoApproval.setWflProcessType(wflProcessType);
                availAutoApproval.setEmployee(employee);

                employeeService.persistEmployeeProcessAutoApproval(availAutoApproval);
                autoApprovals.add(availAutoApproval);
            }

            if (updateEmployeeFlag) {
                employeeService.update(employee);
            }
        }

        return getAutoApprovals(employeePrimaryKey);
    }  

    @Validation
    public HoursAndOvertimeDto getHoursAndOvertime(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey) {
        HoursAndOvertimeDto result = new HoursAndOvertimeDto();

        Employee employee = employeeService.getEmployee(employeePrimaryKey);
        Set<EmployeeContract> employeeContracts = employee.getEmployeeContracts();
        Set<ContractLine> contractLines = new HashSet<>();
        
        if (employeeContracts != null) {
	        for (EmployeeContract employeeContract : employeeContracts) {
	            contractLines.addAll(employeeContract.getContractLines());
	        }
        }

        for (ContractLine contractLine : contractLines) {
            if (contractLine instanceof IntMinMaxCL) {
                IntMinMaxCL intMinMaxCL = (IntMinMaxCL) contractLine;
                ContractLineType contractLineType = contractLine.getContractLineType();
                if (ContractLineType.DAYS_PER_WEEK.equals(contractLineType)) {
                    int daysPerWeek = -1;
                    if (intMinMaxCL.getMaximumEnabled()) {
                        daysPerWeek = intMinMaxCL.getMaximumValue();
                    } else if (intMinMaxCL.getMinimumEnabled()) {
                        daysPerWeek = intMinMaxCL.getMinimumValue();
                    }
                    result.setDaysPerWeek(daysPerWeek);
                } else if (ContractLineType.HOURS_PER_DAY.equals(contractLineType)) {
                    if (intMinMaxCL.getMinimumEnabled()) {
                        result.setMinHoursPerDay(intMinMaxCL.getMinimumValue());
                    }
                    if (intMinMaxCL.getMaximumEnabled()) {
                        result.setMaxHoursPerDay(intMinMaxCL.getMaximumValue());
                    }
                } else if (ContractLineType.HOURS_PER_WEEK.equals(contractLineType)) {
                    if (intMinMaxCL.getMinimumEnabled()) {
                        result.setMinHoursPerWeek(intMinMaxCL.getMinimumValue());
                    }
                    if (intMinMaxCL.getMaximumEnabled()) {
                        result.setMaxHoursPerWeek(intMinMaxCL.getMaximumValue());
                    }
                } else if (ContractLineType.CONSECUTIVE_WORKING_DAYS.equals(contractLineType)) {
                    int consecutiveDays = -1;
                    if (intMinMaxCL.getMaximumEnabled()) {
                        consecutiveDays = intMinMaxCL.getMaximumValue();
                    } else if (intMinMaxCL.getMinimumEnabled()) {
                        consecutiveDays = intMinMaxCL.getMinimumValue();
                    }
                    result.setConsecutiveDays(consecutiveDays);
                } else if (ContractLineType.HOURS_PER_WEEK_PRIME_SKILL.equals(contractLineType)) {
                    int primarySkillHours = -1;
                    if (intMinMaxCL.getMaximumEnabled()) {
                        primarySkillHours = intMinMaxCL.getMaximumValue();
                    } else if (intMinMaxCL.getMinimumEnabled()) {
                        primarySkillHours = intMinMaxCL.getMinimumValue();
                    }
                    result.setPrimarySkillHours(primarySkillHours);
                } else if (ContractLineType.DAILY_OVERTIME.equals(contractLineType)) {
                    int dailyOvertime = -1;
                    if (intMinMaxCL.getMaximumEnabled()) {
                        dailyOvertime = intMinMaxCL.getMaximumValue();
                    } else if (intMinMaxCL.getMinimumEnabled()) {
                        dailyOvertime = intMinMaxCL.getMinimumValue();
                    }
                    result.getOvertimeDto().setDailyOvertimeMins(dailyOvertime * 60);
                } else if (ContractLineType.WEEKLY_OVERTIME.equals(contractLineType)) {
                    int weeklyOvertime = -1;
                    if (intMinMaxCL.getMaximumEnabled()) {
                        weeklyOvertime = intMinMaxCL.getMaximumValue();
                    } else if (intMinMaxCL.getMinimumEnabled()) {
                        weeklyOvertime = intMinMaxCL.getMinimumValue();
                    }
                    result.getOvertimeDto().setWeeklyOvertimeMins(weeklyOvertime * 60);
                } else if (ContractLineType.TWO_WEEK_OVERTIME.equals(contractLineType)) {
                    int biweeklyOvertime = -1;
                    if (intMinMaxCL.getMaximumEnabled()) {
                        biweeklyOvertime = intMinMaxCL.getMaximumValue();
                    } else if (intMinMaxCL.getMinimumEnabled()) {
                        biweeklyOvertime = intMinMaxCL.getMinimumValue();
                    }
                    result.getOvertimeDto().setBiweeklyOvertimeMins(biweeklyOvertime * 60);
                }
            }
        }

        return result;
    }

    @Validation
    public HoursAndOvertimeDto updateHoursAndOvertime(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            HoursAndOvertimeDto hoursAndOvertimeDto) {
        Employee employee = employeeService.getEmployee(employeePrimaryKey);
        Set<EmployeeContract> employeeContracts = employee.getEmployeeContracts();
        Set<ContractLine> contractLines = new HashSet<>();
        EmployeeContract employeeContract = null;
        
        if (employeeContracts != null) {
	        for (EmployeeContract contract : employeeContracts) {
	            if (employeeContract == null) {
	                employeeContract = contract;
	            }
	            contractLines.addAll(contract.getContractLines());
	        }
        }

        String tenantId = employeePrimaryKey.getTenantId();

        if (employeeContract == null) {
            employeeContract = contractService.createEmployeeContract(new PrimaryKey(tenantId));

            employeeContract.setEmployee(employee);
            contractService.updateContract(employeeContract);
        }

        updateMaxValueInContractLine(ContractLineType.DAYS_PER_WEEK, contractLines, tenantId,
                hoursAndOvertimeDto.getDaysPerWeek(), employeeContract);

        updateMaxValueInContractLine(ContractLineType.CONSECUTIVE_WORKING_DAYS, contractLines, tenantId,
                hoursAndOvertimeDto.getConsecutiveDays(), employeeContract);

        updateMaxValueInContractLine(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, contractLines, tenantId,
                hoursAndOvertimeDto.getPrimarySkillHours(), employeeContract);

        updateMinMaxValueInContractLine(ContractLineType.HOURS_PER_DAY, contractLines, tenantId,
                hoursAndOvertimeDto.getMinHoursPerDay(), hoursAndOvertimeDto.getMaxHoursPerDay(), employeeContract);

        updateMinMaxValueInContractLine(ContractLineType.HOURS_PER_WEEK, contractLines, tenantId,
                hoursAndOvertimeDto.getMinHoursPerWeek(), hoursAndOvertimeDto.getMaxHoursPerWeek(), employeeContract);

        updateMaxValueInContractLine(ContractLineType.DAILY_OVERTIME, contractLines, tenantId,
                hoursAndOvertimeDto.getOvertimeDto().getDailyOvertimeMins(), employeeContract);

        updateMaxValueInContractLine(ContractLineType.WEEKLY_OVERTIME, contractLines, tenantId,
                hoursAndOvertimeDto.getOvertimeDto().getWeeklyOvertimeMins(), employeeContract);

        updateMaxValueInContractLine(ContractLineType.TWO_WEEK_OVERTIME, contractLines, tenantId,
                hoursAndOvertimeDto.getOvertimeDto().getBiweeklyOvertimeMins(), employeeContract);

        return getHoursAndOvertime(employeePrimaryKey);
    }

    @Validation
    public boolean updateEmployeeSkills(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            AddUpdateRemoveDto<EmployeeSkillAssociationDto> addUpdateRemoveDto) {
        Employee employee = employeeService.getEmployee(employeePrimaryKey);

        String tenantId = employee.getTenantId();
        boolean update = false;

        if (addUpdateRemoveDto.getAddCollection() != null) {
            for (EmployeeSkillAssociationDto dto : addUpdateRemoveDto.getAddCollection()) {
                Skill skill = skillService.getSkill(new PrimaryKey(tenantId, dto.getSkillId()));
                if (skill == null) {
                    throw new ValidationException(getMessage("validation.schedule.noskill", dto.getSkillId()));
                }
                EmployeeSkill employeeSkill = new EmployeeSkill(new PrimaryKey(tenantId));
                employeeSkill.setIsPrimarySkill(dto.isPrimarySkill());
                employeeSkill.setSkillScore(dto.getSkillScore());
                employeeService.addEmployeeSkill(employee, skill, employeeSkill);
                update = true;
            }
        }

        if (addUpdateRemoveDto.getUpdateCollection() != null) {
            for (EmployeeSkillAssociationDto dto : addUpdateRemoveDto.getUpdateCollection()) {
                Skill skill = skillService.getSkill(new PrimaryKey(tenantId, dto.getSkillId()));
                if (skill == null) {
                    throw new ValidationException(getMessage("validation.schedule.noskill", dto.getSkillId()));
                }
                EmployeeSkill employeeSkill = employeeService.findEmployeeSkill(employee, skill);
                if (employeeSkill == null) {
                    throw new ValidationException(getMessage("validation.schedule.noemployeeskill", skill.getId(),
                            employee.getId()));
                }
                employeeSkill.setIsPrimarySkill(dto.isPrimarySkill());
                employeeSkill.setSkillScore(dto.getSkillScore());
                employeeService.updateEmployeeSkill(employeeSkill);
            }
        }

        if (addUpdateRemoveDto.getRemoveCollection() != null) {
            for (String id : addUpdateRemoveDto.getRemoveCollection()) {
                Skill skill = skillService.getSkill(new PrimaryKey(tenantId, id));
                if (skill == null) {
                    throw new ValidationException(getMessage("validation.schedule.noskill", id));
                }
                employeeService.removeEmployeeSkill(employee, skill);
                update = true;
            }
        }

        if (update) {
            employeeService.update(employee);
        }

        return true;
    }

    @Validation
    public boolean updateEmployeeTeams(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            AddUpdateRemoveDto<EmployeeTeamAssociationDto> addUpdateRemoveDto) {
        Employee employee = employeeService.getEmployee(employeePrimaryKey);

        String tenantId = employee.getTenantId();
        boolean update = false;

        if (addUpdateRemoveDto.getAddCollection() != null) {
            for (EmployeeTeamAssociationDto dto : addUpdateRemoveDto.getAddCollection()) {
                Team team = teamService.getTeam(new PrimaryKey(tenantId, dto.getTeamId()));
                if (team == null) {
                    throw new ValidationException(getMessage("validation.schedule.noteam", dto.getTeamId()));
                }
                EmployeeTeam employeeTeam = new EmployeeTeam(new PrimaryKey(tenantId));
                employeeTeam.setIsFloating(dto.getIsFloating());
                employeeTeam.setIsSchedulable(dto.getIsSchedulable());
                employeeService.addEmployeeTeam(employee, team, employeeTeam);
                update = true;
            }
        }

        if (addUpdateRemoveDto.getUpdateCollection() != null) {
            for (EmployeeTeamAssociationDto dto : addUpdateRemoveDto.getUpdateCollection()) {
                Team team = teamService.getTeam(new PrimaryKey(tenantId, dto.getTeamId()));
                if (team == null) {
                    throw new ValidationException(getMessage("validation.schedule.noteam", dto.getTeamId()));
                }
                EmployeeTeam employeeTeam = employeeService.findEmployeeTeam(employee, team);
                if (employeeTeam == null) {
                    throw new ValidationException(getMessage("validation.schedule.noemployeeteam", team.getId(),
                            employee.getId()));
                }
                employeeTeam.setIsFloating(dto.getIsFloating());
                employeeTeam.setIsSchedulable(dto.getIsSchedulable());
                employeeService.updateEmployeeTeam(employeeTeam);
            }
        }

        if (addUpdateRemoveDto.getRemoveCollection() != null) {
            for (String id : addUpdateRemoveDto.getRemoveCollection()) {
                Team team = teamService.getTeam(new PrimaryKey(tenantId, id));
                if (team == null) {
                    throw new ValidationException(getMessage("validation.schedule.noteam", id));
                }
                employeeService.removeEmployeeTeam(employee, team);
                update = true;
            }
        }

        if (update) {
            employeeService.update(employee);
        }

        return true;
    }

    @Validation
    public ResultSetDto<RememberMeDto> getRememberMeObjects(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQuery simpleQuery = new SimpleQuery(employeePrimaryKey.getTenantId());
        simpleQuery.setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setEntityClass(RememberMe.class).setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC"))
                .setTotalCount(true);

        simpleQuery.addFilter("userAccount.employee.primaryKey.id = '" + employeePrimaryKey.getId() + "'");

        ResultSet<RememberMe> resultSet = organizationService.getRememberMeObjects(simpleQuery);

        return toResultSetDto(resultSet, RememberMeDto.class);
    }

    private ContractLine findContractLineByTypeInCollection(Collection<ContractLine> contractLines,
                                                            ContractLineType contractLineType) {
        ContractLine result = null;
        
        if (contractLines != null) {
	        for (ContractLine contractLine : contractLines) {
	            if (contractLineType.equals(contractLine.getContractLineType())) {
	                result = contractLine;
	                break;
	            }
	        }
        }
        return result;
    }

    private void updateMaxValueInContractLine(ContractLineType contractLineType,
                                              Collection<ContractLine> contractLines, String tenantId, int value,
                                              EmployeeContract employeeContract) {
        updateMinMaxValueInContractLine(contractLineType, contractLines, tenantId, -1, value, employeeContract);
    }

    private void updateMinMaxValueInContractLine(ContractLineType contractLineType,
                                                 Collection<ContractLine> contractLines, String tenantId, int min,
                                                 int max, EmployeeContract employeeContract) {
        IntMinMaxCL intMinMaxCL = (IntMinMaxCL) findContractLineByTypeInCollection(contractLines, contractLineType);
        if (intMinMaxCL == null) {
            if (min != -1 || max != -1) {
                intMinMaxCL = new IntMinMaxCL(new PrimaryKey(tenantId));
                intMinMaxCL.setContractLineType(contractLineType);
                intMinMaxCL.setContract(employeeContract);
                intMinMaxCL.setMinimumEnabled(min != -1);
                intMinMaxCL.setMinimumValue(min);
                intMinMaxCL.setMaximumEnabled(max != -1);
                intMinMaxCL.setMaximumValue(max);

                employeeContract.addContractLine(intMinMaxCL);

                contractLineService.createContractLine(intMinMaxCL);
                contractService.updateContract(employeeContract);
            }
        } else {
            if (min != -1 || max != -1) {
                intMinMaxCL.setMinimumEnabled(min != -1);
                intMinMaxCL.setMinimumValue(min);
                intMinMaxCL.setMaximumEnabled(max != -1);
                intMinMaxCL.setMaximumValue(max);

                contractLineService.updateContractLine(intMinMaxCL);
            } else {
                Contract contract = intMinMaxCL.getContract();
                contract.getContractLines().remove(intMinMaxCL);

                contractLineService.delete(intMinMaxCL);
                contractService.updateContract(contract);
            }
        }
    }

    /**
     * Returns the employee's AvailcalViewDto for the calendar dates requested  
     * @param employeePk
     * @param firstCalendarDate (the UTC instant for site TZ midnight of the FIRST calendar DATE for view)
     * @param lastCalendarDate (the UTC instant for site TZ midnight of the LAST calendar DATE for view)
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    @Validation
	public AvailcalViewDto getAvailcalView(
			@Validate(validator = EntityExistValidatorBean.class, type = Employee.class) PrimaryKey employeePk, 
			Long firstCalendarDate, Long lastCalendarDate) throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		DateTime startDateTime;
		DateTime endDateTime;
		if (firstCalendarDate == null || lastCalendarDate == null){
            throw new ValidationException(getMessage("validation.employee.availcal.daterange"));
		} else {
			startDateTime = new DateTime(firstCalendarDate);
			endDateTime = new DateTime(lastCalendarDate);
		}
		
		Employee employee = employeeService.getEmployee(employeePk);
		return employeeService.getAvailcalView(employee, startDateTime, endDateTime);
	}

	@Validation
    public AvailcalViewDto getAvailcalPreviewForWorkflowRequest(
			@Validate(validator = EntityExistValidatorBean.class, type = Employee.class) PrimaryKey employeePk, 
			AvailcalPreviewWorkflowRequestParamsDto params) throws InstantiationException, 
			IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
		DateTime startDateTime;
		DateTime endDateTime;
		if (params.getDateRangeStart() == null || params.getDateRangeEnd() == null){
            throw new ValidationException(getMessage("validation.employee.availcal.daterange"));
		} else {
			startDateTime = new DateTime(params.getDateRangeStart());
			endDateTime = new DateTime(params.getDateRangeEnd());
		}
		
		if (params.getWorkflowRequestId() == null  ||  params.getWorkflowRequestId().isEmpty()){
            throw new ValidationException(getMessage("validation.employee.availcal.workflowrequestid"));
		}
		
		Employee employee = employeeService.getEmployee(employeePk);
	
//		WorkflowAvailabilitySettingsDto availabilityRequestSettings = 
//				requestDetailsfacade.getAvailabilityRequestSettings(
//						employee.getTenantId(), params.getWorkflowRequestId());				
//		AvailabilityWorkflowRequest updateDto = availabilityRequestSettings.getUpdateDto();
//		AvailabilityRequestSubtype type = updateDto.getType();
//		String actionStr = updateDto.getActionStr();  
		
		WorkflowRequest workflowRequest = workflowRequestService.find(new PrimaryKey(employee.getTenantId(),
                params.getWorkflowRequestId()));
		AvailabilityRequestSubtype availabilityRequestSubtype = workflowRequest.getAvailabilityRequestSubtype();
		String availabilityRequestData = workflowRequest.getData();
		if (availabilityRequestSubtype.equals(AvailabilityRequestSubtype.AvailcalUpdateParamsCDAvailDto)) {
            AvailcalUpdateParamsCDAvailDto cdAvailParams = fromJsonString(availabilityRequestData,
                    AvailcalUpdateParamsCDAvailDto.class);
            return employeeService.getAvailcalPreviewCDAvail(employee, startDateTime, endDateTime, cdAvailParams);
		} else if (availabilityRequestSubtype.equals(AvailabilityRequestSubtype.AvailcalUpdateParamsCIAvailDto)) {
            AvailcalUpdateParamsCIAvailDto ciAvailParams = fromJsonString(availabilityRequestData,
                    AvailcalUpdateParamsCIAvailDto.class);
			return employeeService.getAvailcalPreviewCIAvail(employee, startDateTime, endDateTime, ciAvailParams);			
		} else {
			// Only CDAvail and CIAvail supported (i.e. no support for CDPref or CIPref)
			throw new ValidationException(getMessage("validation.employee.availcal.workflowrequesttype"));
		}
	}

	@Validation
	public AvailcalViewDto getAvailcalPreviewCDAvail(
			@Validate(validator = EntityExistValidatorBean.class, type = Employee.class) PrimaryKey employeePk, 
			Long startDate, Long endDate, AvailcalUpdateParamsCDAvailDto params) throws InstantiationException, 
			IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
		DateTime startDateTime;
		DateTime endDateTime;
		if (startDate == null || endDate == null){
            throw new ValidationException(getMessage("validation.employee.availcal.daterange"));
		} else {
			startDateTime = new DateTime(startDate);
			endDateTime = new DateTime(endDate);
		}
		
		Employee employee = employeeService.getEmployee(employeePk);
		return employeeService.getAvailcalPreviewCDAvail(employee, startDateTime, endDateTime, params);
	}

	@Validation
	public AvailcalViewDto getAvailcalPreviewCIAvail(
			@Validate(validator = EntityExistValidatorBean.class, type = Employee.class) PrimaryKey employeePk, 
			Long startDate, Long endDate, AvailcalUpdateParamsCIAvailDto params) throws InstantiationException, 
			IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
		DateTime startDateTime;
		DateTime endDateTime;
		if (startDate == null || endDate == null){
            throw new ValidationException(getMessage("validation.employee.availcal.daterange"));
		} else {
			startDateTime = new DateTime(startDate);
			endDateTime = new DateTime(endDate);
		}
		
		Employee employee = employeeService.getEmployee(employeePk);
		return employeeService.getAvailcalPreviewCIAvail(employee, startDateTime, endDateTime, params);
	}

    @Validation
	public AvailcalViewDto updateAvailcalCDAvail(
			@Validate(validator = EntityExistValidatorBean.class, type = Employee.class) PrimaryKey employeePk, 
			AvailcalUpdateParamsCDAvailDto params, 
			Long startDate,   // optional, so null is okay
			Long endDate   // optional, so null is okay
			) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		DateTime startDateTime = null;
		DateTime endDateTime = null;
		if (startDate != null && endDate != null) {
			startDateTime = new DateTime(startDate);
			endDateTime = new DateTime(endDate);
		}
	
		Employee employee = employeeService.getEmployee(employeePk);
		return employeeService.updateAvailcalCDAvail(employee, params, startDateTime, endDateTime);
	}

	@Validation
	public AvailcalViewDto updateAvailcalCDPref(
			@Validate(validator = EntityExistValidatorBean.class, type = Employee.class) PrimaryKey employeePk, 
			AvailcalUpdateParamsCDPrefDto params, 
			Long startDate,   // optional, so null is okay
			Long endDate   // optional, so null is okay
			) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		DateTime startDateTime = null;
		DateTime endDateTime = null;
		if (startDate != null && endDate != null) {
			startDateTime = new DateTime(startDate);
			endDateTime = new DateTime(endDate);
		}
		Employee employee = employeeService.getEmployee(employeePk);
		return employeeService.updateAvailcalCDPref(employee, params, startDateTime, endDateTime);
	}

	@Validation
	public AvailcalViewDto updateAvailcalCIAvail(
			@Validate(validator = EntityExistValidatorBean.class, type = Employee.class) PrimaryKey employeePk, 
			AvailcalUpdateParamsCIAvailDto params, 
			Long startDate,   // optional, so null is okay
			Long endDate   // optional, so null is okay
			) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		DateTime startDateTime = null;
		DateTime endDateTime = null;
		if (startDate != null && endDate != null){
			startDateTime = new DateTime(startDate);
			endDateTime = new DateTime(endDate);
		}
		
		Employee employee = employeeService.getEmployee(employeePk);
		return employeeService.updateAvailcalCIAvail(employee, params, startDateTime, endDateTime);
	}

	@Validation
	public AvailcalViewDto updateAvailcalCIPref(
			@Validate(validator = EntityExistValidatorBean.class, type = Employee.class) PrimaryKey employeePk, 
			AvailcalUpdateParamsCIPrefDto params, 
			Long startDate,   // optional, so null is okay
			Long endDate   // optional, so null is okay
			) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		DateTime startDateTime = null;
		DateTime endDateTime = null;
		if (startDate != null && endDate != null){
			startDateTime = new DateTime(startDate);
			endDateTime = new DateTime(endDate);
		}
		
		Employee employee = employeeService.getEmployee(employeePk);
		return employeeService.updateAvailcalCIPref(employee, params, startDateTime, endDateTime);
	}

	@Validation
	public AvailcalViewDto updateAvailcalCDCopy(
			@Validate(validator = EntityExistValidatorBean.class, type = Employee.class) PrimaryKey employeePk, 
			AvailcalUpdateParamsCDCopyDto params, 
			Long startDate,   // optional, so null is okay
			Long endDate   // optional, so null is okay
			) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		DateTime startDateTime = null;
		DateTime endDateTime = null;
		if (startDate != null && endDate != null) {
			startDateTime = new DateTime(startDate);
			endDateTime = new DateTime(endDate);
		}
		
		Employee employee = employeeService.getEmployee(employeePk);
		return employeeService.updateAvailcalCDCopy(employee, params, startDateTime, endDateTime);
	}

	@Validation
	public void updateAvailcalWeekdayRotation(
			@Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePk,
			AvailcalUpdateParamsWeekdayRotationDto params) {
		Employee employee = employeeService.getEmployee(employeePk);
		employeeService.updateAvailcalWeekdayRotation(employee, params);
	}

	@Validation
	public void updateAvailcalWeekendCoupling(
			@Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePk,
			boolean coupleWeekends) {
		Employee employee = employeeService.getEmployee(employeePk);
		employeeService.updateAvailcalWeekendCoupling(employee, coupleWeekends);
	}

    @Validation
    public List<EmployeeCalendarUtils.CalendarEvent> employeeICalendarInfo(
            @Validate(validator = EntityExistValidatorBean.class, type = Employee.class)
            PrimaryKey employeePrimaryKey,
            long startDate) {
        List<EmployeeCalendarUtils.CalendarEvent> result = new ArrayList<>();

        Collection<Object[]> rows = employeeService.employeeICalendarInfo(employeePrimaryKey, startDate);
        
        if (rows!=null){
	        for (Object[] row : rows) {
	            EmployeeCalendarUtils.CalendarEvent calendarEvent = new EmployeeCalendarUtils.CalendarEvent();
	            calendarEvent.setId((String) row[0]);
	            calendarEvent.setStart((Date) row[1]);
	            calendarEvent.setEnd((Date) row[2]);
	            calendarEvent.setSummary((String) row[3]);
	
	            result.add(calendarEvent);
	        }
        }

        return result;
    }

    public Employee findEmployeeByHash(String hash) {
        return employeeService.findEmployeeByHash(hash);
    }

    private Employee createEmployee(String tenantId, EmployeeCreateDto createDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey employeePrimaryKey = new PrimaryKey(tenantId);
        Employee employee = new Employee(employeePrimaryKey, createDto.getEmployeeIdentifier());
        employeeService.persistEmployee(employee);
        
        // remove leading & trailing spaces in case they passed validation
        String fname = StringUtils.trim(createDto.getFirstName());
        String lname = StringUtils.trim(createDto.getLastName());

        PrimaryKey contractKey = new PrimaryKey(tenantId);
        EmployeeContract employeeContract = new EmployeeContract(contractKey);
        employeeContract.setEmployee(employee);
        employeeContract.setDefaultContract(true);
        setCreatedBy(employeeContract);
        setOwnedBy(employeeContract, null);
        employee.getEmployeeContracts().add(employeeContract);
        contractService.persistContract(employeeContract);
        
        // Create associated UserAccount...
        UserAccountDto userAccountDto = createDto.getUserAccountDto();
    	createUserAccount(employee, fname, lname, null, userAccountDto);
        
        // now we can set safely set employee  attributes
        employee.setFirstName(fname);
        employee.setLastName(lname);

        EmployeeUpdateDto employeeUpdateDto = createDto.getUpdateDto();
        if (employeeUpdateDto != null) {
            updateEmployee(employee, employeeUpdateDto);
        }
        
        // initialize notification config
        employeeService.initEmployeeNotificationConfig(employee);

        EmployeeTeamCreateDto employeeTeamCreateDto = createDto.getEmployeeTeamCreateDto();
        if (employeeTeamCreateDto != null) {
            addEmployeeTeam(employeePrimaryKey, employeeTeamCreateDto);

            OvertimeDto overtimeDto = null;
            if (employeeUpdateDto != null) {
                overtimeDto = employeeUpdateDto.getOvertimeDto();
            }
            String teamId = employeeTeamCreateDto.getTeamId();
            if (StringUtils.isNotEmpty(teamId) && overtimeDto == null) {
                PrimaryKey teamPrimaryKey = new PrimaryKey(tenantId, teamId);
                Collection<IntMinMaxCL> intMinMaxCLs = siteService.getSiteIntMinMaxCLs(teamPrimaryKey);
                for (IntMinMaxCL intMinMaxCL : intMinMaxCLs) {
                    if (intMinMaxCL == null) {
                        continue;
                    }
                    try {
                        IntMinMaxCL employeeIntMinMaxCL = intMinMaxCL.clone();
                        employeeIntMinMaxCL.setPrimaryKey(new PrimaryKey(tenantId));
                        employeeIntMinMaxCL.setContract(employeeContract);
                        contractLineService.createContractLine(employeeIntMinMaxCL);
                    } catch (CloneNotSupportedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        getEventService().sendEntityCreateEvent(employee, EmployeeDto.class);
        return employee;
    }

    private boolean updateEmployee(Employee employee, EmployeeUpdateDto employeeUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        boolean modified = false;

        // remove leading & trailing spaces in case they passed validation
        String fname = StringUtils.trim(employeeUpdateDto.getFirstName());
        String lname = StringUtils.trim(employeeUpdateDto.getLastName());

        if (StringUtils.isNotBlank(fname)) {
            employee.setFirstName(fname);
            modified = true;
        }

        if (StringUtils.isNotBlank(lname)) {
            employee.setLastName(lname);
            modified = true;
        }

        String employeeIdentifier = employeeUpdateDto.getEmployeeIdentifier();
        if (StringUtils.isNotBlank(employeeIdentifier)) {
            employee.setEmployeeIdentifier(employeeIdentifier);
            modified = true;
        }

        String middleName = employeeUpdateDto.getMiddleName();
        if (StringUtils.isNotBlank(middleName)) {
            employee.setMiddleName(middleName);
            modified = true;
        }

        String workEmail = employeeUpdateDto.getWorkEmail();
        if (StringUtils.isNotBlank(workEmail)) {
            employee.setWorkEmail(workEmail);
            modified = true;
        }

        if (StringUtils.isNotBlank(employeeUpdateDto.getMobilePhone())) {
            // TODO, Should notificationSmsNumber be unique? Can validation annotation be used?
            employee.setMobilePhone(employeeUpdateDto.getMobilePhone());
            modified = true;
        }

        if (employeeUpdateDto.getActivityType() != null) {
            employee.setActivityType(employeeUpdateDto.getActivityType());
            modified = true;
        }

        if (employeeUpdateDto.getIsDeleted() != null) {
            employee.setIsDeleted(employeeUpdateDto.getIsDeleted());
            modified = true;
        }

        if (employeeUpdateDto.getIsNotificationEnabled() != null) {
            employee.setIsNotificationEnabled(employeeUpdateDto.getIsNotificationEnabled());
            modified = true;
        }

        String address = employeeUpdateDto.getAddress();
        if (StringUtils.isNotBlank(address)) {
            employee.setAddress(address);
            modified = true;
        }

        String address2 = employeeUpdateDto.getAddress2();
        if (StringUtils.isNotBlank(address2)) {
            employee.setAddress2(address2);
            modified = true;
        }

        String city = employeeUpdateDto.getCity();
        if (StringUtils.isNotBlank(city)) {
            employee.setCity(city);
            modified = true;
        }

        String state = employeeUpdateDto.getState();
        if (StringUtils.isNotBlank(state)) {
            employee.setState(state);
            modified = true;
        }

        String zip = employeeUpdateDto.getZip();
        if (StringUtils.isNotBlank(zip)) {
            employee.setZip(zip);
            modified = true;
        }

        String ecRelationship = employeeUpdateDto.getEcRelationship();
        if (StringUtils.isNotBlank(ecRelationship)) {
            employee.setEcRelationship(ecRelationship);
            modified = true;
        }

        String ecPhoneNumber = employeeUpdateDto.getEcPhoneNumber();
        if (StringUtils.isNotBlank(ecPhoneNumber)) {
            employee.setEcPhoneNumber(ecPhoneNumber);
            modified = true;
        }

        String emergencyContact = employeeUpdateDto.getEmergencyContact();
        if (StringUtils.isNotBlank(emergencyContact)) {
            employee.setEmergencyContact(emergencyContact);
            modified = true;
        }

        String gender = employeeUpdateDto.getGender();
        if (StringUtils.isNotBlank(gender)) {
            employee.setGender(gender);
            modified = true;
        }

        String homePhone = employeeUpdateDto.getHomePhone();
        if (StringUtils.isNotBlank(homePhone)) {
            employee.setHomePhone(homePhone);
            modified = true;
        }

        String homeEmail = employeeUpdateDto.getHomeEmail();
        if (StringUtils.isNotBlank(homeEmail)) {
            employee.setHomeEmail(homeEmail);
            modified = true;
        }

        String professionalLabel = employeeUpdateDto.getProfessionalLabel();
        if (StringUtils.isNotBlank(professionalLabel)) {
            employee.setProfessionalLabel(professionalLabel);
            modified = true;
        }

        if (employeeUpdateDto.getPrimaryContactIndicator() != null) {
            employee.setPrimaryContactIndicator(employeeUpdateDto.getPrimaryContactIndicator());
            modified = true;
        }

        if (employeeUpdateDto.getHireDate() != null) {
            employee.setHireDate(new LocalDate(employeeUpdateDto.getHireDate()));
            modified = true;
        }

        if (employeeUpdateDto.getStartDate() != null) {
            employee.setStartDate(new LocalDate(employeeUpdateDto.getStartDate()));
            modified = true;
        }

        if (employeeUpdateDto.getEndDate() != null) {
            employee.setEndDate(new LocalDate(employeeUpdateDto.getEndDate()));
            modified = true;
        }

        if (employeeUpdateDto.getHourlyRate() != null) {
            employee.setHourlyRate(employeeUpdateDto.getHourlyRate());
            modified = true;
        }

        if (employeeUpdateDto.getEmployeeType() != null) {
            employee.setEmployeeType(employeeUpdateDto.getEmployeeType());
            modified = true;
        }

        OvertimeDto overtimeDto = employeeUpdateDto.getOvertimeDto();
        if (overtimeDto != null) {

            // Get the site contract
            EmployeeContract contract = employee.getEmployeeContracts().iterator().next();

            contractFacade.updateOverTimeContractLines(contract, overtimeDto);
            modified = true;
        }

        return modified;
    }

    private <T extends EmployeeAvailabilityDto.BaseDetailDto> Collection<T> findToRemove(Collection<T> entries,
                                                                                         long start, long end) {
        Collection<T> result = new ArrayList<>();
        if (entries!=null){
	        for (T entry : entries) {
	            if (entry.getEndDate() < start || entry.getStartDate() > end) {
	                result.add(entry);
	            }
	        }
        }
        return result;
    }

    private Collection<PostedOpenShift> postedOpenShiftsDeduplication(Collection<PostedOpenShift> openShifts) {
        Map<EmployeeOpenShiftUnicity, PostedOpenShift> map = new HashMap<>();
        
        if (openShifts != null) {
	        for (PostedOpenShift postedOpenShift : openShifts) {
	            EmployeeOpenShiftUnicity key = new EmployeeOpenShiftUnicity();
	            key.skillId = postedOpenShift.getSkillId();
	            key.teamId = postedOpenShift.getTeamId();
	            key.shiftLength = postedOpenShift.getShiftLength();
	            key.startDateTime = postedOpenShift.getStartDateTime();
	
	            map.put(key, postedOpenShift);
	        }
        }
        return map.values();
    }

}
