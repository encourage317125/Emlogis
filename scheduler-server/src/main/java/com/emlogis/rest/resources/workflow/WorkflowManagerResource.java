package com.emlogis.rest.resources.workflow;

import com.emlogis.common.facade.workflow.validator.WorkflowProcessRequestValidator;
import com.emlogis.common.security.AccountACL;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.dto.alerts.ManagerRequestCountsDto;
import com.emlogis.model.workflow.dto.decision.WorkflowDecisionDto;
import com.emlogis.model.workflow.dto.filter.ManagerRequestsFilterDto;
import com.emlogis.model.workflow.dto.task.ManagerRequestDetailsInfoDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;
import com.emlogis.workflow.exception.WorkflowServerException;
import org.apache.log4j.Logger;

import javax.ejb.EJB;
import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;

import static com.emlogis.common.EmlogisUtils.fromJsonString;
import static com.emlogis.common.security.Permissions.Availability_RequestMgmt;
import static com.emlogis.common.security.Permissions.Shift_RequestMgmt;
import static com.emlogis.rest.resources.workflow.common.Utils.fail;
import static com.emlogis.rest.resources.workflow.common.Utils.responseOk;
import static com.emlogis.workflow.enums.WorkflowRequestDecision.APPROVE;
import static com.emlogis.workflow.enums.WorkflowRequestDecision.DECLINE;

/**
 * Created by lucas on 27.05.2015.
 */
@Path("/requests/manager")
@Authenticated
public class WorkflowManagerResource extends BaseResource {

    private final static Logger logger = Logger.getLogger(WorkflowManagerResource.class);

    @EJB
    private WorkflowProcessRequestValidator validator;

    @POST
    @Path("/ops/query")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_RequestMgmt, Shift_RequestMgmt})
    @Audited(label = "Query Manager Requests", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response getObjects(ManagerRequestsFilterDto filterDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            AccountACL acl = getAcl();
            String token = getSessionService().getTokenId();
            String language = getSessionService().getSessionInfo(token).getLanguage();
            PrimaryKey userAccountPk = new PrimaryKey(getTenantId(), getUserId());
            ResultSet<ManagerRequestDetailsInfoDto> tasks = validator.executeManagerQuery(userAccountPk, filterDto, acl, language);
            return responseOk(tasks);
        } catch (WorkflowServerException e) {
            logger.error("ERROR while getting task list", e);
            return fail(e);
        }
    }

    @GET
    @Path("/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_RequestMgmt, Shift_RequestMgmt})
    @Audited(label = "Get Manager Request", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response getObject(@PathParam("requestId") String requestId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            PrimaryKey userAccountPk = new PrimaryKey(getTenantId(), getUserId());
            PrimaryKey requestPk = new PrimaryKey(getTenantId(), requestId);
            return responseOk(validator.getManagerRequestDetails(userAccountPk, requestPk, locale()));
        } catch (WorkflowServerException e) {
            logger.error("ERROR while getting task info", e);
            return fail(e);
        }
    }

    @GET
    @Path("/pendingandnewrequestcounts")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_RequestMgmt, Shift_RequestMgmt})
    @Audited(label = "Get Manager New-Request-Count", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public ManagerRequestCountsDto pendingAndNewRequestCounts(
            @QueryParam("teamrequests") @DefaultValue("true") Boolean teamRequests) {
        PrimaryKey userAccountPk = new PrimaryKey(getTenantId(), getUserId());
        PrimaryKey employeePk = new PrimaryKey(getTenantId(), getEmployeeId());
        return validator.getManagerPendingAndNewRequestCounts(teamRequests, userAccountPk, employeePk, locale());
    }

    @POST
    @Path("/{requestId}/ops/markas")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_RequestMgmt, Shift_RequestMgmt})
    @Audited(label = "Mark Manager Request", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response markas(
            @PathParam("requestId") String requestId,
            Boolean isRead
    ) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            String tenantId = getTenantId();
            String userId = getSessionService().getUserId();
            PrimaryKey userAccountPk = new PrimaryKey(tenantId, userId);
            validator.managerMarkAs(requestId, isRead, userAccountPk, locale());
            return responseOk();
        } catch (WorkflowServerException e) {
            logger.error("ERROR while approve/decline process", e);
            return fail(e);
        }
    }

    @POST
    @Path("/{requestId}/ops/approve")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_RequestMgmt, Shift_RequestMgmt})
    @Audited(label = "Approve Request", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response approve(
            @PathParam("requestId") String requestId,
            String workflowDecisionDto
    ) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            PrimaryKey userAccountPk = new PrimaryKey(getTenantId(), getUserId());
            PrimaryKey requestPk = new PrimaryKey(getTenantId(), requestId);
            WorkflowDecisionDto dto = fromJsonString(workflowDecisionDto, WorkflowDecisionDto.class);
            validator.managerDecision(dto, APPROVE, requestPk, userAccountPk, locale());
            PrimaryKey employeePk = new PrimaryKey(getTenantId(), getEmployeeId());
            return responseOk(validator.getManagerPendingAndNewRequestCounts(true, userAccountPk, employeePk, locale()));
        } catch (WorkflowServerException e) {
            logger.error("ERROR while approve/decline process", e);
            return fail(e);
        }
    }

    @POST
    @Path("/{requestId}/ops/decline")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_RequestMgmt, Shift_RequestMgmt})
    @Audited(label = "Decline Request", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response decline(
            @PathParam("requestId") String requestId,
            String workflowDecisionDto
    ) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            PrimaryKey userAccountPk = new PrimaryKey(getTenantId(), getUserId());
            PrimaryKey requestPk = new PrimaryKey(getTenantId(), requestId);
            WorkflowDecisionDto dto = fromJsonString(workflowDecisionDto, WorkflowDecisionDto.class);
            validator.managerDecision(dto, DECLINE, requestPk, userAccountPk, locale());
            PrimaryKey employeePk = new PrimaryKey(getTenantId(), getEmployeeId());
            return responseOk(validator.getManagerPendingAndNewRequestCounts(true, userAccountPk, employeePk, locale()));
        } catch (WorkflowServerException e) {
            logger.error("ERROR while approve/decline process", e);
            return fail(e);
        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_RequestMgmt, Shift_RequestMgmt})
    @Audited(label = "Decline Request", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response decline()
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            validator.deleteAllRequests();
            return responseOk();
        } catch (WorkflowServerException e) {
            logger.error("ERROR while approve/decline process", e);
            return fail(e);
        }
    }

}
