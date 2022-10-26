package com.emlogis.rest.resources.tenant;

import com.emlogis.common.facade.tenant.PermissionFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.tenant.dto.PermissionDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;

import javax.ejb.EJB;
import javax.interceptor.Interceptors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

@Path("permissions")
@Authenticated
public class PermissionResource extends BaseResource {

    @EJB
    private PermissionFacade permissionFacade;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "List-permissions", callCategory = ApiCallCategory.AccountManagement)
    @RequirePermissionIn(permissions = {Permissions.Role_View, Permissions.Role_Mgmt})
    @Interceptors(AuditingInterceptor.class)
    public Collection<PermissionDto> permissions() throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        return permissionFacade.getPermissions(getTenantId());
    }

}
