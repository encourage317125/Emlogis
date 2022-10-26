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
import com.emlogis.engine.domain.contract.Contract;
import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.engine.domain.contract.contractline.MinMaxContractLine;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.drools.ConstraintTesterBase;

public class Consecutive12HourDaysTester extends ConstraintTesterBase {

    @Override
    protected void loadRosterInfo() {
        ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 27));
        rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
        rosterInfo.setPlanningWindowStart(planningWindowStart);
        rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
        kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
    }


    @Test
    public void testConsecDaysBrokenChain(){
        MinMaxContractLine contractLine = createMaxContractLine(ContractLineType.CONSECUTIVE_TWELVE_HOUR_DAYS, true, 3, -1);
        kSession.insert(contractLine);

        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        Contract contract = createContract(false, employee, contractLine);
        contractLine.setContract(contract);
        kSession.insert(contract);

        ShiftType shiftType = createShiftType(1, new LocalTime(1, 0, 0), new LocalTime(13, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
        kSession.insert(shiftDate);

        ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
        kSession.insert(secondShiftDate);

        ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 30));
        kSession.insert(thirdShiftDate);

        ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 31));
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

        ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
        kSession.insert(fourthShiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.MAX_CONSECUTIVE_12H_DAYS_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfConsecDaysConstraints);
    }

    @Test
    public void testConsecDaysInPlanningWindowViolated(){
        MinMaxContractLine contractLine = createMaxContractLine(ContractLineType.CONSECUTIVE_TWELVE_HOUR_DAYS, true, 3, -1);
        kSession.insert(contractLine);

        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        Contract contract = createContract(false, employee, contractLine);
        contractLine.setContract(contract);
        kSession.insert(contract);

        ShiftType shiftType = createShiftType(1, new LocalTime(2, 0, 0), new LocalTime(14, 0, 0), false);
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

        ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
        kSession.insert(fourthShiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.MAX_CONSECUTIVE_12H_DAYS_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
        assertEquals(contractLine.getMaximumWeight(), numOfConsecDaysConstraints);
    }

    @Test
    public void testRuleWeightMultiplier(){
        // Change the weight multiplier of the rule
        int ruleMultiplier = 4;
        FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
        rosterInfo.putRuleWeightMultiplier(RuleName.MAX_CONSECUTIVE_12H_DAYS_CONSTRAINT, ruleMultiplier);
        kSession.update(rosterHandle, rosterInfo);

        MinMaxContractLine contractLine = createMaxContractLine(ContractLineType.CONSECUTIVE_TWELVE_HOUR_DAYS, true, 3, -1);
        kSession.insert(contractLine);

        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        Contract contract = createContract(false, employee, contractLine);
        contractLine.setContract(contract);
        kSession.insert(contract);

        ShiftType shiftType = createShiftType(1, new LocalTime(3, 0, 0), new LocalTime(15, 0, 0), false);
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

        ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
        kSession.insert(fourthShiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.MAX_CONSECUTIVE_12H_DAYS_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
        assertEquals(ruleMultiplier * contractLine.getMaximumWeight(), numOfConsecDaysConstraints);
    }

    @Test
    public void testConsecDaysInScheduleWindowViolated(){
        MinMaxContractLine contractLine = createMaxContractLine(ContractLineType.CONSECUTIVE_TWELVE_HOUR_DAYS, true, 3, -2);
        kSession.insert(contractLine);

        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        Contract contract = createContract(false, employee, contractLine);
        contractLine.setContract(contract);
        kSession.insert(contract);

        ShiftType shiftType = createShiftType(1, new LocalTime(3, 0, 0), new LocalTime(15, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
        kSession.insert(shiftDate);

        ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
        kSession.insert(secondShiftDate);

        ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 27));
        kSession.insert(thirdShiftDate);

        ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 28));
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

        ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
        kSession.insert(fourthShiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.MAX_CONSECUTIVE_12H_DAYS_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
        assertEquals(contractLine.getMaximumWeight(), numOfConsecDaysConstraints);
    }

    @Test
    public void testConsecDaysOutsideScheduleWindowViolated(){
        MinMaxContractLine contractLine = createMaxContractLine(ContractLineType.CONSECUTIVE_TWELVE_HOUR_DAYS, true, 3, -2);
        kSession.insert(contractLine);

        Employee employee = createEmployee(1, "MR.", "X");
        kSession.insert(employee);

        Contract contract = createContract(false, employee, contractLine);
        contractLine.setContract(contract);
        kSession.insert(contract);

        ShiftType shiftType = createShiftType(1, new LocalTime(3, 0, 0), new LocalTime(15, 0, 0), false);
        kSession.insert(shiftType);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 22));
        kSession.insert(shiftDate);

        ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 23));
        kSession.insert(secondShiftDate);

        ShiftDate thirdShiftDate = new ShiftDate(new LocalDate(2014, 5, 24));
        kSession.insert(thirdShiftDate);

        ShiftDate fourthShiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
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

        ShiftAssignment fourthShiftAssignment = createShiftAssignment(employee, 0, fourthShift);
        kSession.insert(fourthShiftAssignment);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfConsecDaysConstraints = getNumOfConstraintMatches(RuleName.MAX_CONSECUTIVE_12H_DAYS_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
        assertEquals(contractLine.getMaximumWeight(), numOfConsecDaysConstraints);
    }


}
