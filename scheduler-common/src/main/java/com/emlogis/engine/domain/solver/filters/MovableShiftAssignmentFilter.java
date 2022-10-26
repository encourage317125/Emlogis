package com.emlogis.engine.domain.solver.filters;

import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import com.emlogis.engine.domain.EmployeeSchedule;
import com.emlogis.engine.domain.ShiftAssignment;
import com.emlogis.engine.domain.ShiftDate;

public class MovableShiftAssignmentFilter implements SelectionFilter<ChangeMove> {

    public boolean accept(ScoreDirector scoreDirector, ChangeMove move) {
        EmployeeSchedule employeeSchedule = (EmployeeSchedule) scoreDirector.getWorkingSolution();
        ShiftAssignment shiftAssignment = (ShiftAssignment) move.getPlanningEntities().iterator().next();
        return accept(employeeSchedule, shiftAssignment);
    }

    public boolean accept(EmployeeSchedule employeeSchedule, ShiftAssignment shiftAssignment) {
        ShiftDate shiftDate = shiftAssignment.getShift().getStartShiftDate();
        return employeeSchedule.getEmployeeRosterInfo().isInPlanningWindow(shiftDate);
    }

}
