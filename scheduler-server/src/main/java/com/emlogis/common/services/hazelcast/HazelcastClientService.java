package com.emlogis.common.services.hazelcast;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.session.HazelcastExceptionHandling;
import com.emlogis.model.schedule.OpenShiftEligibilityRequestTracker;
import com.emlogis.model.schedule.QualificationRequestTracker;
import com.emlogis.model.schedule.ShiftSwapEligibilityRequestTracker;
import com.emlogis.scheduler.engine.communication.AppServerStatus;
import com.emlogis.scheduler.engine.communication.EngineStatus;
import com.emlogis.scheduler.engine.communication.HzConstants;
import com.emlogis.scheduler.engine.communication.request.EngineRequest;
import com.emlogis.scheduler.engine.communication.request.EngineRequestData;
import com.emlogis.scheduler.engine.communication.request.RequestType;
import com.emlogis.server.services.ASHazelcastService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Stateless
@LocalBean
@HazelcastExceptionHandling
public class HazelcastClientService {

    private final static Logger logger = Logger.getLogger(HazelcastClientService.class);

    final static private String ENGINE_DUMP_REQUEST = "engine.dump.request";
    final static private String ENGINE_DUMP_REQUEST_PATH = "engine.dump.request.path";

	private Map<String, Object> hazelcastCollections;

	@EJB
	private ASHazelcastService hazelcastService;

	@PostConstruct
	public void init() {
		hazelcastCollections = new HashMap<>();
	}

	public HazelcastInstance getInstance() {
		return hazelcastService.getInstance();
	}

	public String getAppServerName() {
		return hazelcastService.getAppServerName();
	}

	public Collection<AppServerStatus> getAppServers() {
		IMap<String, String> serversMap = getMap(HzConstants.APP_SERVER_MAP);

		Collection<AppServerStatus> result = new ArrayList<>();
		for (String value : serversMap.values()) {
			AppServerStatus appServerStatus = EmlogisUtils.fromJsonString(value, AppServerStatus.class);
			result.add(appServerStatus);
		}
		return result;
	}

	public int getAppServerCount() {
		IMap<String, String> serversMap = getMap(HzConstants.APP_SERVER_MAP);
		return serversMap.size();
	}

	public AppServerStatus getAppServer(String id) {
		IMap<String, String> serversMap = getMap(HzConstants.APP_SERVER_MAP);
		return EmlogisUtils.fromJsonString(serversMap.get(id), AppServerStatus.class);
	}

	public Collection<EngineStatus> getEngines() {
		IMap<String, String> enginesMap = getMap(HzConstants.ENGINE_MAP);

		Collection<EngineStatus> result = new ArrayList<>();
		for (String value : enginesMap.values()) {
			EngineStatus engineStatus = EmlogisUtils.fromJsonString(value, EngineStatus.class);
			result.add(engineStatus);
		}
		return result;
	}

	public int getEngineCount() {
		IMap<String, String> enginesMap = getMap(HzConstants.ENGINE_MAP);
		return enginesMap.size();
	}

	public EngineStatus getEngine(String id) {
		IMap<String, String> enginesMap = getMap(HzConstants.ENGINE_MAP);
		return EmlogisUtils.fromJsonString(enginesMap.get(id), EngineStatus.class);
	}

	public void putInRequestDataMap(String requestId, String scheduleId, String scheduleName, String appServerId,
			Object request) {
		IMap<String, String> requestMap = getMap(HzConstants.REQUEST_DATA_MAP);

		EngineRequestData requestData = new EngineRequestData();
		requestData.setRequestId(requestId);
		requestData.setRequestType(RequestType.Assignment);
		requestData.setOriginatingAppServerId(appServerId);
		requestData.setScheduleId(scheduleId);
		requestData.setScheduleName(scheduleName);
		requestData.setRequestData(EmlogisUtils.toJsonString(request));

		String requestDataJson = EmlogisUtils.toJsonString(requestData);

		requestMap.put(requestId, requestDataJson, 1, TimeUnit.HOURS);
	}

