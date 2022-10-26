package com.emlogis.engine.domain.contract.contractline;

public enum ContractLineType {
  //  SINGLE_ASSIGNMENT_PER_DAY,
   // TOTAL_ASSIGNMENTS,
    CONSECUTIVE_WORKING_DAYS("ConsecutiveDays"),
    CONSECUTIVE_TWELVE_HOUR_DAYS("TwelveHourDays"),
    //CONSECUTIVE_FREE_DAYS,
    //CONSECUTIVE_WORKING_WEEKENDS,
    //TOTAL_WORKING_WEEKENDS_IN_FOUR_WEEKS,
    COMPLETE_WEEKENDS("CompleteWeekends"),
    HOURS_BETWEEN_DAYS("HoursBetweenDays"),
    HOURS_PER_DAY("HoursDay"),
    HOURS_PER_WEEK("HoursWeek"),
    HOURS_PER_TWO_WEEKS("HoursBiWeekly"),
    HOURS_PER_WEEK_PRIME_SKILL("HoursWeekPrimarySkill"),
    DAYS_PER_WEEK("DaysWeek"),
    DAILY_OVERTIME("DailyOvertimeStart"),
    WEEKLY_OVERTIME("WeeklyOvertimeStart"),
    TWO_WEEK_OVERTIME("TwoWeekOvertimeSTart"),
    OVERLAPPING_SHIFTS("ShiftOverlap"),
    CUSTOM("");
    //IDENTICAL_SHIFT_TYPES_DURING_WEEKEND,
    //NO_NIGHT_SHIFT_BEFORE_FREE_WEEKEND,
    //ALTERNATIVE_SKILL_CATEGORY
    
    private String value;
    
    private ContractLineType(String value){
    	this.value = value;
    }
    
    public static ContractLineType fromString(String text) {
        if (text != null) {
          for (ContractLineType b : ContractLineType.values()) {
            if (text.equalsIgnoreCase(b.value)) {
              return b;
            }
          }
        }
        return CUSTOM;
      }
    
    public String getValue(){
    	return value;
    }
}
