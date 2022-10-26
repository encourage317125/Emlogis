package com.emlogis.common.facade.workflow.dashboard.details.builder.manager.impl;

import com.emlogis.common.facade.workflow.dashboard.details.annotations.RequestDetailsBuilderQualifier;
import com.emlogis.common.facade.workflow.dashboard.details.builder.RequestDetailsBuilder;
import com.emlogis.common.facade.workflow.dashboard.details.builder.manager.AbstractManagerRequestDetailsBuilder;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.dto.details.manager.impl.WorkInPlaceManagerRequestDetailsDto;
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
@RequestDetailsBuilderQualifier(value = "WIP_REQUEST_MANAGER_DETAILS")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class WorkInPlaceManagerDetailsBuilder
        extends AbstractManagerRequestDetailsBuilder<WorkInPlaceManagerRequestDetailsDto>
        implements RequestDetailsBuilder<WorkInPlaceManagerRequestDetailsDto> {


        @Override
        public WorkInPlaceManagerRequestDetailsDto build(WorkflowRequest request, PrimaryKey requesterPk) {
                WorkInPlaceManagerRequestDetailsDto result = new WorkInPlaceManagerRequestDetailsDto(
                        detailedBaseTaskInfo(request, account(requesterPk)));
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
                return WorkflowRoleDict.MANAGER;
        }
}
