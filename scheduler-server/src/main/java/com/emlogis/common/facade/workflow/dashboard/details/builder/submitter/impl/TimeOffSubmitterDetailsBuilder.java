package com.emlogis.common.facade.workflow.dashboard.details.builder.submitter.impl;

import com.emlogis.common.facade.workflow.dashboard.details.annotations.RequestDetailsBuilderQualifier;
import com.emlogis.common.facade.workflow.dashboard.details.builder.RequestDetailsBuilder;
import com.emlogis.common.facade.workflow.dashboard.details.builder.submitter.AbstractSubmitterRequestDetailsBuilder;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.workflow.dto.details.TimeOffShiftDto;
import com.emlogis.model.workflow.dto.details.submitter.impl.TimeOffSubmitterRequestDetailsDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;

import javax.ejb.*;
import javax.enterprise.inject.Default;

/**
 * Created by user on 21.08.15.
 */
@Stateless
@Local
@Default
@RequestDetailsBuilderQualifier(value = "TIME_OFF_REQUEST_ORIGINATOR_DETAILS")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TimeOffSubmitterDetailsBuilder
        extends AbstractSubmitterRequestDetailsBuilder<TimeOffSubmitterRequestDetailsDto>
        implements RequestDetailsBuilder<TimeOffSubmitterRequestDetailsDto> {


    @Override
    public TimeOffSubmitterRequestDetailsDto build(WorkflowRequest request, PrimaryKey requesterPk) {
        Employee employee = employee(requesterPk);
        TimeOffSubmitterRequestDetailsDto timeOffManagerTaskDto =
                new TimeOffSubmitterRequestDetailsDto(detailedBaseTaskInfo(request, employee));
        for (TimeOffShiftDto timeOffShiftDto : findTimeOffShifts(request)) {
            timeOffManagerTaskDto.getShifts().add(timeOffShiftDto);
        }
        return timeOffManagerTaskDto;
    }

    @Override
    public WorkflowRequestTypeDict requestType() {
        return WorkflowRequestTypeDict.TIME_OFF_REQUEST;
    }

    @Override
    public WorkflowRoleDict role() {
        return WorkflowRoleDict.ORIGINATOR;
    }
}
