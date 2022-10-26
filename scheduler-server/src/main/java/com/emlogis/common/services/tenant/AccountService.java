package com.emlogis.common.services.tenant;

import com.emlogis.common.Constants;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.tenant.Account;
import com.emlogis.model.tenant.Role;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class AccountService<T extends Account> {

	private final Logger logger = LoggerFactory.getLogger(AccountService.class);
	
	@PersistenceContext(unitName=Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    @EJB
    private RoleService roleService;

    @EJB
    private ACEService aclService;
    
    public EntityManager getEntityManager() {
        return entityManager;
    }
    
	/**
	 * Get acccount 
	 * @param primaryKey
	 * @return
	 */
	public Account getAccount(PrimaryKey primaryKey) {
		return entityManager.find(Account.class, primaryKey);
	}

    public void insert(T account) {
        entityManager.persist(account);
    }

    public T update(T account) {
        account.touch();
        return entityManager.merge(account);
    }

    public void delete(T account) {
    	// disconnect from Roles 
    	// delete ACLs
    	account.removeAllRoles();
        entityManager.remove(account);
    }

    public ResultSet<Role> findAccountRoles(PrimaryKey primaryKey, SimpleQuery simpleQuery) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            InstantiationException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetAssociatedWithPaging(getEntityManager(), simpleQuery, primaryKey, Account.class,
                "roles");
    }

    public ResultSet<Role> getUnassociatedRoles(PrimaryKey primaryKey, SimpleQuery simpleQuery)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException, InstantiationException {
        SimpleQueryHelper queryHelper = new SimpleQueryHelper();
        return queryHelper.executeGetUnassociatedWithPaging(getEntityManager(), simpleQuery, primaryKey, Account.class,
                "roles");
    }

    public void addRole(Account account, Role role) {
    	Collection<Role> roles = account.getRoles();
    	logger.debug("Adding Role: " + role.toString() + " to " + account.toString());
        roles.add(role);
        account.touch();        
        entityManager.persist(role);
        entityManager.persist(account);
    }

    public void removeRole(Account account, Role role) {
    	Collection<Role> roles = account.getRoles();
    	logger.debug("Adding Role: " + role.toString() + " to " + account.toString());
        roles.remove(role);
        account.touch();        
        entityManager.persist(account);
    }

	public RoleService getRoleService() {
		return roleService;
	}

	public ACEService getAclService() {
		return aclService;
	}

}
