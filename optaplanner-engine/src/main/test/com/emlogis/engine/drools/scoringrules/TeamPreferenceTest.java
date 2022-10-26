package com.emlogis.engine.drools.scoringrules;

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
import com.emlogis.engine.domain.Skill;
import com.emlogis.engine.domain.SkillProficiency;
import com.emlogis.engine.domain.organization.Team;
import com.emlogis.engine.domain.organization.TeamAssociation;
import com.emlogis.engine.domain.organization.TeamAssociationType;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.drools.ConstraintTesterBase;

public class TeamPreferenceTest extends ConstraintTesterBase {
	
	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
	}
	
	@Test
	public void testEmployeeWorkingHomeTeam() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		//Turn off team scattering
		rosterInfo.getRuleWeightMultipliers().put(RuleName.PREFER_TEAM_SCATTERING_RULE, new Integer(0));
		kSession.update(rosterHandle, rosterInfo);
		
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		TeamAssociation pref = createTeamAssociation(employee, 1, TeamAssociationType.ON, true);
		kSession.insert(pref);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);
		kSession.insert(shiftType);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId(pref.getTeamId());
		kSession.insert(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int teamPref = getNumOfConstraintMatches(RuleName.TEAM_PREFERENCE_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(2, teamPref);
	}
	
	@Test
	public void testEmployeeWorkingOnTeam() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		//Turn off team scattering
		rosterInfo.getRuleWeightMultipliers().put(RuleName.PREFER_TEAM_SCATTERING_RULE, new Integer(0));
		kSession.update(rosterHandle, rosterInfo);
		
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		TeamAssociation pref = createTeamAssociation(employee, 1, TeamAssociationType.ON, false);
		kSession.insert(pref);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);
		kSession.insert(shiftType);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId(pref.getTeamId());
		kSession.insert(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int teamPref = getNumOfConstraintMatches(RuleName.TEAM_PREFERENCE_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(1, teamPref);
	}
	
	@Test
	public void testTeamScatterOverride() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		//Turn on team scattering
		rosterInfo.getRuleWeightMultipliers().put(RuleName.PREFER_TEAM_SCATTERING_RULE, new Integer(1));
		kSession.update(rosterHandle, rosterInfo);
		
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		TeamAssociation pref = createTeamAssociation(employee, 1, TeamAssociationType.ON, false);
		kSession.insert(pref);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);
		kSession.insert(shiftType);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId(pref.getTeamId());
		kSession.insert(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int teamPref = getNumOfConstraintMatches(RuleName.TEAM_PREFERENCE_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, teamPref);
	}
	
	@Test
	public void testEmployeeWorkingDiffHomeTeam() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		kSession.update(rosterHandle, rosterInfo);
		
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		TeamAssociation pref = createTeamAssociation(employee, 1, TeamAssociationType.ON, true);
		kSession.insert(pref);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);
		kSession.insert(shiftType);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId(pref.getTeamId() + "1");
		kSession.insert(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int teamPref = getNumOfConstraintMatches(RuleName.TEAM_PREFERENCE_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, teamPref);
	}
	
	@Test
	public void testEmployeeWorkingDiffOnTeam() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		kSession.update(rosterHandle, rosterInfo);
		
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		TeamAssociation pref = createTeamAssociation(employee, 1, TeamAssociationType.ON, false);
		kSession.insert(pref);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);
		kSession.insert(shiftType);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId(pref.getTeamId() + "1");
		kSession.insert(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int teamPref = getNumOfConstraintMatches(RuleName.TEAM_PREFERENCE_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, teamPref);
	}
	
	@Test
	public void testEmployeeWorkingFloatTeam() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		kSession.update(rosterHandle, rosterInfo);
		
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		TeamAssociation pref = createTeamAssociation(employee, 1, TeamAssociationType.FLOAT, false);
		kSession.insert(pref);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);
		kSession.insert(shiftType);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		shift.setTeamId(pref.getTeamId());
		kSession.insert(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int teamPref = getNumOfConstraintMatches(RuleName.TEAM_PREFERENCE_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, teamPref);
	}
	
}
	

