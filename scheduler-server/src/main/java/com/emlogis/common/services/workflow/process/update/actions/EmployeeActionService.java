package com.emlogis.common.services.workflow.process.update.actions;

import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.exception.WorkflowServerException;

/**
 * Created by user on 19.08.15.
 */
public interface EmployeeActionService {

    WorkflowRequest initiatorProceed(
            WorkflowRequest request,
            String comment,
            Boolean autoApprovalCheck
    ) throws WorkflowServerException;

    WorkflowRequest processPeerApprove(
            WorkflowRequest request, //instance to make action
            WorkflowRequestPeer peer, //peer that makes action
            String comment) throws WorkflowServerException;

    WorkflowRequest processPeerDecline(
            WorkflowRequest request,
            WorkflowRequestPeer peer,
            String comment
    ) throws WorkflowServerException;


    WorkflowRequest managerApprove(
            WorkflowRequest request,
            String managerName,
            PrimaryKey managerUserAccountPk,
            String comment,
            WorkflowRequestPeer chosenPeer
    ) throws WorkflowServerException;

    WorkflowRequest managerDecline(
            WorkflowRequest request,
            String managerName,
            PrimaryKey managerUserAccountPk,
            String comment
    ) throws WorkflowServerException;


}
