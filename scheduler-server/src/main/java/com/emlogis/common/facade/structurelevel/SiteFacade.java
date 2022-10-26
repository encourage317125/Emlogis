package com.emlogis.common.facade.structurelevel;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.facade.shiftpattern.ShiftLengthFacade;
import com.emlogis.common.services.shiftpattern.ShiftLengthService;
import com.emlogis.common.services.shiftpattern.ShiftTypeService;
import com.emlogis.common.validation.annotations.*;
import com.emlogis.model.employee.AbsenceTypeDeleteResult;
import com.emlogis.model.employee.dto.*;
import com.emlogis.model.schedule.Schedule;
import com.emlogis.model.schedule.dto.ScheduleDto;
import com.emlogis.model.shiftpattern.ShiftLength;
import com.emlogis.model.shiftpattern.ShiftType;
import com.emlogis.model.shiftpattern.dto.*;
import com.emlogis.model.structurelevel.ShiftDropReason;
import com.emlogis.model.structurelevel.dto.*;
import com.emlogis.rest.resources.util.DtoMapper;

import com.emlogis.rest.resources.util.SimpleQueryHelper;
import org.apache.commons.lang3.StringUtils;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.contract.ContractFacade;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.services.employee.AbsenceTypeService;
import com.emlogis.common.services.structurelevel.SiteService;
import com.emlogis.common.services.structurelevel.StructureLevelService;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.contract.SiteContract;
import com.emlogis.model.contract.dto.ContractDTO;
import com.emlogis.model.contract.dto.OvertimeDto;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.employee.AbsenceType;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.Skill;
import com.emlogis.model.structurelevel.PostOverrides;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.tenant.settings.SchedulingSettings;
import com.emlogis.model.tenant.settings.dto.SchedulingSettingsDto;
import com.emlogis.model.tenant.settings.dto.SchedulingSettingsUpdateDto;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class SiteFacade extends StructureLevelFacade {

    @EJB
    private ShiftTypeService shiftTypeService;

    @EJB
    private ShiftLengthFacade shiftLengthFacade;

    @EJB
    private ShiftLengthService shiftLengthService;

    @EJB
    private SiteService siteService;

    @EJB
    private ContractFacade contractFacade;
    
    @EJB
    private AbsenceTypeService absenceTypeService;

	@Override
	protected StructureLevelService getStructureLevelService() {
		return siteService;
	}

    @Validation
    public ResultSetDto<SiteDto> getObjects(
                String tenantId,
                String select,		// select is NOT IMPLEMENTED FOR NOW ..
                String filter,
                int offset,
                int limit,
                @ValidatePaging(name = Constants.ORDER_BY)
                String orderBy,
                @ValidatePaging(name = Constants.ORDER_DIR)
                String orderDir,
                AccountACL acl) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQuery simpleQuery = new SimpleQuery(tenantId);
        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
            .setOrderAscending(StringUtils.equals(orderDir, "ASC")).setTotalCount(true);
        SimpleQueryHelper.tryAddNotDeletedFilter(simpleQuery);

        ResultSet<Site> resultSet = siteService.findSites(simpleQuery, acl);
        return toResultSetDto(resultSet, SiteDto.class);
    }

    @Validation
    public SiteWithOvertimeDto getObject(
                @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
                PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Site site = siteService.getSite(primaryKey);

        DtoMapper<Site, SiteWithOvertimeDto> dtoMapper = new DtoMapper<>();
        dtoMapper.registerExceptDtoFieldForMapping("overtimeDto");

        SiteWithOvertimeDto dto = dtoMapper.map(site, SiteWithOvertimeDto.class);
        // Site dto created with an empty OvertimeDto sub object. 
        // now need to get Overtime configuration and set OvertimeDto attributes
        SiteContract contract = site.getSiteContracts().iterator().next();
        OvertimeDto overtimeDto = contractFacade.getOverTimeCLValues(contract);
        dto.setOvertimeDto(overtimeDto);
                
        return dto;
    }

    @Validation
    public SiteWithOvertimeDto updateObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey primaryKey,
            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = SiteUpdateDto.NAME, min = 1, max = 128, passNull = true),
                            @ValidateStrLength(field = SiteUpdateDto.DESCRIPTION, min = 0, max = 128, passNull = true),
                            @ValidateStrLength(field = SiteUpdateDto.ABBREVIATION, min = 2, max = 16, passNull = true),
                            @ValidateStrLength(field = SiteUpdateDto.ADDRESS, min = 0, max = 128, passNull = true),
                            @ValidateStrLength(field = SiteUpdateDto.ADDRESS2, min = 0, max = 128, passNull = true),
                            @ValidateStrLength(field = SiteUpdateDto.CITY, min = 0, max = 128, passNull = true),
                            @ValidateStrLength(field = SiteUpdateDto.STATE, min = 0, max = 128, passNull = true),
                            @ValidateStrLength(field = SiteUpdateDto.COUNTRY, min = 0, max = 128, passNull = true),
                            @ValidateStrLength(field = SiteUpdateDto.ZIP, min = 0, max = 128, passNull = true),
                    },
                    uniques = {
                            @ValidateUnique(fields = SiteUpdateDto.NAME, type = Site.class)
                    },
                    regexes = {
                            @ValidateRegex(field = SiteUpdateDto.ZIP, regex = "\\d+"),
                            @ValidateRegex(field = SiteUpdateDto.SHIFT_INCREMENTS, regex = "\\d+"),
                            @ValidateRegex(field = SiteUpdateDto.SHIFT_OVERLAPS, regex = "\\d+"),
                            @ValidateRegex(field = SiteUpdateDto.MAX_CONSECUTIVE_SHIFTS, regex = "\\d+"),
                            @ValidateRegex(field = SiteUpdateDto.TIME_OFF_BETWEEN_SHIFTS, regex = "\\d+")
                    }
            )
            SiteUpdateDto siteUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (isEmpty(siteUpdateDto.getAddress()) && StringUtils.isNotEmpty(siteUpdateDto.getAddress2())) {
            throw new ValidationException(getMessage("validation.error.address.address2"));
        }

        boolean modified = false;
        Site site = siteService.getSite(primaryKey);

        if (StringUtils.isNotBlank(siteUpdateDto.getName())) {
            site.setName(siteUpdateDto.getName());
            modified = true;
        }
        if (StringUtils.isNotBlank(siteUpdateDto.getDescription())) {
            site.setDescription(siteUpdateDto.getDescription());
            modified = true;
        }
        if (StringUtils.isNotBlank(siteUpdateDto.getTimeZone())) {
            site.setTimeZone(DateTimeZone.forID(siteUpdateDto.getTimeZone()));
            modified = true;
        }
        if (siteUpdateDto.getFirstDayOfWeek() != null) {
            site.setFirstDayOfWeek(siteUpdateDto.getFirstDayOfWeek());
            modified = true;
        }
        if (siteUpdateDto.getWeekendDefinition() != null) {
            site.setWeekendDefinition(siteUpdateDto.getWeekendDefinition());
            modified = true;
        }
        if (siteUpdateDto.getTwoWeeksOvertimeStartDate() > 0) {
            site.setTwoWeeksOvertimeStartDate(siteUpdateDto.getTwoWeeksOvertimeStartDate());
            modified = true;
        }
        if (StringUtils.isNotBlank(siteUpdateDto.getAddress())) {
            site.setAddress(siteUpdateDto.getAddress());
            modified = true;
        }
        if (StringUtils.isNotBlank(siteUpdateDto.getAddress2())) {
            site.setAddress2(siteUpdateDto.getAddress2());
            modified = true;
        }
        if (StringUtils.isNotBlank(siteUpdateDto.getAbbreviation())) {
            site.setAbbreviation(siteUpdateDto.getAbbreviation());
            modified = true;
        }
        if (StringUtils.isNotBlank(siteUpdateDto.getZip())) {
            site.setZip(siteUpdateDto.getZip());
            modified = true;
        }
        if (StringUtils.isNotBlank(siteUpdateDto.getCity())) {
            site.setCity(siteUpdateDto.getCity());
            modified = true;
        }
        if (StringUtils.isNotBlank(siteUpdateDto.getCountry())) {
            site.setCountry(siteUpdateDto.getCountry());
            modified = true;
        }
        if (StringUtils.isNotBlank(siteUpdateDto.getState())) {
            site.setState(siteUpdateDto.getState());
            modified = true;
        }
        if (siteUpdateDto.getShiftIncrements() > 0) {
            site.setShiftIncrements(siteUpdateDto.getShiftIncrements());
            modified = true;
        }
        if (siteUpdateDto.getShiftOverlaps() > 0) {
            site.setShiftOverlaps(siteUpdateDto.getShiftOverlaps());
            modified = true;
        }
        if (siteUpdateDto.getMaxConsecutiveShifts() > 0) {
            site.setMaxConsecutiveShifts(siteUpdateDto.getMaxConsecutiveShifts());
            modified = true;
        }
        if (siteUpdateDto.getTimeOffBetweenShifts() > 0) {
            site.setTimeOffBetweenShifts(siteUpdateDto.getTimeOffBetweenShifts());
            modified = true;
        }
        if (siteUpdateDto.isEnableWIPFragments() != null) {
            site.setEnableWIPFragments(siteUpdateDto.isEnableWIPFragments());
            modified = true;
        }
        if (siteUpdateDto.getIsNotificationEnabled() != null) {
            site.setIsNotificationEnabled(siteUpdateDto.getIsNotificationEnabled());
            modified = true;
        }
        
    	OvertimeDto overtimeDto = siteUpdateDto.getOvertimeDto();
    	if (overtimeDto != null) {
    		// TODO update overtime contract lines as needed (if needed)
    		// Get the site contract
    		SiteContract contract = site.getSiteContracts().iterator().next();
    		
    		contractFacade.updateOverTimeContractLines(contract, overtimeDto);
    		modified = true;
    	}

        if (modified) {
            setUpdatedBy(site);
        	site = siteService.update(site);
            getEventService().sendEntityUpdateEvent(site, SiteDto.class);
        }

        return getObject(primaryKey);
    }

    @Validation
    public SiteWithOvertimeDto createObject(
                @Validate(validator = EntityExistValidatorBean.class, type = Site.class, expectedResult = false)
                PrimaryKey primaryKey,
                @ValidateAll(
                        strLengths = {
                            @ValidateStrLength(field = SiteCreateDto.NAME, min = 1, max = 128, passNull = false),
                            @ValidateStrLength(field = SiteCreateDto.DESCRIPTION, min = 0, max = 128, passNull = true),
                            @ValidateStrLength(field = SiteCreateDto.ABBREVIATION, min = 2, max = 16, passNull = true),
                            @ValidateStrLength(field = SiteCreateDto.ADDRESS, min = 0, max = 128, passNull = true),
                            @ValidateStrLength(field = SiteCreateDto.ADDRESS2, min = 0, max = 128, passNull = true),
                            @ValidateStrLength(field = SiteCreateDto.CITY, min = 0, max = 128, passNull = true),
                            @ValidateStrLength(field = SiteCreateDto.STATE, min = 0, max = 128, passNull = true),
                            @ValidateStrLength(field = SiteCreateDto.COUNTRY, min = 0, max = 128, passNull = true),
                            @ValidateStrLength(field = SiteCreateDto.ZIP, min = 0, max = 128, passNull = true),
                        },
                        uniques = {
                            @ValidateUnique(fields = SiteCreateDto.NAME, type = Site.class)
                        },
                        regexes = {
                            @ValidateRegex(field = SiteCreateDto.ZIP, regex = "\\d+"),
                            @ValidateRegex(field = SiteCreateDto.SHIFT_INCREMENTS, regex = "\\d+"),
                            @ValidateRegex(field = SiteCreateDto.SHIFT_OVERLAPS, regex = "\\d+"),
                            @ValidateRegex(field = SiteCreateDto.MAX_CONSECUTIVE_SHIFTS, regex = "\\d+"),
                            @ValidateRegex(field = SiteCreateDto.TIME_OFF_BETWEEN_SHIFTS, regex = "\\d+")
                        }
                )
                SiteCreateDto siteCreateDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        SiteUpdateDto updateDto = siteCreateDto.getUpdateDto();

        Site site = siteService.createSite(primaryKey);
        
        // Create default contract for Site
        SiteContract siteContract = contractFacade.createSiteContract(site, true);        
        site.getSiteContracts().add(siteContract);

        if (updateDto != null) {
        	updateObject(primaryKey, updateDto);
        }
        // set path for ACL
		site.setPath(site.getId());

        setCreatedBy(site);
        setOwnedBy(site, this.getActualUserId());

        Integer[] defaultShiftLengths = siteCreateDto.getDefaultShiftLengths();
        if (defaultShiftLengths != null && defaultShiftLengths.length > 0) {
            for (Integer length : defaultShiftLengths) {
                ShiftLength shiftLength = new ShiftLength(new PrimaryKey(site.getTenantId()));
                shiftLength.setSite(site);
                shiftLength.setLengthInMin(length);
                shiftLength.setName(length.toString() + " min");

                shiftLengthService.persist(shiftLength);

                Set<ShiftLength> shiftLengths = site.getShiftLengths();
                if (shiftLengths == null) {
                    shiftLengths = new HashSet<>();
                    site.setShiftLengths(shiftLengths);
                }
                shiftLengths.add(shiftLength);
            }
        }

        siteService.update(site);
        getEventService().sendEntityCreateEvent(site, SiteDto.class);
        return getObject(primaryKey);
    }

    @Validation
    public boolean softDeleteObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class) PrimaryKey primaryKey) {
        Site site = siteService.getSite(primaryKey);
        // TODO site delete must likely cascade delete to a whole bunch of entities / structurelevel / schedules, etc
        // should we do that ?
        siteService.softDelete(site);
        getEventService().sendEntityDeleteEvent(site, SiteDto.class);
        return true;
    }

    @Validation
    public boolean hardDeleteObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class) PrimaryKey primaryKey) {
        Site site = siteService.getSite(primaryKey);
        siteService.hardDelete(site);
        getEventService().sendEntityDeleteEvent(site, SiteDto.class);
        return true;
    }

    /**
     * Get a collection of Skills associated to the Site specified by the PrimaryKey
     * @param sitePrimaryKey
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    @Validation
	public ResultSetDto<SkillDto> getSkills(
			@Validate(validator = EntityExistValidatorBean.class, type = Site.class) 
			PrimaryKey sitePrimaryKey,
            String select,
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
	        NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(sitePrimaryKey.getTenantId());
        simpleQuery.setEntityClass(Skill.class).setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit)
                .setOrderByField(orderBy).setOrderAscending(StringUtils.equals(orderDir, "ASC")).setTotalCount(true);
        ResultSet<Skill> resultSet = siteService.getSkills(sitePrimaryKey, simpleQuery);
	    return this.toResultSetDto(resultSet, SkillDto.class);
	}

    /**
     * Add Skill specified by id to Site specified by PrimaryKey
     * @param sitePrimaryKey
     * @param skillId
     */
	@Validation
    public void addSkill(
    		@Validate(validator = EntityExistValidatorBean.class, type = Site.class) 
    		PrimaryKey sitePrimaryKey,
    		String skillId) {
        siteService.addSkill(sitePrimaryKey, skillId);
    }

	/**
	 * Remove Skill specified by id to Team specified by PrimaryKey
	 * @param sitePrimaryKey
	 * @param skillId
	 */
    @Validation
	public void removeSkill(
			@Validate(validator = EntityExistValidatorBean.class, type = Site.class) PrimaryKey sitePrimaryKey,
			String skillId) {
		siteService.removeSkill(sitePrimaryKey, skillId);
	}

    @Validation
    public ResultSetDto<SkillDto> getUnassociatedSkills(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class) PrimaryKey primaryKey,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(primaryKey.getTenantId());
        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equals(orderDir, "ASC")).setTotalCount(true)
                .setEntityClass(Skill.class);
        ResultSet<Skill> resultSet = siteService.getUnassociatedSkills(primaryKey, simpleQuery);
        return toResultSetDto(resultSet, SkillDto.class);
    }

    @Validation
    public ResultSetDto<TeamDto> getTeams(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey primaryKey,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir,
            AccountACL acl) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(primaryKey.getTenantId());
        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true)
                .setEntityClass(Team.class).setAcl(acl);

        if (StringUtils.isEmpty(simpleQuery.getFilter()) || !simpleQuery.getFilter().contains("isDeleted")) {
            simpleQuery.addFilter("isDeleted=false");
        }

        ResultSet<Team> resultSet = siteService.getTeams(primaryKey, simpleQuery);
        return toResultSetDto(resultSet, TeamDto.class);
    }

    @Validation
    public ResultSetDto<TeamSkillsDto> getTeamsSkills(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey primaryKey,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir,
            AccountACL acl) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(primaryKey.getTenantId());
        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true)
                .setEntityClass(Team.class).setAcl(acl);

        if (StringUtils.isEmpty(simpleQuery.getFilter()) || !simpleQuery.getFilter().contains("isDeleted")) {
            simpleQuery.addFilter("isDeleted=false");
        }

        ResultSet<Team> resultSet = siteService.getTeams(primaryKey, simpleQuery);
        ResultSet<TeamSkillsDto> newRS = new ResultSet<>();
        newRS.setResult(new ArrayList<TeamSkillsDto>());

        Collection<Team> teams = (resultSet!=null)? resultSet.getResult():null;
        if (teams!=null){
	        for(Team team : teams) {
	            Collection<SkillDto> skills = new DtoMapper<Skill, SkillDto>().map(team.getSkills(), SkillDto.class);
	            TeamSkillsDto dto = toDto(team, TeamSkillsDto.class);
	            dto.setSkills(skills);
	            newRS.getResult().add(dto);
	        }
        }
        return toResultSetDto(newRS, TeamSkillsDto.class);
    }
    
	public ResultSetDto<ContractDTO> getContracts(
			@Validate(validator = EntityExistValidatorBean.class, type = Employee.class) 
			PrimaryKey sitePrimaryKey,
            String select, // select is NOT IMPLEMENTED FOR NOW ...
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
		Site site = siteService.getSite(sitePrimaryKey);
		
		filter = "site.primaryKey.id="+ "'" + site.getId()  + "' ";
		
		return contractFacade.getObjects(sitePrimaryKey.getTenantId(),select, filter, offset, limit, orderBy, orderDir);
        
	}

    @Validation
    public ResultSetDto<ShiftTypeDto> getShiftTypes(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey sitePrimaryKey,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQuery simpleQuery = new SimpleQuery(sitePrimaryKey.getTenantId());
        simpleQuery.setSelect(select).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setFilter(filter).addFilter("site.primaryKey.id='" + sitePrimaryKey.getId() + "'")
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true);
        if (!simpleQuery.getFilter().contains("isActive")) {
            simpleQuery.addFilter("isActive = TRUE");
        }
        ResultSet<ShiftType> resultSet = shiftTypeService.findShiftTypes(simpleQuery);
        return toResultSetDto(resultSet, ShiftTypeDto.class);
    }

    @Validation
    public ShiftTypeDto createShiftType(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey sitePrimaryKey,
            @ValidateStrLength(field = "shiftLengthId", min = 1, passNull = false)
            ShiftTypeCreateDto shiftTypeCreateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String tenantId = sitePrimaryKey.getTenantId();

        PrimaryKey shiftTypePrimaryKey = new PrimaryKey(tenantId, shiftTypeCreateDto.getId());
        ShiftType shiftType = shiftTypeService.create(shiftTypePrimaryKey);

        PrimaryKey lengthPrimaryKey = new PrimaryKey(tenantId, shiftTypeCreateDto.getShiftLengthId());
        ShiftLength shiftLength = shiftLengthService.getShiftLength(lengthPrimaryKey);
        shiftType.setShiftLength(shiftLength);

        Site site = siteService.getSite(sitePrimaryKey);
        shiftType.setSite(site);

        ShiftTypeUpdateDto updateDto = shiftTypeCreateDto.getUpdateDto();
        if (updateDto != null) {
            shiftType.setStartTime(new LocalTime(updateDto.getStartTime()));
            shiftType.setName(updateDto.getName());
            if (updateDto.getPaidTimeInMin() != null) {
                shiftType.setPaidTimeInMin(updateDto.getPaidTimeInMin());
            }
        }

        setCreatedBy(shiftType);
        setOwnedBy(shiftType);

        shiftType = shiftTypeService.update(shiftType);

        return toDto(shiftType, ShiftTypeDto.class);
    }

    @Validation
    public ShiftTypeDto getShiftType(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftType.class)
            PrimaryKey shiftTypePrimaryKey) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        ShiftType shiftLength = shiftTypeService.getShiftType(shiftTypePrimaryKey);
        return toDto(shiftLength, ShiftTypeDto.class);
    }

    @Validation
    public ShiftTypeDto updateShiftType(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey sitePrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftType.class)
            PrimaryKey shiftTypePrimaryKey,
            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = ShiftTypeDto.NAME, min = 1, max = 256, passNull = true),
                            @ValidateStrLength(field = ShiftTypeDto.DESCRIPTION, min = 1, max = 256, passNull = true)
                    }
            )
            ShiftTypeUpdateDto shiftTypeUpdateDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        ShiftType shiftType = shiftTypeService.getShiftType(shiftTypePrimaryKey);
        Site site = siteService.getSite(sitePrimaryKey);

        if (!site.equals(shiftType.getSite())) {
            throw new ValidationException(getMessage("validation.shifttype.site.error", shiftType.getId(),
                    site.getName()));
        }

        boolean modified = false;

        if (StringUtils.isNotBlank(shiftTypeUpdateDto.getName())) {
            shiftType.setName(shiftTypeUpdateDto.getName());
            modified = true;
        }
        if (StringUtils.isNotBlank(shiftTypeUpdateDto.getDescription())) {
            shiftType.setDescription(shiftTypeUpdateDto.getDescription());
            modified = true;
        }
        if (shiftTypeUpdateDto.getStartTime() != null) {
            shiftType.setStartTime(new LocalTime(shiftTypeUpdateDto.getStartTime()));
            modified = true;
        }
        if (shiftType.isActive() != shiftTypeUpdateDto.isActive()) {
            shiftType.setActive(shiftTypeUpdateDto.isActive());
            modified = true;
        }
        if (shiftTypeUpdateDto.getPaidTimeInMin() != null) {
            shiftType.setPaidTimeInMin(shiftTypeUpdateDto.getPaidTimeInMin());
        }

        if (modified) {
            setUpdatedBy(shiftType);
            shiftType = shiftTypeService.update(shiftType);
        }

        return toDto(shiftType, ShiftTypeDto.class);
    }

    @Validation
    public void removeShiftType(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey sitePrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftType.class)
            PrimaryKey shiftTypePrimaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ShiftType shiftType = shiftTypeService.getShiftType(shiftTypePrimaryKey);
        Site site = siteService.getSite(sitePrimaryKey);

        if (!site.equals(shiftType.getSite())) {
            throw new ValidationException(getMessage("validation.shifttype.site.error", shiftType.getId(),
                    site.getName()));
        }

        shiftTypeService.delete(shiftType);
    }

    @Validation
    public ResultSetDto<ShiftLengthDto> getShiftLengths(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey sitePrimaryKey,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQuery simpleQuery = new SimpleQuery(sitePrimaryKey.getTenantId());
        simpleQuery.setSelect(select).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setFilter(filter).addFilter("site.primaryKey.id='" + sitePrimaryKey.getId() + "'")
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true);
        if (!simpleQuery.getFilter().contains("active")) {
            simpleQuery.addFilter("active = TRUE");
        }
        ResultSet<ShiftLength> resultSet = shiftLengthService.findShiftLengths(simpleQuery);
        return toResultSetDto(resultSet, ShiftLengthDto.class);
    }

    @Validation
    public ShiftLengthDto getShiftLength(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftLength.class)
            PrimaryKey shiftLengthPrimaryKey) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        ShiftLength shiftLength = shiftLengthService.getShiftLength(shiftLengthPrimaryKey);
        return toDto(shiftLength, ShiftLengthDto.class);
    }


    @Validation
    public ShiftLengthDto createShiftLength(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey sitePrimaryKey,
            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = ShiftLengthDto.NAME, min = 1, max = 256)
                    }
            )
            ShiftLengthCreateDto shiftLengthCreateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String tenantId = sitePrimaryKey.getTenantId();

        ShiftLength shiftLength = null;

        ShiftLengthUpdateDto updateDto = shiftLengthCreateDto.getUpdateDto();
        if (updateDto != null) {
            checkUniqueLengthForSite(updateDto.getLengthInMin(), sitePrimaryKey, null);

            PrimaryKey shiftLengthPrimaryKey = new PrimaryKey(tenantId, shiftLengthCreateDto.getId());
            shiftLength = shiftLengthService.create(shiftLengthPrimaryKey);
            shiftLength.setName(shiftLengthCreateDto.getName());

            shiftLength.setDescription(updateDto.getDescription());
            shiftLength.setLengthInMin(updateDto.getLengthInMin() == null ? 0 : updateDto.getLengthInMin());
            shiftLength.setActive(updateDto.isActive());

            Site site = siteService.getSite(sitePrimaryKey);
            shiftLength.setSite(site);

            setCreatedBy(shiftLength);
            setOwnedBy(shiftLength);

            shiftLength = shiftLengthService.update(shiftLength);
        }

        return toDto(shiftLength, ShiftLengthDto.class);
    }

    @Validation
    public Collection<ShiftLengthDto> createMultipleShiftLengths(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey sitePrimaryKey,
            MultipleShiftLengthDto multipleShiftLengthDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Collection<MultipleShiftLengthDto.CreateShiftLengthDto> dtos = multipleShiftLengthDto.getShiftLengthDtos();

        // check IDs first
        Set<Integer> lengthSet = new HashSet<>();
        if (dtos!=null){
	        for (MultipleShiftLengthDto.CreateShiftLengthDto dto : dtos) {
	            checkUniqueLengthForSite(dto.getLengthInMin(), sitePrimaryKey, null);
	
	            // check that length is unique in incoming collection
	            if (lengthSet.contains(dto.getLengthInMin())) {
	                throw new ValidationException(getMessage("validation.site.notunique.lengths.collection"));
	            } else {
	                lengthSet.add(dto.getLengthInMin());
	            }
	        }
        }

        Collection<ShiftLengthDto> result = new ArrayList<>();

        Site site = siteService.getSite(sitePrimaryKey);

        String tenantId = sitePrimaryKey.getTenantId();

        if (dtos != null) {
	        for (MultipleShiftLengthDto.CreateShiftLengthDto dto : dtos) {
	            PrimaryKey shiftLengthPrimaryKey = new PrimaryKey(tenantId);
	            ShiftLength shiftLength = shiftLengthService.create(shiftLengthPrimaryKey);

                int lengthHours = dto.getLengthInMin() / 60;
                int lengthMinutes = dto.getLengthInMin() % 60;
                if (lengthMinutes > 0) {
                    shiftLength.setName(String.format("%d-hours %d-min", lengthHours, lengthMinutes));
                } else {
                    shiftLength.setName(String.format("%d-hours", lengthHours));
                }
	            shiftLength.setLengthInMin(dto.getLengthInMin());
	            shiftLength.setActive(dto.isActive());
	
	            shiftLength.setSite(site);
	
	            setCreatedBy(shiftLength);
	            setOwnedBy(shiftLength);
	
	            shiftLength = shiftLengthService.update(shiftLength);
	
	            result.add(toDto(shiftLength, ShiftLengthDto.class));
	        }
        }

        return result;
    }

    @Validation
    public ShiftLengthDto updateShiftLength(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey sitePrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftLength.class)
            PrimaryKey shiftLengthPrimaryKey,
            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = ShiftLengthDto.NAME, min = 1, max = 256, passNull = true)
                    }
            )
            ShiftLengthUpdateDto shiftLengthUpdateDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        ShiftLength shiftLength = shiftLengthService.getShiftLength(shiftLengthPrimaryKey);
        Site site = siteService.getSite(sitePrimaryKey);

        if (!site.equals(shiftLength.getSite())) {
            throw new ValidationException(getMessage("validation.shiftlength.site.error", shiftLength.getId(),
                    site.getName()));
        }

        boolean modified = false;

        if (StringUtils.isNotBlank(shiftLengthUpdateDto.getName())) {
            shiftLength.setName(shiftLengthUpdateDto.getName());
            modified = true;
        }
        if (StringUtils.isNotBlank(shiftLengthUpdateDto.getDescription())) {
            shiftLength.setDescription(shiftLengthUpdateDto.getDescription());
            modified = true;
        }
        if (shiftLengthUpdateDto.getLengthInMin() != null) {
            shiftLength.setLengthInMin(shiftLengthUpdateDto.getLengthInMin());
            modified = true;
        }
        if (shiftLengthUpdateDto.isActive() != shiftLength.isActive()) {
            shiftLength.setActive(shiftLengthUpdateDto.isActive());
            modified = true;
        }

        if (modified) {
            checkUniqueLengthForSite(shiftLength.getLengthInMin(), sitePrimaryKey, shiftLengthPrimaryKey);

            setUpdatedBy(shiftLength);
            shiftLength = shiftLengthService.update(shiftLength);
        }

        return toDto(shiftLength, ShiftLengthDto.class);
    }

    @Validation
    public void removeShiftLength(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey sitePrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftLength.class)
            PrimaryKey shiftLengthPrimaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ShiftLength shiftLength = shiftLengthService.getShiftLength(shiftLengthPrimaryKey);
        Site site = siteService.getSite(sitePrimaryKey);

        if (!site.equals(shiftLength.getSite())) {
            throw new ValidationException(getMessage("validation.shiftlength.site.error", shiftLength.getId(),
                    site.getName()));
        }

        shiftLengthService.delete(shiftLength);
    }

    @Validation
    public Collection<ShiftLengthDto> updateActivationShiftLengths(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey sitePrimaryKey,
            Map<String, Boolean> updateActivationMap) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Collection<ShiftLengthDto> result = new ArrayList<>();

        Site site = siteService.getSite(sitePrimaryKey);

        if (updateActivationMap != null) {
	        for (Map.Entry<String, Boolean> entry : updateActivationMap.entrySet()) {
	            String shiftLengthId = entry.getKey();
	            PrimaryKey shiftLengthPrimaryKey = new PrimaryKey(sitePrimaryKey.getTenantId(), shiftLengthId);
	            ShiftLength shiftLength = shiftLengthService.getShiftLength(shiftLengthPrimaryKey);
	
	            if (shiftLength == null) {
	                throw new ValidationException(getMessage("validation.shiftlength.not.exist", shiftLengthId));
	            }
	            if (!site.equals(shiftLength.getSite())) {
	                throw new ValidationException(getMessage("validation.shiftlength.site.error", shiftLengthId,
	                        site.getName()));
	            }
	
	            shiftLength.setActive(entry.getValue());
	
	            shiftLengthService.update(shiftLength);
	
	            result.add(toDto(shiftLength, ShiftLengthDto.class));
	        }
        }
        return result;
    }

    @Validation
    public Collection<ShiftTypeDto> updateActivationShiftTypes(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey sitePrimaryKey,
            Map<String, Boolean> updateActivationMap) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Collection<ShiftTypeDto> result = new ArrayList<>();

        Site site = siteService.getSite(sitePrimaryKey);
     
        if (updateActivationMap != null) {
	        for (Map.Entry<String, Boolean> entry : updateActivationMap.entrySet()) {
	            String shiftTypeId = entry.getKey();
	            PrimaryKey shiftTypePrimaryKey = new PrimaryKey(sitePrimaryKey.getTenantId(), shiftTypeId);
	            ShiftType shiftType = shiftTypeService.getShiftType(shiftTypePrimaryKey);
	
	            if (shiftType == null) {
	                throw new ValidationException(getMessage("validation.shifttype.not.exist", shiftTypeId));
	            }
	            if (!site.equals(shiftType.getSite())) {
	                throw new ValidationException(getMessage("validation.shifttype.site.error", shiftTypeId,
	                        site.getName()));
	            }
	
	            shiftType.setActive(entry.getValue());
	
	            shiftTypeService.update(shiftType);
	
	            result.add(toDto(shiftType, ShiftTypeDto.class));
	        }
        }

        return result;
    }

    @Validation
    public Collection<ShiftTypeDto> getShiftTypesOfShiftLength(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey sitePrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftLength.class)
            PrimaryKey shiftLengthPrimaryKey) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Site site = siteService.getSite(sitePrimaryKey);
        ShiftLength shiftLength = shiftLengthService.getShiftLength(shiftLengthPrimaryKey);
        if (!site.equals(shiftLength.getSite())) {
            throw new ValidationException(getMessage("validation.shiftlength.site.error", shiftLength.getId(),
                    site.getName()));
        }

        return toCollectionDto(shiftLength.getShiftTypes(), ShiftTypeDto.class);
    }

    @Validation
    public Collection<ShiftTypeDto> createMultipleShiftTypes(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey sitePrimaryKey,
            MultipleShiftTypeDto multipleShiftTypeDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        String shiftLengthId = multipleShiftTypeDto.getShiftLengthId();
        ShiftLength shiftLength = shiftLengthService.getShiftLength(
                new PrimaryKey(sitePrimaryKey.getTenantId(), shiftLengthId));
        if (shiftLength == null) {
            throw new ValidationException(getMessage("validation.shiftlength.not.exist", shiftLengthId));
        }
        int interval = multipleShiftTypeDto.getInterval() * 60 * 1000;
        if (interval <= 0) {
            throw new ValidationException(getMessage("validation.multiple.shifttypes.interval.error"));
        }
        if (multipleShiftTypeDto.getStartTime() >= multipleShiftTypeDto.getEndTime()) {
            throw new ValidationException(getMessage("validation.error.startdate.enddate"));
        }

        Collection<ShiftTypeDto> result = new ArrayList<>();

        Locale locale = Locale.US;
        String baseName = isEmpty(multipleShiftTypeDto.getBaseName()) ? StringUtils.EMPTY
                : multipleShiftTypeDto.getBaseName();

        for (long startTime = multipleShiftTypeDto.getStartTime();
             startTime <= multipleShiftTypeDto.getEndTime();
             startTime += interval) {
            ShiftTypeCreateDto shiftTypeCreateDto = new ShiftTypeCreateDto();
            ShiftTypeUpdateDto shiftTypeUpdateDto = new ShiftTypeUpdateDto();

            shiftTypeCreateDto.setUpdateDto(shiftTypeUpdateDto);
            shiftTypeCreateDto.setShiftLengthId(shiftLengthId);
            shiftTypeUpdateDto.setActive(multipleShiftTypeDto.getIsActive());
            shiftTypeUpdateDto.setStartTime(startTime);

            long endTime = startTime + shiftLength.getLengthInMin() * 1000 * 60;

            shiftTypeUpdateDto.setName(baseName + " " + EmlogisUtils.toShortLocaleTimeString(startTime, locale) + "-"
                    + EmlogisUtils.toShortLocaleTimeString(endTime, locale));

            ShiftTypeDto shiftTypeDto = createShiftType(sitePrimaryKey, shiftTypeCreateDto);
            result.add(shiftTypeDto);
        }

        return result;
    }

    /**
     * Get queried collection of AbsenceTypes
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
    public ResultSetDto<AbsenceTypeDto> getAbsenceTypes(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
    		PrimaryKey sitePrimaryKey,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQuery simpleQuery = new SimpleQuery(sitePrimaryKey.getTenantId());
        if (StringUtils.isNotEmpty(filter)) {
            filter = filter + " AND " + " site.primaryKey.id=" + "'" + sitePrimaryKey.getId()  + "' ";
        } else {
            filter = "site.primaryKey.id=" + "'" + sitePrimaryKey.getId() + "' ";
        }
        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true);
        ResultSet<AbsenceType> rs = absenceTypeService.findAbsenceTypes(simpleQuery);
        return toResultSetDto(rs, AbsenceTypeDto.class);
    }
    
    @Validation
    public ResultSetDto<AbsenceTypeDto> getDropShiftReasonsAbsenceTypes(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
    		PrimaryKey sitePrimaryKey,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        ResultSet<Object[]> resultSet = siteService.getDropShiftReasonsAbsenceTypes(sitePrimaryKey, filter, offset,
                limit, orderBy, orderDir);

        Collection<AbsenceTypeDto> absenceTypeDtos = new ArrayList<>();
        for (Object[] row : resultSet.getResult()) {
            AbsenceTypeDto absenceTypeDto = new AbsenceTypeDto();
            absenceTypeDto.setId((String) row[0]);
            absenceTypeDto.setName((String) row[1]);
            absenceTypeDto.setDescription((String) row[2]);
            absenceTypeDto.setTimeToDeductInMin(((Number) row[3]).intValue());
            absenceTypeDto.setSiteId((String) row[4]);
            if (row[5] instanceof Number) {
                absenceTypeDto.setActive(((Number) row[3]).intValue() != 0);
            } else {
                absenceTypeDto.setActive((boolean) row[5]);
            }
            if (StringUtils.equals((String) row[6], "AbsenceType")) {
                absenceTypeDto.setClName(AbsenceType.class.getName());
            } else if (StringUtils.equals((String) row[6], "ShiftDropReason")) {
                absenceTypeDto.setClName(ShiftDropReason.class.getName());
            }

            absenceTypeDtos.add(absenceTypeDto);
        }

        ResultSetDto<AbsenceTypeDto> result = new ResultSetDto<>();
        result.setTotal(resultSet.getTotal());
        result.setResult(absenceTypeDtos);

        return result;
    }

    /**
     * Get specified AbsenceType
     *
     * @param primaryKey
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public AbsenceTypeDto getAbsenceType(
            @Validate(validator = EntityExistValidatorBean.class, type = AbsenceType.class)
            PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        AbsenceType absenceType = absenceTypeService.getAbsenceType(primaryKey);
        return toDto(absenceType, AbsenceTypeDto.class);
    }

    /**
     * Update absenceType
     * @param primaryKey
     * @param absenceTypeUpdateDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public AbsenceTypeDto updateAbsenceType(
            @Validate(validator = EntityExistValidatorBean.class, type = AbsenceType.class)
            PrimaryKey primaryKey,
            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = AbsenceTypeUpdateDto.NAME, min = 1, max = 32, passNull = true),         // (max 32 in legacy app)
                            @ValidateStrLength(field = AbsenceTypeUpdateDto.DESCRIPTION, min = 1, max = 255, passNull = true)   // (max 50 in legacy app)
                    },
                    uniques = {
                            @ValidateUnique(fields = AbsenceTypeCreateDto.NAME, type = AbsenceType.class)
                    }
            )
            AbsenceTypeUpdateDto absenceTypeUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        boolean modified = false;
        AbsenceType absenceType = absenceTypeService.getAbsenceType(primaryKey);

        if (StringUtils.isNotBlank(absenceTypeUpdateDto.getName())) {
            absenceType.setName(absenceTypeUpdateDto.getName());
            modified = true;
        }
        if (StringUtils.isNotBlank(absenceTypeUpdateDto.getDescription())) {
            absenceType.setDescription(absenceTypeUpdateDto.getDescription());
            modified = true;
        }
        if (absenceTypeUpdateDto.isActive() != absenceType.isActive()) {
            absenceType.setActive(absenceTypeUpdateDto.isActive());;
            modified = true;
        }

        if (modified) {
            absenceType.setTimeToDeductInMin(absenceTypeUpdateDto.getTimeToDeductInMin());
            setUpdatedBy(absenceType);
            absenceType = absenceTypeService.update(absenceType);
        }

        return toDto(absenceType, AbsenceTypeDto.class);
    }
    
    @Validation
    public AbsenceTypeDto createAbsenceType(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey siteKey,
            @Validate(validator = EntityExistValidatorBean.class, type = AbsenceType.class, expectedResult = false)
            PrimaryKey primaryKey,
            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = AbsenceTypeCreateDto.NAME, min = 1, max = 32),         // (max 32 in legacy app)
                            @ValidateStrLength(field = AbsenceTypeCreateDto.DESCRIPTION, min = 1, max = 255, passNull = true)   // (max 50 in legacy app)
                    },
                    uniques = {
                            @ValidateUnique(fields = AbsenceTypeCreateDto.NAME, type = AbsenceType.class)
                    }
            )
            AbsenceTypeCreateDto absenceTypeCreateDTO)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	Site site = siteService.getSite(siteKey);

        AbsenceType absenceType = absenceTypeService.createAbsenceType(primaryKey, absenceTypeCreateDTO.getName(),
                absenceTypeCreateDTO.getTimeToDeductInMin(), absenceTypeCreateDTO.getDescription(),
                absenceTypeCreateDTO.isActive(), site);
        setCreatedBy(absenceType);
        setOwnedBy(absenceType, null);

        absenceType = absenceTypeService.update(absenceType);
        
        site.addAbsenceType(absenceType);

        return toDto(absenceType, AbsenceTypeDto.class);
    }

    /**
     * Delete AbsenceType
     * @param primaryKey
     * @return
     */
    @Validation
    public AbsenceTypeDeleteResult deleteAbsenceType(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class) PrimaryKey siteKey,
            @Validate(validator = EntityExistValidatorBean.class, type = AbsenceType.class) PrimaryKey primaryKey){
        AbsenceType absenceType = absenceTypeService.getAbsenceType(primaryKey);
        Site site = siteService.getSite(siteKey);
        return absenceTypeService.delete(absenceType, site);
    }

    public Collection<QueryDto> siteTeamSkills(
            String tenantId,
            String searchValue,
            String searchFields,
            String siteFilter,
            String teamFilter,
            String skillFilter,
            AccountACL acl) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        Collection<QueryDto> result = new ArrayList<>();

        List<Object[]> objectList = siteService.siteTeamSkills(tenantId, searchValue, searchFields, siteFilter,
                teamFilter, skillFilter, acl);
        if (objectList != null) {
            for (Object[] objects : objectList) {
                String siteId = (String) objects[0];
                QueryDto siteDto = findById(result, siteId);
                if (siteDto == null) {
                    siteDto = new QueryDto();
                    siteDto.setId(siteId);
                    siteDto.setClName(Site.class.getSimpleName());
                    siteDto.setName((String) objects[1]);
                    siteDto.setChildren(new ArrayList<QueryDto>());

                    result.add(siteDto);
                }
                String teamId = (String) objects[2];
                if (teamId != null) {
                    Collection<QueryDto> teamSkillDtos = siteDto.getChildren();
                    QueryDto teamSkillDto = findById(teamSkillDtos, teamId);
                    if (teamSkillDto == null) {
                        teamSkillDto = new QueryDto();
                        teamSkillDto.setId(teamId);
                        teamSkillDto.setClName(Team.class.getSimpleName());
                        teamSkillDto.setName((String) objects[3]);
                        teamSkillDto.setChildren(new ArrayList<QueryDto>());

                        teamSkillDtos.add(teamSkillDto);
                    }
                    String skillId = (String) objects[4];
                    if (skillId != null) {
                        Collection<QueryDto> skillDtos = teamSkillDto.getChildren();
                        QueryDto skillDto = new QueryDto();
                        skillDto.setId(skillId);
                        skillDto.setClName(Skill.class.getSimpleName());
                        skillDto.setName((String) objects[5]);

                        skillDtos.add(skillDto);
                    }
                }
            }
        }

        return result;
    }

    private QueryDto findById(Collection<QueryDto> list, String id) {
        if (list != null) {
            for (QueryDto dto : list) {
                if (StringUtils.equals(dto.getId(), id)) {
                    return dto;
                }
            }
        }
        return null;
    }

    @Validation
	public SchedulingSettingsDto getSchedulingSettings(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class) PrimaryKey siteKey)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Site site = siteService.getSite(siteKey);
        SchedulingSettings settings = site.getSchedulingSettings();
        // note that by default, a site doesn't have any scheduling settings as they are optional
        return (settings != null ? toDto(settings, SchedulingSettingsDto.class) : null);
	}

    @Validation
	public SchedulingSettingsDto setSchedulingSettings(
			@Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey siteKey,
			SchedulingSettingsUpdateDto updateDto) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
		// TODO add validation for updateDto (not null + attribute values)
			
        Site site = siteService.getSite(siteKey);
        SchedulingSettings settings = site.getSchedulingSettings();
        if (settings == null) {
        	// create case (no settings yet), create default settings
        	settings = siteService.createSchedulingSettings(site);
        }
        if (updateDto != null) {
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
	        siteService.updateSchedulingSettings(settings);
        }
        return toDto(settings, SchedulingSettingsDto.class);
	}

	public boolean deleteSchedulingSettings(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey siteKey) {
		Site site = siteService.getSite(siteKey);
		siteService.deleteSchedulingSettings(site);
		return true;
	}

    public Collection<SiteTeamDto> siteTeams(String tenantId, AccountACL acl) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Collection<SiteTeamDto> result = new ArrayList<>();

        List<Object[]> rawObjects = siteService.siteTeams(tenantId, acl);
        if (rawObjects!=null){
	        for (Object[] raw : rawObjects) {
	            SiteTeamDto siteTeamDto = new SiteTeamDto();
	
	            siteTeamDto.setTeamId((String) raw[0]);
	            siteTeamDto.setTeamName((String) raw[1]);
	            siteTeamDto.setSiteId((String) raw[2]);
	            siteTeamDto.setSiteName((String) raw[3]);
	
	            result.add(siteTeamDto);
	        }
        }

        return result;
    }

    public Collection<SiteScheduleDto> siteSchedules(String tenantId, Long startDate, AccountACL acl)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Collection<SiteScheduleDto> result = new ArrayList<>();

        List<Object[]> rawObjects = siteService.siteSchedules(tenantId, startDate, acl);
        
        if (rawObjects!=null){
	        for (Object[] raw : rawObjects) {
	            SiteScheduleDto siteScheduleDto = new SiteScheduleDto();
	
	            siteScheduleDto.setScheduleId((String) raw[0]);
	            siteScheduleDto.setScheduleName((String) raw[1]);
	            siteScheduleDto.setSiteId((String) raw[2]);
	            siteScheduleDto.setSiteName((String) raw[3]);
	
	            result.add(siteScheduleDto);
	        }
        }

        return result;
    }

    public Collection<SiteScheduleDto> schedules(String siteId, Long startDate, AccountACL acl, String filter)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Collection<SiteScheduleDto> result = new ArrayList<>();

        List<Object[]> rawObjects = siteService.schedules(siteId, startDate, acl, filter);
        
        if (rawObjects!=null){
	        for (Object[] raw : rawObjects) {
	            if (raw[0] != null) {
	                SiteScheduleDto siteScheduleDto = new SiteScheduleDto();
	                siteScheduleDto.setScheduleId((String) raw[0]);
	                siteScheduleDto.setScheduleName((String) raw[1]);
	                result.add(siteScheduleDto);
	            }
	        }
        }
        return result;
    }

    @Validation
    public ResultSetDto<ScheduleDto> schedulesForSite(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey sitePrimaryKey,
            String filter,
            Integer offset,
            Integer limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir,
            AccountACL acl)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ResultSet<Schedule> resultSet = siteService.schedulesForSite(sitePrimaryKey, filter, offset, limit, orderBy,
                orderDir, acl);
        return toResultSetDto(resultSet, ScheduleDto.class);
    }

    @Validation
	public ResultSetDto<PostOverridesDto> getPostOverrides(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class) PrimaryKey sitePK)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Site site = siteService.getSite(sitePK);
		ResultSet<PostOverrides> rs = siteService.getPostOverrides(site);
        return toResultSetDto(rs, PostOverridesDto.class);
	}

    @Validation
	public PostOverridesDto createPostOverrides(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class) PrimaryKey sitePK,
            PostOverridesDto overridesDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Site site = siteService.getSite(sitePK);
		PostOverrides po = siteService.createPostOverrides(site, overridesDto.getName(),
                overridesDto.getOverrideOptions());
		return toDto(po, PostOverridesDto.class);		
	}

    @Validation
	public PostOverridesDto getPostOverrides(
			@Validate(validator = EntityExistValidatorBean.class, type = Site.class) PrimaryKey sitePK,
			String name) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
		// get a Site post override. if not exiting, creates a default one
        Site site = siteService.getSite(sitePK);
		PostOverrides po = siteService.getPostOverrides(site, name);
		if (po == null) {
			po = siteService.createPostOverrides(site, name, null);
		}
		return toDto(po, PostOverridesDto.class);
	}

    @Validation
	public PostOverridesDto updatePostOverrides(
			@Validate(validator = EntityExistValidatorBean.class, type = Site.class)
            PrimaryKey sitePK,
			String name,
            PostOverridesDto overridesDto) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Site site = siteService.getSite(sitePK);
		PostOverrides po = siteService.updatePostOverrides(site, overridesDto.getName(),
                overridesDto.getOverrideOptions());
		return toDto(po, PostOverridesDto.class);
	}

    @Validation
	public void deletePostOverrides(
			@Validate(validator = EntityExistValidatorBean.class, type = Site.class) PrimaryKey sitePK, String name) {
        Site site = siteService.getSite(sitePK);
		siteService.deletePostOverrides(site, name);
	}
	
    private void checkUniqueLengthForSite(int length, PrimaryKey sitePrimaryKey,
                                          PrimaryKey exceptShiftLengthPrimaryKey) {
        int count = siteService.countLengthOfShiftLengthForSite(length, sitePrimaryKey, exceptShiftLengthPrimaryKey);
        if (count > 0) {
            throw new ValidationException("validation.site.shiftlength.notunique");
        }
    }

}
