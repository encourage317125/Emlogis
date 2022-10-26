package com.emlogis.engine.domain.solver.filters;

import com.emlogis.engine.domain.Employee;
import com.emlogis.engine.domain.ShiftAssignment;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.move.generic.SwapMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

/**
 * Only allow a swap of employees when the employees are on correct teams
 * 
 * @author emlogis
 *
 */
public class EmployeeOnTeamSwapFilter extends EmployeeFilters implements SelectionFilter<SwapMove> {

	@Override
	public boolean accept(ScoreDirector scoreDirector, SwapMove selection) {
		ShiftAssignment leftAssignment = (ShiftAssignment) selection.getLeftEntity();
		ShiftAssignment rightAssignment = (ShiftAssignment) selection.getRightEntity();
		
		Employee leftEmp = leftAssignment.getEmployee();
		Employee rightEmp = rightAssignment.getEmployee();
		
		return (rightEmp == null || employeeOnTeam(leftAssignment, rightEmp)) && 
			   (leftEmp == null || employeeOnTeam(rightAssignment, leftEmp));
	}
}
