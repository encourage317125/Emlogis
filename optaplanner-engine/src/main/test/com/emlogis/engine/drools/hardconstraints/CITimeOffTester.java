package com.emlogis.engine.drools.hardconstraints;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.kie.api.runtime.rule.FactHandle;
import org.optaplanner.core.api.score.buildin.bendable.BendableScoreHolder;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.Employee;
import com.emlogis.engine.domain.Shift;
import com.emlogis.engine.domain.ShiftAssignment;
import com.emlogis.engine.domain.ShiftDate;
import com.emlogis.engine.domain.ShiftType;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.domain.timeoff.CDOverrideAvailDate;
import com.emlogis.engine.domain.timeoff.CDTimeWindow;
import com.emlogis.engine.domain.timeoff.CITimeWindow;
import com.emlogis.engine.domain.timeoff.TimeWindow;
import com.emlogis.engine.drools.ConstraintTesterBase;

public class CITimeOffTester extends ConstraintTesterBase {

    @Test
    public void testRuleWeightMultiplier() {
        // Change the weight multiplier of the rule
        int ruleMultiplier = 4;
        FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
        rosterInfo.putRuleWeightMultiplier(RuleName.CI_TIME_OFF_CONSTRAINT, ruleMultiplier);
        kSession.update(rosterHandle, rosterInfo);

        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.MONDAY, -1);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(ruleMultiplier * timeOff.getWeight(), numOfMaxPerWeekConstraints);
    }

    @Test
    public void testCIOneDayInPlanningWindowViolated() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.MONDAY, -1);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(timeOff.getWeight(), numOfMaxPerWeekConstraints);
    }
    
    @Test
    public void testCIOViolatedInEffectiveRange() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.MONDAY, -1);
        timeOff.setEffectiveStart(new DateTime(2014, 5, 25, 0, 0));
        timeOff.setEffectiveEnd(new DateTime(2014, 5, 28, 0, 0));
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(timeOff.getWeight(), numOfMaxPerWeekConstraints);
    }
    
    @Test
    public void testCIOViolatedAllDayCI() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.MONDAY, -1);
        timeOff.setTimeWindow(new LocalTime(0, 0, 0), TimeWindow.END_OF_DAY_TIME);
        timeOff.setAllDay(true);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(timeOff.getWeight(), numOfMaxPerWeekConstraints);
    }
    
    @Test
    public void testCIOHonoredElevenToMidnight() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.MONDAY, -1);
        timeOff.setTimeWindow(new LocalTime(23, 0, 0), TimeWindow.END_OF_DAY_TIME);
        timeOff.setAllDay(false);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfMaxPerWeekConstraints);
    }
    
    @Test
    public void testCIOViolatedEightToMidnight() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.MONDAY, -1);
        timeOff.setTimeWindow(new LocalTime(20, 0, 0), TimeWindow.END_OF_DAY_TIME);
        timeOff.setAllDay(false);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(timeOff.getWeight(), numOfMaxPerWeekConstraints);
    }
    

    @Test
    public void testCIOViolatedBothToMidnight() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.MONDAY, -1);
        timeOff.setTimeWindow(new LocalTime(20, 0, 0), TimeWindow.END_OF_DAY_TIME);
        timeOff.setAllDay(false);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), TimeWindow.END_OF_DAY_TIME, false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(timeOff.getWeight(), numOfMaxPerWeekConstraints);
    }
    
    
    
    @Test
    public void testCIViolatedBeforeEffectiveStart() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.MONDAY, -1);
        timeOff.setEffectiveStart(new DateTime(2014, 5, 27, 0, 0));
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfMaxPerWeekConstraints);
    }
    
    @Test
    public void testCIViolatedAfterEffectiveEnd() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.MONDAY, -1);
        timeOff.setEffectiveEnd(new DateTime(2014, 5, 25, 0, 0));
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfMaxPerWeekConstraints);
    }
    
    @Test
    public void testCIOneDayOverriddenByCD() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.MONDAY, -1);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);
        
        
        CDTimeWindow cdTimeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 26), -1);
        kSession.insert(cdTimeOff);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int ciTimeOffConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(0, ciTimeOffConstraints);
        
        int cdTimeOffConstraints = getNumOfConstraintMatches(RuleName.CD_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(-1, cdTimeOffConstraints);
    }
    
    @Test
    public void testCIOneDayOverriddenByCDOverride() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);
        
        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.MONDAY, -1);
        timeOff.setCdOverrides(Arrays.asList(new CDOverrideAvailDate(shiftDate.getDate())));
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftType);


        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);
        
        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int ciTimeOffConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(0, ciTimeOffConstraints);
    }
    
    @Test
    public void testCIOneDayMissedByCDOverride() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);
        
        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.MONDAY, -1);
        timeOff.setCdOverrides(Arrays.asList(new CDOverrideAvailDate(shiftDate.getDate().plusDays(1))));
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftType);


        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);
        
        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int ciTimeOffConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(-1, ciTimeOffConstraints);
    }
    
    @Test
    public void testCIOneDayAvoidedByCD() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.MONDAY, -1);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);
        
        CDTimeWindow cdTimeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2015, 02, 17), -1);
        kSession.insert(cdTimeOff);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(timeOff.getWeight(), numOfMaxPerWeekConstraints);
    }

    @Test
    public void testCIOneDayTimeWindowMissed() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), new LocalTime(18, 0, 0), new LocalTime(22, 0, 0),
                DayOfWeek.MONDAY, -1);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(17, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfMaxPerWeekConstraints);
    }

    @Test
    public void testCIOneDayTimeWindowStartViolated() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), new LocalTime(18, 0, 0), new LocalTime(22, 0, 0),
                DayOfWeek.MONDAY, -1);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(18, 1, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(timeOff.getWeight(), numOfMaxPerWeekConstraints);
    }
    
    @Test
    public void testCIOneDayTimeWindowStartHonored() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), new LocalTime(18, 0, 0), new LocalTime(22, 0, 0),
                DayOfWeek.MONDAY, -1);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(18, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfMaxPerWeekConstraints);
    }

    @Test
    public void testCIOneDayTimeWindowEndViolated() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), new LocalTime(18, 0, 0), new LocalTime(22, 0, 0),
                DayOfWeek.MONDAY, -1);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(21, 59, 59), new LocalTime(23, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(timeOff.getWeight(), numOfMaxPerWeekConstraints);
    }
    
    @Test
    public void testCIOneDayTimeWindowEndHonored() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), new LocalTime(18, 0, 0), new LocalTime(22, 0, 0),
                DayOfWeek.MONDAY, -1);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(23, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfMaxPerWeekConstraints);
    }

    @Test
    public void testCIOneDayViolatedByOverlappingShift() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), new LocalTime(18, 0, 0), new LocalTime(20, 0, 0),
                DayOfWeek.MONDAY, -1);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(17, 0, 0), new LocalTime(23, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(timeOff.getWeight(), numOfMaxPerWeekConstraints);
    }

    @Test
    public void testCIOneDayOutsidePlanningWindow() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.SATURDAY, -1);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 24)); // Saturday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfMaxPerWeekConstraints);
    }

    @Test
    public void testCIOneDayCrossTimeWindowStart() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), new LocalTime(10, 0, 0), new LocalTime(22, 0, 0),
                DayOfWeek.SATURDAY, -1);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 24)); // Saturday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfMaxPerWeekConstraints);
    }
    
    @Test
    public void testCIShiftStartsOnCIEnd() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), new LocalTime(10, 0, 0), new LocalTime(16, 0, 0),
                DayOfWeek.SATURDAY, -1);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(16, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 24)); // Saturday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfMaxPerWeekConstraints);
    }
    
    @Test
    public void testCIShiftEndsOnCIStart() {
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), new LocalTime(16, 0, 0), new LocalTime(23, 0, 0),
                DayOfWeek.SATURDAY, -1);
        kSession.insert(timeOff);

        ShiftType shiftType = createShiftType(1, new LocalTime(5, 0, 0), new LocalTime(16, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 24)); // Saturday
        // Shift
        kSession.insert(shiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
                .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
                scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfMaxPerWeekConstraints);
    }

    @Override
    protected void loadRosterInfo() {
        ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
        rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
        rosterInfo.setPlanningWindowStart(planningWindowStart);
        rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
        kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
    }

}
