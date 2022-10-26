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
import com.emlogis.engine.drools.ConstraintTesterBase;

public class RequiredEmployeeSizeTester extends ConstraintTesterBase {
	
	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
		
	}
	
	@Test
	public void testShiftFilledOutsidePlanningWindow() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 27));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		kSession.update(rosterHandle, rosterInfo);
		
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(null, 0, shift);
		kSession.insert(shiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfUnfilledShiftConstraints = getNumOfConstraintMatches(RuleName.REQUIRED_EMPLOYEES_MATCH_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfUnfilledShiftConstraints);
	}
	
	@Test
	public void testShiftFilledOutsideScheduleWindow() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.setPlanningWindowStart(new ShiftDate(new LocalDate(2014, 5, 27)));
		kSession.update(rosterHandle, rosterInfo);
		
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 18));
		kSession.insert(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(null, 0, shift);
		kSession.insert(shiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfUnfilledShiftConstraints = getNumOfConstraintMatches(RuleName.REQUIRED_EMPLOYEES_MATCH_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfUnfilledShiftConstraints);
	}
	
	@Test
	public void testShiftAssignmentUnfilled() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 3);
		kSession.insert(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(null, 0, shift);
		kSession.insert(shiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfUnfilledShiftConstraints = getNumOfConstraintMatches(RuleName.REQUIRED_EMPLOYEES_MATCH_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(-1 * shiftAssignment.getShift().getRequiredEmployeeSize(), numOfUnfilledShiftConstraints);
	}
	
	@Test
	public void testRuleWeightMultiplier() {
		// Change the weight multiplier of the rule
		int ruleMultiplier = 4;
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.putRuleWeightMultiplier(RuleName.REQUIRED_EMPLOYEES_MATCH_RULE, ruleMultiplier);
		kSession.update(rosterHandle, rosterInfo);
				
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 3);
		kSession.insert(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(null, 0, shift);
		kSession.insert(shiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfUnfilledShiftConstraints = getNumOfConstraintMatches(RuleName.REQUIRED_EMPLOYEES_MATCH_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(-1 * ruleMultiplier * shiftAssignment.getShift().getRequiredEmployeeSize(), numOfUnfilledShiftConstraints);
	}
	
	@Test
	public void testShiftAssignmentFilled() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfUnfilledShiftConstraints = getNumOfConstraintMatches(RuleName.REQUIRED_EMPLOYEES_MATCH_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfUnfilledShiftConstraints);
	}
}
