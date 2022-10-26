package com.emlogis.rest.resources.workflow;

import com.emlogis.common.facade.workflow.validator.WorkflowProcessRequestValidator;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.dto.alerts.TeamRequestCountsDto;
import com.emlogis.model.workflow.dto.decision.PeerDecisionDto;
import com.emlogis.model.workflow.dto.filter.PeerRequestsFilterDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;
import com.emlogis.workflow.exception.WorkflowClientException;
import com.emlogis.workflow.exception.WorkflowServerException;
import org.apache.log4j.Logger;

import javax.ejb.EJB;
import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;

import static com.emlogis.common.EmlogisUtils.fromJsonString;
import static com.emlogis.common.security.Permissions.*;
import static com.emlogis.rest.resources.workflow.common.Utils.fail;
import static com.emlogis.rest.resources.workflow.common.Utils.responseOk;
import static com.emlogis.workflow.enums.WorkflowRequestDecision.APPROVE;
import static com.emlogis.workflow.enums.WorkflowRequestDecision.DECLINE;

/**
 * Created by lucas on 27.05.2015.
 */
@Path("/requests/peer")
@Authenticated
public class WorkflowPeerResource extends BaseResource {

    private final static Logger logger = Logger.getLogger(WorkflowPeerResource.class);

    @EJB
    private WorkflowProcessRequestValidator validator;

    @POST
    @Path("/ops/query")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_Request, Shift_Request})
    @Audited(label = "Query Peer Requests", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response getObjects(PeerRequestsFilterDto filterDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            PrimaryKey userAccountPk = new PrimaryKey(getTenantId(), getUserId());
            return responseOk(validator.executePeerQuery(userAccountPk, filterDto, locale()));
        } catch (WorkflowClientException e) {
            logger.error("ERROR while getting task list", e);
            return fail(e);
        }
    }

    @POST
    @Path("/ops/queryold")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_Request, Shift_Request})
    @Audited(label = "Query Peer Requests", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response getObjectsOld(PeerRequestsFilterDto filterDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            return responseOk(validator.getPeerAssignedProcessesOld(getTenantId(), getSessionService().getUserId(),
                    filterDto, locale()));
        } catch (WorkflowClientException e) {
            logger.error("ERROR while getting task list", e);
            return fail(e);
        }
    }


    @GET
    @Path("/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_Request, Shift_Request})
    @Audited(label = "Get Peer Request", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response getObject(@PathParam("requestId") String requestId)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            PrimaryKey requestPk = new PrimaryKey(getTenantId(), requestId);
            PrimaryKey userAccountPk = new PrimaryKey(getTenantId(), getUserId());
            return responseOk(validator.getPeerRequestDetails(userAccountPk, requestPk, locale()));
        } catch (WorkflowServerException e) {
            logger.error("ERROR while getting task info", e);
            return fail(e);
        }
    }

    @POST
    @Path("/{requestId}/ops/markas")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_RequestMgmt, Shift_RequestMgmt})
    @Audited(label = "Mark Peer Request", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response markas(
            @PathParam("requestId") String requestId,
            Boolean isRead
    ) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            String tenantId = getTenantId();
            String userId = getSessionService().getUserId();
            validator.peerMarkAs(requestId, isRead, userId, tenantId, locale());
            return responseOk();
        } catch (WorkflowServerException e) {
            logger.error("ERROR while approve/decline process", e);
            return fail(e);
        }
    }

    @POST
    @Path("/{requestId}/ops/cancel")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_Request, Shift_Request})
    @Audited(label = "Decline Team Request", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response cancel(
            @PathParam("requestId") String requestId
    ) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            PrimaryKey requestPk = new PrimaryKey(getTenantId(), requestId);
            PrimaryKey userAccountId = new PrimaryKey(getTenantId(), getUserId());
            validator.cancelApprovedRequest(requestPk, userAccountId, locale());
            PrimaryKey employeePrimaryKey = new PrimaryKey(getTenantId(), getEmployeeId());
            return responseOk(validator.getTeamPendingAndNewRequestCounts(employeePrimaryKey, locale()));
        } catch (WorkflowClientException e) {
            logger.error("ERROR while approve/decline process", e);
            return fail(e);
        }
    }

    @POST
    @Path("/{requestId}/ops/decline")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_Request, Shift_Request})
    @Audited(label = "Decline Team Request", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response decline(
            @PathParam("requestId") String requestId,
            String workflowDecisionDto
    ) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            PrimaryKey requestPk  = new PrimaryKey(getTenantId(), requestId);
            PrimaryKey userAccounPk = new PrimaryKey(getTenantId(), getUserId());
            PeerDecisionDto dto = fromJsonString(workflowDecisionDto, PeerDecisionDto.class);
            validator.peerDecision(dto, DECLINE, requestPk, userAccounPk, locale());
            PrimaryKey employeePrimaryKey = new PrimaryKey(getTenantId(), getEmployeeId());
            return responseOk(validator.getTeamPendingAndNewRequestCounts(employeePrimaryKey, locale()));
        } catch (WorkflowClientException e) {
            logger.error("ERROR while approve/decline process", e);
            return fail(e);
        }
    }

    @POST
    @Path("/{requestId}/ops/approve")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_Request, Shift_Request})
    @Audited(label = "Accept Team Request", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response approve(
            @PathParam("requestId") String requestId,
            String workflowDecisionDto
    ) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            PrimaryKey requestPk = new PrimaryKey(getTenantId(), requestId);
            PrimaryKey userAccountPk = new PrimaryKey(getTenantId(), getUserId());
            PeerDecisionDto dto = fromJsonString(workflowDecisionDto, PeerDecisionDto.class);
            validator.peerDecision(dto, APPROVE, requestPk, userAccountPk, locale());
            PrimaryKey employeePrimaryKey = new PrimaryKey(getTenantId(), getEmployeeId());
            return responseOk(validator.getTeamPendingAndNewRequestCounts(employeePrimaryKey, locale()));
        } catch (WorkflowClientException e) {
            logger.error("ERROR while approve/decline process", e);
            return fail(e);
        }
    }

    @GET
    @Path("/pendingandnewrequestcounts")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_Request, Shift_Request})
    @Audited(label = "Get Peer New-Request-Count", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public TeamRequestCountsDto pendingAndNewRequestCounts() {
        PrimaryKey employeePrimaryKey = new PrimaryKey(getTenantId(), getEmployeeId());
        return validator.getTeamPendingAndNewRequestCounts(employeePrimaryKey, locale());
    }
}
