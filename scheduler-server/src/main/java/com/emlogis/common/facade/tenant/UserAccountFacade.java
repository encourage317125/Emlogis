package com.emlogis.common.facade.tenant;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.emlogis.common.exceptions.credentials.PasswordViolationException;
import com.emlogis.common.facade.employee.EmployeeFacade;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.EmployeeTeam;
import com.emlogis.model.employee.NotificationConfig;
import com.emlogis.model.employee.dto.EmployeeDto;
import com.emlogis.model.employee.dto.EmployeeInfoDto;
import com.emlogis.model.employee.dto.EmployeeUpdateDto;
import com.emlogis.model.employee.dto.NotificationSettingDto;
import com.emlogis.model.employee.dto.PasswordUpdateDto;
import com.emlogis.model.notification.MsgDeliveryType;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.tenant.*;
import com.emlogis.model.tenant.dto.*;
import com.emlogis.rest.resources.util.DtoMapper;

import org.apache.commons.lang3.StringUtils;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.notifications.NotificationType;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.notification.NotificationConfigInfo;
import com.emlogis.common.services.notification.NotificationService;
import com.emlogis.common.services.tenant.AccountService;
import com.emlogis.common.services.tenant.GroupAccountService;
import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.services.util.AccountUtilService;
import com.emlogis.common.validation.annotations.Validate;
import com.emlogis.common.validation.annotations.ValidateAll;
import com.emlogis.common.validation.annotations.ValidateNumeric;
import com.emlogis.common.validation.annotations.ValidatePaging;
import com.emlogis.common.validation.annotations.ValidatePassword;
import com.emlogis.common.validation.annotations.ValidateRegex;
import com.emlogis.common.validation.annotations.ValidateStrLength;
import com.emlogis.common.validation.annotations.ValidateUnique;
import com.emlogis.common.validation.annotations.Validation;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.security.SessionService;

