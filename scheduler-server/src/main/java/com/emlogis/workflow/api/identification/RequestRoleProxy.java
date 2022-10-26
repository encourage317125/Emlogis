package com.emlogis.workflow.api.identification;

import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;

import java.util.List;

/**
 * Represents interface to communicate between workflow engine and core server logic.
 *
 * @author alex@borlis.net Copyright (2015)
 * @version 1.0
 */
public interface RequestRoleProxy {

    List<UserAccount> findManagers(WorkflowRequestTypeDict requestTypeDict, Employee originatorEmpl);

    Boolean validateIsManager(WorkflowRequestTypeDict requestTypeDict, String managerTenantId, String managerAccountId, Employee initiator);

    Boolean validateIsSubmitter(WorkflowRequestTypeDict requestTypeDict, String submitterTenantId, String submitterAccountId, Employee initiator);

    Boolean validateIsManager(WorkflowRequestTypeDict requestTypeDict, UserAccount userAccount, Employee initiator);

    Boolean validateIsPeer(WorkflowRequest processInstance, Employee employee);

    Boolean validateIsSubmitter(Employee requestedEmployee, WorkflowRequest workflowRequest);
}
