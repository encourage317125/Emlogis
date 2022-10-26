package com.emlogis.common.facade.structurelevel;

import java.lang.reflect.InvocationTargetException;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.shiftpattern.ShiftPatternService;
import com.emlogis.common.validation.annotations.*;
import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.model.shiftpattern.dto.DailyDemandDto;
import com.emlogis.model.shiftpattern.dto.ExtendedShiftReqDto;
import com.emlogis.rest.resources.util.*;
import org.apache.commons.lang3.StringUtils;

import com.emlogis.common.Constants;
import com.emlogis.common.TimeUtil;
import com.emlogis.common.availability.*;
import com.emlogis.common.availability.AvailcalViewDto;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.contract.ContractFacade;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.security.PermissionCheck;
import com.emlogis.common.security.Permissions;
import com.emlogis.common.services.structurelevel.SiteService;
import com.emlogis.common.services.structurelevel.StructureLevelService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.aom.AOMRelationshipDef;
import com.emlogis.model.contract.TeamContract;
import com.emlogis.model.contract.dto.ContractDTO;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.employee.*;
import com.emlogis.model.employee.dto.*;
import com.emlogis.model.schedule.ShiftStructure;
import com.emlogis.model.schedule.dto.ScheduleDto;
import com.emlogis.model.schedule.dto.ShiftStructureDto;
import com.emlogis.model.structurelevel.AOMRelationship;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.structurelevel.dto.*;

