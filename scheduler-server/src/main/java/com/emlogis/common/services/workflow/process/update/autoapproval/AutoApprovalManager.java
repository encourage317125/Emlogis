package com.emlogis.common.services.workflow.process.update.autoapproval;

import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.enums.WorkflowActionDict;

/**
 * Created by user on 19.08.15.
 */
public interface AutoApprovalManager {

    WorkflowRequest execute(
            WorkflowActionDict actionDict,
            WorkflowRequest instance,
            WorkflowRequestPeer peerInstance,
            String comment,
            PrimaryKey userPk
    );
}
