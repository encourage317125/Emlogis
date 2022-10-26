package com.emlogis.engine.drools.scoringrules;

import com.emlogis.engine.domain.*;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.domain.timeoff.CIPreference;
import com.emlogis.engine.domain.timeoff.PreferenceType;
import com.emlogis.engine.drools.ConstraintTesterBase;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.optaplanner.core.api.score.buildin.bendable.BendableScoreHolder;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;

import static org.junit.Assert.assertEquals;

public class CIPreferenceTester extends ConstraintTesterBase {
	
	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 27));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
	}
	
	@Test
	public void testCIAvoidOneFullDay() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CIPreference ciPreferenceAgainst = createCIPreference(employee.getEmployeeId(), DayOfWeek.TUESDAY, -1);
		ciPreferenceAgainst.setType(PreferenceType.PreferedUnavail);
		kSession.insert(ciPreferenceAgainst);
		
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
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_PREF_CONSTRAINT ,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(ciPreferenceAgainst.getWeight() * shift.getShiftDurationHours(), numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testCIAvoidOneFullDayHonored() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CIPreference ciPreferenceAgainst = createCIPreference(employee.getEmployeeId(), DayOfWeek.TUESDAY, -1);
		ciPreferenceAgainst.setType(PreferenceType.PreferedUnavail);
		kSession.insert(ciPreferenceAgainst);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_PREF_CONSTRAINT ,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testCIPreferOneFullDay() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CIPreference ciPreferenceFor = createCIPreference(employee.getEmployeeId(), DayOfWeek.TUESDAY, 1);
		ciPreferenceFor.setType(PreferenceType.PreferedAvail);
		kSession.insert(ciPreferenceFor);
		
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
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_PREF_CONSTRAINT ,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(ciPreferenceFor.getWeight() * shift.getShiftDurationHours(), numOfMaxPerWeekConstraints);
	}
	
	@Test
	public void testCIPreferOneFullDayMissed() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);

		CIPreference ciPreferenceFor = createCIPreference(employee.getEmployeeId(), DayOfWeek.TUESDAY, 1);
		ciPreferenceFor.setType(PreferenceType.PreferedUnavail);
		kSession.insert(ciPreferenceFor);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfMaxPerWeekConstraints = getNumOfConstraintMatches(RuleName.CI_PREF_CONSTRAINT ,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMaxPerWeekConstraints);
	}
	
}
