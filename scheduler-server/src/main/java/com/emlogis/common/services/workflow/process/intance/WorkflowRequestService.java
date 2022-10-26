package com.emlogis.common.services.workflow.process.intance;

import com.emlogis.common.services.common.GeneralJPARepository;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.enums.status.RequestTechnicalStatusDict;

import java.util.List;
import java.util.Set;

/**
 * Created by alexborlis on 19.02.15.
 */
public interface WorkflowRequestService extends GeneralJPARepository<WorkflowRequest, PrimaryKey> {

    List<WorkflowRequest> findByTenantAndStatus(String tenantId, RequestTechnicalStatusDict status);

    WorkflowRequest findByEngineIdIfExists(Long engineId);

    List<WorkflowRequest> findByRecipientAndStatuses(
            List<RequestTechnicalStatusDict> statuses, Employee recipient);

    List<WorkflowRequest> findByRecipientAndStatus(
            RequestTechnicalStatusDict status, Employee recipient);

    List<WorkflowRequest> findByOriginatorAndStatuses(
            List<RequestTechnicalStatusDict> statuses, Employee originator);

    List<WorkflowRequest> findByOriginatorAndStatus(
            RequestTechnicalStatusDict status, Employee originator);

    List<WorkflowRequest> findOriginatedBy(Employee originator);

    List<WorkflowRequest> findAllByTenant(String tenantId);

    List<WorkflowRequest> findAllByParameters(String type, String site, List<String> teams, Long startDate, Long endDate);

    WorkflowRequest merge(WorkflowRequest instance);

    void removeRecipient(PrimaryKey requestPk, String employeeId);

    WorkflowRequest findByCode(String notificationCode);

    List<WorkflowRequest> findOpenShiftProcessesByShiftId(String shiftId);

    void declineOtherRequestsOnThatOpenShift(WorkflowRequest instance, PrimaryKey actorPk);

    List<WorkflowRequest> findShiftConcurrentRequests(String shiftId);

    List<WorkflowRequest> findConcurrentSwapWipRequests(String submitterShiftId, String requestId);

    List<WorkflowRequest> findConcurrentSwapWipRequests(Set<String> shiftIds, String requestId);

}
