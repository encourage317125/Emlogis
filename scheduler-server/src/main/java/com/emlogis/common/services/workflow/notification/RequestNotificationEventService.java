package com.emlogis.common.services.workflow.notification;

import com.emlogis.common.services.common.GeneralJPARepository;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.entities.RequestNotificationEvent;

import java.util.List;

/**
 * Created by user on 14.08.15.
 */
public interface RequestNotificationEventService extends GeneralJPARepository<RequestNotificationEvent, PrimaryKey> {

    List<RequestNotificationEvent> getNotificationsToSend();

    void deleteSelected(String idsCommaSeparated);

    void merge(RequestNotificationEvent requestNotificationEvent);

    void updateProcessed(List<String> notificationIds);

    List<RequestNotificationEvent> findEventsByParameters(RequestNotificationEvent event);
}
