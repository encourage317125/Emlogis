package com.emlogis.rest.resources.shiftpattern;

import com.emlogis.common.facade.shiftpattern.ShiftPatternFacade;
import com.emlogis.common.facade.structurelevel.TeamFacade;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.security.PermissionCheck;
import com.emlogis.common.security.Permissions;
import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.shiftpattern.dto.*;
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
import javax.xml.bind.annotation.XmlRootElement;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Path("shiftpatterns")
@Authenticated
public class ShiftPatternResource extends BaseResource {

    @EJB
    private ShiftPatternFacade shiftPatternFacade;
    
    @EJB
    private TeamFacade teamFacade;

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "List ShiftPattern summary", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ResultSetDto<ShiftPatternSummaryDto> getShiftPatternSummary(
            @QueryParam("select") String select,        // select is NOT IMPLEMENTED FOR NOW ..
            @QueryParam("filter") String filter,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("orderby") String orderBy,
            @QueryParam("orderdir") String orderDir) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        String tenantId = getTenantId();
        AccountACL acl = getAcl();
        return shiftPatternFacade.getSummary(tenantId, select, filter, offset, limit, orderBy, orderDir, acl);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_View})
    @Audited(label = "Get ShiftPattern info", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftPatternDto getObject(@PathParam("id") String id) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        // check access to pattern's Team (which requires loading the pattern first ...)        
        ShiftPatternDto dto = shiftPatternFacade.getObject(primaryKey);
        checkSiteAndTeamReadAccess(createPrimaryKey(dto.getTeamId()));		// check Site & Team Access
        return dto;
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Update ShiftPattern", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftPatternDto updateObject(@PathParam("id") String id, ShiftPatternUpdateDto shiftPatternUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        
        // check access to pattern's Team (which requires loading the pattern first ...)        
        ShiftPatternDto dto = shiftPatternFacade.getObject(primaryKey);
        checkSiteAndTeamWriteAccess(createPrimaryKey(dto.getTeamId()));		// check Site & Team Access
        return shiftPatternFacade.updateObject(primaryKey, shiftPatternUpdateDto);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Create ShiftPattern", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public ShiftPatternDto createObject(ShiftPatternCreateDto shiftPatternCreateDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PrimaryKey primaryKey;
        if (StringUtils.isBlank(shiftPatternCreateDto.getId())) {
            // id is not specified (which is preferred), let's generate one
            primaryKey = createUniquePrimaryKey();
        } else {
            primaryKey = createPrimaryKey(shiftPatternCreateDto.getId());
        }
//        checkAcl(primaryKey, PermissionCheck.ANY, Permissions.Demand_Mgmt);
        // check access to pattern's Team  
        checkSiteAndTeamWriteAccess(createPrimaryKey(shiftPatternCreateDto.getTeamId()));		// check Site & Team Access
        shiftPatternCreateDto.setId(primaryKey.getId());
        return shiftPatternFacade.createObject(primaryKey, shiftPatternCreateDto);
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Delete ShiftPattern", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteObject(@PathParam("id") String id) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        // check access to pattern's Team (which requires loading the pattern first ...)        
        ShiftPatternDto dto = shiftPatternFacade.getObject(primaryKey);
        checkSiteAndTeamWriteAccess(createPrimaryKey(dto.getTeamId()));		// check Site & Team Access
        shiftPatternFacade.deleteObject(primaryKey);
        return Response.ok().build();
    }

    @POST
    @Path("{id}/ops/duplicate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Duplicate ShiftPattern", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<ShiftPatternDto> duplicate(@PathParam("id") String id, DuplicateDto dto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        // check access to pattern's Team (which requires loading the pattern first ...)        
        ShiftPatternDto srcDto = shiftPatternFacade.getObject(primaryKey);
        checkSiteAndTeamWriteAccess(createPrimaryKey(srcDto.getTeamId()));		// check Site & Team Access

        Set<DayOfWeek> dayOfWeeks = new HashSet<>();

        if (StringUtils.isNotBlank(dto.getDays())) {
            String[] daysArray = dto.getDays().split(",");
            for (String day : daysArray) {
                if (StringUtils.isNotBlank(day)) {
                    dayOfWeeks.add(DayOfWeek.valueOfDayName(day));
                }
            }
        }

        // assume cdDate is provided when value > 0
        Long cdDateLongValue = dto.getCdDate() > 0 ? dto.getCdDate() : null;

        return shiftPatternFacade.duplicate(primaryKey, dayOfWeeks, cdDateLongValue);
    }

    @POST
    @Path("{id}/ops/computeshiftreqs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Generate demand ShiftReqs", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<ShiftReqDto> generateDemandShiftReqs(@PathParam("id") String id) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        PrimaryKey primaryKey = createPrimaryKey(id);
        ShiftPatternDto dto = shiftPatternFacade.getObject(primaryKey);
        checkSiteAndTeamWriteAccess(createPrimaryKey(dto.getTeamId()));		// check Site & Team Access
        return shiftPatternFacade.generateDemandShiftReqs(primaryKey);
    }
    
    @POST
    @Path("/ops/draftcomputeshiftreqs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Permissions.Demand_Mgmt})
    @Audited(label = "Generate draft demand ShiftReqs", callCategory = ApiCallCategory.DemandManagement)
    @Interceptors(AuditingInterceptor.class)
    public Collection<ShiftReqDto> generateDraftDemandShiftReqs(DraftDemandDto draftDemandDto)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return shiftPatternFacade.generateDraftDemandShiftReqs(getTenantId(), draftDemandDto);
    }

    private void checkSiteAndTeamReadAccess(PrimaryKey primaryKey) {
        AccountACL acl = getAcl();
        Permissions[] sitePermissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt};
        Permissions[] teamPermissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt};
        teamFacade.checkSiteAndTeamAcl(primaryKey, acl, PermissionCheck.ANY, sitePermissions, PermissionCheck.ANY,
                teamPermissions);
	}

    private void checkSiteAndTeamWriteAccess(PrimaryKey primaryKey) {
        AccountACL acl = getAcl();
        Permissions[] sitePermissions = {Permissions.OrganizationProfile_View, Permissions.OrganizationProfile_Mgmt};
        Permissions[] teamPermissions = {Permissions.OrganizationProfile_Mgmt};
        teamFacade.checkSiteAndTeamAcl(primaryKey, acl, PermissionCheck.ANY, sitePermissions, PermissionCheck.ANY,
                teamPermissions);
	}

}

@XmlRootElement
class DuplicateDto {

	private	String days;
	private	long cdDate;

	public String getDays() {
		return days;
	}

	public long getCdDate() {
		return cdDate;
	}
}

