package com.emlogis.common.facade.workflow.process.submition.builder.request.impl;

import com.emlogis.common.facade.workflow.process.submition.builder.request.RequestBuilder;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.process.request.submit.WorkInPlaceSubmitDto;
import com.emlogis.model.workflow.dto.process.response.ErrorSubmitResultDto;
import com.emlogis.model.workflow.dto.process.response.SubmitRequestResultDto;
import com.emlogis.model.workflow.dto.process.response.SuccessSubmitResultDto;
import com.emlogis.model.workflow.entities.WflProcess;
import com.emlogis.model.workflow.entities.WorkflowRequest;

import javax.ejb.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.emlogis.common.EmlogisUtils.toJsonString;
import static com.emlogis.workflow.WflUtil.errorPrm;
import static com.emlogis.workflow.WflUtil.locale;

/**
 * Created by user on 20.08.15.
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class WorkInPlaceRequestBuilder
        extends AbstractRequestBuilder<WorkInPlaceSubmitDto>
        implements RequestBuilder<WorkInPlaceSubmitDto> {

    @Override
    public SubmitRequestResultDto build(
            WorkInPlaceSubmitDto req,
            WflProcess protoProcess,
            UserAccount userAccount
    ) {
        Employee employee = employee(userAccount);
        SubmitRequestResultDto result = new SubmitRequestResultDto(employee.getId(), employee.reportName());
        try {
            Map<String, List<String>> assignments = new HashMap<>();
            for (String employeeId : req.getRecipientIds()) {
                assignments.put(employeeId, Arrays.asList("EMPTY"));
            }
            WorkflowRequest request = initialDataSearchService.initialize(
                    protoProcess, userAccount, employee, req.getSubmitterShiftId(), req.getExpiration(),
                    toJsonString(req), req.getRecipientIds().size(), assignments);
            request = requestActionService.initiatorProceed(request, req.getComment());
            result.getCreated().add(new SuccessSubmitResultDto(true, request.getId(),
                    request.getRequestStatus().name(), request.getRequestType().name(),
                    request.getRequestDate(), request.getSubmitterShiftId(), request.getRecipients().size(),
                    request.getManagers().size(), null, request.getDeclineReason()));
        } catch (Throwable throwable) {
            String message = translator.getMessage(locale(employee), "request.error.submit", errorPrm(throwable));
            throwable.printStackTrace();
            result.getErrors().add(new ErrorSubmitResultDto(false, message, req.getSubmitterShiftId(), null));
        }
        return result;
    }
}
