package com.emlogis.schedule.engine;

import java.util.concurrent.TimeUnit;

import com.emlogis.common.EmlogisUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emlogis.scheduler.engine.communication.EngineStatus;
import com.emlogis.scheduler.engine.communication.HzConstants;
import com.emlogis.shared.services.hazelcastservice.HazelcastServiceImpl;

public class EngineHazelcastService extends HazelcastServiceImpl {
	
	private final Logger logger = LoggerFactory.getLogger(EngineHazelcastService.class);

    @Override
    public void init() {
        String engineName = System.getProperty(HzConstants.ENGINE_ID_PARAMNAME);
        if (StringUtils.isBlank(engineName)) {
            logger.debug("Engine.HazelcastService initialization: Unable to find name for this Service, please set: '"
            		+ HzConstants.ENGINE_ID_PARAMNAME + "' system property.");        
        }
    	this.init(engineName, false);
    }

	protected void registerComponentInMap(String ip) {
        EngineStatus engineStatus = new EngineStatus();
        engineStatus.setIp(ip);
        engineStatus.setName(getComponentName());
        engineStatus.setUpdated(System.currentTimeMillis());

        String engineStatusJson = EmlogisUtils.toJsonString(engineStatus);
        
        getComponentMap().put(getComponentName(), engineStatusJson, 1, TimeUnit.MINUTES);
    }

}