package com.emlogis.rest.resources.tenant;

import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.tenant.OrganizationFacade;
import com.emlogis.common.facade.tenant.TenantFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.tenant.dto.PasswordComplianceReportDto;
import com.emlogis.model.tenant.dto.PasswordPoliciesDto;
import com.emlogis.model.tenant.dto.PasswordPoliciesUpdateDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;
import com.emlogis.server.services.StartupService;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


/**
 * Base Resource for Customer & ServiceProvider Administration.
 * @author EmLogis
 *
 */
@Authenticated
public abstract class TenantResource extends BaseResource {

    @EJB
    private TenantFacade tenantFacade;

    @EJB
    private OrganizationFacade organizationFacade;

    @Inject
    private StartupService startupService;
	
	public TenantFacade getTenantFacade() {
		return tenantFacade;
	}

	public OrganizationFacade getOrganizationFacade() {
		return organizationFacade;
	}

	public StartupService getStartupService() {
		return startupService;
	}

	/**
	 * Sets the password policies of the current organization
	 * @param id
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws ValidationException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@GET
	@Path("/ops/getpasswordpolicies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
	@Audited(label = "Get Password Policies", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
	public PasswordPoliciesDto getPasswordPolicies(@PathParam("id") final String id) throws InstantiationException,
			IllegalAccessException, NoSuchMethodException, InvocationTargetException, ValidationException,
			IllegalArgumentException, IOException {
		return getTenantFacade().getPasswordPolicies(getTenantId());
	}

	/**
	 * Updates the password policies of the current organization
	 * @param id
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws ValidationException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@PUT
	@Path("/ops/setpasswordpolicies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_Mgmt})
	@Audited(label = "Set Password Policies", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
	public PasswordComplianceReportDto setPasswordPolicies(@PathParam("id") final String id,
			PasswordPoliciesUpdateDto updateDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, ValidationException, IllegalArgumentException,
			IOException {
		return getTenantFacade().setPasswordPolicies(getTenantId(), updateDto);
	}

	/**
	 * Get a report of account compliance with  password strength policies for current tenant:
	 *
	 * an optional PasswordPoliciesUpdateDto can be provided.
	 * If specified, the report will be run against this provided set of policies
	 * If unspecified, the report will be run against the policies currently defined for the organization
	 *
	 * @param passwordPoliciesDto (optional)
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws ValidationException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@POST
	@Path("/ops/getpasswordcomplianceviolations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Account_View, Permissions.Account_Mgmt})
	@Audited(label = "Get Password Policies Report", callCategory = ApiCallCategory.AccountManagement)
    @Interceptors(AuditingInterceptor.class)
	public PasswordComplianceReportDto getPasswordComplianceViolations(PasswordPoliciesDto passwordPoliciesDto)
			throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
			ValidationException, IllegalArgumentException, IOException {
		return getTenantFacade().getPasswordComplianceViolations(getTenantId(), passwordPoliciesDto);
	}

}
