package com.emlogis.common.facade.tenant;

import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.security.PermissionScope;
import com.emlogis.common.services.tenant.PermissionService;
import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.common.validation.annotations.Validate;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.tenant.Permission;
import com.emlogis.model.tenant.ServiceProvider;
import com.emlogis.model.tenant.Tenant;
import com.emlogis.model.tenant.dto.PermissionDto;

import javax.ejb.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class PermissionFacade extends BaseFacade {

    @EJB
    private PermissionService permissionService;
    
    @EJB
    private TenantService tenantService;

    /**
     * getPermissions() return the list of all possible permissions based on Tenant type (ServiceProvider | Customer)
     * 
     * @param tenantId
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public Collection<PermissionDto> getPermissions(
    		@Validate(validator = EntityExistValidatorBean.class, type = Tenant.class) String tenantId)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Tenant tenant = tenantService.getTenant(tenantId);
        boolean isServiceProvider = tenant instanceof ServiceProvider;

        List<Permission> allPermissions = permissionService.findAll();
        List<Permission> permissions = new ArrayList<>();
        for (Permission perm : allPermissions) {
        	PermissionScope permScope = perm.getScope();
        	if (isServiceProvider) {
        		if (permScope == PermissionScope.All || permScope == PermissionScope.ServiceProvider) {
        			permissions.add(perm);
        		}
        	} else {
        		if (permScope == PermissionScope.All || permScope == PermissionScope.Customer) {
        			permissions.add(perm);
        		}
    		}
    	}
        return toCollectionDto(permissions, PermissionDto.class);
    }

}
