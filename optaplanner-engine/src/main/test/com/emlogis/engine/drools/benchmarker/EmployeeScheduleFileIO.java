package com.emlogis.engine.drools.benchmarker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.io.FileUtils;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.engine.domain.EmployeeSchedule;
import com.emlogis.engine.domain.Shift;
import com.emlogis.engine.domain.ShiftAssignment;
import com.emlogis.engine.domain.ShiftDate;
import com.emlogis.engine.domain.dto.AssignmentRequestDto;
import com.emlogis.scheduler.engine.communication.AssignmentRequestSerializer;

public class EmployeeScheduleFileIO implements SolutionFileIO {

	@Override
	public String getInputFileExtension() {
		return "xml";
	}

	@Override
	public String getOutputFileExtension() {
		return "xml";
	}

	@Override
	public Solution read(File inputSolutionFile) {
		AssignmentRequestDto assignmentRequestDto = null;
		try {
			assignmentRequestDto = EmlogisUtils.fromJsonString(FileUtils.readFileToString(inputSolutionFile),
					AssignmentRequestDto.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AssignmentRequestSerializer serializer = new AssignmentRequestSerializer();
		EmployeeSchedule employeeSchedule = serializer.mapToEmployeeSchedule(assignmentRequestDto);

		// Create shift assignment objects for each shift in the schedule
		createShiftAssignments(employeeSchedule);

		// Create a shift date object for every date in the schedule
		createShiftDateObjects(employeeSchedule);

		return employeeSchedule;
	}

	@Override
	public void write(Solution solution, File outputSolutionFile) {
		// TODO Auto-generated method stub

	}

	// TODO: REMOVE THIS DUPLICATED CODE that is in AbstractEngineManager

	/**
	 * Creates a shift date object for ever date from the schedule start date to
	 * the end
	 * 
	 * @param workingSolution
	 */
	protected void createShiftDateObjects(EmployeeSchedule workingSolution) {
		ShiftDate startDate = workingSolution.getEmployeeRosterInfo().getFirstShiftDate();
		
		//Extra day required by two week overtime constraint
		ShiftDate endDate   = workingSolution.getEmployeeRosterInfo().getLastShiftDate().plusDays(1);
		ShiftDate currentDate = startDate;
		while (currentDate.isBeforeOrEquals(endDate)) {
			workingSolution.getShiftDateList().add(currentDate);
			currentDate = currentDate.plusDays(1);
		}
	}

	/**
	 * Iterates through the list of Shift objects in the schedule and create a
	 * Shift Assignment object for each shift that does not already have a shift
	 * assignment associated to it.
	 * 
	 * @param workingSolution
	 */
	protected void createShiftAssignments(EmployeeSchedule workingSolution) {
		// Create shift assignments for shifts without an existing matching
		// shift assignment
		List<ShiftAssignment> existingAssignments = workingSolution.getShiftAssignmentList();
		List<ShiftAssignment> newAssignments = createShiftAssignments(workingSolution.getShiftList(),
				existingAssignments);

		workingSolution.getShiftAssignmentList().addAll(newAssignments);
	}

	/**
	 * For each shift creates a shift assignment to cover each shift's required
	 * employee size.
	 * 
	 * @param shifts
	 *            Shift demand for the current schedule
	 * @param existingShiftAssignments
	 *            Existing shift assignments in the schedule
	 * @return
	 */
	protected List<ShiftAssignment> createShiftAssignments(List<Shift> shifts,
			List<ShiftAssignment> existingShiftAssignments) {
		List<ShiftAssignment> shiftAssignments = new ArrayList<>();
		for (final Shift shift : shifts) {
			Collection<ShiftAssignment> matchingShiftAssignments = getMatchingShiftAssignments(shift,
					existingShiftAssignments);

			for (int i = 0; i < shift.getRequiredEmployeeSize() - matchingShiftAssignments.size(); i++) {
				ShiftAssignment shiftAssignment = new ShiftAssignment();
				shiftAssignment.setIndexInShift(i);
				shiftAssignment.setShift(shift);
				shiftAssignments.add(shiftAssignment);
			}
		}
		return shiftAssignments;
	}

	protected Collection<ShiftAssignment> getMatchingShiftAssignments(final Shift shift,
			List<ShiftAssignment> existingShiftAssigments) {
		return CollectionUtils.select(existingShiftAssigments, new Predicate<ShiftAssignment>() {

			@Override
			public boolean evaluate(ShiftAssignment shiftAssigment) {
				return shiftAssigment.getShift().equals(shift);
			}
		});
	}

}
