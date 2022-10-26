package com.emlogis.common.facade.workflow.process.submition.builder.request.impl;

import com.emlogis.common.facade.workflow.process.submition.builder.request.RequestBuilder;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.process.request.TimeOffRequestInfoDto;
import com.emlogis.model.workflow.dto.process.request.submit.TimeOffSubmitDto;
import com.emlogis.model.workflow.dto.process.response.ErrorSubmitResultDto;
import com.emlogis.model.workflow.dto.process.response.SubmitRequestResultDto;
import com.emlogis.model.workflow.dto.process.response.SuccessSubmitResultDto;
import com.emlogis.model.workflow.entities.WflProcess;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.enums.AvailabilityRequestSubtype;

import javax.ejb.*;
import java.util.HashMap;
import java.util.Iterator;
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
public class TimeOffRequestBuilder
        extends AbstractRequestBuilder<TimeOffSubmitDto>
        implements RequestBuilder<TimeOffSubmitDto> {


    @Override
    public SubmitRequestResultDto build(
            TimeOffSubmitDto req,
            WflProcess protoProcess,
            UserAccount userAccount
    ) {
        Employee employee = employee(userAccount);
        SubmitRequestResultDto result = new SubmitRequestResultDto(employee.getId(), employee.reportName());
        Map<WorkflowRequest, ReqData> createdRequests = new HashMap<>();
        for (TimeOffRequestInfoDto timeOffRequestInfoDto : req.getRequests()) {
            try {
                WorkflowRequest request = initialDataSearchService.initialize(
                        protoProcess, userAccount, employee, timeOffRequestInfoDto.getDate(), null,
                        AvailabilityRequestSubtype.NONE, req.getExpiration(), toJsonString(timeOffRequestInfoDto));
                createdRequests.put(request, new ReqData(false, timeOffRequestInfoDto.getIdentifier()));
            } catch (Throwable throwable) {
                String message = translator.getMessage(locale(employee), "request.error.submit", errorPrm(throwable));
                throwable.printStackTrace();
                result.getErrors().add(new ErrorSubmitResultDto(false, message, null, timeOffRequestInfoDto.getIdentifier()));
            }
        }
        Iterator<Map.Entry<WorkflowRequest, ReqData>> iterator = createdRequests.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<WorkflowRequest, ReqData> item = iterator.next();
            try {
                WorkflowRequest request = requestActionService.initiatorProceed(item.getKey(), req.getComment());
                result.getCreated().add(new SuccessSubmitResultDto(true, request.getId(),
                        request.getRequestStatus().name(), request.getRequestType().name(),
                        request.getRequestDate(), null, request.getRecipients().size(),
                        request.getManagers().size(), item.getValue().getIdentifier(),
                        request.getDeclineReason()));
            } catch (Throwable throwable) {
                String message = translator.getMessage(locale(employee), "request.error.submit", errorPrm(throwable));
                throwable.printStackTrace();
                result.getErrors().add(new ErrorSubmitResultDto(false, message, null, item.getValue().getIdentifier()));
            }
        }
        return result;
    }
}
