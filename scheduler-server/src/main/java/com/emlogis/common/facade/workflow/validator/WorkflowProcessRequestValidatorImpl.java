package com.emlogis.common.facade.workflow.validator;


import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.workflow.dashboard.change.RequestDashChangeFacade;
import com.emlogis.common.facade.workflow.dashboard.details.RequestDetailsFacade;
import com.emlogis.common.facade.workflow.dashboard.query.RequestQueryFacade;
import com.emlogis.common.facade.workflow.process.action.RequestActionFacade;
import com.emlogis.common.facade.workflow.process.management.RequestManagementFacade;
import com.emlogis.common.facade.workflow.process.submition.RequestSubmitFacade;
import com.emlogis.common.facade.workflow.reports.WorkflowReportFacade;
import com.emlogis.common.security.AccountACL;
import com.emlogis.common.services.schedule.PostedOpenShiftService;
import com.emlogis.common.services.schedule.ShiftService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.services.workflow.WorkflowRequestTranslator;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.dto.AvailabilityWorkflowRequest;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.action.InstanceLog;
import com.emlogis.model.workflow.dto.alerts.ManagerRequestCountsDto;
import com.emlogis.model.workflow.dto.alerts.TeamRequestCountsDto;
import com.emlogis.model.workflow.dto.decision.WorkflowDecisionDto;
import com.emlogis.model.workflow.dto.decision.WorkflowDecisionResultInfoDto;
import com.emlogis.model.workflow.dto.decision.WorkflowDecisionResultListDto;
import com.emlogis.model.workflow.dto.details.abstracts.AbstractRequestDetailsInfoDto;
import com.emlogis.model.workflow.dto.details.abstracts.RequestDetailsInfo;
import com.emlogis.model.workflow.dto.details.manager.DetailedManagerRequestDetailsDto;
import com.emlogis.model.workflow.dto.filter.ManagerRequestsFilterDto;
import com.emlogis.model.workflow.dto.filter.PeerRequestsFilterDto;
import com.emlogis.model.workflow.dto.filter.SubmitterRequestsFilterDto;
import com.emlogis.model.workflow.dto.process.request.AddRequestPeersDto;
import com.emlogis.model.workflow.dto.process.request.RemoveRequestPeersDto;
import com.emlogis.model.workflow.dto.process.request.TimeOffRequestInfoDto;
import com.emlogis.model.workflow.dto.process.request.submit.*;
import com.emlogis.model.workflow.dto.process.response.SubmitRequestResultDto;
import com.emlogis.model.workflow.dto.process.response.WflOriginatorInstanceBriefInfoDto;
import com.emlogis.model.workflow.dto.task.ManagerRequestDetailsInfoDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.workflow.api.identification.RequestRoleProxy;
import com.emlogis.workflow.enums.WorkflowRequestDecision;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;

import javax.ejb.*;
import java.util.Locale;

import static com.emlogis.common.EmlogisUtils.deserializeObject;
import static com.emlogis.workflow.WflUtil.*;
import static com.emlogis.workflow.enums.WorkflowRoleDict.MANAGER;
import static com.emlogis.workflow.enums.WorkflowRoleDict.PEER;

/**
 * Created by alexborlis on 22.01.15.
 */
