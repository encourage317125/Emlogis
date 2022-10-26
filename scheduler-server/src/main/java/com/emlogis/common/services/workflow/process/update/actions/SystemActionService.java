package com.emlogis.common.services.workflow.process.update.actions;

import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.dto.decision.ShiftDecisionAction;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.exception.WorkflowServerException;

/**
 * Created by user on 19.08.15.
 */
public interface SystemActionService {

    WorkflowRequest openShiftAction(
            Boolean isAutoApproval,
            WorkflowRequest request,
            String comment,
            String actorName,
            PrimaryKey actorPk
    );

    WorkflowRequest shiftSwapAction(
            WorkflowRequest request,
            String comment,
            WorkflowRequestPeer peer,
            String actorName,
            PrimaryKey actorPk
    );

    WorkflowRequest wipAction(
            WorkflowRequest request,
            String comment,
            WorkflowRequestPeer peer,
            String actorName,
            PrimaryKey actorPk
    );


    WorkflowRequest availabilityAction(
            WorkflowRequest request,
            String comment,
            String actorName,
            PrimaryKey actorPk
    );


    WorkflowRequest dropShift(
            WorkflowRequest request,
            ShiftDecisionAction decisionAction,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) throws WorkflowServerException;

    WorkflowRequest assignShiftTo(
            WorkflowRequest request,
            ShiftDecisionAction decisionAction,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) throws WorkflowServerException;

    WorkflowRequest postOpenShifts(
            WorkflowRequest request,
            ShiftDecisionAction decisionAction,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) throws WorkflowServerException;

    WorkflowRequest timeOffWithoutAction(
            WorkflowRequest request,
            String comment,
            String actorName,
            PrimaryKey actorPk);
}
