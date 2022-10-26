package com.emlogis.scheduler.engine.communication.response;

import com.emlogis.scheduler.engine.communication.request.RequestType;

import java.io.Serializable;

public class EngineResponseData implements Serializable {

	// these attributes duplicated from request mainly for logging and debugging purpose 
	private String tenantId;				// engine this response comes from
	private String engineId;				// engine this response comes from
	private RequestType requestType;
	private	String requestId;				// requestId (from EngineRequest)
	private	String scheduleId;				// schedule Id (from EngineRequest)
	private	String originatingAppServerId;	// id of requesting AppServer (from EngineRequest)
	
	private String	responseData;			// JSON serialized engine response data 

    public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getEngineId() {
        return engineId;
    }

    public void setEngineId(String engineId) {
        this.engineId = engineId;
    }

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

    public String getOriginatingAppServerId() {
        return originatingAppServerId;
    }

    public void setOriginatingAppServerId(String originatingAppServerId) {
        this.originatingAppServerId = originatingAppServerId;
    }

	public String getResponseData() {
		return responseData;
	}

	public void setResponseData(String responseData) {
		this.responseData = responseData;
	}
}
