package com.emlogis.workflow.listeners;

import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import org.apache.log4j.Logger;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.TaskLifeCycleEventListener;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * Created by alexborlis on 09.02.15.
 */
@Stateless
@LocalBean
public class PredefinedTaskListener implements TaskLifeCycleEventListener {

    private final static Logger logger = Logger.getLogger(PredefinedTaskListener.class);

    @EJB
    private WorkflowRequestService instanceService;

    @Override
    public void beforeTaskActivatedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void beforeTaskClaimedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void beforeTaskSkippedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void beforeTaskStartedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void beforeTaskStoppedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void beforeTaskCompletedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void beforeTaskFailedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void beforeTaskAddedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void beforeTaskExitedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void beforeTaskReleasedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void beforeTaskResumedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void beforeTaskSuspendedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void beforeTaskForwardedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void beforeTaskDelegatedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void beforeTaskNominatedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void afterTaskActivatedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void afterTaskClaimedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void afterTaskSkippedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void afterTaskStartedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void afterTaskStoppedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void afterTaskCompletedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void afterTaskFailedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void afterTaskAddedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void afterTaskExitedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void afterTaskReleasedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void afterTaskResumedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void afterTaskSuspendedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void afterTaskForwardedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void afterTaskDelegatedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }

    @Override
    public void afterTaskNominatedEvent(TaskEvent event) {
        logger.info("PredefinedTaskListener "+ event.toString());
    }
}
