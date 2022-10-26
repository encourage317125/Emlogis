package com.emlogis.common.facade.workflow.process.submition;

import com.emlogis.common.facade.workflow.dashboard.change.RequestDashChangeFacade;
import com.emlogis.common.facade.workflow.helper.ServiceHelper;
import com.emlogis.common.facade.workflow.process.action.RequestActionFacade;
import com.emlogis.common.facade.workflow.process.submition.builder.request.WorkflowRequestProducer;
import com.emlogis.common.services.workflow.TranslationParam;
import com.emlogis.common.services.workflow.WorkflowRequestTranslator;
import com.emlogis.common.services.workflow.action.RequestLogService;
import com.emlogis.common.services.workflow.manager.WorkflowRequestManagerService;
import com.emlogis.common.services.workflow.peer.WorkflowRequestPeerService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.common.services.workflow.process.proto.WflProcessService;
import com.emlogis.common.services.workflow.process.update.RequestActionService;
import com.emlogis.common.services.workflow.roles.WflRoleService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.decision.OpenShiftDecisionDto;
import com.emlogis.model.workflow.dto.decision.WorkflowDecisionResultInfoDto;
import com.emlogis.model.workflow.dto.process.request.submit.OpenShiftSubmitDto;
import com.emlogis.model.workflow.dto.process.request.submit.SubmitDto;
import com.emlogis.model.workflow.dto.process.response.SubmitRequestResultDto;
import com.emlogis.model.workflow.dto.process.response.SuccessSubmitResultDto;
import com.emlogis.model.workflow.entities.*;
import com.emlogis.workflow.api.notification.WorkflowNotificationFacade;
import com.emlogis.workflow.enums.WorkflowActionDict;
import com.emlogis.workflow.enums.WorkflowRequestDecision;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.enums.status.RequestTechnicalStatusDict;
import com.emlogis.workflow.enums.status.WorkflowRequestStatusDict;
import com.emlogis.workflow.exception.WorkflowServerException;
import org.apache.log4j.Logger;

import javax.ejb.*;

import static com.emlogis.workflow.WflUtil.*;
import static com.emlogis.workflow.exception.ExceptionCode.CAN_NOT_REMOVE_REQUEST;
import static com.emlogis.workflow.exception.ExceptionCode.CAN_NOT_SUBMIT_REQUEST;

/**
 * Created by user on 21.08.15.
 */
@Stateless
@Local(value = RequestSubmitFacade.class)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class RequestSubmitFacadeImpl implements RequestSubmitFacade {

    private final static Logger logger = Logger.getLogger(RequestSubmitFacadeImpl.class);

    @EJB
    private WorkflowRequestTranslator translator;

    @EJB
    private WflProcessService wflProcessService;

    @EJB
    private WflRoleService wflRoleService;

    @EJB
    private WorkflowRequestService workflowRequestService;

    @EJB
    private RequestActionService requestActionService;

    @EJB
    private WorkflowNotificationFacade workflowNotificationFacade;

    @EJB
    private WorkflowRequestProducer requestProducer;

    @EJB
    private ServiceHelper serviceHelper;

    @EJB
    private RequestLogService requestLogService;

    @EJB
    private WorkflowRequestManagerService workflowRequestManagerService;

    @EJB
    private RequestDashChangeFacade requestDashChangeFacade;

    @EJB
    private WorkflowRequestPeerService workflowRequestPeerService;

    @EJB
    private RequestActionFacade actionFcd;


    /**
     * {@inheritDoc}
     */
    //@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public SubmitRequestResultDto submitRequest(
            UserAccount userAccount,
            Employee changerEmployee,
            SubmitDto request // T extends SubmitDto
    ) throws WorkflowServerException {
        WflProcess parent = wflProcessService.findByTypeAndOrganization(
                serviceHelper.type(request), changerEmployee.getTenantId());
        try {
            //submit results
            SubmitRequestResultDto resultDto = requestProducer.produce(request, parent, userAccount);

            //approve auto_approve ones
            if (request instanceof OpenShiftSubmitDto) {
                for (SuccessSubmitResultDto successDto : resultDto.getCreated()) {
                    if (successDto.isAutoApprove()) {
                        WorkflowRequest workflowRequest = workflowRequestService.find(new PrimaryKey(userAccount.getTenantId(), successDto.getRequestId()));
                        OpenShiftDecisionDto decisionDto = new OpenShiftDecisionDto("auto approve");
                        UserAccount manager = null;
                        for (WorkflowRequestManager mngr : workflowRequest.getManagers()) {
                            if (manager == null) {
                                manager = mngr.getManager();
                                break;
                            }
                        }
                        WorkflowDecisionResultInfoDto resultInfoDto =
                                actionFcd.processManagerAction(workflowRequest, WorkflowRequestDecision.APPROVE, decisionDto, manager);
                        successDto.setRequestStatus(resultInfoDto.getStatus());
                    }
                }
            }
            return resultDto;

        } catch (Exception exception) {
            String message = translator.getMessage(locale(changerEmployee), "request.error.submit", errorPrm(exception));
            logger.error(message, exception);
            throw new WorkflowServerException(CAN_NOT_SUBMIT_REQUEST, message, exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeSubmittedWorkflowProcess(
            WorkflowRequest workflowRequest, Employee requestedEmployee, String comment
    ) throws WorkflowServerException {
        try {
            workflowRequest.setStatus(RequestTechnicalStatusDict.REMOVED);
            workflowRequest.setRequestStatus(WorkflowRequestStatusDict.DELETED);
            workflowRequest = workflowRequestService.update(workflowRequest);
            for (WorkflowRequestPeer peer : workflowRequest.getRecipients()) {
                peer.setPeerStatus(getPeerAggregatedRequestStatus(workflowRequest, peer));
                workflowRequestPeerService.update(peer);
            }
            TranslationParam[] params = {new TranslationParam("requestId", workflowRequest.getId())};
            String defaultComment = translator.getMessage(locale(requestedEmployee), "request.cancel.default.comment", params);
            WorkflowRequestLog requestLog = new WorkflowRequestLog(workflowRequest, WorkflowActionDict.ORIGINATOR_REMOVED,
                    WorkflowRoleDict.ORIGINATOR, requestedEmployee.getId(), defaultComment, comment);
            workflowRequest.getActions().add(requestLog);
            workflowRequestService.update(workflowRequest);
        } catch (Exception exception) {
            String message = translator.getMessage(locale(requestedEmployee), "request.cancel.error", errorPrm(exception));
            logger.error(message, exception);
            throw new WorkflowServerException(CAN_NOT_REMOVE_REQUEST, message, exception);
        }
    }
}
