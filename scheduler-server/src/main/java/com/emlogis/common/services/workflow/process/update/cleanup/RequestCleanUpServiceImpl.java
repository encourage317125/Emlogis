package com.emlogis.common.services.workflow.process.update.cleanup;

import com.emlogis.common.facade.workflow.helper.ServiceHelper;
import com.emlogis.common.services.hazelcast.HazelcastClientService;
import com.emlogis.common.services.workflow.WorkflowRequestTranslator;
import com.emlogis.common.services.workflow.action.RequestLogService;
import com.emlogis.common.services.workflow.manager.WorkflowRequestManagerService;
import com.emlogis.common.services.workflow.peer.WorkflowRequestPeerService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.details.TimeOffShiftDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestLog;
import com.emlogis.model.workflow.entities.WorkflowRequestManager;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.api.notification.WorkflowNotificationFacade;
import org.apache.log4j.Logger;

import javax.ejb.*;
import java.util.*;

import static com.emlogis.workflow.WflUtil.*;
import static com.emlogis.workflow.enums.WorkflowActionDict.PEER_CANCELLED;
import static com.emlogis.workflow.enums.WorkflowActionDict.PROCESS_TERMINATED;
import static com.emlogis.workflow.enums.WorkflowRequestTypeDict.OPEN_SHIFT_REQUEST;
import static com.emlogis.workflow.enums.WorkflowRequestTypeDict.TIME_OFF_REQUEST;
import static com.emlogis.workflow.enums.WorkflowRoleDict.MANAGER;
import static com.emlogis.workflow.enums.status.RequestTechnicalStatusDict.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Created by user on 10.09.15.
 */
