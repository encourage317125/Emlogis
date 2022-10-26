package com.emlogis.common.services.tenant;

import com.emlogis.common.ModelUtils;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.security.Permissions;
import com.emlogis.common.services.contract.ContractService;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.model.ACE;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.aom.AOMRelationshipDef;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.dto.EmployeeCreateDto;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.structurelevel.Holiday;
import com.emlogis.model.tenant.*;
import com.emlogis.model.tenant.settings.SchedulingSettings;
import com.emlogis.rest.resources.util.QueryPattern;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.server.services.AOMService;
import com.emlogis.model.common.CacheConstants;
import com.emlogis.model.contract.EmployeeContract;
import com.emlogis.server.services.cache.BasicCacheService;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.persistence.Query;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Service in charge of Customer / Account / Role and Permission Administration.
 *
 * @author EmLogis
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement()
public class OrganizationService extends TenantService<Organization> {

    private final Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    @EJB
    private AOMService aomService;

    @EJB
    private EmployeeService employeeSvc;

    @EJB
    private ContractService contractSvc;

    @EJB
    private BasicCacheService cacheService;
    
    @EJB
	protected ACEService aceService;


    /**
     * findOrganizations() find a list of Organizations matching criteria;
     *
     * @param simpleQuery
     * @return ResultSet<Organization>
     */
    public ResultSet<Organization> findOrganizations(SimpleQuery simpleQuery) {
        return this.findTenants(Organization.class, simpleQuery);
    }

    /**
     * getOrganizationById() return organization data specified by tenantId (= organization id)
     *
     * @param tenantId
     * @return Organization
     */
    public Organization getOrganization(String tenantId) {
        return this.getTenant(Organization.class, tenantId);
    }

    public Organization updateOrganization(Organization organization) {
        return this.updateTenant(organization);
    }

    // tried to make this package level method so as to be invoked only by the ServiceProvider Service
    // but seems that EJB can expose only public methods  (which makes sense...)
    public Organization createOrganization(String tenantId, String name, String description)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            IOException, NoSuchMethodException {
        Organization tenant = new Organization();
        tenant.setTenantId(tenantId);
        if (StringUtils.isNotBlank(name)) {
            tenant.setName(name);
        }
        if (StringUtils.isNotBlank(description)) {
            tenant.setDescription(description);
        }

        getEntityManager().persist(tenant);

        SchedulingSettings schedulingSettings = new SchedulingSettings(new PrimaryKey(tenantId));
        tenant.setSchedulingSettings(schedulingSettings);
        insertSchedulingSettings(schedulingSettings);

        initTenant(tenant);
        return tenant;
    }

    public void insertSchedulingSettings(SchedulingSettings schedulingSettings) {
        getEntityManager().persist(schedulingSettings);
    }

    public SchedulingSettings updateSchedulingSettings(SchedulingSettings schedulingSettings) {
        return getEntityManager().merge(schedulingSettings);
    }

