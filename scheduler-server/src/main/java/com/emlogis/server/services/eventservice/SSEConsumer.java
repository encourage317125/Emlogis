package com.emlogis.server.services.eventservice;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.event.Event;
import reactor.event.registry.Registration;
import reactor.event.support.EventConsumer;
import reactor.function.Consumer;

import java.io.IOException;


/**
 * Simple {@link EventConsumer} implementation that associate an EventConsumer to a SSEClient
 *
 * @param <T> the type of the event that can be handled by the consumer and the type that
 *            can be handled by the delegate
 * @param sseClient the sseclient to associate this EventConsumer to            
 *
 */
public class SSEConsumer<T> implements Consumer<Event<T>>  {
	
	private final Logger logger = LoggerFactory.getLogger(SSEConsumer.class);

	private SSEClient 	 				sseClient;		// SSE client 'associated' to this consumer. 
	private Registration<Consumer<?>> 	registration; 	// reactor registration
	private ASEventService				eventService;	

	/**
	 * Creates a new {@code EventConsumer} that will pass event data to the given {@code
	 * delegate} and associate the consumer to the SSE client
	 *
	 * @param delegate The delegate consumer
	 */
	public SSEConsumer(SSEClient sseClient) {
		super();
		this.sseClient = sseClient;
		sseClient.addConsumer(this);
	}

	public SSEClient getSseClient() {
		return sseClient;
	}

	public void setSseClient(SSEClient sseClient) {
		this.sseClient = sseClient;
	}

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
		logger.debug("SSEConsumer Received event with data: " + ev.getKey().toString() + "=" + ev.getData());

		try {
			SSEEvent sseEvent = new SSEEvent(ev.getId().toString(), ev.getKey().toString(), ev.getHeaders().asMap(), ev.getData());
			String s = toJsonString(sseEvent);
			logger.debug("S: " + s);
			sseClient.sendSSEEvent(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.debug("SSEConsumer failed to push event to client, unregistering client: " + sseClient.getTokenId());
			eventService.unregisterSSEClient(sseClient.getTokenId(), "failure when forwarding event, client may be disconected");
		}
		catch (Throwable t) {
			t.printStackTrace();
			// disconnect SSEClient and remove consumers asscoiated to it.
			logger.debug("SSEConsumer failed to push event to client, unregistering client: " + sseClient.getTokenId());
			eventService.unregisterSSEClient(sseClient.getTokenId(), "failure when forwarding event, client may be disconected");
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