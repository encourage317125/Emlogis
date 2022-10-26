package com.emlogis.engine.drools.scoringrules;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
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
import com.emlogis.engine.drools.ConstraintTesterBase;

public class SeniorityRuleTester extends ConstraintTesterBase {
	public final static String SENIORITY_CONSTRAINT_MATCH = "SeniorityRanking";

	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
	}

	@Test
	public void testSeniorityOneEmployee() {
		Employee employee = createEmployee(1, "MR.", "X", 2);
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
		int seniorityScore = getWeightedSumForConstraint(SENIORITY_CONSTRAINT_MATCH,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(employee.getSeniority(), seniorityScore);
	}

	@Test
	public void testSeniorityOneEmployeeTwoShifts() {
		Employee employee = createEmployee(1, "MR.", "X", 2);
		kSession.insert(employee);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		kSession.insert(secondShiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		kSession.insert(secondShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		kSession.insert(secondShiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int seniorityScore = getWeightedSumForConstraint(SENIORITY_CONSTRAINT_MATCH,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(employee.getSeniority() * 2, seniorityScore);
	}

	@Test
	public void testSeniorityTwoEmployees() {
		Employee employee = createEmployee(1, "MR.", "X", 2);
		kSession.insert(employee);

		Employee secondEmployee = createEmployee(2, "MR.", "Y", 4);
		kSession.insert(secondEmployee);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0,shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(secondEmployee, 1, shift);
		kSession.insert(secondShiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int seniorityScore = getWeightedSumForConstraint(SENIORITY_CONSTRAINT_MATCH,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(employee.getSeniority() + secondEmployee.getSeniority(), seniorityScore);
	}

	@Test
	public void testSeniorityTwoEmployeesOneShift() {
		Employee employee = createEmployee(1, "MR.", "X", 2);
		kSession.insert(employee);

		Employee secondEmployee = createEmployee(2, "MR.", "Y", 4);
		kSession.insert(secondEmployee);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(null, 0, shift);
		kSession.insert(shiftAssignment);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(secondEmployee, 1, shift);
		kSession.insert(secondShiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int seniorityScore = getWeightedSumForConstraint(SENIORITY_CONSTRAINT_MATCH,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(secondEmployee.getSeniority(), seniorityScore);
	}

	@Test
	public void testFindMostSeniorAndJuniorEmployee() {
		Employee oldestEmployee = createEmployee(1, "MR.", "X", -1);
		oldestEmployee.setStartDate(new DateTime(2014, 01, 27, 5, 2));
		kSession.insert(oldestEmployee);

		Employee juniorEmployee = createEmployee(2, "MR.", "Y", -1);
		juniorEmployee.setStartDate(new DateTime(2014, 01, 27, 8, 2));
		kSession.insert(juniorEmployee);

		kSession.fireAllRules();

		FactHandle employeeFactHandle = kSession.getFactHandle(oldestEmployee);
		Employee oldestEmp = (Employee) kSession.getObject(employeeFactHandle);
		
		FactHandle secondEmployeeFactHandle = kSession.getFactHandle(juniorEmployee);
		Employee youngestEmp = (Employee) kSession.getObject(secondEmployeeFactHandle);
		
		assertEquals(100, oldestEmp.getSeniority());
		assertEquals(1, youngestEmp.getSeniority());
	}
	
	@Test
	public void testFindMostSeniorMiddleJuniorEmployee() {
		Employee oldestEmployee = createEmployee(1, "MR.", "X", -1);
		oldestEmployee.setStartDate(new DateTime(2014, 01, 27, 5, 2));
		kSession.insert(oldestEmployee);
		
		Employee middleEmployee = createEmployee(2, "MR.", "M", -1);
		middleEmployee.setStartDate(new DateTime(2014, 05, 10, 5, 2));
		kSession.insert(middleEmployee);

		Employee juniorEmployee = createEmployee(3, "MR.", "Y", -1);
		juniorEmployee.setStartDate(new DateTime(2014, 8, 27, 5, 2));
		kSession.insert(juniorEmployee);

		kSession.fireAllRules();

		FactHandle employeeFactHandle = kSession.getFactHandle(oldestEmployee);
		Employee oldestEmp = (Employee) kSession.getObject(employeeFactHandle);
		
		FactHandle secondEmployeeFactHandle = kSession.getFactHandle(juniorEmployee);
		Employee youngestEmp = (Employee) kSession.getObject(secondEmployeeFactHandle);
		
		FactHandle middleEmployeeFactHandle = kSession.getFactHandle(middleEmployee);
		Employee midEmp = (Employee) kSession.getObject(middleEmployeeFactHandle);
		
		assertEquals(51, midEmp.getSeniority());
		assertEquals(100, oldestEmp.getSeniority());
		assertEquals(1, youngestEmp.getSeniority());
	}
	
	@Test
	public void testRankMultipleEmployees() {
		Employee oldestEmployee = createEmployee(1, "MR.", "X", -1);
		oldestEmployee.setStartDate(new DateTime(2014, 01, 27, 5, 2));
		kSession.insert(oldestEmployee);
		
		Employee middleEmployee = createEmployee(2, "MR.", "M", -1);
		middleEmployee.setStartDate(new DateTime(2014, 03, 29, 5, 2));
		kSession.insert(middleEmployee);
		
		Employee middleEmployee1 = createEmployee(4, "MR.", "M2", -1);
		middleEmployee1.setStartDate(new DateTime(2014, 07, 01, 5, 2));
		kSession.insert(middleEmployee1);

		Employee juniorEmployee = createEmployee(3, "MR.", "Y", -1);
		juniorEmployee.setStartDate(new DateTime(2014, 8, 27, 5, 2));
		kSession.insert(juniorEmployee);

		kSession.fireAllRules();

		FactHandle employeeFactHandle = kSession.getFactHandle(oldestEmployee);
		Employee oldestEmp = (Employee) kSession.getObject(employeeFactHandle);
		
		FactHandle secondEmployeeFactHandle = kSession.getFactHandle(juniorEmployee);
		Employee youngestEmp = (Employee) kSession.getObject(secondEmployeeFactHandle);
		
		FactHandle middleEmployeeFactHandle = kSession.getFactHandle(middleEmployee);
		Employee midEmp = (Employee) kSession.getObject(middleEmployeeFactHandle);
		
		FactHandle middleEmployeeFactHandle1 = kSession.getFactHandle(middleEmployee1);
		Employee midEmp1 = (Employee) kSession.getObject(middleEmployeeFactHandle1);
		
		assertEquals(71, midEmp.getSeniority());
		assertEquals(27, midEmp1.getSeniority());
		assertEquals(100, oldestEmp.getSeniority());
		assertEquals(1, youngestEmp.getSeniority());
	}
	
	
	@Test
	public void testFindTwoMostSeniorEmployees() {
		DateTime sameDateTime = new DateTime(2014, 01, 27, 5, 2);
		
		Employee oldestEmployee = createEmployee(1, "MR.", "X", -1);
		oldestEmployee.setStartDate(sameDateTime);
		kSession.insert(oldestEmployee);

		Employee secondOldEmployee = createEmployee(2, "MR.", "Y", -1);
		secondOldEmployee.setStartDate(sameDateTime);
		kSession.insert(secondOldEmployee);
		
		Employee juniorEmployee = createEmployee(3, "MR.", "NEW", -1);
		juniorEmployee.setStartDate(new DateTime(2014, 05, 27, 5, 2));
		kSession.insert(juniorEmployee);

		kSession.fireAllRules();

		FactHandle employeeFactHandle = kSession.getFactHandle(oldestEmployee);
		Employee emp = (Employee) kSession.getObject(employeeFactHandle);
		
		FactHandle secondEmployeeFactHandle = kSession.getFactHandle(secondOldEmployee);
		Employee emp2 = (Employee) kSession.getObject(secondEmployeeFactHandle);
		
		FactHandle juniorEmployeeFH = kSession.getFactHandle(juniorEmployee);
		Employee juniorEmp = (Employee) kSession.getObject(juniorEmployeeFH);
		
		assertEquals(1, juniorEmp.getSeniority());
		assertEquals(100, emp.getSeniority());
		assertEquals(100, emp2.getSeniority());
	}

}
