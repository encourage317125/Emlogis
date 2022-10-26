package com.emlogis.schedule.engine.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.emlogis.scheduler.engine.communication.response.EngineResponseType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.engine.domain.EmployeeSchedule;
import com.emlogis.engine.domain.communication.AssignmentResultDto;
import com.emlogis.engine.domain.communication.EngineEventService;
import com.emlogis.engine.domain.communication.ScheduleCompletion;
import com.emlogis.engine.domain.dto.AssignmentRequestDto;
import com.emlogis.engine.sqlserver.loader.SQLServerDtoEngineLoader;
import com.emlogis.schedule.engine.EngineHazelcastService;
import com.emlogis.scheduler.engine.communication.ComponentRole;
import com.emlogis.scheduler.engine.communication.HzConstants;
import com.emlogis.scheduler.engine.communication.request.EngineRequest;
import com.emlogis.scheduler.engine.communication.request.EngineRequestData;
import com.emlogis.scheduler.engine.communication.request.RequestType;
import com.emlogis.scheduler.engine.communication.response.EngineResponse;
import com.emlogis.scheduler.engine.communication.response.EngineResponseData;
import com.emlogis.shared.services.eventservice.EventService;
import com.emlogis.shared.services.hazelcastservice.HazelcastService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;

public class SchedulingEngineClient {

    private final static Logger logger = Logger.getLogger(SchedulingEngineClient.class);

    private static final String TENANT_ID = "jcso";
	private static final String SCHEDULE_ID = "fakeScheduleId:"; 
	private static final String ORIGIN_ID = "SchedulingEngineClient"; 

    public static void main(String[] args) {
    	SchedulingEngineClient client = new SchedulingEngineClient();
    	client.launch();
    	System.exit(0);
    }
    
