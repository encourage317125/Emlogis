package com.emlogis.engine.drools.hardconstraints;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.kie.api.runtime.rule.FactHandle;
import org.optaplanner.core.api.score.buildin.bendable.BendableScoreHolder;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.Employee;
import com.emlogis.engine.domain.Shift;
import com.emlogis.engine.domain.ShiftAssignment;
import com.emlogis.engine.domain.ShiftDate;
import com.emlogis.engine.domain.ShiftSkillRequirement;
import com.emlogis.engine.domain.ShiftType;
import com.emlogis.engine.domain.Skill;
import com.emlogis.engine.domain.SkillProficiency;
import com.emlogis.engine.domain.contract.ConstraintOverride;
import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.engine.domain.contract.Contract;
import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.engine.domain.contract.contractline.MinMaxContractLine;
import com.emlogis.engine.domain.contract.contractline.PatternContractLine;
import com.emlogis.engine.domain.contract.patterns.CompleteWeekendWorkPattern;
import com.emlogis.engine.domain.contract.patterns.WeekdayRotationPattern;
import com.emlogis.engine.domain.contract.patterns.WeekdayRotationPattern.RotationPatternType;
import com.emlogis.engine.domain.organization.TeamAssociation;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.domain.timeoff.CDTimeWindow;
import com.emlogis.engine.domain.timeoff.CITimeWindow;
import com.emlogis.engine.drools.ConstraintTesterBase;

public class ConstraintOverrideTester extends ConstraintTesterBase {

	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
	}

	@Test
	public void testCDAllDayOverride() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.ALL_DAY_UNAVAILABLE_OVERRIDE);
		kSession.insert(override);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 27), -1);
		timeOff.setPTO(false);
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
		assertEquals(0, numOfMaxPerWeekConstraints);
	}

	@Test
	public void testCDPTOOverride() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.PTO_OVERRIDE);
		kSession.insert(override);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 27), -1);
		timeOff.setPTO(true);
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
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testCDTimeWindowOverride() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.TIME_WINDOW_UNAVAILABLE_OVERRIDE);
		kSession.insert(override);

		CDTimeWindow timeOff = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 27), -1);
		timeOff.setTimeWindow(new LocalTime(9, 0, 0), new LocalTime(21, 0, 0));
		timeOff.setPTO(false);
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
		assertEquals(0, numOfMaxPerWeekConstraints);
	}

	@Test
	public void testCIAllDayOverride() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.ALL_DAY_UNAVAILABLE_OVERRIDE);
		kSession.insert(override);

		CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.MONDAY, -1);
		timeOff.setPTO(false);
		kSession.insert(timeOff);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testCITimeWindowOverride() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.TIME_WINDOW_UNAVAILABLE_OVERRIDE);
		kSession.insert(override);

		CITimeWindow timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.MONDAY, -1);
		timeOff.setTimeWindow(new LocalTime(9, 0, 0), new LocalTime(21, 0, 0));
		timeOff.setPTO(false);
		kSession.insert(timeOff);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	
