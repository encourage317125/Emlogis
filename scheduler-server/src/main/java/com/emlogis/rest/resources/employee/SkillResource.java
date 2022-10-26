package com.emlogis.rest.resources.employee;

import com.emlogis.common.facade.employee.SkillFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.employee.dto.SkillDto;
import com.emlogis.model.structurelevel.dto.SiteDto;
import com.emlogis.model.structurelevel.dto.SiteTeamDto;
import com.emlogis.model.structurelevel.dto.TeamDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;


/**
 * Resource for Skill Administration.
 * allows listing/viewing/updating/creating/deleting user skills.
 * @author EmLogis
 *
 */
@Path("/skills")
@Authenticated
public class SkillResource extends BaseResource {

    @EJB
    private SkillFacade skillFacade;

    /**
     * Get queried collection of Skills
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_View, Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "List Skills", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<SkillDto> getObjects(
            @QueryParam("select") String select,		// select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        return skillFacade.getObjects(tenantId, select, filter, offset, limit, orderBy, orderDir);
    }

    /**
     * Get specified a Skill
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Employee_View, Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Get SkillInfo", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public SkillDto getObject(@PathParam("id") final String id) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return skillFacade.getObject(primaryKey);
    }

    /**
     * Update a  Skill
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Update SkillInfo", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public SkillDto updateObject(@PathParam("id") final String id, SkillDto skillDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        return skillFacade.updateObject(primaryKey, skillDto);
    }

    /**
     * Creates a skill
     * @return
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Create Skill", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public SkillDto createObject(SkillDto skillDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey;
        // for testing reasons, we allow to provide an id via the DTO
        // in production, this option won't be available any more, ids will be generated on backend side
        // TODO, remove this option
        if (StringUtils.isBlank(skillDto.getId())) {
        	// id is not specified (which is preferred), let's generate one
        	primaryKey = createUniquePrimaryKey();
        } else {
        	primaryKey = createPrimaryKey(skillDto.getId());
        }
        skillDto.setId(primaryKey.getId());
        return skillFacade.createObject(primaryKey, skillDto);
    }

    /**
     * Delete a skill
     * @return
     */
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Delete Skill", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteObject(@PathParam("id") final String id) {
        PrimaryKey primaryKey = createPrimaryKey(id);
        skillFacade.deleteObject(primaryKey);
		return Response.ok().build();
    }

    @GET
    @Path("{skillId}/siteteamassociations")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Get Site/Team Associations", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<Map<String, Object>> getSiteTeamAssociations(@PathParam("skillId") String skillId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(skillId);
        return skillFacade.getSiteTeamAssociations(primaryKey);
    }

    @GET
    @Path("{skillId}/teamassociations")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Get Team Associations", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<SiteTeamDto> getTeamAssociations(@PathParam("skillId") String skillId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(skillId);
        return skillFacade.getTeamAssociations(primaryKey);
    }

    @POST
    @Path("{skillId}/teamassociations/ops/update")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Get SkillInfo", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<SiteTeamDto> updateTeamAssociations(@PathParam("skillId") String skillId, String[] teamIdArray)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(skillId);
        return skillFacade.updateTeamAssociations(primaryKey, teamIdArray);
    }

    /**
     * Get the collection of Sites associated to the Skill specified by the skillId
     * @param skillId
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    @Deprecated  // Not part of originally prescribed API, but leaving in case it proves useful. 
    @GET
	@Path("{skillId}/sites")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View})
	@Audited(label = "List Skill Sites", callCategory = ApiCallCategory.OrganizationManagement)
	@Interceptors(AuditingInterceptor.class)
	public Collection<SiteDto> getSites(@PathParam("skillId") String skillId) throws InstantiationException,
	        IllegalAccessException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException {
		PrimaryKey primaryKey = createPrimaryKey(skillId);
	
	    return skillFacade.getSites(primaryKey);
	}

    /**
     * Get the collection of Sites associated to the Skill specified by the skillId
     * @param skillId
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    @Deprecated  // Not part of originally prescribed API, but leaving in case it proves useful.
    @GET
	@Path("{skillId}/teams")
	@Produces(MediaType.APPLICATION_JSON)
	@RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View})
	@Audited(label = "List Skill Teams", callCategory = ApiCallCategory.OrganizationManagement)
	@Interceptors(AuditingInterceptor.class)
	public Collection<TeamDto> getTeams(@PathParam("skillId") String skillId) throws InstantiationException,
	        IllegalAccessException, InvocationTargetException, NoSuchMethodException, IllegalArgumentException {
		PrimaryKey primaryKey = createPrimaryKey(skillId);
	
	    return skillFacade.getTeams(primaryKey);
	}

}

