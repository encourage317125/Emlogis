package com.emlogis.shared.services.eventservice;

import com.emlogis.scheduler.engine.communication.ComponentRole;
import com.emlogis.shared.services.hazelcastservice.HazelcastService;

import reactor.event.Event;
import reactor.event.registry.Registration;
import reactor.event.selector.Selector;
import reactor.function.Consumer;


public interface EventService {

	public final String	TOPIC_SYSTEM				= "System";
	public final String	TOPIC_AOM					= "AOM";
	public final String	TOPIC_OBJECTLIFECYCLE		= "ObjLifecycle";
	public final String	TOPIC_SYSTEM_NOTIFICATION	= "SysNotifications";	// system level / internal notifications
	public final String	TOPIC_NOTIFICATION			= "Notifications";		// employee level notifications

	public void init(HazelcastService hzService, ComponentRole role);
	
	public String getComponentName();
	
	// event consumer registration method
    public <E extends Event<?>> Registration<Consumer<E>> on(Selector selector, final Consumer<E> consumer);
	
    // event notify method
    public void sendEvent(EventScope evtScope, Object key, Event<?> event, String service) throws InterruptedException;

}
