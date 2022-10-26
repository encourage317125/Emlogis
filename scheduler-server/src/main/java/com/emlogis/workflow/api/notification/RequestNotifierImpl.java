package com.emlogis.workflow.api.notification;

import com.emlogis.common.Constants;
import com.emlogis.common.facade.workflow.notification.annotations.RequestNotificationBuilderQualifierImpl;
import com.emlogis.common.facade.workflow.notification.builder.RequestNotificationContentBuilder;
import com.emlogis.common.notifications.NotificationDeliveryFormat;
import com.emlogis.common.notifications.NotificationDeliveryMethod;
import com.emlogis.common.notifications.NotificationOperation;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.notification.NotificationService;
import com.emlogis.common.services.notification.template.NotificationMessageTemplateServiceImpl;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.common.services.workflow.notification.RequestNotificationEventService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.NotificationConfig;
import com.emlogis.model.notification.dto.NotificationMessageDTO;
import com.emlogis.model.tenant.PersonalizedEntity;
import com.emlogis.model.workflow.entities.RequestNotificationEvent;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.enums.status.WorkflowRequestStatusDict;
import org.apache.log4j.Logger;

import javax.ejb.*;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.emlogis.common.ModelUtils.commaSeparatedQuotedIds;
import static com.emlogis.model.notification.NotificationPriorityType.NORMAL;
import static com.emlogis.model.workflow.entities.RequestNotificationStatus.*;
import static com.emlogis.workflow.WflUtil.*;
import static com.emlogis.workflow.enums.WorkflowRoleDict.MANAGER;
import static java.util.UUID.randomUUID;

/**
 * Created by user on 13.08.15.
 */