import org.joda.time.DateTimeZone;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class UserAccountFacade extends AccountFacade {

    @EJB
    private TenantService tenantService;

    @EJB
    private UserAccountService userAccountService;

    @EJB
    private GroupAccountService groupAccountService;

    @EJB
    private SessionService sessionService;

    @EJB
    private EmployeeService employeeService;

    @EJB
    private EmployeeFacade employeeFacade;

    @EJB
	private NotificationService notificationService;
    
    @EJB
    private AccountUtilService accountUtilService;

    @Override
    protected AccountService getAccountService() {
        return userAccountService;
    }

    @Validation
    public ResultSetDto<UserAccountDto> getObjects(
                String tenantId,
                String select,		// select is NOT IMPLEMENTED FOR NOW ..
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
        ResultSet<UserAccount> rs = userAccountService.findUserAccounts(simpleQuery);
        return toResultSetDto(rs, UserAccountDto.class);
    }

    @Validation
    public UserAccountDto getObject(
                @Validate(validator = EntityExistValidatorBean.class, type = UserAccount.class)
                PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        UserAccount userAccount = userAccountService.getUserAccount(primaryKey);
        return toDto(userAccount, UserAccountDto.class);
    }

    @Validation
    public UserAccountDto updateObject(
        	@Validate(validator = EntityExistValidatorBean.class, type = UserAccount.class) PrimaryKey primaryKey,
            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = UserAccountDto.LOGIN, max = 64, passNull = true),
                            @ValidateStrLength(field = UserAccountDto.FIRST_NAME, min = 1, max = 50, passNull = true),
                            @ValidateStrLength(field = UserAccountDto.LAST_NAME, min = 1, max = 50, passNull = true),
                            @ValidateStrLength(field = UserAccountDto.MIDDLE_NAME, min = 1, max = 50, passNull = true),
                            @ValidateStrLength(field = UserAccountDto.WORK_EMAIL, min = 6, max = 256, passNull = true),
                            @ValidateStrLength(field = UserAccountDto.ADDRESS, min = 1, max = 50, passNull = true),
                            @ValidateStrLength(field = UserAccountDto.ADDRESS2, min = 1, max = 50, passNull = true),
                            @ValidateStrLength(field = UserAccountDto.CITY, min = 1, max = 50, passNull = true),
                            @ValidateStrLength(field = UserAccountDto.STATE, min = 1, max = 2, passNull = true),
                            @ValidateStrLength(field = UserAccountDto.ZIP, min = 1, max = 13, passNull = true),
                            @ValidateStrLength(field = UserAccountDto.GENDER, min = 1, max = 6, passNull = true),
                            @ValidateStrLength(field = UserAccountDto.HOME_PHONE, min = 1, max = 20, passNull = true),
                            @ValidateStrLength(field = UserAccountDto.WORK_PHONE, min = 1, max = 20, passNull = true),
                            @ValidateStrLength(field = UserAccountDto.MOBILE_PHONE, min = 1, max = 20, passNull = true),
                            @ValidateStrLength(field = UserAccountDto.HOME_EMAIL, min = 1, max = 50, passNull = true)
                    },
                    regexes = {
                            @ValidateRegex(field = UserAccountDto.WORK_EMAIL, regex = Constants.EMAIL_REGEX),
                            @ValidateRegex(field = UserAccountDto.HOME_EMAIL, regex = Constants.EMAIL_REGEX),
                            @ValidateRegex(field = UserAccountDto.LOGIN, regex = Constants.USERNAME_REGEX)
                    },
                    uniques = {
                            @ValidateUnique(fields = Constants.LOGIN, type = UserAccount.class)
                    }
            )
            @ValidateUnique(fields = Constants.NAME, type = UserAccount.class)
            UserAccountDto accountDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        UserAccount account = userAccountService.getUserAccount(primaryKey);

        boolean modified = updateUserAccount(account, accountDto);

        if (modified) {
            setUpdatedBy(account);
        	account = userAccountService.update(account);   	
            getEventService().sendEntityUpdateEvent(account, UserAccountDto.class);
        }

        return toDto(account, UserAccountDto.class);
    }

    @Validation
    public UserAccountDto createObject(
                @Validate(validator = EntityExistValidatorBean.class, type = UserAccount.class, expectedResult = false)
                PrimaryKey primaryKey,

                @ValidateAll(
                        strLengths = {
                                @ValidateStrLength(field = UserAccountDto.LOGIN, min = 1, max = 64),
                                @ValidateStrLength(field = UserAccountDto.FIRST_NAME, min = 1, max = 50),
                                @ValidateStrLength(field = UserAccountDto.LAST_NAME, min = 1, max = 50),
                                @ValidateStrLength(field = UserAccountDto.MIDDLE_NAME, min = 1, max = 50, passNull = true),
                                @ValidateStrLength(field = UserAccountDto.WORK_EMAIL, min = 6, max = 256, passNull = true),
                                @ValidateStrLength(field = UserAccountDto.ADDRESS, min = 1, max = 50, passNull = true),
                                @ValidateStrLength(field = UserAccountDto.ADDRESS2, min = 1, max = 50, passNull = true),
                                @ValidateStrLength(field = UserAccountDto.CITY, min = 1, max = 50, passNull = true),
                                @ValidateStrLength(field = UserAccountDto.STATE, min = 1, max = 2, passNull = true),
                                @ValidateStrLength(field = UserAccountDto.ZIP, min = 1, max = 13, passNull = true),
                                @ValidateStrLength(field = UserAccountDto.GENDER, min = 1, max = 6, passNull = true),
                                @ValidateStrLength(field = UserAccountDto.HOME_PHONE, min = 1, max = 20, passNull = true),
                                @ValidateStrLength(field = UserAccountDto.WORK_PHONE, min = 1, max = 20, passNull = true),
                                @ValidateStrLength(field = UserAccountDto.MOBILE_PHONE, min = 1, max = 20, passNull = true),
                                @ValidateStrLength(field = UserAccountDto.HOME_EMAIL, min = 1, max = 50, passNull = true)
                        },
                        regexes = {
                                @ValidateRegex(field = UserAccountDto.WORK_EMAIL, regex = Constants.EMAIL_REGEX),
                                @ValidateRegex(field = UserAccountDto.HOME_EMAIL, regex = Constants.EMAIL_REGEX),
                                @ValidateRegex(field = UserAccountDto.LOGIN, regex = Constants.USERNAME_REGEX)
                        },
                        uniques = {
                                @ValidateUnique(fields = Constants.LOGIN, type = UserAccount.class)
                        }
                )
                @ValidateNumeric(field = UserAccountDto.INACTIVITY_PERIOD, max = 30)
                UserAccountDto accountDto) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        UserAccount account = new UserAccount(primaryKey);	// use Login as unique Id for now ...
        account.setLogin(accountDto.getLogin());
        account.setFirstName(accountDto.getFirstName());
        account.setLastName(accountDto.getLastName());
        account.setDescription(accountDto.getDescription());
        account.setWorkEmail(accountDto.getWorkEmail());
        account.setPassword("chgpwd", false);

        updateUserAccount(account, accountDto);

        setCreatedBy(account);
        setOwnedBy(account, account.getName()); // TODO for the sake of debugging we use name vs Id. to be replaced by Id in future
        userAccountService.insert(account);

        getEventService().sendEntityCreateEvent(account, UserAccountDto.class);
        return toDto(account, UserAccountDto.class);
    }

    @Validation
    public boolean deleteObject(
            @Validate(validator = EntityExistValidatorBean.class, type = UserAccount.class) PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        UserAccount account = userAccountService.getUserAccount(primaryKey);
        Employee employee = account.getEmployee();
        if (employee != null) {
            throw new ValidationException(getMessage("referenced.entity.delete.error", "Employee", employee.getId()));
        }

        userAccountService.delete(account);

        getEventService().sendEntityDeleteEvent(account, UserAccountDto.class);
        return true;
    }

    @Validation
	public ResultSetDto<GroupAccountDto> groups(
            @Validate(validator = EntityExistValidatorBean.class, type = UserAccount.class)
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
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true)
                .setEntityClass(GroupAccount.class);
        ResultSet<GroupAccount> resultSet = userAccountService.getGroups(primaryKey, simpleQuery);
        return toResultSetDto(resultSet, GroupAccountDto.class);
    }

    @Validation
    public ResultSetDto<GroupAccountDto> getUnassociatedGroups(
                @Validate(validator = EntityExistValidatorBean.class, type = UserAccount.class) PrimaryKey primaryKey,
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
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true)
                .setEntityClass(GroupAccount.class);
        ResultSet<GroupAccount> resultSet = userAccountService.getUnassociatedGroups(primaryKey, simpleQuery);
        return toResultSetDto(resultSet, GroupAccountDto.class);
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
        return userAccountService.quickSearch(tenantId, searchValue, searchFields, returnedFields, limit, orderBy,
                orderDir);
    }

    @Validation
    public ResultSetDto<UserAccountQueryDto> query(String tenantId,
                                                   String searchValue,
                                                   String searchFields,
                                                   String employeeFilter,
                                                   int offset,
                                                   int limit,
                                                   @ValidatePaging(name = Constants.ORDER_BY)
                                                   String orderBy,
                                                   @ValidatePaging(name = Constants.ORDER_DIR)
                                                   String orderDir) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        ResultSet<Object[]> userResultSet = userAccountService.query(tenantId, searchValue, searchFields,
                employeeFilter, offset, limit, orderBy, orderDir);

        List<UserAccountQueryDto> queryDtos = new ArrayList<>();
        for (Object[] userInfo : userResultSet.getResult()) {
            UserAccountQueryDto dto = new UserAccountQueryDto();
            dto.setId((String) userInfo[0]);
            dto.setName((String) userInfo[1]);
            dto.setDescription((String) userInfo[2]);
            dto.setLogin((String) userInfo[3]);
            dto.setEmail((String) userInfo[4]);
            dto.setInactivityPeriod(((BigInteger) userInfo[5]).intValue());
            dto.setLanguage((String) userInfo[6]);
            dto.setStatus(AccountStatus.values()[(Integer) userInfo[7]]);
            dto.setEmployeeId((String) userInfo[8]);
            dto.setEmployeeFirstName((String) userInfo[9]);
            dto.setEmployeeLastName((String) userInfo[10]);
            dto.setNbOfGroups(((BigInteger) userInfo[11]).intValue());
            dto.setNbOfRoles(((BigInteger) userInfo[12]).intValue());
            dto.setGroups((String) userInfo[13]);
            dto.setRoles((String) userInfo[14]);

            queryDtos.add(dto);
        }

        ResultSetDto<UserAccountQueryDto> result = new ResultSetDto<>();

        result.setResult(queryDtos);
        result.setTotal(userResultSet.getTotal());

        return result;
    }
	
    @Validation
    public boolean addGroup(
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
            PrimaryKey userPrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
            PrimaryKey groupPrimaryKey) {
        UserAccount userAccount = userAccountService.getUserAccount(userPrimaryKey);
        GroupAccount groupAccount = groupAccountService.getGroupAccount(groupPrimaryKey);

        return userAccountService.addGroup(userAccount, groupAccount);
    }

    @Validation
    public boolean addGroups(
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
            PrimaryKey userPrimaryKey,
            List<PrimaryKey> groupPrimaryKeys) {
        UserAccount user = userAccountService.getUserAccount(userPrimaryKey);
        List<GroupAccount> groups = getGroups(groupPrimaryKeys);
        for (GroupAccount group : groups) {
            userAccountService.addGroup(user, group);
        }
        return true;
    }

    @Validation
    public boolean removeGroup(
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
            PrimaryKey userPrimaryKey,
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
            PrimaryKey groupPrimaryKey) {
        UserAccount userAccount = userAccountService.getUserAccount(userPrimaryKey);
        GroupAccount groupAccount = groupAccountService.getGroupAccount(groupPrimaryKey);

        return userAccountService.removeGroup(userAccount, groupAccount);
    }

    @Validation
    public boolean removeGroups(
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
            PrimaryKey userPrimaryKey,
            List<PrimaryKey> groupPrimaryKeys) {
        UserAccount user = userAccountService.getUserAccount(userPrimaryKey);
        List<GroupAccount> groups = getGroups(groupPrimaryKeys);
        for (GroupAccount group : groups) {
            userAccountService.removeGroup(user, group);
        }
        return true;
    }

    @Validation
    public PermissionRoleGroupMappingDto permissionsMapping(
            @Validate(validator = EntityExistValidatorBean.class, type = Account.class)
            PrimaryKey userPrimaryKey) {
        PermissionRoleGroupMappingDto result = new PermissionRoleGroupMappingDto();

        UserAccount user = userAccountService.getUserAccount(userPrimaryKey);

        Set<Role> userRoles = user.getRoles();
        for (Role role : userRoles) {
            Set<Permission> permissions = role.getPermissions();
            for (Permission permission : permissions) {
                String permissionId = permission.getId().getValue();
                String permissionName = permission.getName();

                result.add(new PermissionRoleGroupMappingDto.PermissionDto(permissionId, permissionName),
                        new PermissionRoleGroupMappingDto.RoleDto(role.getId(), role.getName()));
            }
        }

        Set<GroupAccount> groups = user.getGroupAccounts();
        for (GroupAccount group : groups) {
            Set<Role> groupRoles = group.getRoles();
            for (Role role : groupRoles) {
                Set<Permission> permissions = role.getPermissions();
                for (Permission permission : permissions) {
                    String permissionId = permission.getId().getValue();
                    String permissionName = permission.getName();

                    result.add(new PermissionRoleGroupMappingDto.PermissionDto(permissionId, permissionName),
                            new PermissionRoleGroupMappingDto.RoleDto(role.getId(), role.getName()),
                            new PermissionRoleGroupMappingDto.GroupDto(group.getId(), group.getName()));
                }
            }
        }

        return result;
    }

    private List<GroupAccount> getGroups(List<PrimaryKey> groupPrimaryKeys) {
        List<GroupAccount> result = new ArrayList<>();
        if (groupPrimaryKeys != null) {
            for (PrimaryKey primaryKey : groupPrimaryKeys) {
                GroupAccount group = (GroupAccount) groupAccountService.getAccount(primaryKey);
                if (group == null) {
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("accountId", primaryKey.getId());
                    throw new ValidationException(
                            "Cannot add/remove group to/from user as at least one group is invalid.", paramMap);
                } else {
                    result.add(group);
                }
            }
        }
        return result;
    }

    @Validation
    public NotificationSettingDto getNotificationSettings(
            @Validate(validator = EntityExistValidatorBean.class, type = UserAccount.class)
            PrimaryKey accountPrimaryKey) {
        UserAccount account = userAccountService.getUserAccount(accountPrimaryKey);
        return userAccountService.getNotificationSettings(account);
    }

    @Validation
    public NotificationSettingDto updateNotificationSettings(
            @Validate(validator = EntityExistValidatorBean.class, type = UserAccount.class)
            PrimaryKey accountPrimaryKey,
            NotificationSettingDto notificationSettingDto) {
        UserAccount account = userAccountService.getUserAccount(accountPrimaryKey);
        return userAccountService.updateNotificationSettings(account, notificationSettingDto);
    }

    @Validation
	public NotificationSettingDto enableNotificationSettings(
            @Validate(validator = EntityExistValidatorBean.class, type = UserAccount.class) PrimaryKey accountPrimaryKey,
			Boolean enable) {

    	UserAccount account = userAccountService.getUserAccount(accountPrimaryKey);
    	NotificationSettingDto notificationSettingDto = userAccountService.getNotificationSettings(account);
    	notificationSettingDto.setIsNotificationEnabled(enable);
        return userAccountService.updateNotificationSettings(account, notificationSettingDto);
	}
	
    @Validation
    public UserAccountViewDto getUserAccountView(
            @Validate(validator = EntityExistValidatorBean.class, type = UserAccount.class)
            PrimaryKey accountPrimaryKey) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        UserAccount account = userAccountService.getUserAccount(accountPrimaryKey);

        DtoMapper<UserAccount, UserAccountViewDto> dtoMapper = new DtoMapper<>();
        dtoMapper.registerExceptDtoFieldForMapping("groups");
        dtoMapper.registerExceptDtoFieldForMapping("roles");
        dtoMapper.registerExceptDtoFieldForMapping("inheritedRoles");

        UserAccountViewDto result = dtoMapper.map(account, UserAccountViewDto.class);

        List<UserAccountViewDto.RoleDto> roles = new ArrayList<>();
        List<UserAccountViewDto.RoleDto> inheritedRoles = new ArrayList<>();
        List<UserAccountViewDto.GroupDto> groups = new ArrayList<>();

        result.setRoles(roles);
        result.setInheritedRoles(inheritedRoles);
        result.setGroups(groups);

        Collection<Object[]> roleRows = userAccountService.rolesInfo(accountPrimaryKey);
        for (Object[] row : roleRows) {
            UserAccountViewDto.RoleDto role = new UserAccountViewDto.RoleDto();
            role.setRoleId((String) row[0]);
            role.setName((String) row[1]);
            role.setDescription((String) row[2]);

            roles.add(role);
        }

        Collection<Object[]> inheritedRoleRows = userAccountService.inheritedRolesInfo(accountPrimaryKey);
        for (Object[] row : inheritedRoleRows) {
            UserAccountViewDto.RoleDto role = new UserAccountViewDto.RoleDto();
            role.setRoleId((String) row[0]);
            role.setName((String) row[1]);
            role.setDescription((String) row[2]);

            inheritedRoles.add(role);
        }

        Collection<Object[]> groupRows = userAccountService.groupsInfo(accountPrimaryKey);
        for (Object[] row : groupRows) {
            UserAccountViewDto.GroupDto group = new UserAccountViewDto.GroupDto();
            group.setGroupId((String) row[0]);
            group.setName((String) row[1]);
            group.setDescription((String) row[2]);

            groups.add(group);
        }

        Employee employee = account.getEmployee();
        if (employee != null) {
        	Site site = account.getEmployee().getSite();
        	if (site != null) {
        		DateTimeZone timeZone = site.getTimeZone();
        		result.setTimeZone(timeZone.toString());
        	}
        }

        return result;
    }

    private boolean updateUserAccount(UserAccount userAccount, UserAccountDto userAccountDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        boolean modified = false;

        if (userAccountDto.getStatus() != null) {
            userAccount.setStatus(userAccountDto.getStatus());
            modified = true;
        }

        String firstName = userAccountDto.getFirstName();
        if (StringUtils.isNotBlank(firstName)) {
            userAccount.setFirstName(firstName);
            modified = true;
        }

        String lastName = userAccountDto.getLastName();
        if (StringUtils.isNotBlank(lastName)) {
            userAccount.setLastName(lastName);
            modified = true;
        }

        String middleName = userAccountDto.getMiddleName();
        if (StringUtils.isNotBlank(middleName)) {
            userAccount.setMiddleName(middleName);
            modified = true;
        }

        String workEmail = userAccountDto.getWorkEmail();
        if (StringUtils.isNotBlank(workEmail)) {
            userAccount.setWorkEmail(workEmail);
            modified = true;
        }

        if (StringUtils.isNotBlank(userAccountDto.getMobilePhone())) {
            userAccount.setMobilePhone(userAccountDto.getMobilePhone());
            modified = true;
        }

        if (StringUtils.isNotBlank(userAccountDto.getWorkPhone())) {
            userAccount.setWorkPhone(userAccountDto.getWorkPhone());
            modified = true;
        }

        if (userAccountDto.isNotificationEnabled() != userAccount.isNotificationEnabled()) {
            userAccount.setNotificationEnabled(userAccountDto.isNotificationEnabled());
            modified = true;
        }

        if (StringUtils.isNotBlank(userAccountDto.getCountry())) {
            userAccount.setCountry(userAccountDto.getCountry());
            modified = true;
        }

        String address = userAccountDto.getAddress();
        if (StringUtils.isNotBlank(address)) {
            userAccount.setAddress(address);
            modified = true;
        }

        String address2 = userAccountDto.getAddress2();
        if (StringUtils.isNotBlank(address2)) {
            userAccount.setAddress2(address2);
            modified = true;
        }

        String city = userAccountDto.getCity();
        if (StringUtils.isNotBlank(city)) {
            userAccount.setCity(city);
            modified = true;
        }

        String state = userAccountDto.getState();
        if (StringUtils.isNotBlank(state)) {
            userAccount.setState(state);
            modified = true;
        }

        String zip = userAccountDto.getZip();
        if (StringUtils.isNotBlank(zip)) {
            userAccount.setZip(zip);
            modified = true;
        }

        String gender = userAccountDto.getGender();
        if (StringUtils.isNotBlank(gender)) {
            userAccount.setGender(gender);
            modified = true;
        }

        String homePhone = userAccountDto.getHomePhone();
        if (StringUtils.isNotBlank(homePhone)) {
            userAccount.setHomePhone(homePhone);
            modified = true;
        }

        String homeEmail = userAccountDto.getHomeEmail();
        if (StringUtils.isNotBlank(homeEmail)) {
            userAccount.setHomeEmail(homeEmail);
            modified = true;
        }

        if (StringUtils.isNotBlank(userAccountDto.getLogin())) {
            userAccount.setLogin(userAccountDto.getLogin());
            modified = true;
        }

        if (StringUtils.isNotBlank(userAccountDto.getEmail())) {
            userAccount.setEmail(userAccountDto.getEmail());
            modified = true;
        }

        if (userAccountDto.getInactivityPeriod() != 0) {
            userAccount.setInactivityPeriod(userAccountDto.getInactivityPeriod());
            modified = true;
        }

        return modified;
    }

    @Validation
    public PasswordResetDto resetPassword(
            @Validate(validator = EntityExistValidatorBean.class, type = UserAccount.class)
            PrimaryKey accountPrimaryKey) {
    	UserAccount userAccount = userAccountService.getUserAccount(accountPrimaryKey);
    	return resetPassword(userAccount);
    }

    /**
     * Update the password of a UserAccount
     *
     * @param userPrimaryKey
     * @param passwordUpdateDto
     */
	
    @Validation
    public void updateUserAccountPassword(
            @Validate(validator = EntityExistValidatorBean.class, type = UserAccount.class)
            PrimaryKey userPrimaryKey,
            @ValidatePassword(passwordField = "newPassword")
            PasswordUpdateDto passwordUpdateDto,
            URI uri) throws PasswordViolationException {
        UserAccount userAccount = userAccountService.getUserAccount(userPrimaryKey);
        if (userAccount.getPassword().equals(passwordUpdateDto.getCurrentPassword())) {
            userAccountService.changePassword(userAccount, passwordUpdateDto.getNewPassword(), uri);
        } else {
            throw new PasswordViolationException(sessionService.getMessage("validation.password.incorrect"));
        }
        
        // event below is a bit misleading as actually this is the employee account which is modified, not the employee directly
        getEventService().sendEntityUpdateEvent(userAccount, UserAccountDto.class);
    }

    @Validation
    public AccountPictureDto getPicture(
            @Validate(validator = EntityExistValidatorBean.class, type = UserAccount.class)
            PrimaryKey userPrimaryKey) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, IOException {
        AccountPictureDto result = null;

        UserAccount userAccount = userAccountService.getUserAccount(userPrimaryKey);
        AccountPicture accountPicture = userAccount.getUserPicture();
        if (accountPicture != null) {
            DtoMapper<AccountPicture, AccountPictureDto> dtoMapper = new DtoMapper<>();
            dtoMapper.registerExceptDtoFieldForMapping("format");
            dtoMapper.registerExceptDtoFieldForMapping("width");
            dtoMapper.registerExceptDtoFieldForMapping("height");
            dtoMapper.registerExceptDtoFieldForMapping("size");

            result = dtoMapper.map(accountPicture, AccountPictureDto.class);

            if (result.getImage() != null) {
                InputStream byteInputStream = new ByteArrayInputStream(result.getImage());
                ImageInputStream imageInputStream = ImageIO.createImageInputStream(byteInputStream);
                Iterator<ImageReader> iterator = ImageIO.getImageReaders(imageInputStream);

                if (!iterator.hasNext()) {
                    throw new ValidationException(getMessage("employee.image.error"));
                }
                ImageReader reader = iterator.next();
                reader.setInput(imageInputStream);

                result.setFormat(reader.getFormatName());
                result.setWidth(reader.getWidth(reader.getMinIndex()));
                result.setHeight(reader.getHeight(reader.getMinIndex()));
                result.setSize(result.getImage().length);
            }
        }
        return result;
    }

    @Validation
    public boolean updatePicture(
            @Validate(validator = EntityExistValidatorBean.class, type = UserAccount.class)
            PrimaryKey userPrimaryKey,
            AccountPictureDto accountPictureDto) {
        UserAccount userAccount = userAccountService.getUserAccount(userPrimaryKey);
        AccountPicture accountPicture = userAccount.getUserPicture();
        if (accountPicture != null) {
        	accountPicture.setImage(accountPictureDto.getImage());
        	userAccountService.mergeAccountPicture(accountPicture);
        } else {
        	accountPicture = new AccountPicture(new PrimaryKey(userAccount.getTenantId()));
        	accountPicture.setImage(accountPictureDto.getImage());
        	accountPicture.setUserAccount(userAccount);
        	userAccount.setUserPicture(accountPicture);
        	userAccountService.persistAccountPicture(accountPicture);
        	userAccountService.update(userAccount);
        }
        return true;
    }

    public void deleteRememberMeByToken(String tokenId) {
        userAccountService.deleteRememberMeByToken(tokenId);
    }

    private PasswordResetDto resetPassword(UserAccount userAccount) {
        NotificationConfigInfo config = notificationService.userHasNotificationEnabled(userAccount,
                MsgDeliveryType.EMAIL);

        PasswordResetDto result = new PasswordResetDto();
        if (!config.isEnabled()) {
            // we cannot proceed with password reset because employee cannot receive email
            result.setInfo(config.getInfo());
        } else {
            // let's try to reset the password now and send notification
            String info = userAccountService.resetPassword(userAccount);
            result.setEmailAddress(config.getDeliveryAddress());
            result.setInfo(info);
        }
        return result;
    }

    @Validation
	public UserAccountInfoDto getUserAccountInfo(@Validate(validator = EntityExistValidatorBean.class, type = UserAccount.class) PrimaryKey  primaryKey)                                          
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	   	
        UserAccount userAccount = userAccountService.getUserAccount(primaryKey);
        UserAccountInfoDto infoDto = new UserAccountInfoDto();
        infoDto.setAccountDto(toDto(userAccount, UserAccountDto.class));

        DateTimeZone tenantTimeZone = tenantService.getTenant(primaryKey.getTenantId()).getTimeZone();
        // overrides TimeZone by 'tenant' time zone if they are different and user account Tz still on UTC
        if (tenantTimeZone != DateTimeZone.UTC && userAccount.getTimeZone() == DateTimeZone.UTC) {
        	infoDto.setActualTimeZone(tenantTimeZone);
        }
        else {
        	infoDto.setActualTimeZone(userAccount.getTimeZone());        	
        }
        return infoDto;
    }

}