    /**
     * initTenant() Initialize a newly created Organization (Customer) with a default structure
     * - default roles
     * - default groups
     * - default accounts
     * - AOM core metatModel
     *
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws IOException
     * @throws NoSuchMethodException 
     */
    private Organization initTenant(Organization tenant)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            IOException, NoSuchMethodException {
        // creates a fake couple of Optional Module
        List<ModuleLicense> moduleLicenses = new ArrayList<>();
        long now = System.currentTimeMillis();
        long expiration = now + (30L * 24 * 3600 * 1000);
        moduleLicenses.add(new ModuleLicense("ShiftBidding", ModuleStatus.Trial, new Date(expiration).getTime()));
        moduleLicenses.add(new ModuleLicense("TimeAccounting", ModuleStatus.Disabled, 0));
        tenant.setModuleLicenses(moduleLicenses);

        Map<String, Role> commonRoles = super.initTenant(tenant);
        Role adminRole = commonRoles.get(Role.DEFAULT_ADMINROLE_ID);
		roleService.addPermissions(adminRole, 
			Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt,	
			Permissions.Employee_View, Permissions.Employee_Mgmt
		);

        Role accountMngrRole = commonRoles.get(Role.DEFAULT_ACCOUNTMANAGERROLE_ID);
//        Role roleMngrRole = commonRoles.get(Role.DEFAULT_ROLEMANAGERROLE_ID);

        // create default accounts, groups and roles
        String createdBy = UserAccount.DEFAULT_ADMIN_ID;
        String tenantId = tenant.getTenantId();

        PrimaryKey pk = new PrimaryKey(tenantId, Role.DEFAULT_SCHEDULECREATORROLE_ID);
        Role scheduleCreatorRole = roleService.createRole(pk);
        scheduleCreatorRole.setName("Schedule Creator");
        scheduleCreatorRole.setLabel("Schedule Creator");
        scheduleCreatorRole.setDescription("Role with full Demand management, Schedule management, and Employee management access");
        scheduleCreatorRole.setCreatedBy(createdBy);
        roleService.update(scheduleCreatorRole);
        roleService.addPermissions(scheduleCreatorRole,
                Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt,
                Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update,
                Permissions.EmployeeWages_Mgmt,

                Permissions.Demand_View, Permissions.Demand_Mgmt,
                Permissions.Schedule_View, Permissions.Schedule_Mgmt, Permissions.Schedule_AdvancedMgmt, Permissions.Schedule_Update,

                Permissions.Reports_View, Permissions.Reports_Mgmt,
                Permissions.Account_View,

                Permissions.Impersonate_ViewOnly,
                Permissions.Role_View
        );


        pk = new PrimaryKey(tenantId, Role.DEFAULT_SHIFTMANAGERROLE_ID);
        Role shiftMngrRole = roleService.createRole(pk);
        shiftMngrRole.setName("Shift Manager");
        shiftMngrRole.setLabel("Shift Manager");
        shiftMngrRole.setDescription("Role with Shift management and Schedule modification access");
        shiftMngrRole.setCreatedBy(createdBy);
        roleService.update(shiftMngrRole);
        roleService.addPermissions(shiftMngrRole,
                Permissions.OrganizationProfile_View,
                Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.EmployeeProfile_Update,

                Permissions.Schedule_View, Permissions.Schedule_Update, Permissions.Schedule_AdvancedMgmt,
                Permissions.Demand_View,
                Permissions.Shift_Mgmt,

                Permissions.Reports_View, Permissions.Reports_Mgmt,

                Permissions.Availability_Request, Permissions.Availability_RequestMgmt,
                Permissions.Shift_Request, Permissions.Shift_RequestMgmt,
                Permissions.Notification_Recipient,

                Permissions.Impersonate_ViewOnly,
                Permissions.Account_View, Permissions.AccountProfile_Update,

                Permissions.Role_View
        );

        pk = new PrimaryKey(tenantId, Role.DEFAULT_EMPLOYEEROLE_ID);
        Role employeeRole = roleService.createRole(pk);
        employeeRole.setName("Employee");
        employeeRole.setLabel("Employee");
        employeeRole.setDescription("Employee");
        employeeRole.setCreatedBy(createdBy);
        roleService.update(employeeRole);
        roleService.addPermissions(employeeRole,
                Permissions.Employee_View, Permissions.EmployeeProfile_Update,

                Permissions.Schedule_View,
                Permissions.Availability_Request,
                Permissions.Shift_Request,
                Permissions.Notification_Recipient,

                Permissions.AccountProfile_Update
        );

        pk = new PrimaryKey(tenantId, Role.DEFAULT_SERVICESUPPORTROLE_ID);
        Role supportRole = roleService.createRole(pk);
        supportRole.setName("Support");
        supportRole.setLabel("Support");
        supportRole.setDescription("Service Support Personel");
        supportRole.setCreatedBy(createdBy);
        roleService.update(supportRole);
        roleService.addPermissions(supportRole,
                Permissions.Employee_View, Permissions.EmployeeProfile_Update, 
                Permissions.AccountProfile_Update,
                Permissions.Role_View, Permissions.Role_Mgmt,

                Permissions.Impersonate_ViewOnly,
                Permissions.Support
        );

        // now create default groups and accounts/employees
        // scheduler creators
        pk = new PrimaryKey(tenantId, GroupAccount.DEFAULT_SCHEDULECREATORGROUP_ID);
        GroupAccount schedulecreators = groupAccountService.createGroupAccount(pk);
        schedulecreators.setName("Schedule Creators");
        schedulecreators.setDescription("Schedule Creators");
        schedulecreators.setCreatedBy(createdBy);
        groupAccountService.update(schedulecreators);
        groupAccountService.addRole(schedulecreators, scheduleCreatorRole);
//        groupAccountService.addRole(schedulecreators, employeeRole);
        setACLforGroup(schedulecreators, true);
//        groupAccountService.addRole(schedulecreators, accountMngrRole);
//        groupAccountService.addRole(schedulecreators, roleMngrRole);

        pk = new PrimaryKey(tenantId, UserAccount.DEFAULT_SCHEDULECREATOR_ID);
//        String name = "Scheduler creator";
        UserAccount schedulecreator = new UserAccount(pk);
        schedulecreator.setFirstName("Scheduler");
        schedulecreator.setLastName("creator");
        schedulecreator.setLogin("schedulecreator");
        schedulecreator.setPassword("chgpwd", false);
        schedulecreator.setCreatedBy(createdBy);
        schedulecreator.setOwnedBy(schedulecreator.getId());
        userAccountService.insert(schedulecreator);
//        createDefaultEmployee(tenantId, UserAccount.DEFAULT_SCHEDULECREATOR_ID + "-emp", name, name,
//                UserAccount.DEFAULT_SCHEDULECREATOR_ID, schedulecreator);
        groupAccountService.addMember(schedulecreators, schedulecreator);

        // Shift managers
        pk = new PrimaryKey(tenantId, GroupAccount.DEFAULT_SHIFTMANAGERGROUP_ID);
        GroupAccount shiftMngrs = groupAccountService.createGroupAccount(pk);
        shiftMngrs.setName("Shift Managers");
        shiftMngrs.setDescription("Shift Managers");
        shiftMngrs.setCreatedBy(createdBy);
        groupAccountService.update(shiftMngrs);
        groupAccountService.addRole(shiftMngrs, shiftMngrRole);
//        groupAccountService.addRole(shiftMngrs, employeeRole);
        setACLforGroup(shiftMngrs, true);

        pk = new PrimaryKey(tenantId, UserAccount.DEFAULT_SHIFTMANAGER_ID);
//        name = "Default Shift Manager";
        UserAccount shiftMngr = new UserAccount(pk);
        shiftMngr.setFirstName("Shift");
        shiftMngr.setLastName("Manager");
        shiftMngr.setLogin("shiftmanager");
        shiftMngr.setPassword("chgpwd", false);
        shiftMngr.setCreatedBy(createdBy);
        shiftMngr.setOwnedBy(shiftMngr.getId());
        userAccountService.insert(shiftMngr);
//        createDefaultEmployee(tenantId, UserAccount.DEFAULT_SHIFTMANAGER_ID + "-emp", name, name,
//                UserAccount.DEFAULT_SHIFTMANAGER_ID, shiftMngr);
        groupAccountService.addMember(shiftMngrs, shiftMngr);

        // Employees
        pk = new PrimaryKey(tenantId, GroupAccount.DEFAULT_EMPLOYEEGROUP_ID);
        GroupAccount employees = groupAccountService.createGroupAccount(pk);
        employees.setName("Employees");
        employees.setDescription("Employees");
        employees.setCreatedBy(createdBy);
        groupAccountService.update(employees);
        groupAccountService.addRole(employees, employeeRole);

        // Support
        pk = new PrimaryKey(tenantId, GroupAccount.DEFAULT_SUPPORTGROUP_ID);
        GroupAccount supports = groupAccountService.createGroupAccount(pk);
        supports.setName("Support Group");
        supports.setDescription("Support Group");
        supports.setCreatedBy(createdBy);
        groupAccountService.update(supports);
        groupAccountService.addRole(supports, supportRole);
        groupAccountService.addRole(supports, accountMngrRole);
        setACLforGroup(supports, true);
//        groupAccountService.addRole(supports, roleMngrRole);
//        groupAccountService.addRole(supports, employeeRole);


        pk = new PrimaryKey(tenantId, UserAccount.DEFAULT_SUPPORTACCOUNT_ID);
//        name = "Support Person";
        UserAccount support = new UserAccount(pk);
        support.setFirstName("EmLogis");
        support.setLastName("Support");
        support.setLogin("support");
        support.setPassword("chgpwd", false);
        support.setCreatedBy(createdBy);
        support.setOwnedBy(support.getId());
        userAccountService.insert(support);
//        createDefaultEmployee(tenantId, UserAccount.DEFAULT_SUPPORTACCOUNT_ID + "-emp", name, name,
//                UserAccount.DEFAULT_SUPPORTACCOUNT_ID, support);
        groupAccountService.addMember(supports, support);

        // make schedulecreator, shiftmanager, support employees
//        groupAccountService.addMember(employees, schedulecreator);
//        groupAccountService.addMember(employees, shiftMngr);
//        groupAccountService.addMember(employees, support);

        // System Admins
        pk = new PrimaryKey(tenantId, GroupAccount.DEFAULT_ADMINGROUP_ID);
        GroupAccount admins = groupAccountService.createGroupAccount(pk);
        admins.setName("Administrators");
        admins.setDescription("System Administrators");
        admins.setCreatedBy(createdBy);
        groupAccountService.update(admins);
        groupAccountService.addRole(admins, adminRole);
        groupAccountService.addRole(admins, accountMngrRole);
        setACLforGroup(admins, true);

//        groupAccountService.addRole(admins, roleMngrRole);

        pk = new PrimaryKey(tenantId, UserAccount.DEFAULT_ADMIN_ID);
        UserAccount admin = new UserAccount(pk);
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setLogin("admin");
        admin.setPassword("chgpwd", false);
        admin.setCreatedBy(createdBy);
        admin.setOwnedBy(admin.getId());
        userAccountService.insert(admin);
        userAccountService.addRole(admin, adminRole);
        userAccountService.addRole(admin, accountMngrRole);
//        userAccountService.addRole(admin, roleMngrRole);
        groupAccountService.addMember(admins, admin);

        // create a special migration account with all Roles
        pk = new PrimaryKey(tenantId, UserAccount.MIGRATIONACCOUNT_ID);
        UserAccount migration = new UserAccount(pk);
        migration.setFirstName("Migration");
        migration.setLastName("Account");
        migration.setLogin("migration");
        migration.setPassword("migration", true);
        migration.setCreatedBy(createdBy);
        migration.setOwnedBy(migration.getId());
        userAccountService.insert(migration);
        userAccountService.addRole(migration, adminRole);
        userAccountService.addRole(migration, accountMngrRole);
//        userAccountService.addRole(migration, roleMngrRole);
        userAccountService.addRole(migration, scheduleCreatorRole);
        userAccountService.addRole(migration, shiftMngrRole);
        groupAccountService.addMember(admins, migration);

        // now, create a default metamodel for this organization
        aomService.createAOMTenantMetamodel(tenantId, UserAccount.DEFAULT_ADMIN_ID, UserAccount.DEFAULT_SCHEDULECREATOR_ID);

        // with a Site to Team relationships
        AOMRelationshipDef rdef = new AOMRelationshipDef();
        rdef.setType(AOMRelationshipDef.SITE_TEAM_REL);
        rdef.setLabel("Site Team membership");
        rdef.setSrcEntityType(Site.AOM_ENTITY_TYPE);
        rdef.setSrcCardinality("1");                    // this is not checked yet. cardinality should be define via an ENUM !!
        rdef.setDstEntityType(Team.AOM_ENTITY_TYPE);
        rdef.setDstCardinality("*");                    // this is not checked yet.
        aomService.addAOMRelationshipDef(tenantId, rdef, UserAccount.DEFAULT_ADMIN_ID);

        return tenant;
    }
    
