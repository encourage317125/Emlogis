package com.emlogis.common.services.workflow.notification;

import com.emlogis.common.services.common.PrimaryKeyJPARepositoryServiceImpl;
import com.emlogis.model.workflow.entities.RequestNotificationEvent;

import javax.ejb.*;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

import static com.emlogis.common.ModelUtils.commaSeparatedQuotedValues;

/**
 * Created by user on 14.08.15.
 */
@Stateless
@Local(value = RequestNotificationEventService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class RequestNotificationEventServiceImpl
        extends PrimaryKeyJPARepositoryServiceImpl<RequestNotificationEvent>
        implements RequestNotificationEventService {

    @Override
    public Class<RequestNotificationEvent> getEntityClass() {
        return RequestNotificationEvent.class;
    }

    public List<RequestNotificationEvent> getNotificationsToSend() {
        String queryStr = "" +
                " SELECT rnl.* FROM RequestNotificationEvent rnl " +
                "  WHERE rnl.status = 'PENDING' LIMIT 50 ";
        Query query = getEntityManager().createNativeQuery(queryStr, RequestNotificationEvent.class);
        return query.getResultList();
    }

    public void updateProcessed(List<String> notificationIds) {
        try {
            String queryStr = "" +
                    " UPDATE RequestNotificationEvent rnl " +
                    " SET rnl.status = 'PROCESSED' " +
                    "   JOIN RequestNotificationEvent rnl ON ( qa.RequestNotificationEvent_id = rnl.id " +
                    "    AND qa.RequestNotificationEvent_tenantId = rnl.tenantId )" +
                    "  WHERE qa.queuedNotifications_KEY in (" + commaSeparatedQuotedValues(notificationIds) + ") " +
                    "    AND rnl.status in ('QUEUED') ";
            Query query = getEntityManager().createNativeQuery(queryStr);
            query.executeUpdate();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void deleteSelected(String idsCommaSeparated) {
        try {
            String queryStrAttributes = "" +
                    " DELETE qa.* FROM RequestNotificationEvent_queuedNotifications qa " +
                    "   JOIN RequestNotificationEvent rnl ON ( qa.RequestNotificationEvent_id = rnl.id " +
                    "    AND qa.RequestNotificationEvent_tenantId = rnl.tenantId )" +
                    "  WHERE rnl.id in (" + idsCommaSeparated + ") " +
                    "    AND rnl.status in ('PROCESSED', 'NOT_QUALIFIED', 'DUPLICATION') ";
            Query queryAttributes = getEntityManager().createNativeQuery(queryStrAttributes);
            queryAttributes.executeUpdate();
            String queryStr = "" +
                    " DELETE rnl.* FROM RequestNotificationEvent rnl " +
                    "  WHERE rnl.id in (" + idsCommaSeparated + ") " +
                    "    AND rnl.status in ('PROCESSED', 'NOT_QUALIFIED', 'DUPLICATION') ";
            Query query = getEntityManager().createNativeQuery(queryStr);
            query.executeUpdate();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public List<RequestNotificationEvent> findEventsByParameters(RequestNotificationEvent event) {
        String queryStr = "" +
                " SELECT rnl.* FROM RequestNotificationEvent rnl " +
                "  WHERE rnl.requestId = '" + event.getRequestId() + "' " +
                "    AND rnl.role = '" + event.getRole() + "' " +
                "    AND rnl.action = '" + event.getAction().name() + "' ";
        Query query = getEntityManager().createNativeQuery(queryStr, RequestNotificationEvent.class);
        return query.getResultList();
    }

    @Override
    public void merge(RequestNotificationEvent event) {
        try {
            String queryStr = "" +
                    " SELECT rnl.* FROM RequestNotificationEvent rnl " +
                    "  WHERE rnl.requestId = '" + event.getRequestId() + "' " +
                    "    AND rnl.role = '" + event.getRole() + "' " +
                    "    AND rnl.action = '" + event.getAction().name() + "' ";
            Query query = getEntityManager().createNativeQuery(queryStr, RequestNotificationEvent.class);
            query.getSingleResult();
        } catch (NoResultException nre) {
            create(event);
        }
    }
}
