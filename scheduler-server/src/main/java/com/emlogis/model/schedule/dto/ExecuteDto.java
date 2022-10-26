package com.emlogis.model.schedule.dto;

import java.io.Serializable;

public class ExecuteDto implements Serializable {

	private int maxComputationTime = -1;
	private int maximumUnimprovedSecondsSpent = 100;

	public ExecuteDto() {
		super();
	}

	public int getMaxComputationTime() {
	    return maxComputationTime;
	}

	public void setMaxComputationTime(int maxComputationTime) {
	    this.maxComputationTime = maxComputationTime;
	}

	public int getMaximumUnimprovedSecondsSpent() {
	    return maximumUnimprovedSecondsSpent;
	}

	public void setMaximumUnimprovedSecondsSpent(int maximumUnimprovedSecondsSpent) {
	    this.maximumUnimprovedSecondsSpent = maximumUnimprovedSecondsSpent;
	}

}