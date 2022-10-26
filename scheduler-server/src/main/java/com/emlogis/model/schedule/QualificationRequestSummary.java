package com.emlogis.model.schedule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.concurrent.Future;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class QualificationRequestSummary {
	
	private	String requestId;
	private boolean fullyQualified;
	private boolean forced;
	private boolean isSuccess = true;
	private String message;
	
	
	
	public QualificationRequestSummary() {
		super();
	}

	public QualificationRequestSummary(String requestId) {
		super();
		this.requestId = requestId;
	}

	public QualificationRequestSummary(String requestId, boolean fullyQualified, boolean forced) {
		super();
		this.requestId = requestId;
		this.fullyQualified = fullyQualified;
		this.forced = forced;
	}

	public QualificationRequestSummary(String requestId, boolean fullyQualified, boolean forced, String message, boolean isSuccess) {
		super();
		this.requestId = requestId;
		this.fullyQualified = fullyQualified;
		this.forced = forced;
		this.message = message;
		this.isSuccess = isSuccess;
	}

	public String getRequestId() {
		return requestId;
	}
	
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	
	public boolean getFullyQualified() {
		return fullyQualified;
	}
	
	public void setFullyQualified(boolean fullyQualified) {
		this.fullyQualified = fullyQualified;
	}

	public boolean isForced() {
		return forced;
	}

	public void setForced(boolean forced) {
		this.forced = forced;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setIsSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
