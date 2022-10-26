package com.emlogis.engine.drools.scoringrules;

import com.emlogis.engine.domain.*;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.drools.ConstraintTesterBase;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.optaplanner.core.api.score.buildin.bendable.BendableScoreHolder;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;

import static org.junit.Assert.assertEquals;

public class DoublingUpTester extends ConstraintTesterBase {

    @Override
    protected void loadRosterInfo() {
        ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 27));
        rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
        rosterInfo.setPlanningWindowStart(planningWindowStart);
        rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
        kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
    }

    @Test
    public void testSkillChangeBackToBack(){
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);
        
        Skill skill = createSkill(1, "Awesomeness");
        kSession.insert(skill);
        
        Skill secondSkill = createSkill(2, "Realism");
        kSession.insert(secondSkill);

        ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
        kSession.insert(shiftType);
        
        ShiftType shiftTypeSecond = createShiftType(1, new LocalTime(15, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftTypeSecond);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
        kSession.insert(shiftDate);

        ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
        kSession.insert(secondShiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        shift.setSkillId(skill.getCode());
        kSession.insert(shift);

        Shift secondShift = createShift("2", 0, secondShiftDate, shiftTypeSecond, 1);
        secondShift.setSkillId(secondSkill.getCode());
        kSession.insert(secondShift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
        kSession.insert(secondShiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.AVOID_SKILL_CHANGE_RULE, scoreHolder.getConstraintMatchTotals());
        assertEquals(-1, numOfConsecDaysConstraints);
    }
    
    @Test
    public void testNoSkillChangeBackToBack(){
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);
        
        Skill skill = createSkill(1, "Awesomeness");
        kSession.insert(skill);

        ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
        kSession.insert(shiftType);
        
        ShiftType shiftTypeSecond = createShiftType(1, new LocalTime(15, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftTypeSecond);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
        kSession.insert(shiftDate);

        ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
        kSession.insert(secondShiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        shift.setSkillId(skill.getCode());
        kSession.insert(shift);

        Shift secondShift = createShift("2", 0, secondShiftDate, shiftTypeSecond, 1);
        secondShift.setSkillId(skill.getCode());
        kSession.insert(secondShift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
        kSession.insert(secondShiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.AVOID_SKILL_CHANGE_RULE, scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfConsecDaysConstraints);
    }
    
    @Test
    public void testSkillChangeSameDayNotAdjacent(){
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);
        
        Skill skill = createSkill(1, "Awesomeness");
        kSession.insert(skill);
        
        Skill secondSkill = createSkill(2, "Realism");
        kSession.insert(secondSkill);

        ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
        kSession.insert(shiftType);
        
        ShiftType shiftTypeSecond = createShiftType(1, new LocalTime(15, 30, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftTypeSecond);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
        kSession.insert(shiftDate);

        ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
        kSession.insert(secondShiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        shift.setSkillId(skill.getCode());
        kSession.insert(shift);

        Shift secondShift = createShift("2", 0, secondShiftDate, shiftTypeSecond, 1);
        secondShift.setSkillId(secondSkill.getCode());
        kSession.insert(secondShift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
        kSession.insert(secondShiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.AVOID_SKILL_CHANGE_RULE, scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfConsecDaysConstraints);
    }
    
    // TEAM ///
    @Test
    public void testTeamChangeBackToBack(){
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);
        
        String teamId = "Alpha";
        String secondTeamId = "Omega";
        
        Skill secondSkill = createSkill(2, "Realism");
        kSession.insert(secondSkill);

        ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
        kSession.insert(shiftType);
        
        ShiftType shiftTypeSecond = createShiftType(1, new LocalTime(15, 0, 0), new LocalTime(22, 0, 0), false);
        kSession.insert(shiftTypeSecond);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
        kSession.insert(shiftDate);

        ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
        kSession.insert(secondShiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        shift.setTeamId(teamId);
        kSession.insert(shift);

        Shift secondShift = createShift("2", 0, secondShiftDate, shiftTypeSecond, 1);
        secondShift.setTeamId(secondTeamId);
        kSession.insert(secondShift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
        kSession.insert(secondShiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.AVOID_TEAM_CHANGE_RULE, scoreHolder.getConstraintMatchTotals());
        assertEquals(-1, numOfConsecDaysConstraints);
    }
    
    @Test
    public void testNoTeamChangeBackToBack(){
    	 Employee employee = createEmployee(1, "MR.", "X");
         kSession.insert(employee);
         
         String teamId = "Alpha";
         
         Skill secondSkill = createSkill(2, "Realism");
         kSession.insert(secondSkill);

         ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
         kSession.insert(shiftType);
         
         ShiftType shiftTypeSecond = createShiftType(1, new LocalTime(15, 0, 0), new LocalTime(22, 0, 0), false);
         kSession.insert(shiftTypeSecond);

         ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
         kSession.insert(shiftDate);

         ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
         kSession.insert(secondShiftDate);

         Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
         shift.setTeamId(teamId);
         kSession.insert(shift);

         Shift secondShift = createShift("2", 0, secondShiftDate, shiftTypeSecond, 1);
         secondShift.setTeamId(teamId);
         kSession.insert(secondShift);

         ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
         kSession.insert(shiftAssignment);

         ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
         kSession.insert(secondShiftAssignment);

         kSession.fireAllRules();

         BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
         int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.AVOID_TEAM_CHANGE_RULE, scoreHolder.getConstraintMatchTotals());
         assertEquals(0, numOfConsecDaysConstraints);
    }
    
    @Test
    public void testTeamChangeSameDayNotAdjacent(){
    	  Employee employee = createEmployee(1, "MR.", "X");
          kSession.insert(employee);
          
          String teamId = "Alpha";
          String secondTeamId = "Omega";
          
          Skill secondSkill = createSkill(2, "Realism");
          kSession.insert(secondSkill);

          ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
          kSession.insert(shiftType);
          
          ShiftType shiftTypeSecond = createShiftType(1, new LocalTime(15, 20, 0), new LocalTime(22, 0, 0), false);
          kSession.insert(shiftTypeSecond);

          ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
          kSession.insert(shiftDate);

          ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
          kSession.insert(secondShiftDate);

          Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
          shift.setTeamId(teamId);
          kSession.insert(shift);

          Shift secondShift = createShift("2", 0, secondShiftDate, shiftTypeSecond, 1);
          secondShift.setTeamId(secondTeamId);
          kSession.insert(secondShift);

          ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
          kSession.insert(shiftAssignment);

          ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
          kSession.insert(secondShiftAssignment);

          kSession.fireAllRules();

          BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
          int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.AVOID_TEAM_CHANGE_RULE, scoreHolder.getConstraintMatchTotals());
          assertEquals(0, numOfConsecDaysConstraints);
    }
}
