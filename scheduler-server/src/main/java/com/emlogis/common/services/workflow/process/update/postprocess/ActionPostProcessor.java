package com.emlogis.common.services.workflow.process.update.postprocess;

import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestLog;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;

/**
 * Created by user on 19.08.15.
 */
public interface ActionPostProcessor {

    WorkflowRequest processActionStatusCommentaryCleanupNotification(
            WorkflowRequest request,
            ResultPair resultPair,
            PrimaryKey actorPk,
            String actorName,
            String commentaryStr,
            Boolean notify);

    WorkflowRequest processCommentaryHistoryCleanupNotification(
            WorkflowRequestLog action,
            WorkflowRequest request,
            WorkflowRequestPeer peer,
            String comment,
            String actorName,
            PrimaryKey actorPk,
            Boolean notify);

    WorkflowRequest processCommentaryHistoryCleanupNotification(
            WorkflowRequestLog action, WorkflowRequest request, String actorName, PrimaryKey actorPk, String comment,
            Boolean notify);

    WorkflowRequest processActionResults(WorkflowRequest instance, ResultPair resultPair);

    /**
     * Updates statuses for system actions
     * @param request
     * @param result
     * @return
     */
    WorkflowRequest updateStatuses(WorkflowRequest request, Object result);

    /**
     * Updates statuses for employee actions
     * @param request
     * @return
     */
    WorkflowRequest updateStatuses(WorkflowRequest request);

    WorkflowRequest processStatusCommentaryHistoryCleanupNotification(
            WorkflowRequestLog log,
            WorkflowRequest request,
            WorkflowRequestPeer peer,
            String comment,
            String actorName,
            PrimaryKey actorPk,
            Boolean notify);
}
