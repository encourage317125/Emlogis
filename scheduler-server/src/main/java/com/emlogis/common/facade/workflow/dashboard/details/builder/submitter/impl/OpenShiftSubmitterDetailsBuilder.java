package com.emlogis.common.facade.workflow.dashboard.details.builder.submitter.impl;

import com.emlogis.common.facade.workflow.dashboard.details.annotations.RequestDetailsBuilderQualifier;
import com.emlogis.common.facade.workflow.dashboard.details.builder.RequestDetailsBuilder;
import com.emlogis.common.facade.workflow.dashboard.details.builder.submitter.AbstractSubmitterRequestDetailsBuilder;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.workflow.dto.details.submitter.impl.OpenShiftSubmitterRequestDetailsDto;
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
@RequestDetailsBuilderQualifier(value = "OPEN_SHIFT_REQUEST_ORIGINATOR_DETAILS")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class OpenShiftSubmitterDetailsBuilder
        extends AbstractSubmitterRequestDetailsBuilder<OpenShiftSubmitterRequestDetailsDto>
        implements RequestDetailsBuilder<OpenShiftSubmitterRequestDetailsDto> {

        @Override
        public OpenShiftSubmitterRequestDetailsDto build(
                WorkflowRequest request, PrimaryKey requesterPk
        ) {
                Employee employee = employee(requesterPk);
                OpenShiftSubmitterRequestDetailsDto openShiftManagerTaskDto =
                        new OpenShiftSubmitterRequestDetailsDto(detailedBaseTaskInfo(request, employee));
                return openShiftManagerTaskDto;
        }

        @Override
        public WorkflowRequestTypeDict requestType() {
                return WorkflowRequestTypeDict.OPEN_SHIFT_REQUEST;
        }

        @Override
        public WorkflowRoleDict role() {
                return WorkflowRoleDict.ORIGINATOR;
        }
}
