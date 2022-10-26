package com.emlogis.common.facade.workflow.dashboard.change;

import com.emlogis.common.services.workflow.WorkflowRequestTranslator;
import com.emlogis.common.services.workflow.manager.WorkflowRequestManagerService;
import com.emlogis.common.services.workflow.peer.WorkflowRequestPeerService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.common.services.workflow.process.search.WflProcessSearchService;
import com.emlogis.common.services.workflow.process.update.RequestActionService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.alerts.ManagerRequestCountsDto;
import com.emlogis.model.workflow.dto.alerts.TeamRequestCountsDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.api.identification.RequestRoleProxy;
import org.apache.log4j.Logger;

import javax.ejb.*;
import java.math.BigInteger;

//import com.emlogis.workflow.context.WorkflowEngineContext;

/**
 * Represents interface to communicate between workflow engine and core server logic.
 *
 * @author alex@borlis.net Copyright (2015)
 * @version 1.0
 */
@Stateless
@Local(value = RequestDashChangeFacade.class)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class RequestDashChangeFacadeImpl implements RequestDashChangeFacade {

    private final static Logger logger = Logger.getLogger(RequestDashChangeFacadeImpl.class);

    @EJB
    private WorkflowRequestTranslator translator;

    @EJB
    private WorkflowRequestPeerService workflowRequestPeerService;

    @EJB
    private WorkflowRequestService workflowRequestService;

    @EJB
    private RequestActionService requestActionService;

    @EJB
    private WflProcessSearchService wflProcessSearchService;

    @EJB
    private WorkflowRequestManagerService workflowRequestManagerService;

    @EJB
    private RequestRoleProxy requestRoleProxy;


    @Override
    public void managerMarkAs(WorkflowRequest request, UserAccount requester, Boolean isRead) {
        workflowRequestManagerService.markRead(request, requester, isRead);
    }

    @Override
    public void peerMarkAs(WorkflowRequest request, Employee requestEmployee, Boolean isRead) {
        workflowRequestPeerService.markRead(request, requestEmployee, isRead);
    }

    @Override
    public ManagerRequestCountsDto getManagerPendingAndNewRequestCounts(
            boolean teamRequests,
            PrimaryKey userAccpuntPk,
            PrimaryKey employeePk
    ) {
        ManagerRequestCountsDto result = new ManagerRequestCountsDto();

        Object[] counts = wflProcessSearchService.managerPendingAndNewRequestCounts(teamRequests, userAccpuntPk, employeePk);

        result.setPendingManagerRequests(((BigInteger) counts[0]).intValue());
        result.setNewManagerRequests(((BigInteger) counts[1]).intValue());

        if (teamRequests) {
            result.setPendingTeamRequests(((BigInteger) counts[2]).intValue());
            result.setNewTeamRequests(((BigInteger) counts[3]).intValue());
        }

        return result;
    }

    @Override
    public TeamRequestCountsDto getTeamPendingAndNewRequestCounts(PrimaryKey employeePrimaryKey) {
        TeamRequestCountsDto result = new TeamRequestCountsDto();

        Object[] counts = wflProcessSearchService.teamPendingAndNewRequestCounts(employeePrimaryKey);

        result.setPendingTeamRequests(((BigInteger) counts[0]).intValue());
        result.setNewTeamRequests(((BigInteger) counts[1]).intValue());

        return result;
    }


}
