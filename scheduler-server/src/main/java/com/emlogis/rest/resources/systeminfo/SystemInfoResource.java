package com.emlogis.rest.resources.systeminfo;

import com.emlogis.common.facade.systeminfo.SystemInfoFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.dto.systeminfo.HazelcastInfoDto;
import com.emlogis.model.dto.systeminfo.NotificationInfoDto;
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
import java.util.List;
import java.util.Map;

@Path("/systeminfo")
@Authenticated
public class SystemInfoResource extends BaseResource {

    @EJB
    private SystemInfoFacade systemInfoFacade;

    /**
     * Get summary information (counts of records) from database
     */
    @GET
    @Path("dbsummary")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_View, Permissions.Tenant_Mgmt})
    @Audited(label = "Database Summary", callCategory = ApiCallCategory.Unclassified)
    @Interceptors(AuditingInterceptor.class)
    public Map getDbSummary() {
        return systemInfoFacade.dbSummary();
    }

    /**
     * Get counts of records from database for all customers
     */
    @GET
    @Path("dbpercustomer")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_View, Permissions.Tenant_Mgmt})
    @Audited(label = "Database Per Customer", callCategory = ApiCallCategory.Unclassified)
    @Interceptors(AuditingInterceptor.class)
    public List dbPerCustomer() {
        return systemInfoFacade.dbPerCustomer();
    }

    /**
     * Get sizes of hazelcast's maps/queues
     */
    @GET
    @Path("hzinfo")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_View, Permissions.Tenant_Mgmt})
    @Audited(label = "Hazelcast Info", callCategory = ApiCallCategory.Unclassified)
    @Interceptors(AuditingInterceptor.class)
    public HazelcastInfoDto hzInfo() {
        return systemInfoFacade.hzInfo();
    }

    /**
     * Get notification info
     */
    @GET
    @Path("notificationinfo")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_View, Permissions.Tenant_Mgmt})
    @Audited(label = "Notification Info", callCategory = ApiCallCategory.Unclassified)
    @Interceptors(AuditingInterceptor.class)
    public NotificationInfoDto notificationInfo() {
        return systemInfoFacade.notificationInfo();
    }
}
