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
import com.emlogis.engine.domain.ShiftType;
import com.emlogis.engine.domain.WeekendDefinition;
import com.emlogis.engine.domain.contract.Contract;
import com.emlogis.engine.domain.contract.contractline.PatternContractLine;
import com.emlogis.engine.domain.contract.patterns.CompleteWeekendWorkPattern;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.drools.ConstraintTesterBase;

public class WeekendConstraintsTester extends ConstraintTesterBase {
	
	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 18));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(6));

		rosterInfo.setWeekendDefinition(WeekendDefinition.SATURDAY_SUNDAY);

		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
		kSession.insert(planningWindowStart);
		kSession.insert(rosterInfo.getLastShiftDate());


	}

	private void setUpWeekendPatternTwoDayData(Collection<DayOfWeek> daysOffAfter, Collection<DayOfWeek> daysOffBefore,
			LocalDate dayOne, LocalDate dayTwo, int weight) {
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(daysOffAfter, daysOffBefore, weight);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, weekendPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(dayOne);

		ShiftDate secondShiftDate = new ShiftDate(dayTwo);

		// Add shiftdates for start of weekend
		kSession.insert(shiftDate.minusDays(1));
		kSession.insert(secondShiftDate.minusDays(1));

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

	private void setUpWeekendPatternTwoDayData(LocalDate dayOne, LocalDate dayTwo, int weight) {
		setUpWeekendPatternTwoDayData(null, null, dayOne, dayTwo, weight);
	}

	@Test
	public void testDaysBeforeWeekendSatisfied() {
		int weight = -1;
		Collection<DayOfWeek> daysOffBefore = Arrays.asList(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

		setUpWeekendPatternTwoDayData(null, daysOffBefore, new LocalDate(2014, 5, 17), new LocalDate(2014, 5, 18),
				weight);

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.DAYS_OFF_BEFORE_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}

	@Test
	public void testDaysBeforeWeekendSatisfiedMon() {
		int weight = -1;
		Collection<DayOfWeek> daysOffBefore = Arrays.asList(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(null, daysOffBefore, weight);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, weekendPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 17));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 18));

		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 12));
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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.DAYS_OFF_BEFORE_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}

	@Test
	public void testDaysBeforeWeekendOneDayViolated() {
		int weight = -1;
		Collection<DayOfWeek> daysOffBefore = Arrays.asList(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(null, daysOffBefore, weight);

		Employee employee = createEmployee(1, "MR.", "X");

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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.DAYS_OFF_BEFORE_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(weight, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testRuleWeightMultiplier() {
		// Change the weight multiplier of the rule
		int ruleMultiplier = 4;
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.putRuleWeightMultiplier(RuleName.DAYS_OFF_BEFORE_WEEKEND_RULE, ruleMultiplier);
		kSession.update(rosterHandle, rosterInfo);
	
		int weight = -1;
		Collection<DayOfWeek> daysOffBefore = Arrays.asList(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(null, daysOffBefore, weight);

		Employee employee = createEmployee(1, "MR.", "X");

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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.DAYS_OFF_BEFORE_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(ruleMultiplier * weight, numOfMinPerWeekConstraints);
	}

	@Test
	public void testDaysBeforeWeekendTwoDaysViolated() {
		int weight = -1;
		Collection<DayOfWeek> daysOffBefore = Arrays.asList(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(null, daysOffBefore, weight);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, weekendPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 17));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 18));

		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 16));
		kSession.insert(thirdShiftDate);

		ShiftDate fourthShiftDay = new ShiftDate(new LocalDate(2014, 5, 15));
		kSession.insert(fourthShiftDay);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);

		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);

		Shift fourthShift = createShift("4", 0, fourthShiftDay, shiftType, 1);
		kSession.insert(fourthShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);

		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
		kSession.insert(fourthShiftAssignment);

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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.DAYS_OFF_BEFORE_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(weight * 2, numOfMinPerWeekConstraints);
	}

	@Test
	public void testDaysAfterWeekendSatisfied() {
		int weight = -1;
		Collection<DayOfWeek> daysOffAfter = Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);

		setUpWeekendPatternTwoDayData(daysOffAfter, null, new LocalDate(2014, 5, 17), new LocalDate(2014, 5, 18),
				weight);

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.DAYS_OFF_AFTER_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}

	@Test
	public void testDaysAfterWeekendSatisfiedWed() {
		int weight = -1;
		Collection<DayOfWeek> daysOffAfter = Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(daysOffAfter, null, weight);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, weekendPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 17));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 18));

		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 21));
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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.DAYS_OFF_AFTER_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}

	@Test
	public void testDaysAfterWeekendOneDayViolated() {
		int weight = -1;
		Collection<DayOfWeek> daysOffAfter = Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(daysOffAfter, null, weight);

		Employee employee = createEmployee(1, "MR.", "X");

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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.DAYS_OFF_AFTER_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(weight, numOfMinPerWeekConstraints);
	}

	@Test
	public void testDaysAfterWeekendTwoDaysViolated() {
		int weight = -1;
		Collection<DayOfWeek> daysOffAfter = Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(daysOffAfter, null, weight);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, weekendPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 17));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 18));

		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 19));
		kSession.insert(thirdShiftDate);

		ShiftDate fourthShiftDay = new ShiftDate(new LocalDate(2014, 5, 20));
		kSession.insert(fourthShiftDay);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);

		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);

		Shift fourthShift = createShift("4e", 0, fourthShiftDay, shiftType, 1);
		kSession.insert(fourthShift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);

		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
		kSession.insert(fourthShiftAssignment);

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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.DAYS_OFF_AFTER_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(weight * 2, numOfMinPerWeekConstraints);
	}

	@Test
	public void testCoupledWeekendPatternSatWorkViolated() {
		int weight = -1;
		setUpWeekendPatternTwoDayData(new LocalDate(2014, 5, 17), new LocalDate(2014, 5, 19), weight);

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.COUPLED_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(weight, numOfMinPerWeekConstraints);
	}

	@Test
	public void testCoupledWeekendPatternOutsideScheduleSatWorkViolated() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 27));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		kSession.update(rosterHandle, rosterInfo);

		int weight = -1;
		setUpWeekendPatternTwoDayData(new LocalDate(2014, 5, 17), new LocalDate(2014, 5, 19), weight);

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.COUPLED_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}

	@Test
	public void testCoupledWeekendPatternInsidePlanningSunWorkViolated() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
		kSession.update(rosterHandle, rosterInfo);

		int weight = -1;
		setUpWeekendPatternTwoDayData(new LocalDate(2014, 5, 18), new LocalDate(2014, 5, 25), weight);

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.COUPLED_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(weight, numOfMinPerWeekConstraints);
	}

	@Test
	public void testCoupledFSSWeekendPatternInsidePlanningSunWorkViolated() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 11));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(14));
		rosterInfo.setWeekendDefinition(WeekendDefinition.FRIDAY_SATURDAY_SUNDAY);
		kSession.update(rosterHandle, rosterInfo);

		int weight = -1;
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(null, null, weight);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, weekendPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 17)); // Saturday

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 18)); // Sunday

		// Add shiftdates for start of weekend
		kSession.insert(shiftDate.minusDays(1)); //Friday

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

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.COUPLED_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(weight, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testCoupledFSSWeekendPatternInsidePlanningSunWorkHonored() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 11));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(14));
		rosterInfo.setWeekendDefinition(WeekendDefinition.FRIDAY_SATURDAY_SUNDAY);
		kSession.update(rosterHandle, rosterInfo);

		int weight = -1;
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(null, null, weight);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, weekendPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 16)); // Friday

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 17)); // Saturday
	
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 18)); // Sunday
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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.COUPLED_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testCoupledFSSMWeekendPatternInsidePlanningSunWorkViolated() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 11));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(14));
		rosterInfo.setWeekendDefinition(WeekendDefinition.FRIDAY_SATURDAY_SUNDAY_MONDAY);
		kSession.update(rosterHandle, rosterInfo);

		int weight = -1;
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(null, null, weight);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, weekendPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 16)); // Friday

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 17)); // Saturday
	
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 18)); // Sunday
		kSession.insert(thirdShiftDate);
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 19)); // Monday
		kSession.insert(fourthShiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		
		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);
		
		Shift fourthShift = createShift("4", 0, fourthShiftDate, shiftType, 1);
		kSession.insert(fourthShift);
		
