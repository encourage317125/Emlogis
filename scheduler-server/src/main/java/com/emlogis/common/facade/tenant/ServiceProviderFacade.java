package com.emlogis.common.facade.tenant;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.employee.EmployeeFacade;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.hazelcast.HazelcastClientService;
import com.emlogis.common.services.tenant.OrganizationService;
import com.emlogis.common.services.tenant.ServiceProviderService;
import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.validation.annotations.*;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.dto.NotificationSettingDto;
import com.emlogis.model.tenant.Organization;
import com.emlogis.model.tenant.ServiceProvider;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.tenant.dto.*;
import com.emlogis.rest.resources.util.DtoMapper;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;

import org.apache.commons.lang3.StringUtils;

import javax.ejb.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ServiceProviderFacade extends TenantFacade {

    @EJB
    private ServiceProviderService serviceProviderService;
    
    @EJB
    private OrganizationService organizationService;
    
    @EJB
    private EmployeeFacade employeeFacade;
    
    @EJB
    private EmployeeService employeeService;

    @EJB
    private UserAccountService userAccountService;

    @EJB
    private HazelcastClientService hazelcastClientService;

    @Override
	protected TenantService getActualTenantService() {
		return serviceProviderService;
	}

    // -----------------------------------------------------------------------
    // Service provider APIs (APIs that act upon ServiceProvider entities)
    // -----------------------------------------------------------------------

    @Validation
    public ResultSetDto<ServiceProviderDto> getObjects (
                String select,		// NOT IMPLEMENTED FOR NOW ..
                String filter,
                int offset,
                int limit,
                @ValidatePaging(name = Constants.ORDER_BY) String orderByField,
                @ValidatePaging(name = Constants.ORDER_DIR) String orderByDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SimpleQuery simpleQuery = new SimpleQuery()
	        .setSelect(select)
	        .setFilter(filter)
	        .setOffset(offset)
	        .setLimit(limit)
	        .setOrderByField(orderByField)
	        .setOrderAscending(StringUtils.equals(orderByDir, "ASC")).setTotalCount(true);
        ResultSet<ServiceProvider> rs = serviceProviderService.findServiceProviders(simpleQuery);
        return toResultSetDto(rs, ServiceProviderDto.class);
    }

    @Validation
    public ServiceProviderDto createServiceProvider(
                @Validate(validator = EntityExistValidatorBean.class, type = ServiceProvider.class, expectedResult = false)
                ServiceProviderCreateDto createDto) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException, IllegalArgumentException, IOException {
    	throw new RuntimeException("Creating a ServiceProvider entity is an unsupported operation.");
    	// as of today, we support only one SP, which is created on Server startup. 
    	// no operation is possible without this primary SP, thus no need for a create API.
    	// (code would be similar to this.createOrganization() )
    }
    
    @Validation
    public ServiceProviderDto getServiceProvider(
                @Validate(validator = EntityExistValidatorBean.class, type = ServiceProvider.class) String spId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ServiceProvider sp = serviceProviderService.getServiceProvider(spId);
        return toDto(sp, ServiceProviderDto.class);
    }

    @Validation
    public ServiceProviderDto updateServiceProvider(
                @Validate(validator = EntityExistValidatorBean.class, type = ServiceProvider.class) String spId,
                ServiceProviderUpdateDto updateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ServiceProvider sp = (ServiceProvider)updateTenant(spId, updateDto);
    	// so far no ServiceProvider specific attribute to set.
    	getTenantService().updateTenant(sp);
        return toDto(sp, ServiceProviderDto.class);
    }

    @Validation
    public boolean deleteServiceProvider(
                @Validate(validator = EntityExistValidatorBean.class, type = ServiceProvider.class) final String spId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            IllegalArgumentException, IOException {
    	throw new RuntimeException("Deleting a ServiceProvider entity is an unsupported operation.");
    }

    // -----------------------------------------------------------------------
    // Organization Management APIs 
    // (ServiceProvider APIs that act upon Customers entities)
    // -----------------------------------------------------------------------

    @Validation
    public ResultSetDto<OrganizationDto> getOrganizations (
                String select,		// NOT IMPLEMENTED FOR NOW ..
                String filter,
                int offset,
                int limit,
                @ValidatePaging(name = Constants.ORDER_BY) String orderByField,
                @ValidatePaging(name = Constants.ORDER_DIR) String orderByDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SimpleQuery simpleQuery = new SimpleQuery()
	        .setSelect(select)
	        .setFilter(filter)
	        .setOffset(offset)
	        .setLimit(limit)
	        .setOrderByField(orderByField)
	        .setOrderAscending(StringUtils.equals(orderByDir, "ASC")).setTotalCount(true);
        ResultSet<Organization> rs = organizationService.findOrganizations(simpleQuery);
        return toResultSetDto(rs, OrganizationDto.class);
    }
    
    @Validation
    public OrganizationDto createOrganization(
            	@Validate(validator = EntityExistValidatorBean.class, type = ServiceProvider.class)
                String spId,

                @ValidateAll(
                    strLengths = {
                        @ValidateStrLength(field = TenantCreateDto.TENANT_ID, min = 2, max = 32, passNull = false),
                        @ValidateStrLength(field = TenantCreateDto.NAME, min = 1, max = 128, passNull = false),
                    },
                    uniques = {
                        @ValidateUnique(fields = TenantCreateDto.TENANT_ID, type = Organization.class),
                        @ValidateUnique(fields = TenantCreateDto.NAME, type = Organization.class)
                    },
                    regexes = {
                        @ValidateRegex(field = TenantCreateDto.ZIP, regex = "\\d+")
                    }
                )
                OrganizationCreateDto createDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, IllegalArgumentException, IOException {
    	String tenantId = createDto.getTenantId();

        // further validate tenantId
        tenantId = tenantId.toLowerCase(); 
        //make sure it contains only valid characters and that 
        boolean isValid = StringUtils.containsOnly(tenantId, "abcdefghijklmnopqrstuvwxyz0123456789-");
        char first = tenantId.charAt(0);
        isValid &= (first >= 'a' && first <= 'z');   // starts by a char (no digit or -) 
        isValid &= (!tenantId.endsWith("-"));			 // doesn't end with -
        if (!isValid) {
            throw new ValidationException("Invalid tenant identifier, id must comply with allowed formats");
        }

    	Organization organization = serviceProviderService.createOrganization(tenantId, null, null);
        organization.setName(createDto.getName());

    	OrganizationUpdateDto updateDto = (OrganizationUpdateDto) createDto.getUpdateDto();
        if (updateDto != null) {
        	return updateOrganization(tenantId, updateDto);
        } else {
        	return toDto(organization, OrganizationDto.class);
        }
    }

    @Validation
    public OrganizationQueryDto getOrganization(
            @Validate(validator = EntityExistValidatorBean.class, type = Organization.class) String orgId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ResultSet<Organization> orgResultSet = serviceProviderService.query(orgId, Constants.TENANT_ID, 0, 1, null,
                null);

        DtoMapper<Organization, OrganizationQueryDto> dtoMapper = new DtoMapper<>();
        dtoMapper.registerExceptDtoFieldForMapping("nbOfSites");
        dtoMapper.registerExceptDtoFieldForMapping("nbOfTeams");
        dtoMapper.registerExceptDtoFieldForMapping("nbOfEmployees");
        ResultSetDto<OrganizationQueryDto> resultSet = dtoMapper.mapResultSet(orgResultSet, OrganizationQueryDto.class);

        OrganizationQueryDto result = null;
        if (resultSet.getResult() != null && resultSet.getResult().size() > 0) {
            result = resultSet.getResult().iterator().next();

            result.setNbOfSites(serviceProviderService.getSiteCount(orgId));
            result.setNbOfTeams(serviceProviderService.getTeamCount(orgId));
            result.setNbOfEmployees(serviceProviderService.getEmployeeCount(orgId));
        }
        return result;
    }
    
    @Validation
    public OrganizationDto updateOrganization(
            @Validate(validator = EntityExistValidatorBean.class, type = Organization.class) String orgId,
            OrganizationUpdateDto updateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	
    	Organization org = (Organization) updateTenant(orgId, updateDto);
    	// so far no organization specific attribute to set.
    	getTenantService().updateTenant(org);
        return toDto(org, OrganizationDto.class);
    }

    @Validation
    public boolean deleteOrganization(
                @Validate(validator = EntityExistValidatorBean.class, type = Organization.class)
                String id) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException, IllegalArgumentException, IOException {
    	
        serviceProviderService.deleteTenant(id);
        return true;
    }

    @Validation
    public Collection<Object> quickSearch(String searchValue,
                                            String searchFields,
                                            String returnedFields,
                                            int limit,
                                            @ValidatePaging(name = Constants.ORDER_BY)
                                            String orderBy,
                                            @ValidatePaging(name = Constants.ORDER_DIR)
                                            String orderDir) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        return serviceProviderService.quickSearch(searchValue, searchFields, returnedFields, limit, orderBy, orderDir);
    }

    @Validation
    public ResultSetDto<OrganizationQueryDto> query(String searchValue,
                                                    String searchFields,
                                                    int offset,
                                                    int limit,
                                                    @ValidatePaging(name = Constants.ORDER_BY)
                                                    String orderBy,
                                                    @ValidatePaging(name = Constants.ORDER_DIR)
                                                    String orderDir) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        ResultSet<Organization> orgResultSet = serviceProviderService.query(searchValue, searchFields, offset, limit,
                orderBy, orderDir);

        DtoMapper<Organization, OrganizationQueryDto> dtoMapper = new DtoMapper<>();
        dtoMapper.registerExceptDtoFieldForMapping("nbOfSites");
        dtoMapper.registerExceptDtoFieldForMapping("nbOfTeams");
        dtoMapper.registerExceptDtoFieldForMapping("nbOfEmployees");
        ResultSetDto<OrganizationQueryDto> result = dtoMapper.mapResultSet(orgResultSet, OrganizationQueryDto.class);

        for (OrganizationQueryDto dto : result.getResult()) {
            String tenantId = dto.getTenantId();
            dto.setNbOfSites(serviceProviderService.getSiteCount(tenantId));
            dto.setNbOfTeams(serviceProviderService.getTeamCount(tenantId));
            dto.setNbOfEmployees(serviceProviderService.getEmployeeCount(tenantId));
        }
        return result;
    }

	/**
	 * resetAllLogins() 
	 * 						sets all logins with; lowercase( firstname + firstchar(lastname) + lastchar(lastname))
	 * 						resets all passwords to 'chgpwd'
	 * @param orgId
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public void resetAllLogins(String orgId, boolean obfuscate) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
		Set<String> logins = new HashSet<>();			// currently processed logins.

		// TODO break  queries into chunks (say 100 emps at a time)

		SimpleQuery sq = new SimpleQuery(orgId);
		sq.setLimit(100000);
		ResultSet<UserAccount> accounts = userAccountService.findUserAccounts(sq);
		for (UserAccount account : accounts.getResult()) {
			logins.add(account.getLogin());	
		}
		
		for (UserAccount account : accounts.getResult()) {
			Employee emp = account.getEmployee();
			if (emp == null) {
				userAccountService.changePassword(account, "chgpwd", null);
			} else {
				String login = employeeService.createLogin(emp, logins, account.getFirstName(), account.getLastName(),
                        null, false);
				// skip employees with no valid first + last name, and employees generating duplicate logins
				if (!logins.contains(login)) {			
					account.setLogin(login);
					userAccountService.changePassword(account, "chgpwd", null); // likely to  change login + password
					userAccountService.update(account);
					logins.add(login);
				}
				if (obfuscate) {
					changeEmployeeContactInformation(emp, account);
					userAccountService.update(account);
					employeeService.update(emp);
				}
			}
		}
	}

	public void migrateEmployees(String orgId) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		SimpleQuery sq = new SimpleQuery(orgId);
		sq.setLimit(100000);
		ResultSet<Employee> employees = employeeService.findEmployees(sq);

		for (Employee emp : employees.getResult()) {
			PrimaryKey empPk = emp.getPrimaryKey();
			UserAccount account = emp.getUserAccount();
			if (account == null) {
				// let's create an account as this emp doens't have one yet
		        employeeFacade.updateUserAccount(empPk, null);
		        // initialize notification config
		        employeeService.initEmployeeNotificationConfig(emp);
		        account = emp.getUserAccount();
			}
			// let's duplicate the employee attribute values to user account
			if (emp.getSite() != null) {
				account.setTimeZone(emp.getSite().getTimeZone());
			}
			account.setCountry(emp.getCountry());	
			account.setCity(emp.getCity())	;
			account.setAddress(emp.getAddress());
			account.setAddress2(emp.getAddress2());
			account.setState(emp.getState());	
			account.setZip(emp.getZip());
			account.setGender(emp.getGender());
			account.setFirstName(emp.getFirstName());
			account.setLastName(emp.getLastName());
			account.setMiddleName(emp.getMiddleName());

			// change employee contact information 
			changeEmployeeContactInformation(emp, account); 

			// re-set notification config with current values so as to copy config to user acount
			NotificationSettingDto notificationSettingDto = employeeFacade.getNotificationSettings(empPk);
			employeeFacade.updateNotificationSettings(empPk, notificationSettingDto);
		}
	}
	
	private void changeEmployeeContactInformation(Employee emp, UserAccount account){
		String login = account.getLogin();
		String tenantId = account.getTenantId();
		emp.setWorkEmail("work." + login + "@" + tenantId + ".emlogis.com");
		emp.setHomeEmail("home." + login + "@" + tenantId + ".emlogis.com");
		emp.setWorkPhone("713-785-0960");
		emp.setMobilePhone("713-785-0960");
		emp.setHomePhone("713-785-0960");
		emp.setAddress("9800 Richmond Avenue");
		emp.setAddress2("Suite 235");
		emp.setCity("Houston");
		emp.setZip("77042");
		emp.setCountry("USA");
		emp.setState("Texas");
		emp.setEcPhoneNumber("713-785-0960");
		emp.setEmergencyContact("EmLogis");
		emp.setEcRelationship("Employer");
		
		account.setWorkEmail(emp.getWorkEmail());
		account.setWorkPhone(emp.getWorkPhone());
		account.setMobilePhone(emp.getMobilePhone());
		account.setHomePhone(emp.getHomePhone());
		account.setHomeEmail(emp.getHomeEmail());
		account.setNotificationEnabled(emp.getIsNotificationEnabled());
		if (StringUtils.isNotEmpty(emp.getWorkEmail())) {
			account.setEmail(emp.getWorkEmail());
		} else if (StringUtils.isNotEmpty(emp.getHomeEmail())) {
			account.setEmail(emp.getHomeEmail());
		}
	}
	
	/**
	 * hashAllPasswords() 
	 */
	public void hashAllPasswords() throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
		SimpleQuery sq = new SimpleQuery();
		sq.setLimit(100000);
		ResultSet<UserAccount> accounts = userAccountService.findUserAccounts(sq);
		
		// hash all passwords in system, but migration account ones
		for (UserAccount account : accounts.getResult()) {
			if (!StringUtils.equals(account.getLogin(), "migration")) {
				String currentPassword = account.getPassword();
				if (currentPassword != null && currentPassword.length() < 16) {
					userAccountService.changePassword(account, currentPassword, null);
					userAccountService.update(account);
				}
			}
		}
	}

    public Map<String, Integer> getCounters(String spId) {
        Map<String, Integer> result = new HashMap<>();

        List<Object[]> rows = serviceProviderService.getCounters(spId);
        for (Object[] row : rows) {
            result.put((String) row[1], ((Number) row[0]).intValue());
        }

        result.put("appServerCount", hazelcastClientService.getAppServerCount());
        result.put("engineCount", hazelcastClientService.getEngineCount());

        return result;
    }
}
