package com.emlogis.common.services.workflow.action;

import com.emlogis.common.services.common.PrimaryKeyJPARepositoryServiceImpl;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestLog;
import com.emlogis.workflow.enums.WorkflowActionDict;

import javax.ejb.*;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * Created by alexborlis on 15.02.15.
 */
@Stateless
@Local(value = RequestLogService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class RequestLogServiceImpl
        extends PrimaryKeyJPARepositoryServiceImpl<WorkflowRequestLog>
        implements RequestLogService {

    @Override
    public Class<WorkflowRequestLog> getEntityClass() {
        return WorkflowRequestLog.class;
    }

    @Override
    public WorkflowRequestLog create(WorkflowRequestLog workflowRequestLog) {
        WorkflowRequestLog actionLog = null;
        try {
            actionLog = findActionByParameters(
                    workflowRequestLog.getProcessInstance(),
                    workflowRequestLog.getAction(),
                    workflowRequestLog.getActorId(),
                    workflowRequestLog.getShiftId());
        } catch (NoResultException nre) {
            actionLog = super.create(workflowRequestLog);
        }
        return actionLog;
    }

    private WorkflowRequestLog findActionByParameters(
            WorkflowRequest request,
            WorkflowActionDict action,
            String actorId,
            String shiftId
    ) {
        String queryStr = " SELECT log.* FROM WorkflowRequestLog log " +
                " WHERE log.fk_wfl_process_instance_id = '" + request.getId() + "' " +
                "   AND log.fk_wfl_process_tenant_id = '" + request.getTenantId() + "' " +
                "   AND log.action = '" + action.name() + "' " +
                "   AND log.shiftId = '" + shiftId + "' " +
                "   AND log.fk_actor_id = '" + actorId + "';";
        Query query = getEntityManager().createNativeQuery(queryStr, WorkflowRequestLog.class);
        return (WorkflowRequestLog) query.getSingleResult();
    }

    private WorkflowRequestLog findActionByParameters(
            WorkflowRequest request,
            WorkflowActionDict action,
            PrimaryKey employeePk,
            PrimaryKey userAccountPk,
            String shiftId
    ) {
        String queryStr = " SELECT log.* FROM WorkflowRequestLog log " +
                " WHERE log.fk_wfl_process_instance_id = '" + request.getId() + "' " +
                "   AND log.fk_wfl_process_tenant_id = '" + request.getTenantId() + "' " +
                "   AND log.action = '" + action.name() + "' " +
                "   AND log.shiftId = '" + shiftId + "' " +
                "   AND (log.fk_actor_id = '" + employeePk.getId() + "' OR log.fk_actor_id = '" + userAccountPk.getId() + "');";
        Query query = getEntityManager().createNativeQuery(queryStr, WorkflowRequestLog.class);
        return (WorkflowRequestLog) query.getSingleResult();
    }

}
