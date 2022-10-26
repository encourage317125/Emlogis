package com.emlogis.engine.drools;

import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.Employee;
import com.emlogis.engine.domain.Shift;
import com.emlogis.engine.domain.ShiftAssignment;
import com.emlogis.engine.domain.ShiftDate;
import com.emlogis.engine.domain.ShiftSkillRequirement;
import com.emlogis.engine.domain.ShiftType;
import com.emlogis.engine.domain.Skill;
import com.emlogis.engine.domain.SkillProficiency;
import com.emlogis.engine.domain.contract.Contract;
import com.emlogis.engine.domain.contract.contractline.ContractLine;
import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.engine.domain.contract.contractline.ContractScope;
import com.emlogis.engine.domain.contract.contractline.MinMaxContractLine;
import com.emlogis.engine.domain.contract.contractline.PatternContractLine;
import com.emlogis.engine.domain.contract.patterns.CompleteWeekendWorkPattern;
import com.emlogis.engine.domain.contract.patterns.Pattern;
import com.emlogis.engine.domain.contract.patterns.WeekdayRotationPattern;
import com.emlogis.engine.domain.contract.patterns.WeekdayRotationPattern.RotationPatternType;
import com.emlogis.engine.domain.organization.TeamAssociation;
import com.emlogis.engine.domain.organization.TeamAssociationType;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.domain.timeoff.CDPreference;
import com.emlogis.engine.domain.timeoff.CDTimeOff;
import com.emlogis.engine.domain.timeoff.CIPreference;
import com.emlogis.engine.domain.timeoff.CITimeOff;

public class TestDataUtility {
	public SkillProficiency createSkillProficiency(Employee employee, Skill skill) {
		return createSkillProficiency(employee, skill, false);
	}

	public SkillProficiency createSkillProficiency(Employee employee, Skill skill, boolean isPrimary) {
		SkillProficiency skillProf = new SkillProficiency();
		skillProf.setEmployee(employee);
		skillProf.setSkill(skill);
		skillProf.setPrimarySkill(isPrimary);
		return skillProf;
	}

	public Skill createSkill(long code, String name) {
		Skill skill = new Skill();
		skill.setCode(String.valueOf(code));
		skill.setName(name);
		return skill;
	}

	public MinMaxContractLine createMinContractLineInHours(ContractLineType type, boolean isEnabled, int value,
			int weight) {
		return createMinContractLine(type, isEnabled, value * 60, weight);
	}

	public MinMaxContractLine createMinContractLine(ContractLineType type, boolean isEnabled, int value, int weight) {
		MinMaxContractLine contractLine = new MinMaxContractLine();
		contractLine.setContractLineType(type);
		contractLine.setMinimumEnabled(isEnabled);
		contractLine.setMinimumValue(value);
		contractLine.setMinimumWeight(weight);
		return contractLine;
	}

	public Contract createContract(boolean isTeamContract, Employee employee, ContractLine contractLine, long teamId) {
		Contract contract = new Contract();
		contract.setScope(isTeamContract ? ContractScope.TeamContract : ContractScope.EmployeeContract);
		if (!isTeamContract) {
			contract.setContractRefId(employee.getEmployeeId());
		} else {
			contract.setContractRefId(String.valueOf(teamId));
		}
		if (contractLine != null)
			contractLine.setContract(contract);
		return contract;
	}

	public Contract createContract(boolean isTeamContract, Employee employee) {
		return createContract(isTeamContract, employee, null, 0);
	}

	public Contract createContract(boolean isTeamContract, Employee employee, ContractLine contractLine) {
		return createContract(isTeamContract, employee, contractLine, 0);
	}

	public TeamAssociation createTeamAssociation(Employee employee, long teamId) {
		return createTeamAssociation(employee, teamId, TeamAssociationType.ON, true);
	}

	public TeamAssociation createTeamAssociation(Employee employee, long teamId, TeamAssociationType type,
			boolean isHomeTeam) {
		return createTeamAssociation(employee, String.valueOf(teamId), type, isHomeTeam);
	}
	
	public TeamAssociation createTeamAssociation(Employee employee, String teamId) {
		return createTeamAssociation(employee, teamId, TeamAssociationType.ON, true);
	}
	
