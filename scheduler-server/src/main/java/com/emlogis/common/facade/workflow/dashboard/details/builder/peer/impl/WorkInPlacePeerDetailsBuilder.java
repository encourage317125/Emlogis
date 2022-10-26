package com.emlogis.common.facade.workflow.dashboard.details.builder.peer.impl;

import com.emlogis.common.facade.workflow.dashboard.details.annotations.RequestDetailsBuilderQualifier;
import com.emlogis.common.facade.workflow.dashboard.details.builder.RequestDetailsBuilder;
import com.emlogis.common.facade.workflow.dashboard.details.builder.peer.AbstractPeerRequestDetailsBuilder;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.workflow.dto.details.peer.impl.WorkInPlacePeerRequestDetailsDto;
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
@RequestDetailsBuilderQualifier(value = "WIP_REQUEST_PEER_DETAILS")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class WorkInPlacePeerDetailsBuilder
        extends AbstractPeerRequestDetailsBuilder<WorkInPlacePeerRequestDetailsDto>
        implements RequestDetailsBuilder<WorkInPlacePeerRequestDetailsDto> {


        @Override
        public WorkInPlacePeerRequestDetailsDto build(WorkflowRequest request, PrimaryKey requesterPk) {
                Employee employee = employee(requesterPk);
                WorkInPlacePeerRequestDetailsDto result = new WorkInPlacePeerRequestDetailsDto(
                        detailedBaseTaskInfo(request, employee));
                for (WorkflowRequestPeer peerInstance : request.getRecipients()) {
                        if(peerInstance.getRecipient().getId().equals(requesterPk.getId())) {
                                result.getRecipients().add(peerBaseInfo(peerInstance));
                        }
                }
                return result;
        }

        @Override
        public WorkflowRequestTypeDict requestType() {
                return WorkflowRequestTypeDict.WIP_REQUEST;
        }

        @Override
        public WorkflowRoleDict role() {
                return WorkflowRoleDict.PEER;
        }
}
