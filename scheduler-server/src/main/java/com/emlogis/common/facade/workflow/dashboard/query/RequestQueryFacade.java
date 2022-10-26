package com.emlogis.common.facade.workflow.dashboard.query;

import com.emlogis.common.security.AccountACL;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.details.abstracts.AbstractRequestDetailsInfoDto;
import com.emlogis.model.workflow.dto.filter.ManagerRequestsFilterDto;
import com.emlogis.model.workflow.dto.filter.PeerRequestsFilterDto;
import com.emlogis.model.workflow.dto.filter.SubmitterRequestsFilterDto;
import com.emlogis.model.workflow.dto.process.response.WflOriginatorInstanceBriefInfoDto;
import com.emlogis.model.workflow.dto.task.ManagerRequestDetailsInfoDto;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.workflow.exception.WorkflowServerException;

/**
 * Created by user on 21.08.15.
 */
public interface RequestQueryFacade {

    ResultSet<? extends AbstractRequestDetailsInfoDto> getEmployeeTasks(
            Employee employee, PeerRequestsFilterDto filterDto) throws WorkflowServerException;

    ResultSet<? extends AbstractRequestDetailsInfoDto> getEmployeeTasksOld(
            Employee employee, PeerRequestsFilterDto filterDto) throws WorkflowServerException;


    ResultSet<WflOriginatorInstanceBriefInfoDto> getAllSubmittedRequests(
            Employee employee, SubmitterRequestsFilterDto filterDto) throws WorkflowServerException;

    ResultSet<ManagerRequestDetailsInfoDto> getManagerTasks(
            UserAccount account, ManagerRequestsFilterDto filterDto, AccountACL acl) throws WorkflowServerException;

}