	public TeamAssociation createTeamAssociation(Employee employee, String teamId, TeamAssociationType type,
			boolean isHomeTeam) {
		TeamAssociation teamAssos = new TeamAssociation();
		teamAssos.setEmployee(employee);
		teamAssos.setTeamId(String.valueOf(teamId));
		teamAssos.setType(type);
		teamAssos.setHomeTeam(isHomeTeam);
		return teamAssos;
	}

	public MinMaxContractLine createMaxContractLineInHours(ContractLineType type, boolean isEnabled, int value,
			int weight) {
		return createMaxContractLine(type, isEnabled, value * 60, weight);
	}

	public MinMaxContractLine createMaxContractLine(ContractLineType type, boolean isEnabled, int value, int weight) {
		MinMaxContractLine contractLine = new MinMaxContractLine();
		contractLine.setContractLineType(type);
		contractLine.setMaximumEnabled(isEnabled);
		contractLine.setMaximumValue(value);
		contractLine.setMaximumWeight(weight);
		return contractLine;
	}

	public PatternContractLine createPatternContractLine(Contract contract, Pattern pattern) {
		PatternContractLine pcl = new PatternContractLine();
		pcl.setContract(contract);
		pcl.setPattern(pattern);
		return pcl;
	}

	public WeekdayRotationPattern createWeekdayRotationPattern(RotationPatternType type, DayOfWeek dayOfWeek,
			int numOfDays, int numTotalDays, int weight) {
		WeekdayRotationPattern pattern = new WeekdayRotationPattern();
		pattern.setRotationType(type);
		pattern.setDayOfWeek(dayOfWeek);
		pattern.setNumberOfDays(numOfDays);
		pattern.setOutOfTotalDays(numTotalDays);
		pattern.setWeight(weight);
		return pattern;
	}

	public CompleteWeekendWorkPattern createWeekendWorkPattern(Collection<DayOfWeek> daysOffAfter,
			Collection<DayOfWeek> daysOffBefore, int weight) {
		CompleteWeekendWorkPattern weekendPattern = new CompleteWeekendWorkPattern();
		weekendPattern.setWeight(weight);
		weekendPattern.setDaysOffAfter(daysOffAfter);
		weekendPattern.setDaysOffBefore(daysOffBefore);
		return weekendPattern;
	}

	public Employee createEmployee(long id, String firstName, String lastName, int seniority) {
		Employee employee = new Employee();
		employee.setFirstName(firstName);
		employee.setLastName(lastName);
		employee.setEmployeeId(String.valueOf(id));
		employee.setSeniority(seniority);
		return employee;
	}

	public Employee createEmployee(long id, String firstName, String lastName) {
		return createEmployee(id, firstName, lastName, 1);
	}

	public ShiftType createShiftType(long id, LocalTime startTime, LocalTime endTime, boolean isExcess) {
		ShiftType shiftType = new ShiftType();
		shiftType.setExcessShift(isExcess);
		shiftType.setShiftId(String.valueOf(id));
		shiftType.setStartTime(startTime);
		shiftType.setEndTime(endTime);
		return shiftType;
	}

	public Shift createShift(String siteRequirementId, int index, ShiftDate shiftStartDate, ShiftType shiftType,
			int requiredEmployeeSize) {
		return createShift(siteRequirementId, index, shiftStartDate, shiftStartDate, shiftType, requiredEmployeeSize,
				0L);
	}

	public Shift createShift(String siteRequirementId, int index, ShiftDate shiftStartDate, ShiftDate shiftEndDate,
			ShiftType shiftType, int requiredEmployeeSize) {
		return createShift(siteRequirementId, index, shiftStartDate, shiftEndDate, shiftType, requiredEmployeeSize, 0L);
	}

	public Shift createShift(String siteRequirementId, int index, ShiftDate shiftStartDate, ShiftType shiftType,
			int requiredEmployeeSize, long teamId) {
		return createShift(siteRequirementId, index, shiftStartDate, shiftStartDate, shiftType, requiredEmployeeSize,
				teamId);
	}

	public Shift createShift(String siteRequirementId, int index, ShiftDate shiftStartDate, ShiftDate shiftEndDate,
			ShiftType shiftType, int requiredEmployeeSize, long teamId, String skillId) {
		return createShift(siteRequirementId, index, shiftStartDate, shiftEndDate, shiftType, requiredEmployeeSize, teamId,
				"", false);
	}
	
