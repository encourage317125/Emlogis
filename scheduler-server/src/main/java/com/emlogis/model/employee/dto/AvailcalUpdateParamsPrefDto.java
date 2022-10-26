package com.emlogis.model.employee.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AvailcalUpdateParamsPrefDto extends AvailcalUpdateParamsDto implements Serializable {
	
	public enum PrefAction implements Serializable {
		PREFER_DAY,
		AVOID_DAY,
		TIMEFRAMES
	}

    public static class PrefTimeFrame implements Serializable {
    	private Long startTime;
    	private Long endTime;
    	
		public Long getStartTime() {return startTime;}
		public void setStartTime(Long startTime) {this.startTime = startTime;}
		public Long getEndTime() {return endTime;}
		public void setEndTime(Long endTime) {this.endTime = endTime;}
    }

	private PrefAction action;
	private List<AvailcalSimpleTimeFrame> preferTimeFrames = new ArrayList<AvailcalSimpleTimeFrame>();
	private List<AvailcalSimpleTimeFrame> avoidTimeFrames = new ArrayList<AvailcalSimpleTimeFrame>();    

	public PrefAction getAction() {return action;}
	public void setAction(PrefAction action) {this.action = action;}
	public List<AvailcalSimpleTimeFrame> getPreferTimeFrames() {return preferTimeFrames;}
	public void setPreferTimeFrames(List<AvailcalSimpleTimeFrame> preferTimeFrames) {this.preferTimeFrames = preferTimeFrames;}
	public List<AvailcalSimpleTimeFrame> getAvoidTimeFrames() {return avoidTimeFrames;}
	public void setAvoidTimeFrames(List<AvailcalSimpleTimeFrame> avoidTimeFrames) {this.avoidTimeFrames = avoidTimeFrames;}
}
