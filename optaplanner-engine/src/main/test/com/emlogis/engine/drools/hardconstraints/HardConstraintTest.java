package com.emlogis.engine.drools.hardconstraints;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ MinHoursBetweenDays.class, MinHoursWeekTester.class, MaxHoursWeekTester.class,
		WeekdayRotationTester.class,
		WeekendConstraintsTester.class, SkillsMatchConstraintTester.class,
		CDTimeOffTester.class, CDTimeOffWithTimeTester.class, CITimeOffTester.class,
		MinHoursPerDayTester.class, MaxDaysWeekTester.class, RequiredEmployeeSizeTester.class,
		TeamAssociationTester.class, MinHoursPrimeSkillWeekTester.class, ConsecutiveDaysTester.class,
		MaxHoursPerDayTester.class, ConstraintOverrideTester.class, OverlappingShifts.class, OvertimeConstraintTester.class,
		Consecutive12HourDaysTester.class})
public class HardConstraintTest {

}
