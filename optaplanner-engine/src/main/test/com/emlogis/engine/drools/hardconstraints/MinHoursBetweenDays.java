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
import com.emlogis.engine.drools.ConstraintTesterBase;

public class MinHoursBetweenDays extends ConstraintTesterBase{
	
	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
	}
	
	@Test
	public void testRuleWeightMultiplier() {
		int ruleMultiplier = 4;
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.putRuleWeightMultiplier(RuleName.MIN_HOURS_BETWEEN_DAYS_RULE, ruleMultiplier);
		kSession.update(rosterHandle, rosterInfo);
		
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_BETWEEN_DAYS, true, 13, -1);
		
		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));
		
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
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_BETWEEN_DAYS_RULE, scoreHolder.getConstraintMatchTotals());
		assertEquals(ruleMultiplier * contractLine.getMinimumWeight(), numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testMinHoursOneShiftOutsidePlanningWindow() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.setPlanningWindowStart(new ShiftDate(new LocalDate(2014, 5, 26)));
		kSession.update(rosterHandle, rosterInfo);
		
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_BETWEEN_DAYS, true, 13, -1);
		
		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));
		
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
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_BETWEEN_DAYS_RULE, scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMinimumWeight(), numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testMinHoursBothShiftOutsidePlanningWindow() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.setPlanningWindowStart(new ShiftDate(new LocalDate(2014, 5, 27)));
		kSession.update(rosterHandle, rosterInfo);
		
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_BETWEEN_DAYS, true, 13, -1);
		
		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));
		
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
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_BETWEEN_DAYS_RULE, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testMinHoursDifferentDaysPositive() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_BETWEEN_DAYS, true, 13, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));
		
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
		kSession.insert(contract);
		kSession.insert(employee);
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_BETWEEN_DAYS_RULE, scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMinimumWeight(), numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testMinHoursSameStartDateNegative() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_BETWEEN_DAYS, true, 5, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(14, 0, 0), false);
		ShiftType secondShiftType = createShiftType(1, new LocalTime(18, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setSkillId("X11");
		
		Shift secondShift = createShift("2", 0, shiftDate, secondShiftType, 1);
		secondShift.setSkillId("X12");
		
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
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_BETWEEN_DAYS_RULE, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testMinHoursDifferentDaysNegative() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_BETWEEN_DAYS, true, 12, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));
		
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
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_BETWEEN_DAYS_RULE, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testMinHoursSameStartDateBelowValueNegative() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_BETWEEN_DAYS, true, 4, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(14, 0, 0), false);
		ShiftType secondShiftType = createShiftType(1, new LocalTime(18, 0, 0), new LocalTime(22, 0, 0), false);

		
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
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_BETWEEN_DAYS_RULE, scoreHolder.getConstraintMatchTotals());
		
		assertEquals(0, numOfMinPerWeekConstraints);
	}
	


}
