package com.emlogis.shared.services.hazelcastservice;

import com.hazelcast.core.HazelcastInstance;

public interface HazelcastService {
	
	public void init();

	public HazelcastInstance getInstance();

	public String getComponentName();

}