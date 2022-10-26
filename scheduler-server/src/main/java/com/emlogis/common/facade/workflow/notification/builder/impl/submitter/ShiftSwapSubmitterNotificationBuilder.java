package com.emlogis.common.facade.workflow.notification.builder.impl.submitter;

import com.emlogis.common.facade.workflow.notification.annotations.RequestNotificationBuilderQualifier;
import com.emlogis.common.facade.workflow.notification.builder.RequestNotificationContentBuilder;
import com.emlogis.common.facade.workflow.notification.builder.impl.AbstractRequestNotificationContentBuilder;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.PersonalizedEntity;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.notification.ShiftSwapMessageParameters;
import com.emlogis.model.workflow.notification.common.MessageParametersShiftInfo;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;

import javax.ejb.*;
import javax.enterprise.inject.Default;
import java.util.Map;

/**
 * Created by user on 25.08.15.
 */
@Local
@Default
@RequestNotificationBuilderQualifier(value = "SHIFT_SWAP_REQUEST_ORIGINATOR_NOTIFICATION")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ShiftSwapSubmitterNotificationBuilder extends AbstractRequestNotificationContentBuilder
        implements RequestNotificationContentBuilder {

    @Override
    public Map<String, String> build(
            WorkflowRequest request,
            PersonalizedEntity personalizedEntity,
            String logCode, String shiftId
    ) {
        Employee employee = (Employee) personalizedEntity;
        MessageParametersShiftInfo chosenPeerInfo = identifyChoosenPeerinfo(request);
        ShiftSwapMessageParameters parameters = null;
        parameters = new ShiftSwapMessageParameters(
                request.getId(),
                request.getCode(),
                request.getRequestStatus(),
                request.getSubmitted(),
                parseToSubmitterShiftInfo(request),
                parseToPeersSubmittedShiftInfoList(request),
                null,
                (long) request.getRecipients().size(),
                null,
                employee.reportName(),
                request.getRequestStatus(),
                chosenPeerInfo,
                logCode);
        return parameters.post();
    }

    @Override
    public WorkflowRequestTypeDict type() {
        return WorkflowRequestTypeDict.SHIFT_SWAP_REQUEST;
    }

    @Override
    public WorkflowRoleDict role() {
        return WorkflowRoleDict.ORIGINATOR;
    }
}