import com.emlogis.rest.security.SessionService;
import org.joda.time.DateTime;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TeamFacade extends StructureLevelFacade {

    @EJB
    private SiteService siteService;

    @EJB
    private TeamService teamService;

    @EJB
    private EmployeeService employeeService;

    @EJB
    private SessionService sessionService;
    
    @EJB
    private ContractFacade contractFacade;

    @EJB
    private ShiftPatternService shiftPatternService;

    @Override
    protected StructureLevelService getStructureLevelService() {
        return teamService;
    }

    @Validation
    public ResultSetDto<TeamDto> getObjects(
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
        ResultSet<Team> resultSet = teamService.findTeams(simpleQuery, acl);
        return toResultSetDto(resultSet, TeamDto.class);
    }

    @Validation
    public TeamDto getObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Team team = teamService.getTeam(primaryKey);
        return toDto(team, TeamDto.class);
    }

    @Validation
    public TeamDto updateObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey primaryKey,
            @ValidateAll(
                    strLengths = {
                        @ValidateStrLength(field = TeamCreateDto.ABBREVIATION, max = 50),
                        @ValidateStrLength(field = TeamCreateDto.NAME, max = 255)
                    }
            )
            TeamUpdateDto teamUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Team team = teamService.getTeam(primaryKey);
        Site site = teamService.getSite(team);

        team = update(primaryKey, teamUpdateDto, site);

        return toDto(team, TeamDto.class);
    }

    /**
     * Creates a Team and associate it to a 'parent' Site
     * @param sitePrimaryKey
     * @param primaryKey
     * @param createDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public TeamDto createObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Site.class, expectedResult = true)
            PrimaryKey sitePrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class, expectedResult = false)
            PrimaryKey primaryKey,
            @ValidateAll(
                    strLengths = {
                        @ValidateStrLength(field = TeamCreateDto.ABBREVIATION, max = 50),
                        @ValidateStrLength(field = TeamCreateDto.NAME, max = 255)
                    }
            )
            TeamCreateDto createDto) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        Site site = siteService.getSite(sitePrimaryKey);

        Team team = teamService.createTeam(primaryKey);
        setCreatedBy(team);
        setOwnedBy(team, this.getActualUserId());

        // set path for ACL
        team.setPath(site.getId(), AOMRelationshipDef.SITE_TEAM_REL, team.getId());

        TeamUpdateDto updateDto = createDto.getUpdateDto();
        if (updateDto != null) {
            team = update(primaryKey, updateDto, site);
        }
        
        // create default team contract
        TeamContract teamContract = contractFacade.createTeamContract(team, true);        
        team.getTeamContracts().add(teamContract);
        
        teamService.update(team);

        // link site & team
        siteService.addAOMRelationship(site, team, AOMRelationshipDef.SITE_TEAM_REL);
        return toDto(team, TeamDto.class);
    }

    @Validation
    public boolean softDeleteObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class) PrimaryKey primaryKey) {
        Team team = teamService.getTeam(primaryKey);
        // TODO team delete must likely cascade delete to a whole bunch of entities / structurelevel / schedules, etc
        // should we do that ?
        teamService.softDelete(team);
        return true;
    }

    @Validation
    public boolean hardDeleteObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class) PrimaryKey primaryKey) {
        Team team = teamService.getTeam(primaryKey);
        teamService.hardDelete(team);
        return true;
    }

    /**
     * Get a collection of Skills associated to the Team specified by the PrimaryKey
     * @param teamPrimaryKey
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    @Validation
    public ResultSetDto<SkillDto> getSkills(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey teamPrimaryKey,
            String select,
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(teamPrimaryKey.getTenantId());
        simpleQuery.setEntityClass(Skill.class).setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit)
                .setOrderByField(orderBy).setOrderAscending(StringUtils.equals(orderDir, "ASC")).setTotalCount(true);
        ResultSet<Skill> resultSet = teamService.getSkills(teamPrimaryKey, simpleQuery);
        return this.toResultSetDto(resultSet, SkillDto.class);
    }

    /**
     * Add Skill specified by id to Team specified by PrimaryKey.
     * Also adds the Skill to the Team's parent Site if it isn't already added.
     * @param teamPrimaryKey
     * @param skillId
     */
    @Validation
    public void addSkill(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey teamPrimaryKey,
            String skillId) {
        teamService.addSkill(teamPrimaryKey, skillId);
    }

    /**
     * Remove Skill specified by id to Team specified by PrimaryKey
     * @param teamPrimaryKey
     * @param skillId
     */
    @Validation
    public void removeSkill(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey teamPrimaryKey,
            String skillId) {
        teamService.removeSkill(teamPrimaryKey, skillId);
    }

    @Validation
    public void addShiftStructure(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey teamPrimaryKey,
            String shiftStructureId) {
        teamService.addShiftStructure(teamPrimaryKey, shiftStructureId);
    }

    @Validation
    public void removeShiftStructure(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey teamPrimaryKey,
            String shiftStructureId) {
        teamService.removeShiftStructure(teamPrimaryKey, shiftStructureId);
    }

    @Validation
    public ResultSetDto<ShiftStructureDto> getShiftStructures(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey teamPrimaryKey,
            String select,
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(teamPrimaryKey.getTenantId());
        simpleQuery.setEntityClass(ShiftStructure.class).setSelect(select).setFilter(filter).setOffset(offset)
                .setLimit(limit).setOrderByField(orderBy).setOrderAscending(StringUtils.equals(orderDir, "ASC"))
                .setTotalCount(true);
        ResultSet<ShiftStructure> resultSet = teamService.getShiftStructures(teamPrimaryKey, simpleQuery);
        return this.toResultSetDto(resultSet, ShiftStructureDto.class);
    }

    @Validation
    public ResultSetDto<EmployeeTeamViewDto> getTeamEmployees(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey teamPrimaryKey,
            String select, // select is NOT IMPLEMENTED FOR NOW ...
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(teamPrimaryKey.getTenantId());
        simpleQuery.setEntityClass(EmployeeTeam.class).setSelect(select).setFilter(filter).setOffset(offset)
                .setLimit(limit).setOrderByField(orderBy).setOrderAscending(StringUtils.equals(orderDir, "ASC"))
                .addFilter("team.primaryKey.id='" + teamPrimaryKey.getId() + "'")
                .setTotalCount(true);
        ResultSet<EmployeeTeam> resultSet = teamService.getEmployeeTeams(teamPrimaryKey, simpleQuery);
        return this.toResultSetDto(resultSet, EmployeeTeamViewDto.class, new EmployeeTeamDtoMapper());
    }

    @Validation
    public ResultSetDto<EmployeeDto> getUnassociatedTeamEmployees(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey teamPrimaryKey,
            String select, // select is NOT IMPLEMENTED FOR NOW ...
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(teamPrimaryKey.getTenantId());
        simpleQuery.setEntityClass(Employee.class).setSelect(select).setFilter(filter).setOffset(offset)
                .setLimit(limit).setOrderByField(orderBy).setOrderAscending(StringUtils.equals(orderDir, "ASC"))
                .setTotalCount(true);
        ResultSet<Employee> resultSet = teamService.getUnassociatedTeamEmployees(teamPrimaryKey, simpleQuery);
        return this.toResultSetDto(resultSet, EmployeeDto.class);
    }

    @Validation
    public ResultSetDto<ScheduleDto> getSchedules(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey primaryKey,
            String select,		// select is NOT IMPLEMENTED FOR NOW ...
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, InstantiationException {
        SimpleQuery simpleQuery = new SimpleQuery(primaryKey.getTenantId());
        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setEntityClass(com.emlogis.model.schedule.Schedule.class)
                .setOrderAscending(StringUtils.equals(orderDir, "ASC")).setTotalCount(true);
        ResultSet<com.emlogis.model.schedule.Schedule> resultSet = teamService.getSchedules(primaryKey, simpleQuery);
        return toResultSetDto(resultSet, ScheduleDto.class);
    }

    @Validation
    public ResultSetDto<SkillDto> getUnassociatedSkills(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey primaryKey,
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
        ResultSet<Skill> resultSet = teamService.getUnassociatedSkills(primaryKey, simpleQuery);
        return toResultSetDto(resultSet, SkillDto.class);
    }

    @Validation
    public ResultSetDto<SiteDto> getUnassociatedSites(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey primaryKey,
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
                .setEntityClass(Site.class);
        ResultSet<Site> resultSet = teamService.getUnassociatedSites(primaryKey, simpleQuery);
        return toResultSetDto(resultSet, SiteDto.class);
    }

    @Validation
	public ResultSetDto<ContractDTO> getContracts(
			@Validate(validator = EntityExistValidatorBean.class, type = Team.class) 
			PrimaryKey sitePrimaryKey,
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
		Team team = teamService.getTeam(sitePrimaryKey);

		String teamFilter = "team.primaryKey.id=" + "'" + team.getId() + "' ";
        String newFilter = filter == null ? teamFilter : filter + ";" + teamFilter;

		return contractFacade.getObjects(tenantId, select, newFilter, offset, limit, orderBy, orderDir);
	}

    @Validation
    public SiteDto getSite(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey teamPrimaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        Team team = teamService.getTeam(teamPrimaryKey);
        Site site = teamService.getSite(team);
        return toDto(site, SiteDto.class);
    }

    @Validation
    public ResultSet<EmployeeQueryDto> queryEmployees(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey teamPrimaryKey,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) {
        ResultSet<EmployeeQueryDto> result = new ResultSet<>();
        List<EmployeeQueryDto> employeeList = new ArrayList<>();

        ResultSet<Object[]> resultSet = teamService.queryEmployees(teamPrimaryKey, offset, limit, orderBy, orderDir);
        
        if (resultSet!=null && resultSet.getResult()!=null){
	        for (Object[] objects : resultSet.getResult()) {
	            EmployeeQueryDto employeeQueryDto = new EmployeeQueryDto();
	
	            employeeQueryDto.setEmployeeId((String) objects[0]);
	            employeeQueryDto.setFirstName((String) objects[1]);
	            employeeQueryDto.setLastName((String) objects[2]);
	            employeeQueryDto.setEmployeeType(objects[3] == null ? null : EmployeeType.values()[(int) objects[3]]);
	            employeeQueryDto.setHomeTeam((boolean) objects[4]);
	            employeeQueryDto.setPrimarySkill((String) objects[5]);
	
	            employeeList.add(employeeQueryDto);
	        }
        }

        result.setResult(employeeList);
        result.setTotal(resultSet.getTotal());

        return result;
    }

    public List<Map<String, Object>> dailyScheduleReport(String teamsIds, long startDate, long startTime, long endTime) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Object[]> reportList = teamService.dailyScheduleReport(teamsIds, startDate, startTime, endTime);
        
        if (reportList!=null){
	        for (Object[] objects : reportList) {
	            Map<String, Object> reportMap = new HashMap<>();
	            reportMap.put("shiftId", objects[0]);
	            reportMap.put("employeeId", objects[1]);
	            reportMap.put("employeeName", objects[2]);
	            reportMap.put("skillId", objects[3]);
	            reportMap.put("skillName", objects[4]);
	            reportMap.put("startDateTime", objects[5]);
	            reportMap.put("endDateTime", objects[6]);
	            reportMap.put("requestedDate", objects[7]);
	            reportMap.put("comment", objects[8]);
	            reportMap.put("professionalLabel", objects[9]);
	            reportMap.put("teamId", objects[10]);
	            reportMap.put("teamName", objects[11]);
	            result.add(reportMap);
	        }
        }

        return result;
    }

    /**
     * Returns all employees for teams in "teamIds"
     *
     * @param teamIds
     * @return
     */
    public List<Map<String, Object>> getTeamsEmployees(List<String> teamIds) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Object[]> reportList = teamService.getTeamsEmployees(teamIds);
        
        if (reportList!=null){
	        for (Object[] objects : reportList) {
	            Map<String, Object> reportMap = new HashMap<>();
	            reportMap.put("employeeId", objects[0]);
	            reportMap.put("employeeName", objects[1]);
	            reportMap.put("professionalLabel", objects[2]);
	            reportMap.put("teamId", objects[3]);
	            reportMap.put("teamName", objects[4]);
	            reportMap.put("isFloating", objects[5]);
	            reportMap.put("employeeType", objects[6] == null ? null : EmployeeType.values()[(Integer) objects[6]]);
	            result.add(reportMap);
	        }
        }
        return result;
    }

    public List<Map<String, Object>> getTeamsEmployeesExtended(List<String> teamIds) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Object[]> reportList = teamService.getTeamsEmployeesExtended(teamIds);
        
        if (reportList!=null){
	        for (Object[] objects : reportList) {
	            Map<String, Object> reportMap = new HashMap<>();
	            reportMap.put("employeeId", objects[0]);
	            reportMap.put("employeeName", objects[1]);
	            reportMap.put("employeeType", objects[2] == null ? null : EmployeeType.values()[(Integer) objects[2]]);
	            reportMap.put("activityType", objects[3] == null ? null : EmployeeActivityType.values()[(Integer) objects[3]]);
	            reportMap.put("startDate", objects[4]);
	            reportMap.put("endDate", objects[5]);
	            reportMap.put("login", objects[6]);
	            reportMap.put("hourlyRate", objects[7]);
	            reportMap.put("minHoursWeek", objects[8]);
	            reportMap.put("maxHoursWeek", objects[9]);
	            reportMap.put("minHoursDay", objects[10]);
	            reportMap.put("maxHoursDay", objects[11]);
	            reportMap.put("maxDaysWeek", objects[12]);
	            reportMap.put("skills", objects[13]);
	            reportMap.put("teams", objects[14]);
	            result.add(reportMap);
	        }
        }
        return result;
    }

    @Validation
    public ResultSetDto<EmployeeViewDto> getEmployees(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey teamPrimaryKey,
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
        ResultSet<Object[]> resultSet = teamService.getEmployees(teamPrimaryKey, filter, offset, limit, orderBy,
                orderDir);

        ResultSetDto<EmployeeViewDto> result = new ResultSetDto<>();
        result.setTotal(resultSet.getTotal());

        List<EmployeeViewDto> employeeViewDtos = new ArrayList<>();
        
        if (resultSet!=null && resultSet.getResult()!=null){
	        for (Object[] objects : resultSet.getResult()) {
	            EmployeeViewDto employeeViewDto = new EmployeeViewDto();
	
	            employeeViewDto.setEmployeeId((String) objects[0]);
	            employeeViewDto.setFirstName((String) objects[1]);
	            employeeViewDto.setLastName((String) objects[2]);
	            employeeViewDto.setEmployeeType(EmployeeType.values()[(Integer) objects[3]]);
	            employeeViewDto.setHireDate(objects[4] == null ? null : ((Date) objects[4]).getTime());
	            employeeViewDto.setPrimarySkillName((String) objects[5]);
	            employeeViewDto.setPrimarySkillId((String) objects[6]);
	            employeeViewDto.setHomeTeamName((String) objects[7]);
	            employeeViewDto.setHomeTeamId((String) objects[8]);
	            employeeViewDto.setIsFloating((Boolean) objects[9]);
	
	            employeeViewDtos.add(employeeViewDto);
	        }
        }

        result.setResult(employeeViewDtos);

        return result;
    }

    @Validation
    public ResultSetDto<EmployeeViewDto> getUnassociatedEmployees(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey teamPrimaryKey,
            String select, // select is NOT IMPLEMENTED FOR NOW ...
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
        ResultSet<Object[]> resultSet = teamService.getUnassociatedEmployees(teamPrimaryKey, filter, offset, limit,
                orderBy, orderDir);

        ResultSetDto<EmployeeViewDto> result = new ResultSetDto<>();
        result.setTotal(resultSet.getTotal());

        List<EmployeeViewDto> employeeViewDtos = new ArrayList<>();
        
        if (resultSet!=null && resultSet.getResult()!=null){
	        for (Object[] objects : resultSet.getResult()) {
	            EmployeeViewDto employeeViewDto = new EmployeeViewDto();
	
	            employeeViewDto.setEmployeeId((String) objects[0]);
	            employeeViewDto.setFirstName((String) objects[1]);
	            employeeViewDto.setLastName((String) objects[2]);
	            employeeViewDto.setEmployeeType(EmployeeType.values()[(Integer) objects[3]]);
	            employeeViewDto.setHireDate(objects[4] == null ? null : ((Date) objects[4]).getTime());
	            employeeViewDto.setPrimarySkillName((String) objects[5]);
	            employeeViewDto.setPrimarySkillId((String) objects[6]);
	            employeeViewDto.setHomeTeamName((String) objects[7]);
	            employeeViewDto.setHomeTeamId((String) objects[8]);
	            employeeViewDto.setIsFloating((Boolean) objects[9]);
	
	            employeeViewDtos.add(employeeViewDto);
	        }
        }

        result.setResult(employeeViewDtos);

        return result;
    }

    @Validation
    public ResultSetDto<EmployeeTeamViewDto> getTeamMembership(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey teamPrimaryKey,
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
        ResultSet<EmployeeTeam> resultSet = teamService.getTeamMembership(teamPrimaryKey, filter, offset, limit,
                orderBy, orderDir);

        ResultSetDto<EmployeeTeamViewDto> result = new ResultSetDto<>();

        List<EmployeeTeamViewDto> employeeTeamViewDtos = (List<EmployeeTeamViewDto>)
                new EmployeeTeamDtoMapper().map(resultSet.getResult(), EmployeeTeamViewDto.class);

        result.setTotal(resultSet.getTotal());
        result.setResult(employeeTeamViewDtos);

        return result;
    }

    @Validation
    public Collection<EmployeeTeamViewDto> addTeamMembership(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey teamPrimaryKey,
            TeamEmployeeCreateDto[] dtos) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, IllegalArgumentException, NoSuchFieldException {
        List<EmployeeTeamViewDto> result = new ArrayList<>();

        Team team = teamService.getTeam(teamPrimaryKey);

        String tenantId = teamPrimaryKey.getTenantId();

        if (dtos != null) {
            EmployeeTeamDtoMapper mapper = new EmployeeTeamDtoMapper();

            if (dtos!=null){
	            for (TeamEmployeeCreateDto teamEmployeeCreateDto : dtos) {
	                String employeeId = teamEmployeeCreateDto.getEmployeeId();
	                Employee employee = employeeService.getEmployee(new PrimaryKey(tenantId, employeeId));
	
	                if (employee == null) {
	                    throw new ValidationException(getMessage("validation.employee.not.exist", employeeId));
	                }
	
	                if (employeeService.findEmployeeTeam(employee, team) == null) {
	                    EmployeeTeam employeeTeam = new EmployeeTeam(new PrimaryKey(tenantId));
	                    if (teamEmployeeCreateDto.getIsFloating() != null) {
	                        employeeTeam.setIsFloating(teamEmployeeCreateDto.getIsFloating());
	                    }
	                    if (teamEmployeeCreateDto.getIsHomeTeam() != null) {
	                        employeeTeam.setIsHomeTeam(teamEmployeeCreateDto.getIsHomeTeam());
	                    }
	                    if (teamEmployeeCreateDto.getIsSchedulable() != null) {
	                        employeeTeam.setIsSchedulable(teamEmployeeCreateDto.getIsSchedulable());
	                    }
	                    employeeTeam = employeeService.addEmployeeTeam(employee, team, employeeTeam);
	
	                    EmployeeTeamViewDto employeeTeamViewDto = mapper.map(employeeTeam, EmployeeTeamViewDto.class);
	                    result.add(employeeTeamViewDto);
	                }
	            }
            }
        }

        return result;
    }

    @Validation
    public void removeTeamMembership(
            @Validate(validator = EntityExistValidatorBean.class, type = Team.class)
            PrimaryKey teamPrimaryKey,
            String[] employeeIds) {
        Team team = teamService.getTeam(teamPrimaryKey);

        String tenantId = teamPrimaryKey.getTenantId();

        if (employeeIds != null) {
            for (String employeeId : employeeIds) {
                Employee employee = employeeService.getEmployee(new PrimaryKey(tenantId, employeeId));

                if (employee == null) {
                    throw new ValidationException(getMessage("validation.employee.not.exist", employeeId));
                }

                employeeService.removeEmployeeTeam(employee, team);
            }
        }
    }

    /**
     * Returns all CD availability time frames for teams in "teamIds" in time interval
     *
     * @param teamIds
     * @return
     */
    public Collection<AvailcalViewDto.AvailCDTimeFrame> getCDAvailability(List<String> teamIds, Long startDate, Long endDate) {
        DateTime startDateTime = TimeUtil.toServerDateTime(startDate);
        DateTime endDateTime = TimeUtil.toServerDateTime(endDate);
        if (startDate == null || endDate == null){
            throw new ValidationException("Require startDate and endDate"); //TODO i18n
        }

        List<CDAvailabilityTimeFrame> dbTimeFrames = teamService.getCDAvailability(teamIds, startDateTime, endDateTime, false);
        //TODO: possibly it is the wrong way using the TimeZone of the first team's Site. Consult and Fix!
        Team theTeam = teamService.getTeam(new PrimaryKey(sessionService.getTenantId(), teamIds.get(0)));
        Site theSite = teamService.getSite(theTeam);

        CDTimeFrameProcessor<AvailcalViewDto.AvailCDTimeFrame> processor = new AvailCDTimeFrameProcessor(theSite.getTimeZone());

        Map<AvailcalViewDto.CDTimeFrame.GroupKey, List<CDAvailabilityTimeFrame>> availsByGroups
                = processor.group(dbTimeFrames);

        Collection<AvailcalViewDto.AvailCDTimeFrame> timeFrames = processor.buildFromGroups(availsByGroups);

        return timeFrames;
    }

    /**
     * Returns all CI availability time frames for teams in "teamIds"
     *
     * @param teamIds
     * @return
     */
    public Collection<AvailcalViewDto.AvailCITimeFrame> getCIAvailability(List<String> teamIds, Long startDate, Long endDate) {
        DateTime startDateTime = TimeUtil.toServerDateTime(startDate);
        DateTime endDateTime = TimeUtil.toServerDateTime(endDate);
        if (startDate == null || endDate == null){
            throw new ValidationException("Require startDate and endDate"); //TODO i18n
        }

        List<CIAvailabilityTimeFrame> dbTimeFrames = teamService.getCIAvailability(teamIds, startDateTime, endDateTime, false);

        //TODO: possibly it is the wrong way using the TimeZone of the first team's Site. Consult and Fix!
        Team theTeam = teamService.getTeam(new PrimaryKey(sessionService.getTenantId(), teamIds.get(0)));
        Site theSite = teamService.getSite(theTeam);

        CITimeFrameProcessor<com.emlogis.common.availability.AvailcalViewDto.AvailCITimeFrame> processor
                = new AvailCITimeFrameProcessor(theSite.getTimeZone());

        Map<AvailcalViewDto.CITimeFrame.GroupKey, List<CIAvailabilityTimeFrame>> availsByGroups
                = processor.group(dbTimeFrames);

        Collection<AvailcalViewDto.AvailCITimeFrame> timeFrames
                = processor.buildFromGroups(availsByGroups, startDateTime, endDateTime);

        return timeFrames;
    }

    /**
     * Returns all CD preference time frames for teams in "teamIds" in time interval
     *
     * @param teamIds
     * @return
     */
    public Collection<AvailcalViewDto.PrefCDTimeFrame> getCDPreference(List<String> teamIds,
                                                                       Long startDate,
                                                                       Long endDate) {
        DateTime startDateTime = TimeUtil.toServerDateTime(startDate);
        DateTime endDateTime = TimeUtil.toServerDateTime(endDate);
        if (startDate == null || endDate == null){
            throw new ValidationException("Require startDate and endDate"); //TODO i18n
        }

        List<CDAvailabilityTimeFrame> dbTimeFrames = teamService.getCDAvailability(teamIds,
                startDateTime, endDateTime, true);
        //TODO: possibly it is the wrong way using the TimeZone of the first team's Site. Consult and Fix!
        Team theTeam = teamService.getTeam(new PrimaryKey(sessionService.getTenantId(), teamIds.get(0)));
        Site theSite = teamService.getSite(theTeam);

        CDTimeFrameProcessor<com.emlogis.common.availability.AvailcalViewDto.PrefCDTimeFrame> processor
                = new PrefCDTimeFrameProcessor(theSite.getTimeZone());

        Map<com.emlogis.common.availability.AvailcalViewDto.CDTimeFrame.GroupKey,
                List<CDAvailabilityTimeFrame>> prefsByGroups = processor.group(dbTimeFrames);

        Collection<com.emlogis.common.availability.AvailcalViewDto.PrefCDTimeFrame> timeFrames
                = processor.buildFromGroups(prefsByGroups);

        return timeFrames;
    }

    /**
     * Returns all CI preference time frames for teams in "teamIds"
     *
     * @param teamIds
     * @return
     */
    public Collection<AvailcalViewDto.PrefCITimeFrame> getCIPreference(List<String> teamIds,
                                                                       Long startDate,
                                                                       Long endDate) {
        DateTime startDateTime = TimeUtil.toServerDateTime(startDate);
        DateTime endDateTime = TimeUtil.toServerDateTime(endDate);
        if (startDate == null || endDate == null){
            throw new ValidationException("Require startDate and endDate"); //TODO i18n
        }

        List<CIAvailabilityTimeFrame> dbTimeFrames = teamService.getCIAvailability(teamIds, startDateTime,
                endDateTime, true);

        //TODO: possibly it is the wrong way using the TimeZone of the first team's Site. Consult and Fix!
        Team theTeam = teamService.getTeam(new PrimaryKey(sessionService.getTenantId(), teamIds.get(0)));
        Site theSite = teamService.getSite(theTeam);

        CITimeFrameProcessor<AvailcalViewDto.PrefCITimeFrame> processor
                = new PrefCITimeFrameProcessor(theSite.getTimeZone());

        Map<AvailcalViewDto.CITimeFrame.GroupKey, List<CIAvailabilityTimeFrame>> availsByGroups
                = processor.group(dbTimeFrames);

        Collection<AvailcalViewDto.PrefCITimeFrame> timeFrames
                = processor.buildFromGroups(availsByGroups, startDateTime, endDateTime);

        return timeFrames;
    }

    /**
     * Returns all skills for teams in "teamIds"
     *
     * @param teamIds
     * @return
     */
    public List<Map<String, Object>> getTeamsSkills(List<String> teamIds) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Object[]> skillsList = teamService.getTeamsSkills(teamIds);
        
        if (skillsList!=null){
	        for (Object[] objects : skillsList) {
	            Map<String, Object> reportMap = new HashMap<>();
	            reportMap.put("teamId", objects[0]);
	            reportMap.put("skillId", objects[1]);
	            reportMap.put("skillName", objects[2]);
	            result.add(reportMap);
	        }
        }
        return result;
    }

    /**
     * Returns all shift patterns for teams in "teamIds"
     *
     * @param teamIds
     * @return
     */
    public List<DailyDemandDto> getShiftDemands(List<String> teamIds, Long date) {
        DateTime dateTime = TimeUtil.toServerDateTime(date);
        if (dateTime == null){
            throw new ValidationException("Date required!"); //TODO i18n
        }
        List<DailyDemandDto> result = new ArrayList<>();
        List<Object[]> skillsList = teamService.getShiftDemands(teamIds, dateTime);
        
        if (skillsList!=null){
	        for (Object[] obj : skillsList) {
	            DailyDemandDto dto = new DailyDemandDto();
	            dto.setShiftPatternId((String) obj[0]);
	            dto.setTeamId((String) obj[1]);
	            dto.setSkillId((String) obj[2]);
	            dto.setDayOfWeek(obj[3] != null ? DayOfWeek.values()[(Integer) obj[3]] : null);
	            dto.setCdDate(obj[4] != null ? ((Timestamp) obj[4]).getTime() : null);
	            dto.setMaxEmployeeCount((Integer) obj[5]);
	            dto.setId((String) obj[6]);
	            dto.setStartTime(obj[7] != null ? ((Time) obj[7]).getTime() : null);
	            dto.setLengthInMin((Integer) obj[8]);
	            dto.setEmployeeCount((Integer) obj[9]);
	            result.add(dto);
	        }
        }
        return result;
    }

    /**
     * Returns all shift requirements for teams in "teamIds"
     *
     * @param teamIds
     * @return
     */
    public List<ExtendedShiftReqDto> getShiftRequirements(List<String> teamIds, Long startDate, Long endDate) {
        DateTime startDateTime = TimeUtil.toServerDateTime(startDate);
        DateTime endDateTime = TimeUtil.toServerDateTime(endDate);
        if (startDate == null || endDate == null) {
            throw new ValidationException("Require startDate and endDate"); //TODO i18n
        }

        List<Object[]> pas = teamService.getExtendedShiftRequirements(startDateTime, endDateTime, teamIds);

        List<ExtendedShiftReqDto> result = new ArrayList<>();

        if (pas!=null){
	        for(Object[] obj : pas) {
	            ExtendedShiftReqDto dto = new ExtendedShiftReqDto();
	            dto.setId((String) obj[0]);
	            dto.setCdDate(obj[1] != null ? ((Timestamp) obj[1]).getTime() : null);
	            dto.setDayOfTheWeek((Integer) obj[2]);
	            dto.setShiftStartTimeMillis(obj[3] != null ? ((Time) obj[3]).getTime() : 0);
	            dto.setShiftLengthMillis(obj[4] != null ? ((Integer) obj[4]) * 60 * 1000 : 0);
	            dto.setEmployeeCount((Integer) obj[5]);
	            dto.setExcessCount((Integer) obj[6]);
	            dto.setTeamId((String) obj[7]);
	            dto.setSkillId((String) obj[8]);
	            result.add(dto);
	        }
        }
        return result;
    }

    /**
	 * checkSiteAndTeamAcl() checks Site and Team are compliant with required permissions and defined ACL
	 * Note that it would be more logical to check then site, but because of API that allow Site and Teams to be built independently
	 * the Site id is not always available from a team API. 
	 * Thus we have to load the Team to pull the Site, and then do the checks in any order. 
	 * (for the sake of performance and existing APIs we check team first and then site) 
	 * @param primaryKey
	 * @param acl
	 * @param siteCheckType
	 * @param sitePermissions
	 * @param teamCheckType
	 * @param teamPermissions
	 */
	public void checkSiteAndTeamAcl(PrimaryKey primaryKey, AccountACL acl,
                                    PermissionCheck siteCheckType, Permissions[] sitePermissions,
                                    PermissionCheck teamCheckType, Permissions[] teamPermissions) {
        // check team first
        Team team = (Team) aceService.checkAcl(primaryKey, acl, teamCheckType, teamPermissions);
        // then get site and check site.
        Site site = teamService.getSite(team);
        aceService.checkPermissions(site, acl, siteCheckType, sitePermissions);
	}

    private Team update(PrimaryKey teamPrimaryKey, TeamUpdateDto teamUpdateDto, Site site) {
        boolean modified = false;

        Team team = teamService.getTeam(teamPrimaryKey);

        validateTeamName(site.getPrimaryKey(), teamUpdateDto.getName(), team.getId());

        if (StringUtils.isNotBlank(teamUpdateDto.getName())) {
            team.setName(teamUpdateDto.getName());
            modified = true;
        }
        if (StringUtils.isNotBlank(teamUpdateDto.getDescription())) {
            team.setDescription(teamUpdateDto.getDescription());
            modified = true;
        }
        if (StringUtils.isNotBlank(teamUpdateDto.getAbbreviation())) {
            team.setAbbreviation(teamUpdateDto.getAbbreviation());
            modified = true;
        }
        if (teamUpdateDto.isActive() != null) {
            team.setActive(teamUpdateDto.isActive());
            modified = true;
        }

        if (modified) {
            setUpdatedBy(team);
            team = teamService.update(team);
        }

        return team;
    }

    private void validateTeamName(PrimaryKey sitePrimaryKey, String teamName, String teamId) {
        teamService.checkTeamNameUnicityOnSite(sitePrimaryKey, teamName, teamId);
    }

}