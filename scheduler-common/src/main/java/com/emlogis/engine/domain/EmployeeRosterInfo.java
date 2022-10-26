package com.emlogis.engine.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.domain.solver.SolverConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class EmployeeRosterInfo  {

    private ShiftDate firstShiftDate; // Date of firstDayOfWeek before the planning window starts
    private ShiftDate lastShiftDate; // Last day where assignments can be modified

    private ShiftDate planningWindowStart; // First day where assignments can be modified
    
    private ShiftDate twoWeekOvertimeStartDate; // First date of two week overtime start
    
    private DayOfWeek 			firstDayOfWeek; // Definition of first day of week for this schedule
    private WeekendDefinition  	weekendDefinition; // Defines weekend in this schedule(SAT_SUN, FRI_SAT_SUN, etc..)
    
    private Map<RuleName, Integer> ruleWeightMultipliers; // Defines the weight multiplier for each rule
    
    private Map<RuleName, Integer> scoringRulesToScoreLevelMap; // Defines the score level for each soft constraint rule

    private int highestScoreLevel = -1; // Cached value of highest value in scoringRulesToScoreLevelMap
    
    private boolean includePTOInMinCalculations = true; // If true PTO is included when calculating minHoursPerDay/Week/PrimeSkill
    
    private boolean forceCompletionEnabled = false; // Add additional excess shifts to satisfy min constraints(day, week, primeskill)
    
    public EmployeeRosterInfo(){
    	ruleWeightMultipliers = new HashMap<RuleName, Integer>();
    	scoringRulesToScoreLevelMap = new HashMap<RuleName, Integer>();
    }
    
    public int getHighestRuleLevel(){
    	if(highestScoreLevel == -1 ){
    		highestScoreLevel =  Collections.max(scoringRulesToScoreLevelMap.values());
    	}
    	return highestScoreLevel;
    }
    
    public int getRuleWeightMultiplier(RuleName ruleName){
    	int ruleWeight = -1; //Default rule weight value
    	Integer definedWeight = ruleWeightMultipliers.get(ruleName);
    	if(definedWeight != null){
    		ruleWeight = definedWeight.intValue();
    	}
    	
    	return ruleWeight;
    }
    
    public int getRuleWeightMultiplier(String ruleName){
    	int ruleWeight = -1; //Default rule weight value
    	Integer definedWeight = ruleWeightMultipliers.get(RuleName.fromString(ruleName));
    	if(definedWeight != null){
    		ruleWeight = definedWeight.intValue();
    	}
    	
    	return ruleWeight;
    }
    
    public int getScoringRuleScoreLevel(String ruleName){
    	int scoreLevel = getHighestRuleLevel(); //Default rule level is the last one
    	RuleName ruleEnum = RuleName.fromString(ruleName);
    	Integer definedLevel = scoringRulesToScoreLevelMap.get(ruleEnum);
    	if(definedLevel != null){
    		scoreLevel = definedLevel.intValue();
    	}
    	
    	return scoreLevel;
    }
    
    public void putRuleWeightMultiplier(RuleName ruleName, int ruleWeight){
    	ruleWeightMultipliers.put(ruleName, ruleWeight);
    }
    
    public void putScoringRuleScoreLevel(RuleName ruleName, int ruleLevel){
    	scoringRulesToScoreLevelMap.put(ruleName, ruleLevel);
    }
    
    public Map<RuleName, Integer> getRuleWeightMultipliers() {
		return ruleWeightMultipliers;
	}

	public void setRuleWeightMultipliers(Map<RuleName, Integer> ruleWeights) {
		this.ruleWeightMultipliers = ruleWeights;
	}

	public ShiftDate getFirstShiftDate() {
        return firstShiftDate;
    }

    public void setFirstShiftDate(ShiftDate firstShiftDate) {
        this.firstShiftDate = firstShiftDate;
    }

    public ShiftDate getLastShiftDate() {
        return lastShiftDate;
    }

    public void setLastShiftDate(ShiftDate lastShiftDate) {
        this.lastShiftDate = lastShiftDate;
    }
    
    @JsonIgnore
    public int getFirstShiftDateDayIndex() {
        return firstShiftDate.getDayIndex();
    }

    @JsonIgnore
    public int getLastShiftDateDayIndex() {
        return lastShiftDate.getDayIndex();
    }

    public ShiftDate getPlanningWindowStart() {
        return planningWindowStart;
    }

    public void setPlanningWindowStart(ShiftDate planningWindowStart) {
        this.planningWindowStart = planningWindowStart;
    }
    
    public DayOfWeek getFirstDayOfWeek() {
		return firstDayOfWeek;
	}

	public void setFirstDayOfWeek(DayOfWeek firstDayOfWeek) {
		this.firstDayOfWeek = firstDayOfWeek;
	}

    // ************************************************************************
    // Worker methods
    // ************************************************************************

	public WeekendDefinition getWeekendDefinition() {
		return weekendDefinition;
	}

	public void setWeekendDefinition(WeekendDefinition weekendDefinition) {
		this.weekendDefinition = weekendDefinition;
	}

	@JsonIgnore
	public ShiftDate getFirstShiftDateOn(DayOfWeek day){
		int distanceTo = firstShiftDate.getDayOfWeek().getDistanceToNext(day);
		return new ShiftDate(firstShiftDate.getDate().plusDays(distanceTo));
	}
	
    public boolean isInPlanningWindow(ShiftDate shiftDate) {
      	if(shiftDate == null || planningWindowStart == null){
    		return false;
    	}
        return planningWindowStart.isBeforeOrEquals(shiftDate) && lastShiftDate.isAfterOrEquals(shiftDate);
    }
    
    public boolean isInScheduleWindow(ShiftDate shiftDate) {
    	if(shiftDate == null || firstShiftDate == null){
    		return false;
    	}
        return  firstShiftDate.isBeforeOrEquals(shiftDate) && lastShiftDate.isAfterOrEquals(shiftDate);
    }
    
    public boolean isInPlanningWindow(DateTime shiftDate) {
      	if(shiftDate == null || planningWindowStart == null){
    		return false;
    	}
        return planningWindowStart.isBeforeOrEquals(shiftDate);
    }
    
    public boolean isInScheduleWindow(DateTime shiftDate) {
    	if(shiftDate == null || firstShiftDate == null){
    		return false;
    	}
        return  firstShiftDate.isBeforeOrEquals(shiftDate);
    }

	public ShiftDate getTwoWeekOvertimeStartDate() {
		return twoWeekOvertimeStartDate;
	}

	public void setTwoWeekOvertimeStartDate(ShiftDate twoWeekOvertimeStartDate) {
		this.twoWeekOvertimeStartDate = twoWeekOvertimeStartDate;
	}

	public Map<RuleName, Integer> getScoringRulesToScoreLevelMap() {
		return scoringRulesToScoreLevelMap;
	}

	public void setScoringRulesToScoreLevelMap(
			Map<RuleName, Integer> scoringRulesToScoreLevelMap) {
		this.scoringRulesToScoreLevelMap = scoringRulesToScoreLevelMap;
		highestScoreLevel = -1; //reset cached value
	}

	public boolean isIncludePTOInMinCalculations() {
		return includePTOInMinCalculations;
	}

	public void setIncludePTOInMinCalculations(boolean includePTOInMinCalculations) {
		this.includePTOInMinCalculations = includePTOInMinCalculations;
	}

	public boolean isForceCompletionEnabled() {
	    return forceCompletionEnabled;
	}

	public void setForceCompletionEnabled(boolean forceCompletionEnabled) {
	    this.forceCompletionEnabled = forceCompletionEnabled;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EmployeeRosterInfo [firstShiftDate=");
		builder.append(firstShiftDate);
		builder.append(", lastShiftDate=");
		builder.append(lastShiftDate);
		builder.append(", planningWindowStart=");
		builder.append(planningWindowStart);
		builder.append(", firstDayOfWeek=");
		builder.append(firstDayOfWeek);
		builder.append("]");
		return builder.toString();
	}

}
