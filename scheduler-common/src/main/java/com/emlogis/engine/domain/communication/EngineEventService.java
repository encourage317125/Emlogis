package com.emlogis.engine.domain.communication;

import com.emlogis.scheduler.engine.communication.ComponentRole;
import com.emlogis.shared.services.eventservice.EventServiceImpl;
import com.emlogis.shared.services.hazelcastservice.HazelcastService;

public class EngineEventService extends EventServiceImpl{

	public EngineEventService() {
		super();
	}
	
	public void init(HazelcastService hzService, ComponentRole role) {
		super.init(hzService, role);
	}

}
