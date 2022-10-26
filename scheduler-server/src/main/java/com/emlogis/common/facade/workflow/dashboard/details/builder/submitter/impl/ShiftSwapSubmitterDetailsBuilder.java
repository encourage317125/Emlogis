package com.emlogis.common.facade.workflow.dashboard.details.builder.submitter.impl;

import com.emlogis.common.facade.workflow.dashboard.details.annotations.RequestDetailsBuilderQualifier;
import com.emlogis.common.facade.workflow.dashboard.details.builder.RequestDetailsBuilder;
import com.emlogis.common.facade.workflow.dashboard.details.builder.submitter.AbstractSubmitterRequestDetailsBuilder;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.workflow.dto.details.submitter.impl.ShiftSwapSubmitterRequestDetailsDto;
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
@RequestDetailsBuilderQualifier(value = "SHIFT_SWAP_REQUEST_ORIGINATOR_DETAILS")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ShiftSwapSubmitterDetailsBuilder
        extends AbstractSubmitterRequestDetailsBuilder<ShiftSwapSubmitterRequestDetailsDto>
        implements RequestDetailsBuilder<ShiftSwapSubmitterRequestDetailsDto> {


    @Override
    public ShiftSwapSubmitterRequestDetailsDto build(WorkflowRequest request, PrimaryKey requesterPk) {
        Employee employee = employee(requesterPk);
        ShiftSwapSubmitterRequestDetailsDto result = new ShiftSwapSubmitterRequestDetailsDto(
                detailedBaseTaskInfo(request, employee));
        for (WorkflowRequestPeer peerInstance : request.getRecipients()) {
            result.getRecipients().add(peerDetailedInfo(peerInstance));
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
        return WorkflowRoleDict.ORIGINATOR;
    }
}
