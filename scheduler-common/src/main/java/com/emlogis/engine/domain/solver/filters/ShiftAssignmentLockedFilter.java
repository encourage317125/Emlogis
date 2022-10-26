package com.emlogis.engine.domain.solver.filters;

import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import com.emlogis.engine.domain.ShiftAssignment;

public class ShiftAssignmentLockedFilter implements SelectionFilter<ShiftAssignment> {

	@Override
	public boolean accept(ScoreDirector scoreDirector, ShiftAssignment selection) {
		return !selection.isLocked();
	}
}
