package com.emlogis.engine.drools.hardconstraints;

import static org.junit.Assert.assertEquals;

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
import com.emlogis.engine.domain.contract.Contract;
import com.emlogis.engine.domain.contract.contractline.PatternContractLine;
import com.emlogis.engine.domain.contract.patterns.WeekdayRotationPattern;
import com.emlogis.engine.domain.contract.patterns.WeekdayRotationPattern.RotationPatternType;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.drools.ConstraintTesterBase;

public class WeekdayRotationTester extends ConstraintTesterBase {

	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
	}
	
	@Test
	public void testNoWeekdayRotationContractLine() {
		Employee employee = createEmployee(1, "MR.", "X");

		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));

		rosterInfo.setLastShiftDate(secondShiftDate);
		FactHandle info = kSession.getFactHandle(rosterInfo);
		kSession.update(info, rosterInfo);
		
		Shift shift = createShift("1", 0,shiftDate, secondShiftDate, shiftType, 1);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		kSession.insert(shiftType);
		kSession.insert(shiftDate);
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
	public void testOneOfTwoWeekdayPatternNoMatch() {
		WeekdayRotationPattern oneOfTwoPattern = createWeekdayRotationPattern(RotationPatternType.DAYS_OFF_PATTERN,
				DayOfWeek.MONDAY, 1, 2, -1);
		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, oneOfTwoPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 12));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 17));

		rosterInfo.setLastShiftDate(secondShiftDate);
		FactHandle info = kSession.getFactHandle(rosterInfo);
		kSession.update(info, rosterInfo);
		
		
		Shift shift = createShift("1", 0,shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0,secondShiftDate, shiftType, 1);

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
	public void testOneOfTwoWeekdayPattern() {
		WeekdayRotationPattern oneOfTwoPattern = createWeekdayRotationPattern(RotationPatternType.DAYS_OFF_PATTERN,
				DayOfWeek.MONDAY, 1, 2, -1);
		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, oneOfTwoPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 19));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));

		rosterInfo.setLastShiftDate(secondShiftDate);
		FactHandle info = kSession.getFactHandle(rosterInfo);
		kSession.update(info, rosterInfo);
		
		Shift shift = createShift("1", 0,shiftDate, shiftType, 1);
		shift.setSkillId("X11");

		Shift secondShift = createShift("2", 0,secondShiftDate, shiftType, 1);
		shift.setSkillId("X12");

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
		assertEquals(oneOfTwoPattern.getWeight(), numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testRuleWeightMultiplier() {
		// Change the weight multiplier of the rule
		int ruleMultiplier = 4;
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.putRuleWeightMultiplier(RuleName.WEEKDAY_ROTATION_PATTERN_RULE, ruleMultiplier);
		kSession.update(rosterHandle, rosterInfo);
						
		WeekdayRotationPattern oneOfTwoPattern = createWeekdayRotationPattern(RotationPatternType.DAYS_OFF_PATTERN,
				DayOfWeek.MONDAY, 1, 2, -1);
		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, oneOfTwoPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 19));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));

		rosterInfo.setLastShiftDate(secondShiftDate);
		FactHandle info = kSession.getFactHandle(rosterInfo);
		kSession.update(info, rosterInfo);
		
		Shift shift = createShift("1", 0,shiftDate, shiftType, 1);
		shift.setSkillId("X11");

		Shift secondShift = createShift("2", 0,secondShiftDate, shiftType, 1);
		shift.setSkillId("X12");

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
		assertEquals(ruleMultiplier * oneOfTwoPattern.getWeight(), numOfMinPerWeekConstraints);
	}

	@Test
	public void testOneOfThreeWeekdayPattern() {
		WeekdayRotationPattern oneOfTwoPattern = createWeekdayRotationPattern(RotationPatternType.DAYS_OFF_PATTERN,
				DayOfWeek.MONDAY, 1, 3, -1);
		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);
		
		PatternContractLine contractLine = createPatternContractLine(contract, oneOfTwoPattern);

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 12));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 19));

		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		
		rosterInfo.setLastShiftDate(thirdShiftDate);
		FactHandle info = kSession.getFactHandle(rosterInfo);
		kSession.update(info, rosterInfo);

		Shift shift = createShift("1", 0,shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0,secondShiftDate, shiftType, 1);

		Shift thirdShift = createShift("3", 0,thirdShiftDate, shiftType, 1);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);

		kSession.insert(shiftType);
		kSession.insert(oneOfTwoPattern);
		kSession.insert(contractLine);
		kSession.insert(shiftDate);
		kSession.insert(thirdShift);
		kSession.insert(thirdShiftAssignment);
		kSession.insert(secondShiftDate);
		kSession.insert(thirdShiftDate);
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
		assertEquals(oneOfTwoPattern.getWeight(), numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testOneOfThreeWeekdayPatternNoViolation() {
		WeekdayRotationPattern oneOfTwoPattern = createWeekdayRotationPattern(RotationPatternType.DAYS_OFF_PATTERN,
				DayOfWeek.MONDAY, 1, 3, -1);
		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, oneOfTwoPattern);

		
		///////////////////ShiftDates and Types/////////
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 12));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 19));

		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 6, 2));
		
		rosterInfo.setLastShiftDate(thirdShiftDate);
		FactHandle info = kSession.getFactHandle(rosterInfo);
		kSession.update(info, rosterInfo);
		///////////////////////SHIFTS//////////////////////////

		Shift shift = createShift("1", 0,shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0,secondShiftDate, shiftType, 1);

		Shift thirdShift = createShift("3", 0,thirdShiftDate, shiftType, 1);
		
		Shift fourthShift = createShift("4", 0,fourthShiftDate, shiftType, 1);

		/////////////////////////Shift Assignments////////////////////////
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		
		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);

		kSession.insert(shiftType);
		kSession.insert(oneOfTwoPattern);
		kSession.insert(contractLine);
		kSession.insert(shiftDate);
		kSession.insert(thirdShift);
		kSession.insert(fourthShift);
		//kSession.insert(thirdShiftAssignment); //Left out on purpose
		kSession.insert(fourthShiftAssignment);
		kSession.insert(secondShiftDate);
		kSession.insert(thirdShiftDate);
		kSession.insert(fourthShiftDate);
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
	public void testTwoOfFourWeekdayPattern() {
		WeekdayRotationPattern oneOfTwoPattern = createWeekdayRotationPattern(RotationPatternType.DAYS_OFF_PATTERN,
				DayOfWeek.MONDAY, 2, 4, -1);
		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);
		
	
		PatternContractLine contractLine = createPatternContractLine(contract, oneOfTwoPattern);

		///////////////////ShiftDates and Types/////////
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 12));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 19));

		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 6, 2));
		
		rosterInfo.setLastShiftDate(fourthShiftDate);
		FactHandle info = kSession.getFactHandle(rosterInfo);
		kSession.update(info, rosterInfo);
		
		///////////////////////SHIFTS//////////////////////////

		Shift shift = createShift("1", 0,shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0,secondShiftDate, shiftType, 1);

		Shift thirdShift = createShift("3", 0,thirdShiftDate, shiftType, 1);
		
		Shift fourthShift = createShift("4", 0,fourthShiftDate, shiftType, 1);

		/////////////////////////Shift Assignments////////////////////////
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		
		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);

		
		kSession.insert(shiftType);
		kSession.insert(oneOfTwoPattern);
		kSession.insert(contractLine);
		kSession.insert(shiftDate);
		kSession.insert(thirdShift);
		kSession.insert(fourthShift);
		kSession.insert(thirdShiftAssignment);
		kSession.insert(fourthShiftAssignment);
		kSession.insert(secondShiftDate);
		kSession.insert(thirdShiftDate);
		kSession.insert(fourthShiftDate);
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
		assertEquals(oneOfTwoPattern.getWeight(), numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testTwoOfFourWeekdayPatternOneDayOff() {
		WeekdayRotationPattern oneOfTwoPattern = createWeekdayRotationPattern(RotationPatternType.DAYS_OFF_PATTERN,
				DayOfWeek.MONDAY, 2, 4, -1);
		Employee employee = createEmployee(1, "MR.", "X");

		Contract contract = createContract(false, employee);
		kSession.insert(contract);

		PatternContractLine contractLine = createPatternContractLine(contract, oneOfTwoPattern);

		///////////////////ShiftDates and Types/////////
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 12));

		ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 19));

		ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
		
		ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 6, 2));
		
		rosterInfo.setLastShiftDate(fourthShiftDate);
		FactHandle info = kSession.getFactHandle(rosterInfo);
		kSession.update(info, rosterInfo);
		
		///////////////////////SHIFTS//////////////////////////

		Shift shift = createShift("1", 0,shiftDate, shiftType, 1);

		Shift secondShift = createShift("2", 0,secondShiftDate, shiftType, 1);

		Shift thirdShift = createShift("3", 0,thirdShiftDate, shiftType, 1);
		
		Shift fourthShift = createShift("4", 0,fourthShiftDate, shiftType, 1);
		

		/////////////////////////Shift Assignments////////////////////////
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

		ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);

		ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
		
		ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
		
		kSession.insert(shiftType);
		kSession.insert(oneOfTwoPattern);
		kSession.insert(contractLine);
		kSession.insert(shiftDate);
		kSession.insert(thirdShift);
		kSession.insert(fourthShift);
	//	kSession.insert(thirdShiftAssignment); //Comment out to show that employee has this shift off
		kSession.insert(fourthShiftAssignment);
		kSession.insert(secondShiftDate);
		kSession.insert(thirdShiftDate);
		kSession.insert(fourthShiftDate);
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
		assertEquals(oneOfTwoPattern.getWeight(), numOfMinPerWeekConstraints);
	}

}