	public Shift createShift(String siteRequirementId, int index, ShiftDate shiftStartDate, ShiftDate shiftEndDate,
			ShiftType shiftType, int requiredEmployeeSize, long teamId, String skillId, boolean isExcess) {
		Shift shift = new Shift();
		shift.setId(siteRequirementId);
		shift.setIndex(index);
		shift.setShiftType(shiftType);
		
		shift.setExcessShift(shiftType.isExcessShift());
		shift.setStartDateTime(shiftStartDate.getDate().toDateTime(shiftType.getStartTime(), DateTimeZone.UTC));
		shift.setEndDateTime(shiftEndDate.getDate().toDateTime(shiftType.getEndTime(), DateTimeZone.UTC));
		
		shift.setRequiredEmployeeSize(requiredEmployeeSize);
		shift.setTeamId(String.valueOf(teamId));
		shift.setSkillId(skillId);
		shift.setExcessShift(isExcess);
		
		return shift;
	}
	

	public Shift createShift(String siteDemandId, int index, ShiftDate shiftStartDate, ShiftDate shiftEndDate,
			ShiftType shiftType, int requiredEmployeeSize, long teamId) {
		return createShift(siteDemandId, index, shiftStartDate, shiftEndDate, shiftType, requiredEmployeeSize, teamId,
				"");
	}

	public ShiftAssignment createShiftAssignment(Employee employee, int indexInShift, Shift shift) {
		return createShiftAssignment(employee, indexInShift, shift, false);
	}
	
	public ShiftAssignment createShiftAssignment(Employee employee, int indexInShift, Shift shift, boolean beingQualified) {
		ShiftAssignment shiftAssignment = new ShiftAssignment();
		shiftAssignment.setEmployee(employee);
		shiftAssignment.setIndexInShift(indexInShift);
		shiftAssignment.setShift(shift);
		shiftAssignment.getShift().setBeingQualified(beingQualified);
		return shiftAssignment;
	}
	
	public ShiftSkillRequirement createShiftTypeSkillRequirement(Shift shift, Skill skill) {
		ShiftSkillRequirement shiftSkilReq = new ShiftSkillRequirement(shift, skill);
		return shiftSkilReq;
	}

	protected int getNumOfConstraintMatches(final RuleName constraintName,
			Collection<ConstraintMatchTotal> constraintMatches) {
		ConstraintMatchTotal cmTotal = CollectionUtils.find(constraintMatches, new Predicate<ConstraintMatchTotal>() {
			@Override
			public boolean evaluate(ConstraintMatchTotal arg0) {
				return arg0.getConstraintName().equals(constraintName.getValue());
			}
		});

		if (cmTotal == null) {
			return 0;
		}
		return cmTotal.getWeightTotalAsNumber().intValue();
	}

	protected int getWeightedSumForConstraint(final String constraintName,
			Collection<ConstraintMatchTotal> constraintMatches) {
		int sum = 0;
		for (ConstraintMatchTotal cmt : constraintMatches) {
			if (cmt.getConstraintName().equals(constraintName)) {
				sum += cmt.getWeightTotalAsNumber().intValue();
			}
		}
		return sum;
	}

	protected CITimeOff createCITimeOff(long employeeId, LocalTime startTime, LocalTime endTime, DayOfWeek dayOfWeek,
			int weight) {
		return createCITimeOff(String.valueOf(employeeId), startTime, endTime, dayOfWeek, weight);
	}

	protected CITimeOff createCITimeOff(String employeeId, LocalTime startTime, LocalTime endTime, DayOfWeek dayOfWeek,
			int weight) {
		CITimeOff ciTimeOff = new CITimeOff();
		ciTimeOff.setEmployeeId(String.valueOf(employeeId));
		ciTimeOff.setTimeWindow(startTime, endTime);
		ciTimeOff.setDayOfWeek(dayOfWeek);
		ciTimeOff.setWeight(weight);
		return ciTimeOff;
	}

	protected CITimeOff createCITimeOff(long employeeId, DayOfWeek dayOfWeek, int weight) {
		return createCITimeOff(employeeId, null, null, dayOfWeek, weight);
	}

