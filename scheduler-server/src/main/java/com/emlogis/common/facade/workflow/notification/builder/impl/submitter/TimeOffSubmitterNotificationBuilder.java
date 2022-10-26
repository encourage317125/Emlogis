package com.emlogis.common.facade.workflow.notification.builder.impl.submitter;

import com.emlogis.common.facade.workflow.notification.annotations.RequestNotificationBuilderQualifier;
import com.emlogis.common.facade.workflow.notification.builder.RequestNotificationContentBuilder;
import com.emlogis.common.facade.workflow.notification.builder.impl.AbstractRequestNotificationContentBuilder;
import com.emlogis.common.services.employee.AbsenceTypeService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.PersonalizedEntity;
import com.emlogis.model.workflow.dto.process.request.TimeOffRequestInfoDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.notification.TimeOffMessageParameters;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;

import javax.ejb.*;
import javax.enterprise.inject.Default;
import java.util.Map;

import static com.emlogis.common.EmlogisUtils.fromJsonString;
import static com.emlogis.workflow.WflUtil.dateStr;
import static com.emlogis.workflow.enums.WorkflowRequestTypeDict.TIME_OFF_REQUEST;
import static com.emlogis.workflow.enums.WorkflowRoleDict.ORIGINATOR;

/**
 * Created by user on 25.08.15.
 */
@Local
@Default
@RequestNotificationBuilderQualifier(value = "TIME_OFF_REQUEST_ORIGINATOR_NOTIFICATION")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TimeOffSubmitterNotificationBuilder extends AbstractRequestNotificationContentBuilder
        implements RequestNotificationContentBuilder {

    @EJB
    private AbsenceTypeService absenceTypeService;

    @Override
    public Map<String, String> build(
            WorkflowRequest request,
            PersonalizedEntity personalizedEntity,
            String logCode, String shiftId
    ) {
        Employee employee = (Employee) personalizedEntity;
        TimeOffRequestInfoDto timeOffRequestInfoDto =
                fromJsonString(request.getData(), TimeOffRequestInfoDto.class);
        String absenceTypename = absenceTypeService.getAbsenceType(
                new PrimaryKey(request.getTenantId(), timeOffRequestInfoDto.getAbsenceTypeId())).getName();
        TimeOffMessageParameters parameters = new TimeOffMessageParameters(
                request.getId(),
                request.getCode(),
                request.getRequestStatus(),
                request.getSubmitted(),
                parsePerson(request.getInitiator()),
                absenceTypename,
                timeOffRequestInfoDto.getReason(),
                parseShiftInfoList(shifts(request), request.getInitiator()),
                (long) shifts(request).size(),
                employee.reportName(),
                logCode,
                dateStr(request.getSubmitterTz(), request.getRequestDate(), request.locale()));
        return parameters.post();
    }

    @Override
    public WorkflowRequestTypeDict type() {
        return TIME_OFF_REQUEST;
    }

    @Override
    public WorkflowRoleDict role() {
        return ORIGINATOR;
    }
}
