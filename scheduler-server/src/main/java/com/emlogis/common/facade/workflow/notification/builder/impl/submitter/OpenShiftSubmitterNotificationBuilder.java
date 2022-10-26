package com.emlogis.common.facade.workflow.notification.builder.impl.submitter;

import com.emlogis.common.facade.workflow.notification.annotations.RequestNotificationBuilderQualifier;
import com.emlogis.common.facade.workflow.notification.builder.RequestNotificationContentBuilder;
import com.emlogis.common.facade.workflow.notification.builder.impl.AbstractRequestNotificationContentBuilder;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.PersonalizedEntity;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.notification.OpenShiftMessageParameters;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;

import javax.ejb.*;
import javax.enterprise.inject.Default;
import java.util.Map;

import static com.emlogis.workflow.WflUtil.dateStr;
import static com.emlogis.workflow.WflUtil.timeStr;

/**
 * Created by user on 25.08.15.
 */
@Local
@Default
@RequestNotificationBuilderQualifier(value = "OPEN_SHIFT_REQUEST_ORIGINATOR_NOTIFICATION")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class OpenShiftSubmitterNotificationBuilder extends AbstractRequestNotificationContentBuilder
        implements RequestNotificationContentBuilder {

    @Override
    public Map<String, String> build(
            WorkflowRequest request,
            PersonalizedEntity personalizedEntity,
            String logCode, String shiftId
    ) {
        Employee employee = (Employee) personalizedEntity;
        OpenShiftMessageParameters parameters = new OpenShiftMessageParameters(
                request.getId(),
                request.getCode(),
                request.getRequestStatus(),
                request.getSubmitted(),
                parseToSubmitterShiftInfo(request),
                request.getComment(),
                request.getDeclineReason(),
                employee.reportName(),
                logCode,
                dateStr(request.getSubmitterTz(), request.getRequestDate(), request.locale()),
                timeStr(request.getSubmitterTz(), request.getSubmitterShiftStartDateTime(), request.locale()),
                timeStr(request.getSubmitterTz(), request.getSubmitterShiftEndDateTime(), request.locale()));
        return parameters.post();
    }

    @Override
    public WorkflowRequestTypeDict type() {
        return WorkflowRequestTypeDict.OPEN_SHIFT_REQUEST;
    }

    @Override
    public WorkflowRoleDict role() {
        return WorkflowRoleDict.ORIGINATOR;
    }
}
