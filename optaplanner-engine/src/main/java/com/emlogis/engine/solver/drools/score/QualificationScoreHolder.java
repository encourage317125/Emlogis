package com.emlogis.engine.solver.drools.score;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.LocalDate;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.buildin.bendable.BendableScoreHolder;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.ShiftAssignment;
import com.emlogis.engine.domain.Skill;
import com.emlogis.engine.domain.communication.constraints.CDTimeOffShiftConstraintDto;
import com.emlogis.engine.domain.communication.constraints.CITimeOffShiftConstraintDto;
import com.emlogis.engine.domain.communication.constraints.CoupledWeekendConstraintDto;
import com.emlogis.engine.domain.communication.constraints.DaysAfterWeekendConstraintDto;
import com.emlogis.engine.domain.communication.constraints.DaysBeforeWeekendConstraintDto;
import com.emlogis.engine.domain.communication.constraints.MaxShiftConstraintDto;
import com.emlogis.engine.domain.communication.constraints.MinShiftConstraintDto;
import com.emlogis.engine.domain.communication.constraints.ShiftConstraintDto;
import com.emlogis.engine.domain.communication.constraints.SkillShiftConstraintDto;
import com.emlogis.engine.domain.communication.constraints.TeamShiftConstraintDto;
import com.emlogis.engine.domain.communication.constraints.WeekdayRotationConstraintDto;
import com.emlogis.engine.domain.contract.contractline.MinMaxContractLine;
import com.emlogis.engine.domain.contract.patterns.WeekdayRotationPattern;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.domain.solver.SolverConstants;
import com.emlogis.engine.domain.timeoff.CDTimeWindow;
import com.emlogis.engine.domain.timeoff.CITimeWindow;
import com.emlogis.engine.domain.timeoff.dto.CDTimeOffDto;
import com.emlogis.engine.domain.timeoff.dto.CITimeOffDto;

/**
 * This class is responsible for holding constraint violations for all shifts
 * that are part of the shiftConstraintQualificationMap
 * 
 * @author emlogis
 * 
 */
public class QualificationScoreHolder extends BendableScoreHolder {
	private Map<ShiftAssignment, List<ShiftConstraintDto>> shiftConstraintQualificationMap;

	public QualificationScoreHolder(boolean constraintMatchEnabled) {
		super(constraintMatchEnabled, SolverConstants.NUM_HARD_LEVELS, SolverConstants.NUM_SOFT_LEVELS);
		shiftConstraintQualificationMap = new HashMap<>();
	}
	
	public QualificationScoreHolder(boolean constraintMatchEnabled, int softLevels) {
		super(constraintMatchEnabled, SolverConstants.NUM_HARD_LEVELS, softLevels);
		shiftConstraintQualificationMap = new HashMap<>();
	}

	/**
	 * Initalizes this class and creates an empty list for every ShiftAssignment
	 * object that was passed in.
	 * 
	 * @param constraintMatchEnabled
	 * @param shiftsBeingQualified
	 */
	public QualificationScoreHolder(boolean constraintMatchEnabled, Collection<ShiftAssignment> shiftsBeingQualified) {
		super(constraintMatchEnabled, SolverConstants.NUM_HARD_LEVELS, SolverConstants.NUM_SOFT_LEVELS);
		shiftConstraintQualificationMap = new HashMap<>();
		for (ShiftAssignment shiftBeingQualified : shiftsBeingQualified) {
			shiftConstraintQualificationMap.put(shiftBeingQualified, new ArrayList<ShiftConstraintDto>());
		}
	}
	
	public void addRotationHardConstraintMatch(RuleContext kcontext, final int weight, WeekdayRotationPattern pattern, List involvedShiftAssignments) {
		if (!shiftConstraintQualificationMap.isEmpty()) {
			Collection<String> involvedShiftIds =  extractShiftIdsFromAssignments(involvedShiftAssignments);
			// Adds constraint information iff there are shifts being qualified
			// during optimization the shiftQualificationMap is always empty
			Collection<ShiftAssignment> matchingShifts = CollectionUtils.intersection(
					shiftConstraintQualificationMap.keySet(), involvedShiftAssignments);
			for (ShiftAssignment matchingShift : matchingShifts) {
				WeekdayRotationConstraintDto weekdayRotationPattern = new WeekdayRotationConstraintDto(pattern);
				weekdayRotationPattern.setConstraintName(RuleName.WEEKDAY_ROTATION_PATTERN_RULE);
				weekdayRotationPattern.setWeight(weight);
				weekdayRotationPattern.setInvolvedShifts(involvedShiftIds);
				shiftConstraintQualificationMap.get(matchingShift).add(weekdayRotationPattern);
			}
		}
		addHardConstraintMatch(kcontext,SolverConstants. HARD_CONSTRAINT_LEVEL, weight);
	}
	
