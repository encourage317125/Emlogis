package com.emlogis.engine.domain.communication;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.event.Event;

import com.emlogis.shared.services.eventservice.EventKeyBuilder;
import com.emlogis.shared.services.eventservice.EventScope;
import com.emlogis.shared.services.eventservice.EventService;

public class NotificationService {
	
	private final Logger logger = LoggerFactory.getLogger(NotificationService.class);
	
	private EventService eventService;
	
	private	String	tenantId;
	private String	accountId;
	private	String	requestId;
	private	String	scheduleId;
	
	private	long	lastEventDate;		// datetime last event has been sent, in seconds
	
	
	public NotificationService(EventService eventService) {
		super();
		this.eventService = eventService;
	}

	public EventService getEventService() {
		return eventService;
	}

	public void setEventService(EngineEventService eventService) {
		this.eventService = eventService;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public void notifyProgress(int progressPercentage, int hardScore, int softScore, String progressInfo) {
		
		// throttle sending of progress events (1 event / sec), to the exception of start (0%) and end (100%) events
		if (progressPercentage != 0 && progressPercentage != 100) {
			long now = System.currentTimeMillis() / 1000; // now in secs.
			if (lastEventDate == now) {
				return;		// swallow event if within same second
			}
			lastEventDate = now;
		}
		
        Object key = new EventKeyBuilder().setTopic(EventService.TOPIC_SYSTEM_NOTIFICATION).setTenantId(tenantId)
                .setEntityClass("Schedule").setEventType("Progress").setEntityId(scheduleId).build();
        logger.info("Sending progress notification...");
        Map<String,Object> eventBody = new HashMap<>();
        eventBody.put("progress", progressPercentage);
        eventBody.put("hardScore", hardScore);
        eventBody.put("softScore", softScore);
        eventBody.put("msg", progressInfo);
        try {
			eventService.sendEvent(EventScope.AppServer, key, Event.wrap(eventBody),"SchedulingService");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public void notifyProgress(double progressPercentage, String progressInfo) {
		
		// throttle sending of progress events (1 event / sec), to the exception of start (0%) and end (100%) events
		if (progressPercentage != 0.0 && progressPercentage != 100.0) {
			long now = System.currentTimeMillis() / 1000; // now in secs.
			if (lastEventDate == now) {
				return;		// swallow event if within same second
			}
			lastEventDate = now;
		}

        Object key = new EventKeyBuilder().setTopic(EventService.TOPIC_SYSTEM_NOTIFICATION).setTenantId(tenantId)
                .setEntityClass("Schedule").setEventType("Progress").setEntityId(scheduleId).build();
        logger.info("Sending progress notification...");
        Map<String,Object> eventBody = new HashMap<>();
        eventBody.put("progress", progressPercentage);
        eventBody.put("msg", progressInfo);
        try {
			eventService.sendEvent(EventScope.AppServer, key, Event.wrap(eventBody),"SchedulingService");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
}
