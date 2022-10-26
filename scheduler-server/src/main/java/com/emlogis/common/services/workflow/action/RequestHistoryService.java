package com.emlogis.common.services.workflow.action;

import com.emlogis.model.workflow.entities.WorkflowRequestLog;

/**
 * Created by user on 13.07.15.
 */
public interface RequestHistoryService {

    byte[] log(WorkflowRequestLog action);
}
