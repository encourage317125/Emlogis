package com.emlogis.rest.resources.schedule;

import javax.ejb.EJB;
import javax.interceptor.Interceptors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.emlogis.common.EmployeeCalendarUtils;
import com.emlogis.common.facade.employee.EmployeeFacade;
import com.emlogis.common.facade.schedule.ScheduleFacade;
import com.emlogis.model.PrimaryKey;
import com.emlogis.rest.auditing.ApiCallCategory;
import com.emlogis.rest.auditing.Audited;
import com.emlogis.rest.auditing.AuditingInterceptor;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.security.Authenticated;
import net.fortuna.ical4j.model.Calendar;
import org.joda.time.DateTime;

import java.net.URISyntaxException;
import java.util.List;

// WARNING: THIS IS AN INTENTIONALLY UNPROTECTED RESOURCE, iw WO AUTHENTICATION REQUIRED 
// SPECIFY AUTHENTICATION REQUIRED PER METHOD AS NEEDED 
@Path("/calendarsync")
public class CalendarSyncResource extends BaseResource {

    @EJB
    private ScheduleFacade scheduleFacade;

    @EJB
    private EmployeeFacade employeeFacade;

    /**
     * Build a Calendar Sync URL for an employee
     */
    @Authenticated
    @GET
    @Path("/employees/url")
    @Produces(MediaType.APPLICATION_JSON)	
    @Audited(label = "Get Calendar Sync URL", callCategory = ApiCallCategory.CalendarSync)
    @Interceptors(AuditingInterceptor.class)
    public String getCalendarSyncURL(@PathParam("employeeId") final String employeeId,
                                     @QueryParam("timezone") String timeZone) {
        PrimaryKey employeePK = createPrimaryKey(employeeId);
        String calendarId = scheduleFacade.getCalendarSyncId(employeePK);
        return "/calendar/" + calendarId;
    }

    /**
     * Build a Calendar Sync URL for the currently logged employee
     */
    @Authenticated
    @GET
    @Path("/url")
    @Produces(MediaType.APPLICATION_JSON)	
    @Audited(label = "Get MyCalendar Sync URL", callCategory = ApiCallCategory.CalendarSync)
    @Interceptors(AuditingInterceptor.class)
    public String getCalendarURL(@QueryParam("timezone") String timeZone) {
    	return getCalendarSyncURL(this.getEmployeeId(), timeZone);
    }

    @Path("/{tenantId}/{employeeId}/calendar")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Audited(label = "Get Calendar Sync", callCategory = ApiCallCategory.CalendarSync)
    @Interceptors(AuditingInterceptor.class)
    public Response getCalendar(@PathParam("tenantId") final String tenantId,
                                @PathParam("employeeId") final String employeeId) throws URISyntaxException {
        PrimaryKey employeePrimaryKey = new PrimaryKey(tenantId, employeeId);

        DateTime dateTime = new DateTime();
        dateTime = dateTime.minusMonths(3);

        List<EmployeeCalendarUtils.CalendarEvent> allCurrentVEvents =
                employeeFacade.employeeICalendarInfo(employeePrimaryKey, dateTime.getMillis());

        Calendar iCal = EmployeeCalendarUtils.createICalendar(allCurrentVEvents);

        return Response.ok(iCal).build();
    }

}
