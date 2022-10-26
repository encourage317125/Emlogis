package com.emlogis.rest.resources.hazelcast;

import com.emlogis.common.services.hazelcast.HazelcastClientService;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.scheduler.engine.communication.EngineStatus;

import javax.ejb.EJB;
import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Path("/engines")
@Authenticated
public class EngineResource {

    @EJB
    private HazelcastClientService hazelcastService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Collection Engines", callCategory = ApiCallCategory.Unclassified)
    @Interceptors(AuditingInterceptor.class)
    public Collection<EngineStatus> getEngines() {
        return hazelcastService.getEngines();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Engine info", callCategory = ApiCallCategory.Unclassified)
    @Interceptors(AuditingInterceptor.class)
    public EngineStatus getEngine(@PathParam("id") String id) {
        return hazelcastService.getEngine(id);
    }

    @POST
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Shutdown engine", callCategory = ApiCallCategory.Unclassified)
    @Interceptors(AuditingInterceptor.class)
    public Response shutdownEngine(@PathParam("id") String id, @QueryParam("timeout") @DefaultValue("0") Long timeout) {
        hazelcastService.shutdownEngine(id, timeout);
        return Response.ok().build();
    }

}
