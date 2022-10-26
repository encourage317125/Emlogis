package com.emlogis.server.services.eventservice;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.dto.Dto;
import com.emlogis.model.schedule.QualificationRequestTracker;
import com.emlogis.model.schedule.dto.QualificationRequestTrackerDto;
import com.emlogis.model.tenant.Tenant;
import com.emlogis.rest.resources.util.DtoMapper;
import com.emlogis.scheduler.engine.communication.ComponentRole;
import com.emlogis.shared.services.eventservice.EventKeyBuilder;
import com.emlogis.shared.services.eventservice.EventScope;
import com.emlogis.shared.services.eventservice.EventService;
import com.emlogis.shared.services.eventservice.EventServiceImpl;
import com.emlogis.shared.services.hazelcastservice.HazelcastService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.event.Event;
import reactor.event.registry.Registration;
import reactor.event.selector.Selector;
import reactor.function.Consumer;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Startup
@Singleton
@DependsOn("HazelcastServiceBean")
@AccessTimeout(10000)	// 10 secs delay: allow more than the default 5sec for client EJBs to connect to this Service 
						// as connection to Hazelcast on startup can take few secs and potentially more than 5secs
public class EventServiceBean extends EventServiceImpl implements ASEventService{
	
	private final Logger logger = LoggerFactory.getLogger(EventServiceBean.class);
		
	@Inject
	private HazelcastService hzService;
	
	// Map of sessions which have registered for events.  key is tokenId, value is SSEClient (HttpServletResponse + AsyncContext)
	Map<String, SSEClient> sseClientRegistry = new HashMap();
	

    @PostConstruct
    void init() {
    	init(hzService);
    }
    
	public void init(HazelcastService hzService) {
		logger.debug("EventServiceBean.init()...");

		super.init(hzService, ComponentRole.AppServer);
		lauchSSEClientsCheck();
		logger.debug("EventServiceBean.init() done.");
	}

    public void registerSSEClient(SSEClient client){
    	sseClientRegistry.put(client.getTokenId(), client);
    }

    public SSEClient getSSEClient(String tokenId){
    	return sseClientRegistry.get(tokenId);
    }
    
    public void touchSSEClient(String tokenId){
    	if (StringUtils.isBlank(tokenId)) return;
    	SSEClient sseClient = sseClientRegistry.get(tokenId);
    	if (sseClient != null) {
    		sseClient.touch();
    	}
    }

    public void unregisterSSEClient(String tokenId, String reason){
    	logger.debug("releasing resources associated with SSEClient: "  + tokenId + " because of: " + reason);

    	SSEClient sseClient = sseClientRegistry.get(tokenId);
    	if (sseClient != null) {
            sseClientRegistry.remove(tokenId);
            try{
            	sseClient.getAc().complete();	// complete async
            }
            finally {
	            // unregister reactor consumers
	        	for (SSEConsumer sseConsumer :  sseClient.getConsumers()) {
	        		Registration reg = sseConsumer.getRegistration();
	        		if (reg !=null) {
	        			reg.cancel();
	        		}
	        	}        
            }
    	}
    }
    
    public <E extends Event<?>> Registration<Consumer<E>> on(Selector selector, final Consumer<E> consumer) {
    	Registration reg = super.on(selector, consumer);
    	if (consumer instanceof SSEConsumer) {
    		SSEConsumer sseConsumer = (SSEConsumer)consumer;
    		sseConsumer.setRegistration(reg); 	
    		sseConsumer.setEventService(this);
    	}
		return reg;
	}


