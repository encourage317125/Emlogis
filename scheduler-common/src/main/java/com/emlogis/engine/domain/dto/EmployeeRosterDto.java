package com.emlogis.engine.domain.dto;

import java.io.Serializable;
import java.util.Map;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.ShiftDate;
import com.emlogis.engine.domain.WeekendDefinition;
import com.emlogis.engine.domain.solver.RuleName;

public class EmployeeRosterDto implements Serializable {

    private ShiftDate firstShiftDate;
    private ShiftDate lastShiftDate;
    private ShiftDate planningWindowStart;
    private ShiftDate twoWeekOvertimeStartDate;
    private DayOfWeek firstDayOfWeek;
    private WeekendDefinition weekendDefinition;
    private Map<RuleName, Integer> ruleWeightMultipliers;
    private Map<RuleName, Integer> scoringRulesToScoreLevelMap;

    // Engine Optimization Options
    private boolean includePTOInMinCalculations = true; // If true PTO is included when calculating minHoursPerDay/Week/PrimeSkill
    private ProfileDayType profileDayType; // Which day do shift hours belong to
    private boolean forceCompletionEnabled = false; // Add additional excess shifts to satisfy min constraints(day, week, primeskill)

    private String timeZone;
    
	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public boolean isIncludePTOInMinCalculations() {
		return includePTOInMinCalculations;
	}

	public void setIncludePTOInMinCalculations(
			boolean includePTOInMinCalculations) {
		this.includePTOInMinCalculations = includePTOInMinCalculations;
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

	public WeekendDefinition getWeekendDefinition() {
		return weekendDefinition;
	}

	public void setWeekendDefinition(WeekendDefinition weekendDefinition) {
		this.weekendDefinition = weekendDefinition;
	}

	public Map<RuleName, Integer> getRuleWeightMultipliers() {
		return ruleWeightMultipliers;
	}

	public void setRuleWeightMultipliers(
			Map<RuleName, Integer> ruleWeightMultipliers) {
		this.ruleWeightMultipliers = ruleWeightMultipliers;
	}

	public void putRuleWeightMultiplier(RuleName ruleName, int ruleWeight) {
		ruleWeightMultipliers.put(ruleName, ruleWeight);
	}

	public void putScoringRuleScoreLevel(RuleName ruleName, int ruleLevel) {
		scoringRulesToScoreLevelMap.put(ruleName, ruleLevel);
	}

	public Map<RuleName, Integer> getScoringRulesToScoreLevelMap() {
		return scoringRulesToScoreLevelMap;
	}

	public void setScoringRulesToScoreLevelMap(
			Map<RuleName, Integer> scoringRulesToScoreLevelMap) {
		this.scoringRulesToScoreLevelMap = scoringRulesToScoreLevelMap;
	}

	public ShiftDate getTwoWeekOvertimeStartDate() {
		return twoWeekOvertimeStartDate;
	}

	public void setTwoWeekOvertimeStartDate(ShiftDate twoWeekOvertimeStartDate) {
		this.twoWeekOvertimeStartDate = twoWeekOvertimeStartDate;
	}

	public ProfileDayType getProfileDayType() {
		return profileDayType;
	}

	public void setProfileDayType(ProfileDayType profileDayType) {
		this.profileDayType = profileDayType;
	}

	public boolean isForceCompletionEnabled() {
		return forceCompletionEnabled;
	}

	public void setForceCompletionEnabled(boolean forceCompletionEnabled) {
		this.forceCompletionEnabled = forceCompletionEnabled;
	}

}
