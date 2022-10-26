package com.emlogis.common.services.workflow.process.search;

import com.emlogis.common.security.AccountACL;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.details.manager.DetailedManagerRequestDetailsDto;
import com.emlogis.model.workflow.dto.filter.ManagerRequestsFilterDto;
import com.emlogis.model.workflow.dto.filter.PeerRequestsFilterDto;
import com.emlogis.model.workflow.dto.filter.SubmitterRequestsFilterDto;
import com.emlogis.rest.resources.util.ResultSet;

public interface WflProcessSearchService {

    ResultSet<Object[]> peerProcessSearch(PeerRequestsFilterDto filterDto, Employee requester);

    ResultSet<Object[]> peerProcessSearchOld(PeerRequestsFilterDto filter, Employee requester);

    ResultSet<Object[]> managerProcessSearch(ManagerRequestsFilterDto filter, UserAccount account, AccountACL acl);

    <T extends DetailedManagerRequestDetailsDto> ResultSet<Object[]> submittedProcessSearch(SubmitterRequestsFilterDto filter,
                                                                                  Employee requester);

    Object[] managerPendingAndNewRequestCounts(boolean teamRequests, PrimaryKey userAccountPk, PrimaryKey employeePk);

    Object[] teamPendingAndNewRequestCounts(PrimaryKey employeePrimaryKey);
}
