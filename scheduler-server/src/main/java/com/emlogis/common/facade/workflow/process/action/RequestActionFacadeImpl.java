package com.emlogis.common.facade.workflow.process.action;

import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.workflow.helper.ServiceHelper;
import com.emlogis.common.services.workflow.TranslationParam;
import com.emlogis.common.services.workflow.WorkflowRequestTranslator;
import com.emlogis.common.services.workflow.action.RequestLogService;
import com.emlogis.common.services.workflow.peer.WorkflowRequestPeerService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.common.services.workflow.process.update.RequestActionService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.decision.*;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.WflUtil;
import com.emlogis.workflow.api.notification.WorkflowNotificationFacade;
import com.emlogis.workflow.enums.WorkflowRequestDecision;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.enums.status.RequestTechnicalStatusDict;
import com.emlogis.workflow.exception.WorkflowServerException;
import org.apache.log4j.Logger;

import javax.ejb.*;
import java.util.List;
import java.util.Set;

import static com.emlogis.workflow.WflUtil.*;
import static com.emlogis.workflow.enums.WorkflowRequestDecision.APPROVE;
import static com.emlogis.workflow.enums.WorkflowRequestDecision.DECLINE;
import static com.emlogis.workflow.enums.WorkflowShiftManagerActionDict.*;
import static com.emlogis.workflow.enums.status.RequestTechnicalStatusDict.ACTION_COMPLETE_WITH_ERRORS;
import static com.emlogis.workflow.enums.status.WorkflowRequestStatusDict.DECLINED;
import static com.emlogis.workflow.enums.status.WorkflowRequestStatusDict.PEER_DECLINED;
import static com.emlogis.workflow.exception.ExceptionCode.ACTION_ROLE_UNIDENTIFIED;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Created by user on 21.08.15.
 */
