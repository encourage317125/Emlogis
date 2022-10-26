package com.emlogis.common.facade.shiftpattern;

import com.emlogis.common.Constants;
import com.emlogis.common.ModelUtils;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.services.employee.SkillService;
import com.emlogis.common.services.shiftpattern.ShiftLengthService;
import com.emlogis.common.services.shiftpattern.ShiftPatternService;
import com.emlogis.common.services.shiftpattern.ShiftTypeService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.common.validation.annotations.*;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.employee.Skill;
import com.emlogis.model.shiftpattern.*;
import com.emlogis.model.shiftpattern.dto.*;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.ShiftPatternDtoMapper;
import com.emlogis.rest.resources.util.SimpleQuery;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import javax.ejb.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ShiftPatternFacade extends BaseFacade {

    @EJB
    private ShiftPatternService shiftPatternService;

    @EJB
    private TeamService teamService;

    @EJB
    private SkillService skillService;

    @EJB
    private ShiftTypeService shiftTypeService;

    @EJB
    private ShiftLengthService shiftLengthService;

    @Validation
    public ResultSetDto<ShiftPatternDto> getObjects(
            String tenantId,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir,
            AccountACL acl) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        ResultSet<ShiftPattern> resultSet = getShiftPatternResultSet(tenantId, select, filter, offset, limit, orderBy,
                orderDir, acl);
        return toResultSetDto(resultSet, ShiftPatternDto.class, new ShiftPatternDtoMapper());
    }

    @Validation
    public ResultSetDto<ShiftPatternSummaryDto> getSummary(
            String tenantId,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir,
            AccountACL acl) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        ResultSetDto<ShiftPatternSummaryDto> result = new ResultSetDto<>();

        ResultSet<ShiftPattern> resultSet = getShiftPatternResultSet(tenantId, select, filter, offset, limit, orderBy,
                orderDir, acl);

        List<ShiftPatternSummaryDto> summaryDtos = new ArrayList<>();
        for (ShiftPattern shiftPattern : resultSet.getResult()) {
            ShiftPatternSummaryDto summaryDto = new ShiftPatternSummaryDto();

            Long cdDate = shiftPattern.getCdDate() == null ? null : shiftPattern.getCdDate().toDate().getTime();
            summaryDto.setShiftPatternCdDate(cdDate);
            summaryDto.setShiftPatternDayOfWeek(shiftPattern.getDayOfWeek());
            summaryDto.setShiftPatternId(shiftPattern.getId());
            summaryDto.setShiftPatternName(shiftPattern.getName());
            summaryDto.setShiftPatternType(shiftPattern.getType());

            Team team = shiftPattern.getTeam();
            if (team != null) {
                summaryDto.setTeamId(team.getId());
                summaryDto.setTeamName(team.getName());

                Site site = teamService.getSite(team);
                if (site != null) {
                    summaryDto.setSiteId(site.getId());
                    summaryDto.setSiteName(site.getName());
                    summaryDto.setSiteFirstDayOfTheWeek(site.getFirstDayOfWeek());
                }
            }

            Skill skill = shiftPattern.getSkill();
            if (skill != null) {
                summaryDto.setSkillId(skill.getId());
                summaryDto.setSkillName(skill.getName());
            }

            summaryDtos.add(summaryDto);
        }

        result.setTotal(resultSet.getTotal());
        result.setResult(summaryDtos);

        return result;
    }

    @Validation
    public ShiftPatternDto getObject(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftPattern.class)
            PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ShiftPattern shiftPattern = shiftPatternService.getShiftPattern(primaryKey);
        return new ShiftPatternDtoMapper().map(shiftPattern, ShiftPatternDto.class);
    }

    @Validation
    public ShiftPatternDto updateObject(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftPattern.class)
            PrimaryKey primaryKey,
            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = ShiftPatternDto.NAME, min = 1, max = 256, passNull = true)
                    }
            )
            @ValidateNotNullNumber(fields = {ShiftPatternUpdateDto.CD_DATE, ShiftPatternUpdateDto.DAY_OF_WEEK})
            ShiftPatternUpdateDto shiftPatternUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ShiftPattern shiftPattern = shiftPatternService.getShiftPattern(primaryKey);

        shiftPattern = updateShiftPattern(shiftPattern, shiftPatternUpdateDto);

        ShiftPatternDto result = new ShiftPatternDtoMapper().map(shiftPattern, ShiftPatternDto.class);
        getEventService().sendEntityUpdateEvent(shiftPattern, ShiftPatternDto.class, result);
        return result;
    }

    @Validation
    public ShiftPatternDto createObject(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftPattern.class, expectedResult = false)
            PrimaryKey primaryKey,

            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = ShiftPatternDto.NAME, min = 1, max = 256)
                    }
            )
            @ValidateNotNullNumber(fields = {ShiftPatternCreateDto.UPDATE_CD_DATE,
                    ShiftPatternCreateDto.UPDATE_DAY_OF_WEEK})
            ShiftPatternCreateDto createDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        ShiftPattern shiftPattern = shiftPatternService.create(primaryKey);
        shiftPattern.setName(createDto.getName());

        if (createDto.getTeamId() != null) {
            PrimaryKey teamPrimaryKey = new PrimaryKey(primaryKey.getTenantId(), createDto.getTeamId());
            Team team = teamService.getTeam(teamPrimaryKey);
            shiftPattern.setTeam(team);
        }

        if (createDto.getSkillId() != null) {
            PrimaryKey skillPrimaryKey = new PrimaryKey(primaryKey.getTenantId(), createDto.getSkillId());
            Skill skill = skillService.getSkill(skillPrimaryKey);
            shiftPattern.setSkill(skill);
        }

        ShiftPatternUpdateDto updateDto = createDto.getUpdateDto();
        if (updateDto != null) {
            shiftPattern = updateShiftPattern(shiftPattern, updateDto);
        }

        setCreatedBy(shiftPattern);
        setOwnedBy(shiftPattern);

        shiftPattern = shiftPatternService.update(shiftPattern);

        ShiftPatternDto result = new ShiftPatternDtoMapper().map(shiftPattern, ShiftPatternDto.class);
        getEventService().sendEntityCreateEvent(shiftPattern, ShiftPatternDto.class, result);
        return result;
    }

    @Validation
    public void deleteObject(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftPattern.class) PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        ShiftPattern shiftPattern = shiftPatternService.getShiftPattern(primaryKey);
        ShiftPatternDto shiftPatternDto = new ShiftPatternDtoMapper().map(shiftPattern, ShiftPatternDto.class);
        shiftPatternService.delete(shiftPattern);
        getEventService().sendEntityDeleteEvent(shiftPattern, ShiftPatternDto.class, shiftPatternDto);
    }

    @Validation
    public ShiftPatternDto saveAs(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftPattern.class)
            PrimaryKey primaryKey,
            String name) {
        ShiftPattern shiftPattern = shiftPatternService.getShiftPattern(primaryKey);

        ShiftPattern newShiftPattern = duplicateShiftPattern(shiftPattern);
        newShiftPattern.setName(name);

        newShiftPattern = shiftPatternService.update(newShiftPattern);

        ShiftPatternDto result = new ShiftPatternDtoMapper().map(newShiftPattern, ShiftPatternDto.class);
        getEventService().sendEntityCreateEvent(newShiftPattern, ShiftPatternDto.class, result);
        return result;
    }

    @Validation
    public Collection<ShiftPatternDto> duplicate(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftPattern.class)
            PrimaryKey primaryKey,
            Set<DayOfWeek> dayOfWeeks,
            Long cdDate) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        List<ShiftPattern> result = new ArrayList<>();
        ShiftPatternDtoMapper shiftPatternDtoMapper = new ShiftPatternDtoMapper();

        ShiftPattern shiftPattern = shiftPatternService.getShiftPattern(primaryKey);

        if (dayOfWeeks != null && dayOfWeeks.size() > 0) {
            for (DayOfWeek dayOfWeek : dayOfWeeks) {
                ShiftPattern newShiftPattern = duplicateShiftPattern(shiftPattern);
                newShiftPattern.setDayOfWeek(dayOfWeek);
                newShiftPattern.setCdDate(null);
                newShiftPattern = shiftPatternService.update(newShiftPattern);
                result.add(newShiftPattern);
                ShiftPatternDto shiftPatternDto = shiftPatternDtoMapper.map(newShiftPattern, ShiftPatternDto.class);
                getEventService().sendEntityCreateEvent(newShiftPattern, ShiftPatternDto.class, shiftPatternDto);
            }
        }
        if (cdDate != null) {
            ShiftPattern newShiftPattern = duplicateShiftPattern(shiftPattern);
            newShiftPattern.setCdDate(new DateTime(cdDate));
            newShiftPattern = shiftPatternService.update(newShiftPattern);
            result.add(newShiftPattern);
            ShiftPatternDto shiftPatternDto = shiftPatternDtoMapper.map(newShiftPattern, ShiftPatternDto.class);
            getEventService().sendEntityCreateEvent(newShiftPattern, ShiftPatternDto.class, shiftPatternDto);
        }

        return new ShiftPatternDtoMapper().map(result, ShiftPatternDto.class);
    }

    @Validation
    public Collection<ShiftReqDto> generateDemandShiftReqs(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftPattern.class)
            PrimaryKey primaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        Set<ShiftReq> shiftReqs = shiftPatternService.generateDemandShiftReqs(primaryKey);
        return toCollectionDto(shiftReqs, ShiftReqDto.class);
    }

    public Collection<ShiftReqDto> generateDraftDemandShiftReqs(String tenantId, DraftDemandDto draftDemandDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Collection<ShiftDemand> shiftDemands = new ArrayList<>();
        for (ShiftDemandDto shiftDemandDto : draftDemandDto.getShiftDemandDtos()) {
            ShiftDemand shiftDemand = new ShiftDemand();
            shiftDemand.setEmployeeCount(shiftDemandDto.getEmployeeCount());
            shiftDemand.setLengthInMin(shiftDemandDto.getLengthInMin());
            shiftDemand.setStartTime(new LocalTime(shiftDemandDto.getStartTime()));

            shiftDemands.add(shiftDemand);
        }

        Collection<ShiftLength> allowedShiftLengths = new ArrayList<>();
        for (String lengthId : draftDemandDto.getAllowedShiftLengthIds()) {
            PrimaryKey primaryKey = new PrimaryKey(tenantId, lengthId);
            ShiftLength shiftLength = shiftLengthService.getShiftLength(primaryKey);

            allowedShiftLengths.add(shiftLength);
        }

        Set<ShiftReq> shiftReqs = shiftPatternService.generateDraftDemandShiftReqs(allowedShiftLengths, shiftDemands);

        return toCollectionDto(shiftReqs, ShiftReqDto.class);
    }

    private ResultSet<ShiftPattern> getShiftPatternResultSet(String tenantId, String select, String filter, int offset,
                                                             int limit, String orderBy, String orderDir,
                                                             AccountACL acl) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SimpleQuery simpleQuery = new SimpleQuery(tenantId);
        simpleQuery.setSelect(select).setOffset(offset).setLimit(limit).setOrderByField(orderBy).setFilter(filter)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true);

        if (acl != null) {
            ResultSet<Team> teamResultSet = teamService.findTeams(new SimpleQuery(tenantId), acl);
            Collection<Team> teams = teamResultSet.getResult();
            if (teams != null && teams.size() > 0) {
                String filterByTeams = "team.primaryKey.id IN (" + ModelUtils.commaSeparatedQuotedIds(teams) + ") ";
                simpleQuery.addFilter(filterByTeams);
            }
        }

        return shiftPatternService.findShiftPatterns(simpleQuery);
    }

    private ShiftPattern duplicateShiftPattern(ShiftPattern shiftPattern) {
        String tenantId = shiftPattern.getTenantId();

        ShiftPattern result = shiftPatternService.create(new PrimaryKey(tenantId));

        result.setName(shiftPattern.getName());
        result.setTeam(shiftPattern.getTeam());
        result.setCdDate(shiftPattern.getCdDate());
        result.setDayOfWeek(shiftPattern.getDayOfWeek());
        result.setDescription(shiftPattern.getDescription());
        result.setMaxEmployeeCount(shiftPattern.getMaxEmployeeCount());
        result.setShiftLengthList(shiftPattern.getShiftLengthList());
        result.setSkill(shiftPattern.getSkill());
        result.setType(shiftPattern.getType());
        result.setShiftStructureGenerated(shiftPattern.isShiftStructureGenerated());

        if (shiftPattern.getPatternElts() != null) {
            Set<PatternElt> patternElts = new HashSet<>();
            patternElts.addAll(shiftPattern.getPatternElts());
            result.setPatternElts(patternElts);
        }
        if (shiftPattern.getShiftDemands() != null) {
            Set<ShiftDemand> shiftDemands = new HashSet<>();
            shiftDemands.addAll(shiftPattern.getShiftDemands());
            result.setShiftDemands(shiftDemands);
        }

        if (shiftPattern.getShiftReqs() != null) {
            Set<ShiftReq> shiftReqs = new HashSet<>();
            for (ShiftReq shiftReq : shiftPattern.getShiftReqs()) {
                ShiftReq resultShiftReq = new ShiftReq();
                resultShiftReq.setPrimaryKey(new PrimaryKey(tenantId));
                resultShiftReq.setEmployeeCount(shiftReq.getEmployeeCount());
                resultShiftReq.setExcessCount(shiftReq.getExcessCount());
                resultShiftReq.setShiftType(shiftReq.getShiftType());
                resultShiftReq.setShiftPattern(result);

                shiftReqs.add(resultShiftReq);

                shiftPatternService.createShiftReq(resultShiftReq);
            }
            result.setShiftReqs(shiftReqs);
        }

        return result;
    }

    private ShiftPattern updateShiftPattern(ShiftPattern shiftPattern, ShiftPatternUpdateDto shiftPatternUpdateDto) {
        boolean modified = false;
        String tenantId = shiftPattern.getTenantId();

        if (StringUtils.isNotBlank(shiftPatternUpdateDto.getName())) {
            shiftPattern.setName(shiftPatternUpdateDto.getName());
            modified = true;
        }
        if (StringUtils.isNotBlank(shiftPatternUpdateDto.getDescription())) {
            shiftPattern.setDescription(shiftPatternUpdateDto.getDescription());
            modified = true;
        }/*
        if (shiftPatternUpdateDto.isShiftStructureGenerated() != null) {
            shiftPattern.setShiftStructureGenerated(shiftPatternUpdateDto.isShiftStructureGenerated());
            modified = true;
        }*/
        if (shiftPatternUpdateDto.getCdDate() != null) {
            DateTime cdDate = new DateTime(shiftPatternUpdateDto.getCdDate());
            if (!cdDate.equals(shiftPattern.getCdDate())) {
                shiftPattern.setCdDate(cdDate);
                modified = true;
            }
        } else {
            if (shiftPattern.getCdDate() != null) {
                shiftPattern.setCdDate(null);
                modified = true;
            }
        }
        if (shiftPatternUpdateDto.getType() != null) {
            shiftPattern.setType(shiftPatternUpdateDto.getType());
            modified = true;
        }
        if (shiftPatternUpdateDto.getDayOfWeek() != null) {
            if (!shiftPatternUpdateDto.getDayOfWeek().equals(shiftPattern.getDayOfWeek())) {
                shiftPattern.setDayOfWeek(shiftPatternUpdateDto.getDayOfWeek());
                modified = true;
            }
        } else {
            if (shiftPattern.getDayOfWeek() != null) {
                shiftPattern.setDayOfWeek(null);
                modified = true;
            }
        }
        if (StringUtils.isNotBlank(shiftPatternUpdateDto.getShiftLengthList())) {
            shiftPattern.setShiftLengthList(shiftPatternUpdateDto.getShiftLengthList());
            modified = true;
        }
        if (shiftPatternUpdateDto.getMaxEmployeeCount() != null) {
            shiftPattern.setMaxEmployeeCount(shiftPatternUpdateDto.getMaxEmployeeCount());
            modified = true;
        }

        // Let's get all ShifReq existing ids and remove those from this collection which will need to keep
        Set<String> shiftReqToDeleteIds = ModelUtils.idSet(shiftPattern.getShiftReqs());

        List<String> shiftReqIncomingIds = new ArrayList<>();
        if (shiftPatternUpdateDto.getShiftReqDtos() != null) {
            for (ShiftReqDto shiftReqDto : shiftPatternUpdateDto.getShiftReqDtos()) {
                modified = true;

                String id = shiftReqDto.getId();
                if (id != null) {
                    shiftReqIncomingIds.add(id);

                    // update ShiftReq
                    PrimaryKey shiftReqPrimaryKey = new PrimaryKey(tenantId, id);
                    ShiftReq shiftReq = shiftPatternService.getShiftReq(shiftReqPrimaryKey);

                    PrimaryKey shiftTypePrimaryKey = new PrimaryKey(tenantId, shiftReqDto.getShiftTypeId());
                    ShiftType shiftType = shiftTypeService.getShiftType(shiftTypePrimaryKey);

                    validateShiftLength(shiftType, shiftPattern.getShiftLengthList(), shiftPattern.getId());

                    shiftReq.setShiftType(shiftType);
                    shiftReq.setEmployeeCount(shiftReqDto.getEmployeeCount());
                    shiftReq.setExcessCount(shiftReqDto.getExcessCount());
                    shiftReq.setShiftPattern(shiftPattern);
                    setUpdatedBy(shiftReq);
                    shiftPatternService.updateShiftReq(shiftReq);
                } else {
                    // create new ShiftReq
                    PrimaryKey shiftReqPrimaryKey = new PrimaryKey(tenantId);
                    ShiftReq shiftReq = new ShiftReq();
                    shiftReq.setPrimaryKey(shiftReqPrimaryKey);

                    PrimaryKey shiftTypePrimaryKey = new PrimaryKey(tenantId, shiftReqDto.getShiftTypeId());
                    ShiftType shiftType = shiftTypeService.getShiftType(shiftTypePrimaryKey);

                    validateShiftLength(shiftType, shiftPattern.getShiftLengthList(), shiftPattern.getId());

                    shiftReq.setShiftType(shiftType);
                    shiftReq.setEmployeeCount(shiftReqDto.getEmployeeCount());
                    shiftReq.setExcessCount(shiftReqDto.getExcessCount());
                    shiftReq.setShiftPattern(shiftPattern);

                    shiftPatternService.createShiftReq(shiftReq);

                    if (shiftPattern.getShiftReqs() == null) {
                        shiftPattern.setShiftReqs(new HashSet<ShiftReq>());
                    }
                    shiftPattern.getShiftReqs().add(shiftReq);
                }
            }
        }

        Set<ShiftReq> shiftReqToDelete = new HashSet<>();
        shiftReqToDeleteIds.removeAll(shiftReqIncomingIds);
        for (String id : shiftReqToDeleteIds) {
            if (shiftPattern.getShiftReqs() != null) {
                for (ShiftReq shiftReq : shiftPattern.getShiftReqs()) {
                    if (StringUtils.equals(id, shiftReq.getId())) {
                        shiftReqToDelete.add(shiftReq);
                    }
                }
            }
        }

        if (shiftPattern.getShiftReqs() != null) {
            shiftPattern.getShiftReqs().removeAll(shiftReqToDelete);
        }
        for (ShiftReq shiftReq : shiftReqToDelete) {
            modified = true;
            shiftPatternService.deleteShiftReq(shiftReq);
        }

        // Let's get all ShiftDemand existing ids and remove those from this collection which will need to keep
        Set<String> shiftDemandToDeleteIds = ModelUtils.idSet(shiftPattern.getShiftDemands());

        List<String> shiftDemandIncomingIds = new ArrayList<>();
        if (shiftPatternUpdateDto.getShiftDemandDtos() != null) {
            for (ShiftDemandDto shiftDemandDto : shiftPatternUpdateDto.getShiftDemandDtos()) {
                modified = true;

                String id = shiftDemandDto.getId();
                if (id != null) {
                    shiftDemandIncomingIds.add(id);

                    // update ShiftDemand
                    PrimaryKey shiftDemandPrimaryKey = new PrimaryKey(tenantId, id);
                    ShiftDemand shiftDemand = shiftPatternService.getShiftDemand(shiftDemandPrimaryKey);

                    shiftDemand.setLengthInMin(shiftDemandDto.getLengthInMin());
                    shiftDemand.setEmployeeCount(shiftDemandDto.getEmployeeCount());
                    shiftDemand.setStartTime(new LocalTime(shiftDemandDto.getStartTime()));
                    shiftDemand.setShiftPattern(shiftPattern);
                    setUpdatedBy(shiftDemand);
                    shiftPatternService.updateShiftDemand(shiftDemand);
                } else {
                    // create new ShiftDemand
                    PrimaryKey shiftDemandPrimaryKey = new PrimaryKey(tenantId);
                    ShiftDemand shiftDemand = new ShiftDemand();
                    shiftDemand.setPrimaryKey(shiftDemandPrimaryKey);

                    shiftDemand.setLengthInMin(shiftDemandDto.getLengthInMin());
                    shiftDemand.setEmployeeCount(shiftDemandDto.getEmployeeCount());
                    shiftDemand.setStartTime(new LocalTime(shiftDemandDto.getStartTime()));
                    shiftDemand.setShiftPattern(shiftPattern);

                    shiftPatternService.createShiftDemand(shiftDemand);

                    if (shiftPattern.getShiftDemands() == null) {
                        shiftPattern.setShiftDemands(new HashSet<ShiftDemand>());
                    }
                    shiftPattern.getShiftDemands().add(shiftDemand);
                }
            }
        }

        Set<ShiftDemand> shiftDemandToDelete = new HashSet<>();
        shiftDemandToDeleteIds.removeAll(shiftDemandIncomingIds);
        for (String id : shiftDemandToDeleteIds) {
            if (shiftPattern.getShiftDemands() != null) {
                for (ShiftDemand shiftDemand : shiftPattern.getShiftDemands()) {
                    if (StringUtils.equals(id, shiftDemand.getId())) {
                        shiftDemandToDelete.add(shiftDemand);
                    }
                }
            }
        }

        if (shiftPattern.getShiftDemands() != null) {
            shiftPattern.getShiftDemands().removeAll(shiftDemandToDelete);
        }
        for (ShiftDemand shiftDemand : shiftDemandToDelete) {
            modified = true;
            shiftPatternService.deleteShiftDemand(shiftDemand);
        }

        if (modified) {
            setUpdatedBy(shiftPattern);
            shiftPattern = shiftPatternService.update(shiftPattern);
        }

        return shiftPattern;
    }

    private void validateShiftLength(ShiftType shiftType, String shiftLengthList, String patternId) {
        if (shiftType != null && StringUtils.isNotBlank(shiftLengthList)) {
            ShiftLength shiftLength = shiftType.getShiftLength();
            if (shiftLength != null) {
                if (!shiftLengthList.contains(shiftLength.getId())) {
                    throw new ValidationException(getMessage("validation.shiftlength.add.forbidden",
                            shiftLength.getId(), patternId));
                }
            }
        }
    }

}
