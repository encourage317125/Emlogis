package com.emlogis.scheduler.engine.communication.request;


import java.io.Serializable;

/**
 * @author EmLogis
 * 
 * This class is used by the AppServers to delegate /send a request to one of the Scheduling Engines connected to
 * the system via Hazelcast.
 *
 */

public class EngineRequestData implements Serializable {

	// these attributes duplicated from request mainly for logging and debugging purpose 
	private RequestType	requestType;
	private	String requestId;				// requestId is the key in RequestMap
	private	String scheduleId;				// schedule Id
	private	String scheduleName;			// schedule Name
	private	String originatingAppServerId;	// id of requesting AppServer
	
	private String requestData;			// JSON serialized engine request data
	
	// placeholder for actual data based on requestType
	// TODO 

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getScheduleName() {
		return scheduleName;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	public String getOriginatingAppServerId() {
        return originatingAppServerId;
    }

    public void setOriginatingAppServerId(String originatingAppServerId) {
        this.originatingAppServerId = originatingAppServerId;
    }

	public String getRequestData() {
		return requestData;
	}

	public void setRequestData(String requestData) {
		this.requestData = requestData;
	}
}
