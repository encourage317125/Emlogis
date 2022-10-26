package com.emlogis.common.services.workflow.process.update.qualify;

import com.emlogis.model.workflow.entities.WorkflowRequest;

/**
 * Created by user on 19.08.15.
 */
public interface RequestActionValidateManager {

    /**
     * Method validates if workflow request final action is still valid due to other business riles
     *
     * @param request  - {@link WorkflowRequest} Workflow request instance link
     * @return - {@link com.emlogis.common.services.workflow.process.update.qualify.RequestActionValidateManagerImpl.QualificationResult}
     */
    QualificationResult qualify(WorkflowRequest request);
}
