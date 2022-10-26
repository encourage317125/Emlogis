package com.emlogis.engine.domain.solver.filters;

import com.emlogis.engine.domain.Employee;
import com.emlogis.engine.domain.ShiftAssignment;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.PillarChangeMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Collection;

/**
 * Only assign an employee to a pillar of shifts when the employee has the required skill
 * for each shift
 * 
 * @author emlogis
 * 
 */
public class EmployeeHasSkillPillarChangeFilter extends EmployeeFilters implements
		SelectionFilter<PillarChangeMove> {

	@Override
	public boolean accept(ScoreDirector scoreDirector,
			PillarChangeMove selection) {
		Collection<ShiftAssignment> shiftAssignments = (Collection<ShiftAssignment>) selection
				.getPlanningEntities();
		Employee employee = (Employee) selection.getToPlanningValue();
		if (employee == null)
			return true; // Taking an employee off a shift is always accepted

		// If any shift requires skill employee does not have reject pillar move
		for (ShiftAssignment shiftAssignment : shiftAssignments) {
			if (!employeeHasSkill(shiftAssignment, employee))
				return false;
		}
		
		return true;
	}

}
