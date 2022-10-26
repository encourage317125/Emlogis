package com.emlogis.workflow.api.notification;

import com.emlogis.common.services.workflow.notification.RequestNotificationEventService;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.PersonalizedEntity;
import com.emlogis.model.workflow.entities.RequestNotificationEvent;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.api.identification.RequestRoleProxy;
import com.emlogis.workflow.enums.WorkflowRoleDict;

import javax.ejb.*;
import java.util.*;

import static com.emlogis.workflow.enums.WorkflowRoleDict.values;

/**
 * Created by alexborlis on 19.02.15.
 */
@Stateless
@Local(value = WorkflowNotificationFacade.class)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class WorkflowNotificationFacadeImpl implements WorkflowNotificationFacade {

    @EJB
    private RequestNotifier notifier;

    @EJB
    private RequestRoleProxy requestRoleProxy;

    @EJB
    private RequestNotificationEventService requestNotificationEventService;

    @Override
    public void notifyAll(String tenantId, WorkflowRequest request, String shiftId) {
        try {
            for (WorkflowRoleDict role : values()) {
                Set<? extends PersonalizedEntity> employees = new HashSet<>(findEmployeeAsRoleToRequest(request, role));
                for (PersonalizedEntity pe : employees) {
                    RequestNotificationEvent event = new RequestNotificationEvent(request, request.lastAction(), pe.getId(), role, shiftId);
                    List<RequestNotificationEvent> sameEvents = requestNotificationEventService.findEventsByParameters(event);
                    if (sameEvents.isEmpty()) {
                        requestNotificationEventService.merge(event);
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new RuntimeException("Can not push request notifications to stack");
        }
    }

    private List<? extends PersonalizedEntity> findEmployeeAsRoleToRequest(WorkflowRequest instance, WorkflowRoleDict role) {
        switch (role) {
            case ORIGINATOR: {
                return Arrays.asList(instance.getInitiator());
            }
            case PEER: {
                List<Employee> resultList = new ArrayList<>();
                for (WorkflowRequestPeer peer : instance.getRecipients()) {
                    resultList.add(peer.getRecipient());
                }
                return resultList;
            }
            case MANAGER: {
                return requestRoleProxy.findManagers(instance.getRequestType(), instance.getInitiator());
            }
        }
        throw new RuntimeException("Unknown workflow request type");
    }

}
