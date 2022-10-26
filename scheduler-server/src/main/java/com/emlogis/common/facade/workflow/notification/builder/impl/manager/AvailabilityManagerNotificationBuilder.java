package com.emlogis.common.facade.workflow.notification.builder.impl.manager;

import com.emlogis.common.facade.workflow.notification.annotations.RequestNotificationBuilderQualifier;
import com.emlogis.common.facade.workflow.notification.builder.RequestNotificationContentBuilder;
import com.emlogis.common.facade.workflow.notification.builder.impl.AbstractRequestNotificationContentBuilder;
import com.emlogis.model.tenant.PersonalizedEntity;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.notification.AvailabilityMessageParameters;
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
@RequestNotificationBuilderQualifier(value = "AVAILABILITY_REQUEST_MANAGER_NOTIFICATION")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AvailabilityManagerNotificationBuilder extends AbstractRequestNotificationContentBuilder
        implements RequestNotificationContentBuilder {

    @Override
    public Map<String, String> build(
            WorkflowRequest request,
            PersonalizedEntity personalizedEntity,
            String logCode, String shiftId
    ) {
        UserAccount account = (UserAccount) personalizedEntity;
        AvailabilityMessageParameters parameters = new AvailabilityMessageParameters(
                request.getId(),
                request.getCode(),
                request.getSubmitted(),
                parsePerson(request.getInitiator()),
                request.getAvailabilityRequestSubtype(),
                availabilityEffectiveStartDate(request), //to string formatted date
                request.getRequestStatus(),
                account.reportName(),
                logCode,
                availabilityWeekDay(request),
                availabilityStartTime(request),
                availabilityEndTime(request),
                availabilityEffectiveUntilDate(request)
        );
        return parameters.post();
    }

    @Override
    public WorkflowRequestTypeDict type() {
        return WorkflowRequestTypeDict.AVAILABILITY_REQUEST;
    }

    @Override
    public WorkflowRoleDict role() {
        return WorkflowRoleDict.MANAGER;
    }
}
