package com.emlogis.engine.domain.communication.constraints;

import com.emlogis.engine.domain.solver.RuleName;

import java.util.HashMap;
import java.util.Map;

public class ScoreLevelResultDto {
	private int totalScoreValue; // Total score value should equal the sum of the score level details values
	private Map<RuleName, Integer> scoreLevelDetails;
	
	public ScoreLevelResultDto(){
		totalScoreValue = 0;
		scoreLevelDetails = new HashMap<>();
	}
	
	public int getTotalScoreValue() {
		return totalScoreValue;
	}
	
	public void setTotalScoreValue(int totalScoreValue) {
		this.totalScoreValue = totalScoreValue;
	}
	
	public Map<RuleName, Integer> getScoreLevelDetails() {
		return scoreLevelDetails;
	}
	
	public void setScoreLevelDetails(Map<RuleName, Integer> scoreLevelDetails) {
		this.scoreLevelDetails = scoreLevelDetails;
	}
	
	public void addScoreLevelDetail(RuleName name, int value){
		scoreLevelDetails.put(name, value);
		totalScoreValue += value;
	}

	@Override
	public String toString() {
		return "ScoreLevelResultDto [totalScoreValue=" + totalScoreValue
				+ ", scoreLevelDetails=" + scoreLevelDetails + "]";
	}
	
}