	public void addDaysAfterHardConstraintMatch(RuleContext kcontext, final int weight, Collection<DayOfWeek> daysOfWeek, List involvedShiftAssignments) {
		if (!shiftConstraintQualificationMap.isEmpty()) {
			Collection<String> involvedShiftIds =  extractShiftIdsFromAssignments(involvedShiftAssignments);
			// Adds constraint information iff there are shifts being qualified
			// during optimization the shiftQualificationMap is always empty
			Collection<ShiftAssignment> matchingShifts = CollectionUtils.intersection(
					shiftConstraintQualificationMap.keySet(), involvedShiftAssignments);
			for (ShiftAssignment matchingShift : matchingShifts) {
				DaysAfterWeekendConstraintDto daysAfterConstraint = new DaysAfterWeekendConstraintDto(daysOfWeek);
				daysAfterConstraint.setConstraintName(RuleName.DAYS_OFF_AFTER_WEEKEND_RULE);
				daysAfterConstraint.setWeight(weight);
				daysAfterConstraint.setInvolvedShifts(involvedShiftIds);
				shiftConstraintQualificationMap.get(matchingShift).add(daysAfterConstraint);
			}
		}
		addHardConstraintMatch(kcontext,SolverConstants.HARD_CONSTRAINT_LEVEL, weight);
	}
	
	public void addDaysBeforeHardConstraintMatch(RuleContext kcontext, final int weight, Collection<DayOfWeek> daysOfWeek, List involvedShiftAssignments) {
		if (!shiftConstraintQualificationMap.isEmpty()) {
			Collection<String> involvedShiftIds =  extractShiftIdsFromAssignments(involvedShiftAssignments);
			// Adds constraint information iff there are shifts being qualified
			// during optimization the shiftQualificationMap is always empty
			Collection<ShiftAssignment> matchingShifts = CollectionUtils.intersection(
					shiftConstraintQualificationMap.keySet(), involvedShiftAssignments);
			for (ShiftAssignment matchingShift : matchingShifts) {
				DaysBeforeWeekendConstraintDto daysAfterConstraint = new DaysBeforeWeekendConstraintDto(daysOfWeek);
				daysAfterConstraint.setConstraintName(RuleName.DAYS_OFF_BEFORE_WEEKEND_RULE);
				daysAfterConstraint.setWeight(weight);
				daysAfterConstraint.setInvolvedShifts(involvedShiftIds);
				shiftConstraintQualificationMap.get(matchingShift).add(daysAfterConstraint);
			}
		}
		addHardConstraintMatch(kcontext, SolverConstants.HARD_CONSTRAINT_LEVEL, weight);
	}
	
	public void addCoupledWeekendHardConstraintMatch(RuleContext kcontext, final int weight, LocalDate weekendStartDate, List involvedShiftAssignments) {
		if (!shiftConstraintQualificationMap.isEmpty()) {
			Collection<String> involvedShiftIds =  extractShiftIdsFromAssignments(involvedShiftAssignments);
			// Adds constraint information iff there are shifts being qualified
			// during optimization the shiftQualificationMap is always empty
			Collection<ShiftAssignment> matchingShifts = CollectionUtils.intersection(
					shiftConstraintQualificationMap.keySet(), involvedShiftAssignments);
			for (ShiftAssignment matchingShift : matchingShifts) {
				CoupledWeekendConstraintDto teamConstraint = new CoupledWeekendConstraintDto();
				teamConstraint.setConstraintName(RuleName.COUPLED_WEEKEND_RULE);
				teamConstraint.setWeekendStartDate(weekendStartDate);
				teamConstraint.setWeight(weight);
				shiftConstraintQualificationMap.get(matchingShift).add(teamConstraint);
			}
		}
		addHardConstraintMatch(kcontext, SolverConstants.HARD_CONSTRAINT_LEVEL, weight);
	}
	
	public void addTeamHardConstraintMatch(RuleContext kcontext, final int weight, ShiftAssignment shift) {
		// Adds constraint information iff there are shifts being qualified
		// during optimization the shiftQualificationMap is always empty
		if (shiftConstraintQualificationMap.containsKey(shift)) {
			TeamShiftConstraintDto teamConstraint = new TeamShiftConstraintDto();
			teamConstraint.setConstraintName(RuleName.TEAM_ASSOCIATION_CONSTRAINT);
			teamConstraint.setRequiredTeamId(shift.getTeamId());
			teamConstraint.setInvolvedShifts(Arrays.asList(shift.getShiftId()));
			teamConstraint.setWeight(weight);
			shiftConstraintQualificationMap.get(shift).add(teamConstraint);
		}
		addHardConstraintMatch(kcontext, SolverConstants.HARD_CONSTRAINT_LEVEL, weight);
	}
	
