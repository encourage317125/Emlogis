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
import com.emlogis.engine.domain.organization.TeamAssociation;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.drools.ConstraintTesterBase;

public class MaxHoursWeekTester extends ConstraintTesterBase{
	
	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
	}
	
	@Test
	public void testMaxHoursPerWeekEnoughHoursOneShiftOutsidePlanningWindow() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.setPlanningWindowStart(new ShiftDate(new LocalDate(2014, 5, 27)));
		kSession.update(rosterHandle, rosterInfo);
		
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_WEEK, true, 13, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		
		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		
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
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_WEEK_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testMaxHoursPerWeekEnoughHoursOneShift() {
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_WEEK, true, 12, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		
		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		
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
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_WEEK_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testRuleWeightMultiplier() {
		// Change the weight multiplier of the rule
		int ruleMultiplier = 4;
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.putRuleWeightMultiplier(RuleName.MAX_HOURS_PER_WEEK_CONSTRAINT, ruleMultiplier);
		kSession.update(rosterHandle, rosterInfo);

		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_WEEK, true, 23, -1);

		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(true, null, contractLine, teamId);
		contractLine.setContract(contract);
		kSession.insert(contract);

		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		kSession.insert(teamAssos);
		
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
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_WEEK_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(ruleMultiplier * contractLine.getMaximumWeight(), numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testTeamMaxHoursPerWeekTooManyHours() {
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_WEEK, true, 23, -1);

		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(true, null, contractLine, teamId);
		contractLine.setContract(contract);
		kSession.insert(contract);

		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		kSession.insert(teamAssos);
		
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
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_WEEK_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMaximumWeight(), numOfMaxPerWeekConstraints);
	}
	
	
	@Test
	public void testMaxHoursPerWeekTooManyHours() {
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_WEEK, true, 23, -1);

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
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_WEEK_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMaximumWeight(), numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testMaxHoursPerWeekTooManyHoursNoWeekendShift() {
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_WEEK, true, 23, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate weekendShiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		
		kSession.insert(shiftType);
		kSession.insert(shiftDate);
		kSession.insert(weekendShiftDate);
		kSession.insert(secondShiftDate);
		kSession.insert(secondShift);
		kSession.insert(secondShiftAssignment);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_WEEK_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMaximumWeight(), numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testMaxHoursPerWeekShiftsTooFarCase() {
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_WEEK, true, 11, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 6, 1));
		
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
		
		//Constraint is violated for two separate weeks
		int numOfHoursOver = 1; //Each week is 1 hour over the goal of 11h a week
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_WEEK_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMaximumWeight() * numOfHoursOver * 2, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testMaxHoursPerWeekTooManyMultiShift() {
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_WEEK, true, 33, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);
		
		// Shift of 12 hours
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		// Shift of 11 hours crossing midnight 
		ShiftType secondShiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(9, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		
		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 29));
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 30));
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1); //12h shift
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1); //12h shift
		
		Shift thirdShift = createShift("3", 0, thirdShiftDate, fourthShiftDate, secondShiftType, 1); //11h shift
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
	
		kSession.insert(shiftType);
		kSession.insert(secondShiftType);
		kSession.insert(shiftDate);
		kSession.insert(secondShiftDate);
		kSession.insert(fourthShiftDate);
		kSession.insert(thirdShiftDate);
		kSession.insert(secondShift);
		kSession.insert(secondShiftAssignment);
		kSession.insert(thirdShiftAssignment);
		kSession.insert(shift);
		kSession.insert(thirdShift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_WEEK_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMaximumWeight() * 2, numOfMaxPerWeekConstraints);
	}
}
