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
import com.emlogis.engine.domain.contract.Contract;
import com.emlogis.engine.domain.contract.contractline.ContractLine;
import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.drools.ConstraintTesterBase;

public class OverlappingShifts extends ConstraintTesterBase{
	
	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
	}
	
	@Test
	public void testOverlapThirtyViolated() {
		Employee employee = createEmployee(1, "MR.", "X");
		
		ContractLine line = createMaxContractLine(ContractLineType.OVERLAPPING_SHIFTS, true, 30, -1);
		
		Contract contract = createContract(false, employee, line);
		kSession.insert(line);
		kSession.insert(contract);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		ShiftType secondShiftType = createShiftType(2, new LocalTime(21, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		Shift secondShift = createShift("2", 0, shiftDate, secondShiftType, 1);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		
		kSession.insert(shiftType);
		kSession.insert(secondShiftType);
		kSession.insert(shiftDate);
		kSession.insert(secondShift);
		kSession.insert(secondShiftAssignment);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.OVERLAPPING_SHIFTS_RULE, scoreHolder.getConstraintMatchTotals());
		assertEquals(-10, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testOverlapThirtyHonored() {
		Employee employee = createEmployee(1, "MR.", "X");
		
		ContractLine line = createMaxContractLine(ContractLineType.OVERLAPPING_SHIFTS, true, 30, -1);
		
		Contract contract = createContract(false, employee, line);
		kSession.insert(line);
		kSession.insert(contract);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		ShiftType secondShiftType = createShiftType(2, new LocalTime(21, 31, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		Shift secondShift = createShift("2", 0, shiftDate, secondShiftType, 1);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		
		kSession.insert(shiftType);
		kSession.insert(secondShiftType);
		kSession.insert(shiftDate);
		kSession.insert(secondShift);
		kSession.insert(secondShiftAssignment);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.OVERLAPPING_SHIFTS_RULE, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}
	
	
	@Test
	public void testOverlapViolated() {
		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee);
		kSession.insert(contract);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		ShiftType secondShiftType = createShiftType(2, new LocalTime(21, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		Shift secondShift = createShift("2", 0, shiftDate, secondShiftType, 1);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		
		kSession.insert(shiftType);
		kSession.insert(secondShiftType);
		kSession.insert(shiftDate);
		kSession.insert(secondShift);
		kSession.insert(secondShiftAssignment);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.OVERLAPPING_SHIFTS_RULE, scoreHolder.getConstraintMatchTotals());
		assertEquals(-10, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testSameStartDateAndTimeViolated() {
		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 25));
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		
		kSession.insert(shiftType);
		kSession.insert(shiftDate);
		kSession.insert(secondShiftDate);
		kSession.insert(secondShift);
		kSession.insert(secondShiftAssignment);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.OVERLAPPING_SHIFTS_RULE, scoreHolder.getConstraintMatchTotals());
		assertEquals(-10 * 2, numOfMinPerWeekConstraints); // Shifts overlaps both ways hence there are two constraint violations
	}
	
	@Test
	public void testSameStartDateNoOverlaps() {
		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee);
		kSession.insert(contract);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(15, 0, 0), false);
		ShiftType secondShiftType = createShiftType(2, new LocalTime(16, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		Shift secondShift = createShift("2", 0, shiftDate, secondShiftType, 1);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		
		kSession.insert(shiftType);
		kSession.insert(secondShiftType);
		kSession.insert(shiftDate);
		kSession.insert(secondShift);
		kSession.insert(secondShiftAssignment);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.OVERLAPPING_SHIFTS_RULE, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testDifferentStartDateNoOverlaps() {
		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee);
		kSession.insert(contract);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(15, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		kSession.insert(secondShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		
		kSession.insert(shiftType);
		kSession.insert(shiftDate);
		kSession.insert(secondShift);
		kSession.insert(secondShiftAssignment);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.OVERLAPPING_SHIFTS_RULE, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}
	
}