	public void addSkillHardConstraintMatch(RuleContext kcontext, final int weight, Skill requiredSkill, ShiftAssignment shift) {
		// Adds constraint information iff there are shifts being qualified
		// during optimization the shiftQualificationMap is always empty
		if (shiftConstraintQualificationMap.containsKey(shift)) {
			SkillShiftConstraintDto teamConstraint = new SkillShiftConstraintDto();
			teamConstraint.setConstraintName(RuleName.SKILL_MATCH_RULE);
			teamConstraint.setWeight(weight);
			teamConstraint.setRequiredSkillId(requiredSkill.getCode());
			teamConstraint.setInvolvedShifts(Arrays.asList(shift.getShiftId()));
			shiftConstraintQualificationMap.get(shift).add(teamConstraint);
		}
		addHardConstraintMatch(kcontext, SolverConstants.HARD_CONSTRAINT_LEVEL, weight);
	}

	public void addHardConstraintMatch(RuleContext kcontext, final int weight, CITimeWindow ciTimeOff,
			ShiftAssignment shift) {
		// Adds constraint information iff there are shifts being qualified
		// during optimization the shiftQualificationMap is always empty
		if (shiftConstraintQualificationMap.containsKey(shift)) {
			CITimeOffShiftConstraintDto ciShiftConstraint = new CITimeOffShiftConstraintDto();
			ciShiftConstraint.setConstraintName(RuleName.CI_TIME_OFF_CONSTRAINT);
			ciShiftConstraint.setWeight(weight);
			ciShiftConstraint.setInvolvedShifts(Arrays.asList(shift.getShift().getId()));
			ciShiftConstraint.setTimeOff(convertCITimeOffToDto(ciTimeOff));
			shiftConstraintQualificationMap.get(shift).add(ciShiftConstraint);
		}
		addHardConstraintMatch(kcontext, SolverConstants.HARD_CONSTRAINT_LEVEL, weight);
	}

	public void addHardConstraintMatch(RuleContext kcontext, final int weight, CDTimeWindow cdTimeOff,
			ShiftAssignment shift) {
		// Adds constraint information iff there are shifts being qualified
		// during optimization the shiftQualificationMap is always empty
		if (shiftConstraintQualificationMap.containsKey(shift)) {
			CDTimeOffShiftConstraintDto ciShiftConstraint = new CDTimeOffShiftConstraintDto();
			ciShiftConstraint.setConstraintName(RuleName.CD_TIME_OFF_CONSTRAINT);
			ciShiftConstraint.setWeight(weight);
			ciShiftConstraint.setInvolvedShifts(Arrays.asList(shift.getShift().getId()));
			ciShiftConstraint.setTimeOff(convertCDTimeOffToDto(cdTimeOff));
			shiftConstraintQualificationMap.get(shift).add(ciShiftConstraint);
		}

		addHardConstraintMatch(kcontext, SolverConstants.HARD_CONSTRAINT_LEVEL, weight);
	}

	public void addMinHardConstraintMatch(RuleContext kcontext, final int weight, int actualValue, MinMaxContractLine contractLine,
			List<ShiftAssignment> involvedShiftAssignments) {
		if (!shiftConstraintQualificationMap.isEmpty()) {
			Collection<String> involvedShiftIds =  extractShiftIdsFromAssignments(involvedShiftAssignments);
			// Adds constraint information iff there are shifts being qualified
			// during optimization the shiftQualificationMap is always empty
			for (ShiftAssignment matchingShift : involvedShiftAssignments) {
				if(shiftConstraintQualificationMap.containsKey(matchingShift)){
					MinShiftConstraintDto minShiftConstraint = new MinShiftConstraintDto();
					minShiftConstraint.setConstraintName(RuleName.fromContractLineType(contractLine.getContractLineType(), false));
					minShiftConstraint.setWeight(weight);
					minShiftConstraint.setInvolvedShifts(involvedShiftIds);
					minShiftConstraint.setActualValue(actualValue);
					minShiftConstraint.setMinValue(contractLine.getMinimumValue());
					shiftConstraintQualificationMap.get(matchingShift).add(minShiftConstraint);
				}
			}
		}
		addHardConstraintMatch(kcontext, SolverConstants.HARD_CONSTRAINT_LEVEL, weight);
	}
	