    private void setACLforGroup(GroupAccount group, boolean createFullAccessACL) {
    	/*
    	String id = "acl-" + group.getId();
        PrimaryKey pk = new PrimaryKey(group.getTenantId(), id);
        Role aclRole = roleService.createRole(pk);
        String name = "ACL for " + group.getName();
        String description = group.getName() + " ACL Role";
        aclRole.setName(name);
        aclRole.setLabel(description);
        aclRole.setDescription(description);
        aclRole.setCreatedBy(group.getCreatedBy());
        roleService.update(aclRole);
        groupAccountService.addRole(group, aclRole);
        */
        Role aclRole = groupAccountService.getACLRole(group);  
        if (createFullAccessACL) {
        	Set<Permissions> permissions = new HashSet<>();
        	permissions.add(Permissions.OrganizationProfile_View);
        	permissions.add(Permissions.OrganizationProfile_Mgmt);
        	
            ACE ace = aceService.createAce(
                    new PrimaryKey(group.getTenantId()),
                    Site.class,
                    "/.*",				// pattern for all Sites access
                    permissions,
                    "ACL-Role READ/WRITE access to all sites");
            aclRole.addAce(ace);
            ace = aceService.createAce(
                    new PrimaryKey(group.getTenantId()),
                    Team.class,
                    "/.*/Site_Team/.*",				// pattern for all Sites / All Teams access
                    permissions,
                    "ACL-Role READ/WRITE access to all Teams of All Sites");
            aclRole.addAce(ace);
            updateNameAndDescr(group, aclRole);
            roleService.update(aclRole);
        }
        else {
            updateNameAndDescr(group, aclRole);
        	aclRole.setDescription(aclRole.getDescription() + " (No Access)");
        }
    }
    
