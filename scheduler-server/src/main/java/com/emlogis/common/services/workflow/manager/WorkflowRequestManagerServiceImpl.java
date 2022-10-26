package com.emlogis.common.services.workflow.manager;

import com.emlogis.common.services.common.PrimaryKeyJPARepositoryServiceImpl;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestManager;

import javax.ejb.*;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;

/**
 * Created by user on 20.07.15.
 */
@Stateless
@Local(WorkflowRequestManagerService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class WorkflowRequestManagerServiceImpl extends PrimaryKeyJPARepositoryServiceImpl<WorkflowRequestManager>
        implements WorkflowRequestManagerService {

    @Override
    public Boolean isReadForManager(WorkflowRequest request, UserAccount manager) {
        if (request.getRequestStatus().isFinalState()) {
            return true;
        }
        try {
            String queryStr = new String(
                    "SELECT manager.isRead from WorkflowRequestManager manager " +
                            " WHERE manager.fk_wfl_process_instance_id = '" + request.getId() + "' " +
                            "   AND manager.fk_manager_account_id = '" + manager.getId() + "' "
            );
            return (Boolean) getEntityManager().createNativeQuery(queryStr).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    @Override
    public Boolean isReadForSubmitter(WorkflowRequest request, Employee manager) {
        if (request.getRequestStatus().isFinalState()) {
            return true;
        }
        String queryStr = new String(
                "SELECT manager.isRead from WorkflowRequestManager manager " +
                        " WHERE manager.fk_wfl_process_instance_id = '" + request.getId() + "'; ");
        List<Boolean> resultList = getEntityManager().createNativeQuery(queryStr).getResultList();
        return resultList.contains(Boolean.TRUE);
    }

    @Override
    public Boolean readAtLeastByOneManager(WorkflowRequest request) {
        if (request.getRequestStatus().isFinalState()) {
            return true;
        }
        Collection<Boolean> collection = getEntityManager().createNativeQuery("" +
                "SELECT manager.isRead from WorkflowRequestManager manager " +
                " WHERE manager.fk_wfl_process_instance_id = '" + request.getId() + "' ").getResultList();
        for (Boolean read : collection) {
            if (read) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void markRead(WorkflowRequest request, UserAccount requester, Boolean isRead) {
        String updateString =
                new String("UPDATE WorkflowRequestManager AS manager " +
                        "      SET manager.isRead = " + (isRead ? 1 : 0)  + " " +
                        "    WHERE manager.fk_manager_account_id = '" + requester.getId() + "' " +
                        "      AND manager.fk_manager_tenant_id = '" + requester.getTenantId() + "' " +
                        "      AND manager.fk_wfl_process_instance_id = '" + request.getId() + "';");
        getEntityManager().createNativeQuery(updateString).executeUpdate();
    }

    @Override
    public WorkflowRequest cleanup(WorkflowRequest request) {
        if (request.getRequestStatus().isFinalState()) {
            String deleteQuery = new String("DELETE man.* FROM WorkflowRequestManager man WHERE man.fk_wfl_process_instance_id = '" + request.getId() + "';");
            Query query = getEntityManager().createNativeQuery(deleteQuery);
            query.executeUpdate();
        }
        return request;
    }

    @Override
    public Class<WorkflowRequestManager> getEntityClass() {
        return WorkflowRequestManager.class;
    }
}