@Stateless
@Local(value = WorkflowProcessRequestValidator.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class WorkflowProcessRequestValidatorImpl<T extends SubmitDto> implements WorkflowProcessRequestValidator<T> {

    @EJB
    private WorkflowRequestTranslator translator;

    @EJB
    private RequestQueryFacade queryFcd;

    @EJB
    private RequestDetailsFacade detailsFcd;

    @EJB
    private RequestDashChangeFacade dshbFacade;

    @EJB
    private RequestActionFacade actionFcd;

    @EJB
    private WorkflowReportFacade workflowReportFacade;

    @EJB
    private RequestRoleProxy requestRoleProxy;

    @EJB
    private WorkflowRequestService workflowRequestService;

    @EJB
    private RequestSubmitFacade submitFcd;

    @EJB
    private RequestManagementFacade mngmntFcd;

    @EJB
    private UserAccountService userAccountService;

    @EJB
    private PostedOpenShiftService postedOpenShiftService;

    @EJB
    private ShiftService shiftService;

    @Override
    public SubmitPreValidationResultDto validateRequestDate(
            PrimaryKey userAccountPk,
            T request
    ) {
        UserAccount userAccount = userAccountService.getUserAccount(userAccountPk);
        Employee employee = userAccount.getEmployee();
        WorkflowRequestTypeDict type = identifyType(employee, request);
        Long currentDate = currentDate().getTime();
        switch (type) {
            case AVAILABILITY_REQUEST: {
//                AvailabilitySubmitDto dto = (AvailabilitySubmitDto) request;
//                switch (dto.getAvailUpdate().getType()) {
//                    case AvailcalUpdateParamsCDAvailDto: {
//                        AvailcalUpdateParamsCDAvailDto adto = (AvailcalUpdateParamsCDAvailDto) dto.getAvailUpdate();
//                        break;
//                    }
//                    case AvailcalUpdateParamsCDPrefDto: {
//                        AvailcalUpdateParamsCDPrefDto adto = (AvailcalUpdateParamsCDPrefDto) dto.getAvailUpdate();
//                        break;
//                    }
//                    case AvailcalUpdateParamsCIAvailDto: {
//                        break;
//                    }
//                    case AvailcalUpdateParamsCIPrefDto: {
//                        break;
//                    }
//                }
                break;
            }
            case WIP_REQUEST: {
                WorkInPlaceSubmitDto dto = (WorkInPlaceSubmitDto) request;
                Shift shift = shiftService.getShift(new PrimaryKey(userAccountPk.getTenantId(), dto.getSubmitterShiftId()));
                if (shift.getStartDateTime() <= currentDate) {
                    return new SubmitPreValidationResultDto(userAccount, employee, false,
                            translator.getMessage(locale(employee), "workflow.request.incorrect.request.date", null));
                }
                break;
            }
            case SHIFT_SWAP_REQUEST: {
                ShiftSwapSubmitDto dto = (ShiftSwapSubmitDto) request;
                Shift shift = shiftService.getShift(new PrimaryKey(userAccountPk.getTenantId(), dto.getSubmitterShiftId()));
                if (shift.getStartDateTime() <= currentDate) {
                    return new SubmitPreValidationResultDto(userAccount, employee, false,
                            translator.getMessage(locale(employee), "workflow.request.incorrect.request.date", null));
                }
                break;
            }
            case TIME_OFF_REQUEST: {
                TimeOffSubmitDto dto = (TimeOffSubmitDto) request;
                for (TimeOffRequestInfoDto timeOffRequestInfoDto : dto.getRequests()) {
                    Long requestDate = timeOffRequestInfoDto.getDate();
                    if (requestDate <= currentDate) {
                        return new SubmitPreValidationResultDto(userAccount, employee, false,
                                translator.getMessage(locale(employee), "workflow.request.incorrect.request.date", null));
                    }
                }
                break;
            }
            case OPEN_SHIFT_REQUEST: {
                OpenShiftSubmitDto dto = (OpenShiftSubmitDto) request;
                for (OpenShiftSubmitItemDto pos : dto.getOpenShifts()) {
                    PrimaryKey posPk = new PrimaryKey(userAccountPk.getTenantId(), pos.getShiftId());
                    try {
                        Shift postedOpenShift = shiftService.getShift(posPk);
                        if (postedOpenShift.getStartDateTime() <= currentDate) {
                            return new SubmitPreValidationResultDto(userAccount, employee, false,
                                    translator.getMessage(locale(employee), "workflow.request.incorrect.request.date", null));
                        }
                    } catch (Throwable throwable) {
                        return new SubmitPreValidationResultDto(userAccount, employee, false, throwable.getMessage());
                    }
                }
                break;
            }
        }
        return new SubmitPreValidationResultDto(userAccount, employee, true, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubmitRequestResultDto submitRequest(
            SubmitPreValidationResultDto validationResultDto,
            T request,
            Locale locale
    ) {
        Employee employee = validationResultDto.getEmployee();
        if (!employee.getId().equals(request.getSubmitterId())) {
            String message = translator.getMessage(locale(employee), "request.current.user.is.not.provided.submitter", null);
            throw new ValidationException(message);
        }
        if (request.getExpiration() == null) {
            String message = translator.getMessage(locale(employee), "request.expiration.date.mandatory", null);
            throw new ValidationException(message);
        }
        WorkflowRequestTypeDict type = identifyType(employee, request);
        switch (type) {
            case AVAILABILITY_REQUEST: {
                AvailabilitySubmitDto dto = (AvailabilitySubmitDto) request;
                AvailabilityWorkflowRequest availabilityWorkflowRequest = dto.getAvailUpdate();
                if (availabilityWorkflowRequest == null) {
                    String message = translator.getMessage(locale(employee), "request.availability.info.mandatory", null);
                    throw new ValidationException(message);
                }
                break;
            }
            case TIME_OFF_REQUEST: {
                if (((TimeOffSubmitDto) request).getRequests().isEmpty()) {
                    String message = translator.getMessage(locale(employee), "request.timeoff.info.mandatory", null);
                    throw new ValidationException(message);
                }
                for (TimeOffRequestInfoDto timeOffRequestInfoDto : ((TimeOffSubmitDto) request).getRequests()) {
                    if (timeOffRequestInfoDto.getAbsenceTypeId() == null) {
                        String message = translator.getMessage(locale(employee), "request.absence.type.mandatory", null);
                        throw new ValidationException(message);
                    }
                    if (timeOffRequestInfoDto.getAbsenceTypeId().isEmpty()) {
                        String message = translator.getMessage(locale(employee), "request.absence.type.mandatory", null);
                        throw new ValidationException(message);
                    }
                    if (timeOffRequestInfoDto.getDate() == null) {
                        String message = translator.getMessage(locale(employee), "request.timeoff.date.mandatory", null);
                        throw new ValidationException(message);
                    }
                }
                break;
            }
            case OPEN_SHIFT_REQUEST: {
                OpenShiftSubmitDto dto = (OpenShiftSubmitDto) request;
                if (dto.getOpenShifts() == null) {
                    String message = translator.getMessage(locale(employee), "request.openshift.info.mandatory", null);
                    throw new ValidationException(message);
                }
                if (dto.getOpenShifts().isEmpty()) {
                    String message = translator.getMessage(locale(employee), "request.openshift.info.mandatory", null);
                    throw new ValidationException(message);
                }
                break;
            }
            case SHIFT_SWAP_REQUEST: {
                ShiftSwapSubmitDto dto = (ShiftSwapSubmitDto) request;
                if (dto.getSubmitterShiftId() == null || dto.getSubmitterShiftId().isEmpty()) {
                    String message = translator.getMessage(locale(employee), "request.submitter.shift.mandatory", null);
                    throw new ValidationException(message);
                }
                if (dto.getAssignments() == null || dto.getAssignments().isEmpty()) {
                    String message = translator.getMessage(locale(employee), "request.assignments.mandatory", null);
                    throw new ValidationException(message);
                }

                break;
            }
            case WIP_REQUEST: {
                WorkInPlaceSubmitDto dto = (WorkInPlaceSubmitDto) request;
                if (dto.getSubmitterShiftId() == null || dto.getSubmitterShiftId().isEmpty()) {
                    String message = translator.getMessage(locale(employee), "request.submitter.shift.mandatory", null);
                    throw new ValidationException(message);
                }
                if (dto.getRecipientIds() == null || dto.getRecipientIds().isEmpty()) {
                    String message = translator.getMessage(locale(employee), "request.assignments.mandatory", null);
                    throw new ValidationException(message);
                }
                break;
            }
        }
        return submitFcd.submitRequest(validationResultDto.getUserAccount(), employee, request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeRequest(
            PrimaryKey userAccountPk,
            PrimaryKey requestPk,
            String comment,
            Locale locale
    ) {
        WorkflowRequest request = workflowRequestService.find(requestPk);
        Employee requestedEmployee = userAccountService.getUserAccount(userAccountPk).getEmployee();
        if (!request.getInitiator().getId().equals(requestedEmployee.getId())) {
            String message = translator.getMessage(request.locale(), "request.employee.is.not.submitter", null);
            throw new ValidationException(message);
        }
        submitFcd.removeSubmittedWorkflowProcess(request, requestedEmployee, comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePeers(
            RemoveRequestPeersDto dto,
            PrimaryKey userAccountPk,
            PrimaryKey requestPk,
            Locale locale
    ) {
        WorkflowRequest request = workflowRequestService.find(requestPk);
        Employee employee = userAccountService.getUserAccount(userAccountPk).getEmployee();
        if (!requestRoleProxy.validateIsSubmitter(employee, request)) {
            String message = translator.getMessage(request.locale(), "request.employee.is.not.submitter", null);
            throw new ValidationException(message);
        }
        mngmntFcd.removeRequestPeers(employee, request, dto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPeers(
            AddRequestPeersDto dto,
            PrimaryKey userAccountPk,
            PrimaryKey requestPk,
            Locale locale
    ) {
        WorkflowRequest request = workflowRequestService.find(requestPk);
        Employee requestedEmployee = userAccountService.getUserAccount(userAccountPk).getEmployee();
        if (!requestRoleProxy.validateIsSubmitter(requestedEmployee, request)) {
            String message = translator.getMessage(request.locale(), "request.employee.is.not.submitter", null);
            throw new ValidationException(message);
        }
        mngmntFcd.addRequestPeers(requestedEmployee, request, dto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultSet<? extends AbstractRequestDetailsInfoDto> executePeerQuery(
            PrimaryKey userAccountPk,
            PeerRequestsFilterDto filterDto,
            Locale locale
    ) {
        Employee employee = userAccountService.getUserAccount(userAccountPk).getEmployee();
        return queryFcd.getEmployeeTasks(employee, filterDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultSet<ManagerRequestDetailsInfoDto> executeManagerQuery(
            PrimaryKey userAccountPk,
            ManagerRequestsFilterDto filterDto,
            AccountACL acl,
            String locale
    ) {
        UserAccount account = userAccountService.getUserAccount(userAccountPk);
        ResultSet<ManagerRequestDetailsInfoDto> resultSet = queryFcd.getManagerTasks(account, filterDto, acl);
        return resultSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultSet<WflOriginatorInstanceBriefInfoDto> executeSubmitterQuery(
            PrimaryKey userAccountPk,
            SubmitterRequestsFilterDto filterDto,
            Locale locale
    ) {
        Employee employee = userAccountService.getUserAccount(userAccountPk).getEmployee();
        return queryFcd.getAllSubmittedRequests(employee, filterDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <D extends DetailedManagerRequestDetailsDto> D getManagerRequestDetails(
            PrimaryKey userAccountPk,
            PrimaryKey requestPk,
            Locale locale
    ) {
        WorkflowRequest request = workflowRequestService.find(requestPk);
        UserAccount account = userAccountService.getUserAccount(userAccountPk);
        if (requestRoleProxy.validateIsManager(
                request.getRequestType(), account, request.getInitiator())) {
            return (D) detailsFcd.getRequestDetails(account.getPrimaryKey(), request, MANAGER);
        } else {
            String message = translator.getMessage(request.locale(), "request.employee.has.no.permissions", null);
            throw new ValidationException(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestDetailsInfo getSubmitterRequestDetails(
            PrimaryKey userAccountPk,
            PrimaryKey requestPk,
            Locale locale
    ) {
        WorkflowRequest request = workflowRequestService.find(requestPk);
        Employee requestEmployee = userAccountService.getUserAccount(userAccountPk).getEmployee();
        if (requestRoleProxy.validateIsSubmitter(request.getRequestType(), userAccountPk.getTenantId(),
                userAccountPk.getId(), request.getInitiator())) {
            return detailsFcd.getRequestDetails(userAccountPk, request, WorkflowRoleDict.ORIGINATOR);
        } else {
            String message = translator.getMessage(request.locale(), "request.employee.is.not.submitter", null);
            throw new ValidationException(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestDetailsInfo getPeerRequestDetails(
            PrimaryKey userAccountPk,
            PrimaryKey requestPk,
            Locale locale
    ) {
        WorkflowRequest request = workflowRequestService.find(requestPk);
        Employee requestEmployee = userAccountService.getUserAccount(userAccountPk).getEmployee();
        if (requestRoleProxy.validateIsPeer(request, requestEmployee)) {
            return detailsFcd.getRequestDetails(userAccountPk, request, PEER);
        } else {
            String message = translator.getMessage(request.locale(), "request.employee.has.no.permissions", null);
            throw new ValidationException(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowDecisionResultListDto peerDecision(
            WorkflowDecisionDto workflowDecisionDto,
            WorkflowRequestDecision decision,
            PrimaryKey requestPk,
            PrimaryKey userAccountPk,
            Locale locale
    ) {
        WorkflowRequest request = workflowRequestService.find(requestPk);
        Employee employee = userAccountService.getUserAccount(userAccountPk).getEmployee();
        if (requestRoleProxy.validateIsPeer(request, employee)) {
            return actionFcd.processPeerAction(request, decision, workflowDecisionDto, employee);
        } else {
            String message = translator.getMessage(request.locale(), "request.employee.is.not.peer", null);
            throw new ValidationException(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowDecisionResultInfoDto managerDecision(
            WorkflowDecisionDto workflowDecisionDto,
            WorkflowRequestDecision decision,
            PrimaryKey requestPk,
            PrimaryKey userAccountPk,
            Locale locale
    ) {
        WorkflowRequest request = workflowRequestService.find(requestPk);
        UserAccount account = userAccountService.getUserAccount(userAccountPk);
        if (requestRoleProxy.validateIsManager(request.getRequestType(), account, request.getInitiator())) {
            return actionFcd.processManagerAction(request, decision, workflowDecisionDto, account);
        } else {
            String message = translator.getMessage(request.locale(), "request.employee.is.not.manager", null);
            throw new ValidationException(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestDetailsInfo cancelApprovedRequest(
            PrimaryKey requestPk,
            PrimaryKey userAccountPk,
            Locale locale
    ) {
        WorkflowRequest request = workflowRequestService.find(requestPk);
        Employee requestEmployee = userAccountService.getUserAccount(userAccountPk).getEmployee();
        if (requestRoleProxy.validateIsPeer(request, requestEmployee)) {
            actionFcd.processPeerCancelRequest(request, requestEmployee);
            return detailsFcd.getRequestDetails(requestEmployee.getPrimaryKey(), request, PEER);
        } else {
            String message = translator.getMessage(request.locale(), "request.employee.has.no.permissions", null);
            throw new ValidationException(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public ResultSet<? extends AbstractRequestDetailsInfoDto> getPeerAssignedProcessesOld(
            String tenantId,
            String userAccountId,
            PeerRequestsFilterDto filterDto,
            Locale locale
    ) {
        Employee employee = userAccountService.getUserAccount(new PrimaryKey(tenantId, userAccountId)).getEmployee();
        return queryFcd.getEmployeeTasksOld(employee, filterDto);
    }


    @Override
    public InstanceLog getRequestHistory(
            String tenantId,
            String userAccountId,
            String requestId,
            Locale locale
    ) {
        PrimaryKey requestPk = new PrimaryKey(tenantId, requestId);
        WorkflowRequest request = workflowRequestService.find(requestPk);
        Boolean isManager = requestRoleProxy.validateIsManager(request.getRequestType(), tenantId,
                userAccountId, request.getInitiator());
        if (!isManager) {
            Boolean isSubmitter = requestRoleProxy.validateIsSubmitter(request.getRequestType(), tenantId,
                    userAccountId, request.getInitiator());
            if (!isSubmitter) {
                String message = translator.getMessage(request.locale(), "request.history.unavailable", null);
                throw new ValidationException(message);
            }
        }
        return (InstanceLog) deserializeObject(request.getHistory());
    }

    @Override
    public void managerMarkAs(
            String requestId,
            Boolean isRead,
            PrimaryKey userAccountPk,
            Locale locale
    ) {
        PrimaryKey requestPk = new PrimaryKey(userAccountPk.getTenantId(), requestId);
        WorkflowRequest request = workflowRequestService.find(requestPk);
        UserAccount requester = userAccountService.getUserAccount(userAccountPk);
        Boolean isManager = requestRoleProxy.validateIsManager(request.getRequestType(),
                requester, request.getInitiator());
        if (!isManager) {
            String message = translator.getMessage(request.locale(), "request.employee.is.not.manager", null);
            throw new ValidationException(message);
        }
        dshbFacade.managerMarkAs(workflowRequestService.find(requestPk), requester, isRead);
    }

    @Override
    public void peerMarkAs(
            String requestId,
            Boolean isRead,
            String userId,
            String tenantId,
            Locale locale
    ) {
        PrimaryKey requestPk = new PrimaryKey(tenantId, requestId);
        WorkflowRequest request = workflowRequestService.find(requestPk);
        Employee requestEmployee = userAccountService.getUserAccount(new PrimaryKey(tenantId, userId)).getEmployee();
        if (!requestRoleProxy.validateIsPeer(request, requestEmployee)) {
            String message = translator.getMessage(request.locale(), "request.employee.is.not.peer", null);
            throw new ValidationException(message);
        }
        dshbFacade.peerMarkAs(workflowRequestService.find(requestPk), requestEmployee, isRead);
    }


    private WorkflowRequestTypeDict identifyType(
            Employee employee,
            T request
    ) {
        if (request instanceof ShiftSwapSubmitDto) {
            return WorkflowRequestTypeDict.SHIFT_SWAP_REQUEST;
        }
        if (request instanceof AvailabilitySubmitDto) {
            return WorkflowRequestTypeDict.AVAILABILITY_REQUEST;
        }
        if (request instanceof WorkInPlaceSubmitDto) {
            return WorkflowRequestTypeDict.WIP_REQUEST;
        }
        if (request instanceof TimeOffSubmitDto) {
            return WorkflowRequestTypeDict.TIME_OFF_REQUEST;
        }
        if (request instanceof OpenShiftSubmitDto) {
            return WorkflowRequestTypeDict.OPEN_SHIFT_REQUEST;
        }
        String message = translator.getMessage(locale(employee), "request.unknown.type", errorPrm(request.getClass()));
        throw new ValidationException(message);
    }


    @Override
    public ManagerRequestCountsDto getManagerPendingAndNewRequestCounts(
            boolean teamRequests,
            PrimaryKey userAccountPk,
            PrimaryKey employeePk,
            Locale locale
    ) {
        return dshbFacade.getManagerPendingAndNewRequestCounts(teamRequests, userAccountPk, employeePk);
    }

    @Override
    public TeamRequestCountsDto getTeamPendingAndNewRequestCounts(
            PrimaryKey employeePrimaryKey,
            Locale locale
    ) {
        return dshbFacade.getTeamPendingAndNewRequestCounts(employeePrimaryKey);
    }

    @Override
    public void deleteAllRequests() {
        mngmntFcd.deteleAllRequests();
    }

}