	private void updateNameAndDescr(GroupAccount group, Role aclRole) {
		String name = group.getName();
        if (!StringUtils.isBlank(name)) {
        	aclRole.setName(name + " ACL");
        	aclRole.setDescription("ACL for Group: " + name);
        }
	}

    @SuppressWarnings("unchecked")
    public ResultSet<Holiday> getHolidays(SimpleQuery simpleQuery) {
		simpleQuery.setEntityClass(Holiday.class);
		SimpleQueryHelper queryHelper = new SimpleQueryHelper();
		return queryHelper.executeSimpleQueryWithPaging(getEntityManager(), simpleQuery);
    }

    @SuppressWarnings("unchecked")
    public Collection<Holiday> getHolidays(String tenantId) {
        Collection<Holiday> holidays = (Collection<Holiday>) cacheService.getEntries(CacheConstants.ORG_HOLIDAYS_CACHE,
                tenantId);
        if (holidays == null) {
            SimpleQuery simpleQuery = new SimpleQuery(tenantId);
            simpleQuery.setOffset(0).setLimit(-1).setOrderByField("effectiveStartDate").setOrderAscending(true)
                    .setTotalCount(false);
            ResultSet<Holiday> resultSet = getHolidays(simpleQuery);
            holidays = resultSet.getResult();
            cacheService.putEntry(CacheConstants.ORG_HOLIDAYS_CACHE, tenantId, holidays);
        }
        return holidays;
    }

