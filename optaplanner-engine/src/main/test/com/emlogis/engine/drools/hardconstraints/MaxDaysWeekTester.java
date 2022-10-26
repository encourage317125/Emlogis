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
import com.emlogis.engine.domain.contract.Contract;
import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.engine.domain.contract.contractline.MinMaxContractLine;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.domain.timeoff.CDTimeWindow;
import com.emlogis.engine.drools.ConstraintTesterBase;

public class MaxDaysWeekTester extends ConstraintTesterBase{
	
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
		rosterInfo.putRuleWeightMultiplier(RuleName.MAX_DAYS_PER_WEEK_CONSTRAINT, ruleMultiplier);
		kSession.update(rosterHandle, rosterInfo);

		MinMaxContractLine contractLine = createMaxContractLine(ContractLineType.DAYS_PER_WEEK, true, 3, -1);
		kSession.insert(contractLine);

		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		kSession.insert(secondShiftDate);
		
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(thirdShiftDate);
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
		kSession.insert(fourthShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		kSession.insert(secondShift);
		
		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);
		
		Shift fourthShift = createShift("4", 0, fourthShiftDate, shiftType, 1);
		kSession.insert(fourthShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		kSession.insert(secondShiftAssignment);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);
		
		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
		kSession.insert(fourthShiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_DAYS_PER_WEEK_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(ruleMultiplier * contractLine.getMaximumWeight(), numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testMaxDaysPerWeekInScheduleWindow() {
		MinMaxContractLine contractLine = createMaxContractLine(ContractLineType.DAYS_PER_WEEK, true, 3, -1);
		kSession.insert(contractLine);

		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		kSession.insert(secondShiftDate);
		
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(thirdShiftDate);
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
		kSession.insert(fourthShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		kSession.insert(secondShift);
		
		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);
		
		Shift fourthShift = createShift("4", 0, fourthShiftDate, shiftType, 1);
		kSession.insert(fourthShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		kSession.insert(secondShiftAssignment);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);
		
		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
		kSession.insert(fourthShiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_DAYS_PER_WEEK_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMaximumWeight(), numOfMaxPerWeekConstraints);
	}
	

	@Test
	public void testMaxDaysPerWeekMultiShiftsPerDay() {
		MinMaxContractLine contractLine = createMaxContractLine(ContractLineType.DAYS_PER_WEEK, true, 3, -1);
		kSession.insert(contractLine);

		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		kSession.insert(secondShiftDate);
		
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(thirdShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		kSession.insert(secondShift);
		
		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);
		
		Shift fourthShift = createShift("4", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(fourthShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		kSession.insert(secondShiftAssignment);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);
		
		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
		kSession.insert(fourthShiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_DAYS_PER_WEEK_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testMaxDaysPerWeekPTOOutside() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.setPlanningWindowStart(new ShiftDate(new LocalDate(2014, 5, 25)));
		rosterInfo.setFirstShiftDate(rosterInfo.getPlanningWindowStart().getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		kSession.update(rosterHandle, rosterInfo);
		
		MinMaxContractLine contractLine = createMaxContractLine(ContractLineType.DAYS_PER_WEEK, true, 3, -1);
		kSession.insert(contractLine);

		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		kSession.insert(secondShiftDate);
		
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(thirdShiftDate);
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
		kSession.insert(fourthShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		kSession.insert(secondShift);
		
		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		kSession.insert(secondShiftAssignment);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);

		CDTimeWindow pto = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 20), new LocalTime(02, 00, 00), new LocalTime(8, 00, 00), -1);
		kSession.insert(pto);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_DAYS_PER_WEEK_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testMaxDaysPerWeekOutsideScheduleWindowPTO() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.setPlanningWindowStart(new ShiftDate(new LocalDate(2014, 5, 25)));
		rosterInfo.setFirstShiftDate(rosterInfo.getPlanningWindowStart().getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		kSession.update(rosterHandle, rosterInfo);
		
		MinMaxContractLine contractLine = createMaxContractLine(ContractLineType.DAYS_PER_WEEK, true, 3, -1);
		kSession.insert(contractLine);

		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		kSession.insert(secondShiftDate);
		
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(thirdShiftDate);
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
		kSession.insert(fourthShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		kSession.insert(secondShift);
		
		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		kSession.insert(secondShiftAssignment);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);

		CDTimeWindow pto = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 20), new LocalTime(02, 00, 00), new LocalTime(8, 00, 00), -1);
		kSession.insert(pto);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_DAYS_PER_WEEK_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testMaxDaysPerWeekInPlanningWindow() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.setPlanningWindowStart(new ShiftDate(new LocalDate(2014, 5, 25)));
		rosterInfo.setFirstShiftDate(rosterInfo.getPlanningWindowStart().getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		kSession.update(rosterHandle, rosterInfo);
		
		MinMaxContractLine contractLine = createMaxContractLine(ContractLineType.DAYS_PER_WEEK, true, 3, -1);
		kSession.insert(contractLine);

		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);


		ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		kSession.insert(secondShiftDate);
		
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(thirdShiftDate);
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
		kSession.insert(fourthShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		kSession.insert(secondShift);
		
		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);
		
		Shift fourthShift = createShift("4", 0, fourthShiftDate, shiftType, 1);
		kSession.insert(fourthShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		kSession.insert(secondShiftAssignment);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);
		
		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
		kSession.insert(fourthShiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_DAYS_PER_WEEK_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMaximumWeight(), numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testMaxDaysPerWeekInScheduleWindowSatisfied() {
		MinMaxContractLine contractLine = createMaxContractLine(ContractLineType.DAYS_PER_WEEK, true, 4, -1);
		kSession.insert(contractLine);

		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);


		ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		kSession.insert(secondShiftDate);
		
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(thirdShiftDate);
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
		kSession.insert(fourthShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		kSession.insert(secondShift);
		
		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);
		
		Shift fourthShift = createShift("4", 0, fourthShiftDate, shiftType, 1);
		kSession.insert(fourthShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		kSession.insert(secondShiftAssignment);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);
		
		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
		kSession.insert(fourthShiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_DAYS_PER_WEEK_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}

	
	@Test
	public void testMaxDaysPerWeekInPlanningWindowSatisfied() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.setPlanningWindowStart(new ShiftDate(new LocalDate(2014, 5, 25)));
		rosterInfo.setFirstShiftDate(rosterInfo.getPlanningWindowStart().getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		kSession.update(rosterHandle, rosterInfo);
		
		MinMaxContractLine contractLine = createMaxContractLine(ContractLineType.DAYS_PER_WEEK, true, 4, -1);
		kSession.insert(contractLine);

		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);


		ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		kSession.insert(secondShiftDate);
		
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(thirdShiftDate);
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
		kSession.insert(fourthShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		kSession.insert(secondShift);
		
		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);
		
		Shift fourthShift = createShift("4", 0, fourthShiftDate, shiftType, 1);
		kSession.insert(fourthShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		kSession.insert(secondShiftAssignment);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);
		
		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
		kSession.insert(fourthShiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_DAYS_PER_WEEK_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	

}
