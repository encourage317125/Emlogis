package com.emlogis.engine.domain.solver.filters;

import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import com.emlogis.engine.domain.ShiftAssignment;

public class OnlyExcessShiftFilter implements SelectionFilter<ShiftAssignment> {

    public boolean accept(ScoreDirector scoreDirector, ShiftAssignment shiftAssignment) {
        return shiftAssignment.isExcessShift();
    }
}
