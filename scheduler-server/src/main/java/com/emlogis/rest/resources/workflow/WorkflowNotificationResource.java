package com.emlogis.rest.resources.workflow;

import com.emlogis.common.facade.workflow.notification.IWflNotificationActionFacade;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.exception.WorkflowClientException;
import org.apache.log4j.Logger;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Created by alex on 3/19/15.
 */
@Path("/requests/notification")
public class WorkflowNotificationResource extends BaseResource {

    private final static Logger logger = Logger.getLogger(WorkflowNotificationResource.class);

    @EJB
    private IWflNotificationActionFacade notificationActionFacade;

    @Path("/process")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String process(
            @QueryParam("code") String code,
            @QueryParam("account") String account,
            @QueryParam("tenant") String tenant,
            @QueryParam("decision") String decision,
            @QueryParam("role") String role) {
        try {
            return notificationActionFacade.processRequest(code, account, tenant, decision, WorkflowRoleDict.valueOf(role));
        } catch (WorkflowClientException e) {
            logger.error("ERROR while getting available processes to start", e);
            return "ERROR";
        }
    }
}
