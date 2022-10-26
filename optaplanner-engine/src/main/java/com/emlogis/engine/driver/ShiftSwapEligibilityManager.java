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
import com.emlogis.engine.domain.ShiftAssignment;
import com.emlogis.engine.domain.communication.ShiftQualificationDto;
import com.emlogis.engine.domain.communication.constraints.ShiftConstraintDto;
import com.emlogis.engine.solver.drools.score.QualificationScoreHolder;

public class ShiftSwapEligibilityManager extends QualificationManager {
    protected Map<String, Collection<ShiftQualificationDto>> shiftSwapResults;

    /**
     * For every shift being checked for swap eligibility swap the shift with
     * every shift that has beingQualified==true and check for constraint
     * violations
     * 
     * Run schedule qualification for each ScheduleAssignment, get and store the
     * results then remove the ScheduleAssigment before continuing.
     * 
     * @param schedule
     * @return Eligibility results
     */
    public Map<String, Collection<ShiftQualificationDto>> getShiftSwapEligibility(EmployeeSchedule schedule) {
	StopWatch watch = new StopWatch();
	watch.start();
	loadScheduleData(schedule);

	shiftSwapResults = new HashMap<>();
	shiftQualificationResults = new ArrayList<>();

	Collection<ShiftAssignment> shiftsBeingSwapped = getShiftAssignmentsBeingSwapped(schedule
		.getShiftAssignmentList());
	Collection<ShiftAssignment> shiftsBeingQualified = getShiftAssignmentsBeingQualified(schedule
		.getShiftAssignmentList());

	logger.info(shiftsBeingSwapped.size() + " shifts being checked for swap eligibility against "
		+ shiftsBeingQualified.size() + " shifts.");

	// Set up variables for statistics
	int totalCombinations = shiftsBeingSwapped.size() * shiftsBeingQualified.size();
	int totalTried = 0;
	try {
	    for (ShiftAssignment shiftAssignmentA : shiftsBeingSwapped) {
		FactHandle shiftAssignmentAFactHandle = kSession.getFactHandle(shiftAssignmentA);
		for (ShiftAssignment shiftAssignmentB : shiftsBeingQualified) {
		    totalTried++;
		    FactHandle shiftAssignmentBFactHandle = kSession.getFactHandle(shiftAssignmentB);

		    String notificationMsg = "Testing swap eligibility of: " + shiftAssignmentA.getShiftId()
			    + " against " + shiftAssignmentB.getShiftId();
		    logger.debug(notificationMsg);

		    double percentComplete = Math.round(100.0 * totalTried / totalCombinations);
		    notifyProgress(percentComplete, notificationMsg);

		    // No point in swapping two shifts with the same id
		    if (shiftAssignmentA.getShiftId().equals(shiftAssignmentB.getShiftId())) {
			logger.debug("Don't test swapping shift against itself");
			continue;
		    }

		    // No point in swapping two shifts with the same employee
		    // TODO: Should this return a ResultDto anyway?
		    if (shiftAssignmentA.getEmployeeId().equals(shiftAssignmentB.getEmployeeId())) {
			logger.debug("Both shifts have the same employee, no need to test");
			continue;
		    }

		    // Ignore trying to swap shifts that don't match on skill or
		    // required team
		    // TODO: Should this run through the qualification process
		    // anyway? Or should we just create a ShiftQualificationResult manually?
		    if (!shiftAssignmentA.getEmployee().getTeamIds().contains(shiftAssignmentB.getTeamId())
			    || !shiftAssignmentA.getEmployee().getSkillIds().contains(shiftAssignmentB.getSkillId())) {
			logger.debug("Shift being tested requires skill/team not posssed by incoming employee");
			continue;
		    }

		    if (!shiftAssignmentB.getEmployee().getTeamIds().contains(shiftAssignmentA.getTeamId())
			    || !shiftAssignmentB.getEmployee().getSkillIds().contains(shiftAssignmentA.getSkillId())) {
			logger.debug("Shift being swapped to requires skill/team not posssed by incoming employee");
			continue;
		    }

		    // Get list of shifts being qualified and initialize
		    // QualfiicationScoreHolder using this list
		    Collection<ShiftAssignment> shiftBeingQualfied = Arrays.asList(shiftAssignmentA, shiftAssignmentB);
		    kSession.setGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY, new QualificationScoreHolder(true,
			    shiftBeingQualfied));

		    // Swap the Employees assigned to each shift
		    Employee employeeA = shiftAssignmentA.getEmployee();
		    Employee employeeB = shiftAssignmentB.getEmployee();
		    shiftAssignmentA.setEmployee(employeeB);
		    shiftAssignmentB.setEmployee(employeeA);

		    // Update Working Memory After Swap
		    kSession.update(shiftAssignmentAFactHandle, shiftAssignmentA);
		    kSession.update(shiftAssignmentBFactHandle, shiftAssignmentB);

		    // ATTACK!! Fire the rules in order to get schedule
		    // conflicts
		    kSession.fireAllRules();

		    // Retrieve scoreHolder global containing score and
		    // constraints
		    QualificationScoreHolder scoreHolder = (QualificationScoreHolder) kSession
			    .getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		    ShiftQualificationDto resultDto = convertSwapEligibilityResults(shiftAssignmentB,
			    scoreHolder.getShiftConstraintQualificationMap());

		    if (resultDto.getIsAccepted())
			logger.debug("Shift Assignment is accepted");
		    else
			logger.debug("Shift Assignment rejected due to " + resultDto.getCauses());

		    // Only include reject results if include details flag is
		    // true
		    if (resultDto.getIsAccepted() || (!resultDto.getIsAccepted() && includeResultDetails)) {
			// Add new result to the map
			String currentShiftBeingSwappedId = shiftAssignmentA.getShiftId();

			// The result object should contain the old employee
			// name for clarity
			resultDto.setEmployeeId(employeeB.getEmployeeId());
			resultDto.setEmployeeName(employeeB.getFullName());

			if (shiftSwapResults.containsKey(currentShiftBeingSwappedId)) {
			    shiftSwapResults.get(shiftAssignmentA.getShiftId()).add(resultDto);
			} else {
			    List<ShiftQualificationDto> newResultList = new ArrayList<>();
			    newResultList.add(resultDto);
			    shiftSwapResults.put(currentShiftBeingSwappedId, newResultList);
			}
		    }

		    // Undo the swap before moving on to the next
		    shiftAssignmentA.setEmployee(employeeA);
		    shiftAssignmentB.setEmployee(employeeB);
		    // Update Working Memory After Undoing Swap
		    kSession.update(shiftAssignmentAFactHandle, shiftAssignmentA);
		    kSession.update(shiftAssignmentBFactHandle, shiftAssignmentB);

		}
	    }
	} catch (Exception e) {
	    logger.error("Shift Swap Eligibility Error", e);
	    throw e;
	}
	watch.stop();
	logger.info("Shift Swap Eligibility process finished after " + watch.getTime() + " ms");

	return shiftSwapResults;
    }

    /**
     * Converts each ShiftAssignment to a ShiftQualificationDto object and sets
     * its accepted flag as well as any constraints if present. Combines all
     * constraint causes under the shift being swapped to
     * 
     */
    protected ShiftQualificationDto convertSwapEligibilityResults(ShiftAssignment shiftToBeCheckedAgainst,
	    Map<ShiftAssignment, List<ShiftConstraintDto>> shiftResults) {
	ShiftQualificationDto shiftDto = new ShiftQualificationDto(shiftToBeCheckedAgainst);
	List<ShiftConstraintDto> swapConstraintResults = shiftResults.remove(shiftToBeCheckedAgainst);

	// Add the rest of the causes to 1 list
	for (ShiftAssignment shift : shiftResults.keySet()) {
	    swapConstraintResults.addAll(shiftResults.get(shift));
	}

	if (swapConstraintResults.size() > 0) {
	    shiftDto.setIsAccepted(false);
	    if (includeResultDetails)
		shiftDto.setCauses(swapConstraintResults);
	}

	return shiftDto;
    }

}
