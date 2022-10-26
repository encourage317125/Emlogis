package com.emlogis.common.services.tenant;


import com.emlogis.common.security.Permissions;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.tenant.*;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;

import org.apache.commons.lang3.StringUtils;

import javax.ejb.*;
import javax.persistence.Query;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.*;

/**
 * 
 * Service in charge of  operations specific to ServiceProvider tenant, ie creating & managing Customers
 * @author EmLogis
 *
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement()
public class ServiceProviderService extends TenantService<ServiceProvider> {

    @EJB
    protected OrganizationService orgService;

    // -----------------------------------------------------------------------
    // Service provider APIs (APIs that act upon ServiceProvider entities)
    // -----------------------------------------------------------------------
	    
	/**
	 * findServiceProviders() find a list of ServiceProviders matching criteria;
	 * @param simpleQuery
	 * @return ResultSet<ServiceProvider>
	 */
	public ResultSet<ServiceProvider> findServiceProviders(SimpleQuery simpleQuery) {
		return this.findTenants(ServiceProvider.class, simpleQuery);
	}
    
	/**
	 * getServiceProviderById() return serviceProvider data specified by tenantId (= serviceProvider id)
	 * @param tenantId
	 * @return ServiceProvider
	 */
	public ServiceProvider getServiceProvider(String tenantId) {
		return this.getTenant(ServiceProvider.class, tenantId);
	}    

	public ServiceProvider updateServiceProvider(ServiceProvider serviceProvider) {
        return this.updateTenant(serviceProvider);
	}

    public ServiceProvider createServiceProvider(String tenantId, String name, String description)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	ServiceProvider tenant = new ServiceProvider();
    	tenant.setTenantId(tenantId);
    	tenant.setName(name);
    	tenant.setDescription(description);
        getEntityManager().persist(tenant);
        initTenant(tenant);
    	return tenant;
    }


    // -----------------------------------------------------------------------
    // Organization Management APIs 
    // (ServiceProvider APIs that act upon Customers entities)
    // -----------------------------------------------------------------------
    
    public Organization createOrganization(String tenantId, String name, String description)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            IOException, NoSuchMethodException {
    	return orgService.createOrganization(tenantId, name, description);
    }

    /**
     * initTenant() Initialize a newly created ServiceProvider with a default structure
     * 	- default roles
     *  - default groups
     *  - default accounts
     *  - AOM core metatModel
     *  
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws IOException 
     */
    private ServiceProvider initTenant(ServiceProvider tenant)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    	// create default accounts, groups and roles
		String createdBy = UserAccount.DEFAULT_ADMIN_ID;
		String tenantId = tenant.getTenantId();

		Map<String,Role> commonRoles = super.initTenant(tenant);
		Role adminRole = commonRoles.get( Role.DEFAULT_ADMINROLE_ID);
		Role accountMngrRole = commonRoles.get( Role.DEFAULT_ACCOUNTMANAGERROLE_ID);
