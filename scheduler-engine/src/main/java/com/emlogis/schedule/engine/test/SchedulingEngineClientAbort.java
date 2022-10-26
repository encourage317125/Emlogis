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
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;

/**
 * This class loads data from hickory and sends it to the RequestMap to be
 * executed by the optimization engine. After it recieves an acknowledgment of
 * the request receipt this class will wait for 30 seconds and then send an
 * abort It should receive a schedule result after the abort
 * 
 * @author emlogis
 * 
 */
public class SchedulingEngineClientAbort {

	private final static Logger logger = Logger.getLogger(SchedulingEngineClientAbort.class);

	private static final String TENANT_ID = "jcso";
	private static final String SCHEDULE_ID = "fakeScheduleId:";
	private static final String ORIGIN_ID = "SchedulingEngineClient";

	private String requestId;

	public static void main(String[] args) {
		SchedulingEngineClientAbort client = new SchedulingEngineClientAbort();
		client.launch();
		System.exit(0);
	}

	private void abort(HazelcastInstance hzInstance) {
		final IMap<String, Object> abortMap = hzInstance.getMap(HzConstants.ABORT_MAP);
		logger.info("Sending abort to engine for requestId: " + requestId);
		abortMap.put(requestId, 1000, 5, TimeUnit.MINUTES);
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
		requestId = "req:" + now;
		EngineRequest engineRequest = new EngineRequest();
		engineRequest.setRequestId(requestId);
		engineRequest.setTenantId(TENANT_ID);
		engineRequest.setRequestType(RequestType.Assignment);
		engineRequest.setScheduleId(SCHEDULE_ID + now);
		engineRequest.setAccountId("request from Eugene");
		engineRequest.setOriginatingAppServerId(ORIGIN_ID);
        engineRequest.setStoreSchedulerReportInS3(false);


		// prior to sending the request,
		// put Solution in request Map (it includes some request duplicated
		// fields just for the sake of facilitating debug)
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
				jsonSolution = loadJSonFileBasedData("G:/SampleScheduleInput.json");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		FileWriter fw;
		try {
			fw = new FileWriter("G:/SampleScheduleInput.json");
			fw.write(jsonSolution);
			fw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		requestData.setRequestData(jsonSolution); // finally we put the data :)
		String requestDataJson = EmlogisUtils.toJsonString(requestData);
		requestDataMap.put(requestId, requestDataJson, 1, TimeUnit.HOURS);

		// now push the request into queue to trigger one of the engines of the
		// pool
		IQueue<EngineRequest> requestQueue = hzService.getInstance().getQueue(
				HzConstants.REQUEST_QUEUE_PREFIX + HzConstants.COMMON_SUFFIX);
		try {
			requestQueue.put(engineRequest);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Sent request: " + engineRequest.getTenantId() + ":" + engineRequest.getRequestType() + ":"
				+ requestId + " by: " + engineRequest.getAccountId() + "@" + engineRequest.getOriginatingAppServerId());

		// now, wait for responses ( first one is Ack response, 2nd one should
		// be the actual response from engine
		IQueue<EngineResponse> responseQueue = hzService.getInstance().getQueue(
				HzConstants.RESPONSE_QUEUE_PREFIX + HzConstants.COMMON_SUFFIX);

		IMap<String, String> responseDataMap = hzService.getInstance().getMap(HzConstants.RESPONSE_DATA_MAP);

		boolean responseLoop = true;
		while (responseLoop) {
			try {
				EngineResponse engineResponse = responseQueue.take();

				// logger.info(String.format("EngineResponse take: scheduleId=%s requestId=%s",
				// engineResponse.getScheduleId(),
				// engineResponse.getRequestId()));

				String receivedScheduleId = engineResponse.getScheduleId();
				String receivedTenantId = engineResponse.getTenantId();
				String receivedRequestId = engineResponse.getRequestId();
				System.out.println("Got something on response queue:" + engineResponse.getClass().getSimpleName() + ":"
						+ receivedTenantId + ":" + engineResponse.getRequestType() + ":" + receivedRequestId);

				if (StringUtils.equals(receivedRequestId, requestId)) {
					System.out.println("Got a response for our request Id. what is it ?");
				} else {
					System.out
							.println("Got a response, but corresponding to a different request, continue waiting ...");
					continue;
				}

				if (EngineResponseType.Acknowledge.equals(engineResponse.getEngineResponseType())) {
					System.out.println("Not bad, Engine got request and Acknowledged it !");
					try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					abort(hzService.getInstance());
				} else if (EngineResponseType.Abort.equals(engineResponse.getEngineResponseType())) {
					responseLoop = false; // flag exit

					ScheduleCompletion completion = engineResponse.getCompletion();
					String completionInfo = engineResponse.getCompletionInfo();
					System.out.println("Even better we got a response...");
					if (completion == ScheduleCompletion.Aborted) {
						// get response data and release the entry from response
						// map
						System.out.println("Cool, status is Aborted...");
						String responseDataJson = responseDataMap.get(requestId);
						EngineResponseData responseData =
								EmlogisUtils.fromJsonString(requestDataJson, EngineResponseData.class);
						if (responseData != null && responseData.getResponseData() != null) {
							// process response
							// System.out.println("And we have data ! :" +
							// responseData.getResponseData());
							// logger.debug("And we have data ! :" +
							// responseData.getResponseData());
							// get object from json deserialized response

							FileWriter filewriter = new FileWriter("ScheduleOutput.csv");
							filewriter.write(responseData.getResponseData());
							filewriter.close();
							AssignmentResultDto responseObj = EmlogisUtils.fromJsonString(
									responseData.getResponseData(), AssignmentResultDto.class);

							System.out.println("data as object: " + responseObj.getShiftAssignments());
						}
					} else {
						System.out.println("Too bad, status is " + completion + "(" + completionInfo + ")");
					}
				} else {
					responseLoop = false; // flag exit
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
