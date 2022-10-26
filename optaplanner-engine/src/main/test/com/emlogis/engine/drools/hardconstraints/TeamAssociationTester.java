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
import com.emlogis.engine.domain.organization.TeamAssociation;
import com.emlogis.engine.domain.organization.TeamAssociationType;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.drools.ConstraintTesterBase;

public class TeamAssociationTester extends ConstraintTesterBase {
	
	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
	}
	
	@Test
	public void testEmployeeNotOnTeam(){
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		TeamAssociation teamAssos = new TeamAssociation();
		teamAssos.setEmployee(employee);
		teamAssos.setTeamId("0");
		teamAssos.setType(TeamAssociationType.ON); 
		kSession.insert(teamAssos);
		

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1, 1L);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int teamAssosConstraintViolations = getNumOfConstraintMatches(RuleName.TEAM_ASSOCIATION_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(-10, teamAssosConstraintViolations);
	}
	
	@Test
	public void testRuleWeightMultiplier(){
		// Change the weight multiplier of the rule
		int ruleMultiplier = 4;
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.putRuleWeightMultiplier(RuleName.TEAM_ASSOCIATION_CONSTRAINT, ruleMultiplier);
		kSession.update(rosterHandle, rosterInfo);
						

		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		TeamAssociation teamAssos = new TeamAssociation();
		teamAssos.setEmployee(employee);
		teamAssos.setTeamId("0");
		teamAssos.setType(TeamAssociationType.ON); 
		kSession.insert(teamAssos);
		

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1, 1L);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int teamAssosConstraintViolations = getNumOfConstraintMatches(RuleName.TEAM_ASSOCIATION_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(-ruleMultiplier, teamAssosConstraintViolations);
	}
	
	@Test
	public void testEmployeeOnTeam(){
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		TeamAssociation teamAssos = new TeamAssociation();
		teamAssos.setEmployee(employee);
		teamAssos.setTeamId("1");
		teamAssos.setType(TeamAssociationType.ON); 
		kSession.insert(teamAssos);
		

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1, 1L);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int teamAssosConstraintViolations = getNumOfConstraintMatches(RuleName.TEAM_ASSOCIATION_CONSTRAINT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, teamAssosConstraintViolations);
	}
	
	@Test
	public void testEmployeeFloatTeam(){
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		TeamAssociation teamAssos = new TeamAssociation();
		teamAssos.setEmployee(employee);
		teamAssos.setTeamId("1");
		teamAssos.setType(TeamAssociationType.FLOAT); 
		kSession.insert(teamAssos);
		

		ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(22, 0, 0), false);
		kSession.insert(shiftType);

		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 26)); // Monday
		kSession.insert(shiftDate);

		Shift shift = createShift("1", 0, shiftDate, shiftType, 1, 1L);
		kSession.insert(shift);

		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);

		kSession.fireAllRules();

		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int teamAssosConstraintViolations = getNumOfConstraintMatches(RuleName.TEAM_ASSOCIATION_CONSTRAINT_FLOAT,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, teamAssosConstraintViolations);
	}

}
