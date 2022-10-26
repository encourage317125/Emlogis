package com.emlogis.common.facade.workflow.process.management;

import com.emlogis.common.facade.workflow.dashboard.change.RequestDashChangeFacade;
import com.emlogis.common.facade.workflow.helper.ServiceHelper;
import com.emlogis.common.facade.workflow.process.submition.builder.request.WorkflowRequestProducer;
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
import com.emlogis.model.workflow.dto.process.request.AddRequestPeersDto;
import com.emlogis.model.workflow.dto.process.request.RemoveRequestPeersDto;
import com.emlogis.model.workflow.dto.process.response.ProcessRecipientDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.api.notification.WorkflowNotificationFacade;
import com.emlogis.workflow.exception.WorkflowServerException;
import org.apache.log4j.Logger;

import javax.ejb.*;
import java.util.List;
import java.util.Locale;

import static com.emlogis.workflow.WflUtil.*;
import static com.emlogis.workflow.exception.ExceptionCode.*;

/**
 * Created by user on 21.08.15.
 */
@Stateless
@Local(value = RequestManagementFacade.class)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class RequestManagementFacadeImpl implements RequestManagementFacade {

    private final static Logger logger = Logger.getLogger(RequestManagementFacadeImpl.class);

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeRequestPeers(
            Employee employee,
            WorkflowRequest request,
            RemoveRequestPeersDto dto
    ) throws WorkflowServerException {
        try {
            List<ProcessRecipientDto> peersProvided = dto.getPeers();
            for (ProcessRecipientDto peerToRemove : peersProvided) {
                if (!employeeAlreadyMadeDecision(request, peerToRemove.getEmployeeId())) {
                    workflowRequestService.removeRecipient(request.getPrimaryKey(), peerToRemove.getEmployeeId());
                }
            }
        } catch (Exception error) {
            String message = translator.getMessage(locale(employee), "request.error.remove.peer", errorPrm(error));
            logger.error(message, error);
            throw new WorkflowServerException(FAIL_REMOVE_PEER, message, error);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addRequestPeers(
            Employee employee,
            WorkflowRequest request,
            AddRequestPeersDto dto
    ) throws WorkflowServerException {
        try {
            List<ProcessRecipientDto> recipientDtos = dto.getPeers();
            for (ProcessRecipientDto peerToAdd : recipientDtos) {
                WorkflowRequestPeer roleInstance = new WorkflowRequestPeer();
                roleInstance.setProcess(request);
                roleInstance.setRecipient(serviceHelper.employee(employee.getTenantId(), peerToAdd.getEmployeeId()));
                roleInstance.setPeerShiftId(peerToAdd.getShiftId() != null ? peerToAdd.getShiftId() : null);
                workflowRequestPeerService.create(roleInstance);
            }
        } catch (Exception error) {
            String message = translator.getMessage(locale(employee), "request.error.add.peer", errorPrm(error));
            logger.error(message, error);
            throw new WorkflowServerException(FAIL_ADD_PEER, message, error);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void withdrawRequestsByShiftId(
            String tenantId,
            String userAccountId,
            String shiftId,
            String reason
    ) throws Exception {
        try {
            requestActionService.actionTerminated(shiftId, reason, new PrimaryKey(tenantId, userAccountId));
        } catch (Exception error) {
            String message = translator.getMessage(new Locale("en", "US"), "request.terminate.openshift", errorPrm(error));
            logger.error(message, error);
            throw new WorkflowServerException(REQUEST_TERMINATE_FAIL, message, error);
        }
    }


    @Override
    public void deteleAllRequests() {
        requestLogService.deleteSimple();
        workflowRequestManagerService.deleteSimple();
        workflowRequestPeerService.deleteSimple();
        workflowRequestService.deleteSimple();
    }
}
