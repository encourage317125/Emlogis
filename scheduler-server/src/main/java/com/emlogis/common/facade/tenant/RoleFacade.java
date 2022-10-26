package com.emlogis.common.facade.tenant;

import com.emlogis.common.Constants;
import com.emlogis.common.ModelUtils;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.security.AccountACE;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.security.Permissions;
import com.emlogis.common.services.structurelevel.SiteService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.common.services.tenant.ACEService;
import com.emlogis.common.services.tenant.PermissionService;
import com.emlogis.common.services.tenant.RoleService;
import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.common.validation.annotations.Validate;
import com.emlogis.common.validation.annotations.ValidatePaging;
import com.emlogis.common.validation.annotations.Validation;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.ACE;
import com.emlogis.model.AccessType;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.common.CacheConstants;
import com.emlogis.model.dto.ACECreateDto;
import com.emlogis.model.dto.ACEDto;
import com.emlogis.model.dto.ACEUpdateDto;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.dto.convenience.*;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.structurelevel.dto.SiteDto;
import com.emlogis.model.structurelevel.dto.TeamDto;
import com.emlogis.model.tenant.*;
import com.emlogis.model.tenant.dto.*;
import com.emlogis.rest.resources.util.DtoMapper;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.server.services.cache.BasicCacheService;

import org.apache.commons.lang3.StringUtils;

