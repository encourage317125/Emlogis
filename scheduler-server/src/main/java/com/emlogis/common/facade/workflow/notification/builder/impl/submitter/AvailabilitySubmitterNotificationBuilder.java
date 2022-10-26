package com.emlogis.common.facade.workflow.notification.builder.impl.submitter;

import com.emlogis.common.facade.workflow.notification.annotations.RequestNotificationBuilderQualifier;
import com.emlogis.common.facade.workflow.notification.builder.RequestNotificationContentBuilder;
import com.emlogis.common.facade.workflow.notification.builder.impl.AbstractRequestNotificationContentBuilder;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.PersonalizedEntity;
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
@RequestNotificationBuilderQualifier(value = "AVAILABILITY_REQUEST_ORIGINATOR_NOTIFICATION")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AvailabilitySubmitterNotificationBuilder extends AbstractRequestNotificationContentBuilder
        implements RequestNotificationContentBuilder {

    @Override
    public Map<String, String> build(
            WorkflowRequest request,
            PersonalizedEntity personalizedEntity,
            String logCode,
            String shiftId
    ) {
        Employee employee = (Employee) personalizedEntity;
        AvailabilityMessageParameters parameters = new AvailabilityMessageParameters(
                request.getId(),
                request.getCode(),
                request.getSubmitted(),
                parsePerson(request.getInitiator()),
                request.getAvailabilityRequestSubtype(),
                availabilityEffectiveStartDate(request), //to string formatted date
                request.getRequestStatus(),
                employee.reportName(),
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
        return WorkflowRoleDict.ORIGINATOR;
    }
}
