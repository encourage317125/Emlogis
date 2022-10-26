package com.emlogis.schedule.engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.StringInputStream;
import com.emlogis.common.Constants;
import com.emlogis.engine.domain.ScheduleReport;
import com.emlogis.engine.domain.communication.*;
import com.emlogis.engine.domain.communication.constraints.ScoreLevelResultDto;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.emlogis.common.EmlogisUtils;
import com.emlogis.engine.api.OptaplannerEngine;
import com.emlogis.engine.domain.EmployeeSchedule;
import com.emlogis.engine.domain.dto.AssignmentRequestDto;
import com.emlogis.scheduler.engine.communication.AssignmentRequestSerializer;
import com.emlogis.scheduler.engine.communication.HzConstants;
import com.emlogis.scheduler.engine.communication.request.EngineRequest;
import com.emlogis.scheduler.engine.communication.request.EngineRequestData;
import com.emlogis.scheduler.engine.communication.request.RequestType;
import com.emlogis.scheduler.engine.communication.response.EngineResponse;
import com.emlogis.scheduler.engine.communication.response.EngineResponseData;
import com.emlogis.scheduler.engine.communication.response.EngineResponseType;
import com.emlogis.shared.services.hazelcastservice.HazelcastService;
import com.emlogis.util.graphite.GraphiteSender;
import com.emlogis.util.graphite.GraphiteTCP;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.MapEvent;

public class RequestHandlerService {

	private final static Logger logger = Logger.getLogger(RequestHandlerService.class.getSimpleName());

	private String requestId;			// request being currently processed
	private OptaplannerEngine engine;

	private HazelcastService  hzService;
	private HazelcastInstance hzInstance;
	private NotificationService notificationService;
	private ESService esService;
	private GraphiteSender graphiteSender;

	private IQueue<String> requestQueue;
	private IMap<String, String> requestDataMap;
	private IMap<String, String> responseDataMap;

	private boolean exitRequested = false;
	private boolean waiting = false;

	static final MetricRegistry metrics = new MetricRegistry();
	private final Counter assignmentsCounter = metrics.counter(metrics.name("Request", "Assignment"));
	private final Timer responses = metrics.timer(metrics.name("Request", "requests"));

	private String 	engineName;
	private String	metricPrefix = "test.engine.";

	private String hostName = null;

	public RequestHandlerService(final HazelcastService hzService, final NotificationService notificationService,
			ESService esService, GraphiteSender graphiteSender) {

		this.hzService = hzService;
		hzInstance = hzService.getInstance();
		this.notificationService = notificationService;
		this.esService = esService;
		this.graphiteSender =  graphiteSender;
		engineName = hzService.getComponentName();


		requestQueue = hzInstance.getQueue(HzConstants.REQUEST_QUEUE_PREFIX + HzConstants.COMMON_SUFFIX);
		requestDataMap = hzInstance.getMap(HzConstants.REQUEST_DATA_MAP);

		responseDataMap = hzInstance.getMap(HzConstants.RESPONSE_DATA_MAP);

		initAbortMap(hzInstance);
		initShutdownMap(hzInstance);
	}

	public void start() {
		ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS).build();
		// commented out not to generate noise on console
		//		reporter.start(5, TimeUnit.SECONDS);
		//
		//		final JmxReporter jmxreporter = JmxReporter.forRegistry(metrics)
		//				.build();
		//		jmxreporter.start();

		try {
			hostName  = java.net.InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			System.out.println("Warning: Unable to retrieve host name. host will not be recorded in ElasticSearch Engine Records");
		}

		engine = new OptaplannerEngineImpl();

