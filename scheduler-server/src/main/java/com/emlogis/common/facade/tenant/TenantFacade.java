package com.emlogis.common.facade.tenant;

import com.emlogis.common.PasswordUtils;
import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.validation.annotations.Validate;
import com.emlogis.common.validation.annotations.Validation;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.tenant.PasswordPolicies;
import com.emlogis.model.tenant.Tenant;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.tenant.dto.*;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.security.SessionService;
import com.emlogis.server.services.PasswordCoder;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;

import javax.ejb.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TenantFacade extends BaseFacade {

    @EJB
    private UserAccountService userAccountService;

    @EJB
    private TenantService tenantService;

    @EJB
    private PasswordCoder passwordCoder;

    @EJB
    private SessionService sessionService;
    

    /**
   	 * by default, return generic tenantService
   	 * in subclasses, should return either OrganizationService or ServiceProviderService  
     * @return
     */
    protected TenantService getActualTenantService() {
    	return getTenantService() ;
    }

    public UserAccountService getUserAccountService() {
		return userAccountService;
	}


	public TenantService getTenantService() {
		return tenantService;
	}

	// TODO: add validation on certain fields like inactivityPeriod (must have certain values)
    protected Tenant updateTenant(String tenantId, TenantUpdateDto updateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        boolean modified = false;
        Tenant tenant = getTenantService().getTenant(tenantId);
        if (updateDto == null) {
        	return tenant;
        }

        if (StringUtils.isNotBlank(updateDto.getName())) {
            // TODO, here we need to make sure name is unique. validation annotation cannot be used ?
            tenant.setName(updateDto.getName());
            modified = true;
        }
        if (StringUtils.isNotBlank(updateDto.getDescription())) {
            tenant.setDescription(updateDto.getDescription());
            modified = true;
        }
        if (StringUtils.isNotBlank(updateDto.getTimeZone())) {
            tenant.setTimeZone(DateTimeZone.forID(updateDto.getTimeZone()));
            modified = true;
        }       
        if (StringUtils.isNotBlank(updateDto.getLanguage())) {
            tenant.setLanguage(updateDto.getLanguage());
            modified = true;
        }
        if (StringUtils.isNotBlank(updateDto.getGeo())) {
            tenant.setGeo(updateDto.getGeo());
            modified = true;
        }
        if (StringUtils.isNotBlank(updateDto.getAddress())) {
            tenant.setAddress(updateDto.getAddress());
            modified = true;
        }
        if (StringUtils.isNotBlank(updateDto.getAddress2())) {
            tenant.setAddress2(updateDto.getAddress2());
            modified = true;
        }
        if (StringUtils.isNotBlank(updateDto.getCity())) {
            tenant.setCity(updateDto.getCity());
            modified = true;
        }
        if (StringUtils.isNotBlank(updateDto.getState())) {
            tenant.setState(updateDto.getState());
            modified = true;
        }
        if (StringUtils.isNotBlank(updateDto.getCountry())) {
            tenant.setCountry(updateDto.getCountry());
            modified = true;
        }
        if (StringUtils.isNotBlank(updateDto.getZip())) {
            tenant.setZip(updateDto.getZip());
            modified = true;
        }
        if (updateDto.getInactivityPeriod() > 0) {		// consider -10 as special value = unset by client
        	tenant.setInactivityPeriod(updateDto.getInactivityPeriod());
            modified = true;
        }
                
        // TODO harden licence management information , 
        if (this instanceof ServiceProviderFacade) {
        	//  make license management & delivery provider management available only to Service provider Tenant (customer should not be able to chnage it)
	        if (updateDto.getProductLicenseInfo() != null) {
	    		tenant.setProductLicenseInfo(updateDto.getProductLicenseInfo());
	            modified = true;       	
	        }
	        List<ModuleLicenseDto> moduleLicenses = updateDto.getModulesLicenseInfo();
	    	if (moduleLicenses != null) {
	    		tenant.setModulesLicenseInfo(moduleLicenses);
	            modified = true;
	    	}    
	    	
	    	modified |= getTenantService().updateDeliverySettings(tenant.getSmsDeliveryTenantSettings(),
                    updateDto.getSmsDeliveryTenantSettingsDto());
	    	modified |= getTenantService().updateDeliverySettings(tenant.getEmailDeliveryTenantSettings(),
                    updateDto.getEmailDeliveryTenantSettingsDto());
        }
        
        if (modified) {
            tenant.setUpdated(System.currentTimeMillis());
        }
        return tenant;
    }



	
	@Validation
    public PasswordPoliciesDto getPasswordPolicies(
            @Validate(validator = EntityExistValidatorBean.class, type = Tenant.class) String tenantId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        Tenant tenant = getActualTenantService().getTenant(tenantId);
        return toDto(tenant.getPasswordPolicies(), PasswordPoliciesDto.class);
    }

    @Validation
    public PasswordComplianceReportDto setPasswordPolicies(
            @Validate(validator = EntityExistValidatorBean.class, type = Tenant.class)
            String tenantId,
            PasswordPoliciesUpdateDto updateDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Tenant tenant = getActualTenantService().getTenant(tenantId);
        PasswordPolicies policies = tenant.getPasswordPolicies();

        policies.setForceChangeOnFirstLogon(updateDto.isForceChangeOnFirstLogon());
        policies.setMinPasswordLength(updateDto.getMinPasswordLength());
        policies.setMaxPasswordLength(updateDto.getMaxPasswordLength());
        policies.setDisallowOldPasswordNb(updateDto.getDisallowOldPasswordNb());

        policies.setMaxUnsucessfullLogin(updateDto.getMaxUnsucessfullLogin());
        policies.setUnsucessfullLoginAction(updateDto.getUnsucessfullLoginAction());
        policies.setSuspendAccountTime(updateDto.getSuspendAccountTime());

        policies.setRequireAtLeastOneLowercaseChar(updateDto.isRequireAtLeastOneLowercaseChar());
        policies.setRequireAtLeastOneUppercaseChar(updateDto.isRequireAtLeastOneUppercaseChar());
        policies.setRequireAtLeastOneNonalphaChar(updateDto.isRequireAtLeastOneNonalphaChar());
        policies.setRequireAtLeastOneNumberChar(updateDto.isRequireAtLeastOneNumberChar());

        setUpdatedBy(policies);
        getActualTenantService().updatePasswordPolicies(tenant);

        return getPasswordComplianceViolations(tenantId, null);
    }

	public PasswordComplianceReportDto getPasswordComplianceViolations(
            @Validate(validator = EntityExistValidatorBean.class, type = Tenant.class)
            String tenantId,
            PasswordPoliciesDto dto) throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        Tenant tenant = getActualTenantService().getTenant(tenantId);
        if (dto == null) {
            // no update dto provided, use the one associated to the organization
            PasswordPolicies policies = tenant.getPasswordPolicies();
            dto = toDto(policies, PasswordPoliciesDto.class);
        }
        // pull all user accounts
        SimpleQuery simpleQuery = new SimpleQuery(tenantId);
        simpleQuery.setSelect(null).setFilter(null).setOffset(0).setLimit(9999).setOrderByField("login")
                .setOrderAscending(false).setTotalCount(false);
        ResultSet<UserAccount> rs = userAccountService.findUserAccounts(simpleQuery);
        Collection<UserAccount> accounts = rs.getResult();
        Collection<PasswordComplianceDto> violatingAccounts = new ArrayList<>();
        for (UserAccount account : accounts) {
            checkPasswordStrengthCompliance(dto, account, violatingAccounts);
        }

        int accountTotal = accounts.size();
        return new PasswordComplianceReportDto(accountTotal, accountTotal - violatingAccounts.size(),
                violatingAccounts.size(), violatingAccounts);
    }

    public void setPasswordComplianceUserState(int chunkSize) {
        ResultSet<Tenant> tenantResultSet = tenantService.findTenants(Tenant.class, new SimpleQuery());
        for (Tenant tenant : tenantResultSet.getResult()) {
            setPasswordComplianceUserStateForTenant(tenant, chunkSize);
        }
    }

    public void setPasswordComplianceUserStateForTenant(Tenant tenant, int chunkSize) {
        String tenantId = tenant.getTenantId();
        PasswordPolicies passwordPolicies = tenant.getPasswordPolicies();

        List<UserAccount> userAccounts;
        do {
            long selectedTime = System.currentTimeMillis() - 24L * 60 * 60 * 1000;
            userAccounts = userAccountService.quickFindUserAccounts(tenantId, selectedTime, 0, chunkSize);
            tenantService.setPasswordComplianceForUsers(userAccounts, passwordPolicies);
        } while (userAccounts.size() > 0);
    }

    /**
     *
     * checkPasswordStrengthCompliance checks password compliance with strength rules and add it to
     * violatingAccounts collection if not passing rules
     *
     * @param policiesDto
     * @param account
     * @param violatingAccounts
     */
    private void checkPasswordStrengthCompliance(PasswordPoliciesDto policiesDto, UserAccount account,
                                                 Collection<PasswordComplianceDto> violatingAccounts) {
        String password = passwordCoder.decode(account.getPassword());
        if (password == null) {
            password = "";		// make sure password is not null
        }

        String violations = PasswordUtils.getPasswordViolations(policiesDto, password);

        if (StringUtils.isNotEmpty(violations)) {
            PasswordComplianceDto complianceDto = new PasswordComplianceDto();
            complianceDto.setId(account.getId());
            complianceDto.setLogin(account.getLogin());
            complianceDto.setEmail(account.getEmail());
//          complianceDto.setFirstLogin(account.isFirstLogin());
            complianceDto.setAccountStatus(account.getStatus());

            violations = violations.substring(0, violations.length() - 1);  // remove trailing ','
            complianceDto.setViolations(violations);
            violatingAccounts.add(complianceDto);
        }
    }

}
