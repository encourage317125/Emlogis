package com.emlogis.common.facade.workflow.notification;

import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.exception.WorkflowClientException;

/**
 * Created by alex on 3/19/15.
 */
public interface IWflNotificationActionFacade {

    /**
     * Method process approve/decline action on request that was sent in notification
     *
     * @param code - secret code of notification
     * @param userAccount - Base64 formatted user account identifier
     * @param tenant - tenant id
     * @param wflPredefinedRoleDict
     * @return
     * @throws WorkflowClientException
     */
    String processRequest(String code, String userAccount, String tenant, String decision, WorkflowRoleDict wflPredefinedRoleDict) throws WorkflowClientException;

}
