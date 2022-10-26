package com.emlogis.server.services;

import com.emlogis.shared.services.hazelcastservice.HazelcastService;
import com.hazelcast.core.HazelcastInstance;

public interface ASHazelcastService extends HazelcastService {

	public HazelcastInstance getInstance();

	public String getAppServerName();
	
}