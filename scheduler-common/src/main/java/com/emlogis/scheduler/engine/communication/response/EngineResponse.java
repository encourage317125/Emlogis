package com.emlogis.scheduler.engine.communication.response;

import com.emlogis.engine.domain.communication.ScheduleCompletion;
import com.emlogis.scheduler.engine.communication.request.RequestType;

import java.io.Serializable;

public class EngineResponse implements Serializable {
	
	private String tenantId;
	private String tenantName;
    private String accountId;
	private String accountName;			// name of user doing the request
	private String engineId;				// engine this reponse comes from
	private String engineLabel;			// label of engine this reponse comes from
	private RequestType requestType;
	private	String requestId;				// requestId (from EngineRequest)
	private	String scheduleId;				// schedule Id (from EngineRequest)
	private	String scheduleName;			// schedule name (from EngineRequest)
	private	String originatingAppServerId;	// id of requesting AppServer (from EngineRequest)
	private long requestStartdate;		// datetime the request was picked up by engine
	private ScheduleCompletion completion = ScheduleCompletion.OK;
	private String completionInfo;
	private EngineResponseType engineResponseType;


    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getEngineId() {
        return engineId;
    }

    public void setEngineId(String engineId) {
        this.engineId = engineId;
    }

    public String getScheduleName() {
		return scheduleName;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
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

    public long getRequestStartdate() {
        return requestStartdate;
    }

    public void setRequestStartdate(long requestStartdate) {
        this.requestStartdate = requestStartdate;
    }

	public ScheduleCompletion getCompletion() {
		return completion;
	}

	public void setCompletion(ScheduleCompletion completion) {
		this.completion = completion;
	}

	public String getCompletionInfo() {
		return completionInfo;
	}

	public void setCompletionInfo(String completionInfo) {
		this.completionInfo = completionInfo;
	}

	public String getEngineLabel() {
		return engineLabel;
	}

	public void setEngineLabel(String engineLabel) {
		this.engineLabel = engineLabel;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

    public EngineResponseType getEngineResponseType() {
        return engineResponseType;
    }

    public void setEngineResponseType(EngineResponseType engineResponseType) {
        this.engineResponseType = engineResponseType;
    }
}
