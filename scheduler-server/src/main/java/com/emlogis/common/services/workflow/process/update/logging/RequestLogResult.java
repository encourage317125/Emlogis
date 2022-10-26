package com.emlogis.common.services.workflow.process.update.logging;

import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestLog;

/**
 * Created by user on 26.08.15.
 */
public class RequestLogResult {

    private WorkflowRequest request;
    private WorkflowRequestLog log;

    public RequestLogResult(WorkflowRequest request, WorkflowRequestLog log) {
        this.request = request;
        this.log = log;
    }

    public WorkflowRequest getRequest() {
        return request;
    }

    public WorkflowRequestLog getLog() {
        return log;
    }
}
