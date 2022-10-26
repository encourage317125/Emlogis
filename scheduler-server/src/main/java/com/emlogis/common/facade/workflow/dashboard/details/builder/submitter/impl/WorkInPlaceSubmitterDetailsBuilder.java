package com.emlogis.common.facade.workflow.dashboard.details.builder.submitter.impl;

import com.emlogis.common.facade.workflow.dashboard.details.annotations.RequestDetailsBuilderQualifier;
import com.emlogis.common.facade.workflow.dashboard.details.builder.RequestDetailsBuilder;
import com.emlogis.common.facade.workflow.dashboard.details.builder.submitter.AbstractSubmitterRequestDetailsBuilder;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.workflow.dto.details.submitter.impl.WorkInPlaceSubmitterRequestDetailsDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
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
@RequestDetailsBuilderQualifier(value = "WIP_REQUEST_ORIGINATOR_DETAILS")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class WorkInPlaceSubmitterDetailsBuilder
        extends AbstractSubmitterRequestDetailsBuilder<WorkInPlaceSubmitterRequestDetailsDto>
        implements RequestDetailsBuilder<WorkInPlaceSubmitterRequestDetailsDto> {

        @Override
        public WorkInPlaceSubmitterRequestDetailsDto build(WorkflowRequest request, PrimaryKey requesterPk) {
                Employee employee = employee(requesterPk);
                WorkInPlaceSubmitterRequestDetailsDto result = new WorkInPlaceSubmitterRequestDetailsDto(
                        detailedBaseTaskInfo(request, employee));
                for (WorkflowRequestPeer peerInstance : request.getRecipients()) {
                        result.getRecipients().add(peerBaseInfo(peerInstance));
                }
                return result;
        }

        @Override
        public WorkflowRequestTypeDict requestType() {
                return WorkflowRequestTypeDict.WIP_REQUEST;
        }

        @Override
        public WorkflowRoleDict role() {
                return WorkflowRoleDict.ORIGINATOR;
        }


}
