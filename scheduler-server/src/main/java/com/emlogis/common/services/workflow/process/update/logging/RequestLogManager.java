package com.emlogis.common.services.workflow.process.update.logging;

import com.emlogis.common.services.common.GeneralJPARepository;
import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestLog;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;

/**
 * Created by user on 19.08.15.
 */
public interface RequestLogManager extends GeneralJPARepository<WorkflowRequestLog, PrimaryKey> {

    RequestLogResult originatorProceed(WorkflowRequest request, String comment);

    RequestLogResult peerApprove(WorkflowRequest request, WorkflowRequestPeer peer, String comment);

    RequestLogResult managerApprove(WorkflowRequest request, WorkflowRequestPeer peer, String comment, PrimaryKey managerAccountPk);

    RequestLogResult peerDecline(WorkflowRequest request, WorkflowRequestPeer peer, String comment);

    RequestLogResult managerDecline(WorkflowRequest request, PrimaryKey managerAccountPk, String comment);

    RequestLogResult requestTerminate(WorkflowRequest request, PrimaryKey accountPk, String reason);

    RequestLogResult systemAction(WorkflowRequest request, ResultPair resultPair);
}
