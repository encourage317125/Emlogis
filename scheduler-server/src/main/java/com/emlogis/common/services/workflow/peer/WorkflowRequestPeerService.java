package com.emlogis.common.services.workflow.peer;

import com.emlogis.common.services.common.GeneralJPARepository;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;

import java.util.List;
import java.util.Set;

/**
 * Created by alexborlis on 19.02.15.
 */
public interface WorkflowRequestPeerService extends GeneralJPARepository<WorkflowRequestPeer, PrimaryKey> {

    List<WorkflowRequestPeer> findPeers(PrimaryKey requestPk, Employee recipient);

    List<WorkflowRequestPeer> findPeers(PrimaryKey requestPk, Employee recipient, String shiftId);

    WorkflowRequestPeer findPeer(PrimaryKey requestPk, Employee recipient, String shiftId);

    Boolean readAtLeastByOnePeer(String requestId);

    void markRead(WorkflowRequest request, Employee requestEmployee, Boolean isRead);

    Boolean isRead(String requestId, Employee requestEmployee);

    List<WorkflowRequestPeer> findPeerConcurrentSwapRequests(String submitterShiftId, String requestId);

    List<WorkflowRequestPeer> findPeerConcurrentSwapRequests(Set<String> shiftIds, String requestId);

    List<WorkflowRequestPeer> findPeerConcurrentWipRequests(
            Long startDate,
            Long endDate,
            String requestId,
            String employeeId);

}
