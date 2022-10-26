package com.emlogis.workflow.scheduled;

import com.emlogis.common.services.hazelcast.HazelcastClientService;
import com.emlogis.common.services.workflow.process.expire.WflExpirationService;
import com.emlogis.workflow.api.notification.RequestNotifier;

import javax.ejb.*;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by user on 09.06.15.
 */
@Startup
@Singleton
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@DependsOn({"NotificationTimerService"})
public class WorkflowExpirationSchedule {

    private final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(WorkflowExpirationSchedule.class);
    private static final String LOCK_KEY = "WORKFLOW_EXPIRATION_SCHEDULE";
    private static final Long SECONDS_PER_ACTION = 2l;

    @EJB
    private WflExpirationService expirationService;

    @EJB
    private RequestNotifier notifier;

    @EJB
    private HazelcastClientService hazelcastClientService;


    private java.util.concurrent.locks.Lock lock() {
        java.util.concurrent.locks.Lock executeLock = hazelcastClientService.getLock(LOCK_KEY);
        if (executeLock == null) {
            executeLock = new ReentrantLock();
            hazelcastClientService.putLock(executeLock, LOCK_KEY);
        }

        //executeLock.lock();
        return executeLock;
    }

    @Schedule(minute = "7/20", hour = "*", persistent = false)
    private void execute() throws InterruptedException {
        java.util.concurrent.locks.Lock lock = lock();
        try {
            if (lock.tryLock(SECONDS_PER_ACTION, SECONDS)) {
                lock.lock();
                expirationService.expire();
            }
        } catch (Throwable error) {
            error.printStackTrace();
            logger.error("Error processing expired instances",error);
            throw new RuntimeException(error);
        } finally {
            lock.unlock();
        }
    }
}
