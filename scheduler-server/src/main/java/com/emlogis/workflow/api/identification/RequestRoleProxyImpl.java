package com.emlogis.workflow.api.identification;

import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import org.apache.log4j.Logger;

import javax.ejb.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by alexborlis on 30.01.15.
 */
@Stateless
@Local(value = RequestRoleProxy.class)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class RequestRoleProxyImpl implements RequestRoleProxy {

    private final Logger logger = Logger.getLogger(RequestRoleProxyImpl.class);

    @EJB
    private EmployeeService employeeService;


    @EJB
    private UserAccountService userAccountService;

    @Override
    public List<UserAccount> findManagers(WorkflowRequestTypeDict requestTypeDict, Employee originator) {
        Collection<String> managersAccountIds = employeeService.managerAccountIds(originator.getPrimaryKey(),
                requestTypeDict.getManagementPermission());
        List<UserAccount> resultList = new ArrayList<>();
        for (String managerAccountId : managersAccountIds) {
            PrimaryKey managerPk = new PrimaryKey(originator.getTenantId(), managerAccountId);
            resultList.add(userAccountService.getUserAccount(managerPk));
        }
        return resultList;
    }

    @Override
    public Boolean validateIsManager(
            WorkflowRequestTypeDict requestTypeDict,
            String candidateTenantId,
            String candidateAccountId,
            Employee initiator
    ) {
        PrimaryKey candidatePk = new PrimaryKey(candidateTenantId, candidateAccountId);
        UserAccount account = userAccountService.getUserAccount(candidatePk);
        return validateIsManager(requestTypeDict, account, initiator);
    }

    @Override
    public Boolean validateIsSubmitter(
            WorkflowRequestTypeDict requestTypeDict,
            String candidateTenantId,
            String candidateAccountId,
            Employee initiator
    ) {
        PrimaryKey candidatePk = new PrimaryKey(candidateTenantId, candidateAccountId);
        Employee candidate = userAccountService.getUserAccount(candidatePk).getEmployee();
        return candidate.getId().equals(initiator.getId()) && candidate.getTenantId().equals(initiator.getTenantId());
    }

    @Override
    public Boolean validateIsManager(
            WorkflowRequestTypeDict requestTypeDict, UserAccount account, Employee initiator) {
        List<UserAccount> managers = findManagers(requestTypeDict, initiator);
        for (UserAccount manager : managers) {
            if (account.getId().equals(manager.getId()) && account.getTenantId().equals(manager.getTenantId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean validateIsPeer(WorkflowRequest processInstance, Employee employee) {
        for (WorkflowRequestPeer peerInstance : processInstance.getRecipients()) {
            if (peerInstance.getRecipient().getId().equals(employee.getId()) &&
                    peerInstance.getRecipient().getTenantId().equals(employee.getTenantId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean validateIsSubmitter(Employee employee, WorkflowRequest workflowRequest) {
        return employee.getId().equals(workflowRequest.getInitiator().getId()) &&
                employee.getTenantId().equals(workflowRequest.getTenantId());
    }

}
