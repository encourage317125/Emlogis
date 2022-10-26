package com.emlogis.model.employee.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AvailcalPreviewWorkflowRequestParamsDto implements Serializable {

	private String workflowRequestId;
	private Long dateRangeStart;
    private Long dateRangeEnd;
    
	public String getWorkflowRequestId() {
		return workflowRequestId;
	}
	
	public void setWorkflowRequestId(String workflowRequestId) {
		this.workflowRequestId = workflowRequestId;
	}
	
	public Long getDateRangeStart() {
		return dateRangeStart;
	}
	
	public void setDateRangeStart(Long dateRangeStart) {
		this.dateRangeStart = dateRangeStart;
	}
	
	public Long getDateRangeEnd() {
		return dateRangeEnd;
	}
	
	public void setDateRangeEnd(Long dateRangeEnd) {
		this.dateRangeEnd = dateRangeEnd;
	}
}
