package com.emlogis.engine.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emlogis.engine.domain.EmployeeSchedule;
import com.emlogis.engine.domain.Shift;
import com.emlogis.engine.domain.ShiftAssignment;
import com.emlogis.engine.domain.ShiftDate;

public abstract class AbstractEngineManager {
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	// If true include constraint details for assignment, qualification and eligibility
	protected boolean includeResultDetails; 

	public boolean isIncludeResultDetails() {
		return includeResultDetails;
	}

	public void setIncludeResultDetails(boolean includeResultDetails) {
		this.includeResultDetails = includeResultDetails;
	}

	/**
	 * Iterates through the list of Shift objects in the schedule
	 * and create a Shift Assignment object for each shift that does
	 * not already have a shift assignment associated to it. 
	 * 
	 * @param workingSolution
	 */
	protected void createShiftAssignments(EmployeeSchedule workingSolution) {
		// Create shift assignments for shifts without an existing matching shift assignment
		List<ShiftAssignment> existingAssignments = workingSolution.getShiftAssignmentList();
		List<ShiftAssignment> newAssignments = createShiftAssignments(workingSolution.getShiftList(),
                existingAssignments);
		
		workingSolution.getShiftAssignmentList().addAll(newAssignments);
	}
	
	/**
	 * For each shift creates a shift assignment to cover each shift's required
	 * employee size. 
	 * 
	 * @param shifts Shift demand for the current schedule
	 * @param existingShiftAssignments Existing shift assignments in the schedule
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
    
    /**
	 * Creates a shift date object for ever
	 * date from the schedule start date to the end
	 * 
	 * @param workingSolution
	 */
	protected void createShiftDateObjects(EmployeeSchedule workingSolution){
		ShiftDate startDate = workingSolution.getEmployeeRosterInfo().getFirstShiftDate();
		
		//Extra day required by two week overtime constraint
		ShiftDate endDate   = workingSolution.getEmployeeRosterInfo().getLastShiftDate().plusDays(1);
		ShiftDate currentDate = startDate;
		while(currentDate.isBeforeOrEquals(endDate)){
			workingSolution.getShiftDateList().add(currentDate);
			currentDate = currentDate.plusDays(1);
		}
	}

	protected Collection<ShiftAssignment> getMatchingShiftAssignments(final Shift shift, List<ShiftAssignment> existingShiftAssigments) {
		return CollectionUtils.select(existingShiftAssigments, new Predicate<ShiftAssignment>() {

			@Override
			public boolean evaluate(ShiftAssignment shiftAssigment) {
				return shiftAssigment.getShift().equals(shift);
			}
		});
	}
	
	protected Collection<ShiftAssignment> getShiftAssignmentsBeingQualified(List<ShiftAssignment> shifts){
		return CollectionUtils.select(shifts, new Predicate<ShiftAssignment>() {
			@Override
			public boolean evaluate(ShiftAssignment shift) {
				return shift.isBeingQualified();
			}
		});
	}
	
	protected Collection<ShiftAssignment> getShiftAssignmentsBeingSwapped(List<ShiftAssignment> shifts){
		return CollectionUtils.select(shifts, new Predicate<ShiftAssignment>() {
			@Override
			public boolean evaluate(ShiftAssignment shift) {
				return shift.isBeingSwapped();
			}
		});
	}
	
	protected Collection<Shift> getShiftsBeingQualified(List<Shift> shifts){
		return CollectionUtils.select(shifts, new Predicate<Shift>() {
			@Override
			public boolean evaluate(Shift shift) {
				return shift.isBeingQualified();
			}
		});
	}
	
	protected Collection<Shift> getShiftsBeingSwapped(List<Shift> shifts){
		return CollectionUtils.select(shifts, new Predicate<Shift>() {
			@Override
			public boolean evaluate(Shift shift) {
				return shift.isBeingSwapped();
			}
		});
	}


}
