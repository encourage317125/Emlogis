package com.emlogis.common.services.tenant;

import com.emlogis.common.Constants;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.security.PermissionScope;
import com.emlogis.common.security.Permissions;
import com.emlogis.common.services.structurelevel.SiteService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.model.ACE;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.tenant.*;
import com.emlogis.rest.resources.util.QueryPattern;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.shared.services.eventservice.EventService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class RoleService {
	
	private final Logger logger = LoggerFactory.getLogger(RoleService.class);
	
    @EJB
    private ACEService aclService;
    
    @EJB
    private TeamService teamService;

    @EJB
    private SiteService siteService;

    @Inject
    EventService eventService;

	@PersistenceContext(unitName=Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

	/**
	 * findRoles() find a list of Roles matching criteria;
	 * @param simpleQuery
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public ResultSet<Role> findRoles(SimpleQuery simpleQuery) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException{
		simpleQuery.setEntityClass(Role.class);
		SimpleQueryHelper simpleQueryHelper = new SimpleQueryHelper();
		return simpleQueryHelper.executeSimpleQueryWithPaging(entityManager, simpleQuery);
	}

	public Role getRole(PrimaryKey primaryKey) {
		return entityManager.find(Role.class, primaryKey);
	}

    /**
     * add one permission to a Role
     * @param role
     * @param permissionId
     * @return
     */
    public boolean addPermission(Role role, Permissions permissionId) {
        Permission permission = entityManager.find(Permission.class, permissionId);

        Collection<Permission> permissions = role.getPermissions();
        if (!permissions.contains(permission)) {
            permissions.add(permission);

            update(role);
            return true;
        }
        return false;
    }

    /**
     * add several permissions to a Role
     * @param role
     * @param permissionKeys
     * @return boolean
     */
    public boolean addPermissions(Role role, Permissions ... permissionKeys) {
    	Collection<Permission> permissions = role.getPermissions();
        
        boolean modified = false;
        for (Permissions permissionKey : permissionKeys) {
            Permission permission = entityManager.find(Permission.class, permissionKey);
            if (!permissions.contains(permission)) {
                permissions.add(permission);
                modified = true;
            }
        }
        if (modified) {
        	update(role);
        }
        return modified;
    }
    
    public boolean removePermission(Role role, Permissions permissionId) {
        Permission permission = entityManager.find(Permission.class, permissionId);

        Collection<Permission> permissions = role.getPermissions();
        if (permissions.contains(permission)) {
            permissions.remove(permission);

            update(role);
            return true;
        }
        return false;
    }
    
	public Role createRole(PrimaryKey pk) {
		logger.debug("creating Role: " + pk.toString());

		Role role = new Role(pk);
		insert(role);
		return role;
	}

    protected void insert(Role role) {
        entityManager.persist(role);
    }

    public void update(Role role) {
        role.touch();
        entityManager.merge(role);
    }

    public void updateAce(ACE ace) {
        entityManager.merge(ace);
    }

    public void addAce(Role role, ACE ace) {
    	int aclPrevSize = role.getAcl().size();
    	logger.debug("Adding ACE: " + ace.toString() + " to " + role.toString());
        role.addAce(ace);
        ace.addRole(role);
//        if (aclPrevSize != role.getAcl().size()) {
        	update(role);
        	updateAce(ace);
//        }
    }

    public void removeAce(Role role, ACE ace) {
    	int aclPrevSize = role.getAcl().size();
    	logger.debug("Removing ACE: " + ace.toString() + " from " + role.toString());
        role.removeAce(ace);
        ace.removeRole(role);
//        if (aclPrevSize != role.getAcl().size()) {
        	update(role);
        	updateAce(ace);
//        }
    }

    public void delete(Role role) {
    	role.removeAllPermissions();
    	aclService.deleteAcl(role.getAcl());
    	role.removeAllAces();
        entityManager.remove(role);
    }

    public ResultSet<Permission> getPermissions(PrimaryKey primaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetAssociatedWithPaging(entityManager, simpleQuery, primaryKey, Role.class,
                "permissions");
    }

    public ResultSet<Account> getUnassociatedAccounts(PrimaryKey primaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetUnassociatedWithPaging(entityManager, simpleQuery, primaryKey, Role.class,
                "accounts");
    }

    public ResultSet<Permission> getUnassociatedRolePermissions(PrimaryKey rolePrimaryKey, String filter, int offset,
                                                                int limit, String orderBy, String orderDir)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        String sql =
            "SELECT * " +
            "  FROM Permission " +
            " WHERE id NOT IN ( " +
            "                  SELECT permissions_id " +
            "                    FROM Role_Permission " +
            "                   WHERE Role_tenantId = :tenantId " +
            "                     AND Role_id = :roleId " +
            "                  ) " +
            "   AND (   scope = " + PermissionScope.All.ordinal() +
            "        OR scope = " + PermissionScope.ServiceProvider.ordinal() +
            "            AND EXISTS (SELECT tenantId FROM ServiceProvider WHERE tenantId = :tenantId)" +
            "        OR scope = " + PermissionScope.Customer.ordinal() +
            "            AND EXISTS (SELECT tenantId FROM Organization WHERE tenantId = :tenantId)" +
            "       )" +
            (StringUtils.isEmpty(filter) ? "" : " AND " + filter);

        String countSql = "SELECT count(*) FROM (" + sql + ") x";

        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter("tenantId", rolePrimaryKey.getTenantId());
        countQuery.setParameter("roleId", rolePrimaryKey.getId());

        if (StringUtils.isNotBlank(orderBy)) {
            sql += " ORDER BY " + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        Query query = entityManager.createNativeQuery(sql, Permission.class);
        query.setParameter("tenantId", rolePrimaryKey.getTenantId());
        query.setParameter("roleId", rolePrimaryKey.getId());
        if (offset > 0) {
            query.setFirstResult(offset);
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        ResultSet<Permission> result = new ResultSet<>();
        result.setResult(query.getResultList());
        result.setTotal(((Number) countQuery.getSingleResult()).intValue());

        return result;
    }

    public Collection<UserAccount> findUserAccounts(PrimaryKey rolePrimaryKey, boolean inherited) {
        Role role = getRole(rolePrimaryKey);

        Set<UserAccount> userAccounts = new HashSet<>();
        Set<Account> accounts = role.getAccounts();
        for (Account account : accounts) {
            if (account instanceof UserAccount) {
                userAccounts.add((UserAccount) account);
            } else if (account instanceof GroupAccount && inherited) {
                userAccounts.addAll(((GroupAccount) account).getMembers());
            }
        }
        return userAccounts;
    }

    public Collection<GroupAccount> findGroupAccounts(PrimaryKey rolePrimaryKey) {
        Role role = getRole(rolePrimaryKey);

        Set<GroupAccount> result = new HashSet<>();
        Set<Account> accounts = role.getAccounts();
        for (Account account : accounts) {
            if (account instanceof GroupAccount) {
                result.add((GroupAccount) account);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public Collection<Site> getMatchedSites(SimpleQuery simpleQuery, AccountACL accountACL)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return siteService.findSites(simpleQuery, accountACL).getResult();
    }

    @SuppressWarnings("unchecked")
    public Collection<Team> getMatchedTeams(SimpleQuery simpleQuery, AccountACL accountACL)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return teamService.findTeams(simpleQuery, accountACL).getResult();
    }

    public Collection<ACE> getACEsByEntityClass(PrimaryKey rolePrimaryKey, String entityClass) {
        String sql =
                "SELECT DISTINCT a.* " +
                "  FROM ACE a INNER JOIN Role_ACE ra ON a.id = ra.ace_id AND a.tenantId = ra.ace_tenantId " +
                " WHERE ra.role_id = :roleId AND ra.role_tenantId = :tenantId AND a.entityClass = :entityClass ";

        Query query = entityManager.createNativeQuery(sql, ACE.class);
        query.setParameter("tenantId", rolePrimaryKey.getTenantId());
        query.setParameter("roleId", rolePrimaryKey.getId());
        query.setParameter("entityClass", entityClass);

        return query.getResultList();
    }

    public List<Object> quickSearch(String tenantId, String searchValue, String searchFields, String returnedFields,
                                      int limit, String orderBy, String orderDir)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String searchFieldsClause = SimpleQueryHelper.createSearchFieldsClause(searchValue, searchFields, "r");

        String returnedFieldsClause = SimpleQueryHelper.createReturnedFieldsClause(returnedFields, "r");

        String sql =
                "SELECT " + (returnedFieldsClause != null ? returnedFieldsClause : "r.* ") +
                "  FROM Role r " +
                " WHERE r.tenantId = :tenantId " +
                (searchFieldsClause != null ? " AND (" + searchFieldsClause + ")" : "");

        if (StringUtils.isNotBlank(orderBy)) {
            sql += " ORDER BY r." + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        return query.getResultList();
    }

    public ResultSet<Object[]> query(String tenantId, String searchValue, String searchFields, String userFilter,
                                     String roleFilter, int offset, int limit, String orderBy, String orderDir)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String searchFieldsClause = SimpleQueryHelper.createSearchFieldsClause(searchValue, searchFields, "r");

        String sql =
                "SELECT r.id, r.created, r.createdBy, r.ownedBy, r.updated, r.updatedBy, r.description, " +
                "       r.label, r.name, " +
                "       count(DISTINCT g.id) nbOfGroups, count(DISTINCT u.id) nbOfMembers, " +
                "       GROUP_CONCAT(DISTINCT g.name SEPARATOR ', ') groups " +
                "  FROM Role r " +
                "       LEFT JOIN Role_Account ra ON r.id = ra.role_id AND r.tenantId = ra.role_tenantId " +
                "       LEFT JOIN UserAccount u ON ra.account_id = u.id AND ra.account_tenantId = u.tenantId " +
                "       LEFT JOIN GroupAccount g ON ra.account_id = g.id AND ra.account_tenantId = g.tenantId " +
                " WHERE r.tenantId = :tenantId " +
                (searchFieldsClause != null ? " AND (" + searchFieldsClause + ")" : "");

        if (StringUtils.isNotBlank(userFilter)) {
            sql += " AND (" + SimpleQueryHelper.buildFilterClause(userFilter, "u") + ") ";
        }
        if (StringUtils.isNotBlank(roleFilter)) {
            sql += " AND (" + SimpleQueryHelper.buildFilterClause(roleFilter, "r") + ") ";
        }

        sql += " GROUP BY r.id, r.tenantId ";

        String countSql = "SELECT COUNT(*) FROM (" + sql + ") x";

        if (StringUtils.isNotBlank(orderBy)) {
            if (orderBy.contains("nbOfGroups")) {
                orderBy = "nbOfGroups";
            } else if (orderBy.contains("nbOfMembers")) {
                orderBy = "nbOfMembers";
            } else if (orderBy.contains("groups")) {
                orderBy = "groups";
            } else if (orderBy.startsWith("GroupAccount.")) {
                orderBy = orderBy.replaceFirst("GroupAccount.", "g.");
            } else if (orderBy.startsWith("UserAccount.")) {
                orderBy = orderBy.replaceFirst("UserAccount.", "u.");
            } else if (orderBy.startsWith("Role.")) {
                orderBy = orderBy.replaceFirst("Role.", "r.");
            }
            sql += " ORDER BY " + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter("tenantId", tenantId);

        Collection<Object[]> roleInfos = query.getResultList();
        BigInteger total = (BigInteger) countQuery.getSingleResult();
        return new ResultSet<>(roleInfos, total.intValue());
    }

    public Collection<Employee> employeesInRole(PrimaryKey rolePrimaryKey) {
        String sql =
            "SELECT DISTINCT e.* " +
            "  FROM Employee e " +
            "    LEFT JOIN UserAccount ua ON ua.id = e.userAccountId AND ua.tenantId = e.userAccountTenantId " +
            "    LEFT JOIN User_Group ug ON ua.id = ug.user_id AND ua.tenantId = ug.user_tenantId " +
            "    LEFT JOIN GroupAccount ga ON ga.id = ug.group_id AND ga.tenantId = ug.group_tenantId " +
            "    LEFT JOIN Role_Account ra ON ra.account_id = ua.id AND ra.account_tenantId = ua.tenantId " +
            "                              OR ra.account_id = ga.id AND ra.account_tenantId = ga.tenantId " +
            "    LEFT JOIN Role r ON r.id = ra.role_id AND r.tenantId = ra.role_tenantId " +
                    " WHERE ua.tenantId = :tenantId AND r.id = :roleId " + " AND " + QueryPattern.NOT_DELETED.val("e");

        Query query = entityManager.createNativeQuery(sql, Employee.class);
        query.setParameter("tenantId", rolePrimaryKey.getTenantId());
        query.setParameter("roleId", rolePrimaryKey.getId());

        return query.getResultList();
    }

}
