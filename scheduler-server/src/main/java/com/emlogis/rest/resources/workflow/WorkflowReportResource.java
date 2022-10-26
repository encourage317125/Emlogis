package com.emlogis.rest.resources.workflow;

import com.emlogis.common.facade.workflow.validator.WorkflowReportRequestValidator;
import com.emlogis.model.dto.ReportDto;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.resources.workflow.common.CustomJsonResponse;
import com.emlogis.rest.resources.workflow.common.ResponseStatus;
import com.emlogis.workflow.exception.WorkflowServerException;
import org.apache.log4j.Logger;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;

import static com.emlogis.common.ReportAdapter.adaptCollection;

/**
 * Created by alex on 2/27/15.
 */
@Path("/requests/report")
public class WorkflowReportResource extends BaseResource {

    private final static Logger logger = Logger.getLogger(WorkflowReportResource.class);

    @EJB
    private WorkflowReportRequestValidator validator;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response report(
            @QueryParam("type") String type,
            @QueryParam("site") String site,
            @QueryParam("teams") String teams,
            @QueryParam("startDate") Long startDate,
            @QueryParam("endDate") Long endDate
    ) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            ReportDto reportDto = validator.report(type, site, teams.split(";"), startDate, endDate);
            return Response.status(Response.Status.OK).entity(
                    new CustomJsonResponse(ResponseStatus.SUCCESS, reportDto)).build();
        } catch (WorkflowServerException e) {
            logger.error("ERROR while getting available processes to start", e);
            return Response.serverError().build();
        }
    }

    @Path("/sites")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String sites() throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
             return adaptCollection(validator.getReportSites());
        } catch (WorkflowServerException e) {
            logger.error("ERROR while getting available processes to start", e);
            return "ERROR";
        }
    }

    @Path("/teams")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String teams(@QueryParam(value = "tenantId") String tenantId) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            return adaptCollection(validator.getReportTeams(tenantId));
        } catch (WorkflowServerException e) {
            logger.error("ERROR while getting available processes to start", e);
            return "ERROR";
        }
    }


    @Path("/types")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String types() throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            return adaptCollection(validator.getReportTypes());
        } catch (WorkflowServerException e) {
            logger.error("ERROR while getting available processes to start", e);
            return "ERROR";
        }
    }
}
