package com.emlogis.common.facade.workflow.notification.builder.impl.peer;

import com.emlogis.common.facade.workflow.notification.annotations.RequestNotificationBuilderQualifier;
import com.emlogis.common.facade.workflow.notification.builder.RequestNotificationContentBuilder;
import com.emlogis.common.facade.workflow.notification.builder.impl.AbstractRequestNotificationContentBuilder;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.PersonalizedEntity;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.model.workflow.notification.WorkInPlaceMessageParameters;
import com.emlogis.model.workflow.notification.common.MessageEmployeeInfo;
import com.emlogis.workflow.WflUtil;
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
@RequestNotificationBuilderQualifier(value = "WIP_REQUEST_PEER_NOTIFICATION")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class WorkInPlacePeerNotificationBuilder extends AbstractRequestNotificationContentBuilder
        implements RequestNotificationContentBuilder {

    @Override
    public Map<String, String> build(
            WorkflowRequest request,
            PersonalizedEntity personalizedEntity,
            String logCode, String shiftId
    ) {
        Employee employee = (Employee) personalizedEntity;
        MessageEmployeeInfo choosen = null;
        if (request.getChosenPeerId() != null) {
            choosen = parsePerson(employeeForPeer(request.getTenantId(), request.getChosenPeerId()));
        }
        WorkInPlaceMessageParameters parameters = null;
        WorkflowRequestPeer peer = findPeer(request, employee, shiftId);
        parameters = new WorkInPlaceMessageParameters(
                request.getId(),
                request.getCode(),
                request.getRequestStatus(),
                request.getSubmitted(),
                parseToSubmitterShiftInfo(request),
                parsePersons(request.getRecipients()),
                parsePerson(employee),
                (long) request.getRecipients().size(),
                employee.reportName(),
                WflUtil.getPeerAggregatedRequestStatus(request, peer),
                choosen,
                logCode);
        return parameters.post();
    }

    @Override
    public WorkflowRequestTypeDict type() {
        return WorkflowRequestTypeDict.WIP_REQUEST;
    }

    @Override
    public WorkflowRoleDict role() {
        return WorkflowRoleDict.PEER;
    }
}
