/*
 * Package: com.emlogis.common.services.workflow
 *
 * File: WorkflowRequestService.java
 *
 * Created:February 6, 2015 1:57:06 PM
 *
 * Copyright (c) 2004-2012 EmLogis, Inc. 9800 Richmond Ave. Suite 235, Houston,
 * Texas, 77042, U.S.A. All rights reserved.
 *
 * This software is the confidential and proprietary information of EmLogis,
 * Inc. ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with EmLogis.
 *
 * Date: February 6, 2015
 *
 * Author: alex@borlis.net
 *
 * Changes: Created
 */
package com.emlogis.common.services.workflow.process.intance;

import com.emlogis.common.services.common.PrimaryKeyJPARepositoryServiceImpl;
import com.emlogis.common.services.schedule.ShiftService;
import com.emlogis.common.services.workflow.peer.WorkflowRequestPeerService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.workflow.entities.WflProcess;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestLog;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.enums.WorkflowActionDict;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.enums.status.RequestTechnicalStatusDict;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;

import javax.ejb.*;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.*;

import static com.emlogis.common.ModelUtils.commaSeparatedQuotedValues;
import static com.emlogis.common.services.common.GeneralJPARepository.Operation.select;
import static com.emlogis.workflow.WflUtil.*;

/**
 * Represents repository level access EJB for {@link WorkflowRequest}.
 *
 * @author alex@borlis.net Copyright (2015)
 * @version 1.0
 *          reviewed by
 */
