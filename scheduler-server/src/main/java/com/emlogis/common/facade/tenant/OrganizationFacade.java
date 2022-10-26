package com.emlogis.common.facade.tenant;

import com.emlogis.common.Constants;
import com.emlogis.common.ModelUtils;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.services.tenant.OrganizationService;
import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.common.validation.annotations.*;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.employee.dto.EmployeeRememberMeDto;
import com.emlogis.model.structurelevel.Holiday;
import com.emlogis.model.structurelevel.dto.HolidayCreateDto;
import com.emlogis.model.structurelevel.dto.HolidayDto;
import com.emlogis.model.structurelevel.dto.HolidayUpdateDto;
import com.emlogis.model.structurelevel.dto.TeamManagersDto;
import com.emlogis.model.tenant.Organization;
import com.emlogis.model.tenant.RememberMe;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.tenant.dto.OrganizationDto;
import com.emlogis.model.tenant.dto.OrganizationUpdateDto;
import com.emlogis.model.tenant.settings.SchedulingSettings;
import com.emlogis.model.tenant.settings.dto.SchedulingSettingsDto;
import com.emlogis.model.tenant.settings.dto.SchedulingSettingsUpdateDto;
import com.emlogis.rest.resources.util.DtoMapper;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;

import org.apache.commons.lang3.StringUtils;

