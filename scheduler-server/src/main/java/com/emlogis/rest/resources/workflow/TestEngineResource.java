package com.emlogis.rest.resources.workflow;

import com.emlogis.common.facade.workflow.validator.testengine.SchedulerTestEngineProxy;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.workflow.exception.WorkflowServerException;
import org.apache.log4j.Logger;

import javax.ejb.EJB;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;

import static com.emlogis.rest.resources.workflow.common.Utils.fail;
import static com.emlogis.rest.resources.workflow.common.Utils.responseOk;

/**
 * Created by user on 01.10.15.
 */
@Path("/requests/test")
public class TestEngineResource extends BaseResource {

    private final static Logger logger = Logger.getLogger(TestEngineResource.class);

    @EJB
    private SchedulerTestEngineProxy proxy;


    @POST
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response start() throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            return responseOk(proxy.start());
        } catch (WorkflowServerException error) {
            error.printStackTrace();
            logger.error("ERROR while starting Scheduler test engine ...", error);
            return fail(error);
        }
    }

    @POST
    @Path("/stop")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stop() throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        try {
            return responseOk(proxy.stop());
        } catch (WorkflowServerException error) {
            error.printStackTrace();
            logger.error("ERROR while stopping Scheduler test engine ...", error);
            return fail(error);
        }
    }
}
