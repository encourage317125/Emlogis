package com.emlogis.server.services;

import java.io.File;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.services.schedule.ResponseHandlerService;
import com.emlogis.common.session.HazelcastExceptionHandling;
import com.emlogis.scheduler.engine.communication.HzConstants;
import com.emlogis.scheduler.engine.communication.response.EngineResponse;
import com.emlogis.scheduler.engine.communication.response.EngineResponseType;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
@DependsOn("HazelcastServiceBean")
@HazelcastExceptionHandling
public class SchedulingResponseServiceBean implements SchedulingResponseService {

    private final static Logger logger = Logger.getLogger(SchedulingResponseServiceBean.class);

    @EJB
    private ResponseHandlerService responseHandlerService;

    @EJB
    private ASHazelcastService hazelcastService;

    private boolean responseLoop = true;

    private Thread commonQueueThread;

    private Thread appServerQueueThread;
    
    private boolean logRequests = false;			// TODO MOHAMED   initilaize value in init().
    private String logRequestsDirectory = null;	// TODO MOHAMED   initilaize value in init().

    public SchedulingResponseServiceBean() {
	}

	@PostConstruct
    public void init() {

        HazelcastInstance hzInstance = hazelcastService.getInstance();

        final IQueue<String> commonResponseQueue = hzInstance.getQueue(HzConstants.RESPONSE_QUEUE_PREFIX
                + HzConstants.COMMON_SUFFIX);

        final IQueue<String> appServerResponseQueue = hzInstance.getQueue(HzConstants.RESPONSE_QUEUE_PREFIX
                + hazelcastService.getAppServerName());

    	final IMap<String, String> responseDataMap = hzInstance.getMap(HzConstants.RESPONSE_DATA_MAP);

        if (commonQueueThread != null) {
            commonQueueThread.interrupt();
        }
        if (appServerQueueThread != null) {
            appServerQueueThread.interrupt();
        }
      //Remove Engine Request Dumped.
		String dump_request = System.getProperty(ENGINE_DUMP_REQUEST);
		if (StringUtils.equals(dump_request, "true")) {
			logRequests = true;
			logRequestsDirectory = System.getProperty(ENGINE_DUMP_REQUEST_PATH);
			logger.info("ENGINE_DUMP_REQUEST_PATH = " + logRequestsDirectory);
			if (StringUtils.isEmpty(logRequestsDirectory)) {
				logRequestsDirectory = "/tmp/emlogis/DumpEngineRequest";
			}
		}

        commonQueueThread = createResponseQueueListener(commonResponseQueue, responseDataMap);
        appServerQueueThread = createResponseQueueListener(appServerResponseQueue, responseDataMap);
    }

    private Thread createResponseQueueListener(final IQueue<String> responseQueue,
                                               final IMap<String, String> responseDataMap) {
        Thread result = new Thread(new Runnable() {

            @Override
            public void run() {
                while (responseLoop) {
                    try {
                        String engineResponseJson = responseQueue.take();

                        EngineResponse engineResponse =
                                EmlogisUtils.fromJsonString(engineResponseJson, EngineResponse.class);
                        
                        if (logRequests) {
	                        // TODO MOHAMED   (NOTE DELETE MUST NOT FAIL)
	                        // conditionally delete  file named with engineResponse.getRequestId() if( engineResponse.getEngineResponseType() != EngineResponseType.Acknowledge)
                    		//Remove Engine Request Dumped.
                    		logger.info("EngineRequest dump :"+engineResponse.getEngineResponseType());
                    		if (engineResponse.getEngineResponseType() == null || !EngineResponseType.Acknowledge.equals(engineResponse.getEngineResponseType())) {
                    			logger.info("EngineRequest dump to be removed: RequestId = " + engineResponse.getRequestId() 
                    					+ ", OriginatingAppServerId = " + engineResponse.getOriginatingAppServerId()
                    					+", OriginatingAppServerId = " + engineResponse.getOriginatingAppServerId()
                    					+",requestType = " + engineResponse.getRequestType());
                    			removeDumpEngineRequest(engineResponse.getRequestId());
                    		}
                    		
                        }
                        
                        responseHandlerService.handleResponse(engineResponse, responseDataMap);
                    } catch (Throwable t) {
                        logger.error("Error processing engine response", t);
                        if ( logRequests) {

	                        // TODO MOHAMED   (NOTE DELETE MUST NOT FAIL)
	                        // try to see if we can get the requestId and delete the file if not already deleted, if( engineResponse.getEngineResponseType() != EngineResponseType.Acknowledge)
                        }
                    }
                }
            }
        });

        result.start();

        return result;
    }
    
    
	static private String ENGINE_DUMP_REQUEST= "engine.dump.request";
	static private String ENGINE_DUMP_REQUEST_PATH= "engine.dump.request.path";
	
	private void removeDumpEngineRequest(String requestid) {
		try {
			File out = new File(logRequestsDirectory + File.separator + requestid);
			if (out.exists()) {
				out.delete();
				logger.info("EngineRequest Dump REMOVED : " + requestid);
			} else {
				logger.info("EngineRequest Dump NOT FOUND for : " + requestid);
			}
		} catch (Exception e) {
			logger.error("Error removing DumpEngineRequest", e);
		}
	}

}