    public Collection<Holiday> getHolidays(String tenantId, Long startDate, Long endDate) {
        String sql =
            "SELECT * FROM Holiday WHERE tenantId = :tenantId " +
            (startDate == null ? "" : " AND effectiveEndDate >= :startDate ") +
            (endDate == null ? "" : " AND effectiveStartDate <= :endDate ") +
            " ORDER BY effectiveStartDate ";

        Query query = getEntityManager().createNativeQuery(sql, Holiday.class);
        query.setParameter("tenantId", tenantId);
        if (startDate != null) {
            query.setParameter("startDate", new Timestamp(startDate));
        }
        if (endDate != null) {
            query.setParameter("endDate", new Timestamp(endDate));
        }

        return query.getResultList();
    }

    public Holiday getHoliday(PrimaryKey primaryKey) {
        return getEntityManager().find(Holiday.class, primaryKey);
    }

    public Holiday updateHoliday(Holiday holiday) {
    	cacheService.clearEntry(CacheConstants.ORG_HOLIDAYS_CACHE, holiday.getTenantId());
        cacheService.clearCache(CacheConstants.EMP_AVAILABILITY_CACHE);

        return getEntityManager().merge(holiday);
    }

	public void deleteHoliday(Organization organization, Holiday holiday) {
    	cacheService.clearEntry(CacheConstants.ORG_HOLIDAYS_CACHE, holiday.getTenantId());
        cacheService.clearCache(CacheConstants.EMP_AVAILABILITY_CACHE);

    	Set<Holiday> holidays = organization.getHolidays();
        holidays.remove(holiday);
        updateOrganization(organization);
        getEntityManager().remove(holiday);
        updateOrganization(organization);
    }

