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
import com.emlogis.engine.domain.contract.ConstraintOverride;
import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.engine.domain.contract.Contract;
import com.emlogis.engine.domain.contract.contractline.ContractLine;
import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.engine.domain.organization.TeamAssociation;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.drools.ConstraintTesterBase;

public class OvertimeConstraintTester extends ConstraintTesterBase {

	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(14));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
	}
	
	@Test
	public void testAvoidWeeklyOvertimeOn() {
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		
		ContractLine line = createMaxContractLineInHours(ContractLineType.WEEKLY_OVERTIME, true, 20, -1);
		kSession.insert(line);
		
		Contract contract = createContract(false, employee, line);
		line.setContract(contract);
		kSession.insert(contract);
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.AVOID_OVERTIME);
		kSession.insert(override);

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
		kSession.insert(employee);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.AVOID_WEEKLY_OVERTIME_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(-1, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testAvoidWeeklyOvertimeOff() {
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		
//		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.AVOID_OVERTIME);
//		kSession.insert(override); //Commented out to show intent of test

		ContractLine line = createMaxContractLineInHours(ContractLineType.WEEKLY_OVERTIME, true, 20, -1);
		kSession.insert(line);
		
		Contract contract = createContract(false, employee, line);
		line.setContract(contract);
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
		kSession.insert(employee);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.AVOID_WEEKLY_OVERTIME_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testAvoidTwoWeekOvertimeOn() {
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		
		ShiftDate firstShiftDate = new ShiftDate(new LocalDate(2014,06,01));
		kSession.insert(firstShiftDate);
		
		ShiftDate lastShiftDate = new ShiftDate(new LocalDate(2014,06,7));
		kSession.insert(lastShiftDate);
		kSession.insert(lastShiftDate.plusDays(1)); //Need the start of next period
		
		ShiftDate startTwoWeek = new ShiftDate(new LocalDate(2014,05,25));
		kSession.insert(startTwoWeek);
		
		rosterInfo.setTwoWeekOvertimeStartDate(startTwoWeek);
		rosterInfo.setFirstShiftDate(firstShiftDate);
		rosterInfo.setLastShiftDate(lastShiftDate);
		rosterInfo.setPlanningWindowStart(firstShiftDate);
		kSession.update(rosterHandle, rosterInfo);
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.AVOID_OVERTIME);
		kSession.insert(override);
		
		ContractLine line = createMaxContractLineInHours(ContractLineType.TWO_WEEK_OVERTIME, true, 59, -1);
		kSession.insert(line);
		
		Contract contract = createContract(false, employee, line);
		line.setContract(contract);
		kSession.insert(contract);

		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		kSession.insert(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 30));
		kSession.insert(thirdShiftDate);
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 6, 2));
		kSession.insert(fourthShiftDate);
		
		ShiftDate fifthShiftDate = new ShiftDate(new LocalDate(2014, 6, 5));
		kSession.insert(fifthShiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		
		Shift thirdShift = createShift("4", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);
		
		Shift fourthShift = createShift("5", 0, fourthShiftDate, shiftType, 1);
		kSession.insert(fourthShift);
		
		Shift fifthShift = createShift("6", 0, fifthShiftDate, shiftType, 1);
		kSession.insert(fifthShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);
		
		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
		kSession.insert(fourthShiftAssignment);
		
		ShiftAssignment fifthShiftAssignment = createShiftAssignment(employee, 0, fifthShift);
		kSession.insert(fifthShiftAssignment);

		kSession.insert(shiftType);
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
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.AVOID_TWO_WEEK_OVERTIME_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(-1, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testAvoidTwoWeekOvertimeWrongStartWeek() {
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		
		ShiftDate firstShiftDate = new ShiftDate(new LocalDate(2014,06,8));
		kSession.insert(firstShiftDate);
		
		ShiftDate lastShiftDate = new ShiftDate(new LocalDate(2014,06,14));
		kSession.insert(lastShiftDate);
		kSession.insert(lastShiftDate.plusDays(1)); //Need the start of next period
		
		ShiftDate startTwoWeek = new ShiftDate(new LocalDate(2014,05,25));
		kSession.insert(startTwoWeek);
		
		rosterInfo.setTwoWeekOvertimeStartDate(startTwoWeek);
		rosterInfo.setFirstShiftDate(firstShiftDate);
		rosterInfo.setLastShiftDate(lastShiftDate);
		rosterInfo.setPlanningWindowStart(firstShiftDate);
		kSession.update(rosterHandle, rosterInfo);
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.AVOID_OVERTIME);
		kSession.insert(override);
		
		ContractLine line = createMaxContractLineInHours(ContractLineType.TWO_WEEK_OVERTIME, true, 59, -1);
		kSession.insert(line);
		
		Contract contract = createContract(false, employee, line);
		line.setContract(contract);
		kSession.insert(contract);

		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		kSession.insert(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 6, 1));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 6, 2));
		
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 6, 6));
		kSession.insert(thirdShiftDate);
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 6, 8));
		kSession.insert(fourthShiftDate);
		
		ShiftDate fifthShiftDate = new ShiftDate(new LocalDate(2014, 6, 11));
		kSession.insert(fifthShiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		
		Shift thirdShift = createShift("4", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);
		
		Shift fourthShift = createShift("5", 0, fourthShiftDate, shiftType, 1);
		kSession.insert(fourthShift);
		
		Shift fifthShift = createShift("6", 0, fifthShiftDate, shiftType, 1);
		kSession.insert(fifthShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);
		
		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
		kSession.insert(fourthShiftAssignment);
		
		ShiftAssignment fifthShiftAssignment = createShiftAssignment(employee, 0, fifthShift);
		kSession.insert(fifthShiftAssignment);

		kSession.insert(shiftType);
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
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.AVOID_TWO_WEEK_OVERTIME_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testAvoidTwoWeekOvertimeTwoWeekForward() {
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		
		ShiftDate firstShiftDate = new ShiftDate(new LocalDate(2014,06,15));
		kSession.insert(firstShiftDate);
		
		ShiftDate lastShiftDate = new ShiftDate(new LocalDate(2014,06,21));
		kSession.insert(lastShiftDate);
		kSession.insert(lastShiftDate.plusDays(1)); //Need the start of next period
		
		ShiftDate startTwoWeek = new ShiftDate(new LocalDate(2014,05,25));
		kSession.insert(startTwoWeek);
		
		rosterInfo.setTwoWeekOvertimeStartDate(startTwoWeek);
		rosterInfo.setFirstShiftDate(firstShiftDate);
		rosterInfo.setLastShiftDate(lastShiftDate);
		rosterInfo.setPlanningWindowStart(firstShiftDate);
		kSession.update(rosterHandle, rosterInfo);
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.AVOID_OVERTIME);
		kSession.insert(override);
		
		ContractLine line = createMaxContractLineInHours(ContractLineType.TWO_WEEK_OVERTIME, true, 59, -1);
		kSession.insert(line);
		
		Contract contract = createContract(false, employee, line);
		line.setContract(contract);
		kSession.insert(contract);

		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		kSession.insert(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 6, 8));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 6, 9));
		
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 6, 13));
		kSession.insert(thirdShiftDate);
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 6, 15));
		kSession.insert(fourthShiftDate);
		
		ShiftDate fifthShiftDate = new ShiftDate(new LocalDate(2014, 6, 18));
		kSession.insert(fifthShiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		
		Shift thirdShift = createShift("4", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);
		
		Shift fourthShift = createShift("5", 0, fourthShiftDate, shiftType, 1);
		kSession.insert(fourthShift);
		
		Shift fifthShift = createShift("6", 0, fifthShiftDate, shiftType, 1);
		kSession.insert(fifthShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);
		
		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
		kSession.insert(fourthShiftAssignment);
		
		ShiftAssignment fifthShiftAssignment = createShiftAssignment(employee, 0, fifthShift);
		kSession.insert(fifthShiftAssignment);

		kSession.insert(shiftType);
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
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.AVOID_TWO_WEEK_OVERTIME_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(-1, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testAvoidTwoWeekOvertimeFirstWeekOff() {
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		ShiftDate firstShiftDate = new ShiftDate(new LocalDate(2014,05,25));
		kSession.insert(firstShiftDate);
		
		ShiftDate lastShiftDate = new ShiftDate(new LocalDate(2014,05,31));
		kSession.insert(lastShiftDate);
		kSession.insert(lastShiftDate.plusDays(1)); //Need the start of next period
		
		ShiftDate startTwoWeek = new ShiftDate(new LocalDate(2014,05,25));
		kSession.insert(startTwoWeek);
		
		rosterInfo.setTwoWeekOvertimeStartDate(startTwoWeek);
		rosterInfo.setFirstShiftDate(firstShiftDate);
		rosterInfo.setLastShiftDate(lastShiftDate);
		rosterInfo.setPlanningWindowStart(firstShiftDate);
		kSession.update(rosterHandle, rosterInfo);
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.AVOID_OVERTIME);
		kSession.insert(override); //Overridden on purpose to show intent

		ContractLine line = createMaxContractLineInHours(ContractLineType.TWO_WEEK_OVERTIME, true, 59, -1);
		kSession.insert(line);
		
		Contract contract = createContract(false, employee, line);
		line.setContract(contract);
		kSession.insert(contract);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		kSession.insert(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(thirdShiftDate);
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
		kSession.insert(fourthShiftDate);
		
		ShiftDate fifthShiftDate = new ShiftDate(new LocalDate(2014, 5, 29));
		kSession.insert(fifthShiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		
		Shift thirdShift = createShift("4", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);
		
		Shift fourthShift = createShift("5", 0, fourthShiftDate, shiftType, 1);
		kSession.insert(fourthShift);
		
		Shift fifthShift = createShift("6", 0, fifthShiftDate, shiftType, 1);
		kSession.insert(fifthShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);
		
		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
		kSession.insert(fourthShiftAssignment);
		
		ShiftAssignment fifthShiftAssignment = createShiftAssignment(employee, 0, fifthShift);
		kSession.insert(fifthShiftAssignment);

		kSession.insert(shiftType);
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
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.AVOID_TWO_WEEK_OVERTIME_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testAvoidTwoWeekOvertimeNoConstraintOverride() {
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.setTwoWeekOvertimeStartDate(rosterInfo.getFirstShiftDate());
		kSession.update(rosterHandle, rosterInfo);
		
//		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.AVOID_OVERTIME);
//		kSession.insert(override); //Overridden on purpose to show intent

		ContractLine line = createMaxContractLineInHours(ContractLineType.TWO_WEEK_OVERTIME, true, 59, -1);
		kSession.insert(line);
		
		Contract contract = createContract(false, employee, line);
		line.setContract(contract);
		kSession.insert(contract);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		kSession.insert(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 30));
		kSession.insert(thirdShiftDate);
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 6, 2));
		kSession.insert(fourthShiftDate);
		
		ShiftDate fifthShiftDate = new ShiftDate(new LocalDate(2014, 6, 5));
		kSession.insert(fifthShiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		
		Shift thirdShift = createShift("4", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);
		
		Shift fourthShift = createShift("5", 0, fourthShiftDate, shiftType, 1);
		kSession.insert(fourthShift);
		
		Shift fifthShift = createShift("6", 0, fifthShiftDate, shiftType, 1);
		kSession.insert(fifthShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);
		
		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
		kSession.insert(fourthShiftAssignment);
		
		ShiftAssignment fifthShiftAssignment = createShiftAssignment(employee, 0, fifthShift);
		kSession.insert(fifthShiftAssignment);

		kSession.insert(shiftType);
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
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.AVOID_TWO_WEEK_OVERTIME_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	
	@Test
	public void testAvoidDailyOvertimeOn() {
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		
		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.AVOID_OVERTIME);
		kSession.insert(override);
		
		ContractLine line = createMaxContractLineInHours(ContractLineType.DAILY_OVERTIME, true, 8, -1);
		kSession.insert(line);
		
		Contract contract = createContract(false, employee, line);
		line.setContract(contract);
		kSession.insert(contract);

		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		kSession.insert(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(18, 10, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		kSession.insert(shiftType);
		kSession.insert(shiftDate);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.AVOID_DAILY_OVERTIME_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(-1, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testAvoidDailyOvertimeOff() {
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		
//		ConstraintOverride override = new ConstraintOverride(employee, ConstraintOverrideType.AVOID_OVERTIME);
//		kSession.insert(override); //Commented out to show intent of test

		ContractLine line = createMaxContractLineInHours(ContractLineType.DAILY_OVERTIME, true, 8, -1);
		kSession.insert(line);
		
		Contract contract = createContract(false, employee, line);
		line.setContract(contract);
		kSession.insert(contract);
		
		TeamAssociation teamAssos = createTeamAssociation(employee, teamId);
		kSession.insert(teamAssos);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(8, 10, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		kSession.insert(shiftType);
		kSession.insert(shiftDate);
		kSession.insert(shift);
		kSession.insert(shiftAssignment);
		kSession.insert(employee);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.AVOID_DAILY_OVERTIME_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}

}