import javax.ejb.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class OrganizationFacade extends TenantFacade {

    @EJB
    private OrganizationService organizationService;

	@Override
	protected TenantService getActualTenantService() {
		return organizationService;
	}

    @Validation
    public OrganizationDto getObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Organization.class) String orgId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	Organization org = organizationService.getOrganization(orgId);
        return toDto(org, OrganizationDto.class);
    }
    
    @Validation
    @SuppressWarnings("unchecked")
    public OrganizationDto updateObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Organization.class) String orgId,
            OrganizationUpdateDto updateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	Organization org = (Organization)updateTenant(orgId, updateDto);
    	// so far no organization specific attribute to set.
    	getTenantService().updateTenant(org);
        return toDto(org, OrganizationDto.class);
    }

    @Validation
    @SuppressWarnings("unchecked")
    public ResultSetDto<HolidayDto> getHolidays(
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
	    ResultSet<Holiday> resultSet = organizationService.getHolidays(simpleQuery);
	    return toResultSetDto(resultSet, HolidayDto.class);
    }

    @Validation
    public HolidayDto getHoliday(
                @Validate(validator = EntityExistValidatorBean.class, type = Holiday.class)
                PrimaryKey primaryKey) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Holiday holiday = organizationService.getHoliday(primaryKey);
        return toDto(holiday, HolidayDto.class);
    }

    @Validation
    public HolidayDto createHoliday(
            String tenantId,
            @ValidateAll(
                    dates = {
                           @ValidateDate(field = HolidayCreateDto.EFFECTIVE_START_DATE)
                    },
                    strLengths = {
                            @ValidateStrLength(field = HolidayCreateDto.NAME, min = 3)
                    },
                    uniques = {
                            @ValidateUnique(fields = {HolidayCreateDto.EFFECTIVE_START_DATE, HolidayCreateDto.NAME},
                                    type = Holiday.class),
                            @ValidateUnique(fields = {HolidayCreateDto.UPDATE_EFFECTIVE_START_DATE,
                                    HolidayCreateDto.NAME}, type = Holiday.class),
                    }
            )
            HolidayCreateDto holidayCreateDto) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        PrimaryKey primaryKey = new PrimaryKey(tenantId);
        Holiday holiday = new Holiday(primaryKey);
        holiday.setName(holidayCreateDto.getName());
        holiday.setEffectiveStartDate(holidayCreateDto.getEffectiveStartDate());
        holiday.setEffectiveEndDate(holidayCreateDto.getEffectiveStartDate());
        Organization organization = organizationService.getOrganization(tenantId);
        organizationService.addHoliday(organization, holiday);
        if (holidayCreateDto.getUpdateDto() == null) {
            return toDto(holiday, HolidayDto.class);
        } else {
        	return updateHoliday(primaryKey, holidayCreateDto.getUpdateDto());
        }
    }

    @Validation
    public boolean deleteHoliday(
            @Validate(validator = EntityExistValidatorBean.class, type = Holiday.class)
            PrimaryKey primaryKey) {
        Holiday holiday = organizationService.getHoliday(primaryKey);
        Organization organization = organizationService.getOrganization(holiday.getTenantId());
        organizationService.deleteHoliday(organization, holiday);
        return true;
    }

    @Validation
    public HolidayDto updateHoliday(
            @Validate(validator = EntityExistValidatorBean.class, type = Holiday.class)
            PrimaryKey primaryKey,
            @ValidateStrLength(field = HolidayCreateDto.NAME, min = 3, passNull = true)
            @ValidateUnique(fields = {HolidayCreateDto.EFFECTIVE_START_DATE, HolidayCreateDto.NAME}, type = Holiday.class)
            HolidayUpdateDto holidayUpdateDto) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Holiday holiday = organizationService.getHoliday(primaryKey);
        if (StringUtils.isNotEmpty(holidayUpdateDto.getName())) {
        	holiday.setName(holidayUpdateDto.getName());
        }
        if (StringUtils.isNotEmpty(holidayUpdateDto.getName())) {
        	holiday.setName(holidayUpdateDto.getName());
        }
        if (holidayUpdateDto.getEffectiveStartDate() > 0) {
        	holiday.setEffectiveStartDate(holidayUpdateDto.getEffectiveStartDate());
        }
        if (holidayUpdateDto.getEffectiveEndDate() > 0) {
        	holiday.setEffectiveEndDate(holidayUpdateDto.getEffectiveEndDate());
        }
        if (holiday.getEffectiveStartDate() > holiday.getEffectiveEndDate()) {
            holiday.setEffectiveEndDate(holiday.getEffectiveStartDate());
        }
        if (holidayUpdateDto.getTimeToDeductInMin() > 0) {
        	holiday.setTimeToDeductInMin(holidayUpdateDto.getTimeToDeductInMin());
        }
        holiday.setDescription(holidayUpdateDto.getDescription());
        holiday.setAbbreviation(holidayUpdateDto.getAbbreviation());

        holiday = organizationService.updateHoliday(holiday);
        return toDto(holiday, HolidayDto.class);
    }

    @Validation
    public ResultSetDto<HolidayDto> duplicateHolidays(
            @Validate(validator = EntityExistValidatorBean.class, type = Organization.class)
            String orgId,
            int yearFrom,
            int yearInto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Organization organization = organizationService.getOrganization(orgId);
        ResultSet<Holiday> resultSet = organizationService.duplicateHolidays(organization, yearFrom, yearInto);
        return toResultSetDto(resultSet, HolidayDto.class);
    }

    @Validation
	public SchedulingSettingsDto getSchedulingSettings(
            @Validate(validator = EntityExistValidatorBean.class, type = Organization.class)
            String tenantId) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        Organization org = organizationService.getOrganization(tenantId);
        return toDto(org.getSchedulingSettings(), SchedulingSettingsDto.class);
	}

    @Validation
	public SchedulingSettingsDto setSchedulingSettings(
            @Validate(validator = EntityExistValidatorBean.class, type = Organization.class)
            String tenantId,
            SchedulingSettingsUpdateDto updateDto) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Organization org = organizationService.getOrganization(tenantId);
        SchedulingSettings settings = org.getSchedulingSettings();
        
        settings.setAllowChainingAccrossMidnight(updateDto.isAllowChainingAccrossMidnight());
        settings.setAllowChainingAccrossSkills(updateDto.isAllowChainingAccrossSkills());
        settings.setAllowChainingAccrossTeams(updateDto.isAllowChainingAccrossTeams());
        settings.setBreakShiftAtMidnightForDisplay(updateDto.isBreakShiftAtMidnightForDisplay());
        settings.setBreakShiftAtMidnightForHours(updateDto.isBreakShiftAtMidnightForHours());
        settings.setConsecutiveLimitOf12hoursDays(updateDto.getConsecutiveLimitOf12hoursDays());
        settings.setForceCompletion(updateDto.isForceCompletion());
        settings.setProfileDayType(updateDto.getProfileDayType());
        settings.setReduceMaximumHoursForPTO(updateDto.isReduceMaximumHoursForPTO());
        settings.setOptimizationSettings(updateDto.getOptimizationSettings());
        organizationService.updateSchedulingSettings(settings);
        return toDto(org.getSchedulingSettings(), SchedulingSettingsDto.class);
	}

    @Validation
    public Collection<TeamManagersDto> getManagersByTeams(
            @Validate(validator = EntityExistValidatorBean.class, type = Organization.class)
            String tenantId,
            AccountACL accountAcl) {
        Collection<TeamManagersDto> result = new ArrayList<>();

        Collection<Object[]> rows = organizationService.getManagersByTeams(tenantId, accountAcl);

        for (Object[] row : rows) {
            TeamManagersDto teamManagersDto = null;
            String teamId = (String) row[0];
            for (TeamManagersDto dto : result) {
                if (StringUtils.equals(dto.getTeamId(), teamId)) {
                    teamManagersDto = dto;
                    break;
                }
            }
            if (teamManagersDto == null) {
                teamManagersDto = new TeamManagersDto();
                teamManagersDto.setTeamId(teamId);
                teamManagersDto.setTeamName((String) row[1]);

                result.add(teamManagersDto);
            }
            TeamManagersDto.ManagerDto managerDto = new TeamManagersDto.ManagerDto();
            managerDto.setEmployeeId((String) row[2]);
            managerDto.setName((String) row[5]);
            managerDto.setAccountId((String) row[4]);

            teamManagersDto.getManagers().add(managerDto);
        }

        return result;
    }

    @Validation
    public ResultSetDto<EmployeeRememberMeDto> getRememberMeObjects(
            @Validate(validator = EntityExistValidatorBean.class, type = Organization.class)
            String tenantId,
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        return rememberMeObjects(tenantId, filter, offset, limit, orderBy, orderDir);
    }

    @Validation
    public ResultSetDto<EmployeeRememberMeDto> getAllRememberMeObjects(
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        return rememberMeObjects(null, filter, offset, limit, orderBy, orderDir);
    }

    @Validation
    public Map<String, Integer> getCounters(
            @Validate(validator = EntityExistValidatorBean.class, type = Organization.class)
            String tenantId) {
        Map<String, Integer> result = new HashMap<>();

        List<Object[]> rows = organizationService.getCounters(tenantId);
        for (Object[] row : rows) {
            result.put((String) row[1], ((Number) row[0]).intValue());
        }

        return result;
    }

    private ResultSetDto<EmployeeRememberMeDto> rememberMeObjects(
            String tenantId, String filter, int offset, int limit, String orderBy, String orderDir)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SimpleQuery simpleQuery = new SimpleQuery(tenantId);
        simpleQuery.setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setEntityClass(RememberMe.class).setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC"))
                .setTotalCount(true);

        ResultSet<RememberMe> resultSet = organizationService.getRememberMeObjects(simpleQuery);

        DtoMapper<RememberMe, EmployeeRememberMeDto> dtoMapper = new DtoMapper<>();
        dtoMapper.registerExceptDtoFieldForMapping("employeeId");
        dtoMapper.registerExceptDtoFieldForMapping("employeeName");
        dtoMapper.registerExceptDtoFieldForMapping("tenantId");

        ResultSetDto<EmployeeRememberMeDto> result = dtoMapper.mapResultSet(resultSet, EmployeeRememberMeDto.class);

        for (EmployeeRememberMeDto dto : result.getResult()) {
            dto.setTenantId(tenantId);

            RememberMe rememberMe = ModelUtils.find(resultSet.getResult(), dto.getId());
            UserAccount userAccount = rememberMe.getUserAccount();
            dto.setEmployeeId(userAccount.getEmployeeId());
            dto.setEmployeeName(userAccount.getFirstName() + ' ' + userAccount.getLastName());
        }

        return result;
    }

}
