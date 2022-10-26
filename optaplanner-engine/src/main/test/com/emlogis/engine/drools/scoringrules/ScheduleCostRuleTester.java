package com.emlogis.engine.drools.scoringrules;

import com.emlogis.engine.domain.*;
import com.emlogis.engine.domain.contract.ConstraintOverride;
import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.engine.domain.contract.Contract;
import com.emlogis.engine.domain.contract.contractline.ContractLine;
import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.engine.domain.organization.TeamAssociation;
import com.emlogis.engine.drools.ConstraintTesterBase;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.kie.api.runtime.rule.FactHandle;
import org.optaplanner.core.api.score.buildin.bendable.BendableScoreHolder;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;

import static org.junit.Assert.assertEquals;

public class ScheduleCostRuleTester extends ConstraintTesterBase {
	public final static String SCHEDULE_COST_RULE = "ScheduleCost";
	public final static String SCHEDULE_DAILY_OVERTIME_COST = "ScheduleDailyOvertimeCost";

	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setTwoWeekOvertimeStartDate(planningWindowStart.minusDays(6));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
	}

	@Test
	public void testCostOneEmployee() {
		Employee employee = createEmployee(1, "MR.", "X", 2);
		int empRate = 5000;
		employee.setHourlyRate(empRate); //$50.00/h
		kSession.insert(employee);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int scheduleCost = getWeightedSumForConstraint(SCHEDULE_COST_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(-empRate * shift.getShiftDurationHours(), scheduleCost);
	}

	@Test
	public void testCostOneEmployeeTwoShifts() {
		Employee employee = createEmployee(1, "MR.", "X", 2);
		int empRate = 5000;
		employee.setHourlyRate(empRate); //$50.00/h
		kSession.insert(employee);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		kSession.insert(secondShiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		kSession.insert(secondShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
		kSession.insert(secondShiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int scheduleCost = getWeightedSumForConstraint(SCHEDULE_COST_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(- (empRate * shift.getShiftDurationHours() + empRate * secondShift.getShiftDurationHours()), scheduleCost);
	}

	@Test
	public void testCostTwoEmployees() {
		Employee employee = createEmployee(1, "MR.", "X", 2);
		int empRate = 5000;
		employee.setHourlyRate(empRate); //$50.00/h
		kSession.insert(employee);

		Employee secondEmployee = createEmployee(2, "MR.", "Y", 4);
		int secondEmpRate = 10000;
		secondEmployee.setHourlyRate(secondEmpRate); //$100.00/h
		kSession.insert(secondEmployee);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0,shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(secondEmployee, 1, shift);
		kSession.insert(secondShiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int scheduleCost = getWeightedSumForConstraint(SCHEDULE_COST_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(- (empRate * shift.getShiftDurationHours() + secondEmpRate * shift.getShiftDurationHours()), scheduleCost);
	}
	

	@Test
	public void testCostDailyOvertimeOneEmployee() {
		Employee employee = createEmployee(1, "MR.", "X", 2);
		int empRate = 5000;
		employee.setHourlyRate(empRate); //$50.00/h
		kSession.insert(employee);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		ContractLine line = createMaxContractLine(ContractLineType.DAILY_OVERTIME, true, 8 * 60 , 1);
		kSession.insert(line);
		
		Contract contract = createContract(false, employee, line);
		kSession.insert(contract);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int scheduleCost = getWeightedSumForConstraint(SCHEDULE_COST_RULE,
				scoreHolder.getConstraintMatchTotals());
		int expectedCost = (int) (empRate * 8 + 1.5 * empRate * 4) ;
		assertEquals(-expectedCost, scheduleCost);
	}
	
	@Test
	public void testCostWeeklyOvertimeOneEmployee() {
		Employee employee = createEmployee(1, "MR.", "X", 2);
		int empRate = 5000;
		employee.setHourlyRate(empRate); //$50.00/h
		kSession.insert(employee);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		ShiftDate secondDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(secondDate);
		
		ShiftDate thirdDate = new ShiftDate(new LocalDate(2014, 5, 28));
		kSession.insert(thirdDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		Shift secondShift = createShift("1", 0, secondDate, shiftType, 1);
		kSession.insert(secondShift);
		
		Shift thirdShift = createShift("1", 0, thirdDate, shiftType, 1);
		kSession.insert(thirdShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 1, secondShift);
		kSession.insert(secondShiftAssignment);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 2, thirdShift);
		kSession.insert(thirdShiftAssignment);
		
		int overtimeStart = 30;
		ContractLine line = createMaxContractLine(ContractLineType.WEEKLY_OVERTIME, true, overtimeStart * 60 , 1);
		kSession.insert(line);
		
		Contract contract = createContract(false, employee, line);
		kSession.insert(contract);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int scheduleCost = getWeightedSumForConstraint(SCHEDULE_COST_RULE,
				scoreHolder.getConstraintMatchTotals());
		int expectedCost = (int) (empRate * overtimeStart  + 1.5 * empRate * 6) ;
		assertEquals(-expectedCost, scheduleCost);
	}
	
	@Test
	public void testCostDailyAndWeeklyOvertimeOneEmployee() {
		Employee employee = createEmployee(1, "MR.", "X", 2);
		int empRate = 5000;
		employee.setHourlyRate(empRate); //$50.00/h
		kSession.insert(employee);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		ShiftDate secondDate = new ShiftDate(new LocalDate(2014, 5, 27));
		kSession.insert(secondDate);
		
		ShiftDate thirdDate = new ShiftDate(new LocalDate(2014, 5, 28));
		kSession.insert(thirdDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		Shift secondShift = createShift("1", 0, secondDate, shiftType, 1);
		kSession.insert(secondShift);
		
		Shift thirdShift = createShift("1", 0, thirdDate, shiftType, 1);
		kSession.insert(thirdShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 1, secondShift);
		kSession.insert(secondShiftAssignment);
		
		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 2, thirdShift);
		kSession.insert(thirdShiftAssignment);
		
		int weeklyOvertimeStart = 30;
		ContractLine weeklyLine = createMaxContractLine(ContractLineType.WEEKLY_OVERTIME, true, weeklyOvertimeStart * 60 , 1);
		kSession.insert(weeklyLine);
		
		Contract contract = createContract(false, employee, weeklyLine);
		kSession.insert(contract);

		int dailyOvertimeStart = 11;
		ContractLine dailyLine = createMaxContractLine(ContractLineType.DAILY_OVERTIME, true, dailyOvertimeStart * 60 , 1);
		dailyLine.setContract(contract);
		kSession.insert(dailyLine);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int scheduleCost = getWeightedSumForConstraint(SCHEDULE_COST_RULE,
				scoreHolder.getConstraintMatchTotals());
		int expectedCost = (int) (empRate * weeklyOvertimeStart  + 1.5 * empRate * 6) ;
		assertEquals(-expectedCost, scheduleCost);
	}
	
	@Test
	public void testCostTwoWeekOvertimeOneEmployee() {
		long teamId = 1;
		Employee employee = createEmployee(1, "MR.", "X");
		int empRate = 5000;
		employee.setHourlyRate(empRate); //$50.00/h
		
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
		
		int overtimeStart = 59;
		
		ContractLine line = createMaxContractLineInHours(ContractLineType.TWO_WEEK_OVERTIME, true, overtimeStart, -1);
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
		int scheduleCost = getWeightedSumForConstraint(SCHEDULE_COST_RULE,
				scoreHolder.getConstraintMatchTotals());
		int expectedCost = (int) (empRate * overtimeStart + 1.5 * empRate) ;
		assertEquals(-expectedCost, scheduleCost);
		
	}

}
