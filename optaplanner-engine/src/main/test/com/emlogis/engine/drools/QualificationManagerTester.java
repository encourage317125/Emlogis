package com.emlogis.engine.drools;

import com.emlogis.engine.domain.*;
import com.emlogis.engine.domain.communication.ShiftQualificationDto;
import com.emlogis.engine.domain.communication.constraints.*;
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
import com.emlogis.engine.domain.organization.TeamAssociationType;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.domain.timeoff.CDTimeOff;
import com.emlogis.engine.domain.timeoff.CITimeOff;
import com.emlogis.engine.driver.QualificationManager;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QualificationManagerTester extends TestDataUtility {
	private QualificationManager qualificationManager;
	protected EmployeeRosterInfo rosterInfo;
	protected EmployeeSchedule schedule;

	@Before
	public void setUp() throws Exception {
		qualificationManager = new QualificationManager();
		qualificationManager.initialize();
		qualificationManager.setIncludeResultDetails(true);
		rosterInfo = new EmployeeRosterInfo();
		
		rosterInfo.setFirstDayOfWeek(DayOfWeek.SUNDAY);
		rosterInfo.setWeekendDefinition(WeekendDefinition.SATURDAY_SUNDAY);
		setUpRuleWeights();
		setUpScoringRuleScoreLevels();
		
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(6));
	
		
		schedule = new EmployeeSchedule();
		schedule.setEmployeeRosterInfo(rosterInfo);
	}
	
	@Test
	public void testSkillConstraint() {
		String employeeTeamId = "1";
		String requiredSkillId = "Magic";
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, employeeTeamId);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(17, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId(employeeTeamId);
		shift.setSkillId(requiredSkillId);
		schedule.getShiftList().add(shift);
		
		Skill skill = new Skill(requiredSkillId, "AWESOME");
		
		ShiftSkillRequirement req = new ShiftSkillRequirement(shift, skill);
		schedule.getShiftTypeSkillRequirementList().add(req);
		
		SkillProficiency skillProf = new SkillProficiency();
		skillProf.setEmployee(employee);
		skillProf.setSkill(new Skill("lessAwesome", "dude"));
		schedule.getSkillProficiencyList().add(skillProf);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(1, results.size());

		Collection<ShiftConstraintDto> constraints = CollectionUtils.extractSingleton(results).getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof SkillShiftConstraintDto);
		assertEquals(requiredSkillId, ((SkillShiftConstraintDto) constraint).getRequiredSkillId());
		assertEquals(RuleName.SKILL_MATCH_RULE, constraint.getConstraintName());
	}
	
	@Test
	public void testTeamAssociation() {
		long employeeTeamId = 1;
		String requiredTeamId = "2";
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, employeeTeamId);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(17, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId(requiredTeamId);
		schedule.getShiftList().add(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(1, results.size());

		Collection<ShiftConstraintDto> constraints = CollectionUtils.extractSingleton(results).getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof TeamShiftConstraintDto);
		assertEquals(requiredTeamId, ((TeamShiftConstraintDto) constraint).getRequiredTeamId());
		assertEquals(RuleName.TEAM_ASSOCIATION_CONSTRAINT, constraint.getConstraintName());
	}
	
	//TODO: Float test incomplete since both team constraints will fire. 
	@Test
	public void testTeamFloatAssociation() {
		long employeeTeamId = 1;
		String requiredTeamId = "2";
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		ConstraintOverride teamFloat = new ConstraintOverride(employee, ConstraintOverrideType.TEAM_FLOAT_ON);
		schedule.getConstraintOverrides().add(teamFloat);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, employeeTeamId, TeamAssociationType.FLOAT, true);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(17, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId(requiredTeamId);
		schedule.getShiftList().add(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(1, results.size());

		Collection<ShiftConstraintDto> constraints = CollectionUtils.extractSingleton(results).getCauses();
		assertEquals(2, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof TeamShiftConstraintDto);
		assertEquals(requiredTeamId, ((TeamShiftConstraintDto) constraint).getRequiredTeamId());
		assertEquals(RuleName.TEAM_ASSOCIATION_CONSTRAINT, constraint.getConstraintName());
	}

	@After
	public void tearDown() throws Exception {
		qualificationManager.clear();
	}

	@Test
	public void testQualifyScheduleAssignments() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK, true, 5, -1);
		schedule.getContractLineList().add(contractLine);
		
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		Contract contract = createContract(true, null, contractLine, teamId);
		contractLine.setContract(contract);
		schedule.getContractList().add(contract);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));
		schedule.getShiftDateList().add(secondShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(secondShift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift, true);	
		schedule.getShiftAssignmentList().add(secondShiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		assertEquals(2, qualificationManager.getShiftQualificationResults().size());
	}
	
	@Test
	public void testMinHoursPerWeek() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK, true, 25, -1);
		schedule.getContractLineList().add(contractLine);
		
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		Contract contract = createContract(true, null, contractLine, teamId);
		contractLine.setContract(contract);
		schedule.getContractList().add(contract);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));
		schedule.getShiftDateList().add(secondShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(secondShift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift, true);	
		schedule.getShiftAssignmentList().add(secondShiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(2, results.size());

		Collection<ShiftConstraintDto> constraints = results.iterator().next().getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof MinShiftConstraintDto);
		assertEquals(24*60, ((MinShiftConstraintDto)constraint).getActualValue());
		assertEquals(25*60, ((MinShiftConstraintDto)constraint).getMinValue());
		assertTrue(constraint.getInvolvedShifts().contains(shiftAssignment.getShift().getId()));
		assertTrue(constraint.getInvolvedShifts().contains(secondShiftAssignment.getShift().getId()));
		assertEquals(RuleName.MIN_HOURS_PER_WEEK_CONSTRAINT, constraint.getConstraintName());
	}
	
	
	
	@Test
	public void testMaxHoursPerWeek() {
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_WEEK, true, 12, -1);
		schedule.getContractLineList().add(contractLine);
		
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		Contract contract = createContract(true, null, contractLine, teamId);
		contractLine.setContract(contract);
		schedule.getContractList().add(contract);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));
		schedule.getShiftDateList().add(secondShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(secondShift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift, true);	
		schedule.getShiftAssignmentList().add(secondShiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(2, results.size());

		Collection<ShiftConstraintDto> constraints = results.iterator().next().getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof MaxShiftConstraintDto);
		assertEquals(24*60, ((MaxShiftConstraintDto)constraint).getActualValue());
		assertEquals(12*60, ((MaxShiftConstraintDto)constraint).getMaxValue());
		assertTrue(constraint.getInvolvedShifts().contains(shiftAssignment.getShift().getId()));
		assertTrue(constraint.getInvolvedShifts().contains(secondShiftAssignment.getShift().getId()));
		assertEquals(RuleName.MAX_HOURS_PER_WEEK_CONSTRAINT, constraint.getConstraintName());
	}
	
	@Test
	public void testMaxDaysPerWeek() {
		MinMaxContractLine contractLine = createMaxContractLine(ContractLineType.DAYS_PER_WEEK, true, 1, -1);
		schedule.getContractLineList().add(contractLine);
		
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		Contract contract = createContract(true, null, contractLine, teamId);
		contractLine.setContract(contract);
		schedule.getContractList().add(contract);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));
		schedule.getShiftDateList().add(secondShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(secondShift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift, true);	
		schedule.getShiftAssignmentList().add(secondShiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(2, results.size());

		Collection<ShiftConstraintDto> constraints = results.iterator().next().getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof MaxShiftConstraintDto);
		assertEquals(2, ((MaxShiftConstraintDto)constraint).getActualValue());
		assertEquals(1, ((MaxShiftConstraintDto)constraint).getMaxValue());
		assertTrue(constraint.getInvolvedShifts().contains(shiftAssignment.getShift().getId()));
		assertTrue(constraint.getInvolvedShifts().contains(secondShiftAssignment.getShift().getId()));
		assertEquals(RuleName.MAX_DAYS_PER_WEEK_CONSTRAINT, constraint.getConstraintName());
	}
	
	@Test
	public void testOverlappingShifts() {
		MinMaxContractLine contractLine = createMaxContractLine(ContractLineType.OVERLAPPING_SHIFTS, true, 6, -1);
		schedule.getContractLineList().add(contractLine);
		
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		Contract contract = createContract(true, null, contractLine, teamId);
		contractLine.setContract(contract);
		schedule.getContractList().add(contract);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(shift);
		
		Shift secondShift = createShift("2", 0, shiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(secondShift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift, true);	
		schedule.getShiftAssignmentList().add(secondShiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(2, results.size());

		Collection<ShiftConstraintDto> constraints = results.iterator().next().getCauses();
		assertEquals(2, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof MaxShiftConstraintDto);
		assertEquals(12*60, ((MaxShiftConstraintDto)constraint).getActualValue()); //Shifts overlap by 12h
		assertEquals(6, ((MaxShiftConstraintDto)constraint).getMaxValue());
		assertTrue(constraint.getInvolvedShifts().contains(shiftAssignment.getShift().getId()));
		assertTrue(constraint.getInvolvedShifts().contains(secondShiftAssignment.getShift().getId()));
		assertEquals(RuleName.OVERLAPPING_SHIFTS_RULE, constraint.getConstraintName());
	}
	
	@Test
	public void testMaxConsecutiveDays() {
		String teamId = "1";
		MinMaxContractLine contractLine = createMaxContractLine(ContractLineType.CONSECUTIVE_WORKING_DAYS, true, 1, -1);
		schedule.getContractLineList().add(contractLine);
		
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		Contract contract = createContract(false, employee, contractLine);
		contractLine.setContract(contract);
		schedule.getContractList().add(contract);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));
		schedule.getShiftDateList().add(secondShiftDate);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId(teamId);
		schedule.getShiftList().add(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		secondShift.setTeamId(teamId);
		schedule.getShiftList().add(secondShift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift, true);	
		schedule.getShiftAssignmentList().add(secondShiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(2, results.size());

		Collection<ShiftConstraintDto> constraints = results.iterator().next().getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof MaxShiftConstraintDto);
		assertEquals(2, ((MaxShiftConstraintDto)constraint).getActualValue());
		assertEquals(1, ((MaxShiftConstraintDto)constraint).getMaxValue());
		assertTrue(constraint.getInvolvedShifts().contains(shiftAssignment.getShift().getId()));
		assertTrue(constraint.getInvolvedShifts().contains(secondShiftAssignment.getShift().getId()));
		assertEquals(RuleName.MAX_CONSECUTIVE_DAYS_CONSTRAINT, constraint.getConstraintName());
	}
	
	@Test
	public void testMinHoursPerWeekPrimarySkill() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_WEEK_PRIME_SKILL, true, 25, -1);
		schedule.getContractLineList().add(contractLine);
		
		long teamId = 1;
		String skillId = "1";
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		Skill skill = new Skill(skillId, "X");
		schedule.getSkillList().add(skill);
		
		SkillProficiency prof = createSkillProficiency(employee, skill, true);
		schedule.getSkillProficiencyList().add(prof);
		
		Contract contract = createContract(true, null, contractLine, teamId);
		contractLine.setContract(contract);
		schedule.getContractList().add(contract);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));
		schedule.getShiftDateList().add(secondShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setSkillId(skillId);
		shift.setTeamId("1");
		schedule.getShiftList().add(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		secondShift.setSkillId(skillId);
		shift.setTeamId("1");
		schedule.getShiftList().add(secondShift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift, true);	
		schedule.getShiftAssignmentList().add(secondShiftAssignment);
		
		ShiftSkillRequirement skr = createShiftTypeSkillRequirement(shift, skill);
		schedule.getShiftTypeSkillRequirementList().add(skr);
		
		ShiftSkillRequirement skr2 = createShiftTypeSkillRequirement(secondShift, skill);
		schedule.getShiftTypeSkillRequirementList().add(skr2);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(2, results.size());

		Collection<ShiftConstraintDto> constraints = results.iterator().next().getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof MinShiftConstraintDto);
		assertEquals(24*60, ((MinShiftConstraintDto)constraint).getActualValue());
		assertEquals(25*60, ((MinShiftConstraintDto)constraint).getMinValue());
		assertTrue(constraint.getInvolvedShifts().contains(shiftAssignment.getShift().getId()));
		assertTrue(constraint.getInvolvedShifts().contains(secondShiftAssignment.getShift().getId()));
		assertEquals(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT, constraint.getConstraintName());
	}
	
	@Test
	public void testMinHoursPerDay() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_DAY, true, 8, -1);
		schedule.getContractLineList().add(contractLine);
		
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		Contract contract = createContract(true, null, contractLine, teamId);
		contractLine.setContract(contract);
		schedule.getContractList().add(contract);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(17, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(1, results.size());

		Collection<ShiftConstraintDto> constraints = CollectionUtils.extractSingleton(results).getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof MinShiftConstraintDto);
		assertEquals(7*60, ((MinShiftConstraintDto)constraint).getActualValue());
		assertEquals(8*60, ((MinShiftConstraintDto)constraint).getMinValue());
		assertTrue(constraint.getInvolvedShifts().contains(shiftAssignment.getShift().getId()));
		assertEquals(RuleName.MIN_HOURS_PER_DAY_CONSTRAINT, constraint.getConstraintName());
	}
	
	@Test
	public void testMaxHoursPerDay() {
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.HOURS_PER_DAY, true, 4, -1);
		schedule.getContractLineList().add(contractLine);
		
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		Contract contract = createContract(true, null, contractLine, teamId);
		contractLine.setContract(contract);
		schedule.getContractList().add(contract);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(17, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(1, results.size());

		Collection<ShiftConstraintDto> constraints = CollectionUtils.extractSingleton(results).getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof MaxShiftConstraintDto);
		assertEquals(7*60, ((MaxShiftConstraintDto)constraint).getActualValue());
		assertEquals(4*60, ((MaxShiftConstraintDto)constraint).getMaxValue());
		assertTrue(constraint.getInvolvedShifts().contains(shiftAssignment.getShift().getId()));
		assertEquals(RuleName.MAX_HOURS_PER_DAY_CONSTRAINT, constraint.getConstraintName());
	}
	
	@Test
	public void testDailyOvertime() {
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.DAILY_OVERTIME, true, 4, -1);
		schedule.getContractLineList().add(contractLine);
		
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.AVOID_OVERTIME);
		schedule.getConstraintOverrides().add(override);
		
		Contract contract = createContract(true, null, contractLine, teamId);
		contractLine.setContract(contract);
		schedule.getContractList().add(contract);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(17, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(1, results.size());

		Collection<ShiftConstraintDto> constraints = CollectionUtils.extractSingleton(results).getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof MaxShiftConstraintDto);
		assertEquals(3*60, ((MaxShiftConstraintDto)constraint).getActualValue());
		assertEquals(4*60, ((MaxShiftConstraintDto)constraint).getMaxValue());
		assertTrue(constraint.getInvolvedShifts().contains(shiftAssignment.getShift().getId()));
		assertEquals(RuleName.AVOID_DAILY_OVERTIME_RULE, constraint.getConstraintName());
	}
	
	@Test
	public void testWeeklyOvertime() {
		MinMaxContractLine contractLine = createMaxContractLineInHours(ContractLineType.WEEKLY_OVERTIME, true, 10, -1);
		schedule.getContractLineList().add(contractLine);
		
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.AVOID_OVERTIME);
		schedule.getConstraintOverrides().add(override);
		
		Contract contract = createContract(true, null, contractLine, teamId);
		contractLine.setContract(contract);
		schedule.getContractList().add(contract);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));
		schedule.getShiftDateList().add(secondShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(secondShift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift, true);	
		schedule.getShiftAssignmentList().add(secondShiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(2, results.size());

		Collection<ShiftConstraintDto> constraints = results.iterator().next().getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof MaxShiftConstraintDto);
		assertEquals(14*60, ((MaxShiftConstraintDto)constraint).getActualValue());
		assertEquals(10*60, ((MaxShiftConstraintDto)constraint).getMaxValue());
		assertTrue(constraint.getInvolvedShifts().contains(shiftAssignment.getShift().getId()));
		assertEquals(RuleName.AVOID_WEEKLY_OVERTIME_RULE, constraint.getConstraintName());
	}
	
	@Test
	public void testAvoidDaysAfterWeekend() {
		int weight = -1;
		Collection<DayOfWeek> daysOffAfter = Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(daysOffAfter, null, weight);
		schedule.getPatternList().add(weekendPattern);
		
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		Contract contract = createContract(false, employee);
		schedule.getContractList().add(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, weekendPattern);
		schedule.getPatternContractLineList().add(contractLine);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 24));
		schedule.getShiftDateList().add(shiftDate);
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(secondShiftDate);
		
		ShiftDate thirdShiftDate = new ShiftDate();
		thirdShiftDate.setDate(new LocalDate(2014, 5, 26));  //Monday shift date
		schedule.getShiftDateList().add(thirdShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		secondShift.setTeamId("1");
		schedule.getShiftList().add(secondShift);
		
		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1); //Monday shift
		thirdShift.setTeamId("1");
		schedule.getShiftList().add(thirdShift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, false);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift, false);	
		schedule.getShiftAssignmentList().add(secondShiftAssignment);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift, true);	
		schedule.getShiftAssignmentList().add(thirdShiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(1, results.size());

		Collection<ShiftConstraintDto> constraints = CollectionUtils.extractSingleton(results).getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof DaysAfterWeekendConstraintDto);
		assertEquals(1, constraint.getInvolvedShifts().size());
		assertTrue(constraint.getInvolvedShifts().contains(thirdShiftAssignment.getShift().getId()));
		assertEquals(RuleName.DAYS_OFF_AFTER_WEEKEND_RULE, constraint.getConstraintName());
	}
	
	@Test
	public void testCoupledWeekend() {
		int weight = -1;
		Collection<DayOfWeek> daysOffAfter = Arrays.asList(DayOfWeek.MONDAY);
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(daysOffAfter, null, weight);
		schedule.getPatternList().add(weekendPattern);
		
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		Contract contract = createContract(false, employee);
		schedule.getContractList().add(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, weekendPattern);
		schedule.getPatternContractLineList().add(contractLine);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 24));
		schedule.getShiftDateList().add(shiftDate);
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(secondShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		secondShift.setTeamId("1");
		schedule.getShiftList().add(secondShift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift, true);	
	//	schedule.getShiftAssignmentList().add(secondShiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(1, results.size());

		Collection<ShiftConstraintDto> constraints = CollectionUtils.extractSingleton(results).getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof CoupledWeekendConstraintDto);
		assertEquals(shiftDate.getDate(), ((CoupledWeekendConstraintDto)constraint).getWeekendStartDate());
		assertEquals(RuleName.COUPLED_WEEKEND_RULE, constraint.getConstraintName());
	}
	
	@Test
	public void testWeekdayRotationPattern() {
		WeekdayRotationPattern oneOfTwoPattern = createWeekdayRotationPattern(RotationPatternType.DAYS_OFF_PATTERN,
				DayOfWeek.MONDAY, 1, 2, -1);
		schedule.getPatternList().add(oneOfTwoPattern);
		
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		Contract contract = createContract(false, employee);
		schedule.getContractList().add(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, oneOfTwoPattern);
		schedule.getPatternContractLineList().add(contractLine);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 19));
		schedule.getShiftDateList().add(shiftDate);
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));
		schedule.getShiftDateList().add(secondShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		secondShift.setTeamId("1");
		schedule.getShiftList().add(secondShift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift, true);	
		schedule.getShiftAssignmentList().add(secondShiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(2, results.size());

		Collection<ShiftConstraintDto> constraints = results.iterator().next().getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof WeekdayRotationConstraintDto);
		assertEquals(1, ((WeekdayRotationConstraintDto)constraint).getPattern().getNumberOfDays());
		assertEquals(2, ((WeekdayRotationConstraintDto)constraint).getPattern().getOutOfTotalDays());
		assertEquals(RuleName.WEEKDAY_ROTATION_PATTERN_RULE, constraint.getConstraintName());
	}

	@Test
	public void testMinHoursBetweenDays() {
		MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_BETWEEN_DAYS, true, 5, -1);
		schedule.getContractLineList().add(contractLine);
		
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		Contract contract = createContract(true, null, contractLine, teamId);
		contractLine.setContract(contract);
		schedule.getContractList().add(contract);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(1, 0, 0), new LocalTime(21, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);
		
		ShiftDate secondShiftDate = new ShiftDate();
		secondShiftDate.setDate(new LocalDate(2014, 5, 26));
		schedule.getShiftDateList().add(secondShiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		shift.setTeamId("1");
		schedule.getShiftList().add(secondShift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift, true);	
		schedule.getShiftAssignmentList().add(secondShiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(2, results.size());

		Collection<ShiftConstraintDto> constraints = results.iterator().next().getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof MinShiftConstraintDto);
		assertEquals(4*60, ((MinShiftConstraintDto)constraint).getActualValue());
		assertEquals(5*60, ((MinShiftConstraintDto)constraint).getMinValue());
		assertTrue(constraint.getInvolvedShifts().contains(shiftAssignment.getShift().getId()));
		assertEquals(RuleName.MIN_HOURS_BETWEEN_DAYS_RULE, constraint.getConstraintName());
	}
	
	@Test
	public void testCITimeOff() {
		String teamId = "1";
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(17, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);
		
		CITimeOff timeOff = new CITimeOff();
		timeOff.setDayOfWeek(shiftDate.getDayOfWeek());
		timeOff.setEmployeeId(employee.getEmployeeId());
		timeOff.setTimeWindow(shiftType.getStartTime(), shiftType.getEndTime());
		timeOff.setWeight(-1);
		schedule.getEmployeeTimeOffs().add(timeOff);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId(teamId);
		schedule.getShiftList().add(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(1, results.size());

		Collection<ShiftConstraintDto> constraints = CollectionUtils.extractSingleton(results).getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof CITimeOffShiftConstraintDto);
		assertEquals(RuleName.CI_TIME_OFF_CONSTRAINT, constraint.getConstraintName());
	}
	
	@Test
	public void testCDTimeOff() {
		String teamId = "1";
		Employee employee = createEmployee(1, "MR.", "X");
		schedule.getEmployeeList().add(employee);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(17, 0, 0), false);
		schedule.getShiftTypeList().add(shiftType);
		
		ShiftDate shiftDate = new ShiftDate();
		shiftDate.setDate(new LocalDate(2014, 5, 25));
		schedule.getShiftDateList().add(shiftDate);
		
		CDTimeOff timeOff = new CDTimeOff();
		timeOff.setEmployeeId(employee.getEmployeeId());
		timeOff.setDayOffEnd(shiftDate.getDate().toDateTimeAtStartOfDay());
		timeOff.setDayOffStart(shiftDate.getDate().toDateTimeAtStartOfDay());
		timeOff.setTimeWindow(shiftType.getStartTime(), shiftType.getEndTime());
		timeOff.setWeight(-1);
		schedule.getEmployeeTimeOffs().add(timeOff);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		schedule.getTeamAssoctiations().add(teamAssos);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId(teamId);
		schedule.getShiftList().add(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift, true);
		schedule.getShiftAssignmentList().add(shiftAssignment);
		
		qualificationManager.qualifyScheduleAssignments(schedule);
		
		Collection<ShiftQualificationDto> results = qualificationManager.getShiftQualificationResults();
		assertEquals(1, results.size());

		Collection<ShiftConstraintDto> constraints = CollectionUtils.extractSingleton(results).getCauses();
		assertEquals(1, constraints.size());
		
		ShiftConstraintDto constraint = constraints.iterator().next();
		assertTrue(constraint instanceof CDTimeOffShiftConstraintDto);
		assertEquals(RuleName.CD_TIME_OFF_CONSTRAINT, constraint.getConstraintName());
	}
	
	/**
	 * Set the weight of each rule to 1 by default
	 * 
	 */
	protected void setUpRuleWeights() {
		rosterInfo.putRuleWeightMultiplier(RuleName.CD_TIME_OFF_CONSTRAINT , 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.CI_TIME_OFF_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MAX_CONSECUTIVE_DAYS_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MAX_CONSECUTIVE_12H_DAYS_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MAX_DAYS_PER_WEEK_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MAX_HOURS_PER_DAY_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MAX_HOURS_PER_WEEK_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MIN_HOURS_BETWEEN_DAYS_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MIN_HOURS_PER_DAY_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MIN_HOURS_PER_WEEK_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MIN_HOURS_PER_TWO_WEEKS_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.REQUIRED_EMPLOYEES_MATCH_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.SKILL_MATCH_RULE, 40);
		rosterInfo.putRuleWeightMultiplier(RuleName.TEAM_ASSOCIATION_CONSTRAINT, 10);
		rosterInfo.putRuleWeightMultiplier(RuleName.TEAM_ASSOCIATION_CONSTRAINT_FLOAT, 10);
		rosterInfo.putRuleWeightMultiplier(RuleName.WEEKDAY_ROTATION_PATTERN_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.COUPLED_WEEKEND_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.DAYS_OFF_AFTER_WEEKEND_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.DAYS_OFF_BEFORE_WEEKEND_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.AVOID_DAILY_OVERTIME_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.AVOID_WEEKLY_OVERTIME_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.AVOID_TWO_WEEK_OVERTIME_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.OVERLAPPING_SHIFTS_RULE, 10);	
	}
	
	/**
	 * Set default values for the soft scoring rules
	 * in future this will be loaded from the schedule
	 * generation request
	 * 
	 * @param rosterInfo
	 */
	protected void setUpScoringRuleScoreLevels(){
		rosterInfo.setScoringRulesToScoreLevelMap(new HashMap<RuleName, Integer>());
		rosterInfo.putScoringRuleScoreLevel(RuleName.CD_PREFERENCE_RULE, 2);
		rosterInfo.putScoringRuleScoreLevel(RuleName.CI_PREFERENCE_RULE, 2);
		rosterInfo.putScoringRuleScoreLevel(RuleName.SCHEDULE_COST_RULE, 0);
		rosterInfo.putScoringRuleScoreLevel(RuleName.SCHEDULE_OVERTIME_RULE, 1);
		rosterInfo.putScoringRuleScoreLevel(RuleName.SENIORITY_RULE, 3);
		rosterInfo.putScoringRuleScoreLevel(RuleName.EXTRA_SHIFT_RULE, 3);
	}


}
