package com.emlogis.model.employee.dto;

import com.emlogis.engine.domain.DayOfWeek;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)

public class CIAvailabilityTimeFrameDto extends AvailabilityTimeFrameDto  implements Serializable {
	
	private long startDateTime;
	private long endDateTime;
	private DayOfWeek dayOfTheWeek;
	private long startTime;
	

	public long getStartDateTime() {
		return startDateTime;
	}
	public void setStartDateTime(long startDateTime) {
		this.startDateTime = startDateTime;
	}
	public long getEndDateTime() {
		return endDateTime;
	}
	public void setEndDateTime(long endDateTime) {
		this.endDateTime = endDateTime;
	}
	public DayOfWeek getDayOfTheWeek() {
		return dayOfTheWeek;
	}
	public void setDayOfTheWeek(DayOfWeek dayOfTheWeek) {
		this.dayOfTheWeek = dayOfTheWeek;
	}		
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTimeLong) {
		this.startTime = startTimeLong;
	}
}