@Stateless
@Local(RequestCleanUpService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class RequestCleanUpServiceImpl implements RequestCleanUpService {

    private final static Logger logger = Logger.getLogger(RequestCleanUpServiceImpl.class);
    private static final Long SECONDS_PER_ACTION = 3l;

    @EJB
    private WorkflowRequestService wrService;

    @EJB
    private WorkflowRequestManagerService wmService;

    @EJB
    private WorkflowRequestPeerService wpService;

    @EJB
    private ServiceHelper serviceHelper;

    @EJB
    private RequestLogService requestLogService;

    @EJB
    private WorkflowNotificationFacade workflowNotificationFacade;

    @EJB
    private WorkflowRequestTranslator translator;

    @EJB
    private HazelcastClientService hazelcastClientService;

//    private java.util.concurrent.locks.Lock lock(String key) {
//        java.util.concurrent.locks.Lock executeLock = hazelcastClientService.getLock(key.toString());
//        if (executeLock == null) {
//            executeLock = new ReentrantLock();
//            hazelcastClientService.putLock(executeLock, key.toString());
//        }
//
//        executeLock.lock();
//        return executeLock;
//    }

//    private void release(String key) {
//        java.util.concurrent.locks.Lock executeLock = hazelcastClientService.getLock(key.toString());
//        if (executeLock != null) {
//            executeLock.unlock();
//        }
//    }

    public void cleanUp(String shiftId, String reason, PrimaryKey accountPk) {
        List<WorkflowRequest> requests = wrService.findShiftConcurrentRequests(shiftId);
        for (WorkflowRequest request : requests) {
            if (reason == null || isEmpty(reason)) {
                reason = translator.getMessage(locale(request.getInitiator()), "request.terminate.shift.reason", null);
            }
            if (accountPk == null) {
                UserAccount account = serviceHelper.systemAccount(request.getTenantId());
                if (account != null) {
                    accountPk = account.getPrimaryKey();
                } else {
                    Set<WorkflowRequestManager> managers = request.getManagers();
                    if (!managers.isEmpty()) {
                        accountPk = ((WorkflowRequestManager) new ArrayList(managers).get(0)).getManager().getPrimaryKey();
                    } else {
                        String message = translator.getMessage(locale(request.getInitiator()),
                                "request.error.has.no.managers", null);
                        throw new RuntimeException(message);
                    }
                }
            }
            //java.util.concurrent.locks.Lock lock = lock(request.getId());
            try {
            //    lock.tryLock(SECONDS_PER_ACTION, SECONDS);
                cleanUp(request, accountPk, reason);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                logger.error(" Error in WORKFLOW: actionTerminated", throwable);
                throw new RuntimeException(throwable);
            } finally {
             //   release(request.getId());
            }
        }
    }

    @Override
    public WorkflowRequest cleanUp(WorkflowRequest request, PrimaryKey actorPk) {
        try {
            String reason = translator.getMessage(locale(request.getInitiator()), "request.terminate.shift.reason", null);
            request = cleanUp(request, actorPk, reason);
            return wmService.cleanup(request);
        } catch (Throwable throwable) {
            String message = translator.getMessage(locale(request.getInitiator()), "request.error.termination", null);
            logger.error(message, throwable);
            throw new RuntimeException(message, throwable);
        }
    }

    private WorkflowRequest cleanUp(WorkflowRequest request, PrimaryKey actorPk, String reason) {
        if (isSwapOrWip(request)) {
            if ((request.getStatus().equals(ACTION_COMPLETE_SUCCESS))) {
                List<WorkflowRequest> concurrentRequests = wrService.findConcurrentSwapWipRequests(
                        request.getSubmitterShiftId(), request.getId());
                for (WorkflowRequest concRequest : concurrentRequests) {
                    terminate(concRequest, actorPk, reason);
                }
                List<WorkflowRequestPeer> concurrentPeerSwapRequests = wpService.findPeerConcurrentSwapRequests(
                        request.getSubmitterShiftId(), request.getId());
                for (WorkflowRequestPeer concPeer : concurrentPeerSwapRequests) {
                    terminatePeer(concPeer, actorPk, reason);
                }

                List<WorkflowRequestPeer> concurrentPeerWipRequests = wpService.findPeerConcurrentWipRequests(
                        requestStartDateDayStart(request), requestStartDateDayEnd(request),
                        request.getId(), actorPk.getId());

                for (WorkflowRequestPeer concPeer : concurrentPeerWipRequests) {
                    terminatePeer(concPeer, actorPk, reason);
                }
            }
        } else if (request.getRequestType().equals(TIME_OFF_REQUEST)) {
            if ((request.getStatus().equals(ACTION_COMPLETE_SUCCESS))) {
                Set<TimeOffShiftDto> shifts = serviceHelper.findTimeOffShifts(request);
                Set<String> shiftIds = new HashSet<>();
                for (TimeOffShiftDto shiftDto : shifts) {
                    shiftIds.add(shiftDto.getId());
                }
                List<WorkflowRequest> concurrentRequests = wrService.findConcurrentSwapWipRequests(
                        shiftIds, request.getId());
                for (WorkflowRequest concRequest : concurrentRequests) {
                    terminate(concRequest, actorPk, reason);
                }
                List<WorkflowRequestPeer> concurrentPeerSwapRequests = wpService.findPeerConcurrentSwapRequests(
                        shiftIds, request.getId());
                for (WorkflowRequestPeer concPeer : concurrentPeerSwapRequests) {
                    terminatePeer(concPeer, actorPk, reason);
                }

                List<WorkflowRequestPeer> concurrentPeerWipRequests = wpService.findPeerConcurrentWipRequests(
                        requestStartDateDayStart(request), requestStartDateDayEnd(request),
                        request.getId(), actorPk.getId());

                for (WorkflowRequestPeer concPeer : concurrentPeerWipRequests) {
                    terminatePeer(concPeer, actorPk, reason);
                }
            }
        } else if (request.getRequestType().equals(OPEN_SHIFT_REQUEST)) {
            if ((request.getStatus().equals(ACTION_COMPLETE_SUCCESS))) {
                List<WorkflowRequestPeer> concurrentPeerWipRequests = wpService.findPeerConcurrentWipRequests(
                        request.getSubmitterShiftStartDateTime(), request.getSubmitterShiftEndDateTime(),
                        request.getId(), actorPk.getId());
                for (WorkflowRequestPeer concPeer : concurrentPeerWipRequests) {
                    terminatePeer(concPeer, actorPk, reason);
                }
            }
        }
        return request;
    }

    private void terminatePeer(WorkflowRequestPeer concPeer, PrimaryKey actorPk, String reason) {
        try {
            String peerEmployeeId = concPeer.getRecipient().getId();
            String peerShiftId = concPeer.getPeerShiftId();
            WorkflowRequest concRequest = concPeer.getProcess();
            WorkflowRequestLog peerCancelLog = new WorkflowRequestLog(
                    concRequest, PEER_CANCELLED, MANAGER, actorPk.getId(),
                    concPeer.getPeerShiftId(), reason);
            concRequest.getActions().add(peerCancelLog);
            wpService.delete(concPeer);

            Iterator<WorkflowRequestLog> iterator = concRequest.getActions().iterator();
            while (iterator.hasNext()) {
                WorkflowRequestLog log = iterator.next();
                if (log.getActorId().equals(peerEmployeeId) && log.getShiftId().equals(peerShiftId)) {
                    requestLogService.delete(log);
                }
            }

            if (!concRequest.getRequestStatus().isFinalState()) {
                Integer peersCount = concRequest.getRecipients().size();
                List<WorkflowRequestPeer> approved = recipientsApproved(concRequest);
                List<WorkflowRequestPeer> declined = recipientsDeclined(concRequest);
                if (peersCount == 0) {
                    terminate(concRequest, actorPk, reason);
                } else if (approved.isEmpty() && declined.isEmpty()) {
                    concRequest.setStatus(PROCESS_INITIATED);
                } else if (approved.size() == peersCount) {
                    concRequest.setStatus(READY_FOR_ADMIN);
                } else if (declined.size() == peersCount) {
                    concRequest.setStatus(DECLINED_BY_PEERS);
                }
                concRequest.setRequestStatus(getRequestStatus(concRequest));
                concRequest = recalculatePeersStatuses(concRequest);
            }

            wrService.update(concRequest);
            // workflowNotificationFacade.notifyAll(concRequest.getTenantId(), concRequest, concPeer.get);
        } catch (Throwable throwable) {

        }
    }

    private WorkflowRequest terminate(WorkflowRequest concRequest, PrimaryKey actorPk, String reason) {
        WorkflowRequestLog logItem = new WorkflowRequestLog(concRequest,
                PROCESS_TERMINATED, MANAGER, actorPk.getId(),
                concRequest.getSubmitterShiftId(), reason);
        concRequest.setDeclineReason(reason);
        concRequest.getActions().add(logItem);
        concRequest.setStatus(TERMINATED);
        concRequest.setRequestStatus(getRequestStatus(concRequest));
        for (WorkflowRequestPeer peer : concRequest.getRecipients()) {
            peer.setPeerStatus(getPeerAggregatedRequestStatus(concRequest, peer));
        }
        return wrService.update(concRequest);
    }
}
