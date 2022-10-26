package com.emlogis.workflow.scheduled;

import com.emlogis.common.services.hazelcast.HazelcastClientService;
import com.emlogis.common.services.workflow.notification.RequestNotificationEventService;
import com.emlogis.workflow.api.notification.RequestNotifier;

import javax.ejb.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 31.08.15.
 */
@Startup
@Singleton
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@DependsOn({"WorkflowExpirationSchedule"})
public class WorkflowNotificationSchedule {

    private final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(WorkflowNotificationSchedule.class);
    private static final String LOCK_KEY = "WORKFLOW_NOTIFICATION_SCHEDULE";
    private static final Long SECONDS_PER_ACTION = 3l;

    @EJB
    private RequestNotifier notifier;

    @EJB
    private RequestNotificationEventService requestNotificationEventService;

    @EJB
    private HazelcastClientService hazelcastClientService;

    private java.util.concurrent.locks.Lock lock() {
        java.util.concurrent.locks.Lock executeLock = hazelcastClientService.getLock(LOCK_KEY);
        if (executeLock == null) {
            executeLock = new ReentrantLock();
            hazelcastClientService.putLock(executeLock, LOCK_KEY);
        }
        return executeLock;
    }

    @Schedule(minute = "*/3", hour = "*", persistent = false)
    private void execute() throws Exception {
        java.util.concurrent.locks.Lock lock = lock();
        try {
            if (lock.tryLock(SECONDS_PER_ACTION, TimeUnit.SECONDS)) {
                lock.lock();
                String processCommaSeparated = notifier.process();
                //requestNotificationEventService.deleteSelected(processCommaSeparated);
            }
        } catch (Throwable error) {
            error.printStackTrace();
            logger.error("Error processing expired instances", error);
            throw new RuntimeException(error);
        } finally {
            lock.unlock();
        }
    }
}
