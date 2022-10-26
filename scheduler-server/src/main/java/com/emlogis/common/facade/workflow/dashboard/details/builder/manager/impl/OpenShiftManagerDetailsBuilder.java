package com.emlogis.common.facade.workflow.dashboard.details.builder.manager.impl;

import com.emlogis.common.facade.workflow.dashboard.details.annotations.RequestDetailsBuilderQualifier;
import com.emlogis.common.facade.workflow.dashboard.details.builder.RequestDetailsBuilder;
import com.emlogis.common.facade.workflow.dashboard.details.builder.manager.AbstractManagerRequestDetailsBuilder;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.dto.details.manager.impl.OpenShiftManagerRequestDetailsDto;
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
@RequestDetailsBuilderQualifier(value = "OPEN_SHIFT_REQUEST_MANAGER_DETAILS")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class OpenShiftManagerDetailsBuilder
        extends AbstractManagerRequestDetailsBuilder<OpenShiftManagerRequestDetailsDto>
        implements RequestDetailsBuilder<OpenShiftManagerRequestDetailsDto> {

        @Override
        public OpenShiftManagerRequestDetailsDto build(WorkflowRequest request, PrimaryKey requesterPk) {
                OpenShiftManagerRequestDetailsDto openShiftManagerTaskDto =
                        new OpenShiftManagerRequestDetailsDto(detailedBaseTaskInfo(request, account(requesterPk)));
                return openShiftManagerTaskDto;
        }

        @Override
        public WorkflowRequestTypeDict requestType() {
                return WorkflowRequestTypeDict.OPEN_SHIFT_REQUEST;
        }

        @Override
        public WorkflowRoleDict role() {
                return WorkflowRoleDict.MANAGER;
        }
}