    private void lauchSSEClientsCheck() {
    	new Thread(
    			new Runnable() {
    				private boolean abort = false;
    				@Override
    				public void run() {

    					int cnt = 0;
    					while (!abort) {
    						if (sseClientRegistry.size() > 0) {
    							logger.info("Checking active SSE clients: "  + sseClientRegistry.size());
    							logger.debug("Checking active SSE clients: "  + sseClientRegistry.size() + " At " + new Date().toLocaleString());
    						}
    						long now = System.currentTimeMillis();
	    					for (SSEClient sseClient : sseClientRegistry.values()) {
	    						// 1) check time to live
	    						if ((now - sseClient.getUpdated())  > 300 * 1000L) {
	    							// time to live expired, close SSEClient
									unregisterSSEClient(sseClient.getTokenId(), "time to live expired");
									break; // restart loop as we have modified the sseClientRegistry
	    						}

	    						// 2) check heartbit message  is successfully sent
	    						try {
	    							Object key = new EventKeyBuilder().setTopic(EventService.TOPIC_SYSTEM).setEventType("Heartbit").setEntityId("EventService").build();
	    							Map<String, Object> headers = new HashMap();
	    							headers.put("timestamp", now);
	    							headers.put("x-reactor-origin", getComponentName());
	    							headers.put("tokenId", sseClient.getTokenId());
	    							SSEEvent sseEvent = new SSEEvent(String.valueOf(cnt), key.toString(), headers, "Heartbit #" + (cnt));
	    							String s = toJsonString(sseEvent);
	    							sseClient.sendSSEEvent(s);
								} catch (IOException e) {
									e.printStackTrace();
									// disconnect SSEClient and remove consumers asscoiated to it.
									unregisterSSEClient(sseClient.getTokenId(), "failed to send heartbit to client");
									break; // restart loop as we have modified the sseClientRegistry
								}
	    						catch (Throwable t) {
									t.printStackTrace();
									// disconnect SSEClient and remove consumers asscoiated to it.
									unregisterSSEClient(sseClient.getTokenId(), "failed to send heartbit to client");
									break; // restart loop as we have modified the sseClientRegistry
								}

	    					}
	    					cnt++;

	    					try {
	    						Thread.sleep(60 * 1000L);				// sleep 5mins (30 secs for testing)
	    					} catch (InterruptedException e) {
	    						throw new RuntimeException(e);
	    					}

    					}
    				}
    			}
    			).start();
    }
    
    
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
    public void sendEntityCreateEvent(Object entity, Class<? extends Dto> overrideDtoClass)  {
    	sendLifecycleEvent("Create", entity, overrideDtoClass, null);
    }

    public void sendEntityCreateEvent(Object entity, Class<? extends Dto> overrideDtoClass, Dto dto)  {
    	sendLifecycleEvent("Create", entity, overrideDtoClass, dto);
    }

    public void sendEntityUpdateEvent(Object entity, Class<? extends Dto> overrideDtoClass) {
    	sendLifecycleEvent("Update", entity, overrideDtoClass, null);
    }

    public void sendEntityUpdateEvent(Object entity, Class<? extends Dto> overrideDtoClass, Dto dto) {
    	sendLifecycleEvent("Update", entity, overrideDtoClass, dto);
    }

    public void sendEntityDeleteEvent(Object entity, Class<? extends Dto> overrideDtoClass)  {
    	sendLifecycleEvent("Delete", entity, overrideDtoClass, null);
    }

    public void sendEntityDeleteEvent(Object entity, Class<? extends Dto> overrideDtoClass, Dto dto)  {
    	sendLifecycleEvent("Delete", entity, overrideDtoClass, dto);
    }

    private void sendLifecycleEvent(String eventType, Object entity, Class<? extends Dto> overrideDtoClass, Dto dto) {
    	String tenantId;
    	String entityId;
    	Class<? extends Dto> entityDtoClass;
    	if (entity instanceof Tenant) {
    		Tenant tenant = (Tenant)entity;
    		tenantId = tenant.getTenantId();
    		entityDtoClass = (overrideDtoClass != null ? overrideDtoClass : tenant.getReadDtoClass());
    		entityId = tenantId;
    	} else if (entity instanceof BaseEntity) {
    		BaseEntity baseEntity = (BaseEntity) entity;
    		tenantId = baseEntity.getTenantId();
    		entityDtoClass = (overrideDtoClass != null ? overrideDtoClass : baseEntity.getReadDtoClass());
    		entityId = baseEntity.getId();
    	} else if (entity instanceof QualificationRequestTracker) {
    		QualificationRequestTracker requestTracker = (QualificationRequestTracker) entity;
    		tenantId = requestTracker.getTenantId();
    		entityDtoClass = (overrideDtoClass != null ? overrideDtoClass : QualificationRequestTrackerDto.class);
    		entityId = requestTracker.getRequestId();
    	} else {
    		throw new RuntimeException("sendLifecycleEvent() cannot be used for class: " + entity.getClass().getName());
    	}
    	if (entityDtoClass == null) {
    		throw new RuntimeException("sendLifecycleEvent() cannot determine the Dto class to use.");
    	}

		Object key = new EventKeyBuilder()
			.setTopic(EventService.TOPIC_OBJECTLIFECYCLE)
			.setEventType(eventType)
            .setTenantId(tenantId).setEntityClass(entity.getClass().getSimpleName())
            .setEntityId(entityId).build();
		try {
            if (dto == null) {
                dto = toDto(entity, entityDtoClass);
            }
			sendEvent(EventScope.AppServer, key, Event.wrap(dto), "SSEServlet");
		} catch (InterruptedException | InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException e) {
            logger.error("Unable to send lifecycle event.", e);
		}
    }

    /**
     * map() maps a source pojo into a target pojo, by copying fields defined into target DataTransferObjects, from source pojo
     * @param src
     * @param tgtClass
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private <E, T> T toDto(E src, Class<T> tgtClass) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        return new DtoMapper<E, T>().map(src, tgtClass);
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


