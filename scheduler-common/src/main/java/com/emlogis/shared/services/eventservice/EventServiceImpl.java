package com.emlogis.shared.services.eventservice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import com.emlogis.common.EmlogisUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import reactor.event.registry.Registration;
import reactor.event.selector.Selector;
import reactor.function.Consumer;

import com.emlogis.scheduler.engine.communication.ComponentRole;
import com.emlogis.scheduler.engine.communication.HzConstants;
import com.emlogis.shared.services.hazelcastservice.HazelcastService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

public class EventServiceImpl implements EventService{
	
	private final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);
		
	// topics for broadcasting events through hazelcast
	private	ITopic<byte[]> globalEventTopic;
	private	ITopic<byte[]> appSrvEventTopic;
	private	ITopic<byte[]> engineEventTopic;
	
	private Reactor reactor;
	
	private HazelcastService hzService;
		

    public EventServiceImpl() {
		super();
	}

	@Override
	public String getComponentName() {
		return hzService.getComponentName();
	}

    public void init(HazelcastService hzService, ComponentRole role) {
    	// initialize distributed queue / topic
    	this.hzService = hzService;
    	
    	// initialize reactor
    	getReactor();
    	    	
    	// get hazelcast topics to publish/receive events
    	HazelcastInstance hzInstance = hzService.getInstance();
    	globalEventTopic = hzInstance.getTopic(HzConstants.GLOBAL_EVENT_TOPIC);
    	appSrvEventTopic = hzInstance.getTopic(HzConstants.AS_EVENT_TOPIC);
    	engineEventTopic = hzInstance.getTopic(HzConstants.ENGINE_EVENT_TOPIC);
    	
    	// install a topic listener to get events from topic
    	MessageListener<byte[]> listener = new MessageListener<byte[]>() {
			
			@Override
			public void onMessage(Message<byte[]> message) {
//				logger.debug("Received: "+ m.getMessageObject());
				byte[] bytes = message.getMessageObject();
				KeyedEvent keyedEvent = (KeyedEvent) EmlogisUtils.deserializeObject(bytes);
//                logger.debug("Got event !!, notifying locally...");
//                logger.debug("Got Event = " + ke.toString());

                try {
                	Object key = EmlogisUtils.deserializeObject(keyedEvent.getKey());
//	                logger.debug("Key deserialized=" + key.toString());
	                Event<?> event = deserializeEvent(keyedEvent.getEvent());
	                logger.debug("Got Event. deserialized=" + key.toString() + " || " + event.toString());

					sendEvent(EventScope.Local, key,event, (String) event.getHeaders().get("service"));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        };	

        // install message listneres basd on component role
    	switch (role) {
    	case AppServer:
        	globalEventTopic.addMessageListener(listener);		// listen to global events 
        	appSrvEventTopic.addMessageListener(listener);		// listen to AppServer only events
        	engineEventTopic.addMessageListener(listener);		// listen to Engine only events
    		break;
    	case Engine:
        	globalEventTopic.addMessageListener(listener);		// listen to global events 
        	engineEventTopic.addMessageListener(listener);		// listen to Engine only events
    		break;
    	}

    	 
    	logger.debug("EventService Subscribed to topics");
    }
        
    
	public Reactor getReactor() {
		if (null == reactor) {
			Environment env = new Environment();
			// Use a Reactor to dispatch events using the default Dispatcher
			reactor = Reactors.reactor()
				.env(env)
				.dispatcher(Environment.RING_BUFFER)
				.get();
		}
		return reactor;
	}

    public <E extends Event<?>> Registration<Consumer<E>> on(Selector selector, final Consumer<E> consumer) {
    	return reactor.on(selector, consumer);
	}
    
    public void sendEvent(EventScope evtScope, Object key, Event<?> event, String service) throws InterruptedException {
        event.getId();	// generate an event guid
        // add some metadata in header (timestamp, AppServer or Engine Id, service name (optional)
		Event.Headers headers = event.getHeaders();
		headers.set("timestamp", String.valueOf(System.currentTimeMillis()));
		headers.setOrigin(hzService.getComponentName()); 
		if (StringUtils.isNotBlank(service)) {
			headers.set("service", service);
		}
        
    	if (evtScope != EventScope.Local ) {
    		// event must be sent out to all members in cluster
    		
    		byte[] serializedKey = EmlogisUtils.serializeObject(key);
    		byte[] serializedEvent = serializeEvent(event);
			KeyedEvent keyedEvent = new KeyedEvent(serializedKey, serializedEvent);
			ITopic<byte[]> topic = null;
			switch (evtScope) {
			case Global:
				topic = globalEventTopic;
				break;
			case AppServer:
				topic = appSrvEventTopic;
				break;
			case Engine:
				topic = engineEventTopic;
				break;
			}
			if (topic != null) {
				topic.publish(EmlogisUtils.serializeObject(keyedEvent));
			}	
//            logger.debug("Event Queued.");            
    	} else {
    		// event must be distributed locally
    		reactor.notify(key, event);
//            logger.debug("Event published locally.");
    	}
    }

    // because of some dependencies, events cannot be serialized as json directly. need to use an intermediate class as
    // in SSEConsumer. in the meantime, use java serialization ...
    
	private byte[] serializeEvent(Event<?> event) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);   
			out.writeObject(event);
			byte[] evtBytes = bos.toByteArray();
			return evtBytes;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException ex) {
				// ignore close exception
			}
			try {
				bos.close();
			} catch (IOException ex) {
				// ignore close exception
			}
		}
	}	
	
	private Event<?> deserializeEvent(byte[] serEvent) {
		ByteArrayInputStream bis = new ByteArrayInputStream(serEvent);
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(bis);
			Event<?> o = (Event<?>)in.readObject(); 
			return o;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} finally {
		    try {
				bis.close();
			} catch (IOException ex) {
				// ignore close exception
			}
			try {
				if (in != null) {
				  in.close();
				}
		  } catch (IOException ex) {
		    // ignore close exception
		  }
		}
	}

}
  

	
