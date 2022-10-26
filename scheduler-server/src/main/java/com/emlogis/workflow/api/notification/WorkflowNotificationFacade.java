package com.emlogis.workflow.api.notification;

import com.emlogis.model.workflow.entities.WorkflowRequest;

/**
 * Created by alexborlis on 19.02.15.
 */
public interface WorkflowNotificationFacade {


    /**
     *
     * @param tenantId
     * @param request
     * @param shiftId
     */
    void notifyAll(String tenantId, WorkflowRequest request, String shiftId);

}
