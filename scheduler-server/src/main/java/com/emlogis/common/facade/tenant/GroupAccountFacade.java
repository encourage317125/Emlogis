package com.emlogis.common.facade.tenant;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.services.tenant.AccountService;
import com.emlogis.common.services.tenant.GroupAccountService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.validation.annotations.*;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.dto.convenience.ACEConfigurationAllSitesDto;
import com.emlogis.model.tenant.Account;
import com.emlogis.model.tenant.GroupAccount;
import com.emlogis.model.tenant.Role;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.tenant.dto.*;
import com.emlogis.rest.resources.util.DtoMapper;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;

import org.apache.commons.lang3.StringUtils;

import javax.ejb.*;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.*;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class GroupAccountFacade extends AccountFacade {
	
	@EJB
	private RoleFacade roleFacade;

    @EJB
    private GroupAccountService groupAccountService;

    @EJB
    private UserAccountService userAccountService;

    @Override
    protected AccountService getAccountService() {
        return groupAccountService;
    }

    @Validation
    public ResultSetDto<GroupAccountDto> getObjects(
                String tenantId,
                String select,		// select is NOT IMPLEMENTED FOR NOW ..
                String filter,
                int offset,
                int limit,
                @ValidatePaging(name = Constants.ORDER_BY)
                String orderBy,
                @ValidatePaging(name = Constants.ORDER_DIR)
                String orderDir)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SimpleQuery simpleQuery = new SimpleQuery(tenantId);
        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true);
        ResultSet<GroupAccount> resultSet = groupAccountService.findGroupAccounts(simpleQuery);
        return toResultSetDto(resultSet, GroupAccountDto.class);
    }

    @Validation
    public GroupAccountDto getObject(
                @Validate(validator = EntityExistValidatorBean.class, type = GroupAccount.class) PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        GroupAccount groupAccount = groupAccountService.getGroupAccount(primaryKey);
        return toDto(groupAccount, GroupAccountDto.class);
    }

    @Validation
    public GroupAccountDto updateObject(
            	@Validate(validator = EntityExistValidatorBean.class, type = GroupAccount.class)
                PrimaryKey primaryKey,
                @ValidateUnique(fields = Constants.NAME, type = GroupAccount.class)
                GroupAccountDto accountDto) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, IllegalArgumentException {
    	
        GroupAccount account = groupAccountService.getGroupAccount(primaryKey);       
        boolean modified = updateNameAndDescr(account, accountDto.getName(), accountDto.getDescription());
        if (modified ) {
        	setUpdatedBy(account);
        	account = groupAccountService.update(account);  
            getEventService().sendEntityUpdateEvent(account, GroupAccountDto.class);
        }
        return toDto(account, GroupAccountDto.class);
    }

	private boolean updateNameAndDescr(GroupAccount account, String name, String description) {
		boolean modified = false;
        if (!StringUtils.isBlank(name) && !StringUtils.equals(account.getName(), name)) {
            account.setName(name);
            Role aclRole = groupAccountService.getACLRole(account);
            if (aclRole != null) {
            	aclRole.setName(name + " ACL");
            	aclRole.setDescription("ACL for Group: " + name);
            	getRoleService().update(aclRole);
            }      
            modified = true;
        }
        if (!StringUtils.isBlank(description) && !StringUtils.equals(account.getDescription(), description)) {
            account.setDescription(description);
            modified = true;
        }		
        return modified;
	}

	@Validation
    public boolean deleteObject(
            @Validate(validator = EntityExistValidatorBean.class, type = GroupAccount.class)
            PrimaryKey primaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
		GroupAccount account = groupAccountService.getGroupAccount(primaryKey);
        groupAccountService.delete(account);
        
        getEventService().sendEntityDeleteEvent(account, GroupAccountDto.class);
        return true;
    }

    @Validation
    public GroupAccountDto createObject(
                @Validate(validator = EntityExistValidatorBean.class, type = GroupAccount.class, expectedResult = false)
                PrimaryKey primaryKey,
                @ValidateUnique(fields = Constants.NAME, type = GroupAccount.class)
                @ValidateNotNullNumber(fields = {"name", "description"})
                AccountDto accountDto) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IllegalArgumentException {
        GroupAccount account = groupAccountService.createGroupAccount(primaryKey);
        updateNameAndDescr(account, accountDto.getName(), accountDto.getDescription());
        account.setCreatedBy(getActualUserId());
        setCreatedBy(account);
        setOwnedBy(account, null);
        groupAccountService.update(account);

        getEventService().sendEntityCreateEvent(account, GroupAccountDto.class);
        return toDto(account, GroupAccountDto.class);
    }

    @Validation
    public ResultSetDto<UserAccountDto> members(
                @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
                PrimaryKey primaryKey,
                String select,        // select is NOT IMPLEMENTED FOR NOW ..
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
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true)
                .setEntityClass(UserAccount.class);
        ResultSet<UserAccount> resultSet = groupAccountService.getMembers(primaryKey, simpleQuery);
        return toResultSetDto(resultSet, UserAccountDto.class);
    }

    @Validation
    public ResultSetDto<UserAccountDto> getUnassociatedMembers(
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
            PrimaryKey primaryKey,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
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
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true)
                .setEntityClass(UserAccount.class);
        ResultSet<UserAccount> resultSet = groupAccountService.getUnassociatedMembers(primaryKey, simpleQuery);
        return toResultSetDto(resultSet, UserAccountDto.class);
    }

    @Validation
    public boolean addMember(
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
            PrimaryKey groupPrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
            PrimaryKey memberPrimaryKey) {
    	// Should add / remove member update the group updated and updatedBy fields ??
        GroupAccount groupAccount = groupAccountService.getGroupAccount(groupPrimaryKey);
        UserAccount memberAccount = userAccountService.getUserAccount(memberPrimaryKey);
        return groupAccountService.addMember(groupAccount, memberAccount);
    }
    
    @Validation
    public boolean addMembers(
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
            PrimaryKey groupPrimaryKey,
            List<PrimaryKey> memberPrimaryKeys) {
    	// TODO, replace some code in getUsers()  by a new Array Validator when available
    	GroupAccount group = groupAccountService.getGroupAccount(groupPrimaryKey);
    	List<UserAccount> users = getUsers(memberPrimaryKeys);
    	for (UserAccount user : users) {
    		if (!group.getMembers().contains(user)) {  // silently skip users already associated
    			groupAccountService.addMember(group, user);
    		}
   		}
    	return true;
    }

    @Validation
    public boolean removeMember(
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
            PrimaryKey groupPrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
            PrimaryKey memberPrimaryKey) {
    	// Should add / remove member update the group updated and updatedBy fields ??
        GroupAccount groupAccount = groupAccountService.getGroupAccount(groupPrimaryKey);
        UserAccount memberAccount = userAccountService.getUserAccount(memberPrimaryKey);
        return groupAccountService.removeMember(groupAccount, memberAccount);
    }

    @Validation
    public boolean removeMembers(
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
            PrimaryKey groupPrimaryKey,
            List<PrimaryKey> memberPrimaryKeys) {
    	// TODO, replace some code in getUsers()  by a new Array Validator when available
    	GroupAccount group = groupAccountService.getGroupAccount(groupPrimaryKey);
    	List<UserAccount> users = getUsers(memberPrimaryKeys);
    	for (UserAccount user : users) {
    		if (group.getMembers().contains(user)) {  // silently skip users not associated
    			groupAccountService.removeMember(group, user);
    		}
   		}
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
                                            String orderDir) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        return groupAccountService.quickSearch(tenantId, searchValue, searchFields, returnedFields, limit, orderBy,
                orderDir);
    }

    @Validation
    public ResultSetDto<GroupReadDto> query(String tenantId,
                                            String searchValue,
                                            String searchFields,
                                            String userFilter,
                                            String roleFilter,
                                            String groupFilter,
                                            String filter,
                                            int offset,
                                            int limit,
                                            @ValidatePaging(name = Constants.ORDER_BY)
                                            String orderBy,
                                            @ValidatePaging(name = Constants.ORDER_DIR)
                                            String orderDir) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        ResultSet<Object[]> groupResultSet = groupAccountService.query(tenantId, searchValue, searchFields,
                userFilter, roleFilter, groupFilter, filter, offset, limit, orderBy, orderDir);

        List<GroupReadDto> groupReadDtos = new ArrayList<>();
        for (Object[] objects : groupResultSet.getResult()) {
            GroupReadDto groupReadDto = new GroupReadDto();
            groupReadDto.setId((String) objects[0]);
            groupReadDto.setName((String) objects[8]);
            groupReadDto.setDescription((String) objects[7]);
            groupReadDto.setCreatedBy((String) objects[3]);
            groupReadDto.setCreated(((Date) objects[2]).getTime());
            groupReadDto.setUpdatedBy((String) objects[6]);
            groupReadDto.setUpdated(((Date) objects[5]).getTime());
            groupReadDto.setOwnedBy((String) objects[4]);
            groupReadDto.setNbOfMembers(((BigInteger) objects[9]).intValue());
            groupReadDto.setNbOfRoles(((BigInteger) objects[10]).intValue());
            groupReadDto.setRoles((String) objects[11]);

            groupReadDtos.add(groupReadDto);
        }

        ResultSetDto<GroupReadDto> result = new ResultSetDto<>();

        result.setResult(groupReadDtos);
        result.setTotal(groupResultSet.getTotal());

        return result;
    }

    @Validation
    public ACEConfigurationAllSitesDto getSitesTeamsAces(
            @Validate(validator = EntityExistValidatorBean.class, type = GroupAccount.class)
            PrimaryKey groupPrimaryKey) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
    	Role aclRole = getAclRole(groupPrimaryKey);
        return roleFacade.getSitesTeamsAces(aclRole.getPrimaryKey());
    }

	@Validation
    public ACEConfigurationAllSitesDto setSitesTeamsAces(
            @Validate(validator = EntityExistValidatorBean.class, type = GroupAccount.class)
            PrimaryKey groupPrimaryKey,
            ACEConfigurationAllSitesDto aceConfigurationAllSitesDto) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
    	Role aclRole = getAclRole(groupPrimaryKey);
        return roleFacade.setSitesTeamsAces(aclRole.getPrimaryKey(), aceConfigurationAllSitesDto);
    }

	@Validation
    public GroupAccountViewDto getGroupAccountView(
            @Validate(validator = EntityExistValidatorBean.class, type = GroupAccount.class)
            PrimaryKey groupPrimaryKey) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        GroupAccount groupAccount = groupAccountService.getGroupAccount(groupPrimaryKey);

        DtoMapper<GroupAccount, GroupAccountViewDto> dtoMapper = new DtoMapper<>();
        dtoMapper.registerExceptDtoFieldForMapping("members");
        dtoMapper.registerExceptDtoFieldForMapping("roles");
        dtoMapper.registerExceptDtoFieldForMapping("aceConfigurationAllSitesDto");

        GroupAccountViewDto result = dtoMapper.map(groupAccount, GroupAccountViewDto.class);

        Role aclRole = getAclRole(groupPrimaryKey);
        ACEConfigurationAllSitesDto aceConfigurationAllSitesDto = roleFacade.getSitesTeamsAces(aclRole.getPrimaryKey());

        result.setAceConfigurationAllSitesDto(aceConfigurationAllSitesDto);

        List<GroupAccountViewDto.RoleDto> roleDtoList = new ArrayList<>();
        result.setRoles(roleDtoList);

        ResultSetDto<RoleDto> roleResultSet = findAccountRoles(groupPrimaryKey, null, null, 0, 0, "name", null);
        for (RoleDto roleDto : roleResultSet.getResult()) {
            GroupAccountViewDto.RoleDto dto = new GroupAccountViewDto.RoleDto();
            dto.setRoleId(roleDto.getId());
            dto.setName(roleDto.getName());
            dto.setDescription(roleDto.getDescription());

            roleDtoList.add(dto);
        }

        List<GroupAccountViewDto.MemberDto> memberDtoList = new ArrayList<>();
        result.setMembers(memberDtoList);
        Collection<Object[]> rows = groupAccountService.membersInfo(groupPrimaryKey);

        for (Object[] row : rows) {
            GroupAccountViewDto.MemberDto dto = new GroupAccountViewDto.MemberDto();
            dto.setAccountId((String) row[0]);
            dto.setEmployeeId((String) row[1]);
            dto.setName((String) row[2]);

            memberDtoList.add(dto);
        }

        return result;
    }

    private Role getAclRole(PrimaryKey groupPrimaryKey) throws ValidationException {
        GroupAccount group = groupAccountService.getGroupAccount(groupPrimaryKey);
        Role aclRole = groupAccountService.getACLRole(group);
        if (aclRole == null) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("groupId", group.getId());
            paramMap.put("groupName", group.getName());
            throw new ValidationException("Cannot get ACL for this group as it has no ACL Role.", paramMap);
        } else {
        	return aclRole;
        }
	}
    
    private List<UserAccount> getUsers(List<PrimaryKey> memberPrimaryKeys) {
        List<UserAccount> users = new ArrayList<>();
        if (memberPrimaryKeys != null) {
            for (PrimaryKey userKey : memberPrimaryKeys) {
                UserAccount user = (UserAccount) userAccountService.getAccount(userKey);
                if (user == null) {
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("accountId", userKey.getId());
                    throw new ValidationException(
                            "Cannot add/remove user to/from group as at least one user is invalid.", paramMap);
                } else {
                    users.add(user);
                }
            }
        }
        return users;
    }

}
