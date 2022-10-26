package com.emlogis.common.facade.workflow.validator;


import com.emlogis.common.security.AccountACL;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.dto.action.InstanceLog;
import com.emlogis.model.workflow.dto.alerts.ManagerRequestCountsDto;
import com.emlogis.model.workflow.dto.alerts.TeamRequestCountsDto;
import com.emlogis.model.workflow.dto.decision.WorkflowDecisionDto;
import com.emlogis.model.workflow.dto.decision.WorkflowDecisionResultInfoDto;
import com.emlogis.model.workflow.dto.decision.WorkflowDecisionResultListDto;
import com.emlogis.model.workflow.dto.details.abstracts.AbstractRequestDetailsInfoDto;
import com.emlogis.model.workflow.dto.details.abstracts.RequestDetailsInfo;
import com.emlogis.model.workflow.dto.details.manager.DetailedManagerRequestDetailsDto;
import com.emlogis.model.workflow.dto.filter.ManagerRequestsFilterDto;
import com.emlogis.model.workflow.dto.filter.PeerRequestsFilterDto;
import com.emlogis.model.workflow.dto.filter.SubmitterRequestsFilterDto;
import com.emlogis.model.workflow.dto.process.request.AddRequestPeersDto;
import com.emlogis.model.workflow.dto.process.request.RemoveRequestPeersDto;
import com.emlogis.model.workflow.dto.process.request.submit.SubmitDto;
import com.emlogis.model.workflow.dto.process.response.SubmitRequestResultDto;
import com.emlogis.model.workflow.dto.process.response.WflOriginatorInstanceBriefInfoDto;
import com.emlogis.model.workflow.dto.task.ManagerRequestDetailsInfoDto;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.workflow.enums.WorkflowRequestDecision;

import java.util.Locale;

/**
 * Created by alexborlis on 22.01.15.
 */
public interface WorkflowProcessRequestValidator<T extends SubmitDto> {

    /**
     *
     * @param userAccountPk
     * @param request
     * @return
     */
    SubmitPreValidationResultDto validateRequestDate(PrimaryKey userAccountPk, T request);


    /**
     * Method to create a new workflow request
     *
     * @param validationResultDto - {@link SubmitPreValidationResultDto}
     * @param request       - {@link SubmitDto} one of childs
     * @param locale-       - {@link Locale}
     * @return {@link SubmitRequestResultDto}
     */
    SubmitRequestResultDto submitRequest(
            SubmitPreValidationResultDto validationResultDto,
            T request,
            Locale locale
    );

    /**
     * Method to remove a workflow request
     *
     * @param userAccountPk - {@link com.emlogis.model.tenant.UserAccount}
     * @param requestPk     - {@link PrimaryKey} of {@link com.emlogis.model.workflow.entities.WorkflowRequest}
     * @param comment       - remove commentary
     * @param locale        - {@link Locale}
     */
    void removeRequest(
            PrimaryKey userAccountPk,
            PrimaryKey requestPk,
            String comment,
            Locale locale
    );

    /**
     * Method used by request submitter to remove existing peers if request not in final state
     *
     * @param dto           - {@link RemoveRequestPeersDto} - list of Peers to remove
     * @param userAccountPk - {@link com.emlogis.model.tenant.UserAccount}
     * @param requestPk     - {@link PrimaryKey} of {@link com.emlogis.model.workflow.entities.WorkflowRequest}
     * @param locale        - {@link Locale}
     */
    void removePeers(
            RemoveRequestPeersDto dto,
            PrimaryKey userAccountPk,
            PrimaryKey requestPk,
            Locale locale
    );

    /**
     * Method used by request submitter to add peers if request not in final state
     *
     * @param dto           - {@link AddRequestPeersDto} - list of Peers to add
     * @param userAccountPk - {@link com.emlogis.model.tenant.UserAccount}
     * @param requestPk     - {@link PrimaryKey} of {@link com.emlogis.model.workflow.entities.WorkflowRequest}
     * @param locale        - {@link Locale}
     */
    void addPeers(
            AddRequestPeersDto dto,
            PrimaryKey userAccountPk,
            PrimaryKey requestPk,
            Locale locale
    );

    /**
     * Peer's query method
     *
     * @param userAccountPk - {@link com.emlogis.model.tenant.UserAccount}
     * @param filterDto     - {@link PeerRequestsFilterDto} filter for query
     * @param locale        - {@link Locale}
     * @return
     */
    ResultSet<? extends AbstractRequestDetailsInfoDto> executePeerQuery(
            PrimaryKey userAccountPk,
            PeerRequestsFilterDto filterDto,
            Locale locale
    );

    /**
     * Manager's query method
     *
     * @param userAccountPk - {@link com.emlogis.model.tenant.UserAccount}
     * @param filterDto     - {@link ManagerRequestsFilterDto} filter for query
     * @param acl           - list of {@link AccountACL} account access lists
     * @param locale        - {@link Locale}
     * @return
     */
    ResultSet<ManagerRequestDetailsInfoDto> executeManagerQuery(
            PrimaryKey userAccountPk,
            ManagerRequestsFilterDto filterDto,
            AccountACL acl,
            String locale
    );