	public void putQualificationRequestTracker(
            String requestId, QualificationRequestTracker qualificationRequestTracker) {
		IMap<String, String> trackingMap = getMap(HzConstants.QUALIFICATION_TRACKING_MAP);
		String qualificationRequestTrackerJson = EmlogisUtils.toJsonString(qualificationRequestTracker);
		trackingMap.set(requestId, qualificationRequestTrackerJson, 1, TimeUnit.HOURS); 
	}

	public QualificationRequestTracker getQualificationRequestTracker(String requestId) {
		IMap<String, String> trackingMap = getMap(HzConstants.QUALIFICATION_TRACKING_MAP);
		String qualificationRequestTrackerJson = trackingMap.get(requestId);
		if (qualificationRequestTrackerJson != null) {
			return EmlogisUtils.fromJsonString(qualificationRequestTrackerJson, QualificationRequestTracker.class);
		} else {
			return null;
		}
	}

	public void putOpenShiftEligibilityRequestTracker(
            String requestId, OpenShiftEligibilityRequestTracker openShiftEligibilityRequestTracker) {
		IMap<String, String> trackingMap = getMap(HzConstants.QUALIFICATION_TRACKING_MAP);
		String openShiftEligibilityRequestTrackerJson = EmlogisUtils.toJsonString(openShiftEligibilityRequestTracker);
		trackingMap.set(requestId, openShiftEligibilityRequestTrackerJson, 1, TimeUnit.HOURS); 
	}

	public OpenShiftEligibilityRequestTracker getOpenShiftEligibilityRequestTracker(String requestId) {
		IMap<String, String> trackingMap = getMap(HzConstants.QUALIFICATION_TRACKING_MAP);
		String openShiftEligibilityRequestTrackerJson = trackingMap.get(requestId);
		if (openShiftEligibilityRequestTrackerJson != null) {
			return EmlogisUtils.fromJsonString(openShiftEligibilityRequestTrackerJson,
                    OpenShiftEligibilityRequestTracker.class);
		} else {
			return null;
		}
	}

	public void putShiftSwapEligibilityRequestTracker(
            String requestId, ShiftSwapEligibilityRequestTracker shiftSwapEligibilityRequestTracker) {
		IMap<String, String> trackingMap = getMap(HzConstants.QUALIFICATION_TRACKING_MAP);
		String shiftSwapEligibilityRequestTrackerJson = EmlogisUtils.toJsonString(shiftSwapEligibilityRequestTracker);
		trackingMap.set(requestId, shiftSwapEligibilityRequestTrackerJson, 1, TimeUnit.HOURS); 
	}

	public ShiftSwapEligibilityRequestTracker getShiftSwapEligibilityRequestTracker(String requestId) {
		IMap<String, String> trackingMap = getMap(HzConstants.QUALIFICATION_TRACKING_MAP);
		String shiftSwapEligibilityRequestTrackerJson = trackingMap.get(requestId);
		if (shiftSwapEligibilityRequestTrackerJson != null) {
			return EmlogisUtils.fromJsonString(shiftSwapEligibilityRequestTrackerJson,
                    ShiftSwapEligibilityRequestTracker.class);
		} else {
			return null;
		}
	}

	public void shutdownEngine(String engineId, long timeout) {
		IMap<String, Long> shutdownMap = getMap(HzConstants.SHUTDOWN_MAP);
		shutdownMap.put(engineId, timeout, 5, TimeUnit.MINUTES);
	}

	public void removeRequestAndResponseData(String requestId) {
		IMap<String, String> requestDataMap = getMap(HzConstants.REQUEST_DATA_MAP);
		requestDataMap.delete(requestId);
		IMap<String, String> responseDataMap = getMap(HzConstants.RESPONSE_DATA_MAP);
		responseDataMap.delete(requestId);
	}

