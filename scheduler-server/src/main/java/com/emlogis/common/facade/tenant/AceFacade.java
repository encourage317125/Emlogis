package com.emlogis.common.facade.tenant;

import com.emlogis.common.Constants;
import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.services.tenant.ACEService;
import com.emlogis.common.validation.annotations.Validate;
import com.emlogis.common.validation.annotations.ValidatePaging;
import com.emlogis.common.validation.annotations.Validation;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.ACE;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ACEDto;
import com.emlogis.model.dto.ACEUpdateDto;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.dto.convenience.MatchedDto;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.structurelevel.dto.SiteDto;
import com.emlogis.model.structurelevel.dto.TeamDto;
import com.emlogis.model.tenant.Role;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.tenant.dto.RoleDto;
import com.emlogis.model.tenant.dto.UserAccountDto;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AceFacade extends BaseFacade {

    @EJB
    private ACEService aceService;

    @Validation
    public ResultSetDto<ACEDto> getObjects(
            String tenantId,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(tenantId);
        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true)
                .setEntityClass(ACE.class);
        ResultSet<ACE> resultSet = aceService.findAcl(simpleQuery);
        return toResultSetDto(resultSet, ACEDto.class);
    }

    @Validation
    public ACEDto getObject(
            @Validate(validator = EntityExistValidatorBean.class, type = ACE.class)
            PrimaryKey acePrimaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        ACE ace = aceService.getAce(acePrimaryKey);
        return toDto(ace, ACEDto.class);
    }

    @Validation
    public void delete(
            @Validate(validator = EntityExistValidatorBean.class, type = ACE.class)
            PrimaryKey acePrimaryKey) {
        ACE ace = aceService.getAce(acePrimaryKey);
        aceService.delete(ace);
        getSessionService().updateAllSessionsACL();
    }

    @Validation
    public ACEDto update(
            @Validate(validator = EntityExistValidatorBean.class, type = ACE.class)
            PrimaryKey acePrimaryKey,
            ACEUpdateDto aceUpdateDto) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        ACE ace = aceService.getAce(acePrimaryKey);
        return updateAce(ace, aceUpdateDto);
    }

    public ACEDto updateAce(ACE ace, ACEUpdateDto aceUpdateDto) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        boolean modified = false;

        if (aceUpdateDto.getDescription() != null) {
            ace.setDescription(aceUpdateDto.getDescription());
            modified = true;
        }
        if (aceUpdateDto.getPattern() != null) {
            ace.setPattern(aceUpdateDto.getPattern());
            modified = true;
        }
        if (aceUpdateDto.getPermissions() != null) {
            ace.setPermissions(aceUpdateDto.getPermissions());
            modified = true;
        }

        if (modified) {
            ace = aceService.update(ace);
            getSessionService().updateAllSessionsACL();
        }

        return toDto(ace, ACEDto.class);
    }

    @Validation
    public Collection<RoleDto> getRoles(
            @Validate(validator = EntityExistValidatorBean.class, type = ACE.class)
            PrimaryKey acePrimaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        SimpleQuery simpleQuery = new SimpleQuery(acePrimaryKey.getTenantId());
        simpleQuery.setEntityClass(Role.class);

        Collection<Role> roles = aceService.findRoles(acePrimaryKey, simpleQuery);
        return toCollectionDto(roles, RoleDto.class);
    }

    @Validation
    public Collection<UserAccountDto> getUserAccounts(
            @Validate(validator = EntityExistValidatorBean.class, type = ACE.class)
            PrimaryKey acePrimaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        Collection<UserAccount> userAccounts = aceService.findUserAccounts(acePrimaryKey);
        return toCollectionDto(userAccounts, UserAccountDto.class);
    }

    @Validation
    public MatchedDto getMatched(
            @Validate(validator = EntityExistValidatorBean.class, type = ACE.class)
            PrimaryKey acePrimaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException {
        MatchedDto result = new MatchedDto();

        ACE ace = aceService.getAce(acePrimaryKey);

        Collection<Site> sites = aceService.getMatchedSites(ace);
        Collection<Team> teams = aceService.getMatchedTeams(ace);

        result.setSiteDtos(toCollectionDto(sites, SiteDto.class));
        result.setTeamDtos(toCollectionDto(teams, TeamDto.class));

        return result;
    }

}