//		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
//		kSession.insert(shiftAssignment);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);
		
		ShiftAssignment fourtShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
		kSession.insert(fourtShiftAssignment);
		
		kSession.insert(shiftType);
		kSession.insert(weekendPattern);
		kSession.insert(contractLine);
		kSession.insert(shiftDate);
		kSession.insert(secondShiftDate);
		kSession.insert(secondShift);
		kSession.insert(secondShiftAssignment);
		kSession.insert(shift);
		kSession.insert(employee);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.COUPLED_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(weight, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testCoupledFSSMWeekendPatternInsidePlanningSunWorkHonored() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 11));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(14));
		rosterInfo.setWeekendDefinition(WeekendDefinition.FRIDAY_SATURDAY_SUNDAY_MONDAY);
		kSession.update(rosterHandle, rosterInfo);

		int weight = -1;
		CompleteWeekendWorkPattern weekendPattern = createWeekendWorkPattern(null, null, weight);

		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, weekendPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 16)); // Friday

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 17)); // Saturday
	
		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 18)); // Sunday
		kSession.insert(thirdShiftDate);
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 19)); // Monday
		kSession.insert(fourthShiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
		
		Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
		kSession.insert(thirdShift);
		
		Shift fourthShift = createShift("4", 0, fourthShiftDate, shiftType, 1);
		kSession.insert(fourthShift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		kSession.insert(thirdShiftAssignment);
		
		ShiftAssignment fourtShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
		kSession.insert(fourtShiftAssignment);
		
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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.COUPLED_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}

	@Test
	public void testCoupledWeekendPatternInsidePlanningSatWorkViolated() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(planningWindowStart.plusDays(6));
		kSession.update(rosterHandle, rosterInfo);

		int weight = -1;
		setUpWeekendPatternTwoDayData(new LocalDate(2014, 5, 19), new LocalDate(2014, 5, 24), weight);

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.COUPLED_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(weight, numOfMinPerWeekConstraints);
	}

	@Test
	public void testCoupledWeekendPatternOutsidePlanningSatWorkViolated() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 27));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		kSession.update(rosterHandle, rosterInfo);

		int weight = -1;
		setUpWeekendPatternTwoDayData(new LocalDate(2014, 5, 17), new LocalDate(2014, 5, 15), weight);

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.COUPLED_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}

	@Test
	public void testCoupledWeekendPatternTwoSunWorkViolated() {
		int weight = -1;
		setUpWeekendPatternTwoDayData(new LocalDate(2014, 5, 11), new LocalDate(2014, 5, 18), weight);

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.COUPLED_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(weight, numOfMinPerWeekConstraints);
	}

	@Test
	public void testCoupledWeekendPatternSatisfied() {
		int weight = -1;
		setUpWeekendPatternTwoDayData(new LocalDate(2014, 5, 17), new LocalDate(2014, 5, 18), weight);

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.COUPLED_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}

	@Test
	public void testCoupledWeekendPatternNoWeekends() {
		int weight = -1;
		setUpWeekendPatternTwoDayData(new LocalDate(2014, 5, 20), new LocalDate(2014, 5, 21), weight);

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.COUPLED_WEEKEND_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}
}
