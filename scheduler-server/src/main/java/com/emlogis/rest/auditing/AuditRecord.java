package com.emlogis.rest.auditing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditRecord {
	
	@JsonIgnore
	private	boolean				processed = false;	// flag for tagging the record as sent to ES. 
													// (allows the RestExceptionHandler to make a decision on whether to log the record or not)

	@JsonIgnore
	ParametersLogging 			paramsLogging = ParametersLogging.None;	// flag driven by ParametersLogging annotation param, indicating if input/outparams must be logged
													// allows to prevent sending huge parameters to ES

	private	String				id;					// unique record Id
	private	String				envName;			// environment Name (qa, prod, uat, local, etc...)
	
	private	LogRecordCategory	category;			// record category

	@JsonIgnore
	private long 				timestamp;			// timestamp for internal use. timetstamp is then sent to ES 
	@JsonProperty("@timestamp")						// as a ISO formated String exported into the @timestamp field
	private String 				_timestamp;			//

	private	String				URL;				// URL invoked by client
	private	String				HTTPMethod;			// GET / POST / PUT etc
	private	String				token;				// EmLogis token sent by client

	private	String				clientIPAddress;	
	
	private	String				clientHostname;
	
	private	String				clientUserAgent;	// user agent field from HTTP header

	private	String				clientOrigin;		// origin HTTP header field
	
	private	String				tenantId = "System";	// default TenantId when no Tenant is associated to a record (ex: failed logins if org not specified)		
	private	String				tenantName = "System";	// default TenantName when no Tenant is associated to a record (ex: failed logins if org not specified)		
	private	String				sessionId;			
	private	String				userId;
	private	String				userName;
	
	private	String				resourceClass;		// java class of  REST resource 
	private	String				resourceMethod;		// java method of REST resource
	private	String				apiCall;			// user friendly label for the REST method
	private	String				callCategory;		// user friendly category for the cal
	private	long				callDuration;		// call duration in millisec
	
	private	Map<String,Object>	inputParams = new HashMap<>();		// list of input parameters keyed by classname_methodname_parameterindex
	private	Map<String,Object>	outputParams = new HashMap<>();	// output parameters
	private	Response.Status		HTTPReturnCode;		// HTTP return code

	
	private	String				exception;			// exception name (if any)
	private	StackTraceElement[]	stackTrace;			// exception stack trace (if any)
	
	public AuditRecord() {
		super();
	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	public ParametersLogging getParamsLogging() {
		return paramsLogging;
	}

	public void setParamsLogging(ParametersLogging paramsLogging) {
		this.paramsLogging = paramsLogging;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LogRecordCategory getCategory() {
		return category;
	}

	public void setCategory(LogRecordCategory category) {
		this.category = category;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String get_timestamp() {
		return _timestamp;
	}

	public void set_timestamp(String _timestamp) {
		this._timestamp = _timestamp;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public String getHTTPMethod() {
		return HTTPMethod;
	}

	public void setHTTPMethod(String hTTPMethod) {
		HTTPMethod = hTTPMethod;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getClientIPAddress() {
		return clientIPAddress;
	}

	public void setClientIPAddress(String clientIPAddress) {
		this.clientIPAddress = clientIPAddress;
	}

	public String getClientHostname() {
		return clientHostname;
	}

	public void setClientHostname(String clientHostname) {
		this.clientHostname = clientHostname;
	}

	public String getClientUserAgent() {
		return clientUserAgent;
	}

	public void setClientUserAgent(String clientUserAgent) {
		this.clientUserAgent = clientUserAgent;
	}

	public String getClientOrigin() {
		return clientOrigin;
	}

	public void setClientOrigin(String clientOrigin) {
		this.clientOrigin = clientOrigin;
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

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getResourceClass() {
		return resourceClass;
	}

	public void setResourceClass(String resourceClass) {
		this.resourceClass = resourceClass;
	}

	public String getResourceMethod() {
		return resourceMethod;
	}

	public void setResourceMethod(String resourceMethod) {
		this.resourceMethod = resourceMethod;
	}

	public String getApiCall() {
		return apiCall;
	}

	public void setApiCall(String apiCall) {
		this.apiCall = apiCall;
	}

	public String getCallCategory() {
		return callCategory;
	}

	public void setCallCategory(String callCategory) {
		this.callCategory = callCategory;
	}

	public long getCallDuration() {
		return callDuration;
	}

	public void setCallDuration(long callDuration) {
		this.callDuration = callDuration;
	}

	public Map<String, Object> getInputParams() {
		return inputParams;
	}

	public void setInputParams(Map<String, Object> inputParameters) {
		this.inputParams = inputParameters;
	}
	
	public void clearInputParams() {
		this.inputParams = new HashMap<>();
	}
	
	public void setInputParam( String name, Object value) {
		inputParams.put(name, value);
	}

	public Response.Status getHTTPReturnCode() {
		return HTTPReturnCode;
	}

	public void setHTTPReturnCode(Response.Status hTTPReturnCode) {
		HTTPReturnCode = hTTPReturnCode;
	}

	public Map<String, Object> getOutputParams() {
		return outputParams;
	}

	public void setOutputParams(Map<String, Object> outputParameters) {
		this.outputParams = outputParameters;
	}

	public void clearOutputParams() {
		this.outputParams = new HashMap<>();
	}
	public void setOutputParam( String name, Object value) {
		outputParams.put(name, value);
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public StackTraceElement[] getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(StackTraceElement[] stackTrace) {
		this.stackTrace = stackTrace;
	}

	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

    @Override
    public AuditRecord clone() throws CloneNotSupportedException {
    	
    	AuditRecord clone = new AuditRecord();
    	clone.setApiCall(this.getApiCall());
    	clone.setCallCategory(this.getCallCategory());
    	clone.setCallDuration(this.getCallDuration());
    	clone.setCategory(this.getCategory());
    	clone.setClientHostname(this.getClientHostname());
    	clone.setClientIPAddress(this.getClientIPAddress());
    	clone.setClientOrigin(this.getClientOrigin());
    	clone.setClientUserAgent(this.getClientUserAgent());
    	clone.setEnvName(this.getEnvName());
    	clone.setException(this.getException());
    	clone.setHTTPMethod(this.getHTTPMethod());
    	clone.setHTTPReturnCode(this.getHTTPReturnCode());
    	clone.setId(this.getId());
    	clone.setInputParams(this.getInputParams());
    	clone.setOutputParams(this.getOutputParams());
    	clone.setParamsLogging(this.getParamsLogging());
    	clone.setResourceClass(this.getResourceClass());
    	clone.setResourceMethod(this.getResourceMethod());
    	clone.setSessionId(this.getSessionId());
    	clone.setStackTrace(this.getStackTrace());
    	clone.setTenantId(this.getTenantId());
    	clone.setTenantName(this.getTenantName());
    	clone.setToken(this.getToken());
    	clone.setURL(this.getURL());
    	clone.setUserId(this.getUserId());
    	clone.setUserName(this.getUserName());
    	clone.setTimestamp(this.getTimestamp());
    	clone.set_timestamp(this.get_timestamp());
        return clone;
    }

}
