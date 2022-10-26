package com.emlogis.common.facade.workflow.process.action;

import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.decision.WorkflowDecisionDto;
import com.emlogis.model.workflow.dto.decision.WorkflowDecisionResultInfoDto;
import com.emlogis.model.workflow.dto.decision.WorkflowDecisionResultListDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.enums.WorkflowRequestDecision;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.exception.WorkflowServerException;

/**
 * Created by user on 21.08.15.
 */
public interface RequestActionFacade {

    WorkflowDecisionResultInfoDto processManagerAction(
            WorkflowRequest instance, WorkflowRequestDecision decision,
            WorkflowDecisionDto workflowDecisionDto,
            UserAccount account);

    WorkflowDecisionResultListDto processPeerAction(
            WorkflowRequest instance, WorkflowRequestDecision decision, WorkflowDecisionDto workflowDecisionDto, Employee employee);

    String approve(WorkflowRequest instance, UserAccount account, WorkflowRoleDict role) throws WorkflowServerException;

    String deny(WorkflowRequest instance, UserAccount account, WorkflowRoleDict role) throws WorkflowServerException;

    void processPeerCancelRequest(WorkflowRequest request, Employee requestEmployee);
}
