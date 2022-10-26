package com.emlogis.model.schedule.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenShiftEligibilityExecuteDto extends ExecuteDto {
	
	private int maxSynchronousWaitSeconds = 60;
	private List<String> employeeIds = new ArrayList<>();
	private List<String> shiftIds = new ArrayList<>();
	private Map<ConstraintOverrideType, Boolean> overrideOptions;	// overrides to use for qualification/eligibility
																	// null means no client override, use schedule overrides
	
	public OpenShiftEligibilityExecuteDto() {
		super();
	}

	public List<String> getEmployeeIds() {
		return employeeIds;
	}
	
	public void setEmployeeIds(List<String> employeeIds) {
		this.employeeIds = employeeIds;
	}
	
	public List<String> getShiftIds() {
		return shiftIds;
	}
	
	public void setShiftIds(List<String> shiftIds) {
		this.shiftIds = shiftIds;
	}

	public int getMaxSynchronousWaitSeconds() {
		return maxSynchronousWaitSeconds;
	}

	public void setMaxSynchronousWaitSeconds(int maxSynchronousWaitSeconds) {
		this.maxSynchronousWaitSeconds = maxSynchronousWaitSeconds;
	}

	public Map<ConstraintOverrideType, Boolean> getOverrideOptions() {
		return overrideOptions;
	}

	public void setOverrideOptions(Map<ConstraintOverrideType, Boolean> overrideOptions) {
		this.overrideOptions = overrideOptions;
	}

}