@Stateless
@Local(value = WorkflowRequestService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class WorkflowRequestServiceImpl
        extends PrimaryKeyJPARepositoryServiceImpl<WorkflowRequest>
        implements WorkflowRequestService {

    private final static Logger logger = Logger.getLogger(WorkflowRequestServiceImpl.class);
    private static final Calendar FAR_PAST = new GregorianCalendar(1939, Calendar.SEPTEMBER, 1);
    private static final Calendar FAR_FUTURE = new GregorianCalendar(2139, Calendar.SEPTEMBER, 1);

    private static final String INITIATOR_FIELD = "initiator";
    private static final String CHANGER_FIELD = "statusChanger";
    private static final String PROCESS_FIELD = "protoProcess";
    private static final String ENGINE_ID_FIELD = "engineId";
    private static final String STATUS_FIELD = "status";
    private static final String SUBMITTER_SHIFT_ID_FIELD = "submitterShiftId";
    private static final String SUBMITTER_TEAM_ID_FIELD = "submitterTeamId";

    @EJB
    private ShiftService shiftService;

    @EJB
    private WorkflowRequestPeerService workflowRequestPeerService;


    @Override
    public Class<WorkflowRequest> getEntityClass() {
        return WorkflowRequest.class;
    }

    @Override
    public void declineOtherRequestsOnThatOpenShift(WorkflowRequest instance, PrimaryKey actorPk) {
        List<WorkflowRequest> sameOpenShiftRequests = findAllBy(getBuilder().and(
                getBuilder().equal(getFrom(select).get("requestType"), WorkflowRequestTypeDict.OPEN_SHIFT_REQUEST),
                getBuilder().equal(getFrom(select).get("submitterShiftId"), instance.getSubmitterShiftId())));
        for (WorkflowRequest request : sameOpenShiftRequests) {
            if (!request.equals(instance)) {
                request.setStatus(RequestTechnicalStatusDict.TERMINATED);
                request.setRequestStatus(getRequestStatus(request));
                request = update(request);
                for (WorkflowRequestPeer wrp : request.getRecipients()) {
                    wrp.setPeerStatus(getPeerAggregatedRequestStatus(request, wrp));
                    workflowRequestPeerService.update(wrp);
                }
                WorkflowRequestLog workflowRequestLog =
                        new WorkflowRequestLog(request, WorkflowActionDict.PROCESS_TERMINATED, WorkflowRoleDict.MANAGER,
                                actorPk.getId(), instance.hasShift() ? instance.getSubmitterShiftId() : null,
                                "declined because posted open shift not actual");
                request.getActions().add(workflowRequestLog);
                update(request);
            }
        }
    }

    @Override
    public List<WorkflowRequest> findByTenantAndStatus(
            String tenantId, RequestTechnicalStatusDict status
    ) {
        return findAllBy(getBuilder().and(
                getBuilder().equal(getFrom(select).get(INITIATOR_FIELD).get(TENANT_FIELD), tenantId),
                getBuilder().equal(getFrom(select).get(STATUS_FIELD), status)
        ));
    }

    @Override
    public WorkflowRequest findByEngineIdIfExists(Long engineId) {
        try {
            return findBy(getBuilder().equal(getFrom(select).get(ENGINE_ID_FIELD), engineId));
        } catch (Exception nre) {
            //expected
            logger.info("expected exception");
            return null;
        }
    }

    @Override
    public List<WorkflowRequest> findByRecipientAndStatuses(
            List<RequestTechnicalStatusDict> statuses, Employee recipient
    ) {
        Set<WorkflowRequest> resultList = new HashSet<>();
        for (RequestTechnicalStatusDict status : statuses) {
            resultList.addAll(findByRecipientAndStatus(status, recipient));
        }
        return new ArrayList<>(resultList);
    }

    @Override
    public List<WorkflowRequest> findByRecipientAndStatus(
            RequestTechnicalStatusDict status, Employee recipient
    ) {
        String query = "SELECT WorkflowRequest.* " +
                "FROM WorkflowRequest, WorkflowRequestPeer WHERE " +
                "WorkflowRequestPeer.fk_wfl_process_instance_id = WorkflowRequest.id AND " +
                "WorkflowRequest.status = '" + status.name() + "' AND " +
                "WorkflowRequestPeer.fk_recipient_tenant_id = '" + recipient.getTenantId() + "' AND " +
                "WorkflowRequestPeer.fk_recipient_employee_id = '" + recipient.getId() + "';";
        Query nativeQuery = getEntityManager().createNativeQuery(query, WorkflowRequest.class);
        return nativeQuery.getResultList();
    }


    @Override
    public List<WorkflowRequest> findByOriginatorAndStatuses(
            List<RequestTechnicalStatusDict> statuses, Employee originator
    ) {
        List<WorkflowRequest> resultList = new ArrayList<>();
        for (RequestTechnicalStatusDict status : statuses) {
            resultList.addAll(findByOriginatorAndStatus(status, originator));
        }
        return resultList;
    }

    @Override
    public List<WorkflowRequest> findByOriginatorAndStatus(
            RequestTechnicalStatusDict status, Employee originator
    ) {
        return findAllBy(getBuilder().and(
                getBuilder().equal(getFrom(select).get(STATUS_FIELD), status),
                getBuilder().equal(getFrom(select).get(INITIATOR_FIELD), originator)
        ));
    }

    @Override
    public List<WorkflowRequest> findOriginatedBy(Employee originator) {
        return findAllBy(getBuilder().equal(getFrom(select).get(INITIATOR_FIELD), originator));
    }

    @Override
    public List<WorkflowRequest> findAllByTenant(String tenantId) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT instance.* FROM WorkflowRequest instance ");
        queryBuilder.append("WHERE instance.fk_initiator_tenant_id = '" + tenantId + "';");
        Query query = getEntityManager().createNativeQuery(queryBuilder.toString(), WorkflowRequest.class);
        return query.getResultList();
    }

    @Override
    public List<WorkflowRequest> findAllByParameters(
            String type, String siteId, List<String> teams, Long startDate, Long endDate
    ) {
        String queryString = "" +
                "SELECT req.* FROM WorkflowRequest req " +
                " JOIN Site s ON (s.tenantId = req.tenantId AND s.id = '" + siteId + "' ) " +
                " WHERE requestType = '" + type + "' " +
                "   AND submitterTeamId in (" + commaSeparatedQuotedValues(teams) + ") ";
        if (startDate != null) {
            if (endDate != null) {
                queryString += " AND req.created BETWEEN '" + new Timestamp(startDate) + "' " +
                        "AND '" + new Timestamp(endDate) + "' ";
            } else {
                queryString += " AND req.created BETWEEN '" + toZonedTimeStamp(startDate) + "' " +
                        "AND '" + toZonedTimeStamp(null) + "' ";
            }
        }
        Query query = getEntityManager().createNativeQuery(queryString, WorkflowRequest.class);
        return query.getResultList();
    }

    private Timestamp toZonedTimeStamp(Long date) {
        if (date == null) {
            return new Timestamp(Calendar.getInstance(DateTimeZone.UTC.toTimeZone()).getTimeInMillis());
        } else {
            return new Timestamp(date);
        }
    }

    @Override
    public WorkflowRequest merge(WorkflowRequest instance) {
        try {
            WorkflowRequest request = findByParameters(
                    instance.getProtoProcess(),
                    instance.getStatus(),
                    instance.getStatusChanger(),
                    instance.getInitiator(),
                    instance.getSubmitterShiftId(),
                    instance.getSubmitterTeamId(),
                    instance.getData());
            request.getActions().clear();
            return update(request);
        } catch (NoResultException nre) {
            return create(instance);
        }
    }

    @Override
    public void removeRecipient(PrimaryKey requestPk, String employeeId) {
        WorkflowRequest instance = find(requestPk);
        instance = recalculatePeersStatuses(instance);
        Set<WorkflowRequestPeer> recipients = instance.getRecipients();
        Iterator<WorkflowRequestPeer> iterator = recipients.iterator();
        while (iterator.hasNext()) {
            WorkflowRequestPeer role = iterator.next();
            if (role.getRecipient().getId().equals(employeeId)) {
                iterator.remove();
            }
        }
        instance.setRecipients(recipients);
        update(recalculatePeersStatuses(instance));
    }

    @Override
    public WorkflowRequest findByCode(String notificationCode) {
        return findBy(getBuilder().equal(getFrom(select).get("notificationCode"), notificationCode));
    }

    @Override
    public List<WorkflowRequest> findOpenShiftProcessesByShiftId(String shiftId) {
        return findAllBy(getBuilder().and(
                getBuilder().equal(getFrom(select).get("protoProcess").get("type").get("type"),
                        WorkflowRequestTypeDict.OPEN_SHIFT_REQUEST),
                getBuilder().equal(getFrom(select).get(SUBMITTER_SHIFT_ID_FIELD), shiftId)
        ));
    }


    public List<WorkflowRequest> findShiftConcurrentRequests(String shiftId) {
        String queryStr =
                " SELECT req.* FROM WorkflowRequest req " +
                        "  WHERE req.requestType in ('SHIFT_SWAP_REQUEST', 'WIP_REQUEST') " +
                        "    AND req.submitterShiftId = '" + shiftId + "' " +
                        "    AND req.status in ('PROCESS_INITIATED', 'READY_FOR_ACTION', 'READY_FOR_ADMIN', 'APPROVED'); ";
        Query query = getEntityManager().createNativeQuery(queryStr, WorkflowRequest.class);
        return query.getResultList();
    }

    public List<WorkflowRequest> findConcurrentSwapWipRequests(String submitterShiftId, String requestId) {
        String queryStr =
                " SELECT req.* FROM WorkflowRequest req " +
                        "  WHERE req.requestType in ('SHIFT_SWAP_REQUEST', 'WIP_REQUEST') " +
                        "    AND req.submitterShiftId = '" + submitterShiftId + "' " +
                        "    AND req.status in ('PROCESS_INITIATED', 'READY_FOR_ACTION', 'READY_FOR_ADMIN', 'APPROVED') " +
                        "    AND req.id <> '" + requestId + "'; ";
        Query query = getEntityManager().createNativeQuery(queryStr, WorkflowRequest.class);
        return query.getResultList();
    }

    @Override
    public List<WorkflowRequest> findConcurrentSwapWipRequests(Set<String> shiftIds, String requestId) {
        String queryStr =
                " SELECT req.* FROM WorkflowRequest req " +
                        "  WHERE req.requestType in ('SHIFT_SWAP_REQUEST', 'WIP_REQUEST') " +
                        "    AND req.submitterShiftId in (" + commaSeparatedQuotedValues(shiftIds) + ") " +
                        "    AND req.status in ('PROCESS_INITIATED', 'READY_FOR_ACTION', 'READY_FOR_ADMIN', 'APPROVED') " +
                        "    AND req.id <> '" + requestId + "'; ";
        Query query = getEntityManager().createNativeQuery(queryStr, WorkflowRequest.class);
        return query.getResultList();
    }


    private WorkflowRequest findByParameters(
            WflProcess process, RequestTechnicalStatusDict status, Employee statusChanger,
            Employee originator, String shiftId, String teamId, String data
    ) {
        try {
            return findBy(
                    getBuilder().and(
                            getBuilder().equal(getFrom(select).get(PROCESS_FIELD), process),
                            getBuilder().equal(getFrom(select).get(CHANGER_FIELD), statusChanger),
                            getBuilder().equal(getFrom(select).get(INITIATOR_FIELD), originator),
                            getBuilder().equal(getFrom(select).get(SUBMITTER_SHIFT_ID_FIELD), shiftId),
                            getBuilder().equal(getFrom(select).get(SUBMITTER_TEAM_ID_FIELD), teamId),
                            getBuilder().equal(getFrom(select).get("data"), data)
                    )
            );
        } catch (Throwable throwable) {
            if (throwable instanceof NoResultException) {
                throw throwable;
            }
            String parameters = new StringBuilder()
                    .append("process : " + process.toString() + "\\n")
                    .append("statusChangerEmployeeId : " + statusChanger.getId() + "\\n")
                    .append("statusChangerTenantId : " + statusChanger.getTenantId() + "\\n")
                    .append("initiatorEmployeeId : " + originator.getId() + "\\n")
                    .append("initiatorTenantId : " + originator.getTenantId() + "\\n")
                    .append("submitterShiftId : " + shiftId + "\\n")
                    .append("teamId : " + teamId + "\\n").toString();
            logger.error("Error while looking for a workflow process instance by parameters: " + parameters, throwable);
            throw new RuntimeException("Error while looking for a workflow process instance by parameters: " + parameters);
        }
    }
}