		Timer.Context context = null;
		while (!exitRequested) {
			try {
				// wait for request
				waiting = true;
				String engineRequestJson = requestQueue.take();
				EngineRequest engineRequest = EmlogisUtils.fromJsonString(engineRequestJson, EngineRequest.class);
				waiting = false;
				requestId = engineRequest.getRequestId();
				startMeasurement(engineRequest);
				//                logRequest(requestId, null);

				logger.debug("Got EngineRequest: " 
						+ engineRequest.getTenantId() + ":" + engineRequest.getRequestType() + ":" + requestId + " from: "
						+ engineRequest.getAccountId() + "@" + engineRequest.getAccountId()
						+ " AppServerId " + engineRequest.getOriginatingAppServerId()
						+ " ResponseQueueName " + engineRequest.getResponseQueueName()
						+ " QueueName " + requestQueue.getName()
						);

				logger.info(String.format("requestId=%s has been taken from request queue '%s'", requestId, requestQueue.getName()));

				// instrumentation
				assignmentsCounter.inc();
				context = responses.time();


				String requestDataJson = requestDataMap.get(requestId);
				EngineRequestData requestData = EmlogisUtils.fromJsonString(requestDataJson, EngineRequestData.class);
				// remove request entry from map to release resources
				requestDataMap.delete(requestId); // release map entry

				// got request, send acknowledge
				EngineResponse engineResponse = new EngineResponse();
				engineResponse.setEngineResponseType(EngineResponseType.Acknowledge);
				initResponse(engineRequest, engineResponse);

				IQueue<String> responseQueue;
				if (StringUtils.isNotBlank(engineRequest.getResponseQueueName())) {
					responseQueue = hzInstance.getQueue(engineRequest.getResponseQueueName());
				} else {
					responseQueue = hzInstance.getQueue(HzConstants.RESPONSE_QUEUE_PREFIX + HzConstants.COMMON_SUFFIX);
				}

				String engineResponseJson = EmlogisUtils.toJsonString(engineResponse);
				responseQueue.put(engineResponseJson);

				logger.info(String.format("Response '%s' Acknowledge put into response queue '%s'", engineResponse,
						responseQueue.getName()));

				logger.info("Put ACKNOWLEDGE on response queue:"
						+ engineResponse.getClass().getSimpleName() + ":" + engineResponse.getTenantId() + ":"
						+ engineResponse.getRequestType() + ":" + engineResponse.getRequestId()
						);

				// get the request corresponding data and invoke the scheduling engine
				EmployeeSchedule employeeSchedule;
				String incomingJson = requestData.getRequestData();
				boolean validRequest = true;
				boolean validResponse = true;
				try {
					AssignmentRequestDto assignmentRequestDto =
							EmlogisUtils.fromJsonString(incomingJson, AssignmentRequestDto.class);
					AssignmentRequestSerializer serializer = new AssignmentRequestSerializer();
					employeeSchedule = serializer.mapToEmployeeSchedule(assignmentRequestDto);
				} catch (Exception e) {
					logger.error("AssignmentRequestDto:fromJsonString",e);
					// TODO cleanly abort current request: = send response with error info, and keep receiving and processing requests
					validRequest = false;
					employeeSchedule = null;
				}

				if (validRequest) {
					LocalDate startDate = employeeSchedule.getEmployeeRosterInfo().getFirstShiftDate().getDate();

					String startDateStr = startDate.toString("yyyy-MM-dd");
					logRequestJson(incomingJson, engineRequest.getScheduleId(), startDateStr,
							engineRequest.getRequestType());

					// update request related attributes of notification service
					setNotificationRequestAttributes(notificationService, engineRequest);

					// debug
					int shiftCount = employeeSchedule.getShiftList().size();
					int empCount = employeeSchedule.getEmployeeList().size();
					System.out.println( "--> request " + engineRequest.getRequestType() + " of " + shiftCount + " shifts * " + empCount + "employees");
					// end debug	

					StopWatch watch = new StopWatch();
					watch.start();
					ScheduleResult result = fireSchedulingTask(engineRequest, employeeSchedule);
					// Debugging
					if (result == null || result.getResult()==null) {
						logger.error("jsonResponse is NULL, engineRequest will be replayed");
						validResponse=false;
						result = fireSchedulingTask(engineRequest, employeeSchedule);
						if (result == null || result.getResult()==null) {
							logger.error("jsonResponse is STILL NULL, engineRequest processing failed again");
							continue;
						}else{
							validResponse=true;
						}
					}
					watch.stop();

					// debug
					System.out.println( "<-- request " + engineRequest.getRequestType() + " of " + shiftCount + " shifts * " + empCount + "employees done in: "
							+ watch.getTime() / 1000 + "sec.");
					// end debug

					// put response data into response Map
					EngineResponseData responseData = new EngineResponseData();
					initResponseData(responseData, engineRequest);
					String jsonResponse = EmlogisUtils.toJsonString(result.getResult());


					logResponseJson(jsonResponse, engineRequest.getScheduleId(), startDateStr,
							responseData.getRequestType());

					responseData.setResponseData(jsonResponse);
					String responseDataJson = EmlogisUtils.toJsonString(responseData);
					responseDataMap.put(requestId, responseDataJson);

					logger.info(String.format("Response data '%s' put into responseDataMap '%s' under the key '%s'",
							responseData, responseDataMap.getName(), requestId));

					engineResponse = new EngineResponse();
					// and send response status via response Q
					if (ScheduleCompletion.OK.equals(result.getCompletion())) {
						engineResponse.setEngineResponseType(EngineResponseType.Completion);
					} else {
						engineResponse.setEngineResponseType(EngineResponseType.Abort);
					}

					initResponse(engineRequest, engineResponse);
					engineResponse.setCompletion(result.getCompletion());
					engineResponse.setCompletionInfo(result.getCompletionInfo());
					engineResponseJson = EmlogisUtils.toJsonString(engineResponse);
					responseQueue.put(engineResponseJson);

					logger.info(String.format("EngineResponse put: scheduleId=%s(%s) requestId=%s requestType=%s reponseType=%s EngineResponseType=%s from:%s(%s)",
							engineResponse.getScheduleId(), engineResponse.getScheduleName(), engineResponse.getRequestId(),
							engineResponse.getRequestType(),engineResponse.getClass().getSimpleName(), engineResponse.getEngineResponseType(), engineResponse.getEngineId(), engineResponse.getEngineLabel()));

					if (esService != null) {
						if (EngineResponseType.Completion.equals(engineResponse.getEngineResponseType())) {
							logExecution(engineRequest, employeeSchedule, engineResponse, result.getResult(), watch);
						} else {
							logExecution(engineRequest, engineResponse);
						}
					}

					if (engineRequest.isStoreSchedulerReportInS3()) {
						storeSchedulerReport(jsonResponse, engineRequest.getTenantId(), engineRequest.getScheduleId());
					}
				} 

				if (!validRequest || !validResponse) {
					// we have been unable to understand / decode the request
					engineResponse = new EngineResponse();
					engineResponse.setEngineResponseType(EngineResponseType.Abort);
					engineResponse.setCompletion(ScheduleCompletion.Aborted);
					if (!validRequest) {
						engineResponse.setCompletionInfo("Error while decoding request, unable to process Engine request");
					} else {
						engineResponse.setCompletionInfo("Error while decoding request, unable to process Engine request");
					}
					engineResponseJson = EmlogisUtils.toJsonString(engineResponse);
					responseQueue.put(engineResponseJson);
					logger.info(String.format("EngineResponse put (Invalid Request): scheduleId=%s(%s) requestId=%s requestType=%s reponseType=%s EngineResponseType=%s from:%s(%s)",
							engineResponse.getScheduleId(), engineResponse.getScheduleName(), engineResponse.getRequestId(),
							engineResponse.getRequestType(),engineResponse.getClass().getSimpleName(), engineResponse.getEngineResponseType(), engineResponse.getEngineId(), engineResponse.getEngineLabel()));

					if (esService != null) {
						logExecution(engineRequest, engineResponse);
					}                	
				}

				// Tell JVM this is a good time to clean up memory
				// otherwise it might wait until the next schedule execution
				// to perform GC, thus slowing down the scheduling process
				System.gc();
			} catch (HazelcastException | HazelcastInstanceNotActiveException e) {
				logger.error("Hazelcast exception", e);
				throw e;
			} catch (InterruptedException | UnsupportedEncodingException e) {
				logger.error("InterruptedException",e);
				throw new RuntimeException(e);
			} catch (Throwable t) {
				logger.error("RuntimeException", t);
				throw t;
			} finally {
				// instrumentation
				assignmentsCounter.dec();
				stopMeasurement();
				if (context != null) {
					context.stop();
				}
			}
		}
	}


	private void logExecution(EngineRequest engineRequest, EngineResponse engineResponse) {
		EngineRecord record = new EngineRecord();

		try {
			record
			.setHostName(hostName)
			.setRequestId(engineRequest.getRequestId())
			.setRequestType(engineRequest.getRequestType().getValue())
			.setTenantId(engineRequest.getTenantId())
			.setTenantName(engineRequest.getTenantName())
			.setAccountId(engineRequest.getAccountId())
			.setAccountName(engineRequest.getAccountName())
			.setOriginatingAppServerId(engineRequest.getOriginatingAppServerId())
			.setResponseQueueName(engineRequest.getResponseQueueName())
			.setEngineId(hzService.getComponentName())
			.setScheduleId(engineRequest.getScheduleId())
			.setScheduleName(engineRequest.getScheduleName())
			.setCompletionStatus(engineResponse.getCompletion().getValue());

			esService.indexEngineRecord(engineRequest.getTenantId(), record);
		} catch (Throwable t) {
			logger.error("Error while logging Engine execution into ElasticSearch", t);
		}		
	}

	private void logExecution(EngineRequest engineRequest, EmployeeSchedule employeeSchedule,
			EngineResponse engineResponse, ScheduleResultDto result, StopWatch watch) {
		EngineRecord record = new EngineRecord();
		DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinuteSecondMillis().withZoneUTC();

		try {
			record.setTimestamp(watch.getStartTime())
			.setHostName(hostName)
			.setRequestId(engineRequest.getRequestId())
			.setRequestType(engineRequest.getRequestType().getValue())
			.setTenantId(engineRequest.getTenantId())
			.setTenantName(engineRequest.getTenantName())
			.setAccountId(engineRequest.getAccountId())
			.setAccountName(engineRequest.getAccountName())
			.setOriginatingAppServerId(engineRequest.getOriginatingAppServerId())
			.setResponseQueueName(engineRequest.getResponseQueueName())
			.setEngineId(hzService.getComponentName())
			.setScheduleId(engineRequest.getScheduleId())
			.setScheduleName(engineRequest.getScheduleName())

			.setStartDate(employeeSchedule.getEmployeeRosterInfo().getFirstShiftDate().getDateString())
			.setEndDate(employeeSchedule.getEmployeeRosterInfo().getLastShiftDate().getDateString())
			.setMaxComputationTime(employeeSchedule.getMaxComputationTime())
			.setMaxUnimprovedSecondsSpent(employeeSchedule.getMaximumUnimprovedSecondsSpent())
			.setShiftCount(employeeSchedule.getShiftList().size())
			.setEmployeeCount(employeeSchedule.getEmployeeList().size())

			.setStarted(formatter.print(watch.getStartTime()))
			.setEnded(formatter.print(watch.getStartTime() + watch.getTime()))
			.setDuration(watch.getTime() / 1000) // (in sec)
			.setCompletionStatus(engineResponse.getCompletion().getValue())
			// TODO get actual CompletionMessage
			.setCompletionMessage("-");
			//	        .setHardScore(result.getHardScore())
			//	        .setSoftScore(result.getSoftScore());        
			setResultCounts(record, engineResponse, result);
			esService.indexEngineRecord(engineRequest.getTenantId(), record);
		} catch (Throwable t) {
			logger.error("Error while logging Engine execution into ElasticSearch", t);
		}
	}


	private void setResultCounts(EngineRecord record, EngineResponse engineResponse,
			ScheduleResultDto result) {

		switch (engineResponse.getRequestType()) {
		case Assignment:
			// get nb of engine assigned employees.
			List<ShiftAssignmentDto> assignments  = ((AssignmentResultDto)result).getShiftAssignments();
			int assignedCount = 0;
			for (ShiftAssignmentDto dto : assignments) {
				if (dto.getEmployeeId() != null && !dto.isLocked()) assignedCount++;
			}
			record.setAssignedCount(assignedCount);		
			break;
		case Qualification:
			Collection<ShiftQualificationDto> qualifyingShifts = ((QualificationResultDto)result).getQualifyingShifts();
			int qualifiedCount = 0;
			for (ShiftQualificationDto dto : qualifyingShifts) {
				if (dto.getIsAccepted()) qualifiedCount++;
			}
			record.setQualifiedCount(qualifiedCount);        	
			break;
		case OpenShiftEligibility:
			Collection<ShiftQualificationDto> eligibleShifts = ((QualificationResultDto)result).getQualifyingShifts();
			int eligibleCount = 0;
			for (ShiftQualificationDto dto : eligibleShifts) {
				if (dto.getIsAccepted()) eligibleCount++;
			}
			record.setEligibleCount(eligibleCount);
			break;
		case ShiftSwapEligibility:
			Map<String,Collection<ShiftQualificationDto>> swapEligibleShifts = ((ShiftSwapEligibilityResultDto)result).getQualifyingShifts();
			int swapEligibleCount = 0;
			for (Collection<ShiftQualificationDto> dtoList : swapEligibleShifts.values()) {
				for (ShiftQualificationDto dto : dtoList) {
					if (dto.getIsAccepted()) swapEligibleCount++;
				}   			 
			}
			record.setEligibleCount(swapEligibleCount);
			break;
		}
	}


	private ScheduleResult fireSchedulingTask(EngineRequest engineRequest, EmployeeSchedule employeeSchedule) {
		ScheduleResult result = null;
		try{
			boolean includeDetails = engineRequest.isIncludeDetailedResponse();
			if (engineRequest.getRequestType() == RequestType.Assignment) {
				result = engine.findAssignments(employeeSchedule, notificationService, includeDetails);
			} else if(engineRequest.getRequestType() == RequestType.Qualification){
				result = engine.checkQualification(employeeSchedule, notificationService, includeDetails);
			} else if(engineRequest.getRequestType() == RequestType.OpenShiftEligibility){
				result = engine.getOpenShiftEligibility(employeeSchedule, notificationService, includeDetails);
			} else if(engineRequest.getRequestType() == RequestType.ShiftSwapEligibility){
				result = engine.getShiftSwapEligibility(employeeSchedule, notificationService, includeDetails);
			} else {
				logger.error("Error on RequestType: "+engineRequest.getRequestType()+" not supported");
			} 
		} catch (Throwable t) {
			logger.error("Fatal error during  fireSchedulingTask.",t);
		}
		return result;
	}

	private void initResponse(EngineRequest engineRequest, EngineResponse engineResponse) {
		engineResponse.setTenantId(engineRequest.getTenantId());
		engineResponse.setTenantName(engineRequest.getTenantName());
		engineResponse.setAccountId(engineRequest.getAccountId());
		engineResponse.setAccountName(engineRequest.getAccountName());
		engineResponse.setOriginatingAppServerId(engineRequest.getOriginatingAppServerId());
		engineResponse.setRequestType(engineRequest.getRequestType());
		engineResponse.setRequestId(engineRequest.getRequestId());
		engineResponse.setScheduleId(engineRequest.getScheduleId());
		engineResponse.setScheduleName(engineRequest.getScheduleName());
		engineResponse.setEngineId(hzService.getComponentName());
		engineResponse.setEngineLabel(hzService.getComponentName());  	// we don't have a label as of now, use Id
	}

	private void initResponseData(EngineResponseData responseData, EngineRequest engineRequest) {
		responseData.setEngineId(hzService.getComponentName());
		responseData.setOriginatingAppServerId(engineRequest.getOriginatingAppServerId());
		responseData.setRequestId(engineRequest.getRequestId());
		responseData.setRequestType(engineRequest.getRequestType());
		responseData.setScheduleId(engineRequest.getScheduleId());
		responseData.setTenantId(engineRequest.getTenantId());	
	}

	private void setNotificationRequestAttributes(NotificationService notificationService,
			EngineRequest engineRequest) {
		notificationService.setTenantId(engineRequest.getTenantId());
		notificationService.setAccountId(engineRequest.getAccountId());
		notificationService.setScheduleId(engineRequest.getScheduleId());
		notificationService.setRequestId(requestId);  		
	}


	private void initAbortMap(final HazelcastInstance hzInstance) {
		final BlockingQueue<String> abortQueue = new LinkedBlockingQueue<>();

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!exitRequested) {
					try {
						String requestIdFromQueue = abortQueue.take();
						while (engine != null && !engine.abortSuccessful()
								&& StringUtils.equals(requestIdFromQueue, requestId)) {
							logger.info(String.format("Trying abort for requestId=%s", requestId));

							if (engine.abortAllowed()) {
								engine.abort();

								logger.info(String.format("Abort for requestId=%s finished successfully", requestId));
							} else {
								Thread.sleep(1000);
							}
						}
					} catch (InterruptedException e) {
						logger.error(e);
						throw new RuntimeException(e);
					}
				}
			}
		}).start();

		final IMap<String, Object> abortMap = hzInstance.getMap(HzConstants.ABORT_MAP);

		EntryListener<String, Object> entryListener = new EntryListener<String, Object>() {
			@Override
			public void entryAdded(EntryEvent event) {
				String key = (String) event.getKey();
				abortQueue.offer(key);
			}

			@Override
			public void entryRemoved(EntryEvent event) {}

			@Override
			public void entryUpdated(EntryEvent event) {}

			@Override
			public void entryEvicted(EntryEvent event) {}

			@Override
			public void mapEvicted(MapEvent event) {}

			@Override
			public void mapCleared(MapEvent event) {}
		};

		abortMap.addEntryListener(entryListener, false);
	}

	private void initShutdownMap(final HazelcastInstance hzInstance) {
		final IMap<String, Long> abortMap = hzInstance.getMap(HzConstants.SHUTDOWN_MAP);
		final IMap<String, Long> enginesMap = hzInstance.getMap(HzConstants.ENGINE_MAP);

		EntryListener<String, Long> entryListener = new EntryListener<String, Long>() {
			@Override
			public void entryAdded(EntryEvent event) {
				String key = (String) event.getKey();
				if (StringUtils.equals(key, hzService.getComponentName())) {
					exitRequested = true;	// set exit flag
					if (!waiting) {			// however, if currently blocked waiting for a request, let's exit immediatly
						// note there is a very short race condition on waiting flag, so
						// so a little chance that a request has been pulled from queue and we are stopping the engine resulting in a lost request
						// however this seems to be a very edge condition ... let's observe how this works before we chnage anything
						try {
							// otherwise, give a chance to engine to complete current request.
							Long timeout = (Long) event.getValue();
							Thread.sleep(timeout);
						} catch (Exception e) {
							logger.error(e);
						}
					}
					enginesMap.remove(key);
					System.exit(0);
				}
			}

			@Override
			public void entryRemoved(EntryEvent event) {}

			@Override
			public void entryUpdated(EntryEvent event) {}

			@Override
			public void entryEvicted(EntryEvent event) {}

			@Override
			public void mapEvicted(MapEvent event) {}

			@Override
			public void mapCleared(MapEvent event) {}
		};

		abortMap.addEntryListener(entryListener, false);
	}

	private void logRequestJson(String json, String scheduleId, String startDate, RequestType requestType) {
		logJson(json, "request", "schedule_" + startDate + "_" + scheduleId + "_" + requestType + "_req.json");
	}

	private void logResponseJson(String json, String scheduleId, String startDate, RequestType requestType) {
		logJson(json, "response", "schedule_" + startDate + "_" + scheduleId + "_" + requestType + "_resp.json");
	}

	private void logRequest(String requestId, String content) {

		if (content == null) content = "";
		String json = EmlogisUtils.toJsonString(content);
		logJson(json, "request", "request_" + requestId + "_req.json");
	}

	private void logJson(String json, String jsonDirName, String jsonFileName) {
		try {
			FileAppender fileAppender = (FileAppender) Logger.getRootLogger().getAppender("FILE");
			if (fileAppender != null) {
				String fileName = fileAppender.getFile();
				File file = new File(fileName);
				String dirName = StringUtils.defaultString(file.getParent());
				File dir = new File(dirName + File.separator + jsonDirName);
				File out = new File(dirName + File.separator + jsonDirName + File.separator + jsonFileName);
				if (!dir.exists()) {
					if (!dir.mkdir()) {
						logger.error("Can't create folder " + dir.getName());
						return;
					}
				}
				if (out.exists()) {
					out.delete();  // so we can replace with new
				}
				if (out.createNewFile()) {
					BufferedWriter writer = new BufferedWriter(new FileWriter(out));
					writer.write(json);
					writer.close();
				} else {
					logger.error("Can't create file " + out.getName());
				}
			}
		} catch (Exception e) {
			logger.error("Error logging json file", e);
		}
	}

	private void storeSchedulerReport(String responseJson, String tenantId, String key)
			throws UnsupportedEncodingException {
		AssignmentResultDto assignmentResultData = EmlogisUtils.fromJsonString(responseJson, AssignmentResultDto.class);

		if (assignmentResultData != null) {
			String completionReport = EmlogisUtils.toJsonString(assignmentResultData.getConstraintMatchTotals());

			ScheduleReport scheduleReport = new ScheduleReport();
			scheduleReport.setCompletionReport(completionReport);

			List<ScoreLevelResultDto> hardScores = assignmentResultData.getHardScores();
			if (hardScores != null) {
				scheduleReport.setHardScoreDetails(EmlogisUtils.toJsonString(hardScores));
			}
			List<ScoreLevelResultDto> softScores = assignmentResultData.getSoftScores();
			if (softScores != null) {
				scheduleReport.setSoftScoreDetails(EmlogisUtils.toJsonString(softScores));
			}

			String schedulerReportStr = EmlogisUtils.toJsonString(scheduleReport);

			StringInputStream inputStream = new StringInputStream(schedulerReportStr);
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setHeader("Content-Length", Long.valueOf(schedulerReportStr.length()));

			String awsFolder = System.getProperty(Constants.AWS_FOLDER_PROPERTY);
			if (StringUtils.isBlank(awsFolder)) {
				awsFolder = Constants.AWS_DEFAULT_FOLDER;
			}
			awsFolder += "/EmLogis-" + tenantId;

			AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(
					Constants.AWS_PROFILE_CONFIG_FILE_PATH, Constants.AWS_PROFILE_NAME);
			AmazonS3 s3client = new AmazonS3Client(credentialsProvider);
			s3client.putObject(new PutObjectRequest(awsFolder, key, inputStream, objectMetadata));
		}
	}


	private void startMeasurement(EngineRequest engineRequest) {

		if (graphiteSender == null) return;		// skip if no graphite defined

		int reqGauge = 1;
		int reqTypeGauge = -1; // -1 is for unknown request types

		// start time = current time (in seconds), minus 1 second (to avoid peaks that last less than 1 sec, 
		// thus under graphite resolution, and thus not visible)
		long now = (System.currentTimeMillis() / 1000L) -1; 
		try {
			graphiteSender.send(metricPrefix + engineName + ".activity", "1", now);
			if (engineRequest.getRequestType() == RequestType.Assignment) {
				graphiteSender.send(metricPrefix + engineName + ".assignment", "1", now);
				reqTypeGauge = 2;
			} else if(engineRequest.getRequestType() == RequestType.OpenShiftEligibility){
				graphiteSender.send(metricPrefix + engineName + ".openshifteligibility", "1", now);
				reqTypeGauge = 4;
			} else if(engineRequest.getRequestType() == RequestType.ShiftSwapEligibility){
				graphiteSender.send(metricPrefix + engineName + ".shiftswapeligibility", "1", now);
				reqTypeGauge = 6;
			} else if(engineRequest.getRequestType() == RequestType.Qualification){
				graphiteSender.send(metricPrefix + engineName + ".qualification", "1", now);
				reqTypeGauge = 8;
			}
			graphiteSender.send(metricPrefix + engineName + ".reqType", "" + reqTypeGauge, now);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to send data to Graphite.");
			System.out.println("********* SHUTING DOWN SENDING OF METRICS TO GRAPHITE  *********");	
			graphiteSender = null;
		}
	}

	private void stopMeasurement() {

		if (graphiteSender == null) return;		// skip if no graphite defined

		long now = System.currentTimeMillis() / 1000L; // time in sec.
		try {
			graphiteSender.send(metricPrefix + engineName + ".activity", "0", now);
			graphiteSender.send(metricPrefix + engineName + ".reqType", "0", now);
			graphiteSender.send(metricPrefix + engineName + ".assignment", "0", now);
			graphiteSender.send(metricPrefix + engineName + ".openshifteligibility", "0", now);
			graphiteSender.send(metricPrefix + engineName + ".shiftswapeligibility", "0", now);
			graphiteSender.send(metricPrefix + engineName + ".qualification", "0", now);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to send data to Graphite.");
			System.out.println("********* SHUTING DOWN SENDING OF METRICS TO GRAPHITE  *********");	
			graphiteSender = null;
		}
	}
}
