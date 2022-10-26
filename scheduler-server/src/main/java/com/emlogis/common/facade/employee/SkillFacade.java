package com.emlogis.common.facade.employee;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.services.employee.SkillService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.common.validation.annotations.*;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.employee.Skill;
import com.emlogis.model.employee.dto.SkillDto;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.structurelevel.dto.SiteDto;
import com.emlogis.model.structurelevel.dto.SiteTeamDto;
import com.emlogis.model.structurelevel.dto.TeamDto;
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
public class SkillFacade extends BaseFacade {

    @EJB
    private SkillService skillService;

    @EJB
    private TeamService teamService;

    /**
     * Getter for the skillService field
     *
     * @return
     */
    protected SkillService getSkillService() {
        return skillService;
    }

    /**
     * Get queried collection of Skills
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
    public ResultSetDto<SkillDto> getObjects(
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
        ResultSet<Skill> rs = skillService.findSkills(simpleQuery);
        return toResultSetDto(rs, SkillDto.class);
    }

    /**
     * Get specified Skill
     *
     * @param primaryKey
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public SkillDto getObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Skill.class)
            PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Skill skill = skillService.getSkill(primaryKey);
        return toDto(skill, SkillDto.class);
    }


    /**
     * Update skill
     *
     * @param primaryKey
     * @param skillDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public SkillDto updateObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Skill.class)
            PrimaryKey primaryKey,
            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = SkillDto.NAME, min = 1, max = 32),         // (max 32 in legacy app)
                            @ValidateStrLength(field = SkillDto.ABBREVIATION, min = 1, max = 20), // (max 12 in legacy app)
                            @ValidateStrLength(field = SkillDto.DESCRIPTION, min = 0, max = 50)   // (max 50 in legacy app)
                    },
                    uniques = {
                            @ValidateUnique(fields = SkillDto.NAME, type = Skill.class),          // (unique in legacy app)
                            @ValidateUnique(fields = SkillDto.ABBREVIATION, type = Skill.class),  // (unique in legacy app)
                    }
            )
            SkillDto skillDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (skillDto.getEndDate() > Constants.DATE_2000_01_01 && skillDto.getStartDate() > skillDto.getEndDate()) {
            throw new ValidationException(getMessage("validation.error.startdate.enddate"));
        }

        boolean modified = false;
        Skill skill = skillService.getSkill(primaryKey);

        if (!StringUtils.isBlank(skillDto.getName())) {
            skill.setName(skillDto.getName());
            modified = true;
        }
        if (!StringUtils.isBlank(skillDto.getDescription())) {
            skill.setDescription(skillDto.getDescription());
            modified = true;
        }
        if (!StringUtils.isBlank(skillDto.getAbbreviation())) {
            skill.setAbbreviation(skillDto.getAbbreviation());
            modified = true;
        }
        if (skillDto.getIsActive() != skill.getIsActive()) {
            skill.setIsActive(skillDto.getIsActive());
            modified = true;
        }
        if (skillDto.getStartDate() > 0) {
            skill.setStartDate(skillDto.getStartDate());
            modified = true;
        }
        if (skillDto.getEndDate() > 0) {
            skill.setEndDate(skillDto.getEndDate());
            modified = true;
        }

        if (modified) {
            setUpdatedBy(skill);
            skill = skillService.update(skill);
        }

        return toDto(skill, SkillDto.class);
    }


    /**
     * Create Skill
     *
     * @param primaryKey
     * @param skillDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public SkillDto createObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Skill.class, expectedResult = false)
            PrimaryKey primaryKey,
            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = SkillDto.NAME, min = 1, max = 40),         // (max 32 in legacy app)
                            @ValidateStrLength(field = SkillDto.ABBREVIATION, min = 1, max = 20), // (max 12 in legacy app)
                            @ValidateStrLength(field = SkillDto.DESCRIPTION, min = 0, max = 50)   // (max 50 in legacy app)
                    },
                    uniques = {
                            @ValidateUnique(fields = SkillDto.NAME, type = Skill.class),         // (unique in legacy app)
                            @ValidateUnique(fields = SkillDto.ABBREVIATION, type = Skill.class)  // (unique in legacy app)
                    }
            )
            SkillDto skillDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (skillDto.getEndDate() > Constants.DATE_2000_01_01 && skillDto.getStartDate() > skillDto.getEndDate()) {
            throw new ValidationException(getMessage("validation.error.startdate.enddate"));
        }

        Skill skill = skillService.createSkill(primaryKey, skillDto.getName(), skillDto.getAbbreviation(),
                skillDto.getDescription());

        if (skillDto.getIsActive() != skill.getIsActive()) {
            skill.setIsActive(skillDto.getIsActive());
        }
        skill.setStartDate(skillDto.getStartDate());
        skill.setEndDate(skillDto.getEndDate());

        setCreatedBy(skill);
        setOwnedBy(skill, null); // TODO for the sake of debugging we use name vs Id. to be replaced by Id in future
        skillService.update(skill);

        return toDto(skill, SkillDto.class);
    }

    /**
     * Delete Skill
     *
     * @param primaryKey
     * @return
     */
    @Validation
    public boolean deleteObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Skill.class) PrimaryKey primaryKey) {
        Skill skill = skillService.getSkill(primaryKey);
        skillService.delete(skill);
        return true;
    }

    @Validation
    public Collection<Map<String, Object>> getSiteTeamAssociations(
            @Validate(validator = EntityExistValidatorBean.class, type = Skill.class)
            PrimaryKey primaryKey) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        Collection<Map<String, Object>> result = new ArrayList<>();

        List<Object[]> rawObjects = skillService.getSiteTeamAssociations(primaryKey);
        for (Object[] raw : rawObjects) {
            Map<String, Object> rawMap = new HashMap<>();

            rawMap.put("teamId", raw[0]);
            rawMap.put("teamName", raw[1]);
            rawMap.put("teamDescription", raw[2]);
            rawMap.put("siteId", raw[3]);
            rawMap.put("siteName", raw[4]);
            rawMap.put("teamHasSkill", raw[5]);

            result.add(rawMap);
        }

        return result;
    }

    @Validation
    public Collection<SiteTeamDto> getTeamAssociations(
            @Validate(validator = EntityExistValidatorBean.class, type = Skill.class)
            PrimaryKey primaryKey) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        Collection<SiteTeamDto> result = new ArrayList<>();

        List<Object[]> rawObjects = skillService.getTeamAssociations(primaryKey);
        for (Object[] raw : rawObjects) {
            SiteTeamDto siteTeamDto = new SiteTeamDto();

            siteTeamDto.setTeamId((String) raw[0]);
            siteTeamDto.setTeamName((String) raw[1]);
            siteTeamDto.setSiteId((String) raw[2]);
            siteTeamDto.setSiteName((String) raw[3]);

            result.add(siteTeamDto);
        }

        return result;
    }

    @Validation
    public Collection<SiteTeamDto> updateTeamAssociations(
            @Validate(validator = EntityExistValidatorBean.class, type = Skill.class)
            PrimaryKey primaryKey,
            String[] teamIdArray) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        Skill skill = skillService.getSkill(primaryKey);

        List<Team> teamsToUnassociate = new ArrayList<>();
        Set<String> teamIdsToAdd = new HashSet<>();
        teamIdsToAdd.addAll(Arrays.asList(teamIdArray));

        Set<Team> teams = skill.getTeams();
        for (Team team : teams) {
            String teamId = team.getId();
            if (Arrays.binarySearch(teamIdArray, teamId) < 0) {
                teamsToUnassociate.add(team);
            } else {
                teamIdsToAdd.remove(teamId);
            }
        }

        teams.removeAll(teamsToUnassociate);
        for (Team team : teamsToUnassociate) {
            team.getSkills().remove(skill);
            teamService.update(team);
        }

        Collection<Team> teamsToAdd = teamService.getTeams(primaryKey.getTenantId(), teamIdsToAdd);
        for (Team team : teamsToAdd) {
            team.getSkills().add(skill);
            teamService.update(team);
        }
        teams.addAll(teamsToAdd);

        skillService.update(skill);

        return getTeamAssociations(primaryKey);
    }

    /**
     * Gets collection of Sites associated with this Skill
     *
     * @param skillPrimaryKey
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    @Deprecated  // Not part of originally prescribed API, but leaving in case it proves useful.
    @Validation
    public Collection<SiteDto> getSites(
            @Validate(validator = EntityExistValidatorBean.class, type = Skill.class)
            PrimaryKey skillPrimaryKey) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, IllegalArgumentException {
        Collection<Site> sites = skillService.getSites(skillPrimaryKey);
        return this.toCollectionDto(sites, SiteDto.class);
    }

    /**
     * Get collection of Teams associated with this Skill
     *
     * @param skillPrimaryKey
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    @Deprecated  // Not part of originally prescribed API, but leaving in case it proves useful.
    @Validation
    public Collection<TeamDto> getTeams(
            @Validate(validator = EntityExistValidatorBean.class, type = Skill.class)
            PrimaryKey skillPrimaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IllegalArgumentException {

        Collection<Team> teams = skillService.getTeams(skillPrimaryKey);
        return this.toCollectionDto(teams, TeamDto.class);
    }
}
    
