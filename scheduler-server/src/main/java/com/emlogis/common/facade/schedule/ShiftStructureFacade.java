package com.emlogis.common.facade.schedule;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.services.employee.SkillService;
import com.emlogis.common.services.schedule.ShiftStructureService;
import com.emlogis.common.services.shiftpattern.ShiftLengthService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.common.validation.annotations.*;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.employee.Skill;
import com.emlogis.model.schedule.Schedule;
import com.emlogis.model.schedule.ShiftReqOld;
import com.emlogis.model.schedule.ShiftStructure;
import com.emlogis.model.schedule.dto.*;
import com.emlogis.model.shiftpattern.ShiftLength;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalTime;

import javax.ejb.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ShiftStructureFacade extends BaseFacade {

    @EJB
    private ShiftStructureService shiftStructureService;

    @EJB
    private TeamService teamService;

    @EJB
    private SkillService skillService;

    @EJB
    private ShiftLengthService shiftLengthService;

    @Validation
    public ResultSetDto<ShiftStructureDto> getObjects(
            String tenantId,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQuery simpleQuery = new SimpleQuery(tenantId);
        simpleQuery.setSelect(select).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equals(orderDir, "ASC")).setTotalCount(true);
        ResultSet<ShiftStructure> resultSet = shiftStructureService.findShiftStructures(simpleQuery);
        return toResultSetDto(resultSet, ShiftStructureDto.class);
    }

    @Validation
    public ShiftStructureDto getObject(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftStructure.class)
            PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ShiftStructure shiftStructure = shiftStructureService.getShiftStructure(primaryKey);
        return toDto(shiftStructure, ShiftStructureDto.class);
    }

    @Validation
    public ShiftStructureDto updateObject(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftStructure.class)
            PrimaryKey primaryKey,
            @ValidateUnique(fields = {ShiftStructureCreateDto.START_DATE, ShiftStructureCreateDto.TEAM_ID},
                    type = ShiftStructure.class)
            ShiftStructureUpdateDto shiftStructureUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        boolean modified = false;
        ShiftStructure shiftStructure = shiftStructureService.getShiftStructure(primaryKey);

        if (shiftStructureUpdateDto.getStartDate() > 0) {
            shiftStructure.setStartDate(shiftStructureUpdateDto.getStartDate());
            modified = true;
        }

        if (modified) {
            setUpdatedBy(shiftStructure);
            shiftStructure = shiftStructureService.update(shiftStructure);
        }

        return toDto(shiftStructure, ShiftStructureDto.class);
    }

    @Validation
    public ShiftStructureDto createObject(
            PrimaryKey primaryKey,
            @ValidateUnique(fields = {ShiftStructureCreateDto.START_DATE, ShiftStructureCreateDto.TEAM_ID},
                    type = ShiftStructure.class)
            @ValidateNotNullNumber(fields = ShiftStructureCreateDto.TEAM_ID)
            ShiftStructureCreateDto shiftStructureCreateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey teamPrimaryKey = new PrimaryKey(primaryKey.getTenantId(), shiftStructureCreateDto.getTeamId());
        Team team = teamService.getTeam(teamPrimaryKey);

        ShiftStructure shiftStructure = shiftStructureService.create(primaryKey);

        shiftStructure.setStartDate(shiftStructureCreateDto.getStartDate());
        shiftStructure.setTeam(team);
        team.addShiftStructure(shiftStructure);

        setCreatedBy(shiftStructure);
        setOwnedBy(shiftStructure);

        shiftStructureService.update(shiftStructure);

        return toDto(shiftStructure, ShiftStructureDto.class);
    }

    @Validation
    public boolean deleteObject(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftStructure.class) PrimaryKey primaryKey) {
        ShiftStructure shiftStructure = shiftStructureService.getShiftStructure(primaryKey);
        shiftStructureService.delete(shiftStructure);
        return true;
    }

    @Validation
    public ResultSetDto<ShiftReqOldDto> getShiftReqs(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftStructure.class)
            PrimaryKey shiftStructurePrimaryKey,
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQuery simpleQuery = new SimpleQuery(shiftStructurePrimaryKey.getTenantId());
        simpleQuery.setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setFilter(filter).addFilter("shiftStructureId='" + shiftStructurePrimaryKey.getId() + "'")
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true);
        ResultSet<ShiftReqOld> resultSet = shiftStructureService.findShiftReqs(simpleQuery);

        return toResultSetDto(resultSet, ShiftReqOldDto.class);
    }

    @Validation
    public ShiftReqOldDto getShiftReq(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftReqOld.class)
            PrimaryKey reqPrimaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ShiftReqOld shiftReq = shiftStructureService.getShiftReq(reqPrimaryKey);
        return toDto(shiftReq, ShiftReqOldDto.class);
    }

    @Validation
    public ShiftReqOldDto createShiftReq(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftStructure.class)
            PrimaryKey structurePrimaryKey,
            ShiftReqOldDto shiftReqOldDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey reqPrimaryKey = new PrimaryKey(structurePrimaryKey.getTenantId(), shiftReqOldDto.getId());

        ShiftReqOld shiftReq = new ShiftReqOld();
        shiftReq.setPrimaryKey(reqPrimaryKey);
        shiftReq.setShiftStructureId(structurePrimaryKey.getId());

        PrimaryKey shiftLengthPrimaryKey = new PrimaryKey(structurePrimaryKey.getTenantId(),
                shiftReqOldDto.getShiftLengthId());
        ShiftLength shiftLength = shiftLengthService.getShiftLength(shiftLengthPrimaryKey);
        if (shiftLength != null) {
            shiftReq.setShiftLength(shiftLength);
            shiftReq.setShiftLengthName(shiftLength.getName());
        }

        shiftReq.setStartTime(new LocalTime(shiftReqOldDto.getStartTime()));
        shiftReq.setDurationInMins(shiftReqOldDto.getDurationInMins());
        shiftReq.setExcess(shiftReqOldDto.isExcess());
        shiftReq.setSkillId(shiftReqOldDto.getSkillId());
        shiftReq.setSkillProficiencyLevel(shiftReqOldDto.getSkillProficiencyLevel());
        shiftReq.setEmployeeCount(shiftReqOldDto.getEmployeeCount());
        shiftReq.setDayIndex(shiftReqOldDto.getDayIndex());

        PrimaryKey skillPrimaryKey = new PrimaryKey(structurePrimaryKey.getTenantId(), shiftReqOldDto.getSkillId());
        Skill skill = skillService.getSkill(skillPrimaryKey);
        if (skill != null) {
            shiftReq.setSkillName(skill.getName());
        }

        setCreatedBy(shiftReq);
        setOwnedBy(shiftReq);

        shiftStructureService.persistShiftReq(shiftReq);

        ShiftStructure shiftStructure = shiftStructureService.getShiftStructure(structurePrimaryKey);
        shiftStructure.getShiftReqs().add(shiftReq);

        shiftStructureService.update(shiftStructure);

        return toDto(shiftReq, ShiftReqOldDto.class);
    }

    @Validation
    public ShiftReqOldDto updateShiftReq(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftStructure.class)
            PrimaryKey structurePrimaryKey,
            ShiftReqOldDto shiftReqOldDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        PrimaryKey reqPrimaryKey = new PrimaryKey(structurePrimaryKey.getTenantId(), shiftReqOldDto.getId());

        ShiftReqOld shiftReq = shiftStructureService.getShiftReq(reqPrimaryKey);

        shiftReq.setDayIndex(shiftReqOldDto.getDayIndex());
        shiftReq.setEmployeeCount(shiftReqOldDto.getEmployeeCount());
        shiftReq.setDurationInMins(shiftReqOldDto.getDurationInMins());
        shiftReq.setExcess(shiftReqOldDto.isExcess());
        shiftReq.setNight(shiftReqOldDto.isNight());
        shiftReq.setSkillProficiencyLevel(shiftReqOldDto.getSkillProficiencyLevel());
        shiftReq.setStartTime(new LocalTime(shiftReqOldDto.getStartTime()));

        PrimaryKey skillPrimaryKey = new PrimaryKey(structurePrimaryKey.getTenantId(), shiftReqOldDto.getSkillId());
        Skill skill = skillService.getSkill(skillPrimaryKey);
        if (skill != null) {
            shiftReq.setSkillName(skill.getName());
        }

        PrimaryKey shiftLengthPrimaryKey = new PrimaryKey(structurePrimaryKey.getTenantId(),
                shiftReqOldDto.getShiftLengthId());
        ShiftLength shiftLength = shiftLengthService.getShiftLength(shiftLengthPrimaryKey);
        if (shiftLength != null) {
            shiftReq.setShiftLengthName(shiftLength.getName());
        }

        setUpdatedBy(shiftReq);

        shiftStructureService.updateShiftReq(shiftReq);

        return toDto(shiftReq, ShiftReqOldDto.class);
    }

    @Validation
    public boolean deleteShiftReq(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftStructure.class)
            PrimaryKey structurePrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftReqOld.class)
            PrimaryKey reqPrimaryKey) {
        shiftStructureService.deleteShiftReq(structurePrimaryKey, reqPrimaryKey);
        return true;
    }

    @Validation
    public ResultSetDto<ScheduleDto> getSchedules(
            PrimaryKey structurePrimaryKey,
            String select, // select is NOT IMPLEMENTED FOR NOW ...
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(structurePrimaryKey.getTenantId());
        simpleQuery.setEntityClass(Schedule.class).setSelect(select).setFilter(filter).setOffset(offset)
                .setLimit(limit).setOrderByField(orderBy).setOrderAscending(StringUtils.equals(orderDir, "ASC"))
                .setTotalCount(true);
        ResultSet<Schedule> resultSet = shiftStructureService.getSchedules(structurePrimaryKey, simpleQuery);
        return this.toResultSetDto(resultSet, ScheduleDto.class);
    }

    @Validation
    public ShiftStructureDto duplicate(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftStructure.class)
            PrimaryKey structurePrimaryKey,
            long startDate) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        validateStartDateForTeam(structurePrimaryKey, startDate);

        ShiftStructure shiftStructure = shiftStructureService.duplicate(structurePrimaryKey, startDate);
        return toDto(shiftStructure, ShiftStructureDto.class);
    }

    @Validation
    public ShiftStructureDto duplicateShiftReqs(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftStructure.class)
            PrimaryKey structurePrimaryKey,
            int dayIndexFrom,
            int dayIndexTo) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        validateShiftReqsDay(structurePrimaryKey, dayIndexFrom, dayIndexTo);

        ShiftStructure shiftStructure = shiftStructureService.duplicateShiftReqs(structurePrimaryKey, dayIndexFrom,
                dayIndexTo);
        return toDto(shiftStructure, ShiftStructureDto.class);
    }

    public void validateStartDateForTeam(PrimaryKey structurePrimaryKey, long startDate) {
        ShiftStructure shiftStructure = shiftStructureService.getShiftStructure(structurePrimaryKey);
        Team team = shiftStructure.getTeam();
        List<ShiftStructure> shiftStructures = shiftStructureService.getTeamShiftStructuresByDate(team, startDate);
        if (shiftStructures.size() > 0) {
            throw new ValidationException(getMessage("validation.shiftstructure.startdate", startDate, team.getName()));
        }
    }

    public void validateShiftReqsDay(PrimaryKey structurePrimaryKey, int dayIndexFrom, int dayIndexTo) {
        ShiftStructure shiftStructure = shiftStructureService.getShiftStructure(structurePrimaryKey);

        Set<ShiftReqOld> shiftReqFromSet = shiftStructureService.getShiftReqsOfDay(shiftStructure, dayIndexFrom);
        if (shiftReqFromSet == null || shiftReqFromSet.size() == 0) {
            throw new ValidationException(getMessage("validation.shiftstructure.shiftreqs.dayindexfrom", dayIndexFrom));
        }

        Set<ShiftReqOld> shiftReqToSet = shiftStructureService.getShiftReqsOfDay(shiftStructure, dayIndexTo);
        if (shiftReqToSet != null && shiftReqToSet.size() > 0) {
            throw new ValidationException(getMessage("validation.shiftstructure.shiftreqs.dayindexto", dayIndexTo));
        }
    }

}