    /**
     * Submitter query method
     *
     * @param userAccountPk - {@link com.emlogis.model.tenant.UserAccount}
     * @param filterDto     - {@link SubmitterRequestsFilterDto} filter for query
     * @param locale        - {@link Locale}
     * @return
     */
    ResultSet<WflOriginatorInstanceBriefInfoDto> executeSubmitterQuery(
            PrimaryKey userAccountPk,
            SubmitterRequestsFilterDto filterDto,
            Locale locale
    );

    /**
     * @param userAccountPk - {@link com.emlogis.model.tenant.UserAccount}
     * @param requestPk     - {@link PrimaryKey} to {@link com.emlogis.model.workflow.entities.WorkflowRequest}
     * @param locale        - {@link Locale}
     * @param <D>           - {@link DetailedManagerRequestDetailsDto} child
     * @return
     */
    <D extends DetailedManagerRequestDetailsDto> D getManagerRequestDetails(
            PrimaryKey userAccountPk,
            PrimaryKey requestPk,
            Locale locale
    );

    /**
     * Method to get detailed info on request for submitter
     *
     * @param userAccountPk - {@link com.emlogis.model.tenant.UserAccount}
     * @param requestPk     - {@link PrimaryKey} to {@link com.emlogis.model.workflow.entities.WorkflowRequest}
     * @param locale        - {@link Locale}
     * @return {@link RequestDetailsInfo}
     */
    RequestDetailsInfo getSubmitterRequestDetails(
            PrimaryKey userAccountPk,
            PrimaryKey requestPk,
            Locale locale
    );

    /**
     * Method to get detailed info on request for request Peer
     *
     * @param userAccountPk - {@link com.emlogis.model.tenant.UserAccount}
     * @param requestPk     - {@link PrimaryKey} to {@link com.emlogis.model.workflow.entities.WorkflowRequest}
     * @param locale        - {@link Locale}
     * @return
     */
    RequestDetailsInfo getPeerRequestDetails(
            PrimaryKey userAccountPk,
            PrimaryKey requestPk,
            Locale locale
    );

    /**
     * Method to process Peer's APPROVE/DECLINE request decision
     *
     * @param workflowDecisionDto - {@link WorkflowDecisionDto} dto with chosen options data
     * @param decision            - {@link WorkflowRequestDecision} enum APPROVE/DECLINE
     * @param requestPk           - {@link PrimaryKey} to {@link com.emlogis.model.workflow.entities.WorkflowRequest}
     * @param userAccountPk       - {@link com.emlogis.model.tenant.UserAccount}
     * @param locale              - {@link Locale}
     * @return
     */
    WorkflowDecisionResultListDto peerDecision(
            WorkflowDecisionDto workflowDecisionDto,
            WorkflowRequestDecision decision,
            PrimaryKey requestPk,
            PrimaryKey userAccountPk,
            Locale locale
    );

    /**
     * Method to process Manager's APPROVE/DECLINE request decision
     *
     * @param workflowDecisionDto - {@link WorkflowDecisionDto} dto with chosen options data
     * @param decision            - {@link WorkflowRequestDecision} enum APPROVE/DECLINE
     * @param requestPk           - {@link PrimaryKey} to {@link com.emlogis.model.workflow.entities.WorkflowRequest}
     * @param userAccountPk       - {@link com.emlogis.model.tenant.UserAccount}
     * @param locale              - {@link Locale}
     * @return
     */
    WorkflowDecisionResultInfoDto managerDecision(
            WorkflowDecisionDto workflowDecisionDto,
            WorkflowRequestDecision decision,
            PrimaryKey requestPk,
            PrimaryKey userAccountPk,
            Locale locale
    );

    /**
     * Method that used by Peer to decline previously approved request if
     * {@link com.emlogis.model.workflow.entities.WorkflowRequest} not in final state yet
     *
     * @param requestPk     - {@link PrimaryKey} to {@link com.emlogis.model.workflow.entities.WorkflowRequest}
     * @param userAccountPk - {@link com.emlogis.model.tenant.UserAccount}
     * @param locale        - {@link Locale}
     * @return
     */
    RequestDetailsInfo cancelApprovedRequest(
            PrimaryKey requestPk,
            PrimaryKey userAccountPk,
            Locale locale
    );

    @Deprecated
    ResultSet<? extends AbstractRequestDetailsInfoDto> getPeerAssignedProcessesOld(
            String tenantId, String userAccountId, PeerRequestsFilterDto filterDto, Locale locale);


    InstanceLog getRequestHistory(String tenantId, String userId, String requestId, Locale locale);

    void managerMarkAs(String requestId, Boolean isRead, PrimaryKey userAccountPk, Locale locale);

    void peerMarkAs(String requestId, Boolean isRead, String userId, String tenantId, Locale locale);

    ManagerRequestCountsDto getManagerPendingAndNewRequestCounts(
            boolean teamRequests, PrimaryKey employeePrimaryKe, PrimaryKey employeePk, Locale localey);

    TeamRequestCountsDto getTeamPendingAndNewRequestCounts(PrimaryKey employeePrimaryKey, Locale locale);

    void deleteAllRequests();


}
