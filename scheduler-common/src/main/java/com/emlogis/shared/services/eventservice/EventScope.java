package com.emlogis.shared.services.eventservice;

public enum EventScope {
	
	Local,			// events are published on current JVM only 
	Engine,			// events are broadcasted through hazelcast, to Engines only
	AppServer,		// events are broadcasted through hazelcast, but ignored by Engines
	Global;			// events are broadcasted through hazelcast to all components connected to hazelcast
	
}
