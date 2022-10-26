package com.emlogis.scheduler.engine.communication.request;

import java.io.Serializable;

/**
 * @author EmLogis
 *         <p/>
 *         This class is used by the AppServers to delegate /send a request to one of the Scheduling Engines connected to
 *         the system via Hazelcast.
 */
public class EngineRequest implements Serializable {

    private RequestType requestType;
    private String requestId;                // requestId is the key in RequestMap
    private String scheduleId;               // schedule Id
    private String scheduleName;           	 // schedule Name
    private String tenantId;
    private String tenantName;
    private String originatingAppServerId;   // id of requesting AppServer
    private String responseQueueName;        // name of Queue to post te response, the common one if unspecified (null)
    private String accountId;                // id of user doing the request
    private String accountName;           	 // name of user doing the request
    private boolean includeDetailedResponse = false;    // If true include constraint details for assignment, qualification and eligibility
    private boolean storeSchedulerReportInS3 = true;

    public boolean isIncludeDetailedResponse() {
        return includeDetailedResponse;
    }

    public void setIncludeDetailedResponse(boolean includeDetailedResponse) {
        this.includeDetailedResponse = includeDetailedResponse;
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

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getOriginatingAppServerId() {
        return originatingAppServerId;
    }

    public void setOriginatingAppServerId(String originatingAppServerId) {
        this.originatingAppServerId = originatingAppServerId;
    }

    public String getResponseQueueName() {
        return responseQueueName;
    }

    public void setResponseQueueName(String responseQueueName) {
        this.responseQueueName = responseQueueName;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public boolean isStoreSchedulerReportInS3() {
        return storeSchedulerReportInS3;
    }

    public void setStoreSchedulerReportInS3(boolean storeSchedulerReportInS3) {
        this.storeSchedulerReportInS3 = storeSchedulerReportInS3;
    }
}
