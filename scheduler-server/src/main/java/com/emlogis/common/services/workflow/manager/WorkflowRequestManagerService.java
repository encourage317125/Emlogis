package com.emlogis.common.services.workflow.manager;

import com.emlogis.common.services.common.GeneralJPARepository;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestManager;

/**
 * Created by user on 20.07.15.
 */
public interface WorkflowRequestManagerService extends GeneralJPARepository<WorkflowRequestManager, PrimaryKey> {

    Boolean isReadForManager(WorkflowRequest request, UserAccount manager);

    Boolean isReadForSubmitter(WorkflowRequest request, Employee manager);

    Boolean readAtLeastByOneManager(WorkflowRequest request);

    void markRead(WorkflowRequest request, UserAccount userAccount, Boolean isRead);

    WorkflowRequest cleanup(WorkflowRequest request);

}
