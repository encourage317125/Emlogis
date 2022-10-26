package com.emlogis.scheduler.engine.communication;

import java.io.Serializable;

/**
 * @author EmLogis
 * 
 * This class is used by the SchedulingEngines to make the System aware of their presence.
 * Each SchedulingEngine periodically (every 30secs) writes an entry into the 'Engine' Map of the Hazelcast cluster.
 * the entry has a 1min time to leave, so updates must happen within this 60sec interval.  
 *
 */
public class EngineStatus extends ComponentStatus {

	private String type = "generic";	// type of  engine
	
    public EngineStatus() {
		super();
	}

	public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
