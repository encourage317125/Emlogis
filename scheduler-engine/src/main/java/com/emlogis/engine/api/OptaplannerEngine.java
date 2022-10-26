package com.emlogis.engine.api;

import com.emlogis.engine.domain.EmployeeSchedule;
import com.emlogis.engine.domain.communication.NotificationService;
import com.emlogis.engine.domain.communication.ScheduleResult;

// TODO
// instead of passing the EventService to the engine, we should pass a simplified / convenience method that 
// will abstract the event service, and expose a method for sending notifications

public interface OptaplannerEngine {

    public ScheduleResult findAssignments(EmployeeSchedule schedule, NotificationService notificationService, boolean includeDetails);

    public ScheduleResult checkQualification(EmployeeSchedule schedule, NotificationService notificationService, boolean includeDetails);
    
	public ScheduleResult getOpenShiftEligibility(EmployeeSchedule schedule, NotificationService notificationService, boolean includeDetails);
	
	public ScheduleResult getShiftSwapEligibility(EmployeeSchedule schedule, NotificationService notificationService, boolean includeDetails);

    void abort();

    boolean abortAllowed();
    
    boolean abortSuccessful();

}
