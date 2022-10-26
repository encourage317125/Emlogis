package com.emlogis.common.facade.workflow.dashboard.details;

import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.workflow.dashboard.details.annotations.RequestDetailsBuilderQualifierImpl;
import com.emlogis.common.facade.workflow.dashboard.details.builder.RequestDetailsBuilder;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.services.workflow.TranslationParam;
import com.emlogis.common.services.workflow.WorkflowRequestTranslator;
import com.emlogis.common.services.workflow.manager.WorkflowRequestManagerService;
import com.emlogis.common.services.workflow.peer.WorkflowRequestPeerService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.dto.AvailcalUpdateParamsCDAvailDto;
import com.emlogis.model.employee.dto.AvailcalUpdateParamsCDPrefDto;
import com.emlogis.model.employee.dto.AvailcalUpdateParamsCIAvailDto;
import com.emlogis.model.employee.dto.AvailcalUpdateParamsCIPrefDto;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.details.WorkflowAvailabilitySettingsDto;
import com.emlogis.model.workflow.dto.details.abstracts.RequestDetailsInfo;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.exception.WorkflowServerException;
import org.apache.log4j.Logger;

import javax.ejb.*;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static com.emlogis.common.EmlogisUtils.fromJsonString;
import static com.emlogis.workflow.enums.WorkflowRoleDict.MANAGER;
import static com.emlogis.workflow.enums.WorkflowRoleDict.PEER;
import static com.emlogis.workflow.exception.ExceptionCode.MANAGER_REQUEST_DETAIL_FAIL;

/**
 * Created by user on 21.08.15.
 */
@Stateful
@Local(RequestDetailsFacade.class)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class RequestDetailsFacadeImpl implements RequestDetailsFacade {

    private final static Logger logger = Logger.getLogger(RequestDetailsFacadeImpl.class);

    @EJB
    private WorkflowRequestTranslator translator;

    @Inject
    @Any
    private Instance<RequestDetailsBuilder<?>> requestDetailsBuilder;

    @EJB
    private WorkflowRequestManagerService workflowRequestManagerService;

    @EJB
    private WorkflowRequestPeerService workflowRequestPeerService;

    @EJB
    private UserAccountService userAccountService;

    @EJB
    private EmployeeService employeeService;

    @EJB
    private WorkflowRequestService workflowRequestService;


    public WorkflowAvailabilitySettingsDto getAvailabilityRequestSettings(
            String tenantId,
            String requestId
    ) {
        WorkflowRequest request = workflowRequestService.find(new PrimaryKey(tenantId, requestId));
        if (!request.getRequestType().equals(WorkflowRequestTypeDict.AVAILABILITY_REQUEST)) {
            throw new ValidationException("Not an availability request");
        }
        Object availabilityReqData = fromJsonString(request.getData(), request.getAvailabilityRequestSubtype().getClazz());
        switch (request.getAvailabilityRequestSubtype()) {
            case AvailcalUpdateParamsCIPrefDto: {
                AvailcalUpdateParamsCIPrefDto data = (AvailcalUpdateParamsCIPrefDto) availabilityReqData;
                return new WorkflowAvailabilitySettingsDto(data, tenantId, requestId, request.getRequestStatus());
            }
            case AvailcalUpdateParamsCDPrefDto: {
                AvailcalUpdateParamsCDPrefDto data = (AvailcalUpdateParamsCDPrefDto) availabilityReqData;
                return new WorkflowAvailabilitySettingsDto(data, tenantId, requestId, request.getRequestStatus());
            }
            case AvailcalUpdateParamsCIAvailDto: {
                AvailcalUpdateParamsCIAvailDto data = (AvailcalUpdateParamsCIAvailDto) availabilityReqData;
                return new WorkflowAvailabilitySettingsDto(data, tenantId, requestId, request.getRequestStatus());
            }
            case AvailcalUpdateParamsCDAvailDto: {
                AvailcalUpdateParamsCDAvailDto data = (AvailcalUpdateParamsCDAvailDto) availabilityReqData;
                return new WorkflowAvailabilitySettingsDto(data, tenantId, requestId, request.getRequestStatus());
            }
            default: {
                throw new RuntimeException("Availability request: " + requestId + " is of unknown type!");
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestDetailsInfo getRequestDetails(
            PrimaryKey requesterPk,
            WorkflowRequest request,
            WorkflowRoleDict role
    ) throws WorkflowServerException {
        try {
            UserAccount userAccount = userAccountService.getUserAccount(requesterPk);
            RequestDetailsBuilder service = requestDetailsBuilder.select(
                    new RequestDetailsBuilderQualifierImpl(request.getRequestType(), role)).get();
            RequestDetailsInfo result = null;
            if (role.equals(MANAGER)) {
                result = service.build(request, requesterPk);
                workflowRequestManagerService.markRead(request, userAccount, true);
            } else if (role.equals(PEER)) {
                result = service.build(request, userAccount.getEmployee().getPrimaryKey());
                workflowRequestPeerService.markRead(request, userAccount.getEmployee(), true);
            } else {
                result = service.build(request, userAccount.getEmployee().getPrimaryKey());
            }
            return result;
        } catch (Exception error) {
            TranslationParam[] params = {new TranslationParam("requestId", request.getId()),
                    new TranslationParam("error", error.getMessage())};
            String message = translator.getMessage(request.locale(), "request.error.request.details", params);
            logger.error(message, error);
            throw new WorkflowServerException(MANAGER_REQUEST_DETAIL_FAIL, message, error);
        }
    }

}
