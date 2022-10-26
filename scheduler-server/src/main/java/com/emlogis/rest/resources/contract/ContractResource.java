package com.emlogis.rest.resources.contract;

import com.emlogis.common.facade.contract.ContractFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.contract.dto.ContractDTO;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;

import javax.ejb.EJB;
import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.InvocationTargetException;

/** Resource for Contracts
 * 
 * @author rjackson
 *
 */

@Path("/contracts")
@Authenticated

public class ContractResource extends BaseResource {
	@EJB
	ContractFacade contractFacade;
	
    /**
     * Get list of contracts
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View, Permissions.Employee_View, Permissions.Employee_Mgmt, Permissions.Availability_RequestMgmt,Permissions.Availability_Request})
    @Audited(label = "Get Contracts", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
	public ResultSetDto<ContractDTO> getObjects(
			@QueryParam("select") String select,
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		
		String tenantId = getTenantId();
        		
        return contractFacade.getObjects(tenantId, select, filter, offset, limit, orderBy, orderDir);
    }
	
    
    /**
     * Get specified a Contract
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View, Permissions.Employee_View, Permissions.Availability_RequestMgmt,Permissions.Availability_Request})
    @Audited(label = "Get Contract", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ContractDTO getObject(@PathParam("id") final String id) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	
        PrimaryKey primaryKey = createPrimaryKey(id);
        return contractFacade.getObject(primaryKey);
    }


}
