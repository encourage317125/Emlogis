package com.emlogis.common.facade.workflow.dashboard.details.builder.peer.impl;

import com.emlogis.common.facade.workflow.dashboard.details.annotations.RequestDetailsBuilderQualifier;
import com.emlogis.common.facade.workflow.dashboard.details.builder.RequestDetailsBuilder;
import com.emlogis.common.facade.workflow.dashboard.details.builder.peer.AbstractPeerRequestDetailsBuilder;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.workflow.dto.details.peer.impl.ShiftSwapPeerRequestDetailsDto;
import com.emlogis.model.workflow.dto.task.TaskRecipientInfoDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.exception.WorkflowServerException;

import javax.ejb.*;
import javax.enterprise.inject.Default;

/**
 * Created by user on 21.08.15.
 */
@Stateless
@Local
@Default
@RequestDetailsBuilderQualifier(value = "SHIFT_SWAP_REQUEST_PEER_DETAILS")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ShiftSwapPeerDetailsBuilder
        extends AbstractPeerRequestDetailsBuilder<ShiftSwapPeerRequestDetailsDto>
        implements RequestDetailsBuilder<ShiftSwapPeerRequestDetailsDto> {

    @Override
    public ShiftSwapPeerRequestDetailsDto build(WorkflowRequest request, PrimaryKey requesterPk) {
        Employee employee = employee(requesterPk);
        ShiftSwapPeerRequestDetailsDto result = new ShiftSwapPeerRequestDetailsDto(
                detailedBaseTaskInfo(request, employee));
        for (WorkflowRequestPeer peerInstance : request.getRecipients()) {
            if (peerInstance.getRecipient().getId().equals(employee.getId())) {
                result.getRecipients().add(peerDetailedInfo(peerInstance));
            }
        }
        return result;
    }

    private TaskRecipientInfoDto peerDetailedInfo(WorkflowRequestPeer peerInstance) throws WorkflowServerException {
        TaskRecipientInfoDto result = new TaskRecipientInfoDto(peerBaseInfo(peerInstance));
        result.setRecipientShift(shiftInfo(peerInstance));
        return result;
    }

    @Override
    public WorkflowRequestTypeDict requestType() {
        return WorkflowRequestTypeDict.SHIFT_SWAP_REQUEST;
    }

    @Override
    public WorkflowRoleDict role() {
        return WorkflowRoleDict.PEER;
    }
}
