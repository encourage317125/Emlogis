package com.emlogis.common.services.workflow.process.update.autoapproval;

import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.common.services.workflow.process.update.actions.EmployeeActionService;
import com.emlogis.common.services.workflow.process.update.actions.SystemActionService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestManager;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.api.identification.RequestRoleProxy;
import com.emlogis.workflow.enums.WorkflowActionDict;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.status.RequestTechnicalStatusDict;

import javax.ejb.*;
import java.util.ArrayList;
import java.util.List;

import static com.emlogis.workflow.WflUtil.employeeHasAutoApprovalFlag;
import static com.emlogis.workflow.WflUtil.isSwapOrWip;

/**
 * Created by user on 19.08.15.
 */
@Stateless
@Local(AutoApprovalManager.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class AutoApprovalManagerImpl implements AutoApprovalManager {

    @EJB
    private EmployeeService employeeService;

    @EJB
    private SystemActionService sas;
    @EJB
    private EmployeeActionService eas;

    @EJB
    private WorkflowRequestService wrService;
    @EJB
    private RequestRoleProxy requestRoleProxy;

    @Override
    public WorkflowRequest execute(
            WorkflowActionDict actionDict,
            WorkflowRequest request,
            WorkflowRequestPeer peerInstance,
            String comment,
            PrimaryKey employeePk
    ) {
        Employee requestEmployee = employeeService.getEmployee(employeePk);
        Boolean isPeerApproveAction = actionDict.equals(WorkflowActionDict.PEER_APPROVE);
        Boolean beforeStatusIsInitiated = request.getStatus().equals(RequestTechnicalStatusDict.PROCESS_INITIATED);
        Boolean requestHasNoPeers = !isSwapOrWip(request);
        Boolean isOriginatorProceedAction = actionDict.equals(WorkflowActionDict.ORIGINATOR_PROCEED);

        WorkflowRequestTypeDict requestType = request.getRequestType();
        Boolean submitterHasAutoApproval = employeeHasAutoApprovalFlag(request.getProtoProcess(), request.getInitiator());
        Boolean requesterHasAutoApprovalFlag = employeeHasAutoApprovalFlag(request.getProtoProcess(), requestEmployee);

        if (isPeerApproveAction && beforeStatusIsInitiated) {
            if (requesterHasAutoApprovalFlag && submitterHasAutoApproval) {
                request.setStatus(RequestTechnicalStatusDict.READY_FOR_ACTION);
                request.setChosenPeerId(peerInstance.getId());
                request = wrService.update(request);
                switch (requestType) {
                    case SHIFT_SWAP_REQUEST: {
                        request = sas.shiftSwapAction(request, comment, peerInstance, requestEmployee.reportName(),
                                requestEmployee.getUserAccount().getPrimaryKey());
                        break;
                    }
                    case WIP_REQUEST: {
                        request = sas.wipAction(request, comment, peerInstance, requestEmployee.reportName(),
                                requestEmployee.getUserAccount().getPrimaryKey());
                        break;
                    }
                }
            }
        } else if (isOriginatorProceedAction) {
            if (requestHasNoPeers) {
                if (submitterHasAutoApproval) {
                    switch (requestType) {
                        case OPEN_SHIFT_REQUEST: {
                            request = sas.openShiftAction(true, request, comment, requestEmployee.reportName(),
                                    requestEmployee.getPrimaryKey());
                            break;
                        }
                        case AVAILABILITY_REQUEST: {
                            request = sas.availabilityAction(request, comment, requestEmployee.reportName(),
                                    requestEmployee.getUserAccount().getPrimaryKey());
                            break;
                        }
                    }
                }
            } else {
                boolean processed = false;
                for (WorkflowRequestPeer peer : request.getRecipients()) {
                    if (employeeHasAutoApprovalFlag(request.getProtoProcess(), peer.getRecipient())) {
                        if (submitterHasAutoApproval) {
                            if (!processed) {
                                if (isSwapOrWip(request)) {
                                    request = eas.processPeerApprove(request, peer, "auto approval");
                                    UserAccount manager = managerSelector(request);
                                    request = eas.managerApprove(request, requestEmployee.reportName(),
                                            requestEmployee.getUserAccount().getPrimaryKey(), "auto approval", peer);
                                }
                                switch (requestType) {
                                    case SHIFT_SWAP_REQUEST: {
                                        request = sas.shiftSwapAction(request, comment, peer, requestEmployee.reportName(),
                                                requestEmployee.getUserAccount().getPrimaryKey());
                                        processed = true;
                                        break;
                                    }
                                    case WIP_REQUEST: {
                                        request = sas.wipAction(request, comment, peer, requestEmployee.reportName(),
                                                requestEmployee.getUserAccount().getPrimaryKey());
                                        processed = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return request;
    }


    private UserAccount managerSelector(WorkflowRequest instance) {
        List<WorkflowRequestManager> list = new ArrayList<>(instance.getManagers());
        if (!list.isEmpty()) {
            return list.get(0).getManager();
        } else {
            List<UserAccount> managers = requestRoleProxy.findManagers(instance.getRequestType(), instance.getInitiator());
            if (!managers.isEmpty()) {
                return managers.get(0);
            } else {
                throw new RuntimeException("Request has no managers!");
            }
        }
    }
}
