package com.emlogis.engine.domain.solver.filters;

import com.emlogis.engine.domain.Employee;
import com.emlogis.engine.domain.ShiftAssignment;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.Collection;

/**
 * Only assign an employee to a shift when the employee belongs to the team 
 * needed by the shift (FLOAT or ON)
 * 
 * @author emlogis
 *
 */
public class EmployeeIsOnTeamFilter extends EmployeeFilters implements SelectionFilter<ChangeMove> {

	@Override
	public boolean accept(ScoreDirector scoreDirector, ChangeMove selection) {
		Collection<ShiftAssignment> shiftAssignments = (Collection<ShiftAssignment>) selection.getPlanningEntities();
		Employee employee = (Employee) selection.getToPlanningValue();
		ShiftAssignment shiftAssignment = shiftAssignments.iterator().next();
	
		if(employee == null) return true; // Taking an employee off a shift is always accepted 
		
		return employeeOnTeam(shiftAssignment, employee);
	}



}
