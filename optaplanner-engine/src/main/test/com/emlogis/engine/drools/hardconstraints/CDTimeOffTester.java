package com.emlogis.engine.drools.hardconstraints;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.kie.api.runtime.rule.FactHandle;
import org.optaplanner.core.api.score.buildin.bendable.BendableScoreHolder;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;

import com.emlogis.engine.domain.Employee;
import com.emlogis.engine.domain.Shift;
import com.emlogis.engine.domain.ShiftAssignment;
import com.emlogis.engine.domain.ShiftDate;
import com.emlogis.engine.domain.ShiftType;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.domain.timeoff.CDTimeWindow;
import com.emlogis.engine.drools.ConstraintTesterBase;

public class CDTimeOffTester extends ConstraintTesterBase {
	
	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 27));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
	}
	
	@Test
	public void testRuleWeightMultiplier() {
		// Change the weight multiplier of the rule
		int ruleMultiplier = 4;
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.putRuleWeightMultiplier(RuleName.CD_TIME_OFF_CONSTRAINT , ruleMultiplier);
		kSession.update(rosterHandle, rosterInfo);
		
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 27), -1);
		kSession.insert(timeOff);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CD_TIME_OFF_CONSTRAINT ,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(ruleMultiplier * timeOff.getWeight(), numOfMaxPerWeekConstraints);
	}

	@Test
	public void testCDOneDayInPlanningWindowViolated() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 27), -1);
		kSession.insert(timeOff);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CD_TIME_OFF_CONSTRAINT ,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(timeOff.getWeight(), numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testCDOneDayShiftOverlapsViolated() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 28), -1);
		kSession.insert(timeOff);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(shiftDate);
		ShiftDate secondDay = shiftDate.plusDays(2);
		kSession.insert(secondDay);

		Shift shift = createShift("1", 0, shiftDate, secondDay, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CD_TIME_OFF_CONSTRAINT ,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(timeOff.getWeight(), numOfMaxPerWeekConstraints);
	}
	


	
	@Test
	public void testCDOneDayOutsideSchedule() {
		//Date is outside both planning and schedule start dates
		LocalDate scheduledDate = new LocalDate(2014, 5, 24);

		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), scheduledDate, -1);
		kSession.insert(timeOff);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(scheduledDate);
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CD_TIME_OFF_CONSTRAINT ,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testCDOneDayOutsideOfDayOff() {
		//Date is outside both planning and schedule start dates
		LocalDate scheduledDate = new LocalDate(2014, 5, 24);

		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), scheduledDate, -1);
		kSession.insert(timeOff);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(scheduledDate.plusDays(1));
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CD_TIME_OFF_CONSTRAINT ,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}

	
	@Test
	public void testCDTwoDayViolatedByTwoShifts() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 27),  new LocalDate(2014, 5, 28), -1);
		kSession.insert(timeOff);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		ShiftDate shiftDateSecond = new ShiftDate(new LocalDate(2014, 5, 28));
		kSession.insert(shiftDateSecond);

		Shift shiftSecond = createShift("2", 0, shiftDateSecond, shiftType, 1);
		kSession.insert(shiftSecond);

		ShiftAssignment shiftAssignmentSecond = createShiftAssignment(employee, 0, shiftSecond);
		kSession.insert(shiftAssignmentSecond);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CD_TIME_OFF_CONSTRAINT ,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(2*timeOff.getWeight(), numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testCDThreeDayViolatedByMiddleShift() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 27),  new LocalDate(2014, 5, 29), -1);
		kSession.insert(timeOff);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CD_TIME_OFF_CONSTRAINT ,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(timeOff.getWeight(), numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testCDTwoDayViolatedByFirstDay() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 27),  new LocalDate(2014, 5, 28), -1);
		kSession.insert(timeOff);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CD_TIME_OFF_CONSTRAINT ,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(timeOff.getWeight(), numOfMaxPerWeekConstraints);
	}
	
	public void testCDTwoDayViolatedBySecondDay() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 27),  new LocalDate(2014, 5, 28), -1);
		kSession.insert(timeOff);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
		kSession.insert(shiftDate);
 
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CD_TIME_OFF_CONSTRAINT ,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(timeOff.getWeight(), numOfMaxPerWeekConstraints);
	}

}
