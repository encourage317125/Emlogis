package com.emlogis.common.facade.structurelevel;

import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.security.ACLProtected;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.security.PermissionCheck;
import com.emlogis.common.security.Permissions;
import com.emlogis.common.services.structurelevel.StructureLevelService;
import com.emlogis.common.services.structurelevel.TraversalDirection;
import com.emlogis.common.services.tenant.ACEService;
import com.emlogis.common.validation.annotations.Validate;
import com.emlogis.common.validation.annotations.Validation;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.ACEProtectedEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.StructureLevel;
import com.emlogis.model.structurelevel.dto.StructureLevelDto;

import javax.ejb.EJB;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlException;
import java.util.Collection;

abstract public class StructureLevelFacade extends BaseFacade {

    @EJB
	protected ACEService aceService;

    abstract protected StructureLevelService getStructureLevelService();

    /*
    @SuppressWarnings("unchecked")
    public Collection<RoleDto> findAccountRoles(PrimaryKey accountPrimaryKey) throws Exception {
        List<Role> roles = getAccountService().findAccountRoles(accountPrimaryKey);

        return toCollectionDto(roles, RoleDto.class);
    }

    public void addRole(PrimaryKey accountPrimaryKey, String roleId) {
        getAccountService().addRole(accountPrimaryKey, roleId);
    }

    public void removeRole(PrimaryKey accountPrimaryKey, String roleId) {
        getAccountService().removeRole(accountPrimaryKey, roleId);
    }
    */
    

	/**
	 * check an entity has at least one of the permissions specified in parameter
	 * throw an exception if access denied
     * @param primaryKey
     * @param acl
     * @param checkType
     * @param permissions
     * @return
     * @throws AccessControlException
     */
    protected ACLProtected checkAcl(PrimaryKey primaryKey, AccountACL acl, PermissionCheck checkType,  Permissions ... permissions) throws AccessControlException{
    	return aceService.checkAcl(primaryKey, acl, checkType, permissions);
	}
    
    /**
     * checkAcl() check if there is an ACE that allows entity access with ANY of the permissions specified. 
     * @param entity
     * @param acl
     * @param checkType 
     * @param permissions
     * 
     */
    protected ACLProtected checkAcl(ACEProtectedEntity entity, AccountACL acl, PermissionCheck checkType, Permissions ...permissions) {
    	
    	if (acl == null) {
    		return entity;
    	}
    	return aceService.checkPermissions(entity, acl, checkType, permissions);
    }

    /**
     * checkAnyAcl() check if there is an ACE that allows entity access with ANY of the permissions specified. 
     * @param entity
     * @param acl
     * @param checkType 
     * @param permissions
     * 
     */
/*    SEE ACEService
    private void checkAnyAcl(ACEProtectedEntity entity, AccountACL acl, Permissions ...permissions) {
    	
    	
    	String path = entity.getPath();
    	List<AccountACE> aces = acl.getAcl();
		System.out.println("Checking ACL for:" + entity.getClName() + ":" + entity.getId() + " path=" + path);
    	for (AccountACE ace : aces) {
    		System.out.println("Checking ACE for:" + ace.getEntityClass() + ":" + ace.getPattern());
    		if (StringUtils.equals(entity.getClName(), ace.getEntityClass()) ) {
    			if (path.matches(ace.getPattern())) {
    	    		System.out.println("Pattern match for:" + ace.getPattern());

    				// we found an ACE that matches the entity, 
    				// now check permissions 
    				if (permissions == null) {
	    				System.out.println("No required permission specified, => MATCH !");
    					return; // no permission specified => OK (note that read permission is implicit)
    				}
   					// check we have at list one permission
    				Set<Permissions> acePerms = ace.getPermissions();
    				for (Permissions perm : permissions) {
    					if (acePerms.contains(perm)) {
    	    				System.out.println("Permission MATCH ! on " + perm.toString());
    						return;
    					}
    				}
    				System.out.println("No permission match");
    			}
    		}
    	}
    	throw new AccessControlException("ACL do not allow access to object: " + entity.getClName() + ":" + entity.getId());       	
    }
*/
    /**
     * checkAllAcl() check if there is an ACE that allows entity access with ALL of the permissions specified. 
     * @param entity
     * @param acl
     * @param checkType 
     * @param permissions
     * 
     */
/*    SEE ACEService
    private void checkAllAcl(ACEProtectedEntity entity, AccountACL acl, Permissions ...permissions) {
    	
    	throw new RuntimeException("checkAllAcl() is not supported yet. Can't check access to object: " + entity.getClName() + ":" + entity.getId());       	
    }
*/
    /**
     * return the list of objects associated via a specific relationship type to a 'seed' structurelevel instance.
     * dir allows to get related objects that are either source (pointing to the seed), either destination of the relationship
     * (both can be easily be implemented too, but waiting for a use case)
     * @param seed
     * 
     * @param relationshipType
     * @param dir
     * @return
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    @Validation
    public	Collection<StructureLevelDto> getAssociatedObjects(
                @Validate(validator = EntityExistValidatorBean.class, type = Site.class)
                PrimaryKey seedStructureLevelPK,
                String relationshipType, TraversalDirection dir)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	StructureLevel seed = getStructureLevelService().getStructureLevel(seedStructureLevelPK);
    	// TODO check with Metamodel that relationshipType is valid + authorized from/to Seed object
    	Collection<StructureLevel> l = getStructureLevelService().getAssociatedObjects(seed, relationshipType, dir);
        return this.toCollectionDto(l, StructureLevelDto.class);
    }

}