//		Role roleMngrRole = commonRoles.get( Role.DEFAULT_ROLEMANAGERROLE_ID);
		
		PrimaryKey pk = new PrimaryKey(tenantId, Role.DEFAULT_ROLEMANAGERROLE_ID);
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

				
		// roles for Service provider
		pk = new PrimaryKey(tenantId, Role.DEFAULT_SERVICEADMIN_ID);
		Role svcadminRole = roleService.createRole(pk);
		svcadminRole.setName("Customer Manager");
		svcadminRole.setLabel("Customer Manager");
		svcadminRole.setDescription("Customer Manager");
		svcadminRole.setCreatedBy(createdBy);
		roleService.update(svcadminRole);
		roleService.addPermissions(svcadminRole, Permissions.Tenant_View, Permissions.Tenant_Mgmt, Permissions.AccountProfile_Update);
		
		// Service Admins (Customer admins)
		pk = new PrimaryKey(tenantId, GroupAccount.DEFAULT_SERVICEADMINGROUP_ID);
		GroupAccount svcadmins = groupAccountService.createGroupAccount(pk);
		svcadmins.setName("Customer Managers");
		svcadmins.setDescription("Customer Managers");
		svcadmins.setCreatedBy(createdBy);
		groupAccountService.update(svcadmins);
		groupAccountService.addRole(svcadmins, svcadminRole);
		groupAccountService.addRole(svcadmins, adminRole);

		pk = new PrimaryKey(tenantId, UserAccount.DEFAULT_SERVICEADMIN_ID);
		UserAccount svcadmin = new UserAccount(pk);
		svcadmin.setFirstName("Customer");
		svcadmin.setLastName("Manager");
		svcadmin.setLogin("svcadmin");
		svcadmin.setPassword("svcadmin", false);
		svcadmin.setCreatedBy(createdBy);
		svcadmin.setOwnedBy(svcadmin.getId());
		userAccountService.insert(svcadmin);
		groupAccountService.addMember(svcadmins, svcadmin);
		

		// System Admins
		pk = new PrimaryKey(tenantId, GroupAccount.DEFAULT_ADMINGROUP_ID);
		GroupAccount admins = groupAccountService.createGroupAccount(pk);
		admins.setName("Administrators");
		admins.setDescription("System Administrators");
		admins.setCreatedBy(createdBy);
		groupAccountService.update(admins);
		groupAccountService.addRole(admins, adminRole);
		groupAccountService.addRole(admins, accountMngrRole);
		groupAccountService.addRole(admins, roleMngrRole);

		pk = new PrimaryKey(tenantId, UserAccount.DEFAULT_ADMIN_ID);
		UserAccount admin = new UserAccount(pk);
		admin.setName("System Administrator");
		admin.setLogin("sysadmin");
		admin.setPassword("sysadmin", false);
		admin.setCreatedBy(createdBy);	
		admin.setOwnedBy(admin.getId());
		userAccountService.insert(admin);
		userAccountService.addRole(admin, accountMngrRole);
		userAccountService.addRole(admin, roleMngrRole);
		groupAccountService.addMember(admins, admin);
		
		return tenant;
    }

    public List<Object> quickSearch(String searchValue, String searchFields, String returnedFields, int limit,
                                      String orderBy, String orderDir)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String searchFieldsClause = SimpleQueryHelper.createSearchFieldsClause(searchValue, searchFields, "o");

        String returnedFieldsClause = SimpleQueryHelper.createReturnedFieldsClause(returnedFields, "o");

        String sql =
                "SELECT " + (returnedFieldsClause != null ? returnedFieldsClause : "o.* ") +
                "  FROM Organization o " +
                (searchFieldsClause != null ? " WHERE (" + searchFieldsClause + ")" : "");

        if (StringUtils.isNotBlank(orderBy)) {
            sql += " ORDER BY o." + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        Query query = getEntityManager().createNativeQuery(sql);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        return query.getResultList();
    }

    public ResultSet<Organization> query(String searchValue, String searchFields, int offset, int limit, String orderBy,
                                         String orderDir)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String searchFieldsClause = SimpleQueryHelper.createSearchFieldsClause(searchValue, searchFields, "o");

        String sql =
                "SELECT o.* FROM Organization o " +
                (searchFieldsClause != null ? " WHERE (" + searchFieldsClause + ")" : "");

        String countSql = "SELECT COUNT(*) FROM (" + sql + ") x";

        if (StringUtils.isNotBlank(orderBy)) {
            sql += " ORDER BY o." + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        Query query = getEntityManager().createNativeQuery(sql, Organization.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        Query countQuery = getEntityManager().createNativeQuery(countSql);

        Collection<Organization> organizations = query.getResultList();
        BigInteger total = (BigInteger) countQuery.getSingleResult();

        return new ResultSet<>(organizations, total.intValue());
    }

    public int getSiteCount(String tenantId) {
        return getCount("Site", tenantId);
    }

    public int getTeamCount(String tenantId) {
        return getCount("Team", tenantId);
    }

    public int getEmployeeCount(String tenantId) {
        return getCount("Employee", tenantId);
    }

    public void deleteTenant(String tenantId) {
        String sql =
            "SELECT table_name, column_name " +
            "  FROM information_schema.columns " +
            " WHERE (column_name LIKE '%tenantId' OR column_name LIKE '%tenant_id') AND table_schema = 'EGS'";
        Query query = getEntityManager().createNativeQuery(sql);
        List<Object[]> rows = query.getResultList();

        Map<String, String> tableColumnMap = new HashMap<>();
        for (Object[] row : rows) {
            String table = (String) row[0];
            if (!StringUtils.equalsIgnoreCase("ArchivedSendQueueNotification", table)
                    && !StringUtils.equalsIgnoreCase("FailedSendNotification", table)
                    && !StringUtils.equalsIgnoreCase("ReceiveNotification", table)) {
                tableColumnMap.put(table, (String) row[1]);
            }
        }

        while (tableColumnMap.keySet().iterator().hasNext()) {
            String table = tableColumnMap.keySet().iterator().next();
            deleteFromTable(table, tenantId, tableColumnMap);
        }
    }

    public List<Object[]> getCounters(String spId) {
        String sql =
            "SELECT count(*), 'customerCount' FROM Organization " +
            "UNION " +
            "SELECT count(*), 'siteCount' FROM Site WHERE isDeleted = FALSE " +
            "UNION " +
            "SELECT count(*), 'teamCount' FROM Team WHERE isDeleted = FALSE " +
            "UNION " +
            "SELECT count(*), 'skillCount' FROM Skill WHERE isActive " +
            "UNION " +
            "SELECT count(*), 'employeeCount' FROM Employee WHERE activityType != 0 " +
            "UNION " +
            "SELECT count(*), 'userCount' FROM UserAccount ua  " +
            "              LEFT JOIN Employee e ON ua.id = e.userAccountId AND ua.tenantId = e.userAccountTenantId " +
            " WHERE e.id IS NULL " +
            "UNION " +
            "SELECT count(*), 'groupCount' FROM GroupAccount " +
            "UNION " +
            "SELECT count(*), 'simulationScheduleCount' FROM Schedule WHERE status = 0 " +
            "UNION " +
            "SELECT count(*), 'productionScheduleCount' FROM Schedule WHERE status = 1 " +
            "UNION " +
            "SELECT count(*), 'postedScheduleCount' FROM Schedule WHERE status = 2 ";

        Query query = getEntityManager().createNativeQuery(sql);

        return query.getResultList();
    }

    private int getCount(String table, String tenantId) {
        Query query = getEntityManager().createNativeQuery("SELECT COUNT(*) FROM " + table
                + " WHERE tenantId = :tenantId");
        query.setParameter("tenantId", tenantId);
        return ((BigInteger) query.getSingleResult()).intValue();
    }

    private void deleteFromTable(String tableName, String tenantId, Map<String, String> tableColumnMap) {
        String sql =
            "SELECT DISTINCT table_name " +
            "  FROM information_schema.key_column_usage " +
            " WHERE referenced_table_name = :tableName ";

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tableName", tableName);

        List<String> tables = (List<String>) query.getResultList();
        for (String table : tables) {
            deleteFromTable(table, tenantId, tableColumnMap);
        }

        String column = tableColumnMap.get(tableName);

        try {
            sql = String.format("DELETE FROM %s WHERE %s = '%s'", tableName, column, tenantId);
            query = getEntityManager().createNativeQuery(sql);
            query.executeUpdate();

            tableColumnMap.remove(tableName);
        } catch (Exception e) {
            System.out.println(tableName + " " + column);
            throw e;
        }
    }

}
