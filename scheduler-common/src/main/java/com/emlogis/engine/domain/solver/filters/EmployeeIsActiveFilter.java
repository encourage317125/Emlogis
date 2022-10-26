package com.emlogis.engine.domain.solver.filters;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import com.emlogis.engine.domain.Employee;
import com.emlogis.engine.domain.ShiftAssignment;

/**
 * 
 * Only assign an employee to a shift when the employee is schedulable AND
 * the Shift happens while the employee is employed 
 * 
 * @author emlogis
 *
 */
public class EmployeeIsActiveFilter extends EmployeeFilters implements SelectionFilter<ChangeMove> {

	@Override
	public boolean accept(ScoreDirector scoreDirector, ChangeMove selection) {
		Collection<ShiftAssignment> shiftAssignments = (Collection<ShiftAssignment>) selection.getPlanningEntities();
		Employee employee = (Employee) selection.getToPlanningValue();
		ShiftAssignment shiftAssignment = shiftAssignments.iterator().next();
	
		if(employee == null) return true; // Taking an employee off a shift is always accepted 
		
		return isEmployeeWorking(shiftAssignment, employee) && isEmployeeSchedulable(employee);
	}

}
