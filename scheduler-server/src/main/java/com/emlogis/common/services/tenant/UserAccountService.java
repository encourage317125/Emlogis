package com.emlogis.common.services.tenant;

import com.emlogis.common.*;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.notifications.NotificationCategory;
import com.emlogis.common.notifications.NotificationOperation;
import com.emlogis.common.notifications.NotificationResult;
import com.emlogis.common.notifications.NotificationType;
import com.emlogis.common.security.AccountACE;
import com.emlogis.common.security.PermissionType;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.notification.NotificationService;
import com.emlogis.common.services.util.AccountUtilService;
import com.emlogis.common.validation.annotations.Validate;
import com.emlogis.common.validation.annotations.Validation;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.NotificationConfig;
import com.emlogis.model.employee.dto.NotificationSettingDto;
import com.emlogis.model.notification.MsgDeliveryType;
import com.emlogis.model.notification.dto.NotificationMessageDTO;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.tenant.*;
import com.emlogis.rest.resources.util.QueryPattern;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.rest.security.SessionService;
import com.emlogis.server.services.PasswordCoder;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.ejb.*;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.URI;
import java.sql.Timestamp;
import java.util.*;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class UserAccountService extends AccountService<UserAccount> {

    @EJB
    private TenantService tenantService;

    @EJB
    private PasswordCoder passwordCoder;
    
	@EJB
	private AccountUtilService accountUtilService;

	@EJB
	private NotificationService notificationService;

	@EJB
	private SessionService sessionService;

    @EJB
    private ResourcesBundle resourcesBundle;

	/**
	 * findUserAccounts() find a list of UserAccounts matching criteria;
	 * @param simpleQuery
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public ResultSet<UserAccount> findUserAccounts(SimpleQuery simpleQuery) {
		simpleQuery.setEntityClass(UserAccount.class);
		SimpleQueryHelper sqh = new SimpleQueryHelper();
		return sqh.executeSimpleQueryWithPaging(getEntityManager(), simpleQuery);
	}

    public List<UserAccount> quickFindUserAccounts(String tenantId, long lastStateCheckedDate, int offset, int limit) {
        String sql = "SELECT * FROM EGS.UserAccount " +
                     " WHERE lastStateCheckedDate < :lastStateCheckedDate AND tenantId = :tenantId " +
                     " ORDER BY lastStateCheckedDate";
        Query query = getEntityManager().createNativeQuery(sql, UserAccount.class);

        query.setParameter("lastStateCheckedDate", new Timestamp(lastStateCheckedDate));
        query.setParameter("tenantId", tenantId);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        return query.getResultList();
    }

	public UserAccount getUserAccount(PrimaryKey primaryKey) {
		return getEntityManager().find(UserAccount.class, primaryKey);
	}
	
	public UserAccount getUserAccountBylogin(String tenantId, String login) {
		String queryStr = "SELECT users FROM UserAccount users " +
                "WHERE users.primaryKey.tenantId = :tenantId AND users.login = :login";
		Query query = getEntityManager().createQuery(queryStr);
    	query.setParameter("tenantId", tenantId);
    	query.setParameter("login", login);
		UserAccount userAccount;
		try {
			userAccount = (UserAccount) query.getSingleResult();
		} catch (NoResultException e) {
			userAccount = null;
		}
		return userAccount;
	}

    public void delete(UserAccount userAccount) {
    	// TODO Revisit Employee/Account relationship.  Initial implementation of 
    	//      Employee/UserAccount relationship is one directional with Employee 
    	//      owning/cascading. This means that an Employee-related UserAccount  
    	//      deletion of from this method will fail unless UserAccount is first 
    	//      dissociated from Employee. Consider making relationship bidirectional.
    	super.delete(userAccount);
    }

    public NotificationSettingDto getNotificationSettings(UserAccount userAccount) {
        NotificationSettingDto result = new NotificationSettingDto();
        result.setHomeEmail(userAccount.getHomeEmail());
        result.setWorkEmail(userAccount.getWorkEmail());
        result.setMobilePhone(userAccount.getMobilePhone());

        Map<NotificationType, Boolean> notificationTypes = new HashMap<>();
        Map<NotificationType, Boolean> accountNotificationTypes = userAccount.getNotificationTypes();
        if (accountNotificationTypes != null) {
            for (Map.Entry<NotificationType, Boolean> entry : accountNotificationTypes.entrySet()) {
                    notificationTypes.put(entry.getKey(), entry.getValue());
            }
        }
        result.setNotificationTypes(notificationTypes);
        result.setIsNotificationEnabled(userAccount.isNotificationEnabled());

        Set<NotificationConfig> employeeNotificationConfigs = userAccount.getNotificationConfigs();
        Collection<NotificationSettingDto.NotificationConfigDto> notificationConfigDtos = new ArrayList<>();
        for (NotificationConfig notificationConfig : employeeNotificationConfigs) {
            NotificationSettingDto.NotificationConfigDto dto = new NotificationSettingDto.NotificationConfigDto();
            notificationConfigDtos.add(dto);
            dto.setEnabled(notificationConfig.getEnabled());
            dto.setFormat(notificationConfig.getFormat());
            dto.setMethod(notificationConfig.getMethod());
        }
        result.setNotificationConfigs(notificationConfigDtos);
        return result;
    }

    public NotificationSettingDto updateNotificationSettings(UserAccount userAccount,
                                                             NotificationSettingDto notificationSettingDto) {
        userAccount.setNotificationEnabled(notificationSettingDto.getIsNotificationEnabled());

        Map<NotificationType, Boolean> notificationTypes = notificationSettingDto.getNotificationTypes();
        if (notificationTypes != null) {
            Map<NotificationType, Boolean> accountNotificationTypes = userAccount.getNotificationTypes();
            accountNotificationTypes.putAll(notificationTypes);
        }

        Collection<NotificationSettingDto.NotificationConfigDto> notificationConfigDtos =
                notificationSettingDto.getNotificationConfigs();
        Set<NotificationConfig> userAccountNotificationConfigs = userAccount.getNotificationConfigs();
        for (NotificationSettingDto.NotificationConfigDto configDto : notificationConfigDtos) {
            boolean found = false;
            for (NotificationConfig userAccountNotificationConfig : userAccountNotificationConfigs) {
                if (configDto.getMethod() == userAccountNotificationConfig.getMethod()) {
                    userAccountNotificationConfig.setEnabled(configDto.getEnabled());
                    userAccountNotificationConfig.setFormat(configDto.getFormat());

                    getEntityManager().merge(userAccountNotificationConfig);
                    found = true;
                    break;
                }
            }
            if (!found) {
                NotificationConfig userAccountNotificationConfig = new NotificationConfig(
                        new PrimaryKey(userAccount.getTenantId()), configDto.getEnabled(), configDto.getMethod(),
                        configDto.getFormat());
                userAccount.addNotificationConfig(userAccountNotificationConfig);
            }
        }
        update(userAccount);
        return getNotificationSettings(userAccount);
    }

    public NotificationConfig mergeNotificationConfig(NotificationConfig notificationConfig) {
        return getEntityManager().merge(notificationConfig);
    }

    public boolean addGroup(UserAccount userAccount, GroupAccount groupAccount) {
        Collection<GroupAccount> groups = userAccount.getGroupAccounts();
        if (!groups.contains(groupAccount)) {
            groups.add(groupAccount);

            update(userAccount);
        }
        return true;
    }

    public boolean removeGroup(UserAccount userAccount, GroupAccount groupAccount) {
        Collection<GroupAccount> groups = userAccount.getGroupAccounts();
        if (groups.contains(groupAccount)) {
            groups.remove(groupAccount);

            update(userAccount);
        }
        return true;
    }

    public Set<Object> getPermissionsAndRoles(UserAccount user, boolean readOnlyPermissions) {
        Set<Object> result = new HashSet<>();

        if (user != null) {
            // collect all roles this account has (ie User Roles + Roles associated to all Groups it is a member of)
            // get account roles
            Set<Role> roles = new HashSet<>();
            roles.addAll(user.getRoles());

            // then add groups roles
            Collection<GroupAccount> groups = user.getGroupAccounts();
            for (GroupAccount group : groups) {
                roles.addAll(group.getRoles());
            }

            // from Roles, get all permissions
            Set<Permission> permissions = new HashSet<>();
            for (Role role : roles) {
                Collection<Permission> perms = role.getPermissions();
            	if (readOnlyPermissions) {
            		// filter out permissions that are not readonly
            		for (Permission perm : perms) {
            			if (perm.getType() == PermissionType.View) {
            				permissions.add(perm);
            			}
            		}
            	} else {
            		permissions.addAll(perms);            		
            	}
            }

            // and return it all
            result.addAll(roles);
            result.addAll(permissions);
        }
    	return result;  	
    }
    
    /**
     * getAcls() returns all ACLs associated with this account.
     * For now, implementation is using getPermissionsAndRoles(). This can be implemented in a more optimized way 
     * 
     * NOTE: entityClass param is not used yet
     * 
     * @param userAccount
     * @return
     */
    public Set<AccountACE> getAcl(UserAccount userAccount) {
        Set<AccountACE> result = new HashSet<>();
        
        // get roles associated with this account and then accumulate the ACLs associated with each role
        Set<Object> rolesAndPermissions = getPermissionsAndRoles(userAccount, false);
        for (Object obj: rolesAndPermissions) {
        	if (obj instanceof Role) {
        		Role role = (Role) obj;
                Set<AccountACE> accountACESet = ModelUtils.buildAccountACESet(role.getAcl());
                result.addAll(accountACESet);
        	}
        }
        return result;	
    }

	public ResultSet<GroupAccount> getGroups(PrimaryKey primaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetAssociatedWithPaging(getEntityManager(), simpleQuery, primaryKey,
                UserAccount.class, "groupAccounts");
	}

    public ResultSet<GroupAccount> getUnassociatedGroups(PrimaryKey primaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetUnassociatedWithPaging(getEntityManager(), simpleQuery, primaryKey,
                UserAccount.class, "groupAccounts");
    }

    public List<Object> quickSearch(String tenantId, String searchValue, String searchFields, String returnedFields,
                                      int limit, String orderBy, String orderDir)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String searchFieldsClause = SimpleQueryHelper.createSearchFieldsClause(searchValue, searchFields, "u");

        String returnedFieldsClause = SimpleQueryHelper.createReturnedFieldsClause(returnedFields, "u");

        String sql =
                "SELECT " + (returnedFieldsClause != null ? returnedFieldsClause : "u.*, e.firstName, e.lastName ") +
                        "FROM UserAccount u " +
                        "   LEFT JOIN Employee e ON u.id = e.userAccountId " + " AND u.tenantId = e.userAccountTenantId " +
                " WHERE u.tenantId = :tenantId " +
                        (searchFieldsClause != null ? " AND (" + searchFieldsClause + ")" : "")
                        + " AND " + QueryPattern.NOT_DELETED.val("e");

        if (StringUtils.isNotBlank(orderBy)) {
            sql += " ORDER BY u." + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        return query.getResultList();
    }

    public ResultSet<Object[]> query(String tenantId, String searchValue, String searchFields, String employeeFilter,
                                     int offset, int limit, String orderBy, String orderDir)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String searchFieldsClause = SimpleQueryHelper.createSearchFieldsClause(searchValue, searchFields, "u");

        String sql =
                "SELECT u.id, u.name, u.description, u.login, u.email, u.inactivityPeriod, u.language, u.status, " +
                "       e.id employeeId, e.firstName, e.lastName, " +
                "       count(DISTINCT g.id) nbOfGroups, count(DISTINCT r.id) nbOfRoles, " +
                "       GROUP_CONCAT(DISTINCT g.name SEPARATOR ', ') groups, " +
                "       GROUP_CONCAT(DISTINCT r.name SEPARATOR ', ') roles " +
                "  FROM UserAccount u " +
                "       LEFT JOIN Employee e ON e.userAccountId = u.id AND e.userAccountTenantId = u.tenantId " +
                            QueryPattern.NOT_DELETED.val("e", " AND ", " ") +
                "       LEFT JOIN User_Group ug ON ug.user_id = u.id AND ug.user_tenantId = u.tenantId " +
                "       LEFT JOIN GroupAccount g ON g.id = ug.group_id AND g.tenantId = ug.group_tenantId " +
                "       LEFT JOIN Role_Account ra ON u.id = ra.account_id AND u.tenantId = ra.account_tenantId " +
                "       LEFT JOIN Role r ON ra.role_id = r.id AND ra.role_tenantId = r.tenantId " +
                " WHERE u.tenantId = :tenantId " +
                        (searchFieldsClause != null ? " AND (" + searchFieldsClause + ")" : "");

        if (StringUtils.isNotBlank(employeeFilter)) {
            sql += " AND (" + SimpleQueryHelper.buildFilterClause(employeeFilter, "e") + ") ";
        }

        sql += " GROUP BY u.id, u.tenantId ";

        String countSql = "SELECT COUNT(*) FROM (" + sql + ") x";

        if (StringUtils.isNotBlank(orderBy)) {
            if (orderBy.contains("nbOfGroups")) {
                orderBy = "nbOfGroups";
            } else if (orderBy.contains("nbOfRoles")) {
                orderBy = "nbOfRoles";
            } else if (orderBy.contains("roles")) {
                orderBy = "roles";
            } else if (orderBy.contains("groups")) {
                orderBy = "groups";
            } else if (orderBy.startsWith("UserAccount.")) {
                orderBy = orderBy.replaceFirst("UserAccount.", "u.");
            } else if (orderBy.startsWith("Employee.")) {
                orderBy = orderBy.replaceFirst("Employee.", "e.");
            }
            sql += " ORDER BY " + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        Query countQuery = getEntityManager().createNativeQuery(countSql);
        countQuery.setParameter("tenantId", tenantId);

        Collection<Object[]> userInfo = query.getResultList();
        BigInteger total = (BigInteger) countQuery.getSingleResult();
        return new ResultSet<>(userInfo, total.intValue());
    }

    public Collection<Object[]> rolesInfo(PrimaryKey userPrimaryKey) {
        String sql =
            "SELECT DISTINCT r.id, r.name, r.description " +
            "  FROM UserAccount u " +
            "       LEFT JOIN Role_Account ra ON u.id = ra.account_id AND u.tenantId = ra.account_tenantId " +
            "       LEFT JOIN Role r ON ra.role_id = r.id AND ra.role_tenantId = r.tenantId " +
            " WHERE u.tenantId = :tenantId AND u.id = :userId AND r.id IS NOT NULL " +
            " ORDER BY r.name ";

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", userPrimaryKey.getTenantId());
        query.setParameter("userId", userPrimaryKey.getId());

        return query.getResultList();
    }

    public Collection<Object[]> inheritedRolesInfo(PrimaryKey userPrimaryKey) {
        String sql =
            "SELECT DISTINCT r.id, r.name, r.description " +
            "  FROM UserAccount u " +
            "       LEFT JOIN User_Group ug ON ug.user_id = u.id AND ug.user_tenantId = u.tenantId " +
            "       LEFT JOIN GroupAccount g ON g.id = ug.group_id AND g.tenantId = ug.group_tenantId " +
            "       LEFT JOIN Role_Account ra ON g.id = ra.account_id AND g.tenantId = ra.account_tenantId " +
            "       LEFT JOIN Role r ON ra.role_id = r.id AND ra.role_tenantId = r.tenantId " +
            " WHERE u.tenantId = :tenantId AND u.id = :userId AND r.id IS NOT NULL " +
            " ORDER BY r.name ";

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", userPrimaryKey.getTenantId());
        query.setParameter("userId", userPrimaryKey.getId());

        return query.getResultList();
    }

    public Collection<Object[]> groupsInfo(PrimaryKey userPrimaryKey) {
        String sql =
            "SELECT DISTINCT g.id, g.name, g.description " +
            "  FROM UserAccount u " +
            "       LEFT JOIN User_Group ug ON ug.user_id = u.id AND ug.user_tenantId = u.tenantId " +
            "       LEFT JOIN GroupAccount g ON g.id = ug.group_id AND g.tenantId = ug.group_tenantId " +
            " WHERE u.tenantId = :tenantId AND u.id = :userId AND g.id IS NOT NULL " +
            " ORDER BY g.name ";

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", userPrimaryKey.getTenantId());
        query.setParameter("userId", userPrimaryKey.getId());

        return query.getResultList();
    }

    public String resetPassword(UserAccount userAccount) {
        sessionService.validateResetPassword(userAccount);

        PasswordPolicies passwordPolicies = tenantService.getPasswordPolicies(userAccount.getTenantId());
        String result = PasswordUtils.generatePassword(passwordPolicies);

        changePassword(userAccount, result, null);
        
        DateTime resetDateTime = new DateTime();
        DateTime resetByDateTime = new DateTime().plusHours(24);

        userAccount.setInactivityPeriod(24 * 60L); // 24 hours
        userAccount.setStatus(AccountStatus.PendingPwdChange);

        NotificationResult notificationResult = sendGeneralNotification(userAccount, MsgDeliveryType.EMAIL,
                result, resetDateTime, resetByDateTime);

        return notificationResult.getMessage();
    }

    public void changePassword(UserAccount account, String newPassword, URI uri) {
        long currentTime = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        calendar.add(Calendar.MONTH, 3);

        account.setPassword(newPassword, false); // note that after password is set, it is now its SHA 256 hash value.
        String newHashedPassword = account.getPassword();
        account.setPwdChanged(currentTime);
        account.setAccountValidityDate(calendar.getTime().getTime());

        String accountHistory = account.getPasswordHistory();
        PasswordPolicies passwordPolicies = tenantService.getPasswordPolicies(account.getTenantId());

        int disallowNumber = passwordPolicies.getDisallowOldPasswordNb();

        if (StringUtils.isBlank(accountHistory) || disallowNumber == 0) {
            accountHistory = currentTime + "," + newHashedPassword.hashCode();
        } else {
            String[] passwordInfoList = accountHistory.split(";");
            List<String> checksumList = new ArrayList<>();
            for (String info : passwordInfoList) {
                String[] dateChecksum = info.split(",");
                String checksum = dateChecksum[1];
                checksumList.add(checksum);
            }

            validateChecksum(checksumList, newHashedPassword);

            if (checksumList.size() >= disallowNumber) {
                accountHistory = accountHistory.substring(accountHistory.indexOf(";") + 1);
            } else {
                accountHistory += ";" + currentTime + "," + newHashedPassword.hashCode();
            }
        }
        account.setPasswordHistory(accountHistory);
/* We should sent only a notification that has password has changed, but we don't want to switch into confirmation status
        if (uri != null) {
            account.setStatus(AccountStatus.PendingConfirmation);
            account.setConfirmationId(UniqueId.getId());

            sendConfirmationNotification(account, uri, account.getConfirmationId());
        }
*/
        update(account);
    }

    public void changePasswordConfirmation(String confirmationId) {
        String sql = "SELECT * FROM UserAccount WHERE confirmationId = :confirmationId";
        Query query = getEntityManager().createNativeQuery(sql, UserAccount.class);
        query.setParameter("confirmationId", confirmationId);
        UserAccount account;
        try {
            account = (UserAccount) query.getSingleResult();
        } catch (Exception e) {
            throw new ValidationException(resourcesBundle.getMessage("en", "validate.account.confirmation.error"));
        }

        if (!AccountStatus.PendingConfirmation.equals(account.getStatus())) {
            throw new ValidationException(resourcesBundle.getMessage(account.getLanguage(),
                    "validate.account.status.error", account.getStatus()));
        }

        account.setStatus(AccountStatus.Active);
        account.setConfirmationId(null);

        update(account);
    }

    private void validateChecksum(List<String> checksums, String newPassword) {
        String encodedNewPassword = passwordCoder.decode(newPassword);
        int newChecksum = encodedNewPassword.hashCode();
        for (String checksum : checksums) {
            if (Integer.valueOf(checksum) == newChecksum) {
                throw new ValidationException("Password's history already contains password with same checksum");
            }
        }
    }

    private NotificationResult sendGeneralNotification(UserAccount userAccount, MsgDeliveryType deliveryType,
			String password, DateTime resetDateTime, DateTime resetByDateTime) {
		NotificationMessageDTO notificationMessageDTO = new NotificationMessageDTO();
        notificationMessageDTO.setNotificationOperation(NotificationOperation.RESET);
        notificationMessageDTO.setNotificationCategory(NotificationCategory.PASSWORD);
        
        Map<String, String> messageAttributes = new HashMap<>();
        
        String resetDate = accountUtilService.getTimeZoneAdjustedDateString(resetDateTime, userAccount,
                Constants.NOTIF_DATETIME_FORMATTER);
        String resetByDate = accountUtilService.getTimeZoneAdjustedDateString(resetByDateTime, userAccount,
                Constants.NOTIF_DATETIME_FORMATTER);
        String resetPassword = password;

        messageAttributes.put("resetDate", resetDate);
        messageAttributes.put("resetByDate", resetByDate);
        messageAttributes.put("resetPassword", resetPassword);
        
        notificationMessageDTO.setMessageAttributes(messageAttributes);
        notificationMessageDTO.setTenantId(userAccount.getTenantId());
        notificationMessageDTO.setReceiverUserId(userAccount.getId());
		
        return notificationService.sendGeneralNotification(notificationMessageDTO, deliveryType);
	}

    private NotificationResult sendConfirmationNotification(UserAccount userAccount, URI uri, String confirmationId) {
		NotificationMessageDTO notificationMessageDTO = new NotificationMessageDTO();
        notificationMessageDTO.setNotificationOperation(NotificationOperation.CONFIRMATION);
        notificationMessageDTO.setNotificationCategory(NotificationCategory.PASSWORD);

        Map<String, String> messageAttributes = new HashMap<>();

        String confirmationUrl = uri.toString() + "sessions/ops/chgpassword/confirmation/" + confirmationId;

        messageAttributes.put("confirmationUrl", confirmationUrl);

        notificationMessageDTO.setMessageAttributes(messageAttributes);
        notificationMessageDTO.setTenantId(userAccount.getTenantId());
        notificationMessageDTO.setReceiverUserId(userAccount.getId());

        return notificationService.sendGeneralNotification(notificationMessageDTO, MsgDeliveryType.EMAIL);
	}

    public void persistAccountPicture(AccountPicture accountPicture) {
    	getEntityManager().persist(accountPicture);
    }

    public AccountPicture mergeAccountPicture(AccountPicture accountPicture) {
        return getEntityManager().merge(accountPicture);
    }

    public void persistRememberMe(RememberMe rememberMe) {
    	getEntityManager().persist(rememberMe);
    }

    public RememberMe mergeRememberMe(RememberMe rememberMe) {
        return getEntityManager().merge(rememberMe);
    }

    @Schedule(second = "00", minute = "00", hour = "00", persistent = false)
    public void cleanupPostedOpenShifts() {
        String deleteSql = "DELETE FROM RememberMe WHERE expirationDate < NOW()";
        Query query = getEntityManager().createNativeQuery(deleteSql);
        query.executeUpdate();
    }

    public void deleteRememberMeByToken(Serializable tokenId) {
        if (tokenId != null) {
            String sql = "DELETE FROM RememberMe WHERE tokenId = :tokenId";

            Query query = getEntityManager().createNativeQuery(sql);
            query.setParameter("tokenId", tokenId);

            query.executeUpdate();
        }
    }

}
