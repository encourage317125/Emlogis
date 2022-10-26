package com.emlogis.model.schedule.dto;

import java.util.Map;

import com.emlogis.engine.domain.contract.ConstraintOverrideType;

public class OverriddenShiftQualExecuteDto extends ExecuteDto {

	private int maxSynchronousWaitSeconds = 60;
	private String shiftId;
	private String employeeId;
	private Map<ConstraintOverrideType, Boolean> overrideOptions;	// overrides to use for qualification/eligibility
																	// null means no client override, use schedule overrides

	public int getMaxSynchronousWaitSeconds() {
		return maxSynchronousWaitSeconds;
	}

	public void setMaxSynchronousWaitSeconds(int maxSynchronousWaitSeconds) {
		this.maxSynchronousWaitSeconds = maxSynchronousWaitSeconds;
	}

	public String getShiftId() {
		return shiftId;
	}

	public void setShiftId(String shiftId) {
		this.shiftId = shiftId;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public Map<ConstraintOverrideType, Boolean> getOverrideOptions() {
		return overrideOptions;
	}

	public void setOverrideOptions(Map<ConstraintOverrideType, Boolean> overrideOptions) {
		this.overrideOptions = overrideOptions;
	}

}