	public void putEngineRequest(EngineRequest engineRequest, boolean processResponseWithAnyAppServer) {
		IQueue<String> requestQueue = getQueue(HzConstants.REQUEST_QUEUE_PREFIX + HzConstants.COMMON_SUFFIX);

		engineRequest.setOriginatingAppServerId(getAppServerName());
		String reponseQueueName = null;
		if (!processResponseWithAnyAppServer) {
			// specifiy reponse queue to be the current AppServer one, so that we get back the response in app server
			// that initiated request
			reponseQueueName = HzConstants.RESPONSE_QUEUE_PREFIX + getAppServerName();
			engineRequest.setResponseQueueName(reponseQueueName);
		}
		String engineRequestJson = EmlogisUtils.toJsonString(engineRequest);

		// conditionally dump engineRequestJson into a file named with engineRequest.getRequestId();
		String dump_request = System.getProperty(ENGINE_DUMP_REQUEST);
		if (StringUtils.equals(dump_request, "true")) {
			dumpEngineRequestJson(engineRequestJson, engineRequest.getRequestId());
		}
		try {
			logger.info("EngineRequest sent: RequestId = " +  engineRequest.getRequestId()
					+ ", OriginatingAppServerId = " +engineRequest.getOriginatingAppServerId()
					+ ", for Queue: " + (reponseQueueName != null ? reponseQueueName : "default")
					);
			requestQueue.put(engineRequestJson);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void abort(String requestId, Long timeout) {
		IMap<String, Long> abortMap = getMap(HzConstants.ABORT_MAP);
		abortMap.put(requestId, timeout, 5, TimeUnit.MINUTES);
	}

	public void removeRequestIdFromAbortMap(String requestId) {
		IMap<String, Long> abortMap = getMap(HzConstants.ABORT_MAP);
		abortMap.delete(requestId);
	}

	public Lock getLock(String key) {
		IMap<String, Lock> lockMap = getMap(HzConstants.LOCK_MAP);
		return lockMap.get(key);
	}

	public Lock getNativeLock(String key) {
		return getInstance().getLock(key);
	}

	public void releaseNativeLock(Lock nativeLock) {
		nativeLock.unlock();
	}

	public void putLock(Lock lock, String key) {
		IMap<String, Lock> lockMap = getMap(HzConstants.LOCK_MAP);
		lockMap.put(key, lock, 1, TimeUnit.HOURS);
	}

	public IMap<Serializable, byte[]> getActiveSessions() {
		return getMap(HzConstants.SESSION_MAP);
	}

	public int getMapSize(String mapName){
		return getMap(mapName).size();
	}

	public int getQueueSize(String queueName){
		return getQueue(queueName).size();
	}

	@SuppressWarnings("unchecked")
	private <T> IQueue<T> getQueue(String name) {
		Object result = hazelcastCollections.get(name);
		if (result == null) {
			result = getInstance().getQueue(name);
		}
		return (IQueue<T>) result;
	}

	@SuppressWarnings("unchecked")
	private <T, V> IMap<T, V> getMap(String name) {
		Object result = hazelcastCollections.get(name);
		if (result == null) {
			result = getInstance().getMap(name);

			hazelcastCollections.put(name, result);
		}
		return (IMap<T, V>) result;
	}

	private void dumpEngineRequestJson(String json, String requestId) {
		try {
			logger.info(ENGINE_DUMP_REQUEST + ":" + ENGINE_DUMP_REQUEST);

			String dump_request_path = System.getProperty(ENGINE_DUMP_REQUEST_PATH);
			logger.info(ENGINE_DUMP_REQUEST_PATH + " = " + dump_request_path);
			if (StringUtils.isEmpty(dump_request_path)) {
				dump_request_path = "/tmp/emlogis/logs";
			}
			File dir = new File(dump_request_path);
			File out = new File(dump_request_path + File.separator + requestId);
			if (!dir.exists()) {
				if (!dir.mkdir()) {
					logger.error("Can't create folder " + dir.getName());
					return;
				}
			}
			if (out.createNewFile()) {
				BufferedWriter writer = new BufferedWriter(new FileWriter(out));
				writer.write(json);
				writer.close();
			} else {
				logger.error("Can't create file " + out.getName());
			}

		} catch (Exception e) {
			logger.error("Error logging json file", e);
		}
	}

}
