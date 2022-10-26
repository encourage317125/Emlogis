package com.emlogis.model.schedule.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShiftSwapEligibilityExecuteDto extends ExecuteDto implements Serializable {
	
	private int maxSynchronousWaitSeconds = 60;
	private List<String> swapSeekingShifts = new ArrayList<String>();
	private List<String> swapCandidateShifts = new ArrayList<String>();
	
	public ShiftSwapEligibilityExecuteDto() {
		super();
	}

	public int getMaxSynchronousWaitSeconds() {
		return maxSynchronousWaitSeconds;
	}

	public List<String> getSwapSeekingShifts() {
		return swapSeekingShifts;
	}

	public void setSwapSeekingShifts(List<String> swapSeekingShifts) {
		this.swapSeekingShifts = swapSeekingShifts;
	}

	public List<String> getSwapCandidateShifts() {
		return swapCandidateShifts;
	}

	public void setSwapCandidateShifts(List<String> swapCandidateShifts) {
		this.swapCandidateShifts = swapCandidateShifts;
	}

	public void setMaxSynchronousWaitSeconds(int maxSynchronousWaitSeconds) {
		this.maxSynchronousWaitSeconds = maxSynchronousWaitSeconds;
	}

}
