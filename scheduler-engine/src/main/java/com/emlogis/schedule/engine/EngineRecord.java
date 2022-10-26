package com.emlogis.schedule.engine;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EngineRecord {
	
	private	String	requestId;			// unique record Id
	private	String	envName;			// environment Name (qa, prod, uat, local, etc...)
	
	@JsonIgnore
	private long 	timestamp;			// timestamp for internal use. timetstamp is then sent to ES 
	@JsonProperty("@timestamp")						// as a ISO formated String exported into the @timestamp field
	private String 	_timestamp;			//
	
	private String 	hostName;
	private String 	engineId;
	private String 	tenantId;
	private String	tenantName;
	private String	originatingAppServerId;
	private String	accountId;
	private String	accountName;
	private String	scheduleId;
	private String	scheduleName;
	private String	responseQueueName;
	private String	requestType;
	private String	startDate;
	private String	endDate;
	private int		shiftCount;
	private int		employeeCount;
    private int 	maxComputationTime;						
    private int 	maxUnimprovedSecondsSpent;     
    private int 	assignedCount; 		// nb of assignmment computed (for Assignment requests)
    private int 	qualifiedCount; 	// nb of qualificated employees  (for Qualification requests)
    private int 	eligibleCount; 		// nb of eligible employees   (for Eligibility requests)
    

	
	private String	started;
	private String	ended;
	private long	duration;				// in seconds
	private String	completionStatus;
	private String	completionMessage;
//    private long 	softScore;     
//    private long 	hardScore;     
	
	
	public String getHostName() {
		return hostName;
	}
	public EngineRecord setHostName(String hostName) {
		this.hostName = hostName;
		return this;
	}
	public String getRequestId() {
		return requestId;
	}
	public EngineRecord setRequestId(String requestId) {
		this.requestId = requestId;
		return this;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public EngineRecord setTimestamp(long timestamp) {
		this.timestamp = timestamp;
		return this;
	}
	public String get_timestamp() {
		return _timestamp;
	}
	public EngineRecord set_timestamp(String _timestamp) {
		this._timestamp = _timestamp;
		return this;
	}
	public String getEngineId() {
		return engineId;
	}
	public EngineRecord setEngineId(String engineId) {
		this.engineId = engineId;
		return this;
	}
	public String getTenantId() {
		return tenantId;
	}
	public EngineRecord setTenantId(String tenantId) {
		this.tenantId = tenantId;
		return this;
	}
	public String getTenantName() {
		return tenantName;
	}
	public EngineRecord setTenantName(String tenantName) {
		this.tenantName = tenantName;
		return this;
	}
	public String getOriginatingAppServerId() {
		return originatingAppServerId;
	}
	public EngineRecord setOriginatingAppServerId(String originatingAppServerId) {
		this.originatingAppServerId = originatingAppServerId;
		return this;
	}
	public String getAccountId() {
		return accountId;
	}
	public EngineRecord setAccountId(String accountId) {
		this.accountId = accountId;
		return this;
	}
	public String getAccountName() {
		return accountName;
	}
	public EngineRecord setAccountName(String accountName) {
		this.accountName = accountName;
		return this;
	}
	public String getScheduleId() {
		return scheduleId;
	}
	public EngineRecord setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
		return this;
	}
	public String getScheduleName() {
		return scheduleName;
	}
	public EngineRecord setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
		return this;
	}
	public String getResponseQueueName() {
		return responseQueueName;
	}
	public EngineRecord setResponseQueueName(String responseQueueName) {
		this.responseQueueName = responseQueueName;
		return this;
	}
	public String getRequestType() {
		return requestType;
	}
	public EngineRecord setRequestType(String requestType) {
		this.requestType = requestType;
		return this;
	}
	public String getStartDate() {
		return startDate;
	}
	public EngineRecord setStartDate(String startDate) {
		this.startDate = startDate;
		return this;
	}
	public String getEndDate() {
		return endDate;
	}
	public EngineRecord setEndDate(String endDate) {
		this.endDate = endDate;
		return this;
	}
	public int getShiftCount() {
		return shiftCount;
	}
	public EngineRecord setShiftCount(int shiftCount) {
		this.shiftCount = shiftCount;
		return this;
	}
	public int getEmployeeCount() {
		return employeeCount;
	}
	public EngineRecord setEmployeeCount(int employeeCount) {
		this.employeeCount = employeeCount;
		return this;
	}
	public String getStarted() {
		return started;
	}
	public EngineRecord setStarted(String started) {
		this.started = started;
		return this;
	}
	public String getEnded() {
		return ended;
	}
	public EngineRecord setEnded(String ended) {
		this.ended = ended;
		return this;
	}
	public long getDuration() {
		return duration;
	}
	public EngineRecord setDuration(long duration) {
		this.duration = duration;
		return this;
	}
	public String getCompletionStatus() {
		return completionStatus;
	}
	public EngineRecord setCompletionStatus(String completionStatus) {
		this.completionStatus = completionStatus;
		return this;
	}
	public String getCompletionMessage() {
		return completionMessage;
	}
	public EngineRecord setCompletionMessage(String completionMessage) {
		this.completionMessage = completionMessage;
		return this;
	}
	public int getMaxComputationTime() {
		return maxComputationTime;
	}
	public EngineRecord setMaxComputationTime(int maxComputationTime) {
		this.maxComputationTime = maxComputationTime;
		return this;
	}
	public int getMaxUnimprovedSecondsSpent() {
		return maxUnimprovedSecondsSpent;
	}
	public EngineRecord setMaxUnimprovedSecondsSpent(int maxUnimprovedSecondsSpent) {
		this.maxUnimprovedSecondsSpent = maxUnimprovedSecondsSpent;
		return this;
	}
	public int getAssignedCount() {
		return assignedCount;
	}
	public void setAssignedCount(int assignedCount) {
		this.assignedCount = assignedCount;
	}
	public int getQualifiedCount() {
		return qualifiedCount;
	}
	public void setQualifiedCount(int qualifiedCount) {
		this.qualifiedCount = qualifiedCount;
	}
	public int getEligibleCount() {
		return eligibleCount;
	}
	public void setEligibleCount(int eligibleCount) {
		this.eligibleCount = eligibleCount;
	}
	public String getEnvName() {
		return envName;
	}
	public void setEnvName(String envName) {
		this.envName = envName;
	}

}
