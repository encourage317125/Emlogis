package com.emlogis.server.services.eventservice;

import com.emlogis.model.dto.Dto;
import com.emlogis.shared.services.eventservice.EventService;

import java.lang.reflect.InvocationTargetException;

public interface ASEventService  extends EventService {

	public void registerSSEClient(SSEClient sseClient);
	
    public SSEClient getSSEClient(String tokenId);

    public void unregisterSSEClient(String tokenId, String reason);
    
    public void touchSSEClient(String tokenId);
    
    /**
     * Entity Lifecycle event generation convenience methods:  (create/update/delete)
     *
     * As this method is generally invoked in the context of a transaction, it SHOULD NOT throw any exception
     * for event transport related issues
     *
     * @param entity
     * @param overrideDtoClass
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public void sendEntityCreateEvent(Object entity, Class<? extends Dto> overrideDtoClass);

    public void sendEntityCreateEvent(Object entity, Class<? extends Dto> overrideDtoClass, Dto dto);

    public void sendEntityUpdateEvent(Object entity, Class<? extends Dto> overrideDtoClass);

    public void sendEntityUpdateEvent(Object entity, Class<? extends Dto> overrideDtoClass, Dto dto);

    public void sendEntityDeleteEvent(Object entity, Class<? extends Dto> overrideDtoClass);

    public void sendEntityDeleteEvent(Object entity, Class<? extends Dto> overrideDtoClass, Dto dto);

}
