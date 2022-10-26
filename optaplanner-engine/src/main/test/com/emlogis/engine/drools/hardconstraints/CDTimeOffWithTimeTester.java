package com.emlogis.engine.drools.hardconstraints;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;
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

public class CDTimeOffWithTimeTester extends ConstraintTesterBase {
	
	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 27));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
	}

	@Test
	public void testCDOneDayInPlanningWindowInTimeWindowViolated() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 27), 
				new LocalTime(10, 0, 0), new LocalTime(20, 0, 0), -1);
		kSession.insert(timeOff);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(19, 0, 0), false);
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
	public void testCDOneDayInPlanningWindowCrossTWStartViolated() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 27), 
				new LocalTime(10, 0, 0), new LocalTime(20, 0, 0), -1);
		kSession.insert(timeOff);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(8, 0, 0), new LocalTime(12, 0, 0), false);
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
	public void testCDOneDayInPlanningWindowCrossTWSEndViolated() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 27), 
				new LocalTime(10, 0, 0), new LocalTime(20, 0, 0), -1);
		kSession.insert(timeOff);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(15, 0, 0), new LocalTime(21, 0, 0), false);
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
	public void testCDOneDayShiftStartsOnTimeWindowEnd() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 27), 
				new LocalTime(10, 0, 0), new LocalTime(20, 0, 0), -1);
		kSession.insert(timeOff);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(20, 0, 0), new LocalTime(23, 0, 0), false);
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
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testCDOneDayShiftEndsOnTimeWindowStart() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 27), 
				new LocalTime(10, 0, 0), new LocalTime(16, 0, 0), -1);
		kSession.insert(timeOff);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(16, 0, 0), new LocalTime(23, 0, 0), false);
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
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testCDOneDayOutsideTimeWindow() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 27), 
				new LocalTime(10, 0, 0), new LocalTime(20, 0, 0), -1);
		kSession.insert(timeOff);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(7, 0, 0), new LocalTime(9, 0, 0), false);
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
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
}
