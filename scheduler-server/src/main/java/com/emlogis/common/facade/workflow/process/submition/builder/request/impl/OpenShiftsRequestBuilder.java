package com.emlogis.common.facade.workflow.process.submition.builder.request.impl;

import com.emlogis.common.facade.workflow.process.submition.builder.request.RequestBuilder;
import com.emlogis.common.services.schedule.PostedOpenShiftService;
import com.emlogis.common.services.workflow.TranslationParam;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.process.request.submit.OpenShiftSubmitDto;
import com.emlogis.model.workflow.dto.process.request.submit.OpenShiftSubmitItemDto;
import com.emlogis.model.workflow.dto.process.response.ErrorSubmitResultDto;
import com.emlogis.model.workflow.dto.process.response.SubmitRequestResultDto;
import com.emlogis.model.workflow.dto.process.response.SuccessSubmitResultDto;
import com.emlogis.model.workflow.entities.WflProcess;
import com.emlogis.model.workflow.entities.WorkflowRequest;

import javax.ejb.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.emlogis.workflow.WflUtil.errorPrm;
import static com.emlogis.workflow.WflUtil.locale;

/**
 * Created by user on 20.08.15.
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class OpenShiftsRequestBuilder
        extends AbstractRequestBuilder<OpenShiftSubmitDto>
        implements RequestBuilder<OpenShiftSubmitDto> {

    @EJB
    private PostedOpenShiftService postedOpenShiftService;

    @Override
    public SubmitRequestResultDto build(
            OpenShiftSubmitDto req,
            WflProcess protoProcess,
            UserAccount userAccount
    ) {
        Employee employee = employee(userAccount);
        SubmitRequestResultDto result = new SubmitRequestResultDto(employee.getId(), employee.reportName());

        Map<WorkflowRequest, ReqData> requests = new HashMap<>();
        for (OpenShiftSubmitItemDto os : req.getOpenShifts()) {
            PostedOpenShiftService.ShiftPostedPair spp = postedOpenShiftService.checkShift(
                    employee.getPrimaryKey(), os.getShiftId());
            if (spp == null) {
                TranslationParam[] params = {new TranslationParam("shiftId", os.getShiftId())};
                String message = translator.getMessage(locale(employee), "request.posted.openshift.unavailable", params);
                result.getErrors().add(new ErrorSubmitResultDto(false, message, os.getShiftId(), os.getIdentifier()));
            } else {
                //initial setup
                try {
                    Boolean autoApprovedPostedOs = false;
                    if (spp.getPostedOpenShift().getTerms() != null) {
                        if (spp.getPostedOpenShift().getTerms().contains("AutoApprove")) {
                            autoApprovedPostedOs = true;
                        }
                    }
                    WorkflowRequest request = initialDataSearchService.initialize(
                            protoProcess, userAccount, employee, spp.getShift().getStartDateTime(),
                            spp.getShift().getId(), null, req.getExpiration(), null);
                    requests.put(request, new ReqData(autoApprovedPostedOs, os.getIdentifier()));
                } catch (Throwable throwable) {
                    String message = translator.getMessage(locale(employee), "request.error.submit", errorPrm(throwable));
                    throwable.printStackTrace();
                    result.getErrors().add(new ErrorSubmitResultDto(false, message, os.getShiftId(), os.getIdentifier()));
                }
            }
        }

        Map<WorkflowRequest, ReqData> initializedRequestMap = new HashMap<>();
        Iterator<Map.Entry<WorkflowRequest, ReqData>> iterator = requests.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<WorkflowRequest, ReqData> item = iterator.next();
            try {
                WorkflowRequest initialized = requestActionService.initiatorProceed(item.getKey(), req.getComment());
                initializedRequestMap.put(initialized, item.getValue());
                result.getCreated().add(new SuccessSubmitResultDto(true, initialized.getId(),
                        initialized.getRequestStatus().name(), initialized.getRequestType().name(),
                        initialized.getRequestDate(), initialized.getSubmitterShiftId(), 0,
                        initialized.getManagers().size(), item.getValue().getIdentifier(),
                        initialized.getDeclineReason(), item.getValue().getAutoApprove()));
            } catch (Throwable throwable) {
                String message = translator.getMessage(locale(employee), "request.error.submit", errorPrm(throwable));
                throwable.printStackTrace();
                result.getErrors().add(new ErrorSubmitResultDto(false, message, item.getKey().getSubmitterShiftId(),
                        item.getValue().getIdentifier()));
            }
        }

        return result;
    }



}
