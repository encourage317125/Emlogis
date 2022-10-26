package com.emlogis.engine.domain.solver;

import com.emlogis.engine.domain.EmployeeSchedule;
import com.emlogis.engine.domain.ShiftAssignment;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.DateTime;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;

/**
 * Sorts the difficulty of planning a shift assignment based on
 * number of employees who have the right skill and the right team 
 * as well as the total duration of the shift being scheduled. 
 * 
 * The more difficult a shift is to schedule the sooner we should 
 * try to schedule it. 
 * 
 * @author emlogis
 *
 */
public class ShiftAssignmentDifficultyFactory implements SelectionSorterWeightFactory<EmployeeSchedule, ShiftAssignment> {

    public Comparable createSorterWeight(EmployeeSchedule schedule, ShiftAssignment shift) {
    	String skillId = shift.getShift().getSkillId();
    	String teamId = shift.getShift().getTeamId();
    	
    	// Set a default value of 0
    	int numberOfMatchingEmployees = 0;
    	if(schedule.getNumEmployeesPerSkill().containsKey(skillId))
    		numberOfMatchingEmployees = schedule.getNumEmployeesPerSkill().get(skillId);
      
    	// Set a default value of 0
    	int numberOfEmployeesMatchingTeam  = 0; 
    	if(schedule.getNumEmployeesPerTeam().containsKey(teamId))
    		numberOfEmployeesMatchingTeam = schedule.getNumEmployeesPerTeam().get(teamId);
    	
        return new ShiftAssignmentDifficultyWeight(shift, numberOfMatchingEmployees, numberOfEmployeesMatchingTeam);
    }

    public static class ShiftAssignmentDifficultyWeight implements Comparable<ShiftAssignmentDifficultyWeight> {

        private final ShiftAssignment shift;
        private final int numberOfEmployeesMatchingSkill;
        private final int numberOfEmployeesMatchingTeam;

        public ShiftAssignmentDifficultyWeight(ShiftAssignment shift, int numberOfEmployeesMatchingSkill, int numberOfEmployeesMatchingTeam) {
            this.shift = shift;
            this.numberOfEmployeesMatchingSkill = numberOfEmployeesMatchingSkill;
            this.numberOfEmployeesMatchingTeam = numberOfEmployeesMatchingTeam;
        }

        public int compareTo(ShiftAssignmentDifficultyWeight other) {
            return new CompareToBuilder()
                    // The less employees who have the skill the sooner it should be filled
            		.append(other.numberOfEmployeesMatchingSkill, numberOfEmployeesMatchingSkill) // Descending
                    .append(other.numberOfEmployeesMatchingTeam, numberOfEmployeesMatchingTeam) // Descending
                    .append(getShiftDurationMinutes(), other.getShiftDurationMinutes()) // Ascending
                    // Tie breaker
                    .append(other.getShiftStartDateTime(), getShiftStartDateTime()) // Descending
                    .append(getShiftId(), other.getShiftId())
                    .toComparison();
        }
        
        public DateTime getShiftStartDateTime(){
        	return shift.getShiftStartDateTime();
        }
        
        public int getShiftDurationMinutes(){
        	return shift.getShiftDurationMinutes();
        }
        
        public String getShiftId(){
        	return shift.getShiftId();
        }

    }

}