//	@Test
//	public void testCIOverride() {
//		Employee employee = createEmployee(1, "MR.", "X");
//		kSession.insert(employee);
//		
//		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.TIME_OFF_OVERRIDE);
//		kSession.insert(override);
//
//		CITimeOff timeOff = createCITimeOff(employee.getEmployeeId(), DayOfWeek.MONDAY, -1);
//		kSession.insert(timeOff);
//
//		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
//		kSession.insert(shiftType);
//
//		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
//																			// Shift
//		kSession.insert(shiftDate);
//
//		Shift shift = createShift(0, shiftDate, shiftType, 1);
//		kSession.insert(shift);
//
//		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
//		kSession.insert(shiftAssignment);
//
//		kSession.fireAllRules();
//
//		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
//				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
//		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_TIME_OFF_CONSTRAINT,
//				scoreHolder.getConstraintMatchTotals());
//		assertEquals(0, numOfMaxPerWeekConstraints);
//	}

	@Test
	public void testConsecutiveDaysOverride() {
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.CONSECUTIVE_WORKING_DAYS, true, 3, -1);
		kSession.insert(contractLine);

		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.MAX_CONSECUTIVE_DAYS_OVERRIDE);
		kSession.insert(override);

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(shiftDate);

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
		kSession.insert(secondShiftDate);

		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 29));
		kSession.insert(thirdShiftDate);

		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 30));
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

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfConsecDaysConstraints = getNumOfConstraintMatches(
				RuleName.MAX_CONSECUTIVE_DAYS_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfConsecDaysConstraints);
	}

	@Test
	public void testMaxDaysPerWeekOverride() {
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.DAYS_PER_WEEK, true, 3, -1);
		kSession.insert(contractLine);

		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.MAX_DAYS_WEEK_OVERRIDE);
		kSession.insert(override);

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

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_DAYS_PER_WEEK_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}

	@Test
	public void testMaxHoursPerDayOverride() {
		int maxValue = 8;
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_DAY, true, maxValue, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.MAX_HOURS_DAY_OVERRIDE);
		kSession.insert(override);

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

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxHoursDayConstraint = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_DAY_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxHoursDayConstraint);

	}

	@Test
	public void testMaxHoursPerWeekOverride() {
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_WEEK, true, 23, -1);

		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.MAX_HOURS_WEEK_OVERRIDE);
		kSession.insert(override);

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

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.MAX_HOURS_PER_WEEK_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	

	@Test
	public void testMinHoursBetweenShiftsOverride() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_BETWEEN_DAYS, true, 13, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.MIN_HOURS_BETWEEN_DAYS_OVERRIDE);
		kSession.insert(override);

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

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_BETWEEN_DAYS_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}

	@Test
	public void testMinHoursPerDayOverride() {
		int minValue = 12;
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_DAY, true, minValue, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.MIN_HOURS_DAY_OVERRIDE);
		kSession.insert(override);

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(18, 0, 0), false);

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

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinHoursDayConstraint = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_DAY_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinHoursDayConstraint);
	}

	@Test
	public void testMinHoursPerWeekPrimeSkillOverride() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 25,
				-1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.MIN_HOURS_WEEK_OVERRIDE);
		kSession.insert(override);

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));

		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		Skill skill = createSkill(1, "SuperMan");
		kSession.insert(skill);

		SkillProficiency skillProff = createSkillProficiency(employee, skill, true);
		kSession.insert(skillProff);

		ShiftSkillRequirement shiftSkillReq = new ShiftSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);

		ShiftSkillRequirement shiftSkillSecReq = new ShiftSkillRequirement(secondShift, skill);
		kSession.insert(shiftSkillSecReq);

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

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(
				RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}


	@Test
	public void testDaysOffRotationPatternOverride() {
		WeekdayRotationPattern oneOfTwoPattern = createWeekdayRotationPattern(RotationPatternType.DAYS_OFF_PATTERN,
				DayOfWeek.MONDAY, 1, 2, -1);
		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.WEEKDAY_ROTATION_OVERRIDE);
		kSession.insert(override);

		PatternContractLine contractLine = createPatternContractLine(contract, oneOfTwoPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 12));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 19));

		rosterInfo.setLastShiftDate(secondShiftDate);
		FactHandle info = kSession.getFactHandle(rosterInfo);
		kSession.update(info, rosterInfo);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		kSession.insert(shiftType);
		kSession.insert(oneOfTwoPattern);
		kSession.insert(contractLine);
		kSession.insert(shiftDate);
		kSession.insert(secondShiftDate);
		kSession.insert(secondShift);
		kSession.insert(secondShiftAssignment);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.WEEKDAY_ROTATION_PATTERN_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}

	@Test
	public void testDaysOffBeforeWeekendOverride() {
		int weight = -1;
		Collection<DayOfWeek> daysOffBefore = Arrays.asList(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(null, daysOffBefore, weight);

		Employee employee = createEmployee(1, "MR.", "X");

		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.DAYS_OFF_BEFORE_OVERRIDE);
		kSession.insert(override);
		
		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, weekendPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 17));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 18));

		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 16));
		kSession.insert(thirdShiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);

		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);

		kSession.insert(shiftType);
		kSession.insert(weekendPattern);
		kSession.insert(contractLine);
		kSession.insert(shiftDate);
		kSession.insert(secondShiftDate);
		kSession.insert(secondShift);
		kSession.insert(secondShiftAssignment);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(
				RuleName.DAYS_OFF_BEFORE_WEEKEND_RULE, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}

	@Test
	public void testDaysAfterWeekendOverride() {
		int weight = -1;
		Collection<DayOfWeek> daysOffAfter = Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(daysOffAfter, null, weight);

		Employee employee = createEmployee(1, "MR.", "X");
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.DAYS_OFF_AFTER_OVERRIDE);
		kSession.insert(override);

		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, weekendPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 17));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 18));

		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 20));
		kSession.insert(thirdShiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);

		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);

		kSession.insert(shiftType);
		kSession.insert(weekendPattern);
		kSession.insert(contractLine);
		kSession.insert(shiftDate);
		kSession.insert(secondShiftDate);
		kSession.insert(secondShift);
		kSession.insert(secondShiftAssignment);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(
				RuleName.DAYS_OFF_AFTER_WEEKEND_RULE, scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}

	private void setUpWeekendPatternTwoDayData(LocalDate dayOne, LocalDate dayTwo, int weight) {
		setUpWeekendPatternTwoDayData(null, null, dayOne, dayTwo, weight);
	}

	void setUpWeekendPatternTwoDayData(Collection<DayOfWeek> daysOffAfter, Collection<DayOfWeek> daysOffBefore,
			LocalDate dayOne, LocalDate dayTwo, int weight) {
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(daysOffAfter, daysOffBefore, weight);

		Employee employee = createEmployee(1, "MR.", "X");
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.COUPLED_WEEKEND_OVERRIDE);
		kSession.insert(override);

		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, weekendPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(dayOne);

		ShiftDate secondShiftDate = new ShiftDate(dayTwo);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		kSession.insert(shiftType);
		kSession.insert(weekendPattern);
		kSession.insert(contractLine);
		kSession.insert(shiftDate);
		kSession.insert(secondShiftDate);
		kSession.insert(secondShift);
		kSession.insert(secondShiftAssignment);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);

		kSession.fireAllRules();
	}

	@Test
	public void testCoupledWeekendOverride() {
		int weight = -1;
		setUpWeekendPatternTwoDayData(new LocalDate(2014, 5, 25), new LocalDate(2014, 5, 27), weight);

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.COUPLED_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}
}
