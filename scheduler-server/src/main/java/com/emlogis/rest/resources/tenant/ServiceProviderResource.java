package com.emlogis.rest.resources.tenant;

import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.tenant.ServiceProviderFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.employee.dto.EmployeeRememberMeDto;
import com.emlogis.model.tenant.dto.*;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;

import javax.ejb.EJB;
import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

/**
 * Resource for Customer Administration.
 * allows listing/viewing/updating/creating/deleting customer accounts.
 *
 * Access should  be restricted to EmLogis personel only
 * -----------------------------------------------------
 * @author EmLogis
 *
 */

@Path("/serviceproviders")
@Authenticated
public class ServiceProviderResource extends TenantResource {

    @EJB
    private ServiceProviderFacade serviceProviderFacade;
    

    // -----------------------------------------------------------------------
    // Service provider APIs (APIs that act upon ServiceProvider entities)
    // -----------------------------------------------------------------------


	/**
	 * Read a ServiceProvider
	 * @return Response
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IOException 
	 */
	@GET
	@Path("{spId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_View})
	@Audited(label = "Get ServiceProvider Info", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
	public ServiceProviderDto getServiceProvider(@PathParam("spId") final String spId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            ValidationException, IllegalArgumentException, IOException {

		return serviceProviderFacade.getServiceProvider(spId);     
	}
	
    /**
	 * Update a ServiceProvider
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws Exception
	 */
    
	@PUT
	@Path("{spId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_Mgmt , Permissions.OrganizationProfile_Mgmt})
	@Audited(label = "Update Customer Info", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
	public ServiceProviderDto updateObject(@PathParam("spId") final String spId, ServiceProviderUpdateDto spUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

		return serviceProviderFacade.updateServiceProvider(spId, spUpdateDto);
	}

    // -----------------------------------------------------------------------
    // Organization Management APIs 
    // (ServiceProvider APIs that act upon Customers entities)
    // -----------------------------------------------------------------------
    
	/**
	 * Read list of Organizations
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws Exception
	 */
	@Path("{spId}/orgs/")
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_View})
	@Audited(label = "List Customers", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
	public ResultSetDto<OrganizationDto> getObjects (
				@PathParam("spId") final String spId,
                @QueryParam("select") String select,		// NOT IMPLEMENTED FOR NOW ..
                @QueryParam("filter") String filter,
                @QueryParam("offset") @DefaultValue("0") int offset,
                @QueryParam("limit") @DefaultValue("20") int limit,
                @QueryParam("orderby") String orderBy,
                @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException{
		
		// TODO make the query relative to service provider.
		// for now, do it globally
        return serviceProviderFacade.getOrganizations(select, filter, offset, limit, orderBy, orderDir);
	}

	/**
	 * Creates an organization
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ValidationException
	 * @throws IOException 
	 */
	@Path("{spId}/orgs/")
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_Mgmt})
	@Audited(label = "Create Customer", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
	public OrganizationDto createObject(@PathParam("spId") final String spId, OrganizationCreateDto orgCreateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            ValidationException, IllegalArgumentException, IOException  {
		return serviceProviderFacade.createOrganization(spId, orgCreateDto);
	}

	/**
	 * Read an organization
	 * @return Response
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IOException 
	 */
	@GET
	@Path("{spId}/orgs/{orgId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_View})
	@Audited(label = "Get Customer Info(SP)", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
	public OrganizationDto getOrganization(@PathParam("spId") final String spId, @PathParam("orgId") final String orgId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            ValidationException, IllegalArgumentException, IOException {
		return serviceProviderFacade.getOrganization(orgId);
	}

    /**
	 * Update an  Organization
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws Exception
	 */
    
	@PUT
	@Path("{spId}/orgs/{orgId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_Mgmt , Permissions.OrganizationProfile_Mgmt})
	@Audited(label = "Update Customer Info(SP)", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
	public OrganizationDto updateOrganization(@PathParam("orgId") final String orgId,
                                              OrganizationUpdateDto orgUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		// TODO make the delete relative to service provider.
		// for now, do it globally
		return serviceProviderFacade.updateOrganization(orgId, orgUpdateDto);
	}

	/**
	 * Delete an organization
	 * @return Response
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IOException 
	 */
	@DELETE
	@Path("{spId}/orgs/{orgId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_Mgmt})
	@Audited(label = "Delete Customer", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
	public Response deleteObject(@PathParam("spId") final String spId, @PathParam("orgId") final String orgId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            ValidationException, IllegalArgumentException, IOException {
        serviceProviderFacade.deleteOrganization(orgId);
		return Response.ok().build();
	}

    @GET
    @Path("{spId}/orgs/ops/quicksearch")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_View})
    @Audited(label = "Organizations quick search", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<Object> quickSearch(
            @PathParam("spId") String spId,
            @QueryParam("search") String searchValue,
            @QueryParam("searchfields") String searchFields,
            @QueryParam("returnedfields") String returnedFields,
            @QueryParam("limit") @DefaultValue("-1") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        // TODO serviceProviderId
        return serviceProviderFacade.quickSearch(searchValue, searchFields, returnedFields, limit, orderBy, orderDir);
    }

    @GET
    @Path("{spId}/orgs/ops/query")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_View})
    @Audited(label = "Organization query", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<OrganizationQueryDto> query(
            @PathParam("spId") String spId,
            @QueryParam("search") String searchValue,
            @QueryParam("searchfields") String searchFields,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        // TODO serviceProviderId
        return serviceProviderFacade.query(searchValue, searchFields, offset, limit, orderBy, orderDir);
    }

	@POST
	@Path("{id}/ops/getpasswordcomplianceviolations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
	@Audited(label = "Get Password Policies Report", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
	public PasswordComplianceReportDto getPasswordComplianceViolations(@PathParam("id") final String id,
			PasswordPoliciesDto passwordPoliciesDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, ValidationException, IllegalArgumentException,
			IOException {
		return getTenantFacade().getPasswordComplianceViolations(id, passwordPoliciesDto);
	}

    @POST
    @Path("/ops/setpasswordcomplianceuserstate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
    @Audited(label = "Get Password Policies Report", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response setPasswordComplianceUserState(@QueryParam("chunk") @DefaultValue("50") int chunk) {
        getTenantFacade().setPasswordComplianceUserState(chunk);
        return Response.ok().build();
    }

    @POST
    @Path("{spId}/orgs/{orgId}/ops/resetlogins")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_Mgmt})
    @Audited(label = "Organization Reset All logins", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response resetAllLogins(@PathParam("spId") String spId, @PathParam("orgId") String orgId, @QueryParam("obfuscate") @DefaultValue("true") boolean obfuscate)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        serviceProviderFacade.resetAllLogins(orgId, obfuscate);
        return Response.ok().build();
    }

    @POST
    @Path("{spId}/orgs/{orgId}/ops/migrateemployees")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_Mgmt})
    @Audited(label = "Organization Migrate Employees", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response migrateEmployees(@PathParam("spId") String spId, @PathParam("orgId") String orgId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        serviceProviderFacade.migrateEmployees(orgId);
        return Response.ok().build();
    }
    
    @POST
    @Path("{spId}/ops/hashallpasswords")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_Mgmt})
    @Audited(label = "Hash All Passwords", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response migrateEmployees(@PathParam("spId") String spId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        serviceProviderFacade.hashAllPasswords();
        return Response.ok().build();
    }

    @GET
    @Path("{spId}/remembermesessions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt,
            Permissions.Tenant_View})
    @Audited(label = "Get RememberMe sessions", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<EmployeeRememberMeDto> getRememberMeObjects(
            @PathParam("spId") String spId,
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return getOrganizationFacade().getAllRememberMeObjects(filter, offset, limit, orderBy, orderDir);
    }

    @GET
    @Path("summary")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Get Counters", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Map<String, Integer> getCounters() {
        return serviceProviderFacade.getCounters(getTenantId());
    }

}
