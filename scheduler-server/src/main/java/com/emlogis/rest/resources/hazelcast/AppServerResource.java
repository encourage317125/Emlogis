package com.emlogis.rest.resources.hazelcast;

import com.emlogis.common.services.hazelcast.HazelcastClientService;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.security.Authenticated;
import com.emlogis.scheduler.engine.communication.AppServerStatus;

import javax.ejb.EJB;
import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Path("/appservers")
@Authenticated
public class AppServerResource {

    @EJB
    private HazelcastClientService hazelcastService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Collection AppServers", callCategory = ApiCallCategory.Unclassified)
    @Interceptors(AuditingInterceptor.class)
    public Collection<AppServerStatus> getAppServers() {
        return hazelcastService.getAppServers();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "AppServer info", callCategory = ApiCallCategory.Unclassified)
    @Interceptors(AuditingInterceptor.class)
    public AppServerStatus getAppServer(@PathParam("id") String id) {
        return hazelcastService.getAppServer(id);
    }

    @POST
    @Path("{requestId}/ops/abort")
    @Produces(MediaType.APPLICATION_JSON)
    @Audited(label = "Abort", callCategory = ApiCallCategory.Unclassified)
    @Interceptors(AuditingInterceptor.class)
    public Response abort(@PathParam("requestId") String requestId, @QueryParam("timeout") Long timeout) {
        hazelcastService.abort(requestId, timeout);
        return Response.ok().build();
    }
}
