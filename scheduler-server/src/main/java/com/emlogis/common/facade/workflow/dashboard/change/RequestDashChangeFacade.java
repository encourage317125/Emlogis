package com.emlogis.common.facade.workflow.dashboard.change;

import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.alerts.ManagerRequestCountsDto;
import com.emlogis.model.workflow.dto.alerts.TeamRequestCountsDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;

/**
 * Created by alexborlis on 22.01.15.
 */
public interface RequestDashChangeFacade {

    void managerMarkAs(WorkflowRequest request, UserAccount requester, Boolean isRead);

    void peerMarkAs(WorkflowRequest request, Employee requestEmployee, Boolean isRead);

    ManagerRequestCountsDto getManagerPendingAndNewRequestCounts(boolean teamRequests,
                                                                 PrimaryKey employeePrimaryKey, PrimaryKey employeePk);

    TeamRequestCountsDto getTeamPendingAndNewRequestCounts(PrimaryKey employeePrimaryKey);

}