import javax.ejb.*;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class RoleFacade extends BaseFacade {

    public final static String ALL_SITES_PATH = "/.*";
    public final static String ALL_SITE_TEAM_PATH = "/Site_Team/.*";

    @EJB
    private RoleService roleService;

    @EJB
    private PermissionService permissionService;

    @EJB
    private SiteService siteService;

    @EJB
    private TeamService teamService;

    @EJB
    private ACEService aceService;

    @EJB
    private AceFacade aceFacade;
    
    @EJB
    private TenantService tenantService;

    @EJB
    private BasicCacheService basicCacheService;
    
    @Validation
    public ResultSetDto<RoleDto> getObjects(
            String tenantId,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SimpleQuery simpleQuery = new SimpleQuery(tenantId);
        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equals(orderDir, "ASC")).setTotalCount(true);
        ResultSet<Role> resultSet = roleService.findRoles(simpleQuery);

        return toResultSetDto(resultSet, RoleDto.class);
    }

    @Validation
    public RoleDto getObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey primaryKey) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        return toDto(roleService.getRole(primaryKey), RoleDto.class);
    }

    @Validation
    public RoleDto updateObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey primaryKey,
            RoleDto roleDto) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        Role role = roleService.getRole(primaryKey);

        if (!StringUtils.isBlank(roleDto.getName())) {
            role.setName(roleDto.getName());
        }
        if (!StringUtils.isBlank(roleDto.getLabel())) {
            role.setLabel(roleDto.getLabel());
        }
        if (!StringUtils.isBlank(roleDto.getDescription())) {
            role.setDescription(roleDto.getDescription());
        }

        setUpdatedBy(role);
        roleService.update(role);

        getEventService().sendEntityUpdateEvent(role, RoleDto.class);
        return toDto(role, RoleDto.class);
    }

    @Validation
    public RoleDto createObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class, expectedResult = false)
            PrimaryKey primaryKey,
            RoleCreateDto roleCreateDto) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
    	
        Role role = roleService.createRole(primaryKey);    
        role.setName(roleCreateDto.getName());
        role.setDescription(roleCreateDto.getDescription());
        role.setLabel(roleCreateDto.getLabel());

        setCreatedBy(role);
        setOwnedBy(role, null);
        roleService.update(role);

        getEventService().sendEntityCreateEvent(role, RoleDto.class);
        return toDto(role, RoleDto.class);
    }


    /**
     * duplicateObject() duplicate a role including permissions and ACEs
     * @param prevRolePrimaryKey
     * @param roleCreateDto
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public RoleDto duplicateObject(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class, expectedResult = true)
            PrimaryKey prevRolePrimaryKey,
            RoleCreateDto roleCreateDto) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
    	
        Role prevRole = roleService.getRole(prevRolePrimaryKey);
        PrimaryKey newRolePrimaryKey = new PrimaryKey(prevRole.getTenantId());  
        Role newRole = roleService.createRole(newRolePrimaryKey); 
        
        newRole.setName(StringUtils.isBlank(roleCreateDto.getName()) ? prevRole.getName() : roleCreateDto.getName());
    	newRole.setDescription(StringUtils.isBlank(roleCreateDto.getDescription()) ? prevRole.getDescription()
                : roleCreateDto.getDescription());
    	newRole.setLabel(StringUtils.isBlank(roleCreateDto.getLabel()) ? prevRole.getLabel() : roleCreateDto.getLabel());

    	Set<Permission> permissions = prevRole.getPermissions();
    	for (Permission perm : permissions) {
    		newRole.addPermission(perm);
    	}
    	Set<ACE> acl = prevRole.getAcl();
    	for (ACE ace : acl) {
    		newRole.addAce(ace);
    	}    	
        setCreatedBy(newRole);
        setOwnedBy(newRole, null);
        roleService.update(newRole);

        getEventService().sendEntityCreateEvent(newRole, RoleDto.class);
        return toDto(newRole, RoleDto.class);
    }

    @Validation
    public boolean delete(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey primaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        Role role = roleService.getRole(primaryKey);
        roleService.delete(role);

        clearAclTeamIdsCache(primaryKey);

        getEventService().sendEntityDeleteEvent(role, RoleDto.class);
        return true;
    }

    @Validation
    public ResultSetDto<PermissionDto> getPermissions(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
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
        SimpleQuery simpleQuery = new SimpleQuery();
        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true)
                .setEntityClass(Permission.class);
        ResultSet<Permission> resultSet = roleService.getPermissions(primaryKey, simpleQuery);
        return toResultSetDto(resultSet, PermissionDto.class);
    }

    @Validation
    public boolean addPermission(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class) PrimaryKey primaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = Permission.class) Permissions permissionId) {
        Role role = roleService.getRole(primaryKey);
        clearAclTeamIdsCache(primaryKey);
        return roleService.addPermission(role, permissionId);
    }

    @Validation
    public boolean addPermissions(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey rolePrimaryKey,
            List<Permissions> permissions) {
    	Role role = roleService.getRole(rolePrimaryKey);
        Collection<Permission> permissionList = permissionService.getPermissionList(permissions);
    	for (Permission permission : permissionList) {
    		if (!role.getPermissions().contains(permission)) {  // silently skip permissions already associated
    			roleService.addPermission(role, permission.getId());
    		}
   		}
        clearAclTeamIdsCache(rolePrimaryKey);
    	return true;
    } 

    @Validation
    public boolean removePermission(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class) PrimaryKey primaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = Permission.class) Permissions permissionId) {
        Role role = roleService.getRole(primaryKey);
        return roleService.removePermission(role, permissionId);
    }

    @Validation
    public boolean removePermissions(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey rolePrimaryKey,
            List<Permissions> permissions) {
    	Role role = roleService.getRole(rolePrimaryKey);
        Collection<Permission> permissionList = permissionService.getPermissionList(permissions);
    	for (Permission permission : permissionList) {
    		if (role.getPermissions().contains(permission)) {  // silently skip permissions not associated
    			roleService.removePermission(role, permission.getId());
    		}
   		}
        clearAclTeamIdsCache(rolePrimaryKey);
    	return true;
    }
    
    @Validation
    public ResultSetDto<AccountDto> getUnassociatedAccounts(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey primaryKey,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir, 
            Class<?> accountClass) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(primaryKey.getTenantId());
        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equals(orderDir, "ASC")).setTotalCount(true)
                .setEntityClass(accountClass);
        ResultSet<Account> resultSet = roleService.getUnassociatedAccounts(primaryKey, simpleQuery);
        return toResultSetDto(resultSet, AccountDto.class);
    }

    @Validation
    public ResultSetDto<PermissionDto> getUnassociatedRolePermissions(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
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
        ResultSet<Permission> resultSet = roleService.getUnassociatedRolePermissions(primaryKey, filter, offset, limit,
                orderBy, orderDir);
        return toResultSetDto(resultSet, PermissionDto.class);
    }

    @Validation
    public ResultSetDto<ACEDto> getAcl(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey rolePrimaryKey,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(rolePrimaryKey.getTenantId());
        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true)
                .setEntityClass(ACE.class);
        ResultSet<ACE> resultSet = aceService.getAcl(rolePrimaryKey, simpleQuery);
        return toResultSetDto(resultSet, ACEDto.class);
    }

    @Validation
    public ACEDto getAce(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey rolePrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = ACE.class)
            PrimaryKey acePrimaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        ACE ace = getAceOfRole(rolePrimaryKey, acePrimaryKey);
        if (ace == null) {
            throw new ValidationException(getMessage("validation.error.ace.not.in.role"));
        } else {
            return toDto(ace, ACEDto.class);
        }
    }

    @Validation
    public ACEDto createAce(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey rolePrimaryKey,
            ACECreateDto aceCreateDto) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        if (aceCreateDto.getUpdateDto() == null) {
            throw new ValidationException(getMessage("validation.error.not.enough.params"));
        }
        Class entityClass;
        try {
            entityClass = Class.forName(aceCreateDto.getEntityClass());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ACE ace = aceService.createAce(
                new PrimaryKey(rolePrimaryKey.getTenantId()),
                entityClass,
                aceCreateDto.getUpdateDto().getPattern(),
                aceCreateDto.getUpdateDto().getPermissions(),
                aceCreateDto.getUpdateDto().getDescription());

        Role role = roleService.getRole(rolePrimaryKey);
        role.addAce(ace);
        roleService.update(role);

        clearAclTeamIdsCache(rolePrimaryKey);
        getSessionService().updateAllSessionsACL();

        return toDto(ace, ACEDto.class);
    }

    @Validation
    public ACEDto updateAce(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey rolePrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = ACE.class)
            PrimaryKey acePrimaryKey,
            ACEUpdateDto aceUpdateDto) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        ACE ace = getAceOfRole(rolePrimaryKey, acePrimaryKey);
        if (ace == null) {
            throw new ValidationException(getMessage("validation.error.ace.not.in.role"));
        } else {
            clearAclTeamIdsCache(rolePrimaryKey);
            return aceFacade.updateAce(ace, aceUpdateDto);
        }
    }

    @Validation
    public void deleteAce(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey rolePrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = ACE.class)
            PrimaryKey acePrimaryKey) {
        ACE ace = getAceOfRole(rolePrimaryKey, acePrimaryKey);
        if (ace == null) {
            throw new ValidationException(getMessage("validation.error.ace.not.in.role"));
        } else {
            clearAclTeamIdsCache(rolePrimaryKey);
            aceService.delete(ace);
            getSessionService().updateAllSessionsACL();
        }
    }

    @Validation
    public ResultSetDto<UserAccountDto> getUserAccounts(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey rolePrimaryKey,
            boolean inherited) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        Collection<UserAccount> userAccounts = roleService.findUserAccounts(rolePrimaryKey, inherited);
        Collection<UserAccountDto> dtos = toCollectionDto(userAccounts, UserAccountDto.class);
        ResultSetDto<UserAccountDto> rs = new ResultSetDto<>();
        rs.setResult(dtos).setTotal(dtos.size());
        return rs;
    }

    @Validation
    public ResultSetDto<GroupAccountDto> getGroupAccounts(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey rolePrimaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        Collection<GroupAccount> groupAccounts = roleService.findGroupAccounts(rolePrimaryKey);
        Collection<GroupAccountDto> dtos = toCollectionDto(groupAccounts, GroupAccountDto.class);
        ResultSetDto<GroupAccountDto> rs = new ResultSetDto<>();
        rs.setResult(dtos).setTotal(dtos.size());
        return rs;
    }

    @Validation
    public MatchedDto getMatched(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey rolePrimaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        MatchedDto result = new MatchedDto();

        Role role = roleService.getRole(rolePrimaryKey);

        Set<AccountACE> accountACEs = ModelUtils.buildAccountACESet(role.getAcl());
        AccountACL accountACL = new AccountACL();
        accountACL.getAcl().addAll(accountACEs);

        SimpleQuery siteSimpleQuery = new SimpleQuery(role.getTenantId());
        siteSimpleQuery.setEntityClass(Site.class);
        Collection<Site> sites = roleService.getMatchedSites(siteSimpleQuery, accountACL);

        SimpleQuery teamSimpleQuery = new SimpleQuery(role.getTenantId());
        teamSimpleQuery.setEntityClass(Team.class);
        Collection<Team> teams = roleService.getMatchedTeams(teamSimpleQuery, accountACL);

        result.setSiteDtos(toCollectionDto(sites, SiteDto.class));
        result.setTeamDtos(toCollectionDto(teams, TeamDto.class));

        return result;
    }

    @Validation
    public MatchedDto getMatched(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey rolePrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = ACE.class)
            PrimaryKey acePrimaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        ACE ace = getAceOfRole(rolePrimaryKey, acePrimaryKey);
        if (ace == null) {
            throw new ValidationException(getMessage("validation.error.ace.not.in.role"));
        } else {
            MatchedDto result = new MatchedDto();

            AccountACL accountACL = new AccountACL();
            accountACL.getAcl().add(ModelUtils.buildAccountACE(ace));

            SimpleQuery siteSimpleQuery = new SimpleQuery(rolePrimaryKey.getTenantId());
            siteSimpleQuery.setEntityClass(Site.class);
            Collection<Site> sites = roleService.getMatchedSites(siteSimpleQuery, accountACL);

            SimpleQuery teamSimpleQuery = new SimpleQuery(rolePrimaryKey.getTenantId());
            teamSimpleQuery.setEntityClass(Team.class);
            Collection<Team> teams = roleService.getMatchedTeams(teamSimpleQuery, accountACL);

            result.setSiteDtos(toCollectionDto(sites, SiteDto.class));
            result.setTeamDtos(toCollectionDto(teams, TeamDto.class));

            return result;
        }
    }

    @Validation
    public ACEConfigurationAllSitesDto getSitesTeamsAces(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey rolePrimaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        ACEConfigurationAllSitesDto result = new ACEConfigurationAllSitesDto();

        String tenantId = rolePrimaryKey.getTenantId();

        Collection<ACE> siteACEs = roleService.getACEsByEntityClass(rolePrimaryKey, Site.class.getSimpleName());
        Collection<ACE> teamACEs = roleService.getACEsByEntityClass(rolePrimaryKey, Team.class.getSimpleName());

        AccessType allSiteAccessType = getAccessTypeByPath(siteACEs, ALL_SITES_PATH);
        result.setAllSitesAccessType(allSiteAccessType);

        SimpleQuery simpleQuery = new SimpleQuery(tenantId);
        SimpleQueryHelper.tryAddNotDeletedFilter(simpleQuery);
        ResultSet<Site> siteResultSet = siteService.findSites(simpleQuery, null);
        Collection<Site> sites = siteResultSet.getResult();

        Set<ACEConfigurationSiteDto> siteDtos = new HashSet<>();
        result.setResult(siteDtos);

        for (Site site : sites) {
            ACEConfigurationSiteDto ACEConfigurationSiteDto = new ACEConfigurationSiteDto();
            siteDtos.add(ACEConfigurationSiteDto);

            ACEConfigurationSiteDto.setId(site.getId());
            ACEConfigurationSiteDto.setDescription(site.getDescription());
            ACEConfigurationSiteDto.setName(site.getName());
            ACEConfigurationSiteDto.setAccessType(getAccessTypeByPath(siteACEs, site.getPath()));

            ACEConfigurationAllTeamsDto ACEConfigurationAllTeamsDto = new ACEConfigurationAllTeamsDto();
            ACEConfigurationSiteDto.setTeamsDto(ACEConfigurationAllTeamsDto);
            Set<ACEConfigurationTeamDto> teamDtos = new HashSet<>();

            AccessType allTeamAccessType = getAccessTypeByPath(teamACEs, site.getPath() + ALL_SITE_TEAM_PATH);
            ACEConfigurationAllTeamsDto.setAllTeamsAccessType(allTeamAccessType);
            ACEConfigurationAllTeamsDto.setTeamDtos(teamDtos);

            List<Team> teams = siteService.getTeams(site);
            if (teams != null) {
                for (Team team : teams) {
                    ACEConfigurationTeamDto ACEConfigurationTeamDto = new ACEConfigurationTeamDto();
                    teamDtos.add(ACEConfigurationTeamDto);

                    ACEConfigurationTeamDto.setId(team.getId());
                    ACEConfigurationTeamDto.setDescription(team.getDescription());
                    ACEConfigurationTeamDto.setName(team.getName());
                    ACEConfigurationTeamDto.setAccessType(getAccessTypeByPath(teamACEs, team.getPath()));
                }
            }
        }

        return result;
    }

    @Validation
    public ACEConfigurationAllSitesDto setSitesTeamsAces(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey rolePrimaryKey,
            ACEConfigurationAllSitesDto aceConfigurationAllSitesDto) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        String tenantId = rolePrimaryKey.getTenantId();

        Role role = roleService.getRole(rolePrimaryKey);
        Set<ACE> acl = role.getAcl();
        if (acl != null) {
            aceService.deleteAcl(acl);
            acl.clear();
        }

        String roleName = role.getName();
        Set<Permissions> allSitesACEPermissions = null;
        String allSitesDescription = null;
        switch (aceConfigurationAllSitesDto.getAllSitesAccessType()) {
            case RO:
                allSitesACEPermissions = new HashSet<>();
                allSitesACEPermissions.add(Permissions.OrganizationProfile_View);
                allSitesDescription = roleName + " READ only access to all sites";
                break;
            case RW:
                allSitesACEPermissions = new HashSet<>();
                allSitesACEPermissions.add(Permissions.OrganizationProfile_View);
                allSitesACEPermissions.add(Permissions.OrganizationProfile_Mgmt);
                allSitesDescription = roleName + " READ/WRITE access to all sites";
                break;
        }
        if (allSitesACEPermissions != null) {
            ACE ace = aceService.createAce(new PrimaryKey(tenantId), Site.class, ALL_SITES_PATH,
                    allSitesACEPermissions, allSitesDescription);
            role.addAce(ace);
        }

        if (AccessType.RW.equals(aceConfigurationAllSitesDto.getAllSitesAccessType())) {
            ACE ace = aceService.createAce(new PrimaryKey(tenantId), Team.class, ALL_SITES_PATH + ALL_SITE_TEAM_PATH,
                    allSitesACEPermissions, "");
            role.addAce(ace);
        } else {
            Set<ACEConfigurationSiteDto> siteDtos = aceConfigurationAllSitesDto.getResult();
            for (ACEConfigurationSiteDto siteDto : siteDtos) {
                Set<Permissions> siteACEPermissions = null;
                String siteDescription = null;
                switch (siteDto.getAccessType()) {
                    case RO:
                        siteACEPermissions = new HashSet<>();
                        siteACEPermissions.add(Permissions.OrganizationProfile_View);
                        siteDescription = roleName + " READ only access to the site " + siteDto.getName();
                        break;
                    case RW:
                        siteACEPermissions = new HashSet<>();
                        siteACEPermissions.add(Permissions.OrganizationProfile_View);
                        siteACEPermissions.add(Permissions.OrganizationProfile_Mgmt);
                        siteDescription = roleName + " READ/WRITE access to the site " + siteDto.getName();
                        break;
                }
                if (siteACEPermissions != null) {
                    Site site = siteService.getSite(new PrimaryKey(tenantId, siteDto.getId()));
                    ACE ace = aceService.createAce(new PrimaryKey(tenantId), Site.class, site.getPath(),
                            siteACEPermissions, siteDescription);
                    role.addAce(ace);
                }

                ACEConfigurationAllTeamsDto aceConfigurationAllTeamsDto = siteDto.getTeamsDto();

                Set<Permissions> allTeamsACEPermissions = null;
                String allTeamDescription = null;
                switch (aceConfigurationAllTeamsDto.getAllTeamsAccessType()) {
                    case RO:
                        allTeamsACEPermissions = new HashSet<>();
                        allTeamsACEPermissions.add(Permissions.OrganizationProfile_View);
                        allTeamDescription = roleName + " READ only access to all teams of site " + siteDto.getName();
                        break;
                    case RW:
                        allTeamsACEPermissions = new HashSet<>();
                        allTeamsACEPermissions.add(Permissions.OrganizationProfile_View);
                        allTeamsACEPermissions.add(Permissions.OrganizationProfile_Mgmt);
                        allTeamDescription = roleName + " READ/WRITE access to all teams of site " + siteDto.getName();
                        break;
                }
                if (allTeamsACEPermissions != null) {
                    Site site = siteService.getSite(new PrimaryKey(tenantId, siteDto.getId()));
                    ACE ace = aceService.createAce(new PrimaryKey(tenantId), Team.class,
                            site.getPath() + ALL_SITE_TEAM_PATH, allTeamsACEPermissions, allTeamDescription);
                    role.addAce(ace);
                }

                Set<ACEConfigurationTeamDto> teamDtos = aceConfigurationAllTeamsDto.getTeamDtos();
                for (ACEConfigurationTeamDto teamDto : teamDtos) {
                    Set<Permissions> teamACEPermissions = null;
                    String teamDescription = null;
                    switch (teamDto.getAccessType()) {
                        case RO:
                            teamACEPermissions = new HashSet<>();
                            teamACEPermissions.add(Permissions.OrganizationProfile_View);
                            teamDescription = roleName + " READ only access to the team " + teamDto.getName();
                            break;
                        case RW:
                            teamACEPermissions = new HashSet<>();
                            teamACEPermissions.add(Permissions.OrganizationProfile_View);
                            teamACEPermissions.add(Permissions.OrganizationProfile_Mgmt);
                            teamDescription = roleName + " READ/WRITE access to the team " + teamDto.getName();
                            break;
                    }
                    if (teamACEPermissions != null) {
                        Team team = teamService.getTeam(new PrimaryKey(tenantId, teamDto.getId()));
                        ACE ace = aceService.createAce(new PrimaryKey(tenantId), Team.class, team.getPath(),
                                teamACEPermissions, teamDescription);
                        role.addAce(ace);
                    }
                }
            }
        }
        roleService.update(role);
        clearAclTeamIdsCache(rolePrimaryKey);
        return getSitesTeamsAces(rolePrimaryKey);
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
                                          String orderDir) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        return roleService.quickSearch(tenantId, searchValue, searchFields, returnedFields, limit, orderBy, orderDir);
    }

    @Validation
    public ResultSetDto<RoleReadDto> query(String tenantId,
                                           String searchValue,
                                           String searchFields,
                                           String userFilter,
                                           String groupFilter,
                                           int offset,
                                           int limit,
                                           @ValidatePaging(name = Constants.ORDER_BY)
                                           String orderBy,
                                           @ValidatePaging(name = Constants.ORDER_DIR)
                                           String orderDir) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        ResultSet<Object[]> roleResultSet = roleService.query(tenantId, searchValue, searchFields, userFilter,
                groupFilter, offset, limit, orderBy, orderDir);

        List<RoleReadDto> roleReadDtos = new ArrayList<>();
        for (Object[] roleInfo : roleResultSet.getResult()) {
            RoleReadDto roleReadDto = new RoleReadDto();
            roleReadDto.setId((String) roleInfo[0]);
            roleReadDto.setName((String) roleInfo[8]);
            roleReadDto.setDescription((String) roleInfo[6]);
            roleReadDto.setLabel((String) roleInfo[7]);
            roleReadDto.setCreatedBy((String) roleInfo[2]);
            roleReadDto.setUpdatedBy((String) roleInfo[5]);
            roleReadDto.setOwnedBy((String) roleInfo[3]);
            roleReadDto.setCreated(((Date) roleInfo[1]).getTime());
            roleReadDto.setUpdated(((Date) roleInfo[4]).getTime());
            roleReadDto.setGroups((String) roleInfo[11]);
            roleReadDto.setNbOfGroups(((BigInteger) roleInfo[9]).intValue());
            roleReadDto.setNbOfMembers(((BigInteger) roleInfo[10]).intValue());

            roleReadDtos.add(roleReadDto);
        }

        ResultSetDto<RoleReadDto> result = new ResultSetDto<>();

        result.setResult(roleReadDtos);
        result.setTotal(roleResultSet.getTotal());

        return result;
    }

    @Validation
    public RoleViewDto getRoleView(
            @Validate(validator = EntityExistValidatorBean.class, type = Role.class)
            PrimaryKey rolePrimaryKey) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        Role role = roleService.getRole(rolePrimaryKey);

        DtoMapper<Role, RoleViewDto> dtoMapper = new DtoMapper<>();
        dtoMapper.registerExceptDtoFieldForMapping("members");
        dtoMapper.registerExceptDtoFieldForMapping("groups");
        dtoMapper.registerExceptDtoFieldForMapping("permissions");
        dtoMapper.registerExceptDtoFieldForMapping("aceConfigurationAllSitesDto");

        RoleViewDto result = dtoMapper.map(role, RoleViewDto.class);

        List<RoleViewDto.MemberDto> members = new ArrayList<>();
        List<RoleViewDto.GroupDto> groups = new ArrayList<>();
        List<RoleViewDto.PermissionDto> permissions = new ArrayList<>();

        result.setMembers(members);
        result.setGroups(groups);
        result.setPermissions(permissions);

        Collection<UserAccount> userAccounts = roleService.findUserAccounts(rolePrimaryKey, false);
        for (UserAccount userAccount : userAccounts) {
            RoleViewDto.MemberDto dto = new RoleViewDto.MemberDto();
            dto.setAccountId(userAccount.getId());
            dto.setName(userAccount.getName());
            dto.setEmployeeId(userAccount.getEmployeeId());

            members.add(dto);
        }

        Collections.sort(members, new Comparator<RoleViewDto.MemberDto>() {
            @Override
            public int compare(RoleViewDto.MemberDto o1, RoleViewDto.MemberDto o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            }
        });

        Collection<GroupAccount> groupAccounts = roleService.findGroupAccounts(rolePrimaryKey);
        for (GroupAccount groupAccount : groupAccounts) {
            RoleViewDto.GroupDto dto = new RoleViewDto.GroupDto();
            dto.setGroupId(groupAccount.getId());
            dto.setName(groupAccount.getName());
            dto.setDescription(groupAccount.getDescription());

            groups.add(dto);
        }

        Collections.sort(groups, new Comparator<RoleViewDto.GroupDto>() {
            @Override
            public int compare(RoleViewDto.GroupDto o1, RoleViewDto.GroupDto o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            }
        });

        SimpleQuery simpleQuery = new SimpleQuery();
        simpleQuery.setOrderByField("name").setEntityClass(Permission.class);
        ResultSet<Permission> resultSet = roleService.getPermissions(rolePrimaryKey, simpleQuery);
        if (resultSet.getResult() != null) {
            for (Permission permission : resultSet.getResult()) {
                RoleViewDto.PermissionDto dto = new RoleViewDto.PermissionDto();
                dto.setPermissionId(permission.getId().getValue());
                dto.setName(permission.getName());
                dto.setDescription(permission.getDescription());

                permissions.add(dto);
            }
        }

        ACEConfigurationAllSitesDto aceConfigurationAllSitesDto = getSitesTeamsAces(rolePrimaryKey);
        result.setAceConfigurationAllSitesDto(aceConfigurationAllSitesDto);

        return result;
    }


    private AccessType getAccessTypeByPath(Collection<ACE> aces, String path) {
        AccessType result = AccessType.Void;

        Set<Permissions> permissionsSet = findPermissionsByPattern(aces, path);

        if (permissionsSet != null) {
            for (Permissions permissions : permissionsSet) {
                if (Permissions.OrganizationProfile_Mgmt.equals(permissions)) {
                    result = AccessType.RW;
                    break;
                } else if (Permissions.OrganizationProfile_View.equals(permissions)) {
                    result = AccessType.RO;
                }
            }
        }
        return result;
    }

    private Set<Permissions> findPermissionsByPattern(Collection<ACE> aces, String path) {
        for (ACE ace : aces) {
            Pattern pattern = Pattern.compile(ace.getPattern());
            Matcher matcher = pattern.matcher(path);
            if (matcher.matches()) {
                return ace.getPermissions();
            }
        }
        return null;
    }

    private ACE getAceOfRole(PrimaryKey rolePrimaryKey, PrimaryKey acePrimaryKey) {
        Role role = roleService.getRole(rolePrimaryKey);
        ACE ace = aceService.getAce(acePrimaryKey);
        if (role.getAcl() != null && role.getAcl().contains(ace)) {
            return ace;
        } else {
            return null;
        }
    }

    private void clearAclTeamIdsCache(PrimaryKey rolePrimaryKey) {
        Collection<Employee> employees = roleService.employeesInRole(rolePrimaryKey);
        for (Employee employee : employees) {
            basicCacheService.clearEntry(CacheConstants.ALLOWED_BY_ACL_TEAM_IDS_CACHE, rolePrimaryKey.getTenantId(),
                    employee.getId());
        }
    }

}