	public void addHoliday(Organization organization, Holiday holiday) {
		cacheService.clearEntry(CacheConstants.ORG_HOLIDAYS_CACHE, holiday.getTenantId());
		cacheService.clearCache(CacheConstants.EMP_AVAILABILITY_CACHE);

        Set<Holiday> holidays = organization.getHolidays();
        holidays.add(holiday);
        getEntityManager().persist(holiday);	
        updateOrganization(organization);	
	}

    public ResultSet<Holiday> duplicateHolidays(Organization organization, int yearFrom, int yearInto) {
    	cacheService.clearEntry(CacheConstants.ORG_HOLIDAYS_CACHE, organization.getTenantId());
        cacheService.clearCache(CacheConstants.EMP_AVAILABILITY_CACHE);

        List<Holiday> holidaysFrom = getHolidaysOfYear(organization, yearFrom);
        List<Holiday> holidaysInto = getHolidaysOfYear(organization,yearInto);

        List<Holiday> holidaysToAdd = new ArrayList<>();

        for (Holiday holidayFrom : holidaysFrom) {
            boolean existsInTargetYear = false;
            for (Holiday holidayInto : holidaysInto) {
                long startDate = plusYear(holidayFrom.getEffectiveStartDate(), yearInto - yearFrom);
                long endDate = plusYear(holidayFrom.getEffectiveEndDate(), yearInto - yearFrom);
                if (StringUtils.equals(holidayFrom.getName(), holidayInto.getName())
                        && StringUtils.equals(holidayFrom.getAbbreviation(), holidayInto.getAbbreviation())
                        && startDate == holidayInto.getEffectiveStartDate()
                        && endDate == holidayInto.getEffectiveEndDate()) {
                    existsInTargetYear = true;
                    break;
                }
            }
            if (!existsInTargetYear) {
                Holiday holiday = new Holiday(new PrimaryKey(organization.getTenantId()));
                holiday.setName(holidayFrom.getName());
                holiday.setAbbreviation(holidayFrom.getAbbreviation());
                holiday.setDescription(holidayFrom.getDescription());
                holiday.setTimeToDeductInMin(holidayFrom.getTimeToDeductInMin());

                long startDateTime = plusYear(holidayFrom.getEffectiveStartDate(), yearInto - yearFrom);
                holiday.setEffectiveStartDate(startDateTime);

                long endDateTime = plusYear(holidayFrom.getEffectiveEndDate(), yearInto - yearFrom);
                holiday.setEffectiveEndDate(endDateTime);

                getEntityManager().persist(holiday);

                holidaysToAdd.add(holiday);
            }
        }

        organization.getHolidays().addAll(holidaysToAdd);
        updateOrganization(organization);

        holidaysInto.addAll(holidaysToAdd);

        ResultSet<Holiday> resultSet = new ResultSet<>();
        resultSet.setResult(holidaysInto);
        resultSet.setTotal(holidaysInto.size());

        return resultSet;
    }

    private long plusYear(long date, int years) {
        DateTime dateTime = new DateTime(date);
        dateTime = dateTime.plusYears(years);
        return dateTime.getMillis();
    }

    private List<Holiday> getHolidaysOfYear(Organization organization, int year) {
        Calendar yearFromFirstDayCalendar = Calendar.getInstance();
        yearFromFirstDayCalendar.set(Calendar.YEAR, year);
        yearFromFirstDayCalendar.set(Calendar.MONTH, Calendar.JANUARY);
        yearFromFirstDayCalendar.set(Calendar.DAY_OF_MONTH, 1);
        yearFromFirstDayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        yearFromFirstDayCalendar.set(Calendar.MILLISECOND, 0);

        Calendar yearFromLastDayCalendar = Calendar.getInstance();
        yearFromLastDayCalendar.set(Calendar.YEAR, year);
        yearFromLastDayCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
        yearFromLastDayCalendar.set(Calendar.DAY_OF_MONTH, 31);
        yearFromLastDayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        yearFromLastDayCalendar.set(Calendar.MILLISECOND, 0);

        String sql = "SELECT * FROM Holiday h WHERE h.tenantId = :tenantId " +
                     "                          AND DATE(h.effectiveStartDate) >= DATE(:dateFrom) " +
                     "                          AND DATE(h.effectiveEndDate) <= DATE(:dateTo) ";

        Query query = getEntityManager().createNativeQuery(sql, Holiday.class);
        query.setParameter("tenantId", organization.getTenantId());
        query.setParameter("dateFrom", yearFromFirstDayCalendar.getTime());
        query.setParameter("dateTo", yearFromLastDayCalendar.getTime());

        return query.getResultList();
    }

