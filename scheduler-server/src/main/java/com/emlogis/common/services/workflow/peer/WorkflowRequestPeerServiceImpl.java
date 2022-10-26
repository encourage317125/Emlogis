/*
 * Package: com.emlogis.common.services.workflow
 *
 * File: WFLRoleInstanceService.java
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
package com.emlogis.common.services.workflow.peer;

import com.emlogis.common.services.common.PrimaryKeyJPARepositoryServiceImpl;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import org.apache.log4j.Logger;

import javax.ejb.*;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.emlogis.common.ModelUtils.commaSeparatedQuotedValues;
import static com.emlogis.common.services.common.GeneralJPARepository.Operation.select;


/**
 * Represents repository level access EJB for {@link WorkflowRequestPeer}.
 *
 * @author alex@borlis.net Copyright (2015)
 * @version 1.0
 *          reviewed by
 */
@Stateless
@Local(value = WorkflowRequestPeerService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class WorkflowRequestPeerServiceImpl
        extends PrimaryKeyJPARepositoryServiceImpl<WorkflowRequestPeer>
        implements WorkflowRequestPeerService {

    private final static Logger logger = Logger.getLogger(WorkflowRequestPeerServiceImpl.class);

    @Override
    public Class<WorkflowRequestPeer> getEntityClass() {
        return WorkflowRequestPeer.class;
    }

    @Override
    public Boolean readAtLeastByOnePeer(String requestId) {
        Collection<Boolean> isReadCl = getEntityManager().createNativeQuery("" +
                "SELECT DISTINCT (peer.isRead) from WorkflowRequestPeer peer " +
                " WHERE peer.fk_wfl_process_instance_id = '" + requestId + "' ").getResultList();
        for (Boolean read : isReadCl) {
            if (read) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void markRead(WorkflowRequest request, Employee requestEmployee, Boolean isRead) {
        String updateString =
                new String("UPDATE WorkflowRequestPeer AS peer " +
                        "      SET peer.isRead = " + (isRead ? 1 : 0) + " " +
                        "    WHERE peer.fk_recipient_employee_id = '" + requestEmployee.getId() + "' " +
                        "      AND peer.fk_recipient_tenant_id = '" + requestEmployee.getTenantId() + "' " +
                        "      AND peer.fk_wfl_process_instance_id = '" + request.getId() + "';");
        getEntityManager().createNativeQuery(updateString).executeUpdate();
    }

    @Override
    public Boolean isRead(String requestId, Employee requestEmployee) {
        String queryStr = "SELECT p.isRead FROM WorkflowRequestPeer p " +
                "WHERE p.fk_recipient_employee_id = '" + requestEmployee.getId() + "' " +
                "  AND p.fk_wfl_process_instance_id = '" + requestId + "' ";
        Collection<Boolean> isReadC = getEntityManager().createNativeQuery(queryStr).getResultList();
        return isReadC.contains(Boolean.TRUE);
    }

    @Override
    public List<WorkflowRequestPeer> findPeerConcurrentSwapRequests(String submitterShiftId, String requestId) {
        String queryStr =
                " SELECT peer.* FROM WorkflowRequestPeer peer " +
                        " JOIN WorkflowRequest req on req.id = fk_wfl_process_instance_id "+
                        "  WHERE req.requestType = 'SHIFT_SWAP_REQUEST' " +
                        "    AND peer.peerShiftId = '" + submitterShiftId + "' " +
                        "    AND req.status in ('PROCESS_INITIATED', 'READY_FOR_ACTION', 'READY_FOR_ADMIN', 'APPROVED') " +
                        "    AND req.id <> '" + requestId + "'; ";
        Query query = getEntityManager().createNativeQuery(queryStr, WorkflowRequest.class);
        return query.getResultList();
    }

    @Override
    public List<WorkflowRequestPeer> findPeerConcurrentSwapRequests(Set<String> shiftIds, String requestId) {
        String queryStr =
                " SELECT peer.* FROM WorkflowRequestPeer peer " +
                        " JOIN WorkflowRequest req on req.id = fk_wfl_process_instance_id "+
                        "  WHERE req.requestType = 'SHIFT_SWAP_REQUEST' " +
                        "    AND peer.peerShiftId in (" + commaSeparatedQuotedValues(shiftIds) + ") " +
                        "    AND req.status in ('PROCESS_INITIATED', 'READY_FOR_ACTION', 'READY_FOR_ADMIN', 'APPROVED') " +
                        "    AND req.id <> '" + requestId + "'; ";
        Query query = getEntityManager().createNativeQuery(queryStr, WorkflowRequest.class);
        return query.getResultList();
    }

    @Override
    public List<WorkflowRequestPeer> findPeerConcurrentWipRequests(
            Long startDate,
            Long endDate,
            String requestId,
            String employeeId
    ) {
        String queryStr =
                " SELECT peer.* FROM WorkflowRequestPeer peer " +
                        " JOIN WorkflowRequest req on req.id = fk_wfl_process_instance_id "+
                        "  WHERE req.requestType = 'WIP_REQUEST' " +
                        "    AND peer.Id = '" + employeeId + "' " +
                        "    AND req.requestDate BETWEEN '"+new Timestamp(startDate)+"' AND '"+new Timestamp(endDate)+"' "+
                        "    AND req.status in ('PROCESS_INITIATED', 'READY_FOR_ACTION', 'READY_FOR_ADMIN', 'APPROVED') " +
                        "    AND req.id <> '" + requestId + "'; ";
        Query query = getEntityManager().createNativeQuery(queryStr, WorkflowRequest.class);
        return query.getResultList();
    }

    @Override
    public List<WorkflowRequestPeer> findPeers(PrimaryKey requestPk, Employee recipient) {
        String queryStr = "" +
                " SELECT p.* FROM WorkflowRequestPeer p " +
                "  WHERE p.fk_wfl_process_instance_id = '" + requestPk.getId() + "' " +
                "    AND p.fk_wfl_process_tenant_id = '" + requestPk.getTenantId() + "' " +
                "    AND p.fk_recipient_employee_id = '" + recipient.getId() + "' " +
                "    AND p.fk_recipient_tenant_id = '" + recipient.getTenantId() + "';";
        Query query = getEntityManager().createNativeQuery(queryStr, WorkflowRequestPeer.class);
        return query.getResultList();
    }

    @Override
    public List<WorkflowRequestPeer> findPeers(PrimaryKey requestPk, Employee recipient, String shiftId) {
        if (shiftId == null) {
            return findPeers(requestPk, recipient);
        }
        if (shiftId.equals("EMPTY")) {
            return findPeers(requestPk, recipient);
        }
        try {
            return findAllBy(getBuilder().and(
                    getBuilder().equal(getFrom(select).get("process").get("primaryKey"), requestPk),
                    getBuilder().equal(getFrom(select).get("recipient"), recipient),
                    getBuilder().equal(getFrom(select).get("peerShiftId"), shiftId)
            ));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public WorkflowRequestPeer findPeer(PrimaryKey requestPk, Employee recipient, String shiftId) {
        return findBy(getBuilder().and(
                getBuilder().equal(getFrom(select).get("process").get("primaryKey"), requestPk),
                getBuilder().equal(getFrom(select).get("recipient"), recipient),
                getBuilder().equal(getFrom(select).get("peerShiftId"), shiftId)
        ));
    }
}
