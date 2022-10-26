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
import com.emlogis.engine.domain.ShiftSkillRequirement;
import com.emlogis.engine.domain.ShiftType;
import com.emlogis.engine.domain.Skill;
import com.emlogis.engine.domain.SkillProficiency;
import com.emlogis.engine.domain.contract.Contract;
import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.engine.domain.contract.contractline.MinMaxContractLine;
import com.emlogis.engine.domain.organization.TeamAssociation;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.domain.timeoff.CDTimeWindow;
import com.emlogis.engine.drools.ConstraintTesterBase;

public class MinHoursBiWeeklyTester extends ConstraintTesterBase{
	
	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(13));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
	}
	
	@Test
	public void testMinHoursPerTwoWeeksEnoughHoursOneShiftOutsidePlanningWindow() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.setPlanningWindowStart(new ShiftDate(new LocalDate(2014, 5, 27)));
		kSession.update(rosterHandle, rosterInfo);
		
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_TWO_WEEKS, true, 11, -1);

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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_TWO_WEEKS_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testMinHoursPerTwoWeeksEnoughHoursOneShift() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_TWO_WEEKS, true, 11, -1);

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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_TWO_WEEKS_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testRuleWeightMultiplier() {
		// Change the weight multiplier of the rule
		int ruleMultiplier = 4;
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.putRuleWeightMultiplier(RuleName.MIN_HOURS_PER_TWO_WEEKS_CONSTRAINT, ruleMultiplier);
		kSession.update(rosterHandle, rosterInfo);
				
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_TWO_WEEKS, true, 25, -1);

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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_TWO_WEEKS_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(ruleMultiplier * contractLine.getMinimumWeight(), numOfMinPerWeekConstraints);
	}
	
	
	@Test
	public void testMinHoursPerTwoWeeksNotEnoughHours() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_TWO_WEEKS, true, 49, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		
		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 6, 01));
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 6, 05));
		kSession.insert(shiftDate);
		kSession.insert(secondShiftDate);
		kSession.insert(thirdShiftDate);
		kSession.insert(fourthShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		Shift fourthShift = createShift("4", 0, fourthShiftDate, shiftType, 1);
		kSession.insert(shift);
		kSession.insert(secondShift);
		kSession.insert(thirdShift);
		kSession.insert(fourthShift);


		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);

		kSession.insert(shiftAssignment);
		kSession.insert(secondShiftAssignment);
		kSession.insert(thirdShiftAssignment);
		kSession.insert(fourthShiftAssignment);
		
		kSession.insert(shiftType);
		kSession.insert(employee);
		kSession.insert(contractLine);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_TWO_WEEKS_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMinimumWeight(), numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testMinHoursPerWeekEnoughHoursPTO() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_TWO_WEEKS, true, 42, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		
		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 6, 01));
		kSession.insert(shiftDate);
		kSession.insert(secondShiftDate);
		kSession.insert(thirdShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(shift);
		kSession.insert(secondShift);
		kSession.insert(thirdShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(shiftAssignment);
		kSession.insert(secondShiftAssignment);
		kSession.insert(thirdShiftAssignment);
	
		CDTimeWindow pto = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 28), new LocalTime(02, 00, 00), new LocalTime(8, 00, 00),-1);
		kSession.insert(pto);
		
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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_TWO_WEEKS_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testMinHoursPerWeekNotEnoughHoursPTO() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_TWO_WEEKS, true, 45, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		
		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 6, 01));
		kSession.insert(shiftDate);
		kSession.insert(secondShiftDate);
		kSession.insert(thirdShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(shift);
		kSession.insert(secondShift);
		kSession.insert(thirdShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(shiftAssignment);
		kSession.insert(secondShiftAssignment);
		kSession.insert(thirdShiftAssignment);
	
		CDTimeWindow pto = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 28), new LocalTime(02, 00, 00), new LocalTime(8, 00, 00),-1);
		kSession.insert(pto);
		
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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_TWO_WEEKS_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(-3, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testMinHoursPerWeekNotEnoughHoursExcludePTO() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.setIncludePTOInMinCalculations(false);
		kSession.update(rosterHandle, rosterInfo);
		
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_TWO_WEEKS, true, 37, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		
		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 6, 05));
		kSession.insert(shiftDate);
		kSession.insert(secondShiftDate);
		kSession.insert(thirdShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(shift);
		kSession.insert(secondShift);
		kSession.insert(thirdShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(shiftAssignment);
		kSession.insert(secondShiftAssignment);
		kSession.insert(thirdShiftAssignment);
	
		CDTimeWindow pto = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 28), new LocalTime(02, 00, 00), new LocalTime(8, 00, 00),-1);
		kSession.insert(pto);
		
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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_TWO_WEEKS_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(-1, numOfMinPerWeekConstraints);
	}
	
}