    public Collection<Object[]> getManagersByTeams(String tenantId, AccountACL accountAcl) {
        String aceIds = accountAcl == null ? null : ModelUtils.commaSeparatedQuotedValues(accountAcl.getAceIds());

        String sql =
            "SELECT DISTINCT t.id teamId, t.name teamName, e.id employeeId, CONCAT(e.firstName, ' ', e.lastName)" +
            "     , ua.id, ua.name " +
            " FROM UserAccount ua " +
            "  LEFT JOIN Employee e ON ua.id = e.userAccountId AND ua.tenantId = e.userAccountTenantId " +
            "  LEFT JOIN User_Group ug ON ua.id = ug.user_id AND ua.tenantId = ug.user_tenantId " +
            "  LEFT JOIN GroupAccount ga ON ug.group_id = ga.id AND ug.group_tenantId = ga.tenantId " +
            "  LEFT JOIN Role_Account ra ON ra.account_id = ua.id OR ra.account_id = ga.id " +
            "  LEFT JOIN Role r ON r.id = ra.role_id AND r.tenantId = ra.role_tenantId " +
            "  LEFT JOIN Role_ACE role_ace ON r.id = role_ace.role_id AND r.tenantId = role_ace.role_tenantId " +
            "  LEFT JOIN ACE ace ON ace.id = role_ace.ace_id AND ace.tenantId = role_ace.ace_tenantId " +
            "   , Team t " +
            (StringUtils.isEmpty(aceIds) ? "" : " , ACE race ") +
            "WHERE ua.tenantId = :tenantId AND t.tenantId = ua.tenantId " +
            "  AND t.path REGEXP ace.pattern AND ace.entityClass = 'Team' " +
            (StringUtils.isEmpty(aceIds) ? ""
                    : " AND race.id IN (" + aceIds + ") AND t.path REGEXP race.pattern AND race.entityClass = 'Team' ") +
                    "  AND " + QueryPattern.NOT_DELETED.val("e") + "  AND " + QueryPattern.NOT_DELETED.val("t");

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);

        return query.getResultList();
    }

    public ResultSet<RememberMe> getRememberMeObjects(SimpleQuery simpleQuery) {
        SimpleQueryHelper simpleQueryHelper = new SimpleQueryHelper();
        return simpleQueryHelper.executeSimpleQueryWithPaging(getEntityManager(), simpleQuery);
    }

    public List<Object[]> getCounters(String tenantId) {
        String sql = 
            "SELECT count(*), 'siteCount' FROM Site WHERE tenantId = :tenantId AND isDeleted = FALSE " +
            "UNION " +
            "SELECT count(*), 'teamCount' FROM Team WHERE tenantId = :tenantId AND isDeleted = FALSE " +
            "UNION " +
            "SELECT count(*), 'skillCount' FROM Skill WHERE tenantId = :tenantId AND isActive " +
            "UNION " +
            "SELECT count(*), 'employeeCount' FROM Employee WHERE tenantId = :tenantId AND activityType != 0 " +
            "UNION " +
            "SELECT count(*), 'userCount' FROM UserAccount ua  " +
            "              LEFT JOIN Employee e ON ua.id = e.userAccountId AND ua.tenantId = e.userAccountTenantId " +
            " WHERE ua.tenantId = :tenantId AND e.id IS NULL " +
            "UNION " +
            "SELECT count(*), 'groupCount' FROM GroupAccount WHERE tenantId = :tenantId " +
            "UNION " +
            "SELECT count(*), 'simulationScheduleCount' FROM Schedule WHERE tenantId = :tenantId AND status = 0 " +
            "UNION " +
            "SELECT count(*), 'productionScheduleCount' FROM Schedule WHERE tenantId = :tenantId AND status = 1 " +
            "UNION " +
            "SELECT count(*), 'postedScheduleCount' FROM Schedule WHERE tenantId = :tenantId AND status = 2 ";

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);

        return query.getResultList();
    }

}
