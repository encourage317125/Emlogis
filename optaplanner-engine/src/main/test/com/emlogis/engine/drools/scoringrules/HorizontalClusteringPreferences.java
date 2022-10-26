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

public class HorizontalClusteringPreferences extends ConstraintTesterBase {

    @Override
    protected void loadRosterInfo() {
        ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 27));
        rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
        rosterInfo.setPlanningWindowStart(planningWindowStart);
        rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
        kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
    }

    
    @Test
    public void testNoConsecutiveShifts(){
        Employee employee = createEmployee(1, "MR.", "X");
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

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
        kSession.insert(secondShift);

        Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
        kSession.insert(thirdShift);

        Shift fourthShift = createShift("4", 0, fourthShiftDate, shiftType, 1);
        kSession.insert(fourthShift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        ShiftAssignment secondShiftAssignment = createShiftAssignment(null, 0, secondShift);
        kSession.insert(secondShiftAssignment);

        ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
        kSession.insert(thirdShiftAssignment);

        ShiftAssignment fourthShiftAssignment = createShiftAssignment(null, 0, fourthShift);
        kSession.insert(fourthShiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.HORIZONTAL_CLUSTERING_RULE, scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfConsecDaysConstraints);
    }

    @Test
    public void testTwoConsecutiveShifts(){
        Employee employee = createEmployee(1, "MR.", "X");
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

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
        kSession.insert(secondShift);

        Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
        kSession.insert(thirdShift);

        Shift fourthShift = createShift("4", 0, fourthShiftDate, shiftType, 1);
        kSession.insert(fourthShift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
        kSession.insert(secondShiftAssignment);

        ShiftAssignment thirdShiftAssignment = createShiftAssignment(null, 0, thirdShift);
        kSession.insert(thirdShiftAssignment);

        ShiftAssignment fourthShiftAssignment = createShiftAssignment(null, 0, fourthShift);
        kSession.insert(fourthShiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.HORIZONTAL_CLUSTERING_RULE, scoreHolder.getConstraintMatchTotals());
        assertEquals(1, numOfConsecDaysConstraints);
    }
    
    @Test
    public void testOverlappingConsecutiveShifts(){
        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        ShiftType shiftType = createShiftType(1, new LocalTime(9, 0, 0), new LocalTime(15, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
        kSession.insert(shiftDate);

        ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
        kSession.insert(secondShiftDate);

        ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 29));
        kSession.insert(thirdShiftDate);

        ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 30));
        kSession.insert(fourthShiftDate);

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
        kSession.insert(secondShift);

        Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
        kSession.insert(thirdShift);

        Shift fourthShift = createShift("4", 0, fourthShiftDate, shiftType, 1);
        kSession.insert(fourthShift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
        kSession.insert(secondShiftAssignment);

        ShiftAssignment thirdShiftAssignment = createShiftAssignment(null, 0, thirdShift);
        kSession.insert(thirdShiftAssignment);

        ShiftAssignment fourthShiftAssignment = createShiftAssignment(null, 0, fourthShift);
        kSession.insert(fourthShiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.HORIZONTAL_CLUSTERING_RULE, scoreHolder.getConstraintMatchTotals());
        assertEquals(2, numOfConsecDaysConstraints);
    }
    
    @Test
    public void testThreeConsecutiveShifts(){
        Employee employee = createEmployee(1, "MR.", "X");
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

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);
        kSession.insert(shift);

        Shift secondShift = createShift("2", 0, secondShiftDate, shiftType, 1);
        kSession.insert(secondShift);

        Shift thirdShift = createShift("3", 0, thirdShiftDate, shiftType, 1);
        kSession.insert(thirdShift);

        Shift fourthShift = createShift("4", 0, fourthShiftDate, shiftType, 1);
        kSession.insert(fourthShift);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);
        kSession.insert(shiftAssignment);

        ShiftAssignment secondShiftAssignment = createShiftAssignment(employee, 0, secondShift);
        kSession.insert(secondShiftAssignment);

        ShiftAssignment thirdShiftAssignment = createShiftAssignment(employee, 0, thirdShift);
        kSession.insert(thirdShiftAssignment);

        ShiftAssignment fourthShiftAssignment = createShiftAssignment(null, 0, fourthShift);
        kSession.insert(fourthShiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.HORIZONTAL_CLUSTERING_RULE, scoreHolder.getConstraintMatchTotals());
        assertEquals(2, numOfConsecDaysConstraints);
    }
    
   
}
