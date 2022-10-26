package com.emlogis.engine.domain.solver.filters;

import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import com.emlogis.engine.domain.ShiftAssignment;

public class ExcessShiftChangeMoveFilter implements SelectionFilter<ChangeMove> {

    public boolean accept(ScoreDirector scoreDirector, ChangeMove move) {
        ShiftAssignment shiftAssignment = (ShiftAssignment) move.getPlanningEntities().iterator().next();
        return !shiftAssignment.isExcessShift();
    }
}
