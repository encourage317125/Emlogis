package com.emlogis.common.services.tenant;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.exceptions.credentials.EmLogisCredentialsException;
import com.emlogis.common.security.Permissions;
import com.emlogis.common.services.BaseService;
import com.emlogis.common.services.notification.MsgDeliveryProviderSettingsService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.notification.MsgDeliveryProviderSettings;
import com.emlogis.model.notification.MsgDeliveryTenantSettings;
import com.emlogis.model.notification.MsgDeliveryType;
import com.emlogis.model.notification.ProviderAttributeMetadata;
import com.emlogis.model.notification.dto.MsgDeliveryTenantSettingsUpdateDto;
import com.emlogis.model.tenant.PasswordPolicies;
import com.emlogis.model.tenant.Role;
import com.emlogis.model.tenant.Tenant;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.rest.security.SessionService;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Service in charge of  Account / Role and Permission Administration, & operations common to all Tenant types
 * @author EmLogis
 *
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class TenantService<T extends Tenant> extends BaseService {

    @EJB
    protected GroupAccountService groupAccountService;

    @EJB
    protected UserAccountService userAccountService;

    @EJB
    protected SessionService sessionService;

    @EJB
    protected RoleService roleService;
    
    @EJB
    protected ACEService aclService;
    
    @EJB
    protected MsgDeliveryProviderSettingsService msgDeliveryProviderService;
    
    @PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;
    
    protected EntityManager getEntityManager() {
        return entityManager;
    }
    
    // Tenant related methods
	//-----------------------
    
	/**
	 * findTenants() find a list of Tenants matching criteria;
	 * @param simpleQuery
	 * @return ResultSet<Tenant>
	 */
	public ResultSet<T> findTenants(Class<T> entityClass, SimpleQuery simpleQuery) {
		simpleQuery.setEntityClass(entityClass);
		SimpleQueryHelper simpleQueryHelper = new SimpleQueryHelper();
		return simpleQueryHelper.executeSimpleQueryWithPaging(entityManager, simpleQuery);
	}
    
	/**
	 * getTenantById() return organization data specified by tenantId (= tenant id)
	 * @param tenantId
	 * @return Tenant
	 */
	public T getTenant(Class<T> entityClass, String tenantId) {
		return entityManager.find(entityClass, tenantId);
	} 
    
	/**
	 * getTenantById() return tenant data specified by tenantId (= tenant id)
	 * @param tenantId
	 * @return Tenant
	 */
	public Tenant getTenant(String tenantId) {
		return entityManager.find(Tenant.class, tenantId);
	}    

	public T updateTenant(T tenant) {
        return entityManager.merge(tenant);
	}
	   
	public PasswordPolicies updatePasswordPolicies(T tenant) {
        return entityManager.merge(tenant.getPasswordPolicies());
	}

	public PasswordPolicies getPasswordPolicies(String tenantId) {
        String hql = "SELECT pp FROM PasswordPolicies pp WHERE pp.primaryKey.tenantId = :tenantId ";
        Query query = entityManager.createQuery(hql);
        query.setParameter("tenantId", tenantId);
        query.setMaxResults(1);
        List resultList = query.getResultList();
        return resultList.size() == 0 ? null : (PasswordPolicies) resultList.get(0);
	}

	public Map<String,Role> initTenant(Tenant tenant) {
		MsgDeliveryTenantSettings smsSettings = new MsgDeliveryTenantSettings(new PrimaryKey(tenant.getTenantId()),
                MsgDeliveryType.SMS);
		MsgDeliveryProviderSettings twilio = msgDeliveryProviderService.getMsgDeliveryProvider(
                MsgDeliveryProviderSettings.TWILIO_PROVIDER_ID);
		smsSettings.setDeliveryProviderSettings(twilio);
		Map<String,String> smsTenantSettings = new HashMap<>();
		smsTenantSettings.put(MsgDeliveryProviderSettings.TENANT_TWILIO_TENANTCALLNUMBER, "(xxx) xxx xxxx");
		smsSettings.setSettings(smsTenantSettings);
		entityManager.persist(smsSettings);
		tenant.setSmsDeliveryTenantSettings(smsSettings);
		
		MsgDeliveryTenantSettings emailSettings = new MsgDeliveryTenantSettings(new PrimaryKey(tenant.getTenantId()),
                MsgDeliveryType.EMAIL);
		MsgDeliveryProviderSettings email = msgDeliveryProviderService.getMsgDeliveryProvider(
                MsgDeliveryProviderSettings.PRIMARYEMAIL_PROVIDER_ID);
		emailSettings.setDeliveryProviderSettings(email);
		Map<String,String> emailTenantSettings = new HashMap<>();
		emailTenantSettings.put(MsgDeliveryProviderSettings.TENANT_MAILBOX, tenant.getTenantId());
		emailSettings.setSettings(emailTenantSettings);
		entityManager.persist(emailSettings);
		tenant.setEmailDeliveryTenantSettings(emailSettings);
		
		Map<String,Role> commonRoles = new HashMap<String,Role>();
    	// create default Password Policies.
    	PasswordPolicies passwordPolicies = new PasswordPolicies(new PrimaryKey(tenant.getTenantId()));
    	tenant.setPasswordPolicies(passwordPolicies);
        entityManager.persist(passwordPolicies);
        
    	// create default accounts, groups and roles
		String createdBy = UserAccount.DEFAULT_ADMIN_ID;
		String tenantId = tenant.getTenantId();

		// roles common to all types of tenants/organizations
		// role common to all types of tenants/organizations
        PrimaryKey pk = new PrimaryKey(tenantId, Role.DEFAULT_ADMINROLE_ID);
		Role adminRole = roleService.createRole(pk);
		adminRole.setName("System Admin");
		adminRole.setLabel("System Admin");
		adminRole.setDescription("System configuration Role");
		adminRole.setCreatedBy(createdBy);
		roleService.update(adminRole);
		roleService.addPermissions(adminRole, 
			Permissions.SystemConfiguration_Mgmt,
			Permissions.AccountProfile_Update
		);
		commonRoles.put(Role.DEFAULT_ADMINROLE_ID, adminRole);
		
        pk = new PrimaryKey(tenantId, Role.DEFAULT_ACCOUNTMANAGERROLE_ID);
		Role accountMngrRole = roleService.createRole(pk);
		accountMngrRole.setName("Account Manager");
		accountMngrRole.setLabel("Account Manager");
		accountMngrRole.setDescription("Role allowing to manage Groups, User Accounts and Role associations");
		accountMngrRole.setCreatedBy(createdBy);
		roleService.update(accountMngrRole);
		roleService.addPermissions(accountMngrRole, 
			Permissions.Account_View, Permissions.Account_Mgmt,
			Permissions.Role_View
		);
		commonRoles.put(Role.DEFAULT_ACCOUNTMANAGERROLE_ID, accountMngrRole);
/*	Moved to Service provider for now
        pk = new PrimaryKey(tenantId, Role.DEFAULT_ROLEMANAGERROLE_ID);
		Role roleMngrRole = roleService.createRole(pk);
		roleMngrRole.setName("Role Manager");
		roleMngrRole.setLabel("Role Manager");
		roleMngrRole.setDescription("Role allowing to create, edit, delete Roles and Access Control lists");
		roleMngrRole.setCreatedBy(createdBy);
		roleService.update(roleMngrRole);
		roleService.addPermissions(roleMngrRole, 
			Permissions.Role_View, Permissions.Role_Mgmt
		);
		commonRoles.put(Role.DEFAULT_ROLEMANAGERROLE_ID, roleMngrRole);
*/
		return commonRoles;
	}

	public void setPasswordComplianceForUsers(List<UserAccount> userAccounts, PasswordPolicies passwordPolicies) {
		for (UserAccount userAccount : userAccounts) {
			try {
				sessionService.updateUserStatusDueToPasswordPolicies(userAccount, passwordPolicies);
			} catch (EmLogisCredentialsException e) {
				// ignore this exception for now
			} finally {
				sessionService.checkMaxUnsuccessfulLogins(userAccount, passwordPolicies);

				userAccount.setLastStateCheckedDate(new DateTime(System.currentTimeMillis()));
				userAccountService.update(userAccount);
			}
		}
		entityManager.flush();
	}
	
	public boolean updateDeliverySettings(
			MsgDeliveryTenantSettings currentTenantDeliverySettings,
			MsgDeliveryTenantSettingsUpdateDto newTenantDeliverySettingsDto) {
    	if (newTenantDeliverySettingsDto == null) {
    		return false;
    	}

        boolean modified = false;

    	String currentProviderId = currentTenantDeliverySettings.getDeliveryProviderSettings().getId();
		String newProviderId = newTenantDeliverySettingsDto.getProviderId();
    	MsgDeliveryProviderSettings currentProviderDeliverySettings =
                currentTenantDeliverySettings.getDeliveryProviderSettings();
    	if (!StringUtils.equals(currentProviderId, newProviderId)) {
    		// delivery provider change: remove previous association and create a new one.
    		MsgDeliveryProviderSettings newProviderDeliverySettings =
                    msgDeliveryProviderService.getMsgDeliveryProvider(newProviderId);
    		if (newProviderDeliverySettings == null) {
    			throw new ValidationException("Cannot switch " + currentProviderDeliverySettings.getDeliveryType() +
                        " Tenant delivery settings, as unable to find new provider with Id: " + newProviderId);
    		}
    		// check new provider is of appropriate type
    		if (currentProviderDeliverySettings.getDeliveryType() != newProviderDeliverySettings.getDeliveryType()) {
    			throw new ValidationException("Cannot switch " + currentProviderDeliverySettings.getDeliveryType() +
                        " Tenant delivery settings, incompatible type specified for new provider");
    		}
    		// and switch current provider with new one  
    		modified = true;
    		currentTenantDeliverySettings.setDeliveryProviderSettings(newProviderDeliverySettings);
    		// last, set tenant specific settings
    		if (newTenantDeliverySettingsDto.getSettings() != null) {
				Map<String,ProviderAttributeMetadata> metadata =
                        newProviderDeliverySettings.getTenantProviderAttributeMetadata();
				Map<String,String> currentSettings = currentTenantDeliverySettings.getSettings();
				Map<String,String> updateSettings = newTenantDeliverySettingsDto.getSettings();
				for (ProviderAttributeMetadata  settingMetadata : metadata.values()) {
					String key = settingMetadata.getName();
					System.out.println("Setting setting: " + key + " with: " + updateSettings.get(key));
					if (updateSettings.get(key) != null && !StringUtils.equals(currentSettings.get(key),
                            updateSettings.get(key))) {
						currentSettings.put(key, updateSettings.get(key));
					}
				}
				currentTenantDeliverySettings.setSettings(currentSettings);	
    		}
    	} else {
    		// update  current settings that need to be updated,
    		if (newTenantDeliverySettingsDto.getSettings() != null) {
				Map<String,ProviderAttributeMetadata> metadata =
                        currentTenantDeliverySettings.getTenantProviderAttributeMetadata();
				Map<String,String> currentSettings = currentTenantDeliverySettings.getSettings();
				Map<String,String> updateSettings = newTenantDeliverySettingsDto.getSettings();
				for (ProviderAttributeMetadata  settingMetadata : metadata.values()) {
					String key = settingMetadata.getName();
					System.out.println("Checking setting: " + key + " with: " + updateSettings.get(key));
					if (updateSettings.get(key) != null && !StringUtils.equals(currentSettings.get(key),
                            updateSettings.get(key))) {
						currentSettings.put(key, updateSettings.get(key));
						modified = true;
					}
				}
				if (modified) {
					currentTenantDeliverySettings.setSettings(currentSettings);	
				}
    		}
    	}
		return modified;
	}

}
