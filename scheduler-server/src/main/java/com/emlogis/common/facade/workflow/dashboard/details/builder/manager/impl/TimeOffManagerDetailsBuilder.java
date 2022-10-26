package com.emlogis.common.facade.workflow.dashboard.details.builder.manager.impl;

import com.emlogis.common.facade.workflow.dashboard.details.annotations.RequestDetailsBuilderQualifier;
import com.emlogis.common.facade.workflow.dashboard.details.builder.RequestDetailsBuilder;
import com.emlogis.common.facade.workflow.dashboard.details.builder.manager.AbstractManagerRequestDetailsBuilder;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.dto.details.TimeOffShiftDto;
import com.emlogis.model.workflow.dto.details.manager.impl.TimeOffManagerRequestDetailsDto;
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
@RequestDetailsBuilderQualifier(value = "TIME_OFF_REQUEST_MANAGER_DETAILS")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TimeOffManagerDetailsBuilder
        extends AbstractManagerRequestDetailsBuilder<TimeOffManagerRequestDetailsDto>
        implements RequestDetailsBuilder<TimeOffManagerRequestDetailsDto> {


        @Override
        public TimeOffManagerRequestDetailsDto build(WorkflowRequest request, PrimaryKey requesterPk) {
                TimeOffManagerRequestDetailsDto timeOffManagerTaskDto =
                        new TimeOffManagerRequestDetailsDto(detailedBaseTaskInfo(request, account(requesterPk)));
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
                return WorkflowRoleDict.MANAGER;
        }
}
