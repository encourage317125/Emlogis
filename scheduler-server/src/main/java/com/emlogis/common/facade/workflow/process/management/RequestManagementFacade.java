package com.emlogis.common.facade.workflow.process.management;

import com.emlogis.model.employee.Employee;
import com.emlogis.model.workflow.dto.process.request.AddRequestPeersDto;
import com.emlogis.model.workflow.dto.process.request.RemoveRequestPeersDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.exception.WorkflowServerException;

/**
 * Created by user on 21.08.15.
 */
public interface RequestManagementFacade {

    void removeRequestPeers(
            Employee employee,
            WorkflowRequest request,
            RemoveRequestPeersDto dto
    ) throws WorkflowServerException;

    void addRequestPeers(
            Employee employee,
            WorkflowRequest request,
            AddRequestPeersDto dto
    ) throws WorkflowServerException;

    void withdrawRequestsByShiftId(String tenantId, String userAccountId, String shiftId, String reason) throws Exception;

    void deteleAllRequests();
}
