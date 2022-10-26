package com.emlogis.common.services.workflow.action;

import com.emlogis.common.services.common.GeneralJPARepository;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.entities.WorkflowRequestLog;

/**
 * Created by alexborlis on 19.02.15.
 */
public interface RequestLogService extends GeneralJPARepository<WorkflowRequestLog, PrimaryKey> {

    WorkflowRequestLog create(WorkflowRequestLog workflowRequestLog);

    WorkflowRequestLog update(WorkflowRequestLog workflowRequestLog);
}
