package com.emlogis.common.services.workflow.process.update.logging;

import com.emlogis.common.services.common.PrimaryKeyJPARepositoryServiceImpl;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestLog;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.enums.WorkflowActionDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;

import javax.ejb.*;

import static com.emlogis.workflow.WflUtil.currentDateTime;
import static com.emlogis.workflow.enums.WorkflowActionDict.ACTION_SUCCESS;
import static com.emlogis.workflow.enums.WorkflowActionDict.ERROR_IN_ACTION;
import static com.emlogis.workflow.enums.status.RequestTechnicalStatusDict.ACTION_COMPLETE_SUCCESS;
import static com.emlogis.workflow.enums.status.RequestTechnicalStatusDict.ACTION_COMPLETE_WITH_ERRORS;

/**
 * Created by user on 19.08.15.
 */
@Stateless
@Local(RequestLogManager.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class RequestLogManagerImpl extends PrimaryKeyJPARepositoryServiceImpl<WorkflowRequestLog>
        implements RequestLogManager {

    @EJB
    private WorkflowRequestService wrService;

    @Override
    public Class<WorkflowRequestLog> getEntityClass() {
        return WorkflowRequestLog.class;
    }

    public RequestLogResult originatorProceed(
            WorkflowRequest request,
            String comment
    ) {
        WorkflowRequestLog action = new WorkflowRequestLog(request.getTenantId());
        action.setAction(WorkflowActionDict.ORIGINATOR_PROCEED);
        action.setActorId(request.getInitiator().getId());
        action.setRole(WorkflowRoleDict.ORIGINATOR);
        action.setComment(comment);
        action.setPrevRequestStatus(request.getRequestStatus());
        action.setProcessInstance(request);
        action.setShiftId(request.getSubmitterShiftId());
        request.getActions().add(action);
        return new RequestLogResult(wrService.update(request), action);
    }

    public RequestLogResult peerApprove(
            WorkflowRequest request,
            WorkflowRequestPeer peer,
            String comment
    ) {
        WorkflowRequestLog action = new WorkflowRequestLog(request.getTenantId());
        action.setAction(WorkflowActionDict.PEER_APPROVE);
        action.setActorId(peer.getRecipient().getId());
        action.setRole(WorkflowRoleDict.PEER);
        action.setComment(comment);
        action.setProcessInstance(request);
        if (peer.hasShift()) {
            action.setShiftId(peer.getPeerShiftId());
        }
        action.setPrevRequestStatus(request.getRequestStatus());
        action.setPrevPeerStatus(peer.getPeerStatus());
        request.getActions().add(action);
        return new RequestLogResult(wrService.update(request), action);
    }

    public RequestLogResult managerApprove(
            WorkflowRequest request,
            WorkflowRequestPeer peer,
            String comment,
            PrimaryKey managerAccountPk
    ) {
        WorkflowRequestLog action = new WorkflowRequestLog(request.getTenantId());
        action.setAction(WorkflowActionDict.MANAGER_APPROVE);
        action.setActorId(managerAccountPk.getId());
        action.setRole(WorkflowRoleDict.MANAGER);
        action.setComment(comment);
        action.setProcessInstance(request);
        action.setPrevRequestStatus(request.getRequestStatus());
        if (peer != null) {
            if (peer.getPeerShiftId() != null) {
                action.setShiftId(peer.getPeerShiftId());
            } else if (request.hasShift()) {
                action.setShiftId(request.getSubmitterShiftId());
            }
        }
        request.getActions().add(action);
        return new RequestLogResult(wrService.update(request), action);
    }

    public RequestLogResult managerDecline(
            WorkflowRequest request,
            PrimaryKey managerAccountPk,
            String comment
    ) {
        WorkflowRequestLog action = new WorkflowRequestLog(request.getTenantId());
        action.setAction(WorkflowActionDict.MANAGER_DECLINE);
        action.setActorId(managerAccountPk.getId());
        action.setRole(WorkflowRoleDict.MANAGER);
        action.setComment(comment);
        action.setProcessInstance(request);
        action.setPrevRequestStatus(request.getRequestStatus());
        request.getActions().add(action);
        return new RequestLogResult(wrService.update(request), action);
    }

    public RequestLogResult peerDecline(
            WorkflowRequest request,
            WorkflowRequestPeer peer,
            String comment
    ) {
        WorkflowRequestLog action = new WorkflowRequestLog(request.getTenantId());
        action.setAction(WorkflowActionDict.PEER_DECLINE);
        action.setActorId(peer.getRecipient().getId());
        action.setRole(WorkflowRoleDict.PEER);
        action.setComment(comment);
        action.setProcessInstance(request);
        action.setPrevRequestStatus(request.getRequestStatus());
        action.setPrevPeerStatus(peer.getPeerStatus());
        if (peer.hasShift()) {
            action.setShiftId(peer.getPeerShiftId());
        }
        request.getActions().add(action);
        return new RequestLogResult(wrService.update(request), action);
    }


    public RequestLogResult requestTerminate(
            WorkflowRequest request,
            PrimaryKey accountPk,
            String reason
    ) {
        WorkflowRequestLog actionLog = new WorkflowRequestLog(request, WorkflowActionDict.PROCESS_TERMINATED,
                WorkflowRoleDict.MANAGER, accountPk.getId(), request.getSubmitterShiftId(), reason);
        actionLog.setComment(reason);
        request.getActions().add(actionLog);
        return new RequestLogResult(wrService.update(request), actionLog);
    }

    public RequestLogResult systemAction(WorkflowRequest request, ResultPair resultPair) {
        WorkflowRequestLog action = new WorkflowRequestLog(request.getTenantId());
        action.setProcessInstance(request);
        action.setComment(resultPair.getResult() ? "" + currentDateTime() : resultPair.getMessage());
        action.setAction(resultPair.getResult() ? ACTION_SUCCESS : ERROR_IN_ACTION);
        action.setRole(WorkflowRoleDict.ORIGINATOR);
        action.setActorId(request.getInitiator().getId());
        action.setShiftId(request.getSubmitterShiftId());
        action.setPrevRequestStatus(request.getRequestStatus());
        request.setStatus(resultPair.getResult() ? ACTION_COMPLETE_SUCCESS : ACTION_COMPLETE_WITH_ERRORS);
        action.setNewRequestStatus(request.getRequestStatus());
        request.getActions().add(action);
        return new RequestLogResult(wrService.update(request), action);
    }


}
