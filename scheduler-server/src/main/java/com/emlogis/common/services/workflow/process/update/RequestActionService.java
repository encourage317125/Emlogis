package com.emlogis.common.services.workflow.process.update;

import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.dto.decision.ShiftDecisionAction;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.enums.WorkflowRequestDecision;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.exception.WorkflowServerException;

/**
 * Created by user on 09.07.15.
 */
public interface RequestActionService {

    /**
     * Method that used to be invoked after peer or manager email/sms reply on notification
     *
     * @param role          - {@link WorkflowRoleDict} possible : MANAGER or PEER
     * @param decision      - {@link WorkflowRequestDecision} decision on action : APPROVE or DECLINE
     * @param requestPk     - {@link PrimaryKey} of {@link WorkflowRequest} request key on action
     * @param shiftPk       - {@link PrimaryKey} of {@link com.emlogis.model.schedule.Shift}
     * @param commentaryStr - commentary on action
     * @param userAccountPk - {@link PrimaryKey} of {@link com.emlogis.model.tenant.UserAccount} who replies
     * @return - {@link Boolean} TRUE if success
     */
    Boolean emailAction(
            WorkflowRoleDict role,
            WorkflowRequestDecision decision,
            PrimaryKey requestPk,
            PrimaryKey shiftPk,
            String commentaryStr,
            PrimaryKey userAccountPk
    );

    /**
     * Action of making created {@link WorkflowRequest} alive as submitted
     *
     * @param request - {@link WorkflowRequest} created instance
     * @param comment - commentary on creation
     * @return - {@link WorkflowRequest} submitted request
     */
    WorkflowRequest initiatorProceed(
            WorkflowRequest request,
            String comment
    );

    /**
     * Method to process {@link WorkflowRequest} approval action by {@link WorkflowRequestPeer}
     *
     * @param request - {@link WorkflowRequest} request to process
     * @param peer    - {@link WorkflowRequestPeer} peer. that makes an action
     * @param comment - commentary on approval
     * @return
     */
    WorkflowRequest processPeerApprove(
            WorkflowRequest request,
            WorkflowRequestPeer peer,
            String comment
    );

    /**
     * Method to process {@link WorkflowRequest} approval by manager
     *
     * @param request    - {@link WorkflowRequest} request to process
     * @param comment    - commentary on approval
     * @param chosenPeer - {@link WorkflowRequestPeer} peer that is chosen to take the action, can be null
     * @param actorName  - {@link String} name of person who mades approval
     * @param actorPk    - {@link PrimaryKey} of {@link com.emlogis.model.tenant.UserAccount} for manager
     * @return
     */
    WorkflowRequest managerApprove(
            WorkflowRequest request,
            String comment,
            WorkflowRequestPeer chosenPeer,
            String actorName,
            PrimaryKey actorPk
    );

    /**
     * Method to process {@link WorkflowRequest} decline action by {@link WorkflowRequestPeer}
     *
     * @param request - {@link WorkflowRequest} request to process
     * @param peer    - {@link WorkflowRequestPeer} peer. that makes an action
     * @param comment - commentary on decline
     * @return
     */
    WorkflowRequest processPeerDecline(
            WorkflowRequest request,
            WorkflowRequestPeer peer,
            String comment
    );

    /**
     * Method to process {@link WorkflowRequest} decline by manager
     *
     * @param request   - {@link WorkflowRequest} request to process
     * @param comment   - commentary on decline
     * @param actorName - {@link String} name of person who makes decline
     * @param actorPk   - {@link PrimaryKey} of {@link com.emlogis.model.tenant.UserAccount} for manager
     * @return
     */
    WorkflowRequest managerDecline(
            WorkflowRequest request,
            String comment,
            String actorName,
            PrimaryKey actorPk
    );

    /**
     * Method that invoked in cases when {@link WorkflowRequest} is not longer applicable to its final business mission
     *
     * @param shiftId - {@link com.emlogis.model.schedule.Shift} identifier of a shift that requests belong
     * @param reason  - commentary on termination
     * @param actorPk - {@link PrimaryKey} of {@link com.emlogis.model.tenant.UserAccount} for invoker
     */
    void actionTerminated(
            String shiftId,
            String reason,
            PrimaryKey actorPk
    );

    /**
     * Pre-qualified method to invoke asynchronous call of Posted open shift assign
     *
     * @param request   - {@link WorkflowRequest} request to process
     * @param comment   - commentary on action
     * @param actorName - {@link String} name of person who makes termination (system-user possible)
     * @param actorPk   - {@link PrimaryKey} of {@link com.emlogis.model.tenant.UserAccount} for invoker
     * @return
     */
    WorkflowRequest openShiftAction(
            WorkflowRequest request,
            String comment,
            String actorName,
            PrimaryKey actorPk
    );

