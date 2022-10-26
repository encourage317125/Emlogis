package com.emlogis.model.employee.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AvailcalUpdateParamsAvailDto extends AvailcalUpdateParamsDto implements Serializable {
	
	public enum AvailAction implements Serializable {
		AVAILABLE_FOR_DAY,
		UNAVAILABLE_FOR_DAY,
		AVAILABLE_FOR_TIMEFRAMES
	}

	private AvailAction action;
	private List<AvailcalSimpleTimeFrame> timeFrames = new ArrayList<AvailcalSimpleTimeFrame>();  //optional; only applicable for AvailForTimeFrames    

	public AvailAction getAction() {return action;}
	public void setAction(AvailAction action) {this.action = action;}
	public List<AvailcalSimpleTimeFrame> getTimeFrames() {return timeFrames;}
	public void setTimeFrames(List<AvailcalSimpleTimeFrame> timeFrames) {this.timeFrames = timeFrames;}
}
