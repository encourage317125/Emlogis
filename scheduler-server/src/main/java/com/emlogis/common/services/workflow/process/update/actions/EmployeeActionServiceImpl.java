package com.emlogis.common.services.workflow.process.update.actions;

import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.schedule.ScheduleService;
import com.emlogis.common.services.schedule.ShiftService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.common.services.workflow.process.update.autoapproval.AutoApprovalManager;
import com.emlogis.common.services.workflow.process.update.logging.RequestLogManager;
import com.emlogis.common.services.workflow.process.update.logging.RequestLogResult;
import com.emlogis.common.services.workflow.process.update.postprocess.ActionPostProcessor;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.enums.status.RequestTechnicalStatusDict;
import com.emlogis.workflow.exception.WorkflowServerException;
import org.apache.log4j.Logger;

import javax.ejb.*;

import static com.emlogis.workflow.WflUtil.*;
import static com.emlogis.workflow.enums.WorkflowActionDict.*;
import static com.emlogis.workflow.enums.status.RequestTechnicalStatusDict.READY_FOR_ACTION;
import static com.emlogis.workflow.enums.status.RequestTechnicalStatusDict.READY_FOR_ADMIN;

/**
 * Created by user on 19.08.15.
 */
@Stateless
@Local(EmployeeActionService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class EmployeeActionServiceImpl implements EmployeeActionService {

    private final static Logger logger = Logger.getLogger(SystemActionServiceImpl.class);

    @EJB
    private AutoApprovalManager autoApprovalManager;
    @EJB
    private RequestLogManager rlts;
    @EJB
    private ActionPostProcessor postProcessService;
    @EJB
    private WorkflowRequestService wrService;
    @EJB
    private EmployeeService employeeService;
    @EJB
    private ShiftService shiftService;
    @EJB
    private ScheduleService scheduleService;

    @Override
    public WorkflowRequest initiatorProceed(
            WorkflowRequest request,
            String comment,
            Boolean autoApprovalCheck
    ) throws WorkflowServerException {
        logger.debug("PROCEED PROCESS INSTANCE " + request.toString());
        //track action log item
        RequestLogResult lr = rlts.originatorProceed(request, comment);
        request = lr.getRequest();
        //update statuses after change
        request = postProcessService.updateStatuses(lr.getRequest());
        //check if auto -approval options are actual
        if (autoApprovalCheck) {
            request = autoApprovalManager.execute(ORIGINATOR_PROCEED, request, null, comment, request.getInitiator().getPrimaryKey());
        }
        //process commentary, history and cleanup data
        return postProcessService.processCommentaryHistoryCleanupNotification(lr.getLog(), request,
                request.getInitiator().reportName(),
                request.getInitiator().getPrimaryKey(), comment, true);
    }

    @Override
    public WorkflowRequest processPeerApprove(
            WorkflowRequest request, //instance to make action
            WorkflowRequestPeer peer, //peer that makes action
            String comment//commentary for action
    ) throws WorkflowServerException {
        RequestLogResult lr = rlts.peerApprove(request, peer, comment);
        request = lr.getRequest();

        if (isLastPeerAttender(lr.getRequest(), PEER_APPROVE)) {
            logger.info("PROCESS " + lr.getRequest().toString() + " READY TO ADMIN APPROVAL");
            lr.getRequest().setStatus(READY_FOR_ADMIN);
        }
        request = postProcessService.updateStatuses(request);
        request = autoApprovalManager.execute(PEER_APPROVE, request, peer, comment, peer.getRecipient().getPrimaryKey());
        return postProcessService.processCommentaryHistoryCleanupNotification(lr.getLog(), request, peer, comment,
                peer.getRecipient().reportName(),
                peer.getRecipient().getPrimaryKey(), true);
    }

    @Override
    public WorkflowRequest processPeerDecline(
            WorkflowRequest request,
            WorkflowRequestPeer peer,
            String comment
    ) throws WorkflowServerException {
        RequestLogResult lr = rlts.peerDecline(request, peer, comment);
        request = lr.getRequest();

        if (isLastPeerAttender(request, PEER_DECLINE)) {
            if (recipientsApproved(request).isEmpty()) {
                request.setStatus(RequestTechnicalStatusDict.DECLINED_BY_PEERS);
            } else {
                request.setStatus(READY_FOR_ADMIN);
            }
        }
        return postProcessService.processStatusCommentaryHistoryCleanupNotification(lr.getLog(), request, peer, comment,
                peer.getRecipient().reportName(), peer.getRecipient().getPrimaryKey(), true);
    }

    @Override
    public WorkflowRequest managerApprove(
            WorkflowRequest request,
            String managerName,
            PrimaryKey managerUserAccountPk,
            String comment,
            WorkflowRequestPeer chosenPeer
    ) throws WorkflowServerException {
        RequestLogResult lr = rlts.managerApprove(request, chosenPeer, comment, managerUserAccountPk);
        request = lr.getRequest();

        if (request.getStatus().equals(READY_FOR_ADMIN) && doesNotContainErrors(request)) {
            request.setStatus(READY_FOR_ACTION);
        }
        request.setChosenPeerId(chosenPeer != null ? chosenPeer.getId() : null);
        request = postProcessService.updateStatuses(request);
        return postProcessService.processCommentaryHistoryCleanupNotification(lr.getLog(), request, chosenPeer, comment,
                managerName, managerUserAccountPk, false);
    }

    @Override
    public WorkflowRequest managerDecline(
            WorkflowRequest request,
            String managerName,
            PrimaryKey managerUserAccountPk,
            String comment
    ) throws WorkflowServerException {
        RequestLogResult lr = rlts.managerDecline(request, managerUserAccountPk, comment);
        request = lr.getRequest();
        request.setStatus(RequestTechnicalStatusDict.DECLINED_BY_MANAGERS);
        return postProcessService.processStatusCommentaryHistoryCleanupNotification(lr.getLog(), request, null,
                comment, managerName, managerUserAccountPk, true);
    }
}
