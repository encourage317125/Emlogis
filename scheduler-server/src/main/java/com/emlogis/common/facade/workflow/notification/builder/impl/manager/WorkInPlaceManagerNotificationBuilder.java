package com.emlogis.common.facade.workflow.notification.builder.impl.manager;

import com.emlogis.common.facade.workflow.notification.annotations.RequestNotificationBuilderQualifier;
import com.emlogis.common.facade.workflow.notification.builder.RequestNotificationContentBuilder;
import com.emlogis.common.facade.workflow.notification.builder.impl.AbstractRequestNotificationContentBuilder;
import com.emlogis.model.tenant.PersonalizedEntity;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.notification.WorkInPlaceMessageParameters;
import com.emlogis.model.workflow.notification.common.MessageEmployeeInfo;
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
@RequestNotificationBuilderQualifier(value = "WIP_REQUEST_MANAGER_NOTIFICATION")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class WorkInPlaceManagerNotificationBuilder extends AbstractRequestNotificationContentBuilder
        implements RequestNotificationContentBuilder {

    @Override
    public Map<String, String> build(
            WorkflowRequest request,
            PersonalizedEntity personalizedEntity,
            String logCode, String shiftId
    ) {
        UserAccount account = (UserAccount) personalizedEntity;
        MessageEmployeeInfo choosen = null;
        if (request.getChosenPeerId() != null) {
            choosen = parsePerson(employeeForPeer(request.getTenantId(), request.getChosenPeerId()));
        }
        WorkInPlaceMessageParameters parameters = null;
            parameters = new WorkInPlaceMessageParameters(
                    request.getId(),
                    request.getCode(),
                    request.getRequestStatus(),
                    request.getSubmitted(),
                    parseToSubmitterShiftInfo(request),
                    parsePersons(request.getRecipients()),
                    parsePerson(account),
                    (long) request.getRecipients().size(),
                    account.reportName(),
                    request.getRequestStatus(),
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
        return WorkflowRoleDict.MANAGER;
    }
}