    /**
     * Pre-qualified method to invoke asynchronous call of swap shifts action between employees
     *
     * @param request   - {@link WorkflowRequest} request to process
     * @param comment   - commentary on action
     * @param peer      - {@link WorkflowRequestPeer} that is chosen to swap shift
     * @param actorName - {@link String} name of person who makes termination (system-user possible)
     * @param actorPk   - {@link PrimaryKey} of {@link com.emlogis.model.tenant.UserAccount} for invoker
     * @return
     */
    WorkflowRequest shiftSwapAction(
            WorkflowRequest request,
            String comment,
            WorkflowRequestPeer peer,
            String actorName,
            PrimaryKey actorPk
    );

    /**
     * Pre-qualified method to invoke asynchronous call of work in place action between employees
     *
     * @param request   - {@link WorkflowRequest} request to process
     * @param comment   - commentary on action
     * @param peer      - {@link WorkflowRequestPeer} that is chosen to swap shift
     * @param actorName - {@link String} name of person who makes termination (system-user possible)
     * @param actorPk   - {@link PrimaryKey} of {@link com.emlogis.model.tenant.UserAccount} for invoker
     * @return
     */
    WorkflowRequest wipAction(
            WorkflowRequest request,
            String comment,
            WorkflowRequestPeer peer,
            String actorName,
            PrimaryKey actorPk
    );

    /**
     * Pre-qualified method to invoke asynchronous call of changing employee availability settings
     *
     * @param request   - {@link WorkflowRequest} request to process
     * @param comment   - commentary on action
     * @param actorName - {@link String} name of person who makes termination (system-user possible)
     * @param actorPk   - {@link PrimaryKey} of {@link com.emlogis.model.tenant.UserAccount} for invoker
     * @return
     */
    WorkflowRequest availabilityAction(
            WorkflowRequest request,
            String comment,
            String actorName,
            PrimaryKey actorPk
    );

    /**
     * Pre-qualified method to invoke asynchronous call of handling new employee PTO
     *
     * @param request        - {@link WorkflowRequest} request to process
     * @param decisionAction - {@link ShiftDecisionAction} decision how to handle PTO's shifts assigned possible:
     *                       DROP shift, ASSIGN to eligible employee, POST shift as open
     * @param comment        - commentary on action
     * @param actorName      - {@link String} name of person who makes termination (system-user possible)
     * @param actorPk        - {@link PrimaryKey} of {@link com.emlogis.model.tenant.UserAccount} for invoker
     * @return
     * @throws WorkflowServerException
     */
    WorkflowRequest dropShift(
            WorkflowRequest request,
            ShiftDecisionAction decisionAction,
            String comment,
            String actorName,
            PrimaryKey actorPk
    ) throws WorkflowServerException;

    /**
     * Pre-qualified method to invoke asynchronous call of assigning some shift to provided target employee
     *
     * @param request        - {@link WorkflowRequest} request to process
     * @param decisionAction - {@link ShiftDecisionAction} decision how to handle PTO's shifts assigned possible:
     *                       DROP shift, ASSIGN to eligible employee, POST shift as open
     * @param comment        - commentary on action
     * @param actorName      - {@link String} name of person who makes termination (system-user possible)
     * @param actorPk        - {@link PrimaryKey} of {@link com.emlogis.model.tenant.UserAccount} for invoker
     * @return
     */
    WorkflowRequest assignShiftTo(
            WorkflowRequest request,
            ShiftDecisionAction decisionAction,
            String comment,
            String actorName,
            PrimaryKey actorPk
    );

    /**
     * Pre-qualified method to invoke asynchronous call of posting released shift as posted
     *
     * @param request        - {@link WorkflowRequest} request to process
     * @param decisionAction - {@link ShiftDecisionAction} decision how to handle PTO's shifts assigned possible:
     *                       DROP shift, ASSIGN to eligible employee, POST shift as open
     * @param comment        - commentary on action
     * @param actorName      - {@link String} name of person who makes termination (system-user possible)
     * @param actorPk        - {@link PrimaryKey} of {@link com.emlogis.model.tenant.UserAccount} for invoker
     * @return
     */
    WorkflowRequest postOpenShifts(
            WorkflowRequest request,
            ShiftDecisionAction decisionAction,
            String comment,
            String actorName,
            PrimaryKey actorPk
    );

    /**
     * Method to track availability options due to the result of Time Off request
     *
     * @param request   - {@link WorkflowRequest} request to process
     * @param comment   - commentary on action
     * @param actorName - {@link String} name of person who makes termination (system-user possible)
     * @param actorPk   - {@link PrimaryKey} of {@link com.emlogis.model.tenant.UserAccount} for invoker
     * @return
     */
    WorkflowRequest timeOffWithoutAction(
            WorkflowRequest request,
            String comment,
            String actorName,
            PrimaryKey actorPk);

    /**
     * Method solves express open shift assign action
     *
     * @param request   - {@link WorkflowRequest} request to process
     * @param comment   - commentary on action
     * @param actorName - {@link String} name of person who makes termination (system-user possible)
     * @param actorPk   - {@link PrimaryKey} of {@link com.emlogis.model.tenant.UserAccount} for invoker
     * @return
     */
    WorkflowRequest forcedOpenShiftAction(
            WorkflowRequest request,
            String comment,
            String actorName,
            PrimaryKey actorPk);

}
