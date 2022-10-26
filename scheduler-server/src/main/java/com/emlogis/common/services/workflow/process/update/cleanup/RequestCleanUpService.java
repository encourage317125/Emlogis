package com.emlogis.common.services.workflow.process.update.cleanup;

import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.entities.WorkflowRequest;

/**
 * Created by user on 10.09.15.
 */
public interface RequestCleanUpService {

    WorkflowRequest cleanUp(WorkflowRequest request, PrimaryKey actorPk);

    void cleanUp(String shiftId, String reason, PrimaryKey accountPk);
}
