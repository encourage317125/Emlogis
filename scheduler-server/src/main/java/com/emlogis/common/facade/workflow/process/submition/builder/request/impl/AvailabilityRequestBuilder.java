package com.emlogis.common.facade.workflow.process.submition.builder.request.impl;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.facade.workflow.process.submition.builder.request.RequestBuilder;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.process.request.submit.AvailabilitySubmitDto;
import com.emlogis.model.workflow.dto.process.response.ErrorSubmitResultDto;
import com.emlogis.model.workflow.dto.process.response.SubmitRequestResultDto;
import com.emlogis.model.workflow.dto.process.response.SuccessSubmitResultDto;
import com.emlogis.model.workflow.entities.WflProcess;
import com.emlogis.model.workflow.entities.WorkflowRequest;

import javax.ejb.*;

import static com.emlogis.workflow.WflUtil.*;

/**
 * Created by user on 20.08.15.
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AvailabilityRequestBuilder
        extends AbstractRequestBuilder<AvailabilitySubmitDto>
        implements RequestBuilder<AvailabilitySubmitDto> {

    @Override
    public SubmitRequestResultDto build(
            AvailabilitySubmitDto req,
            WflProcess protoProces,
            UserAccount userAccount
    ) {
        Employee employee = employee(userAccount);
        SubmitRequestResultDto result = new SubmitRequestResultDto(employee.getId(), employee.reportName());
        try {
            String data = EmlogisUtils.toJsonString(req.getAvailUpdate());
            WorkflowRequest request = initialDataSearchService.initialize(
                    protoProces, userAccount, employee,
                    identifyAvailabilityRequestDate(accountUtilService.getActualTimeZone(employee), req),
                    null, req.getAvailUpdate().getType(), req.getExpiration(), data);

            request = requestActionService.initiatorProceed(request, req.getComment());

            result.getCreated().add(new SuccessSubmitResultDto(true, request.getId(),
                    request.getRequestStatus().name(), request.getRequestType().name(),
                    request.getRequestDate(), null, request.getRecipients().size(),
                    request.getManagers().size(), null, request.getDeclineReason()));

        } catch (Throwable throwable) {
            String message = translator.getMessage(locale(employee), "request.error.submit", errorPrm(throwable));
            throwable.printStackTrace();
            result.getErrors().add(new ErrorSubmitResultDto(false, message, null, null));
        }
        return result;
    }
}
