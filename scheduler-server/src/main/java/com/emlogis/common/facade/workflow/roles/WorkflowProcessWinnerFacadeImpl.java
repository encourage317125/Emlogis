package com.emlogis.common.facade.workflow.roles;

import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.schedule.ShiftService;
import com.emlogis.common.services.workflow.WorkflowRequestTranslator;
import com.emlogis.workflow.api.identification.RequestRoleProxy;
import org.apache.log4j.Logger;

import javax.ejb.*;

/**
 * Created by user on 08.05.15.
 */
@Stateless
@Local(value = WorkflowProcessWinnerFacade.class)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class WorkflowProcessWinnerFacadeImpl implements WorkflowProcessWinnerFacade {

    private final Logger logger = Logger.getLogger(WorkflowProcessWinnerFacadeImpl.class);

    @EJB
    private WorkflowRequestTranslator translator;

    @EJB
    private ShiftService shiftService;

    @EJB
    private EmployeeService employeeService;

    @EJB
    private RequestRoleProxy roleAssignmentFacade;

//    //todo:: implement strategy dynamically
//    @Override
//    public Employee findWinner(WorkflowRequest request) throws WorkflowServerException {
//            switch (request.getProtoProcess().getType().getType()) {
//                case SHIFT_SWAP_REQUEST:
//                    return identifyShiftSwapWinner(request);
//                case WIP_REQUEST:
//                    return identifyWorkInPlaceWinner(request);
//                case OPEN_SHIFT_REQUEST:
//                    return identifyShortOpenShiftRequestWinner(request);
//                case TIME_OFF_REQUEST:
//                    return identifyPtoRequestWinner(request);
//                case AVAILABILITY_REQUEST:
//                    return identifyAvailabilityRequestWinner(request);
//                default:
//                    String message = translator.getMessage(request.locale(), "request.unknown.type",
//                            errorPrm(request.getProtoProcess().getType().getType().getClass()));
//                    throw new WorkflowServerException(REQUEST_TYPE_UNIDENTIFIED, message);
//            }
//    }
//
//    @Override
//    public WorkflowRequestPeer findWinnerRecipient(WorkflowRequest request) throws WorkflowServerException {
//        Employee employee = findWinner(request);
//        Set<WorkflowRequestPeer> recipients = request.getRecipients();
//        for (WorkflowRequestPeer recipient : recipients) {
//            if (recipient.getRecipient().getId().equals(employee.getId())) {
//                return recipient;
//            }
//        }
//        String message = translator.getMessage(request.locale(), "request.unknown.type",
//                errorPrm(request.getProtoProcess().getType().getType().getClass()));
//        throw new WorkflowServerException(REQUEST_TYPE_UNIDENTIFIED, message);
//    }
//
//    private Employee identifyCustomRequestWinner(WorkflowRequest instance) {
//        return null;
//    }
//
//    private Employee identifyAvailabilityRequestWinner(WorkflowRequest instance) {
//        return null;
//    }
//
//    private Employee identifyPtoRequestWinner(WorkflowRequest instance) {
//        return null;
//    }
//
//    private Employee identifyShortOpenShiftRequestWinner(WorkflowRequest instance) {
//        return instance.getInitiator();
//    }
//
//    private Employee identifyOpenShiftRequestWinner(WorkflowRequest instance) {
//        return null;
//    }
//
//    private Employee identifyWorkInPlaceWinner(WorkflowRequest instance) throws WorkflowServerException {
//        return identifyShiftSwapWinner(instance);
//    }
//
//    private Employee identifyShiftSwapWinner(WorkflowRequest instance) throws WorkflowServerException {
//        Employee winner = null;
//        Boolean manuallySelect = instance.getApplyStrategy().equals(MANUAL_SELECT);
//        if (manuallySelect) {
//            Set<WorkflowRequestPeer> recipients = instance.getRecipients();
//            for (WorkflowRequestPeer recipient : recipients) {
//                if (winner == null && recipient.getSelectedManually()) {
//                    winner = recipient.getRecipient();
//                } else if (winner != null && recipient.getSelectedManually()) {
//                    throw new WorkflowServerException(REQUEST_HAS_MORE_THEN_ONE_CHOSEN_PEER,
//                            "Can not execute shift swap with more then one selected recipient");
//                }
//            }
//        } else {
//            Set<WorkflowRequestLog> actions = getActionsByType(instance, PEER_APPROVE);
//            Employee winnerEmployee = null;
//            Long winnerTime = null;
//            for (WorkflowRequestLog action : actions) {
//                if (winnerEmployee == null) {
//                    winnerEmployee = employeeService.getEmployee(instance.getTenantId(), action.getActorId());
//                    winnerTime = action.getUpdated().getMillis();
//                }
//                if (action.getUpdated().getMillis() < winnerTime) {
//                    winnerEmployee = action.getEmployee();
//                    winnerTime = action.getUpdated().getMillis();
//                }
//            }
//            Set<WorkflowRequestPeer> recipients = instance.getRecipients();
//            for (WorkflowRequestPeer recipient : recipients) {
//                if (recipient.getRecipient().getId().equals(winnerEmployee.getId())) {
//                    winner = recipient.getRecipient();
//                }
//            }
//        }
//        return winner;
//    }
//
//    @Override
//    public Boolean isAutoApproval(WflProcessType type, Employee employee) throws WorkflowServerException {
//        Set<EmployeeProcessAutoApproval> autoApprovals = employee.getEmployeeProcessAutoApprovals();
//        for (EmployeeProcessAutoApproval autoApproval : autoApprovals) {
//            if (autoApproval.getWflProcessType().equals(type)) {
//                return autoApproval.isAutoApproval();
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public Boolean isAutoApproval(WorkflowRequestPeer peerInstance) {
//        Set<EmployeeProcessAutoApproval> autoApprovals = peerInstance.getRecipient().getEmployeeProcessAutoApprovals();
//        for (EmployeeProcessAutoApproval autoApproval : autoApprovals) {
//            if (autoApproval.getWflProcessType().equals(peerInstance.getProcess().getProtoProcess().getType())) {
//                return autoApproval.isAutoApproval();
//            }
//        }
//        return false;
//    }
//
////    @Override
////    public Boolean isSupposedWinner(WorkflowRequestPeer peerInstance) throws WorkflowServerException {
////        Employee winner = findWinner(peerInstance.getProcess());
////        if (winner == null) {
////            return false;
////        } else {
////            return winner.getId().equals(peerInstance.getRecipient().getId());
////        }
////    }
//
//
//    private Shift shift(String tenantId, String id) {
//        return shiftService.getShift(new PrimaryKey(tenantId, id));
//    }
//
//    private Employee employee(java.lang.String tenantId, java.lang.String id) {
//        return employeeService.getEmployee(new PrimaryKey(tenantId, id));
//    }

}
