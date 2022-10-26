package com.emlogis.shared.services.hazelcastservice;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emlogis.scheduler.engine.communication.HzConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public abstract class HazelcastServiceImpl implements HazelcastService {
	
	private final Logger logger = LoggerFactory.getLogger(HazelcastServiceImpl.class);

    private String componentName;

    private HazelcastInstance hazelcastInstance;

    private IMap<String, String> componentMap;

    public IMap<String, String> getComponentMap() {
        return componentMap;
    }

    @Override
    public String getComponentName() {
        return componentName;
    }

    /* (non-Javadoc)
	 * @see com.emlogis.server.services.HazelcastService#getInstance()
	 */
    @Override
	public HazelcastInstance getInstance() {
    	if (hazelcastInstance == null) {
    		init();
    	}
    	return hazelcastInstance;
    }

    protected void init(String componentName, boolean isAppServer)  {
        if (StringUtils.isBlank(componentName)) {
        	// use guid if no appservername found. 
        	componentName = UUID.randomUUID().toString();

        	logger.error("Unable to find name for this Hazelcast Service, using guid: " + componentName);
        }
    	this.componentName = componentName;

    	// connect to Hazelcast
        hazelcastInstance = HazelcastInstanceBuilder.newHazelcastClient();

        //get map of component status
        componentMap = hazelcastInstance.getMap(isAppServer ? HzConstants.APP_SERVER_MAP : HzConstants.ENGINE_MAP);

        String ip = "unknown";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			// don't fail although we couldn't get ip address
		}
		final String ipAddress = ip;

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    registerComponentInMap(ipAddress);
                } catch (Exception e) {
                    cancel();
                }
            }
        }, 0, 30 * 1000);
        
        // immediately notify this AppServer or Engine joined the cluster
        registerComponentInMap(ip);
    }

    protected abstract void registerComponentInMap(String ipAddress);

}
