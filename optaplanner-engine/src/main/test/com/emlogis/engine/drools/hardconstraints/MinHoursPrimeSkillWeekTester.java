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

public class MinHoursPrimeSkillWeekTester extends ConstraintTesterBase {

	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
	}

	@Test
	public void testMinHoursPerWeekEnoughHoursOneShiftOutsidePlanningWindow() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.setPlanningWindowStart(new ShiftDate(new LocalDate(2014, 5, 27)));
		kSession.update(rosterHandle, rosterInfo);

		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 11,
				-1);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));

		Shift shift = createShift("1", 0, shiftDate, secondShiftDate, shiftType, 1);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		Skill skill = createSkill(1, "SuperMan");
		kSession.insert(skill);

		SkillProficiency skillProff = createSkillProficiency(employee, skill, true);
		kSession.insert(skillProff);

		ShiftSkillRequirement shiftSkillReq = new ShiftSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);

		kSession.insert(shiftType);
		kSession.insert(shiftDate);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(contract);
		kSession.insert(employee);
		kSession.insert(contractLine);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}

	@Test
	public void testMinHoursPerWeekEnoughHoursOneShift() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 11,
				-1);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));

		Shift shift = createShift("1", 0, shiftDate, secondShiftDate, shiftType, 1);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		Skill skill = createSkill(1, "SuperMan");
		kSession.insert(skill);

		SkillProficiency skillProff = createSkillProficiency(employee, skill, true);
		kSession.insert(skillProff);

		ShiftSkillRequirement shiftSkillReq = new ShiftSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);

		kSession.insert(shiftType);
		kSession.insert(shiftDate);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(contract);
		kSession.insert(employee);
		kSession.insert(contractLine);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testMinHoursPerWeekNotEnoughHoursExcludePTO() {
		int minHours = 29;
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.setIncludePTOInMinCalculations(false);
		kSession.update(rosterHandle, rosterInfo);
		
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, minHours, -1);

		Employee employee = createEmployee(1, "MR.", "X");
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		kSession.insert(contract);

		// Shift of 12 hours
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		
		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1); //12h shift
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1); //12h shift
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		
		CDTimeWindow pto = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 28), new LocalTime(02, 00, 00), new LocalTime(8, 00, 00),-1);
		kSession.insert(pto);
		
		Skill skill = createSkill(1, "SuperMan");
		kSession.insert(skill);

		SkillProficiency skillProff = createSkillProficiency(employee, skill, true);
		kSession.insert(skillProff);

		ShiftSkillRequirement shiftSkillReq = new ShiftSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);
		
		ShiftSkillRequirement shiftSkillReq2 = new ShiftSkillRequirement(secondShift, skill);
		kSession.insert(shiftSkillReq2);
		
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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
		assertEquals(shift.getShiftDurationHours()+secondShift.getShiftDurationHours()-minHours, numOfMinPerWeekConstraints);
	}

	@Test
	public void testMinHoursPerWeekNotEnoughHours() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 25,
				-1);

		Employee employee = createEmployee(1, "MR.", "X");

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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMinimumWeight(), numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testRuleWeightMultiplier() {
		// Change the weight multiplier of the rule
		int ruleMultiplier = 4;
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.putRuleWeightMultiplier(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT, ruleMultiplier);
		kSession.update(rosterHandle, rosterInfo);
		
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 25,
				-1);

		Employee employee = createEmployee(1, "MR.", "X");

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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(ruleMultiplier * contractLine.getMinimumWeight(), numOfMinPerWeekConstraints);
	}

	@Test
	public void testMinHoursPerWeekNotEnoughPrimarySkillHoursTeamContract() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 23,
				-1);

		Employee employee = createEmployee(1, "MR.", "X");

		Employee employeeTwo = createEmployee(2, "MR.", "Y");
		kSession.insert(employeeTwo);

		Contract contract = createContract(true, null, contractLine, 1);
		contractLine.setContract(contract);

		TeamAssociation teamAssos = createTeamAssociation(employeeTwo, 1);
		kSession.insert(teamAssos);

		TeamAssociation teamAssosOne = createTeamAssociation(employee, 1);
		kSession.insert(teamAssosOne);

		// 12 hour shift type
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));

		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employeeTwo, 0, secondShift);

		Skill skill = createSkill(1, "SuperMan");
		kSession.insert(skill);

		ShiftSkillRequirement shiftSkillReq = new ShiftSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);

		SkillProficiency skillProff = createSkillProficiency(employee, skill, true);
		kSession.insert(skillProff);

		ShiftSkillRequirement shiftSkillReqTwp = new ShiftSkillRequirement(secondShift, skill);
		kSession.insert(shiftSkillReqTwp);

		SkillProficiency skillProffTwo = createSkillProficiency(employeeTwo, skill, true);
		kSession.insert(skillProffTwo);

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

		int hoursMissingEmpOne = 11;
		int hoursMissingEmpTwo = 11;
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMinimumWeight() * (hoursMissingEmpOne + hoursMissingEmpTwo),
				numOfMinPerWeekConstraints);
	}

	@Test
	public void testMinHoursPerWeekNotEnoughPrimarySkillHoursWrongTeamContract() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 23,
				-1);

		Employee employee = createEmployee(1, "MR.", "X");

		Employee employeeTwo = createEmployee(2, "MR.", "Y");
		kSession.insert(employeeTwo);

		Contract contract = createContract(true, null, contractLine, 2);
		contractLine.setContract(contract);

		TeamAssociation teamAssos = createTeamAssociation(employeeTwo, 1);
		kSession.insert(teamAssos);

		TeamAssociation teamAssosOne = createTeamAssociation(employee, 1);
		kSession.insert(teamAssosOne);

		// 12 hour shift type
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));

		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employeeTwo, 0, secondShift);

		Skill skill = createSkill(1, "SuperMan");
		kSession.insert(skill);

		ShiftSkillRequirement shiftSkillReq = new ShiftSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);

		SkillProficiency skillProff = createSkillProficiency(employee, skill, true);
		kSession.insert(skillProff);

		ShiftSkillRequirement shiftSkillReqTwp = new ShiftSkillRequirement(secondShift, skill);
		kSession.insert(shiftSkillReqTwp);

		SkillProficiency skillProffTwo = createSkillProficiency(employeeTwo, skill, true);
		kSession.insert(skillProffTwo);

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

		int hoursMissingEmpOne = 11;
		int hoursMissingEmpTwo = 11;
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}

	@Test
	public void testMinHoursPerWeekEnoughPrimarySkillHoursTeamContract() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 23,
				-1);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(true, null, contractLine, 1);
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

		ShiftSkillRequirement shiftSkillReqTwp = new ShiftSkillRequirement(secondShift, skill);
		kSession.insert(shiftSkillReqTwp);

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

		int hoursMissing = 13;
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}

	@Test
	public void testMinHoursPerWeekNotEnoughPrimarySkillHoursWrongSkill() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 25,
				-1);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));

		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1); // Primary Skill
																// Shift

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		Skill skill = createSkill(1, "SuperMan"); // Primary skill
		kSession.insert(skill);

		ShiftSkillRequirement shiftSkillReq = new ShiftSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);

		Skill skillS = createSkill(2, "X-Men");
		kSession.insert(skillS);

		ShiftSkillRequirement shiftSkillReqS = new ShiftSkillRequirement(secondShift, skillS);
		kSession.insert(shiftSkillReqS);

		SkillProficiency skillProff = createSkillProficiency(employee, skill, true);
		kSession.insert(skillProff);

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

		int hoursMissing = 13;
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMinimumWeight() * hoursMissing, numOfMinPerWeekConstraints);
	}

	@Test
	public void testMinHoursPerWeekNotEnoughHoursNoWeekendShift() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 25,
				-1);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate weekendShiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 28));

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		Skill skill = createSkill(1, "SuperMan");
		kSession.insert(skill);

		ShiftSkillRequirement shiftSkillReq = new ShiftSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);

		ShiftSkillRequirement shiftSkillReqSecond = new ShiftSkillRequirement(secondShift, skill);
		kSession.insert(shiftSkillReqSecond);

		SkillProficiency skillProff = createSkillProficiency(employee, skill, true);
		kSession.insert(skillProff);

		kSession.insert(shiftType);
		kSession.insert(shiftDate);
		kSession.insert(weekendShiftDate);
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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMinimumWeight(), numOfMinPerWeekConstraints);
	}

	@Test
	public void testMinHoursPerWeekShiftsTooFarCase() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 25,
				-1);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));

		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 6, 1));

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		Skill skill = createSkill(1, "SuperMan");
		kSession.insert(skill);

		ShiftSkillRequirement shiftSkillReq = new ShiftSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);

		ShiftSkillRequirement shiftSkillReqSecond = new ShiftSkillRequirement(secondShift, skill);
		kSession.insert(shiftSkillReqSecond);

		SkillProficiency skillProff = createSkillProficiency(employee, skill, true);
		kSession.insert(skillProff);

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

		// Constraint is violated for two separate weeks
		int numOfHoursMissing = 13; // Each week is 13 hours short of the goal
									// of 25h per week
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMinimumWeight() * numOfHoursMissing, numOfMinPerWeekConstraints);
	}

	@Test
	public void testMinHoursPerWeekEnoughHoursMultiShift() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 29,
				-1);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);

		// Shift of 12 hours
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		// Shift of 11 hours crossing midnight
		ShiftType secondShiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(9, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));

		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 29));

		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 30));

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1); // 12h shift

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1); // 12h
																			// shift

		Shift thirdShift = createShift("3", 0, thirdShiftDate, fourthShiftDate, secondShiftType, 1); // 11h
																								// shift

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);

		Skill skill = createSkill(1, "SuperMan"); // Primary Skill
		kSession.insert(skill);

		SkillProficiency skillProff = createSkillProficiency(employee, skill, true);
		kSession.insert(skillProff);

		ShiftSkillRequirement shiftSkillReq = new ShiftSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);

		ShiftSkillRequirement shiftSkillReqSecond = new ShiftSkillRequirement(secondShift, skill);
		kSession.insert(shiftSkillReqSecond);

		ShiftSkillRequirement shiftSkillReqThird = new ShiftSkillRequirement(thirdShift, skill);
		kSession.insert(shiftSkillReqThird);

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
		kSession.insert(contract);
		kSession.insert(employee);
		kSession.insert(contractLine);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}

	@Test
	public void testMinHoursPerWeekEnoughHoursPTO() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 29,
				-1);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);

		// Shift of 12 hours
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		// Shift of 11 hours crossing midnight
		ShiftType secondShiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(9, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1); // 12h shift

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1); // 12h
																			// shift

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		Skill skill = createSkill(1, "SuperMan"); // Primary Skill
		kSession.insert(skill);

		SkillProficiency skillProff = createSkillProficiency(employee, skill, true);
		kSession.insert(skillProff);

		ShiftSkillRequirement shiftSkillReq = new ShiftSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);

		ShiftSkillRequirement shiftSkillReqSecond = new ShiftSkillRequirement(secondShift, skill);
		kSession.insert(shiftSkillReqSecond);

		// TODO:Make sure the CD time off is within the week you are looking
		// for, FOR ALL THE RULES!!
		CDTimeWindow pto = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 28),
				new LocalTime(02, 00, 00), new LocalTime(8, 00, 00), -1);
		kSession.insert(pto);

		kSession.insert(shiftType);
		kSession.insert(secondShiftType);
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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testMinHoursPerWeekNotEnoughHoursPTOWrongWeek() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.setPlanningWindowStart(new ShiftDate(new LocalDate(2014, 5, 18)));
		rosterInfo.setFirstShiftDate(rosterInfo.getPlanningWindowStart().getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(14));
		kSession.update(rosterHandle, rosterInfo);
		
		// Need this since day of week shift dates are
		// inserted automatically during normal execution
		kSession.insert(rosterInfo.getFirstShiftDate());
		kSession.insert(rosterInfo.getPlanningWindowStart());
		kSession.insert(rosterInfo.getFirstShiftDate().plusDays(7));
		
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 29,
				-1);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);

		// Shift of 12 hours
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		// Shift of 11 hours crossing midnight
		ShiftType secondShiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(9, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1); // 12h shift

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1); // 12h
																			// shift

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		Skill skill = createSkill(1, "SuperMan"); // Primary Skill
		kSession.insert(skill);

		SkillProficiency skillProff = createSkillProficiency(employee, skill, true);
		kSession.insert(skillProff);

		ShiftSkillRequirement shiftSkillReq = new ShiftSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);

		ShiftSkillRequirement shiftSkillReqSecond = new ShiftSkillRequirement(secondShift, skill);
		kSession.insert(shiftSkillReqSecond);

		CDTimeWindow pto = createCDTimeOff(employee.getEmployeeId(), new LocalDate(2014, 5, 19),
				new LocalTime(02, 00, 00), new LocalTime(8, 00, 00), -1);
		kSession.insert(pto);

		kSession.insert(shiftType);
		kSession.insert(secondShiftType);
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

		int hoursShortSecondWeek = 5;
		int hoursShortFirstWeek = 23;
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(-hoursShortFirstWeek - hoursShortSecondWeek, numOfMinPerWeekConstraints);
	}

	@Test
	public void testNotEnoughPrimaryHoursMultiShift() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 29,
				-1);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);

		// Shift of 12 hours
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		// Shift of 11 hours crossing midnight
		ShiftType secondShiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(9, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));

		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 29));

		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 30));

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1); // 12h shift

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1); // 12h
																			// shift

		Shift thirdShift = createShift("3", 0, thirdShiftDate, fourthShiftDate, secondShiftType, 1); // 11h
																								// shift

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);

		Skill skill = createSkill(1, "SuperMan");
		kSession.insert(skill);

		Skill skillSecond = createSkill(2, "Krieger");
		kSession.insert(skillSecond);

		SkillProficiency skillProff = createSkillProficiency(employee, skill, true);
		kSession.insert(skillProff);

		ShiftSkillRequirement shiftSkillReq = new ShiftSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);

		ShiftSkillRequirement shiftSkillReqSecond = new ShiftSkillRequirement(secondShift, skill);
		kSession.insert(shiftSkillReqSecond);

		ShiftSkillRequirement shiftSkillReqThird = new ShiftSkillRequirement(thirdShift, skillSecond);
		kSession.insert(shiftSkillReqThird);

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
		kSession.insert(contract);
		kSession.insert(employee);
		kSession.insert(contractLine);

		kSession.fireAllRules();

		int numOfHoursMissing = 5; // Due to second shift being of different
									// skill req 5 hours are missing from min
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMinimumWeight() * numOfHoursMissing, numOfMinPerWeekConstraints);
	}

	@Test
	public void testMinHoursPerWeekNotEnoughPrimarySkillHours() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 25,
				-1);

		Employee employee = createEmployee(1, "MR.", "X");

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

		ShiftSkillRequirement shiftSkillReq = new ShiftSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);

		SkillProficiency skillProff = createSkillProficiency(employee, skill, true);
		kSession.insert(skillProff);

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

		int hoursMissing = 13;
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(contractLine.getMinimumWeight() * hoursMissing, numOfMinPerWeekConstraints);
	}
}
