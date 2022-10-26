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

public class MaxHoursPerDayTester extends ConstraintTesterBase{
	
	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
	}
	
	@Test
	public void testMaxHoursPerDayOutsidePlanning() {
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_DAY, true, 9, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 24));
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		
		kSession.insert(shiftType);
		kSession.insert(shiftDate);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxHoursDayConstraint = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_DAY_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxHoursDayConstraint);
	}
	
	@Test
	public void testMaxHoursPerDayViolated() {
		int maxValue = 8;
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_DAY, true, maxValue, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		
		kSession.insert(shiftType);
		kSession.insert(shiftDate);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxHoursDayConstraint = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_DAY_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMaximumWeight()*(shift.getShiftDurationHours() - maxValue), numOfMaxHoursDayConstraint);
	}
	
	@Test
	public void testMaxHoursPerDayThirtyMinViolated() {
		int maxValue = 8;
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_DAY, true, maxValue, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(18, 29, 0), false);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		
		kSession.insert(shiftType);
		kSession.insert(shiftDate);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxHoursDayConstraint = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_DAY_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(-1, numOfMaxHoursDayConstraint);
	}
	
	@Test
	public void testRuleWeightMultiplier() {
		// Change the weight multiplier of the rule
		int ruleMultiplier = 4;
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.putRuleWeightMultiplier(RuleName.MAX_HOURS_PER_DAY_CONSTRAINT, ruleMultiplier);
		kSession.update(rosterHandle, rosterInfo);
				
		int maxValue = 8;
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_DAY, true, maxValue, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		
		kSession.insert(shiftType);
		kSession.insert(shiftDate);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxHoursDayConstraint = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_DAY_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(ruleMultiplier*contractLine.getMaximumWeight()*(shift.getShiftDurationHours() - maxValue), numOfMaxHoursDayConstraint);
	}
	
	@Test
	public void testMaxHoursPerDayViolatedOvernight() {
		int maxValue = 8;
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_DAY, true, maxValue, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(8, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		kSession.insert(secondShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, secondShiftDate, shiftType, 1);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		
		kSession.insert(shiftType);
		kSession.insert(shiftDate);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(contract);
		kSession.insert(employee);
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxHoursDayConstraint = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_DAY_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMaximumWeight()*(shift.getShiftDurationHours() - maxValue), numOfMaxHoursDayConstraint);
	}
	
	@Test
	public void testMaxHoursPerDayHonored() {
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_DAY, true, 8, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(6, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		
		kSession.insert(shiftType);
		kSession.insert(shiftDate);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinHoursDayConstraint = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_DAY_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinHoursDayConstraint);
	}
	
	@Test
	public void testMaxHoursPerDayOvernightHonored() {
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_DAY, true, 12, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(8, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		kSession.insert(secondShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, secondShiftDate, shiftType, 1);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		
		kSession.insert(shiftType);
		kSession.insert(shiftDate);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinHoursDayConstraint = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_DAY_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinHoursDayConstraint);
	}
	
}
