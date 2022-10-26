package com.emlogis.rest.resources.structurelevel;

import com.emlogis.common.facade.structurelevel.StructureLevelFacade;
import com.emlogis.common.security.Permissions;
import com.emlogis.common.services.structurelevel.TraversalDirection;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.structurelevel.dto.AOMRelationshipDto;
import com.emlogis.model.structurelevel.dto.StructureLevelDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;

import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

@Authenticated
abstract public class StructureLevelResource extends BaseResource {

    abstract protected StructureLevelFacade getStructureLevelFacade();

    // TODO
    // add AOM relationship handling methods
    // add AOM attribute handling methods
    
/*    
    @GET
    @Path("{accountId}/roles")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "List StructureLevel Roles", callCategory = ApiCallCategory.StructureLevelManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<RoleDto> findStructureLevelRoles(@PathParam("accountId") String accountId) throws Exception {
        PrimaryKey accountPrimaryKey = createPrimaryKey(accountId);

        return getStructureLevelFacade().findStructureLevelRoles(accountPrimaryKey);
    }

    @POST
    @Path("{accountId}/ops/addrole")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "AddRole To StructureLevel", callCategory = ApiCallCategory.StructureLevelManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response addRole(@PathParam("accountId") String accountId,
                            @QueryParam("id") String roleId) {
        PrimaryKey accountPrimaryKey = createPrimaryKey(accountId);

        getStructureLevelFacade().addRole(accountPrimaryKey, roleId);
		return Response.ok().build();
    }

    @DELETE
    @Path("{accountId}/ops/removerole")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "RemoveRole From StructureLevel", callCategory = ApiCallCategory.StructureLevelManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response removeRole(@PathParam("accountId") String accountId,
                               @QueryParam("id") String roleId) {
        PrimaryKey accountPrimaryKey = createPrimaryKey(accountId);

        getStructureLevelFacade().removeRole(accountPrimaryKey, roleId);
		return Response.ok().build();
    }
*/
    
    @GET
    @Path("{stlId}/associatedobjects/{relationshipType}/")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "List StructureLevelRelatedEntities", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<StructureLevelDto> getAssociatedObjects(
    		@PathParam("stlId") String seedStructureLevelId,
			@PathParam("relationshipType") String relationshipType,
			@QueryParam("dir")  @DefaultValue("OUT") TraversalDirection dir) throws Exception {
    	
    	// TODO add paging/sorting and retun a resultset vs a collection
    	return doGetAssociatedObjects(seedStructureLevelId, relationshipType, dir);
    }
    
    protected Collection<StructureLevelDto> doGetAssociatedObjects(String seedStructureLevelId,
                                                                   String relationshipType,
                                                                   TraversalDirection dir) throws Exception {
    	PrimaryKey seedStructureLevelPK = createPrimaryKey(seedStructureLevelId);
        return getStructureLevelFacade().getAssociatedObjects(seedStructureLevelPK, relationshipType, dir);
    }
    
    

    /**
     * Creates an AOM relationship between 2 entities.
     * @return
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Path("/associations/ops/createaomrelationship")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Create AOM Relationship", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public AOMRelationshipDto createAOMRelationship(AOMRelationshipParam relationshipParam) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        PrimaryKey primaryKey;
        /*
        // for testing reasons, we allow to provide an id via the DTO
        // in production, this option won't be available any more, ids will be generated on backend side
        // TODO, remove this option
        if (StringUtils.isBlank(teamDto.getId())) {
        	// id is not specified (which is preferred), let's generate one
        	primaryKey = createUniquePrimaryKey();
        }
        else {
        	primaryKey = createPrimaryKey(teamDto.getId());
        }
        teamDto.setId(primaryKey.getId());
        PrimaryKey sitePrimaryKey = createPrimaryKey(teamDto.getSiteId());
        return teamFacade.createObject(sitePrimaryKey, primaryKey, teamDto);
        */
        return null;
    }

    

    /**
     * Removes an AOM relationship between 2 entities.
     * @return
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Path("/associations/ops/removeaomrelationship")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.OrganizationProfile_Mgmt})
    @Audited(label = "Create AOM Relationship", callCategory = ApiCallCategory.OrganizationManagement)
    @Interceptors(AuditingInterceptor.class)
    public boolean removeAOMRelationship(AOMRelationshipParam relationshipParam) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        PrimaryKey primaryKey;
        /*
        // for testing reasons, we allow to provide an id via the DTO
        // in production, this option won't be available any more, ids will be generated on backend side
        // TODO, remove this option
        if (StringUtils.isBlank(teamDto.getId())) {
        	// id is not specified (which is preferred), let's generate one
        	primaryKey = createUniquePrimaryKey();
        }
        else {
        	primaryKey = createPrimaryKey(teamDto.getId());
        }
        teamDto.setId(primaryKey.getId());
        PrimaryKey sitePrimaryKey = createPrimaryKey(teamDto.getSiteId());
        return teamFacade.createObject(sitePrimaryKey, primaryKey, teamDto);
        */
        return false;
    }
}

class AOMRelationshipParam {
	
	private String	srcId;
	private String	dstId;
	private String	relationshipType;
	
	public String getSrcId() {
		return srcId;
	}
	public void setSrcId(String srcId) {
		this.srcId = srcId;
	}
	public String getDstId() {
		return dstId;
	}
	public void setDstId(String dstId) {
		this.dstId = dstId;
	}
	public String getRelationshipType() {
		return relationshipType;
	}
	public void setRelationshipType(String relationshipType) {
		this.relationshipType = relationshipType;
	}
	
}
