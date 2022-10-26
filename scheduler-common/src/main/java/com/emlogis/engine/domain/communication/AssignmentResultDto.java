package com.emlogis.engine.domain.communication;

import java.util.Collection;
import java.util.List;

public class AssignmentResultDto extends ScheduleResultDto {

	private List<ShiftAssignmentDto> shiftAssignments;
	private Collection<IntConstraintMatchTotalDto> constraintMatchTotals;
	
	public List<ShiftAssignmentDto> getShiftAssignments() {
		return shiftAssignments;
	}
	public void setShiftAssignments(List<ShiftAssignmentDto> shiftAssignments) {
		this.shiftAssignments = shiftAssignments;
	}
	
	public Collection<IntConstraintMatchTotalDto> getConstraintMatchTotals() {
		return constraintMatchTotals;
	}

	public void setConstraintMatchTotals(Collection<IntConstraintMatchTotalDto> constraintMatchTotals) {
		this.constraintMatchTotals = constraintMatchTotals;
	}
}
