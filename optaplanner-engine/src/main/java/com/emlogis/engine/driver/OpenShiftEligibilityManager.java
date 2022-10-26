package com.emlogis.engine.driver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;
import org.kie.api.runtime.rule.FactHandle;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;

import com.emlogis.engine.domain.Employee;
import com.emlogis.engine.domain.EmployeeSchedule;
import com.emlogis.engine.domain.Shift;
import com.emlogis.engine.domain.ShiftAssignment;
import com.emlogis.engine.domain.communication.ShiftQualificationDto;
import com.emlogis.engine.domain.communication.constraints.ShiftConstraintDto;
import com.emlogis.engine.solver.drools.score.QualificationScoreHolder;

public class OpenShiftEligibilityManager extends QualificationManager{
	
	/**
	 * For every shift being qualified create a ShiftAssignment 
	 * for that shift and for each employee in the schedule.
	 * 
	 * Run schedule qualification for each ScheduleAssignment,
	 * get and store the results then remove the ScheduleAssigment
	 * before continuing. 
	 * 
	 * @param schedule
	 * @return Eligibility results
	 */
	public Collection<ShiftQualificationDto> getAssignmentsEligibility(EmployeeSchedule schedule){
		StopWatch watch = new StopWatch();
		watch.start();
		loadScheduleData(schedule);
		
		shiftQualificationResults = new ArrayList<>();
		Collection<Shift> shiftsBeingQualified = getShiftsBeingQualified(schedule.getShiftList());
		logger.info(shiftsBeingQualified.size() + " shifts being checked for eligibility against " 
												+ schedule.getEmployeeList().size() + " employees.");
		
		// Set up variables for statistics
		int totalCombinations = shiftsBeingQualified.size()
				* schedule.getEmployeeList().size();
		int totalTried = 0;

		for (Shift currentShift : shiftsBeingQualified) {
			for (Employee emp : schedule.getEmployeeList()){
				totalTried++;
				ShiftAssignment assignment = new ShiftAssignment();
				assignment.setShift(currentShift);
				assignment.setIndexInShift(0);
				assignment.setEmployee(emp);
				
				String notificationMsg = "Testing eligibility of: " + emp.getFullName() + " at shift: " + assignment.getShiftId();
				logger.debug(notificationMsg);
				
				double percentComplete = Math.round(100.0*totalTried/totalCombinations);
				notifyProgress(percentComplete, notificationMsg);
				
				
				// Get list of shifts being qualified and initialize QualfiicationScoreHolder using this list
				Collection<ShiftAssignment> shiftBeingQualfied =  Arrays.asList(assignment);
				kSession.setGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY, new QualificationScoreHolder(true, shiftBeingQualfied));
				
				FactHandle assignmentHandle = kSession.insert(assignment);
				
				//ATTACK!! Fire the rules in order to get schedule conflicts
				kSession.fireAllRules();
				
				// Retrieve scoreHolder global containing score and constraints
				QualificationScoreHolder scoreHolder = (QualificationScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
				Collection<ShiftQualificationDto> convertedResults = convertQualificationResults(scoreHolder.getShiftConstraintQualificationMap());
				
				if(convertedResults.iterator().hasNext()){
				ShiftQualificationDto resultDto = convertedResults.iterator().next();
					if(resultDto.getIsAccepted())
						logger.debug("Shift Assignment is accepted");
					else 
						logger.debug("Shift Assignment rejected due to " + resultDto.getCauses());
				} else { // If include details is off rejected assignments are not included in results
					logger.debug("Shift Assignment rejected, set includeDetails == true for details");
				}
				shiftQualificationResults.addAll(convertedResults);
				
				kSession.delete(assignmentHandle);
			}
		}
		watch.stop();
		logger.info("Eligibility process finished after " +  watch.getTime() + " ms");
		
		return shiftQualificationResults;
	}
	
}