@Stateless
@Local(value = RequestNotifier.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class RequestNotifierImpl implements RequestNotifier {

    private final Logger logger = Logger.getLogger(RequestNotifierImpl.class);

    @EJB
    private NotificationService notificationService;

    @EJB
    private RequestNotificationEventService requestNotificationEventService;

    @Inject
    @Any
    private Instance<RequestNotificationContentBuilder> contentBuilders;

    @EJB
    private EmployeeService employeeService;

    @EJB
    private UserAccountService userAccountService;

    @EJB
    private WorkflowRequestService wsService;

    @EJB
    private NotificationMessageTemplateServiceImpl notificationMessageTemplateService;

    @PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager em;

    @Override
    public String process() {
        Boolean failed = false;
        try {
            Set<RequestNotificationEvent> set = new HashSet<>(requestNotificationEventService.getNotificationsToSend());
            Iterator<RequestNotificationEvent> iterator = set.iterator();
            while (!failed && iterator.hasNext()) {
                RequestNotificationEvent event = iterator.next();
                WorkflowRequest request = wsService.find(new PrimaryKey(event.getTenantId(), event.getRequestId()));
                PersonalizedEntity person = person(event.getTenantId(), event.getActorId(), event.getRole());
                NotificationMessageDTO messageDTO = null;
                try {
                    messageDTO = notifyRecipient(request, event.getRole(), person, event.getShiftId(), event.getCode());
                    String notificationId = queueNotification(messageDTO);
                    try {
                        if (notificationId == null) {
                            logger.debug("Notification framework fails on " + event.toString());
                            System.out.println("Notification framework fails on " + event.toString());
                            event.getQueuedNotifications().put(randomUUID().toString() + FAILED.identifier(),
                                    currentDate().toString());
                            event.setReason("Notifier failed on send operation");
                            event.setStatus(FAILED);
                        } else {
                            if (notificationId.equals(NOT_QUALIFIED.name())) {
                                event.getQueuedNotifications().put(randomUUID().toString() + NOT_QUALIFIED.identifier(),
                                        currentDate().toString());
                                event.setReason(NOT_QUALIFIED.name());
                                event.setStatus(NOT_QUALIFIED);
                            } else if (notificationId.contains(FAILED.name())) {
                                event.getQueuedNotifications().put(randomUUID().toString() + FAILED.identifier(),
                                        currentDate().toString());
                                event.setReason(notificationId);
                                event.setStatus(FAILED);
                            } else if (notificationId.equals(DUPLICATION.name())) {
                                event.getQueuedNotifications().put(randomUUID().toString() + DUPLICATION.identifier(),
                                        currentDate().toString());
                                event.setReason(DUPLICATION.name());
                                event.setStatus(DUPLICATION);
                            } else {
                                event.getQueuedNotifications().put(notificationId, currentDate().toString());
                                event.setStatus(QUEUED);
                            }
                        }
                        requestNotificationEventService.update(event);
                    } catch (Exception error) {
                        error.printStackTrace();
                        failed = true;
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    failed = true;
                }
            }
            return commaSeparatedQuotedIds(set);
        } catch (Exception error) {
            error.printStackTrace();
            throw new RuntimeException("Error while sanding notifications", error);
        }
    }


    private String queueNotification(NotificationMessageDTO messageDTO) {
        if (messageDTO != null) {
            if (templateExists(messageDTO)) {
                try {
                    return notificationService.sendNotification(messageDTO);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    logger.error("Error while calling notification API", exception);
                    return FAILED.identifier() + exception.getMessage();
                }
            } else {
                return DUPLICATION.name();
            }
        }
        return NOT_QUALIFIED.name();
    }

    private Boolean templateExists(NotificationMessageDTO messageDTO) {
        String templateName = messageDTO.getNotificationCategory().getFilePart() + "_" +
                messageDTO.getNotificationRole().getFilePart() + "_" +
                messageDTO.getNotificationOperation().getFilePart() +
                "_notification_";
        return notificationMessageTemplateService.templatesNameLikeExists(templateName);
    }

//    private boolean suchNotificationNotSentYet(NotificationMessageDTO dto, String logCode) {
//        try {
//            String queryStr = " " +
//                    " SELECT count(sn.id) FROM SendNotification sn " +
//                    "   JOIN SendNotification_messageAttributes snma on " +
//                    "( snma.SendNotification_id = sn.id " +
//                    " AND (snma.messageAttributes_KEY = 'logCode' " +
//                    " AND snma.messageAttributes = '" + logCode + "'))" +
//                    "  WHERE sn.notificationCategory = '" + dto.getNotificationCategory().name() + "' " +
//                    "    AND sn.notificationRole = '" + dto.getNotificationRole().name() + "' " +
//                    "    AND sn.notificationOperation = '" + dto.getNotificationOperation().name() + "' ";
//            Query query = em.createNativeQuery(queryStr);
//            return ((BigInteger) query.getSingleResult()).longValue() == 0;
//        } catch (Throwable error) {
//            error.printStackTrace();
//            throw new RuntimeException(error);
//        }
//    }

    private PersonalizedEntity person(String tenantId, String id, WorkflowRoleDict role) {
        PersonalizedEntity result = null;
        try {
            if (role.equals(MANAGER)) {
                result = userAccountService.getUserAccount(new PrimaryKey(tenantId, id));
            } else {
                result = employeeService.getEmployee(new PrimaryKey(tenantId, id));
            }
            return result;
        } catch (Throwable throwable) {
            logger.error("Can not find such Employee/UserAccount - tenantId: " +
                    tenantId + " id: " + id + " for Role: " + role.name(), throwable);
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }

    private Boolean identifyEmloyeeEnabledOnTypeAndMethod(
            Employee employee,
            NotificationDeliveryFormat format,
            NotificationDeliveryMethod method
    ) {
        for (NotificationConfig nc : employee.getNotificationConfigs()) {
            if (nc.getFormat().equals(format) && nc.getMethod().equals(method)) {
                return nc.getEnabled();
            }
        }
        return false;
    }

    private NotificationMessageDTO notifyRecipient(
            WorkflowRequest request,
            WorkflowRoleDict role,
            PersonalizedEntity personalizedEntity,
            String shiftId,
            String logCode
    ) {
        try {
            NotificationOperation notificationOperation = identifyNotificationOperation(request);
            String receiverUserId = null;
            if (role.equals(MANAGER)) {
                receiverUserId = personalizedEntity.getId();
            } else {
                receiverUserId = ((Employee) personalizedEntity).getUserAccount().getId();
            }
            if (notificationOperation != null) {
                if (conditionsOk(request, role)) {
                    RequestNotificationContentBuilder service = contentBuilders.select(
                            new RequestNotificationBuilderQualifierImpl(request.getRequestType(), role)).get();
                    NotificationMessageDTO notificationMessageDTO = new NotificationMessageDTO();
                    notificationMessageDTO.setSenderUserId(request.getInitiator().getUserAccount().getId());
                    notificationMessageDTO.setMessageAttributes(service.build(request, personalizedEntity, shiftId, logCode));
                    notificationMessageDTO.setIsWorkflowType(true);
                    notificationMessageDTO.setNotificationRole(role.notificationRole());
                    notificationMessageDTO.setNotificationOperation(notificationOperation);
                    notificationMessageDTO.setNotificationCategory(request.getRequestType().getNotificationCategory());
                    notificationMessageDTO.setTenantId(request.getInitiator().getTenantId());
                    notificationMessageDTO.setReceiverUserId(receiverUserId);
                    notificationMessageDTO.setPriorityType(NORMAL);
                    return notificationMessageDTO;
                }
            }
            return null;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException("Error while sanding notifications", throwable);
        }
    }

    private NotificationOperation identifyNotificationOperation(WorkflowRequest instance) {
        if (instance.getRequestStatus().equals(WorkflowRequestStatusDict.ADMIN_PENDING)) {
            return NotificationOperation.BECOME_ADMIN_PENDING;
        }
        if (instance.getRequestStatus().equals(WorkflowRequestStatusDict.PEER_PENDING)) {
            return NotificationOperation.BECOME_PEER_PENDING;
        }
        if (instance.getRequestStatus().isFinalState()) {
            return NotificationOperation.OBTAINED_FINAL_STATE;
        }
        return null;
    }

    private boolean conditionsOk(
            WorkflowRequest request,
            WorkflowRoleDict role
    ) {
        RequestConditions rc = new RequestConditions(request, role);

        //cases for manager
        if (rc.manager() && !rc.submitterHasAutoApproval() && rc.adminPending() && !isSwapOrWip(request)) {
            return true;
        }
        if (rc.manager() && rc.adminPending() && isSwapOrWip(request) && (!rc.approvedPeerHasAutoApproval() || !rc.submitterHasAutoApproval())) {
            return true;
        }
        if (rc.manager() && rc.finalState() && rc.submitterHasAutoApproval() && rc.approvedPeerHasAutoApproval() && isSwapOrWip(request)) {
            return true;
        }
        if (rc.manager() && rc.finalState() && rc.submitterHasAutoApproval() && !isSwapOrWip(request)) {
            return true;
        }

        //cases for peer
        if (rc.peer() && !isSwapOrWip(request)) {
            return false;
        }
        if (rc.peer() && isSwapOrWip(request) && rc.finalState()) {
            return true;
        }

        //cases for submitter
        if (rc.submitter()) {
            return true;
        }

        return false;
    }

    private class RequestConditions {
        private final WorkflowRequest request;
        private final WorkflowRoleDict role;
        private Boolean finalState;
        private Boolean adminPending;
        private Boolean approvedPeerHasAutoApproval;
        private Boolean submitterHasAutoApproval;
        private Boolean manager;
        private Boolean peer;
        private Boolean submitter;


        public RequestConditions(WorkflowRequest request, WorkflowRoleDict role) {
            this.request = request;
            this.role = role;
        }

        public Boolean finalState() {
            if (finalState == null) {
                finalState = request.getRequestStatus().isFinalState();
            }
            return finalState;
        }

        public Boolean adminPending() {
            if (adminPending == null) {
                adminPending = request.getRequestStatus().equals(WorkflowRequestStatusDict.ADMIN_PENDING);
            }
            return adminPending;
        }

        public Boolean approvedPeerHasAutoApproval() {
            if (approvedPeerHasAutoApproval == null) {
                approvedPeerHasAutoApproval = approvedPeerHasAutoApproval(request);
            }
            return approvedPeerHasAutoApproval;
        }

        public Boolean submitterHasAutoApproval() {
            if (submitterHasAutoApproval == null) {
                submitterHasAutoApproval = employeeHasAutoApprovalFlag(request.getProtoProcess(), request.getInitiator());
            }
            return submitterHasAutoApproval;
        }

        public Boolean manager() {
            if (manager == null) {
                manager = role.equals(MANAGER);
            }
            return manager;
        }

        public Boolean peer() {
            if (peer == null) {
                peer = role.equals(WorkflowRoleDict.PEER);
            }
            return peer;
        }

        public Boolean submitter() {
            if (submitter == null) {
                submitter = role.equals(WorkflowRoleDict.ORIGINATOR);
            }
            return submitter;
        }

        private Boolean approvedPeerHasAutoApproval(WorkflowRequest request) {
            if (!isSwapOrWip(request)) {
                return false;
            } else {
                List<WorkflowRequestPeer> peersApproved = recipientsApproved(request);
                for (WorkflowRequestPeer peer : peersApproved) {
                    if (employeeHasAutoApprovalFlag(request.getProtoProcess(), peer.getRecipient())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

}
