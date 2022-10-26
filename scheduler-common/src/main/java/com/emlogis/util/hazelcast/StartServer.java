package com.emlogis.util.hazelcast;

import com.hazelcast.core.Hazelcast;

public class StartServer {

	public static void main(String[] args) {
		 Hazelcast.newHazelcastInstance();
	}

}
