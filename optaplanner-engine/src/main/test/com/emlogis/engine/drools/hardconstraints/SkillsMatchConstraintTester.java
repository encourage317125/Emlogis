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
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.drools.ConstraintTesterBase;

public class SkillsMatchConstraintTester extends ConstraintTesterBase {
	
	@Override
	protected void loadRosterInfo() {
		ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
		rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
		rosterInfo.setPlanningWindowStart(planningWindowStart);
		rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
		kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
	}
	
	@Test
	public void testShiftOutsidePlanningWindowSkillRequirement() {
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.setPlanningWindowStart(new ShiftDate(new LocalDate(2014, 5, 27)));
		kSession.update(rosterHandle, rosterInfo);
		
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);
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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.SKILL_MATCH_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testNoShiftSkillRequirement() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);
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
		int numOfMinPerWeekConstraints = getNumOfConstraintMatches(RuleName.SKILL_MATCH_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfMinPerWeekConstraints);
	}
	
	@Test
	public void testNoEmployeeSkill() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);
		kSession.insert(shiftType);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		Skill skill = new Skill();
		skill.setCode("0");
		skill.setName("X-Ray Tech");
		kSession.insert(skill);
		
		ShiftSkillRequirement shiftSkillReq = createShiftTypeSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfSkillConstraints = getNumOfConstraintMatches(RuleName.SKILL_MATCH_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(-40, numOfSkillConstraints);
	}
	
	@Test
	public void testRuleWeightMultiplier() {
		// Change the weight multiplier of the rule
		int ruleMultiplier = 4;
		FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.putRuleWeightMultiplier(RuleName.SKILL_MATCH_RULE, ruleMultiplier);
		kSession.update(rosterHandle, rosterInfo);
						

		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);
		kSession.insert(shiftType);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		Skill skill = new Skill();
		skill.setCode("0");
		skill.setName("X-Ray Tech");
		kSession.insert(skill);
		
		ShiftSkillRequirement shiftSkillReq = createShiftTypeSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfSkillConstraints = getNumOfConstraintMatches(RuleName.SKILL_MATCH_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(-ruleMultiplier, numOfSkillConstraints);
	}
	
	@Test
	public void testNoMatchingEmployeeSkill() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);
		kSession.insert(shiftType);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		Skill skill = createSkill(0, "X-Ray Magician");
		kSession.insert(skill);
		
		ShiftSkillRequirement shiftSkillReq = createShiftTypeSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);
		
		Skill wrongSkill = createSkill(1, "Wrong  Skill");
		kSession.insert(wrongSkill);
		
		SkillProficiency skillProf = createSkillProficiency(employee, wrongSkill);
		kSession.insert(skillProf);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfSkillConstraints = getNumOfConstraintMatches(RuleName.SKILL_MATCH_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(-40, numOfSkillConstraints);
	}
	
	
	@Test
	public void testWithMatchingEmployeeSkill() {
		Employee employee = createEmployee(1, "MR.", "X");
		kSession.insert(employee);
		
		ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);
		kSession.insert(shiftType);
		
		ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
		kSession.insert(shiftDate);
		
		Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
		kSession.insert(shift);
		
		ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
		kSession.insert(shiftAssignment);
		
		Skill skill = createSkill(0, "X-Ray Magician");
		kSession.insert(skill);
		
		ShiftSkillRequirement shiftSkillReq = createShiftTypeSkillRequirement(shift, skill);
		kSession.insert(shiftSkillReq);
		
		Skill wrongSkill = createSkill(1, "Wrong  Skill");
		kSession.insert(wrongSkill);
		
		SkillProficiency skillProf = createSkillProficiency(employee, skill);
		kSession.insert(skillProf);
		
		kSession.fireAllRules();
		
		BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession
				.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		int numOfSkillConstraints = getNumOfConstraintMatches(RuleName.SKILL_MATCH_RULE,
				scoreHolder.getConstraintMatchTotals());
		assertEquals(0, numOfSkillConstraints);
	}

}
