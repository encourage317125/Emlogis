package com.emlogis.server.services;

import com.emlogis.model.notification.Notification;
import com.emlogis.model.notification.SendNotification;
import com.emlogis.rest.auditing.AuditRecord;

import org.elasticsearch.client.Client;

public interface ESClientService {
	
	Client getESClient();		// elastic search client

	void indexAuditRecord(String tenantId, AuditRecord record);

	void indexNotification(Notification notification, String category);

}