	protected CITimeOff createCITimeOff(String employeeId, DayOfWeek dayOfWeek, int weight) {
		return createCITimeOff(employeeId, null, null, dayOfWeek, weight);
	}

	protected CDTimeOff createCDTimeOff(long employeeId, LocalDate dayOffStart, LocalDate dayOffEnd,
			LocalTime startTime, LocalTime endTime, int weight) {
		return createCDTimeOff(String.valueOf(employeeId), dayOffStart, dayOffEnd, startTime, endTime, weight);
	}

	protected CDTimeOff createCDTimeOff(String employeeId, LocalDate dayOffStart, LocalDate dayOffEnd,
			LocalTime startTime, LocalTime endTime, int weight) {
		CDTimeOff cdTimeOff = new CDTimeOff();
		cdTimeOff.setEmployeeId(employeeId);
		cdTimeOff.setDayOffStart(dayOffStart.toDateTimeAtStartOfDay(DateTimeZone.UTC));
		cdTimeOff.setDayOffEnd(dayOffEnd.toDateTimeAtStartOfDay(DateTimeZone.UTC));
		cdTimeOff.setTimeWindow(startTime, endTime);
		cdTimeOff.setWeight(weight);
		return cdTimeOff;
	}
	
	protected CDTimeOff createCDTimeOff(long employeeId, LocalDate dayOffStart, LocalDate dayOffEnd, int weight) {
		return createCDTimeOff(employeeId, dayOffStart, dayOffEnd, null, null, weight);
	}

	protected CDTimeOff createCDTimeOff(long employeeId, LocalDate dayOffStart, LocalTime startTime, LocalTime endTime,
			int weight) {
		return createCDTimeOff(employeeId, dayOffStart, dayOffStart, startTime, endTime, weight);
	}

	protected CDTimeOff createCDTimeOff(long employeeId, LocalDate dayOffStart, int weight) {
		return createCDTimeOff(employeeId, dayOffStart, dayOffStart, null, null, weight);
	}

	protected CDTimeOff createCDTimeOff(String employeeId, LocalDate dayOffStart, LocalDate dayOffEnd, int weight) {
		return createCDTimeOff(employeeId, dayOffStart, dayOffEnd, null, null, weight);
	}

	protected CDTimeOff createCDTimeOff(String employeeId, LocalDate dayOffStart, LocalTime startTime,
			LocalTime endTime, int weight) {
		return createCDTimeOff(employeeId, dayOffStart, dayOffStart, startTime, endTime, weight);
	}

	protected CDTimeOff createCDTimeOff(String employeeId, LocalDate dayOffStart, int weight) {
		return createCDTimeOff(employeeId, dayOffStart, dayOffStart, null, null, weight);
	}
	
	protected CDPreference createCDPreference(String employeeId, LocalDate prefStart, LocalDate prefEnd,
			LocalTime startTime, LocalTime endTime, int weight) {
		CDPreference cdPref = new CDPreference();
		cdPref.setEmployeeId(employeeId);
		cdPref.setDayOffStart(prefStart.toDateTimeAtStartOfDay());
		cdPref.setDayOffEnd(prefEnd.toDateTimeAtStartOfDay());
		cdPref.setTimeWindow(startTime, endTime);
		cdPref.setWeight(weight);
		return cdPref;
	}
	
	protected CDPreference createCDPreference(String employeeId, LocalDate prefStart, LocalDate prefEnd, int weight) {
		return createCDPreference(employeeId, prefStart, prefEnd, null,  null, weight);
	}
	
	protected CDPreference createCDPreference(String employeeId, LocalDate prefStart, int weight) {
		return createCDPreference(employeeId, prefStart, prefStart, null,  null, weight);
	}
	
	protected CIPreference createCIPreference(String employeeId, DayOfWeek dow,
			LocalTime startTime, LocalTime endTime, int weight) {
		CIPreference ciPref = new CIPreference();
		ciPref.setEmployeeId(employeeId);
		ciPref.setDayOfWeek(dow);
		ciPref.setTimeWindow(startTime, endTime);
		ciPref.setWeight(weight);
		return ciPref;
	}
	
	protected CIPreference createCIPreference(String employeeId, DayOfWeek dow, int weight) {
		return createCIPreference(employeeId, dow, null, null, weight);
	}
	
	
}
