package com.emlogis.common.services.tenant;

import com.emlogis.model.PrimaryKey;
import com.emlogis.model.tenant.GroupAccount;
import com.emlogis.model.tenant.Role;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.rest.resources.util.QueryPattern;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;

import org.apache.commons.lang3.StringUtils;

import javax.ejb.*;
import javax.persistence.Query;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Stateless
@LocalBean
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class GroupAccountService extends AccountService<GroupAccount> {
	
    @EJB
    public UserAccountService userAccountService;

	/**
	 * findGroupAccounts() find a list of GroupAccounts matching criteria;
	 * @param simpleQuery
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public ResultSet<GroupAccount> findGroupAccounts(SimpleQuery simpleQuery) throws InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		simpleQuery.setEntityClass(GroupAccount.class);
		SimpleQueryHelper sqh = new SimpleQueryHelper();
		return sqh.executeSimpleQueryWithPaging(getEntityManager(), simpleQuery);
	}

	public GroupAccount getGroupAccount(PrimaryKey primaryKey) {
		return getEntityManager().find(GroupAccount.class, primaryKey);
	}

	public GroupAccount createGroupAccount(PrimaryKey pk) {
		GroupAccount groupAccount = new GroupAccount(pk);
		Role aclRole = new Role(new PrimaryKey(pk.getTenantId(), "acl-" + pk.getId()));
		insert(groupAccount);
		addRole(groupAccount, aclRole);
		return groupAccount;
	}

    public void delete(GroupAccount groupAccount) {
    	Role aclRole = this.getACLRole(groupAccount);
    	groupAccount.removeAllMembers();
    	super.delete(groupAccount);
    	getRoleService().delete(aclRole);
    }

    public ResultSet<UserAccount> getMembers(PrimaryKey primaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetAssociatedWithPaging(getEntityManager(), simpleQuery, primaryKey,
                GroupAccount.class, "members");
    }

    public ResultSet<UserAccount> getUnassociatedMembers(PrimaryKey primaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        SimpleQueryHelper.tryAddNotDeletedFilter(simpleQuery, QueryPattern.NOT_DELETED.val("employee"));
        return queryHelper.executeGetUnassociatedWithPaging(getEntityManager(), simpleQuery, primaryKey,
                GroupAccount.class, "members");
    }

    public boolean addMember(GroupAccount groupAccount, UserAccount memberAccount) {
        Collection<UserAccount> members = groupAccount.getMembers();
        if (members != null && !members.contains(memberAccount)) {
            members.add(memberAccount);

            update(groupAccount);
        }
        return true;
    }

    public boolean removeMember(GroupAccount groupAccount, UserAccount memberAccount) {
        Collection<UserAccount> members = groupAccount.getMembers();
        if (members != null && members.contains(memberAccount)) {
            members.remove(memberAccount);

            update(groupAccount);
        }
        return true;
    }

    public List<Object> quickSearch(String tenantId, String searchValue, String searchFields, String returnedFields,
                                    int limit, String orderBy, String orderDir) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String searchFieldsClause = SimpleQueryHelper.createSearchFieldsClause(searchValue, searchFields, "g");

        String returnedFieldsClause = SimpleQueryHelper.createReturnedFieldsClause(returnedFields, "g");

        String sql =
                "SELECT " + (returnedFieldsClause != null ? returnedFieldsClause : "g.* ") +
                "  FROM GroupAccount g " +
                " WHERE g.tenantId = :tenantId " +
                (searchFieldsClause != null ? " AND (" + searchFieldsClause + ")" : "");

        if (StringUtils.isNotBlank(orderBy)) {
            sql += " ORDER BY g." + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        return query.getResultList();
    }

    public ResultSet<Object[]> query(String tenantId, String searchValue, String searchFields, String userFilter,
                                     String roleFilter, String groupFilter, String filter, int offset, int limit,
                                     String orderBy, String orderDir)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String searchFieldsClause = SimpleQueryHelper.createSearchFieldsClause(searchValue, searchFields, "g");

        String filterClause = null;
        if (StringUtils.isNotBlank(filter)) {
            if (filter.startsWith("GroupAccount.")) {
                filterClause = filter.replaceFirst("GroupAccount.", "g.");
            } else if (filter.startsWith("primaryKey.")) {
                filterClause = filter.replaceFirst("primaryKey.", "g.");
            } else if (filter.startsWith("Role.")) {
                filterClause = filter.replaceFirst("Role.", "r.");
            } else {
                filterClause = "g." + filter;
            }
        }

        String sql =
                "SELECT g.id, g.tenantId, g.created, g.createdBy, g.ownedBy, g.updated, g.updatedBy, g.description, " +
                "       g.name, count(DISTINCT u.id) nbOfMembers, count(DISTINCT r.id) nbOfRoles, " +
                "       GROUP_CONCAT(DISTINCT r.name SEPARATOR ', ') roles " +
                "  FROM GroupAccount g " +
                "       LEFT JOIN User_Group ug ON g.id = ug.group_id AND g.tenantId = ug.group_tenantId " +
                "       LEFT JOIN UserAccount u ON ug.user_id = u.id AND ug.user_tenantId = u.tenantId " +
                "       LEFT JOIN Role_Account ra ON g.id = ra.account_id AND g.tenantId = ra.account_tenantId " +
                "       LEFT JOIN Role r ON ra.role_id = r.id AND ra.role_tenantId = r.tenantId " +
                " WHERE g.tenantId = :tenantId " +
                (StringUtils.isNotBlank(searchFieldsClause) ? " AND (" + searchFieldsClause + ")" : "") +
                (StringUtils.isNotBlank(filterClause) ? " AND " + filterClause : "");

        if (StringUtils.isNotBlank(userFilter)) {
            sql += " AND (" + SimpleQueryHelper.buildFilterClause(userFilter, "u") + ") ";
        }
        if (StringUtils.isNotBlank(roleFilter)) {
            sql += " AND (" + SimpleQueryHelper.buildFilterClause(roleFilter, "r") + ") ";
        }
        if (StringUtils.isNotBlank(groupFilter)) {
            sql += " AND (" + SimpleQueryHelper.buildFilterClause(groupFilter, "g") + ") ";
        }

        sql += " GROUP BY g.id, g.tenantId ";

        String countSql = "SELECT COUNT(*) FROM (" + sql + ") x";

        if (StringUtils.isNotBlank(orderBy)) {
            if (orderBy.contains("nbOfMembers")) {
                orderBy = "nbOfMembers";
            } else if (orderBy.contains("nbOfRoles")) {
                orderBy = "nbOfRoles";
            } else if (orderBy.contains("roles")) {
                orderBy = "roles";
            } else if (orderBy.startsWith("GroupAccount.")) {
                orderBy = orderBy.replaceFirst("GroupAccount.", "g.");
            } else if (orderBy.startsWith("UserAccount.")) {
                orderBy = orderBy.replaceFirst("UserAccount.", "u.");
            } else if (orderBy.startsWith("Role.")) {
                orderBy = orderBy.replaceFirst("Role.", "r.");
            }
            sql += " ORDER BY " + orderBy + " " + (StringUtils.equalsIgnoreCase("DESC", orderDir) ? orderDir : "");
        }

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", tenantId);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        Query countQuery = getEntityManager().createNativeQuery(countSql);
        countQuery.setParameter("tenantId", tenantId);

        List<Object[]> resultData = query.getResultList();
        BigInteger total = (BigInteger) countQuery.getSingleResult();
        return new ResultSet<>(resultData, total.intValue());
    }
    
    public Role getACLRole(GroupAccount group) {
    	Set<Role> roles  = group.getRoles();
    	if (roles != null) {
    		for (Role role : roles) {
    			if (role.getId().startsWith("acl-")) {
    				return role;
    			}
    		}    		
    	}
    	return null;
    }

    public Collection<Object[]> membersInfo(PrimaryKey groupPrimaryKey) {
        String sql =
            "SELECT ua.id accountId, e.id employeeId, ua.name " +
            "  FROM User_Group ug " +
            "    LEFT JOIN UserAccount ua ON ua.id = ug.user_id AND ua.tenantId = ug.user_tenantId " +
            "    LEFT JOIN Employee e ON ua.id = e.userAccountId AND ua.tenantId = e.userAccountTenantId " +
            " WHERE ug.group_tenantId = :tenantId AND ug.group_id = :groupId " +
                    "       AND " + QueryPattern.NOT_DELETED.val("e") +
            " ORDER BY ua.name ";

        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("tenantId", groupPrimaryKey.getTenantId());
        query.setParameter("groupId", groupPrimaryKey.getId());

        return query.getResultList();
    }
}
