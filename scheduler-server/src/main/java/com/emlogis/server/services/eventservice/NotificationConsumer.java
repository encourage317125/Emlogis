package com.emlogis.server.services.eventservice;

import com.emlogis.model.notification.ReceiveNotification;
import com.emlogis.model.notification.dto.NotificationMessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.event.Event;
import reactor.event.registry.Registration;
import reactor.function.Consumer;

import java.io.IOException;

public class NotificationConsumer<T> implements Consumer<Event<T>>  {
	
	private final Logger logger = LoggerFactory.getLogger(SSEConsumer.class);
	
	private NotificationClient client;

	public NotificationConsumer(NotificationClient client) {
		this.client = client;
	}

	private Registration<Consumer<?>> 	registration; 	// reactor registration
	private ASEventService				eventService;
	
	public Registration<Consumer<?>> getRegistration() {
		return registration;
	}

	public void setRegistration(Registration<Consumer<?>> registration) {
		this.registration = registration;
	}

	public ASEventService getEventService() {
		return eventService;
	}

	public void setEventService(ASEventService eventService) {
		this.eventService = eventService;
	}
	
	@Override
	public void accept(Event<T> ev) {
		logger.debug("NotificationConsumer event with data: " + ev.getKey().toString() + "=" + ev.getData());
		
		NotificationMessageDTO notificationMessageDTO = (NotificationMessageDTO) ev.getData();
		
		if(notificationMessageDTO != null) {
			client.notify(notificationMessageDTO);
		}
		
	}
	
	private String toJsonString(Object o)  {
		if (o == null) { 
			return "{}"; 
		}
		ObjectMapper objMapper = new ObjectMapper();
		try {
			String s = objMapper.writeValueAsString(o);
			return s;
	    } catch (IOException e) {
	        e.printStackTrace();
	        throw new RuntimeException(e);
	    } 		
	}

}
