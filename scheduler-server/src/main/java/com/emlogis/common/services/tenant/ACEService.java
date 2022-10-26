package com.emlogis.common.services.tenant;

import com.emlogis.common.Constants;
import com.emlogis.common.security.*;
import com.emlogis.model.ACE;
import com.emlogis.model.ACEProtectedEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.tenant.Account;
import com.emlogis.model.tenant.GroupAccount;
import com.emlogis.model.tenant.Role;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.rest.resources.util.QueryPattern;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ACEService {

	@PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;
    
	public EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * findAcl() find a list of ACEs matching criteria;
	 * @param simpleQuery
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public ResultSet<ACE> findAcl(SimpleQuery simpleQuery) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException{
		simpleQuery.setEntityClass(ACE.class);
		SimpleQueryHelper sqh = new SimpleQueryHelper();
		return sqh.executeSimpleQueryWithPaging(entityManager, simpleQuery);
	}
	
	/**
	 * Get ace 
	 * @param primaryKey
	 * @return
	 */
	public ACE getAce(PrimaryKey primaryKey) {
		return entityManager.find(ACE.class, primaryKey);
	}

	/**
	 * Create ace
	 * @param pk
	 * @param description
	 * @return
	 */
    public ACE createAce(PrimaryKey pk, Class targetClass, String pattern, Set<Permissions> permissions,
                         String description) {
		ACE ace = new ACE(pk, targetClass, pattern, permissions, description);
		entityManager.persist(ace);
		return ace;
	}

    /**
     * Update ace
     * @param ace
     * @return
     */
    public ACE update(ACE ace) {
        ace.touch();
        return entityManager.merge(ace);
    }
     
    /**
     * Delete ace
     * @param ace
     */
    public void delete(ACE ace) {
        entityManager.remove(ace);
    }

	public void deleteAcl(Set<ACE> acl) {
		for (ACE ace: acl) {
	        entityManager.remove(ace);			
		}
	}
	
	/**
	 * check an entity has at least one of the permissions specified in parameter
	 * throw an exception if access denied
	 */
	public ACLProtected checkAcl(PrimaryKey primaryKey, AccountACL acl, PermissionCheck checkType,
                                 Permissions... permissions) throws AccessControlException{
		ACEProtectedEntity entity = getEntityManager().find(ACEProtectedEntity.class, primaryKey);
		if (acl == null) {		
			return entity;				// ACL not activated
		}
		return checkPermissions(entity, acl, checkType, permissions);
	}

	/**
	 * check an entity has at least one of the permissions specified in parameter
	 * throw an exception if access denied
	 */
	public ACLProtected checkPermissions(ACEProtectedEntity entity, AccountACL acl, PermissionCheck checkType,
                                         Permissions... permissions) throws AccessControlException {
		if (acl == null) {
			return entity;				// ACL not activated
		}
		if (entity == null) {
			throw new AccessControlException("access to entity cannot be controlled, because not found or null");
		}
		ACLUtil.setPermissions(entity, acl);
		ACLUtil.checkAccess(entity, acl);
    	if (checkType == PermissionCheck.ANY) {
    		return checkPermissions(entity, permissions);
    	} else {
    		return checkAllPermissions(entity, permissions);
    	}      
	}

	private ACLProtected checkPermissions(ACEProtectedEntity entity, Permissions[] permissions) {
		if (permissions == null) {
			return entity;		// no permission to check
		}
		if (entity.getPermissions() != null) {
			for (Permissions perm : permissions) {
				if (entity.hasPermission(perm)) {
					return entity;
				}
			}
		}
		throw new AccessControlException("access to entity " + entity.getClName() + ":" + entity.getId()
                + " is not permitted by ACLs, because of missing Permission(s)");
	}

	private ACLProtected checkAllPermissions(ACEProtectedEntity entity, Permissions[] permissions) {
		if (entity.getPermissions() == null) {
			throw new AccessControlException("access to entity " + entity.getClName() + ":" + entity.getId()
                    + " is not permitted as no ACL grant access to this entity");
		}
		for (Permissions perm : permissions) {
			if (! entity.hasPermission(perm)) {
				throw new AccessControlException("access to entity " + entity.getClName() + ":" + entity.getId()
                        + " is not permitted by ACL, because of missing Permission(s):" + perm.getValue());
			}
		}
		return entity;
	}

	public ResultSet<ACE> getAcl(PrimaryKey rolePrimaryKey, SimpleQuery simpleQuery) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetAssociatedWithPaging(entityManager, simpleQuery, rolePrimaryKey, Role.class, "acl");
    }

    public Collection<Role> findRoles(PrimaryKey acePrimaryKey, SimpleQuery simpleQuery) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetAssociated(entityManager, simpleQuery, acePrimaryKey, ACE.class, "roles");
    }

    public Collection<UserAccount> findUserAccounts(PrimaryKey acePrimaryKey)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        ACE ace = entityManager.find(ACE.class, acePrimaryKey);
        Set<Role> aceRoles = ace.getRoles();

        Set<UserAccount> userAccounts = new HashSet<>();
        for (Role role : aceRoles) {
            Set<Account> accounts = role.getAccounts();
            for (Account account : accounts) {
                if (account instanceof UserAccount) {
                    userAccounts.add((UserAccount) account);
                } else if (account instanceof GroupAccount) {
                    userAccounts.addAll(((GroupAccount) account).getMembers());
                }
            }
        }
        return userAccounts;
    }

    @SuppressWarnings("unchecked")
    public Collection<Site> getMatchedSites(ACE ace) {
        Query query = entityManager.createNativeQuery(
                "SELECT * FROM Site s WHERE s.path REGEXP :pattern AND s.tenantId = :tenantId AND "
                        + QueryPattern.NOT_DELETED.val("s"), Site.class);
        query.setParameter("pattern", ace.getPattern());
        query.setParameter("tenantId", ace.getTenantId());

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public Collection<Team> getMatchedTeams(ACE ace) {
        Query query = entityManager.createNativeQuery(
                "SELECT * FROM Team t " +
                        "WHERE t.path REGEXP :pattern AND t.tenantId = :tenantId "
                        + " AND " + QueryPattern.NOT_DELETED.val("t"), Team.class);
        query.setParameter("pattern", ace.getPattern());
        query.setParameter("tenantId", ace.getTenantId());

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<ACE> getUnlinkedACEs() {
        final String sql = "SELECT a.* FROM ACE a WHERE a.id NOT IN (SELECT ra.ace_id FROM Role_ACE ra)";
        Query query = entityManager.createNativeQuery(sql, ACE.class);
        return query.getResultList();
    }

}