	public void addMinMediumConstraintMatch(RuleContext kcontext, final int weight, int actualValue, MinMaxContractLine contractLine,
			List<ShiftAssignment> involvedShiftAssignments) {
		if (!shiftConstraintQualificationMap.isEmpty()) {
			Collection<String> involvedShiftIds =  extractShiftIdsFromAssignments(involvedShiftAssignments);
			// Adds constraint information iff there are shifts being qualified
			// during optimization the shiftQualificationMap is always empty
			for (ShiftAssignment matchingShift : involvedShiftAssignments) {
				if(shiftConstraintQualificationMap.containsKey(matchingShift)){
					MinShiftConstraintDto minShiftConstraint = new MinShiftConstraintDto();
					minShiftConstraint.setConstraintName(RuleName.fromContractLineType(contractLine.getContractLineType(), false));
					minShiftConstraint.setWeight(weight);
					minShiftConstraint.setInvolvedShifts(involvedShiftIds);
					minShiftConstraint.setActualValue(actualValue);
					minShiftConstraint.setMinValue(contractLine.getMinimumValue());
					shiftConstraintQualificationMap.get(matchingShift).add(minShiftConstraint);
				}
			}
		}
		addHardConstraintMatch(kcontext, SolverConstants.MED_CONSTRAINT_LEVEL, weight);
	}
	
	public void addMaxHardConstraintMatch(RuleContext kcontext, final int weight, int actualValue, MinMaxContractLine contractLine,
			List<ShiftAssignment> involvedShiftAssignments) {
		if (!shiftConstraintQualificationMap.isEmpty()) {
			Collection<String> involvedShiftIds =  extractShiftIdsFromAssignments(involvedShiftAssignments);
			// Adds constraint information iff there are shifts being qualified
			// during optimization the shiftQualificationMap is always empty
			for (ShiftAssignment matchingShift : involvedShiftAssignments) {
				if(shiftConstraintQualificationMap.containsKey(matchingShift)){
					MaxShiftConstraintDto maxShiftConstraint = new MaxShiftConstraintDto();
					maxShiftConstraint.setConstraintName(RuleName.fromContractLineType(contractLine.getContractLineType(), true));
					maxShiftConstraint.setWeight(weight);
					maxShiftConstraint.setInvolvedShifts(involvedShiftIds);
					maxShiftConstraint.setActualValue(actualValue);
					maxShiftConstraint.setMaxValue(contractLine.getMaximumValue());
					shiftConstraintQualificationMap.get(matchingShift).add(maxShiftConstraint);
				}
			}
		}
		
		addHardConstraintMatch(kcontext, SolverConstants.HARD_CONSTRAINT_LEVEL, weight);
	}
	
	public void addMediumConstraintMatch(RuleContext kcontext, final int weight) {
		addHardConstraintMatch(kcontext, SolverConstants.MED_CONSTRAINT_LEVEL, weight);
	}

	public Map<ShiftAssignment, List<ShiftConstraintDto>> getShiftConstraintQualificationMap() {
		return shiftConstraintQualificationMap;
	}

	public void setShiftConstraintQualificationMap(
			Map<ShiftAssignment, List<ShiftConstraintDto>> shiftConstraintQualificationMap) {
		this.shiftConstraintQualificationMap = shiftConstraintQualificationMap;
	}
	
	private Collection<String> extractShiftIdsFromAssignments(Collection<ShiftAssignment> shiftAssignments){
		Collection<String> involvedShiftAssignmentIds = new ArrayList<>();
		for(ShiftAssignment shift : shiftAssignments){
			involvedShiftAssignmentIds.add(shift.getShift().getId());
		}
		return involvedShiftAssignmentIds;
	}

	private CITimeOffDto convertCITimeOffToDto(CITimeWindow ciTimeOff) {
		CITimeOffDto dto = new CITimeOffDto();
		dto.setDayOfWeek(ciTimeOff.getDayOfWeek());
		dto.setEmployeeId(ciTimeOff.getEmployeeId());
		dto.setEndTime(ciTimeOff.getEndTime());
		dto.setStartTime(ciTimeOff.getStartTime());
		dto.setPTO(ciTimeOff.isPTO());
		dto.setAllDay(ciTimeOff.isAllDay());
		dto.setWeight(ciTimeOff.getWeight());
		return dto;
	}

	private CDTimeOffDto convertCDTimeOffToDto(CDTimeWindow cdTimeOff) {
		CDTimeOffDto dto = new CDTimeOffDto();
		dto.setEmployeeId(cdTimeOff.getEmployeeId());
		dto.setDayOffStart(cdTimeOff.getDayOffStart());
		dto.setDayOffEnd(cdTimeOff.getDayOffEnd());
		dto.setEndTime(cdTimeOff.getEndTime());
		dto.setStartTime(cdTimeOff.getStartTime());
		dto.setPTO(cdTimeOff.isPTO());
		dto.setAllDay(cdTimeOff.isAllDay());
		dto.setWeight(cdTimeOff.getWeight());
		return dto;
	}

}
