package com.emlogis.engine.drools.scoringrules;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.optaplanner.core.api.score.buildin.bendable.BendableScoreHolder;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;

import com.emlogis.engine.domain.Employee;
import com.emlogis.engine.domain.Shift;
import com.emlogis.engine.domain.ShiftAssignment;
import com.emlogis.engine.domain.ShiftDate;
import com.emlogis.engine.domain.ShiftType;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.drools.ConstraintTesterBase;

public class TeamScatteringPreferences extends ConstraintTesterBase {

    @Override
    protected void loadRosterInfo() {
        ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 27));
        rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
        rosterInfo.setPlanningWindowStart(planningWindowStart);
        rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
        kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
    }

    
    @Test
    public void testAllShiftsSameTeam(){
        long teamId1 = 1L;
        long teamId2 = 2L;
        
        Employee employee = createEmployee(1, "MR.", "X");
        employee.getTeamIds().add(String.valueOf(teamId1));
        employee.getTeamIds().add(String.valueOf(teamId2));
        kSession.insert(employee);
    
        
        ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
        kSession.insert(shiftDate);

        ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
        kSession.insert(secondShiftDate);

        ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 29));
        kSession.insert(thirdShiftDate);

        ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 30));
        kSession.insert(fourthShiftDate);

        // Needed to show that other teams exist
        Shift shift = createShift("1", 0, shiftDate, shiftType, 1, teamId2); 
        kSession.insert(shift);

        Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1, teamId1);
        kSession.insert(secondShift);

        Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1, teamId1);
        kSession.insert(thirdShift);

        Shift fourthShift = createShift("4", 0, fourthShiftDate, shiftType, 1, teamId1);
        kSession.insert(fourthShift);

        ShiftAssignment shiftAssignment = createShiftAssignment(null, 0, shift);
        kSession.insert(shiftAssignment);

        ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
        kSession.insert(secondShiftAssignment);

        ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
        kSession.insert(thirdShiftAssignment);

        ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
        kSession.insert(fourthShiftAssignment);

        kSession.fireAllRules();
        
        int teamOneShifts = 3;
        int teamTwoShifts = 0;
        int averageEmployeeToTeamAssignments = (teamOneShifts+teamTwoShifts)/2; // 3 assignments on team 1, 0 on team 2 out of 2 teams
        
        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.PREFER_TEAM_SCATTERING_RULE, scoreHolder.getConstraintMatchTotals());
        int expectedScore = -( Math.abs(teamOneShifts-averageEmployeeToTeamAssignments) + Math.abs(teamTwoShifts-averageEmployeeToTeamAssignments));
        assertEquals(expectedScore, numOfConsecDaysConstraints);
    }
    
    
    @Test
    public void testThreeTeamDist(){
        long teamId1 = 1L;
        long teamId2 = 2L;
        long teamId3 = 3L;
        
        Employee employee = createEmployee(1, "MR.", "X");
        employee.getTeamIds().add(String.valueOf(teamId1));
        employee.getTeamIds().add(String.valueOf(teamId2));
        employee.getTeamIds().add(String.valueOf(teamId3));
        kSession.insert(employee);
    
        
        ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
        kSession.insert(shiftDate);

        ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
        kSession.insert(secondShiftDate);

        ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 29));
        kSession.insert(thirdShiftDate);

        ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 30));
        kSession.insert(fourthShiftDate);
        
        ShiftDate fifthShiftDate = new ShiftDate(new LocalDate(2014, 5, 31));
        kSession.insert(fifthShiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1, teamId2); 
        kSession.insert(shift);

        Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1, teamId1);
        kSession.insert(secondShift);

        Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1, teamId1);
        kSession.insert(thirdShift);

        Shift fourthShift = createShift("4", 0, fourthShiftDate, shiftType, 1, teamId1);
        kSession.insert(fourthShift);
        
        Shift fifthShift = createShift("5", 0, fifthShiftDate, shiftType, 1, teamId3);
        kSession.insert(fifthShift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
        kSession.insert(secondShiftAssignment);

        ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
        kSession.insert(thirdShiftAssignment);

        ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
        kSession.insert(fourthShiftAssignment);
        
        ShiftAssignment fifthShiftAssignment = createShiftAssignment(employee, 0, fifthShift);
        kSession.insert(fifthShiftAssignment);

        kSession.fireAllRules();
        
        int teamOneShifts = 3;
        int teamTwoShifts = 1;
        int teamThreeShifts = 1;
        int averageEmployeeToTeamAssignments = (teamOneShifts+teamTwoShifts+teamThreeShifts)/3; 
        
        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.PREFER_TEAM_SCATTERING_RULE, scoreHolder.getConstraintMatchTotals());
        int expectedScore = -( Math.abs(teamOneShifts-averageEmployeeToTeamAssignments) + Math.abs(teamTwoShifts-averageEmployeeToTeamAssignments)
        		 + Math.abs(teamThreeShifts-averageEmployeeToTeamAssignments));
        assertEquals(expectedScore, numOfConsecDaysConstraints);
    }
    
    @Test
    public void testEqualDistributionOfTeams(){
        long teamId1 = 1L;
        long teamId2 = 2L;
        
        Employee employee = createEmployee(1, "MR.", "X");
        employee.getTeamIds().add(String.valueOf(teamId1));
        employee.getTeamIds().add(String.valueOf(teamId2));
        kSession.insert(employee);
    
        ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
        kSession.insert(shiftDate);

        ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
        kSession.insert(secondShiftDate);

        ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 29));
        kSession.insert(thirdShiftDate);

        ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 30));
        kSession.insert(fourthShiftDate);

        // Needed to show that other teams exist
        Shift shift = createShift("1", 0, shiftDate, shiftType, 1, teamId2); 
        kSession.insert(shift);

        Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1, teamId1);
        kSession.insert(secondShift);

        Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1, teamId2);
        kSession.insert(thirdShift);

        Shift fourthShift = createShift("4", 0, fourthShiftDate, shiftType, 1, teamId1);
        kSession.insert(fourthShift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
        kSession.insert(secondShiftAssignment);

        ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
        kSession.insert(thirdShiftAssignment);

        ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
        kSession.insert(fourthShiftAssignment);

        kSession.fireAllRules();
        
        int teamOneShifts = 2;
        int teamTwoShifts = 2;
        int averageEmployeeToTeamAssignments = (teamOneShifts+teamTwoShifts)/2; 

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.PREFER_TEAM_SCATTERING_RULE, scoreHolder.getConstraintMatchTotals());
        int expectedScore = -( Math.abs(teamOneShifts-averageEmployeeToTeamAssignments) + Math.abs(teamTwoShifts-averageEmployeeToTeamAssignments));
        assertEquals(expectedScore, numOfConsecDaysConstraints);
    }

  
}