@Stateless
@Local(value = RequestActionFacade.class)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class RequestActionFacadeImpl implements RequestActionFacade {

    private final static Logger logger = Logger.getLogger(RequestActionFacadeImpl.class);

    @EJB
    private WorkflowRequestTranslator translator;

    @EJB
    private WorkflowRequestPeerService peerService;

    @EJB
    private WorkflowRequestService wrService;

    @EJB
    private RequestActionService instanceUpdateService;

    @EJB
    private ServiceHelper serviceHelper;

    @EJB
    private RequestActionService requestActionService;

    @EJB
    private WorkflowNotificationFacade notificationFacade;

    @EJB
    private RequestLogService requestLogService;

    @Override
    public WorkflowDecisionResultInfoDto processManagerAction(
            WorkflowRequest request,
            WorkflowRequestDecision decision,
            WorkflowDecisionDto decisionDto,
            UserAccount account
    ) {
        if (decision.equals(APPROVE)) {
            //first off all process manager decision
            WorkflowRequestPeer chosenPeer = approveChosenPeer(request, decisionDto, account);
            request = approveAction(request.getPrimaryKey(), chosenPeer, decisionDto, account);
        } else if (decision.equals(DECLINE)) {
            instanceUpdateService.managerDecline(request, decisionDto.getComment(),
                    account.reportName(), account.getPrimaryKey());
        }
        return new WorkflowDecisionResultInfoDto(getRequestStatus(request).name(), null,
                account.getTenantId(), account.getId());
    }

    @Override
    public WorkflowDecisionResultListDto processPeerAction(
            WorkflowRequest request, WorkflowRequestDecision decision, WorkflowDecisionDto workflowDecisionDto, Employee employee
    ) {
        WorkflowDecisionResultListDto result = new WorkflowDecisionResultListDto(request.getId());
        PeerDecisionDto peerDecisionDto = (PeerDecisionDto) workflowDecisionDto;
        List<String> shiftIds = ((PeerDecisionDto) workflowDecisionDto).getShiftIdList();
        if (WflUtil.isShiftSwap(request)) {
            return processSwapPeerAction(decision, employee, request, result, peerDecisionDto, shiftIds);
        } else {
            List<WorkflowRequestPeer> peers = peerService.findPeers(request.getPrimaryKey(), employee);
            if (decision.equals(APPROVE)) {
                for (WorkflowRequestPeer peer : peers) {
                    result.setCount(result.getCount() + 1);
                    request = instanceUpdateService.processPeerApprove(request, peer, peerDecisionDto.getComment());
                    result.getResultList().add(new WorkflowDecisionResultInfoDto(
                            peer.getPeerStatus().name(), getPeerAggregatedRequestStatus(request, peer).name(),
                            employee.getTenantId(), employee.getId()));
                }
            } else if (decision.equals(DECLINE)) {
                for (WorkflowRequestPeer peer : peers) {
                    instanceUpdateService.processPeerDecline(request, peer, workflowDecisionDto.getComment());
                }
            }
        }
        result.setRequestStatus(request.getRequestStatus().name());
        return result;
    }

    private WorkflowDecisionResultListDto processSwapPeerAction(
            WorkflowRequestDecision decision,
            Employee employee,
            WorkflowRequest request,
            WorkflowDecisionResultListDto result,
            PeerDecisionDto peerDecisionDto,
            List<String> shiftIds
    ) {
        if (shiftIds == null || shiftIds.isEmpty()) {
            List<WorkflowRequestPeer> peers = peerService.findPeers(request.getPrimaryKey(), employee);
            superSwapPeerAction(decision, employee, request, result, peerDecisionDto, peers);
        } else {
            for (String shiftId : shiftIds) {
                List<WorkflowRequestPeer> peers = peerService.findPeers(request.getPrimaryKey(), employee, shiftId);
                if (decision.equals(APPROVE)) {
                    for (WorkflowRequestPeer peer : peers) {
                        result.setCount(result.getCount() + 1);
                        request = instanceUpdateService.processPeerApprove(request, peer, peerDecisionDto.getComment());
                        result.getResultList().add(new WorkflowDecisionResultInfoDto(
                                peer.getPeerStatus().name(), getPeerAggregatedRequestStatus(request, peer).name(),
                                employee.getTenantId(), employee.getId()));
                    }
                    Set<WorkflowRequestPeer> idlePeersOfEmployee = request.getIdlePeersOnEmployee(employee);
                    for (WorkflowRequestPeer peer : idlePeersOfEmployee) {
                        if (peerDidNotActYet(request, peer)) {
                            request = instanceUpdateService.processPeerDecline(request, peer, "auto decline");
                        }
                    }
                } else if (decision.equals(DECLINE)) {
                    for (WorkflowRequestPeer peer : peers) {
                        instanceUpdateService.processPeerDecline(request, peer, peerDecisionDto.getComment());
                    }
                }

            }
        }
        result.setRequestStatus(request.getRequestStatus().name());
        return result;
    }

    /**
     * Method that provides logic for super powered approve/decline for ShiftSwap peer action
     * in case api did not receive shift id to choose swap option logic must apply approve/decline
     * to all peer options
     *
     * @param decision        {@link WorkflowDecisionDto} APPROVE/DECLINE
     * @param employee        {@link Employee} employee as peer role
     * @param request         {@link WorkflowRequest} request instance
     * @param result          {@link WorkflowDecisionResultListDto} actionn result representation
     * @param peerDecisionDto {@link PeerDecisionDto} API input DTO
     * @param peers           {@link WorkflowRequestPeer}'s list of employee peer roles
     * @return
     */
    private WorkflowDecisionResultListDto superSwapPeerAction(
            WorkflowRequestDecision decision,
            Employee employee,
            WorkflowRequest request,
            WorkflowDecisionResultListDto result,
            PeerDecisionDto peerDecisionDto,
            List<WorkflowRequestPeer> peers
    ) {
        if (decision.equals(APPROVE)) {
            for (WorkflowRequestPeer peer : peers) {
                if (peerDidNotActYet(request, peer)) {
                    request = requestActionService.processPeerApprove(request, peer, peerDecisionDto.getComment());
                }
            }
        } else {
            for (WorkflowRequestPeer peer : peers) {
                result.setCount(result.getCount() + 1);
                peer.setPeerStatus(PEER_DECLINED);
                if (employeeApprovedAsPeer(request, peer)) {
                    switchApprovalForDecline(request, peer);
                } else if (peerDidNotActYet(request, peer)) {
                    request = requestActionService.processPeerDecline(request, peer, peerDecisionDto.getComment());
                    // request.getActions().add(requestLogService.create(makePeerDeclined(peer, peerDecisionDto.getComment())));
                }
                peerService.update(peer);
                result.getResultList().add(new WorkflowDecisionResultInfoDto(
                        peer.getPeerStatus().name(), getPeerAggregatedRequestStatus(request, peer).name(),
                        employee.getTenantId(), employee.getId()));
            }
        }
        wrService.update(request);
        result.setRequestStatus(request.getRequestStatus().name());
        return result;
    }

    public WorkflowRequest skipOtherPeersForWipOrSwap(WorkflowRequest instance) {
        //here we need to process case when not all off peers made their decision yet, but at least one
        if (isSwapOrWip(instance)) {
            Boolean instanceIsNotINAdminPendingStatus = !instance.getStatus().equals(RequestTechnicalStatusDict.READY_FOR_ADMIN);
            Boolean atLeastOnePeerApprovedRequest = recipientsApproved(instance).size() > 0;
            if (atLeastOnePeerApprovedRequest && instanceIsNotINAdminPendingStatus) {
                // workflowService.approveTask(instance.getEngineId(), WorkflowRoleDict.PEER);
                instance.setStatus(RequestTechnicalStatusDict.READY_FOR_ADMIN);
                instance = wrService.update(instance);
            }
        }

        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String approve(
            WorkflowRequest request,
            UserAccount account,
            WorkflowRoleDict role
    ) throws WorkflowServerException {
        try {
            switch (role) {
                case PEER: {
                    List<WorkflowRequestPeer> peers = peerService.findPeers(request.getPrimaryKey(), account.getEmployee());
                    for (WorkflowRequestPeer peer : peers) {
                        instanceUpdateService.processPeerApprove(request, peer, null);
                    }
                    break;
                }
                case MANAGER: {
                    //todo:: fix with chosen recipient
                    //wrService.managerApprove(instance, employee, null, choosenEmployee);
                    break;
                }
                default:
                    TranslationParam[] params = {new TranslationParam("role", role.name())};
                    String message = translator.getMessage(request.locale(), "request.unknown.role", params);
                    throw new WorkflowServerException(ACTION_ROLE_UNIDENTIFIED, message);
            }
            return toSuccessString(account, request, "APPROVE");
        } catch (Exception error) {
            TranslationParam[] params = {new TranslationParam("error", error.getMessage())};
            String message = translator.getMessage(request.locale(), "request.notification.approve", params);
            logger.error(message, error);
            return toErrorString(message, account);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String deny(
            WorkflowRequest request,
            UserAccount account,
            WorkflowRoleDict role
    ) throws WorkflowServerException {
        try {
            switch (role) {
                case PEER: {
                    List<WorkflowRequestPeer> peers = peerService.findPeers(request.getPrimaryKey(), account.getEmployee());
                    for (WorkflowRequestPeer peer : peers) {
                        instanceUpdateService.processPeerDecline(request, peer, null);
                    }
                    break;
                }
                case MANAGER: {
                    instanceUpdateService.managerDecline(request, null, account.reportName(), account.getPrimaryKey());
                    break;
                }
                default:
                    TranslationParam[] params = {new TranslationParam("role", role.name())};
                    String message = translator.getMessage(request.locale(), "request.unknown.role", params);
                    throw new WorkflowServerException(ACTION_ROLE_UNIDENTIFIED, message);
            }
            return toSuccessString(account, request, "APPROVE");
        } catch (Exception error) {
            TranslationParam[] params = {new TranslationParam("error", error.getMessage())};
            String message = translator.getMessage(request.locale(), "request.notification.decline", params);
            logger.error(message, error);
            return toErrorString(message, account);
        }
    }

    @Override
    public void processPeerCancelRequest(
            WorkflowRequest request,
            Employee requestEmployee
    ) {
        if (request.getRequestStatus().isFinalState()) {
            List<WorkflowRequestPeer> peers = findAllEligiblePeersThatApproved(request, requestEmployee);
            for (WorkflowRequestPeer peer : peers) {
                switchApprovalForDecline(request, peer);
            }
            request.setRequestStatus(getRequestStatus(request));
            for (WorkflowRequestPeer peer : request.getRecipients()) {
                peer.setPeerStatus(WflUtil.getPeerAggregatedRequestStatus(request, peer));
            }
            wrService.update(request);
        }
    }

    public String toSuccessString(UserAccount account, WorkflowRequest request, String resolutionStr) {
        StringBuilder builder = new StringBuilder();
        return builder.
                append("<!DOCTYPE HTML>\n").
                append("<html>\n").
                append("<head>\n").
                append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n").
                append("<title>Dear " + account.reportName() + "</title>\n").
                append("<p>You have " + resolutionStr + " request for: " + request.getProtoProcess().getName() + " </p>\n").
                append("<p>That was sent to you by " + request.getInitiator().reportName() + " </p>\n").
                append("</body>\n").
                append("</html>\n").
                toString();
    }

    public String toErrorString(String errorMessage, UserAccount account) {
        StringBuilder builder = new StringBuilder();
        return builder.
                append("<!DOCTYPE HTML>\n").
                append("<html>\n").
                append("<head>\n").
                append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n").
                append("<title>Dear " + account.reportName() + "</title>\n").
                append("<p>An error occurred " + errorMessage + " </p>\n").
                append("<p>So we can not process your request </p>\n").
                append("</body>\n").
                append("</html>\n").
                toString();
    }

    private WorkflowRequestPeer approveChosenPeer(
            WorkflowRequest request,
            WorkflowDecisionDto workflowDecisionDto,
            UserAccount account
    ) {
        WorkflowRequestPeer chosenPeer = null;
        if (isSwapOrWip(request)) {
            if (request.getRequestType().equals(WorkflowRequestTypeDict.WIP_REQUEST)) {
                Employee chosenEmployee = serviceHelper.employee(request.getTenantId(),
                        ((WipDecisionDto) workflowDecisionDto).getChosenEmployee());
                chosenPeer = peerService.findPeer(request.getPrimaryKey(), chosenEmployee, "EMPTY");
            } else {
                Employee chosenEmployee = serviceHelper.employee(
                        request.getTenantId(), ((SwapDecisionDto) workflowDecisionDto).getChosenEmployee());
                chosenPeer = peerService.findPeer(request.getPrimaryKey(), chosenEmployee,
                        ((SwapDecisionDto) workflowDecisionDto).getShiftId());
            }
            request = skipOtherPeersForWipOrSwap(request);
            instanceUpdateService.managerApprove(request, workflowDecisionDto.getComment(), chosenPeer,
                    account.reportName(), account.getPrimaryKey());
        } else {
            instanceUpdateService.managerApprove(request, workflowDecisionDto.getComment(), null,
                    account.reportName(), account.getPrimaryKey());
        }
        return chosenPeer;
    }

    private WorkflowRequest approveAction(
            PrimaryKey requestPk,
            WorkflowRequestPeer chosenPeer,
            WorkflowDecisionDto decisionDto,
            UserAccount account
    ) {
        WorkflowRequest request = wrService.find(requestPk);
        switch (request.getRequestType()) {
            case AVAILABILITY_REQUEST: {
                AvailabilityDecisionDto dto = (AvailabilityDecisionDto) decisionDto;
                return requestActionService.availabilityAction(request, dto.getComment(),
                        account.reportName(), account.getPrimaryKey());
            }
            case TIME_OFF_REQUEST: {
                TimeOffDecisionDto dto = (TimeOffDecisionDto) decisionDto;
                StringBuffer declineReasons = new StringBuffer();
                if (checkNumberOfAssignOptionsMoreThenOne(dto.getDecisionActionList())) {
                    request.setRequestStatus(DECLINED);
                    request.setStatus(ACTION_COMPLETE_WITH_ERRORS);
                    request.setDeclineReason("More then one assign/post action on manager decision!");
                    request = recalculatePeersStatuses(request);
                    request = wrService.update(request);
                } else {
                    for (ShiftDecisionAction decisionAction : dto.getDecisionActionList()) {
                        if (decisionAction.getAction().equals(DROP_SHIFT)) {
                            request = requestActionService.dropShift(request, decisionAction, dto.getComment(),
                                    account.reportName(), account.getPrimaryKey());
                        } else if (decisionAction.getAction().equals(ASSIGN_SHIFT)) {
                            request = requestActionService.assignShiftTo(request, decisionAction, dto.getComment(),
                                    account.reportName(), account.getPrimaryKey());
                        } else if (decisionAction.getAction().equals(POST_AS_OPEN_SHIFT)) {
                            request = requestActionService.postOpenShifts(request, decisionAction, dto.getComment(),
                                    account.reportName(), account.getPrimaryKey());
                        }
                        if (request.getDeclineReason() != null && !declineReasons.toString().contains(request.getDeclineReason())) {
                            declineReasons.append(isEmpty(request.getDeclineReason()) ? "" : request.getDeclineReason());
                        }
                    }
                }
                if (dto.getDecisionActionList().isEmpty()) {
                    request = requestActionService.timeOffWithoutAction(request, dto.getComment(), account.reportName(),
                            account.getPrimaryKey());
                } else {
                    request.setDeclineReason(declineReasons.toString());
                }
                return request;
            }
            case SHIFT_SWAP_REQUEST: {
                request.setChosenPeerId(chosenPeer.getId());
                request = wrService.update(request);
                return requestActionService.shiftSwapAction(request, decisionDto.getComment(), chosenPeer,
                        account.reportName(), account.getPrimaryKey());
            }
            case WIP_REQUEST: {
                request.setChosenPeerId(chosenPeer.getId());
                request = wrService.update(request);
                return requestActionService.wipAction(request, decisionDto.getComment(), chosenPeer,
                        account.reportName(), account.getPrimaryKey());
            }
            case OPEN_SHIFT_REQUEST: {
                OpenShiftDecisionDto dto = (OpenShiftDecisionDto) decisionDto;
                return requestActionService.openShiftAction(request, dto.getComment(),
                        account.reportName(), account.getPrimaryKey());
            }
        }
        throw new ValidationException("Can not identify workflow request typ[e!");
    }
}
