package com.emlogis.common.facade.workflow.validator.records;

import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;

import java.util.Locale;

/**
 * Created by user on 21.08.15.
 */
public class RequestRecord {

    private Employee employee;
    private UserAccount userAccount;
    private Locale locale;
    private WorkflowRequest workflowRequest;
    private WorkflowRequestTypeDict requestType;
    private WorkflowRoleDict role;

    public RequestRecord(
            Employee employee,
            UserAccount userAccount,
            Locale locale
    ) {
        this.employee = employee;
        this.userAccount = userAccount;
        this.locale = locale;
    }

    public RequestRecord(
            Employee employee,
            UserAccount userAccount,
            Locale locale,
            WorkflowRequest workflowRequest,
            WorkflowRequestTypeDict requestType,
            WorkflowRoleDict role
    ) {
        this.employee = employee;

        this.userAccount = userAccount;
        this.locale = locale;
        this.workflowRequest = workflowRequest;
        this.requestType = requestType;
        this.role = role;
    }

    public Employee employee() {
        return employee;
    }

    public UserAccount account() {
        return userAccount;
    }

    public Locale locale() {
        return locale;
    }

    public WorkflowRequest request() {
        return workflowRequest;
    }

    public WorkflowRequestTypeDict type() {
        return requestType;
    }

    public WorkflowRoleDict role() {
        return role;
    }
}
