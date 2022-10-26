package com.emlogis.shared.services.hazelcastservice;

import java.util.Arrays;
import java.util.List;

import com.emlogis.scheduler.engine.communication.HzConstants;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HazelcastInstanceBuilder {

	private static final Logger logger = LoggerFactory.getLogger(HazelcastInstanceBuilder.class); 
    public static HazelcastInstance newHazelcastClient() {
        ClientConfig clientConfig = new ClientConfig();
        
        String address = System.getProperty(HzConstants.HAZELCAST_ADDRESS);
        logger.info(HzConstants.HAZELCAST_ADDRESS + ":" + address);
        if (StringUtils.isNotEmpty(address)) {
        	List<String> addresses = Arrays.asList(address.replaceAll("\\s", "").split(","));
        	
            ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
            //clientNetworkConfig.addAddress(address);
            clientNetworkConfig.setAddresses(addresses);
            clientConfig.setNetworkConfig(clientNetworkConfig);
        }
        
        String connectionTimeout = System.getProperty(HzConstants.HAZELCAST_TIMEOUT);
        logger.info(HzConstants.HAZELCAST_TIMEOUT + ":" + connectionTimeout);
        if (StringUtils.isNotEmpty(connectionTimeout)) {
            clientConfig.getNetworkConfig().setConnectionTimeout(Integer.parseInt(connectionTimeout));
        }
        
        String attemptlimit = System.getProperty(HzConstants.HAZELCAST_ATTEMP_LIMIT);
        logger.info(HzConstants.HAZELCAST_ATTEMP_LIMIT + ":" + attemptlimit);
        if (StringUtils.isNotEmpty(attemptlimit)) {
            clientConfig.getNetworkConfig().setConnectionAttemptLimit(Integer.parseInt(attemptlimit));
        }
        
        String attemptperiod = System.getProperty(HzConstants.HAZELCAST_ATTEMP_PERIOD);
        logger.info(HzConstants.HAZELCAST_ATTEMP_PERIOD + ":" + attemptperiod);
        if (StringUtils.isNotEmpty(attemptperiod)) {
            clientConfig.getNetworkConfig().setConnectionAttemptPeriod(Integer.parseInt(attemptperiod));
        }

        String redooperation = System.getProperty(HzConstants.HAZELCAST_REDO_OPERATION);
        logger.info(HzConstants.HAZELCAST_REDO_OPERATION + ":" + redooperation);
        if (StringUtils.isNotEmpty(redooperation)) {
            clientConfig.getNetworkConfig().setRedoOperation(Boolean.parseBoolean(redooperation));
        }
        
        return HazelcastClient.newHazelcastClient(clientConfig);
    }
}
