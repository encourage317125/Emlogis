package com.emlogis.common.security;

import com.emlogis.model.ACEProtectedEntity;

import java.security.AccessControlException;
import java.util.List;


public class ACLUtil {
	
	/**
	 * enrich a persistent object with permissions as defined by ACLs 
	 * @param entity
	 * @param acls
	 * if on return the entity.permissions is null, that means that no pattern/ACL matched the entity
	 */
	public static void setPermissions(ACLProtected entity, AccountACL acls) {

		if (acls == null) {
			return;							// ACL not activated
		}
		String entityClassName = entity.getClass().getSimpleName();
		String path = entity.getPath();
		entity.setPermissions(null);		// set entity permissions to null to indicate no ACL match
		List<AccountACE> entityacls = acls.getEntityAcls(entityClassName).getAcl();
		for( AccountACE acl : entityacls) {
			String pattern = acl.getPattern();
			if (path.matches(pattern)) {
				entity.initPermissions();	// make entity permissions NOT null to indicate at least one ACL matches
				for (Permissions perm : acl.getPermissions() ) {
					entity.addPermission(perm);
				}			
			}
		}
	}
	

	/**
	 * check an entity matches access specified by acls (if specified)
	 * bypass test if acl is null (meaning ACL is not activated)
	 * 
	 * IMPORTANT: call this method AFTER setPermissions() has been invoked on the entity
	 * 
	 * throw an exception if access denied
	 * @param entity
	 * @param acls
	 */

	public static void checkAccess(ACEProtectedEntity entity, AccountACL acls) throws AccessControlException{
		
		if ((acls != null) && (entity.getPermissions() == null)) {
			throw new AccessControlException("access to entity " + entity.getClName() + ":" + entity.getId() + " is not permitted by ACLs");
		}
	}

	/**
	 * check an entity has at least one of the permissions specified in parameter
	 * throw an exception if access denied
	 * @param entity
	 * @param acls
	 */
/*
	public static void checkPermissions(ACLProtectedEntity entity, AccountACLList acls, Permissions ... permissions) throws AccessControlException{
		
		if (acls == null) {		
			return;				// ACL not activated
		}
		if (entity.getPermissions() != null) {
			for (Permissions perm : permissions) {
				if (entity.hasPermission(perm)) {
					return;
				}
			}
		}
		throw new AccessControlException("access to entity " + entity.getClName() + ":" + entity.getId() + " is not permitted by ACLs, because of missing Permission(s)");
	}
*/	
	/**
	 * check an entity has at ALL of the permissions specified in parameter
	 * throw an exception if access denied
	 * @param entity
	 * @param acls
	 */
/*
	public static void checkAllPermissions(ACLProtectedEntity entity, AccountACLList acls, Permissions ... permissions) throws AccessControlException{
		
		if (acls == null) {		
			return;				// ACL not activated
		}
		if (entity.getPermissions() == null) {
			throw new AccessControlException("access to entity " + entity.getClName() + ":" + entity.getId() + " is not permitted as no ACL grant access to this entity");
		}
		for (Permissions perm : permissions) {
			if (! entity.hasPermission(perm)) {
				throw new AccessControlException("access to entity " + entity.getClName() + ":" + entity.getId() + " is not permitted by ACL, because of missing Permission(s):" + perm.getValue());
			}
		}
	}
*/	

}
