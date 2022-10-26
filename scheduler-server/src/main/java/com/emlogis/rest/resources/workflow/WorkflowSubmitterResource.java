package com.emlogis.rest.resources.workflow;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.facade.workflow.validator.SubmitPreValidationResultDto;
import com.emlogis.common.facade.workflow.validator.WorkflowProcessRequestValidator;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.dto.details.abstracts.RequestDetailsInfo;
import com.emlogis.model.workflow.dto.filter.SubmitterRequestsFilterDto;
import com.emlogis.model.workflow.dto.process.request.AddRequestPeersDto;
import com.emlogis.model.workflow.dto.process.request.RemoveRequestPeersDto;
import com.emlogis.model.workflow.dto.process.request.submit.SubmitDto;
import com.emlogis.model.workflow.dto.process.response.SubmitRequestResultDto;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.resources.workflow.common.CustomJsonResponse;
import com.emlogis.rest.resources.workflow.common.ResponseStatus;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.rest.security.RequirePermissionIn;
import com.emlogis.workflow.exception.WorkflowClientException;

import org.apache.log4j.Logger;

import javax.ejb.EJB;
import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.lang.reflect.InvocationTargetException;

import static com.emlogis.common.EmlogisUtils.fromJsonString;
import static com.emlogis.common.security.Permissions.*;
import static com.emlogis.rest.resources.workflow.common.ResponseStatus.FAIL;
import static com.emlogis.rest.resources.workflow.common.Utils.fail;
import static com.emlogis.rest.resources.workflow.common.Utils.responseOk;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE;

/**
 * Created by lucas on 27.05.2015.
 */
@Path("/requests/submitter")
@Authenticated
public class WorkflowSubmitterResource extends BaseResource {

    private final static Logger logger = Logger.getLogger(WorkflowSubmitterResource.class);

    @EJB
    private WorkflowProcessRequestValidator validator;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_Request, Shift_Request})
    @Audited(label = "Submitter Request", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response createObject(String submitDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            PrimaryKey userAccountPk = new PrimaryKey(getTenantId(), getUserId());
            SubmitDto dto = EmlogisUtils.fromJsonString(submitDto, SubmitDto.class);
            SubmitPreValidationResultDto submitPreValidationResultDto = validator.validateRequestDate(userAccountPk, dto);
            if (submitPreValidationResultDto.getResult()) {
                SubmitRequestResultDto resultDto = validator.submitRequest(submitPreValidationResultDto, dto, locale());
                if (!resultDto.getErrors().isEmpty()) {
                    return Response.status(OK).entity(new CustomJsonResponse(ResponseStatus.FAIL, resultDto)).build();
                } else {
                    return responseOk(resultDto);
                }
            } else {
                return Response.status(BAD_REQUEST).entity(
                        new CustomJsonResponse(FAIL, submitPreValidationResultDto.getMessage())).build();
            }
        } catch (WorkflowClientException e) {
            logger.error("ERROR while creating proto process", e);
            return fail(e);
        }
    }

    @POST
    @Path("/ops/query")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_Request, Shift_Request})
    @Audited(label = "Query Submitter Requests", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response getObjects(SubmitterRequestsFilterDto filterDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            PrimaryKey userAccountPk = new PrimaryKey(getTenantId(), getUserId());
            return responseOk(validator.executeSubmitterQuery(userAccountPk, filterDto, locale()));
        } catch (WorkflowClientException e) {
            logger.error("ERROR while getting request list", e);
            return fail(e);
        }
    }

    @GET
    @Path("/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_Request, Shift_Request})
    @Audited(label = "Get Submitter Request", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response getObject(@PathParam("requestId") String requestId
    ) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            PrimaryKey userAccountPk = new PrimaryKey(getTenantId(), getUserId());
            PrimaryKey requestPk = new PrimaryKey(getTenantId(), requestId);
            return responseOk(validator.getSubmitterRequestDetails(userAccountPk, requestPk, locale()));
        } catch (WorkflowClientException e) {
            logger.error("ERROR while getting request info", e);
            return fail(e);
        }
    }

    @DELETE
    @Path("/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_Request, Shift_Request})
    @Audited(label = "Cancel Submitter Request", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response deleteObject(@PathParam("requestId") final String requestId) {
        try {
            PrimaryKey userAccountPk = new PrimaryKey(getTenantId(), getUserId());
            PrimaryKey requestPk = new PrimaryKey(getTenantId(), requestId);
            validator.removeRequest(userAccountPk, requestPk,
                    getTenantId() + ":" + getSessionService().getActualUserName() + " cancel request " + requestId,
                    locale());
            RequestDetailsInfo wflOriginatorProcessInstanceDetailedDto =
                    validator.getSubmitterRequestDetails(userAccountPk, requestPk, locale());
            return responseOk(wflOriginatorProcessInstanceDetailedDto);
        } catch (WorkflowClientException e) {
            logger.error("ERROR while removing process", e);
            return fail(e);
        }
    }

    @GET
    @Path("/{requestId}/history")
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_Request, Shift_Request})
    @Audited(label = "Get Request History", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response history(@PathParam("requestId") final String requestId) {
        try {
            String tenantId = getTenantId();
            String userId = getSessionService().getUserId();
            return responseOk(validator.getRequestHistory(tenantId, userId, requestId, locale()));
        } catch (WorkflowClientException e) {
            logger.error("ERROR retrieving history", e);
            return fail(e);
        }
    }

    @POST
    @Path("/{requestId}/ops/cancel/peers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_RequestMgmt, Shift_Request})
    @Audited(label = "Cancel Peer", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response updateObject(
            @PathParam("requestId") String requestId,
            RemoveRequestPeersDto dto
    ) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            PrimaryKey userAccountPk = new PrimaryKey(getTenantId(), getUserId());
            PrimaryKey requestPk = new PrimaryKey(getTenantId(), requestId);
            validator.removePeers(dto, userAccountPk, requestPk, locale());
            RequestDetailsInfo wflOriginatorProcessInstanceDetailedDto =
                    validator.getSubmitterRequestDetails(userAccountPk, requestPk, locale());
            return responseOk(wflOriginatorProcessInstanceDetailedDto);
        } catch (WorkflowClientException e) {
            logger.error("ERROR while removing recipients from process", e);
            return fail(e);
        }
    }

    @POST
    @Path("/{requestId}/ops/add/peers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequirePermissionIn(permissions = {Availability_RequestMgmt, Shift_Request})
    @Audited(label = "Add Peer", callCategory = ApiCallCategory.Requests)
    @Interceptors(AuditingInterceptor.class)
    public Response updateObject(
            @PathParam("requestId") String requestId,
            AddRequestPeersDto dto
    ) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            PrimaryKey userAccountPk = new PrimaryKey(getTenantId(), getSessionService().getActualUserId());
            PrimaryKey requestPk = new PrimaryKey(getTenantId(), requestId);
            validator.addPeers(dto, userAccountPk, requestPk, locale());
            return responseOk();
        } catch (WorkflowClientException e) {
            logger.error("ERROR while adding recipients to the process", e);
            return fail(e);
        }
    }
}
