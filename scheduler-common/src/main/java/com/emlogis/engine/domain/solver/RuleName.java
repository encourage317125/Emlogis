package com.emlogis.engine.domain.solver;

import com.emlogis.engine.domain.contract.contractline.ContractLineType;

public enum RuleName {
	
	//Scoring rules
	CD_PREFERENCE_RULE("cdPrefRule"),
	CI_PREFERENCE_RULE("ciPrefRule"),
	SCHEDULE_COST_RULE("ScheduleCost"),
	SCHEDULE_OVERTIME_RULE("ScheduleOvertime"),
	SENIORITY_RULE("SeniorityRanking"),
	EXTRA_SHIFT_RULE("ExtraShiftDeduction"),
	OPEN_SHIFT_SEPARATION_RULE("OpenShiftSeparation"),
	EXCESS_SHIFT_SEPARATION_RULE("ExcessShiftSeparation"),
	HORIZONTAL_CLUSTERING_RULE("HorizontalClustering"),
	VERTICAL_CLUSTERING_RULE("VerticalClustering"),
	WORKED_WEEKENDS_SEPARATION_RULE("WorkedWeekendsSeperation"),
	PREFER_PRIMARY_SKILL_RULE("PreferPrimarySkill"),
	TEAM_PREFERENCE_RULE("TeamPreference"),
	PREFER_TEAM_SCATTERING_RULE("PreferTeamScattering"),
	AVOID_SKILL_CHANGE_RULE("AvoidSkillChange"),
	AVOID_TEAM_CHANGE_RULE("AvoidTeamChange"),


	
	// Constraint Rules
	 CD_TIME_OFF_CONSTRAINT ("cdTimeOffRule"),
	 CI_TIME_OFF_CONSTRAINT("ciTimeOffRule"),
	 CD_PREF_CONSTRAINT ("cdPrefRule"),
	 CI_PREF_CONSTRAINT("ciPrefRule"),
	 MAX_CONSECUTIVE_DAYS_CONSTRAINT("maxConsecutiveDays"),
	 MAX_CONSECUTIVE_12H_DAYS_CONSTRAINT("maxConsecutive12HourDays"),
	 MAX_DAYS_PER_WEEK_CONSTRAINT("maxDaysPerWeek"),
	 MAX_HOURS_PER_DAY_CONSTRAINT("maxHoursPerDay"),
	 MAX_HOURS_PER_WEEK_CONSTRAINT("maxHoursPerWeek"),
	 MIN_HOURS_BETWEEN_DAYS_RULE("minHoursBetweenDays"),
	 MIN_HOURS_PER_DAY_CONSTRAINT("minHoursPerDay"),
	 MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT("minHoursPrimeSkillPerWeek"),
	 MIN_HOURS_PER_WEEK_CONSTRAINT("minHoursPerWeek"),
	 MIN_HOURS_PER_TWO_WEEKS_CONSTRAINT("minHoursPerTwoWeeks"),
	 REQUIRED_EMPLOYEES_MATCH_RULE("requiredEmployeeSizePerShift"),
	 SKILL_MATCH_RULE("skillsMatch"),
	 TEAM_ASSOCIATION_CONSTRAINT("correctTeamAssociation"),
	 WEEKDAY_ROTATION_PATTERN_RULE("weekdayRotationPattern"),
	 COUPLED_WEEKEND_RULE("coupledWeekendPattern"),
	 DAYS_OFF_AFTER_WEEKEND_RULE("daysOffAfterWeekend"),
	 DAYS_OFF_BEFORE_WEEKEND_RULE("daysOffBeforeWeekend"),
	 TEAM_ASSOCIATION_CONSTRAINT_FLOAT("correctTeamAssociationFloat"),
	 AVOID_DAILY_OVERTIME_RULE("avoidDailyOvertimeConstraint"),
	 AVOID_WEEKLY_OVERTIME_RULE("avoidWeeklyOvertimeConstraint"),
	 AVOID_TWO_WEEK_OVERTIME_RULE("avoidTwoWeekOvertimeConstraint"),
	 OVERLAPPING_SHIFTS_RULE("overlappingShifts");


	private String value;

	private RuleName(String name) {
		this.value = name;
	}

	public static RuleName fromString(String text) {
		if (text != null) {
			for (RuleName b : values()) {
				if (text.equalsIgnoreCase(b.value)) {
					return b;
				}
			}
		}
		return null;
	}
	
	public String getValue(){
		return value;
	}
	
	public static RuleName fromContractLineType(ContractLineType type, boolean isMaxContractLine){
		switch(type){
		case HOURS_PER_WEEK:
			if(isMaxContractLine)
				return MAX_HOURS_PER_WEEK_CONSTRAINT;
			return MIN_HOURS_PER_WEEK_CONSTRAINT;
		case HOURS_PER_TWO_WEEKS:
			return MIN_HOURS_PER_TWO_WEEKS_CONSTRAINT;
		case HOURS_PER_WEEK_PRIME_SKILL:
			return MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT;
		case HOURS_PER_DAY:
			if(isMaxContractLine)
				return MAX_HOURS_PER_DAY_CONSTRAINT;
			return MIN_HOURS_PER_DAY_CONSTRAINT;
		case HOURS_BETWEEN_DAYS:
			return MIN_HOURS_BETWEEN_DAYS_RULE;
		case DAYS_PER_WEEK:
			return MAX_DAYS_PER_WEEK_CONSTRAINT;
		case CONSECUTIVE_WORKING_DAYS:
			return MAX_CONSECUTIVE_DAYS_CONSTRAINT;
		case CONSECUTIVE_TWELVE_HOUR_DAYS:
			return MAX_CONSECUTIVE_12H_DAYS_CONSTRAINT;
		case OVERLAPPING_SHIFTS:
			return OVERLAPPING_SHIFTS_RULE;
		case DAILY_OVERTIME:
			return AVOID_DAILY_OVERTIME_RULE;
		case WEEKLY_OVERTIME:
			return AVOID_WEEKLY_OVERTIME_RULE;
		case TWO_WEEK_OVERTIME:
			return AVOID_TWO_WEEK_OVERTIME_RULE;
		}
		return null;
	}
}
