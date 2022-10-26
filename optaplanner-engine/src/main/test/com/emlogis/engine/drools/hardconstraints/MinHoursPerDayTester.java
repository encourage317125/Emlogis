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
import com.emlogis.engine.domain.timeoff.CDTimeWindow;
import com.emlogis.engine.drools.ConstraintTesterBase;

public class MinHoursPerDayTester extends ConstraintTesterBase{

    @Override
    protected void loadRosterInfo() {
        ShiftDate planningWindowStart = new ShiftDate(new LocalDate(2014, 5, 25));
        rosterInfo.setFirstShiftDate(planningWindowStart.getDateOfFirstDayOfWeek(rosterInfo.getFirstDayOfWeek()));
        rosterInfo.setPlanningWindowStart(planningWindowStart);
        rosterInfo.setLastShiftDate(rosterInfo.getFirstShiftDate().plusDays(7));
        kSession.insert(rosterInfo.getFirstShiftDateOn(rosterInfo.getFirstDayOfWeek()));
    }

    @Test
    public void testMinHoursPerDayOutsidePlanning() {
        MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_DAY, true, 12, -1);

        Employee employee = createEmployee(1, "MR.", "X");

        Contract contract = createContract(false, employee, contractLine);
        contractLine.setContract(contract);
        kSession.insert(contract);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(18, 0, 0), false);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 24));

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

        kSession.insert(shiftType);
        kSession.insert(shiftDate);
        kSession.insert(shift);
        kSession.insert(shiftAssignment);
        kSession.insert(employee);
        kSession.insert(contractLine);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMinHoursDayConstraint = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_DAY_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfMinHoursDayConstraint);
    }

    @Test
    public void testMinHoursPerDayViolated() {
        int minValue = 12;
        MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_DAY, true, minValue, -1);

        Employee employee = createEmployee(1, "MR.", "X");

        Contract contract = createContract(false, employee, contractLine);
        contractLine.setContract(contract);
        kSession.insert(contract);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(18, 0, 0), false);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

        kSession.insert(shiftType);
        kSession.insert(shiftDate);
        kSession.insert(shift);
        kSession.insert(shiftAssignment);
        kSession.insert(employee);
        kSession.insert(contractLine);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMinHoursDayConstraint = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_DAY_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
        assertEquals(contractLine.getMinimumWeight()*(minValue - shift.getShiftDurationHours()), numOfMinHoursDayConstraint);
    }

    @Test
    public void testRuleWeightMultiplier() {
        // Change the weight multiplier of the rule
        int ruleMultiplier = 4;
        FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
        rosterInfo.putRuleWeightMultiplier(RuleName.MIN_HOURS_PER_DAY_CONSTRAINT, ruleMultiplier);
        kSession.update(rosterHandle, rosterInfo);

        int minValue = 12;
        MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_DAY, true, minValue, -1);

        Employee employee = createEmployee(1, "MR.", "X");

        Contract contract = createContract(false, employee, contractLine);
        contractLine.setContract(contract);
        kSession.insert(contract);

        ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(8, 0, 0), false);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
        ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
        kSession.insert(secondShiftDate);

        Shift shift = createShift("1", 0, shiftDate, secondShiftDate, shiftType, 1);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

        kSession.insert(shiftType);
        kSession.insert(shiftDate);
        kSession.insert(shift);
        kSession.insert(shiftAssignment);
        kSession.insert(employee);
        kSession.insert(contractLine);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMinHoursDayConstraint = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_DAY_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
        assertEquals(ruleMultiplier * contractLine.getMinimumWeight()*(minValue - shift.getShiftDurationHours()), numOfMinHoursDayConstraint);
    }

    @Test
    public void testMinHoursPerDayViolatedOvernight() {
        int minValue = 12;
        MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_DAY, true, minValue, -1);

        Employee employee = createEmployee(1, "MR.", "X");

        Contract contract = createContract(false, employee, contractLine);
        contractLine.setContract(contract);
        kSession.insert(contract);

        ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(8, 0, 0), false);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
        ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
        kSession.insert(secondShiftDate);

        Shift shift = createShift("1", 0, shiftDate, secondShiftDate, shiftType, 1);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

        kSession.insert(shiftType);
        kSession.insert(shiftDate);
        kSession.insert(shift);
        kSession.insert(shiftAssignment);
        kSession.insert(employee);
        kSession.insert(contractLine);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMinHoursDayConstraint = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_DAY_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
        assertEquals(contractLine.getMinimumWeight()*(minValue - shift.getShiftDurationHours()), numOfMinHoursDayConstraint);
    }

    @Test
    public void testMinHoursPerDayEnoughHours() {
        MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_DAY, true, 8, -1);

        Employee employee = createEmployee(1, "MR.", "X");

        Contract contract = createContract(false, employee, contractLine);
        contractLine.setContract(contract);
        kSession.insert(contract);

        ShiftType shiftType = createShiftType(1, new LocalTime(10, 0, 0), new LocalTime(20, 0, 0), false);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

        kSession.insert(shiftType);
        kSession.insert(shiftDate);
        kSession.insert(shift);
        kSession.insert(shiftAssignment);
        kSession.insert(employee);
        kSession.insert(contractLine);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMinHoursDayConstraint = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_DAY_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfMinHoursDayConstraint);
    }

    @Test
    public void testMinHoursPerDayEnoughHoursWithPTO() {
        MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_DAY, true, 12, -1);

        Employee employee = createEmployee(1, "MR.", "X");

        Contract contract = createContract(false, employee, contractLine);
        contractLine.setContract(contract);
        kSession.insert(contract);

        ShiftType shiftType = createShiftType(1, new LocalTime(6, 0, 0), new LocalTime(12, 0, 0), false);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

        CDTimeWindow cdTimeOff = createCDTimeOff(employee.getEmployeeId(), shiftDate.getDate(), new LocalTime(12, 01, 0), new LocalTime(18, 03, 0), -1);
        kSession.insert(cdTimeOff);

        kSession.insert(shiftType);
        kSession.insert(shiftDate);
        kSession.insert(shift);
        kSession.insert(shiftAssignment);
        kSession.insert(employee);
        kSession.insert(contractLine);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMinHoursDayConstraint = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_DAY_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfMinHoursDayConstraint);
    }
    
    @Test
    public void testMinHoursPerDayNotEnoughPTOOff() {
    	int minHours = 12;
    	FactHandle rosterHandle = kSession.getFactHandle(rosterInfo);
		rosterInfo.setIncludePTOInMinCalculations(false);
		kSession.update(rosterHandle, rosterInfo);
        MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_DAY, true, minHours, -1);

        Employee employee = createEmployee(1, "MR.", "X");

        Contract contract = createContract(false, employee, contractLine);
        contractLine.setContract(contract);
        kSession.insert(contract);

        ShiftType shiftType = createShiftType(1, new LocalTime(6, 0, 0), new LocalTime(12, 0, 0), false);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));

        Shift shift = createShift("1", 0, shiftDate, shiftType, 1);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

        CDTimeWindow cdTimeOff = createCDTimeOff(employee.getEmployeeId(), shiftDate.getDate(), new LocalTime(12, 01, 0), new LocalTime(18, 03, 0), -1);
        kSession.insert(cdTimeOff);

        kSession.insert(shiftType);
        kSession.insert(shiftDate);
        kSession.insert(shift);
        kSession.insert(shiftAssignment);
        kSession.insert(employee);
        kSession.insert(contractLine);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMinHoursDayConstraint = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_DAY_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
        assertEquals(shift.getShiftDurationHours()-minHours, numOfMinHoursDayConstraint);
    }


    @Test
    public void testMinHoursPerDayEnoughOvernight() {
        MinMaxContractLine contractLine = createMinContractLineInHours(ContractLineType.HOURS_PER_DAY, true, 8, -1);

        Employee employee = createEmployee(1, "MR.", "X");

        Contract contract = createContract(false, employee, contractLine);
        contractLine.setContract(contract);
        kSession.insert(contract);

        ShiftType shiftType = createShiftType(1, new LocalTime(22, 0, 0), new LocalTime(10, 0, 0), false);

        ShiftDate shiftDate = new ShiftDate(new LocalDate(2014, 5, 25));
        ShiftDate secondShiftDate = new ShiftDate(new LocalDate(2014, 5, 26));
        kSession.insert(secondShiftDate);

        Shift shift = createShift("1", 0, shiftDate, secondShiftDate, shiftType, 1);

        ShiftAssignment shiftAssignment = createShiftAssignment(employee, 0, shift);

        kSession.insert(shiftType);
        kSession.insert(shiftDate);
        kSession.insert(shift);
        kSession.insert(shiftAssignment);
        kSession.insert(employee);
        kSession.insert(contractLine);

        kSession.fireAllRules();

        BendableScoreHolder scoreHolder = (BendableScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
        int numOfMinHoursDayConstraint = getNumOfConstraintMatches(RuleName.MIN_HOURS_PER_DAY_CONSTRAINT, scoreHolder.getConstraintMatchTotals());
        assertEquals(0, numOfMinHoursDayConstraint);
    }

}
