package com.emlogis.engine.domain.solver.filters;

import com.emlogis.engine.domain.Employee;
import com.emlogis.engine.domain.ShiftAssignment;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

/**
 * Methods that check if employee is elgible to be scheduled 
 * 
 * @author emlogis
 *
 */
public abstract class EmployeeFilters {

	/**
	 * Returns true if Employee has skill required by Shift 
	 * 
	 * @param shiftAssignment
	 * @param employee
	 * @return
	 */
	protected boolean employeeHasSkill(ShiftAssignment shiftAssignment, Employee employee){
		return employee.getSkillIds().contains(shiftAssignment.getShift().getSkillId());
	}
	
	/**
	 * Returns true if employee belongs to or floats on team 
	 * required by Shift
	 * 
	 * @param shiftAssignment
	 * @param employee
	 * @return
	 */
	public boolean employeeOnTeam(ShiftAssignment shiftAssignment, Employee employee){
		return employee.getTeamIds().contains(shiftAssignment.getShift().getTeamId());
	}
	
	/**
	 * Returns true if Shift occurs between the time that the employee begins his 
	 * employment with client and before employee terminates his employment. 
	 * 
	 * @param shiftAssignment
	 * @param employee
	 * @return
	 */
	public boolean isEmployeeWorking(ShiftAssignment shiftAssignment, Employee employee) {
		DateTime shiftStartTime = shiftAssignment.getShift().getShiftStartDateTime();
		DateTime shiftEndTime = shiftAssignment.getShift().getShiftEndDateTime();

		boolean isAccepted = employee.isEmployeeActive(shiftStartTime, shiftEndTime);
		if (!isAccepted)
			Logger.getLogger(this.getClass()).trace(employee + " begin or ends employment outside of  " + shiftAssignment);
		
		
		return isAccepted;
	}
	
	/**
	 * Returns true if Employee is marked as Schedulable in the database
	 * 
	 * @param employee
	 * @return
	 */
	public boolean isEmployeeSchedulable(Employee employee){
		return employee.isScheduleable();
	}
	
	
}
