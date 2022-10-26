package com.emlogis.common.services.tenant;

import com.emlogis.common.Constants;
import com.emlogis.common.security.PermissionScope;
import com.emlogis.common.security.PermissionType;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.tenant.Permission;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class PermissionService {

	@PersistenceContext(unitName=Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<Permission> findAll() {
        Query query = entityManager.createQuery("SELECT perms FROM Permission perms ");
        return (List<Permission>) query.getResultList();
    }
        
	/**
	 * getPermissionByKey() return a permission object specified by permission type
	 * @param permission
	 * @return
	 */
	public Permission getPermissionByKey(Permissions permission) {
		return entityManager.find(Permission.class, permission);
	}  
	
    public Permission createPermission(Permissions permission, PermissionType type, PermissionScope scope,
                                       String description) {
    	Permission perm = new Permission(permission, type, scope, description);
    	entityManager.persist( perm);
    	return perm;
    }

    @SuppressWarnings("unchecked")
    public Collection<Permission> getPermissionList(Collection<Permissions> keys) {
        Query query = entityManager.createQuery("SELECT perms FROM Permission perms WHERE perms.id IN (:keys)");
        query.setParameter("keys", keys);
        return (List<Permission>) query.getResultList();
    }

}
