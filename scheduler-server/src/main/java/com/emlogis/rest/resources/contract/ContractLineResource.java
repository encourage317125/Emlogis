package com.emlogis.rest.resources.contract;

import com.emlogis.common.facade.contract.ContractLineFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.contract.dto.ContractLineCreateDto;
import com.emlogis.model.contract.dto.ContractLineDTO;
import com.emlogis.model.contract.dto.ContractLineUpdateDto;
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
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;

@Path("/contracts/{contractId}/contractlines")
@Authenticated

public class ContractLineResource extends BaseResource{
	
	@EJB
	ContractLineFacade contractLineFacade;
	
    /**
     * Get list of contract lines
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
            Permissions.Employee_View, Permissions.Employee_Mgmt})
    @Audited(label = "Get Contract Lines", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
	public ResultSetDto<ContractLineDTO> getObjects(@PathParam("contractId") final String contractId,
			@QueryParam("select") String select,
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		String tenantId = getTenantId();
        		
        return contractLineFacade.getObjects(tenantId, contractId, select, filter, offset, limit, orderBy, orderDir);
    }	
	
	@GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
            Permissions.Employee_View, Permissions.Availability_RequestMgmt,Permissions.Availability_Request})
    @Audited(label = "Get Contract Line", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ContractLineDTO getObject(@PathParam("id") final String id) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return contractLineFacade.getObject(primaryKey);
    }
	
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
            Permissions.Employee_View, Permissions.Employee_Mgmt})
    @Audited(label = "Create Contract Line", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
	public ContractLineDTO createObject(
            @PathParam("contractId") final String contractId,
            ContractLineCreateDto contractLineCreateDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
		PrimaryKey primaryKey;
        primaryKey = createUniquePrimaryKey();
        
        contractLineCreateDto.setContractId(contractId);
        
        return contractLineFacade.createObject(primaryKey, contractLineCreateDto);
	}
	
	@PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
            Permissions.Employee_View, Permissions.Employee_Mgmt})
    @Audited(label = "Update Contract Line", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ContractLineDTO updateObject(@PathParam("id") final String id, ContractLineUpdateDto contractLineUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        contractLineUpdateDto.setContractId(id);
        return contractLineFacade.updateObject(primaryKey, contractLineUpdateDto);
    }
	
	@DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt, Permissions.OrganizationProfile_View,
            Permissions.Employee_View})
    @Audited(label = "Delete Contract Line", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteObject(@PathParam("contractId") final String contractId, @PathParam("id") final String id) {
        PrimaryKey primaryKey = createPrimaryKey(id);
        contractLineFacade.deleteObject(primaryKey, contractId);
		return Response.ok().build();
    }
}