    private void launch() {
    	// initialize Hazelcast Service (engine or utility side)
    	HazelcastService hzService = new EngineHazelcastService();
    	hzService.init();
    	
    	// initialize Event Service (engine or utility side)
    	EventService eventService = new EngineEventService();
    	eventService.init(hzService, ComponentRole.Engine);

    	// prepare a request
    	long now = System.currentTimeMillis();
    	String requestId = "req:" + now;
   	 	EngineRequest engineRequest = new EngineRequest();
        engineRequest.setRequestId(requestId);
        engineRequest.setTenantId(TENANT_ID);
        engineRequest.setRequestType(RequestType.Assignment);
        engineRequest.setScheduleId(SCHEDULE_ID + now);
        engineRequest.setAccountId("request from Eugene");
        engineRequest.setIncludeDetailedResponse(true);
        engineRequest.setOriginatingAppServerId(ORIGIN_ID);
        engineRequest.setStoreSchedulerReportInS3(false);

        
        // prior to sending the request,
        // put Solution in request Map (it includes some request duplicated fields just for the sake of facilitating debug)
        IMap<String, String> requestDataMap = hzService.getInstance().getMap(HzConstants.REQUEST_DATA_MAP);
        EngineRequestData requestData = new EngineRequestData();
        requestData.setRequestId(requestId);
        requestData.setRequestType(RequestType.Assignment);
        requestData.setScheduleId(SCHEDULE_ID + now);
        requestData.setOriginatingAppServerId(ORIGIN_ID);

        String jsonSolution = null;
		
        // Change this to false if you want to use the JSON 
        // Otherwise it will use data from database
        boolean loadHickory = false;
        
        if (loadHickory) {
        	AssignmentRequestDto solution = loadHickoryData();
        	jsonSolution = EmlogisUtils.toJsonString(solution);
        } else {
    		try {
    			jsonSolution = loadJSonFileBasedData("data/input/ScheduleAssignmentInput.json");
    		} catch (IOException e1) {
    			e1.printStackTrace();
    		}
        }

        requestData.setRequestData(jsonSolution);				// finally we put the data :)
		String requestDataJson = EmlogisUtils.toJsonString(requestData);
        requestDataMap.put(requestId, requestDataJson, 1, TimeUnit.HOURS);

         // now push the request into queue to trigger one of the engines of the pool
        IQueue<String> requestQueue = hzService.getInstance().getQueue(HzConstants.REQUEST_QUEUE_PREFIX
                + HzConstants.COMMON_SUFFIX);
        String engineRequestJson = EmlogisUtils.toJsonString(engineRequest);

        try {
        	requestQueue.put(engineRequestJson);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    	System.out.println("Sent request: "
                        + engineRequest.getTenantId() + ":" + engineRequest.getRequestType() + ":" + requestId + " by: "
                        + engineRequest.getAccountId() + "@" + engineRequest.getOriginatingAppServerId()
        );

        // now, wait for responses ( first one is Ack response, 2nd one should be the actual response from engine
        IQueue<String> responseQueue = hzService.getInstance().getQueue(HzConstants.RESPONSE_QUEUE_PREFIX
                + engineRequest.getOriginatingAppServerId());

    	IMap<String, String> responseDataMap = hzService.getInstance().getMap(HzConstants.RESPONSE_DATA_MAP);

        boolean responseLoop = true;
        while (responseLoop) {
            try {
            	String engineResponseJson = responseQueue.take();
            	EngineResponse engineResponse = EmlogisUtils.fromJsonString(engineResponseJson, EngineResponse.class);

//                logger.info(String.format("EngineResponse take: scheduleId=%s requestId=%s",
//                        engineResponse.getScheduleId(), engineResponse.getRequestId()));

                String receivedScheduleId = engineResponse.getScheduleId();
                String receivedTenantId = engineResponse.getTenantId();
                String receivedRequestId = engineResponse.getRequestId();
            	System.out.println("Got something on response queue:"  + engineResponse.getClass().getSimpleName()
                                + ":" + receivedTenantId + ":" + engineResponse.getRequestType() + ":"
                                + receivedRequestId
            	);
                
                if( StringUtils.equals(receivedRequestId, requestId)) {
                	System.out.println("Got a response for our request Id. what is it ?");
                } else {
                	System.out.println("Got a response, but corresponding to a different request, continue waiting ...");
                	continue;
                }

                if (EngineResponseType.Acknowledge.equals(engineResponse.getEngineResponseType())) {
                	System.out.println("Not bad, Engine got request and Acknowledged it !");
                } else if (EngineResponseType.Completion.equals(engineResponse.getEngineResponseType())) {
                	responseLoop = false;	// flag exit
               
                	ScheduleCompletion completion = engineResponse.getCompletion();
                	String completionInfo = engineResponse.getCompletionInfo();
                	System.out.println("Even better we got a response...");
                	if (completion == ScheduleCompletion.OK) {
                		// get response data and release the entry from response map
                    	System.out.println("Cool, status is OK...");
                        String responseDataJson = responseDataMap.get(requestId);
						EngineResponseData responseData =
								EmlogisUtils.fromJsonString(responseDataJson, EngineResponseData.class);
                        if (responseData != null && responseData.getResponseData() != null) {
                        	  
                        	FileUtils.writeStringToFile(new File("data/output/SampleAssignmentResult.json"), responseData.getResponseData());
                        	AssignmentResultDto responseObj = EmlogisUtils.fromJsonString(
                                    responseData.getResponseData(), AssignmentResultDto.class);
                        
                        	logger.info("Hard Scores " + responseObj.getHardScores());
                        	logger.info("Soft Scores " + responseObj.getSoftScores());
                        	logger.info("Shift Assignment: " + responseObj.getShiftAssignments());
                        	logger.info("Constraint Matches: " + responseObj.getConstraintMatchTotals());
                        }
                	} else {
                    	logger.error("Status is " + completion + "\n(" + completionInfo + ")");                		
                	}
                } else {
                	responseLoop = false;	// flag exit
                	System.out.println("What ??? got unexpected response class: " + engineResponse.getClass().getName()
                            + ", exiting ...");
                }
            } catch (Throwable t) {
            	t.printStackTrace();
            }
            // always release resources regardless of status
            cleanupRequest(requestId, requestDataMap, responseDataMap);
        }
    }
    
	private AssignmentRequestDto loadHickoryData() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("EmLogisOptaDB");
		EntityManager em = emf.createEntityManager();

		long scheduleId = 132;
		SQLServerDtoEngineLoader sqlServerLoader = new SQLServerDtoEngineLoader(scheduleId, 17,
                Arrays.asList(4L, 5L), new LocalDate(2014, 11, 23), new LocalDate(2014, 11, 30));
		sqlServerLoader.setEntityManager(em);
		return sqlServerLoader.getStartingSchedule();
	}
	
	private EmployeeSchedule loadFileBasedData(String fileName) throws IOException{
		String jsonEmployeeSchedule = FileUtils.readFileToString(new File(fileName));
		return EmlogisUtils.fromJsonString(jsonEmployeeSchedule, EmployeeSchedule.class);
	}
	
	private String loadJSonFileBasedData(String fileName) throws IOException{
		return FileUtils.readFileToString(new File(fileName));
	}

	private void cleanupRequest(String requestId, IMap<String, String> requestDataMap,
                                IMap<String, String> responseDataMap) {
		requestDataMap.delete(requestId);
		responseDataMap.delete(requestId);
	}

}
