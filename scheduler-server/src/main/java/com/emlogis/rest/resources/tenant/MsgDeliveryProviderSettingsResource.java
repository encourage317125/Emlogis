package com.emlogis.rest.resources.tenant;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.ejb.EJB;
import javax.interceptor.Interceptors;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.emlogis.common.facade.notification.MsgDeliveryProviderSettingsFacade;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.notification.dto.MsgDeliveryProviderSettingsCreateDto;
import com.emlogis.model.notification.dto.MsgDeliveryProviderSettingsDto;
import com.emlogis.model.notification.dto.MsgDeliveryProviderSettingsUpdateDto;
import com.emlogis.model.structurelevel.dto.TeamDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;


/**
 * Resource for General System & Customer Administration.
 * allows managing Notification providers.
 *
 * Access should  be restricted to EmLogis personel only
 * -----------------------------------------------------
 * @author EmLogis
 *
 */

@Path("/msgdeliveryproviders")
@Authenticated
public class MsgDeliveryProviderSettingsResource extends BaseResource {

    @EJB
    private MsgDeliveryProviderSettingsFacade msgDeliveryProviderSettingsFacade;

	/**
	 * Read all MsgDeliveryProviders
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws Exception
	 */
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_View, Permissions.Tenant_Mgmt})
	@Audited(label = "List Notification Delivery Providers", callCategory = ApiCallCategory.SystemManagement)
    @Interceptors(AuditingInterceptor.class)
	public ResultSetDto<MsgDeliveryProviderSettingsDto> getObjects (
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		
        return msgDeliveryProviderSettingsFacade.getObjects(select, filter, offset, limit, orderBy, orderDir);
	}


    /**
     * Creates a Message Delivery Provider
     * @return
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_Mgmt})
	@Audited(label = "Create Notification Delivery Provider", callCategory = ApiCallCategory.SystemManagement)
    @Interceptors(AuditingInterceptor.class)
    public MsgDeliveryProviderSettingsDto createObject(MsgDeliveryProviderSettingsCreateDto createDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	
        return msgDeliveryProviderSettingsFacade.createObject(createDto);
    }


    /**
     * Read a Message Delivery Provider
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_View, Permissions.Tenant_Mgmt})
	@Audited(label = "Read Notification Delivery Provider", callCategory = ApiCallCategory.SystemManagement)
    @Interceptors(AuditingInterceptor.class)
    public MsgDeliveryProviderSettingsDto getObject(@PathParam("id") final String id) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	
        return msgDeliveryProviderSettingsFacade.getObject(id);
    }

	/**
     * Update a  Message Delivery Provider
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_Mgmt})
	@Audited(label = "Update Notification Delivery Provider", callCategory = ApiCallCategory.SystemManagement)
    @Interceptors(AuditingInterceptor.class)
    public MsgDeliveryProviderSettingsDto updateObject(@PathParam("id") final String id, MsgDeliveryProviderSettingsUpdateDto updateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	
        return msgDeliveryProviderSettingsFacade.updateObject(id, updateDto);
    }
    

	/**
     * Deletes a  Message Delivery Provider
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_Mgmt})
	@Audited(label = "Delete Notification Delivery Provider", callCategory = ApiCallCategory.SystemManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteObject(@PathParam("id") final String id)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	
        msgDeliveryProviderSettingsFacade.deleteObject(id);
        return Response.ok().build();
    }


    /**
     * Creates a Message Delivery Provider
     * @return
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @POST
    @Path("{id}/ops/check")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Tenant_Mgmt})
	@Audited(label = "Check Notification Delivery Provider", callCategory = ApiCallCategory.SystemManagement)
    @Interceptors(AuditingInterceptor.class)
    public MsgDeliveryProviderSettingsDto check(@PathParam("id") final String id) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	    	
    	return msgDeliveryProviderSettingsFacade.check(id);
    }

	
}
